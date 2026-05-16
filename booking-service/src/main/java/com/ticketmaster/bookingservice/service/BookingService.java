package com.ticketmaster.bookingservice.service;

import com.ticketmaster.bookingservice.client.SeatAllocationClient;
import com.ticketmaster.bookingservice.client.PaymentClient;
import com.ticketmaster.bookingservice.dto.request.CheckoutRequest;
import com.ticketmaster.bookingservice.dto.request.FinalizeBookingRequest;
import com.ticketmaster.bookingservice.dto.response.BookingItemResponse;
import com.ticketmaster.bookingservice.dto.response.BookingResponse;
import com.ticketmaster.bookingservice.dto.response.CheckoutResponse;
import com.ticketmaster.bookingservice.dto.response.TicketResponse;
import com.ticketmaster.bookingservice.entity.Booking;
import com.ticketmaster.bookingservice.entity.BookingFulfillmentRequest;
import com.ticketmaster.bookingservice.entity.BookingItem;
import com.ticketmaster.bookingservice.entity.Ticket;
import com.ticketmaster.bookingservice.enums.BookingStatus;
import com.ticketmaster.bookingservice.enums.TicketStatus;
import com.ticketmaster.bookingservice.exception.BadRequestException;
import com.ticketmaster.bookingservice.exception.ResourceNotFoundException;
import com.ticketmaster.bookingservice.repository.BookingFulfillmentRequestRepository;
import com.ticketmaster.bookingservice.repository.BookingItemRepository;
import com.ticketmaster.bookingservice.repository.BookingRepository;
import com.ticketmaster.bookingservice.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final TicketRepository ticketRepository;
    private final BookingFulfillmentRequestRepository fulfillmentRepository;
    private final SeatAllocationClient seatAllocationClient;
    private final PaymentClient paymentClient;

    @Transactional
    public CheckoutResponse checkout(UUID userId, CheckoutRequest request) {
        try {
            log.info("=== CHECKOUT START === userId:{} eventId:{} seats:{}",
                    userId, request.getEventId(), request.getSeatIds());

            String lockIdempotencyKey = "lock-" + userId + "-" + request.getEventId()
                                        + "-" + System.currentTimeMillis();

            log.info("Step 1: Locking seats...");
            Map<String, Object> lockResponse = seatAllocationClient.lockSeats(
                    request.getEventId(), userId, request.getSeatIds(), lockIdempotencyKey);
            log.info("Step 1 DONE");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> lockedSeats =
                    (List<Map<String, Object>>) lockResponse.get("lockedSeats");
            long totalAmountMinor = lockedSeats.stream()
                    .mapToLong(seat -> Long.parseLong(seat.get("priceCents").toString()))
                    .sum();

            log.info("Step 2: Creating booking, total:{}", totalAmountMinor);
            Booking booking = Booking.builder()
                    .userId(userId)
                    .eventId(request.getEventId())
                    .totalAmountMinor(totalAmountMinor)
                    .status(BookingStatus.PENDING)
                    .build();
            booking = bookingRepository.save(booking);
            log.info("Step 2 DONE: BookingId:{}", booking.getId());

            final Booking savedBooking = booking;
            List<BookingItem> items = lockedSeats.stream().map(seat -> BookingItem.builder()
                    .booking(savedBooking)
                    .eventSeatId(UUID.fromString(seat.get("id").toString()))
                    .sectionId(UUID.fromString(seat.get("sectionId").toString()))
                    .seatCode(seat.get("seatCode").toString())
                    .priceMinor(Long.parseLong(seat.get("priceCents").toString()))
                    .build()).collect(Collectors.toList());
            bookingItemRepository.saveAll(items);
            log.info("Step 2b DONE: {} items saved", items.size());

            log.info("Step 3: Initiating payment...");
            String paymentIdempotencyKey = "payment-" + booking.getId();
            Map<String, Object> paymentResponse = paymentClient.initiatePayment(
                    userId, booking.getId(), request.getEventId(),
                    totalAmountMinor, "INR", paymentIdempotencyKey);
            log.info("Step 3 DONE: paymentResponse:{}", paymentResponse);

            UUID paymentId = UUID.fromString(paymentResponse.get("paymentId").toString());
            String providerOrderId = paymentResponse.get("providerOrderId").toString();

            booking.setPaymentId(paymentId);
            bookingRepository.save(booking);

            log.info("=== CHECKOUT COMPLETE === bookingId:{} paymentId:{} providerOrderId:{}",
                    booking.getId(), paymentId, providerOrderId);

            return CheckoutResponse.builder()
                    .bookingId(booking.getId())
                    .paymentId(paymentId)
                    .providerOrderId(providerOrderId)
                    .totalAmountMinor(totalAmountMinor)
                    .currency("INR")
                    .status("PENDING_PAYMENT")
                    .build();

        } catch (Exception e) {
            log.error("=== CHECKOUT FAILED === {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void finalizeBooking(FinalizeBookingRequest request) {
        log.info("=== FINALIZE START === bookingId:{} paymentId:{} status:{}",
                request.getBookingId(), request.getPaymentId(), request.getPaymentStatus());

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found: " + request.getBookingId()));

        if (booking.getStatus() == BookingStatus.CONFIRMED
                || booking.getStatus() == BookingStatus.FAILED) {
            log.info("Booking {} already finalized with status {}",
                    booking.getId(), booking.getStatus());
            return;
        }

        if (fulfillmentRepository.existsByPaymentId(request.getPaymentId())) {
            log.info("Retrying finalize for paymentId:{} booking:{} status:{}",
                    request.getPaymentId(), booking.getId(), booking.getStatus());
        } else {
            fulfillmentRepository.save(BookingFulfillmentRequest.builder()
                    .paymentId(request.getPaymentId())
                    .bookingId(request.getBookingId())
                    .status("PROCESSING")
                    .build());
        }

        if ("SUCCESS".equals(request.getPaymentStatus())) {
            List<BookingItem> items = bookingItemRepository.findByBookingId(booking.getId());
            List<UUID> seatIds = items.stream()
                    .map(BookingItem::getEventSeatId).collect(Collectors.toList());

            seatAllocationClient.confirmSeats(
                    booking.getEventId(), booking.getId(), seatIds,
                    "confirm-" + booking.getId());

            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            List<Ticket> tickets = items.stream().map(item -> Ticket.builder()
                    .booking(booking)
                    .bookingItem(item)
                    .ticketNumber("TKT-" + UUID.randomUUID().toString()
                            .substring(0, 8).toUpperCase())
                    .ticketCode(UUID.randomUUID().toString()
                            .replace("-", "").toUpperCase())
                    .status(TicketStatus.ISSUED)
                    .build()).collect(Collectors.toList());
            ticketRepository.saveAll(tickets);

            fulfillmentRepository.findByPaymentId(request.getPaymentId())
                    .ifPresent(f -> {
                        f.setStatus("COMPLETED");
                        fulfillmentRepository.save(f);
                    });

            log.info("=== FINALIZE DONE === {} tickets issued for booking:{}",
                    tickets.size(), booking.getId());
        } else {
            List<BookingItem> items = bookingItemRepository.findByBookingId(booking.getId());
            List<UUID> seatIds = items.stream()
                    .map(BookingItem::getEventSeatId).collect(Collectors.toList());

            seatAllocationClient.releaseSeats(
                    booking.getEventId(), seatIds, "release-" + booking.getId());

            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);

            fulfillmentRepository.findByPaymentId(request.getPaymentId())
                    .ifPresent(f -> {
                        f.setStatus("FAILED");
                        fulfillmentRepository.save(f);
                    });

            log.info("=== FINALIZE DONE === booking FAILED, seats released");
        }
    }

    public List<BookingResponse> getUserBookings(UUID userId) {
        log.info("Getting bookings for userId:{}", userId);
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    public List<TicketResponse> getTickets(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found: " + bookingId));
        if (!booking.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Booking not found: " + bookingId);
        }
        return ticketRepository.findByBookingId(bookingId)
                .stream().map(this::mapToTicketResponse).collect(Collectors.toList());
    }

    @Transactional
    public TicketResponse scanTicket(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found: " + ticketCode));

        if (ticket.getStatus() == TicketStatus.SCANNED) {
            throw new BadRequestException("Ticket already scanned");
        }
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new BadRequestException("Ticket is cancelled");
        }
        ticket.setStatus(TicketStatus.SCANNED);
        ticketRepository.save(ticket);
        return mapToTicketResponse(ticket);
    }

    private BookingResponse mapToBookingResponse(Booking b) {
        List<BookingItem> items = bookingItemRepository.findByBookingId(b.getId());
        return BookingResponse.builder()
                .id(b.getId()).userId(b.getUserId())
                .eventId(b.getEventId()).paymentId(b.getPaymentId())
                .status(b.getStatus()).totalAmountMinor(b.getTotalAmountMinor())
                .currency(b.getCurrency()).createdAt(b.getCreatedAt())
                .items(items.stream().map(this::mapToItemResponse).collect(Collectors.toList()))
                .build();
    }

    private BookingItemResponse mapToItemResponse(BookingItem i) {
        return BookingItemResponse.builder()
                .id(i.getId()).eventSeatId(i.getEventSeatId())
                .sectionId(i.getSectionId()).seatCode(i.getSeatCode())
                .priceMinor(i.getPriceMinor()).build();
    }

    private TicketResponse mapToTicketResponse(Ticket t) {
        return TicketResponse.builder()
                .id(t.getId()).bookingId(t.getBooking().getId())
                .bookingItemId(t.getBookingItem().getId())
                .seatCode(t.getBookingItem().getSeatCode())
                .ticketNumber(t.getTicketNumber())
                .ticketCode(t.getTicketCode())
                .status(t.getStatus()).createdAt(t.getCreatedAt())
                .build();
    }
}
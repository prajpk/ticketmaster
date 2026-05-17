# 🎫 Ticketmaster — Event Ticketing Backend

A production-grade event ticketing backend inspired by Ticketmaster, built as a **Java Spring Boot microservices** system. 
Supports user authentication, venue and event management, seat allocation with locking, payment processing, and ticket issuance.

## Tech Stack

| Category       | Technology                  |
|----------------|-----------------------------|
| Language       | Java 17                     |
| Framework      | Spring Boot 3.2.4           |
| Database       | PostgreSQL 15               |
| Messaging      | Apache Kafka                |
| Security       | JWT (jjwt 0.12.5), BCrypt   |
| ORM            | Spring Data JPA / Hibernate |
| Build          | Maven                       |
| Infrastructure | Docker Desktop              |

## Services

| Service                  | Port | Database      | Description                             |
|--------------------------|------|---------------|-----------------------------------------|
| user-service             | 8080 | auth_db       | Registration, login, JWT auth           |
| event-management-service | 8081 | event_db      | Venues, events, pricing, Kafka producer |
| booking-service          | 8082 | booking_db    | Checkout, tickets, fulfillment          |
| seats-allocation-service | 8083 | allocation_db | Seat inventory, locking, Kafka consumer |
| payment-service          | 8084 | payment_db    | Payment initiation, webhook processing  |

## Prerequisites

Install the following before running the project:

| Tool           | Version | Download                                       |
|----------------|---------|------------------------------------------------|
| Java           | 17+     | https://adoptium.net                           |
| Maven          | 3.9+    | https://maven.apache.org                       |
| Docker Desktop | Latest  | https://www.docker.com/products/docker-desktop |
| Postman        | Latest  | https://www.postman.com/downloads              |
| Git            | Latest  | https://git-scm.com                            |

**Docker Desktop must be running** before executing any docker commands.

## Getting Started

### Step 1 — Clone the Repository

```bash
git clone https://github.com/prajpk/ticketmaster.git
cd ticketmaster
```

### Step 2 — Start Infrastructure (Docker)

```bash
# Start PostgreSQL, Kafka, Zookeeper
docker-compose up -d

# Verify all containers are running
docker ps
```

You should see these 3 containers running:
```
ticketmaster-postgres
ticketmaster-kafka
ticketmaster-zookeeper
```

### Step 3 — Create Databases

```bash
docker exec -it ticketmaster-postgres psql -U postgres -c "CREATE DATABASE auth_db;"
docker exec -it ticketmaster-postgres psql -U postgres -c "CREATE DATABASE event_db;"
docker exec -it ticketmaster-postgres psql -U postgres -c "CREATE DATABASE allocation_db;"
docker exec -it ticketmaster-postgres psql -U postgres -c "CREATE DATABASE booking_db;"
docker exec -it ticketmaster-postgres psql -U postgres -c "CREATE DATABASE payment_db;"
```

> Spring Boot will auto-create all tables on first run via `ddl-auto: update`

### Step 4 — Start All Services

Open **5 separate terminals** and run one service per terminal. 
Start them in this order:

**Terminal 1 — user-service**
```bash
cd user-service
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Duser.timezone=UTC"
```

**Terminal 2 — event-management-service**
```bash
cd event-management-service
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Duser.timezone=UTC"
```

**Terminal 3 — seats-allocation-service**
```bash
cd seats-allocation-service
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Duser.timezone=UTC"
```

**Terminal 4 — payment-service**
```bash
cd payment-service
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Duser.timezone=UTC"
```

**Terminal 5 — booking-service**
```bash
cd booking-service
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Duser.timezone=UTC"
```

Or

Open Command prompt from root folder and run following commands

```bash
start "user-service" cmd /k "D: && cd user-service && mvn clean package -DskipTests && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Duser.timezone=UTC""

start "event-management-service" cmd /k "d: && cd event-management-service && mvn clean package -DskipTests && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Duser.timezone=UTC""

start "seats-allocation-service" cmd /k "D: && cd seats-allocation-service && mvn clean package -DskipTests && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Duser.timezone=UTC""

start "payment-service" cmd /k "D: && cd payment-service && mvn clean package -DskipTests && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Duser.timezone=UTC""

start "booking-service" cmd /k "D: && cd booking-service && mvn clean package -DskipTests && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Duser.timezone=UTC""
```

This will open 5 new terminals in one go, with header, so it will be easier to find errors.

Wait for each service to print `Started ...Application in X seconds`.

---

## 🐳 Docker Commands Reference

```bash
# Start all containers
docker-compose up -d

# Stop all containers (keeps data)
docker-compose down

# Stop and delete all data (fresh start)
docker-compose down -v

# View running containers
docker ps

# View logs for a container
docker logs ticketmaster-postgres
docker logs ticketmaster-kafka

# Connect to PostgreSQL
docker exec -it ticketmaster-postgres psql -U postgres

# List all databases
docker exec -it ticketmaster-postgres psql -U postgres -c "\l"

# Connect to specific database
docker exec -it ticketmaster-postgres psql -U postgres -d auth_db

# View tables in a database
docker exec -it ticketmaster-postgres psql -U postgres -d booking_db -c "\dt"

# Restart a single container
docker restart ticketmaster-kafka

# Check Kafka topics
docker exec -it ticketmaster-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

---

## 🔄 Complete API Flow (Run in Sequence)

Import the Postman collection `ticketmaster-postman-collection.json` for auto-chained variables.

---

### 👤 PHASE 1 — User Service (:8080)

#### 1. Register User
#### 2. Login
#### 3. Get Profile

### 🏟 PHASE 2 — Event Management Service (:8081)

#### 4. Create Venue
#### 5. Create Section
#### 6. Generate Seats
#### 7. Create Event
#### 8. Configure Pricing
#### 9. Initialize Inventory
#### 10. Publish Event
#### 11. Browse Public Events (no auth needed)

### 💺 PHASE 3 — Seats Allocation Service (:8083)

#### 12. Get Available Seats
#### 13. Checkout

### 💳 PHASE 5 — Payment Service (:8084)

#### 14. Simulate Razorpay Webhook
#### 15. Get Payment Status

### 🎟 PHASE 6 — Booking Confirmed + Tickets

#### 16. Get My Bookings
#### 17. Get Tickets
#### 18. Scan Ticket (Gate Agent)
#### 19. Scan Same Ticket Again (should fail)

## 🗄 Database Schema

### user-service (auth_db)
```
users             → id, email, passwordHash, fullName, phone, role, status
refresh_tokens    → id, userId, tokenHash, expiresAt, revoked
```

### event-management-service (event_db)
```
venues                → id, name, city, address, state, country, capacity
venue_sections        → id, venueId, name, sortOrder
venue_seats           → id, venueId, sectionId, seatCode, rowLabel, seatNumber
events                → id, organiserId, venueId, title, category, status, startsAt, endsAt
event_section_pricing → id, eventId, sectionId, priceCents, currency
event_seats           → id, eventId, venueSeatId
```

### seats-allocation-service (allocation_db)
```
event_inventory_context → id, eventId, venueId, eventTitle, startsAt, status
event_seats             → id, eventId, venueSeatId, sectionId, seatCode, status, lockedBy, bookingId
allocation_idempotency  → id, eventId, idempotencyKey, operation
```

### booking-service (booking_db)
```
bookings                    → id, userId, paymentId, eventId, status, totalAmountMinor
booking_items               → id, bookingId, eventSeatId, sectionId, seatCode, priceMinor
tickets                     → id, bookingId, bookingItemId, ticketNumber, ticketCode, status
booking_fulfillment_requests → id, paymentId, bookingId, status
```

### payment-service (payment_db)
```
payments              → id, userId, bookingId, eventId, amountMinor, provider, providerOrderId, status
payment_idempotency   → id, userId, idempotencyKey, paymentId
processed_webhooks    → id, provider, providerEventId, eventType, processedAt
```

## 🔐 Security

- All services share the same JWT secret key
- JWT tokens expire after **15 minutes** (access) and **7 days** (refresh)
- Passwords stored as **BCrypt** hashes
- Refresh tokens stored as **SHA-256** hashes
- Internal service-to-service calls use no auth (internal network only)
- Seat locks expire after **10 minutes**

## ⚙️ Configuration

All services share these key config values in `application.yml`:

```yaml
jwt:
  secret: "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
  access-token-expiration: 900000    # 15 minutes
  refresh-token-expiration: 604800000 # 7 days
```

---

## 🔁 Kafka

| Topic                 | Producer                 | Consumer                 |
|-----------------------|--------------------------|--------------------------|
| event-inventory-topic | event-management-service | seats-allocation-service |

Message flow:
```
Organiser publishes event
→ event-management-service sends EventInventoryMessage to Kafka
→ seats-allocation-service consumes message
→ Creates AllocationEventSeat records for every seat
→ Seats are now available for booking
```

## 🚨 Troubleshooting

| Error                                         | Cause                         | Fix                                              |
|-----------------------------------------------|-------------------------------|--------------------------------------------------|
| `Connection refused :5432`                    | PostgreSQL not running        | `docker-compose up -d`                           |
| `Connection refused :9092`                    | Kafka not running             | `docker-compose up -d`                           |
| `FATAL: invalid value for parameter TimeZone` | System timezone issue         | Add `-Duser.timezone=UTC` to run command         |
| `Unresolved compilation: parserBuilder()`     | Wrong JWT API                 | Use `Jwts.parser()` not `Jwts.parserBuilder()`   |
| `403 Forbidden`                               | Missing/expired JWT token     | Re-login and get fresh token                     |
| `409 Seats not available`                     | Seats already locked/booked   | Call GET /seats to get fresh available seat IDs  |
| `Seat not in LOCKED state`                    | Lock expired (10 min timeout) | Do checkout and webhook within 10 minutes        |
| `Kafka consumer not receiving`                | Type header mismatch          | Use `StringDeserializer` and parse JSON manually |
| `Tables not created`                          | Database doesn't exist        | Create databases manually (Step 3 above)         |

## 📁 Project Structure

```
ticketmaster/
├── docker-compose.yml
├── init-db.sql
├── README.md
├── ticketmaster-postman-collection.json
├── user-service/
│   ├── pom.xml
│   └── src/main/java/com/ticketmaster/userservice/
│       ├── config/        (SecurityConfig, JwtAuthFilter)
│       ├── controller/    (AuthController, ProfileController, AdminController)
│       ├── dto/           (request/, response/)
│       ├── entity/        (User, RefreshToken)
│       ├── enums/         (UserRole, UserStatus)
│       ├── exception/     (GlobalExceptionHandler)
│       ├── repository/    (UserRepository, RefreshTokenRepository)
│       ├── security/      (JwtService, CustomUserDetailsService)
│       └── service/       (AuthService, UserService)
├── event-management-service/
│   ├── pom.xml
│   └── src/main/java/com/ticketmaster/eventservice/
│       ├── config/        (SecurityConfig, JwtAuthFilter)
│       ├── controller/    (VenueController, EventController)
│       ├── dto/           (request/, response/)
│       ├── entity/        (Venue, VenueSection, VenueSeat, Event, EventSectionPricing, EventSeat)
│       ├── enums/         (EventStatus)
│       ├── exception/     (GlobalExceptionHandler)
│       ├── kafka/         (EventInventoryMessage, EventInventoryProducer)
│       ├── repository/
│       └── service/       (VenueService, EventService)
├── seats-allocation-service/
│   ├── pom.xml
│   └── src/main/java/com/ticketmaster/allocationservice/
│       ├── config/        (SecurityConfig, JwtAuthFilter, SchedulingConfig)
│       ├── controller/    (SeatController)
│       ├── dto/           (request/, response/)
│       ├── entity/        (EventInventoryContext, AllocationEventSeat, AllocationIdempotency)
│       ├── enums/         (SeatStatus)
│       ├── exception/     (GlobalExceptionHandler)
│       ├── kafka/         (EventInventoryMessage, EventInventoryConsumer)
│       ├── repository/
│       └── service/       (SeatAllocationService)
├── booking-service/
│   ├── pom.xml
│   └── src/main/java/com/ticketmaster/bookingservice/
│       ├── client/        (SeatAllocationClient, PaymentClient)
│       ├── config/        (SecurityConfig, JwtAuthFilter, RestTemplateConfig)
│       ├── controller/    (BookingController)
│       ├── dto/           (request/, response/)
│       ├── entity/        (Booking, BookingItem, Ticket, BookingFulfillmentRequest)
│       ├── enums/         (BookingStatus, TicketStatus)
│       ├── exception/     (GlobalExceptionHandler)
│       ├── repository/
│       └── service/       (BookingService)
└── payment-service/
    ├── pom.xml
    └── src/main/java/com/ticketmaster/paymentservice/
        ├── client/        (BookingClient)
        ├── config/        (SecurityConfig, JwtAuthFilter, RestTemplateConfig)
        ├── controller/    (PaymentController)
        ├── dto/           (request/, response/)
        ├── entity/        (Payment, PaymentIdempotency, ProcessedWebhook)
        ├── enums/         (PaymentStatus)
        ├── exception/     (GlobalExceptionHandler)
        ├── repository/
        └── service/       (PaymentService)
```

## 👤 Author

**Prajakta Bhabal**
Master of Science in Computer Science
Scaler Neovarsity — Woolf
May 2026

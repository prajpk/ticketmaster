package com.ticketmaster.userservice.dto.request;

import com.ticketmaster.userservice.enums.UserRole;
import com.ticketmaster.userservice.enums.UserStatus;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private UserRole role;
    private UserStatus status;
}

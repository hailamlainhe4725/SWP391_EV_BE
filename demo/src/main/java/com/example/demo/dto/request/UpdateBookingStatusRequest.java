package com.example.demo.dto.request;

import com.example.demo.enums.BookingStatus;

import lombok.Data;

@Data
public class UpdateBookingStatusRequest {
    private BookingStatus bookingStatus;
}

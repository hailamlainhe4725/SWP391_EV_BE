package com.example.demo.controller;

import com.example.demo.dto.request.CreateBookingRequest;
import com.example.demo.dto.response.BookingResponse;
import com.example.demo.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // ===== USER API =====

    // Tạo booking mới (user đặt lịch)
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/createBooking")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest req) {
        BookingResponse response = bookingService.createBooking(req);
        return ResponseEntity.ok(response);
    }

    // Xem các booking của chính user
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication auth) {
        String email = auth.getName();
        List<BookingResponse> res = bookingService.getBookingsByEmail(email);
        return ResponseEntity.ok(res);
    }

    // Hủy booking (nếu chưa bắt đầu)
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok("Booking cancelled successfully");
    }

    // ===== STAFF API =====

    // Staff xem toàn bộ booking
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/viewAllBooking")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // Staff thay đổi trạng thái booking (Pending -> Confirmed / Cancelled /
    // Completed)
    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{id}/status")
    public ResponseEntity<BookingResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, status));
    }
}

package com.example.demo.controller;

import com.example.demo.dto.request.CreateBookingRequest;
import com.example.demo.dto.request.UpdateStatusBookingRequest;
import com.example.demo.dto.response.BookingResponse;
import com.example.demo.dto.response.BookingVehicleResponse;
import com.example.demo.dto.response.DailyDisputeWindowResponse;
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
    public ResponseEntity<BookingResponse> createBooking(Authentication authentication,@Valid @RequestBody CreateBookingRequest req) {
        BookingResponse response = bookingService.createBooking(authentication,req);
        return ResponseEntity.ok(response);
    }


  

    // Xem các booking của chính user
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public ResponseEntity<List<BookingVehicleResponse>> getMyBookings(Authentication auth) {
        List<BookingVehicleResponse> res = bookingService.getMyBookings(auth);
        return ResponseEntity.ok(res);
    }

    
    //phu offer them tim booking theo id xe
        @GetMapping("/byVehicle/{vehicleId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BookingVehicleResponse>> getBookingsByVehicle(
            Authentication auth,
            @PathVariable Long vehicleId) {

        List<BookingVehicleResponse> bookings = bookingService.getBookingsByVehicle(auth, vehicleId);
        return ResponseEntity.ok(bookings);
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
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @GetMapping("/viewAllBooking")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // Staff thay đổi trạng thái booking (Pending -> Confirmed / Cancelled /
    // Completed)
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PutMapping("updateStatus")
    public ResponseEntity<BookingResponse> updateStatus(@RequestBody UpdateStatusBookingRequest req) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(req));
    }

    //xem duoc cac ngay co tranh chap va khoang thoi gian tranh chap
    @PreAuthorize("hasAnyRole('STAFF','USER','ADMIN')")
    @GetMapping("/vehicles/{vehicleId}/dispute-windows")
public ResponseEntity<List<DailyDisputeWindowResponse>> getDisputeWindows(
        @PathVariable Long vehicleId,
        @RequestParam int year,
        @RequestParam int month
) {
    return ResponseEntity.ok(bookingService.getDisputeWindowsForMonth(vehicleId, year, month));
}

    
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/vehicle/{vehicleId}/schedule")
public ResponseEntity<List<BookingResponse>> getVehicleSchedule(@PathVariable Long vehicleId) {
    List<BookingResponse> schedules = bookingService.getScheduleByVehicle(vehicleId);
    return ResponseEntity.ok(schedules);
}

//theo doi tranh chap theo xe 
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
@GetMapping("/vehicle/{vehicleId}/disputes")
public ResponseEntity<List<BookingResponse>> getDisputedBookingsByVehicle(@PathVariable Long vehicleId) {
    List<BookingResponse> responses = bookingService.getDisputedBookingsByVehicle(vehicleId);
    return ResponseEntity.ok(responses);
}

}

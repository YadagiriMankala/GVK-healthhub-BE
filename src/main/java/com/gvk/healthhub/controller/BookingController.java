package com.gvk.healthhub.controller;

import com.gvk.healthhub.dto.request.BookingRequest;
import com.gvk.healthhub.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookAppointment")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }


    @PostMapping({"", "/"})
    public ResponseEntity<String> bookAppointment(@Valid @RequestBody BookingRequest request){

        bookingService.bookAppointment(request);
        return ResponseEntity.ok("Appointment booked Successfully");
    }


}

package com.ticketflow.payment.controller;

import com.ticketflow.common.dto.ApiResponse;
import com.ticketflow.common.dto.PaymentRequest;
import com.ticketflow.common.dto.PaymentResponse;
import com.ticketflow.payment.model.PaymentTransaction;
import com.ticketflow.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        if (response.getStatus().name().equals("SUCCESS")) {
            return ResponseEntity.ok(ApiResponse.success(response, "Payment processed successfully"));
        } else {
            return ResponseEntity.status(400).body(ApiResponse.failure("Payment failed: " + response.getFailureReason()));
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<PaymentTransaction>> getPayment(@PathVariable String transactionId) {
        return paymentService.getPaymentById(transactionId)
                .map(tx -> ResponseEntity.ok(ApiResponse.success(tx, "Transaction details found")))
                .orElse(ResponseEntity.status(404).body(ApiResponse.failure("Transaction not found: " + transactionId)));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentTransaction>> getPaymentByBooking(@PathVariable String bookingId) {
        return paymentService.getPaymentByBookingId(bookingId)
                .map(tx -> ResponseEntity.ok(ApiResponse.success(tx, "Transaction found")))
                .orElse(ResponseEntity.status(404).body(ApiResponse.failure("No transaction found for booking: " + bookingId)));
    }
}


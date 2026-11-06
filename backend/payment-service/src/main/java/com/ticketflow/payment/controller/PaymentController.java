package com.ticketflow.payment.controller;

import com.ticketflow.common.dto.ApiResponse;
import com.ticketflow.common.dto.PaymentRequest;
import com.ticketflow.common.dto.PaymentResponse;
import com.ticketflow.common.dto.RefundRequest;
import com.ticketflow.common.dto.RefundResponse;
import com.ticketflow.payment.model.PaymentTransaction;
import com.ticketflow.payment.model.RefundLog;
import com.ticketflow.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            return ResponseEntity.status(400)
                    .body(ApiResponse.failure("Payment failed: " + response.getFailureReason()));
        }
    }

    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(@RequestBody RefundRequest request) {
        try {
            RefundResponse response = paymentService.processRefund(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Refund processed successfully"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("Refund processing error: " + e.getMessage()));
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<PaymentTransaction>> getPayment(@PathVariable String transactionId) {
        return paymentService.getPaymentById(transactionId)
                .map(tx -> ResponseEntity.ok(ApiResponse.success(tx, "Transaction details found")))
                .orElse(ResponseEntity.status(404)
                        .body(ApiResponse.failure("Transaction not found: " + transactionId)));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentTransaction>> getPaymentByBooking(@PathVariable String bookingId) {
        return paymentService.getPaymentByBookingId(bookingId)
                .map(tx -> ResponseEntity.ok(ApiResponse.success(tx, "Transaction found")))
                .orElse(ResponseEntity.status(404)
                        .body(ApiResponse.failure("No transaction found for booking: " + bookingId)));
    }

    @GetMapping("/{transactionId}/refunds")
    public ResponseEntity<ApiResponse<List<RefundLog>>> getRefundsByTransaction(@PathVariable String transactionId) {
        List<RefundLog> refunds = paymentService.getRefundsByTransactionId(transactionId);
        return ResponseEntity.ok(ApiResponse.success(refunds, "Refunds retrieved"));
    }

    @GetMapping("/booking/{bookingId}/refunds")
    public ResponseEntity<ApiResponse<List<RefundLog>>> getRefundsByBooking(@PathVariable String bookingId) {
        List<RefundLog> refunds = paymentService.getRefundsByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.success(refunds, "Refunds retrieved"));
    }
}

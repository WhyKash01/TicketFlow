package com.ticketflow.booking.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ticketflow.common.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_user", columnList = "userId"),
        @Index(name = "idx_booking_ref", columnList = "bookingReference", unique = true),
        @Index(name = "idx_booking_show", columnList = "showId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @Column(length = 36)
    private String bookingId;

    @Column(unique = true, nullable = false, length = 16)
    private String bookingReference; // e.g. "TF-2026-X89K"

    @Column(nullable = false, length = 100)
    private String userId;

    @Column(length = 150)
    private String userEmail;

    @Column(length = 20)
    private String userPhone;

    @Column(nullable = false)
    private Long showId;

    @Column(nullable = false, length = 200)
    private String movieTitle;

    @Column(nullable = false, length = 150)
    private String theatreName;

    @Column(nullable = false, length = 100)
    private String screenName;

    @Column(nullable = false, length = 50)
    private String showTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "booking_seat_numbers", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "seat_number")
    @Builder.Default
    private List<String> seatNumbers = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "booking_seat_ids", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "seat_id")
    @Builder.Default
    private List<Long> seatIds = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<BookingItem> items = new ArrayList<>();

    @Column(precision = 8, scale = 2)
    private BigDecimal basePrice;

    @Column(precision = 8, scale = 2)
    private BigDecimal convenienceFee;

    @Column(precision = 8, scale = 2)
    private BigDecimal taxes;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private BookingStatus status;

    @Column(length = 100)
    private String paymentTransactionId;

    @Column(length = 1000)
    private String qrCodePayload;

    private LocalDateTime expiresAt;

    @Version
    @Builder.Default
    private Long version = 0L;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void addItem(BookingItem item) {
        if (items == null) items = new ArrayList<>();
        items.add(item);
        item.setBooking(this);
    }
}

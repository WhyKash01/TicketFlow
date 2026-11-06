package com.ticketflow.catalog.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ticketflow.common.enums.SeatTier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "show_price_tiers", indexes = {
        @Index(name = "idx_show_price_tier", columnList = "show_id, tier")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowPriceTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    @JsonBackReference
    private Show show;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatTier tier;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;
}


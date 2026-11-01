package com.ticketflow.catalog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "screens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g. "Audi 1", "IMAX Laser"

    private String screenFormat; // IMAX 3D, Dolby Atmos, 4DX, Regular 2D
    private Integer totalRows;   // e.g. 10
    private Integer seatsPerRow; // e.g. 15
    private Integer totalCapacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theatre_id", nullable = false)
    @JsonIgnore
    private Theatre theatre;
}


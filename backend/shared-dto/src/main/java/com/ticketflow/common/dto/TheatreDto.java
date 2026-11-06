package com.ticketflow.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheatreDto {
    private Long id;
    private String name;
    private String brand;
    private String city;
    private String location;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer totalScreens;
    private Boolean isActive;
}


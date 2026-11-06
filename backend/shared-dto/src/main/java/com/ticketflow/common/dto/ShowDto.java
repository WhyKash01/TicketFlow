package com.ticketflow.common.dto;

import com.ticketflow.common.enums.ScreenFormat;
import com.ticketflow.common.enums.ShowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowDto {
    private Long id;
    private Long movieId;
    private Long theatreId;
    private Long screenId;
    private String movieTitle;
    private String theatreName;
    private String screenName;
    private String city;
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String soundExperience;
    private ScreenFormat screenFormat;
    private BigDecimal basePrice;
    private ShowStatus status;
}


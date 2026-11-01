package com.ticketflow.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContiguousSeatResponse {
    private boolean found;
    private Long showId;
    private String rowName;
    private List<SeatDto> allocatedSeats;
    private String algorithmUsed; // e.g. "SegmentTree-ContiguousAllocation"
    private String message;
}


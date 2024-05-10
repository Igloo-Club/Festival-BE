package com.iglooclub.nungil.dto;

import com.iglooclub.nungil.domain.enums.AvailableTime;
import com.iglooclub.nungil.domain.enums.Marker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleUpdateRequest {
    private List<AvailableTime> availableTimeList;


    private List<Marker> markerList;
}

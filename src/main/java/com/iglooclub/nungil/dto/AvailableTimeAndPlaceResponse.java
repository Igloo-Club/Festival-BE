package com.iglooclub.nungil.dto;

import com.iglooclub.nungil.domain.enums.AvailableTime;
import com.iglooclub.nungil.domain.enums.Marker;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvailableTimeAndPlaceResponse {


    private String time;

    private List<AvailableMarker> marker;


    //=== 생성 메서드 ===//
    public static AvailableTimeAndPlaceResponse create(
                                                       AvailableTime availableTime,
                                                       List<Marker> markerList) {

        AvailableTimeAndPlaceResponse response = new AvailableTimeAndPlaceResponse();

        response.time = availableTime.getTitle();
        response.marker = markerList.stream().map(AvailableMarker::create).collect(Collectors.toList());

        return response;
    }
}

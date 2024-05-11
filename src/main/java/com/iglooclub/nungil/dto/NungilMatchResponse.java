package com.iglooclub.nungil.dto;

import com.iglooclub.nungil.domain.Nungil;
import com.iglooclub.nungil.domain.enums.AvailableTime;
import com.iglooclub.nungil.domain.enums.Marker;
import lombok.*;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NungilMatchResponse {

    @Nullable
    private String time;

    private List<AvailableMarker> marker;


    @Nullable
    private Long chatRoomId;

    public static NungilMatchResponse create(Nungil nungil, Long chatRoomId) {

        NungilMatchResponse response = new NungilMatchResponse();

        AvailableTime matchedAvailableTime = nungil.getMatchedAvailableTime();
        response.time = (matchedAvailableTime != null) ? matchedAvailableTime.getTitle() : null;

        List<Marker> matchedMarkers = nungil.getMatchedMarkers();
        response.marker = matchedMarkers.stream().map(AvailableMarker::create).collect(Collectors.toList());

        response.chatRoomId = chatRoomId;

        return response;
    }
}

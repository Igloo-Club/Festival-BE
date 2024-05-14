package com.iglooclub.nungil.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Marker {

    FOUNTAIN("분수대 정중앙", "서울 동작구 상도로 369", 37.4963200, 126.957440),
    HORSE("백마상 앞", "서울 동작구 상도로 369", 37.4965900, 126.957440),
    PYRAMID("피라미드 아래 쿱 스켓 앞", "서울 동작구 상도로 369", 37.4961000, 126.958050),
    FOREST("숭실 포레스트 입구", "서울 동작구 상도로 369", 37.4971600, 126.958300),
    HYUNGNAM("김형남 동상 앞", "서울 동작구 상도로 369", 37.4960500, 126.956500),
    HANGYUNGJIK("한경직 동상 앞", "서울 동작구 상도로 369", 37.4959500, 126.957500),
    ;

    // 장소명
    private final String title;
    // 도로명 주소. 없다면 지번 주소
    private final String address;
    private final Double latitude;
    private final Double longitude;
}

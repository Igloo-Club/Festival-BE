package com.iglooclub.nungil.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AvailableTime {
    T1100("11:00"),
    T1200("12:00"),
    T1300("13:00"),
    T1400("14:00"),
    T1500("15:00"),
    T1600("16:00"),
    T1700("17:00"),
    T1800("18:00"),
    T1900("19:00"),
    T2000("20:00"),
    T2100("21:00"),
    T2200("22:00")
    ;

    private final String title;
}

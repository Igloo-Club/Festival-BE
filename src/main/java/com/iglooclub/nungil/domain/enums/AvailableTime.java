package com.iglooclub.nungil.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AvailableTime {
    T1100("11:00", 11),
    T1200("12:00", 12),
    T1300("13:00", 13),
    T1400("14:00", 14),
    T1500("15:00", 15),
    T1600("16:00", 16),
    T1700("17:00", 17),
    T1800("18:00", 18),
    T1900("19:00", 19),
    T2000("20:00", 20),
    T2100("21:00", 21),
    T2200("22:00", 22)
    ;

    private final String title;

    private final Integer value;
}

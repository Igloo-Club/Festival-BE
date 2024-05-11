package com.iglooclub.nungil.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iglooclub.nungil.domain.ConsentPolicy;
import com.iglooclub.nungil.domain.Member;
import com.iglooclub.nungil.domain.enums.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDetailResponse {
    private String nickname;

    private Sex sex;

    @JsonFormat(pattern = "yyyyMMdd")
    private LocalDate birthdate;

    private AnimalFace animalFace;

    private String job;

    private Mbti mbti;

    private List<FaceDepiction> faceDepictionList;

    private List<PersonalityDepiction> personalityDepictionList;

    private String description;

    private List<Marker> markerList;

    private List<AvailableTime> availableTimeList;

    private List<Hobby> hobbyList;


    private Boolean agreeMarketing;

    // == 생성 메서드 == //
    public static MemberDetailResponse create(Member member) {
        List<FaceDepiction> faceDepictionList = member.getFaceDepictionList();
        List<PersonalityDepiction> personalityDepictionList = member.getPersonalityDepictionList();
        List<Marker> markerList = member.getMarkerList();
        List<AvailableTime> availableTimeList = member.getAvailableTimeList();
        List<Hobby> hobbyList = member.getHobbyList();

        MemberDetailResponse response = new MemberDetailResponse();

        response.nickname = member.getNickname();
        response.sex = member.getSex();
        response.birthdate = member.getBirthdate();
        response.animalFace = member.getAnimalFace();
        response.job = member.getJob();
        response.mbti = member.getMbti();
        response.faceDepictionList = faceDepictionList;
        response.personalityDepictionList = personalityDepictionList;
        response.description = member.getDescription();
        response.markerList = markerList;
        response.availableTimeList = availableTimeList;
        response.hobbyList = hobbyList;

        ConsentPolicy consentPolicy = member.getConsentPolicy();
        response.agreeMarketing = consentPolicy != null && consentPolicy.getAgreeMarketing();

        return response;
    }
}

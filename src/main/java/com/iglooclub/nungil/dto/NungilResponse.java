package com.iglooclub.nungil.dto;

import com.iglooclub.nungil.domain.enums.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NungilResponse {
    private Long id;

    private Sex sex;

    private Integer age;

    private String nickname;

    private String animalFace;

    private Mbti mbti;

    private String job;

    private Integer height;

    private String faceDepictionAllocationList;

    private String personalityDepictionAllocationList;

    private String description;

    private String hobbyAllocationList;

    private LocalDateTime expiredAt;
}

package com.iglooclub.nungil.repository;

import com.iglooclub.nungil.domain.Member;
import com.iglooclub.nungil.domain.OauthInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    Optional<Member> findByOauthInfo(OauthInfo oauthInfo);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByPhoneNumber(String phoneNumber);
}

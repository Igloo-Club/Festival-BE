package com.iglooclub.nungil.repository;

import com.iglooclub.nungil.domain.Acquaintance;
import com.iglooclub.nungil.domain.Member;
import com.iglooclub.nungil.domain.enums.NungilStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AcquaintanceRepository extends JpaRepository<Acquaintance, Long> {
    List<Acquaintance> findByMember(Member member);

    Optional<Acquaintance> findByMemberAndAcquaintanceMember(Member member, Member acquaintanceMember);

}
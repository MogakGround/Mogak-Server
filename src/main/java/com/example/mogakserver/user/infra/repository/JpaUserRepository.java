package com.example.mogakserver.user.infra.repository;

import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.mogakserver.user.domain.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.kakaoId = :kakaoId")
    Optional<User> findByKakaoId(@Param("kakaoId") Long kakaoId);

    @Query("SELECT u FROM User u WHERE u.modifiedAt >= :cutoff")
    Page<User> findUsersUpdatedLast30Minutes(@Param("cutoff") LocalDateTime cutoff, Pageable pageable);
}
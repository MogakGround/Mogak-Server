package com.example.mogakserver.user.infra.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.mogakserver.user.domain.entity.User;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.kakaoId = :kakaoId")
    Optional<User> findByKakaoId(@Param("kakaoId") Long kakaoId);
}
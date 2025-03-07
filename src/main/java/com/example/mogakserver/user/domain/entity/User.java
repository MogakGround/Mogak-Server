package com.example.mogakserver.user.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long kakaoId;

    private String name;

    private String nickName;

    private String portfolioUrl;

    private String email;

    private Long todayAddedTime = 0L;

    @Version
    private Integer version = 0;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    public void updateTodayAddedTime(Long updateAddedTime){
        if (this.todayAddedTime == null) {
            this.todayAddedTime = 0L;
        }
        this.todayAddedTime = todayAddedTime+updateAddedTime;
    }

    public void updateProfile(String nickName, String portfolioUrl) {
        this.nickName = nickName;

        if (portfolioUrl != null) {
            this.portfolioUrl = portfolioUrl;
        }
    }

    public void resetTodayAddedTime() {
        this.todayAddedTime = 0L;
    }

    public void setVersion(int version){
        this.version = version;
    }
}

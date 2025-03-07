package com.example.mogakserver.room.domain.entity;

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
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String roomName;

    @NotNull
    private boolean isLocked;

    private String roomPassword;

    private Integer userCnt;

    private String roomExplain;

    public void updateRoom(String roomName, Boolean isLocked, String roomPassword) {
        this.roomName = roomName;
        this.isLocked = isLocked;

        this.roomPassword = Boolean.FALSE.equals(isLocked) ? null : roomPassword;
    }

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;
}

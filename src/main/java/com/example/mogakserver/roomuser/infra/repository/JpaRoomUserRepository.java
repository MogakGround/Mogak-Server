package com.example.mogakserver.roomuser.infra.repository;

import com.example.mogakserver.room.domain.entity.Room;
import com.example.mogakserver.roomuser.domain.entity.RoomUser;
import com.example.mogakserver.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaRoomUserRepository extends JpaRepository<RoomUser, Long> {
    Optional<RoomUser> findByUserIdAndRoomId(Long userId, Long roomId);
    List<RoomUser> findByRoomId(Long roomId);

    void deleteByRoomId(Long roomId);
    void deleteByUserId(Long userId);

    @Query("SELECT ru FROM RoomUser ru WHERE ru.userId = :userId " +
            "AND (ru.createdAt > :sevenDaysAgo OR ru.modifiedAt > :sevenDaysAgo)")
    Page<RoomUser> findRecentRoomUsers(
            @Param("userId") Long userId,
            @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo,
            Pageable pageable
    );
    @Query("SELECT ru FROM RoomUser ru WHERE ru.userId = :userId AND ru.isHost = true")
    Page<RoomUser> findHostRoomsByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("SELECT ru.roomId FROM RoomUser ru WHERE ru.userId = :userId AND ru.isHost = true")
    List<Long> findHostRoomIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT ru.roomId FROM RoomUser ru WHERE ru.userId = :userId")
    List<Long> findJoinedRoomIdsByUserId(@Param("userId") Long userId);

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);
}

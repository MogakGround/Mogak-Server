package com.example.mogakserver.roomuser.infra.repository;

import com.example.mogakserver.roomuser.domain.entity.RoomUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaRoomUserRepository extends JpaRepository<RoomUser, Long> {
    Optional<RoomUser> findByUserIdAndRoomId(Long userId, Long roomId);

    List<RoomUser> findByRoomId(Long roomId);

    void deleteByRoomId(Long roomId);
}

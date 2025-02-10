package com.example.mogakserver.room.infra.repository;

import com.example.mogakserver.room.domain.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRoomRepository extends JpaRepository<Room, Long> {
}

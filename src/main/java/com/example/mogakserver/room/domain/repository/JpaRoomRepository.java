package com.example.mogakserver.room.domain.repository;

import com.example.mogakserver.room.domain.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaRoomRepository extends JpaRepository<Room, Long> {
    Page<Room> findAllByIdIn(List<Long> roomIds, Pageable pageable);
}


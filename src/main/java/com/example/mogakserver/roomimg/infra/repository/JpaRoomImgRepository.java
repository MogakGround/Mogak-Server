package com.example.mogakserver.roomimg.infra.repository;

import com.example.mogakserver.roomimg.domain.entity.RoomImg;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRoomImgRepository extends JpaRepository<RoomImg, Long> {
}

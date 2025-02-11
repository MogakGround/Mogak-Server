package com.example.mogakserver.roomimg.infra.repository;

import com.example.mogakserver.roomimg.domain.entity.RoomImg;
import com.example.mogakserver.roomimg.domain.entity.RoomImgType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaRoomImgRepository extends JpaRepository<RoomImg, Long> {
    @Query("SELECT r.roomImgType FROM RoomImg r WHERE r.roomId = :roomId")
    RoomImgType findRoomImgTypeByRoomId(@Param("roomId") Long roomId);

    void deleteByRoomId(Long roomId);

    List<RoomImg> findByRoomIdIn(List<Long> roomIds);
}

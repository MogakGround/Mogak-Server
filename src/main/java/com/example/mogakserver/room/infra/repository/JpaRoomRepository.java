package com.example.mogakserver.room.infra.repository;

import com.example.mogakserver.room.domain.entity.Room;
import com.example.mogakserver.worktime.domain.entity.WorkHour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaRoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT r FROM Room r WHERE r.id IN " +
            "(SELECT w.roomId FROM WorkTime w WHERE w.workHour IN :workHours)")
    Page<Room> findRoomsByWorkHours(@Param("workHours") List<WorkHour> workHours, Pageable pageable);
}

package com.example.mogakserver.worktime.infra.repository;

import com.example.mogakserver.worktime.domain.entity.WorkTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaWorkTimeRepository extends JpaRepository<WorkTime, Long> {
    @Query("SELECT w.workHour FROM WorkTime w WHERE w.roomId = :roomId")
    List<String> findWorkHoursByRoomId(@Param("roomId") Long roomId);

    void deleteByRoomId(Long roomId);
    List<WorkTime> findAllByRoomIdIn(List<Long> hostRoomIds);

}

package com.example.mogakserver.worktime.domain.repository;

import com.example.mogakserver.worktime.domain.entity.WorkTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaWorkTimeRepository extends JpaRepository<WorkTime, Long> {
    List<WorkTime> findAllByRoomIdIn(List<Long> hostRoomIds);
}

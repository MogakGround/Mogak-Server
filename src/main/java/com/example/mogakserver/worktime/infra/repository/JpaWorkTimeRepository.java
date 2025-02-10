package com.example.mogakserver.worktime.infra.repository;

import com.example.mogakserver.worktime.domain.entity.WorkTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaWorkTimeRepository extends JpaRepository<WorkTime, Long> {
}

package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.Waypoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WaypointRepository extends JpaRepository<Waypoint, Long> {
    List<Waypoint> findByTourId(Long tourId);
}

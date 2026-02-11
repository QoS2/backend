package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.Treasure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreasureRepository extends JpaRepository<Treasure, Long> {
    List<Treasure> findByTourId(Long tourId);
}

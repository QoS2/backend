package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.TourTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourTagRepository extends JpaRepository<TourTag, Long> {

    List<TourTag> findByTourId(Long tourId);
}

package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.UserTourAccess;
import com.app.questofseoul.domain.enums.TourAccessStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserTourAccessRepository extends JpaRepository<UserTourAccess, Long> {

    Optional<UserTourAccess> findByUserIdAndTourId(UUID userId, Long tourId);

    boolean existsByUserIdAndTourIdAndStatus(UUID userId, Long tourId, TourAccessStatus status);
}

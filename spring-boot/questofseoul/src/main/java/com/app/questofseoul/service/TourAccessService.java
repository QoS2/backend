package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.Tour;
import com.app.questofseoul.domain.entity.User;
import com.app.questofseoul.domain.entity.UserTourAccess;
import com.app.questofseoul.domain.enums.TourAccessMethod;
import com.app.questofseoul.domain.enums.TourAccessStatus;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.TourRepository;
import com.app.questofseoul.repository.UserTourAccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TourAccessService {

    private final TourRepository tourRepository;
    private final UserTourAccessRepository userTourAccessRepository;

    @Transactional
    public void unlockTour(User user, Long tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));

        UserTourAccess access = userTourAccessRepository.findByUserIdAndTourId(user.getId(), tourId)
                .orElseGet(() -> userTourAccessRepository.save(UserTourAccess.create(user, tour)));
        access.unlock();
        userTourAccessRepository.save(access);
    }
}

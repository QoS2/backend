package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.UserPhotoSubmission;
import com.app.questofseoul.domain.enums.PhotoSubmissionStatus;
import com.app.questofseoul.dto.admin.PhotoSubmissionVerifyRequest;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.UserPhotoSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminPhotoSubmissionService {

    private final UserPhotoSubmissionRepository userPhotoSubmissionRepository;

    @Transactional(readOnly = true)
    public List<UserPhotoSubmission> getPendingSubmissions() {
        return userPhotoSubmissionRepository.findByStatusOrderBySubmittedAtAsc(PhotoSubmissionStatus.PENDING);
    }

    @Transactional
    public void verifySubmission(Long submissionId, PhotoSubmissionVerifyRequest request, UUID adminId) {
        UserPhotoSubmission submission = userPhotoSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo submission not found"));

        if (submission.getStatus() != PhotoSubmissionStatus.PENDING) {
            throw new IllegalStateException("Already verified");
        }

        if ("APPROVE".equalsIgnoreCase(request.action())) {
            String mintToken = "MINT-" + submission.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);
            submission.approve(adminId, mintToken);
        } else if ("REJECT".equalsIgnoreCase(request.action())) {
            submission.reject(adminId, request.rejectReason());
        } else {
            throw new IllegalArgumentException("action must be APPROVE or REJECT");
        }
    }
}

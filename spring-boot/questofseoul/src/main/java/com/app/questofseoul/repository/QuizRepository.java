package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByStepIdOrderByIdAsc(Long stepId);
}

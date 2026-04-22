package com.quizweb.backend.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByPublishedTrueOrderByIdAsc();

    List<Quiz> findByCreatedByIgnoreCaseOrderByIdDesc(String createdBy);

    Optional<Quiz> findByIdAndCreatedByIgnoreCase(Long id, String createdBy);
}

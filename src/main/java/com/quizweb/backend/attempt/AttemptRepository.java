package com.quizweb.backend.attempt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    List<Attempt> findByQuizIdOrderByScoreDescCorrectCountDescTotalDurationMsAscSubmittedAtAsc(Long quizId);

    boolean existsByQuizIdAndUsernameIgnoreCase(Long quizId, String username);

    void deleteByQuizId(Long quizId);
}

package com.quizweb.backend.live;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {

    Optional<LiveSession> findByPin(String pin);
}

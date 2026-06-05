package com.quizweb.backend.config;

import com.quizweb.backend.attempt.AttemptRepository;
import com.quizweb.backend.quiz.Quiz;
import com.quizweb.backend.quiz.QuizRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final QuizRepository quizRepository;
    private final AttemptRepository attemptRepository;

    public DatabaseSeeder(QuizRepository quizRepository, AttemptRepository attemptRepository) {
        this.quizRepository = quizRepository;
        this.attemptRepository = attemptRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Xóa sạch các bộ Quiz mẫu hệ thống trước đây (createdBy = "system")
        List<Quiz> systemQuizzes = quizRepository.findByCreatedByIgnoreCaseOrderByIdDesc("system");
        if (!systemQuizzes.isEmpty()) {
            System.out.println(">>> Đang xóa các lượt chơi (attempts) liên quan đến Quiz mẫu...");
            for (Quiz q : systemQuizzes) {
                attemptRepository.deleteByQuizId(q.getId());
            }
            System.out.println(">>> Đang xóa " + systemQuizzes.size() + " bộ Quiz mẫu hệ thống...");
            quizRepository.deleteAll(systemQuizzes);
            System.out.println(">>> Đã xóa sạch các bộ Quiz mẫu hệ thống thành công!");
        } else {
            System.out.println(">>> Không tìm thấy bộ Quiz mẫu hệ thống nào để xóa.");
        }
    }
}

package com.quizweb.backend.attempt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizweb.backend.attempt.dto.AnswerSubmissionRequest;
import com.quizweb.backend.attempt.dto.LeaderboardEntryResponse;
import com.quizweb.backend.attempt.dto.PlayQuizResponse;
import com.quizweb.backend.attempt.dto.PlaySlideResponse;
import com.quizweb.backend.attempt.dto.SubmitAttemptRequest;
import com.quizweb.backend.attempt.dto.SubmitAttemptResponse;
import com.quizweb.backend.common.exception.ConflictException;
import com.quizweb.backend.common.exception.NotFoundException;
import com.quizweb.backend.quiz.Quiz;
import com.quizweb.backend.quiz.QuizMode;
import com.quizweb.backend.quiz.QuizRepository;
import com.quizweb.backend.quiz.QuizSlide;
import com.quizweb.backend.quiz.SlideType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AttemptService {

    private static final int MIN_TIME_MODE_SCORE = 100;
    private static final int MAX_TIME_MODE_SCORE = 1000;
    private static final int QUESTION_REVEAL_WPM = 200;

    private final QuizRepository quizRepository;
    private final AttemptRepository attemptRepository;
    private final ObjectMapper objectMapper;

    public AttemptService(QuizRepository quizRepository, AttemptRepository attemptRepository, ObjectMapper objectMapper) {
        this.quizRepository = quizRepository;
        this.attemptRepository = attemptRepository;
        this.objectMapper = objectMapper;
    }

    public PlayQuizResponse getPlayableQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .filter(Quiz::isPublished)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));

        List<PlaySlideResponse> slides = quiz.getSlides().stream()
                .sorted(Comparator.comparingInt(QuizSlide::getPositionIndex))
                .map(this::toPlaySlide)
                .toList();

        return new PlayQuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getMode(),
                quiz.getTotalTimeLimitSeconds(),
                slides
        );
    }

    @Transactional
    public SubmitAttemptResponse submitAttempt(SubmitAttemptRequest request) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new NotFoundException("Quiz not found"));

        String nickname = normalizeNickname(request.getNickname());
        if (attemptRepository.existsByQuizIdAndUsernameIgnoreCase(quiz.getId(), nickname)) {
            throw new ConflictException("You already submitted this quiz");
        }

        List<QuizSlide> slides = quiz.getSlides().stream()
                .sorted(Comparator.comparingInt(QuizSlide::getPositionIndex))
                .toList();
        if (slides.isEmpty()) {
            throw new ConflictException("Quiz has no slides");
        }

        Map<Long, AnswerSubmissionRequest> answerMap = new LinkedHashMap<>();
        for (AnswerSubmissionRequest answer : request.getAnswers()) {
            answerMap.put(answer.getSlideId(), answer);
        }

        ScoreResult result = scoreAttempt(quiz, slides, answerMap, request.getTotalDurationMs());

        Attempt attempt = new Attempt();
        attempt.setQuiz(quiz);
        attempt.setUsername(nickname);
        attempt.setScore(result.score);
        attempt.setCorrectCount(result.correctCount);
        attempt.setTotalQuestions(slides.size());
        attempt.setTotalDurationMs(Math.max(0L, request.getTotalDurationMs()));
        Attempt saved = attemptRepository.save(attempt);

        return new SubmitAttemptResponse(
                saved.getId(),
                quiz.getMode().name(),
                saved.getScore(),
                saved.getCorrectCount(),
                saved.getTotalQuestions(),
                saved.getTotalDurationMs(),
                getLeaderboard(quiz.getId())
        );
    }

    public List<LeaderboardEntryResponse> getLeaderboard(Long quizId) {
        List<Attempt> attempts = attemptRepository.findByQuizIdOrderByScoreDescCorrectCountDescTotalDurationMsAscSubmittedAtAsc(quizId);
        List<LeaderboardEntryResponse> leaderboard = new ArrayList<>();
        int rank = 1;
        for (Attempt attempt : attempts) {
            leaderboard.add(new LeaderboardEntryResponse(
                    rank++,
                    attempt.getUsername(),
                    attempt.getScore(),
                    attempt.getCorrectCount(),
                    attempt.getTotalDurationMs()
            ));
        }
        return leaderboard;
    }

    public SubmitAttemptResponse previewScore(SubmitAttemptRequest request) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new NotFoundException("Quiz not found"));
        List<QuizSlide> slides = quiz.getSlides().stream()
                .sorted(Comparator.comparingInt(QuizSlide::getPositionIndex))
                .toList();

        Map<Long, AnswerSubmissionRequest> answerMap = new LinkedHashMap<>();
        for (AnswerSubmissionRequest answer : request.getAnswers()) {
            answerMap.put(answer.getSlideId(), answer);
        }
        ScoreResult result = scoreAttempt(quiz, slides, answerMap, request.getTotalDurationMs());

        return new SubmitAttemptResponse(
                null,
                quiz.getMode().name(),
                result.score,
                result.correctCount,
                slides.size(),
                Math.max(0L, request.getTotalDurationMs()),
                getLeaderboard(quiz.getId())
        );
    }

    private ScoreResult scoreAttempt(
            Quiz quiz,
            List<QuizSlide> slides,
            Map<Long, AnswerSubmissionRequest> answerMap,
            long totalDurationMs
    ) {
        int score = 0;
        int correctCount = 0;
        long effectiveDuration = Math.max(0L, totalDurationMs);
        long normalLimitMs = quiz.getTotalTimeLimitSeconds() == null ? Long.MAX_VALUE : quiz.getTotalTimeLimitSeconds() * 1000L;

        // Cache đáp án đúng đã parse JSON để tránh parse lặp lại mỗi lần chấm.
        Map<Long, Set<Integer>> expectedChoiceCache = new HashMap<>();
        Map<Long, List<String>> expectedOrderingCache = new HashMap<>();
        Map<Long, Set<String>> expectedTextCache = new HashMap<>();

        for (QuizSlide slide : slides) {
            AnswerSubmissionRequest answer = answerMap.get(slide.getId());
            if (answer == null) {
                continue;
            }

            if (quiz.getMode() == QuizMode.NORMAL && answer.getElapsedMs() > normalLimitMs) {
                continue;
            }

            boolean isCorrect = evaluateSlideAnswer(
                    slide,
                    answer,
                    expectedChoiceCache,
                    expectedOrderingCache,
                    expectedTextCache
            );
            if (!isCorrect) {
                continue;
            }

            correctCount++;
            if (quiz.getMode() == QuizMode.NORMAL) {
                score += 1;
            } else {
                int perQuestionScore = calculateTimeModeScore(slide, answer);
                score += perQuestionScore;
            }
        }

        return new ScoreResult(score, correctCount, effectiveDuration);
    }

    private int calculateTimeModeScore(QuizSlide slide, AnswerSubmissionRequest answer) {
        Integer limitSec = slide.getTimeLimitSeconds();
        if (limitSec == null) {
            return 0;
        }
        long limitMs = limitSec * 1000L;
        long graceLimitMs = limitMs + 1500L; // 1.5s grace period for client timer latency
        if (answer.getElapsedMs() == null || answer.getElapsedMs() > graceLimitMs) {
            return 0;
        }
        long sanitizedElapsedMs = Math.max(0L, answer.getElapsedMs());
        long elapsedMs = Math.min(sanitizedElapsedMs, limitMs);
        double ratio = 1.0 - ((double) elapsedMs / (double) limitMs);
        int computed = (int) Math.round(MAX_TIME_MODE_SCORE * ratio);
        return Math.max(MIN_TIME_MODE_SCORE, Math.min(MAX_TIME_MODE_SCORE, computed));
    }

    private boolean evaluateSlideAnswer(
            QuizSlide slide,
            AnswerSubmissionRequest answer,
            Map<Long, Set<Integer>> expectedChoiceCache,
            Map<Long, List<String>> expectedOrderingCache,
            Map<Long, Set<String>> expectedTextCache
    ) {
        return switch (slide.getType()) {
            case SINGLE_CHOICE, MULTI_CHOICE -> {
                Set<Integer> expected = expectedChoiceCache.computeIfAbsent(slide.getId(), id -> {
                    List<Integer> parsed = readIntegerList(slide.getCorrectAnswersJson());
                    return new LinkedHashSet<>(parsed);
                });

                List<Integer> actual = answer.getSelectedOptionIndexes() == null ? List.of() : answer.getSelectedOptionIndexes();
                Set<Integer> actualSet = new LinkedHashSet<>(actual);
                yield expected.equals(actualSet);
            }
            case ORDERING -> {
                List<String> expected = expectedOrderingCache.computeIfAbsent(slide.getId(), id ->
                        normalize(readStringList(slide.getCorrectAnswersJson()))
                );
                List<String> actual = normalize(answer.getOrderedItems() == null ? List.of() : answer.getOrderedItems());
                yield expected.equals(actual);
            }
            case TEXT -> {
                String text = answer.getTextAnswer();
                if (text == null || text.isBlank()) {
                    yield false;
                }
                String normalizedAnswer = text.trim().toLowerCase();

                Set<String> expected = expectedTextCache.computeIfAbsent(slide.getId(), id -> {
                    List<String> parsed = readStringList(slide.getCorrectAnswersJson());
                    Set<String> set = new LinkedHashSet<>();
                    for (String v : parsed) {
                        if (v == null) continue;
                        String n = v.trim().toLowerCase();
                        if (!n.isBlank()) set.add(n);
                    }
                    return set;
                });
                yield expected.contains(normalizedAnswer);
            }
        };
    }

    private PlaySlideResponse toPlaySlide(QuizSlide slide) {
        List<String> options = switch (slide.getType()) {
            case SINGLE_CHOICE, MULTI_CHOICE -> readStringList(slide.getOptionsJson());
            case ORDERING -> readStringList(slide.getOptionsJson());
            case TEXT -> List.of();
        };

        int wordCount = slide.getQuestionText() == null ? 0 : slide.getQuestionText().trim().split("\\s+").length;
        int revealDurationMs = (int) Math.ceil((wordCount / (double) QUESTION_REVEAL_WPM) * 60_000.0);

        List<Integer> correctOptionIndexes = switch (slide.getType()) {
            case SINGLE_CHOICE, MULTI_CHOICE -> readIntegerList(slide.getCorrectAnswersJson());
            default -> List.of();
        };
        List<String> correctOrderingItems = slide.getType() == SlideType.ORDERING
                ? readStringList(slide.getCorrectAnswersJson())
                : List.of();
        List<String> acceptedAnswers = slide.getType() == SlideType.TEXT
                ? readStringList(slide.getCorrectAnswersJson())
                : List.of();

        return new PlaySlideResponse(
                slide.getId(),
                slide.getPositionIndex(),
                slide.getType(),
                slide.getQuestionText(),
                slide.getImageUrl(),
                slide.getTimeLimitSeconds(),
                Math.max(revealDurationMs, 0),
                options,
                correctOptionIndexes,
                correctOrderingItems,
                acceptedAnswers
        );
    }

    private List<String> readStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        } catch (JsonProcessingException ex) {
            throw new ConflictException("Could not parse slide string data");
        }
    }

    private List<Integer> readIntegerList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class)
            );
        } catch (JsonProcessingException ex) {
            throw new ConflictException("Could not parse slide integer data");
        }
    }

    private List<String> normalize(List<String> values) {
        return values.stream()
                .map(v -> v == null ? "" : v.trim().toLowerCase())
                .toList();
    }

    private String normalizeNickname(String nickname) {
        String normalized = nickname == null ? "" : nickname.trim();
        if (normalized.length() < 2 || normalized.length() > 30) {
            throw new ConflictException("Nickname must be 2-30 characters");
        }
        return normalized;
    }

    private record ScoreResult(int score, int correctCount, long effectiveDurationMs) {}
}

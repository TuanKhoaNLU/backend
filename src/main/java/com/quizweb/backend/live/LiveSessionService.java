package com.quizweb.backend.live;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizweb.backend.attempt.dto.AnswerSubmissionRequest;
import com.quizweb.backend.attempt.dto.PlaySlideResponse;
import com.quizweb.backend.common.exception.ConflictException;
import com.quizweb.backend.common.exception.NotFoundException;
import com.quizweb.backend.live.dto.CreateLiveSessionRequest;
import com.quizweb.backend.live.dto.JoinLiveSessionRequest;
import com.quizweb.backend.live.dto.JoinLiveSessionResponse;
import com.quizweb.backend.live.dto.LiveLeaderboardEntryResponse;
import com.quizweb.backend.live.dto.LobbyParticipantResponse;
import com.quizweb.backend.live.dto.LiveSessionStateResponse;
import com.quizweb.backend.live.dto.SubmitLiveAnswerRequest;
import com.quizweb.backend.live.dto.SubmitLiveAnswerResponse;
import com.quizweb.backend.quiz.Quiz;
import com.quizweb.backend.quiz.QuizMode;
import com.quizweb.backend.quiz.QuizRepository;
import com.quizweb.backend.quiz.QuizSlide;
import com.quizweb.backend.user.UserAccount;
import com.quizweb.backend.user.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class LiveSessionService {

    private static final int MIN_TIME_MODE_SCORE = 100;
    private static final int MAX_TIME_MODE_SCORE = 1000;
    private static final int QUESTION_REVEAL_WPM = 200;

    private final LiveSessionRepository liveSessionRepository;
    private final LiveSessionParticipantRepository participantRepository;
    private final LiveAnswerEventRepository answerEventRepository;
    private final QuizRepository quizRepository;
    private final UserAccountRepository userAccountRepository;
    private final ObjectMapper objectMapper;

    public LiveSessionService(
            LiveSessionRepository liveSessionRepository,
            LiveSessionParticipantRepository participantRepository,
            LiveAnswerEventRepository answerEventRepository,
            QuizRepository quizRepository,
            UserAccountRepository userAccountRepository,
            ObjectMapper objectMapper
    ) {
        this.liveSessionRepository = liveSessionRepository;
        this.participantRepository = participantRepository;
        this.answerEventRepository = answerEventRepository;
        this.quizRepository = quizRepository;
        this.userAccountRepository = userAccountRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public LiveSessionStateResponse createSession(CreateLiveSessionRequest request, String hostUsername) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .filter(Quiz::isPublished)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));
        if (!quiz.isLiveEnabled()) {
            throw new ConflictException("This quiz does not allow live sessions");
        }
        if (quiz.getSlides() == null || quiz.getSlides().isEmpty()) {
            throw new ConflictException("Quiz has no slides");
        }

        LiveSession session = new LiveSession();
        session.setQuiz(quiz);
        session.setPin(generateUniquePin());
        session.setHostUsername(hostUsername);
        session.setAllowLateJoin(request.getAllowLateJoin() == null || request.getAllowLateJoin());
        LiveSession savedSession = liveSessionRepository.save(session);

        LiveSessionParticipant hostParticipant = new LiveSessionParticipant();
        hostParticipant.setLiveSession(savedSession);
        hostParticipant.setDisplayName(hostUsername);
        hostParticipant.setRole(LiveParticipantRole.HOST);
        hostParticipant.setConnected(true);
        userAccountRepository.findByUsernameIgnoreCase(hostUsername).ifPresent(hostParticipant::setUser);
        participantRepository.save(hostParticipant);

        return toSessionState(savedSession);
    }

    @Transactional
    public JoinLiveSessionResponse joinByPin(JoinLiveSessionRequest request) {
        String pin = normalizePin(request.getPin());
        String displayName = normalizeDisplayName(request.getDisplayName());

        LiveSession session = liveSessionRepository.findByPin(pin)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        if (session.getStatus() == LiveSessionStatus.FINISHED || session.getStatus() == LiveSessionStatus.CANCELLED) {
            throw new ConflictException("Session already ended");
        }
        if (session.getStatus() == LiveSessionStatus.IN_PROGRESS && !session.isAllowLateJoin()) {
            throw new ConflictException("Session does not allow late join");
        }
        if (participantRepository.findByLiveSessionIdAndDisplayNameIgnoreCase(session.getId(), displayName).isPresent()) {
            throw new ConflictException("Display name is already used in this room");
        }

        LiveSessionParticipant participant = new LiveSessionParticipant();
        participant.setLiveSession(session);
        participant.setDisplayName(displayName);
        participant.setRole(LiveParticipantRole.PLAYER);
        participant.setConnected(true);
        userAccountRepository.findByUsernameIgnoreCase(displayName).ifPresent(participant::setUser);
        LiveSessionParticipant savedParticipant = participantRepository.save(participant);

        return new JoinLiveSessionResponse(
                savedParticipant.getId(),
                savedParticipant.getDisplayName(),
                savedParticipant.getRole(),
                toSessionState(session)
        );
    }

    @Transactional
    public LiveSessionStateResponse startSession(Long sessionId, String username) {
        LiveSession session = getSession(sessionId);
        ensureHost(session, username);
        if (session.getStatus() != LiveSessionStatus.WAITING) {
            throw new ConflictException("Session can only be started from WAITING state");
        }
        session.setStatus(LiveSessionStatus.IN_PROGRESS);
        Instant now = Instant.now();
        session.setStartedAt(now);
        session.setCurrentQuestionIndex(0);
        session.setCurrentSlideStartedAt(now);
        return toSessionState(liveSessionRepository.save(session));
    }

    @Transactional
    public LiveSessionStateResponse nextQuestion(Long sessionId, String username) {
        LiveSession session = getSession(sessionId);
        ensureHost(session, username);
        if (session.getStatus() != LiveSessionStatus.IN_PROGRESS) {
            throw new ConflictException("Session is not in progress");
        }

        List<QuizSlide> slides = getSortedSlides(session.getQuiz());
        int nextIndex = session.getCurrentQuestionIndex() + 1;
        if (nextIndex >= slides.size()) {
            session.setStatus(LiveSessionStatus.FINISHED);
            session.setEndedAt(Instant.now());
            session.setCurrentSlideStartedAt(null);
        } else {
            session.setCurrentQuestionIndex(nextIndex);
            session.setCurrentSlideStartedAt(Instant.now());
        }
        return toSessionState(liveSessionRepository.save(session));
    }

    @Transactional
    public LiveSessionStateResponse endSession(Long sessionId, String username) {
        LiveSession session = getSession(sessionId);
        ensureHost(session, username);
        if (session.getStatus() == LiveSessionStatus.FINISHED || session.getStatus() == LiveSessionStatus.CANCELLED) {
            return toSessionState(session);
        }
        session.setStatus(LiveSessionStatus.FINISHED);
        session.setEndedAt(Instant.now());
        session.setCurrentSlideStartedAt(null);
        return toSessionState(liveSessionRepository.save(session));
    }

    public LiveSessionStateResponse getSessionState(Long sessionId) {
        return toSessionState(getSession(sessionId));
    }

    @Transactional
    public SubmitLiveAnswerResponse submitAnswer(Long sessionId, SubmitLiveAnswerRequest request) {
        LiveSession session = getSession(sessionId);
        if (session.getStatus() != LiveSessionStatus.IN_PROGRESS) {
            throw new ConflictException("Session is not accepting answers");
        }

        LiveSessionParticipant participant = participantRepository.findById(request.getParticipantId())
                .orElseThrow(() -> new NotFoundException("Participant not found"));
        if (!participant.getLiveSession().getId().equals(sessionId)) {
            throw new ConflictException("Participant does not belong to this session");
        }
        if (participant.getRole() == LiveParticipantRole.HOST) {
            throw new ConflictException("Host cannot submit answers");
        }

        List<QuizSlide> slides = getSortedSlides(session.getQuiz());
        QuizSlide currentSlide = getCurrentSlide(session, slides);
        if (!currentSlide.getId().equals(request.getAnswer().getSlideId())) {
            throw new ConflictException("Answer must target the current question");
        }
        if (answerEventRepository.existsByLiveSessionIdAndParticipantIdAndSlideId(
                sessionId,
                participant.getId(),
                currentSlide.getId()
        )) {
            throw new ConflictException("You already answered this question");
        }

        AnswerSubmissionRequest answer = request.getAnswer();
        applyServerSyncedElapsed(session, currentSlide, answer);

        boolean correct = evaluateSlideAnswer(currentSlide, answer);
        int scoreEarned = correct ? calculateScore(session.getQuiz(), currentSlide, answer) : 0;
        if (correct) {
            participant.setCorrectCount(participant.getCorrectCount() + 1);
        }
        participant.setScore(participant.getScore() + scoreEarned);
        participant.setLastSeenAt(Instant.now());
        participantRepository.save(participant);

        LiveAnswerEvent event = new LiveAnswerEvent();
        event.setLiveSession(session);
        event.setParticipant(participant);
        event.setSlide(currentSlide);
        event.setSubmittedAnswerJson(toAnswerJson(answer));
        event.setCorrect(correct);
        event.setScoreEarned(scoreEarned);
        event.setResponseTimeMs(Math.max(0L, answer.getElapsedMs()));
        answerEventRepository.save(event);

        return new SubmitLiveAnswerResponse(
                correct,
                scoreEarned,
                participant.getScore(),
                participant.getCorrectCount(),
                getLeaderboard(sessionId)
        );
    }

    public List<LiveLeaderboardEntryResponse> getLeaderboard(Long sessionId) {
        getSession(sessionId);
        List<LiveSessionParticipant> participants = participantRepository
                .findByLiveSessionIdOrderByScoreDescCorrectCountDescJoinedAtAsc(sessionId);
        java.util.ArrayList<LiveLeaderboardEntryResponse> leaderboard = new java.util.ArrayList<>();
        int rank = 1;
        for (LiveSessionParticipant participant : participants) {
            if (participant.getRole() != LiveParticipantRole.PLAYER) {
                continue;
            }
            leaderboard.add(new LiveLeaderboardEntryResponse(
                    rank++,
                    participant.getId(),
                    participant.getDisplayName(),
                    participant.getScore(),
                    participant.getCorrectCount(),
                    participant.isConnected()
            ));
        }
        return leaderboard;
    }

    private void applyServerSyncedElapsed(LiveSession session, QuizSlide slide, AnswerSubmissionRequest answer) {
        if (session.getCurrentSlideStartedAt() == null) {
            return;
        }
        long serverElapsed = Duration.between(session.getCurrentSlideStartedAt(), Instant.now()).toMillis();
        serverElapsed = Math.max(0L, serverElapsed);
        if (session.getQuiz().getMode() == QuizMode.TIME && slide.getTimeLimitSeconds() != null) {
            long capMs = slide.getTimeLimitSeconds() * 1000L;
            serverElapsed = Math.min(serverElapsed, capMs);
        }
        answer.setElapsedMs(serverElapsed);
    }

    private LiveSessionStateResponse toSessionState(LiveSession session) {
        List<QuizSlide> slides = getSortedSlides(session.getQuiz());
        PlaySlideResponse currentSlide = null;
        if (session.getStatus() == LiveSessionStatus.IN_PROGRESS && session.getCurrentQuestionIndex() < slides.size()) {
            currentSlide = toPlaySlide(slides.get(session.getCurrentQuestionIndex()));
        }
        List<LobbyParticipantResponse> lobby = participantRepository.findByLiveSessionIdOrderByJoinedAtAsc(session.getId())
                .stream()
                .map(p -> new LobbyParticipantResponse(
                        p.getId(),
                        p.getDisplayName(),
                        p.getRole().name(),
                        p.getRole() == LiveParticipantRole.HOST,
                        p.isConnected()
                ))
                .toList();
        return new LiveSessionStateResponse(
                session.getId(),
                session.getPin(),
                session.getQuiz().getId(),
                session.getQuiz().getTitle(),
                session.getHostUsername(),
                session.getStatus(),
                session.getCurrentQuestionIndex(),
                slides.size(),
                session.isAllowLateJoin(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getCurrentSlideStartedAt(),
                lobby,
                currentSlide
        );
    }

    private LiveSession getSession(Long sessionId) {
        return liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));
    }

    private void ensureHost(LiveSession session, String username) {
        if (!session.getHostUsername().equalsIgnoreCase(username)) {
            throw new ConflictException("Only host can control this session");
        }
    }

    private QuizSlide getCurrentSlide(LiveSession session, List<QuizSlide> slides) {
        int idx = session.getCurrentQuestionIndex();
        if (idx < 0 || idx >= slides.size()) {
            throw new ConflictException("Current question is out of range");
        }
        return slides.get(idx);
    }

    private List<QuizSlide> getSortedSlides(Quiz quiz) {
        return quiz.getSlides().stream()
                .sorted(Comparator.comparingInt(QuizSlide::getPositionIndex))
                .toList();
    }

    private String generateUniquePin() {
        for (int i = 0; i < 20; i++) {
            String pin = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
            if (liveSessionRepository.findByPin(pin).isEmpty()) {
                return pin;
            }
        }
        throw new ConflictException("Could not generate unique PIN");
    }

    private String normalizePin(String pin) {
        String normalized = pin == null ? "" : pin.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() < 4 || normalized.length() > 12) {
            throw new ConflictException("Invalid PIN");
        }
        return normalized;
    }

    private String normalizeDisplayName(String displayName) {
        String normalized = displayName == null ? "" : displayName.trim();
        if (normalized.length() < 2 || normalized.length() > 30) {
            throw new ConflictException("displayName must be 2-30 characters");
        }
        return normalized;
    }

    private String toAnswerJson(AnswerSubmissionRequest answer) {
        try {
            return objectMapper.writeValueAsString(answer);
        } catch (JsonProcessingException ex) {
            throw new ConflictException("Could not serialize answer payload");
        }
    }

    private int calculateScore(Quiz quiz, QuizSlide slide, AnswerSubmissionRequest answer) {
        if (quiz.getMode() == QuizMode.NORMAL) {
            return 1;
        }
        Integer limitSec = slide.getTimeLimitSeconds();
        if (limitSec == null || answer.getElapsedMs() == null) {
            return 0;
        }
        long limitMs = limitSec * 1000L;
        if (answer.getElapsedMs() > limitMs) {
            return 0;
        }
        double ratio = 1.0 - ((double) answer.getElapsedMs() / (double) limitMs);
        int computed = (int) Math.round(MAX_TIME_MODE_SCORE * ratio);
        return Math.max(MIN_TIME_MODE_SCORE, Math.min(MAX_TIME_MODE_SCORE, computed));
    }

    private boolean evaluateSlideAnswer(QuizSlide slide, AnswerSubmissionRequest answer) {
        return switch (slide.getType()) {
            case SINGLE_CHOICE, MULTI_CHOICE -> matchChoiceAnswer(slide, answer);
            case ORDERING -> matchOrderingAnswer(slide, answer);
            case TEXT -> matchTextAnswer(slide, answer);
        };
    }

    private boolean matchChoiceAnswer(QuizSlide slide, AnswerSubmissionRequest answer) {
        List<Integer> expected = readIntegerList(slide.getCorrectAnswersJson());
        List<Integer> actual = answer.getSelectedOptionIndexes() == null ? List.of() : answer.getSelectedOptionIndexes();
        Set<Integer> expectedSet = new LinkedHashSet<>(expected);
        Set<Integer> actualSet = new LinkedHashSet<>(actual);
        return expectedSet.equals(actualSet);
    }

    private boolean matchOrderingAnswer(QuizSlide slide, AnswerSubmissionRequest answer) {
        List<String> expected = normalizeList(readStringList(slide.getCorrectAnswersJson()));
        List<String> actual = normalizeList(answer.getOrderedItems() == null ? List.of() : answer.getOrderedItems());
        return expected.equals(actual);
    }

    private boolean matchTextAnswer(QuizSlide slide, AnswerSubmissionRequest answer) {
        String text = answer.getTextAnswer();
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalizedAnswer = text.trim().toLowerCase(Locale.ROOT);
        return readStringList(slide.getCorrectAnswersJson()).stream()
                .map(v -> v == null ? "" : v.trim().toLowerCase(Locale.ROOT))
                .anyMatch(normalizedAnswer::equals);
    }

    private PlaySlideResponse toPlaySlide(QuizSlide slide) {
        List<String> options = switch (slide.getType()) {
            case SINGLE_CHOICE, MULTI_CHOICE, ORDERING -> readStringList(slide.getOptionsJson());
            case TEXT -> List.of();
        };
        int wordCount = slide.getQuestionText() == null ? 0 : slide.getQuestionText().trim().split("\\s+").length;
        int revealDurationMs = (int) Math.ceil((wordCount / (double) QUESTION_REVEAL_WPM) * 60_000.0);
        return new PlaySlideResponse(
                slide.getId(),
                slide.getPositionIndex(),
                slide.getType(),
                slide.getQuestionText(),
                slide.getImageUrl(),
                slide.getTimeLimitSeconds(),
                Math.max(revealDurationMs, 0),
                options
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

    private List<String> normalizeList(List<String> values) {
        return values.stream()
                .map(v -> v == null ? "" : v.trim().toLowerCase(Locale.ROOT))
                .toList();
    }
}

package com.quizweb.backend.quiz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizweb.backend.common.exception.ConflictException;
import com.quizweb.backend.common.exception.NotFoundException;
import com.quizweb.backend.quiz.dto.CreateQuizRequest;
import com.quizweb.backend.quiz.dto.CreateQuizResponse;
import com.quizweb.backend.quiz.dto.CreateQuizSlideRequest;
import com.quizweb.backend.quiz.dto.MyQuizResponse;
import com.quizweb.backend.quiz.dto.QuizDetailResponse;
import com.quizweb.backend.quiz.dto.QuizSlideDetailResponse;
import com.quizweb.backend.quiz.dto.QuizSummaryResponse;
import com.quizweb.backend.quiz.dto.UpdateQuizRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final ObjectMapper objectMapper;

    public QuizService(QuizRepository quizRepository, ObjectMapper objectMapper) {
        this.quizRepository = quizRepository;
        this.objectMapper = objectMapper;
    }

    public List<QuizSummaryResponse> getPublicQuizzes() {
        return quizRepository.findByPublishedTrueOrderByIdAsc().stream()
                .map(quiz -> new QuizSummaryResponse(quiz.getId(), quiz.getTitle(), quiz.isPublished()))
                .toList();
    }

    public List<MyQuizResponse> getMyQuizzes(String username) {
        return quizRepository.findByCreatedByIgnoreCaseOrderByIdDesc(username).stream()
                .map(this::toMyQuizResponse)
                .toList();
    }

    public QuizDetailResponse getMyQuizDetail(Long id, String username) {
        Quiz quiz = getOwnedQuiz(id, username);
        List<QuizSlideDetailResponse> slides = quiz.getSlides().stream()
                .sorted(java.util.Comparator.comparingInt(QuizSlide::getPositionIndex))
                .map(this::toSlideDetailResponse)
                .toList();
        return new QuizDetailResponse(quiz.getId(), quiz.getTitle(), quiz.isPublished(), quiz.getCreatedBy(), slides);
    }

    @Transactional
    public CreateQuizResponse createQuiz(CreateQuizRequest request, String username) {
        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle().trim());
        quiz.setPublished(request.getPublished() == null || request.getPublished());
        quiz.setCreatedBy(username);

        int position = 1;
        for (CreateQuizSlideRequest slideRequest : request.getSlides()) {
            quiz.getSlides().add(buildSlide(slideRequest, position++, quiz));
        }

        Quiz saved = quizRepository.save(quiz);
        return new CreateQuizResponse(saved.getId(), saved.getTitle(), saved.getSlides().size(), username);
    }

    @Transactional
    public MyQuizResponse updateQuiz(Long id, UpdateQuizRequest request, String username) {
        Quiz quiz = getOwnedQuiz(id, username);
        quiz.setTitle(request.getTitle().trim());
        quiz.setPublished(request.getPublished());
        return toMyQuizResponse(quizRepository.save(quiz));
    }

    @Transactional
    public QuizDetailResponse replaceQuizContent(Long id, CreateQuizRequest request, String username) {
        Quiz quiz = getOwnedQuiz(id, username);
        quiz.setTitle(request.getTitle().trim());
        quiz.setPublished(request.getPublished() == null || request.getPublished());
        quiz.getSlides().clear();

        int position = 1;
        for (CreateQuizSlideRequest slideRequest : request.getSlides()) {
            quiz.getSlides().add(buildSlide(slideRequest, position++, quiz));
        }

        Quiz saved = quizRepository.save(quiz);
        return getMyQuizDetail(saved.getId(), username);
    }

    @Transactional
    public MyQuizResponse hideQuiz(Long id, String username) {
        Quiz quiz = getOwnedQuiz(id, username);
        quiz.setPublished(false);
        return toMyQuizResponse(quizRepository.save(quiz));
    }

    @Transactional
    public void deleteQuiz(Long id, String username) {
        Quiz quiz = getOwnedQuiz(id, username);
        quizRepository.delete(quiz);
    }

    private QuizSlide buildSlide(CreateQuizSlideRequest slideRequest, int position, Quiz quiz) {
        QuizSlide slide = new QuizSlide();
        slide.setQuiz(quiz);
        slide.setPositionIndex(position);
        slide.setType(slideRequest.getType());
        slide.setQuestionText(slideRequest.getQuestion().trim());
        slide.setImageUrl(normalizeNullable(slideRequest.getImageUrl()));

        switch (slideRequest.getType()) {
            case SINGLE_CHOICE -> validateSingleChoice(slideRequest, slide);
            case MULTI_CHOICE -> validateMultiChoice(slideRequest, slide);
            case ORDERING -> validateOrdering(slideRequest, slide);
            case TEXT -> validateText(slideRequest, slide);
            default -> throw new ConflictException("Unsupported slide type");
        }
        return slide;
    }

    private void validateSingleChoice(CreateQuizSlideRequest slideRequest, QuizSlide slide) {
        List<String> options = normalizeTextList(slideRequest.getOptions());
        List<Integer> indexes = slideRequest.getCorrectOptionIndexes();
        if (options.size() != 4) {
            throw new ConflictException("Single-choice slide must have exactly 4 options");
        }
        if (indexes == null || indexes.size() != 1) {
            throw new ConflictException("Single-choice slide must have exactly 1 correct option");
        }
        int index = indexes.get(0);
        if (index < 0 || index > 3) {
            throw new ConflictException("Single-choice correct option index must be from 0 to 3");
        }

        slide.setOptionsJson(toJson(options));
        slide.setCorrectAnswersJson(toJson(List.of(index)));
    }

    private void validateMultiChoice(CreateQuizSlideRequest slideRequest, QuizSlide slide) {
        List<String> options = normalizeTextList(slideRequest.getOptions());
        List<Integer> indexes = slideRequest.getCorrectOptionIndexes();
        if (options.size() != 4) {
            throw new ConflictException("Multi-choice slide must have exactly 4 options");
        }
        if (indexes == null || indexes.isEmpty()) {
            throw new ConflictException("Multi-choice slide must have at least 1 correct option");
        }
        Set<Integer> unique = new LinkedHashSet<>(indexes);
        if (unique.size() != indexes.size()) {
            throw new ConflictException("Multi-choice correct options cannot contain duplicates");
        }
        boolean allValid = indexes.stream().allMatch(index -> index >= 0 && index <= 3);
        if (!allValid) {
            throw new ConflictException("Multi-choice correct option index must be from 0 to 3");
        }

        slide.setOptionsJson(toJson(options));
        slide.setCorrectAnswersJson(toJson(indexes));
    }

    private void validateOrdering(CreateQuizSlideRequest slideRequest, QuizSlide slide) {
        List<String> orderingItems = normalizeTextList(slideRequest.getOrderingItems());
        if (orderingItems.size() < 2) {
            throw new ConflictException("Ordering slide must have at least 2 ordered items");
        }

        slide.setOptionsJson(toJson(orderingItems));
        slide.setCorrectAnswersJson(toJson(orderingItems));
    }

    private void validateText(CreateQuizSlideRequest slideRequest, QuizSlide slide) {
        List<String> answers = normalizeTextList(slideRequest.getAcceptedAnswers());
        if (answers.isEmpty()) {
            throw new ConflictException("Text slide must have at least 1 accepted answer");
        }

        slide.setOptionsJson(null);
        slide.setCorrectAnswersJson(toJson(answers));
    }

    private List<String> normalizeTextList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(this::normalizeNullable)
                .filter(v -> v != null && !v.isBlank())
                .toList();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new ConflictException("Could not serialize slide data");
        }
    }

    private Quiz getOwnedQuiz(Long id, String username) {
        return quizRepository.findByIdAndCreatedByIgnoreCase(id, username)
                .orElseThrow(() -> new NotFoundException("Quiz not found or you do not have permission"));
    }

    private MyQuizResponse toMyQuizResponse(Quiz quiz) {
        return new MyQuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.isPublished(),
                quiz.getSlides() == null ? 0 : quiz.getSlides().size()
        );
    }

    private QuizSlideDetailResponse toSlideDetailResponse(QuizSlide slide) {
        return switch (slide.getType()) {
            case SINGLE_CHOICE, MULTI_CHOICE -> new QuizSlideDetailResponse(
                    slide.getId(),
                    slide.getPositionIndex(),
                    slide.getType(),
                    slide.getQuestionText(),
                    slide.getImageUrl(),
                    readStringList(slide.getOptionsJson()),
                    readIntegerList(slide.getCorrectAnswersJson()),
                    null,
                    null
            );
            case ORDERING -> new QuizSlideDetailResponse(
                    slide.getId(),
                    slide.getPositionIndex(),
                    slide.getType(),
                    slide.getQuestionText(),
                    slide.getImageUrl(),
                    null,
                    null,
                    readStringList(slide.getOptionsJson()),
                    null
            );
            case TEXT -> new QuizSlideDetailResponse(
                    slide.getId(),
                    slide.getPositionIndex(),
                    slide.getType(),
                    slide.getQuestionText(),
                    slide.getImageUrl(),
                    null,
                    null,
                    null,
                    readStringList(slide.getCorrectAnswersJson())
            );
        };
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

    @Service
    static class QuizDataSeeder implements CommandLineRunner {

        private final QuizRepository quizRepository;

        QuizDataSeeder(QuizRepository quizRepository) {
            this.quizRepository = quizRepository;
        }

        @Override
        public void run(String... args) {
            if (quizRepository.count() > 0) {
                return;
            }

            Quiz javaBasics = new Quiz();
            javaBasics.setTitle("Java Basics");
            javaBasics.setPublished(true);
            javaBasics.setCreatedBy("system");

            Quiz springIntro = new Quiz();
            springIntro.setTitle("Spring Boot Intro");
            springIntro.setPublished(true);
            springIntro.setCreatedBy("system");

            quizRepository.saveAll(List.of(javaBasics, springIntro));
        }
    }
}

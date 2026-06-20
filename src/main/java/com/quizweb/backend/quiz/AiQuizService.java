package com.quizweb.backend.quiz;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizweb.backend.common.exception.ConflictException;
import com.quizweb.backend.quiz.dto.AiGenerateRequest;
import com.quizweb.backend.quiz.dto.CreateQuizResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiQuizService {

    private final QuizRepository quizRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.groq.api-key}")
    private String apiKey;

    @Value("${app.groq.url}")
    private String apiUrl;

    public AiQuizService(QuizRepository quizRepository, ObjectMapper objectMapper) {
        this.quizRepository = quizRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CreateQuizResponse generateQuizWithAi(AiGenerateRequest request, String username) {
        List<GeneratedSlideDto> generatedSlides;

        // Nếu API Key là MOCK hoặc không có, tự động chuyển sang chế độ Giả lập (Mock Mode)
        if (apiKey == null || apiKey.trim().isEmpty() || "MOCK".equalsIgnoreCase(apiKey.trim())) {
            System.out.println(">>> Đang chạy sinh Quiz tự động ở chế độ MOCK (Giả lập) theo chủ đề...");
            generatedSlides = generateMockSlides(request.getQuizTitle(), request.getNumberOfQuestions());
        } else {
            System.out.println(">>> Đang gửi yêu cầu sinh câu hỏi theo chủ đề tới Groq AI...");
            generatedSlides = callGroqApi(request.getQuizTitle(), request.getNumberOfQuestions());
        }

        if (generatedSlides.isEmpty()) {
            throw new ConflictException("Không thể tạo được câu hỏi nào cho chủ đề này. Vui lòng thử lại với tên chủ đề rõ ràng hơn.");
        }

        // Tạo đối tượng Quiz mới
        Quiz quiz = new Quiz();
        quiz.setTitle(request.getQuizTitle().trim());
        quiz.setPublished(true);
        quiz.setCreatedBy(username != null ? username : "system");
        quiz.setMode(QuizMode.TIME); // Chế độ giới hạn thời gian mỗi câu hỏi
        quiz.setLifecycleStatus(QuizLifecycleStatus.PUBLISHED);
        quiz.setAccessScope(QuizAccessScope.PUBLIC);
        
        Instant now = Instant.now();
        quiz.setCreatedAt(now);
        quiz.setUpdatedAt(now);
        quiz.setPublishedAt(now);
        
        // Generate a clean slug
        String cleanSlug = request.getQuizTitle().toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
        if (cleanSlug.length() > 80) {
            cleanSlug = cleanSlug.substring(0, 80);
        }
        quiz.setSlug(cleanSlug);
        quiz.setDescription("Quiz tự động được tạo bởi trợ lý AI xoay quanh chủ đề: " + request.getQuizTitle());

        // Tạo các slide câu hỏi và đính kèm vào Quiz
        int position = 1;
        for (GeneratedSlideDto slideDto : generatedSlides) {
            QuizSlide slide = new QuizSlide();
            slide.setQuiz(quiz);
            slide.setPositionIndex(position++);
            slide.setType(SlideType.SINGLE_CHOICE);
            slide.setQuestionText(slideDto.getQuestion().trim());
            slide.setTimeLimitSeconds(request.getTimeLimitSeconds() != null ? request.getTimeLimitSeconds() : 5);
            
            try {
                slide.setOptionsJson(objectMapper.writeValueAsString(slideDto.getOptions()));
                slide.setCorrectAnswersJson(objectMapper.writeValueAsString(slideDto.getCorrectOptionIndexes()));
            } catch (Exception e) {
                throw new ConflictException("Lỗi serialize dữ liệu câu hỏi AI: " + e.getMessage());
            }

            quiz.getSlides().add(slide);
        }

        Quiz savedQuiz = quizRepository.save(quiz);
        return new CreateQuizResponse(savedQuiz.getId(), savedQuiz.getTitle(), savedQuiz.getSlides().size(), quiz.getCreatedBy());
    }

    private List<GeneratedSlideDto> callGroqApi(String topic, int count) {
        String fullUrl = apiUrl;

        // Xây dựng prompt sinh câu hỏi trực tiếp dựa trên tên chủ đề nhập vào
        String prompt = "Bạn là một trợ lý chuyên gia tạo câu hỏi trắc nghiệm.\n" +
                "Nhiệm vụ của bạn là soạn ra chính xác đúng " + count + " câu hỏi trắc nghiệm xoay quanh chủ đề: \"" + topic + "\".\n" +
                "Mỗi câu hỏi phải có đúng 4 lựa chọn (chỉ có duy nhất 1 lựa chọn đúng) và xoay quanh các kiến thức, khía cạnh lịch sử/khoa học/đặc trưng khác nhau của chủ đề này.\n\n" +
                "Yêu cầu định dạng đầu ra bắt buộc:\n" +
                "Bạn PHẢI trả về kết quả dưới dạng một mảng JSON thuần túy (JSON array). Không bao gồm các ký tự định dạng markdown như ```json hoặc bất kỳ văn bản giải thích nào khác ngoài chuỗi JSON.\n" +
                "Mỗi phần tử trong mảng đại diện cho một câu hỏi và phải tuân thủ chính xác cấu trúc sau:\n" +
                "{\n" +
                "  \"question\": \"Nội dung câu hỏi xoay quanh chủ đề...\",\n" +
                "  \"options\": [\n" +
                "    \"Phương án A (chỉ số 0)\",\n" +
                "    \"Phương án B (chỉ số 1)\",\n" +
                "    \"Phương án C (chỉ số 2)\",\n" +
                "    \"Phương án D (chỉ số 3)\"\n" +
                "  ],\n" +
                "  \"correctOptionIndexes\": [chỉ_số_đúng_từ_0_đến_3]\n" +
                "}";

        // Tạo payload tương thích OpenAI cho Groq API
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("role", "user");
        messageMap.put("content", prompt);

        Map<String, Object> responseFormat = new HashMap<>();
        responseFormat.put("type", "json_object");

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "llama-3.1-8b-instant");
        payload.put("messages", List.of(messageMap));
        payload.put("response_format", responseFormat);
        payload.put("temperature", 0.2);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey); // Truyền Groq key dưới dạng Bearer Token
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(fullUrl, entity, Map.class);
            if (response.getBody() == null) {
                throw new ConflictException("Không nhận được phản hồi từ Groq API.");
            }

            // Trích xuất văn bản phản hồi từ cấu trúc: choices[0].message.content
            List<Map> choices = (List<Map>) response.getBody().get("choices");
            Map choice = choices.get(0);
            Map message = (Map) choice.get("message");
            String rawJson = (String) message.get("content");

            String cleanedJson = cleanJsonText(rawJson);
            
            // OpenAI/Groq đôi khi bọc mảng JSON trong một object, ví dụ: { "questions": [...] } hoặc { "quiz": [...] } hoặc trực tiếp là mảng.
            // Để an toàn, chúng ta parse chuỗi JSON thành JsonNode/Map để kiểm tra trước
            try {
                if (cleanedJson.trim().startsWith("{")) {
                    Map<String, Object> parsedMap = objectMapper.readValue(cleanedJson, new TypeReference<Map<String, Object>>() {});
                    // Tìm key chứa List câu hỏi
                    for (Object value : parsedMap.values()) {
                        if (value instanceof List) {
                            String listString = objectMapper.writeValueAsString(value);
                            return objectMapper.readValue(listString, new TypeReference<List<GeneratedSlideDto>>() {});
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Cảnh báo khi parse cấu trúc bọc JSON: " + ex.getMessage());
            }

            return objectMapper.readValue(cleanedJson, new TypeReference<List<GeneratedSlideDto>>() {});

        } catch (Exception e) {
            System.err.println("Lỗi khi kết nối hoặc parse kết quả từ Groq API: " + e.getMessage());
            throw new ConflictException("Lỗi sinh câu hỏi từ AI: " + e.getMessage());
        }
    }

    private String cleanJsonText(String text) {
        if (text == null || text.isBlank()) return "[]";
        String cleaned = text.trim();
        
        int firstBracket = cleaned.indexOf('[');
        int firstBrace = cleaned.indexOf('{');
        int startIndex = (firstBracket != -1 && firstBrace != -1) 
                ? Math.min(firstBracket, firstBrace) 
                : Math.max(firstBracket, firstBrace);
                
        int lastBracket = cleaned.lastIndexOf(']');
        int lastBrace = cleaned.lastIndexOf('}');
        int endIndex = (lastBracket != -1 && lastBrace != -1) 
                ? Math.max(lastBracket, lastBrace) 
                : Math.max(lastBracket, lastBrace);
                
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return cleaned.substring(startIndex, endIndex + 1);
        }
        
        return cleaned;
    }

    private List<GeneratedSlideDto> generateMockSlides(String topic, int count) {
        List<GeneratedSlideDto> list = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            GeneratedSlideDto slide = new GeneratedSlideDto();
            slide.setQuestion("Câu hỏi trắc nghiệm số " + (i + 1) + " về chủ đề \"" + topic + "\": Bạn hãy chọn nhận định đúng nhất dưới đây?");
            
            List<String> options = new ArrayList<>();
            options.add("Đáp án đúng nhất về kiến thức chủ đề " + topic);
            options.add("Phương án trả lời sai lệch gây nhiễu A");
            options.add("Phương án trả lời sai lệch gây nhiễu B");
            options.add("Phương án trả lời sai lệch gây nhiễu C");
            
            slide.setOptions(options);
            slide.setCorrectOptionIndexes(List.of(0)); // 0 là đáp án đầu tiên đúng
            list.add(slide);
        }
        return list;
    }

    // Lớp DTO nội bộ đại diện cho cấu trúc câu hỏi do AI trả về
    public static class GeneratedSlideDto {
        private String question;
        private List<String> options;
        private List<Integer> correctOptionIndexes;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public List<Integer> getCorrectOptionIndexes() {
            return correctOptionIndexes;
        }

        public void setCorrectOptionIndexes(List<Integer> correctOptionIndexes) {
            this.correctOptionIndexes = correctOptionIndexes;
        }
    }
}

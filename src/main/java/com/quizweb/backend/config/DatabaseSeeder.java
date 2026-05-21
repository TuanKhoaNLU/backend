package com.quizweb.backend.config;

import com.quizweb.backend.quiz.Quiz;
import com.quizweb.backend.quiz.QuizAccessScope;
import com.quizweb.backend.quiz.QuizLifecycleStatus;
import com.quizweb.backend.quiz.QuizMode;
import com.quizweb.backend.quiz.QuizRepository;
import com.quizweb.backend.quiz.QuizSlide;
import com.quizweb.backend.quiz.SlideType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final QuizRepository quizRepository;

    public DatabaseSeeder(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Kiểm tra xem database đã có bất kỳ bộ Quiz nào chưa
        if (quizRepository.count() > 0) {
            System.out.println(">>> Database đã có dữ liệu Quiz. Bỏ qua bước khởi tạo (Seeding).");
            return;
        }

        System.out.println(">>> Bắt đầu nạp 7 bộ Quiz dữ liệu mẫu vào Database...");

        List<Quiz> quizzes = new ArrayList<>();
        Instant now = Instant.now();

        // 1. Art & Literature
        Quiz artQuiz = new Quiz();
        artQuiz.setTitle("Art & Literature");
        artQuiz.setPublished(true);
        artQuiz.setCreatedBy("system");
        artQuiz.setMode(QuizMode.NORMAL);
        artQuiz.setLifecycleStatus(QuizLifecycleStatus.PUBLISHED);
        artQuiz.setAccessScope(QuizAccessScope.PUBLIC);
        artQuiz.setCreatedAt(now);
        artQuiz.setUpdatedAt(now);
        artQuiz.setPublishedAt(now);
        artQuiz.setSlug("art-literature");
        artQuiz.setDescription("Kiểm tra kiến thức của bạn về các tác phẩm nghệ thuật kinh điển và văn học thế giới.");
        
        artQuiz.getSlides().add(createSlide(artQuiz, 1, SlideType.SINGLE_CHOICE, 
                "Ai là tác giả của bức họa Mona Lisa nổi tiếng?", 
                "[\"Leonardo da Vinci\", \"Michelangelo\", \"Raphael\", \"Vincent van Gogh\"]", 
                "[0]"));
        artQuiz.getSlides().add(createSlide(artQuiz, 2, SlideType.SINGLE_CHOICE, 
                "Tiểu thuyết kinh điển \"Don Quixote\" được viết bởi nhà văn nào?", 
                "[\"Miguel de Cervantes\", \"William Shakespeare\", \"Victor Hugo\", \"Leo Tolstoy\"]", 
                "[0]"));
        artQuiz.getSlides().add(createSlide(artQuiz, 3, SlideType.TEXT, 
                "Vở kịch Romeo và Juliet là tác phẩm của nhà văn nào?", 
                null, 
                "[\"William Shakespeare\", \"Shakespeare\", \"william shakespeare\"]"));
        quizzes.add(artQuiz);

        // 2. Entertainment
        Quiz entQuiz = new Quiz();
        entQuiz.setTitle("Entertainment");
        entQuiz.setPublished(true);
        entQuiz.setCreatedBy("system");
        entQuiz.setMode(QuizMode.NORMAL);
        entQuiz.setLifecycleStatus(QuizLifecycleStatus.PUBLISHED);
        entQuiz.setAccessScope(QuizAccessScope.PUBLIC);
        entQuiz.setCreatedAt(now);
        entQuiz.setUpdatedAt(now);
        entQuiz.setPublishedAt(now);
        entQuiz.setSlug("entertainment");
        entQuiz.setDescription("Câu hỏi thú vị về điện ảnh, âm nhạc và thế giới giải trí đầy màu sắc.");

        entQuiz.getSlides().add(createSlide(entQuiz, 1, SlideType.SINGLE_CHOICE, 
                "Bộ phim nào đoạt giải Oscar cho Phim hay nhất năm 2020 và là phim không nói tiếng Anh đầu tiên làm được điều này?", 
                "[\"Parasite (Ký Sinh Trùng)\", \"1917\", \"Joker\", \"Once Upon a Time in Hollywood\"]", 
                "[0]"));
        entQuiz.getSlides().add(createSlide(entQuiz, 2, SlideType.MULTI_CHOICE, 
                "Những diễn viên nào sau đây đã từng thủ vai Batman (Người Dơi) trên màn ảnh rộng? (Chọn các đáp án đúng)", 
                "[\"Christian Bale\", \"Robert Pattinson\", \"Tom Holland\", \"Leonardo DiCaprio\"]", 
                "[0,1]"));
        entQuiz.getSlides().add(createSlide(entQuiz, 3, SlideType.SINGLE_CHOICE, 
                "Ban nhạc rock huyền thoại nào trình diễn ca khúc nổi tiếng \"Bohemian Rhapsody\"?", 
                "[\"Queen\", \"The Beatles\", \"Led Zeppelin\", \"Pink Floyd\"]", 
                "[0]"));
        quizzes.add(entQuiz);

        // 3. Geography
        Quiz geoQuiz = new Quiz();
        geoQuiz.setTitle("Geography");
        geoQuiz.setPublished(true);
        geoQuiz.setCreatedBy("system");
        geoQuiz.setMode(QuizMode.NORMAL);
        geoQuiz.setLifecycleStatus(QuizLifecycleStatus.PUBLISHED);
        geoQuiz.setAccessScope(QuizAccessScope.PUBLIC);
        geoQuiz.setCreatedAt(now);
        geoQuiz.setUpdatedAt(now);
        geoQuiz.setPublishedAt(now);
        geoQuiz.setSlug("geography");
        geoQuiz.setDescription("Khám phá thế giới qua các câu hỏi địa lý về quốc gia, thủ đô và danh lam thắng cảnh.");

        geoQuiz.getSlides().add(createSlide(geoQuiz, 1, SlideType.SINGLE_CHOICE, 
                "Quốc gia nào có diện tích lớn nhất thế giới?", 
                "[\"Nga\", \"Canada\", \"Mỹ\", \"Trung Quốc\"]", 
                "[0]"));
        geoQuiz.getSlides().add(createSlide(geoQuiz, 2, SlideType.SINGLE_CHOICE, 
                "Thủ đô chính thức của nước Úc (Australia) là thành phố nào?", 
                "[\"Canberra\", \"Sydney\", \"Melbourne\", \"Brisbane\"]", 
                "[0]"));
        geoQuiz.getSlides().add(createSlide(geoQuiz, 3, SlideType.ORDERING, 
                "Sắp xếp các quốc gia sau theo thứ tự diện tích từ lớn đến nhỏ:", 
                "[\"Nga\", \"Trung Quốc\", \"Việt Nam\", \"Singapore\"]", 
                "[\"Nga\", \"Trung Quốc\", \"Việt Nam\", \"Singapore\"]"));
        quizzes.add(geoQuiz);

        // 4. History
        Quiz histQuiz = new Quiz();
        histQuiz.setTitle("History");
        histQuiz.setPublished(true);
        histQuiz.setCreatedBy("system");
        histQuiz.setMode(QuizMode.NORMAL);
        histQuiz.setLifecycleStatus(QuizLifecycleStatus.PUBLISHED);
        histQuiz.setAccessScope(QuizAccessScope.PUBLIC);
        histQuiz.setCreatedAt(now);
        histQuiz.setUpdatedAt(now);
        histQuiz.setPublishedAt(now);
        histQuiz.setSlug("history");
        histQuiz.setDescription("Du hành thời gian cùng những sự kiện và nhân vật lịch sử nổi tiếng.");

        histQuiz.getSlides().add(createSlide(histQuiz, 1, SlideType.SINGLE_CHOICE, 
                "Sự kiện lịch sử nào đánh dấu sự kết thúc hoàn toàn của Thế chiến thứ hai?", 
                "[\"Nhật Bản đầu hàng Đồng Minh năm 1945\", \"Đức ký hiệp định đầu hàng năm 1945\", \"Trận Trân Châu Cảng năm 1941\", \"Hội nghị Yalta năm 1945\"]", 
                "[0]"));
        histQuiz.getSlides().add(createSlide(histQuiz, 2, SlideType.TEXT, 
                "Vị hoàng đế cuối cùng của triều đại phong kiến Việt Nam (triều Nguyễn) là ai?", 
                null, 
                "[\"Bao Dai\", \"Bảo Đại\", \"vua Bảo Đại\", \"Vua Bao Dai\"]"));
        histQuiz.getSlides().add(createSlide(histQuiz, 3, SlideType.SINGLE_CHOICE, 
                "Người đầu tiên bay vào vũ trụ là ai?", 
                "[\"Yuri Gagarin\", \"Neil Armstrong\", \"Buzz Aldrin\", \"John Glenn\"]", 
                "[0]"));
        quizzes.add(histQuiz);

        // 5. Science & Nature
        Quiz sciQuiz = new Quiz();
        sciQuiz.setTitle("Science & Nature");
        sciQuiz.setPublished(true);
        sciQuiz.setCreatedBy("system");
        sciQuiz.setMode(QuizMode.NORMAL);
        sciQuiz.setLifecycleStatus(QuizLifecycleStatus.PUBLISHED);
        sciQuiz.setAccessScope(QuizAccessScope.PUBLIC);
        sciQuiz.setCreatedAt(now);
        sciQuiz.setUpdatedAt(now);
        sciQuiz.setPublishedAt(now);
        sciQuiz.setSlug("science-nature");
        sciQuiz.setDescription("Giải mã các hiện tượng tự nhiên và kiến thức khoa học đời sống lý thú.");

        sciQuiz.getSlides().add(createSlide(sciQuiz, 1, SlideType.SINGLE_CHOICE, 
                "Hành tinh nào nằm gần Mặt Trời nhất trong Hệ Mặt Trời?", 
                "[\"Sao Thủy\", \"Sao Kim\", \"Trái Đất\", \"Sao Hỏa\"]", 
                "[0]"));
        sciQuiz.getSlides().add(createSlide(sciQuiz, 2, SlideType.MULTI_CHOICE, 
                "Những nguyên tố nào sau đây là kim loại ở điều kiện nhiệt độ phòng bình thường? (Chọn các đáp án đúng)", 
                "[\"Sắt\", \"Đồng\", \"Khí Oxy\", \"Khí Nitơ\"]", 
                "[0,1]"));
        sciQuiz.getSlides().add(createSlide(sciQuiz, 3, SlideType.SINGLE_CHOICE, 
                "Công thức hóa học của nước tinh khiết là gì?", 
                "[\"H2O\", \"CO2\", \"NaCl\", \"O2\"]", 
                "[0]"));
        quizzes.add(sciQuiz);

        // 6. Sports
        Quiz sportQuiz = new Quiz();
        sportQuiz.setTitle("Sports");
        sportQuiz.setPublished(true);
        sportQuiz.setCreatedBy("system");
        sportQuiz.setMode(QuizMode.NORMAL);
        sportQuiz.setLifecycleStatus(QuizLifecycleStatus.PUBLISHED);
        sportQuiz.setAccessScope(QuizAccessScope.PUBLIC);
        sportQuiz.setCreatedAt(now);
        sportQuiz.setUpdatedAt(now);
        sportQuiz.setPublishedAt(now);
        sportQuiz.setSlug("sports");
        sportQuiz.setDescription("Thử tài hiểu biết của bạn về bóng đá, thế vận hội Olympic và các môn thể thao phổ biến.");

        sportQuiz.getSlides().add(createSlide(sportQuiz, 1, SlideType.SINGLE_CHOICE, 
                "Quốc gia nào hiện tại đang nắm giữ kỷ lục vô địch World Cup bóng đá nam nhiều nhất lịch sử?", 
                "[\"Brazil\", \"Đức\", \"Ý\", \"Argentina\"]", 
                "[0]"));
        sportQuiz.getSlides().add(createSlide(sportQuiz, 2, SlideType.SINGLE_CHOICE, 
                "Thế vận hội Olympic mùa hè được tổ chức định kỳ mấy năm một lần?", 
                "[\"4 năm\", \"2 năm\", \"3 năm\", \"5 năm\"]", 
                "[0]"));
        sportQuiz.getSlides().add(createSlide(sportQuiz, 3, SlideType.TEXT, 
                "Huyền thoại bóng rổ nào được mệnh danh là \"His Airness\" và từng mang số áo 23 của Chicago Bulls?", 
                null, 
                "[\"Michael Jordan\", \"Jordan\", \"michael jordan\"]"));
        quizzes.add(sportQuiz);

        // 7. Trivia
        Quiz triviaQuiz = new Quiz();
        triviaQuiz.setTitle("Trivia");
        triviaQuiz.setPublished(true);
        triviaQuiz.setCreatedBy("system");
        triviaQuiz.setMode(QuizMode.NORMAL);
        triviaQuiz.setLifecycleStatus(QuizLifecycleStatus.PUBLISHED);
        triviaQuiz.setAccessScope(QuizAccessScope.PUBLIC);
        triviaQuiz.setCreatedAt(now);
        triviaQuiz.setUpdatedAt(now);
        triviaQuiz.setPublishedAt(now);
        triviaQuiz.setSlug("trivia");
        triviaQuiz.setDescription("Những câu hỏi kiến thức tổng hợp đa dạng chủ đề thú vị hàng ngày.");

        triviaQuiz.getSlides().add(createSlide(triviaQuiz, 1, SlideType.SINGLE_CHOICE, 
                "Mật mã PIN điện thoại mặc định phổ biến thường có bao nhiêu chữ số?", 
                "[\"4 chữ số\", \"6 chữ số\", \"8 chữ số\", \"5 chữ số\"]", 
                "[0]"));
        triviaQuiz.getSlides().add(createSlide(triviaQuiz, 2, SlideType.SINGLE_CHOICE, 
                "Cầu vồng tiêu chuẩn có bao nhiêu màu sắc cơ bản?", 
                "[\"7 màu\", \"6 màu\", \"8 màu\", \"5 màu\"]", 
                "[0]"));
        triviaQuiz.getSlides().add(createSlide(triviaQuiz, 3, SlideType.MULTI_CHOICE, 
                "Những quốc gia nào sau đây thuộc khu vực Đông Nam Á? (Chọn các đáp án đúng)", 
                "[\"Việt Nam\", \"Thái Lan\", \"Hàn Quốc\", \"Nhật Bản\"]", 
                "[0,1]"));
        quizzes.add(triviaQuiz);

        // Lưu tất cả các quiz mẫu cùng với các slide (do CascadeType.ALL cấu hình trên thực thể Quiz)
        quizRepository.saveAll(quizzes);
        System.out.println(">>> Đã nạp thành công 7 bộ Quiz mẫu với đầy đủ thuộc tính vào database!");
    }

    private QuizSlide createSlide(Quiz quiz, int positionIndex, SlideType type, String questionText, String optionsJson, String correctAnswersJson) {
        QuizSlide slide = new QuizSlide();
        slide.setQuiz(quiz);
        slide.setPositionIndex(positionIndex);
        slide.setType(type);
        slide.setQuestionText(questionText);
        slide.setOptionsJson(optionsJson);
        slide.setCorrectAnswersJson(correctAnswersJson);
        return slide;
    }
}

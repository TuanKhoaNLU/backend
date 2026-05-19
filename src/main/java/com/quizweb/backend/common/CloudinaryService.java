package com.quizweb.backend.common;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Service xử lý upload file ảnh lên Cloudinary.
 * Ảnh được lưu trong folder "quiz_web/avatars", tự động crop vuông 256×256
 * với nhận diện khuôn mặt (g_face), tối ưu chất lượng và format tự động.
 */
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload một MultipartFile lên Cloudinary.
     *
     * @param file ảnh người dùng chọn (JPG, PNG, WEBP, …)
     * @return secure_url HTTPS trỏ tới ảnh đã được xử lý
     * @throws RuntimeException nếu upload thất bại (IOException hoặc API error)
     */
    public String uploadFile(MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder",          "quiz_web/avatars",
                            "resource_type",   "image",
                            "transformation",  "c_fill,g_face,h_256,w_256,q_auto,f_auto",
                            "overwrite",       true
                    )
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Upload ảnh lên Cloudinary thất bại: " + e.getMessage(), e);
        }
    }
}

package com.quizweb.backend.user;

import com.quizweb.backend.common.CloudinaryService;
import com.quizweb.backend.user.dto.UpdateProfileRequest;
import com.quizweb.backend.user.dto.UserProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    public UserController(UserService userService, CloudinaryService cloudinaryService) {
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.getProfile(username));
    }

    /**
     * Cập nhật profile người dùng.
     * <p>
     * Endpoint chấp nhận multipart/form-data với các trường:
     * - fullName      (text, tuỳ chọn)
     * - phoneNumber   (text, tuỳ chọn)
     * - avatarFile    (file ảnh, tuỳ chọn — sẽ upload lên Cloudinary)
     * - avatarUrl     (text, tuỳ chọn — dùng khi chọn ảnh minh hoạ Dicebear có sẵn)
     * <p>
     * Ưu tiên: nếu có avatarFile → upload Cloudinary lấy URL;
     * nếu không có file nhưng có avatarUrl → dùng URL trực tiếp;
     * nếu cả hai đều trống → giữ nguyên avatar hiện tại.
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            @RequestParam(value = "avatarUrl", required = false) String avatarUrl
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Quyết định avatarUrl cuối cùng
        String resolvedAvatarUrl;
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Upload file thật lên Cloudinary, nhận lại HTTPS URL ngắn
            resolvedAvatarUrl = cloudinaryService.uploadFile(avatarFile);
        } else if (avatarUrl != null && !avatarUrl.isBlank()) {
            // Người dùng chọn ảnh Dicebear có sẵn → dùng URL trực tiếp
            resolvedAvatarUrl = avatarUrl;
        } else {
            // Không thay đổi avatar → truyền null để service giữ nguyên giá trị cũ
            resolvedAvatarUrl = null;
        }

        UpdateProfileRequest request = new UpdateProfileRequest(fullName, phoneNumber, resolvedAvatarUrl);
        return ResponseEntity.ok(userService.updateProfile(username, request));
    }
}

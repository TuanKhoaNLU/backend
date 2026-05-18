package com.quizweb.backend.user;

import com.quizweb.backend.user.dto.UpdateProfileRequest;
import com.quizweb.backend.user.dto.UserProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.getProfile(username));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.updateProfile(username, request));
    }
}

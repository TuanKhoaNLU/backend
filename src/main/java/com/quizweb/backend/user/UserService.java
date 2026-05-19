package com.quizweb.backend.user;

import com.quizweb.backend.common.exception.NotFoundException;
import com.quizweb.backend.user.dto.UpdateProfileRequest;
import com.quizweb.backend.user.dto.UserProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserAccountRepository userAccountRepository;

    public UserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String username) {
        UserAccount account = userAccountRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return new UserProfileResponse(
                account.getUsername(),
                account.getFullName(),
                account.getPhoneNumber(),
                account.getAvatarUrl(),
                account.getRole()
        );
    }

    @Transactional
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        UserAccount account = userAccountRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        account.setFullName(request.fullName());
        account.setPhoneNumber(request.phoneNumber());
        // Chỉ cập nhật avatar khi có giá trị mới (null = giữ nguyên ảnh cũ)
        if (request.avatarUrl() != null) {
            account.setAvatarUrl(request.avatarUrl());
        }
        
        userAccountRepository.save(account);
        
        return new UserProfileResponse(
                account.getUsername(),
                account.getFullName(),
                account.getPhoneNumber(),
                account.getAvatarUrl(),
                account.getRole()
        );
    }
}

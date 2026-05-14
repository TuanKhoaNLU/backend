package com.quizweb.backend.live.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JoinLiveSessionRequest {

    @NotBlank(message = "pin is required")
    @Size(min = 4, max = 12, message = "pin length must be 4-12")
    private String pin;

    @NotBlank(message = "displayName is required")
    @Size(min = 2, max = 30, message = "displayName must be 2-30 characters")
    private String displayName;

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

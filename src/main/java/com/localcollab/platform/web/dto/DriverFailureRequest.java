package com.localcollab.platform.web.dto;

import jakarta.validation.constraints.NotBlank;

public class DriverFailureRequest {
    @NotBlank(message = "reason is required")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

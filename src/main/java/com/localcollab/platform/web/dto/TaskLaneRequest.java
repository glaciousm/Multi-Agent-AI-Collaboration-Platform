package com.localcollab.platform.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class TaskLaneRequest {
    @NotBlank(message = "name is required")
    private String name;
    @NotNull(message = "implementorId is required")
    private UUID implementorId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getImplementorId() {
        return implementorId;
    }

    public void setImplementorId(UUID implementorId) {
        this.implementorId = implementorId;
    }
}

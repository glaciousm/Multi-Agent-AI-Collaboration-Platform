package com.localcollab.platform.web.dto;

import jakarta.validation.constraints.NotBlank;

public class RoomRequest {
    @NotBlank(message = "name is required")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

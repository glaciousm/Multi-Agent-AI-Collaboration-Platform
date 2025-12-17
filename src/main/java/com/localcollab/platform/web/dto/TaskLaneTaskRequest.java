package com.localcollab.platform.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class TaskLaneTaskRequest {
    @NotNull(message = "taskArtifactId is required")
    private UUID taskArtifactId;

    public UUID getTaskArtifactId() {
        return taskArtifactId;
    }

    public void setTaskArtifactId(UUID taskArtifactId) {
        this.taskArtifactId = taskArtifactId;
    }
}

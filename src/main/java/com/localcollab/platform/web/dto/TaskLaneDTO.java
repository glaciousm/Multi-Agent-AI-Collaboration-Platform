package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.TaskLaneState;

import java.util.List;
import java.util.UUID;

public class TaskLaneDTO {
    private UUID id;
    private String name;
    private UUID implementorId;
    private TaskLaneState state;
    private List<UUID> taskArtifactIds;

    public TaskLaneDTO(UUID id, String name, UUID implementorId, TaskLaneState state, List<UUID> taskArtifactIds) {
        this.id = id;
        this.name = name;
        this.implementorId = implementorId;
        this.state = state;
        this.taskArtifactIds = taskArtifactIds;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getImplementorId() {
        return implementorId;
    }

    public TaskLaneState getState() {
        return state;
    }

    public List<UUID> getTaskArtifactIds() {
        return taskArtifactIds;
    }
}

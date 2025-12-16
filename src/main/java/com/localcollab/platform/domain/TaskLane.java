package com.localcollab.platform.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TaskLane {
    private UUID id;
    private String name;
    private UUID implementorId;
    private TaskLaneState state;
    private List<UUID> taskArtifactIds = new ArrayList<>();

    public TaskLane() {
        // for serialization
    }

    public TaskLane(UUID id, String name, UUID implementorId, TaskLaneState state, List<UUID> taskArtifactIds) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.implementorId = Objects.requireNonNull(implementorId, "implementorId");
        this.state = state == null ? TaskLaneState.ACTIVE : state;
        if (taskArtifactIds != null) {
            this.taskArtifactIds = new ArrayList<>(taskArtifactIds);
        }
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

    public void addTask(UUID taskId) {
        taskArtifactIds.add(taskId);
    }

    public void updateState(TaskLaneState newState) {
        if (newState == null) {
            throw new IllegalArgumentException("Task lane state cannot be null");
        }
        this.state = newState;
    }
}

package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.TaskLaneState;

import java.time.Instant;
import java.util.UUID;

public class TaskDTO {
    private UUID id;
    private String title;
    private String content;
    private int version;
    private Instant createdAt;
    private UUID parentArtifactId;
    private UUID laneId;
    private String laneName;
    private TaskLaneState laneState;
    private UUID implementorId;

    public TaskDTO(UUID id,
                   String title,
                   String content,
                   int version,
                   Instant createdAt,
                   UUID parentArtifactId,
                   UUID laneId,
                   String laneName,
                   TaskLaneState laneState,
                   UUID implementorId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.version = version;
        this.createdAt = createdAt;
        this.parentArtifactId = parentArtifactId;
        this.laneId = laneId;
        this.laneName = laneName;
        this.laneState = laneState;
        this.implementorId = implementorId;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getParentArtifactId() {
        return parentArtifactId;
    }

    public UUID getLaneId() {
        return laneId;
    }

    public String getLaneName() {
        return laneName;
    }

    public TaskLaneState getLaneState() {
        return laneState;
    }

    public UUID getImplementorId() {
        return implementorId;
    }
}

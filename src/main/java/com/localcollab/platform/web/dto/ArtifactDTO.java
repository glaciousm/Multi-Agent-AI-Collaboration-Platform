package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.ArtifactType;

import java.time.Instant;
import java.util.UUID;

public class ArtifactDTO {
    private UUID id;
    private ArtifactType type;
    private String title;
    private String content;
    private int version;
    private Instant createdAt;
    private UUID parentArtifactId;

    public ArtifactDTO(UUID id, ArtifactType type, String title, String content, int version, Instant createdAt, UUID parentArtifactId) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.content = content;
        this.version = version;
        this.createdAt = createdAt;
        this.parentArtifactId = parentArtifactId;
    }

    public UUID getId() {
        return id;
    }

    public ArtifactType getType() {
        return type;
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
}

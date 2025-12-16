package com.localcollab.platform.domain;

import java.time.Instant;
import java.util.UUID;

public class Artifact {
    private UUID id;
    private ArtifactType type;
    private String title;
    private String content;
    private int version;
    private Instant createdAt;

    public Artifact() {
        // for serialization
    }

    public Artifact(UUID id, ArtifactType type, String title, String content, int version, Instant createdAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.content = content;
        this.version = version;
        this.createdAt = createdAt;
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
}

package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.ArtifactType;
import jakarta.validation.constraints.NotBlank;

public class ArtifactRequest {
    private ArtifactType type;
    @NotBlank(message = "title is required")
    private String title;
    @NotBlank(message = "content is required")
    private String content;
    private java.util.UUID parentArtifactId;

    public ArtifactType getType() {
        return type;
    }

    public void setType(ArtifactType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public java.util.UUID getParentArtifactId() {
        return parentArtifactId;
    }

    public void setParentArtifactId(java.util.UUID parentArtifactId) {
        this.parentArtifactId = parentArtifactId;
    }
}

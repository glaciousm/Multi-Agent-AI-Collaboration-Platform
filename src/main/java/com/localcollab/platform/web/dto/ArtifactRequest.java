package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.ArtifactType;

public class ArtifactRequest {
    private ArtifactType type;
    private String title;
    private String content;
    private java.util.UUID parentArtifactId;

    public ArtifactType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public java.util.UUID getParentArtifactId() {
        return parentArtifactId;
    }
}

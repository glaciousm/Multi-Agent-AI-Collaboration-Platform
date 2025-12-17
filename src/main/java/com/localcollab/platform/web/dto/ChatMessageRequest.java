package com.localcollab.platform.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class ChatMessageRequest {

    private UUID participantId;
    @NotBlank(message = "content is required")
    private String content;

    public UUID getParticipantId() {
        return participantId;
    }

    public void setParticipantId(UUID participantId) {
        this.participantId = participantId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

package com.localcollab.platform.domain;

import java.time.Instant;
import java.util.UUID;

public class ChatMessage {

    private UUID id;
    private UUID participantId;
    private String participantName;
    private String content;
    private Instant createdAt;

    public ChatMessage() {
        // for serialization
    }

    public ChatMessage(UUID id, UUID participantId, String participantName, String content, Instant createdAt) {
        this.id = id;
        this.participantId = participantId;
        this.participantName = participantName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getParticipantId() {
        return participantId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

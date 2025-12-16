package com.localcollab.platform.web.dto;

import java.util.UUID;

public class ChatMessageRequest {

    private UUID participantId;
    private String content;

    public UUID getParticipantId() {
        return participantId;
    }

    public String getContent() {
        return content;
    }
}

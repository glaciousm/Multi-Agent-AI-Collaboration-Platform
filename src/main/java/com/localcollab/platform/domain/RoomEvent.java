package com.localcollab.platform.domain;

import java.time.Instant;
import java.util.UUID;

public class RoomEvent {
    private UUID id;
    private UUID roomId;
    private RoomEventType type;
    private String description;
    private Instant occurredAt;
    private UUID participantId;
    private UUID artifactId;
    private UUID taskLaneId;

    public RoomEvent() {
        // for serialization
    }

    public RoomEvent(UUID id,
                     UUID roomId,
                     RoomEventType type,
                     String description,
                     Instant occurredAt,
                     UUID participantId,
                     UUID artifactId,
                     UUID taskLaneId) {
        this.id = id;
        this.roomId = roomId;
        this.type = type;
        this.description = description;
        this.occurredAt = occurredAt;
        this.participantId = participantId;
        this.artifactId = artifactId;
        this.taskLaneId = taskLaneId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public RoomEventType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public UUID getParticipantId() {
        return participantId;
    }

    public UUID getArtifactId() {
        return artifactId;
    }

    public UUID getTaskLaneId() {
        return taskLaneId;
    }
}

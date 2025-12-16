package com.localcollab.platform.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class RoomSummary {
    private UUID roomId;
    private String roomName;
    private Map<ParticipantRole, Long> participantsByRole;
    private Map<ParticipantType, Long> participantsByType;
    private Map<ArtifactType, Long> artifactsByType;
    private Map<TaskLaneState, Long> taskLanesByState;
    private int messageCount;
    private DriverStatus driverStatusSnapshot;

    public RoomSummary() {
        // for serialization
    }

    public RoomSummary(UUID roomId,
                       String roomName,
                       Map<ParticipantRole, Long> participantsByRole,
                       Map<ParticipantType, Long> participantsByType,
                       Map<ArtifactType, Long> artifactsByType,
                       Map<TaskLaneState, Long> taskLanesByState,
                       int messageCount,
                       DriverStatus driverStatusSnapshot) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.participantsByRole = participantsByRole == null ? new EnumMap<>(ParticipantRole.class) : new EnumMap<>(participantsByRole);
        this.participantsByType = participantsByType == null ? new EnumMap<>(ParticipantType.class) : new EnumMap<>(participantsByType);
        this.artifactsByType = artifactsByType == null ? new EnumMap<>(ArtifactType.class) : new EnumMap<>(artifactsByType);
        this.taskLanesByState = taskLanesByState == null ? new EnumMap<>(TaskLaneState.class) : new EnumMap<>(taskLanesByState);
        this.messageCount = messageCount;
        this.driverStatusSnapshot = driverStatusSnapshot;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public Map<ParticipantRole, Long> getParticipantsByRole() {
        return participantsByRole;
    }

    public Map<ParticipantType, Long> getParticipantsByType() {
        return participantsByType;
    }

    public Map<ArtifactType, Long> getArtifactsByType() {
        return artifactsByType;
    }

    public Map<TaskLaneState, Long> getTaskLanesByState() {
        return taskLanesByState;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public DriverStatus getDriverStatusSnapshot() {
        return driverStatusSnapshot;
    }
}

package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.ArtifactType;
import com.localcollab.platform.domain.DriverStatus;
import com.localcollab.platform.domain.ParticipantRole;
import com.localcollab.platform.domain.ParticipantType;
import com.localcollab.platform.domain.TaskLaneState;

import java.util.Map;
import java.util.UUID;

public class RoomSummaryDTO {
    private UUID roomId;
    private String roomName;
    private Map<ParticipantRole, Long> participantsByRole;
    private Map<ParticipantType, Long> participantsByType;
    private Map<ArtifactType, Long> artifactsByType;
    private Map<TaskLaneState, Long> taskLanesByState;
    private int messageCount;
    private DriverStatus driverStatusSnapshot;

    public RoomSummaryDTO(UUID roomId,
                          String roomName,
                          Map<ParticipantRole, Long> participantsByRole,
                          Map<ParticipantType, Long> participantsByType,
                          Map<ArtifactType, Long> artifactsByType,
                          Map<TaskLaneState, Long> taskLanesByState,
                          int messageCount,
                          DriverStatus driverStatusSnapshot) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.participantsByRole = participantsByRole;
        this.participantsByType = participantsByType;
        this.artifactsByType = artifactsByType;
        this.taskLanesByState = taskLanesByState;
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

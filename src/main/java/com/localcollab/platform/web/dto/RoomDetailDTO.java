package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.DriverStatus;
import com.localcollab.platform.domain.RoomEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class RoomDetailDTO {
    private UUID id;
    private String name;
    private Instant createdAt;
    private boolean paused;
    private DriverStatus driverStatus;
    private List<ParticipantDTO> participants;
    private List<ArtifactDTO> artifacts;
    private List<TaskDTO> tasks;
    private List<TaskLaneDTO> taskLanes;
    private List<ProviderAdapterDTO> providerAdapters;
    private List<RoomEvent> events;

    public RoomDetailDTO(UUID id,
                         String name,
                         Instant createdAt,
                         boolean paused,
                         DriverStatus driverStatus,
                         List<ParticipantDTO> participants,
                         List<ArtifactDTO> artifacts,
                         List<TaskDTO> tasks,
                         List<TaskLaneDTO> taskLanes,
                         List<ProviderAdapterDTO> providerAdapters,
                         List<RoomEvent> events) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.paused = paused;
        this.driverStatus = driverStatus;
        this.participants = participants;
        this.artifacts = artifacts;
        this.tasks = tasks;
        this.taskLanes = taskLanes;
        this.providerAdapters = providerAdapters;
        this.events = events;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isPaused() {
        return paused;
    }

    public DriverStatus getDriverStatus() {
        return driverStatus;
    }

    public List<ParticipantDTO> getParticipants() {
        return participants;
    }

    public List<ArtifactDTO> getArtifacts() {
        return artifacts;
    }

    public List<TaskDTO> getTasks() {
        return tasks;
    }

    public List<TaskLaneDTO> getTaskLanes() {
        return taskLanes;
    }

    public List<ProviderAdapterDTO> getProviderAdapters() {
        return providerAdapters;
    }

    public List<RoomEvent> getEvents() {
        return events;
    }
}

package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.Artifact;
import com.localcollab.platform.domain.ArtifactType;
import com.localcollab.platform.domain.Room;
import com.localcollab.platform.domain.RoomSummary;
import com.localcollab.platform.domain.TaskLane;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public final class RoomDtoMapper {

    private RoomDtoMapper() {
    }

    public static List<RoomSummaryDTO> toRoomSummaries(List<RoomSummary> summaries) {
        return summaries.stream()
                .map(RoomDtoMapper::toRoomSummary)
                .toList();
    }

    public static RoomSummaryDTO toRoomSummary(RoomSummary summary) {
        return new RoomSummaryDTO(
                summary.getRoomId(),
                summary.getRoomName(),
                summary.getParticipantsByRole(),
                summary.getParticipantsByType(),
                summary.getArtifactsByType(),
                summary.getTaskLanesByState(),
                summary.getMessageCount(),
                summary.getDriverStatusSnapshot());
    }

    public static RoomDetailDTO toRoomDetail(Room room) {
        List<ParticipantDTO> participants = room.getParticipants().stream()
                .map(participant -> new ParticipantDTO(
                        participant.getId(),
                        participant.getDisplayName(),
                        participant.getType(),
                        participant.getRole(),
                        participant.getProvider(),
                        participant.getCapabilities()))
                .toList();

        List<ArtifactDTO> artifacts = room.getArtifacts().stream()
                .map(artifact -> new ArtifactDTO(
                        artifact.getId(),
                        artifact.getType(),
                        artifact.getTitle(),
                        artifact.getContent(),
                        artifact.getVersion(),
                        artifact.getCreatedAt(),
                        artifact.getParentArtifactId()))
                .toList();

        Map<UUID, TaskLane> lanesByTask = room.getTaskLanes().stream()
                .flatMap(lane -> lane.getTaskArtifactIds().stream().map(taskId -> Map.entry(taskId, lane)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing));

        List<TaskLaneDTO> taskLanes = room.getTaskLanes().stream()
                .map(lane -> new TaskLaneDTO(
                        lane.getId(),
                        lane.getName(),
                        lane.getImplementorId(),
                        lane.getState(),
                        lane.getTaskArtifactIds()))
                .toList();

        List<TaskDTO> tasks = room.getArtifacts().stream()
                .filter(artifact -> artifact.getType() == ArtifactType.TASK)
                .map(task -> toTaskDTO(task, lanesByTask.get(task.getId())))
                .toList();

        List<ProviderAdapterDTO> providers = room.getProviderAdapters().stream()
                .map(adapter -> new ProviderAdapterDTO(
                        adapter.getId(),
                        adapter.getProviderName(),
                        adapter.getAccessMode(),
                        adapter.getCapabilities(),
                        adapter.getEndpoint(),
                        adapter.isAvailable()))
                .toList();

        return new RoomDetailDTO(
                room.getId(),
                room.getName(),
                room.getCreatedAt(),
                room.isPaused(),
                room.getDriverStatus(),
                participants,
                artifacts,
                tasks,
                taskLanes,
                providers,
                new ArrayList<>(room.getEvents()));
    }

    private static TaskDTO toTaskDTO(Artifact task, TaskLane lane) {
        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getContent(),
                task.getVersion(),
                task.getCreatedAt(),
                task.getParentArtifactId(),
                Optional.ofNullable(lane).map(TaskLane::getId).orElse(null),
                Optional.ofNullable(lane).map(TaskLane::getName).orElse(null),
                Optional.ofNullable(lane).map(TaskLane::getState).orElse(null),
                Optional.ofNullable(lane).map(TaskLane::getImplementorId).orElse(null));
    }
}

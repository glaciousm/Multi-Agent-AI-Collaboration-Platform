package com.localcollab.platform.service;

import com.localcollab.platform.domain.Artifact;
import com.localcollab.platform.domain.ArtifactType;
import com.localcollab.platform.domain.ChatMessage;
import com.localcollab.platform.domain.Participant;
import com.localcollab.platform.domain.DriverStatus;
import com.localcollab.platform.domain.ParticipantRole;
import com.localcollab.platform.domain.ParticipantType;
import com.localcollab.platform.domain.Room;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryRoomService {

    private final Map<UUID, Room> rooms = new ConcurrentHashMap<>();

    public InMemoryRoomService() {
        bootstrapDefaultRoom();
    }

    public List<Room> findAll() {
        return new ArrayList<>(rooms.values());
    }

    public Room createRoom(String name) {
        if (!rooms.isEmpty()) {
            return rooms.values().stream().min(Comparator.comparing(Room::getCreatedAt)).orElseThrow();
        }

        Room room = new Room(UUID.randomUUID(), name, Instant.now());
        room.addParticipant(new Participant(UUID.randomUUID(), "You", ParticipantType.HUMAN, ParticipantRole.OBSERVER, "local", List.of("dialog")));
        room.addParticipant(new Participant(UUID.randomUUID(), "Planner", ParticipantType.AI, ParticipantRole.PLANNER, "ChatGPT", List.of("planning", "dialog")));
        room.addParticipant(new Participant(UUID.randomUUID(), "Reviewer", ParticipantType.AI, ParticipantRole.REVIEWER, "Claude", List.of("review", "dialog")));
        room.addParticipant(new Participant(UUID.randomUUID(), "Implementor", ParticipantType.AI, ParticipantRole.IMPLEMENTOR, "Claude Code", List.of("implementation", "patch", "dialog")));

        Artifact starterPlan = new Artifact(UUID.randomUUID(), ArtifactType.PLAN, "Starter Plan", "1) Clarify the request.\n2) Outline a structured plan.\n3) Deliver the plan artifact for review.", 1, Instant.now(), null);
        room.addArtifact(starterPlan);

        room.addArtifact(new Artifact(UUID.randomUUID(), ArtifactType.PATCH, "Patch Draft 1", "Initial patch stub aligned to the starter plan.", 1, Instant.now(), starterPlan.getId()));
        rooms.put(room.getId(), room);
        return room;
    }

    public Room addParticipant(UUID roomId, Participant participant) {
        Room room = getRoomOrThrow(roomId);
        ensureRoomIsActive(room);
        room.addParticipant(participant);
        return room;
    }

    public Artifact addArtifact(UUID roomId, ArtifactType type, String title, String content, UUID parentArtifactId) {
        Room room = getRoomOrThrow(roomId);
        ensureRoomIsActive(room);

        ArtifactType artifactType = type == null ? ArtifactType.NOTE : type;

        validateArtifactRequest(title, content, artifactType, parentArtifactId, room);

        int nextVersion = room.getArtifacts().stream()
                .filter(a -> a.getType() == artifactType)
                .mapToInt(Artifact::getVersion)
                .max()
                .orElse(0) + 1;

        Artifact artifact = new Artifact(
                UUID.randomUUID(),
                artifactType,
                title.trim(),
                content.trim(),
                nextVersion,
                Instant.now(),
                parentArtifactId);

        room.addArtifact(artifact);
        return artifact;
    }

    public ChatMessage addMessage(UUID roomId, UUID participantId, String content) {
        Room room = getRoomOrThrow(roomId);
        ensureRoomIsActive(room);
        Participant author = room.getParticipants()
                .stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Participant not found in room: " + participantId));

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Message content must not be blank");
        }

        ChatMessage message = new ChatMessage(
                UUID.randomUUID(),
                participantId,
                author.getDisplayName(),
                content.trim(),
                Instant.now());

        room.addMessage(message);
        return message;
    }

    public Room getRoom(UUID roomId) {
        return rooms.get(roomId);
    }

    public Room pauseRoom(UUID roomId) {
        Room room = getRoomOrThrow(roomId);
        room.pause();
        return room;
    }

    public Room resumeRoom(UUID roomId) {
        Room room = getRoomOrThrow(roomId);
        room.resume();
        return room;
    }

    public Room recordDriverFailure(UUID roomId, String reason) {
        Room room = getRoomOrThrow(roomId);
        room.getDriverStatus().recordFailure(reason);
        if (room.getDriverStatus().getState() == DriverStatus.State.PAUSED) {
            room.pause();
        }
        return room;
    }

    public Room recordDriverRecovery(UUID roomId) {
        Room room = getRoomOrThrow(roomId);
        room.getDriverStatus().recordRecovery();
        room.resume();
        return room;
    }

    private Room getRoomOrThrow(UUID roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }
        return room;
    }

    private void ensureRoomIsActive(Room room) {
        if (room.isPaused()) {
            throw new IllegalStateException("Room is paused; resume before making changes");
        }
    }

    private void bootstrapDefaultRoom() {
        if (!rooms.isEmpty()) {
            return;
        }

        createRoom("Multi-Agent Planning Room");
    }

    private void validateArtifactRequest(String title, String content, ArtifactType type, UUID parentArtifactId, Room room) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Artifact title must not be blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Artifact content must not be blank");
        }

        if (type == ArtifactType.REVIEW) {
            validateParentPresence(parentArtifactId, "Review artifacts must reference a plan or patch");
        }

        if (type == ArtifactType.PATCH) {
            validateParentPresence(parentArtifactId, "Patch artifacts must reference a plan or prior patch");
        }

        if (parentArtifactId != null) {
            Artifact parentArtifact = room.getArtifacts().stream()
                    .filter(artifact -> artifact.getId().equals(parentArtifactId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Parent artifact not found in room"));

            if (type == ArtifactType.REVIEW && parentArtifact.getType() != ArtifactType.PLAN && parentArtifact.getType() != ArtifactType.PATCH) {
                throw new IllegalArgumentException("Review artifacts must target an existing plan or patch");
            }

            if (type == ArtifactType.PLAN && parentArtifact.getType() != ArtifactType.PLAN) {
                throw new IllegalArgumentException("Plan versions must reference a prior plan");
            }

            if (type == ArtifactType.PATCH && parentArtifact.getType() != ArtifactType.PLAN && parentArtifact.getType() != ArtifactType.PATCH && parentArtifact.getType() != ArtifactType.TASK) {
                throw new IllegalArgumentException("Patch artifacts must reference a plan, task, or prior patch");
            }
        }
    }

    private void validateParentPresence(UUID parentArtifactId, String message) {
        if (parentArtifactId == null) {
            throw new IllegalArgumentException(message);
        }
    }
}

package com.localcollab.platform.service;

import com.localcollab.platform.domain.Artifact;
import com.localcollab.platform.domain.ArtifactType;
import com.localcollab.platform.domain.ChatMessage;
import com.localcollab.platform.domain.Participant;
import com.localcollab.platform.domain.DriverStatus;
import com.localcollab.platform.domain.ParticipantRole;
import com.localcollab.platform.domain.ParticipantType;
import com.localcollab.platform.domain.ProviderAccessMode;
import com.localcollab.platform.domain.ProviderAdapter;
import com.localcollab.platform.domain.Room;
import com.localcollab.platform.domain.RoomEvent;
import com.localcollab.platform.domain.RoomEventType;
import com.localcollab.platform.domain.RoomSummary;
import com.localcollab.platform.domain.TaskLane;
import com.localcollab.platform.domain.TaskLaneState;
import com.localcollab.platform.validation.ProviderIdentityValidator;
import com.localcollab.platform.validation.ProviderIdentityValidator.ProviderIdentity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class InMemoryRoomService {

    private final Map<UUID, Room> rooms = new ConcurrentHashMap<>();
    private final List<ProviderAdapter> providerCatalog = new CopyOnWriteArrayList<>();
    private final Map<UUID, ReentrantLock> roomLocks = new ConcurrentHashMap<>();
    private final ProviderIdentityValidator providerIdentityValidator;

    public InMemoryRoomService(ProviderIdentityValidator providerIdentityValidator) {
        this.providerIdentityValidator = providerIdentityValidator;
        bootstrapProviderCatalog();
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
        cloneProvidersIntoRoom(room);
        room.addParticipant(new Participant(UUID.randomUUID(), "You", ParticipantType.HUMAN, ParticipantRole.OBSERVER, "local", List.of("dialog")));
        room.addParticipant(new Participant(UUID.randomUUID(), "Planner", ParticipantType.AI, ParticipantRole.PLANNER, "ChatGPT", List.of("planning", "dialog")));
        room.addParticipant(new Participant(UUID.randomUUID(), "Reviewer", ParticipantType.AI, ParticipantRole.REVIEWER, "Claude", List.of("review", "dialog")));
        room.addParticipant(new Participant(UUID.randomUUID(), "Implementor", ParticipantType.AI, ParticipantRole.IMPLEMENTOR, "Claude Code", List.of("implementation", "patch", "dialog")));

        Artifact starterPlan = new Artifact(UUID.randomUUID(), ArtifactType.PLAN, "Starter Plan", "1) Clarify the request.\n2) Outline a structured plan.\n3) Deliver the plan artifact for review.", 1, Instant.now(), null);
        room.addArtifact(starterPlan);

        room.addArtifact(new Artifact(UUID.randomUUID(), ArtifactType.PATCH, "Patch Draft 1", "Initial patch stub aligned to the starter plan.", 1, Instant.now(), starterPlan.getId()));
        TaskLane defaultLane = new TaskLane(UUID.randomUUID(), "Primary Lane", room.getParticipants().stream()
                .filter(p -> p.getRole() == ParticipantRole.IMPLEMENTOR)
                .findFirst()
                .map(Participant::getId)
                .orElseThrow(), TaskLaneState.ACTIVE, List.of());
        room.addTaskLane(defaultLane);
        rooms.put(room.getId(), room);
        roomLocks.put(room.getId(), new ReentrantLock());
        return room;
    }

    public Room addParticipant(UUID roomId, Participant participant) {
        return withRoomLock(roomId, room -> {
            ensureRoomIsActive(room);
            ensureProviderIsRegistered(room, participant.getProvider(), ProviderAccessMode.WEB_UI, participant.getCapabilities(), null);
            room.addParticipant(participant);
            return room;
        });
    }

    public ProviderAdapter registerProvider(UUID roomId, String providerName, ProviderAccessMode accessMode, List<String> capabilities, String endpoint, boolean available) {
        return withRoomLock(roomId, room -> registerProviderInternal(room, providerName, accessMode, capabilities, endpoint, available));
    }

    public Artifact addArtifact(UUID roomId, ArtifactType type, String title, String content, UUID parentArtifactId) {
        return withRoomLock(roomId, room -> {
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
            recordEvent(room, RoomEventType.ARTIFACT_CREATED, "Artifact created: " + artifact.getTitle(), null, artifact.getId(), null);
            return artifact;
        });
    }

    public TaskLane createTaskLane(UUID roomId, String name, UUID implementorId) {
        return withRoomLock(roomId, room -> {
            ensureRoomIsActive(room);
            Participant implementor = room.getParticipants().stream()
                    .filter(p -> p.getId().equals(implementorId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Implementor not found for lane"));
            if (implementor.getRole() != ParticipantRole.IMPLEMENTOR) {
                throw new IllegalArgumentException("Task lanes must be owned by an implementor");
            }

            TaskLane lane = new TaskLane(UUID.randomUUID(), name.trim(), implementorId, TaskLaneState.ACTIVE, List.of());
            room.addTaskLane(lane);
            recordEvent(room, RoomEventType.TASK_UPDATED, "Task lane created: " + name, implementorId, null, lane.getId());
            return lane;
        });
    }

    public TaskLane assignTaskToLane(UUID roomId, UUID laneId, UUID taskArtifactId) {
        return withRoomLock(roomId, room -> {
            ensureRoomIsActive(room);
            Artifact task = room.getArtifacts().stream()
                    .filter(a -> a.getId().equals(taskArtifactId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Task artifact not found"));
            if (task.getType() != ArtifactType.TASK) {
                throw new IllegalArgumentException("Only task artifacts can be scheduled into a lane");
            }

            TaskLane lane = room.getTaskLanes().stream()
                    .filter(t -> t.getId().equals(laneId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Task lane not found"));

            if (lane.getState() != TaskLaneState.ACTIVE) {
                throw new IllegalStateException("Tasks can only be assigned to active lanes");
            }

            lane.addTask(taskArtifactId);
            recordEvent(room, RoomEventType.TASK_UPDATED, "Task assigned to lane: " + task.getTitle(), null, taskArtifactId, lane.getId());
            return lane;
        });
    }

    public TaskLane updateTaskLaneState(UUID roomId, UUID laneId, TaskLaneState state) {
        return withRoomLock(roomId, room -> {
            ensureRoomIsActive(room);

            if (state == null) {
                throw new IllegalArgumentException("Task lane state is required");
            }

            TaskLane lane = room.getTaskLanes().stream()
                    .filter(t -> t.getId().equals(laneId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Task lane not found"));

            lane.updateState(state);
            recordEvent(room, RoomEventType.TASK_UPDATED, "Lane state updated to " + state, null, null, lane.getId());
            return lane;
        });
    }

    public ChatMessage addMessage(UUID roomId, UUID participantId, String content) {
        return withRoomLock(roomId, room -> {
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
            recordEvent(room, RoomEventType.MESSAGE_POSTED, "Message posted by " + author.getDisplayName(), participantId, null, null);
            return message;
        });
    }

    public Room getRoom(UUID roomId) {
        return rooms.get(roomId);
    }

    public Room pauseRoom(UUID roomId) {
        return withRoomLock(roomId, room -> {
            room.pause();
            recordEvent(room, RoomEventType.STATE_CHANGED, "Room paused", null, null, null);
            return room;
        });
    }

    public Room resumeRoom(UUID roomId) {
        return withRoomLock(roomId, room -> {
            room.resume();
            recordEvent(room, RoomEventType.STATE_CHANGED, "Room resumed", null, null, null);
            return room;
        });
    }

    public Room recordDriverFailure(UUID roomId, String reason) {
        return withRoomLock(roomId, room -> {
            room.getDriverStatus().recordFailure(reason);
            if (room.getDriverStatus().getState() == DriverStatus.State.PAUSED) {
                room.pause();
            }
            recordEvent(room, RoomEventType.STATE_CHANGED, "Driver failure recorded: " + reason, null, null, null);
            return room;
        });
    }

    public Room recordDriverRecovery(UUID roomId) {
        return withRoomLock(roomId, room -> {
            room.getDriverStatus().recordRecovery();
            room.resume();
            recordEvent(room, RoomEventType.STATE_CHANGED, "Driver recovered", null, null, null);
            return room;
        });
    }

    public RoomSummary summarizeRoom(UUID roomId) {
        Room room = getRoomOrThrow(roomId);

        Map<ParticipantRole, Long> participantsByRole = room.getParticipants().stream()
                .collect(java.util.stream.Collectors.groupingBy(Participant::getRole, java.util.stream.Collectors.counting()));

        Map<ParticipantType, Long> participantsByType = room.getParticipants().stream()
                .collect(java.util.stream.Collectors.groupingBy(Participant::getType, java.util.stream.Collectors.counting()));

        Map<ArtifactType, Long> artifactsByType = room.getArtifacts().stream()
                .collect(java.util.stream.Collectors.groupingBy(Artifact::getType, java.util.stream.Collectors.counting()));

        Map<TaskLaneState, Long> taskLanesByState = room.getTaskLanes().stream()
                .collect(java.util.stream.Collectors.groupingBy(TaskLane::getState, java.util.stream.Collectors.counting()));

        return new RoomSummary(
                room.getId(),
                room.getName(),
                participantsByRole,
                participantsByType,
                artifactsByType,
                taskLanesByState,
                room.getMessages().size(),
                room.getDriverStatus());
    }

    private Room getRoomOrThrow(UUID roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }
        return room;
    }

    private <T> T withRoomLock(UUID roomId, Function<Room, T> action) {
        ReentrantLock lock = roomLocks.computeIfAbsent(roomId, id -> new ReentrantLock());
        lock.lock();
        try {
            Room room = getRoomOrThrow(roomId);
            return action.apply(room);
        } finally {
            lock.unlock();
        }
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

    private void bootstrapProviderCatalog() {
        providerCatalog.add(new ProviderAdapter(UUID.randomUUID(), "ChatGPT", ProviderAccessMode.WEB_UI, List.of("dialog", "planning"), null, true));
        providerCatalog.add(new ProviderAdapter(UUID.randomUUID(), "Claude", ProviderAccessMode.WEB_UI, List.of("dialog", "review"), null, true));
        providerCatalog.add(new ProviderAdapter(UUID.randomUUID(), "Claude Code", ProviderAccessMode.WEB_UI, List.of("implementation", "patch"), null, true));
        providerCatalog.add(new ProviderAdapter(UUID.randomUUID(), "Gemini", ProviderAccessMode.WEB_UI, List.of("dialog", "planning", "review"), null, true));
        providerCatalog.add(new ProviderAdapter(UUID.randomUUID(), "Local API", ProviderAccessMode.API, List.of("dialog", "implementation", "patch"), "http://localhost:11434/api", true));
    }

    private void cloneProvidersIntoRoom(Room room) {
        providerCatalog.forEach(adapter -> room.addProviderAdapter(new ProviderAdapter(
                UUID.randomUUID(),
                adapter.getProviderName(),
                adapter.getAccessMode(),
                adapter.getCapabilities(),
                adapter.getEndpoint(),
                adapter.isAvailable())));
    }

    private void ensureProviderIsRegistered(Room room, String provider, ProviderAccessMode accessMode, List<String> capabilities, String endpoint) {
        if (provider == null || provider.isBlank()) {
            return;
        }
        ProviderIdentity identity = providerIdentityValidator.validate(provider, accessMode);
        boolean exists = room.getProviderAdapters().stream()
                .anyMatch(adapter -> adapter.getProviderName().equalsIgnoreCase(identity.providerName())
                        && adapter.getAccessMode() == identity.accessMode());
        if (!exists) {
            registerProviderInternal(room, identity.providerName(), identity.accessMode(), capabilities, endpoint, true);
        }
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

    private ProviderAdapter registerProviderInternal(Room room, String providerName, ProviderAccessMode accessMode, List<String> capabilities, String endpoint, boolean available) {
        ensureRoomIsActive(room);

        ProviderIdentity identity = providerIdentityValidator.validate(providerName, accessMode);
        Optional<ProviderAdapter> existing = room.getProviderAdapters().stream()
                .filter(adapter -> adapter.getProviderName().equalsIgnoreCase(identity.providerName()) && adapter.getAccessMode() == identity.accessMode())
                .findFirst();
        if (existing.isPresent()) {
            return existing.get();
        }

        ProviderAdapter adapter = new ProviderAdapter(UUID.randomUUID(), identity.providerName(), identity.accessMode(), capabilities, endpoint, available);
        room.addProviderAdapter(adapter);

        boolean catalogHasProvider = providerCatalog.stream()
                .anyMatch(p -> p.getProviderName().equalsIgnoreCase(identity.providerName()) && p.getAccessMode() == identity.accessMode());
        if (!catalogHasProvider) {
            providerCatalog.add(new ProviderAdapter(UUID.randomUUID(), identity.providerName(), identity.accessMode(), capabilities, endpoint, available));
        }
        return adapter;
    }

    private void recordEvent(Room room, RoomEventType type, String description, UUID participantId, UUID artifactId, UUID taskLaneId) {
        room.addEvent(new RoomEvent(UUID.randomUUID(), room.getId(), type, description, Instant.now(), participantId, artifactId, taskLaneId));
    }
}

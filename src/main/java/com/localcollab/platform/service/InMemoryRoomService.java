package com.localcollab.platform.service;

import com.localcollab.platform.domain.Artifact;
import com.localcollab.platform.domain.ArtifactType;
import com.localcollab.platform.domain.ChatMessage;
import com.localcollab.platform.domain.Participant;
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
        room.addArtifact(new Artifact(UUID.randomUUID(), ArtifactType.PLAN, "Starter Plan", "1) Clarify the request.\n2) Outline a structured plan.\n3) Deliver the plan artifact for review.", 1, Instant.now()));
        rooms.put(room.getId(), room);
        return room;
    }

    public Room addParticipant(UUID roomId, Participant participant) {
        Room room = getRoomOrThrow(roomId);
        room.addParticipant(participant);
        return room;
    }

    public Room addArtifact(UUID roomId, Artifact artifact) {
        Room room = getRoomOrThrow(roomId);
        room.addArtifact(artifact);
        return room;
    }

    public ChatMessage addMessage(UUID roomId, UUID participantId, String content) {
        Room room = getRoomOrThrow(roomId);
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

    private Room getRoomOrThrow(UUID roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }
        return room;
    }

    private void bootstrapDefaultRoom() {
        if (!rooms.isEmpty()) {
            return;
        }

        createRoom("Single-Agent Planning Room");
    }
}

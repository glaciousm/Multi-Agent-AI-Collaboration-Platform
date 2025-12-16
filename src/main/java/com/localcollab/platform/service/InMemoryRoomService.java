package com.localcollab.platform.service;

import com.localcollab.platform.domain.Artifact;
import com.localcollab.platform.domain.Participant;
import com.localcollab.platform.domain.ParticipantRole;
import com.localcollab.platform.domain.ParticipantType;
import com.localcollab.platform.domain.Room;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryRoomService {

    private final Map<UUID, Room> rooms = new ConcurrentHashMap<>();

    public List<Room> findAll() {
        return new ArrayList<>(rooms.values());
    }

    public Room createRoom(String name) {
        Room room = new Room(UUID.randomUUID(), name, Instant.now());
        room.addParticipant(new Participant(UUID.randomUUID(), "You", ParticipantType.HUMAN, ParticipantRole.OBSERVER, "local", List.of("dialog")));
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
}

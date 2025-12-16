package com.localcollab.platform.web;

import com.localcollab.platform.domain.Artifact;
import com.localcollab.platform.domain.ArtifactType;
import com.localcollab.platform.domain.Participant;
import com.localcollab.platform.domain.ParticipantRole;
import com.localcollab.platform.domain.ParticipantType;
import com.localcollab.platform.domain.Room;
import com.localcollab.platform.service.InMemoryRoomService;
import com.localcollab.platform.web.dto.ArtifactRequest;
import com.localcollab.platform.web.dto.ParticipantRequest;
import com.localcollab.platform.web.dto.RoomRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final InMemoryRoomService roomService;

    public RoomController(InMemoryRoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> listRooms() {
        return roomService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Room createRoom(@RequestBody RoomRequest request) {
        String name = request.getName() == null || request.getName().isBlank()
                ? "Local Collaboration Room"
                : request.getName().trim();
        return roomService.createRoom(name);
    }

    @PostMapping("/{roomId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    public Room addParticipant(@PathVariable UUID roomId, @RequestBody ParticipantRequest request) {
        Participant participant = new Participant(
                UUID.randomUUID(),
                request.getDisplayName(),
                request.getType() == null ? ParticipantType.AI : request.getType(),
                request.getRole() == null ? ParticipantRole.OBSERVER : request.getRole(),
                request.getProvider(),
                request.getCapabilities());
        return roomService.addParticipant(roomId, participant);
    }

    @PostMapping("/{roomId}/artifacts")
    @ResponseStatus(HttpStatus.CREATED)
    public Room addArtifact(@PathVariable UUID roomId, @RequestBody ArtifactRequest request) {
        Artifact artifact = new Artifact(
                UUID.randomUUID(),
                request.getType() == null ? ArtifactType.NOTE : request.getType(),
                request.getTitle(),
                request.getContent(),
                1,
                Instant.now());
        return roomService.addArtifact(roomId, artifact);
    }
}

package com.localcollab.platform.web;

import com.localcollab.platform.domain.ArtifactType;
import com.localcollab.platform.domain.ChatMessage;
import com.localcollab.platform.domain.Participant;
import com.localcollab.platform.domain.ParticipantRole;
import com.localcollab.platform.domain.ParticipantType;
import com.localcollab.platform.domain.Room;
import com.localcollab.platform.service.InMemoryRoomService;
import com.localcollab.platform.web.dto.ArtifactRequest;
import com.localcollab.platform.web.dto.ChatMessageRequest;
import com.localcollab.platform.web.dto.DriverFailureRequest;
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
import org.springframework.web.server.ResponseStatusException;

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
        try {
            return roomService.addParticipant(roomId, participant);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{roomId}/artifacts")
    @ResponseStatus(HttpStatus.CREATED)
    public Room addArtifact(@PathVariable UUID roomId, @RequestBody ArtifactRequest request) {
        ArtifactType type = request.getType() == null ? ArtifactType.NOTE : request.getType();
        try {
            roomService.addArtifact(roomId, type, request.getTitle(), request.getContent(), request.getParentArtifactId());
            return roomService.getRoom(roomId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    @GetMapping("/{roomId}/messages")
    public List<ChatMessage> listMessages(@PathVariable UUID roomId) {
        Room room = roomService.getRoom(roomId);
        if (room == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
        }
        return room.getMessages();
    }

    @PostMapping("/{roomId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessage postMessage(@PathVariable UUID roomId, @RequestBody ChatMessageRequest request) {
        Room room = roomService.getRoom(roomId);
        if (room == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
        }

        UUID participantId = request.getParticipantId();
        if (participantId == null) {
            participantId = room.getParticipants().stream()
                    .filter(p -> p.getType() == ParticipantType.HUMAN)
                    .findFirst()
                    .map(Participant::getId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No human participant configured"));
        }

        try {
            return roomService.addMessage(roomId, participantId, request.getContent());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{roomId}/pause")
    public Room pauseRoom(@PathVariable UUID roomId) {
        try {
            return roomService.pauseRoom(roomId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{roomId}/resume")
    public Room resumeRoom(@PathVariable UUID roomId) {
        try {
            return roomService.resumeRoom(roomId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{roomId}/driver/failures")
    public Room recordDriverFailure(@PathVariable UUID roomId, @RequestBody DriverFailureRequest request) {
        try {
            return roomService.recordDriverFailure(roomId, request.getReason());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{roomId}/driver/recoveries")
    public Room recordDriverRecovery(@PathVariable UUID roomId) {
        try {
            return roomService.recordDriverRecovery(roomId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}

package com.localcollab.platform.web;

import com.localcollab.platform.domain.ArtifactType;
import com.localcollab.platform.domain.ChatMessage;
import com.localcollab.platform.domain.Room;
import com.localcollab.platform.domain.Participant;
import com.localcollab.platform.domain.ParticipantRole;
import com.localcollab.platform.domain.ParticipantType;
import com.localcollab.platform.service.InMemoryRoomService;
import com.localcollab.platform.web.dto.ArtifactRequest;
import com.localcollab.platform.web.dto.ChatMessageRequest;
import com.localcollab.platform.web.dto.DriverFailureRequest;
import com.localcollab.platform.web.dto.ParticipantRequest;
import com.localcollab.platform.web.dto.ProviderAdapterRequest;
import com.localcollab.platform.web.dto.RoomDetailDTO;
import com.localcollab.platform.web.dto.RoomDtoMapper;
import com.localcollab.platform.web.dto.RoomRequest;
import com.localcollab.platform.web.dto.RoomSummaryDTO;
import com.localcollab.platform.web.dto.TaskLaneRequest;
import com.localcollab.platform.web.dto.TaskLaneTaskRequest;
import com.localcollab.platform.web.dto.TaskLaneStateRequest;
import jakarta.validation.Valid;
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
    public List<RoomSummaryDTO> listRooms() {
        List<RoomSummaryDTO> summaries = roomService.findAll().stream()
                .map(room -> roomService.summarizeRoom(room.getId()))
                .map(RoomDtoMapper::toRoomSummary)
                .toList();
        return summaries;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomDetailDTO createRoom(@Valid @RequestBody RoomRequest request) {
        String name = request.getName().trim();
        return RoomDtoMapper.toRoomDetail(roomService.createRoom(name));
    }

    @PostMapping("/{roomId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomDetailDTO addParticipant(@PathVariable UUID roomId, @Valid @RequestBody ParticipantRequest request) {
        Participant participant = new Participant(
                UUID.randomUUID(),
                request.getDisplayName(),
                request.getType() == null ? ParticipantType.AI : request.getType(),
                request.getRole() == null ? ParticipantRole.OBSERVER : request.getRole(),
                request.getProvider(),
                request.getCapabilities());
        try {
            return RoomDtoMapper.toRoomDetail(roomService.addParticipant(roomId, participant));
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{roomId}/providers")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomDetailDTO registerProvider(@PathVariable UUID roomId, @Valid @RequestBody ProviderAdapterRequest request) {
        try {
            roomService.registerProvider(roomId, request.getProviderName(), request.getAccessMode(), request.getCapabilities(), request.getEndpoint(), request.isAvailable());
            return RoomDtoMapper.toRoomDetail(roomService.getRoom(roomId));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{roomId}/artifacts")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomDetailDTO addArtifact(@PathVariable UUID roomId, @Valid @RequestBody ArtifactRequest request) {
        ArtifactType type = request.getType() == null ? ArtifactType.NOTE : request.getType();
        try {
            roomService.addArtifact(roomId, type, request.getTitle(), request.getContent(), request.getParentArtifactId());
            return RoomDtoMapper.toRoomDetail(roomService.getRoom(roomId));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{roomId}/task-lanes")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomDetailDTO createTaskLane(@PathVariable UUID roomId, @Valid @RequestBody TaskLaneRequest request) {
        try {
            roomService.createTaskLane(roomId, request.getName(), request.getImplementorId());
            return RoomDtoMapper.toRoomDetail(roomService.getRoom(roomId));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{roomId}/task-lanes/{laneId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomDetailDTO assignTaskToLane(@PathVariable UUID roomId, @PathVariable UUID laneId, @Valid @RequestBody TaskLaneTaskRequest request) {
        try {
            roomService.assignTaskToLane(roomId, laneId, request.getTaskArtifactId());
            return RoomDtoMapper.toRoomDetail(roomService.getRoom(roomId));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{roomId}/task-lanes/{laneId}/state")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomDetailDTO updateTaskLaneState(@PathVariable UUID roomId, @PathVariable UUID laneId, @Valid @RequestBody TaskLaneStateRequest request) {
        try {
            roomService.updateTaskLaneState(roomId, laneId, request.getState());
            return RoomDtoMapper.toRoomDetail(roomService.getRoom(roomId));
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
    public ChatMessage postMessage(@PathVariable UUID roomId, @Valid @RequestBody ChatMessageRequest request) {
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
    public RoomDetailDTO pauseRoom(@PathVariable UUID roomId) {
        try {
            return RoomDtoMapper.toRoomDetail(roomService.pauseRoom(roomId));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{roomId}/resume")
    public RoomDetailDTO resumeRoom(@PathVariable UUID roomId) {
        try {
            return RoomDtoMapper.toRoomDetail(roomService.resumeRoom(roomId));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{roomId}/driver/failures")
    public RoomDetailDTO recordDriverFailure(@PathVariable UUID roomId, @Valid @RequestBody DriverFailureRequest request) {
        try {
            return RoomDtoMapper.toRoomDetail(roomService.recordDriverFailure(roomId, request.getReason()));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{roomId}/driver/recoveries")
    public RoomDetailDTO recordDriverRecovery(@PathVariable UUID roomId) {
        try {
            return RoomDtoMapper.toRoomDetail(roomService.recordDriverRecovery(roomId));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @GetMapping("/{roomId}/summary")
    public RoomSummaryDTO summarizeRoom(@PathVariable UUID roomId) {
        try {
            return RoomDtoMapper.toRoomSummary(roomService.summarizeRoom(roomId));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}

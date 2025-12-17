package com.localcollab.platform.service;

import com.localcollab.platform.domain.ArtifactType;
import com.localcollab.platform.domain.DriverStatus;
import com.localcollab.platform.domain.Participant;
import com.localcollab.platform.domain.ParticipantRole;
import com.localcollab.platform.domain.ParticipantType;
import com.localcollab.platform.domain.ProviderAccessMode;
import com.localcollab.platform.domain.TaskLane;
import com.localcollab.platform.domain.Room;
import com.localcollab.platform.domain.TaskLaneState;
import com.localcollab.platform.domain.RoomSummary;
import com.localcollab.platform.validation.ProviderIdentityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRoomServiceTest {

    private InMemoryRoomService service;
    private Room room;

    @BeforeEach
    void setUp() {
        service = new InMemoryRoomService(new ProviderIdentityValidator());
        room = service.findAll().getFirst();
    }

    @Test
    void pausesRoomWhenRetryBudgetExhausted() {
        Room updated = service.recordDriverFailure(room.getId(), "timeout");
        assertEquals(DriverStatus.State.RETRYING, updated.getDriverStatus().getState());
        assertTrue(updated.getDriverStatus().getConsecutiveFailures() > 0);

        service.recordDriverFailure(room.getId(), "timeout2");
        Room pausedRoom = service.recordDriverFailure(room.getId(), "timeout3");

        assertEquals(DriverStatus.State.PAUSED, pausedRoom.getDriverStatus().getState());
        assertTrue(pausedRoom.isPaused());
    }

    @Test
    void blocksArtifactChangesWhilePaused() {
        service.pauseRoom(room.getId());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.addArtifact(room.getId(), ArtifactType.NOTE, "Blocked", "No write while paused", null));

        assertEquals("Room is paused; resume before making changes", ex.getMessage());
    }

    @Test
    void resumingRoomResetsDriverStatus() {
        service.recordDriverFailure(room.getId(), "first");
        service.recordDriverFailure(room.getId(), "second");
        Room pausedRoom = service.recordDriverFailure(room.getId(), "third");
        assertTrue(pausedRoom.isPaused());

        Room resumed = service.resumeRoom(room.getId());
        assertTrue(resumed.getDriverStatus().getConsecutiveFailures() == 0);
        assertEquals(DriverStatus.State.HEALTHY, resumed.getDriverStatus().getState());
        assertTrue(!resumed.isPaused());
    }

    @Test
    void registersApiAdapterAndKeepsCatalogSynced() {
        service.registerProvider(room.getId(), "OpenAI API", ProviderAccessMode.API, java.util.List.of("dialog", "tools"), "https://api.openai.local", true);

        Room refreshed = service.getRoom(room.getId());
        assertTrue(refreshed.getProviderAdapters().stream().anyMatch(adapter ->
                adapter.getProviderName().equals("OpenAI API") && adapter.getAccessMode() == ProviderAccessMode.API));

        Participant apiImplementor = new Participant(java.util.UUID.randomUUID(), "API Implementor", ParticipantType.AI, ParticipantRole.IMPLEMENTOR, "OpenAI API", java.util.List.of("patch"));
        Room withImplementor = service.addParticipant(room.getId(), apiImplementor);

        assertTrue(withImplementor.getParticipants().stream().anyMatch(p -> p.getProvider().equals("OpenAI API")));
    }

    @Test
    void assignsTasksIntoParallelLanes() {
        Participant extraImplementor = new Participant(java.util.UUID.randomUUID(), "Gemini Builder", ParticipantType.AI, ParticipantRole.IMPLEMENTOR, "Gemini", java.util.List.of("implementation"));
        Room roomWithExtra = service.addParticipant(room.getId(), extraImplementor);

        var taskOne = service.addArtifact(room.getId(), ArtifactType.TASK, "Task Alpha", "Build feature A", roomWithExtra.getArtifacts().stream().filter(a -> a.getType() == ArtifactType.PLAN).findFirst().orElseThrow().getId());
        var taskTwo = service.addArtifact(room.getId(), ArtifactType.TASK, "Task Beta", "Build feature B", roomWithExtra.getArtifacts().stream().filter(a -> a.getType() == ArtifactType.PLAN).findFirst().orElseThrow().getId());

        TaskLane laneOne = service.createTaskLane(room.getId(), "Lane One", roomWithExtra.getParticipants().stream().filter(p -> p.getRole() == ParticipantRole.IMPLEMENTOR).findFirst().orElseThrow().getId());
        TaskLane laneTwo = service.createTaskLane(room.getId(), "Lane Two", extraImplementor.getId());

        service.assignTaskToLane(room.getId(), laneOne.getId(), taskOne.getId());
        service.assignTaskToLane(room.getId(), laneTwo.getId(), taskTwo.getId());

        Room updated = service.getRoom(room.getId());
        assertEquals(3, updated.getTaskLanes().size());
        assertTrue(updated.getTaskLanes().stream().anyMatch(lane -> lane.getTaskArtifactIds().contains(taskOne.getId())));
        assertTrue(updated.getTaskLanes().stream().anyMatch(lane -> lane.getTaskArtifactIds().contains(taskTwo.getId())));
    }

    @Test
    void updatesTaskLaneStateAndProducesSummary() {
        TaskLane lane = room.getTaskLanes().getFirst();
        service.updateTaskLaneState(room.getId(), lane.getId(), TaskLaneState.BLOCKED);

        Room updated = service.getRoom(room.getId());
        assertEquals(TaskLaneState.BLOCKED, updated.getTaskLanes().getFirst().getState());

        RoomSummary summary = service.summarizeRoom(room.getId());
        assertEquals(4, summary.getParticipantsByRole().values().stream().mapToLong(Long::longValue).sum());
        assertEquals(1, summary.getTaskLanesByState().get(TaskLaneState.BLOCKED));
        assertTrue(summary.getArtifactsByType().get(ArtifactType.PLAN) >= 1);
        assertEquals(updated.getMessages().size(), summary.getMessageCount());
    }
}

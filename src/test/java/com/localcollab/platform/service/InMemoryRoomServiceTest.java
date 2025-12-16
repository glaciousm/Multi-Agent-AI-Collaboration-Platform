package com.localcollab.platform.service;

import com.localcollab.platform.domain.ArtifactType;
import com.localcollab.platform.domain.DriverStatus;
import com.localcollab.platform.domain.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRoomServiceTest {

    private InMemoryRoomService service;
    private Room room;

    @BeforeEach
    void setUp() {
        service = new InMemoryRoomService();
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
}

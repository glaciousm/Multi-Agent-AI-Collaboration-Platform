package com.localcollab.platform.service;

import com.localcollab.platform.domain.ArtifactType;
import com.localcollab.platform.domain.ChatMessage;
import com.localcollab.platform.domain.Participant;
import com.localcollab.platform.domain.ParticipantRole;
import com.localcollab.platform.domain.ParticipantType;
import com.localcollab.platform.domain.Room;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryRoomServiceTest {

    private final InMemoryRoomService roomService = new InMemoryRoomService();

    @Test
    void bootstrapsSingleRoomWithHumanAndPlanner() {
        List<Room> rooms = roomService.findAll();
        assertThat(rooms).hasSize(1);

        Room room = rooms.getFirst();
        assertThat(room.getParticipants()).hasSize(2);
        assertThat(room.getParticipants())
                .anySatisfy(p -> assertThat(p.getType()).isEqualTo(ParticipantType.HUMAN))
                .anySatisfy(p -> assertThat(p.getRole()).isEqualTo(ParticipantRole.PLANNER));

        assertThat(room.getArtifacts())
                .anySatisfy(a -> {
                    assertThat(a.getType()).isEqualTo(ArtifactType.PLAN);
                    assertThat(a.getTitle()).isEqualTo("Starter Plan");
                    assertThat(a.getContent()).contains("Clarify the request");
                });
    }

    @Test
    void addsMessagesForKnownParticipants() {
        Room room = roomService.findAll().getFirst();
        Participant author = room.getParticipants().stream()
                .filter(p -> p.getType() == ParticipantType.HUMAN)
                .findFirst()
                .orElseThrow();

        ChatMessage message = roomService.addMessage(room.getId(), author.getId(), "  Hello planner  ");

        assertThat(message.getContent()).isEqualTo("Hello planner");
        assertThat(room.getMessages())
                .anySatisfy(m -> assertThat(m.getContent()).isEqualTo("Hello planner"));
    }

    @Test
    void rejectsMessagesFromUnknownParticipants() {
        Room room = roomService.findAll().getFirst();
        UUID unknownParticipant = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> roomService.addMessage(room.getId(), unknownParticipant, "Hi"));
    }
}

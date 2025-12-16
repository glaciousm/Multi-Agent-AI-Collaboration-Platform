package com.localcollab.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localcollab.platform.domain.Participant;
import com.localcollab.platform.domain.ParticipantType;
import com.localcollab.platform.domain.Room;
import com.localcollab.platform.service.InMemoryRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryRoomService roomService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID roomId;
    private UUID humanId;

    @BeforeEach
    void setUp() {
        Room room = roomService.findAll().getFirst();
        roomId = room.getId();
        humanId = room.getParticipants().stream()
                .filter(p -> p.getType() == ParticipantType.HUMAN)
                .map(Participant::getId)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void listsRoomsWithDefaultState() throws Exception {
        var result = mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("Single-Agent Planning Room");
    }

    @Test
    void postsAndListsMessages() throws Exception {
        Map<String, Object> payload = Map.of(
                "participantId", humanId.toString(),
                "content", "Hello planner"
        );

        mockMvc.perform(post("/api/rooms/" + roomId + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participantName").value("You"))
                .andExpect(jsonPath("$.content").value("Hello planner"));

        mockMvc.perform(get("/api/rooms/" + roomId + "/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hello planner"));
    }
}

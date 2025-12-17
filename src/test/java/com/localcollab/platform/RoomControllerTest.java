package com.localcollab.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localcollab.platform.domain.ParticipantRole;
import com.localcollab.platform.domain.ParticipantType;
import com.localcollab.platform.web.dto.ParticipantRequest;
import com.localcollab.platform.web.dto.RoomRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRoomAndList() throws Exception {
        RoomRequest request = new RoomRequest();
        request.setName("Test Room");
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void addParticipantToRoom() throws Exception {
        // create room
        String response = mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequest("Local Room"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID roomId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        ParticipantRequest participantRequest = new ParticipantRequest();
        participantRequest.setDisplayName("Assistant");
        participantRequest.setType(ParticipantType.AI);
        participantRequest.setRole(ParticipantRole.OBSERVER);
        participantRequest.setCapabilities(List.of("planning", "review"));
        mockMvc.perform(post("/api/rooms/" + roomId + "/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(participantRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participants", hasSize(5)))
                .andExpect(jsonPath("$.participants[4].role", is(ParticipantRole.OBSERVER.name())))
                .andExpect(jsonPath("$.participants[4].type", is(ParticipantType.AI.name())));
    }

    private RoomRequest roomRequest(String name) {
        RoomRequest request = new RoomRequest();
        request.setName(name);
        return request;
    }
}

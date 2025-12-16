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
    private UUID starterPlanId;
    private UUID implementorId;

    @BeforeEach
    void setUp() {
        Room room = roomService.findAll().getFirst();
        roomId = room.getId();
        humanId = room.getParticipants().stream()
                .filter(p -> p.getType() == ParticipantType.HUMAN)
                .map(Participant::getId)
                .findFirst()
                .orElseThrow();
        implementorId = room.getParticipants().stream()
                .filter(p -> p.getRole() == com.localcollab.platform.domain.ParticipantRole.IMPLEMENTOR)
                .map(Participant::getId)
                .findFirst()
                .orElseThrow();
        starterPlanId = room.getArtifacts().stream()
                .filter(a -> a.getType().name().equals("PLAN"))
                .findFirst()
                .map(com.localcollab.platform.domain.Artifact::getId)
                .orElseThrow();
    }

    @Test
    void listsRoomsWithDefaultState() throws Exception {
        var result = mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains("Multi-Agent Planning Room")
                .contains("Reviewer");
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

    @Test
    void addsPatchArtifactThroughApi() throws Exception {
        Map<String, Object> payload = Map.of(
                "type", "PATCH",
                "title", "API Patch",
                "content", "Implements API facade",
                "parentArtifactId", starterPlanId.toString()
        );

        var result = mockMvc.perform(post("/api/rooms/" + roomId + "/artifacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains("API Patch")
                .contains("PATCH");
    }

    @Test
    void rejectsPatchWithoutParent() throws Exception {
        Map<String, Object> payload = Map.of(
                "type", "PATCH",
                "title", "Orphan Patch",
                "content", "Missing parent"
        );

        mockMvc.perform(post("/api/rooms/" + roomId + "/artifacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registersProviderAdapterThroughApi() throws Exception {
        Map<String, Object> payload = Map.of(
                "providerName", "Gemini API",
                "accessMode", "API",
                "capabilities", java.util.List.of("dialog", "planning"),
                "endpoint", "https://gemini.local/api"
        );

        mockMvc.perform(post("/api/rooms/" + roomId + "/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.providerAdapters[?(@.providerName=='Gemini API')]").isNotEmpty());
    }

    @Test
    void createsParallelTaskLanesViaApi() throws Exception {
        Map<String, Object> taskPayload = Map.of(
                "type", "TASK",
                "title", "Split Feature",
                "content", "Execute part of the work",
                "parentArtifactId", starterPlanId.toString()
        );

        mockMvc.perform(post("/api/rooms/" + roomId + "/artifacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskPayload)))
                .andExpect(status().isCreated());

        String taskId = roomService.getRoom(roomId).getArtifacts().stream()
                .filter(a -> a.getType() == com.localcollab.platform.domain.ArtifactType.TASK)
                .reduce((first, second) -> second)
                .orElseThrow()
                .getId()
                .toString();

        Map<String, Object> lanePayload = Map.of(
                "name", "Secondary Lane",
                "implementorId", implementorId.toString()
        );

        mockMvc.perform(post("/api/rooms/" + roomId + "/task-lanes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lanePayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskLanes[?(@.name=='Secondary Lane')]").isNotEmpty());

        Map<String, Object> assignmentPayload = Map.of("taskArtifactId", taskId);

        mockMvc.perform(post("/api/rooms/" + roomId + "/task-lanes/" + roomService.getRoom(roomId).getTaskLanes().getLast().getId() + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskLanes[?(@.taskArtifactIds[0]=='" + taskId + "')]").isNotEmpty());
    }
}

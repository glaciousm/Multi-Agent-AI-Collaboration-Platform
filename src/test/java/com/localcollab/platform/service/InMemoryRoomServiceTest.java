package com.localcollab.platform.service;

import com.localcollab.platform.domain.Artifact;
import com.localcollab.platform.domain.ArtifactType;
import com.localcollab.platform.domain.Participant;
import com.localcollab.platform.domain.ParticipantRole;
import com.localcollab.platform.domain.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRoomServiceTest {

    private InMemoryRoomService service;

    @BeforeEach
    void setUp() {
        service = new InMemoryRoomService();
    }

    @Test
    void shouldBootstrapRoomWithPlannerReviewerAndImplementor() {
        Room room = service.findAll().getFirst();

        assertEquals(1, service.findAll().size());
        assertEquals("Multi-Agent Planning Room", room.getName());

        List<Participant> participants = room.getParticipants();
        assertEquals(4, participants.size());
        assertTrue(participants.stream().anyMatch(p -> p.getRole() == ParticipantRole.PLANNER));
        assertTrue(participants.stream().anyMatch(p -> p.getRole() == ParticipantRole.REVIEWER));
        assertTrue(participants.stream().anyMatch(p -> p.getRole() == ParticipantRole.IMPLEMENTOR));
    }

    @Test
    void shouldIncrementPlanVersionsWhenAddingNewPlan() {
        Room room = service.findAll().getFirst();
        Artifact initialPlan = room.getArtifacts().stream()
                .filter(a -> a.getType() == ArtifactType.PLAN)
                .findFirst()
                .orElseThrow();

        Artifact revisedPlan = service.addArtifact(
                room.getId(),
                ArtifactType.PLAN,
                "Revised Plan",
                "Expanded plan details",
                initialPlan.getId());

        assertEquals(2, revisedPlan.getVersion());
        assertEquals(initialPlan.getId(), revisedPlan.getParentArtifactId());
    }

    @Test
    void shouldRejectReviewWithoutArtifactReference() {
        Room room = service.findAll().getFirst();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.addArtifact(room.getId(), ArtifactType.REVIEW, "Review", "Needs more detail", null));

        assertTrue(exception.getMessage().contains("reference"));
    }

    @Test
    void shouldCreatePlanReviewLinkedToVersion() {
        Room room = service.findAll().getFirst();
        Artifact initialPlan = room.getArtifacts().stream()
                .filter(a -> a.getType() == ArtifactType.PLAN)
                .findFirst()
                .orElseThrow();

        Artifact review = service.addArtifact(
                room.getId(),
                ArtifactType.REVIEW,
                "Plan Review",
                "Solid outline; expand on risk management.",
                initialPlan.getId());

        assertEquals(1, review.getVersion());
        assertEquals(initialPlan.getId(), review.getParentArtifactId());
    }

    @Test
    void shouldEnforcePatchParentRequirement() {
        Room room = service.findAll().getFirst();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.addArtifact(room.getId(), ArtifactType.PATCH, "Unlinked Patch", "No parent", null));

        assertTrue(exception.getMessage().contains("Patch artifacts must reference"));
    }

    @Test
    void shouldSupportPatchAndReviewCycle() {
        Room room = service.findAll().getFirst();
        Artifact basePlan = room.getArtifacts().stream()
                .filter(a -> a.getType() == ArtifactType.PLAN)
                .findFirst()
                .orElseThrow();

        Artifact implementorPatch = service.addArtifact(
                room.getId(),
                ArtifactType.PATCH,
                "Patch Draft 2",
                "Adds initial endpoint support",
                basePlan.getId());

        Artifact patchRevision = service.addArtifact(
                room.getId(),
                ArtifactType.PATCH,
                "Patch Draft 3",
                "Incorporates review feedback",
                implementorPatch.getId());

        Artifact patchReview = service.addArtifact(
                room.getId(),
                ArtifactType.REVIEW,
                "Patch Review",
                "Looks solid; add more tests.",
                patchRevision.getId());

        assertEquals(2, implementorPatch.getVersion());
        assertEquals(3, patchRevision.getVersion());
        assertEquals(1, patchReview.getVersion());
        assertEquals(patchRevision.getId(), patchReview.getParentArtifactId());
    }
}

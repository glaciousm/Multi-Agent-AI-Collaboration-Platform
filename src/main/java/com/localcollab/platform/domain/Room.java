package com.localcollab.platform.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Room {
    private UUID id;
    private String name;
    private Instant createdAt;
    private List<Participant> participants = new ArrayList<>();
    private List<Artifact> artifacts = new ArrayList<>();
    private List<ChatMessage> messages = new ArrayList<>();

    public Room() {
        // for serialization
    }

    public Room(UUID id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public List<Artifact> getArtifacts() {
        return Collections.unmodifiableList(artifacts);
    }

    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
    }

    public void addArtifact(Artifact artifact) {
        artifacts.add(artifact);
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
    }
}

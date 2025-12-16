package com.localcollab.platform.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Participant {
    private UUID id;
    private String displayName;
    private ParticipantType type;
    private ParticipantRole role;
    private String provider;
    private List<String> capabilities = new ArrayList<>();

    public Participant() {
        // for serialization
    }

    public Participant(UUID id, String displayName, ParticipantType type, ParticipantRole role, String provider, List<String> capabilities) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.role = role;
        this.provider = provider;
        if (capabilities != null) {
            this.capabilities = new ArrayList<>(capabilities);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ParticipantType getType() {
        return type;
    }

    public ParticipantRole getRole() {
        return role;
    }

    public String getProvider() {
        return provider;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }
}

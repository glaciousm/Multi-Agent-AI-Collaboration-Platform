package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.ParticipantRole;
import com.localcollab.platform.domain.ParticipantType;

import java.util.List;
import java.util.UUID;

public class ParticipantDTO {
    private UUID id;
    private String displayName;
    private ParticipantType type;
    private ParticipantRole role;
    private String provider;
    private List<String> capabilities;

    public ParticipantDTO(UUID id, String displayName, ParticipantType type, ParticipantRole role, String provider, List<String> capabilities) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.role = role;
        this.provider = provider;
        this.capabilities = capabilities;
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

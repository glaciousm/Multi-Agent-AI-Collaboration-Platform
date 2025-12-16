package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.ParticipantRole;
import com.localcollab.platform.domain.ParticipantType;

import java.util.ArrayList;
import java.util.List;

public class ParticipantRequest {
    private String displayName;
    private ParticipantType type;
    private ParticipantRole role;
    private String provider;
    private List<String> capabilities = new ArrayList<>();

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

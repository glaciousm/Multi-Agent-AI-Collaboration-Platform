package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.ProviderAccessMode;

import java.util.List;
import java.util.UUID;

public class ProviderAdapterDTO {
    private UUID id;
    private String providerName;
    private ProviderAccessMode accessMode;
    private List<String> capabilities;
    private String endpoint;
    private boolean available;

    public ProviderAdapterDTO(UUID id, String providerName, ProviderAccessMode accessMode, List<String> capabilities, String endpoint, boolean available) {
        this.id = id;
        this.providerName = providerName;
        this.accessMode = accessMode;
        this.capabilities = capabilities;
        this.endpoint = endpoint;
        this.available = available;
    }

    public UUID getId() {
        return id;
    }

    public String getProviderName() {
        return providerName;
    }

    public ProviderAccessMode getAccessMode() {
        return accessMode;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public boolean isAvailable() {
        return available;
    }
}

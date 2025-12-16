package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.ProviderAccessMode;

import java.util.List;

public class ProviderAdapterRequest {
    private String providerName;
    private ProviderAccessMode accessMode;
    private List<String> capabilities;
    private String endpoint;
    private boolean available = true;

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

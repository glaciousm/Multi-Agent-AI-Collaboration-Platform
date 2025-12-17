package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.ProviderAccessMode;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class ProviderAdapterRequest {
    @NotBlank(message = "providerName is required")
    private String providerName;
    private ProviderAccessMode accessMode;
    private List<String> capabilities;
    private String endpoint;
    private boolean available = true;

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public ProviderAccessMode getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(ProviderAccessMode accessMode) {
        this.accessMode = accessMode;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}

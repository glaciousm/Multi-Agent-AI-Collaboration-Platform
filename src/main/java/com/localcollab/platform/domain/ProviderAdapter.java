package com.localcollab.platform.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProviderAdapter {

    private UUID id;
    private String providerName;
    private ProviderAccessMode accessMode;
    private List<String> capabilities = new ArrayList<>();
    private String endpoint;
    private boolean available;

    public ProviderAdapter() {
        // for serialization
    }

    public ProviderAdapter(UUID id, String providerName, ProviderAccessMode accessMode, List<String> capabilities, String endpoint, boolean available) {
        this.id = Objects.requireNonNull(id, "id");
        this.providerName = Objects.requireNonNull(providerName, "providerName");
        this.accessMode = accessMode == null ? ProviderAccessMode.WEB_UI : accessMode;
        this.endpoint = endpoint;
        this.available = available;
        if (capabilities != null) {
            this.capabilities = new ArrayList<>(capabilities);
        }
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

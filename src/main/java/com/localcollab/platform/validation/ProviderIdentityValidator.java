package com.localcollab.platform.validation;

import com.localcollab.platform.domain.ProviderAccessMode;
import org.springframework.stereotype.Component;

@Component
public class ProviderIdentityValidator {

    public ProviderIdentity validate(String providerName, ProviderAccessMode accessMode) {
        if (providerName == null || providerName.isBlank()) {
            throw new IllegalArgumentException("providerName is required");
        }
        ProviderAccessMode resolvedMode = accessMode == null ? ProviderAccessMode.WEB_UI : accessMode;
        return new ProviderIdentity(providerName.trim(), resolvedMode);
    }

    public record ProviderIdentity(String providerName, ProviderAccessMode accessMode) {
    }
}

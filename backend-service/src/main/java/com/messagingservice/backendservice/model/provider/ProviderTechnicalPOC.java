package com.messagingservice.backendservice.model.provider;

import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.persistence.Embeddable;

@Data
@AllArgsConstructor
@Embeddable
public class ProviderTechnicalPOC {
    private String technicalPOCPhoneNumber;
    private String technicalPOCEmailID;
    public ProviderTechnicalPOC() {
    }
}

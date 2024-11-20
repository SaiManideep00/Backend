package com.messagingservice.backendservice.model.provider;

import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.persistence.Column;

@Data
@AllArgsConstructor
public class ProviderBusinessPOC {
    @Column
    private String businessPOCPhoneNumber;
    @Column
    private String businessPOCEmailID;

    public ProviderBusinessPOC() {
    }
}

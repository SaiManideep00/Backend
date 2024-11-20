package com.messagingservice.backendservice.dto.provider;

import com.messagingservice.backendservice.model.provider.ProviderBusinessPOC;
import com.messagingservice.backendservice.model.provider.ProviderTechnicalPOC;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProviderBasicDetailsDTO {
    private long providerId;
    private String providerName;
    private boolean active = true;
    private ProviderTechnicalPOC providerTechnicalPOC;
    private ProviderBusinessPOC providerBusinessPOC;
    private String alertNotificationEmailID;
}

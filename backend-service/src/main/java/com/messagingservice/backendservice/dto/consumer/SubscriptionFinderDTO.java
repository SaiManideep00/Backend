package com.messagingservice.backendservice.dto.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscriptionFinderDTO {
    private String eventName;
    private String consumerName;
    private String providerName;
    private String subscriptionType;
}

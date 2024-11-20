package com.messagingservice.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscribedEvent {
    private Long subscriptionId;
    private long eventId;
    private String eventName;
    private String providerName;
    private String dataFormat; //Json or XML
    private boolean active = true;
    private List<Filter> filters;
    private String sourceDataFormat;
    private SubscribedEventConnections subscribedEventConnections;
}

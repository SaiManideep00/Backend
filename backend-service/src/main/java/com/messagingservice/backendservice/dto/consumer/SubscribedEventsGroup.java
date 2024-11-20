package com.messagingservice.backendservice.dto.consumer;

import com.messagingservice.backendservice.model.consumer.SubscribedEventConnections;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@AllArgsConstructor
@Getter
@Setter
public class SubscribedEventsGroup {
    private Long subscriptionId;
    private List<Long> eventIds;
    private List<String> eventNames;
    private String providerName;
    private String dataFormat; //Json or XML
    private boolean active = true;
    private SubscribedEventConnections subscribedEventConnections;
}

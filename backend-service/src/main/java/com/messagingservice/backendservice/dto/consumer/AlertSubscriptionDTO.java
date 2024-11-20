package com.messagingservice.backendservice.dto.consumer;

import com.messagingservice.backendservice.model.consumer.AlertCriteria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertSubscriptionDTO {
    private long eventId;
    private String eventName;
    private String providerName;
    private boolean active = true;
    private AlertCriteria alertCriteria;
    private List<Long> dbWatchlistIds;
}

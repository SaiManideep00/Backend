package com.mm.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscribedEventFinderDTO {
    private String eventName;
    private String consumerName;
    private String providerName;
}

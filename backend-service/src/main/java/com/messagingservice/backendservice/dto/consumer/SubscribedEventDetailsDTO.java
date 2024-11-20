package com.messagingservice.backendservice.dto.consumer;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SubscribedEventDetailsDTO {
    private String providerName;
    private String eventName;
}

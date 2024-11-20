package com.messagingservice.backendservice.dto.consumer;

import com.messagingservice.backendservice.model.consumer.ConsumerBusinessPOC;
import com.messagingservice.backendservice.model.consumer.ConsumerTechnicalPOC;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConsumerBasicDetailsDTO {
    private Long consumerId;
    private String consumerName;
    private boolean active = true;
    private ConsumerTechnicalPOC consumerTechnicalPOC;
    private ConsumerBusinessPOC consumerBusinessPOC;
    private String alertNotificationEmailID;
}

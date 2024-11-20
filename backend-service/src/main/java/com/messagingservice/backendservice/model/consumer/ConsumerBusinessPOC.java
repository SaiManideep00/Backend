package com.messagingservice.backendservice.model.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ConsumerBusinessPOC {
    @Column
    private String BusinessPOCPhoneNumber;
    @Column
    private String BusinessPOCEmailID;
}

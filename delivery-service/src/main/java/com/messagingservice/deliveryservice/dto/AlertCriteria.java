package com.messagingservice.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertCriteria {
    private Long criteriaId;
    List<String> matchCriteria;
    RecepientGroup recepientGroup;
}

package com.messagingservice.backendservice.dto.consumer;

import com.messagingservice.backendservice.model.consumer.RecepientGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertCriteriaDTO {
    List<String> matchCriteria;
    RecepientGroup recepientGroup;
}

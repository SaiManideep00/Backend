package com.messagingservice.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecepientGroup {
    private Long groupId;
    private String groupName;
    private List<String> emailIds;
}

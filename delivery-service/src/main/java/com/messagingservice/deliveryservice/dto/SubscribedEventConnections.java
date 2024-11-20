package com.messagingservice.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscribedEventConnections {
    private Long connectionId;
    private String connectionName;
    private String connectionType;
    private String url;
    private String username;
    private String password;
    private boolean active;
}

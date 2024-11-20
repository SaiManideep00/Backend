package com.messagingservice.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DBWatchlist {
    private Long watchlistId;
    private String connectionName;
    private String connectionType;
    private String url;
    private String username;
    private String password;
    private boolean active = true;
    private Set<AlertSubscription> alertSubscriptions = new HashSet<>();

}

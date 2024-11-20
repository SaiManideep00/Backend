package com.messagingservice.backendservice.dto.provider;

import com.messagingservice.backendservice.model.provider.Connections;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@Getter
@Setter
public class EventsDTO {
    private long eventId;
    private String eventName;
    private String eventType; //push or pull
    private String orderOfEvents; //serial or Concurrent
    private boolean active = true;
    private Set<String> filters;
    private Connections connections;
    private Object data;
    private String dataFormat;

}

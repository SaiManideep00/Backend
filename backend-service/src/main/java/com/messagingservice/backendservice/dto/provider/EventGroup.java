package com.messagingservice.backendservice.dto.provider;

import com.messagingservice.backendservice.model.provider.Connections;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@AllArgsConstructor
@Getter
@Setter
public class EventGroup {
    @Column(nullable = false)
    private List<String> eventNames;
    private String eventType; //push or pull
    private String orderOfEvents; //serial or Concurrent
    private boolean active = true;


    //    @OneToMany(cascade = CascadeType.ALL)
//    @JoinTable(name = "connections", joinColumns = @JoinColumn(name = "eventId"), inverseJoinColumns = @JoinColumn(name = "connectionId"))

    private Connections connections;

//    public void addConnection(Connections connections) {
//        this.connections.add(connections);
//    }


    public EventGroup() {
    }
}


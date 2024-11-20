package com.messagingservice.backendservice.model.provider;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import java.util.Set;

@AllArgsConstructor
@Entity
@Table(name = "events",
        uniqueConstraints = { @UniqueConstraint(name = "EventName_Provider", columnNames = {"eventName", "pe_fk"})}
    )
@Getter
@Setter
public class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eventId")
    private long eventId;
    @Column(nullable = false, name = "eventName")
    private String eventName;
    @Column(nullable = false)
    private String eventType; //push or pull
    @Column(nullable = false)
    private String orderOfEvents; //serial or Concurrent
    @Column(nullable = false)
    private boolean active = true;
    @ElementCollection
    @CollectionTable(name = "event_filters",
            joinColumns = @JoinColumn(name = "id"))
    @Column(name = "filters")
    private Set<String> filters;
    private String dataFormat;

//    @OneToMany(cascade = CascadeType.ALL)
//    @JoinTable(name = "connections", joinColumns = @JoinColumn(name = "eventId"), inverseJoinColumns = @JoinColumn(name = "connectionId"))
//    @OneToMany(targetEntity = Connections.class, cascade = CascadeType.ALL)
//    @JoinColumn(name="event_connection_fk", referencedColumnName = "eventId")
//    private Set<Connections> connections;
    @ManyToOne
    @JoinColumn(name="connection_id", nullable = true)
    private Connections connections;

//    public void addConnection(Connections connections) {
//        this.connections.add(connections);
//    }

    public Events() {
    }
}

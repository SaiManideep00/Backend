package com.messagingservice.backendservice.model.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SubscribedEvents", uniqueConstraints = { @UniqueConstraint(name = "SubscribedEvent_Consumer", columnNames = {"eventId", "consumer_events_fk"})})

public class SubscribedEvents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscriptionId")
    private long subscriptionId;
    @Column(nullable = false)
    private long eventId;
    @Column(nullable = false)
    private String eventName;
    @Column(nullable = false)
    private String providerName;
    @Column(nullable = false)
    private String dataFormat; //Json or XML
    @Column
    private String sourceDataFormat;
    @Column(nullable = false)
    private boolean active = true;
    @OneToMany(targetEntity = Filter.class, cascade = CascadeType.ALL)
    @JoinColumn(name="ef_fk", referencedColumnName = "subscriptionId")
    private List<Filter> filters;
    @ManyToOne
    @JoinColumn(name="connection_id")
    private SubscribedEventConnections subscribedEventConnections;

    public void addFilters(Filter filter) {
        this.filters.add(filter);
    }
}

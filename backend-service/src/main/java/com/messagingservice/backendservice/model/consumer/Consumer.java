package com.messagingservice.backendservice.model.consumer;

import lombok.*;

import jakarta.persistence.*;
import java.util.List;
import java.util.Set;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "consumers", uniqueConstraints = { @UniqueConstraint(name = "consumerName", columnNames = "consumerName")})
public class Consumer {
    @Id
    @Column(name = "consumerId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long consumerId;
    @Column(name = "consumerName")
    private String consumerName;
    @Column(nullable = false)
    private boolean active = true;
    @Embedded
    private ConsumerTechnicalPOC consumerTechnicalPOC;
    @Embedded
    private ConsumerBusinessPOC consumerBusinessPOC;
    private String alertNotificationEmailID;

    @OneToMany(targetEntity = SubscribedEvents.class, cascade = CascadeType.ALL)
    @JoinColumn(name="consumer_events_fk", referencedColumnName = "consumerId")
    private Set<SubscribedEvents> subscribedEvents;

    @OneToMany(targetEntity = AlertSubscription.class, cascade = CascadeType.ALL)
    @JoinColumn(name="consumer_alerts_fk", referencedColumnName = "consumerId")
    private Set<AlertSubscription> alertSubscriptions;

    @OneToMany(targetEntity = SubscribedEventConnections.class, cascade = CascadeType.ALL)
    @JoinColumn(name="cc_fk", referencedColumnName = "consumerId", nullable = false)
    private List<SubscribedEventConnections> connections;

    public void addEvent(SubscribedEvents subscribedEvents){
        this.subscribedEvents.add(subscribedEvents);
    }
    public void addConnections(SubscribedEventConnections connection){
        this.connections.add(connection);
    }

    public void addAlert(AlertSubscription alertSubscription){
        this.alertSubscriptions.add(alertSubscription);
    }

}

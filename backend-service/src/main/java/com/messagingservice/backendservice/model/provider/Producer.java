package com.messagingservice.backendservice.model.provider;

import lombok.*;

import jakarta.persistence.*;

import java.util.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "providers", uniqueConstraints = { @UniqueConstraint(name = "providerName", columnNames = "providerName")}) //uniqueConstraints = { @UniqueConstraint(columnNames = { "providerName"})}
public class Producer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "providerId")
    private long providerId;
    @Column(name = "providerName")
    private String providerName;
    @Column(nullable = false)
    private boolean active = true;
    @Embedded
    private ProviderTechnicalPOC providerTechnicalPOC;
    @Embedded
    private ProviderBusinessPOC providerBusinessPOC;
    private String alertNotificationEmailID;
    public Producer(long providerId, String providerName, boolean active, ProviderTechnicalPOC providerTechnicalPOC, ProviderBusinessPOC providerBusinessPOC, String alertNotificationEmailID) {
        this.providerId = providerId;
        this.providerName = providerName;
        this.active = active;
        this.providerTechnicalPOC = providerTechnicalPOC;
        this.providerBusinessPOC = providerBusinessPOC;
        this.alertNotificationEmailID = alertNotificationEmailID;
    }

//    @OneToMany(cascade = CascadeType.ALL)
//    @JoinTable(name = "events", joinColumns = @JoinColumn(name = "providerId"), inverseJoinColumns = @JoinColumn(name = "eventId"))
    @OneToMany(targetEntity = Events.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name="pe_fk", referencedColumnName = "providerId")
    private List<Events> events;
    @OneToMany(targetEntity = Connections.class, cascade = CascadeType.ALL)
    @JoinColumn(name="pc_fk", referencedColumnName = "providerId", nullable = false)
    private List<Connections> connections;
    public void addEvents(Events event) {
        this.events.add(event);
    }
    public void addConnections(Connections connection){
        this.connections.add(connection);
    }

}

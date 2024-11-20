package com.messagingservice.backendservice.model.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SubscribedEventConnections", uniqueConstraints = { @UniqueConstraint(name = "url", columnNames = "url"), @UniqueConstraint(name = "consumer_connectionName", columnNames = {"connection_name", "cc_fk"})})
public class SubscribedEventConnections {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long connectionId;
    @Column(nullable = false, name = "connection_name")
    private String connectionName;
    @Column(nullable = false)
    private String connectionType;
    @Column(nullable = false)
    private String url;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private boolean active = true;

}

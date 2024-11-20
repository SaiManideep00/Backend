package com.messagingservice.backendservice.model.consumer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "DBWatchlist")
public class DBWatchlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "watchlistId", nullable = false)
    private Long watchlistId;
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

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            },
            mappedBy = "watchlists")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<AlertSubscription> alertSubscriptions = new HashSet<>();

}

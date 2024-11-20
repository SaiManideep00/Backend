package com.messagingservice.backendservice.model.consumer;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "AlertSubscription")
public class AlertSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alertId")
    private long alertId;
    @Column(nullable = false)
    private long eventId;
    @Column(nullable = false)
    private String eventName;
    @Column(nullable = false)
    private String providerName;
    @Column(nullable = false)
    private boolean active = true;
    @Column
    private String sourceDataFormat;
    @OneToOne
    @JoinColumn(name = "Subscription_criteria_fk")
    private AlertCriteria alertCriteria;
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "watchlist_subscription",
            joinColumns = @JoinColumn(name = "alert_id"),
            inverseJoinColumns = @JoinColumn(name = "watchlist_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<DBWatchlist> watchlists = new HashSet<>();

    public void addWatchlist(DBWatchlist watchlist){
        this.getWatchlists().add(watchlist);
        watchlist.getAlertSubscriptions().add(this);
    }

    public void removeWatchlist(long watchlistId) {
        DBWatchlist dbWatchlist = this.watchlists.stream().filter(wl -> wl.getWatchlistId() == watchlistId).findFirst().orElse(null);
        if (dbWatchlist != null) {
            this.watchlists.remove(dbWatchlist);
            dbWatchlist.getAlertSubscriptions().remove(this);
        }
    }

}

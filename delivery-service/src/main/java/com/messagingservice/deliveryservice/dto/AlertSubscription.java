package com.messagingservice.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertSubscription {
    private long alertId;
    private long eventId;
    private String eventName;
    private String providerName;
    private boolean active = true;
    private AlertCriteria alertCriteria;
    private Set<DBWatchlist> watchlists = new HashSet<>();
    private String sourceDataFormat;
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

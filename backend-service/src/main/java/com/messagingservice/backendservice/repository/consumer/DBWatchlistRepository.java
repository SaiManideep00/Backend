package com.messagingservice.backendservice.repository.consumer;

import com.messagingservice.backendservice.model.consumer.DBWatchlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DBWatchlistRepository extends JpaRepository<DBWatchlist, Long> {
}

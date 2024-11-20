package com.messagingservice.backendservice.repository.provider;


import com.messagingservice.backendservice.model.provider.Events;
import com.messagingservice.backendservice.model.provider.Producer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventsRepository extends JpaRepository<Events, Long> {

    Optional<Events> findByEventName(String eventName);
    @Query("SELECT p FROM Producer p JOIN p.events e WHERE e.eventId = :eventId")
    Optional<Producer> findProviderByEventId(@Param("eventId") Long eventId);
//    Optional<String>

}

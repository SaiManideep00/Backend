package com.messagingservice.backendservice.repository.consumer;

import com.messagingservice.backendservice.model.consumer.Consumer;
import com.messagingservice.backendservice.model.consumer.SubscribedEvents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubscribedEventsRepository extends JpaRepository<SubscribedEvents, Long> {
    Optional<SubscribedEvents> findByEventName(String eventName);
    int deleteByEventId(long eventId);

    Optional<SubscribedEvents> findByEventNameAndProviderName(String eventName, String Provider);

//

    @Query(value = "SELECT se.* FROM SubscribedEvents se " +
            "JOIN consumers c ON c.consumerId = se.consumer_events_fk " +
            "WHERE c.consumerId = :consumerId " +
            "AND se.eventName = :eventName " +
            "AND se.providerName = :providerName", nativeQuery = true)
    Optional<SubscribedEvents> findByConsumerIdAndEventNameAndProviderName(
            Long consumerId, String eventName, String providerName);



    @Query("SELECT c FROM Consumer c JOIN c.subscribedEvents se WHERE se.subscriptionId = :subscriptionId")
    Optional<Consumer> findConsumerBySubscriptionId(@Param("subscriptionId") Long subscriptionId);
}

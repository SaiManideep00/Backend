package com.messagingservice.backendservice.repository.consumer;

import com.messagingservice.backendservice.model.consumer.AlertSubscription;
import com.messagingservice.backendservice.model.consumer.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AlertSubscriptionRepository extends JpaRepository<AlertSubscription,Long> {
    @Query(value = "SELECT als.* FROM AlertSubscription als " +
            "JOIN consumers c ON c.consumerId = als.consumer_alerts_fk " +
            "WHERE c.consumerId = :consumerId " +
            "AND als.eventName = :eventName " +
            "AND als.providerName = :providerName", nativeQuery = true)
    Optional<AlertSubscription> findByConsumerIdAndEventNameAndProviderName(
            Long consumerId, String eventName, String providerName);

    @Query("SELECT c FROM Consumer c JOIN c.alertSubscriptions als WHERE als.alertId = :alertId")
    Optional<Consumer> findConsumerByAlertId(@Param("alertId") Long alertId);
}

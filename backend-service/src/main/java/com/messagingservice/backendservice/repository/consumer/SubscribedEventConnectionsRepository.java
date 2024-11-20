package com.messagingservice.backendservice.repository.consumer;

import com.messagingservice.backendservice.model.consumer.SubscribedEventConnections;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubscribedEventConnectionsRepository extends JpaRepository<SubscribedEventConnections, Long> {
    @Query(value = "SELECT DISTINCT cc FROM Consumer c JOIN c.connections cc WHERE c.consumerId = :consumerId and cc.connectionName = :connectionName")
    Optional<SubscribedEventConnections> findByConnectionName(@Param("consumerId") Long consumerId, @Param("connectionName") String connectionName);
}

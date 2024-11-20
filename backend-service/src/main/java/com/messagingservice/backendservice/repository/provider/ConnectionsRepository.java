package com.messagingservice.backendservice.repository.provider;

import com.messagingservice.backendservice.model.provider.Connections;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConnectionsRepository extends JpaRepository<Connections, Long> {
    //SELECT c.* FROM connections c JOIN  WHERE p.provider_id = :providerId AND c.connection_name = :connectionName;
//    @Query(value = "SELECT DISTINCT c FROM Producer p JOIN p.connections c WHERE p.providerId = :providerId and c.connectionName = :connectionName")
//    Optional<Connections> findByConnectionName(@Param("providerId") Long providerId, @Param("connectionName") String connectionName);

    @Query(value = "SELECT c.* FROM providers p JOIN connections c ON p.providerId = c.pc_fk WHERE p.providerId = :providerId AND c.connection_name = :connectionName", nativeQuery = true)
    Optional<Connections> findByConnectionName(Long providerId, String connectionName);
}

package com.messagingservice.backendservice.repository.provider;

import com.messagingservice.backendservice.model.provider.Producer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProducerRepository extends JpaRepository<Producer,Long> {
    //List<Producer> findByConnectionType(String connectionType);
    Optional<Producer> findByProviderName(String providerName);

    Optional<Producer> findByProviderId(long providerId);

     //void deleteByProviderId(long providerId);
    //Optional<Set<Events>> findByProviderName(String providerName);

}

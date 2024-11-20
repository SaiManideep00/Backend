package com.messagingservice.backendservice.repository.consumer;

import com.messagingservice.backendservice.model.consumer.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsumerRepository extends JpaRepository<Consumer, Long> {
    Optional<Consumer> findByConsumerName(String consumerName);
}

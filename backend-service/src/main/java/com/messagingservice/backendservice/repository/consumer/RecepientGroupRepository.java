package com.messagingservice.backendservice.repository.consumer;

import com.messagingservice.backendservice.model.consumer.RecepientGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecepientGroupRepository extends JpaRepository<RecepientGroup, Long> {
}

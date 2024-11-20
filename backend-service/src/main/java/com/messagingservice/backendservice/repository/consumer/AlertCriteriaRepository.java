package com.messagingservice.backendservice.repository.consumer;

import com.messagingservice.backendservice.model.consumer.AlertCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertCriteriaRepository extends JpaRepository<AlertCriteria, Long> {
}

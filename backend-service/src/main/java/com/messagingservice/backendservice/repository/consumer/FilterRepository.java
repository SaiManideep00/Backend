package com.messagingservice.backendservice.repository.consumer;

import com.messagingservice.backendservice.model.consumer.Filter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterRepository extends JpaRepository<Filter, Long> {

}

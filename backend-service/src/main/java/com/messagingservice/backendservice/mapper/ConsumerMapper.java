package com.messagingservice.backendservice.mapper;

import com.messagingservice.backendservice.dto.consumer.ConsumerBasicDetailsDTO;
import com.messagingservice.backendservice.model.consumer.Consumer;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface ConsumerMapper {
    ConsumerBasicDetailsDTO toConsumerBasicDetailsDTO(Consumer producer);
    List<ConsumerBasicDetailsDTO> toConsumerBasicDetailsDTO(List<Consumer> producer);
}

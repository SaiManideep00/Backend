package com.messagingservice.backendservice.mapper;

import com.messagingservice.backendservice.dto.consumer.ConsumerBasicDetailsDTO;
import com.messagingservice.backendservice.model.consumer.Consumer;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class ConsumerMapperImpl implements ConsumerMapper {

    @Override
    public ConsumerBasicDetailsDTO toConsumerBasicDetailsDTO(Consumer producer) {
        if ( producer == null ) {
            return null;
        }

        ConsumerBasicDetailsDTO consumerBasicDetailsDTO = new ConsumerBasicDetailsDTO();

        consumerBasicDetailsDTO.setConsumerId( producer.getConsumerId() );
        consumerBasicDetailsDTO.setConsumerName( producer.getConsumerName() );
        consumerBasicDetailsDTO.setActive( producer.isActive() );
        consumerBasicDetailsDTO.setConsumerTechnicalPOC( producer.getConsumerTechnicalPOC() );
        consumerBasicDetailsDTO.setConsumerBusinessPOC( producer.getConsumerBusinessPOC() );
        consumerBasicDetailsDTO.setAlertNotificationEmailID( producer.getAlertNotificationEmailID() );

        return consumerBasicDetailsDTO;
    }

    @Override
    public List<ConsumerBasicDetailsDTO> toConsumerBasicDetailsDTO(List<Consumer> producer) {
        if ( producer == null ) {
            return null;
        }

        List<ConsumerBasicDetailsDTO> list = new ArrayList<ConsumerBasicDetailsDTO>( producer.size() );
        for ( Consumer consumer : producer ) {
            list.add( toConsumerBasicDetailsDTO( consumer ) );
        }

        return list;
    }
}

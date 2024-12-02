package com.messagingservice.backendservice.mapper;

import com.messagingservice.backendservice.dto.provider.ProviderBasicDetailsDTO;
import com.messagingservice.backendservice.model.provider.Producer;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class ProviderMapperImpl implements ProviderMapper {

    @Override
    public ProviderBasicDetailsDTO toProviderBasicDetailsDTO(Producer producer) {
        if ( producer == null ) {
            return null;
        }

        ProviderBasicDetailsDTO providerBasicDetailsDTO = new ProviderBasicDetailsDTO();

        providerBasicDetailsDTO.setProviderId( producer.getProviderId() );
        providerBasicDetailsDTO.setProviderName( producer.getProviderName() );
        providerBasicDetailsDTO.setActive( producer.isActive() );
        providerBasicDetailsDTO.setProviderTechnicalPOC( producer.getProviderTechnicalPOC() );
        providerBasicDetailsDTO.setProviderBusinessPOC( producer.getProviderBusinessPOC() );
        providerBasicDetailsDTO.setAlertNotificationEmailID( producer.getAlertNotificationEmailID() );

        return providerBasicDetailsDTO;
    }

    @Override
    public List<ProviderBasicDetailsDTO> toProviderBasicDetailsDTO(List<Producer> producer) {
        if ( producer == null ) {
            return null;
        }

        List<ProviderBasicDetailsDTO> list = new ArrayList<ProviderBasicDetailsDTO>( producer.size() );
        for ( Producer producer1 : producer ) {
            list.add( toProviderBasicDetailsDTO( producer1 ) );
        }

        return list;
    }
}

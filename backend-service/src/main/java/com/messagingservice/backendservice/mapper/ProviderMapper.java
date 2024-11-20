package com.messagingservice.backendservice.mapper;

import com.messagingservice.backendservice.dto.provider.ProviderBasicDetailsDTO;
import com.messagingservice.backendservice.model.provider.Producer;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface ProviderMapper {
    ProviderBasicDetailsDTO toProviderBasicDetailsDTO(Producer producer);
    List<ProviderBasicDetailsDTO> toProviderBasicDetailsDTO(List<Producer> producer);

}

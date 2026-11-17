package com.covoiturage.covoiturage.mapper.Passager;

import com.covoiturage.covoiturage.dto.Passager.PassagerRequest;
import com.covoiturage.covoiturage.dto.Passager.PassagerResponse;
import com.covoiturage.covoiturage.model.Passager;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PassagerMapper {

    @Mapping(target = "id", ignore = true)
    Passager toEntity(PassagerRequest request);

    PassagerResponse toResponse(Passager passager);

    @Mapping(target = "id", ignore = true)
    void updateEntity(PassagerRequest request, @MappingTarget Passager passager);
}
package com.covoiturage.covoiturage.mapper.Vehicule;

import com.covoiturage.covoiturage.dto.Vehicule.VehiculeRequest;
import com.covoiturage.covoiturage.dto.Vehicule.VehiculeResponse;
import com.covoiturage.covoiturage.model.Vehicule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VehiculeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    Vehicule toEntity(VehiculeRequest request);

    @Mapping(target = "conducteurId", source = "conducteur.id")
    VehiculeResponse toResponse(Vehicule vehicule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    void updateEntity(VehiculeRequest request, @MappingTarget Vehicule vehicule);
}
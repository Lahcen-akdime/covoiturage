package com.covoiturage.covoiturage.mapper.Reservation;

import com.covoiturage.covoiturage.dto.Reservation.ReservationResponse;
import com.covoiturage.covoiturage.model.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "trajetId", source = "trajet.id")
    @Mapping(target = "dateDepartTrajet", source = "trajet.dateDepart")
    @Mapping(target = "villeMontee", source = "pointMontee.ville")
    @Mapping(target = "villeDescente", source = "pointDescente.ville")
    @Mapping(target = "passagerId", source = "passager.id")
    @Mapping(target = "passagerNom", source = "passager.nom")
    @Mapping(target = "passagerPrenom", source = "passager.prenom")
    ReservationResponse toResponse(Reservation reservation);
}
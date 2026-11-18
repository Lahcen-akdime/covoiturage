package com.covoiturage.covoiturage.mapper.Trajet;

import com.covoiturage.covoiturage.dto.Trajet.TrajetDetailResponse;
import com.covoiturage.covoiturage.dto.conducteur.ConducteurResume;
import com.covoiturage.covoiturage.dto.pointArret.PointArretResponse;
import com.covoiturage.covoiturage.mapper.Vehicule.VehiculeMapper;
import com.covoiturage.covoiturage.model.Conducteur;
import com.covoiturage.covoiturage.model.PointArret;
import com.covoiturage.covoiturage.model.Trajet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = VehiculeMapper.class)
public interface TrajetMapper {

    @Mapping(target = "placesRestantes", source = "placesRestantes")
    TrajetDetailResponse toDetail(Trajet trajet, int placesRestantes);

    ConducteurResume toResume(Conducteur conducteur);

    PointArretResponse toPointResponse(PointArret pointArret);
}
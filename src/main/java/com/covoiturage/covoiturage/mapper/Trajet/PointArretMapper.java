package com.covoiturage.covoiturage.mapper.Trajet;

import com.covoiturage.covoiturage.dto.pointArret.PointArretResponse;
import com.covoiturage.covoiturage.model.PointArret;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PointArretMapper {

    PointArretResponse toResponse(PointArret point);

}

package com.covoiturage.covoiturage.dto.pointArret;

import java.math.BigDecimal;
import java.time.LocalTime;

public record PointArretResponse(
        Long id,
        String ville,
        int ordre,
        LocalTime heurePassage,
        BigDecimal prixDepuisDepart
) {
}
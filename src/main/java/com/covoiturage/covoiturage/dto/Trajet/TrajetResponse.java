package com.covoiturage.covoiturage.dto.Trajet;

import com.covoiturage.covoiturage.dto.pointArret.PointArretResponse;
import com.covoiturage.covoiturage.model.Enum.StatutTrajet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record TrajetResponse(
        Long id,
        String villeDepart,
        String villeArrivee,
        LocalDateTime dateDepart,
        int placesProposees,
        StatutTrajet statut,
        String conducteurNom,
        List<PointArretResponse> pointsArret) {}
package com.covoiturage.covoiturage.dto.Trajet;

import com.covoiturage.covoiturage.dto.Vehicule.VehiculeResponse;
import com.covoiturage.covoiturage.dto.conducteur.ConducteurResume;
import com.covoiturage.covoiturage.dto.pointArret.PointArretResponse;
import com.covoiturage.covoiturage.model.Enum.StatutTrajet;

import java.time.LocalDateTime;
import java.util.List;

public record TrajetDetailResponse(
        Long id,
        String villeDepart,
        String villeArrivee,
        LocalDateTime dateDepart,
        int placesProposees,
        int placesRestantes,
        StatutTrajet statut,
        ConducteurResume conducteur,
        VehiculeResponse vehicule,
        List<PointArretResponse> pointsArret
) {
}
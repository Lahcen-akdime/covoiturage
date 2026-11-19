package com.covoiturage.covoiturage.dto.Trajet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record TrajetRechercheResponse(
        Long trajetId,
        LocalDateTime dateDepart,
        Long pointMonteeId,
        String villeMontee,
        LocalTime heureMontee,
        Long pointDescenteId,
        String villeDescente,
        LocalTime heureDescente,
        BigDecimal prixSegment,
        Long placesRestantes,
        String conducteurNom,
        String conducteurPrenom
) {
}
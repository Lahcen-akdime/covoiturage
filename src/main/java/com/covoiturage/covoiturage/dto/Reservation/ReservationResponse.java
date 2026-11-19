package com.covoiturage.covoiturage.dto.Reservation;

import com.covoiturage.covoiturage.model.Enum.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long trajetId,
        LocalDateTime dateDepartTrajet,
        String villeMontee,
        String villeDescente,
        int nombrePlaces,
        BigDecimal montantTotal,
        LocalDateTime dateReservation,
        StatutReservation statut,
        Long passagerId,
        String passagerNom,
        String passagerPrenom
) {
}
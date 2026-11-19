package com.covoiturage.covoiturage.dto.Reservation;

import com.covoiturage.covoiturage.model.Enum.StatutTrajet;

public record ReservationCreeeResponse(
        ReservationResponse reservation,
        int placesRestantes,
        StatutTrajet statutTrajet
) {
}
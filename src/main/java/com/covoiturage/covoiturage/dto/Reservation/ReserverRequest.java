package com.covoiturage.covoiturage.dto.Reservation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReserverRequest(
        @NotNull(message = "Le trajet est obligatoire")
        Long trajetId,

        @NotNull(message = "Le point de montée est obligatoire")
        Long pointMonteeId,

        @NotNull(message = "Le point de descente est obligatoire")
        Long pointDescenteId,

        @NotNull(message = "Le nombre de places est obligatoire")
        @Min(value = 1, message = "Il faut reserver au moins 1 place")
        Integer nombrePlaces
) {
}
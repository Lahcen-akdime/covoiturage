package com.covoiturage.covoiturage.dto.Trajet;

import com.covoiturage.covoiturage.dto.pointArret.PointArretRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record PublierTrajetRequest(
        @NotNull(message = "Le véhicule est obligatoire")
        Long vehiculeId,

        @NotNull(message = "Le nombre de places proposees est obligatoire")
        @Min(value = 1, message = "Il faut proposer au moins 1 place")
        Integer placesProposees,

        @NotNull(message = "La date de depart est obligatoire")
        LocalDateTime dateDepart,

        @NotNull(message = "Le parcours est obligatoire")
        @Size(min = 2, message = "Un trajet doit avoir au moins 2 points (départ et arrive)")
        @Valid
        List<PointArretRequest> pointsArret
) {
}
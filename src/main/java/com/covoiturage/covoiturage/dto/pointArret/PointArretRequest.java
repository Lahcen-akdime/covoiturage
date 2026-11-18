package com.covoiturage.covoiturage.dto.pointArret;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalTime;

public record PointArretRequest(
        @NotBlank(message = "La ville du point d'arret est obligatoire")
        @Size(max = 100, message = "La ville ne doit pas depasser 100 caracteres")
        String ville,

        @NotNull(message = "L'heure de passage est obligatoire")
        LocalTime heurePassage,

        @NotNull(message = "Le prix depuis le départ est obligatoire")
        @DecimalMin(value = "0.00", message = "Le prix ne peut pas etre negatif")
        @Digits(integer = 8, fraction = 2, message = "Le prix doit avoir au maximum 2 décimales")
        BigDecimal prixDepuisDepart
) {
}
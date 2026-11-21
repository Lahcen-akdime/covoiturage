package com.covoiturage.covoiturage.dto.Vehicule;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehiculeRequest(
        @NotBlank(message = "La marque est obligatoire")
        @Size(max = 50, message = "La marque est 50 chifre au max")
        String marque,

        @NotBlank(message = "Le modele obligatoire")
        @Size(max = 50, message = "Le modele est 50 caracteres au max")
        String modele,

        @NotBlank(message = "matricule nessaissaire")
        @Size(max = 20, message = "L'immatriculation 20 chiffre au max")
        String matricule,

        @NotBlank(message = "La couleur est obligatoire")
        @Size(max = 30, message = "La couleur est 30 chifres au max")
        String couleur,

        @NotNull(message = "La capacite est obligatoire")
        @Min(value = 1, message = "La capacite minimale est de 1 place")
        @Max(value = 8, message = "La capacite maximale est de 8 places")
        Integer capacite
) {
}
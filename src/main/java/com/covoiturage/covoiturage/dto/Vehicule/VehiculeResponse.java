package com.covoiturage.covoiturage.dto.Vehicule;

public record VehiculeResponse(
        Long id,
        String marque,
        String modele,
        String matricule,
        String couleur,
        int capacite,
        Long conducteurId
) {
}
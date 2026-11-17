package com.covoiturage.covoiturage.dto.Passager;

public record PassagerResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String telephone
) {
}
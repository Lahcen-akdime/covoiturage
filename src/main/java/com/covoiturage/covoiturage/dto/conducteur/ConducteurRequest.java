package com.covoiturage.covoiturage.dto.conducteur;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConducteurRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom est 100 au max")
        String nom,

        @NotBlank(message = "Le prenom est obligatoire")
        @Size(max = 100, message = "Le prenom est 100 caracteres au max")
        String prenom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email n'est pas valide")
        @Size(max = 150, message = "L'email ne doit pas depasser 150 caracteres au max")
        String email,


        @Size(max=30,min = 6,message = "le password doit contient 6-30 mot")
        @NotBlank(message = "Le password est obligatoire")
        String password ,

        @NotBlank(message = "Le teléphone est obligatoire")
        @Pattern(regexp = "^\\+?[0-9]{8,15}$",
                message = "Le telephone doit contenir de 8 à 15 chiffres")
        String telephone,

        @NotBlank(message = "Le numéro de permis est obligatoire")
        @Size(max = 30, message = "Le numéro de permis ne doit pas dépasser 30 caracteres")
        String numeroPermis
) {
}
package com.covoiturage.covoiturage.dto.Passager;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PassagerRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas depasser 100 caractere")
        String nom,

        @NotBlank(message = "Le prenom est obligatoire")
        @Size(max = 100, message = "Le prenom ne doit pas dépasser 100 caractere")
        String prenom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email n'est pas valide")
        @Size(max = 150, message = "L'email ne doit pas depasser 150 caracteres")
        String email,

        @Size(max=30,min = 6,message = "le password doit contient 6-30 mot")
        @NotBlank(message = "Le password est obligatoire")
        String password ,

        @NotBlank(message = "Le telephone est obligatoire")
        @Pattern(regexp = "^\\+?[0-9]{8,15}$",
                message = "Le telephone doit contenir de 8 à 15 chiffres")
        String telephone
) {
}
package com.covoiturage.covoiturage.model;

import com.covoiturage.covoiturage.model.Personne;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "conducteurs")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Conducteur extends Personne {

    @Column(name = "numero_permis", nullable = false, unique = true, length = 30)
    private String numeroPermis;
}
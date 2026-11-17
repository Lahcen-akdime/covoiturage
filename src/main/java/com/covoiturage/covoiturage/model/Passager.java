package com.covoiturage.covoiturage.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "passagers")
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class Passager extends Personne {

}

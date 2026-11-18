package com.covoiturage.covoiturage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "point_arrets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointArret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trajet_id", nullable = false,referencedColumnName = "id")
    private Trajet trajet;

    @Column(nullable = false, length = 100)
    private String ville;

    @Column(nullable = false)
    private int ordre;

    @Column(name = "heure_passage", nullable = false)
    private LocalTime heurePassage;

    @Column(name = "prix_depuis_depart", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixDepuisDepart;
}
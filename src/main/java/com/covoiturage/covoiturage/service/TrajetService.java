package com.covoiturage.covoiturage.service;

import com.covoiturage.covoiturage.dto.PageResponse;
import com.covoiturage.covoiturage.dto.Trajet.PublierTrajetRequest;
import com.covoiturage.covoiturage.dto.Trajet.TrajetDetailResponse;
import com.covoiturage.covoiturage.dto.Trajet.TrajetResponse;
import com.covoiturage.covoiturage.model.Trajet;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TrajetService {

    TrajetDetailResponse publier(Long conducteurId, PublierTrajetRequest request);

    TrajetDetailResponse consulterDetail(Long id);

    Page<TrajetResponse> rechercher(String villeDepart, String villeArrivee, LocalDate date,
                                    Integer places, BigDecimal prixMax, int page, int taille);
}
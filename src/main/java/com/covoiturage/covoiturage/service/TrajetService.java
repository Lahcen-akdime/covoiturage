package com.covoiturage.covoiturage.service;

import com.covoiturage.covoiturage.dto.Trajet.PublierTrajetRequest;
import com.covoiturage.covoiturage.dto.Trajet.TrajetDetailResponse;

public interface TrajetService {

    TrajetDetailResponse publier(Long conducteurId, PublierTrajetRequest request);

    TrajetDetailResponse consulterDetail(Long id);

}
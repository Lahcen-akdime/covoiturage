package com.covoiturage.covoiturage.service;

import com.covoiturage.covoiturage.dto.Vehicule.VehiculeRequest;
import com.covoiturage.covoiturage.dto.Vehicule.VehiculeResponse;

import java.util.List;
import java.util.UUID;

public interface VehiculeService {

    VehiculeResponse save(Long conducteurId, VehiculeRequest request);

    VehiculeResponse findById(Long id);

    List<VehiculeResponse> findAllByConducteurId(Long conducteurId);

    VehiculeResponse update(Long vehiculeId, VehiculeRequest request);

    void delete(Long conducteurId, Long vehiculeId);
}
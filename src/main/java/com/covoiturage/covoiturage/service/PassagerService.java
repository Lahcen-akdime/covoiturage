package com.covoiturage.covoiturage.service;

import com.covoiturage.covoiturage.dto.Passager.PassagerRequest;
import com.covoiturage.covoiturage.dto.Passager.PassagerResponse;
import com.covoiturage.covoiturage.dto.conducteur.ConducteurResponse;
import com.covoiturage.covoiturage.model.Passager;

import java.util.List;

public interface PassagerService {

    PassagerResponse save(PassagerRequest request);

    Passager findById(Long id);

    List<PassagerResponse> getList();

    PassagerResponse update(Long id, PassagerRequest request);

    PassagerResponse searchByEmail(String email);

    PassagerResponse getById(Long id);

    List<PassagerResponse> searchByNameOrEmail(String critere);
}
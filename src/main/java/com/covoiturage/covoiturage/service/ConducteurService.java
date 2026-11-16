package com.covoiturage.covoiturage.service;

import com.covoiturage.covoiturage.dto.conducteur.ConducteurRequest;
import com.covoiturage.covoiturage.dto.conducteur.ConducteurResponse;

import java.util.List;

public interface ConducteurService {

    ConducteurResponse save(ConducteurRequest request);

    ConducteurResponse getById(Long id);

    List<ConducteurResponse> getList();

    ConducteurResponse modifierConducteur(Long id, ConducteurRequest request);

    List<ConducteurResponse> searchByNameOrEmail(String critere);
}
package com.covoiturage.covoiturage.service.impl;

import com.covoiturage.covoiturage.dto.Vehicule.VehiculeRequest;
import com.covoiturage.covoiturage.dto.Vehicule.VehiculeResponse;
import com.covoiturage.covoiturage.exception.BusinessRuleException;
import com.covoiturage.covoiturage.exception.ForbiddenOperationException;
import com.covoiturage.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage.mapper.Vehicule.VehiculeMapper;
import com.covoiturage.covoiturage.model.Conducteur;
import com.covoiturage.covoiturage.model.Vehicule;
import com.covoiturage.covoiturage.repository.ConducteurRepository;
import com.covoiturage.covoiturage.repository.TrajetRepository;
import com.covoiturage.covoiturage.repository.VehiculeRepository;
import com.covoiturage.covoiturage.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;
    private final TrajetRepository trajetRepository;
    private final VehiculeMapper vehiculeMapper;

    @Override
    @Transactional
    public VehiculeResponse save(Long conducteurId, VehiculeRequest request) {
        Conducteur conducteur = findConducteur(conducteurId);
        String immatriculation = normaliserMatricule(request.matricule());

        if (vehiculeRepository.existsByMatriculeIgnoreCase(immatriculation)) {
            throw new BusinessRuleException("Vhicule avec matricule "
                    + immatriculation + " existe deja");
        }

        Vehicule vehicule = vehiculeMapper.toEntity(request);
        vehicule.setMatricule(immatriculation);
        vehicule.setConducteur(conducteur);

        Vehicule sauvegarde = vehiculeRepository.save(vehicule);
        log.info("Vehicule ajoute : id={}, conducteurId={}", sauvegarde.getId(), conducteurId);
        return vehiculeMapper.toResponse(sauvegarde);
    }

    @Override
    public VehiculeResponse findById(Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicule introuvable avec l'identifiant " + id));
        return vehiculeMapper.toResponse(vehicule);
    }

    @Override
    public List<VehiculeResponse> findAllByConducteurId(Long conducteurId) {
        findConducteur(conducteurId);
        return vehiculeRepository.findByConducteurId(conducteurId).stream()
                .map(vehiculeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public VehiculeResponse update(Long vehiculeId, VehiculeRequest request) {
        Vehicule vehicule = getById(vehiculeId);
        String matricule = normaliserMatricule(request.matricule());

        if (vehiculeRepository.existsByMatriculeIgnoreCaseAndIdNot(matricule, vehiculeId)) {
            throw new BusinessRuleException("Un autre vehicule utilise deja matricule "
                    + matricule + ".");
        }

        vehiculeMapper.updateEntity(request, vehicule);
        vehicule.setMatricule(matricule);

        Vehicule saved = vehiculeRepository.save(vehicule);
        log.info("Vehicule modifie : id={}", saved.getId());
        return vehiculeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long conducteurId, Long vehiculeId) {
        findConducteur(conducteurId);
        Vehicule vehicule = getVehiculeDuConducteur(conducteurId, vehiculeId);

        if (trajetRepository.existsByVehiculeId(vehiculeId)) {
            throw new BusinessRuleException("Ce véhicule est associé à au moins un trajet : "
                    + "il ne peut pas être supprimé.");
        }

        vehiculeRepository.delete(vehicule);
        log.info("Véhicule supprimé : id={}, conducteurId={}", vehiculeId, conducteurId);
    }

    private Conducteur findConducteur(Long conducteurId) {
        return conducteurRepository.findById(conducteurId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conducteur introuvable avec l'identifiant " + conducteurId));
    }

    private Vehicule getById(Long id){
        return vehiculeRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("La vehicule not found")) ;
    }

    private Vehicule getVehiculeDuConducteur(Long conducteurId, Long vehiculeId) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Véhicule introuvable avec l'identifiant " + vehiculeId));
        if (!vehicule.getConducteur().getId().equals(conducteurId)) {
            throw new ForbiddenOperationException("Ce vehicule n'appartient pas au conducteur "
                    + conducteurId + ".");
        }
        return vehicule;
    }

    private String normaliserMatricule(String immatriculation) {
        return immatriculation.trim().toUpperCase(Locale.ROOT);
    }
}
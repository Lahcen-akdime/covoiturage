package com.covoiturage.covoiturage.service.impl;

import com.covoiturage.covoiturage.dto.conducteur.ConducteurRequest;
import com.covoiturage.covoiturage.dto.conducteur.ConducteurResponse;
import com.covoiturage.covoiturage.exception.BusinessRuleException;
import com.covoiturage.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage.mapper.ConducteurMapper;
import com.covoiturage.covoiturage.model.Conducteur;
import com.covoiturage.covoiturage.repository.ConducteurRepository;
import com.covoiturage.covoiturage.service.ConducteurService;
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
public class ConducteurServiceImpl implements ConducteurService {

    private final ConducteurRepository conducteurRepository;
    private final ConducteurMapper conducteurMapper;

    @Override
    @Transactional
    public ConducteurResponse save(ConducteurRequest request) {
        String email = normaliserEmail(request.email());
        String numeroPermis = request.numeroPermis().trim();

        if (conducteurRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("Un conducteur avec l'email " + email + " existe deja");
        }
        if (conducteurRepository.existsByNumeroPermis(numeroPermis)) {
            throw new BusinessRuleException("Un conducteur avec le numéro de permis "
                    + numeroPermis + " existe deja ");
        }

        Conducteur conducteur = conducteurMapper.toEntity(request);
        conducteur.setEmail(email);
        conducteur.setNumeroPermis(numeroPermis);

        Conducteur newConducteur = conducteurRepository.save(conducteur);
        log.info("Conducteur cree : id={}", newConducteur.getId());
        return conducteurMapper.toResponse(newConducteur);
    }

    @Override
    public List<ConducteurResponse> getList() {
        return conducteurRepository.findAll().stream()
                .map(conducteurMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ConducteurResponse modifierConducteur(Long id, ConducteurRequest request) {
        Conducteur conducteur = findById(id);
        String email = normaliserEmail(request.email());
        String numeroPermis = request.numeroPermis().trim();

        if (conducteurRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessRuleException("Un autre conducteur utilise déjà l'email " + email + ".");
        }
        if (conducteurRepository.existsByNumeroPermisAndIdNot(numeroPermis, id)) {
            throw new BusinessRuleException("Un autre conducteur utilise déjà le numéro de permis "
                    + numeroPermis + ".");
        }

        conducteurMapper.updateEntity(request, conducteur);
        conducteur.setEmail(email);
        conducteur.setNumeroPermis(numeroPermis);

        Conducteur modified = conducteurRepository.save(conducteur);
        log.info("Conducteur modifié : id={}", modified.getId());
        return conducteurMapper.toResponse(modified);
    }

    @Override
    public List<ConducteurResponse> searchByNameOrEmail(String critere) {
        return conducteurRepository.rechercherParNomOuEmail(critere.trim()).stream()
                .map(conducteurMapper::toResponse)
                .toList();
    }

    @Override
    public ConducteurResponse getById(Long id){
        return conducteurMapper.toResponse(findById(id)) ;
    }


    public Conducteur findById(Long id) {
        return conducteurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conducteur introuvable avec l'identifiant " + id));
    }


    private String normaliserEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
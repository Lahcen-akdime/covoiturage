package com.covoiturage.covoiturage.service.impl;

import com.covoiturage.covoiturage.dto.Passager.PassagerRequest;
import com.covoiturage.covoiturage.dto.Passager.PassagerResponse;
import com.covoiturage.covoiturage.exception.BusinessRuleException;
import com.covoiturage.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage.mapper.Passager.PassagerMapper;
import com.covoiturage.covoiturage.model.Passager;
import com.covoiturage.covoiturage.repository.PassagerRepository;
import com.covoiturage.covoiturage.service.PassagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@Service
public class PassagerServiceImpl implements PassagerService {

    private final PassagerRepository passagerRepository;
    private final PassagerMapper passagerMapper;
    private final PasswordEncoder passwordEncoder ;

    @Override
    public PassagerResponse save(PassagerRequest request) {
        String email = normaliserEmail(request.email());

        if (passagerRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("Un passager avec l'email " + email + " existe deja");
        }

        Passager passager = passagerMapper.toEntity(request);
        passager.setEmail(email);
        passager.setPassword(passwordEncoder.encode(request.password()));

        Passager newPassager = passagerRepository.save(passager);
        log.info("Passager cree : id={}", newPassager.getId());
        return passagerMapper.toResponse(newPassager);
    }


    public PassagerResponse getById(Long id) {
        return passagerMapper.toResponse(findById(id));
    }

    @Override
    public List<PassagerResponse> searchByNameOrEmail(String critere) {
        return passagerRepository.rechercherParNomOuEmail(critere).stream().map(passagerMapper::toResponse).toList() ;
    }

    @Override
    public Passager findById(Long id) {
        return passagerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Passager introuvable avec l'identifiant " + id));
    }

    @Override
    public List<PassagerResponse> getList() {
        return passagerRepository.findAll().stream()
                .map(passagerMapper::toResponse)
                .toList();
    }

    @Override
    public PassagerResponse update(Long id, PassagerRequest request) {
        Passager passager = findById(id);
        String email = normaliserEmail(request.email());

        if (passagerRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessRuleException("Un autre passager utilise deja l'email " + email + ".");
        }

        passagerMapper.updateEntity(request, passager);
        passager.setEmail(email);

        Passager saved = passagerRepository.save(passager);
        log.info("Passager modifie : id={}", saved.getId());
        return passagerMapper.toResponse(saved);
    }

    @Override
    public PassagerResponse searchByEmail(String email) {
        return passagerRepository.findByEmailIgnoreCase(normaliserEmail(email))
                .map(passagerMapper::toResponse).orElseThrow(() -> new ResourceNotFoundException("Aucun passager trouvé avec cet email"));
    }

    private String normaliserEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /*public void login(String password){
        passwordEncoder.matches(password,encodedPassword)
    }*/
}

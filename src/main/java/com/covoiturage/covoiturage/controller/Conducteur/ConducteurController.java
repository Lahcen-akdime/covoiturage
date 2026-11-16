package com.covoiturage.covoiturage.controller.Conducteur;

import com.covoiturage.covoiturage.dto.conducteur.ConducteurRequest;
import com.covoiturage.covoiturage.dto.conducteur.ConducteurResponse;
import com.covoiturage.covoiturage.service.ConducteurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/v1/conducteurs")
@RequiredArgsConstructor
public class ConducteurController {

    private final ConducteurService conducteurService;

    @PostMapping
    public ResponseEntity<ConducteurResponse> save(@Valid @RequestBody ConducteurRequest request) {
        ConducteurResponse saved = conducteurService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<ConducteurResponse> getList() {
        return conducteurService.getList();
    }

    @GetMapping("/search")
    public List<ConducteurResponse> rechercher(@RequestParam("c") String critere) {
        return conducteurService.searchByNameOrEmail(critere);
    }

    @GetMapping("/{id}")
    public ConducteurResponse findById(@PathVariable Long id) {
        return conducteurService.getById(id);
    }

    @PutMapping("/{id}")
    public ConducteurResponse modifier(@PathVariable Long id,
                                       @Valid @RequestBody ConducteurRequest request) {
        return conducteurService.modifierConducteur(id, request);
    }
}
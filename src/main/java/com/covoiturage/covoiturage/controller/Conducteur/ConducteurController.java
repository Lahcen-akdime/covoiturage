package com.covoiturage.covoiturage.controller.Conducteur;

import com.covoiturage.covoiturage.dto.conducteur.ConducteurRequest;
import com.covoiturage.covoiturage.dto.conducteur.ConducteurResponse;
import com.covoiturage.covoiturage.service.ConducteurService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(description = "enregistrer un conducteur")
    public ResponseEntity<ConducteurResponse> save(@Valid @RequestBody ConducteurRequest request) {
        ConducteurResponse saved = conducteurService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @Operation(description = "afficher une liste des conducteurs")
    public List<ConducteurResponse> getList() {
        return conducteurService.getList();
    }

    @GetMapping("/search")
    @Operation(description = "rechercher une liste des conducteurs par nom ou email")
    public List<ConducteurResponse> rechercher(@RequestParam("c") String critere) {
        return conducteurService.searchByNameOrEmail(critere);
    }

    @GetMapping("/{id}")
    @Operation(description = "rechercher un conducteur par id")
    public ConducteurResponse findById(@PathVariable Long id) {
        return conducteurService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(description = "modifier un conducteur par leur id")
    public ConducteurResponse modifier(@PathVariable Long id,
                                       @Valid @RequestBody ConducteurRequest request) {
        return conducteurService.modifierConducteur(id, request);
    }
}
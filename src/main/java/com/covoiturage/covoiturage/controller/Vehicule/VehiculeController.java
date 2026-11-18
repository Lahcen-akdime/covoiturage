package com.covoiturage.covoiturage.controller.Vehicule;

import com.covoiturage.covoiturage.dto.Vehicule.VehiculeRequest;
import com.covoiturage.covoiturage.dto.Vehicule.VehiculeResponse;
import com.covoiturage.covoiturage.service.VehiculeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicules")
@RequiredArgsConstructor
public class VehiculeController {

    private final VehiculeService vehiculeService;

    @PostMapping("/conducteurs/{conducteurId}")
    public ResponseEntity<VehiculeResponse> ajouter(@PathVariable Long conducteurId,
                                                    @Valid @RequestBody VehiculeRequest request) {
        VehiculeResponse created = vehiculeService.save(conducteurId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/conducteurs/{conducteurId}")
    public List<VehiculeResponse> listerParConducteur(@PathVariable Long conducteurId) {
        return vehiculeService.findAllByConducteurId(conducteurId);
    }

    @GetMapping("/vehicules/{id}")
    public VehiculeResponse trouverParId(@PathVariable Long id) {
        return vehiculeService.findById(id);
    }

    @PutMapping("/{id}")
    public VehiculeResponse modifier(@PathVariable Long id,
                                     @Valid @RequestBody VehiculeRequest request) {
        return vehiculeService.update(id, request);
    }

    @DeleteMapping("/conducteurs/{conducteurId}/{vehiculeId}")
    public ResponseEntity<Void> supprimer(@PathVariable Long conducteurId,
                                          @PathVariable Long vehiculeId) {
        vehiculeService.delete(conducteurId, vehiculeId);
        return ResponseEntity.noContent().build();
    }
}
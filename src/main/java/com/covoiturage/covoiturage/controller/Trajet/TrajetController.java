package com.covoiturage.covoiturage.controller.Trajet;

import com.covoiturage.covoiturage.dto.Trajet.PublierTrajetRequest;
import com.covoiturage.covoiturage.dto.Trajet.TrajetDetailResponse;
import com.covoiturage.covoiturage.model.Trajet;
import com.covoiturage.covoiturage.service.TrajetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/trajets")
@RequiredArgsConstructor
public class TrajetController {

    private final TrajetService trajetService;

    @PostMapping("/conducteurs/{conducteurId}")
    public ResponseEntity<TrajetDetailResponse> publier(@PathVariable Long conducteurId,
                                                        @Valid @RequestBody PublierTrajetRequest request) {
        TrajetDetailResponse cree = trajetService.publier(conducteurId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/trajets/{id}").buildAndExpand(cree.id()).toUri();
        return ResponseEntity.created(location).body(cree);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrajetDetailResponse> consulterDetail(@PathVariable Long id) {
        return ResponseEntity.ok().body(trajetService.consulterDetail(id));
    }

    @GetMapping("/recherche")
    public Page<Trajet> rechercher(
            @RequestParam String villeDepart,
            @RequestParam String villeArrivee,
            @RequestParam LocalDate date,
            @RequestParam(required = false) Integer places,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille) {
        return trajetService.rechercher(villeDepart, villeArrivee, date, places, prixMax, page, taille);
    }

}
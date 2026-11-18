package com.covoiturage.covoiturage.controller.Trajet;

import com.covoiturage.covoiturage.dto.Trajet.PublierTrajetRequest;
import com.covoiturage.covoiturage.dto.Trajet.TrajetDetailResponse;
import com.covoiturage.covoiturage.service.TrajetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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

}
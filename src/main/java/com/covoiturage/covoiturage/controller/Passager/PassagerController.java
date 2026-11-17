package com.covoiturage.covoiturage.controller.Passager;

import com.covoiturage.covoiturage.dto.Passager.PassagerRequest;
import com.covoiturage.covoiturage.dto.Passager.PassagerResponse;
import com.covoiturage.covoiturage.dto.conducteur.ConducteurResponse;
import com.covoiturage.covoiturage.service.PassagerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/passagers")
@RequiredArgsConstructor
public class PassagerController {

    private final PassagerService passagerService ;

    @PostMapping
    public ResponseEntity<PassagerResponse> save(@Valid @RequestBody PassagerRequest request) {
        PassagerResponse created = passagerService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<PassagerResponse> lister() {
        return passagerService.getList();
    }

    @GetMapping("/{id}")
    public PassagerResponse trouverParId(@PathVariable Long id) {
        return passagerService.getById(id);
    }

    @PutMapping("/{id}")
    public PassagerResponse modifier(@PathVariable Long id,
                                     @Valid @RequestBody PassagerRequest request) {
        return passagerService.update(id, request);
    }

    @GetMapping("/search")
    @Operation(description = "rechercher une liste des passagers par nom ou email")
    public List<PassagerResponse> rechercher(@RequestParam("c") String critere) {
        return passagerService.searchByNameOrEmail(critere);
    }

}

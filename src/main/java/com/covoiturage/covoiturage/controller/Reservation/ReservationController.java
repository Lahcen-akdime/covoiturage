package com.covoiturage.covoiturage.controller.Reservation;

import com.covoiturage.covoiturage.dto.Reservation.ReservationCreeeResponse;
import com.covoiturage.covoiturage.dto.Reservation.ReservationResponse;
import com.covoiturage.covoiturage.dto.Reservation.ReserverRequest;
import com.covoiturage.covoiturage.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/passagers/{passagerId}")
    public ResponseEntity<ReservationCreeeResponse> reserver(@PathVariable Long passagerId,
                                                             @Valid @RequestBody ReserverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.reserver(passagerId, request));
    }

    @GetMapping("/passagers/{passagerId}")
    public List<ReservationResponse> getReservationsOfPassager(@PathVariable Long passagerId) {
        return reservationService.getReservationsOfPassager(passagerId);
    }

    @PostMapping("/passagers/{passagerId}/{reservationId}/annulation")
    public ResponseEntity<Void> annuler(@PathVariable Long passagerId,
                                        @PathVariable Long reservationId) {
        reservationService.annuler(passagerId, reservationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conducteurs/{conducteurId}/trajets/{trajetId}")
    public List<ReservationResponse> getReservationsTrajetOfConducteur(@PathVariable Long conducteurId,
                                                                        @PathVariable Long trajetId) {
        return reservationService.getReservationsTrajet(conducteurId, trajetId);
    }
}
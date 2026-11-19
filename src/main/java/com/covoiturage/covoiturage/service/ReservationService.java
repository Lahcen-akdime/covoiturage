package com.covoiturage.covoiturage.service;

import com.covoiturage.covoiturage.dto.Reservation.ReservationCreeeResponse;
import com.covoiturage.covoiturage.dto.Reservation.ReservationResponse;
import com.covoiturage.covoiturage.dto.Reservation.ReserverRequest;

import java.util.List;

public interface ReservationService {

    ReservationCreeeResponse reserver(Long passagerId, ReserverRequest request);

    void annuler(Long passagerId, Long reservationId);

    List<ReservationResponse> getReservationsOfPassager(Long passagerId);

    List<ReservationResponse> getReservationsTrajet(Long conducteurId, Long trajetId);
}
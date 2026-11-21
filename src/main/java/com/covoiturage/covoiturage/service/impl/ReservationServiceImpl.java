package com.covoiturage.covoiturage.service.impl;

import com.covoiturage.covoiturage.dto.Reservation.ReservationCreeeResponse;
import com.covoiturage.covoiturage.dto.Reservation.ReservationResponse;
import com.covoiturage.covoiturage.dto.Reservation.ReserverRequest;
import com.covoiturage.covoiturage.exception.BusinessRuleException;
import com.covoiturage.covoiturage.exception.ForbiddenOperationException;
import com.covoiturage.covoiturage.exception.InvalidRequestException;
import com.covoiturage.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage.mapper.Reservation.ReservationMapper;
import com.covoiturage.covoiturage.model.Enum.StatutReservation;
import com.covoiturage.covoiturage.model.Enum.StatutTrajet;
import com.covoiturage.covoiturage.model.Passager;
import com.covoiturage.covoiturage.model.PointArret;
import com.covoiturage.covoiturage.model.Reservation;
import com.covoiturage.covoiturage.model.Trajet;
import com.covoiturage.covoiturage.repository.ConducteurRepository;
import com.covoiturage.covoiturage.repository.PassagerRepository;
import com.covoiturage.covoiturage.repository.ReservationRepository;
import com.covoiturage.covoiturage.repository.TrajetRepository;
import com.covoiturage.covoiturage.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private static final long annulation_hours = 24;

    private final ReservationRepository reservationRepository;
    private final PassagerRepository passagerRepository;
    private final ConducteurRepository conducteurRepository;
    private final TrajetRepository trajetRepository;
    private final ReservationMapper reservationMapper;
    private final Clock clock;

    @Override
    @Transactional
    public ReservationCreeeResponse reserver(Long passagerId, ReserverRequest request) {

        // On vérifie d'abord que le passager et le trajet existent.
        Passager passager = passagerRepository.findById(passagerId).orElseThrow(() -> new ResourceNotFoundException(
        "Passager introuvable avec l'identifiant " + passagerId));

        Trajet trajet = trajetRepository.findByIdForUpdate(request.trajetId()).orElseThrow(() -> new ResourceNotFoundException(
        "Trajet introuvable avec l'identifiant " + request.trajetId()));

        // Le point de montee doit etre avant le point de descente
        PointArret montee = findPoint(trajet, request.pointMonteeId()) ;
        PointArret descente = findPoint(trajet, request.pointDescenteId()) ;

        if (montee.getOrdre() >= descente.getOrdre()) {
            throw new InvalidRequestException("Le point debut "+ montee.getVille()
                    +" doit etre avant le point de fin "
                    + descente.getVille());
        }

        // Vérification du statut du trajet et des places disponibles
        verifyTrajetOpen(trajet);

        if (reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(
                passagerId, trajet.getId(), StatutReservation.CONFIRMEE)) {
            throw new BusinessRuleException("Vous avez deja une reservation confirmee sur ce trajet");
        }

        int placesDemandees = request.nombrePlaces();
        int placesRestantes = calculateEmptyPlaces(trajet);

        if (placesDemandees > placesRestantes) {
            throw new BusinessRuleException("Il ne reste que " + placesRestantes
                    + " place(s) sur ce trajet : impossible d'en reserver " + placesDemandees);
        }

        // Création de la réservation avec les informations reçues.
        Reservation reservation = new Reservation();
        reservation.setPassager(passager);
        reservation.setTrajet(trajet);
        reservation.setPointMontee(montee);
        reservation.setPointDescente(descente);
        reservation.setNombrePlaces(placesDemandees);
        reservation.setMontantTotal(calculerMontantTotal(montee, descente, placesDemandees));
        reservation.setDateReservation(LocalDateTime.now(clock));
        reservation.setStatut(StatutReservation.CONFIRMEE);

        Reservation newReservation = reservationRepository.saveAndFlush(reservation);

        // si il ya aucun place vide
        int nombredespalacemaintenant = placesRestantes - placesDemandees;

        if (nombredespalacemaintenant == 0) {
            trajet.setStatut(StatutTrajet.COMPLET);
            trajetRepository.save(trajet);
            log.info("le rajet devient complette {}", trajet.getId());
        }

        log.info("reservation : id={} trajetId={} passagerId={} places={} montant={}",
                newReservation.getId(), trajet.getId(), passagerId, placesDemandees, newReservation.getMontantTotal());

        return new ReservationCreeeResponse(reservationMapper.toResponse(newReservation),
                nombredespalacemaintenant, trajet.getStatut());
    }

    @Override
    @Transactional
    public void annuler(Long passagerId, Long reservationId) {
        if(!passagerRepository.existsById(passagerId)){
            throw new ResourceNotFoundException("Le passager pas trouvee avec id "+passagerId) ;
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                                                        .orElseThrow(()->new ResourceNotFoundException("la reservation n'existe pas avec id "+reservationId)) ;

        if(!reservation.getPassager().getId().equals(passagerId)){
            throw new ForbiddenOperationException("la reservation est pas de ce passager");
        }

        Trajet trajet = trajetRepository.findByIdForUpdate(reservation.getTrajet().getId()).orElseThrow(()->new ResourceNotFoundException("trajet introuvable avec cette reservation")) ;

        if(reservation.getStatut() == StatutReservation.ANNULEE){
            throw new BusinessRuleException("La reservation est deja annulee") ;
        }

        if(LocalDateTime.now(clock).isAfter(trajet.getDateDepart().minusHours(annulation_hours))){
            throw new BusinessRuleException("c'est trop tard d'annulé maintenant !");
        }

        reservation.setStatut(StatutReservation.ANNULEE) ;
        reservationRepository.saveAndFlush(reservation) ;

        if(trajet.getStatut() == StatutTrajet.COMPLET && calculateEmptyPlaces(trajet)!=0){
            trajet.setStatut(StatutTrajet.PUBLIE);
            trajetRepository.save(trajet) ;
            log.info("le trajet est publiee maintenant encore");
        }
        log.info("reservation annulee "+reservationId);
    }

    @Override
    public List<ReservationResponse> getReservationsOfPassager(Long passagerId) {

        if (!passagerRepository.existsById(passagerId)) {
            throw new ResourceNotFoundException("Passager introuvable avec l'identifiant " + passagerId);
        }

        return reservationRepository.findHistoriqueParPassager(passagerId).stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Override
    public List<ReservationResponse> getReservationsTrajet(Long conducteurId, Long trajetId) {

        if (!conducteurRepository.existsById(conducteurId)) {
            throw new ResourceNotFoundException("Conducteur introuvable avec l'identifiant " + conducteurId);
        }

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trajet introuvable avec l'identifiant " + trajetId));

        // Seul le conducteur qui a créé le trajet peut voir ses réservations.
        if (!trajet.getConducteur().getId().equals(conducteurId)) {
            throw new ForbiddenOperationException("Ce trajet n'appartient pas au conducteur "
                    + conducteurId + " : vous ne pouvez pas consulter ses réservations.");
        }

        return reservationRepository.findParTrajet(trajetId).stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    private PointArret findPoint(Trajet trajet, Long pointId) {
        return trajet.getPointsArret().stream()
                .filter(p -> p.getId().equals(pointId))
                .findFirst().orElseThrow(() -> new InvalidRequestException("ce point n'est pas du trajet " + trajet.getId()));
    }

    private void verifyTrajetOpen(Trajet trajet) {
        if (trajet.getStatut() == StatutTrajet.COMPLET) {
            throw new BusinessRuleException("trajet est complet ");
        }

        if (trajet.getStatut() != StatutTrajet.PUBLIE) {
            throw new BusinessRuleException("le trajet est " + trajet.getStatut() );
        }
    }

    private int calculateEmptyPlaces(Trajet trajet) {
        return trajet.getPlacesProposees() - reservationRepository.placesOccupees(trajet.getId());
    }


    private BigDecimal calculerMontantTotal(PointArret montee, PointArret descente, int nombrePlaces) {
        return descente.getPrixDepuisDepart()
                .subtract(montee.getPrixDepuisDepart())
                .multiply(BigDecimal.valueOf(nombrePlaces))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
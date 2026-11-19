package com.covoiturage.covoiturage.Service;

import com.covoiturage.covoiturage.dto.Reservation.ReservationCreeeResponse;
import com.covoiturage.covoiturage.dto.Reservation.ReservationResponse;
import com.covoiturage.covoiturage.dto.Reservation.ReserverRequest;
import com.covoiturage.covoiturage.exception.BusinessRuleException;
import com.covoiturage.covoiturage.exception.ForbiddenOperationException;
import com.covoiturage.covoiturage.exception.InvalidRequestException;
import com.covoiturage.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage.mapper.Reservation.ReservationMapper;
import com.covoiturage.covoiturage.model.Conducteur;
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
import com.covoiturage.covoiturage.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private PassagerRepository passagerRepository;
    @Mock
    private ConducteurRepository conducteurRepository;
    @Mock
    private TrajetRepository trajetRepository;
    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    // Horloge figee : maintenant vaut toujours la meme valeur
    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 19, 16, 41);
    // Depart loin dans le futur : l'annulation est permise
    private static final LocalDateTime DEPART_LOINTAIN = LocalDateTime.of(2026, 11, 25, 17, 0);

    @BeforeEach
    void init() {
        Clock clock = Clock.fixed(MAINTENANT.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        // Construction a la main : l'ordre suit celui des champs du service
        reservationService = new ReservationServiceImpl(
                reservationRepository, passagerRepository, conducteurRepository,
                trajetRepository, reservationMapper, clock);
    }

    // ---------- methodes utilitaires (pas des tests) ----------

    // Fabrique un faux point d'arret
    private PointArret point(Long id, int ordre, String ville, String prix) {
        PointArret p = mock(PointArret.class);
        lenient().when(p.getId()).thenReturn(id);
        lenient().when(p.getOrdre()).thenReturn(ordre);
        lenient().when(p.getVille()).thenReturn(ville);
        lenient().when(p.getPrixDepuisDepart()).thenReturn(new java.math.BigDecimal(prix));
        return p;
    }

    // Fabrique un trajet avec le parcours Rabat, Casablanca, Settat, Marrakech (identifiants cent a cent trois)
    private Trajet trajet(StatutTrajet statut, LocalDateTime dateDepart) {
        // Les points sont crees AVANT de commencer le stubbing du trajet :
        // creer un mock (et le stubber) au milieu d'un when(...).thenReturn(...) casse Mockito
        List<PointArret> points = List.of(
                point(100L, 0, "Rabat", "0"),
                point(101L, 1, "Casablanca", "40"),
                point(102L, 2, "Settat", "70"),
                point(103L, 3, "Marrakech", "120"));

        Trajet t = mock(Trajet.class);
        lenient().when(t.getId()).thenReturn(7L);
        lenient().when(t.getStatut()).thenReturn(statut);
        lenient().when(t.getPlacesProposees()).thenReturn(3);
        lenient().when(t.getDateDepart()).thenReturn(dateDepart);
        lenient().when(t.getPointsArret()).thenReturn(points);
        return t;
    }

    // Fabrique une fausse requete de reservation
    private ReserverRequest requete(Long montee, Long descente, int places) {
        ReserverRequest r = mock(ReserverRequest.class);
        lenient().when(r.trajetId()).thenReturn(7L);
        lenient().when(r.pointMonteeId()).thenReturn(montee);
        lenient().when(r.pointDescenteId()).thenReturn(descente);
        lenient().when(r.nombrePlaces()).thenReturn(places);
        return r;
    }

    // Prepare le passager un, le trajet sept et le nombre de places deja occupees
    private Trajet preparerReservation(StatutTrajet statut, int placesOccupees) {
        Passager passager = mock(Passager.class);
        Trajet trajet = trajet(statut, DEPART_LOINTAIN);
        lenient().when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        lenient().when(trajetRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(trajet));
        lenient().when(reservationRepository.placesOccupees(7L)).thenReturn(placesOccupees);
        return trajet;
    }

    // Fabrique une fausse reservation appartenant au passager donne
    private Reservation reservation(Long passagerId, StatutReservation statut, Trajet trajet) {
        Passager proprietaire = mock(Passager.class);
        lenient().when(proprietaire.getId()).thenReturn(passagerId);
        Reservation r = mock(Reservation.class);
        lenient().when(r.getPassager()).thenReturn(proprietaire);
        lenient().when(r.getStatut()).thenReturn(statut);
        lenient().when(r.getTrajet()).thenReturn(trajet);
        return r;
    }

    // ---------- reserver : cas nominaux ----------

    @Test
    @DisplayName("reserver : une place sur une partie du trajet")
    void reserver_cas_nominal_reste_publie() {
        // Principe : Salma reserve une place de Settat a Marrakech ; le montant est la difference des prix, soit cinquante,
        // il reste deux places donc le trajet reste PUBLIE et n'est pas enregistre de nouveau
        Trajet trajet = preparerReservation(StatutTrajet.PUBLIE, 0);
        ReserverRequest request = requete(102L, 103L, 1);
        ReservationResponse reponse = mock(ReservationResponse.class);

        when(reservationRepository.saveAndFlush(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservationMapper.toResponse(any(Reservation.class))).thenReturn(reponse);

        ReservationCreeeResponse resultat = reservationService.reserver(1L, request);

        // Contenu de la reservation enregistree
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).saveAndFlush(captor.capture());
        Reservation enregistree = captor.getValue();
        assertThat(enregistree.getMontantTotal()).isEqualByComparingTo("50.00");
        assertThat(enregistree.getNombrePlaces()).isEqualTo(1);
        assertThat(enregistree.getStatut()).isEqualTo(StatutReservation.CONFIRMEE);
        assertThat(enregistree.getDateReservation()).isEqualTo(MAINTENANT);

        // Reponse : places restantes et statut du trajet
        assertThat(resultat).isEqualTo(new ReservationCreeeResponse(reponse, 2, StatutTrajet.PUBLIE));
        verify(trajetRepository, never()).save(any());
    }

    @Test
    @DisplayName("reserver : la derniere place rend le trajet complet")
    void reserver_derniere_place_trajet_complet() {
        // Principe : une place est deja prise, Karim reserve deux places de Casablanca a Marrakech ;
        // le montant vaut quatre vingt fois deux, soit cent soixante ; il ne reste plus de place donc le trajet passe a COMPLET
        Trajet trajet = preparerReservation(StatutTrajet.PUBLIE, 1);
        ReserverRequest request = requete(101L, 103L, 2);
        ReservationResponse reponse = mock(ReservationResponse.class);

        when(reservationRepository.saveAndFlush(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservationMapper.toResponse(any(Reservation.class))).thenReturn(reponse);

        ReservationCreeeResponse resultat = reservationService.reserver(1L, request);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getMontantTotal()).isEqualByComparingTo("160.00");

        assertThat(resultat).isNotNull();
        verify(trajet).setStatut(StatutTrajet.COMPLET);
        verify(trajetRepository).save(trajet);
    }

    // ---------- reserver : cas d'erreur ----------

    @Test
    @DisplayName("reserver : passager inexistant")
    void reserver_passager_introuvable() {
        // Principe : le passager n'existe pas, donc erreur ressource introuvable et aucune reservation
        when(passagerRepository.findById(99L)).thenReturn(Optional.empty());
        ReserverRequest request = requete(102L, 103L, 1);

        assertThatThrownBy(() -> reservationService.reserver(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("reserver : trajet inexistant")
    void reserver_trajet_introuvable() {
        // Principe : le passager existe mais le trajet demande n'existe pas
        when(passagerRepository.findById(1L)).thenReturn(Optional.of(mock(Passager.class)));
        when(trajetRepository.findByIdForUpdate(7L)).thenReturn(Optional.empty());
        ReserverRequest request = requete(102L, 103L, 1);

        assertThatThrownBy(() -> reservationService.reserver(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("reserver : point qui n'appartient pas au trajet")
    void reserver_point_inconnu() {
        // Principe : l'identifiant de montee ne correspond a aucun point du trajet, donc requete invalide
        preparerReservation(StatutTrajet.PUBLIE, 0);
        ReserverRequest request = requete(999L, 103L, 1);

        assertThatThrownBy(() -> reservationService.reserver(1L, request))
                .isInstanceOf(InvalidRequestException.class);

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("reserver : montee apres la descente")
    void reserver_montee_apres_descente() {
        // Principe : monter a Marrakech et descendre a Settat va dans le mauvais sens, donc requete invalide
        preparerReservation(StatutTrajet.PUBLIE, 0);
        ReserverRequest request = requete(103L, 102L, 1);

        assertThatThrownBy(() -> reservationService.reserver(1L, request))
                .isInstanceOf(InvalidRequestException.class);

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("reserver : trajet complet")
    void reserver_trajet_complet() {
        // Principe : on ne reserve pas sur un trajet COMPLET, regle metier violee
        preparerReservation(StatutTrajet.COMPLET, 3);
        ReserverRequest request = requete(102L, 103L, 1);

        assertThatThrownBy(() -> reservationService.reserver(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("complet");

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("reserver : trajet termine")
    void reserver_trajet_termine() {
        // Principe : seul un trajet PUBLIE est ouvert a la reservation ; un trajet TERMINE est refuse
        preparerReservation(StatutTrajet.TERMINE, 0);
        ReserverRequest request = requete(102L, 103L, 1);

        assertThatThrownBy(() -> reservationService.reserver(1L, request))
                .isInstanceOf(BusinessRuleException.class);

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("reserver : une seule reservation confirmee par trajet")
    void reserver_deja_une_reservation_confirmee() {
        // Principe : le passager a deja une reservation CONFIRMEE sur ce trajet, donc refus
        preparerReservation(StatutTrajet.PUBLIE, 0);
        when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(1L, 7L, StatutReservation.CONFIRMEE))
                .thenReturn(true);
        ReserverRequest request = requete(102L, 103L, 1);

        assertThatThrownBy(() -> reservationService.reserver(1L, request))
                .isInstanceOf(BusinessRuleException.class);

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("reserver : pas assez de places restantes")
    void reserver_pas_assez_de_places() {
        // Principe : le trajet propose trois places, deux sont occupees, il en reste une, mais le passager en demande deux
        preparerReservation(StatutTrajet.PUBLIE, 2);
        ReserverRequest request = requete(102L, 103L, 2);

        assertThatThrownBy(() -> reservationService.reserver(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("place");

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    // ---------- annuler ----------

    @Test
    @DisplayName("annuler : cas nominal")
    void annuler_cas_nominal() {
        // Principe : le depart est dans plus de vingt quatre heures et la reservation appartient au passager ;
        // elle passe a ANNULEE et le trajet n'etant pas complet n'est pas modifie
        Trajet trajet = trajet(StatutTrajet.PUBLIE, DEPART_LOINTAIN);
        Reservation reservation = reservation(1L, StatutReservation.CONFIRMEE, trajet);

        when(passagerRepository.existsById(1L)).thenReturn(true);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));
        when(trajetRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(trajet));

        reservationService.annuler(1L, 50L);

        verify(reservation).setStatut(StatutReservation.ANNULEE);
        verify(reservationRepository).saveAndFlush(reservation);
        verify(trajetRepository, never()).save(any());
    }

    @Test
    @DisplayName("annuler : une place libere rend le trajet de nouveau publie")
    void annuler_trajet_complet_redevient_publie() {
        // Principe : le trajet est COMPLET ; apres l'annulation il reste une place libre (deux occupees sur trois),
        // donc le trajet repasse a PUBLIE et est enregistre
        Trajet trajet = trajet(StatutTrajet.COMPLET, DEPART_LOINTAIN);
        Reservation reservation = reservation(1L, StatutReservation.CONFIRMEE, trajet);

        when(passagerRepository.existsById(1L)).thenReturn(true);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));
        when(trajetRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(trajet));
        when(reservationRepository.placesOccupees(7L)).thenReturn(2);

        reservationService.annuler(1L, 50L);

        verify(trajet).setStatut(StatutTrajet.PUBLIE);
        verify(trajetRepository).save(trajet);
    }

    @Test
    @DisplayName("annuler : passager inexistant")
    void annuler_passager_introuvable() {
        // Principe : le passager n'existe pas, donc erreur ressource introuvable
        when(passagerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> reservationService.annuler(99L, 50L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("annuler : reservation inexistante")
    void annuler_reservation_introuvable() {
        // Principe : le passager existe mais la reservation demandee n'existe pas
        when(passagerRepository.existsById(1L)).thenReturn(true);
        when(reservationRepository.findById(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.annuler(1L, 50L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("50");
    }

    @Test
    @DisplayName("annuler : reservation d'un autre passager")
    void annuler_reservation_dun_autre_passager() {
        // Principe : la reservation appartient au passager deux, mais le passager un essaie de l'annuler : operation interdite
        Trajet trajet = trajet(StatutTrajet.PUBLIE, DEPART_LOINTAIN);
        Reservation reservation = reservation(2L, StatutReservation.CONFIRMEE, trajet);

        when(passagerRepository.existsById(1L)).thenReturn(true);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.annuler(1L, 50L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("annuler : trajet de la reservation introuvable")
    void annuler_trajet_introuvable() {
        // Principe : la reservation existe mais son trajet est introuvable au moment du verrouillage
        Trajet trajet = trajet(StatutTrajet.PUBLIE, DEPART_LOINTAIN);
        Reservation reservation = reservation(1L, StatutReservation.CONFIRMEE, trajet);

        when(passagerRepository.existsById(1L)).thenReturn(true);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));
        when(trajetRepository.findByIdForUpdate(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.annuler(1L, 50L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("annuler : reservation deja annulee")
    void annuler_reservation_deja_annulee() {
        // Principe : on ne peut pas annuler deux fois la meme reservation
        Trajet trajet = trajet(StatutTrajet.PUBLIE, DEPART_LOINTAIN);
        Reservation reservation = reservation(1L, StatutReservation.ANNULEE, trajet);

        when(passagerRepository.existsById(1L)).thenReturn(true);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));
        when(trajetRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(trajet));

        assertThatThrownBy(() -> reservationService.annuler(1L, 50L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("annulee");

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("annuler : delai de vingt quatre heures depasse")
    void annuler_trop_tard() {
        // Principe : le depart est dans deux heures seulement, le delai d'annulation est depasse, regle metier violee
        Trajet trajet = trajet(StatutTrajet.PUBLIE, MAINTENANT.plusHours(2));
        Reservation reservation = reservation(1L, StatutReservation.CONFIRMEE, trajet);

        when(passagerRepository.existsById(1L)).thenReturn(true);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));
        when(trajetRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(trajet));

        assertThatThrownBy(() -> reservationService.annuler(1L, 50L))
                .isInstanceOf(BusinessRuleException.class);

        verify(reservation, never()).setStatut(any());
        verify(reservationRepository, never()).saveAndFlush(any());
    }

    // ---------- getReservationsOfPassager ----------

    @Test
    @DisplayName("historique passager : cas nominal")
    void historique_passager_cas_nominal() {
        // Principe : le passager existe, on renvoie son historique transforme en reponses
        Reservation r1 = mock(Reservation.class);
        Reservation r2 = mock(Reservation.class);
        ReservationResponse rep1 = mock(ReservationResponse.class);
        ReservationResponse rep2 = mock(ReservationResponse.class);

        when(passagerRepository.existsById(1L)).thenReturn(true);
        when(reservationRepository.findHistoriqueParPassager(1L)).thenReturn(List.of(r1, r2));
        when(reservationMapper.toResponse(r1)).thenReturn(rep1);
        when(reservationMapper.toResponse(r2)).thenReturn(rep2);

        assertThat(reservationService.getReservationsOfPassager(1L)).containsExactly(rep1, rep2);
    }

    @Test
    @DisplayName("historique passager : passager inexistant")
    void historique_passager_introuvable() {
        // Principe : si le passager n'existe pas, erreur au lieu d'une liste vide
        when(passagerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> reservationService.getReservationsOfPassager(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(reservationRepository, never()).findHistoriqueParPassager(any());
    }

    // ---------- getReservationsTrajet ----------

    @Test
    @DisplayName("reservations d'un trajet : cas nominal")
    void reservations_trajet_cas_nominal() {
        // Principe : le conducteur est le proprietaire du trajet, il voit les reservations de ce trajet
        Conducteur conducteur = mock(Conducteur.class);
        when(conducteur.getId()).thenReturn(1L);
        Trajet trajet = mock(Trajet.class);
        when(trajet.getConducteur()).thenReturn(conducteur);
        Reservation r1 = mock(Reservation.class);
        ReservationResponse rep1 = mock(ReservationResponse.class);

        when(conducteurRepository.existsById(1L)).thenReturn(true);
        when(trajetRepository.findById(7L)).thenReturn(Optional.of(trajet));
        when(reservationRepository.findParTrajet(7L)).thenReturn(List.of(r1));
        when(reservationMapper.toResponse(r1)).thenReturn(rep1);

        assertThat(reservationService.getReservationsTrajet(1L, 7L)).containsExactly(rep1);
    }

    @Test
    @DisplayName("reservations d'un trajet : conducteur inexistant")
    void reservations_trajet_conducteur_introuvable() {
        // Principe : le conducteur n'existe pas, donc erreur ressource introuvable
        when(conducteurRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> reservationService.getReservationsTrajet(99L, 7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("reservations d'un trajet : trajet inexistant")
    void reservations_trajet_introuvable() {
        // Principe : le conducteur existe mais le trajet n'existe pas
        when(conducteurRepository.existsById(1L)).thenReturn(true);
        when(trajetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getReservationsTrajet(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("reservations d'un trajet : trajet d'un autre conducteur")
    void reservations_trajet_dun_autre_conducteur() {
        // Principe : le trajet appartient au conducteur deux, mais le conducteur un demande ses reservations : operation interdite
        Conducteur proprietaire = mock(Conducteur.class);
        when(proprietaire.getId()).thenReturn(2L);
        Trajet trajet = mock(Trajet.class);
        when(trajet.getConducteur()).thenReturn(proprietaire);

        when(conducteurRepository.existsById(1L)).thenReturn(true);
        when(trajetRepository.findById(7L)).thenReturn(Optional.of(trajet));

        assertThatThrownBy(() -> reservationService.getReservationsTrajet(1L, 7L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(reservationRepository, never()).findParTrajet(any());
    }
}
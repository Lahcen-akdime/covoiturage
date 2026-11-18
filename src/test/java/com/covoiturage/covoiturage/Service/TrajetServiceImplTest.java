package com.covoiturage.covoiturage.Service;

import com.covoiturage.covoiturage.dto.Trajet.PublierTrajetRequest;
import com.covoiturage.covoiturage.dto.Trajet.TrajetDetailResponse;
import com.covoiturage.covoiturage.dto.pointArret.PointArretRequest;
import com.covoiturage.covoiturage.exception.ForbiddenOperationException;
import com.covoiturage.covoiturage.exception.InvalidRequestException;
import com.covoiturage.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage.mapper.Trajet.TrajetMapper;
import com.covoiturage.covoiturage.model.Conducteur;
import com.covoiturage.covoiturage.model.Enum.StatutTrajet;
import com.covoiturage.covoiturage.model.Trajet;
import com.covoiturage.covoiturage.model.Vehicule;
import com.covoiturage.covoiturage.repository.ConducteurRepository;
import com.covoiturage.covoiturage.repository.TrajetRepository;
import com.covoiturage.covoiturage.repository.VehiculeRepository;
import com.covoiturage.covoiturage.service.impl.TrajetServiceImpl;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrajetServiceImplTest {

    @Mock
    private TrajetRepository trajetRepository;
    @Mock
    private ConducteurRepository conducteurRepository;
    @Mock
    private VehiculeRepository vehiculeRepository;
    @Mock
    private TrajetMapper trajetMapper;


    @InjectMocks
    private TrajetServiceImpl trajetService;

    // Horloge figee : "maintenant" vaut toujours la meme valeur, donc les tests ne dependent pas de la date reelle
    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 18, 10, 0);
    private static final LocalDateTime DATE_DEPART = LocalDateTime.of(2026, 11, 20, 17, 0);

    @BeforeEach
    void init() {
        Clock clock = Clock.fixed(MAINTENANT.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        // Construction a la main : l'ordre des arguments suit celui des champs du service
        trajetService = new TrajetServiceImpl(
                trajetRepository, conducteurRepository, vehiculeRepository, trajetMapper, clock);
    }

    // ---------- methodes utilitaires (pas des tests) ----------

    // Fabrique un faux point d'arret avec les trois valeurs utilisees par le service
    private PointArretRequest point(String ville, LocalTime heure, String prix) {
        PointArretRequest p = mock(PointArretRequest.class);
        lenient().when(p.ville()).thenReturn(ville);
        lenient().when(p.heurePassage()).thenReturn(heure);
        lenient().when(p.prixDepuisDepart()).thenReturn(new BigDecimal(prix));
        return p;
    }

    // Fabrique une fausse requete de publication
    private PublierTrajetRequest requete(int places, LocalDateTime dateDepart, List<PointArretRequest> points) {
        PublierTrajetRequest r = mock(PublierTrajetRequest.class);
        lenient().when(r.vehiculeId()).thenReturn(10L);
        lenient().when(r.placesProposees()).thenReturn(places);
        lenient().when(r.dateDepart()).thenReturn(dateDepart);
        lenient().when(r.pointsArret()).thenReturn(points);
        return r;
    }

    // Prepare un conducteur (id un) qui possede un vehicule de capacite quatre
    private void preparerConducteurEtVehicule() {
        Conducteur conducteur = mock(Conducteur.class);
        lenient().when(conducteur.getId()).thenReturn(1L);

        Vehicule vehicule = mock(Vehicule.class);
        lenient().when(vehicule.getConducteur()).thenReturn(conducteur);
        lenient().when(vehicule.getCapacite()).thenReturn(4);

        lenient().when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        lenient().when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(vehicule));
    }

    // Parcours valide : trois points, heures croissantes, prix croissants, premier prix a zero
    private List<PointArretRequest> parcoursValide() {
        return List.of(
                point("Rabat", LocalTime.of(17, 0), "0"),
                point("Casablanca", LocalTime.of(18, 15), "40"),
                point("Marrakech", LocalTime.of(21, 30), "120"));
    }

    // ---------- publier : cas d'erreur ----------

    @Test
    @DisplayName("publier : conducteur inexistant")
    void publier_conducteur_introuvable() {
        // Principe : si le conducteur n'existe pas en base, on s'arrete tout de suite avec une erreur de type ressource introuvable
        when(conducteurRepository.findById(1L)).thenReturn(Optional.empty());
        PublierTrajetRequest request = requete(3, DATE_DEPART, parcoursValide());

        assertThatThrownBy(() -> trajetService.publier(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Conducteur");

        // Rien ne doit etre enregistre
        verify(trajetRepository, never()).save(any());
    }

    @Test
    @DisplayName("publier : vehicule inexistant")
    void publier_vehicule_introuvable() {
        // Principe : le conducteur existe mais le vehicule demande n'existe pas, donc erreur de type ressource introuvable
        Conducteur conducteur = mock(Conducteur.class);
        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.empty());
        PublierTrajetRequest request = requete(3, DATE_DEPART, parcoursValide());

        assertThatThrownBy(() -> trajetService.publier(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("hicule");

        verify(trajetRepository, never()).save(any());
    }

    @Test
    @DisplayName("publier : vehicule d'un autre conducteur")
    void publier_vehicule_dun_autre_conducteur() {
        // Principe : le vehicule appartient au conducteur deux, mais c'est le conducteur un qui publie : operation interdite, que le handler d'erreurs traduira en refus d'acces
        Conducteur demandeur = mock(Conducteur.class);
        Conducteur proprietaire = mock(Conducteur.class);
        when(proprietaire.getId()).thenReturn(2L);
        Vehicule vehicule = mock(Vehicule.class);
        when(vehicule.getConducteur()).thenReturn(proprietaire);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(demandeur));
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(vehicule));
        PublierTrajetRequest request = requete(3, DATE_DEPART, parcoursValide());

        assertThatThrownBy(() -> trajetService.publier(1L, request))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(trajetRepository, never()).save(any());
    }

    @Test
    @DisplayName("publier : trop de places proposees")
    void publier_places_superieures_a_la_capacite() {
        // Principe : le vehicule accepte quatre places au maximum, le conducteur en propose cinq, donc la demande est invalide
        preparerConducteurEtVehicule();
        PublierTrajetRequest request = requete(5, DATE_DEPART, parcoursValide());

        assertThatThrownBy(() -> trajetService.publier(1L, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("maximum");

        verify(trajetRepository, never()).save(any());
    }

    @Test
    @DisplayName("publier : date de depart dans le passe")
    void publier_date_depart_dans_le_passe() {
        // Principe : la date de depart est avant l'horloge figee, donc refus
        preparerConducteurEtVehicule();
        LocalDateTime hier = MAINTENANT.minusDays(1);
        PublierTrajetRequest request = requete(3, hier, parcoursValide());

        assertThatThrownBy(() -> trajetService.publier(1L, request))
                .isInstanceOf(InvalidRequestException.class);

        verify(trajetRepository, never()).save(any());
    }

    @Test
    @DisplayName("publier : le premier prix doit valoir zero")
    void publier_premier_prix_non_nul() {
        // Principe : au depart le prix depuis le depart doit etre egal a zero ; ici le premier point coute quinze
        preparerConducteurEtVehicule();
        List<PointArretRequest> points = List.of(
                point("Rabat", LocalTime.of(17, 0), "15"),
                point("Marrakech", LocalTime.of(21, 30), "120"));
        PublierTrajetRequest request = requete(3, DATE_DEPART, points);

        assertThatThrownBy(() -> trajetService.publier(1L, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("0");

        verify(trajetRepository, never()).save(any());
    }

    @Test
    @DisplayName("publier : l'heure du premier point doit etre l'heure de depart")
    void publier_heure_premier_point_differente_du_depart() {
        // Principe : le trajet part a dix sept heures mais le premier point annonce dix huit heures, donc incoherent
        preparerConducteurEtVehicule();
        List<PointArretRequest> points = List.of(
                point("Rabat", LocalTime.of(18, 0), "0"),
                point("Marrakech", LocalTime.of(21, 30), "120"));
        PublierTrajetRequest request = requete(3, DATE_DEPART, points);

        assertThatThrownBy(() -> trajetService.publier(1L, request))
                .isInstanceOf(InvalidRequestException.class);

        verify(trajetRepository, never()).save(any());
    }

    @Test
    @DisplayName("publier : les heures doivent croitre")
    void publier_heures_non_croissantes() {
        // Principe : le deuxieme point passe avant le premier, le parcours est impossible dans le temps
        preparerConducteurEtVehicule();
        List<PointArretRequest> points = List.of(
                point("Rabat", LocalTime.of(17, 0), "0"),
                point("Marrakech", LocalTime.of(16, 0), "120"));
        PublierTrajetRequest request = requete(3, DATE_DEPART, points);

        assertThatThrownBy(() -> trajetService.publier(1L, request))
                .isInstanceOf(InvalidRequestException.class);

        verify(trajetRepository, never()).save(any());
    }

    @Test
    @DisplayName("publier : une ville ne peut apparaitre qu'une fois")
    void publier_ville_en_double() {
        // Principe : la ville Rabat apparait au debut et a la fin, avec une casse differente ; la comparaison doit l'ignorer
        preparerConducteurEtVehicule();
        List<PointArretRequest> points = List.of(
                point("Rabat", LocalTime.of(17, 0), "0"),
                point("Casablanca", LocalTime.of(18, 15), "40"),
                point("rabat", LocalTime.of(21, 30), "120"));
        PublierTrajetRequest request = requete(3, DATE_DEPART, points);

        assertThatThrownBy(() -> trajetService.publier(1L, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("plusieurs fois");

        verify(trajetRepository, never()).save(any());
    }

    // ---------- publier : cas nominal ----------


    @Test
    @DisplayName("publier : cas normal")
    void publier_cas_normal() {
        preparerConducteurEtVehicule();

        PublierTrajetRequest request = requete(3, DATE_DEPART, parcoursValide());
        TrajetDetailResponse reponse = mock(TrajetDetailResponse.class);

        when(trajetRepository.save(any(Trajet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(trajetMapper.toDetail(any(Trajet.class), anyInt()))
                .thenReturn(reponse);

        TrajetDetailResponse resultat = trajetService.publier(1L, request);

        assertThat(resultat).isSameAs(reponse);

        verify(trajetRepository).save(argThat(trajet ->
                trajet.getStatut() == StatutTrajet.PUBLIE
                        && trajet.getVilleDepart().equals("Rabat")
                        && trajet.getVilleArrivee().equals("Marrakech")
                        && trajet.getPointsArret().size() == 3
        ));
    }


    // ---------- consulterDetail ----------

    @Test
    @DisplayName("consulterDetail : trajet existant")
    void consulterDetail_trouve() {
        // Principe : le repository renvoie le trajet, le service le transforme en reponse detaillee grace au mapper
        Trajet trajet = mock(Trajet.class);
        when(trajet.getPlacesProposees()).thenReturn(3);
        TrajetDetailResponse reponse = mock(TrajetDetailResponse.class);

        when(trajetRepository.findDetailById(5L)).thenReturn(Optional.of(trajet));
        when(trajetMapper.toDetail(any(Trajet.class), anyInt())).thenReturn(reponse);

        assertThat(trajetService.consulterDetail(5L)).isSameAs(reponse);
    }

    @Test
    @DisplayName("consulterDetail : trajet inexistant")
    void consulterDetail_introuvable() {
        // Principe : aucun trajet pour cet identifiant, donc erreur de type ressource introuvable
        when(trajetRepository.findDetailById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trajetService.consulterDetail(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
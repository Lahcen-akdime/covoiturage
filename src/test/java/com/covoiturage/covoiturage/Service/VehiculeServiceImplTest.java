package com.covoiturage.covoiturage.Service;

import com.covoiturage.covoiturage.dto.Vehicule.VehiculeRequest;
import com.covoiturage.covoiturage.dto.Vehicule.VehiculeResponse;
import com.covoiturage.covoiturage.exception.BusinessRuleException;
import com.covoiturage.covoiturage.exception.ForbiddenOperationException;
import com.covoiturage.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage.mapper.Vehicule.VehiculeMapper;
import com.covoiturage.covoiturage.model.Conducteur;
import com.covoiturage.covoiturage.model.Vehicule;
import com.covoiturage.covoiturage.repository.ConducteurRepository;
import com.covoiturage.covoiturage.repository.TrajetRepository;
import com.covoiturage.covoiturage.repository.VehiculeRepository;
import com.covoiturage.covoiturage.service.impl.VehiculeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiculeServiceImplTest {

    @Mock
    private VehiculeRepository vehiculeRepository;
    @Mock
    private ConducteurRepository conducteurRepository;
    @Mock
    private TrajetRepository trajetRepository;
    @Mock
    private VehiculeMapper vehiculeMapper;

    @InjectMocks
    private VehiculeServiceImpl vehiculeService;

    // ---------- methode utilitaire (pas un test) ----------

    // Fabrique une fausse requete qui renvoie le matricule donne
    private VehiculeRequest requete(String matricule) {
        VehiculeRequest request = mock(VehiculeRequest.class);
        when(request.matricule()).thenReturn(matricule);
        return request;
    }

    // ---------- save ----------

    @Test
    @DisplayName("save : cas nominal")
    void save_cas_nominal() {
        // Principe : le conducteur existe et le matricule est libre ; le matricule est nettoye (espaces retires, majuscules),
        // le vehicule est rattache au conducteur puis enregistre
        Conducteur conducteur = mock(Conducteur.class);
        Vehicule entite = mock(Vehicule.class);
        Vehicule sauvegarde = mock(Vehicule.class);
        VehiculeResponse reponse = mock(VehiculeResponse.class);
        VehiculeRequest request = requete("  ab-123-c ");

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.existsByMatriculeIgnoreCase("AB-123-C")).thenReturn(false);
        when(vehiculeMapper.toEntity(request)).thenReturn(entite);
        when(vehiculeRepository.save(entite)).thenReturn(sauvegarde);
        when(vehiculeMapper.toResponse(sauvegarde)).thenReturn(reponse);

        VehiculeResponse resultat = vehiculeService.save(1L, request);

        assertThat(resultat).isSameAs(reponse);
        verify(entite).setMatricule("AB-123-C");
        verify(entite).setConducteur(conducteur);
        verify(vehiculeRepository).save(entite);
    }

    @Test
    @DisplayName("save : conducteur inexistant")
    void save_conducteur_introuvable() {
        // Principe : on ne peut pas ajouter un vehicule a un conducteur qui n'existe pas
        VehiculeRequest request = mock(VehiculeRequest.class);
        when(conducteurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehiculeService.save(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(vehiculeRepository, never()).save(any());
    }

    @Test
    @DisplayName("save : matricule deja utilise")
    void save_matricule_existant() {
        // Principe : le matricule doit etre unique ; s'il existe deja, regle metier violee et rien n'est enregistre
        Conducteur conducteur = mock(Conducteur.class);
        VehiculeRequest request = requete("AB-123-C");

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.existsByMatriculeIgnoreCase("AB-123-C")).thenReturn(true);

        assertThatThrownBy(() -> vehiculeService.save(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("AB-123-C");

        verify(vehiculeRepository, never()).save(any());
    }

    // ---------- findById ----------

    @Test
    @DisplayName("findById : vehicule existant")
    void findById_trouve() {
        // Principe : le repository renvoie le vehicule et le mapper le transforme en reponse
        Vehicule vehicule = mock(Vehicule.class);
        VehiculeResponse reponse = mock(VehiculeResponse.class);

        when(vehiculeRepository.findById(5L)).thenReturn(Optional.of(vehicule));
        when(vehiculeMapper.toResponse(vehicule)).thenReturn(reponse);

        assertThat(vehiculeService.findById(5L)).isSameAs(reponse);
    }

    @Test
    @DisplayName("findById : vehicule inexistant")
    void findById_introuvable() {
        // Principe : aucun vehicule pour cet identifiant, donc erreur de type ressource introuvable
        when(vehiculeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehiculeService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------- findAllByConducteurId ----------

    @Test
    @DisplayName("findAllByConducteurId : liste des vehicules du conducteur")
    void findAllByConducteurId_cas_nominal() {
        // Principe : le conducteur existe, on renvoie tous ses vehicules transformes en reponses
        Conducteur conducteur = mock(Conducteur.class);
        Vehicule v1 = mock(Vehicule.class);
        Vehicule v2 = mock(Vehicule.class);
        VehiculeResponse r1 = mock(VehiculeResponse.class);
        VehiculeResponse r2 = mock(VehiculeResponse.class);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findByConducteurId(1L)).thenReturn(List.of(v1, v2));
        when(vehiculeMapper.toResponse(v1)).thenReturn(r1);
        when(vehiculeMapper.toResponse(v2)).thenReturn(r2);

        assertThat(vehiculeService.findAllByConducteurId(1L)).containsExactly(r1, r2);
    }

    @Test
    @DisplayName("findAllByConducteurId : conducteur inexistant")
    void findAllByConducteurId_conducteur_introuvable() {
        // Principe : si le conducteur n'existe pas, on leve une erreur au lieu de renvoyer une liste vide
        when(conducteurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehiculeService.findAllByConducteurId(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(vehiculeRepository, never()).findByConducteurId(any());
    }

    // ---------- update ----------

    @Test
    @DisplayName("update : cas nominal")
    void update_cas_nominal() {
        // Principe : le vehicule existe et le nouveau matricule n'est pris par aucun autre vehicule ;
        // les champs sont copies par le mapper puis le matricule nettoye est applique
        Vehicule vehicule = mock(Vehicule.class);
        Vehicule sauvegarde = mock(Vehicule.class);
        VehiculeResponse reponse = mock(VehiculeResponse.class);
        VehiculeRequest request = requete(" cd-456-e ");

        when(vehiculeRepository.findById(5L)).thenReturn(Optional.of(vehicule));
        when(vehiculeRepository.existsByMatriculeIgnoreCaseAndIdNot("CD-456-E", 5L)).thenReturn(false);
        when(vehiculeRepository.save(vehicule)).thenReturn(sauvegarde);
        when(vehiculeMapper.toResponse(sauvegarde)).thenReturn(reponse);

        VehiculeResponse resultat = vehiculeService.update(5L, request);

        assertThat(resultat).isSameAs(reponse);
        verify(vehiculeMapper).updateEntity(request, vehicule);
        verify(vehicule).setMatricule("CD-456-E");
    }

    @Test
    @DisplayName("update : vehicule inexistant")
    void update_vehicule_introuvable() {
        // Principe : on ne peut pas modifier un vehicule qui n'existe pas
        VehiculeRequest request = mock(VehiculeRequest.class);
        when(vehiculeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehiculeService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(vehiculeRepository, never()).save(any());
    }

    @Test
    @DisplayName("update : matricule pris par un autre vehicule")
    void update_matricule_pris() {
        // Principe : le nouveau matricule appartient deja a un autre vehicule (identifiant different), donc refus
        Vehicule vehicule = mock(Vehicule.class);
        VehiculeRequest request = requete("CD-456-E");

        when(vehiculeRepository.findById(5L)).thenReturn(Optional.of(vehicule));
        when(vehiculeRepository.existsByMatriculeIgnoreCaseAndIdNot("CD-456-E", 5L)).thenReturn(true);

        assertThatThrownBy(() -> vehiculeService.update(5L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CD-456-E");

        verify(vehiculeRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    @DisplayName("delete : cas nominal")
    void delete_cas_nominal() {
        // Principe : le vehicule appartient au conducteur et n'est lie a aucun trajet, donc il est supprime
        Conducteur conducteur = mock(Conducteur.class);
        when(conducteur.getId()).thenReturn(1L);
        Vehicule vehicule = mock(Vehicule.class);
        when(vehicule.getConducteur()).thenReturn(conducteur);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(5L)).thenReturn(Optional.of(vehicule));
        when(trajetRepository.existsByVehiculeId(5L)).thenReturn(false);

        vehiculeService.delete(1L, 5L);

        verify(vehiculeRepository).delete(vehicule);
    }

    @Test
    @DisplayName("delete : conducteur inexistant")
    void delete_conducteur_introuvable() {
        // Principe : le conducteur est verifie en premier ; s'il n'existe pas, on s'arrete
        when(conducteurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehiculeService.delete(99L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(vehiculeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete : vehicule inexistant")
    void delete_vehicule_introuvable() {
        // Principe : le conducteur existe mais le vehicule demande n'existe pas
        Conducteur conducteur = mock(Conducteur.class);
        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehiculeService.delete(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(vehiculeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete : vehicule d'un autre conducteur")
    void delete_vehicule_dun_autre_conducteur() {
        // Principe : le vehicule appartient au conducteur deux, mais c'est le conducteur un qui demande la suppression : operation interdite
        Conducteur demandeur = mock(Conducteur.class);
        Conducteur proprietaire = mock(Conducteur.class);
        when(proprietaire.getId()).thenReturn(2L);
        Vehicule vehicule = mock(Vehicule.class);
        when(vehicule.getConducteur()).thenReturn(proprietaire);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(demandeur));
        when(vehiculeRepository.findById(5L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> vehiculeService.delete(1L, 5L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(vehiculeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete : vehicule associe a un trajet")
    void delete_vehicule_associe_a_un_trajet() {
        // Principe : un vehicule utilise par au moins un trajet ne peut pas etre supprime (regle metier violee)
        Conducteur conducteur = mock(Conducteur.class);
        when(conducteur.getId()).thenReturn(1L);
        Vehicule vehicule = mock(Vehicule.class);
        when(vehicule.getConducteur()).thenReturn(conducteur);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(5L)).thenReturn(Optional.of(vehicule));
        when(trajetRepository.existsByVehiculeId(5L)).thenReturn(true);

        assertThatThrownBy(() -> vehiculeService.delete(1L, 5L))
                .isInstanceOf(BusinessRuleException.class);

        verify(vehiculeRepository, never()).delete(any());
    }
}
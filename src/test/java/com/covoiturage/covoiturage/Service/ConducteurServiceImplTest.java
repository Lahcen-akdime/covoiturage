package com.covoiturage.covoiturage.Service;

import com.covoiturage.covoiturage.dto.conducteur.ConducteurRequest;
import com.covoiturage.covoiturage.dto.conducteur.ConducteurResponse;
import com.covoiturage.covoiturage.exception.BusinessRuleException;
import com.covoiturage.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage.mapper.Conducteur.ConducteurMapper;
import com.covoiturage.covoiturage.model.Conducteur;
import com.covoiturage.covoiturage.repository.ConducteurRepository;
import com.covoiturage.covoiturage.service.impl.ConducteurServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
@DisplayName("ConducteurServiceImpl")
class ConducteurServiceImplTest {

    @Mock
    private ConducteurRepository conducteurRepository;
    @Mock
    private ConducteurMapper conducteurMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ConducteurServiceImpl conducteurService;


    private ConducteurRequest requete(String email, String permis, String password) {
        ConducteurRequest request = mock(ConducteurRequest.class);
        lenient().when(request.email()).thenReturn(email);
        lenient().when(request.numeroPermis()).thenReturn(permis);
        lenient().when(request.password()).thenReturn(password);
        return request;
    }


    @Test
    @DisplayName("cree le conducteur : email normalise, permis nettoye, mot de passe encode")
    void save_cas_normal() {
        ConducteurRequest request = requete("  Hamid@Mail.COM ", " AB123 ", "secret");
        Conducteur entite = mock(Conducteur.class);
        Conducteur enregistre = mock(Conducteur.class);
        ConducteurResponse reponse = mock(ConducteurResponse.class);

        when(conducteurRepository.existsByEmailIgnoreCase("hamid@mail.com")).thenReturn(false);
        when(conducteurRepository.existsByNumeroPermis("AB123")).thenReturn(false);
        when(conducteurMapper.toEntity(request)).thenReturn(entite);
        when(passwordEncoder.encode("secret")).thenReturn("hash");
        when(conducteurRepository.save(entite)).thenReturn(enregistre);
        when(conducteurMapper.toResponse(enregistre)).thenReturn(reponse);

        ConducteurResponse resultat = conducteurService.save(request);

        assertThat(resultat).isSameAs(reponse);
        verify(entite).setEmail("hamid@mail.com");
        verify(entite).setNumeroPermis("AB123");
        verify(entite).setPassword("hash");
        verify(conducteurRepository).save(entite);
    }

    @Test
    @DisplayName("refuse si l'email existe deja")
    void save_email_existant() {
        ConducteurRequest request = requete("hamid@mail.com", "AB123", "secret");
        when(conducteurRepository.existsByEmailIgnoreCase("hamid@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> conducteurService.save(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("hamid@mail.com");

        verify(conducteurRepository, never()).save(any());
    }

    @Test
    @DisplayName("refuse si le numero de permis existe deja")
    void save_permis_existant() {
        ConducteurRequest request = requete("hamid@mail.com", "AB123", "secret");
        when(conducteurRepository.existsByEmailIgnoreCase("hamid@mail.com")).thenReturn(false);
        when(conducteurRepository.existsByNumeroPermis("AB123")).thenReturn(true);

        assertThatThrownBy(() -> conducteurService.save(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("AB123");

        verify(conducteurRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("getList retourne tous les conducteurs mappes")
    void getList_retourne_tous_les_conducteurs() {
        Conducteur c1 = mock(Conducteur.class);
        Conducteur c2 = mock(Conducteur.class);
        ConducteurResponse r1 = mock(ConducteurResponse.class);
        ConducteurResponse r2 = mock(ConducteurResponse.class);

        when(conducteurRepository.findAll()).thenReturn(List.of(c1, c2));
        when(conducteurMapper.toResponse(c1)).thenReturn(r1);
        when(conducteurMapper.toResponse(c2)).thenReturn(r2);

        assertThat(conducteurService.getList()).containsExactly(r1, r2);
    }

    @Test
    @DisplayName("getList retourne une liste vide si aucun conducteur")
    void getList_vide() {
        when(conducteurRepository.findAll()).thenReturn(List.of());
        assertThat(conducteurService.getList()).isEmpty();
    }

    @Test
    @DisplayName("modifie le profil avec email normalise et permis nettoye")
    void modifier_cas_normal() {
        Conducteur conducteur = mock(Conducteur.class);
        Conducteur modifie = mock(Conducteur.class);
        ConducteurResponse reponse = mock(ConducteurResponse.class);
        ConducteurRequest request = requete(" Nouveau@Mail.com ", " ZZ999 ", "secret");

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(conducteurRepository.existsByEmailIgnoreCaseAndIdNot("nouveau@mail.com", 1L)).thenReturn(false);
        when(conducteurRepository.existsByNumeroPermisAndIdNot("ZZ999", 1L)).thenReturn(false);
        when(conducteurRepository.save(conducteur)).thenReturn(modifie);
        when(conducteurMapper.toResponse(modifie)).thenReturn(reponse);

        ConducteurResponse resultat = conducteurService.modifierConducteur(1L, request);

        assertThat(resultat).isSameAs(reponse);
        verify(conducteurMapper).updateEntity(request, conducteur);
        verify(conducteur).setEmail("nouveau@mail.com");
        verify(conducteur).setNumeroPermis("ZZ999");
    }

    @Test
    @DisplayName("404 si le conducteur n'existe pas")
    void modifier_conducteur_introuvable() {
        ConducteurRequest request = requete("a@mail.com", "AB123", "secret");
        when(conducteurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conducteurService.modifierConducteur(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(conducteurRepository, never()).save(any());
    }

    @Test
    @DisplayName("refuse si l'email appartient a un autre conducteur")
    void modifier_email_pris() {
        Conducteur conducteur = mock(Conducteur.class);
        ConducteurRequest request = requete("pris@mail.com", "AB123", "secret");

            when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
            when(conducteurRepository.existsByEmailIgnoreCaseAndIdNot("pris@mail.com", 1L)).thenReturn(true);

            assertThatThrownBy(() -> conducteurService.modifierConducteur(1L, request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("pris@mail.com");

            verify(conducteurRepository, never()).save(any());
        }


    @Test
    @DisplayName("refuse si le permis appartient a un autre conducteur")
    void modifier_permis_pris() {
        Conducteur conducteur = mock(Conducteur.class);
        ConducteurRequest request = requete("a@mail.com", "AB123", "secret");

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(conducteurRepository.existsByEmailIgnoreCaseAndIdNot("a@mail.com", 1L)).thenReturn(false);
        when(conducteurRepository.existsByNumeroPermisAndIdNot("AB123", 1L)).thenReturn(true);

        assertThatThrownBy(() -> conducteurService.modifierConducteur(1L, request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("AB123");

        verify(conducteurRepository, never()).save(any());
    }


    @Test
    @DisplayName("searchByNameOrEmail nettoie le critere et retourne les resultats mappes")
    void search_retourne_resultats() {
        Conducteur conducteur = mock(Conducteur.class);
        ConducteurResponse reponse = mock(ConducteurResponse.class);

        when(conducteurRepository.rechercherParNomOuEmail("hamid")).thenReturn(List.of(conducteur));
        when(conducteurMapper.toResponse(conducteur)).thenReturn(reponse);

        List<ConducteurResponse> resultat = conducteurService.searchByNameOrEmail("  hamid  ");

        assertThat(resultat).containsExactly(reponse);
        verify(conducteurRepository).rechercherParNomOuEmail("hamid");
    }

    @Test
    @DisplayName("searchByNameOrEmail retourne une liste vide si rien ne correspond")
    void search_aucun_resultat() {
        when(conducteurRepository.rechercherParNomOuEmail("inconnu")).thenReturn(List.of());

        assertThat(conducteurService.searchByNameOrEmail("inconnu")).isEmpty();
    }


    @Test
    @DisplayName("getById retourne le conducteur mappe")
    void getById_cas_normal() {
        Conducteur conducteur = mock(Conducteur.class);
        ConducteurResponse reponse = mock(ConducteurResponse.class);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(conducteurMapper.toResponse(conducteur)).thenReturn(reponse);

        assertThat(conducteurService.getById(1L)).isSameAs(reponse);
    }

    @Test
    @DisplayName("getById leve ResourceNotFoundException si l'id n'existe pas")
    void getById_introuvable() {
        when(conducteurRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conducteurService.getById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    @DisplayName("findById retourne l'entite quand elle existe")
    void findById_cas_normal() {
        Conducteur conducteur = mock(Conducteur.class);
        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));

        assertThat(conducteurService.findById(1L)).isSameAs(conducteur);
    }
}
package com.covoiturage.covoiturage.Service;

import com.covoiturage.covoiturage.dto.Passager.PassagerRequest;
import com.covoiturage.covoiturage.dto.Passager.PassagerResponse;
import com.covoiturage.covoiturage.exception.BusinessRuleException;
import com.covoiturage.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage.mapper.Passager.PassagerMapper;
import com.covoiturage.covoiturage.model.Passager;
import com.covoiturage.covoiturage.repository.PassagerRepository;
import com.covoiturage.covoiturage.service.impl.PassagerServiceImpl;
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
@DisplayName("PassagerServiceImpl")
class PassagerServiceImplTest {

    @Mock
    private PassagerRepository passagerRepository;
    @Mock
    private PassagerMapper passagerMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PassagerServiceImpl passagerService;

    // ---------- helpers ----------

    private PassagerRequest requete(String email, String password) {
        PassagerRequest request = mock(PassagerRequest.class);
        lenient().when(request.email()).thenReturn(email);
        lenient().when(request.password()).thenReturn(password);
        return request;
    }



        @Test
        @DisplayName("cree le passager : email normalise, mot de passe encode")
        void save_cas_nominal() {
            PassagerRequest request = requete("  Elhoucine@Mail.COM ", "secret");
            Passager entite = mock(Passager.class);
            Passager enregistre = mock(Passager.class);
            PassagerResponse reponse = mock(PassagerResponse.class);

            when(passagerRepository.existsByEmailIgnoreCase("elhoucine@mail.com")).thenReturn(false);
            when(passagerMapper.toEntity(request)).thenReturn(entite);
            when(passwordEncoder.encode("secret")).thenReturn("hash");
            when(passagerRepository.save(entite)).thenReturn(enregistre);
            when(passagerMapper.toResponse(enregistre)).thenReturn(reponse);

            PassagerResponse resultat = passagerService.save(request);

            assertThat(resultat).isSameAs(reponse);
            verify(entite).setEmail("elhoucine@mail.com");
            verify(entite).setPassword("hash");
            verify(passagerRepository).save(entite);
        }

        @Test
        @DisplayName("refuse si l'email existe deja")
        void save_email_existant() {
            PassagerRequest request = requete("elhoucine@mail.com", "secret");
            when(passagerRepository.existsByEmailIgnoreCase("elhoucine@mail.com")).thenReturn(true);

            assertThatThrownBy(() -> passagerService.save(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("elhoucine@mail.com");

            verify(passagerRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(any());
        }

    // ---------- getById / findById ----------

    @Test
    @DisplayName("getById retourne le passager mappe")
    void getById_cas_nominal() {
        Passager passager = mock(Passager.class);
        PassagerResponse reponse = mock(PassagerResponse.class);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(passagerMapper.toResponse(passager)).thenReturn(reponse);

        assertThat(passagerService.getById(1L)).isSameAs(reponse);
    }

    @Test
    @DisplayName("getById leve ResourceNotFoundException si l'id n'existe pas")
    void getById_introuvable() {
        when(passagerRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passagerService.getById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    @DisplayName("findById retourne l'entite quand elle existe")
    void findById_cas_nominal() {
        Passager passager = mock(Passager.class);
        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));

        assertThat(passagerService.findById(1L)).isSameAs(passager);
    }

    // ---------- getList ----------

    @Test
    @DisplayName("getList retourne tous les passagers mappes")
    void getList_retourne_tous_les_passagers() {
        Passager p1 = mock(Passager.class);
        Passager p2 = mock(Passager.class);
        PassagerResponse r1 = mock(PassagerResponse.class);
        PassagerResponse r2 = mock(PassagerResponse.class);

        when(passagerRepository.findAll()).thenReturn(List.of(p1, p2));
        when(passagerMapper.toResponse(p1)).thenReturn(r1);
        when(passagerMapper.toResponse(p2)).thenReturn(r2);

        assertThat(passagerService.getList()).containsExactly(r1, r2);
    }

    @Test
    @DisplayName("getList retourne une liste vide si aucun passager")
    void getList_vide() {
        when(passagerRepository.findAll()).thenReturn(List.of());

        assertThat(passagerService.getList()).isEmpty();
    }


    @Test
    @DisplayName("searchByNameOrEmail retourne les resultats mappes")
    void search_retourne_resultats() {
        Passager passager = mock(Passager.class);
        PassagerResponse reponse = mock(PassagerResponse.class);

        when(passagerRepository.rechercherParNomOuEmail("elhoucine")).thenReturn(List.of(passager));
        when(passagerMapper.toResponse(passager)).thenReturn(reponse);

        assertThat(passagerService.searchByNameOrEmail("elhoucine")).containsExactly(reponse);
    }

    @Test
    @DisplayName("searchByNameOrEmail retourne une liste vide si rien ne correspond")
    void search_aucun_resultat() {
        when(passagerRepository.rechercherParNomOuEmail("inconnu")).thenReturn(List.of());
        assertThat(passagerService.searchByNameOrEmail("inconnu")).isEmpty();
    }

        @Test
        @DisplayName("modifie le passager avec email normalise")
        void update_cas_nominal() {
            Passager passager = mock(Passager.class);
            Passager sauvegarde = mock(Passager.class);
            PassagerResponse reponse = mock(PassagerResponse.class);
            PassagerRequest request = requete(" Nouveau@Mail.com ", "secret");

            when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
            when(passagerRepository.existsByEmailIgnoreCaseAndIdNot("nouveau@mail.com", 1L)).thenReturn(false);
            when(passagerRepository.save(passager)).thenReturn(sauvegarde);
            when(passagerMapper.toResponse(sauvegarde)).thenReturn(reponse);

            PassagerResponse resultat = passagerService.update(1L, request);

            assertThat(resultat).isSameAs(reponse);
            verify(passagerMapper).updateEntity(request, passager);
            verify(passager).setEmail("nouveau@mail.com");
        }

        @Test
        @DisplayName("404 si le passager n'existe pas")
        void update_passager_introuvable() {
            PassagerRequest request = requete("a@mail.com", "secret");
            when(passagerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passagerService.update(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(passagerRepository, never()).save(any());
        }

        @Test
        @DisplayName("refuse si l'email appartient a un autre passager")
        void update_email_pris() {
            Passager passager = mock(Passager.class);
            PassagerRequest request = requete("pris@mail.com", "secret");

            when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
            when(passagerRepository.existsByEmailIgnoreCaseAndIdNot("pris@mail.com", 1L)).thenReturn(true);

            assertThatThrownBy(() -> passagerService.update(1L, request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("pris@mail.com");

            verify(passagerRepository, never()).save(any());
        }

    @Test
    @DisplayName("searchByEmail normalise l'email et retourne le passager")
    void searchByEmail_trouve() {
        Passager passager = mock(Passager.class);
        PassagerResponse reponse = mock(PassagerResponse.class);

        when(passagerRepository.findByEmailIgnoreCase("elhoucine@mail.com")).thenReturn(Optional.of(passager));
        when(passagerMapper.toResponse(passager)).thenReturn(reponse);

        assertThat(passagerService.searchByEmail("  Elhoucine@Mail.COM ")).isSameAs(reponse);
    }

    @Test
    @DisplayName("searchByEmail leve ResourceNotFoundException si aucun passager")
    void searchByEmail_introuvable() {
        when(passagerRepository.findByEmailIgnoreCase("inconnu@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passagerService.searchByEmail("inconnu@mail.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
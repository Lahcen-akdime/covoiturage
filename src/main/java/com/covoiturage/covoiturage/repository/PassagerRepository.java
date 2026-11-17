package com.covoiturage.covoiturage.repository;

import com.covoiturage.covoiturage.model.Conducteur;
import com.covoiturage.covoiturage.model.Passager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PassagerRepository extends JpaRepository<Passager, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Optional<Passager> findByEmailIgnoreCase(String email);

    Optional<Passager> findById(Long id) ;

    @Query("""
            select c from Passager c
            where lower(c.nom) like lower(concat('%', :critere, '%'))
               or lower(c.email) like lower(concat('%', :critere, '%'))
            order by c.nom, c.prenom
            """)
    List<Passager> rechercherParNomOuEmail(@Param("critere") String critere);
}
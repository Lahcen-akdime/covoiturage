package com.covoiturage.covoiturage.repository;

import com.covoiturage.covoiturage.dto.Trajet.TrajetRechercheResponse;
import com.covoiturage.covoiturage.model.Enum.StatutReservation;
import com.covoiturage.covoiturage.model.Enum.StatutTrajet;
import com.covoiturage.covoiturage.model.Trajet;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TrajetRepository extends JpaRepository<Trajet, Long> , JpaSpecificationExecutor<Trajet> {

    boolean existsByVehiculeId(Long vehiculeId);

    @Query("""
            select t from Trajet t
            join fetch t.conducteur
            join fetch t.vehicule v
            join fetch v.conducteur
            left join fetch t.pointsArret
            where t.id = :id
            """)
    Optional<Trajet> findDetailById(@Param("id") Long id);

    /** Verrou pessimiste : bloque la ligne du trajet jusqu'à la fin de la transaction. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trajet t where t.id = :id")
    Optional<Trajet> findByIdForUpdate(@Param("id") Long id);

    @Query("""
    SELECT DISTINCT t
    FROM Trajet t
    JOIN t.pointsArret depart
    JOIN t.pointsArret arrivee
    WHERE t.statut = :statutTrajet
      AND LOWER(depart.ville) = LOWER(:villeDepart)
      AND LOWER(arrivee.ville) = LOWER(:villeArrivee)
      AND depart.ordre < arrivee.ordre
      AND t.dateDepart >= :debut
      AND t.dateDepart < :fin
      AND arrivee.prixDepuisDepart - depart.prixDepuisDepart <= :prixMax
      AND t.placesProposees - (
          SELECT COALESCE(SUM(r.nombrePlaces), 0)
          FROM Reservation r
          WHERE r.trajet = t
            AND r.statut = :statutReservation
      ) >= :places
    ORDER BY t.dateDepart ASC
    """)
    Page<Trajet> rechercher(
            @Param("villeDepart") String villeDepart,
            @Param("villeArrivee") String villeArrivee,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin,
            @Param("places") long places,
            @Param("prixMax") BigDecimal prixMax,
            @Param("statutTrajet") StatutTrajet statutTrajet,
            @Param("statutReservation") StatutReservation statutReservation,
            Pageable pageable
    );

    @Query("""
    SELECT distinct t FROM Trajet t
        JOIN FETCH t.conducteur
            left join fetch t.pointsArret
               where t.id in :trajetsId
    """)
    List<Trajet> getConducteurAndPointDarretsByTrajets(List<Long> trajetsId) ;

    @EntityGraph(attributePaths = {"conducteur", "pointsArret"})
    @Override
    Page<Trajet> findAll(
            Specification<Trajet> spec,
            Pageable pageable
    );
}
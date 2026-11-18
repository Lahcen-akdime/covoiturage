package com.covoiturage.covoiturage.repository;

import com.covoiturage.covoiturage.model.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    boolean existsByVehiculeId(Long vehiculeId);
    
    @Query("""
            SELECT t FROM Trajet t 
            join fetch t.conducteur
            join fetch t.vehicule v
            join fetch v.conducteur
            left join fetch t.pointsArret
            where t.id = :id
            """)
    Optional<Trajet> findDetailById(@Param("id") Long id) ;

}
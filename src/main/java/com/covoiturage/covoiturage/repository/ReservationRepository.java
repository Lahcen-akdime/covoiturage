package com.covoiturage.covoiturage.repository;

import com.covoiturage.covoiturage.dto.Trajet.PlacesReservedOfTrajet;
import com.covoiturage.covoiturage.model.Enum.StatutReservation;
import com.covoiturage.covoiturage.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByPassagerIdAndTrajetIdAndStatut(Long passagerId, Long trajetId, StatutReservation statut);

    @Query("""
            select coalesce(sum(r.nombrePlaces), 0L)
            from Reservation r
            where r.trajet.id = :trajetId and r.statut = :statut
            """)
    long sommePlaces(@Param("trajetId") Long trajetId, @Param("statut") StatutReservation statut);

    /** Places occupées = somme des places des réservations CONFIRMEE. */
    default int placesOccupees(Long trajetId) {
        return (int) sommePlaces(trajetId, StatutReservation.CONFIRMEE);
    }

    @Query("""
            select r from Reservation r
            join fetch r.trajet
            join fetch r.pointMontee
            join fetch r.pointDescente
            join fetch r.passager
            where r.passager.id = :passagerId
            order by r.dateReservation desc, r.id desc
            """)
    List<Reservation> findHistoriqueParPassager(@Param("passagerId") Long passagerId);

    @Query("""
            select r from Reservation r
            join fetch r.trajet
            join fetch r.pointMontee
            join fetch r.pointDescente
            join fetch r.passager
            where r.trajet.id = :trajetId
            order by r.dateReservation asc, r.id asc
            """)
    List<Reservation> findParTrajet(@Param("trajetId") Long trajetId);


    @Query("""
    SELECT r.trajet.id as trajetId , sum(r.nombrePlaces) as places 
        from Reservation r 
            where r.trajet.id in :ids and r.statut = :status
                group by r.trajet.id
        """)
    List<PlacesReservedOfTrajet> getListReservationWhereIdIn(@Param("ids") List<Long> ids , @Param("status") StatutReservation status ) ;



}
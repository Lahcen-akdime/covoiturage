package com.covoiturage.covoiturage.specification;

import com.covoiturage.covoiturage.model.Enum.StatutReservation;
import com.covoiturage.covoiturage.model.Enum.StatutTrajet;
import com.covoiturage.covoiturage.model.PointArret;
import com.covoiturage.covoiturage.model.Reservation;
import com.covoiturage.covoiturage.model.Trajet;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrajetSpecs {

    private TrajetSpecs(){

    }

    public static Specification<Trajet> searchCriteria(String villeDepart , String villeArrive ,
                                              LocalDate date , int nombreDesPlaces , BigDecimal prixMax){

        return ((root , query , cb) -> {
            // new predicates array list
            List<Predicate> predicates = new ArrayList<>() ;

            // distinct query
            query.distinct(true) ;

            // join monte avec trajet , et descente aussi
            Join<Trajet , PointArret> montee = root.join("pointsArret") ;
            Join<Trajet , PointArret> descente = root.join("pointsArret") ;

            // declarer l'intervale de la date de paramétre en debut et fin

            LocalDateTime debut = date.atStartOfDay() ;
            LocalDateTime fin = date.plusDays(1).atStartOfDay() ;

            // ajouter la condition de statut qui doit etre publie

            predicates.add(cb.equal(root.get("statut"),StatutTrajet.PUBLIE)) ;

            // ajouter la condition de la ville de montee doit etre la meme que la ville depart

            predicates.add(cb.equal(montee.get("ville"),villeDepart.trim().toLowerCase(Locale.ROOT))) ;

            // ajouter la condition de ville descentr egal au ville de parametre

            predicates.add(cb.equal(descente.get("ville"),villeArrive.trim().toLowerCase(Locale.ROOT))) ;

            // verifier que l'ordre de monte et moins l'ordre de descente

            predicates.add(cb.lessThan(montee.<Integer>get("ordre"),descente.<Integer>get("ordre"))) ;

            // met le trajet dans l'intervale de debut et fin ( date_depart graterThanOrEqualTo debut , etl'nverse pour fin )

            predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("dateDepart"),debut)) ;
            predicates.add(cb.lessThan(root.<LocalDateTime>get("dateDepart"),fin)) ;

            // verifier que la diff entre "prixDupuisDepart" de descente et de montee et lessOrEqualTo prixMax de paramétre

            predicates.add(cb.lessThanOrEqualTo(cb.diff(descente.get("prixDepuisDepart"),montee.get("prixDepuisDepart")),prixMax)) ;

            // creer la subquery qui va recuperer les places ocoupees
            // et la Root reservation qui va stoqué ce nombre avec elle : ocoupees.from(Reservation.class)
            Subquery<Long> ocoupees = query.subquery(Long.class) ;
            Root<Reservation> reservationRoot = ocoupees.from(Reservation.class) ;


            // la selection de sum de nombrePlaces de reservation root dans la subquery ocoupes
            // si le trajet de reservationRoot = root
            // et si la statut = CONFIRMEE
            ocoupees.select(cb.coalesce(cb.sumAsLong(reservationRoot.get("nombrePlaces")),0L))
                            .where(cb.equal(reservationRoot.get("trajet"),root),
                                    cb.equal(reservationRoot.get("statut"),StatutReservation.CONFIRMEE)) ;

            // ajouter la condition finale ,
            // la defference entre placesProposees de root principale ,
            // et ocoupe des reservation
            // et nombreDesPlaces
            // tous doit etre le
            predicates.add(cb.le(ocoupees,cb.diff(root.get("placesProposees"),nombreDesPlaces))) ;

            // finalement retourner predicates.toArray(new Predicate[0])) ;

            return cb.and(predicates.toArray(new Predicate[0])) ;

        });
    }
}

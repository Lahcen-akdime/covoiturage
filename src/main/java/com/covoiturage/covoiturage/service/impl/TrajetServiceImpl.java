package com.covoiturage.covoiturage.service.impl;

import com.covoiturage.covoiturage.dto.Trajet.PublierTrajetRequest;
import com.covoiturage.covoiturage.dto.Trajet.TrajetDetailResponse;
import com.covoiturage.covoiturage.dto.pointArret.PointArretRequest;
import com.covoiturage.covoiturage.exception.ForbiddenOperationException;
import com.covoiturage.covoiturage.exception.InvalidRequestException;
import com.covoiturage.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage.mapper.Trajet.TrajetMapper;
import com.covoiturage.covoiturage.model.Conducteur;
import com.covoiturage.covoiturage.model.Enum.StatutTrajet;
import com.covoiturage.covoiturage.model.PointArret;
import com.covoiturage.covoiturage.model.Trajet;
import com.covoiturage.covoiturage.model.Vehicule;
import com.covoiturage.covoiturage.repository.ConducteurRepository;
import com.covoiturage.covoiturage.repository.ReservationRepository;
import com.covoiturage.covoiturage.repository.TrajetRepository;
import com.covoiturage.covoiturage.repository.VehiculeRepository;
import com.covoiturage.covoiturage.service.TrajetService;
import com.covoiturage.covoiturage.specification.TrajetSpecs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrajetServiceImpl implements TrajetService {

    private final TrajetRepository trajetRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TrajetMapper trajetMapper;
    private final Clock clock;

    private static final BigDecimal maxPriceByDefault = new BigDecimal("99999999.99");

    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public TrajetDetailResponse publier(Long conducteurId, PublierTrajetRequest request) {
        Conducteur conducteur = conducteurRepository.findById(conducteurId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conducteur not found avec id " + conducteurId));
        Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicule not found avec id " + request.vehiculeId()));

        if (!vehicule.getConducteur().getId().equals(conducteurId)) {
            throw new ForbiddenOperationException("Ce vehicule n'appartient pas au conducteur "
                    + conducteurId + " : il ne peut pas l'utiliser pour publier un trajet.");
        }

        verifierPlaces(request.placesProposees(), vehicule);
        verifierDateDepart(request.dateDepart());
        List<PointArretRequest> points = request.pointsArret();
        verifierLeChemin(points, request.dateDepart());

        Trajet trajet = new Trajet();
        trajet.setConducteur(conducteur);
        trajet.setVehicule(vehicule);
        trajet.setDateDepart(request.dateDepart());
        trajet.setPlacesProposees(request.placesProposees());
        trajet.setStatut(StatutTrajet.PUBLIE);
        trajet.setVilleDepart(points.get(0).ville().trim().toLowerCase(Locale.ROOT));
        trajet.setVilleArrivee(points.get(points.size() - 1).ville().trim());

        for (int i = 0; i < points.size() ; i++) {
            trajet.ajouterPointArret(buildPoint(points.get(i),i));
        };

        Trajet sauvegarde = trajetRepository.save(trajet);
        log.info("Trajet publie : id={}, conducteurId={}, points={}",
                sauvegarde.getId(), conducteurId, points.size());
        return trajetMapper.toDetail(sauvegarde, calculerPlacesRestantes(sauvegarde));
    }

    @Override
    public TrajetDetailResponse consulterDetail(Long id) {
        Trajet trajet = trajetRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("il y a pas de trajet avec id " + id));
        return trajetMapper.toDetail(trajet, calculerPlacesRestantes(trajet));
    }

    @Override
    public Page<Trajet> rechercher(String villeDepart, String villeArrivee, LocalDate date, Integer places, BigDecimal prixMax, int page, int taille) {

        if(villeArrivee == null || villeArrivee.isBlank() || villeDepart == null || villeDepart.isBlank()){
            throw new InvalidRequestException("l'un des villes et pas valide !") ;
        }

        if(date == null){
            throw new InvalidRequestException("la date est null ! ") ;
        }

        String depart = villeDepart.trim().toLowerCase(Locale.ROOT) ;
        String fin = villeArrivee.trim().toLowerCase(Locale.ROOT) ;

        if(depart.equalsIgnoreCase(fin)){
            throw new InvalidRequestException("La vile depart et arrive ce sont les meme !") ;
        }

        int placeDemandes = (places == null) ? 1 : places ;

        if(placeDemandes < 1 ){
            throw new InvalidRequestException("Le nombre des places est negatif") ;
        }

        BigDecimal priceLimit = (prixMax == null) ? maxPriceByDefault : prixMax ;

        if(page < 0){
            throw new InvalidRequestException("Le nombre de page est negatif ") ;
        }

        Specification<Trajet> specification = TrajetSpecs.searchCriteria(depart,fin,date,placeDemandes,priceLimit) ;


        Pageable pageable = PageRequest.of(page,taille, Sort.by("dateDepart").ascending().and(Sort.by("id").ascending())) ;

        Page<Trajet> trajetPage = trajetRepository.findAll(specification,pageable) ;

        log.info("le recherche des trajets et passe , resultat : {}",trajetPage) ;

        //if(trajetPage.isEmpty()){
           // throw new InvalidRequestException("Not found") ;
        //}
       return trajetPage ;
    }



    private void verifierPlaces(int placesProposees, Vehicule vehicule) {
        if (placesProposees > vehicule.getCapacite()) {
            throw new InvalidRequestException("Vous proposez " + placesProposees
                    + " places, mais ce vehicule a une capacite de " + vehicule.getCapacite()
                    + " : " + vehicule.getCapacite() + " places au maximum");
        }
    }

    private void verifierDateDepart(LocalDateTime dateDepart) {
        if (!dateDepart.isAfter(LocalDateTime.now(clock))) {
            throw new InvalidRequestException("La date de depart doit etre dans apres aujourd'hui");
        }
    }

    public void verifierLeChemin(List<PointArretRequest> points , LocalDateTime dateDpart){
        // Recupere le depar of voyage
        PointArretRequest depart = points.get(0) ;

        // Verifier si le depart = la premier point
        if(!depart.heurePassage().equals(dateDpart.toLocalTime())){
            throw new InvalidRequestException("Le temp de demmarage doit etre egal au temp de premier point"+
                    "car le deparet est : "+dateDpart.toLocalTime()+" et l'heure de peremir point est "+depart.heurePassage()) ;
        }

        // Verifier si le prix commence par zero au debut
        if(depart.prixDepuisDepart().compareTo(BigDecimal.ZERO) != 0){
            throw new InvalidRequestException("Le prix doit etre 0 en premier point") ;
        }

        // loop to check all points process +
        for (int i = 0; i < points.size()-1; i++) {
            if(points.get(i+1).heurePassage().isBefore(points.get(i).heurePassage())){
                throw new InvalidRequestException("L'heure "+points.get(i+1)+" et avant l'heure "+points.get(i));
            }
        }

        // handle duplicates villes
        Set<String> villes = new HashSet<>();
        points.forEach(p -> {
            if (!villes.add(p.ville().trim().toLowerCase(Locale.ROOT))) {
                throw new InvalidRequestException("La ville " + p.ville().trim()
                        + " apparaît plusieurs fois sur le parcours.");
            }
        });

    }

    private PointArret buildPoint(PointArretRequest source, int ordre) {
        PointArret point = PointArret.builder()
        .ville(source.ville().trim().toLowerCase(Locale.ROOT))
        .ordre(ordre)
        .heurePassage(source.heurePassage())
        .prixDepuisDepart(source.prixDepuisDepart().setScale(2, RoundingMode.HALF_UP))
        .build() ;
        return point;
    }

    private int calculerPlacesRestantes(Trajet trajet) {
        return trajet.getPlacesProposees() - reservationRepository.placesOccupees(trajet.getId());
    }

}






    /*public Page<Trajet> rechercher(String villeDepart, String villeArrivee, LocalDate date, Integer places, BigDecimal prixMax, int page, int taille) {

        if (villeDepart == null || villeDepart.isBlank() || villeArrivee == null || villeArrivee.isBlank()) {
            throw new InvalidRequestException("les villes de depart et d'arrivee sont obligatoires");
        }

        String depart = villeDepart.trim();
        String arrivee = villeArrivee.trim();

        if (depart.equalsIgnoreCase(arrivee)) {
            throw new InvalidRequestException("La ville de depart egal la ville arrivee");
        }

        int placesDemandees = (places == null) ? 1 : places;

        if (placesDemandees < 1) {
            throw new InvalidRequestException("nombre de place recherches < 1 ");
        }

        BigDecimal priceLimit = (prixMax == null) ? Limit_price : prixMax;

        if (priceLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("Le prix maximal < 0");
        }

        Page<Trajet> resultat = trajetRepository.rechercher(
                depart,
                arrivee,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay(),
                placesDemandees,
                priceLimit,
                StatutTrajet.PUBLIE,
                StatutReservation.CONFIRMEE,
                PageRequest.of(page, taille)
        );

        log.info("depart et arrive {} = > {} le {} , resultat : {} résultat(s)", depart, arrivee, date, resultat.getTotalElements());

        return resultat;
    }*/

package com.covoiturage.covoiturage.repository;

import com.covoiturage.covoiturage.model.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    boolean existsByMatriculeIgnoreCase(String matricule);

    boolean existsByMatriculeIgnoreCaseAndIdNot(String matricule, Long id);

    List<Vehicule> findByConducteurId(Long conducteurId);
}
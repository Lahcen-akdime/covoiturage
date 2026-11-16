package ma.youcode.covoiturage.entity;

import com.covoiturage.covoiturage.model.Personne;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.SuperBuilder;

@Entity
@Table(name = "conducteurs")
@Getter
@Setter
@SuperBuilder
public class Conducteur extends Personne {

    @Column(name = "numero_permis", nullable = false, unique = true, length = 30)
    private String numeroPermis;
}
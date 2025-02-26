package fr.formation.repository;

import fr.formation.model.Adherent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdherentRepository extends JpaRepository<Adherent, String> {
    List<Adherent> findByNomContainingIgnoreCase(String nom);
}

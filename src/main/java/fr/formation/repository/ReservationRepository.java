package fr.formation.repository;

import fr.formation.model.Adherent;
import fr.formation.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    int countByAdherentAndDateFinIsNull(Adherent adherent);
}
package fr.formation.repository;

import fr.formation.model.Adherent;
import fr.formation.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    int countByAdherentAndDateFinIsNull(Adherent adherent);

    List<Reservation> findByDateFinAfter(LocalDate date);
    List<Reservation> findByAdherentAndDateFinAfter(Adherent adherent, LocalDate date);
}
package fr.formation.service;

import fr.formation.model.Adherent;
import fr.formation.model.Livre;
import fr.formation.model.Reservation;
import fr.formation.repository.AdherentRepository;
import fr.formation.repository.LivreRepository;
import fr.formation.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private AdherentRepository adherentRepository;

    public Reservation ajouterReservation(String codeAdherent, String isbn) {
        // Vérifier que l'adhérent existe
        Adherent adherent = adherentRepository.findById(codeAdherent)
                .orElseThrow(() -> new EntityNotFoundException("Adhérent non trouvé"));

        // Vérifier que le livre existe
        Livre livre = livreRepository.findById(isbn)
                .orElseThrow(() -> new EntityNotFoundException("Livre non trouvé"));

        // Vérifier si le livre est disponible
        if (!livre.isDisponible()) {
            throw new IllegalStateException("Le livre n'est pas disponible");
        }

        // Vérifier que l'adhérent n'a pas plus de 3 réservations en cours
        int reservationsActives = reservationRepository.countByAdherentAndDateFinIsNull(adherent);
        if (reservationsActives >= 3) {
            throw new IllegalStateException("L'adhérent a atteint le nombre maximal de réservations");
        }

        // Créer et enregistrer la réservation
        Reservation reservation = new Reservation(null, adherent, livre, LocalDate.now(), LocalDate.now().plusMonths(4));
        return reservationRepository.save(reservation);
    }

    public void annulerReservation(Long reservationId) {
        // Vérifier si la réservation existe
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Réservation non trouvée"));

        // Vérifier si la réservation est encore active
        if (reservation.getDateFin().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Impossible d'annuler une réservation déjà terminée");
        }

        // Rendre le livre à nouveau disponible
        Livre livre = reservation.getLivre();
        livre.setDisponible(true);
        livreRepository.save(livre);

        // Supprimer la réservation
        reservationRepository.delete(reservation);
    }
}

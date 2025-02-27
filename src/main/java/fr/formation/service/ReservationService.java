package fr.formation.service;

import fr.formation.model.Adherent;
import fr.formation.model.Livre;
import fr.formation.model.Reservation;
import fr.formation.repository.AdherentRepository;
import fr.formation.repository.LivreRepository;
import fr.formation.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private AdherentRepository adherentRepository;

    @Autowired
    private MailService mailService;

    public Reservation ajouterReservation(String codeAdherent, String isbn, LocalDate dateFin) {
        Adherent adherent = adherentRepository.findById(codeAdherent)
                .orElseThrow(() -> new EntityNotFoundException("Adhérent non trouvé"));
        Livre livre = livreRepository.findById(isbn)
                .orElseThrow(() -> new EntityNotFoundException("Livre non trouvé"));

        if (!livre.isDisponible()) {
            throw new IllegalStateException("Le livre n'est pas disponible");
        }

        int reservationsActives = reservationRepository.countByAdherentAndDateFinIsNull(adherent);
        if (reservationsActives >= 3) {
            throw new IllegalStateException("L'adhérent a atteint le nombre maximal de réservations");
        }

        // Vérification que la date de fin est dans les 4 mois suivant la date de début
        if (dateFin.isAfter(LocalDate.now().plusMonths(4))) {
            throw new IllegalArgumentException("La date de fin ne peut pas être supérieure à 4 mois après la date de début");
        }

        // Créer la réservation avec la date de fin renseignée par l'appelant
        Reservation reservation = new Reservation(null, adherent, livre, LocalDate.now(), dateFin);
        return reservationRepository.save(reservation);
    }


    @Transactional
    public void annulerReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Réservation non trouvée"));

        // Si la réservation est déjà terminée, on ne peut pas l'annuler
        if (reservation.getDateFin().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Impossible d'annuler une réservation déjà terminée");
        }

        // Remet le livre en disponibilité
        Livre livre = reservation.getLivre();
        livre.setDisponible(true);
        livreRepository.save(livre);

        // Supprime la réservation
        reservationRepository.delete(reservation);
    }

    public List<Reservation> recupererReservationsActives() {
        return reservationRepository.findByDateFinAfter(LocalDate.now());
    }

    public List<Reservation> recupererReservationsActivesParAdherent(String codeAdherent) {
        Adherent adherent = adherentRepository.findById(codeAdherent)
                .orElseThrow(() -> new EntityNotFoundException("Adhérent non trouvé"));
        return reservationRepository.findByAdherentAndDateFinAfter(adherent, LocalDate.now());
    }

    public List<Reservation> recupererHistoriqueReservationsAdherent(String codeAdherent) {
        Adherent adherent = adherentRepository.findById(codeAdherent)
                .orElseThrow(() -> new EntityNotFoundException("Adhérent non trouvé"));
        return reservationRepository.findByAdherent(adherent);
    }
    public void envoyerRappelReservationsDepassees() {
        List<Reservation> reservationsDepassees = reservationRepository.findByDateFinBefore(LocalDate.now());

        // Regroupe les réservations par adhérent
        Map<Adherent, List<Reservation>> reservationsParAdherent = reservationsDepassees.stream()
                .collect(Collectors.groupingBy(Reservation::getAdherent));

        // Pour chaque adhérent, on envoie un seul mail
        for (Map.Entry<Adherent, List<Reservation>> entry : reservationsParAdherent.entrySet()) {
            Adherent adherent = entry.getKey();
            List<Reservation> reservations = entry.getValue();

            // Crée le message du mail
            StringBuilder message = new StringBuilder();
            message.append("Cher(e) ").append(adherent.getPrenom()).append(",\n\n");
            message.append("Vous avez des réservations en retard :\n");

            for (Reservation reservation : reservations) {
                message.append("- ").append(reservation.getLivre().getTitre())
                        .append(" (fin prévue le ").append(reservation.getDateFin()).append(")\n");
            }

            message.append("\nMerci de les retourner au plus vite.\n\n");
            message.append("Cordialement,\nBibliothèque");

            mailService.envoyerMail(adherent.getAdresseMail(), "Rappel de vos réservations dépassées", message.toString());
        }
    }

    public void supprimerReservationsExpirees() {
        List<Reservation> reservationsExpirees = reservationRepository.findByDateFinBefore(LocalDate.now());

        for (Reservation reservation : reservationsExpirees) {
            reservationRepository.delete(reservation);
        }
    }
}

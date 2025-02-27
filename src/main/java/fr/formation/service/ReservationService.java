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

    public List<Reservation> getReservationsActives() {
        return reservationRepository.findByDateFinAfter(LocalDate.now());
    }

    public List<Reservation> getReservationsActivesParAdherent(String codeAdherent) {
        // Vérifier si l'adhérent existe
        Adherent adherent = adherentRepository.findById(codeAdherent)
                .orElseThrow(() -> new EntityNotFoundException("Adhérent non trouvé"));

        return reservationRepository.findByAdherentAndDateFinAfter(adherent, LocalDate.now());
    }

    public List<Reservation> getHistoriqueReservationsAdherent(String codeAdherent) {
        // Vérifier que l'adhérent existe
        Adherent adherent = adherentRepository.findById(codeAdherent)
                .orElseThrow(() -> new EntityNotFoundException("Adhérent non trouvé"));

        // Retourner toutes ses réservations (passées et en cours)
        return reservationRepository.findByAdherent(adherent);
    }

    public void envoyerRappelReservationsDepassees() {
        // Récupérer les réservations dépassées
        List<Reservation> reservationsDepassees = reservationRepository.findByDateFinBefore(LocalDate.now());

        // Regrouper les réservations par adhérent
        Map<Adherent, List<Reservation>> reservationsParAdherent = reservationsDepassees.stream()
                .collect(Collectors.groupingBy(Reservation::getAdherent));

        // Envoyer un e-mail pour chaque adhérent
        for (Map.Entry<Adherent, List<Reservation>> entry : reservationsParAdherent.entrySet()) {
            Adherent adherent = entry.getKey();
            List<Reservation> reservations = entry.getValue();

            // Construire le message
            StringBuilder message = new StringBuilder();
            message.append("Cher(e) ").append(adherent.getPrenom()).append(",\n\n");
            message.append("Vous avez des réservations en retard :\n");

            for (Reservation reservation : reservations) {
                message.append("- ").append(reservation.getLivre().getTitre())
                        .append(" (fin prévue le ").append(reservation.getDateFin()).append(")\n");
            }

            message.append("\nMerci de les retourner au plus vite.\n\n");
            message.append("Cordialement,\nBibliothèque");

            // Simuler l’envoi de l’e-mail
            mailService.envoyerMail(adherent.getAdresseMail(), "Rappel de vos réservations dépassées", message.toString());
        }
    }
}

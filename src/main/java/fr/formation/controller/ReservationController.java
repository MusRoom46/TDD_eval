package fr.formation.controller;

import fr.formation.exception.ReservationNotFoundException;
import fr.formation.model.Reservation;
import fr.formation.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // Modification de la méthode pour accepter un paramètre "dateFin"
    @PostMapping
    public ResponseEntity<Reservation> ajouterReservation(
            @RequestParam String codeAdherent,
            @RequestParam String isbn,
            @RequestParam String dateFin // Le paramètre "dateFin" est maintenant requis
    ) {
        // Conversion de la chaîne "dateFin" en LocalDate
        LocalDate dateFinLocalDate = LocalDate.parse(dateFin);

        // Appel à la méthode du service en passant la "dateFin"
        Reservation reservation = reservationService.ajouterReservation(codeAdherent, isbn, dateFinLocalDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annulerReservation(@PathVariable Long id) {
        try {
            reservationService.annulerReservation(id);
            return ResponseEntity.noContent().build();
        } catch (ReservationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/actives")
    public ResponseEntity<List<Reservation>> recupererReservationsActives() {
        List<Reservation> reservations = reservationService.recupererReservationsActives();
        return reservations.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(reservations);
    }

    @GetMapping("/actives/{codeAdherent}")
    public ResponseEntity<List<Reservation>> recupererReservationsActivesAdherent(@PathVariable String codeAdherent) {
        try {
            List<Reservation> reservations = reservationService.recupererReservationsActivesParAdherent(codeAdherent);
            return reservations.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(reservations);
        } catch (ReservationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/historique/{codeAdherent}")
    public ResponseEntity<List<Reservation>> recupererHistoriqueReservationsAdherent(@PathVariable String codeAdherent) {
        try {
            List<Reservation> reservations = reservationService.recupererHistoriqueReservationsAdherent(codeAdherent);
            return reservations.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(reservations);
        } catch (ReservationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/rappel")
    public ResponseEntity<Void> envoyerRappelReservationsDepassees() {
        reservationService.envoyerRappelReservationsDepassees();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/expirees")
    public ResponseEntity<Void> supprimerReservationsExpirees() {
        reservationService.supprimerReservationsExpirees();
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<Void> handleReservationNotFound(ReservationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}

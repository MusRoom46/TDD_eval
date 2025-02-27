package fr.formation.controller;

import fr.formation.exception.ReservationNotFoundException;
import fr.formation.model.*;
import fr.formation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    private Adherent adherent;
    private Livre livre;
    private Reservation reservation;
    private LocalDate dateFin = LocalDate.now().plusMonths(3);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        adherent = new Adherent("A123", "Bedet", "Valentin", "2003-10-24", Civilite.HOMME, "valentin.bedet@mail.com");
        livre = new Livre("9783161484100", "Livre conforme", "Valentin Bedet", "Éditeur IIA", Format.BROCHE, true);

        reservation = new Reservation(1L, adherent, livre, LocalDate.now(), dateFin);
    }

    @Test
    void testAjouterReservation() {
        // Simuler la création d'une réservation avec une dateFin
        LocalDate dateFin = LocalDate.now().plusMonths(4);
        when(reservationService.ajouterReservation("A123", "9783161484100", dateFin)).thenReturn(reservation);

        ResponseEntity<Reservation> response = reservationController.ajouterReservation("A123", "9783161484100", "2025-06-01");

        assertEquals(201, response.getStatusCodeValue());
    }

    @Test
    void testAnnulerReservation_Succes() {
        doNothing().when(reservationService).annulerReservation(1L);

        ResponseEntity<Void> response = reservationController.annulerReservation(1L);

        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testAnnulerReservation_ReservationNonTrouvee() {
        doThrow(new ReservationNotFoundException("Réservation non trouvée")).when(reservationService).annulerReservation(99L);

        ResponseEntity<Void> response = reservationController.annulerReservation(99L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetReservationsActives() {
        when(reservationService.recupererReservationsActives()).thenReturn(List.of(reservation));

        ResponseEntity<List<Reservation>> response = reservationController.recupererReservationsActives();

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetReservationsActives_Vide() {
        when(reservationService.recupererReservationsActives()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Reservation>> response = reservationController.recupererReservationsActives();

        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testGetReservationsActivesAdherent() {
        when(reservationService.recupererReservationsActivesParAdherent("A123")).thenReturn(List.of(reservation));

        ResponseEntity<List<Reservation>> response = reservationController.recupererReservationsActivesAdherent("A123");

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetReservationsActivesAdherent_NonTrouve() {
        doThrow(new ReservationNotFoundException("Adhérent non trouvé")).when(reservationService).recupererReservationsActivesParAdherent("XYZ123");

        ResponseEntity<List<Reservation>> response = reservationController.recupererReservationsActivesAdherent("XYZ123");

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetHistoriqueReservationsAdherent() {
        when(reservationService.recupererHistoriqueReservationsAdherent("A123")).thenReturn(List.of(reservation));

        ResponseEntity<List<Reservation>> response = reservationController.recupererHistoriqueReservationsAdherent("A123");

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetHistoriqueReservationsAdherent_NonTrouve() {
        doThrow(new ReservationNotFoundException("Adhérent non trouvé")).when(reservationService).recupererHistoriqueReservationsAdherent("XYZ123");

        ResponseEntity<List<Reservation>> response = reservationController.recupererHistoriqueReservationsAdherent("XYZ123");

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testEnvoyerRappelReservationsDepassees() {
        doNothing().when(reservationService).envoyerRappelReservationsDepassees();

        ResponseEntity<Void> response = reservationController.envoyerRappelReservationsDepassees();

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testSupprimerReservationsExpirees() {
        doNothing().when(reservationService).supprimerReservationsExpirees();

        ResponseEntity<Void> response = reservationController.supprimerReservationsExpirees();

        assertEquals(204, response.getStatusCodeValue());
    }
}

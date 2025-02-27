package fr.formation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.formation.model.Adherent;
import fr.formation.model.Format;
import fr.formation.model.Livre;
import fr.formation.model.Reservation;
import fr.formation.repository.AdherentRepository;
import fr.formation.repository.LivreRepository;
import fr.formation.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private LivreRepository livreRepository;

    @Mock
    private AdherentRepository adherentRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private ReservationService reservationService;

    private Adherent adherent;
    private Livre livre;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        adherent = new Adherent("A123", "Bedet", "Valentin", "2003-10-24", "valentin.bedet@mail.com");
        livre = new Livre("9783161484100", "Livre conforme", "Valentin Bedet", "Éditeur IIA", Format.BROCHE, true);
        reservation = new Reservation(1L, adherent, livre, LocalDate.now(), LocalDate.now().plusMonths(4));
    }

    @Test
    void testAjouterReservation() {
        // Given : L'adhérent a moins de 3 réservations et le livre est disponible
        when(adherentRepository.findById(adherent.getCodeAdherent())).thenReturn(Optional.of(adherent));
        when(livreRepository.findById(livre.getIsbn())).thenReturn(Optional.of(livre));
        when(reservationRepository.countByAdherentAndDateFinIsNull(adherent)).thenReturn(2);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // When
        Reservation result = reservationService.ajouterReservation(adherent.getCodeAdherent(), livre.getIsbn());

        // Then
        assertNotNull(result);
        assertEquals(adherent.getCodeAdherent(), result.getAdherent().getCodeAdherent());
        assertEquals(livre.getIsbn(), result.getLivre().getIsbn());
    }

    @Test
    void testAjouterReservation_LivreIndisponible() {
        // Given : Livre non disponible
        when(adherentRepository.findById(adherent.getCodeAdherent())).thenReturn(Optional.of(adherent));
        when(livreRepository.findById(livre.getIsbn())).thenReturn(Optional.of(livre));
        livre.setDisponible(false);

        // When - Then
        Exception exception = assertThrows(IllegalStateException.class, () ->
                reservationService.ajouterReservation(adherent.getCodeAdherent(), livre.getIsbn())
        );
        assertEquals("Le livre n'est pas disponible", exception.getMessage());
    }

    @Test
    void testAjouterReservation_MaximumAtteint() {
        // Given : L'adhérent a déjà 3 réservations actives
        when(adherentRepository.findById(adherent.getCodeAdherent())).thenReturn(Optional.of(adherent));
        when(livreRepository.findById(livre.getIsbn())).thenReturn(Optional.of(livre));
        when(reservationRepository.countByAdherentAndDateFinIsNull(adherent)).thenReturn(3);

        // When - Then
        Exception exception = assertThrows(IllegalStateException.class, () ->
                reservationService.ajouterReservation(adherent.getCodeAdherent(), livre.getIsbn())
        );
        assertEquals("L'adhérent a atteint le nombre maximal de réservations", exception.getMessage());
    }

    @Test
    void testAnnulerReservation_Succes() {
        // Given : Une réservation existante et en cours
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        // When
        reservationService.annulerReservation(reservation.getId());

        // Then : Vérifier que la réservation a bien été supprimée
        verify(reservationRepository, times(1)).delete(reservation);
        // Vérifier que le livre est redevenu disponible
        assertTrue(livre.isDisponible());
    }

    @Test
    void testAnnulerReservation_ReservationInexistante() {
        // Given : La réservation n'existe pas
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // When - Then : Vérifier que l'exception est bien levée
        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                reservationService.annulerReservation(999L)
        );
        assertEquals("Réservation non trouvée", exception.getMessage());
    }

    @Test
    void testAnnulerReservation_ReservationDejaTerminee() {
        // Given : Une réservation déjà terminée
        reservation.setDateFin(LocalDate.now().minusDays(1)); // Date passée
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        // When - Then
        Exception exception = assertThrows(IllegalStateException.class, () ->
                reservationService.annulerReservation(reservation.getId())
        );
        assertEquals("Impossible d'annuler une réservation déjà terminée", exception.getMessage());
    }

    @Test
    void testGetReservationsActives() {
        // Given : Deux réservations actives
        Reservation reservation1 = new Reservation(1L, adherent, livre, LocalDate.now(), LocalDate.now().plusMonths(1));
        Reservation reservation2 = new Reservation(2L, adherent, livre, LocalDate.now(), LocalDate.now().plusWeeks(2));
        List<Reservation> reservations = List.of(reservation1, reservation2);

        when(reservationRepository.findByDateFinAfter(LocalDate.now())).thenReturn(reservations);

        // When
        List<Reservation> result = reservationService.getReservationsActives();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    void testGetReservationsActivesAdherent() {
        // Given : Une réservation active pour un adhérent
        Reservation reservation1 = new Reservation(1L, adherent, livre, LocalDate.now(), LocalDate.now().plusMonths(1));
        List<Reservation> reservations = List.of(reservation1);

        when(adherentRepository.findById(adherent.getCodeAdherent())).thenReturn(Optional.of(adherent));
        when(reservationRepository.findByAdherentAndDateFinAfter(adherent, LocalDate.now())).thenReturn(reservations);

        // When
        List<Reservation> result = reservationService.getReservationsActivesParAdherent(adherent.getCodeAdherent());

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void testGetReservationsActivesAdherentInconnu() {
        // Given : Adhérent inconnu
        when(adherentRepository.findById("XYZ123")).thenReturn(Optional.empty());

        // When - Then
        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                reservationService.getReservationsActivesParAdherent("XYZ123")
        );
        assertEquals("Adhérent non trouvé", exception.getMessage());
    }

    @Test
    void testGetHistoriqueReservationsAdherent() {
        // Given : Un adhérent avec 2 réservations (1 active + 1 terminée)
        Reservation reservationActive = new Reservation(1L, adherent, livre, LocalDate.now(), LocalDate.now().plusMonths(1));
        Reservation reservationTerminee = new Reservation(2L, adherent, livre, LocalDate.now().minusMonths(5), LocalDate.now().minusMonths(1));

        List<Reservation> reservations = List.of(reservationActive, reservationTerminee);

        when(adherentRepository.findById(adherent.getCodeAdherent())).thenReturn(Optional.of(adherent));
        when(reservationRepository.findByAdherent(adherent)).thenReturn(reservations);

        // When
        List<Reservation> result = reservationService.getHistoriqueReservationsAdherent(adherent.getCodeAdherent());

        // Then
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    void testGetHistoriqueReservationsAdherent_Vide() {
        // Given : Un adhérent sans réservations
        when(adherentRepository.findById(adherent.getCodeAdherent())).thenReturn(Optional.of(adherent));
        when(reservationRepository.findByAdherent(adherent)).thenReturn(Collections.emptyList());

        // When
        List<Reservation> result = reservationService.getHistoriqueReservationsAdherent(adherent.getCodeAdherent());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetHistoriqueReservationsAdherent_Inexistant() {
        // Given : Adhérent non trouvé
        when(adherentRepository.findById("XYZ123")).thenReturn(Optional.empty());

        // When - Then
        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                reservationService.getHistoriqueReservationsAdherent("XYZ123")
        );
        assertEquals("Adhérent non trouvé", exception.getMessage());
    }

    @Test
    void testEnvoyerRappelReservationsDepassees() {
        // Given : Un adhérent avec 2 réservations dépassées
        Reservation reservation1 = new Reservation(1L, adherent, livre, LocalDate.now().minusMonths(5), LocalDate.now().minusMonths(1));
        Reservation reservation2 = new Reservation(2L, adherent, livre, LocalDate.now().minusMonths(3), LocalDate.now().minusDays(10));

        List<Reservation> reservationsDepassees = List.of(reservation1, reservation2);

        when(reservationRepository.findByDateFinBefore(LocalDate.now())).thenReturn(reservationsDepassees);

        // When
        reservationService.envoyerRappelReservationsDepassees();

        // Then : Vérifier que l'e-mail est bien simulé
        verify(mailService, times(1)).envoyerMail(
                eq(adherent.getAdresseMail()),
                contains("Rappel de vos réservations dépassées"),
                contains(livre.getTitre())
        );
    }

    @Test
    void testEnvoyerRappelReservationsDepassees_Aucune() {
        // Given : Aucune réservation en retard
        when(reservationRepository.findByDateFinBefore(LocalDate.now())).thenReturn(Collections.emptyList());

        // When
        reservationService.envoyerRappelReservationsDepassees();

        // Then : Vérifier qu'aucun e-mail n'est envoyé
        verify(mailService, never()).envoyerMail(anyString(), anyString(), anyString());
    }

    @Test
    void testSuppressionReservationsExpirees() {
        // Given : 2 réservations expirées et 1 encore active
        Reservation reservationExpiree1 = new Reservation(1L, adherent, livre, LocalDate.now().minusMonths(5), LocalDate.now().minusMonths(1));
        Reservation reservationExpiree2 = new Reservation(2L, adherent, livre, LocalDate.now().minusMonths(3), LocalDate.now().minusDays(10));
        Reservation reservationActive = new Reservation(3L, adherent, livre, LocalDate.now().minusDays(10), LocalDate.now().plusDays(10));

        List<Reservation> reservationsExpirees = List.of(reservationExpiree1, reservationExpiree2);

        when(reservationRepository.findByDateFinBefore(LocalDate.now())).thenReturn(reservationsExpirees);

        // When
        reservationService.supprimerReservationsExpirees();

        // Then : Vérifier que seules les réservations expirées sont supprimées
        verify(reservationRepository, times(1)).delete(reservationExpiree1);
        verify(reservationRepository, times(1)).delete(reservationExpiree2);
        verify(reservationRepository, never()).delete(reservationActive);
    }

}

package fr.formation.service;

import fr.formation.model.*;
import fr.formation.repository.AdherentRepository;
import fr.formation.repository.LivreRepository;
import fr.formation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private LocalDate dateFin = LocalDate.now().plusMonths(3);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Création des objets mocks pour Adherent et Livre
        adherent = new Adherent("A123", "Bedet", "Valentin", "2003-10-24", Civilite.HOMME, "valentin.bedet@mail.com");
        livre = new Livre("9783161484100", "Livre conforme", "Valentin Bedet", "Éditeur IIA", Format.BROCHE, true);

        // Créez une réservation avec ID fictif
        Reservation reservationMock = new Reservation(1L, adherent, livre, LocalDate.now(), dateFin);
        // Comportement des mocks
        when(adherentRepository.findById("A123")).thenReturn(Optional.of(adherent));
        when(livreRepository.findById("9783161484100")).thenReturn(Optional.of(livre));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservationMock);
    }

    @Test
    void testAjouterReservation_Success() {
        // Mocker les méthodes nécessaires pour que la logique de la réservation fonctionne
        when(adherentRepository.findById("A123")).thenReturn(Optional.of(adherent));
        when(livreRepository.findById("9783161484100")).thenReturn(Optional.of(livre));
        when(reservationRepository.countByAdherentAndDateFinIsNull(adherent)).thenReturn(0);

        Reservation reservation = reservationService.ajouterReservation("A123", "9783161484100", dateFin);

        // Vérifiez que la réservation n'est pas nulle et que les valeurs sont correctes
        assertNotNull(reservation);
        assertEquals(adherent, reservation.getAdherent());
        assertEquals(livre, reservation.getLivre());
        assertTrue(reservation.getDateFin().isBefore(LocalDate.now().plusMonths(4)));
    }

    @Test
    void testAjouterReservation_LivreIndisponible() {
        // Cas où le livre est indisponible
        when(livreRepository.findById("9783161484100")).thenReturn(Optional.of(new Livre("9783161484100", "Livre ajouté", "Valentin Bedet", "Éditeur IIA", Format.POCHE, false)));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reservationService.ajouterReservation("A123", "9783161484100", dateFin);
        });

        assertEquals("Le livre n'est pas disponible", exception.getMessage());
    }

    @Test
    void testAjouterReservation_MaxReservationsAtteint() {
        // Cas où l'adhérent a déjà 3 réservations actives
        when(reservationRepository.countByAdherentAndDateFinIsNull(adherent)).thenReturn(3);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reservationService.ajouterReservation("A123", "9783161484100", dateFin);
        });

        assertEquals("L'adhérent a atteint le nombre maximal de réservations", exception.getMessage());
    }

    @Test
    void testAnnulerReservation_Success() {
        // Cas où la réservation est annulée avec succès
        Reservation reservation = new Reservation(1L, adherent, livre, LocalDate.now(), LocalDate.now().plusMonths(4));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.annulerReservation(1L);

        verify(reservationRepository).delete(reservation);
        verify(livreRepository).save(livre);
    }

    @Test
    void testAnnulerReservation_ReservationDejaTerminee() {
        // Cas où l'annulation échoue parce que la réservation est déjà terminée
        Reservation reservation = new Reservation(1L, adherent, livre, LocalDate.now().minusMonths(5), LocalDate.now().minusMonths(3));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reservationService.annulerReservation(1L);
        });

        assertEquals("Impossible d'annuler une réservation déjà terminée", exception.getMessage());
    }

    @Test
    void testGetReservationsActives() {
        // Cas où on récupère les réservations actives
        Reservation reservation = new Reservation(1L, adherent, livre, LocalDate.now(), LocalDate.now().plusMonths(4));
        when(reservationRepository.findByDateFinAfter(LocalDate.now())).thenReturn(List.of(reservation));

        List<Reservation> reservations = reservationService.recupererReservationsActives();

        assertFalse(reservations.isEmpty());
        assertEquals(1, reservations.size());
    }

    @Test
    void testEnvoyerRappelReservationsDepassees() {
        // Cas où on envoie un rappel pour les réservations dépassées
        Reservation expiredReservation = new Reservation(1L, adherent, livre, LocalDate.now().minusMonths(6), LocalDate.now().minusMonths(4));
        when(reservationRepository.findByDateFinBefore(LocalDate.now())).thenReturn(List.of(expiredReservation));

        reservationService.envoyerRappelReservationsDepassees();

        // Vérifie que le mail a bien été envoyé avec des matchers pour tous les arguments
        verify(mailService).envoyerMail(
                eq(adherent.getAdresseMail()),
                eq("Rappel de vos réservations dépassées"),
                anyString()
        );
    }

    @Test
    void testSupprimerReservationsExpirees() {
        // Cas où l'on supprime les réservations expirées
        Reservation expiredReservation = new Reservation(1L, adherent, livre, LocalDate.now().minusMonths(6), LocalDate.now().minusMonths(4));
        when(reservationRepository.findByDateFinBefore(LocalDate.now())).thenReturn(List.of(expiredReservation));

        reservationService.supprimerReservationsExpirees();

        verify(reservationRepository).delete(expiredReservation);
    }

    @Test
    void testSupprimerReservationsExpirees_Vide() {
        // Cas où aucune réservation expirée n'est trouvée
        when(reservationRepository.findByDateFinBefore(LocalDate.now())).thenReturn(List.of());

        reservationService.supprimerReservationsExpirees();

        verify(reservationRepository, never()).delete(any(Reservation.class));
    }
}

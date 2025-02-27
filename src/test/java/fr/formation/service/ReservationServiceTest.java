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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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

    @InjectMocks
    private ReservationService reservationService;

    private Adherent adherent;
    private Livre livre;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        adherent = new Adherent("A123", "Bedet", "Valentin", "2003-10-24");
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
}

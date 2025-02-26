package fr.formation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AdherentServiceTest {

    @Mock
    private AdherentRepository adherentRepository;

    @InjectMocks
    private AdherentService adherentService;

    private Adherent adherent;

    @BeforeEach
    void setUp() {
        adherent = new Adherent("A123", "Bedet", "Valentin", "2003-10-24", "M");
    }

    @Test
    void testAjouterAdherent() {
        // Given
        when(adherentRepository.save(any(Adherent.class))).thenReturn(adherent);

        // When
        Adherent result = adherentService.ajouterAdherent(adherent);

        // Then
        assertNotNull(result);
        assertEquals("Doe", result.getNom());
    }

    @Test
    void testModifierAdherent() {
        // Given
        when(adherentRepository.findById(adherent.getCodeAdherent())).thenReturn(Optional.of(adherent));
        Adherent adherentModifie = new Adherent("A123", "Smith", "John", "1990-05-15", "M");
        when(adherentRepository.save(any(Adherent.class))).thenReturn(adherentModifie);

        // When
        Adherent result = adherentService.modifierAdherent("A123", adherentModifie);

        // Then
        assertNotNull(result);
        assertEquals("Smith", result.getNom());
    }

    @Test
    void testSupprimerAdherent() {
        // Given
        when(adherentRepository.existsById(adherent.getCodeAdherent())).thenReturn(true);
        doNothing().when(adherentRepository).deleteById(adherent.getCodeAdherent());

        // When
        adherentService.supprimerAdherent(adherent.getCodeAdherent());

        // Then
        verify(adherentRepository, times(1)).deleteById(adherent.getCodeAdherent());
    }

    @Test
    void testRechercherParCode() {
        // Given
        when(adherentRepository.findById(adherent.getCodeAdherent())).thenReturn(Optional.of(adherent));

        // When
        Optional<Adherent> result = adherentService.rechercherParCode(adherent.getCodeAdherent());

        // Then
        assertTrue(result.isPresent());
        assertEquals("Doe", result.get().getNom());
    }

    @Test
    void testRechercherParNom() {
        // Given
        when(adherentRepository.findByNomContainingIgnoreCase("Doe")).thenReturn(List.of(adherent));

        // When
        List<Adherent> result = adherentService.rechercherParNom("Doe");

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}

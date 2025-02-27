package fr.formation.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.formation.repository.LivreRepository;
import fr.formation.service.LivreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class LivreTest {

    @Mock
    private LivreRepository livreRepository;

    @InjectMocks
    private LivreService livreService;

    private Livre livre;

    @BeforeEach
    void setUp() {
        livre = new Livre("9783161484100", "Livre conforme", "Valentin Bedet", "Éditeur IIA", Format.BROCHE, true);
    }

    @Test
    void testModifierLivre() {
        // Given
        when(livreRepository.findById(livre.getIsbn())).thenReturn(Optional.of(livre));
        Livre livreModifie = new Livre("9783161484100", "Livre modifie", "Valentin Bedet", "Éditeur IIA", Format.BROCHE, true);
        when(livreRepository.save(any(Livre.class))).thenReturn(livreModifie);

        // When
        Livre result = livreService.modifierLivre("9783161484100", livreModifie);

        // Then
        assertNotNull(result);
        assertEquals("Livre modifie", result.getTitre());
    }

    @Test
    void testSupprimerLivre() {
        // Given
        when(livreRepository.existsById(livre.getIsbn())).thenReturn(true);
        doNothing().when(livreRepository).deleteById(livre.getIsbn());

        // When
        livreService.supprimerLivre(livre.getIsbn());

        // Then
        verify(livreRepository, times(1)).deleteById(livre.getIsbn());
    }

    @Test
    void testRechercherParIsbn() {
        // Given
        when(livreRepository.findById(livre.getIsbn())).thenReturn(Optional.of(livre));

        // When
        Optional<Livre> result = Optional.ofNullable(livreService.rechercherParISBN(livre.getIsbn()));

        // Then
        assertTrue(result.isPresent());
        assertEquals("Livre conforme", result.get().getTitre());
    }

    @Test
    void testRechercherParTitre() {
        // Given
        when(livreRepository.findByTitreContainingIgnoreCase("Livre")).thenReturn(List.of(livre));

        // When
        List<Livre> result = livreService.rechercherParTitre("Livre");

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void testRechercherParAuteur() {
        // Given
        when(livreRepository.findByAuteurContainingIgnoreCase("Valentin")).thenReturn(List.of(livre));

        // When
        List<Livre> result = livreService.rechercherParAuteur("Valentin");

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}
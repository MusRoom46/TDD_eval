package fr.formation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.formation.model.Format;
import fr.formation.model.Livre;
import fr.formation.repository.LivreRepository;
import fr.formation.service.LivreService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class LivreServiceTest {

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
    void testAjouterLivre() {
        // Given
        Livre livreAAjouter = new Livre("9783161484100", "Livre ajouté", "Valentin Bedet", "Éditeur IIA", Format.POCHE, true);
        when(livreRepository.save(any(Livre.class))).thenReturn(livreAAjouter);

        // When
        Livre result = livreService.ajouterLivre(livreAAjouter);

        // Then
        assertNotNull(result);
        assertEquals("Livre ajouté", result.getTitre());
    }

    @Test
    void testModifierLivre() {
        // Given
        when(livreRepository.existsById(livre.getIsbn())).thenReturn(true);  // Simuler l'existence du livre

        Livre livreModifie = new Livre("9783161484100", "Livre modifié", "Valentin Bedet", "Éditeur IIA", Format.BROCHE, true);
        when(livreRepository.save(any(Livre.class))).thenReturn(livreModifie);

        // When
        Livre result = livreService.modifierLivre("9783161484100", livreModifie);

        // Then
        assertNotNull(result);
        assertEquals("Livre modifié", result.getTitre());
    }

    @Test
    void testModifierLivre_LivreNonTrouve() {
        // Given
        when(livreRepository.existsById(livre.getIsbn())).thenReturn(false);

        // When / Then
        assertThrows(EntityNotFoundException.class, () -> livreService.modifierLivre(livre.getIsbn(), livre));
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
    void testSupprimerLivre_LivreNonTrouve() {
        // Given
        when(livreRepository.existsById(livre.getIsbn())).thenReturn(false);

        // When / Then
        assertThrows(EntityNotFoundException.class, () -> livreService.supprimerLivre(livre.getIsbn()));
    }

    @Test
    void testRechercherParIsbn() {
        // Given
        when(livreRepository.findById(livre.getIsbn())).thenReturn(Optional.of(livre));

        // When
        Optional<Livre> result = livreService.rechercherParISBN(livre.getIsbn());

        // Then
        assertTrue(result.isPresent());
        assertEquals("Livre conforme", result.get().getTitre());
    }

    @Test
    void testRechercherParIsbn_LivreNonTrouve() {
        // Given
        when(livreRepository.findById(livre.getIsbn())).thenReturn(Optional.empty());

        // When
        Optional<Livre> result = livreService.rechercherParISBN(livre.getIsbn());

        // Then
        assertFalse(result.isPresent());
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
        assertEquals("Livre conforme", result.get(0).getTitre());
    }

    @Test
    void testRechercherParTitre_Vide() {
        // Given
        when(livreRepository.findByTitreContainingIgnoreCase("Inexistant")).thenReturn(List.of());

        // When
        List<Livre> result = livreService.rechercherParTitre("Inexistant");

        // Then
        assertTrue(result.isEmpty());
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
        assertEquals("Valentin Bedet", result.get(0).getAuteur());
    }

    @Test
    void testRechercherParAuteur_Vide() {
        // Given
        when(livreRepository.findByAuteurContainingIgnoreCase("Nonexistent")).thenReturn(List.of());

        // When
        List<Livre> result = livreService.rechercherParAuteur("Nonexistent");

        // Then
        assertTrue(result.isEmpty());
    }
}

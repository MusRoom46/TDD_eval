package fr.formation.service;

import fr.formation.model.Format;
import fr.formation.repository.LivreRepository;
import fr.formation.model.Livre;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class LivreServiceTest {

    @Mock
    private LivreRepository livreRepository;

    @InjectMocks
    private LivreService livreService;

    @Test
    public void testAjouterLivre_TousChampsRenseignes_Succes() {
        // Given
        Livre livre = new Livre("9783161484100", "Livre conforme", "Valentin Bedet", "Éditeur IIA", Format.BROCHE, true);
        when(livreRepository.save(any(Livre.class))).thenReturn(livre);

        // When
        Livre livreAjoute = livreService.ajouterLivre(livre);

        // Then
        assertNotNull(livreAjoute);
        assertEquals("9783161484100", livreAjoute.getIsbn());
        verify(livreRepository, times(1)).save(livre);
    }

    @Test
    public void testAjouterLivre_SansISBN_Echec() {
        // Given
        Livre livre = new Livre(null, "Livre sans ISBN", "Valentin Bedet", "Éditeur IIA", Format.POCHE, true);

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> livreService.ajouterLivre(livre));
        verify(livreRepository, never()).save(any(Livre.class));
    }

    @Test
    public void testModifierLivre_Existant_Succes() {
        // Given
        String isbn = "9783161484100";
        Livre livreExistant = new Livre(isbn, "Livre conforme", "Valentin Bedet", "Éditeur IIA", Format.BROCHE, true);
        Livre livreModifie = new Livre(isbn, "Livre modifie", "Valentin Bedet", "Éditeur CCI", Format.GRAND_FORMAT, false);

        when(livreRepository.findById(isbn)).thenReturn(Optional.of(livreExistant));
        when(livreRepository.save(any(Livre.class))).thenReturn(livreModifie);

        // When
        Livre result = livreService.modifierLivre(isbn, livreModifie);

        // Then
        assertNotNull(result);
        assertEquals("Livre modifie", result.getTitre());
        verify(livreRepository, times(1)).save(livreModifie);
    }

    @Test
    public void testModifierLivre_Inexistant_Echec() {
        // Given
        String isbn = "9783161484100";
        Livre livreModifie = new Livre(isbn, "Livre modifie", "Valentin Bedet", "Éditeur CCI", Format.GRAND_FORMAT, false);

        when(livreRepository.findById(isbn)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(EntityNotFoundException.class, () -> livreService.modifierLivre(isbn, livreModifie));
        verify(livreRepository, never()).save(any(Livre.class));
    }

    @Test
    public void testSupprimerLivre_Existant_Succes() {
        // Given
        String isbn = "9783161484100";
        when(livreRepository.existsById(isbn)).thenReturn(true);
        doNothing().when(livreRepository).deleteById(isbn);

        // When
        livreService.supprimerLivre(isbn);

        // Then
        verify(livreRepository, times(1)).deleteById(isbn);
    }

    @Test
    public void testSupprimerLivre_Inexistant_Echec() {
        // Given
        String isbn = "9783161484100";
        when(livreRepository.existsById(isbn)).thenReturn(false);

        // When / Then
        assertThrows(EntityNotFoundException.class, () -> livreService.supprimerLivre(isbn));
        verify(livreRepository, never()).deleteById(anyString());
    }

    @Test
    public void testRechercherLivre_ParISBN_Succes() {
        // Given
        String isbn = "9783161484100";
        Livre livre = new Livre(isbn, "Livre conforme", "Valentin Bedet", "Éditeur IIA", Format.BROCHE, true);
        when(livreRepository.findById(isbn)).thenReturn(Optional.of(livre));

        // When
        Livre result = livreService.rechercherParISBN(isbn);

        // Then
        assertNotNull(result);
        assertEquals("Livre conforme", result.getTitre());
    }

    @Test
    public void testRechercherLivre_ParTitre_Succes() {
        // Given
        String titre = "Livre conforme";
        List<Livre> livres = List.of(new Livre("9783161484100", titre, "Valentin Bedet", "Éditeur IIA", Format.BROCHE, true));
        when(livreRepository.findByTitreContainingIgnoreCase(titre)).thenReturn(livres);

        // When
        List<Livre> result = livreService.rechercherParTitre(titre);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}

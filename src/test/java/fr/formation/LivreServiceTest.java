package fr.formation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class LivreServiceTest {

    @Mock
    private LivreRepository livreRepository;

    @InjectMocks
    private LivreService livreService;

    @Test
    public void testAjouterLivre_TousChampsRenseignés_Succès() {
        // Given
        Livre livre = new Livre("9783161484100", "TDD en Java", "Kent Beck", "Éditeur X", Format.BROCHE, true);
        when(livreRepository.save(any(Livre.class))).thenReturn(livre);

        // When
        Livre livreAjouté = livreService.ajouterLivre(livre);

        // Then
        assertNotNull(livreAjouté);
        assertEquals("9783161484100", livreAjouté.getIsbn());
        verify(livreRepository, times(1)).save(livre);
    }

    @Test
    public void testAjouterLivre_SansISBN_Echec() {
        // Given
        Livre livre = new Livre(null, "Livre sans ISBN", "Auteur X", "Éditeur Y", Format.POCHE, true);

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> livreService.ajouterLivre(livre));
        verify(livreRepository, never()).save(any(Livre.class));
    }

    @Test
    public void testModifierLivre_Existant_Succès() {
        // Given
        String isbn = "9783161484100";
        Livre livreExistant = new Livre(isbn, "TDD en Java", "Kent Beck", "Éditeur X", Format.BROCHE, true);
        Livre livreModifié = new Livre(isbn, "TDD et Clean Code", "Kent Beck", "Éditeur Y", Format.GRAND_FORMAT, false);

        when(livreRepository.findById(isbn)).thenReturn(Optional.of(livreExistant));
        when(livreRepository.save(any(Livre.class))).thenReturn(livreModifié);

        // When
        Livre result = livreService.modifierLivre(isbn, livreModifié);

        // Then
        assertNotNull(result);
        assertEquals("TDD et Clean Code", result.getTitre());
        verify(livreRepository, times(1)).save(livreModifié);
    }

    @Test
    public void testModifierLivre_Inexistant_Echec() {
        // Given
        String isbn = "9783161484100";
        Livre livreModifié = new Livre(isbn, "TDD et Clean Code", "Kent Beck", "Éditeur Y", Format.GRAND_FORMAT, false);

        when(livreRepository.findById(isbn)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(EntityNotFoundException.class, () -> livreService.modifierLivre(isbn, livreModifié));
        verify(livreRepository, never()).save(any(Livre.class));
    }

    @Test
    public void testSupprimerLivre_Existant_Succès() {
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
    public void testRechercherLivre_ParISBN_Succès() {
        // Given
        String isbn = "9783161484100";
        Livre livre = new Livre(isbn, "TDD en Java", "Kent Beck", "Éditeur X", Format.BROCHE, true);
        when(livreRepository.findById(isbn)).thenReturn(Optional.of(livre));

        // When
        Livre result = livreService.rechercherParISBN(isbn);

        // Then
        assertNotNull(result);
        assertEquals("TDD en Java", result.getTitre());
    }

    @Test
    public void testRechercherLivre_ParTitre_Succès() {
        // Given
        String titre = "TDD en Java";
        List<Livre> livres = List.of(new Livre("9783161484100", titre, "Kent Beck", "Éditeur X", Format.BROCHE, true));
        when(livreRepository.findByTitreContainingIgnoreCase(titre)).thenReturn(livres);

        // When
        List<Livre> result = livreService.rechercherParTitre(titre);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}

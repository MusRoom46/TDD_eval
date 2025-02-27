package fr.formation.controller;

import fr.formation.exception.LivreNotFoundException;
import fr.formation.model.Livre;
import fr.formation.model.Format;
import fr.formation.service.LivreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class LivreControllerTest {

    @Mock
    private LivreService livreService;

    @InjectMocks
    private LivreController livreController;

    private Livre livre;

    @BeforeEach
    void setUp() {
        livre = new Livre("9783161484100", "Livre conforme", "Valentin Bedet", "Éditeur IIA", Format.BROCHE, true);
    }

    // --- Test d'ajout d'un livre ---
    @Test
    void testAjouterLivre_Valide() {
        when(livreService.ajouterLivre(any(Livre.class))).thenReturn(livre);

        ResponseEntity<Livre> response = livreController.ajouterLivre(livre);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Livre conforme", response.getBody().getTitre());
    }

    @Test
    void testAjouterLivre_Invalid() {
        when(livreService.ajouterLivre(any(Livre.class))).thenThrow(new RuntimeException("Erreur d'ajout"));

        try {
            livreController.ajouterLivre(livre);
            fail("Exception attendue");
        } catch (RuntimeException e) {
            assertEquals("Erreur d'ajout", e.getMessage());
        }
    }

    // --- Test de modification d'un livre ---
    @Test
    void testModifierLivre_Valide() {
        Livre livreModifie = new Livre("9783161484100", "Livre modifié", "Valentin Bedet", "Éditeur IIA", Format.BROCHE, true);
        when(livreService.modifierLivre(eq("9783161484100"), any(Livre.class))).thenReturn(livreModifie);

        ResponseEntity<Livre> response = livreController.modifierLivre("9783161484100", livreModifie);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Livre modifié", response.getBody().getTitre());
    }

    @Test
    void testModifierLivre_LivreInexistant() {
        Livre livreModifie = new Livre("9783161484100", "Livre modifié", "Valentin Bedet", "Éditeur IIA", Format.BROCHE, true);
        when(livreService.modifierLivre(eq("9783161484100"), any(Livre.class)))
                .thenThrow(new LivreNotFoundException("Livre non trouvé"));

        ResponseEntity<Livre> response = livreController.modifierLivre("9783161484100", livreModifie);

        assertEquals(404, response.getStatusCodeValue());
    }

    // --- Test de suppression d'un livre ---
    @Test
    void testSupprimerLivre_LivreExistant() {
        doNothing().when(livreService).supprimerLivre("9783161484100");

        ResponseEntity<Void> response = livreController.supprimerLivre("9783161484100");

        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testSupprimerLivre_LivreInexistant() {
        doThrow(new LivreNotFoundException("Livre non trouvé")).when(livreService).supprimerLivre("9783161484100");

        ResponseEntity<Void> response = livreController.supprimerLivre("9783161484100");

        assertEquals(404, response.getStatusCodeValue());
    }

    // --- Test de recherche par ISBN ---
    @Test
    void testRechercherLivreParIsbn_LivreExistant() {
        when(livreService.rechercherParISBN("9783161484100")).thenReturn(Optional.of(livre));

        ResponseEntity<Livre> response = livreController.rechercherLivreParIsbn("9783161484100");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Livre conforme", response.getBody().getTitre());
    }

    @Test
    void testRechercherLivreParIsbn_LivreInexistant() {
        when(livreService.rechercherParISBN("9783161484100")).thenReturn(Optional.empty());

        ResponseEntity<Livre> response = livreController.rechercherLivreParIsbn("9783161484100");

        assertEquals(404, response.getStatusCodeValue());
    }

    // --- Test de recherche par titre ---
    @Test
    void testRechercherLivreParTitre_Valide() {
        when(livreService.rechercherParTitre("Livre")).thenReturn(List.of(livre));

        ResponseEntity<List<Livre>> response = livreController.rechercherLivreParTitre("Livre");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("Livre conforme", response.getBody().get(0).getTitre());
    }

    @Test
    void testRechercherLivreParTitre_Inexistant() {
        when(livreService.rechercherParTitre("Inexistant")).thenReturn(Collections.emptyList());

        ResponseEntity<List<Livre>> response = livreController.rechercherLivreParTitre("Inexistant");

        assertEquals(204, response.getStatusCodeValue());  // No content
    }

    @Test
    void testRechercherLivreParTitre_AvecErreur() {
        when(livreService.rechercherParTitre("Erreur")).thenThrow(new RuntimeException("Erreur de service"));

        try {
            livreController.rechercherLivreParTitre("Erreur");
            fail("Exception attendue");
        } catch (RuntimeException e) {
            assertEquals("Erreur de service", e.getMessage());
        }
    }

    // --- Test de recherche par auteur ---
    @Test
    void testRechercherLivreParAuteur_Existant() {
        when(livreService.rechercherParAuteur("Valentin")).thenReturn(List.of(livre));

        ResponseEntity<List<Livre>> response = livreController.rechercherLivreParAuteur("Valentin");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("Valentin Bedet", response.getBody().get(0).getAuteur());
    }

    @Test
    void testRechercherLivreParAuteur_Inexistant() {
        when(livreService.rechercherParAuteur("NonExistant")).thenReturn(Collections.emptyList());

        ResponseEntity<List<Livre>> response = livreController.rechercherLivreParAuteur("NonExistant");

        assertEquals(204, response.getStatusCodeValue());  // No content
    }

    @Test
    void testRechercherLivreParAuteur_AvecErreur() {
        when(livreService.rechercherParAuteur("Erreur")).thenThrow(new RuntimeException("Erreur de service"));

        try {
            livreController.rechercherLivreParAuteur("Erreur");
            fail("Exception attendue");
        } catch (RuntimeException e) {
            assertEquals("Erreur de service", e.getMessage());
        }
    }

    // --- Test de gestionnaire d'exception pour LivreNotFoundException ---
    @Test
    void testHandleLivreNotFoundException() {
        LivreNotFoundException exception = new LivreNotFoundException("Livre non trouvé");

        // Appel direct du gestionnaire d'exception
        ResponseEntity<Void> response = livreController.handleLivreNotFound(exception);

        assertEquals(404, response.getStatusCodeValue());
    }
}

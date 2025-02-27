package fr.formation.controller;

import fr.formation.exception.AdherentNotFoundException;
import fr.formation.model.Adherent;
import fr.formation.model.Civilite;
import fr.formation.service.AdherentService;
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
public class AdherentControllerTest {

    @Mock
    private AdherentService adherentService;

    @InjectMocks
    private AdherentController adherentController;

    private Adherent adherent;

    @BeforeEach
    void setUp() {
        adherent = new Adherent("A123", "Bedet", "Valentin", "2003-10-24", Civilite.HOMME, "valentin.bedet@mail.com");
    }

    // --- Test d'ajout d'un adhérent ---
    @Test
    void testAjouterAdherent_Valide() {
        when(adherentService.ajouterAdherent(any(Adherent.class))).thenReturn(adherent);

        ResponseEntity<Adherent> response = adherentController.ajouterAdherent(adherent);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Bedet", response.getBody().getNom());
    }

    // --- Test de modification d'un adhérent ---
    @Test
    void testModifierAdherent_Valide() {
        when(adherentService.modifierAdherent(eq("A123"), any(Adherent.class))).thenReturn(adherent);

        ResponseEntity<Adherent> response = adherentController.modifierAdherent("A123", adherent);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Bedet", response.getBody().getNom());
    }

    @Test
    void testModifierAdherent_Inexistant() {
        when(adherentService.modifierAdherent(eq("A123"), any(Adherent.class)))
                .thenThrow(new AdherentNotFoundException("Adhérent non trouvé"));

        ResponseEntity<Adherent> response = adherentController.modifierAdherent("A123", adherent);

        assertEquals(404, response.getStatusCodeValue());
    }

    // --- Test de suppression d'un adhérent ---
    @Test
    void testSupprimerAdherent_Existant() {
        doNothing().when(adherentService).supprimerAdherent("A123");

        ResponseEntity<Void> response = adherentController.supprimerAdherent("A123");

        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testSupprimerAdherent_Inexistant() {
        doThrow(new AdherentNotFoundException("Adhérent non trouvé")).when(adherentService).supprimerAdherent("A123");

        ResponseEntity<Void> response = adherentController.supprimerAdherent("A123");

        assertEquals(404, response.getStatusCodeValue());
    }

    // --- Test de recherche par code ---
    @Test
    void testRechercherAdherentParCode_Existant() {
        when(adherentService.rechercherParCode("A123")).thenReturn(Optional.of(adherent));

        ResponseEntity<Adherent> response = adherentController.rechercherAdherentParCode("A123");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Bedet", response.getBody().getNom());
    }

    @Test
    void testRechercherAdherentParCode_Inexistant() {
        when(adherentService.rechercherParCode("A123")).thenReturn(Optional.empty());

        ResponseEntity<Adherent> response = adherentController.rechercherAdherentParCode("A123");

        assertEquals(404, response.getStatusCodeValue());
    }

    // --- Test de recherche par nom ---
    @Test
    void testRechercherAdherentParNom_Existant() {
        when(adherentService.rechercherParNom("Bedet")).thenReturn(List.of(adherent));

        ResponseEntity<List<Adherent>> response = adherentController.rechercherAdherentParNom("Bedet");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testRechercherAdherentParNom_Inexistant() {
        when(adherentService.rechercherParNom("Inconnu")).thenReturn(Collections.emptyList());

        ResponseEntity<List<Adherent>> response = adherentController.rechercherAdherentParNom("Inconnu");

        assertEquals(204, response.getStatusCodeValue());
    }

    // --- Test du gestionnaire d'exception ---
    @Test
    void testHandleAdherentNotFoundException() {
        AdherentNotFoundException exception = new AdherentNotFoundException("Adhérent non trouvé");

        ResponseEntity<Void> response = adherentController.handleAdherentNotFound(exception);

        assertEquals(404, response.getStatusCodeValue());
    }
}

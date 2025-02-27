package fr.formation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class MailServiceTest {

    @InjectMocks
    private MailService mailService;

    @Mock
    private EmailSender emailSender; // Mock de l'interface EmailSender

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialise les mocks
    }

    @Test
    void testEnvoyerMail_Success() {
        // Given
        String destinataire = "test@example.com";
        String sujet = "Test Email";
        String contenu = "Ceci est un test d'email.";

        // When
        mailService.envoyerMail(destinataire, sujet, contenu);

        // Then
        // Vérifie que la méthode send a été appelée avec les bons paramètres
        verify(emailSender).send(destinataire, sujet, contenu);
    }
}

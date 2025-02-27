package fr.formation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    public void envoyerMail(String destinataire, String sujet, String contenu) {
        logger.info("Simulation d'envoi d'e-mail");
        logger.info("À : {}", destinataire);
        logger.info("Objet : {}", sujet);
        logger.info("Contenu :\n{}", contenu);
    }
}

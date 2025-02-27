package fr.formation.service;

import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final EmailSender emailSender;

    public MailService(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void envoyerMail(String destinataire, String sujet, String contenu) {
        emailSender.send(destinataire, sujet, contenu);
    }
}
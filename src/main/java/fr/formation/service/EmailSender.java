package fr.formation.service;

import org.springframework.stereotype.Service;

@Service
public interface EmailSender {
    void send(String destinataire, String sujet, String contenu);
}
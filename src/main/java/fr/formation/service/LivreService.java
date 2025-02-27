package fr.formation.service;

import fr.formation.model.Livre;
import fr.formation.repository.LivreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LivreService {

    private final LivreRepository livreRepository;

    public LivreService(LivreRepository livreRepository) {
        this.livreRepository = livreRepository;
    }

    public Livre ajouterLivre(Livre livre) {
        return livreRepository.save(livre);
    }

    public Livre modifierLivre(String isbn, Livre livre) {
        if (!livreRepository.existsById(isbn)) {
            throw new EntityNotFoundException("Livre introuvable");
        }
        livre.setIsbn(isbn);
        return livreRepository.save(livre);
    }

    public void supprimerLivre(String isbn) {
        if (!livreRepository.existsById(isbn)) {
            throw new EntityNotFoundException("Livre introuvable");
        }
        livreRepository.deleteById(isbn);
    }

    public Optional<Livre> rechercherParISBN(String isbn) {
        return livreRepository.findById(isbn);
    }

    public List<Livre> rechercherParTitre(String titre) {
        return livreRepository.findByTitreContainingIgnoreCase(titre);
    }

    public List<Livre> rechercherParAuteur(String auteur) {
        return livreRepository.findByAuteurContainingIgnoreCase(auteur);
    }
}

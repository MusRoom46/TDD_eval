package fr.formation.service;

import fr.formation.model.Livre;
import fr.formation.repository.LivreRepository;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityNotFoundException;

@Service
public class LivreService {

    @Autowired
    private LivreRepository livreRepository;

    public Livre ajouterLivre(Livre livre) {
        if (livre.getIsbn() == null || livre.getIsbn().isEmpty()) {
            throw new IllegalArgumentException("L'ISBN est obligatoire.");
        }
        return livreRepository.save(livre);
    }

    public Livre modifierLivre(String isbn, Livre livreModifié) {
        Livre livreExistant = livreRepository.findById(isbn)
                .orElseThrow(() -> new EntityNotFoundException("Livre non trouvé avec ISBN : " + isbn));
        livreModifié.setIsbn(isbn);
        return livreRepository.save(livreModifié);
    }

    public void supprimerLivre(String isbn) {
        if (!livreRepository.existsById(isbn)) {
            throw new EntityNotFoundException("Livre non trouvé avec ISBN : " + isbn);
        }
        livreRepository.deleteById(isbn);
    }

    public Livre rechercherParISBN(String isbn) {
        return livreRepository.findById(isbn)
                .orElseThrow(() -> new EntityNotFoundException("Livre non trouvé avec ISBN : " + isbn));
    }

    public List<Livre> rechercherParTitre(String titre) {
        return livreRepository.findByTitreContainingIgnoreCase(titre);
    }
}

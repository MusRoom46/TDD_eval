package fr.formation.controller;

import fr.formation.exception.LivreNotFoundException;
import fr.formation.model.Livre;
import fr.formation.service.LivreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/livres")
public class LivreController {

    private final LivreService livreService;

    public LivreController(LivreService livreService) {
        this.livreService = livreService;
    }

    @PostMapping
    public ResponseEntity<Livre> ajouterLivre(@RequestBody Livre livre) {
        Livre nouveauLivre = livreService.ajouterLivre(livre);
        return ResponseEntity.status(HttpStatus.CREATED).body(nouveauLivre);
    }

    @PutMapping("/{isbn}")
    public ResponseEntity<Livre> modifierLivre(@PathVariable String isbn, @RequestBody Livre livre) {
        try {
            Livre livreModifie = livreService.modifierLivre(isbn, livre);
            return ResponseEntity.ok(livreModifie);
        } catch (LivreNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> supprimerLivre(@PathVariable String isbn) {
        try {
            livreService.supprimerLivre(isbn);
            return ResponseEntity.noContent().build();
        } catch (LivreNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<Livre> rechercherLivreParIsbn(@PathVariable String isbn) {
        Optional<Livre> livre = livreService.rechercherParISBN(isbn);
        return livre.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/recherche/titre")
    public ResponseEntity<List<Livre>> rechercherLivreParTitre(@RequestParam String titre) {
        List<Livre> livres = livreService.rechercherParTitre(titre);
        return livres.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(livres);
    }

    @GetMapping("/recherche/auteur")
    public ResponseEntity<List<Livre>> rechercherLivreParAuteur(@RequestParam String auteur) {
        List<Livre> livres = livreService.rechercherParAuteur(auteur);
        return livres.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(livres);
    }

    // Exception Handler global
    @ExceptionHandler(LivreNotFoundException.class)
    public ResponseEntity<Void> handleLivreNotFound(LivreNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}


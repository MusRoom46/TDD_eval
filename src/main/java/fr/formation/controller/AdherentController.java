package fr.formation.controller;

import fr.formation.exception.AdherentNotFoundException;
import fr.formation.model.Adherent;
import fr.formation.service.AdherentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/adherents")
public class AdherentController {

    private final AdherentService adherentService;

    public AdherentController(AdherentService adherentService) {
        this.adherentService = adherentService;
    }

    @PostMapping
    public ResponseEntity<Adherent> ajouterAdherent(@RequestBody Adherent adherent) {
        Adherent nouvelAdherent = adherentService.ajouterAdherent(adherent);
        return ResponseEntity.status(HttpStatus.CREATED).body(nouvelAdherent);
    }

    @PutMapping("/{codeAdherent}")
    public ResponseEntity<Adherent> modifierAdherent(@PathVariable String codeAdherent, @RequestBody Adherent adherent) {
        try {
            Adherent adherentModifie = adherentService.modifierAdherent(codeAdherent, adherent);
            return ResponseEntity.ok(adherentModifie);
        } catch (AdherentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{codeAdherent}")
    public ResponseEntity<Void> supprimerAdherent(@PathVariable String codeAdherent) {
        try {
            adherentService.supprimerAdherent(codeAdherent);
            return ResponseEntity.noContent().build();
        } catch (AdherentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{codeAdherent}")
    public ResponseEntity<Adherent> rechercherAdherentParCode(@PathVariable String codeAdherent) {
        Optional<Adherent> adherent = adherentService.rechercherParCode(codeAdherent);
        return adherent.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/recherche/nom")
    public ResponseEntity<List<Adherent>> rechercherAdherentParNom(@RequestParam String nom) {
        List<Adherent> adherents = adherentService.rechercherParNom(nom);
        return adherents.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(adherents);
    }

    // Gestionnaire d'exceptions global
    @ExceptionHandler(AdherentNotFoundException.class)
    public ResponseEntity<Void> handleAdherentNotFound(AdherentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}

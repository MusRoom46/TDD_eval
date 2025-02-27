package fr.formation.service;

import fr.formation.model.Adherent;
import fr.formation.repository.AdherentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@Service
public class AdherentService {

    @Autowired
    private AdherentRepository adherentRepository;

    public Adherent ajouterAdherent(Adherent adherent) {
        return adherentRepository.save(adherent);
    }

    public Adherent modifierAdherent(String code, Adherent adherentModifie) {
        Adherent adherent = adherentRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException("Adhérent non trouvé"));
        adherent.setNom(adherentModifie.getNom());
        adherent.setPrenom(adherentModifie.getPrenom());
        adherent.setDateNaissance(adherentModifie.getDateNaissance());
        adherent.setAdresseMail(adherentModifie.getAdresseMail());
        return adherentRepository.save(adherent);
    }

    public void supprimerAdherent(String code) {
        if (!adherentRepository.existsById(code)) {
            throw new EntityNotFoundException("Adhérent non trouvé");
        }
        adherentRepository.deleteById(code);
    }

    public Optional<Adherent> rechercherParCode(String code) {
        return adherentRepository.findById(code);
    }

    public List<Adherent> rechercherParNom(String nom) {
        return adherentRepository.findByNomContainingIgnoreCase(nom);
    }
}

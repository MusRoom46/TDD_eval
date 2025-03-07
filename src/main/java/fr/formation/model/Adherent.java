package fr.formation.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Adherent {

    @Id
    private String codeAdherent;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String dateNaissance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Civilite civilite;

    @Column(nullable = false)
    private String adresseMail;

}

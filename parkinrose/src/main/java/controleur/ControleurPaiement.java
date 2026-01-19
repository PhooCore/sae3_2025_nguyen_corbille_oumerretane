package controleur;

import ihm.Page_Paiement;
import ihm.Page_Principale;
import modele.Paiement;
import modele.Stationnement;
import modele.Usager;
import modele.dao.PaiementDAO;
import modele.dao.StationnementDAO;
import modele.dao.UsagerDAO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.YearMonth;

public class ControleurPaiement implements ActionListener {
    
    // États du contrôleur
    private enum Etat {
        INITIAL,
        SAISIE_INFORMATIONS,
        VALIDATION_FORMULAIRE,
        TRAITEMENT_PAIEMENT,
        PAIEMENT_REUSSI,
        PAIEMENT_GRATUIT,
        ANNULATION_DEMANDEE,
        REDIRECTION,
        ERREUR
    }
    
    // Références
    private Page_Paiement vue;
    private Etat etat;
    
    // Données
    private String emailUtilisateur;
    private Usager usager;
    private Paiement paiement;
    
    // Constantes
    private static final int LONGUEUR_NUMERO_CARTE = 16;
    private static final int LONGUEUR_CVV = 3;
    
    public ControleurPaiement(Page_Paiement vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.etat = Etat.INITIAL;
        
        initialiserControleur();
    }
    
    private void initialiserControleur() {
        try {
            chargerUtilisateur();
            configurerListeners();
            etat = Etat.SAISIE_INFORMATIONS;
        } catch (Exception e) {
            gererErreurInitialisation(e.getMessage());
        }
    }
    
    private void chargerUtilisateur() throws Exception {
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager == null) {
            throw new Exception("Utilisateur non trouvé");
        }
    }
    
    private void configurerListeners() {
        vue.getBtnAnnuler().addActionListener(this);
        vue.getBtnPayer().addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        switch (etat) {
            case SAISIE_INFORMATIONS:
                if (source == vue.getBtnAnnuler()) {
                    annulerPaiement();
                } else if (source == vue.getBtnPayer()) {
                    validerEtPayer();
                }
                break;
                
            case VALIDATION_FORMULAIRE:
            case TRAITEMENT_PAIEMENT:
                // En cours de traitement
                break;
                
            case PAIEMENT_REUSSI:
            case PAIEMENT_GRATUIT:
                // Le succès est géré dans les méthodes spécifiques
                break;
                
            case ERREUR:
                // En état d'erreur
                if (source == vue.getBtnAnnuler()) {
                    retourPagePrincipale();
                }
                break;
        }
    }
    
    private void validerEtPayer() {
        etat = Etat.VALIDATION_FORMULAIRE;
        
        if (!validerFormulaire()) {
            etat = Etat.SAISIE_INFORMATIONS;
            return;
        }
        
        etat = Etat.TRAITEMENT_PAIEMENT;
        effectuerPaiement();
    }
    
    private boolean validerFormulaire() {
        return validerTitulaireCarte() 
            && validerNumeroCarte() 
            && validerDateExpiration() 
            && validerCVV();
    }
    
    private boolean validerTitulaireCarte() {
        String nomCarte = vue.getTxtNomCarte().getText().trim();
        
        if (nomCarte.isEmpty()) {
            afficherMessageErreur("Veuillez saisir le nom du titulaire de la carte", "Champ manquant");
            return false;
        }
        
        return true;
    }
    
    private boolean validerNumeroCarte() {
        String numeroCarte = vue.getTxtNumeroCarte().getText().trim().replaceAll("\\s+", "");
        
        if (numeroCarte.isEmpty()) {
            afficherMessageErreur("Veuillez saisir le numéro de carte", "Champ manquant");
            return false;
        }
        
        if (!numeroCarte.matches("\\d{" + LONGUEUR_NUMERO_CARTE + "}")) {
            afficherMessageErreur(
                String.format("Numéro de carte invalide\n%d chiffres requis", LONGUEUR_NUMERO_CARTE),
                "Numéro de carte incorrect"
            );
            return false;
        }
        
        return true;
    }
    
    private boolean validerDateExpiration() {
        String dateExpiration = vue.getTxtDateExpiration().getText().trim();
        
        if (dateExpiration.isEmpty()) {
            afficherMessageErreur("Veuillez saisir la date d'expiration", "Champ manquant");
            return false;
        }
        
        if (!dateExpiration.matches("\\d{2}/\\d{2}")) {
            afficherMessageErreur("Format de date invalide\nUtilisez MM/AA (ex: 12/25)", "Date invalide");
            return false;
        }
        
        if (!estCarteValide(dateExpiration)) {
            afficherMessageErreur("La carte est expirée", "Carte expirée");
            return false;
        }
        
        return true;
    }
    
    private boolean validerCVV() {
        String cvv = vue.getTxtCVV().getText().trim();
        
        if (cvv.isEmpty()) {
            afficherMessageErreur("Veuillez saisir le CVV", "Champ manquant");
            return false;
        }
        
        if (!cvv.matches("\\d{" + LONGUEUR_CVV + "}")) {
            afficherMessageErreur(
                String.format("CVV invalide\n%d chiffres requis", LONGUEUR_CVV),
                "CVV incorrect"
            );
            return false;
        }
        
        return true;
    }
    
    private boolean estCarteValide(String dateExpiration) {
        try {
            String[] parties = dateExpiration.split("/");
            int mois = Integer.parseInt(parties[0]);
            int annee = Integer.parseInt(parties[1]) + 2000;
            
            YearMonth expiration = YearMonth.of(annee, mois);
            YearMonth maintenant = YearMonth.now();
            
            return !expiration.isBefore(maintenant);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void effectuerPaiement() {
        try {
            // Vérifier si c'est un paiement pour parking gratuit
            if (vue.getIdStationnement() != null && vue.getMontant() == 0.0) {
                etat = Etat.PAIEMENT_GRATUIT;
                traiterParkingGratuit();
                return;
            }
            
            // Créer l'objet paiement
            this.paiement = new Paiement(
                vue.getTxtNomCarte().getText().trim(),
                nettoyerNumeroCarte(vue.getTxtNumeroCarte().getText().trim()),
                vue.getTxtCVV().getText().trim(),
                vue.getMontant(),
                usager.getIdUsager()
            );
            
            // Traiter selon le type
            boolean succes;
            if (vue.getIdStationnement() == null) {
                succes = traiterPaiementVoirie();
            } else {
                succes = traiterPaiementParking();
            }
            
            if (succes) {
                etat = Etat.PAIEMENT_REUSSI;
                afficherConfirmationPaiement();
                retourPagePrincipale();
            } else {
                gererErreur("Erreur lors du traitement du paiement");
            }
            
        } catch (Exception e) {
            gererErreur("Erreur paiement: " + e.getMessage());
        }
    }
    
    private boolean traiterPaiementVoirie() {
        try {
            // Enregistrer le paiement
            PaiementDAO paiementDAO = PaiementDAO.getInstance();
            paiementDAO.create(paiement);

            // Créer le stationnement en voirie
            Stationnement stationnement = new Stationnement(
                usager.getIdUsager(),
                vue.getTypeVehicule(),
                vue.getPlaqueImmatriculation(),
                vue.getIdZone(),
                vue.getNomZone(),
                vue.getDureeHeures(),
                vue.getDureeMinutes(),
                vue.getMontant(),
                paiement.getIdPaiement()
            );
            
            stationnement.setTypeStationnement("VOIRIE");
            stationnement.setStatutPaiement("PAYE");
            
            StationnementDAO stationnementDAO = StationnementDAO.getInstance();
            stationnementDAO.creerStationnementVoirie(stationnement);
            
            return true;
        } catch (SQLException e) {
            afficherMessageErreur("Erreur création stationnement: " + e.getMessage(), "Erreur système");
            return false;
        }
    }
    
    private boolean traiterPaiementParking() {
        try {
            // Enregistrer le paiement
            PaiementDAO paiementDAO = PaiementDAO.getInstance();
            paiementDAO.create(paiement);

            // Terminer le stationnement parking
            StationnementDAO stationnementDAO = StationnementDAO.getInstance();
            boolean stationnementTermine = stationnementDAO.terminerStationnementParking(
                vue.getIdStationnement(),
                LocalDateTime.now(),
                vue.getMontant(),
                paiement.getIdPaiement()
            );
            
            return stationnementTermine;
            
        } catch (SQLException e) {
            afficherMessageErreur("Erreur traitement paiement parking: " + e.getMessage(), "Erreur système");
            return false;
        }
    }
    
    private void traiterParkingGratuit() throws SQLException {
        StationnementDAO stationnementDAO = StationnementDAO.getInstance();
		boolean stationnementTermine = stationnementDAO.terminerStationnementParking(
		    vue.getIdStationnement(),
		    LocalDateTime.now(),
		    0.0,
		    null
		);
		
		if (stationnementTermine) {
		    afficherConfirmationParkingGratuit();
		    retourPagePrincipale();
		} else {
		    gererErreur("Erreur lors du traitement du stationnement gratuit");
		}
    }
    
    private void afficherConfirmationPaiement() {
        String message;
        
        if (vue.getIdStationnement() == null) {
            // Paiement voirie
            message = String.format(
                "<html><div style='text-align: center;'>"
                + "<h2 style='color: green;'>Paiement effectué !</h2>"
                + "<p>Votre stationnement en voirie est maintenant actif.</p>"
                + "<br>"
                + "<div style='background-color: #f0f8ff; padding: 15px; border-radius: 5px; text-align: left;'>"
                + "<p><b>Zone:</b> %s</p>"
                + "<p><b>Véhicule:</b> %s - %s</p>"
                + "<p><b>Durée:</b> %dh%02dmin</p>"
                + "<p><b>Montant:</b> %.2f €</p>"
                + "</div>"
                + "<p style='color: #666;'>N'oubliez pas de valider la fin de votre stationnement.</p>"
                + "</div></html>",
                vue.getNomZone(),
                vue.getTypeVehicule(),
                vue.getPlaqueImmatriculation(),
                vue.getDureeHeures(),
                vue.getDureeMinutes(),
                vue.getMontant()
            );
        } else {
            // Paiement parking
            message = String.format(
                "<html><div style='text-align: center;'>"
                + "<h2 style='color: green;'>Paiement effectué !</h2>"
                + "<p>Votre stationnement en parking est maintenant terminé.</p>"
                + "<br>"
                + "<div style='background-color: #f0f8ff; padding: 15px; border-radius: 5px; text-align: left;'>"
                + "<p><b>Parking:</b> %s</p>"
                + "<p><b>Véhicule:</b> %s - %s</p>"
                + "<p><b>Montant:</b> %.2f €</p>"
                + "</div>"
                + "<p style='color: #666;'>Vous pouvez maintenant quitter le parking.</p>"
                + "</div></html>",
                vue.getNomZone(),
                vue.getTypeVehicule(),
                vue.getPlaqueImmatriculation(),
                vue.getMontant()
            );
        }
        
        JOptionPane.showMessageDialog(
            vue,
            message,
            "Paiement réussi",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void afficherConfirmationParkingGratuit() {
        String message = String.format(
            "<html><div style='text-align: center;'>"
            + "<h2 style='color: green;'>Stationnement terminé !</h2>"
            + "<p>Votre stationnement en parking est maintenant terminé.</p>"
            + "<br>"
            + "<div style='background-color: #f0f8ff; padding: 15px; border-radius: 5px; text-align: left;'>"
            + "<p><b>Parking:</b> %s</p>"
            + "<p><b>Véhicule:</b> %s - %s</p>"
            + "<p><b>Montant:</b> GRATUIT (15 premières minutes)</p>"
            + "</div>"
            + "<p style='color: #666;'>Vous pouvez maintenant quitter le parking.</p>"
            + "</div></html>",
            vue.getNomZone(),
            vue.getTypeVehicule(),
            vue.getPlaqueImmatriculation()
        );
        
        JOptionPane.showMessageDialog(
            vue,
            message,
            "Stationnement terminé",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void annulerPaiement() {
        etat = Etat.ANNULATION_DEMANDEE;
        
        int confirmation = JOptionPane.showConfirmDialog(
            vue,
            "Êtes-vous sûr de vouloir annuler le paiement ?\nLe stationnement ne sera pas créé.",
            "Confirmation d'annulation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmation == JOptionPane.YES_OPTION) {
            retourPagePrincipale();
        } else {
            etat = Etat.SAISIE_INFORMATIONS;
        }
    }
    
    private void retourPagePrincipale() {
        try {
            etat = Etat.REDIRECTION;
            Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
            pagePrincipale.setVisible(true);
            vue.dispose();
        } catch (Exception e) {
            gererErreur("Erreur redirection: " + e.getMessage());
        }
    }
    
    private String nettoyerNumeroCarte(String numeroCarte) {
        return numeroCarte.replaceAll("\\s+", "");
    }
    
    private void afficherMessageErreur(String message, String titre) {
        JOptionPane.showMessageDialog(vue, message, titre, JOptionPane.ERROR_MESSAGE);
    }
    
    private void gererErreur(String message) {
        System.err.println(message);
        afficherMessageErreur(message, "Erreur");
        etat = Etat.ERREUR;
    }
    
    private void gererErreurInitialisation(String message) {
        System.err.println("Erreur initialisation: " + message);
        afficherMessageErreur("Erreur d'initialisation: " + message, "Erreur");
        vue.dispose();
    }
    
    // Getters pour débogage
    public Etat getEtat() {
        return etat;
    }
}
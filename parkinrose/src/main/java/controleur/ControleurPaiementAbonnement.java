package controleur;

import ihm.Page_Paiement_Abonnement;
import ihm.Page_Abonnements;
import ihm.Page_Utilisateur;
import modele.Abonnement;
import modele.Paiement;
import modele.Usager;
import modele.dao.PaiementDAO;
import modele.dao.UsagerDAO;
import modele.dao.AbonnementDAO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.YearMonth;
import java.util.UUID;

public class ControleurPaiementAbonnement implements ActionListener {
    
    // États du contrôleur
    private enum Etat {
        INITIAL,
        SAISIE_INFORMATIONS,
        VALIDATION_FORMULAIRE,
        TRAITEMENT_PAIEMENT,
        ACTIVATION_ABONNEMENT,
        CONFIRMATION_SUCCES,
        ANNULATION_DEMANDEE,
        REDIRECTION,
        ERREUR
    }
    
    // Références
    private Page_Paiement_Abonnement vue;
    private Etat etat;
    
    // Données
    private String emailUtilisateur;
    private Usager usager;
    private Abonnement abonnement;
    private Paiement paiement;
    
    // Constantes
    private static final int LONGUEUR_NUMERO_CARTE = 16;
    private static final int LONGUEUR_CVV = 3;
    
    public ControleurPaiementAbonnement(Page_Paiement_Abonnement vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.etat = Etat.INITIAL;
        
        initialiserControleur();
    }
    
    private void initialiserControleur() {
        try {
            chargerUtilisateur();
            chargerAbonnement();
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
    
    private void chargerAbonnement() throws Exception {
        this.abonnement = vue.getAbonnement();
        if (abonnement == null) {
            throw new Exception("Abonnement non trouvé");
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
            case ACTIVATION_ABONNEMENT:
                // En cours de traitement
                break;
                
            case CONFIRMATION_SUCCES:
                // Le succès est géré dans afficherConfirmationPaiement()
                break;
                
            case ERREUR:
                // En état d'erreur
                if (source == vue.getBtnAnnuler()) {
                    retourPageAbonnements();
                }
                break;
        }
    }
    
    private void validerEtPayer() {
        etat = Etat.VALIDATION_FORMULAIRE;
        
        if (!validerFormulaire()) {
            etat = Etat.ERREUR;
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
        String nomCarte = vue.getTxtTitulaire().getText().trim();
        
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
        String dateExpiration = vue.getTxtExpiration().getText().trim();
        
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
        String cvv = vue.getTxtCrypto().getText().trim();
        
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
            // Créer le paiement
            this.paiement = creerPaiement();
            
            // Enregistrer le paiement
            boolean paiementReussi = PaiementDAO.getInstance().enregistrerPaiement(paiement);
            
            if (!paiementReussi) {
                gererErreur("Erreur lors de l'enregistrement du paiement");
                return;
            }
            
            etat = Etat.ACTIVATION_ABONNEMENT;
            activerAbonnement();
            
        } catch (Exception e) {
            gererErreur("Erreur paiement: " + e.getMessage());
        }
    }
    
    private Paiement creerPaiement() {
        Paiement paiement = new Paiement();
        
        paiement.setIdPaiement(genererIdPaiement());
        paiement.setNomCarte(vue.getTxtTitulaire().getText().trim());
        paiement.setNumeroCarte(vue.getTxtNumeroCarte().getText().trim().replaceAll("\\s+", ""));
        paiement.setCodeSecretCarte(vue.getTxtCrypto().getText().trim());
        paiement.setMontant(abonnement.getTarifAbonnement());
        paiement.setIdUsager(usager.getIdUsager());
        paiement.setIdAbonnement(abonnement.getIdAbonnement());
        paiement.setTypePaiement("ABONNEMENT");
        paiement.setStatut("REUSSI");
        
        return paiement;
    }
    
    private String genererIdPaiement() {
        return "PAY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private void activerAbonnement() {
        try {
            boolean abonnementActive = AbonnementDAO.getInstance().ajouterAbonnementUtilisateur(
                usager.getIdUsager(), 
                abonnement.getIdAbonnement()
            );
            
            if (abonnementActive) {
                etat = Etat.CONFIRMATION_SUCCES;
                afficherConfirmationPaiement();
                retourPageUtilisateur();
            } else {
                gererErreur("Paiement effectué mais erreur lors de l'activation de l'abonnement");
            }
            
        } catch (Exception e) {
            gererErreur("Erreur activation abonnement: " + e.getMessage());
        }
    }
    
    private void afficherConfirmationPaiement() {
        String message = String.format(
            "<html><div style='text-align: center;'>"
            + "<h2 style='color: green;'>Abonnement activé !</h2>"
            + "<p>Votre abonnement <b>%s</b> a été activé avec succès.</p>"
            + "<br>"
            + "<div style='background-color: #f0f8ff; padding: 15px; border-radius: 5px; text-align: left;'>"
            + "<p><b>Date d'activation :</b> %s</p>"
            + "<p><b>Prix :</b> %.2f €</p>"
            + "</div>"
            + "</div></html>",
            abonnement.getLibelleAbonnement(),
            java.time.LocalDate.now(),
            abonnement.getTarifAbonnement()
        );
        
        JOptionPane.showMessageDialog(
            vue,
            message,
            "Confirmation d'abonnement",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void annulerPaiement() {
        etat = Etat.ANNULATION_DEMANDEE;
        
        int confirmation = JOptionPane.showConfirmDialog(
            vue,
            "Êtes-vous sûr de vouloir annuler ?\nVotre abonnement ne sera pas activé.",
            "Confirmation d'annulation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmation == JOptionPane.YES_OPTION) {
            retourPageAbonnements();
        } else {
            etat = Etat.SAISIE_INFORMATIONS;
        }
    }
    
    private void retourPageUtilisateur() {
        try {
            etat = Etat.REDIRECTION;
            Page_Utilisateur pageUtilisateur = new Page_Utilisateur(emailUtilisateur, true);
            pageUtilisateur.setVisible(true);
            fermerVues();
        } catch (Exception e) {
            gererErreur("Erreur redirection: " + e.getMessage());
        }
    }
    
    private void retourPageAbonnements() {
        try {
            etat = Etat.REDIRECTION;
            Page_Abonnements pageAbonnements = new Page_Abonnements(emailUtilisateur);
            pageAbonnements.setVisible(true);
            vue.dispose();
        } catch (Exception e) {
            gererErreur("Erreur redirection: " + e.getMessage());
        }
    }
    
    private void fermerVues() {
        vue.dispose();
        
        if (vue.getParentFrame() != null) {
            vue.getParentFrame().dispose();
        }
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
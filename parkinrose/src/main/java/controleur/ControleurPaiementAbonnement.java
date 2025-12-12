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
    
    private enum EtatPaiementAbonnement {
        INITIAL,
        SAISIE,
        VALIDATION_EN_COURS,
        VALIDATION_OK,
        VALIDATION_ERREUR,
        TRAITEMENT_EN_COURS,
        TRAITEMENT_REUSSI,
        TRAITEMENT_ERREUR,
        CONFIRMATION,
        REDIRECTION_EN_COURS,
        ANNULATION_EN_COURS
    }
    
    private Page_Paiement_Abonnement vue;
    private EtatPaiementAbonnement etat;
    private String emailUtilisateur;
    private Usager usager;
    private Abonnement abonnement;
    
    public ControleurPaiementAbonnement(Page_Paiement_Abonnement vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        this.abonnement = vue.getAbonnement();
        this.etat = EtatPaiementAbonnement.INITIAL;
        
        configurerListeners();
        etat = EtatPaiementAbonnement.SAISIE;
    }
    
    private void configurerListeners() {
        if (vue.getBtnAnnuler() != null) {
            vue.getBtnAnnuler().addActionListener(this);
        }
        
        if (vue.getBtnPayer() != null) {
            vue.getBtnPayer().addActionListener(this);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        String action = "INCONNU";
        
        if (source instanceof JButton) {
            JButton btn = (JButton) source;
            String actionCommand = btn.getActionCommand();
            
            if (actionCommand != null) {
                if (actionCommand.equals("ANNULER")) {
                    action = "ANNULER";
                } else if (actionCommand.equals("PAYER")) {
                    action = "PAYER";
                }
            }
        }

        
        if (!estActionValide(etat, action)) {
            return;
        }
        
        switch (action) {
            case "ANNULER":
                etat = EtatPaiementAbonnement.ANNULATION_EN_COURS;
                annulerPaiement();
                break;
            case "PAYER":
                etat = EtatPaiementAbonnement.VALIDATION_EN_COURS;
                validerEtTraiterPaiement();
                break;
        }
    }
    
    private boolean estActionValide(EtatPaiementAbonnement etatActuel, String action) {
        switch (etatActuel) {
            case SAISIE:
            case VALIDATION_ERREUR:
            case TRAITEMENT_ERREUR:
                return action.equals("ANNULER") || action.equals("PAYER");
                
            case ANNULATION_EN_COURS:
            case VALIDATION_EN_COURS:
            case TRAITEMENT_EN_COURS:
            case REDIRECTION_EN_COURS:
                return false;
                
            default:
                return false;
        }
    }
    
    private void validerEtTraiterPaiement() {
        
        if (validerFormulaire()) {
            etat = EtatPaiementAbonnement.VALIDATION_OK;
            etat = EtatPaiementAbonnement.TRAITEMENT_EN_COURS;
            traiterPaiementAbonnement();
        } else {
            etat = EtatPaiementAbonnement.VALIDATION_ERREUR;
            etat = EtatPaiementAbonnement.SAISIE;
        }
    }
    
    private boolean validerFormulaire() {
        
        String nomCarte = vue.getTxtTitulaire().getText().trim();
        String numeroCarte = vue.getTxtNumeroCarte().getText().trim();
        String dateExpiration = vue.getTxtExpiration().getText().trim();
        String cvv = vue.getTxtCrypto().getText().trim();
        
        if (nomCarte.isEmpty()) {
            JOptionPane.showMessageDialog(vue,
                "Veuillez saisir le nom du titulaire de la carte",
                "Champ manquant",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        String numeroCarteNettoye = numeroCarte.replaceAll("\\s+", "");
        if (numeroCarteNettoye.isEmpty() || !numeroCarteNettoye.matches("\\d{16}")) {
            JOptionPane.showMessageDialog(vue,
                "Numéro de carte invalide\n16 chiffres requis",
                "Numéro de carte incorrect",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (dateExpiration.isEmpty()) {
            JOptionPane.showMessageDialog(vue,
                "Veuillez saisir la date d'expiration",
                "Champ manquant",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        String cvvNettoye = cvv.trim();
        if (cvvNettoye.isEmpty() || !cvvNettoye.matches("\\d{3}")) {
            JOptionPane.showMessageDialog(vue,
                "CVV invalide\n3 chiffres requis",
                "CVV incorrect",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!validerFormatDateExpiration(dateExpiration)) {
            JOptionPane.showMessageDialog(vue,
                "Format de date invalide\nUtilisez MM/AA (ex: 12/25)",
                "Date invalide",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!estCarteNonExpiree(dateExpiration)) {
            JOptionPane.showMessageDialog(vue,
                "La carte est expirée",
                "Carte expirée",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
    
    private boolean validerFormatDateExpiration(String dateExpiration) {
        return dateExpiration.matches("\\d{2}/\\d{2}");
    }
    
    private boolean estCarteNonExpiree(String dateExpiration) {
        try {
            String[] parties = dateExpiration.split("/");
            int mois = Integer.parseInt(parties[0]);
            int annee = Integer.parseInt(parties[1]);
            
            if (annee < 100) {
                annee += 2000;
            }
            
            YearMonth expiration = YearMonth.of(annee, mois);
            YearMonth maintenant = YearMonth.now();
            
            return !expiration.isBefore(maintenant);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private void traiterPaiementAbonnement() {

        
        try {
            String nomCarte = vue.getTxtTitulaire().getText().trim();
            String numeroCarte = vue.getTxtNumeroCarte().getText().trim();
            String dateExpiration = vue.getTxtExpiration().getText().trim();
            String cvv = vue.getTxtCrypto().getText().trim();
            double montant = abonnement.getTarifAbonnement();
            
            // Créer l'objet paiement
            Paiement paiement = new Paiement();
            paiement.setIdPaiement("PAY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            paiement.setNomCarte(nomCarte);
            paiement.setNumeroCarte(numeroCarte.replaceAll("\\s+", ""));
            paiement.setCodeSecretCarte(cvv);
            paiement.setMontant(montant);
            paiement.setIdUsager(usager.getIdUsager());
            paiement.setIdAbonnement(abonnement.getIdAbonnement());
            paiement.setTypePaiement("ABONNEMENT");
            paiement.setStatut("REUSSI");
            
            boolean paiementEnregistre = PaiementDAO.enregistrerPaiement(paiement);
            
            if (paiementEnregistre) {
                
                // Ajouter l'abonnement à l'utilisateur
                boolean abonnementAjoute = AbonnementDAO.ajouterAbonnementUtilisateur(
                    usager.getIdUsager(), 
                    abonnement.getIdAbonnement()
                );
                
                if (abonnementAjoute) {
                    etat = EtatPaiementAbonnement.TRAITEMENT_REUSSI;
                    etat = EtatPaiementAbonnement.CONFIRMATION;
                    afficherConfirmation();
                    etat = EtatPaiementAbonnement.REDIRECTION_EN_COURS;
                    redirigerVersUtilisateur();
                } else {
                    JOptionPane.showMessageDialog(vue,
                        "Paiement effectué mais erreur lors de l'activation de l'abonnement.",
                        "Erreur d'activation",
                        JOptionPane.ERROR_MESSAGE);
                    etat = EtatPaiementAbonnement.TRAITEMENT_ERREUR;
                    etat = EtatPaiementAbonnement.SAISIE;
                }
            } else {
                JOptionPane.showMessageDialog(vue,
                    "Erreur lors de l'enregistrement du paiement",
                    "Erreur système",
                    JOptionPane.ERROR_MESSAGE);
                etat = EtatPaiementAbonnement.TRAITEMENT_ERREUR;
                etat = EtatPaiementAbonnement.SAISIE;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(vue,
                "Erreur lors du traitement: " + e.getMessage(),
                "Erreur système",
                JOptionPane.ERROR_MESSAGE);
            etat = EtatPaiementAbonnement.TRAITEMENT_ERREUR;
            etat = EtatPaiementAbonnement.SAISIE;
        }
    }
    
    private void afficherConfirmation() {
        String message = "<html><div style='text-align: center;'>"
                + "<h2 style='color: green;'>✅ Abonnement activé !</h2>"
                + "<p>Votre abonnement <b>" + abonnement.getLibelleAbonnement() + "</b> a été activé avec succès.</p>"
                + "<br>"
                + "<div style='background-color: #f0f8ff; padding: 15px; border-radius: 5px; text-align: left;'>"
                + "<p><b>📅 Date d'activation :</b> " + java.time.LocalDate.now() + "</p>"
                + "<p><b>💰 Prix :</b> " + String.format("%.2f €", abonnement.getTarifAbonnement()) + "</p>"
                + "</div>"
                + "</div></html>";
        
        JOptionPane.showMessageDialog(vue,
            message,
            "Confirmation d'abonnement",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void redirigerVersUtilisateur() {
        Page_Utilisateur pageUtilisateur = new Page_Utilisateur(emailUtilisateur, true);
        pageUtilisateur.setVisible(true);
        vue.dispose();
        
        if (vue.getParentFrame() != null) {
            vue.getParentFrame().dispose();
        }
    }
    
    private void annulerPaiement() {
        int confirmation = JOptionPane.showConfirmDialog(vue,
            "<html><div style='text-align: center;'>"
            + "<p>Êtes-vous sûr de vouloir annuler ?</p>"
            + "<p>Votre abonnement ne sera pas activé.</p>"
            + "</div></html>",
            "Confirmation d'annulation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            etat = EtatPaiementAbonnement.REDIRECTION_EN_COURS;
            redirigerVersAbonnements();
        } else {
            etat = EtatPaiementAbonnement.SAISIE;
        }
    }
    
    private void redirigerVersAbonnements() {
        Page_Abonnements pageAbonnements = new Page_Abonnements(emailUtilisateur);
        pageAbonnements.setVisible(true);
        vue.dispose();
    }
    
    public EtatPaiementAbonnement getEtat() {
        return etat;
    }
    
    public String getEtatString() {
        return etat.toString();
    }
}
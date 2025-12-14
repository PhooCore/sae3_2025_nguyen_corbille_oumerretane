package controleur;

import modele.Paiement;
import modele.Stationnement;
import modele.Usager;
import modele.dao.PaiementDAO;
import modele.dao.StationnementDAO;
import modele.dao.UsagerDAO;
import ihm.Page_Paiement;
import ihm.Page_Principale;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.YearMonth;

public class ControleurPaiement implements ActionListener {
    
    // État pour le pattern State
    private enum EtatPaiement {
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
    
    private Page_Paiement vue;
    private EtatPaiement etat;
    private String emailUtilisateur;
    private Usager usager;
    private Paiement paiementEnCours;
    
    /**
     * Constructeur du contrôleur de paiement
     */
    public ControleurPaiement(Page_Paiement vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.emailUtilisateur;
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        this.etat = EtatPaiement.INITIAL;
        
        configurerListeners();
        etat = EtatPaiement.SAISIE;

    }
    
    private void configurerListeners() {
        // Configurer directement avec les boutons publics de la vue
        if (vue.btnAnnuler != null) {
            vue.btnAnnuler.addActionListener(this);
        }
        
        if (vue.btnPayer != null) {
            vue.btnPayer.addActionListener(this);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        String action = "INCONNU";
        
        // Identifier l'action par comparaison directe des sources
        if (source == vue.btnAnnuler) {
            action = "ANNULER";
        } else if (source == vue.btnPayer) {
            action = "PAYER";
        }
        
        
        // Vérifier si l'action est valide dans l'état courant
        if (!estActionValide(etat, action)) {
            return;
        }
        
        switch (action) {
            case "ANNULER":
                etat = EtatPaiement.ANNULATION_EN_COURS;
                annulerPaiement();
                break;
            case "PAYER":
                etat = EtatPaiement.VALIDATION_EN_COURS;
                validerEtTraiterPaiement();
                break;
        }
    }
    
    private boolean estActionValide(EtatPaiement etatActuel, String action) {
        switch (etatActuel) {
            case SAISIE:
            case VALIDATION_ERREUR:
            case TRAITEMENT_ERREUR:
                return action.equals("ANNULER") || action.equals("PAYER");
                
            case ANNULATION_EN_COURS:
            case VALIDATION_EN_COURS:
            case TRAITEMENT_EN_COURS:
            case REDIRECTION_EN_COURS:
                return false; // Actions non valides pendant les transitions
                
            default:
                return false;
        }
    }
    
    private void validerEtTraiterPaiement() {

        
        // 1. Validation du formulaire
        if (validerFormulairePaiement()) {
            etat = EtatPaiement.VALIDATION_OK;
            etat = EtatPaiement.TRAITEMENT_EN_COURS;
            traiterPaiementSelonType();
        } else {
            etat = EtatPaiement.VALIDATION_ERREUR;
            etat = EtatPaiement.SAISIE;
        }
    }
    
    private boolean validerFormulairePaiement() {
        
        // Récupérer les valeurs des champs
        String nomCarte = vue.txtNomCarte.getText().trim();
        String numeroCarte = vue.txtNumeroCarte.getText().trim();
        String dateExpiration = vue.txtDateExpiration.getText().trim();
        String cvv = vue.txtCVV.getText().trim();
        
        // Validation des champs obligatoires
        if (nomCarte.isEmpty()) {
            JOptionPane.showMessageDialog(vue,
                "Veuillez saisir le nom sur la carte",
                "Champ manquant",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        String numeroCarteNettoye = numeroCarte.replaceAll("\\s+", "");
        if (numeroCarteNettoye.isEmpty() || !numeroCarteNettoye.matches("\\d{16}")) {
            JOptionPane.showMessageDialog(vue,
                "Numéro de carte invalide (16 chiffres requis)",
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
        if (cvvNettoye.isEmpty() || !cvvNettoye.matches("\\d{3,4}")) {
            JOptionPane.showMessageDialog(vue,
                "CVV invalide (3 ou 4 chiffres requis)",
                "CVV incorrect",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validation de la date d'expiration
        if (!validerFormatDateExpiration(dateExpiration)) {
            JOptionPane.showMessageDialog(vue,
                "Format de date invalide. Utilisez MM/AA (ex: 12/25)",
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
        if (!dateExpiration.matches("\\d{2}/\\d{2,4}")) {
            return false;
        }
        
        try {
            String[] parties = dateExpiration.split("/");
            int mois = Integer.parseInt(parties[0]);
            int annee = Integer.parseInt(parties[1]);
            
            return mois >= 1 && mois <= 12 && annee >= 0;
            
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean estCarteNonExpiree(String dateExpiration) {
        try {
            String[] parties = dateExpiration.split("/");
            int mois = Integer.parseInt(parties[0]);
            int annee = Integer.parseInt(parties[1]);
            
            // Si l'année est sur 2 chiffres, ajouter 2000
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
    
    private void traiterPaiementSelonType() {
        
        try {
            // Créer l'objet Paiement
            paiementEnCours = new Paiement(
                vue.txtNomCarte.getText().trim(),
                nettoyerNumeroCarte(vue.txtNumeroCarte.getText().trim()),
                vue.txtCVV.getText().trim(),
                vue.montant,
                usager.getIdUsager()
            );
            
            boolean succes = false;
            
            if (vue.idStationnement == null) {
                // Paiement voirie
                succes = traiterPaiementVoirie();
            } else {
                // Paiement parking
                succes = traiterPaiementParking();
            }
            
            if (succes) {
                etat = EtatPaiement.TRAITEMENT_REUSSI;
                etat = EtatPaiement.CONFIRMATION;
                afficherConfirmation();
                etat = EtatPaiement.REDIRECTION_EN_COURS;
                retourAccueil();
            } else {
                etat = EtatPaiement.TRAITEMENT_ERREUR;
                etat = EtatPaiement.SAISIE;
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du paiement:");
            e.printStackTrace();
            JOptionPane.showMessageDialog(vue,
                "Erreur lors du traitement du paiement: " + e.getMessage(),
                "Erreur système",
                JOptionPane.ERROR_MESSAGE);
            etat = EtatPaiement.TRAITEMENT_ERREUR;
            etat = EtatPaiement.SAISIE;
        }
    }
    
    private boolean traiterPaiementVoirie() {
        
        // 1. Enregistrer le paiement
        boolean paiementEnregistre = PaiementDAO.enregistrerPaiement(paiementEnCours);
        if (!paiementEnregistre) {
            JOptionPane.showMessageDialog(vue,
                "Erreur lors de l'enregistrement du paiement",
                "Erreur système",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        
        // 2. Créer le stationnement en voirie
        Stationnement stationnement = new Stationnement(
            usager.getIdUsager(),
            vue.typeVehicule,
            vue.plaqueImmatriculation,
            vue.idZone,
            vue.nomZone, // Utilisation correcte du nom de la zone
            vue.dureeHeures,
            vue.dureeMinutes,
            vue.montant,
            paiementEnCours.getIdPaiement()
        );
        
        boolean stationnementCree = StationnementDAO.creerStationnementVoirie(stationnement);
        
        if (!stationnementCree) {
            JOptionPane.showMessageDialog(vue,
                "Erreur lors de la création du stationnement",
                "Erreur système",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private boolean traiterPaiementParking() {
        
        // Vérifier si c'est un stationnement gratuit
        Stationnement stationnement = StationnementDAO.getStationnementById(vue.idStationnement);
        if (stationnement != null && "GRATUIT".equals(stationnement.getStatutPaiement())) {
            // Si gratuit, pas besoin de paiement
            boolean stationnementTermine = StationnementDAO.terminerStationnementParking(
                vue.idStationnement,
                LocalDateTime.now(),
                0.0,
                null
            );
            
            if (stationnementTermine) {
                afficherConfirmationParkingGratuit();
                return true;
            }
            return false;
        }
        
        // Paiement normal pour parking
        // 1. Enregistrer le paiement
        boolean paiementEnregistre = PaiementDAO.enregistrerPaiement(paiementEnCours);
        if (!paiementEnregistre) {
            JOptionPane.showMessageDialog(vue,
                "Erreur lors de l'enregistrement du paiement",
                "Erreur système",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        
        // 2. Terminer le stationnement parking
        LocalDateTime heureDepart = LocalDateTime.now();
        
        boolean stationnementTermine = StationnementDAO.terminerStationnementParking(
            vue.idStationnement,
            heureDepart,
            vue.montant,
            paiementEnCours.getIdPaiement()
        );
        
        if (!stationnementTermine) {
            JOptionPane.showMessageDialog(vue,
                "Erreur lors de la mise à jour du stationnement",
                "Erreur système",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void afficherConfirmation() {
        String message;
        
        if (vue.idStationnement == null) {
            message = "<html><div style='text-align: center;'>"
                    + "<h2 style='color: green;'>✅ Paiement effectué avec succès !</h2>"
                    + "<p>Votre stationnement en voirie est maintenant actif.</p>"
                    + "<div style='background-color: #f0f8ff; padding: 15px; border-radius: 5px; text-align: left; margin: 15px;'>"
                    + "<p><b>📍 Zone:</b> " + vue.nomZone + "</p>"
                    + "<p><b>🚗 Véhicule:</b> " + vue.typeVehicule + " - " + vue.plaqueImmatriculation + "</p>"
                    + "<p><b>⏱️ Durée:</b> " + vue.dureeHeures + "h" + vue.dureeMinutes + "min</p>"
                    + "<p><b>💰 Montant:</b> " + String.format("%.2f", vue.montant) + " €</p>"
                    + "</div>"
                    + "<p style='color: #666;'>N'oubliez pas de valider la fin de votre stationnement.</p>"
                    + "</div></html>";
        } else {
            message = "<html><div style='text-align: center;'>"
                    + "<h2 style='color: green;'>✅ Paiement effectué avec succès !</h2>"
                    + "<p>Votre stationnement en parking est maintenant terminé.</p>"
                    + "<div style='background-color: #f0f8ff; padding: 15px; border-radius: 5px; text-align: left; margin: 15px;'>"
                    + "<p><b>📍 Parking:</b> " + vue.nomZone + "</p>"
                    + "<p><b>🚗 Véhicule:</b> " + vue.typeVehicule + " - " + vue.plaqueImmatriculation + "</p>"
                    + "<p><b>💰 Montant:</b> " + String.format("%.2f", vue.montant) + " €</p>"
                    + "</div>"
                    + "<p style='color: #666;'>Vous pouvez maintenant quitter le parking.</p>"
                    + "</div></html>";
        }
        
        JOptionPane.showMessageDialog(vue,
            message,
            "Paiement réussi",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void afficherConfirmationParkingGratuit() {
        String message = "<html><div style='text-align: center;'>"
                + "<h2 style='color: green;'>✅ Stationnement terminé !</h2>"
                + "<p>Votre stationnement en parking est maintenant terminé.</p>"
                + "<div style='background-color: #f0f8ff; padding: 15px; border-radius: 5px; text-align: left; margin: 15px;'>"
                + "<p><b>📍 Parking:</b> " + vue.nomZone + "</p>"
                + "<p><b>🚗 Véhicule:</b> " + vue.typeVehicule + " - " + vue.plaqueImmatriculation + "</p>"
                + "<p><b>💰 Montant:</b> GRATUIT (15 premières minutes)</p>"
                + "</div>"
                + "<p style='color: #666;'>Vous pouvez maintenant quitter le parking.</p>"
                + "</div></html>";
        
        JOptionPane.showMessageDialog(vue,
            message,
            "Stationnement terminé",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void retourAccueil() {

        Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
        pagePrincipale.setVisible(true);
        vue.dispose();
    }
    
    private void annulerPaiement() {
        int confirmation = JOptionPane.showConfirmDialog(vue,
            "<html><div style='text-align: center;'>"
            + "<p>Êtes-vous sûr de vouloir annuler le paiement ?</p>"
            + "<p>Le stationnement ne sera pas créé.</p>"
            + "</div></html>",
            "Confirmation d'annulation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (confirmation == JOptionPane.YES_OPTION) {
            etat = EtatPaiement.REDIRECTION_EN_COURS;
            retourAccueil();
        } else {
            etat = EtatPaiement.SAISIE;
        }
    }
    
    private String nettoyerNumeroCarte(String numeroCarte) {
        return numeroCarte.replaceAll("\\s+", "");
    }
    
    // Méthodes utilitaires
    public EtatPaiement getEtat() {
        return etat;
    }
    
    public String getEtatString() {
        return etat.toString();
    }
}
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

/**
 * Contrôleur gérant l'interface de paiement pour les stationnements.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Paiement
 * et le modèle (Paiement, Stationnement, Usager).
 * Gère à la fois les paiements pour les stationnements en voirie et en parking,
 * ainsi que les cas de stationnements gratuits.
 * 
 * @author Équipe 7
 */
public class ControleurPaiement implements ActionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur.
     * Permet de suivre le cycle de vie du processus de paiement et de gérer les transitions.
     */
    private enum Etat {
        /** État initial au démarrage du contrôleur */
        INITIAL,
        /** L'utilisateur saisit les informations de paiement */
        SAISIE_INFORMATIONS,
        /** Validation du formulaire de paiement en cours */
        VALIDATION_FORMULAIRE,
        /** Traitement du paiement en cours */
        TRAITEMENT_PAIEMENT,
        /** Le paiement a été effectué avec succès */
        PAIEMENT_REUSSI,
        /** Traitement d'un stationnement gratuit (parking < 15min) */
        PAIEMENT_GRATUIT,
        /** L'utilisateur a demandé l'annulation */
        ANNULATION_DEMANDEE,
        /** Redirection vers une autre page en cours */
        REDIRECTION,
        /** Une erreur s'est produite */
        ERREUR
    }
    
    private Page_Paiement vue;
    private Etat etat;
    private String emailUtilisateur;
    private Usager usager;
    private Paiement paiement;
    
    private static final int LONGUEUR_NUMERO_CARTE = 16;
    private static final int LONGUEUR_CVV = 3;
    
    /**
     * Constructeur du contrôleur de paiement.
     * Initialise le contrôleur avec la vue associée et déclenche le chargement des données.
     * 
     * @param vue la page d'interface graphique de paiement
     */
    public ControleurPaiement(Page_Paiement vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.etat = Etat.INITIAL;
        
        initialiserControleur();
    }
    
    /**
     * Initialise le contrôleur en chargeant l'utilisateur et en configurant les écouteurs.
     * En cas d'erreur, affiche un message et gère l'erreur d'initialisation.
     */
    private void initialiserControleur() {
        try {
            chargerUtilisateur();
            configurerListeners();
            etat = Etat.SAISIE_INFORMATIONS;
        } catch (Exception e) {
            gererErreurInitialisation(e.getMessage());
        }
    }
    
    /**
     * Charge l'utilisateur depuis la base de données à partir de son email.
     * 
     * @throws Exception si l'utilisateur n'est pas trouvé
     */
    private void chargerUtilisateur() throws Exception {
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager == null) {
            throw new Exception("Utilisateur non trouvé");
        }
    }
    
    /**
     * Configure les écouteurs d'événements pour les boutons de la vue.
     */
    private void configurerListeners() {
        vue.getBtnAnnuler().addActionListener(this);
        vue.getBtnPayer().addActionListener(this);
    }
    
    /**
     * Gère les événements d'action en fonction de l'état courant du contrôleur.
     * Dispatche les actions vers les méthodes appropriées selon l'état.
     * 
     * @param e l'événement d'action
     */
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
                break;
                
            case PAIEMENT_REUSSI:
            case PAIEMENT_GRATUIT:
                break;
                
            case ERREUR:
                if (source == vue.getBtnAnnuler()) {
                    retourPagePrincipale();
                }
                break;
        }
    }
    
    /**
     * Valide le formulaire puis effectue le paiement si la validation réussit.
     * Change l'état du contrôleur en conséquence.
     */
    private void validerEtPayer() {
        etat = Etat.VALIDATION_FORMULAIRE;
        
        if (!validerFormulaire()) {
            etat = Etat.SAISIE_INFORMATIONS;
            return;
        }
        
        etat = Etat.TRAITEMENT_PAIEMENT;
        effectuerPaiement();
    }
    
    /**
     * Valide l'ensemble du formulaire de paiement.
     * Vérifie tous les champs : titulaire, numéro de carte, date d'expiration et CVV.
     * 
     * @return true si tous les champs sont valides, false sinon
     */
    private boolean validerFormulaire() {
        return validerTitulaireCarte() 
            && validerNumeroCarte() 
            && validerDateExpiration() 
            && validerCVV();
    }
    
    /**
     * Valide le nom du titulaire de la carte.
     * Vérifie que le champ n'est pas vide.
     * 
     * @return true si le titulaire est valide, false sinon
     */
    private boolean validerTitulaireCarte() {
        String nomCarte = vue.getTxtNomCarte().getText().trim();
        
        if (nomCarte.isEmpty()) {
            afficherMessageErreur("Veuillez saisir le nom du titulaire de la carte", "Champ manquant");
            return false;
        }
        
        return true;
    }
    
    /**
     * Valide le numéro de carte bancaire.
     * Vérifie que le numéro contient exactement 16 chiffres.
     * 
     * @return true si le numéro de carte est valide, false sinon
     */
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
    
    /**
     * Valide la date d'expiration de la carte.
     * Vérifie le format MM/AA et que la carte n'est pas expirée.
     * 
     * @return true si la date d'expiration est valide, false sinon
     */
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
    
    /**
     * Valide le code CVV de la carte.
     * Vérifie que le CVV contient exactement 3 chiffres.
     * 
     * @return true si le CVV est valide, false sinon
     */
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
    
    /**
     * Vérifie si la carte bancaire est encore valide à la date actuelle.
     * Compare la date d'expiration avec le mois et l'année courants.
     * 
     * @param dateExpiration la date d'expiration au format MM/AA
     * @return true si la carte est valide, false sinon
     */
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
    
    /**
     * Effectue le traitement du paiement.
     * Distingue entre les paiements gratuits (parking < 15min) et les paiements normaux.
     * Traite différemment les stationnements en voirie et en parking.
     */
    private void effectuerPaiement() {
        try {
            if (vue.getIdStationnement() != null && vue.getMontant() == 0.0) {
                etat = Etat.PAIEMENT_GRATUIT;
                traiterParkingGratuit();
                return;
            }
            
            this.paiement = new Paiement(
                vue.getTxtNomCarte().getText().trim(),
                nettoyerNumeroCarte(vue.getTxtNumeroCarte().getText().trim()),
                vue.getTxtCVV().getText().trim(),
                vue.getMontant(),
                usager.getIdUsager()
            );
            
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
    
    /**
     * Traite un paiement pour un stationnement en voirie.
     * Enregistre le paiement puis crée le stationnement associé.
     * 
     * @return true si le traitement a réussi, false sinon
     */
    private boolean traiterPaiementVoirie() {
        try {
            PaiementDAO paiementDAO = PaiementDAO.getInstance();
            paiementDAO.create(paiement);

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
    
    /**
     * Traite un paiement pour un stationnement en parking.
     * Enregistre le paiement puis termine le stationnement parking existant.
     * 
     * @return true si le traitement a réussi, false sinon
     */
    private boolean traiterPaiementParking() {
        try {
            PaiementDAO paiementDAO = PaiementDAO.getInstance();
            paiementDAO.create(paiement);

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
    
    /**
     * Traite un stationnement parking gratuit (moins de 15 minutes).
     * Termine le stationnement sans créer de paiement.
     * 
     * @throws SQLException si une erreur de base de données survient
     */
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
    
    /**
     * Affiche une boîte de dialogue de confirmation après un paiement réussi.
     * Le message affiché dépend du type de stationnement (voirie ou parking).
     */
    private void afficherConfirmationPaiement() {
        String message;
        
        if (vue.getIdStationnement() == null) {
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
    
    /**
     * Affiche une boîte de dialogue de confirmation pour un parking gratuit.
     * Informe l'utilisateur que son stationnement de moins de 15 minutes est gratuit.
     */
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
    
    /**
     * Gère l'annulation du paiement demandée par l'utilisateur.
     * Affiche une confirmation avant de retourner à la page principale.
     */
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
    
    /**
     * Retourne à la page principale de l'application.
     * Ferme la page de paiement et ouvre la page principale.
     */
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
    
    /**
     * Nettoie un numéro de carte en supprimant tous les espaces.
     * 
     * @param numeroCarte le numéro de carte à nettoyer
     * @return le numéro de carte sans espaces
     */
    private String nettoyerNumeroCarte(String numeroCarte) {
        return numeroCarte.replaceAll("\\s+", "");
    }
    
    /**
     * Affiche un message d'erreur dans une boîte de dialogue.
     * 
     * @param message le message d'erreur à afficher
     * @param titre le titre de la boîte de dialogue
     */
    private void afficherMessageErreur(String message, String titre) {
        JOptionPane.showMessageDialog(vue, message, titre, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Gère une erreur générique pendant le processus de paiement.
     * Affiche le message d'erreur et change l'état en ERREUR.
     * 
     * @param message le message d'erreur
     */
    private void gererErreur(String message) {
        System.err.println(message);
        afficherMessageErreur(message, "Erreur");
        etat = Etat.ERREUR;
    }
    
    /**
     * Gère une erreur survenue lors de l'initialisation du contrôleur.
     * Affiche le message d'erreur et ferme la fenêtre.
     * 
     * @param message le message d'erreur
     */
    private void gererErreurInitialisation(String message) {
        System.err.println("Erreur initialisation: " + message);
        afficherMessageErreur("Erreur d'initialisation: " + message, "Erreur");
        vue.dispose();
    }
    
    /**
     * Retourne l'état actuel du contrôleur.
     * 
     * @return l'état actuel
     */
    public Etat getEtat() {
        return etat;
    }
}
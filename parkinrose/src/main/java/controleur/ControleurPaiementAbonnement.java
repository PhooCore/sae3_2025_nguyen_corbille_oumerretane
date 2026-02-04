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

/**
 * Contrôleur gérant l'interface de paiement pour les abonnements.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Paiement_Abonnement
 * et le modèle (Paiement, Abonnement, Usager).
 * Gère le processus complet de paiement et d'activation d'un abonnement.
 * 
 * @author Équipe 7
 */
public class ControleurPaiementAbonnement implements ActionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur.
     * Permet de suivre le cycle de vie du processus de paiement d'abonnement.
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
        /** Activation de l'abonnement après paiement réussi */
        ACTIVATION_ABONNEMENT,
        /** Confirmation et affichage du succès */
        CONFIRMATION_SUCCES,
        /** L'utilisateur a demandé l'annulation */
        ANNULATION_DEMANDEE,
        /** Redirection vers une autre page en cours */
        REDIRECTION,
        /** Une erreur s'est produite */
        ERREUR
    }
    
    private Page_Paiement_Abonnement vue;
    private Etat etat;
    private String emailUtilisateur;
    private Usager usager;
    private Abonnement abonnement;
    private Paiement paiement;
    
    private static final int LONGUEUR_NUMERO_CARTE = 16;
    private static final int LONGUEUR_CVV = 3;
    
    /**
     * Constructeur du contrôleur de paiement d'abonnement.
     * Initialise le contrôleur avec la vue associée et déclenche le chargement des données.
     * 
     * @param vue la page d'interface graphique de paiement d'abonnement
     */
    public ControleurPaiementAbonnement(Page_Paiement_Abonnement vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.etat = Etat.INITIAL;
        
        initialiserControleur();
    }
    
    /**
     * Initialise le contrôleur en chargeant l'utilisateur, l'abonnement et en configurant les écouteurs.
     * En cas d'erreur, affiche un message et gère l'erreur d'initialisation.
     */
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
     * Charge l'abonnement depuis la vue.
     * 
     * @throws Exception si l'abonnement n'est pas trouvé
     */
    private void chargerAbonnement() throws Exception {
        this.abonnement = vue.getAbonnement();
        if (abonnement == null) {
            throw new Exception("Abonnement non trouvé");
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
            case ACTIVATION_ABONNEMENT:
                break;
                
            case CONFIRMATION_SUCCES:
                break;
                
            case ERREUR:
                if (source == vue.getBtnAnnuler()) {
                    retourPageAbonnements();
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
            etat = Etat.ERREUR;
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
        String nomCarte = vue.getTxtTitulaire().getText().trim();
        
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
    
    /**
     * Valide le code CVV de la carte.
     * Vérifie que le CVV contient exactement 3 chiffres.
     * 
     * @return true si le CVV est valide, false sinon
     */
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
     * Effectue le traitement du paiement de l'abonnement.
     * Crée l'objet paiement, l'enregistre en base de données puis active l'abonnement.
     */
    private void effectuerPaiement() {
        try {
            this.paiement = creerPaiement();
            
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
    
    /**
     * Crée un objet Paiement à partir des informations saisies dans le formulaire.
     * Génère automatiquement un identifiant unique pour le paiement.
     * 
     * @return l'objet Paiement créé
     */
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
    
    /**
     * Génère un identifiant unique pour le paiement.
     * Format : PAY_XXXXXXXX où X est un caractère alphanumérique.
     * 
     * @return l'identifiant généré
     */
    private String genererIdPaiement() {
        return "PAY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Active l'abonnement pour l'utilisateur après un paiement réussi.
     * Ajoute l'abonnement à l'utilisateur en base de données et affiche la confirmation.
     */
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
    
    /**
     * Affiche une boîte de dialogue de confirmation après l'activation de l'abonnement.
     * Indique le nom de l'abonnement, la date d'activation et le prix payé.
     */
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
    
    /**
     * Gère l'annulation du paiement demandée par l'utilisateur.
     * Affiche une confirmation avant de retourner à la page des abonnements.
     */
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
    
    /**
     * Retourne à la page utilisateur après un paiement réussi.
     * Ferme toutes les fenêtres liées au processus de paiement.
     */
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
    
    /**
     * Retourne à la page des abonnements en cas d'annulation.
     * Ferme la page de paiement actuelle.
     */
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
    
    /**
     * Ferme la vue actuelle et la fenêtre parente si elle existe.
     * Utilisé lors du retour à la page utilisateur.
     */
    private void fermerVues() {
        vue.dispose();
        
        if (vue.getParentFrame() != null) {
            vue.getParentFrame().dispose();
        }
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
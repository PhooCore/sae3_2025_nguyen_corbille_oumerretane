package controleur;

import modele.Usager;
import modele.dao.UsagerDAO;
import ihm.Page_Inscription;
import ihm.Page_Authentification;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Contrôleur gérant le processus d'inscription des nouveaux utilisateurs.
 * Valide les données saisies (nom, prénom, email, mot de passe), vérifie que l'email
 * n'est pas déjà utilisé, crée le compte dans la base de données et redirige vers
 * la page d'authentification en cas de succès.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Inscription
 * et le modèle (Usager, UsagerDAO).
 * 
 * @author Équipe 7
 */
public class ControleurInscription implements ActionListener {
    
    /**
     * Énumération des différents états possibles du processus d'inscription.
     * Permet de suivre le cycle de vie de la création d'un compte utilisateur.
     */
    private enum Etat {
        /** L'utilisateur est en train de saisir ses informations */
        SAISIE,
        /** Validation des champs du formulaire en cours */
        VALIDATION_CHAMPS,
        /** Vérification de la disponibilité de l'email en cours */
        VERIFICATION_EMAIL,
        /** Création du compte dans la base de données en cours */
        CREATION_COMPTE,
        /** Le compte a été créé avec succès */
        REUSSITE,
        /** Retour à la page d'authentification */
        RETOUR,
        /** Une erreur s'est produite */
        ERREUR
    }
    
    private Page_Inscription vue;
    private Etat etat;
    
    private static final String TITRE_ERREUR = "Erreur";
    private static final String TITRE_SUCCES = "Succès";
    
    /**
     * Constructeur du contrôleur d'inscription.
     * Initialise le contrôleur avec la vue associée et configure les écouteurs.
     * 
     * @param vue la page d'interface graphique d'inscription
     */
    public ControleurInscription(Page_Inscription vue) {
        this.vue = vue;
        this.etat = Etat.SAISIE;
        
        initialiserControleur();
    }
    
    /**
     * Initialise le contrôleur en configurant les écouteurs d'événements.
     */
    private void initialiserControleur() {
        configurerListeners();
    }
    
    /**
     * Configure tous les écouteurs d'événements pour les composants interactifs de la vue.
     * Connecte les boutons et les champs de texte pour permettre la validation par Entrée.
     */
    private void configurerListeners() {
        vue.getBtnRetour().addActionListener(this);
        vue.getBtnCreerCompte().addActionListener(this);
        
        vue.getTextFieldNom().addActionListener(this);
        vue.getTextFieldPrenom().addActionListener(this);
        vue.getTextFieldEmail().addActionListener(this);
        vue.getPasswordField().addActionListener(this);
        vue.getPasswordFieldConfirm().addActionListener(this);
    }
    
    /**
     * Gère les événements d'action des composants de la vue.
     * Route les actions vers les méthodes appropriées en fonction de l'état actuel
     * et de la source de l'événement.
     * 
     * @param e l'événement d'action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        switch (etat) {
            case SAISIE:
                if (source == vue.getBtnRetour()) {
                    retourAuthentification();
                } else if (source == vue.getBtnCreerCompte() || 
                           source == vue.getTextFieldNom() ||
                           source == vue.getTextFieldPrenom() ||
                           source == vue.getTextFieldEmail() ||
                           source == vue.getPasswordField() ||
                           source == vue.getPasswordFieldConfirm()) {
                    lancerCreationCompte();
                }
                break;
                
            case VALIDATION_CHAMPS:
            case VERIFICATION_EMAIL:
            case CREATION_COMPTE:
                break;
                
            case REUSSITE:
                break;
                
            case ERREUR:
                if (source == vue.getBtnRetour()) {
                    retourAuthentification();
                }
                break;
        }
    }
    
    /**
     * Lance le processus complet de création de compte utilisateur.
     * Enchaîne les étapes de validation des champs, vérification de l'email
     * et création effective du compte si toutes les validations passent.
     */
    private void lancerCreationCompte() {
        etat = Etat.VALIDATION_CHAMPS;
        
        String nom = vue.getTextFieldNom().getText().trim();
        String prenom = vue.getTextFieldPrenom().getText().trim();
        String email = vue.getTextFieldEmail().getText().trim();
        String motDePasse = new String(vue.getPasswordField().getPassword());
        String confirmation = new String(vue.getPasswordFieldConfirm().getPassword());
        
        if (!validerChamps(nom, prenom, email, motDePasse, confirmation)) {
            etat = Etat.SAISIE;
            return;
        }
        
        etat = Etat.VERIFICATION_EMAIL;
        if (!verifierEmail(email)) {
            etat = Etat.SAISIE;
            return;
        }
        
        etat = Etat.CREATION_COMPTE;
        creerCompteUtilisateur(nom, prenom, email, motDePasse);
    }
    
    /**
     * Valide tous les champs du formulaire d'inscription.
     * Vérifie que tous les champs sont remplis, que les mots de passe correspondent,
     * que le mot de passe a une longueur minimale de 4 caractères et que l'email
     * a un format valide.
     * 
     * @param nom le nom de l'utilisateur
     * @param prenom le prénom de l'utilisateur
     * @param email l'adresse email
     * @param motDePasse le mot de passe saisi
     * @param confirmation la confirmation du mot de passe
     * @return true si tous les champs sont valides, false sinon avec affichage d'erreur
     */
    private boolean validerChamps(String nom, String prenom, String email, 
                                 String motDePasse, String confirmation) {
        
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || 
            motDePasse.isEmpty() || confirmation.isEmpty()) {
            
            afficherErreur("Veuillez remplir tous les champs");
            return false;
        }
        
        if (!motDePasse.equals(confirmation)) {
            afficherErreur("Les mots de passe ne correspondent pas");
            reinitialiserMotsDePasse();
            return false;
        }
        
        if (motDePasse.length() < 4) {
            afficherErreur("Le mot de passe doit contenir au moins 4 caractères");
            reinitialiserMotsDePasse();
            return false;
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex)) {
            afficherErreur("Veuillez saisir un email valide");
            vue.getTextFieldEmail().requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Vérifie que l'adresse email n'est pas déjà utilisée par un autre compte.
     * 
     * @param email l'adresse email à vérifier
     * @return true si l'email est disponible, false s'il est déjà utilisé
     */
    private boolean verifierEmail(String email) {
        if (UsagerDAO.emailExisteDeja(email)) {
            afficherErreur("Cet email est déjà utilisé");
            vue.getTextFieldEmail().requestFocus();
            vue.getTextFieldEmail().selectAll();
            return false;
        }
        return true;
    }
    
    /**
     * Crée un nouveau compte utilisateur dans la base de données avec les informations fournies.
     * Le compte est créé sans droits d'administration par défaut.
     * Affiche un message de succès ou d'erreur selon le résultat.
     * 
     * @param nom le nom de l'utilisateur
     * @param prenom le prénom de l'utilisateur
     * @param email l'adresse email
     * @param motDePasse le mot de passe
     */
    private void creerCompteUtilisateur(String nom, String prenom, String email, String motDePasse) {
        try {
            Usager nouvelUsager = new Usager(nom, prenom, email, motDePasse);
            nouvelUsager.setAdmin(false);
            
            boolean succes = UsagerDAO.ajouterUsager(nouvelUsager);
            
            if (succes) {
                etat = Etat.REUSSITE;
                afficherSucces();
            } else {
                afficherErreur("Erreur lors de la création du compte");
                etat = Etat.ERREUR;
            }
            
        } catch (Exception e) {
            gererErreur("Erreur création compte", e.getMessage());
        }
    }
    
    /**
     * Affiche un message de succès après la création du compte et propose
     * de retourner à la page de connexion ou de créer un autre compte.
     */
    private void afficherSucces() {
        JOptionPane.showMessageDialog(vue, 
            "Compte créé avec succès !", 
            TITRE_SUCCES, 
            JOptionPane.INFORMATION_MESSAGE);

        int choix = JOptionPane.showConfirmDialog(vue,
            "Voulez-vous retourner à la page de connexion ?",
            "Retour à l'authentification",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (choix == JOptionPane.YES_OPTION) {
            retourAuthentification();
        } else {
            reinitialiserFormulaire();
            etat = Etat.SAISIE;
        }
    }
    
    /**
     * Réinitialise tous les champs du formulaire d'inscription et place
     * le focus sur le champ du nom.
     */
    private void reinitialiserFormulaire() {
        vue.getTextFieldNom().setText("");
        vue.getTextFieldPrenom().setText("");
        vue.getTextFieldEmail().setText("");
        reinitialiserMotsDePasse();
        vue.getTextFieldNom().requestFocus();
    }
    
    /**
     * Réinitialise uniquement les champs de mot de passe et de confirmation,
     * et place le focus sur le champ du mot de passe.
     */
    private void reinitialiserMotsDePasse() {
        vue.getPasswordField().setText("");
        vue.getPasswordFieldConfirm().setText("");
        vue.getPasswordField().requestFocus();
    }
    
    /**
     * Retourne à la page d'authentification et ferme la page d'inscription.
     */
    private void retourAuthentification() {
        etat = Etat.RETOUR;
        Page_Authentification pageAuthentification = new Page_Authentification();
        pageAuthentification.setVisible(true);
        vue.dispose();
    }
    
    /**
     * Affiche un message d'erreur dans une boîte de dialogue.
     * 
     * @param message le message d'erreur à afficher
     */
    private void afficherErreur(String message) {
        JOptionPane.showMessageDialog(vue, 
            message, 
            TITRE_ERREUR, 
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Gère une erreur survenue pendant le processus d'inscription.
     * Affiche un message d'erreur et passe à l'état ERREUR.
     * 
     * @param titre le titre du message d'erreur
     * @param message la description détaillée de l'erreur
     */
    private void gererErreur(String titre, String message) {
        System.err.println(titre + ": " + message);
        afficherErreur(titre + ": " + message);
        etat = Etat.ERREUR;
    }
    
    /**
     * Retourne l'état actuel du contrôleur.
     * Utile pour le débogage et les tests.
     * 
     * @return l'état actuel du processus d'inscription
     */
    public Etat getEtat() {
        return etat;
    }
}
package controleur;

import modele.Usager;
import modele.dao.UsagerDAO;
import ihm.Page_Inscription;
import ihm.Page_Authentification;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControleurInscription implements ActionListener {
    
    // États du contrôleur
    private enum Etat {
        SAISIE,
        VALIDATION_CHAMPS,
        VERIFICATION_EMAIL,
        CREATION_COMPTE,
        REUSSITE,
        RETOUR,
        ERREUR
    }
    
    // Références
    private Page_Inscription vue;
    private Etat etat;
    
    // Constantes
    private static final String TITRE_ERREUR = "Erreur";
    private static final String TITRE_SUCCES = "Succès";
    
    public ControleurInscription(Page_Inscription vue) {
        this.vue = vue;
        this.etat = Etat.SAISIE;
        
        initialiserControleur();
    }
    
    private void initialiserControleur() {
        configurerListeners();
    }
    
    private void configurerListeners() {
        // Boutons principaux
        vue.getBtnRetour().addActionListener(this);
        vue.getBtnCreerCompte().addActionListener(this);
        
        // Entrée dans les champs de texte
        vue.getTextFieldNom().addActionListener(this);
        vue.getTextFieldPrenom().addActionListener(this);
        vue.getTextFieldEmail().addActionListener(this);
        vue.getPasswordField().addActionListener(this);
        vue.getPasswordFieldConfirm().addActionListener(this);
    }
    
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
                // En cours de traitement, aucune action possible
                break;
                
            case REUSSITE:
                // Le succès est géré dans la méthode afficherSucces()
                break;
                
            case ERREUR:
                // En état d'erreur, seul le retour est possible
                if (source == vue.getBtnRetour()) {
                    retourAuthentification();
                }
                break;
        }
    }
    
    private void lancerCreationCompte() {
        etat = Etat.VALIDATION_CHAMPS;
        
        // Récupérer les valeurs
        String nom = vue.getTextFieldNom().getText().trim();
        String prenom = vue.getTextFieldPrenom().getText().trim();
        String email = vue.getTextFieldEmail().getText().trim();
        String motDePasse = new String(vue.getPasswordField().getPassword());
        String confirmation = new String(vue.getPasswordFieldConfirm().getPassword());
        
        // Validation
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
    
    private boolean validerChamps(String nom, String prenom, String email, 
                                 String motDePasse, String confirmation) {
        
        // Vérification des champs vides
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || 
            motDePasse.isEmpty() || confirmation.isEmpty()) {
            
            afficherErreur("Veuillez remplir tous les champs");
            return false;
        }
        
        // Vérification de la correspondance des mots de passe
        if (!motDePasse.equals(confirmation)) {
            afficherErreur("Les mots de passe ne correspondent pas");
            reinitialiserMotsDePasse();
            return false;
        }
        
        // Vérification de la longueur du mot de passe
        if (motDePasse.length() < 4) {
            afficherErreur("Le mot de passe doit contenir au moins 4 caractères");
            reinitialiserMotsDePasse();
            return false;
        }
        
        // Validation du format de l'email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex)) {
            afficherErreur("Veuillez saisir un email valide");
            vue.getTextFieldEmail().requestFocus();
            return false;
        }
        
        return true;
    }
    
    private boolean verifierEmail(String email) {
        if (UsagerDAO.emailExisteDeja(email)) {
            afficherErreur("Cet email est déjà utilisé");
            vue.getTextFieldEmail().requestFocus();
            vue.getTextFieldEmail().selectAll();
            return false;
        }
        return true;
    }
    
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
    
    private void reinitialiserFormulaire() {
        vue.getTextFieldNom().setText("");
        vue.getTextFieldPrenom().setText("");
        vue.getTextFieldEmail().setText("");
        reinitialiserMotsDePasse();
        vue.getTextFieldNom().requestFocus();
    }
    
    private void reinitialiserMotsDePasse() {
        vue.getPasswordField().setText("");
        vue.getPasswordFieldConfirm().setText("");
        vue.getPasswordField().requestFocus();
    }
    
    private void retourAuthentification() {
        etat = Etat.RETOUR;
        Page_Authentification pageAuthentification = new Page_Authentification();
        pageAuthentification.setVisible(true);
        vue.dispose();
    }
    
    private void afficherErreur(String message) {
        JOptionPane.showMessageDialog(vue, 
            message, 
            TITRE_ERREUR, 
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void gererErreur(String titre, String message) {
        System.err.println(titre + ": " + message);
        afficherErreur(titre + ": " + message);
        etat = Etat.ERREUR;
    }
    
    // Getters pour débogage
    public Etat getEtat() {
        return etat;
    }
}
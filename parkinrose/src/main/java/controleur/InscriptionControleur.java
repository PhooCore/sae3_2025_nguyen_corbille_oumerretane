package controleur;

import modele.Usager;
import modele.dao.UsagerDAO;
import ihm.Page_Inscription;
import ihm.Page_Authentification;
import javax.swing.JOptionPane;

public class InscriptionControleur {
    
    /**
     * Contrôleur pour la gestion de l'inscription
     */
    public InscriptionControleur() {}
    
    /**
     * Crée un nouveau compte utilisateur
     * @param nom le nom de l'utilisateur
     * @param prenom le prénom de l'utilisateur
     * @param email l'email de l'utilisateur
     * @param motDePasse le mot de passe
     * @param confirmation la confirmation du mot de passe
     * @param pageInscription la page d'inscription pour les callbacks
     * @return true si l'inscription réussit
     */
    public boolean creerCompte(String nom, String prenom, String email, 
                              String motDePasse, String confirmation, 
                              Page_Inscription pageInscription) {

        if (!validerChamps(nom, prenom, email, motDePasse, confirmation, pageInscription)) {
            return false;
        }
        

        if (UsagerDAO.emailExisteDeja(email)) {
            JOptionPane.showMessageDialog(pageInscription, 
                "Cet email est déjà utilisé", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        Usager nouvelUsager = new Usager(nom, prenom, email, motDePasse);
        
        boolean succes = UsagerDAO.ajouterUsager(nouvelUsager);
        
        if (succes) {
            JOptionPane.showMessageDialog(pageInscription, 
                "Compte créé avec succès !", 
                "Succès", 
                JOptionPane.INFORMATION_MESSAGE);

            Page_Authentification authPage = new Page_Authentification();
            authPage.setVisible(true);
            pageInscription.dispose();
            return true;
        } else {
            JOptionPane.showMessageDialog(pageInscription, 
                "Erreur lors de la création du compte", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Valide tous les champs du formulaire
     */
    private boolean validerChamps(String nom, String prenom, String email, 
                                 String motDePasse, String confirmation, 
                                 Page_Inscription pageInscription) {
        
        if (nom == null || nom.trim().isEmpty() ||
            prenom == null || prenom.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            motDePasse == null || motDePasse.isEmpty()) {
            
            JOptionPane.showMessageDialog(pageInscription, 
                "Veuillez remplir tous les champs", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!motDePasse.equals(confirmation)) {
            JOptionPane.showMessageDialog(pageInscription, 
                "Les mots de passe ne correspondent pas", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (motDePasse.length() < 4) {
            JOptionPane.showMessageDialog(pageInscription, 
                "Le mot de passe doit contenir au moins 4 caractères", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(pageInscription, 
                "Veuillez saisir un email valide", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Redirige vers la page d'authentification
     * @param pageInscription la page actuelle à fermer
     */
    public void redirigerVersAuthentification(Page_Inscription pageInscription) {
        Page_Authentification authPage = new Page_Authentification();
        authPage.setVisible(true);
        pageInscription.dispose();
    }
}
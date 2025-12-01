package controleur;

import modele.Usager;
import modele.dao.AuthentificationDAO;
import modele.dao.UsagerDAO;
import ihm.Page_Authentification;
import ihm.Page_Principale;
import ihm.Page_Inscription;
import javax.swing.JOptionPane;

public class AuthentificationControleur {
    
    /**
     * Contrôleur pour la gestion de l'authentification
     */
    public AuthentificationControleur() {}
    
    /**
     * Authentifie un utilisateur
     * @param email l'email de l'utilisateur
     * @param motDePasse le mot de passe
     * @param pageAuthentification la page d'authentification pour les callbacks
     * @return true si l'authentification réussit
     */
    public boolean authentifierUtilisateur(String email, String motDePasse, Page_Authentification pageAuthentification) {
        if (email == null || email.trim().isEmpty() || motDePasse == null || motDePasse.isEmpty()) {
            JOptionPane.showMessageDialog(pageAuthentification, 
                "Veuillez remplir tous les champs", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        boolean authentifie = AuthentificationDAO.verifierUtilisateur(email, motDePasse);
        
        if (authentifie) {
            String[] infosUtilisateur = AuthentificationDAO.getInfosUtilisateur(email);
            
            if (infosUtilisateur != null) {
                Page_Principale pagePrincipale = new Page_Principale(email);
                pagePrincipale.setVisible(true);
                pageAuthentification.dispose();
                return true;
            } else {
                JOptionPane.showMessageDialog(pageAuthentification, 
                    "Erreur lors de la récupération des informations", 
                    "Erreur", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            JOptionPane.showMessageDialog(pageAuthentification, 
                "Email ou mot de passe incorrect", 
                "Erreur d'authentification", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Redirige vers la page d'inscription
     * @param pageAuthentification la page actuelle à fermer
     */
    public void redirigerVersInscription(Page_Authentification pageAuthentification) {
        Page_Inscription pageInscription = new Page_Inscription();
        pageInscription.setVisible(true);
        pageAuthentification.dispose();
    }
    
    /**
     * Redirige vers la page de modification de mot de passe
     * @param pageAuthentification la page actuelle à fermer
     */
    public void redirigerVersModifMDP(Page_Authentification pageAuthentification) {
        ihm.Page_Modif_MDP pageModifMdp = new ihm.Page_Modif_MDP();
        pageModifMdp.setVisible(true);
        pageAuthentification.dispose();
    }
}
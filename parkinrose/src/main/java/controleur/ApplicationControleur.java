package controleur;

import ihm.Page_Bienvenue;
import ihm.Page_Authentification;
import ihm.Page_Principale;

/**
 * Contrôleur principal de l'application
 * Gère la navigation entre les pages principales
 */
public class ApplicationControleur {
    
    /**
     * Contrôleur principal de l'application
     */
    public ApplicationControleur() {}
    
    /**
     * Démarre l'application
     */
    public void demarrerApplication() {
        Page_Bienvenue pageBienvenue = new Page_Bienvenue();
        pageBienvenue.setVisible(true);
    }
    
    /**
     * Redirige vers la page d'authentification
     * @param pageBienvenue la page de bienvenue à fermer
     */
    public void redirigerVersAuthentification(Page_Bienvenue pageBienvenue) {
        Page_Authentification authPage = new Page_Authentification();
        authPage.setVisible(true);
        pageBienvenue.dispose();
    }
    
    /**
     * Redirige vers la page principale après authentification
     * @param email l'email de l'utilisateur connecté
     * @param pageAuthentification la page d'authentification à fermer
     */
    public void redirigerVersPagePrincipale(String email, Page_Authentification pageAuthentification) {
        Page_Principale pagePrincipale = new Page_Principale(email);
        pagePrincipale.setVisible(true);
        pageAuthentification.dispose();
    }
    
    /**
     * Redirige vers la page d'authentification depuis n'importe quelle page
     * @param pageActuelle la page actuelle à fermer
     */
    public void redirigerVersAuthentification(javax.swing.JFrame pageActuelle) {
        Page_Authentification authPage = new Page_Authentification();
        authPage.setVisible(true);
        pageActuelle.dispose();
    }
}
package controleur;

import ihm.Page_Bienvenue;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Contrôleur gérant la page d'accueil de l'application.
 * Gère la transition entre la page de bienvenue et la page d'authentification.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Bienvenue
 * et la navigation vers l'authentification.
 * 
 * @author Équipe 7
 */
public class ControleurBienvenue implements ActionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur de bienvenue.
     * Permet de suivre le cycle de vie de la page d'accueil.
     */
    private enum EtatBienvenue {
        /** État initial au démarrage du contrôleur */
        INITIAL,
        /** En attente d'une action de l'utilisateur */
        ATTENTE,
        /** Redirection vers la page d'authentification en cours */
        REDIRECTION
    }
    
    private Page_Bienvenue vue;
    private EtatBienvenue etat;
    
    /**
     * Constructeur du contrôleur de bienvenue.
     * Initialise le contrôleur avec la vue associée, configure les écouteurs
     * et passe à l'état d'attente.
     * 
     * @param vue la page d'interface graphique de bienvenue
     */
    public ControleurBienvenue(Page_Bienvenue vue) {
        this.vue = vue;
        this.etat = EtatBienvenue.INITIAL;
        configurerListeners();
        
        etat = EtatBienvenue.ATTENTE;
    }
    
    /**
     * Configure les écouteurs d'événements pour les composants interactifs de la vue.
     * Connecte le bouton d'entrée à l'action du contrôleur.
     */
    private void configurerListeners() {
        vue.getBtnEntrer().addActionListener(this);
    }
    
    /**
     * Gère les événements d'action des composants de la vue.
     * Traite le clic sur le bouton d'entrée en fonction de l'état actuel
     * et redirige vers la page d'authentification.
     * 
     * @param e l'événement d'action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = getActionBouton((JButton) e.getSource());
        
        switch (etat) {
            case ATTENTE:
                if (action.equals("ENTRER")) {
                    etat = EtatBienvenue.REDIRECTION;
                    ouvrirAuthentification();
                }
                break;
                
            case REDIRECTION:
                break;
        }
    }
    
    /**
     * Ouvre la page d'authentification et ferme la page de bienvenue.
     * Effectue la transition vers l'écran de connexion de l'application.
     */
    private void ouvrirAuthentification() {
        ihm.Page_Authentification pageAuth = new ihm.Page_Authentification();
        pageAuth.setVisible(true);
        vue.dispose();
    }
    
    /**
     * Détermine l'action associée à un bouton en analysant son texte.
     * 
     * @param b le bouton dont on veut identifier l'action
     * @return une chaîne représentant l'action ("ENTRER" ou "INCONNU")
     */
    private String getActionBouton(JButton b) {
        String texte = b.getText();
        if (texte != null) {
            if (texte.contains("ENTRER")) {
                return "ENTRER";
            }
        }
        return "INCONNU";
    }
}
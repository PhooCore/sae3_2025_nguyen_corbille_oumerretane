package controleur;

import ihm.Page_Stationnement_En_Cours;
import ihm.Page_Principale;
import utils.NotificationManager;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.JOptionPane;

/**
 * Contrôleur gérant l'interface de consultation d'un stationnement en cours.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Stationnement_En_Cours
 * et le modèle (Stationnement).
 * Gère l'actualisation automatique des données et les notifications de fin de stationnement.
 * 
 * @author Équipe 7
 */
public class ControleurStationnementEnCours implements ActionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur.
     * Permet de suivre le cycle de vie de la consultation du stationnement.
     */
    private enum EtatStationnement {
        /** État initial au démarrage du contrôleur */
        INITIAL,
        /** Affichage des informations du stationnement en cours */
        AFFICHAGE_EN_COURS,
        /** Retour à la page principale en cours */
        RETOUR_EN_COURS,
        /** Chargement des données du stationnement */
        CHARGEMENT_DONNEES,
        /** Vérification de l'état du stationnement */
        VERIFICATION_EN_COURS,
        /** Terminaison du stationnement en cours */
        TERMINAISON_EN_COURS
    }
    
    private Page_Stationnement_En_Cours vue;
    private EtatStationnement etat;
    private Timer timer;
    private NotificationManager notificationManager;
    
    /**
     * Constructeur du contrôleur de stationnement en cours.
     * Initialise le contrôleur avec la vue associée et démarre l'actualisation automatique.
     * 
     * @param vue la page d'interface graphique du stationnement en cours
     */
    public ControleurStationnementEnCours(Page_Stationnement_En_Cours vue) {
        this.vue = vue;
        this.etat = EtatStationnement.INITIAL;
        this.notificationManager = NotificationManager.getInstance();
        configurerListeners();
        
        etat = EtatStationnement.AFFICHAGE_EN_COURS;
        demarrerTimer();
        
        verifierNotifications();
    }
    
    /**
     * Configure les écouteurs d'événements pour les composants de la vue.
     * Recherche et configure les boutons "Retour" et "Arrêter".
     */
    private void configurerListeners() {
        rechercherBoutonRetour(vue.getContentPane());
        ajouterListenerBoutonArreter();
    }
    
    /**
     * Ajoute un écouteur au bouton "Arrêter le stationnement".
     * Recherche le bouton dans les composants de la vue.
     */
    private void ajouterListenerBoutonArreter() {
        try {
            for (java.awt.Component comp : vue.getContentPane().getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    if (btn.getText() != null && btn.getText().contains("Arrêter")) {
                        btn.addActionListener(e -> 
                            actionPerformed(new ActionEvent(btn, ActionEvent.ACTION_PERFORMED, "ARRETER")));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout du listener au bouton Arrêter: " + e.getMessage());
        }
    }
    
    /**
     * Gère les événements d'action en fonction de l'état courant du contrôleur.
     * 
     * @param e l'événement d'action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = obtenirAction(e);
        
        switch (etat) {
            case INITIAL:
                traiterEtatInitial(action);
                break;
                
            case AFFICHAGE_EN_COURS:
                traiterEtatAffichageEnCours(action);
                break;
                
            case RETOUR_EN_COURS:
                traiterEtatRetourEnCours(action);
                break;
                
            case CHARGEMENT_DONNEES:
                traiterEtatChargementDonnees(action);
                break;
                
            case VERIFICATION_EN_COURS:
                traiterEtatVerificationEnCours(action);
                break;
                
            case TERMINAISON_EN_COURS:
                traiterEtatTerminaisonEnCours(action);
                break;
        }
    }
    
    /**
     * Détermine l'action à partir de la source de l'événement.
     * Identifie si c'est un bouton, un timer ou une autre source.
     * 
     * @param e l'événement d'action
     * @return une chaîne identifiant l'action
     */
    private String obtenirAction(ActionEvent e) {
        Object source = e.getSource();
        
        if (source instanceof JButton) {
            JButton btn = (JButton) source;
            String texte = btn.getText();
            
            if (texte != null) {
                if (texte.contains("Retour")) {
                    return "RETOUR";
                } else if (texte.contains("Arrêter")) {
                    return "ARRETER";
                }
            }
        } else if (source instanceof Timer) {
            return "ACTUALISATION_TIMER";
        }
        
        return e.getActionCommand();
    }
    
    /**
     * Traite les actions en état INITIAL.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatInitial(String action) {
        if (action.equals("INITIALISATION_COMPLETE")) {
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
        }
    }
    
    /**
     * Traite les actions en état AFFICHAGE_EN_COURS.
     * Gère le retour, l'arrêt et l'actualisation automatique.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatAffichageEnCours(String action) {
        switch (action) {
            case "RETOUR":
                etat = EtatStationnement.RETOUR_EN_COURS;
                retourAccueil();
                break;
                
            case "ARRETER":
                etat = EtatStationnement.TERMINAISON_EN_COURS;
                terminerStationnement();
                break;
                
            case "ACTUALISATION_TIMER":
                etat = EtatStationnement.CHARGEMENT_DONNEES;
                actualiserDonnees();
                break;
        }
    }
    
    /**
     * Traite les actions en état RETOUR_EN_COURS.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatRetourEnCours(String action) {
        if (action.equals("ANNULER_RETOUR")) {
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
        }
    }
    
    /**
     * Traite les actions en état CHARGEMENT_DONNEES.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatChargementDonnees(String action) {
        if (action.equals("DONNEES_CHARGEES")) {
            etat = EtatStationnement.VERIFICATION_EN_COURS;
            verifierStationnement();
        } else if (action.equals("ERREUR_CHARGEMENT")) {
            afficherErreurChargement();
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
        }
    }
    
    /**
     * Traite les actions en état VERIFICATION_EN_COURS.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatVerificationEnCours(String action) {
        if (action.equals("STATIONNEMENT_ACTIF")) {
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
            rafraichirAffichage();
            
        } else if (action.equals("ERREUR_VERIFICATION")) {
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
        }
    }
    
    /**
     * Traite les actions en état TERMINAISON_EN_COURS.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatTerminaisonEnCours(String action) {
        if (action.equals("TERMINAISON_CONFIRMEE")) {
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
        } else if (action.equals("ANNULER_TERMINAISON")) {
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
        }
    }
    
    /**
     * Retourne à la page d'accueil de l'application.
     * Arrête le timer d'actualisation et ferme la page actuelle.
     */
    private void retourAccueil() {
        arreterTimer();
        
        try {
            Page_Principale pagePrincipale = new Page_Principale(vue.getEmailUtilisateur());
            pagePrincipale.setVisible(true);
            vue.dispose();
        } catch (Exception e) {
            System.err.println("Erreur lors du retour à l'accueil: " + e.getMessage());
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
        }
    }
    
    /**
     * Termine le stationnement en cours.
     * La logique de terminaison est implémentée dans la vue.
     */
    private void terminerStationnement() {
        etat = EtatStationnement.AFFICHAGE_EN_COURS;
    }
    
    /**
     * Actualise les données du stationnement en cours.
     * Recharge le stationnement depuis la base de données et vérifie les notifications.
     */
    private void actualiserDonnees() {
        try {
            vue.chargerStationnementActif();
            
            if (vue.getStationnementActif() != null) {
                notificationManager.verifierStationnement(vue.getStationnementActif());
            }
            
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "DONNEES_CHARGEES"));
        } catch (Exception ex) {
            System.err.println("Erreur lors du rechargement des données: " + ex.getMessage());
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ERREUR_CHARGEMENT"));
        }
    }
    
    /**
     * Vérifie l'état du stationnement actuel.
     * Déclenche l'événement approprié selon que le stationnement existe ou non.
     */
    private void verifierStationnement() {
        try {
            if (vue.getStationnementActif() == null) {
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "STATIONNEMENT_TERMINE"));
            } else {
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "STATIONNEMENT_ACTIF"));
            }
        } catch (Exception ex) {
            System.err.println("Erreur lors de la vérification du stationnement: " + ex.getMessage());
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ERREUR_VERIFICATION"));
        }
    }
    
    /**
     * Vérifie les notifications liées au stationnement.
     * Utilisé au démarrage pour alerter l'utilisateur si nécessaire.
     */
    private void verifierNotifications() {
        if (vue.getStationnementActif() != null) {
            notificationManager.verifierStationnement(vue.getStationnementActif());
        }
    }
    
    /**
     * Rafraîchit l'affichage des informations du stationnement.
     * Appelle la méthode d'affichage de la vue.
     */
    private void rafraichirAffichage() {
        try {
            vue.afficherInformationsStationnement();
        } catch (Exception ex) {
            System.err.println("Erreur lors du rafraîchissement de l'affichage: " + ex.getMessage());
        }
    }
    
    /**
     * Affiche un message d'erreur lors du chargement des données.
     */
    private void afficherErreurChargement() {
        JOptionPane.showMessageDialog(vue, 
            "Erreur lors du chargement des données.", 
            "Erreur", 
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Démarre le timer d'actualisation automatique.
     * Le timer déclenche une actualisation toutes les 30 secondes.
     */
    private void demarrerTimer() {
        timer = new Timer(30000, e -> 
            actionPerformed(new ActionEvent(timer, ActionEvent.ACTION_PERFORMED, "ACTUALISATION_TIMER")));
        timer.start();
    }
    
    /**
     * Arrête le timer d'actualisation.
     * Utilisé lors du retour à la page principale.
     */
    private void arreterTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
    
    /**
     * Recherche et configure le bouton "Retour" dans le conteneur.
     * Méthode récursive qui parcourt tous les composants.
     * 
     * @param container le conteneur à parcourir
     */
    private void rechercherBoutonRetour(java.awt.Container container) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if (btn.getText() != null && btn.getText().contains("Retour")) {
                    btn.addActionListener(this);
                    return;
                }
            } else if (comp instanceof javax.swing.JPanel) {
                rechercherBoutonRetour((javax.swing.JPanel) comp);
            } else if (comp instanceof javax.swing.JScrollPane) {
                rechercherBoutonRetour(((javax.swing.JScrollPane) comp).getViewport());
            }
        }
    }
    
    /**
     * Retourne l'état actuel du contrôleur.
     * 
     * @return l'état actuel
     */
    public EtatStationnement getEtat() {
        return etat;
    }
    
    /**
     * Nettoie les ressources utilisées par le contrôleur.
     * Arrête le timer d'actualisation.
     */
    public void nettoyer() {
        arreterTimer();
    }
}
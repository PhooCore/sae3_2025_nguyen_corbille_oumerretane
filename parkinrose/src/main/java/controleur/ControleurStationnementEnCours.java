package controleur;

import ihm.Page_Stationnement_En_Cours;
import ihm.Page_Principale;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.JOptionPane;

public class ControleurStationnementEnCours implements ActionListener {
    
    private enum EtatStationnement {
        INITIAL,
        AFFICHAGE_EN_COURS,
        RETOUR_EN_COURS,
        CHARGEMENT_DONNEES,
        VERIFICATION_EN_COURS,
        TERMINAISON_EN_COURS
    }
    
    private Page_Stationnement_En_Cours vue;
    private EtatStationnement etat;
    private Timer timer;
    private boolean messageTermineAffiche = false; 
    
    public ControleurStationnementEnCours(Page_Stationnement_En_Cours vue) {
        this.vue = vue;
        this.etat = EtatStationnement.INITIAL;
        configurerListeners();
        
        etat = EtatStationnement.AFFICHAGE_EN_COURS;
        demarrerTimer();
    }
    
    private void configurerListeners() {
        rechercherBoutonRetour(vue.getContentPane());
        
        // Le bouton "Arrêter" est déjà configuré dans la vue
        // On peut ajouter un listener si nécessaire :
        ajouterListenerBoutonArreter();
    }
    
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
    
    private void traiterEtatInitial(String action) {
        // État initial, seulement pour l'initialisation
        if (action.equals("INITIALISATION_COMPLETE")) {
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
        }
    }
    
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
    
    private void traiterEtatRetourEnCours(String action) {
        // En cours de retour à l'accueil
        if (action.equals("ANNULER_RETOUR")) {
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
        }
    }
    
    private void traiterEtatChargementDonnees(String action) {
        // En cours de chargement des données
        if (action.equals("DONNEES_CHARGEES")) {
            etat = EtatStationnement.VERIFICATION_EN_COURS;
            verifierStationnement();
        } else if (action.equals("ERREUR_CHARGEMENT")) {
            afficherErreurChargement();
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
        }
    }
    
    private void traiterEtatVerificationEnCours(String action) {
        // En cours de vérification du stationnement
        if (action.equals("STATIONNEMENT_ACTIF")) {
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
            rafraichirAffichage();
        } else if (action.equals("STATIONNEMENT_TERMINE")) {
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
            stationnementTermine();
        } else if (action.equals("ERREUR_VERIFICATION")) {
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
        }
    }
    
    private void traiterEtatTerminaisonEnCours(String action) {
        // En cours de terminaison du stationnement
        if (action.equals("TERMINAISON_CONFIRMEE")) {
            // La terminaison est gérée par la vue
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
        } else if (action.equals("ANNULER_TERMINAISON")) {
            etat = EtatStationnement.AFFICHAGE_EN_COURS;
        }
    }
    
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
    
    private void terminerStationnement() {
        // La logique de terminaison est déjà implémentée dans la vue
        // On pourrait ici ajouter des vérifications supplémentaires
        etat = EtatStationnement.AFFICHAGE_EN_COURS;
    }
    
    private void actualiserDonnees() {
        try {
            vue.chargerStationnementActif();
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "DONNEES_CHARGEES"));
        } catch (Exception ex) {
            System.err.println("Erreur lors du rechargement des données: " + ex.getMessage());
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ERREUR_CHARGEMENT"));
        }
    }
    
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
    
    private void rafraichirAffichage() {
        try {
            vue.afficherInformationsStationnement();
        } catch (Exception ex) {
            System.err.println("Erreur lors du rafraîchissement de l'affichage: " + ex.getMessage());
        }
    }
    
    private void stationnementTermine() {
        // N'afficher le message qu'une seule fois
        if (!messageTermineAffiche) {
            messageTermineAffiche = true;
            arreterTimer(); // Arrêter le timer pour éviter les vérifications répétées
            vue.afficherInformationsStationnement();
        }
    }
    
    private void afficherErreurChargement() {
        JOptionPane.showMessageDialog(vue, 
            "Erreur lors du chargement des données.", 
            "Erreur", 
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void demarrerTimer() {
        // Timer toutes les 30 secondes pour vérifier l'état du stationnement
        timer = new Timer(30000, e -> 
            actionPerformed(new ActionEvent(timer, ActionEvent.ACTION_PERFORMED, "ACTUALISATION_TIMER")));
        timer.start();
    }
    
    private void arreterTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
    
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
    
    // Méthodes pour la gestion des états
    
    public EtatStationnement getEtat() {
        return etat;
    }
    
    public void nettoyer() {
        arreterTimer();
    }
}
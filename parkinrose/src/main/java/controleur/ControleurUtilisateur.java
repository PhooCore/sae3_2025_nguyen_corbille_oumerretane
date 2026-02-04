package controleur;

import modele.Usager;
import modele.dao.UsagerDAO;
import ihm.Page_Utilisateur;
import ihm.Page_Authentification;
import ihm.Page_Modif_MDP;
import ihm.Page_Principale;
import ihm.Page_Abonnements;
import ihm.Page_Gestion_Vehicules;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Contrôleur gérant l'interface du profil utilisateur.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Utilisateur
 * et le modèle (Usager).
 * Gère la navigation vers les différentes fonctionnalités du profil : modification du mot de passe,
 * gestion des véhicules, déconnexion et retour à l'accueil.
 * 
 * @author Équipe 7
 */
public class ControleurUtilisateur implements ActionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur.
     * Permet de suivre les actions en cours dans le profil utilisateur.
     */
    private enum EtatUtilisateur {
        /** En attente d'une action utilisateur */
        ATTENTE,
        /** Ouverture de la page de gestion des véhicules */
        OUVERTURE_GESTION_VEHICULES,
        /** Ouverture de la page de modification du mot de passe */
        OUVERTURE_MODIF_MDP,
        /** Demande de confirmation de déconnexion */
        CONFIRMATION_DECONNEXION,
        /** Déconnexion en cours */
        DECONNEXION_EN_COURS,
        /** Retour à la page d'accueil en cours */
        RETOUR_ACCUEIL_EN_COURS,
        /** Fermeture des fenêtres en cours */
        FERMETURE_EN_COURS
    }
    
    private Page_Utilisateur vue;
    private String emailUtilisateur;
    private Usager usager;
    private EtatUtilisateur etat;
    
    /**
     * Constructeur du contrôleur utilisateur.
     * Initialise le contrôleur avec la vue associée et charge les données de l'utilisateur.
     * 
     * @param vue la page d'interface graphique du profil utilisateur
     */
    public ControleurUtilisateur(Page_Utilisateur vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.etat = EtatUtilisateur.ATTENTE;
        
        if (emailUtilisateur != null) {
            this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        }
        
        configurerListeners();
    }
    
    /**
     * Configure les écouteurs d'événements pour tous les boutons de la vue.
     */
    private void configurerListeners() {
        if (vue.getBtnModifierMdp() != null) {
            vue.getBtnModifierMdp().addActionListener(this);
        }
        
        if (vue.getBtnDeconnexion() != null) {
            vue.getBtnDeconnexion().addActionListener(this);
        }
        
        if (vue.getBtnRetour() != null) {
            vue.getBtnRetour().addActionListener(this);
        }
        
        if (vue.getBtnGestionVehicules() != null) {
            vue.getBtnGestionVehicules().addActionListener(this);
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
            case ATTENTE:
                traiterEtatAttente(action);
                break;
                
            case OUVERTURE_GESTION_VEHICULES:
                traiterEtatOuvertureGestionVehicules(action);
                break;
                
            case OUVERTURE_MODIF_MDP:
                traiterEtatOuvertureModifMdp(action);
                break;
                
            case CONFIRMATION_DECONNEXION:
                traiterEtatConfirmationDeconnexion(action);
                break;
                
            case DECONNEXION_EN_COURS:
                traiterEtatDeconnexionEnCours(action);
                break;
                
            case RETOUR_ACCUEIL_EN_COURS:
                traiterEtatRetourAccueilEnCours(action);
                break;
                
            case FERMETURE_EN_COURS:
                traiterEtatFermetureEnCours(action);
                break;
        }
    }
    
    /**
     * Détermine l'action à partir de la source de l'événement.
     * 
     * @param e l'événement d'action
     * @return une chaîne identifiant l'action
     */
    private String obtenirAction(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == vue.getBtnModifierMdp()) {
            return "MODIFIER_MDP";
        } else if (source == vue.getBtnGestionVehicules()) {
            return "GESTION_VEHICULES";
        } else if (source == vue.getBtnDeconnexion()) {
            return "DECONNEXION";
        } else if (source == vue.getBtnRetour()) {
            return "RETOUR_ACCUEIL";
        }
        
        return e.getActionCommand();
    }
    
    /**
     * Traite les actions en état ATTENTE.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatAttente(String action) {
        switch (action) {
            case "MODIFIER_MDP":
                etat = EtatUtilisateur.OUVERTURE_MODIF_MDP;
                ouvrirModificationMotDePasse();
                break;
                
            case "GESTION_VEHICULES":
                etat = EtatUtilisateur.OUVERTURE_GESTION_VEHICULES;
                ouvrirGestionVehicules();
                break;
                
            case "DECONNEXION":
                etat = EtatUtilisateur.CONFIRMATION_DECONNEXION;
                demanderConfirmationDeconnexion();
                break;
                
            case "RETOUR_ACCUEIL":
                etat = EtatUtilisateur.RETOUR_ACCUEIL_EN_COURS;
                retourAccueil();
                break;
        }
    }
    
    /**
     * Traite les actions en état OUVERTURE_GESTION_VEHICULES.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatOuvertureGestionVehicules(String action) {
        switch (action) {
            case "FERMER_GESTION_VEHICULES":
                etat = EtatUtilisateur.ATTENTE;
                vue.setVisible(true);
                vue.toFront();
                break;
                
            case "ERREUR_OUVERTURE_VEHICULES":
                etat = EtatUtilisateur.ATTENTE;
                JOptionPane.showMessageDialog(vue,
                    "Erreur lors de l'ouverture de la gestion des véhicules.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
                break;
        }
    }
    
    /**
     * Traite les actions en état OUVERTURE_MODIF_MDP.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatOuvertureModifMdp(String action) {
        switch (action) {
            case "FERMER_MODIF_MDP":
                etat = EtatUtilisateur.ATTENTE;
                vue.setVisible(true);
                vue.toFront();
                break;
                
            case "MDP_MODIFIE":
                etat = EtatUtilisateur.ATTENTE;
                vue.setVisible(true);
                vue.toFront();
                afficherMessageModificationReussie();
                rechargerDonneesUtilisateur();
                break;
                
            case "ERREUR_MODIF_MDP":
                etat = EtatUtilisateur.ATTENTE;
                vue.setVisible(true);
                vue.toFront();
                break;
        }
    }
    
    /**
     * Traite les actions en état CONFIRMATION_DECONNEXION.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatConfirmationDeconnexion(String action) {
        switch (action) {
            case "CONFIRMER_DECONNEXION":
                etat = EtatUtilisateur.DECONNEXION_EN_COURS;
                executerDeconnexion();
                break;
                
            case "ANNULER_DECONNEXION":
                etat = EtatUtilisateur.ATTENTE;
                break;
        }
    }
    
    /**
     * Traite les actions en état DECONNEXION_EN_COURS.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatDeconnexionEnCours(String action) {
        if (action.equals("DECONNEXION_COMPLETE")) {
            etat = EtatUtilisateur.FERMETURE_EN_COURS;
            fermerFenetresUtilisateur();
        }
    }
    
    /**
     * Traite les actions en état RETOUR_ACCUEIL_EN_COURS.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatRetourAccueilEnCours(String action) {
        if (action.equals("RETOUR_COMPLETE")) {
            etat = EtatUtilisateur.FERMETURE_EN_COURS;
            vue.dispose();
        }
    }
    
    /**
     * Traite les actions en état FERMETURE_EN_COURS.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatFermetureEnCours(String action) {
        if (action.equals("FERMETURE_ANNULEE")) {
            etat = EtatUtilisateur.ATTENTE;
        }
    }
    
    /**
     * Ouvre la page de gestion des véhicules.
     * Masque la page utilisateur et configure les écouteurs de fenêtre pour le retour.
     */
    private void ouvrirGestionVehicules() {
        if (usager == null) {
            JOptionPane.showMessageDialog(vue,
                "Utilisateur non trouvé. Veuillez vous reconnecter.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ERREUR_OUVERTURE_VEHICULES"));
            return;
        }
        
        try {
            Page_Gestion_Vehicules pageVehicules = new Page_Gestion_Vehicules(emailUtilisateur);
            pageVehicules.setVisible(true);
            vue.setVisible(false);
            
            pageVehicules.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "FERMER_GESTION_VEHICULES"));
                }
                
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "FERMER_GESTION_VEHICULES"));
                }
            });
        } catch (Exception e) {
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ERREUR_OUVERTURE_VEHICULES"));
        }
    }
    
    /**
     * Ouvre la page de modification du mot de passe.
     * Masque la page utilisateur et configure les écouteurs de fenêtre pour le retour.
     */
    private void ouvrirModificationMotDePasse() {
        try {
            Page_Modif_MDP pageModifMdp = new Page_Modif_MDP(emailUtilisateur, vue);
            pageModifMdp.setVisible(true);
            vue.setVisible(false);
            
            pageModifMdp.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                }
                
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "FERMER_MODIF_MDP"));
                }
            });
        } catch (Exception e) {
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ERREUR_MODIF_MDP"));
        }
    }
    
    /**
     * Demande confirmation à l'utilisateur avant de le déconnecter.
     * Affiche une boîte de dialogue de confirmation.
     */
    private void demanderConfirmationDeconnexion() {
        int confirmation = JOptionPane.showConfirmDialog(vue,
            "Êtes-vous sûr de vouloir vous déconnecter ?",
            "Déconnexion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CONFIRMER_DECONNEXION"));
        } else {
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ANNULER_DECONNEXION"));
        }
    }
    
    /**
     * Exécute la déconnexion de l'utilisateur.
     * Affiche un message de confirmation et ouvre la page d'authentification.
     */
    private void executerDeconnexion() {
        JOptionPane.showMessageDialog(vue,
            "Déconnexion réussie !\nÀ bientôt sur Parkin'Rose.",
            "Déconnexion",
            JOptionPane.INFORMATION_MESSAGE);
        
        try {
            Page_Authentification authPage = new Page_Authentification();
            authPage.setVisible(true);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la page d'authentification: " + e.getMessage());
        }
        
        actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "DECONNEXION_COMPLETE"));
    }
    
    /**
     * Ferme toutes les fenêtres liées à l'utilisateur après la déconnexion.
     * Parcourt toutes les fenêtres ouvertes et ferme celles liées à l'application.
     */
    private void fermerFenetresUtilisateur() {
        if (vue != null && vue.isVisible()) {
            vue.dispose();
        }
        
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window != vue && window.isVisible()) {
                if (window instanceof Page_Principale || 
                    window instanceof Page_Abonnements ||
                    window instanceof Page_Modif_MDP ||
                    window instanceof Page_Gestion_Vehicules) {
                    window.dispose();
                }
            }
        }
    }
    
    /**
     * Retourne à la page d'accueil de l'application.
     * Recherche d'abord une page principale existante, sinon en crée une nouvelle.
     */
    private void retourAccueil() {
        Page_Principale pagePrincipale = trouverPagePrincipaleExistante();
        
        if (pagePrincipale != null) {
            pagePrincipale.setVisible(true);
            pagePrincipale.toFront();
        } else {
            try {
                pagePrincipale = new Page_Principale(emailUtilisateur);
                pagePrincipale.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(vue,
                    "Erreur lors du retour à l'accueil: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "FERMETURE_ANNULEE"));
                return;
            }
        }
        
        actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "RETOUR_COMPLETE"));
    }
    
    /**
     * Recherche une page principale existante parmi les fenêtres ouvertes.
     * 
     * @return la page principale trouvée ou null si aucune n'existe
     */
    private Page_Principale trouverPagePrincipaleExistante() {
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window instanceof Page_Principale && window.isVisible()) {
                return (Page_Principale) window;
            }
        }
        return null;
    }
    
    /**
     * Méthode appelée par la page de modification du mot de passe lors du retour.
     * Gère le retour en fonction du succès de la modification.
     * 
     * @param modificationReussie true si le mot de passe a été modifié, false sinon
     */
    public void onRetourModificationMDP(boolean modificationReussie) {
        if (etat == EtatUtilisateur.OUVERTURE_MODIF_MDP) {
            if (modificationReussie) {
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "MDP_MODIFIE"));
            } else {
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "FERMER_MODIF_MDP"));
            }
        }
    }
    
    /**
     * Affiche un message de confirmation après une modification réussie du mot de passe.
     */
    private void afficherMessageModificationReussie() {
        JOptionPane.showMessageDialog(vue,
            "Mot de passe modifié avec succès !\n" +
            "Votre nouveau mot de passe est maintenant actif.",
            "Modification réussie",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Recharge les données de l'utilisateur depuis la base de données.
     * Utilisé après une modification du mot de passe.
     */
    private void rechargerDonneesUtilisateur() {
        if (emailUtilisateur != null) {
            this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        }
    }
    
    /**
     * Retourne l'état courant du contrôleur.
     * 
     * @return l'état courant
     */
    public EtatUtilisateur getEtatCourant() {
        return etat;
    }
    
    /**
     * Retourne l'utilisateur associé au contrôleur.
     * 
     * @return l'utilisateur
     */
    public Usager getUsager() {
        return usager;
    }
    
    /**
     * Retourne l'email de l'utilisateur.
     * 
     * @return l'email de l'utilisateur
     */
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }
}
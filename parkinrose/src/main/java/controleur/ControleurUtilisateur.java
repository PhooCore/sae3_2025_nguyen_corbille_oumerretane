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

public class ControleurUtilisateur implements ActionListener {
    
    // États possibles du contrôleur
    private enum EtatUtilisateur {
        ATTENTE,
        OUVERTURE_GESTION_VEHICULES,
        OUVERTURE_MODIF_MDP,
        CONFIRMATION_DECONNEXION,
        DECONNEXION_EN_COURS,
        RETOUR_ACCUEIL_EN_COURS,
        FERMETURE_EN_COURS
    }
    
    private Page_Utilisateur vue;
    private String emailUtilisateur;
    private Usager usager;
    private EtatUtilisateur etat;
    
    public ControleurUtilisateur(Page_Utilisateur vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.etat = EtatUtilisateur.ATTENTE;
        
        if (emailUtilisateur != null) {
            this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        }
        
        configurerListeners();
    }
    
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
    
    private void traiterEtatDeconnexionEnCours(String action) {
        // En cours de déconnexion
        if (action.equals("DECONNEXION_COMPLETE")) {
            etat = EtatUtilisateur.FERMETURE_EN_COURS;
            fermerFenetresUtilisateur();
        }
    }
    
    private void traiterEtatRetourAccueilEnCours(String action) {
        // En cours de retour à l'accueil
        if (action.equals("RETOUR_COMPLETE")) {
            etat = EtatUtilisateur.FERMETURE_EN_COURS;
            vue.dispose();
        }
    }
    
    private void traiterEtatFermetureEnCours(String action) {
        // En cours de fermeture
        if (action.equals("FERMETURE_ANNULEE")) {
            etat = EtatUtilisateur.ATTENTE;
        }
    }
    
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
    
    private void ouvrirModificationMotDePasse() {
        try {
            Page_Modif_MDP pageModifMdp = new Page_Modif_MDP(emailUtilisateur, vue);
            pageModifMdp.setVisible(true);
            vue.setVisible(false);
            
            pageModifMdp.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    // Cette méthode est appelée automatiquement par la page
                    // La logique de retour est gérée par onRetourModificationMDP
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
    
    private Page_Principale trouverPagePrincipaleExistante() {
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window instanceof Page_Principale && window.isVisible()) {
                return (Page_Principale) window;
            }
        }
        return null;
    }
    
    public void onRetourModificationMDP(boolean modificationReussie) {
        if (etat == EtatUtilisateur.OUVERTURE_MODIF_MDP) {
            if (modificationReussie) {
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "MDP_MODIFIE"));
            } else {
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "FERMER_MODIF_MDP"));
            }
        }
    }
    
    private void afficherMessageModificationReussie() {
        JOptionPane.showMessageDialog(vue,
            "Mot de passe modifié avec succès !\n" +
            "Votre nouveau mot de passe est maintenant actif.",
            "Modification réussie",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void rechargerDonneesUtilisateur() {
        if (emailUtilisateur != null) {
            this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        }
    }
    
    // Getter pour l'état courant
    public EtatUtilisateur getEtatCourant() {
        return etat;
    }
    
    // Getters
    public Usager getUsager() {
        return usager;
    }
    
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }
}
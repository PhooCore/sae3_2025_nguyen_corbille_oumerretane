package controleur;

import ihm.Page_Principale;
import ihm.Page_Utilisateur;
import ihm.Page_Garer_Voirie;
import ihm.Page_Garer_Parking;
import ihm.Page_Stationnement_En_Cours;
import ihm.Page_Resultats_Recherche;
import ihm.Page_Feedback;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControleurPrincipale implements ActionListener {
    
    // États possibles du contrôleur
    private enum EtatPrincipal {
        ACCUEIL,
        OUVERTURE_PROFIL,
        SELECTION_STATIONNEMENT,
        STATIONNEMENT_VOIRIE,
        STATIONNEMENT_PARKING,
        STATIONNEMENT_EN_COURS,
        RECHERCHE_EN_COURS,
        RECHERCHE_RESULTATS,
        MESSAGERIE
    }
    
    private Page_Principale vue;
    private String emailUtilisateur;
    private EtatPrincipal etat;
    
    public ControleurPrincipale(Page_Principale vue, String email) {
        this.vue = vue;
        this.emailUtilisateur = email;
        this.etat = EtatPrincipal.ACCUEIL;
        configurerListeners();
    }
    
    private void configurerListeners() {
        vue.btnUtilisateur.addActionListener(this);
        vue.btnPreparerStationnement.addActionListener(this);
        vue.btnStationnement.addActionListener(this);
        vue.btnSearch.addActionListener(this);
        vue.btnMessagerie.addActionListener(this);
        
        if (vue.getSearchField() != null) {
            vue.getSearchField().addActionListener(this);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = obtenirAction(e);
        
        switch (etat) {
            case ACCUEIL:
                traiterEtatAccueil(action);
                break;
                
            case OUVERTURE_PROFIL:
                traiterEtatOuvertureProfil(action);
                break;
                
            case SELECTION_STATIONNEMENT:
                traiterEtatSelectionStationnement(action);
                break;
                
            case STATIONNEMENT_VOIRIE:
                traiterEtatStationnementVoirie(action);
                break;
                
            case STATIONNEMENT_PARKING:
                traiterEtatStationnementParking(action);
                break;
                
            case STATIONNEMENT_EN_COURS:
                traiterEtatStationnementEnCours(action);
                break;
                
            case RECHERCHE_EN_COURS:
                traiterEtatRechercheEnCours(action);
                break;
                
            case RECHERCHE_RESULTATS:
                traiterEtatRechercheResultats(action);
                break;
                
            case MESSAGERIE:
                traiterEtatMessagerie(action);
                break;
        }
    }
    
    private String obtenirAction(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == vue.btnUtilisateur) {
            return "PROFIL";
        } else if (source == vue.btnPreparerStationnement || source == vue.btnStationnement) {
            return "STATIONNEMENT";
        } else if (source == vue.btnSearch) {
            return "RECHERCHE";
        } else if (source == vue.btnMessagerie) {
            return "MESSAGERIE";
        } else if (source == vue.getSearchField()) {
            return "ENTREE_RECHERCHE";
        }
        
        return e.getActionCommand();
    }
    
    private void traiterEtatAccueil(String action) {
        switch (action) {
            case "PROFIL":
                etat = EtatPrincipal.OUVERTURE_PROFIL;
                ouvrirProfil();
                break;
                
            case "STATIONNEMENT":
                etat = EtatPrincipal.SELECTION_STATIONNEMENT;
                verifierStationnementActif();
                break;
                
            case "RECHERCHE":
            case "ENTREE_RECHERCHE":
                etat = EtatPrincipal.RECHERCHE_EN_COURS;
                lancerRecherche();
                break;
                
            case "MESSAGERIE":
                etat = EtatPrincipal.MESSAGERIE;
                ouvrirMessagerie();
                break;
        }
    }
    
    private void traiterEtatOuvertureProfil(String action) {
        // Gestion spécifique pendant l'ouverture du profil
        if (action.equals("ANNULER")) {
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    private void traiterEtatSelectionStationnement(String action) {
        switch (action) {
            case "CHOIX_VOIRIE":
                etat = EtatPrincipal.STATIONNEMENT_VOIRIE;
                ouvrirStationnementVoirie();
                break;
                
            case "CHOIX_PARKING":
                etat = EtatPrincipal.STATIONNEMENT_PARKING;
                ouvrirStationnementParking();
                break;
                
            case "ANNULER_STATIONNEMENT":
                etat = EtatPrincipal.ACCUEIL;
                break;
        }
    }
    
    private void traiterEtatStationnementVoirie(String action) {
        // Les actions sont gérées dans le contrôleur de Page_Garer_Voirie
        if (action.equals("RETOUR")) {
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    private void traiterEtatStationnementParking(String action) {
        // Les actions sont gérées dans le contrôleur de Page_Garer_Parking
        if (action.equals("RETOUR")) {
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    private void traiterEtatStationnementEnCours(String action) {
        // Les actions sont gérées dans le contrôleur de Page_Stationnement_En_Cours
        if (action.equals("TERMINER")) {
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    private void traiterEtatRechercheEnCours(String action) {
        // En cours de traitement de la recherche
        if (action.equals("ANNULER_RECHERCHE")) {
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    private void traiterEtatRechercheResultats(String action) {
        // Les résultats sont affichés dans une autre page
        if (action.equals("RETOUR_RECHERCHE")) {
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    private void traiterEtatMessagerie(String action) {
        // La page de messagerie est ouverte
        if (action.equals("RETOUR_MESSAGERIE")) {
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    private void ouvrirProfil() {
        try {
            SwingUtilities.invokeLater(() -> {
                Page_Utilisateur pageUtilisateur = new Page_Utilisateur(emailUtilisateur);
                pageUtilisateur.setVisible(true);
                
                pageUtilisateur.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        etat = EtatPrincipal.ACCUEIL;
                    }
                    
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        etat = EtatPrincipal.ACCUEIL;
                    }
                });
            });
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du profil", e);
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    private void verifierStationnementActif() {
        StationnementControleur stationnementControleur = new StationnementControleur(emailUtilisateur);
        
        if (stationnementControleur.getStationnementActif() != null) {
            etat = EtatPrincipal.STATIONNEMENT_EN_COURS;
            ouvrirStationnementEnCours();
        } else {
            demanderTypeStationnement();
        }
    }
    
    private void demanderTypeStationnement() {
        Object[] options = {"Stationnement en Voirie", "Stationnement en Parking", "Annuler"};
        int choix = JOptionPane.showOptionDialog(vue,
            "Choisissez le type de stationnement :",
            "Nouveau stationnement",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        switch (choix) {
            case 0: // Voirie
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CHOIX_VOIRIE"));
                break;
                
            case 1: // Parking
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CHOIX_PARKING"));
                break;
                
            case 2: // Annuler
            case JOptionPane.CLOSED_OPTION:
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ANNULER_STATIONNEMENT"));
                break;
        }
    }
    
    private void ouvrirStationnementVoirie() {
        try {
            Page_Garer_Voirie page = new Page_Garer_Voirie(emailUtilisateur);
            page.setVisible(true);
            vue.dispose();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du stationnement en voirie", e);
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    private void ouvrirStationnementParking() {
        try {
            Page_Garer_Parking page = new Page_Garer_Parking(emailUtilisateur, null);
            page.setVisible(true);
            vue.dispose();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du stationnement en parking", e);
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    private void ouvrirStationnementEnCours() {
        try {
            Page_Stationnement_En_Cours page = new Page_Stationnement_En_Cours(emailUtilisateur);
            page.setVisible(true);
            vue.dispose();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du stationnement en cours", e);
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    private void lancerRecherche() {
        String recherche = vue.getSearchField().getText().trim();
        
        if (!recherche.isEmpty() && !recherche.equals("Rechercher un parking...")) {
            etat = EtatPrincipal.RECHERCHE_RESULTATS;
            ouvrirResultatsRecherche(recherche);
        } else {
            JOptionPane.showMessageDialog(vue, 
                "Veuillez saisir un terme de recherche", 
                "Recherche vide", 
                JOptionPane.WARNING_MESSAGE);
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    private void ouvrirResultatsRecherche(String recherche) {
        try {
            Page_Resultats_Recherche page = new Page_Resultats_Recherche(emailUtilisateur, recherche);
            page.setVisible(true);
            vue.dispose();
        } catch (Exception e) {
            afficherErreur("Erreur lors de la recherche", e);
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    private void ouvrirMessagerie() {
        try {
            SwingUtilities.invokeLater(() -> {
                Page_Feedback pageFeedback = new Page_Feedback(emailUtilisateur, vue);
                pageFeedback.setVisible(true);
                
                // Cacher la page principale (pas de dispose!)
                vue.setVisible(false);
                
                pageFeedback.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        etat = EtatPrincipal.ACCUEIL;
                        // Réafficher la même page principale
                        vue.setVisible(true);
                        vue.updateMessagerieIcon();
                    }
                    
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        etat = EtatPrincipal.ACCUEIL;
                        // Réafficher la même page principale
                        vue.setVisible(true);
                        vue.updateMessagerieIcon();
                    }
                });
            });
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture de la messagerie", e);
            etat = EtatPrincipal.ACCUEIL;
        }
    }

    private void ouvrirPagePrincipale() {
        SwingUtilities.invokeLater(() -> {
            Page_Principale nouvellePage = new Page_Principale(emailUtilisateur);
            nouvellePage.setVisible(true);
        });
    }
    
    private void afficherErreur(String message, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(vue,
            message + ": " + e.getMessage(),
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
    }
    
    // Getter pour l'état courant
    public EtatPrincipal getEtatCourant() {
        return etat;
    }
    
    // Getter pour l'email utilisateur
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }
}
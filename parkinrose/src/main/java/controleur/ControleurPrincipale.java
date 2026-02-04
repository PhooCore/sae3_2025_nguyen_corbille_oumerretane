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

/**
 * Contrôleur principal de l'application gérant la page d'accueil.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Principale
 * et les différentes pages de l'application.
 * Gère la navigation entre les différentes fonctionnalités : profil, stationnement, recherche et messagerie.
 * 
 * @author Équipe 7
 */
public class ControleurPrincipale implements ActionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur.
     * Permet de suivre l'état de navigation et les actions en cours.
     */
    private enum EtatPrincipal {
        /** État d'accueil, aucune action en cours */
        ACCUEIL,
        /** Ouverture de la page profil utilisateur */
        OUVERTURE_PROFIL,
        /** Sélection du type de stationnement (voirie/parking) */
        SELECTION_STATIONNEMENT,
        /** Préparation d'un stationnement en voirie */
        STATIONNEMENT_VOIRIE,
        /** Préparation d'un stationnement en parking */
        STATIONNEMENT_PARKING,
        /** Consultation d'un stationnement en cours */
        STATIONNEMENT_EN_COURS,
        /** Recherche de parking en cours */
        RECHERCHE_EN_COURS,
        /** Affichage des résultats de recherche */
        RECHERCHE_RESULTATS,
        /** Consultation de la messagerie */
        MESSAGERIE
    }
    
    private Page_Principale vue;
    private String emailUtilisateur;
    private EtatPrincipal etat;
    
    /**
     * Constructeur du contrôleur principal.
     * Initialise le contrôleur avec la vue et l'email de l'utilisateur.
     * 
     * @param vue la page principale de l'interface graphique
     * @param email l'email de l'utilisateur connecté
     */
    public ControleurPrincipale(Page_Principale vue, String email) {
        this.vue = vue;
        this.emailUtilisateur = email;
        this.etat = EtatPrincipal.ACCUEIL;
        configurerListeners();
    }
    
    /**
     * Configure tous les écouteurs d'événements pour les composants de la vue.
     * Connecte les boutons et le champ de recherche aux actions appropriées.
     */
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
    
    /**
     * Gère les événements d'action en fonction de l'état courant du contrôleur.
     * Dispatche les actions vers les méthodes appropriées selon l'état.
     * 
     * @param e l'événement d'action
     */
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
    
    /**
     * Détermine l'action à partir de la source de l'événement.
     * Identifie quel bouton ou composant a déclenché l'événement.
     * 
     * @param e l'événement d'action
     * @return une chaîne identifiant l'action
     */
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
    
    /**
     * Traite les actions lorsque le contrôleur est en état ACCUEIL.
     * Gère les clics sur les différents boutons de la page principale.
     * 
     * @param action l'action à traiter
     */
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
    
    /**
     * Traite les actions lorsque le profil utilisateur est en cours d'ouverture.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatOuvertureProfil(String action) {
        if (action.equals("ANNULER")) {
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    /**
     * Traite les actions lors de la sélection du type de stationnement.
     * Gère le choix entre voirie et parking.
     * 
     * @param action l'action à traiter
     */
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
    
    /**
     * Traite les actions pendant la préparation d'un stationnement en voirie.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatStationnementVoirie(String action) {
        if (action.equals("RETOUR")) {
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    /**
     * Traite les actions pendant la préparation d'un stationnement en parking.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatStationnementParking(String action) {
        if (action.equals("RETOUR")) {
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    /**
     * Traite les actions pendant la consultation d'un stationnement en cours.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatStationnementEnCours(String action) {
        if (action.equals("TERMINER")) {
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    /**
     * Traite les actions pendant une recherche en cours.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatRechercheEnCours(String action) {
        if (action.equals("ANNULER_RECHERCHE")) {
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    /**
     * Traite les actions lors de l'affichage des résultats de recherche.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatRechercheResultats(String action) {
        if (action.equals("RETOUR_RECHERCHE")) {
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    /**
     * Traite les actions pendant la consultation de la messagerie.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatMessagerie(String action) {
        if (action.equals("RETOUR_MESSAGERIE")) {
            etat = EtatPrincipal.ACCUEIL;
        }
    }
    
    /**
     * Ouvre la page de profil utilisateur de manière asynchrone.
     * Configure un écouteur pour revenir à l'état ACCUEIL à la fermeture.
     */
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
    
    /**
     * Vérifie si l'utilisateur a un stationnement actif.
     * Si oui, ouvre la page de stationnement en cours.
     * Si non, demande le type de stationnement souhaité.
     */
    private void verifierStationnementActif() {
        StationnementControleur stationnementControleur = new StationnementControleur(emailUtilisateur);
        
        if (stationnementControleur.getStationnementActif() != null) {
            etat = EtatPrincipal.STATIONNEMENT_EN_COURS;
            ouvrirStationnementEnCours();
        } else {
            demanderTypeStationnement();
        }
    }
    
    /**
     * Affiche une boîte de dialogue pour demander à l'utilisateur
     * quel type de stationnement il souhaite créer (voirie ou parking).
     */
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
            case 0:
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CHOIX_VOIRIE"));
                break;
                
            case 1:
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CHOIX_PARKING"));
                break;
                
            case 2:
            case JOptionPane.CLOSED_OPTION:
                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ANNULER_STATIONNEMENT"));
                break;
        }
    }
    
    /**
     * Ouvre la page de préparation d'un stationnement en voirie.
     * Ferme la page principale actuelle.
     */
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
    
    /**
     * Ouvre la page de préparation d'un stationnement en parking.
     * Ferme la page principale actuelle.
     */
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
    
    /**
     * Ouvre la page de consultation du stationnement en cours.
     * Ferme la page principale actuelle.
     */
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
    
    /**
     * Lance une recherche de parking à partir du texte saisi.
     * Valide que le champ de recherche n'est pas vide avant d'ouvrir les résultats.
     */
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
    
    /**
     * Ouvre la page des résultats de recherche avec le terme recherché.
     * Ferme la page principale actuelle.
     * 
     * @param recherche le terme de recherche saisi par l'utilisateur
     */
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
    
    /**
     * Ouvre la page de messagerie (feedback) de manière asynchrone.
     * Masque la page principale sans la fermer pour pouvoir y revenir.
     * Configure un écouteur pour réafficher la page principale à la fermeture.
     */
    private void ouvrirMessagerie() {
        try {
            SwingUtilities.invokeLater(() -> {
                Page_Feedback pageFeedback = new Page_Feedback(emailUtilisateur, vue);
                pageFeedback.setVisible(true);
                
                vue.setVisible(false);
                
                pageFeedback.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        etat = EtatPrincipal.ACCUEIL;
                        vue.setVisible(true);
                        vue.updateMessagerieIcon();
                    }
                    
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        etat = EtatPrincipal.ACCUEIL;
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

    /**
     * Ouvre une nouvelle instance de la page principale.
     * Utilisé pour rafraîchir la page principale après certaines actions.
     */
    private void ouvrirPagePrincipale() {
        SwingUtilities.invokeLater(() -> {
            Page_Principale nouvellePage = new Page_Principale(emailUtilisateur);
            nouvellePage.setVisible(true);
        });
    }
    
    /**
     * Affiche un message d'erreur dans une boîte de dialogue.
     * Log également l'exception dans la console.
     * 
     * @param message le message d'erreur à afficher
     * @param e l'exception survenue
     */
    private void afficherErreur(String message, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(vue,
            message + ": " + e.getMessage(),
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Retourne l'état courant du contrôleur.
     * 
     * @return l'état courant
     */
    public EtatPrincipal getEtatCourant() {
        return etat;
    }
    
    /**
     * Retourne l'email de l'utilisateur connecté.
     * 
     * @return l'email de l'utilisateur
     */
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }
}
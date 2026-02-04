package controleur;

import ihm.Page_Modif_MDP;
import ihm.Page_Utilisateur;
import ihm.Page_Authentification;
import modele.dao.ModifMdpDAO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Contrôleur gérant la modification du mot de passe utilisateur.
 * Valide le formulaire, vérifie l'email et l'ancien mot de passe si nécessaire,
 * puis effectue la modification dans la base de données.
 * Gère deux contextes : modification depuis le compte utilisateur (authentifié)
 * ou réinitialisation depuis la page d'authentification (email éditable).
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Modif_MDP
 * et le modèle (ModifMdpDAO).
 * 
 * @author Équipe 7
 */
public class ControleurModificationMDP implements ActionListener {
    
    /**
     * Énumération des différents états possibles du processus de modification de mot de passe.
     * Permet de suivre le cycle de vie de la modification.
     */
    private enum Etat {
        /** État initial, formulaire prêt à être rempli */
        INITIAL,
        /** Validation des champs du formulaire en cours */
        VALIDATION_FORMULAIRE,
        /** Vérification de l'existence de l'email en cours */
        VERIFICATION_EMAIL,
        /** Vérification de l'ancien mot de passe en cours (utilisateur connecté) */
        VERIFICATION_ANCIEN_MDP,
        /** Demande de confirmation à l'utilisateur */
        CONFIRMATION,
        /** Modification du mot de passe en cours */
        MODIFICATION,
        /** Modification réussie */
        REUSSITE,
        /** Redirection vers une autre page */
        REDIRECTION,
        /** Une erreur s'est produite */
        ERREUR
    }
    
    private Page_Modif_MDP vue;
    private Etat etat;
    private ModifMdpDAO modifMdpDAO;
    private String email;
    private String nouveauMotDePasse;
    
    /**
     * Constructeur du contrôleur de modification de mot de passe.
     * Initialise le DAO et configure les écouteurs.
     * 
     * @param vue la page d'interface graphique de modification de mot de passe
     */
    public ControleurModificationMDP(Page_Modif_MDP vue) {
        this.vue = vue;
        this.etat = Etat.INITIAL;
        this.modifMdpDAO = new ModifMdpDAO();
        
        initialiserControleur();
    }
    
    /**
     * Initialise le contrôleur en configurant les écouteurs d'événements.
     */
    private void initialiserControleur() {
        configurerListeners();
        etat = Etat.INITIAL;
    }
    
    /**
     * Configure les écouteurs pour les boutons de la vue.
     */
    private void configurerListeners() {
        vue.getBtnModifier().addActionListener(this);
        vue.getBtnRetour().addActionListener(this);
    }
    
    /**
     * Gère les événements d'action des composants de la vue.
     * Route les actions vers les méthodes appropriées en fonction de l'état actuel.
     * 
     * @param e l'événement d'action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        switch (etat) {
            case INITIAL:
                if (source == vue.getBtnModifier()) {
                    lancerModificationMDP();
                } else if (source == vue.getBtnRetour()) {
                    retour();
                }
                break;
                
            case VALIDATION_FORMULAIRE:
            case VERIFICATION_EMAIL:
            case VERIFICATION_ANCIEN_MDP:
            case CONFIRMATION:
            case MODIFICATION:
                break;
                
            case REUSSITE:
                break;
                
            case ERREUR:
                if (source == vue.getBtnRetour()) {
                    retour();
                }
                break;
        }
    }
    
    /**
     * Lance le processus complet de modification du mot de passe.
     * Enchaîne les étapes : validation, vérification email, vérification ancien MDP,
     * confirmation et modification effective.
     */
    private void lancerModificationMDP() {
        etat = Etat.VALIDATION_FORMULAIRE;
        
        this.email = vue.getTxtEmail().getText().trim();
        this.nouveauMotDePasse = new String(vue.getPasswordFieldNouveau().getPassword());
        String confirmationMotDePasse = new String(vue.getPasswordFieldConfirmer().getPassword());
        
        if (!validerFormulaire(email, nouveauMotDePasse, confirmationMotDePasse)) {
            etat = Etat.ERREUR;
            return;
        }
        
        etat = Etat.VERIFICATION_EMAIL;
        if (!verifierEmail(email)) {
            etat = Etat.ERREUR;
            return;
        }
        
        if (!vue.getTxtEmail().isEditable()) {
            etat = Etat.VERIFICATION_ANCIEN_MDP;
            if (!verifierAncienMotDePasse()) {
                etat = Etat.ERREUR;
                return;
            }
        }
        
        etat = Etat.CONFIRMATION;
        if (!demanderConfirmation()) {
            etat = Etat.INITIAL;
            return;
        }
        
        etat = Etat.MODIFICATION;
        effectuerModification();
    }
    
    /**
     * Valide le formulaire de modification de mot de passe.
     * Vérifie que tous les champs sont remplis, que les mots de passe correspondent
     * et que le mot de passe a au moins 6 caractères.
     * 
     * @param email l'adresse email
     * @param nouveauMdp le nouveau mot de passe
     * @param confirmationMdp la confirmation du nouveau mot de passe
     * @return true si le formulaire est valide, false sinon avec affichage d'erreur
     */
    private boolean validerFormulaire(String email, String nouveauMdp, String confirmationMdp) {
        if (email.isEmpty()) {
            afficherErreur("Veuillez saisir votre email", "Champ manquant");
            return false;
        }
        
        if (nouveauMdp.isEmpty()) {
            afficherErreur("Veuillez saisir le nouveau mot de passe", "Champ manquant");
            return false;
        }
        
        if (confirmationMdp.isEmpty()) {
            afficherErreur("Veuillez confirmer le nouveau mot de passe", "Champ manquant");
            return false;
        }
        
        if (!nouveauMdp.equals(confirmationMdp)) {
            afficherErreur("Les mots de passe ne correspondent pas", "Erreur de correspondance");
            reinitialiserChampsMDP();
            return false;
        }
        
        if (nouveauMdp.length() < 6) {
            afficherErreur("Le mot de passe doit contenir au moins 6 caractères", "Mot de passe trop court");
            reinitialiserChampsMDP();
            return false;
        }
        
        return true;
    }
    
    /**
     * Vérifie que l'adresse email existe dans la base de données.
     * 
     * @param email l'adresse email à vérifier
     * @return true si l'email existe, false sinon avec affichage d'erreur
     */
    private boolean verifierEmail(String email) {
        if (!email.contains("@") || !email.contains(".")) {
            afficherErreur("Veuillez saisir un email valide", "Email invalide");
            return false;
        }
        
        boolean emailExiste = modifMdpDAO.verifierEmailExiste(email);
        
        if (!emailExiste) {
            afficherErreur("Email non trouvé dans notre système", "Email inconnu");
            return false;
        }
        
        return true;
    }
    
    /**
     * Vérifie l'ancien mot de passe de l'utilisateur pour des raisons de sécurité.
     * Affiche un dialogue pour demander l'ancien mot de passe et le vérifie
     * contre la base de données.
     * 
     * @return true si l'ancien mot de passe est correct, false sinon
     */
    private boolean verifierAncienMotDePasse() {
        String ancienMotDePasse = demanderAncienMotDePasseDialogue();
        
        if (ancienMotDePasse == null) {
            return false;
        }
        
        boolean estCorrect = modifMdpDAO.verifierAncienMotDePasse(email, ancienMotDePasse);
        
        if (!estCorrect) {
            afficherErreur("L'ancien mot de passe est incorrect", "Erreur de vérification");
            reinitialiserChampsMDP();
            return false;
        }
        
        return true;
    }
    
    /**
     * Affiche un dialogue pour demander l'ancien mot de passe de l'utilisateur.
     * 
     * @return l'ancien mot de passe saisi, ou null si l'utilisateur annule
     */
    private String demanderAncienMotDePasseDialogue() {
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {
            "Pour des raisons de sécurité, veuillez confirmer votre ancien mot de passe:",
            passwordField
        };
        
        int option = JOptionPane.showConfirmDialog(vue,
            message,
            "Confirmation de sécurité",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            String mdp = new String(passwordField.getPassword());
            return mdp.isEmpty() ? null : mdp;
        }
        
        return null;
    }
    
    /**
     * Demande confirmation à l'utilisateur avant d'effectuer la modification.
     * 
     * @return true si l'utilisateur confirme, false sinon
     */
    private boolean demanderConfirmation() {
        String message = "Êtes-vous sûr de vouloir modifier votre mot de passe ?\n" +
                        "Vous devrez utiliser ce nouveau mot de passe pour vos prochaines connexions.";
        
        int confirmation = JOptionPane.showConfirmDialog(vue,
            message,
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        return confirmation == JOptionPane.YES_OPTION;
    }
    
    /**
     * Effectue la modification du mot de passe dans la base de données.
     * Affiche un message de succès ou d'erreur selon le résultat.
     */
    private void effectuerModification() {
        boolean modificationReussie = modifMdpDAO.modifierMotDePasse(email, nouveauMotDePasse);
        
        if (modificationReussie) {
            etat = Etat.REUSSITE;
            afficherSucces();
        } else {
            afficherErreur("Erreur lors de la modification du mot de passe", "Erreur système");
            etat = Etat.ERREUR;
        }
    }
    
    /**
     * Affiche un message de succès après la modification et redirige l'utilisateur.
     * Le message inclut l'email modifié et la date de modification.
     */
    private void afficherSucces() {
        String message = "<html><div style='text-align: center;'>"
                + "<h2 style='color: green;'>Mot de passe modifié !</h2>"
                + "<p>Votre mot de passe a été modifié avec succès.</p>"
                + "<br>"
                + "<div style='background-color: #f0f8ff; padding: 15px; border-radius: 5px; text-align: left;'>"
                + "<p><b>Email:</b> " + email + "</p>"
                + "<p><b>Date de modification:</b> " + java.time.LocalDate.now() + "</p>"
                + "<p><b>Sécurité:</b> Vous devez utiliser votre nouveau mot de passe pour vous connecter.</p>"
                + "</div>"
                + "</div></html>";
        
        JOptionPane.showMessageDialog(vue,
            message,
            "Succès",
            JOptionPane.INFORMATION_MESSAGE);
        
        etat = Etat.REDIRECTION;
        retour();
    }
    
    /**
     * Gère le retour vers la page appropriée selon le contexte.
     * Si une page parente existe, y retourne. Sinon, retourne vers la page utilisateur
     * ou la page d'authentification selon l'état du champ email.
     */
    private void retour() {
        etat = Etat.REDIRECTION;
        
        Page_Utilisateur pageParente = vue.getPageParente();
        
        if (pageParente != null) {
            retourVersPageParente(pageParente);
        } else if (!vue.getTxtEmail().isEditable()) {
            retourVersPageUtilisateur();
        } else {
            retourVersAuthentification();
        }
        
        vue.dispose();
    }
    
    /**
     * Retourne vers la page utilisateur parente.
     * 
     * @param pageParente la page utilisateur à réafficher
     */
    private void retourVersPageParente(Page_Utilisateur pageParente) {
        pageParente.setVisible(true);
    }
    
    /**
     * Retourne vers une nouvelle page utilisateur.
     */
    private void retourVersPageUtilisateur() {
        Page_Utilisateur pageUtilisateur = new Page_Utilisateur(email);
        pageUtilisateur.setVisible(true);
    }
    
    /**
     * Retourne vers la page d'authentification.
     */
    private void retourVersAuthentification() {
        Page_Authentification pageAuth = new Page_Authentification();
        pageAuth.setVisible(true);
    }
    
    /**
     * Réinitialise les champs de mot de passe et place le focus.
     */
    private void reinitialiserChampsMDP() {
        vue.getPasswordFieldNouveau().setText("");
        vue.getPasswordFieldConfirmer().setText("");
        vue.getPasswordFieldNouveau().requestFocus();
    }
    
    /**
     * Affiche un message d'erreur dans une boîte de dialogue.
     * 
     * @param message le message d'erreur
     * @param titre le titre de la boîte de dialogue
     */
    private void afficherErreur(String message, String titre) {
        JOptionPane.showMessageDialog(vue,
            message,
            titre,
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Retourne l'état actuel du contrôleur.
     * Utile pour le débogage et les tests.
     * 
     * @return l'état actuel
     */
    public Etat getEtat() {
        return etat;
    }
    
    /**
     * Retourne l'email en cours de modification.
     * 
     * @return l'email
     */
    public String getEmail() {
        return email;
    }
}
package controleur;

import ihm.Page_Modif_MDP;
import ihm.Page_Utilisateur;
import ihm.Page_Authentification;
import modele.dao.ModifMdpDAO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControleurModificationMDP implements ActionListener {
    
    // États du contrôleur
    private enum Etat {
        INITIAL,
        VALIDATION_FORMULAIRE,
        VERIFICATION_EMAIL,
        VERIFICATION_ANCIEN_MDP,
        CONFIRMATION,
        MODIFICATION,
        REUSSITE,
        REDIRECTION,
        ERREUR
    }
    
    // Références
    private Page_Modif_MDP vue;
    private Etat etat;
    
    // DAO
    private ModifMdpDAO modifMdpDAO;
    
    // Données
    private String email;
    private String nouveauMotDePasse;
    
    public ControleurModificationMDP(Page_Modif_MDP vue) {
        this.vue = vue;
        this.etat = Etat.INITIAL;
        this.modifMdpDAO = new ModifMdpDAO();
        
        initialiserControleur();
    }
    
    private void initialiserControleur() {
        configurerListeners();
        etat = Etat.INITIAL;
    }
    
    private void configurerListeners() {
        vue.getBtnModifier().addActionListener(this);
        vue.getBtnRetour().addActionListener(this);
    }
    
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
                // En cours de traitement
                break;
                
            case REUSSITE:
                // Le succès est géré dans afficherSucces()
                break;
                
            case ERREUR:
                if (source == vue.getBtnRetour()) {
                    retour();
                }
                break;
        }
    }
    
    private void lancerModificationMDP() {
        etat = Etat.VALIDATION_FORMULAIRE;
        
        // Récupérer les données
        this.email = vue.getTxtEmail().getText().trim();
        this.nouveauMotDePasse = new String(vue.getPasswordFieldNouveau().getPassword());
        String confirmationMotDePasse = new String(vue.getPasswordFieldConfirmer().getPassword());
        
        // Validation du formulaire
        if (!validerFormulaire(email, nouveauMotDePasse, confirmationMotDePasse)) {
            etat = Etat.ERREUR;
            return;
        }
        
        etat = Etat.VERIFICATION_EMAIL;
        if (!verifierEmail(email)) {
            etat = Etat.ERREUR;
            return;
        }
        
        // Si l'utilisateur est connecté (email non éditable)
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
    
    private boolean verifierAncienMotDePasse() {
        String ancienMotDePasse = demanderAncienMotDePasseDialogue();
        
        if (ancienMotDePasse == null) {
            // L'utilisateur a annulé
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
    
    private boolean demanderConfirmation() {
        String message = "Êtes-vous sûr de vouloir modifier votre mot de passe ?\n" +
                        "Vous devrez utiliser ce nouveau mot de passe pour vos prochaines connexions.";
        
        int confirmation = JOptionPane.showConfirmDialog(vue,
            message,
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        return confirmation == JOptionPane.YES_OPTION;
    }
    
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
        
        // Après le succès, rediriger
        etat = Etat.REDIRECTION;
        retour();
    }
    
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
    
    private void retourVersPageParente(Page_Utilisateur pageParente) {
        pageParente.setVisible(true);
    }
    
    private void retourVersPageUtilisateur() {
        Page_Utilisateur pageUtilisateur = new Page_Utilisateur(email);
        pageUtilisateur.setVisible(true);
    }
    
    private void retourVersAuthentification() {
        Page_Authentification pageAuth = new Page_Authentification();
        pageAuth.setVisible(true);
    }
    
    private void reinitialiserChampsMDP() {
        vue.getPasswordFieldNouveau().setText("");
        vue.getPasswordFieldConfirmer().setText("");
        vue.getPasswordFieldNouveau().requestFocus();
    }
    
    private void afficherErreur(String message, String titre) {
        JOptionPane.showMessageDialog(vue,
            message,
            titre,
            JOptionPane.ERROR_MESSAGE);
    }
    
    // Getters pour débogage
    public Etat getEtat() {
        return etat;
    }
    
    public String getEmail() {
        return email;
    }
}
package controleur;

import ihm.Page_Modif_MDP;
import ihm.Page_Utilisateur;
import ihm.Page_Authentification;
import modele.dao.ModifMdpDAO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControleurModificationMDP implements ActionListener {
    
    private Page_Modif_MDP vue;
    private ModifMdpDAO modifMdpDAO;
    
    public ControleurModificationMDP(Page_Modif_MDP vue) {
        this.vue = vue;
        this.modifMdpDAO = new ModifMdpDAO();
        
        configurerListeners();
    }
    
    private void configurerListeners() {
        // Configuration directe des boutons
        vue.getBtnModifier().addActionListener(this);
        vue.getBtnRetour().addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == vue.getBtnModifier()) {
            modifierMotDePasse();
        } else if (source == vue.getBtnRetour()) {
            retourProfil();
        }
    }
    
    private void modifierMotDePasse() {
        // Récupération des données
        String email = vue.getTxtEmail().getText().trim();
        String nouveauMotDePasse = new String(vue.getPasswordFieldNouveau().getPassword());
        String confirmerMotDePasse = new String(vue.getPasswordFieldConfirmer().getPassword());
        
        // 1. Validation des champs
        if (!validerFormulaire(email, nouveauMotDePasse, confirmerMotDePasse)) {
            return;
        }
        
        // 2. Vérification de l'existence de l'email
        boolean emailExiste = modifMdpDAO.verifierEmailExiste(email);
        
        if (!emailExiste) {
            vue.afficherMessage("Email non trouvé dans notre système", 
                "Email inconnu", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 3. Si l'utilisateur est connecté (email non éditable), demander l'ancien mot de passe
        if (!vue.getTxtEmail().isEditable()) {
            String ancienMotDePasse = demanderAncienMotDePasse();
            if (ancienMotDePasse == null) {
                return; // L'utilisateur a annulé
            }
            
            // Vérifier l'ancien mot de passe
            boolean ancienMdpCorrect = modifMdpDAO.verifierAncienMotDePasse(email, ancienMotDePasse);
            if (!ancienMdpCorrect) {
                vue.afficherMessage("L'ancien mot de passe est incorrect", 
                    "Erreur de vérification", JOptionPane.ERROR_MESSAGE);
                vue.viderChamps();
                return;
            }
        }
        
        // 4. Demande de confirmation
        int confirmation = vue.demanderConfirmation(
            "Êtes-vous sûr de vouloir modifier votre mot de passe ?\n" +
            "Vous devrez utiliser ce nouveau mot de passe pour vos prochaines connexions.",
            "Confirmation de modification");
        
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        
        // 5. Modifier le mot de passe
        boolean modificationReussie = modifMdpDAO.modifierMotDePasse(email, nouveauMotDePasse);
        
        if (modificationReussie) {
            afficherConfirmation(email);
            retourProfil();
        } else {
            vue.afficherMessage("Erreur lors de la modification du mot de passe", 
                "Erreur système", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validerFormulaire(String email, String nouveauMotDePasse, String confirmerMotDePasse) {
        // Validation des champs obligatoires
        if (email.isEmpty()) {
            vue.afficherMessage("Veuillez saisir votre email",
                "Champ manquant", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (nouveauMotDePasse.isEmpty()) {
            vue.afficherMessage("Veuillez saisir le nouveau mot de passe",
                "Champ manquant", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (confirmerMotDePasse.isEmpty()) {
            vue.afficherMessage("Veuillez confirmer le nouveau mot de passe",
                "Champ manquant", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validation de la correspondance des mots de passe
        if (!nouveauMotDePasse.equals(confirmerMotDePasse)) {
            vue.afficherMessage("Les mots de passe ne correspondent pas",
                "Erreur de correspondance", JOptionPane.ERROR_MESSAGE);
            vue.viderChamps();
            return false;
        }
        
        // Validation de la longueur minimale
        if (nouveauMotDePasse.length() < 6) {
            vue.afficherMessage("Le mot de passe doit contenir au moins 6 caractères",
                "Mot de passe trop court", JOptionPane.ERROR_MESSAGE);
            vue.viderChamps();
            return false;
        }
        
        // Validation du format d'email
        if (!email.contains("@") || !email.contains(".")) {
            vue.afficherMessage("Veuillez saisir un email valide",
                "Email invalide", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private String demanderAncienMotDePasse() {
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
        } else {
            return null;
        }
    }
    
    private void afficherConfirmation(String email) {
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
            "Modification réussie",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void retourProfil() {
        Page_Utilisateur pageParente = vue.getPageParente();
        
        if (pageParente != null) {
            // Retour à la page utilisateur parente
            pageParente.setVisible(true);
        } else if (!vue.getTxtEmail().isEditable()) {
            // Utilisateur connecté mais pas de page parente
            String email = vue.getTxtEmail().getText().trim();
            Page_Utilisateur pageUtilisateur = new Page_Utilisateur(email);
            pageUtilisateur.setVisible(true);
        } else {
            // Utilisateur non connecté
            Page_Authentification pageAuth = new Page_Authentification();
            pageAuth.setVisible(true);
        }
        
        vue.dispose();
    }
}
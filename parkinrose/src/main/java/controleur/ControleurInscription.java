package controleur;

import modele.Usager;
import modele.dao.UsagerDAO;
import ihm.Page_Inscription;
import ihm.Page_Authentification;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControleurInscription implements ActionListener {
    
    private Page_Inscription vue;
    
    public ControleurInscription(Page_Inscription vue) {
        this.vue = vue;
        configurerListeners();
    }
    
    private void configurerListeners() {
        // Récupérer les boutons spécifiques de la vue
        vue.getBtnRetour().addActionListener(this);
        vue.getBtnCreerCompte().addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == vue.getBtnRetour()) {
            retourAuthentification();
        } else if (source == vue.getBtnCreerCompte()) {
            creerCompte();
        }
    }
    
    private void creerCompte() {
        String nom = vue.getTextFieldNom().getText().trim();
        String prenom = vue.getTextFieldPrenom().getText().trim();
        String email = vue.getTextFieldEmail().getText().trim();
        String motDePasse = new String(vue.getPasswordField().getPassword());
        String confirmation = new String(vue.getPasswordFieldConfirm().getPassword());

        if (!validerChamps(nom, prenom, email, motDePasse, confirmation)) {
            return;
        }
        
        if (UsagerDAO.emailExisteDeja(email)) {
            JOptionPane.showMessageDialog(vue, 
                "Cet email est déjà utilisé", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Créer l'usager avec isAdmin = false par défaut
        Usager nouvelUsager = new Usager(nom, prenom, email, motDePasse);
        nouvelUsager.setAdmin(false); // Par défaut, ce n'est pas un admin
        
        boolean succes = UsagerDAO.ajouterUsager(nouvelUsager);
        
        if (succes) {
            JOptionPane.showMessageDialog(vue, 
                "Compte créé avec succès !", 
                "Succès", 
                JOptionPane.INFORMATION_MESSAGE);

            // Retour à l'authentification après création réussie
            retourAuthentification();
        } else {
            JOptionPane.showMessageDialog(vue, 
                "Erreur lors de la création du compte", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void retourAuthentification() {
        // Créer et afficher la page d'authentification
        Page_Authentification pageAuthentification = new Page_Authentification();
        pageAuthentification.setVisible(true);
        
        // Fermer la page d'inscription actuelle
        vue.dispose();
    }
    
    private boolean validerChamps(String nom, String prenom, String email, 
                                 String motDePasse, String confirmation) {
        
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || 
            motDePasse.isEmpty() || confirmation.isEmpty()) {
            
            JOptionPane.showMessageDialog(vue, 
                "Veuillez remplir tous les champs", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!motDePasse.equals(confirmation)) {
            JOptionPane.showMessageDialog(vue, 
                "Les mots de passe ne correspondent pas", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (motDePasse.length() < 4) {
            JOptionPane.showMessageDialog(vue, 
                "Le mot de passe doit contenir au moins 4 caractères", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validation d'email plus robuste
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex)) {
            JOptionPane.showMessageDialog(vue, 
                "Veuillez saisir un email valide", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
}
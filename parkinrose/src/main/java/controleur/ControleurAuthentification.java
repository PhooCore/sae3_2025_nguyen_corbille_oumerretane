package controleur;

import ihm.Page_Authentification;
import ihm.Page_Principale;
import ihm.Page_Inscription;
import javax.swing.*;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import modele.dao.UsagerDAO;
import modele.Usager;

public class ControleurAuthentification implements ActionListener {
    
    private Page_Authentification vue;
    
    public ControleurAuthentification(Page_Authentification vue) {
        this.vue = vue;
        configurerListeners();
    }
    
    private void configurerListeners() {
        // Ajouter l'action listener au bouton de connexion
        vue.btnLogin.addActionListener(this);
        
        // Ajouter un MouseListener pour le label "Mot de passe oublié" (clic)
        vue.lblForgotPassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                motDePasseOublie();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                vue.lblForgotPassword.setForeground(Color.BLUE);
                vue.lblForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                vue.lblForgotPassword.setForeground(Color.DARK_GRAY);
            }
        });
        
        // Ajouter un MouseListener pour le label "Créer un compte" (clic)
        vue.lblCreateAccount.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                creerCompte();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                vue.lblCreateAccount.setForeground(new Color(0, 70, 180));
                vue.lblCreateAccount.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                vue.lblCreateAccount.setForeground(new Color(0, 100, 200));
            }
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = Page_Authentification.getActionBouton((JButton) e.getSource());
        
        if (action.equals("CONNEXION")) {
            connexion();
        }
    }
    
    private void connexion() {
        String email = vue.txtEmail.getText().trim();
        String password = new String(vue.txtPassword.getPassword()).trim();
        
        // Validation des champs
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(vue, 
                "Veuillez remplir tous les champs.", 
                "Champs manquants", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Vérifier si l'email existe dans la base
        if (!UsagerDAO.emailExisteDeja(email)) {
            JOptionPane.showMessageDialog(vue, 
                "Aucun compte n'est associé à cet email.", 
                "Compte introuvable", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Récupérer l'usager depuis la base de données
        Usager usager = UsagerDAO.getUsagerByEmail(email);
        
        if (usager == null) {
            JOptionPane.showMessageDialog(vue, 
                "Erreur lors de la récupération des données utilisateur.", 
                "Erreur technique", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Vérifier le mot de passe (attention: stocké en clair dans votre base)
        String motDePasseStocke = usager.getMotDePasse();
        
        if (motDePasseStocke != null && motDePasseStocke.equals(password)) {
            // SUCCÈS : Fermer la page d'authentification et ouvrir la page principale
            JOptionPane.showMessageDialog(vue, 
                "Connexion réussie ! Bienvenue " + usager.getPrenomUsager() + " " + usager.getNomUsager() + " !", 
                "Succès", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Ouvrir la page principale avec l'email de l'utilisateur
            Page_Principale pagePrincipale = new Page_Principale(email);
            pagePrincipale.setVisible(true);
            vue.dispose();
        } else {
            // ÉCHEC : Afficher message d'erreur
            JOptionPane.showMessageDialog(vue, 
                "Mot de passe incorrect. Veuillez réessayer.", 
                "Échec de connexion", 
                JOptionPane.ERROR_MESSAGE);
            vue.txtPassword.setText(""); // Vider le champ mot de passe
            vue.txtPassword.requestFocus(); // Remettre le focus sur le champ mot de passe
        }
    }
    
    private void motDePasseOublie() {
        // Logique pour réinitialiser le mot de passe
        String email = JOptionPane.showInputDialog(vue, 
            "Entrez votre adresse email pour réinitialiser votre mot de passe:", 
            "Mot de passe oublié", 
            JOptionPane.QUESTION_MESSAGE);
            
        if (email != null && !email.trim().isEmpty()) {
            // Vérifier si l'email existe dans la base
            if (UsagerDAO.emailExisteDeja(email)) {
                // En production, vous enverriez ici un email de réinitialisation
                // Pour l'instant, on affiche juste un message
                JOptionPane.showMessageDialog(vue, 
                    "Un lien de réinitialisation a été envoyé à l'adresse :\n" + email + 
                    "\n\nVeuillez vérifier votre boîte de réception.", 
                    "Email envoyé", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(vue, 
                    "Aucun compte n'est associé à cette adresse email.", 
                    "Email inconnu", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    private void creerCompte() {
        // Ouvrir la page d'inscription (Page_Inscription)
        Page_Inscription pageInscription = new Page_Inscription();
        pageInscription.setVisible(true);
        
        // Fermer la page d'authentification actuelle
        vue.dispose();
    }
}
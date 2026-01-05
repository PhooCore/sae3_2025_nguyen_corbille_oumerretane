package controleur;

import ihm.Page_Authentification;
import ihm.Page_Principale;
import ihm.Page_Inscription;
import javax.swing.*;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import modele.dao.UsagerDAO;
import modele.Usager;

public class ControleurAuthentification implements ActionListener {
    
    // États possibles du contrôleur
    public enum EtatAuthentification {
        ATTENTE_CONNEXION,
        SAISIE_EMAIL,
        SAISIE_MDP,
        VALIDATION,
        OUBLI_MDP,
        CREATION_COMPTE,
        CONNECTE
    }
    
    private Page_Authentification vue;
    private EtatAuthentification etat;
    private String emailOublieMdp; 
    
    // Constantes pour les messages
    private static final String TITRE_CHAMPS_MANQUANTS = "Champs manquants";
    private static final String TITRE_COMPTE_INTROUVABLE = "Compte introuvable";
    private static final String TITRE_ERREUR_TECHNIQUE = "Erreur technique";
    private static final String TITRE_SUCCES = "Succès";
    private static final String TITRE_ECHEC_CONNEXION = "Échec de connexion";
    private static final String TITRE_EMAIL_ENVOYE = "Email envoyé";
    private static final String TITRE_EMAIL_INCONNU = "Email inconnu";
    
    public ControleurAuthentification(Page_Authentification vue) {
        this.vue = vue;
        this.etat = EtatAuthentification.ATTENTE_CONNEXION;
        configurerListeners();
    }
    
    private void configurerListeners() {
        // Bouton de connexion
        vue.btnLogin.addActionListener(this);
        
        // Entrée dans les champs texte
        vue.txtEmail.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    actionPerformed(new ActionEvent(vue.btnLogin, ActionEvent.ACTION_PERFORMED, "ENTREE_EMAIL"));
                }
            }
        });
        
        vue.txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    actionPerformed(new ActionEvent(vue.btnLogin, ActionEvent.ACTION_PERFORMED, "ENTREE_MDP"));
                }
            }
        });

        configurerLabelHyperlink(vue.lblForgotPassword, Color.BLUE, Color.DARK_GRAY, 
            () -> actionPerformed(new ActionEvent(vue.lblForgotPassword, ActionEvent.ACTION_PERFORMED, "OUBLI_MDP")));
        
        configurerLabelHyperlink(vue.lblCreateAccount, new Color(0, 70, 180), 
            new Color(0, 100, 200), 
            () -> actionPerformed(new ActionEvent(vue.lblCreateAccount, ActionEvent.ACTION_PERFORMED, "CREATION_COMPTE")));
    }
    
    /**
     * Configure un label comme un hyperlien cliquable
     */
    private void configurerLabelHyperlink(JLabel label, Color couleurSurvol, 
                                         Color couleurNormale, Runnable action) {
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                label.setForeground(couleurSurvol);
                label.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                label.setForeground(couleurNormale);
            }
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = "";
        
        // Déterminer l'action en fonction de la source
        if (e.getSource() == vue.btnLogin) {
            action = "CONNEXION";
        } else if (e.getSource() == vue.lblForgotPassword) {
            action = "OUBLI_MDP";
        } else if (e.getSource() == vue.lblCreateAccount) {
            action = "CREATION_COMPTE";
        } else if (e.getActionCommand() != null) {
            action = e.getActionCommand();
        }
        
        // Traiter l'action selon l'état courant
        switch (etat) {
            case ATTENTE_CONNEXION:
            case SAISIE_EMAIL:
            case SAISIE_MDP:
            case VALIDATION:
                switch (action) {
                    case "CONNEXION":
                    case "ENTREE_EMAIL":
                    case "ENTREE_MDP":
                        traiterConnexion();
                        break;
                    case "OUBLI_MDP":
                        traiterOubliMdp();
                        break;
                    case "CREATION_COMPTE":
                        traiterCreationCompte();
                        break;
                }
                break;
                
            case OUBLI_MDP:
                if (action.equals("VALIDATION_EMAIL")) {
                    validerEmailOublie();
                }
                break;
                
            case CREATION_COMPTE:
                break;
                
            case CONNECTE:
                break;
        }
    }
    
    /**
     * Traite la tentative de connexion
     */
    private void traiterConnexion() {
        String email = vue.txtEmail.getText().trim();
        String password = new String(vue.txtPassword.getPassword()).trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            afficherMessage(TITRE_CHAMPS_MANQUANTS, 
                "Veuillez remplir tous les champs.", JOptionPane.WARNING_MESSAGE);
            etat = EtatAuthentification.SAISIE_EMAIL;
            vue.txtEmail.requestFocus();
            return;
        }
        
        if (!UsagerDAO.emailExisteDeja(email)) {
            afficherMessage(TITRE_COMPTE_INTROUVABLE,
                "Aucun compte n'est associé à cet email.", JOptionPane.WARNING_MESSAGE);
            etat = EtatAuthentification.SAISIE_EMAIL;
            vue.txtEmail.requestFocus();
            return;
        }
        
        etat = EtatAuthentification.VALIDATION;
        
        try {
            Usager usager = UsagerDAO.getUsagerByEmail(email);
            
            if (usager == null) {
                afficherMessage(TITRE_ERREUR_TECHNIQUE,
                    "Erreur lors de la récupération des données utilisateur.", 
                    JOptionPane.ERROR_MESSAGE);
                etat = EtatAuthentification.ATTENTE_CONNEXION;
                return;
            }
            
            String motDePasseStocke = usager.getMotDePasse();
            
            if (motDePasseStocke != null && motDePasseStocke.equals(password)) {
                etat = EtatAuthentification.CONNECTE;
                connexionReussie(usager);
            } else {
                afficherMessage(TITRE_ECHEC_CONNEXION,
                    "Mot de passe incorrect. Veuillez réessayer.", 
                    JOptionPane.ERROR_MESSAGE);
                etat = EtatAuthentification.SAISIE_MDP;
                reinitialiserChampMotDePasse();
            }
            
        } catch (Exception ex) {
            gererErreurConnexion(ex);
            etat = EtatAuthentification.ATTENTE_CONNEXION;
        }
    }
    
    /**
     * Gère le processus "mot de passe oublié"
     */
    private void traiterOubliMdp() {
        etat = EtatAuthentification.OUBLI_MDP;
        
        String email = JOptionPane.showInputDialog(vue, 
            "Entrez votre adresse email pour réinitialiser votre mot de passe:", 
            "Mot de passe oublié", 
            JOptionPane.QUESTION_MESSAGE);
        
        if (email != null && !email.trim().isEmpty()) {
            this.emailOublieMdp = email.trim();
            validerEmailOublie();
        } else {
            etat = EtatAuthentification.ATTENTE_CONNEXION;
        }
    }
    
    /**
     * Valide l'email pour la réinitialisation
     */
    private void validerEmailOublie() {
        String email = this.emailOublieMdp;
        
        if (email != null && !email.isEmpty()) {
            if (UsagerDAO.emailExisteDeja(email)) {
                String message = String.format(
                    "Un lien de réinitialisation a été envoyé à l'adresse :\n%s\n\n" +
                    "Veuillez vérifier votre boîte de réception.", email);
                
                afficherMessage(TITRE_EMAIL_ENVOYE, message, JOptionPane.INFORMATION_MESSAGE);
            } else {
                afficherMessage(TITRE_EMAIL_INCONNU,
                    "Aucun compte n'est associé à cette adresse email.",
                    JOptionPane.WARNING_MESSAGE);
            }
        }
        
        this.emailOublieMdp = null;
        etat = EtatAuthentification.ATTENTE_CONNEXION;
    }
    
    /**
     * Ouvre la page d'inscription
     */
    private void traiterCreationCompte() {
        etat = EtatAuthentification.CREATION_COMPTE;
        Page_Inscription pageInscription = new Page_Inscription();
        pageInscription.setVisible(true);
        vue.dispose();
    }
    
    /**
     * Gère une connexion réussie
     */
    private void connexionReussie(Usager usager) {
        afficherMessage(TITRE_SUCCES,
            String.format("Connexion réussie ! Bienvenue %s %s !", 
                usager.getPrenomUsager(), usager.getNomUsager()),
            JOptionPane.INFORMATION_MESSAGE);
        
        Page_Principale pagePrincipale = new Page_Principale(usager.getMailUsager());
        pagePrincipale.setVisible(true);
        
        vue.dispose();
    }
    
    /**
     * Gère les erreurs de connexion
     */
    private void gererErreurConnexion(Exception ex) {
        afficherMessage(TITRE_ERREUR_TECHNIQUE,
            "Une erreur est survenue lors de la connexion: " + ex.getMessage(),
            JOptionPane.ERROR_MESSAGE);
        System.err.println("Erreur de connexion: " + ex.getMessage());
        ex.printStackTrace();
    }
    
    /**
     * Réinitialise le champ mot de passe
     */
    private void reinitialiserChampMotDePasse() {
        vue.txtPassword.setText("");
        vue.txtPassword.requestFocus();
    }
    
    /**
     * Affiche un message dans une boîte de dialogue
     */
    private void afficherMessage(String titre, String message, int typeMessage) {
        JOptionPane.showMessageDialog(vue, message, titre, typeMessage);
    }
    
    public EtatAuthentification getEtatCourant() {
        return etat;
    }
}
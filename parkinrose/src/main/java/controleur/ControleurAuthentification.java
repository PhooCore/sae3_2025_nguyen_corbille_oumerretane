package controleur;

import ihm.Page_Authentification;
import ihm.Page_Principale;
import ihm.Page_Inscription;
import javax.swing.*;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import modele.dao.UsagerDAO;
import modele.Usager;

/**
 * Contrôleur gérant le processus d'authentification des utilisateurs.
 * Gère la connexion, la récupération de mot de passe et la redirection vers l'inscription.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Authentification
 * et le modèle (Usager, UsagerDAO).
 * 
 * @author Équipe 7
 */
public class ControleurAuthentification implements ActionListener {
    
    /**
     * Énumération des différents états possibles du processus d'authentification.
     * Permet de suivre le cycle de vie de la connexion utilisateur.
     */
    public enum EtatAuthentification {
        /** État initial, en attente d'une action de l'utilisateur */
        ATTENTE_CONNEXION,
        /** L'utilisateur est en train de saisir son email */
        SAISIE_EMAIL,
        /** L'utilisateur est en train de saisir son mot de passe */
        SAISIE_MDP,
        /** Validation des identifiants en cours */
        VALIDATION,
        /** Processus de récupération de mot de passe oublié */
        OUBLI_MDP,
        /** Navigation vers la page de création de compte */
        CREATION_COMPTE,
        /** Utilisateur authentifié avec succès */
        CONNECTE
    }
    
    private Page_Authentification vue;
    private EtatAuthentification etat;
    private String emailOublieMdp;
    
    private static final String TITRE_CHAMPS_MANQUANTS = "Champs manquants";
    private static final String TITRE_COMPTE_INTROUVABLE = "Compte introuvable";
    private static final String TITRE_ERREUR_TECHNIQUE = "Erreur technique";
    private static final String TITRE_SUCCES = "Succès";
    private static final String TITRE_ECHEC_CONNEXION = "Échec de connexion";
    private static final String TITRE_EMAIL_ENVOYE = "Email envoyé";
    private static final String TITRE_EMAIL_INCONNU = "Email inconnu";
    
    /**
     * Constructeur du contrôleur d'authentification.
     * Initialise le contrôleur avec la vue associée et configure les écouteurs d'événements.
     * 
     * @param vue la page d'interface graphique d'authentification
     */
    public ControleurAuthentification(Page_Authentification vue) {
        this.vue = vue;
        this.etat = EtatAuthentification.ATTENTE_CONNEXION;
        configurerListeners();
    }
    
    /**
     * Configure tous les écouteurs d'événements pour les composants interactifs de la vue.
     * Connecte le bouton de connexion, les champs de saisie avec validation par Entrée,
     * et les liens hypertextes (mot de passe oublié, création de compte).
     */
    private void configurerListeners() {
        vue.btnLogin.addActionListener(this);
        
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
     * Configure un JLabel pour se comporter comme un lien hypertexte cliquable.
     * Ajoute les effets visuels de survol et l'action à exécuter au clic.
     * 
     * @param label le label à transformer en hyperlien
     * @param couleurSurvol la couleur affichée lors du survol de la souris
     * @param couleurNormale la couleur par défaut du label
     * @param action l'action à exécuter lors du clic
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
    
    /**
     * Gère les événements d'action des composants de la vue.
     * Route les actions vers les méthodes appropriées en fonction de l'état actuel
     * et de la source de l'événement (bouton connexion, liens hypertextes, validation par Entrée).
     * 
     * @param e l'événement d'action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = "";
        
        if (e.getSource() == vue.btnLogin) {
            action = "CONNEXION";
        } else if (e.getSource() == vue.lblForgotPassword) {
            action = "OUBLI_MDP";
        } else if (e.getSource() == vue.lblCreateAccount) {
            action = "CREATION_COMPTE";
        } else if (e.getActionCommand() != null) {
            action = e.getActionCommand();
        }
        
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
     * Traite la tentative de connexion de l'utilisateur.
     * Valide les champs saisis, vérifie l'existence du compte dans la base de données,
     * et compare le mot de passe fourni avec celui stocké.
     * Redirige vers la page principale en cas de succès ou affiche un message d'erreur approprié.
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
     * Gère le processus de récupération de mot de passe oublié.
     * Affiche une boîte de dialogue pour saisir l'email et déclenche la validation.
     * Si l'utilisateur annule, retourne à l'état d'attente de connexion.
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
     * Valide l'email fourni pour la réinitialisation du mot de passe.
     * Vérifie si l'email existe dans la base de données et affiche un message
     * de confirmation ou d'erreur selon le cas.
     * Réinitialise l'email mémorisé et retourne à l'état d'attente après validation.
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
     * Ouvre la page d'inscription pour créer un nouveau compte.
     * Ferme la page d'authentification actuelle et affiche la page d'inscription.
     */
    private void traiterCreationCompte() {
        etat = EtatAuthentification.CREATION_COMPTE;
        Page_Inscription pageInscription = new Page_Inscription();
        pageInscription.setVisible(true);
        vue.dispose();
    }
    
    /**
     * Gère le succès de la connexion utilisateur.
     * Affiche un message de bienvenue personnalisé et redirige vers la page principale.
     * Ferme la page d'authentification.
     * 
     * @param usager l'utilisateur authentifié avec succès
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
     * Gère les erreurs survenues lors du processus de connexion.
     * Affiche un message d'erreur à l'utilisateur et log l'exception dans la console.
     * 
     * @param ex l'exception survenue lors de la connexion
     */
    private void gererErreurConnexion(Exception ex) {
        afficherMessage(TITRE_ERREUR_TECHNIQUE,
            "Une erreur est survenue lors de la connexion: " + ex.getMessage(),
            JOptionPane.ERROR_MESSAGE);
        System.err.println("Erreur de connexion: " + ex.getMessage());
        ex.printStackTrace();
    }
    
    /**
     * Réinitialise le champ mot de passe et lui donne le focus.
     * Utilisé après une tentative de connexion échouée.
     */
    private void reinitialiserChampMotDePasse() {
        vue.txtPassword.setText("");
        vue.txtPassword.requestFocus();
    }
    
    /**
     * Affiche un message dans une boîte de dialogue modale.
     * 
     * @param titre le titre de la boîte de dialogue
     * @param message le contenu du message à afficher
     * @param typeMessage le type de message (INFORMATION, WARNING, ERROR, etc.)
     */
    private void afficherMessage(String titre, String message, int typeMessage) {
        JOptionPane.showMessageDialog(vue, message, titre, typeMessage);
    }
    
    /**
     * Retourne l'état actuel du processus d'authentification.
     * Utile pour le débogage et les tests.
     * 
     * @return l'état actuel du contrôleur
     */
    public EtatAuthentification getEtatCourant() {
        return etat;
    }
}
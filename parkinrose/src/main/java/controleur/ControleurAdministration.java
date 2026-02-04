package controleur;

import ihm.Page_Administration;
import ihm.PageGestionUtilisateurs;
import ihm.Page_Principale;
import ihm.CarteAdminOSMPanel;
import ihm.Page_Gestion_Feedback;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Contrôleur gérant l'interface d'administration de l'application.
 * Permet aux administrateurs d'accéder aux différentes fonctionnalités de gestion :
 * utilisateurs, parkings et feedbacks.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Administration
 * et les différentes pages de gestion.
 * 
 * @author Équipe 7
 */
public class ControleurAdministration implements ActionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur.
     * Permet de suivre la navigation dans l'interface d'administration.
     */
    private enum Etat {
        /** État initial au démarrage du contrôleur */
        INITIAL,
        /** Interface d'administration principale affichée */
        ADMINISTRATION,
        /** Navigation vers la gestion des utilisateurs */
        GESTION_UTILISATEURS,
        /** Navigation vers la gestion des parkings */
        GESTION_PARKINGS,
        /** Navigation vers la gestion des feedbacks */
        GESTION_FEEDBACKS,
        /** Retour à la page d'accueil */
        RETOUR
    }
    
    private Page_Administration vue;
    private Etat etat;
    private String emailAdmin;
    
    /**
     * Constructeur du contrôleur d'administration.
     * Initialise le contrôleur avec la vue associée et vérifie les droits d'administration.
     * 
     * @param vue la page d'interface graphique d'administration
     * @param emailAdmin l'email de l'administrateur connecté
     */
    public ControleurAdministration(Page_Administration vue, String emailAdmin) {
        this.vue = vue;
        this.emailAdmin = emailAdmin;
        this.etat = Etat.INITIAL;
        
        initialiserControleur();
    }
    
    /**
     * Initialise le contrôleur en vérifiant les droits d'administration.
     * Si l'utilisateur n'a pas les droits nécessaires, affiche un message d'erreur
     * et redirige vers la page d'accueil.
     */
    private void initialiserControleur() {
        if (verifierDroitsAdmin()) {
            etat = Etat.ADMINISTRATION;
            configurerListeners();
        } else {
            JOptionPane.showMessageDialog(vue,
                "Vous n'avez pas les droits d'administration nécessaires.",
                "Accès refusé",
                JOptionPane.ERROR_MESSAGE);
            retourAccueil();
        }
    }
    
    /**
     * Configure tous les écouteurs d'événements pour les composants interactifs de la vue.
     * Connecte le bouton retour et les panels cliquables aux actions appropriées.
     */
    private void configurerListeners() {
        vue.getBtnRetour().addActionListener(this);
        
        configurerPanelUtilisateurs();
        configurerPanelParkings();
        configurerPanelFeedbacks();
    }
    
    /**
     * Configure les écouteurs pour le panel de gestion des utilisateurs.
     * Gère les clics pour ouvrir la page de gestion et les effets de survol.
     */
    private void configurerPanelUtilisateurs() {
        JPanel panelUtilisateurs = vue.getPanelUtilisateurs();
        if (panelUtilisateurs != null) {
            panelUtilisateurs.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    etat = Etat.GESTION_UTILISATEURS;
                    ouvrirGestionUtilisateurs();
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    vue.survolPanelUtilisateurs(true);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    vue.survolPanelUtilisateurs(false);
                }
            });
        }
    }
    
    /**
     * Configure les écouteurs pour le panel de gestion des parkings.
     * Gère les clics pour ouvrir la carte interactive et les effets de survol.
     */
    private void configurerPanelParkings() {
        JPanel panelParkings = vue.getPanelParkings();
        if (panelParkings != null) {
            panelParkings.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    etat = Etat.GESTION_PARKINGS;
                    ouvrirCarteParkingsAdmin();
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    vue.survolPanelParkings(true);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    vue.survolPanelParkings(false);
                }
            });
        }
    }
    
    /**
     * Configure les écouteurs pour le panel de gestion des feedbacks.
     * Gère les clics pour ouvrir la page de gestion des retours utilisateurs et les effets de survol.
     */
    private void configurerPanelFeedbacks() {
        JPanel panelFeedbacks = vue.getPanelFeedbacks();
        if (panelFeedbacks != null) {
            panelFeedbacks.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    etat = Etat.GESTION_FEEDBACKS;
                    ouvrirGestionFeedbacks();
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    vue.survolPanelFeedbacks(true);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    vue.survolPanelFeedbacks(false);
                }
            });
        }
    }
    
    /**
     * Gère les événements d'action des composants de la vue.
     * Traite principalement le clic sur le bouton retour.
     * 
     * @param e l'événement d'action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == vue.getBtnRetour()) {
            etat = Etat.RETOUR;
            retourAccueil();
        }
    }
    
    /**
     * Ouvre la page de gestion des utilisateurs.
     * Ferme la page d'administration actuelle et affiche la page de gestion.
     */
    private void ouvrirGestionUtilisateurs() {
        PageGestionUtilisateurs pageGestion = new PageGestionUtilisateurs(emailAdmin);
        pageGestion.setVisible(true);
        vue.dispose();
    }
    
    /**
     * Ouvre la carte interactive d'administration des parkings dans une nouvelle fenêtre.
     * Permet d'ajouter, modifier ou supprimer des parkings directement sur la carte.
     * Masque la fenêtre d'administration principale pendant l'utilisation de la carte.
     */
    private void ouvrirCarteParkingsAdmin() {
        JFrame frameCarte = new JFrame("Administration des Parkings - Carte Interactive");
        frameCarte.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCarte.setSize(1300, 850);
        frameCarte.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel lblTitre = new JLabel("Carte Interactive des Parkings - Administration");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitre.setForeground(new Color(0, 102, 204));
        
        JLabel lblInstructions = new JLabel("Ajoutez, modifiez ou supprimez des parkings directement sur la carte");
        lblInstructions.setFont(new Font("Arial", Font.ITALIC, 12));
        lblInstructions.setForeground(Color.DARK_GRAY);
        
        headerPanel.add(lblTitre, BorderLayout.NORTH);
        headerPanel.add(lblInstructions, BorderLayout.SOUTH);
        
        JButton btnRetour = new JButton("← Retour");
        btnRetour.addActionListener(e -> {
            frameCarte.dispose();
            vue.setVisible(true);
        });
        headerPanel.add(btnRetour, BorderLayout.EAST);
        
        CarteAdminOSMPanel cartePanel = new CarteAdminOSMPanel(emailAdmin);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(cartePanel, BorderLayout.CENTER);
        frameCarte.add(mainPanel);
        
        frameCarte.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cartePanel.nettoyer();
            }
        });
        
        frameCarte.setVisible(true);
        vue.setVisible(false);
    }
    
    /**
     * Ouvre la page de gestion des feedbacks utilisateurs.
     * Ferme la page d'administration actuelle et affiche la page de gestion des feedbacks.
     */
    private void ouvrirGestionFeedbacks() {
        Page_Gestion_Feedback pageFeedback = new Page_Gestion_Feedback(emailAdmin);
        pageFeedback.setVisible(true);
        vue.dispose();
    }
    
    /**
     * Retourne à la page principale de l'application.
     * Ferme la page d'administration et affiche la page d'accueil.
     */
    private void retourAccueil() {
        Page_Principale pagePrincipale = new Page_Principale(emailAdmin);
        pagePrincipale.setVisible(true);
        vue.dispose();
    }
    
    /**
     * Vérifie si l'utilisateur possède les droits d'administration.
     * Un utilisateur est considéré comme administrateur si son email contient "admin"
     * ou s'il correspond à l'email administrateur par défaut.
     * 
     * @return true si l'utilisateur a les droits d'administration, false sinon
     */
    private boolean verifierDroitsAdmin() {
        return emailAdmin != null && (emailAdmin.contains("admin") || emailAdmin.equals("admin@pr.com"));
    }
    
    /**
     * Retourne l'état actuel du contrôleur.
     * Utile pour le débogage et les tests.
     * 
     * @return l'état actuel du contrôleur
     */
    public Etat getEtat() {
        return etat;
    }
    
    /**
     * Retourne l'email de l'administrateur connecté.
     * Utile pour le débogage et les tests.
     * 
     * @return l'email de l'administrateur
     */
    public String getEmailAdmin() {
        return emailAdmin;
    }
}
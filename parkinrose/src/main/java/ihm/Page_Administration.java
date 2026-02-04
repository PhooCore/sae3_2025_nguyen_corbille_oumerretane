package ihm;

import javax.swing.*;
import java.awt.*;
import controleur.ControleurAdministration;

public class Page_Administration extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailAdmin;
    
    // Références aux composants
    private JPanel panelUtilisateurs;
    private JPanel panelParkings;
    private JPanel panelFeedbacks; // NOUVEAU : Panel pour les feedbacks
    private JButton btnRetour;
    
    public Page_Administration(String emailAdmin) {
        this.emailAdmin = emailAdmin;
        initialisePage();
        
        // Instancier le contrôleur
        new ControleurAdministration(this, emailAdmin);
    }
    
    private void initialisePage() {
        setTitle("Administration - Parkin'Rose");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600); // Augmenté un peu la hauteur
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(Color.WHITE);
        
        // === PANEL HAUT - TITRE ===
        JPanel panelTitre = new JPanel(new BorderLayout());
        panelTitre.setBackground(Color.WHITE);
        
        JLabel lblTitre = new JLabel("Panneau d'Administration", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitre.setForeground(new Color(0, 102, 204));
        
        JLabel lblInfoAdmin = new JLabel("Connecté en tant que: " + emailAdmin);
        lblInfoAdmin.setFont(new Font("Arial", Font.ITALIC, 12));
        lblInfoAdmin.setForeground(Color.GRAY);
        
        panelTitre.add(lblTitre, BorderLayout.CENTER);
        panelTitre.add(lblInfoAdmin, BorderLayout.SOUTH);
        mainPanel.add(panelTitre, BorderLayout.NORTH);
        
        // === PANEL CENTRE - OPTIONS ===
        JPanel panelOptions = new JPanel(new GridLayout(3, 1, 30, 30)); // Changé à 3 lignes
        panelOptions.setBackground(Color.WHITE);
        panelOptions.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40)); // Réduit le padding
        
        // Option 1 : Gestion des utilisateurs
        panelUtilisateurs = creerPanelOption(
            "Gestion des Utilisateurs",
            new Color(70, 130, 180) // Bleu
        );
        
        // Option 2 : Gestion des parkings (CARTE INTERACTIVE)
        panelParkings = creerPanelOption(
            "Gestion des Parkings - Carte Interactive",
            new Color(60, 179, 113) // Vert
        );
        
        // NOUVELLE OPTION 3 : Répondre au feedback
        panelFeedbacks = creerPanelOption(
            "Répondre au Feedback",
            new Color(186, 85, 211) // Violet
        );
        
        panelOptions.add(panelUtilisateurs);
        panelOptions.add(panelParkings);
        panelOptions.add(panelFeedbacks);
        
        mainPanel.add(panelOptions, BorderLayout.CENTER);
        
        // === PANEL BAS - BOUTON RETOUR ===
        JPanel panelBas = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBas.setBackground(Color.WHITE);
        
        btnRetour = new JButton("Retour à l'accueil");
        btnRetour.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRetour.setBackground(new Color(169, 169, 169));
        btnRetour.setForeground(Color.WHITE);
        btnRetour.setFocusPainted(false);
        btnRetour.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panelBas.add(btnRetour);
        mainPanel.add(panelBas, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel creerPanelOption(String titre, Color couleur) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(couleur, 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setBackground(new Color(240, 240, 240));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Titre uniquement (conservé comme votre version originale)
        JLabel lblTitre = new JLabel(titre);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitre.setForeground(couleur);
        
        panel.add(lblTitre);
        
        return panel;
    }
    
    // Méthodes pour gérer les effets de survol (appelées par le contrôleur)
    public void survolPanelUtilisateurs(boolean survol) {
        Color couleur = new Color(70, 130, 180);
        if (survol) {
            panelUtilisateurs.setBackground(new Color(250, 250, 250));
            panelUtilisateurs.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(couleur.brighter(), 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
        } else {
            panelUtilisateurs.setBackground(new Color(240, 240, 240));
            panelUtilisateurs.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(couleur, 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
        }
    }
    
    public void survolPanelParkings(boolean survol) {
        Color couleur = new Color(60, 179, 113);
        if (survol) {
            panelParkings.setBackground(new Color(250, 250, 250));
            panelParkings.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(couleur.brighter(), 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
        } else {
            panelParkings.setBackground(new Color(240, 240, 240));
            panelParkings.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(couleur, 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
        }
    }
    
    // NOUVELLE MÉTHODE : Gestion du survol pour le panel feedbacks
    public void survolPanelFeedbacks(boolean survol) {
        Color couleur = new Color(186, 85, 211);
        if (survol) {
            panelFeedbacks.setBackground(new Color(250, 250, 250));
            panelFeedbacks.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(couleur.brighter(), 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
        } else {
            panelFeedbacks.setBackground(new Color(240, 240, 240));
            panelFeedbacks.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(couleur, 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
        }
    }
    
    // Getters pour le contrôleur
    public JPanel getPanelUtilisateurs() {
        return panelUtilisateurs;
    }
    
    public JPanel getPanelParkings() {
        return panelParkings;
    }
    
    // NOUVEAU GETTER : Panel feedbacks
    public JPanel getPanelFeedbacks() {
        return panelFeedbacks;
    }
    
    public JButton getBtnRetour() {
        return btnRetour;
    }
    
    public String getEmailAdmin() {
        return emailAdmin;
    }
    

}
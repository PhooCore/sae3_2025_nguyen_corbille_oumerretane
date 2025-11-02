package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

import dao.StationnementDAO;
import dao.ParkingDAO;
import dao.UsagerDAO;
import modèle.Stationnement;
import modèle.Parking;
import modèle.Usager;

public class Page_Principale extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Usager usager;
    private JButton btnStationnement;
    private Timer timer;
    private JTextField searchField;

    public Page_Principale(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        
        // Nettoyer les stationnements expirés au démarrage
        StationnementDAO.nettoyerStationnementsExpires();
        
        initialisePage();
        startStationnementCheck();
    }
    
    private void initialisePage() {
        this.setTitle("ParkinRose - Accueil");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(900, 700);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        // Panel principal avec layout BorderLayout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        this.setContentPane(mainPanel);
        
        // === BARRE DU HAUT (HEADER) - Recherche + Icônes ===
        JPanel headerPanel = creerBarrePanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // === PANEL CENTRAL - Message simple "Map à venir" ===
        JPanel centerPanel = creerCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // === BOUTONS PRINCIPAUX EN BAS ===
        JPanel bottomPanel = creerBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        updateStationnementIcon();
    }
    
    private JPanel creerBarrePanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        headerPanel.setPreferredSize(new Dimension(900, 100));
        
        // === BARRE DE RECHERCHE ===
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(new Color(240, 240, 240));
        
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 40));
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchField.setText("Rechercher un parking...");
        searchField.setForeground(Color.GRAY);
        
        // Gestion du focus
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals("Rechercher un parking...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Rechercher un parking...");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        
        // Bouton de recherche
        JButton btnSearch = new JButton("🔍");
        btnSearch.setFont(new Font("Arial", Font.PLAIN, 16));
        btnSearch.setBackground(Color.WHITE);
        btnSearch.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        btnSearch.setPreferredSize(new Dimension(50, 40));
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnSearch.addActionListener(e -> {
            String recherche = searchField.getText().trim();
            if (!recherche.isEmpty() && !recherche.equals("Rechercher un parking...")) {
                Page_Resultats_Recherche pageResultats = new Page_Resultats_Recherche(emailUtilisateur, recherche);
                pageResultats.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(Page_Principale.this, 
                    "Veuillez saisir un terme de recherche", 
                    "Recherche vide", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        searchField.addActionListener(e -> btnSearch.doClick());
        
        searchPanel.add(searchField);
        searchPanel.add(btnSearch);
        
        // === ICÔNES DE NAVIGATION ===
        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        iconsPanel.setBackground(new Color(240, 240, 240));
        
        // Icône Stationnement seulement
        btnStationnement = new JButton();
        btnStationnement.setLayout(new BorderLayout());
        btnStationnement.setBackground(new Color(240, 240, 240));
        btnStationnement.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btnStationnement.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnStationnement.setPreferredSize(new Dimension(120, 70));
        
        JLabel lblIconePark = chargerIconeLabel("/images/parking.png", 40, 40, "S");
        JLabel lblTextPark = new JLabel("Stationnement", SwingConstants.CENTER);
        lblTextPark.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTextPark.setForeground(Color.DARK_GRAY);
        
        btnStationnement.add(lblIconePark, BorderLayout.CENTER);
        btnStationnement.add(lblTextPark, BorderLayout.SOUTH);
        btnStationnement.addActionListener(e -> ouvrirPageStationnement());
        
        // Icône Utilisateur seulement
        JButton btnUtilisateur = new JButton();
        btnUtilisateur.setLayout(new BorderLayout());
        btnUtilisateur.setBackground(new Color(240, 240, 240));
        btnUtilisateur.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btnUtilisateur.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUtilisateur.setPreferredSize(new Dimension(120, 70));
        
        JLabel lblIconeUser = chargerIconeLabel("/images/utilisateur.png", 40, 40, "U");
        JLabel lblTextUser = new JLabel("Mon Compte", SwingConstants.CENTER);
        lblTextUser.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTextUser.setForeground(Color.DARK_GRAY);
        
        btnUtilisateur.add(lblIconeUser, BorderLayout.CENTER);
        btnUtilisateur.add(lblTextUser, BorderLayout.SOUTH);
        btnUtilisateur.addActionListener(e -> ouvrirPageUtilisateur());
        
        // Ajout des icônes (seulement 2 maintenant)
        iconsPanel.add(btnStationnement);
        iconsPanel.add(btnUtilisateur);
        
        // Assemblage final
        headerPanel.add(searchPanel, BorderLayout.WEST);
        headerPanel.add(iconsPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel creerCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JLabel lblMessage = new JLabel("Map à venir", SwingConstants.CENTER);
        lblMessage.setFont(new Font("Arial", Font.BOLD, 24));
        lblMessage.setForeground(Color.LIGHT_GRAY);
        
        centerPanel.add(lblMessage, BorderLayout.CENTER);
        
        return centerPanel;
    }
    
    private JPanel creerBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 30, 20));
        
        // Bouton unique pour préparer un stationnement
        JButton btnPreparerStationnement = new JButton("Préparer un stationnement");
        btnPreparerStationnement.setFont(new Font("Arial", Font.BOLD, 16));
        btnPreparerStationnement.setBackground(new Color(70, 130, 180));
        btnPreparerStationnement.setForeground(Color.WHITE);
        btnPreparerStationnement.setFocusPainted(false);
        btnPreparerStationnement.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        btnPreparerStationnement.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPreparerStationnement.addActionListener(e -> {
            // Redirige vers le choix du type de stationnement
            ouvrirPageStationnement();
        });
        
        bottomPanel.add(btnPreparerStationnement);
        
        return bottomPanel;
    }
    
    
    /**
     * Charge une image depuis les ressources ou crée un placeholder textuel
     */
    private JLabel chargerIconeLabel(String chemin, int largeur, int hauteur, String textePlaceholder) {
        try {
            java.net.URL imageUrl = getClass().getResource(chemin);
            if (imageUrl != null) {
                ImageIcon iconOriginal = new ImageIcon(imageUrl);
                Image imageRedimensionnee = iconOriginal.getImage().getScaledInstance(largeur, hauteur, Image.SCALE_SMOOTH);
                return new JLabel(new ImageIcon(imageRedimensionnee), SwingConstants.CENTER);
            } else {
                System.err.println("Image non trouvée: " + chemin);
                return creerLabelPlaceholder(largeur, hauteur, textePlaceholder);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image " + chemin + ": " + e.getMessage());
            return creerLabelPlaceholder(largeur, hauteur, textePlaceholder);
        }
    }
    
    /**
     * Crée un label de remplacement avec texte stylisé
     */
    private JLabel creerLabelPlaceholder(int largeur, int hauteur, String texte) {
        JLabel label = new JLabel(texte, SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(largeur, hauteur));
        label.setOpaque(true);
        label.setBackground(Color.LIGHT_GRAY);
        label.setForeground(Color.DARK_GRAY);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        return label;
    }
    
    /**
     * Ouvre la page appropriée selon l'état du stationnement
     */
    private void ouvrirPageStationnement() {
        Stationnement stationnementActif = StationnementDAO.getStationnementActifValideByUsager(usager.getIdUsager());
        
        if (stationnementActif != null) {
            // Stationnement actif : ouvrir la page de gestion
            Page_Stationnement_En_Cours pageStationnement = new Page_Stationnement_En_Cours(emailUtilisateur);
            pageStationnement.setVisible(true);
            dispose();
        } else {
            // Aucun stationnement actif : proposer un choix
            Object[] options = {"Stationnement en Voirie", "Stationnement en Parking"};
            int choix = JOptionPane.showOptionDialog(this,
                "Choisissez le type de stationnement :",
                "Nouveau stationnement",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
                
            if (choix == 0) {
                // Voirie
                Page_Garer_Voirie pageGarer = new Page_Garer_Voirie(emailUtilisateur);
                pageGarer.setVisible(true);
                dispose();
            } else if (choix == 1) {
                // Parking - ouvrir directement la page de préparation
                Page_Garer_Parking pageParking = new Page_Garer_Parking();//mettre l'email
                pageParking.setVisible(true);
                dispose();
            }
        }
    }
    
    /**
     * Ouvre la page des informations utilisateur
     */
    private void ouvrirPageUtilisateur() {
        Page_Utilisateur pageUtilisateur = new Page_Utilisateur(emailUtilisateur);
        pageUtilisateur.setVisible(true);
        dispose();
    }
    
    /**
     * Démarre la vérification périodique des stationnements actifs
     */
    private void startStationnementCheck() {
        timer = new Timer(10000, e -> updateStationnementIcon());
        timer.start();
    }
    
    /**
     * Met à jour l'apparence de l'icône stationnement
     */
    private void updateStationnementIcon() {
        Stationnement stationnementActif = StationnementDAO.getStationnementActifValideByUsager(usager.getIdUsager());
        
        if (stationnementActif != null) {
            btnStationnement.setBackground(new Color(255, 220, 220));
            btnStationnement.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2),
                BorderFactory.createEmptyBorder(3, 13, 3, 13)
            ));
        } else {
            btnStationnement.setBackground(new Color(240, 240, 240));
            btnStationnement.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        }
    }
    
    @Override
    public void dispose() {
        if (timer != null) {
            timer.stop();
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Page_Bienvenue().setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
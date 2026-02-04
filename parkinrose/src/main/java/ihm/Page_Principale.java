package ihm;

import javax.swing.*;
import controleur.ControleurPrincipale;
import controleur.StationnementControleur;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import modele.Stationnement;
import modele.Parking;
import modele.Usager;
import modele.dao.FeedbackDAO;
import modele.dao.MySQLConnection;
import modele.dao.ParkingDAO;
import modele.dao.StationnementDAO;
import modele.dao.UsagerDAO;

public class Page_Principale extends JFrame {
    
    private static final long serialVersionUID = 1L;
    public String emailUtilisateur;
    private Usager usager;
    public JButton btnMessagerie;
    public JButton btnStationnement;
    public JButton btnFavoris;
    public JButton btnUtilisateur;
    public JButton btnPreparerStationnement;
    public JButton btnSearch;
    private Timer timer;
    private JTextField searchField;
    private JPanel headerPanel;
    private CartePanel cartePanel;
    private Map<String, JButton> boutonsZones = new HashMap<>();
    private JButton btnAdmin;
    private CarteOSMPanel carteOSM;
    private boolean hasUnreadMessages = false;
    public Page_Principale(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        
        StationnementDAO.nettoyerStationnementsExpires();
        
        initialisePage();
        startStationnementCheck();
        
        // Créer et lier le contrôleur
        new ControleurPrincipale(this, email);
    }
    
    private void initialisePage() {
        this.setTitle("ParkinRose - Accueil");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1300, 900); // Légèrement plus large pour éviter le chevauchement
        this.setLocationRelativeTo(null);
        this.setResizable(true);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        this.setContentPane(mainPanel);
        
        headerPanel = creerBarrePanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = creerCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = creerBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        updateStationnementIcon();
        if (usager != null && usager.isAdmin()) {
            ajouterBoutonAdmin();
        }
        updateMessagerieIcon();
        updateStationnementIcon();
        if (usager != null && usager.isAdmin()) {
            ajouterBoutonAdmin();
        }
    }
    
    private void ajouterBoutonAdmin() {
        JButton btnAdmin = new JButton("Admin");
        btnAdmin.setPreferredSize(new Dimension(80, 35));
        btnAdmin.setBackground(new Color(255, 165, 0)); 
        btnAdmin.setForeground(Color.WHITE);
        btnAdmin.setFont(new Font("Arial", Font.BOLD, 11));
        btnAdmin.setFocusPainted(false);
        btnAdmin.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 130, 0), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        btnAdmin.addActionListener(e -> {
            Page_Administration adminPage = new Page_Administration(emailUtilisateur);
            adminPage.setVisible(true);
            this.dispose();
        });
        
        // Ajouter au panel des icônes utilisateur
        Component[] components = headerPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                // Chercher le panel avec les icônes utilisateur
                for (Component subComp : panel.getComponents()) {
                    if (subComp instanceof JPanel) {
                        JPanel subPanel = (JPanel) subComp;
                        if (subPanel.getComponentCount() > 0) {
                            // Vérifier si c'est le panel des icônes
                            boolean hasStationnement = false;
                            for (Component iconComp : subPanel.getComponents()) {
                                if (iconComp == btnStationnement) {
                                    hasStationnement = true;
                                    break;
                                }
                            }
                            if (hasStationnement) {
                                subPanel.add(btnAdmin, 0); // Ajouter au début
                                subPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        headerPanel.revalidate();
        headerPanel.repaint();
    }
    
    private JPanel creerBarrePanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setPreferredSize(new Dimension(1300, 90)); 
        
        // PANEL GAUCHE : Recherche
        JPanel panelGauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelGauche.setBackground(new Color(240, 240, 240));
        
        // Champ de recherche
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(250, 38));
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchField.setText("Rechercher un parking...");
        searchField.setForeground(Color.GRAY);
        
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (searchField.getText().equals("Rechercher un parking...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Rechercher un parking...");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        
        // Bouton de recherche
        btnSearch = new JButton();
        btnSearch.setBackground(Color.WHITE);
        btnSearch.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        btnSearch.setPreferredSize(new Dimension(40, 38));
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Icône loupe ou texte
        try {
            java.net.URL imageUrl = getClass().getResource("/images/loupe.png");
            if (imageUrl != null) {
                ImageIcon iconOriginal = new ImageIcon(imageUrl);
                Image imageRedimensionnee = iconOriginal.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                JLabel lblLoupe = new JLabel(new ImageIcon(imageRedimensionnee), SwingConstants.CENTER);
                btnSearch.add(lblLoupe);
            } else {
                btnSearch.setFont(new Font("Arial", Font.PLAIN, 14));
            }
        } catch (Exception e) {
            btnSearch.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        
        panelGauche.add(searchField);
        panelGauche.add(btnSearch);
        if (usager != null && usager.isAdmin()) {
            // Espace entre la recherche et le bouton admin
            panelGauche.add(Box.createRigidArea(new Dimension(15, 0)));
            
            // Bouton Admin
            JButton btnAdmin = new JButton("Admin");
            btnAdmin.setPreferredSize(new Dimension(90, 38));
            btnAdmin.setBackground(new Color(255, 165, 0)); // Orange
            btnAdmin.setForeground(Color.WHITE);
            btnAdmin.setFont(new Font("Arial", Font.BOLD, 12));
            btnAdmin.setFocusPainted(false);
            btnAdmin.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 130, 0), 2),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));
            
            btnAdmin.addActionListener(e -> {
                Page_Administration adminPage = new Page_Administration(emailUtilisateur);
                adminPage.setVisible(true);
                this.dispose();
            });
            
            // Effet hover
            btnAdmin.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    btnAdmin.setBackground(new Color(255, 140, 0));
                }
                public void mouseExited(MouseEvent evt) {
                    btnAdmin.setBackground(new Color(255, 165, 0));
                }
            });
            
            panelGauche.add(btnAdmin);
        }
        

        
        JPanel panelDroit = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelDroit.setBackground(new Color(240, 240, 240));

        btnMessagerie = new JButton();
        btnMessagerie.setLayout(new BorderLayout());
        btnMessagerie.setBackground(new Color(240, 240, 240));
        btnMessagerie.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnMessagerie.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMessagerie.setPreferredSize(new Dimension(140, 70)); 

        JLabel lblIconeMess = chargerIconeLabel("/images/email.png", 40, 40, "F");

        JLabel lblTextMess = new JLabel("Feedback", SwingConstants.CENTER);
        lblTextMess.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTextMess.setForeground(Color.DARK_GRAY);      

        btnMessagerie.add(lblIconeMess, BorderLayout.CENTER);
        btnMessagerie.add(lblTextMess, BorderLayout.SOUTH);
       

        // Bouton Stationnement
        btnStationnement = new JButton();
        btnStationnement.setLayout(new BorderLayout());
        btnStationnement.setBackground(new Color(240, 240, 240));
        btnStationnement.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnStationnement.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnStationnement.setPreferredSize(new Dimension(140, 70));
        
        // Icône stationnement
        JLabel lblIconePark = chargerIconeLabel("/images/parking.png", 40, 40, "P");
        JLabel lblTextPark = new JLabel("Stationnement", SwingConstants.CENTER);
        lblTextPark.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTextPark.setForeground(Color.DARK_GRAY);
        
        btnStationnement.add(lblIconePark, BorderLayout.CENTER);
        btnStationnement.add(lblTextPark, BorderLayout.SOUTH);
        
        //Bouton favoris
        btnFavoris = new JButton();
        btnFavoris = new JButton();
        btnFavoris.setLayout(new BorderLayout());
        btnFavoris.setBackground(new Color(240, 240, 240));
        btnFavoris.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnFavoris.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFavoris.setPreferredSize(new Dimension(140, 70));
        btnFavoris.addActionListener(e -> ouvrirPageFavoris());
        //icone coeur favoris
        JLabel lblIconeFavoris = chargerIconeLabel("/images/coeurRempli.png", 40, 40, "F");
        JLabel lblTexteFavoris = new JLabel("Favoris", SwingConstants.CENTER);
        lblTexteFavoris.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTexteFavoris.setForeground(Color.DARK_GRAY);
        
        btnFavoris.add(lblIconeFavoris, BorderLayout.CENTER);
        btnFavoris.add(lblTexteFavoris, BorderLayout.SOUTH);
        
        // Bouton Mon Compte
        btnUtilisateur = new JButton();
        btnUtilisateur.setLayout(new BorderLayout());
        btnUtilisateur.setBackground(new Color(240, 240, 240));
        btnUtilisateur.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnUtilisateur.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUtilisateur.setPreferredSize(new Dimension(140, 70));
        
        // Icône utilisateur
        JLabel lblIconeUser = chargerIconeLabel("/images/utilisateur.png", 40, 40, "U");
        JLabel lblTextUser = new JLabel("Mon Compte", SwingConstants.CENTER);
        lblTextUser.setFont(new Font("Arial", Font.PLAIN, 10));
        lblTextUser.setForeground(Color.DARK_GRAY);
        
        btnUtilisateur.add(lblIconeUser, BorderLayout.CENTER);
        btnUtilisateur.add(lblTextUser, BorderLayout.SOUTH);
        panelDroit.add(btnMessagerie);
        panelDroit.add(btnStationnement);
        panelDroit.add(btnFavoris);
        panelDroit.add(btnUtilisateur);
        
        // ========== ASSEMBLAGE FINAL ==========
        headerPanel.add(panelGauche, BorderLayout.WEST);
        headerPanel.add(panelDroit, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private Color parseCouleur(String rgb) {
        String[] parts = rgb.split(",");
        return new Color(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2])
        );
    }
    
    private JPanel creerCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        try {
            // Créer la carte OSM
            carteOSM = new CarteOSMPanel(emailUtilisateur);
            
            // Ajouter la carte OSM
            centerPanel.add(carteOSM, BorderLayout.CENTER);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la carte OSM: " + e.getMessage());
            e.printStackTrace();
            
            // Afficher un message d'erreur
            JLabel lblErreur = new JLabel("<html><center>Impossible de charger la carte OpenStreetMap<br>Erreur: " + 
                                          e.getMessage() + "</center></html>", SwingConstants.CENTER);
            lblErreur.setFont(new Font("Arial", Font.BOLD, 16));
            lblErreur.setForeground(Color.RED);
            centerPanel.add(lblErreur, BorderLayout.CENTER);
        }
        
        return centerPanel;
    }
    
    // Méthode pour recharger la carte
    public void rechargerCarte() {
        if (carteOSM != null) {
            carteOSM.recharger();
        }
    }
    private JPanel creerBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
        
        btnPreparerStationnement = new JButton("Faire un stationnement en voirie");
        btnPreparerStationnement.setFont(new Font("Arial", Font.BOLD, 13));
        btnPreparerStationnement.setBackground(new Color(70, 130, 180));
        btnPreparerStationnement.setForeground(Color.WHITE);
        btnPreparerStationnement.setFocusPainted(false);
        btnPreparerStationnement.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        btnPreparerStationnement.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        bottomPanel.add(btnPreparerStationnement);
        
        return bottomPanel;
    }
    
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
    
    private void startStationnementCheck() {
        timer = new Timer(10000, e -> {
            updateStationnementIcon();
            updateMessagerieIcon(); // Cette ligne doit être présente
        });
        timer.start();
    }
    
    private void updateStationnementIcon() {
        StationnementControleur controleur = new StationnementControleur(emailUtilisateur);
        Stationnement stationnementActif = controleur.getStationnementActif();
        
        if (stationnementActif != null) {
            btnStationnement.setBackground(new Color(255, 240, 240));
            btnStationnement.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)
            ));
            
            Component[] components = btnStationnement.getComponents();
            if (components.length >= 2) {
                JLabel lblIcone = (JLabel) components[0];
                JLabel lblTexte = (JLabel) components[1];
                
                lblIcone.setHorizontalAlignment(SwingConstants.CENTER);
                lblIcone.setVerticalAlignment(SwingConstants.CENTER);
                
                lblTexte.setText("<html><center><b>STATIONNEMENT</b><br><font color='red' size='2'>● ACTIF</font></center></html>");
                lblTexte.setForeground(Color.RED);
                lblTexte.setFont(new Font("Arial", Font.BOLD, 9));
            }
        } else {
            btnStationnement.setBackground(new Color(240, 240, 240));
            btnStationnement.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            
            Component[] components = btnStationnement.getComponents();
            if (components.length >= 2) {
                JLabel lblIcone = (JLabel) components[0];
                JLabel lblTexte = (JLabel) components[1];
                
                lblIcone.setHorizontalAlignment(SwingConstants.CENTER);
                lblIcone.setVerticalAlignment(SwingConstants.CENTER);
                
                lblTexte.setText("Stationnement");
                lblTexte.setForeground(Color.DARK_GRAY);
                lblTexte.setFont(new Font("Arial", Font.PLAIN, 12));
            }
        }
        
        btnStationnement.revalidate();
        btnStationnement.repaint();
    }
    
    // Getter pour le champ de recherche
    public JTextField getSearchField() {
        return searchField;
    }
    
    // Getter pour le panneau de carte
    public CartePanel getCartePanel() {
        return cartePanel;
    }
    
    @Override
    public void dispose() {
        
        if (timer != null) {
            timer.stop();
        }
        super.dispose();
    }
    private void checkUnreadMessages() {
        if (usager != null) {
            hasUnreadMessages = FeedbackDAO.hasUnreadMessages(usager.getIdUsager());
        }
    }
    public void updateMessagerieIcon() {
        checkUnreadMessages();
        
        if (hasUnreadMessages) {
            btnMessagerie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(240, 240, 240), 8),
                BorderFactory.createLineBorder(Color.RED, 2)
            ));
            
            Component[] components = btnMessagerie.getComponents();
            if (components.length >= 2) {
                if (components[0] instanceof JPanel) {
                    JPanel iconPanel = (JPanel) components[0];
                    
                    
                    for (Component comp : iconPanel.getComponents()) {
                        if (comp instanceof JLabel && comp.getName() != null && comp.getName().equals("notificationBadge")) {
                            iconPanel.remove(comp);
                        }
                    }

                    JLabel badge = new JLabel("•");
                    badge.setName("notificationBadge");
                    badge.setFont(new Font("Arial", Font.BOLD, 24));
                    badge.setForeground(Color.RED);
                    badge.setHorizontalAlignment(SwingConstants.RIGHT);
                    badge.setVerticalAlignment(SwingConstants.TOP);
                    
                    iconPanel.add(badge, BorderLayout.NORTH);
                }
                

                JLabel lblText = (JLabel) components[1];
                lblText.setText("<html><center><b>Feedback</b><br><font color='red' size='2'>● NOUVEAU</font></center></html>");
                lblText.setFont(new Font("Arial", Font.BOLD, 9));
            }
        } else {

            btnMessagerie.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            
            Component[] components = btnMessagerie.getComponents();
            if (components.length >= 2) {
                if (components[0] instanceof JPanel) {
                    JPanel iconPanel = (JPanel) components[0];
                    

                    for (Component comp : iconPanel.getComponents()) {
                        if (comp instanceof JLabel && comp.getName() != null && comp.getName().equals("notificationBadge")) {
                            iconPanel.remove(comp);
                        }
                    }
                }
                

                JLabel lblText = (JLabel) components[1];
                lblText.setText("Feedback");
                lblText.setForeground(Color.DARK_GRAY);
                lblText.setFont(new Font("Arial", Font.PLAIN, 12));
            }
        }
        
        btnMessagerie.revalidate();
        btnMessagerie.repaint();
    }
    public void markMessagesAsRead() {
        if (usager != null) {
            FeedbackDAO.markMessagesAsRead(usager.getIdUsager());
            hasUnreadMessages = false;
            updateMessagerieIcon();
        }
    }
    
    public void ouvrirPageFavoris() {
        Page_Favoris pageFavoris =
            new Page_Favoris(emailUtilisateur, usager.getIdUsager());

        pageFavoris.setVisible(true);
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
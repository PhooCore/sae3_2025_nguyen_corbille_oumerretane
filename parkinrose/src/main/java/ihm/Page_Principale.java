package ihm;

import javax.swing.*;

import controleur.ControleurPrincipale; // Import du contrôleur
import controleur.StationnementControleur;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import modele.Stationnement;
import modele.Parking;
import modele.Usager;
import modele.dao.ParkingDAO;
import modele.dao.StationnementDAO;
import modele.dao.UsagerDAO;

public class Page_Principale extends JFrame {
    
    private static final long serialVersionUID = 1L;
    public String emailUtilisateur;
    private Usager usager;
    public JButton btnStationnement;
    public JButton btnUtilisateur;
    public JButton btnPreparerStationnement;
    public JButton btnSearch;
    private Timer timer;
    private JTextField searchField;
    private JPanel headerPanel;

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
        this.setSize(900, 700);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
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
    }
    
    private void ajouterBoutonAdmin() {
        JButton btnAdmin = new JButton("Administration");
        btnAdmin.setPreferredSize(new Dimension(130, 35));
        btnAdmin.setBackground(new Color(255, 165, 0)); 
        btnAdmin.setForeground(Color.WHITE);
        btnAdmin.setFont(new Font("Arial", Font.BOLD, 12));
        btnAdmin.setFocusPainted(false);
        
        btnAdmin.addActionListener(e -> {
            Page_Administration adminPage = new Page_Administration(emailUtilisateur);
            adminPage.setVisible(true);
            this.dispose();
        });
        

        Component[] components = headerPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel && ((JPanel) comp).getComponentCount() > 0) {
                JPanel iconsPanel = (JPanel) comp;
                iconsPanel.add(btnAdmin, 0);
                break;
            }
        }
        
        headerPanel.revalidate();
        headerPanel.repaint();
    }
    
    private JPanel creerBarrePanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        headerPanel.setPreferredSize(new Dimension(900, 100));
        
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
        
        btnSearch = new JButton();
        btnSearch.setBackground(Color.WHITE);
        btnSearch.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        btnSearch.setPreferredSize(new Dimension(50, 40));
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel lblLoupe = chargerIconeLabel("/images/loupe.png", 16, 16, "");
        btnSearch.add(lblLoupe);
        
        searchPanel.add(searchField);
        searchPanel.add(btnSearch);
        
        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        iconsPanel.setBackground(new Color(240, 240, 240));
        
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
        
        btnUtilisateur = new JButton();
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
        
        iconsPanel.add(btnStationnement);
        iconsPanel.add(btnUtilisateur);
        
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
        
        btnPreparerStationnement = new JButton("Préparer un stationnement");
        btnPreparerStationnement.setFont(new Font("Arial", Font.BOLD, 16));
        btnPreparerStationnement.setBackground(new Color(70, 130, 180));
        btnPreparerStationnement.setForeground(Color.WHITE);
        btnPreparerStationnement.setFocusPainted(false);
        btnPreparerStationnement.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
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
        timer = new Timer(10000, e -> updateStationnementIcon());
        timer.start();
    }
    
    private void updateStationnementIcon() {
        StationnementControleur controleur = new StationnementControleur(emailUtilisateur);
        Stationnement stationnementActif = controleur.getStationnementActif();
        
        if (stationnementActif != null) {
            // STATIONNEMENT ACTIF - Garder la taille normale de l'icône
            btnStationnement.setBackground(new Color(255, 240, 240));
            
            // ENCADRÉ ROUGE AUTOUR DU BOUTON, pas autour de l'icône
            btnStationnement.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2), // Bordure fine
                BorderFactory.createEmptyBorder(10, 20, 10, 20) // PLUS DE PADDING pour éviter la coupure
            ));
            
            // Récupérer les composants
            Component[] components = btnStationnement.getComponents();
            
            if (components.length >= 2) {
                JLabel lblIcone = (JLabel) components[0];
                JLabel lblTexte = (JLabel) components[1];
                
                // NE PAS CHANGER LA TAILLE DE L'ICÔNE - juste l'encadrer mieux
                // L'icône garde sa taille normale (40x40)
                
                // Assurer que l'icône est centrée
                lblIcone.setHorizontalAlignment(SwingConstants.CENTER);
                lblIcone.setVerticalAlignment(SwingConstants.CENTER);
                
                // Modifier le texte
                lblTexte.setText("<html><center><b>STATIONNEMENT</b><br><font color='red' size='2'>● ACTIF</font></center></html>");
                lblTexte.setForeground(Color.RED);
                lblTexte.setFont(new Font("Arial", Font.BOLD, 11));
            }
            
        } else {
            // AUCUN STATIONNEMENT - Style normal
            btnStationnement.setBackground(new Color(240, 240, 240));
            
            // Bordure normale avec suffisamment d'espace
            btnStationnement.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            
            // Récupérer les composants
            Component[] components = btnStationnement.getComponents();
            
            if (components.length >= 2) {
                JLabel lblIcone = (JLabel) components[0];
                JLabel lblTexte = (JLabel) components[1];
                
                // S'assurer que l'icône est centrée
                lblIcone.setHorizontalAlignment(SwingConstants.CENTER);
                lblIcone.setVerticalAlignment(SwingConstants.CENTER);
                
                // Restaurer le texte normal
                lblTexte.setText("Stationnement");
                lblTexte.setForeground(Color.DARK_GRAY);
                lblTexte.setFont(new Font("Arial", Font.PLAIN, 5));
            }
        }
        
        btnStationnement.revalidate();
        btnStationnement.repaint();
    }
    // Getter pour le champ de recherche
    public JTextField getSearchField() {
        return searchField;
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
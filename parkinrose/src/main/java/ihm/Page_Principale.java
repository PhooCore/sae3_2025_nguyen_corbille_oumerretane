package ihm;

import javax.swing.*;
import controleur.ControleurPrincipale;
import controleur.StationnementControleur;
import java.awt.*;
import java.awt.event.*;
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
    private CartePanel cartePanel;

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
        this.setSize(1200, 900); // Taille augmentée de 850 à 900
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
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Bordures réduites
        headerPanel.setPreferredSize(new Dimension(1200, 100)); // Hauteur réduite
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(new Color(240, 240, 240));
        
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 35)); // Hauteur réduite
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
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
        btnSearch.setPreferredSize(new Dimension(40, 35)); // Hauteur réduite
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
        btnStationnement.setPreferredSize(new Dimension(120, 80)); 
        
        JLabel lblIconePark = chargerIconeLabel("/images/parking.png", 35, 35, "S");
        JLabel lblTextPark = new JLabel("Stationnement", SwingConstants.CENTER);
        lblTextPark.setFont(new Font("Arial", Font.PLAIN, 11)); 
        lblTextPark.setForeground(Color.DARK_GRAY);
        
        btnStationnement.add(lblIconePark, BorderLayout.CENTER);
        btnStationnement.add(lblTextPark, BorderLayout.SOUTH);
        
        btnUtilisateur = new JButton();
        btnUtilisateur.setLayout(new BorderLayout());
        btnUtilisateur.setBackground(new Color(240, 240, 240));
        btnUtilisateur.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btnUtilisateur.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUtilisateur.setPreferredSize(new Dimension(120, 80)); 
        
        JLabel lblIconeUser = chargerIconeLabel("/images/utilisateur.png", 35, 35, "U"); 
        JLabel lblTextUser = new JLabel("Mon Compte", SwingConstants.CENTER);
        lblTextUser.setFont(new Font("Arial", Font.PLAIN, 11));
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
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Bordures réduites
        
        try {
            // Charger l'image de la carte
            java.net.URL imageUrl = getClass().getResource("/images/Map_Toulouse.jpg");
            if (imageUrl == null) {
                throw new Exception("Image Map_Toulouse.jpg non trouvée");
            }
            
            // Créer le panneau de carte avec la nouvelle classe
            cartePanel = new CartePanel(imageUrl, emailUtilisateur);
            
            // NOUVEAU: Ajouter la carte directement sans JScrollPane
            centerPanel.add(cartePanel, BorderLayout.CENTER);
            
            // Panel de contrôle simplifié en haut de la carte
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            controlPanel.setBackground(new Color(255, 255, 255, 180)); // Transparent
            controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            // Boutons de contrôle intégrés
            JButton btnZoomIn = new JButton("+");
            JButton btnZoomOut = new JButton("-");
            JButton btnResetZoom = new JButton("Repull-Up");
            
            // Style minimaliste
            for (JButton btn : new JButton[]{btnZoomIn, btnZoomOut, btnResetZoom}) {
                btn.setPreferredSize(new Dimension(40, 30));
                btn.setFont(new Font("Arial", Font.BOLD, 12));
                btn.setBackground(new Color(70, 130, 180));
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                btn.setFocusPainted(false);
            }
            
            btnZoomIn.addActionListener(e -> cartePanel.zoomIn());
            btnZoomOut.addActionListener(e -> cartePanel.zoomOut());
            btnResetZoom.addActionListener(e -> cartePanel.resetZoom());
            
            
            controlPanel.add(btnZoomIn);
            controlPanel.add(btnZoomOut);
            controlPanel.add(btnResetZoom);

            
            // Positionner le contrôle en haut de la carte
            cartePanel.setLayout(new BorderLayout());
            cartePanel.add(controlPanel, BorderLayout.NORTH);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la carte: " + e.getMessage());
            e.printStackTrace();
            
            // Afficher un message d'erreur
            JLabel lblErreur = new JLabel("<html><center>Impossible de charger la carte<br>Erreur: " + 
                                          e.getMessage() + "</center></html>", SwingConstants.CENTER);
            lblErreur.setFont(new Font("Arial", Font.BOLD, 16));
            lblErreur.setForeground(Color.RED);
            centerPanel.add(lblErreur, BorderLayout.CENTER);
        }
        
        return centerPanel;
    }
    
    private JPanel creerBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20)); // Bordures réduites
        
        btnPreparerStationnement = new JButton("Préparer un stationnement");
        btnPreparerStationnement.setFont(new Font("Arial", Font.BOLD, 13)); // Taille réduite
        btnPreparerStationnement.setBackground(new Color(70, 130, 180));
        btnPreparerStationnement.setForeground(Color.WHITE);
        btnPreparerStationnement.setFocusPainted(false);
        btnPreparerStationnement.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25)); // Padding réduit
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
    
    @Override
    public void dispose() {
        if (timer != null) {
            timer.stop();
        }
        super.dispose();
    }

    public CartePanel getCartePanel() {
        return cartePanel;
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
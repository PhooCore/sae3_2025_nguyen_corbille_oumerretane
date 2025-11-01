package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import dao.StationnementDAO;
import dao.UsagerDAO;
import modèle.Stationnement;
import modèle.Usager;

public class Page_Principale extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Usager usager;
    private JButton btnStationnement;
    private Timer timer;

    /**
     * Constructeur de la page principale
     * Page d'accueil après connexion avec barre de recherche, icônes de navigation et espace pour future carte
     * @param email l'email de l'utilisateur connecté
     */
    public Page_Principale(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        
        // Nettoyer les stationnements expirés au démarrage
        StationnementDAO.nettoyerStationnementsExpires();
        
        initialisePage();
        startStationnementCheck();
    }
    
    /**
     * Initialise l'interface utilisateur principale
     * Structure : Header (recherche + icônes) + Centre (message) + Bas (bouton action)
     */
    private void initialisePage() {
        this.setTitle("ParkinRose - Accueil");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(900, 700); // Taille adaptée pour tout afficher confortablement
        this.setLocationRelativeTo(null); // Centre la fenêtre
        this.setResizable(false); // Taille fixe
        
        // Panel principal avec layout BorderLayout pour une organisation simple
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
        
        // === BOUTON PRINCIPAL EN BAS - Action de stationnement ===
        JPanel bottomPanel = creerBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        updateStationnementIcon(); // Met à jour l'apparence de l'icône stationnement
    }
    
    /**
     * Crée la barre du haut contenant la barre de recherche et les icônes de navigation
     * @return JPanel configuré pour le header
     */
    private JPanel creerBarrePanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240)); // Gris très clair
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); // Marges internes
        headerPanel.setPreferredSize(new Dimension(900, 100)); // Hauteur fixe pour le header
        
        // === BARRE DE RECHERCHE (positionnée à gauche) ===
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(new Color(240, 240, 240));
        
        // Champ de texte pour la recherche
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 40)); // Taille généreuse
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), // Bordure grise
            BorderFactory.createEmptyBorder(8, 12, 8, 12) // Padding interne
        ));
        searchField.setText("Rechercher un parking..."); // Texte d'exemple
        searchField.setForeground(Color.GRAY); // Texte en gris pour l'exemple
        
        // Gestion du focus pour le texte d'exemple
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                // Efface le texte d'exemple quand l'utilisateur clique
                if (searchField.getText().equals("Rechercher un parking...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                // Remet le texte d'exemple si le champ est vide
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Rechercher un parking...");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        
        // Bouton de recherche (loupe)
        JButton btnSearch = new JButton("🔍");
        btnSearch.setFont(new Font("Arial", Font.PLAIN, 16));
        btnSearch.setBackground(Color.WHITE);
        btnSearch.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        btnSearch.setPreferredSize(new Dimension(50, 40));
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        searchPanel.add(searchField);
        searchPanel.add(btnSearch);
        
        // === ICÔNES DE NAVIGATION (positionnées à droite) ===
        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        iconsPanel.setBackground(new Color(240, 240, 240));
        
        // === ICÔNE STATIONNEMENT ===
        btnStationnement = new JButton();
        btnStationnement.setLayout(new BorderLayout()); // Layout pour image + texte
        btnStationnement.setBackground(new Color(240, 240, 240));
        btnStationnement.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); // Padding généreux
        btnStationnement.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnStationnement.setPreferredSize(new Dimension(120, 70)); // Largeur suffisante pour le texte
        
        // Image de l'icône stationnement
        JLabel lblIconePark = chargerIconeLabel("/images/parking.png", 40, 40, "P");
        // Texte sous l'icône
        JLabel lblTextPark = new JLabel("Stationnement", SwingConstants.CENTER);
        lblTextPark.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTextPark.setForeground(Color.DARK_GRAY);
        
        // Assemblage de l'icône : image au centre, texte en bas
        btnStationnement.add(lblIconePark, BorderLayout.CENTER);
        btnStationnement.add(lblTextPark, BorderLayout.SOUTH);
        btnStationnement.addActionListener(e -> ouvrirPageStationnement());
        
        // === ICÔNE UTILISATEUR ===
        JButton btnUtilisateur = new JButton();
        btnUtilisateur.setLayout(new BorderLayout());
        btnUtilisateur.setBackground(new Color(240, 240, 240));
        btnUtilisateur.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btnUtilisateur.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUtilisateur.setPreferredSize(new Dimension(120, 70)); // Même taille que stationnement
        
        // Image de l'icône utilisateur
        JLabel lblIconeUser = chargerIconeLabel("/images/utilisateur.png", 40, 40, "U");
        // Texte sous l'icône
        JLabel lblTextUser = new JLabel("Mon Compte", SwingConstants.CENTER);
        lblTextUser.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTextUser.setForeground(Color.DARK_GRAY);
        
        btnUtilisateur.add(lblIconeUser, BorderLayout.CENTER);
        btnUtilisateur.add(lblTextUser, BorderLayout.SOUTH);
        btnUtilisateur.addActionListener(e -> ouvrirPageUtilisateur());
        
        // Ajout des icônes au panel
        iconsPanel.add(btnStationnement);
        iconsPanel.add(btnUtilisateur);
        
        // === ASSEMBLAGE FINAL DU HEADER ===
        // Recherche à gauche, icônes à droite
        headerPanel.add(searchPanel, BorderLayout.WEST);
        headerPanel.add(iconsPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Crée le panel central avec un message simple
     * Cet espace est réservé pour une future carte interactive
     * @return JPanel configuré pour la zone centrale
     */
    private JPanel creerCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50)); // Marges généreuses
        
        // Message simple indiquant la fonctionnalité à venir
        JLabel lblMessage = new JLabel("Map à venir", SwingConstants.CENTER);
        lblMessage.setFont(new Font("Arial", Font.BOLD, 24));
        lblMessage.setForeground(Color.LIGHT_GRAY); // Couleur discrète
        
        centerPanel.add(lblMessage, BorderLayout.CENTER);
        
        return centerPanel;
    }
    
    /**
     * Crée le panel du bas avec le bouton d'action principal
     * @return JPanel configuré pour la zone du bas
     */
    private JPanel creerBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 30, 20)); // Marges
        
        // Bouton principal pour créer un stationnement
        JButton btnPreparerStationnement = new JButton("Préparer un stationnement en voirie");
        btnPreparerStationnement.setFont(new Font("Arial", Font.BOLD, 16));
        btnPreparerStationnement.setBackground(new Color(70, 130, 180)); // Bleu
        btnPreparerStationnement.setForeground(Color.WHITE);
        btnPreparerStationnement.setFocusPainted(false); // Désactive l'effet de focus
        btnPreparerStationnement.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40)); // Padding généreux
        btnPreparerStationnement.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Action : ouvrir la page de création de stationnement
        btnPreparerStationnement.addActionListener(e -> {
            Page_Garer_Voirie pageGarer = new Page_Garer_Voirie(emailUtilisateur);
            pageGarer.setVisible(true);
            dispose(); // Ferme la page actuelle
        });
        
        bottomPanel.add(btnPreparerStationnement);
        
        return bottomPanel;
    }
    
    /**
     * Charge une image depuis les ressources ou crée un placeholder textuel
     * Gère les erreurs de chargement d'image
     * @param chemin le chemin relatif de l'image dans le classpath
     * @param largeur la largeur souhaitée pour l'image redimensionnée
     * @param hauteur la hauteur souhaitée pour l'image redimensionnée
     * @param textePlaceholder le texte à afficher si l'image n'est pas trouvée
     * @return JLabel contenant l'image ou le placeholder
     */
    private JLabel chargerIconeLabel(String chemin, int largeur, int hauteur, String textePlaceholder) {
        try {
            // Tentative de chargement depuis le classpath
            java.net.URL imageUrl = getClass().getResource(chemin);
            if (imageUrl != null) {
                ImageIcon iconOriginal = new ImageIcon(imageUrl);
                // Redimensionnement avec lissage pour une meilleure qualité
                Image imageRedimensionnee = iconOriginal.getImage().getScaledInstance(largeur, hauteur, Image.SCALE_SMOOTH);
                return new JLabel(new ImageIcon(imageRedimensionnee), SwingConstants.CENTER);
            } else {
                // Image non trouvée : création d'un placeholder
                System.err.println("Image non trouvée: " + chemin);
                return creerLabelPlaceholder(largeur, hauteur, textePlaceholder);
            }
        } catch (Exception e) {
            // Erreur lors du chargement : création d'un placeholder
            System.err.println("Erreur lors du chargement de l'image " + chemin + ": " + e.getMessage());
            return creerLabelPlaceholder(largeur, hauteur, textePlaceholder);
        }
    }
    
    /**
     * Crée un label de remplacement avec texte stylisé
     * Utilisé quand une image n'est pas disponible
     * @param largeur la largeur du placeholder
     * @param hauteur la hauteur du placeholder
     * @param texte le texte à afficher dans le placeholder
     * @return JLabel configuré comme placeholder
     */
    private JLabel creerLabelPlaceholder(int largeur, int hauteur, String texte) {
        JLabel label = new JLabel(texte, SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(largeur, hauteur));
        label.setOpaque(true); // Permet d'avoir un fond coloré
        label.setBackground(Color.LIGHT_GRAY);
        label.setForeground(Color.DARK_GRAY);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        return label;
    }
    
    /**
     * Démarre la vérification périodique des stationnements actifs
     * Met à jour l'apparence de l'icône stationnement toutes les 10 secondes
     */
    private void startStationnementCheck() {
        // Timer qui se déclenche toutes les 10 secondes (10000 millisecondes)
        timer = new Timer(10000, e -> updateStationnementIcon());
        timer.start();
    }
    
    /**
     * Met à jour l'apparence de l'icône stationnement
     * Affiche un indicateur visuel si un stationnement est actif
     */
    private void updateStationnementIcon() {
        // Utilise la nouvelle méthode qui vérifie aussi la date de fin
        Stationnement stationnementActif = StationnementDAO.getStationnementActifValideByUsager(usager.getIdUsager());
        
        if (stationnementActif != null) {
            // Stationnement vraiment actif - indication visuelle
            btnStationnement.setBackground(new Color(255, 220, 220));
            btnStationnement.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2),
                BorderFactory.createEmptyBorder(3, 13, 3, 13)
            ));
        } else {
            // Pas de stationnement actif valide - apparence normale
            btnStationnement.setBackground(new Color(240, 240, 240));
            btnStationnement.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        }
    }
    
    /**
     * Ouvre la page des informations utilisateur
     */
    private void ouvrirPageUtilisateur() {
        Page_Utilisateur pageUtilisateur = new Page_Utilisateur(emailUtilisateur);
        pageUtilisateur.setVisible(true);
        dispose(); // Ferme la page actuelle
    }
    
    /**
     * Ouvre la page appropriée selon l'état du stationnement
     * - Si stationnement actif : page de gestion du stationnement en cours
     * - Si aucun stationnement : proposition de création
     */
    private void ouvrirPageStationnement() {
        // Utilise la nouvelle méthode de vérification
        Stationnement stationnementActif = StationnementDAO.getStationnementActifValideByUsager(usager.getIdUsager());
        
        if (stationnementActif != null) {
            // Stationnement valide : ouvrir la page de gestion
            Page_Stationnement_En_Cours pageStationnement = new Page_Stationnement_En_Cours(emailUtilisateur);
            pageStationnement.setVisible(true);
        } else {
            // Aucun stationnement valide : proposer d'en créer un
            int choix = JOptionPane.showConfirmDialog(this,
                "Aucun stationnement actif.\nVoulez-vous préparer un nouveau stationnement ?",
                "Aucun stationnement",
                JOptionPane.YES_NO_OPTION);
                
            if (choix == JOptionPane.YES_OPTION) {
                Page_Garer_Voirie pageGarer = new Page_Garer_Voirie(emailUtilisateur);
                pageGarer.setVisible(true);
                dispose();
            }
        }
    }
    
    /**
     * Surcharge de la méthode dispose() pour un nettoyage propre
     * Arrête le timer avant la fermeture pour éviter les fuites mémoire
     */
    @Override
    public void dispose() {
        if (timer != null) {
            timer.stop(); // Arrêt du timer
        }
        super.dispose(); // Appel de la méthode parente
    }
    

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Démarrage avec la page de bienvenue
                    new Page_Bienvenue().setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace(); // Affichage des erreurs éventuelles
                }
            }
        });
    }
}
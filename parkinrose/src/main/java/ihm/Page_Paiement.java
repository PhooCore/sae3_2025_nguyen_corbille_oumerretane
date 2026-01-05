package ihm;

import javax.swing.*;
import java.awt.*;
import controleur.ControleurPaiement;
import java.time.LocalDateTime;

public class Page_Paiement extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // Données du paiement
    private double montant;
    private String emailUtilisateur;
    private String typeVehicule;
    private String plaqueImmatriculation;
    private String idZone;
    private String nomZone;
    private int dureeHeures;
    private int dureeMinutes;
    private Integer idStationnement;
    
    // Composants d'interface
    private JTextField txtNomCarte;
    private JTextField txtNumeroCarte;
    private JTextField txtDateExpiration;
    private JTextField txtCVV;
    
    // Boutons
    private JButton btnAnnuler;
    private JButton btnPayer;
    
    // Référence au contrôleur
    private ControleurPaiement controleur;

    // **CONSTRUCTEUR POUR VOIRIE** - SANS idStationnement
    public Page_Paiement(double montant, String emailUtilisateur, String typeVehicule, 
                        String plaqueImmatriculation, String idZone, String nomZone, 
                        int dureeHeures, int dureeMinutes) {
        this(montant, emailUtilisateur, typeVehicule, plaqueImmatriculation, 
             idZone, nomZone, dureeHeures, dureeMinutes, null, null);
    }
    
    // **CONSTRUCTEUR POUR PARKING** - AVEC idStationnement
    public Page_Paiement(double montant, String emailUtilisateur, String typeVehicule, 
                        String plaqueImmatriculation, String idZone, String nomZone,
                        int dureeHeures, int dureeMinutes, Integer idStationnement) {
        this(montant, emailUtilisateur, typeVehicule, plaqueImmatriculation, 
             idZone, nomZone, dureeHeures, dureeMinutes, idStationnement, null);
    }
    
    public Page_Paiement(double montant, String emailUtilisateur, String typeVehicule, 
                        String plaqueImmatriculation, String idZone, String nomZone,
                        int dureeHeures, int dureeMinutes, Integer idStationnement, 
                        LocalDateTime heureDepart) {
        this.montant = montant;
        this.emailUtilisateur = emailUtilisateur;
        this.typeVehicule = typeVehicule;
        this.plaqueImmatriculation = plaqueImmatriculation;
        this.idZone = idZone;
        this.nomZone = nomZone;
        this.dureeHeures = dureeHeures;
        this.dureeMinutes = dureeMinutes;
        this.idStationnement = idStationnement;
        
        initialisePage();
        
        // Initialiser le contrôleur APRÈS avoir créé l'interface
        this.controleur = new ControleurPaiement(this);
    }
    
    private void initialisePage() {
        this.setTitle("Paiement du stationnement");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(700, 800);
        this.setLocationRelativeTo(null);
        this.setResizable(true);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BorderLayout(0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Titre
        JLabel lblTitre = new JLabel("Paiement de votre stationnement", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitre.setForeground(new Color(0, 80, 180));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // Panel central avec scroll pour les petits écrans
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        // ========== PANEL INFORMATIONS STATIONNEMENT ==========
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(240, 245, 255));
        infoPanel.setLayout(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Détails du stationnement"),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JPanel panelInfosDetails = new JPanel(new GridLayout(0, 1, 10, 10));
        panelInfosDetails.setBackground(new Color(240, 245, 255));
        
        // Type de stationnement
        String typeStationnement = (idStationnement == null) ? "Voirie" : "Parking";
        JLabel lblTypeStationnement = new JLabel("Type de stationnement: " + typeStationnement);
        lblTypeStationnement.setFont(new Font("Arial", Font.BOLD, 18));
        lblTypeStationnement.setForeground(new Color(0, 80, 180));
        
        // Montant
        JLabel lblMontant = new JLabel(String.format("%.2f €", montant));
        lblMontant.setFont(new Font("Arial", Font.BOLD, 24));
        lblMontant.setForeground(new Color(0, 150, 0));
        
        // Véhicule
        JLabel lblVehicule = new JLabel("Véhicule: " + typeVehicule + " - " + plaqueImmatriculation);
        lblVehicule.setFont(new Font("Arial", Font.PLAIN, 16));
        
        // Zone/Parking
        JLabel lblZone = new JLabel("Zone: " + nomZone);
        lblZone.setFont(new Font("Arial", Font.PLAIN, 16));
        
        panelInfosDetails.add(lblTypeStationnement);
        panelInfosDetails.add(lblMontant);
        panelInfosDetails.add(lblVehicule);
        panelInfosDetails.add(lblZone);
        
        // Informations spécifiques selon le type
        if (idStationnement == null) {
            // Voirie: ajouter durée
            JLabel lblDuree = new JLabel("Durée: " + dureeHeures + "h" + dureeMinutes + "min");
            lblDuree.setFont(new Font("Arial", Font.PLAIN, 16));
            panelInfosDetails.add(lblDuree);
            
            // Conditions voirie
            JTextArea txtConditions = new JTextArea(
                "✓ Le stationnement en voirie est limité à la durée sélectionnée\n" +
                "✓ Vous devez valider la fin de votre stationnement\n" +
                "✓ Le non-respect des règles peut entraîner une amende"
            );
            txtConditions.setFont(new Font("Arial", Font.PLAIN, 14));
            txtConditions.setBackground(new Color(250, 250, 255));
            txtConditions.setEditable(false);
            txtConditions.setLineWrap(true);
            txtConditions.setWrapStyleWord(true);
            txtConditions.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            infoPanel.add(txtConditions, BorderLayout.CENTER);
        } else {
            // Parking: message d'information
            JTextArea txtInfoParking = new JTextArea(
                "✓ Paiement de votre stationnement en parking\n" +
                "✓ Votre véhicule peut quitter après paiement\n" +
                "✓ Reçu disponible dans votre espace utilisateur"
            );
            txtInfoParking.setFont(new Font("Arial", Font.PLAIN, 14));
            txtInfoParking.setBackground(new Color(250, 250, 255));
            txtInfoParking.setEditable(false);
            txtInfoParking.setLineWrap(true);
            txtInfoParking.setWrapStyleWord(true);
            txtInfoParking.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            infoPanel.add(txtInfoParking, BorderLayout.CENTER);
        }
        
        infoPanel.add(panelInfosDetails, BorderLayout.NORTH);
        centerPanel.add(infoPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // ========== FORMULAIRE DE PAIEMENT ==========
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new BorderLayout(10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
            "Informations de paiement",
            0, 0,
            new Font("Arial", Font.BOLD, 16)
        ));
        
        JPanel panelChamps = new JPanel();
        panelChamps.setBackground(Color.WHITE);
        panelChamps.setLayout(new GridLayout(0, 1, 15, 15));
        panelChamps.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Nom sur la carte
        JLabel lblNomCarte = new JLabel("Nom sur la carte :");
        lblNomCarte.setFont(new Font("Arial", Font.BOLD, 14));
        txtNomCarte = new JTextField();
        txtNomCarte.setFont(new Font("Arial", Font.PLAIN, 14));
        txtNomCarte.setPreferredSize(new Dimension(300, 35));
        
        panelChamps.add(lblNomCarte);
        panelChamps.add(txtNomCarte);
        
        // Numéro de carte
        JLabel lblNumeroCarte = new JLabel("Numéro de carte :");
        lblNumeroCarte.setFont(new Font("Arial", Font.BOLD, 14));
        txtNumeroCarte = new JTextField();
        txtNumeroCarte.setFont(new Font("Arial", Font.PLAIN, 14));
        txtNumeroCarte.setPreferredSize(new Dimension(300, 35));
        
        panelChamps.add(lblNumeroCarte);
        panelChamps.add(txtNumeroCarte);
        
        // Panel pour date et CVV
        JPanel panelDateCVV = new JPanel(new GridLayout(1, 2, 20, 0));
        panelDateCVV.setBackground(Color.WHITE);
        
        // Date d'expiration
        JPanel panelDate = new JPanel(new BorderLayout(0, 5));
        panelDate.setBackground(Color.WHITE);
        JLabel lblDate = new JLabel("Date d'expiration (MM/AA) :");
        lblDate.setFont(new Font("Arial", Font.BOLD, 14));
        txtDateExpiration = new JTextField();
        txtDateExpiration.setFont(new Font("Arial", Font.PLAIN, 14));
        txtDateExpiration.setPreferredSize(new Dimension(150, 35));
        
        panelDate.add(lblDate, BorderLayout.NORTH);
        panelDate.add(txtDateExpiration, BorderLayout.CENTER);
        
        // CVV
        JPanel panelCVV = new JPanel(new BorderLayout(0, 5));
        panelCVV.setBackground(Color.WHITE);
        JLabel lblCVV = new JLabel("Code de sécurité (CVV) :");
        lblCVV.setFont(new Font("Arial", Font.BOLD, 14));
        txtCVV = new JTextField();
        txtCVV.setFont(new Font("Arial", Font.PLAIN, 14));
        txtCVV.setPreferredSize(new Dimension(100, 35));
        
        panelCVV.add(lblCVV, BorderLayout.NORTH);
        panelCVV.add(txtCVV, BorderLayout.CENTER);
        
        panelDateCVV.add(panelDate);
        panelDateCVV.add(panelCVV);
        
        panelChamps.add(panelDateCVV);
        
        // Informations de sécurité
        JPanel panelSecurite = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSecurite.setBackground(Color.WHITE);
        JLabel lblInfoSecurite = new JLabel("🔒 Vos informations de paiement sont sécurisées et cryptées");
        lblInfoSecurite.setFont(new Font("Arial", Font.ITALIC, 12));
        lblInfoSecurite.setForeground(new Color(100, 100, 100));
        panelSecurite.add(lblInfoSecurite);
        
        panelChamps.add(panelSecurite);
        
        formPanel.add(panelChamps, BorderLayout.CENTER);
        centerPanel.add(formPanel);
        
        // Ajouter un scroll pane pour s'assurer que tout est visible
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // ========== PANEL DES BOUTONS ==========
        JPanel panelBoutons = new JPanel(new BorderLayout(20, 0));
        panelBoutons.setBackground(Color.WHITE);
        panelBoutons.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // Bouton Annuler
        btnAnnuler = new JButton("Annuler");
        btnAnnuler.setFont(new Font("Arial", Font.PLAIN, 14));
        btnAnnuler.setPreferredSize(new Dimension(120, 45));
        
        // Panel pour le bouton de paiement
        JPanel panelPaiement = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelPaiement.setBackground(Color.WHITE);
        
        String texteBouton = (idStationnement == null) ? 
            "Payer " + String.format("%.2f", montant) + " € et démarrer" : 
            "Payer " + String.format("%.2f", montant) + " € et terminer";
        
        btnPayer = new JButton(texteBouton);
        btnPayer.setFont(new Font("Arial", Font.BOLD, 16));
        btnPayer.setBackground(new Color(70, 130, 180));
        btnPayer.setForeground(Color.WHITE);
        btnPayer.setFocusPainted(false);
        btnPayer.setPreferredSize(new Dimension(220, 50));
        
        panelPaiement.add(btnPayer);
        
        panelBoutons.add(btnAnnuler, BorderLayout.WEST);
        panelBoutons.add(panelPaiement, BorderLayout.EAST);
        
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
        
        // Focus sur le premier champ
        SwingUtilities.invokeLater(() -> txtNomCarte.requestFocus());
    }
    
    // ========== GETTERS POUR LE CONTRÔLEUR ==========
    
    // Getters pour les champs de formulaire
    public JTextField getTxtNomCarte() { return txtNomCarte; }
    public JTextField getTxtNumeroCarte() { return txtNumeroCarte; }
    public JTextField getTxtDateExpiration() { return txtDateExpiration; }
    public JTextField getTxtCVV() { return txtCVV; }
    
    // Getters pour les données
    public double getMontant() { return montant; }
    public String getEmailUtilisateur() { return emailUtilisateur; }
    public String getTypeVehicule() { return typeVehicule; }
    public String getPlaqueImmatriculation() { return plaqueImmatriculation; }
    public String getIdZone() { return idZone; }
    public String getNomZone() { return nomZone; }
    public int getDureeHeures() { return dureeHeures; }
    public int getDureeMinutes() { return dureeMinutes; }
    public Integer getIdStationnement() { return idStationnement; }
    
    // Getters pour les boutons
    public JButton getBtnAnnuler() { return btnAnnuler; }
    public JButton getBtnPayer() { return btnPayer; }
    
    // Méthode pour obtenir le parent frame
    public JFrame getParentFrame() {
        return null; // Modifier si nécessaire
    }
}
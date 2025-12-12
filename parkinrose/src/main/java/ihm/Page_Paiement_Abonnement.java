package ihm;

import javax.swing.*;
import modele.Abonnement;
import modele.Usager;
import modele.dao.UsagerDAO;
import java.awt.*;
import controleur.ControleurPaiementAbonnement;

public class Page_Paiement_Abonnement extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Abonnement abonnement;
    private Usager usager;
    private double montant;
    
    // Composants d'interface
    private JTextField txtNomCarte;
    private JTextField txtNumeroCarte;
    private JTextField txtDateExpiration;
    private JTextField txtCVV;
    
    // Boutons
    private JButton btnAnnuler;
    private JButton btnPayer;
    
    // Référence au contrôleur
    private ControleurPaiementAbonnement controleur;
    
    public Page_Paiement_Abonnement(String emailUtilisateur, Abonnement abonnement) {
        this.emailUtilisateur = emailUtilisateur;
        this.abonnement = abonnement;
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        this.montant = abonnement.getTarifAbonnement();
        
        initialisePage();
        
        // Initialiser le contrôleur APRÈS avoir créé l'interface
        this.controleur = new ControleurPaiementAbonnement(this);
    }
    
    private void initialisePage() {
        this.setTitle("Paiement d'abonnement");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(700, 800);
        this.setLocationRelativeTo(null);
        this.setResizable(true);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BorderLayout(0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Titre
        JLabel lblTitre = new JLabel("Paiement de votre abonnement", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitre.setForeground(new Color(0, 80, 180));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // Panel central avec scroll pour les petits écrans
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        // ========== PANEL INFORMATIONS ABONNEMENT ==========
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(240, 245, 255));
        infoPanel.setLayout(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Détails de votre abonnement"),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JPanel panelInfosDetails = new JPanel(new GridLayout(0, 1, 10, 10));
        panelInfosDetails.setBackground(new Color(240, 245, 255));
        
        JLabel lblLibelleAbonnement = new JLabel(abonnement.getLibelleAbonnement());
        lblLibelleAbonnement.setFont(new Font("Arial", Font.BOLD, 20));
        lblLibelleAbonnement.setForeground(new Color(0, 80, 180));
        
        JLabel lblMontant = new JLabel(String.format("%.2f €", montant));
        lblMontant.setFont(new Font("Arial", Font.BOLD, 24));
        lblMontant.setForeground(new Color(0, 150, 0));
        
        // Déterminer la période selon le type d'abonnement
        String periode = "Période : ";
        String idAbonnement = abonnement.getIdAbonnement().toUpperCase();
        if (idAbonnement.contains("MENSUEL")) {
            periode += "Mensuel";
        } else if (idAbonnement.contains("HEBDO") || idAbonnement.contains("SEMAINE")) {
            periode += "Hebdomadaire";
        } else if (idAbonnement.contains("ANNUEL") || idAbonnement.contains("ANNU")) {
            periode += "Annuel";
        } else if (idAbonnement.contains("BASIC")) {
            periode += "Mensuel";
        } else if (idAbonnement.contains("PREMIUM")) {
            periode += "Annuel";
        } else if (idAbonnement.contains("ETUDIANT")) {
            periode += "Semestriel";
        } else if (idAbonnement.contains("SENIOR")) {
            periode += "Trimestriel";
        } else {
            periode += "Selon formule";
        }
        
        JLabel lblPeriode = new JLabel(periode);
        lblPeriode.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JLabel lblReference = new JLabel("Référence : " + abonnement.getIdAbonnement());
        lblReference.setFont(new Font("Arial", Font.ITALIC, 14));
        lblReference.setForeground(Color.GRAY);
        
        panelInfosDetails.add(lblLibelleAbonnement);
        panelInfosDetails.add(lblMontant);
        panelInfosDetails.add(lblPeriode);
        panelInfosDetails.add(lblReference);
        
        infoPanel.add(panelInfosDetails, BorderLayout.NORTH);
        
        // Avantages de l'abonnement
        JTextArea txtAvantages = new JTextArea(getAvantagesByType(abonnement.getIdAbonnement()));
        txtAvantages.setFont(new Font("Arial", Font.PLAIN, 14));
        txtAvantages.setBackground(new Color(250, 250, 255));
        txtAvantages.setEditable(false);
        txtAvantages.setLineWrap(true);
        txtAvantages.setWrapStyleWord(true);
        txtAvantages.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Avantages inclus"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        infoPanel.add(txtAvantages, BorderLayout.CENTER);
        
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
        btnAnnuler.setActionCommand("ANNULER");
        
        // Panel pour le bouton de paiement
        JPanel panelPaiement = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelPaiement.setBackground(Color.WHITE);
        
        btnPayer = new JButton("Payer " + String.format("%.2f", montant) + " €");
        btnPayer.setFont(new Font("Arial", Font.BOLD, 16));
        btnPayer.setBackground(new Color(70, 130, 180));
        btnPayer.setForeground(Color.WHITE);
        btnPayer.setFocusPainted(false);
        btnPayer.setPreferredSize(new Dimension(200, 50));
        btnPayer.setActionCommand("PAYER");
        
        panelPaiement.add(btnPayer);
        
        panelBoutons.add(btnAnnuler, BorderLayout.WEST);
        panelBoutons.add(panelPaiement, BorderLayout.EAST);
        
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
        
        // Focus sur le premier champ
        SwingUtilities.invokeLater(() -> txtNomCarte.requestFocus());
    }
    
    private String getAvantagesByType(String idAbonnement) {
        switch(idAbonnement.toUpperCase()) {
            case "ABN_BASIC":
                return "✓ Stationnement illimité en voirie (2h max)\n" +
                       "✓ 10% de réduction dans les parkings partenaires\n" +
                       "✓ Accès aux zones bleues\n" +
                       "✓ Notification par SMS avant expiration";
            case "ABN_PREMIUM":
                return "✓ Stationnement illimité en voirie\n" +
                       "✓ 25% de réduction dans les parkings partenaires\n" +
                       "✓ Accès à toutes les zones\n" +
                       "✓ Réservation prioritaire\n" +
                       "✓ Assistance 24h/24\n" +
                       "✓ Assurance stationnement incluse";
            case "ABN_ETUDIANT":
                return "✓ 50% de réduction sur tous les stationnements\n" +
                       "✓ Accès aux zones universitaires\n" +
                       "✓ Valable uniquement avec carte étudiante\n" +
                       "✓ Paiement mensuel facilité";
            case "ABN_SENIOR":
                return "✓ 40% de réduction sur tous les stationnements\n" +
                       "✓ Accès aux zones résidentielles\n" +
                       "✓ Pour les 65 ans et plus\n" +
                       "✓ Renouvellement automatique\n" +
                       "✓ Service client dédié";
            default:
                return "✓ Avantages personnalisés\n" +
                       "✓ Flexibilité selon vos besoins\n" +
                       "✓ Contactez-nous pour plus d'informations";
        }
    }
    
    // Getters pour le contrôleur
    public JTextField getTxtTitulaire() { return txtNomCarte; }
    public JTextField getTxtNumeroCarte() { return txtNumeroCarte; }
    public JTextField getTxtExpiration() { return txtDateExpiration; }
    public JTextField getTxtCrypto() { return txtCVV; }
    
    public Abonnement getAbonnement() { return abonnement; }
    public String getEmailUtilisateur() { return emailUtilisateur; }
    public double getMontant() { return montant; }
    public Usager getUsager() { return usager; }
    
    // GETTERS POUR LES BOUTONS
    public JButton getBtnAnnuler() { return btnAnnuler; }
    public JButton getBtnPayer() { return btnPayer; }
    
    // Méthode pour obtenir le parent frame (si nécessaire)
    public JFrame getParentFrame() {
        return null;
    }
}
package ihm;

import javax.swing.*;
import java.awt.*;
import controleur.ControleurPaiement;
import java.time.LocalDateTime;

public class Page_Paiement extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Variables accessibles au contrôleur
    public double montant;
    public String emailUtilisateur;
    public String typeVehicule;
    public String plaqueImmatriculation;
    public String idZone;
    public String nomZone;
    public int dureeHeures;
    public int dureeMinutes;
    public Integer idStationnement;
    
    // Champs de formulaire
    public JTextField txtNomCarte;
    public JTextField txtNumeroCarte;
    public JTextField txtDateExpiration;
    public JTextField txtCVV;
    
    // Boutons
    public JButton btnAnnuler; 
    public JButton btnPayer;   

    // **CONSTRUCTEUR POUR VOIRIE** - SANS idStationnement
    public Page_Paiement(double montant, String emailUtilisateur, String typeVehicule, 
                        String plaqueImmatriculation, String idZone, String nomZone, 
                        int dureeHeures, int dureeMinutes) {
        this.montant = montant;
        this.emailUtilisateur = emailUtilisateur;
        this.typeVehicule = typeVehicule;
        this.plaqueImmatriculation = plaqueImmatriculation;
        this.idZone = idZone;
        this.nomZone = nomZone;
        this.dureeHeures = dureeHeures;
        this.dureeMinutes = dureeMinutes;
        this.idStationnement = null; // Pour voirie
        
        initialisePage();
        new ControleurPaiement(this);
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
        new ControleurPaiement(this);
    }
    
    private void initialisePage() {
        this.setTitle("Paiement du stationnement");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(550, 650);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitre = new JLabel("Paiement", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new GridLayout(0, 1, 10, 10));
        
        // Montant
        JLabel lblMontant = new JLabel("Montant à payer: " + String.format("%.2f", montant) + " €");
        lblMontant.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(lblMontant);
        
        formPanel.add(new JLabel(" "));
        
        // Type de stationnement
        String typeStationnement = (idStationnement == null) ? "Voirie" : "Parking";
        JLabel lblType = new JLabel("Type: " + typeStationnement);
        formPanel.add(lblType);
        
        // Véhicule
        JLabel lblVehicule = new JLabel("Véhicule: " + plaqueImmatriculation);
        formPanel.add(lblVehicule);
        
        // Zone/Parking
        JLabel lblZone = new JLabel("Zone: " + nomZone);
        formPanel.add(lblZone);
        
        if (idStationnement == null) {
            JLabel lblDuree = new JLabel("Durée: " + dureeHeures + "h" + dureeMinutes + "min");
            formPanel.add(lblDuree);
        }
        
        formPanel.add(new JLabel(" "));
        
        // Informations de la carte
        formPanel.add(new JLabel("Informations de la carte:"));
        
        // Nom sur la carte
        formPanel.add(new JLabel("Nom sur la carte:"));
        txtNomCarte = new JTextField();
        txtNomCarte.setPreferredSize(new Dimension(300, 30));
        formPanel.add(txtNomCarte);
        
        // Numéro de carte
        formPanel.add(new JLabel("Numéro de carte:"));
        txtNumeroCarte = new JTextField();
        txtNumeroCarte.setPreferredSize(new Dimension(300, 30));
        formPanel.add(txtNumeroCarte);
        
        // Date et CVV
        JPanel panelDateCVV = new JPanel(new GridLayout(1, 2, 15, 10));
        panelDateCVV.setBackground(Color.WHITE);
        
        JPanel panelDate = new JPanel(new BorderLayout());
        panelDate.setBackground(Color.WHITE);
        panelDate.add(new JLabel("Date expiration (MM/AA):"), BorderLayout.NORTH);
        txtDateExpiration = new JTextField();
        txtDateExpiration.setPreferredSize(new Dimension(120, 30));
        panelDate.add(txtDateExpiration, BorderLayout.CENTER);
        
        JPanel panelCVV = new JPanel(new BorderLayout());
        panelCVV.setBackground(Color.WHITE);
        panelCVV.add(new JLabel("CVV:"), BorderLayout.NORTH);
        txtCVV = new JTextField();
        txtCVV.setPreferredSize(new Dimension(80, 30));
        panelCVV.add(txtCVV, BorderLayout.CENTER);
        
        panelDateCVV.add(panelDate);
        panelDateCVV.add(panelCVV);
        
        formPanel.add(panelDateCVV);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Boutons
        JPanel panelBoutons = new JPanel(new FlowLayout());
        panelBoutons.setBackground(Color.WHITE);
        
        btnAnnuler = new JButton("Annuler");
        btnPayer = new JButton("Payer maintenant");
        
        btnPayer.setBackground(new Color(70, 130, 180));
        btnPayer.setForeground(Color.WHITE);
        btnPayer.setFocusPainted(false);
        
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnPayer);
        
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
    }
    
}
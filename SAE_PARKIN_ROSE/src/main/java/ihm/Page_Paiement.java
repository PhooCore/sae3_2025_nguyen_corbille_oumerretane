package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import dao.PaiementDAO;
import modèle.Paiement;
import modèle.Usager;
import dao.UsagerDAO;

public class Page_Paiement extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private double montant;
    private String emailUtilisateur;
    private Usager usager;
    private String typeVehicule;
    private String plaqueImmatriculation;
    private String zone;
    private int dureeHeures;
    private int dureeMinutes;
    
    // Champs de saisie
    private JTextField txtNomCarte;
    private JTextField txtNumeroCarte;
    private JTextField txtDateExpiration;
    private JTextField txtCVV;
    
    public Page_Paiement(double montant, String emailUtilisateur, String typeVehicule, 
                                String plaqueImmatriculation, String zone, int dureeHeures, int dureeMinutes) {
        this.montant = montant;
        this.emailUtilisateur = emailUtilisateur;
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        this.typeVehicule = typeVehicule;
        this.plaqueImmatriculation = plaqueImmatriculation;
        this.zone = zone;
        this.dureeHeures = dureeHeures;
        this.dureeMinutes = dureeMinutes;
        
        initializeUI();
    }
    
    private void initializeUI() {
        this.setTitle("Paiement du stationnement");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(500, 500);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Titre
        JLabel lblTitre = new JLabel("Paiement", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // Panel central avec formulaire
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new GridLayout(0, 1, 10, 10));
        
        // Montant
        JLabel lblMontant = new JLabel("Montant à payer: " + String.format("%.2f", montant) + " €");
        lblMontant.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(lblMontant);
        
        formPanel.add(new JLabel(" ")); // Espace
        
        // Informations carte
        formPanel.add(new JLabel("Informations de la carte:"));
        
        formPanel.add(new JLabel("Nom sur la carte:"));
        txtNomCarte = new JTextField();
        formPanel.add(txtNomCarte);
        
        formPanel.add(new JLabel("Numéro de carte:"));
        txtNumeroCarte = new JTextField();
        formPanel.add(txtNumeroCarte);
        
        JPanel panelDateCVV = new JPanel(new GridLayout(1, 2, 10, 10));
        panelDateCVV.setBackground(Color.WHITE);
        
        JPanel panelDate = new JPanel(new BorderLayout());
        panelDate.setBackground(Color.WHITE);
        panelDate.add(new JLabel("Date expiration (MM/AA):"), BorderLayout.NORTH);
        txtDateExpiration = new JTextField();
        panelDate.add(txtDateExpiration, BorderLayout.CENTER);
        
        JPanel panelCVV = new JPanel(new BorderLayout());
        panelCVV.setBackground(Color.WHITE);
        panelCVV.add(new JLabel("CVV:"), BorderLayout.NORTH);
        txtCVV = new JTextField();
        panelCVV.add(txtCVV, BorderLayout.CENTER);
        
        panelDateCVV.add(panelDate);
        panelDateCVV.add(panelCVV);
        
        formPanel.add(panelDateCVV);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Boutons
        JPanel panelBoutons = new JPanel(new FlowLayout());
        
        JButton btnAnnuler = new JButton("Annuler");
        JButton btnPayer = new JButton("Payer maintenant");
        
        btnAnnuler.addActionListener(e -> annuler());
        btnPayer.addActionListener(e -> traiterPaiement());
        
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnPayer);
        
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
    }
    
    private void annuler() {
        int confirmation = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir annuler le paiement ?",
            "Confirmation d'annulation",
            JOptionPane.YES_NO_OPTION);
            
        if (confirmation == JOptionPane.YES_OPTION) {
            this.dispose();
        }
    }
    
    private void traiterPaiement() {
        // Validation des champs
        if (!validerFormulaire()) {
            return;
        }
        
        try {
            // Créer l'objet Paiement
            Paiement paiement = new Paiement(
                txtNomCarte.getText().trim(),
                txtNumeroCarte.getText().trim(),
                txtCVV.getText().trim(),
                montant,
                usager.getIdUsager()
            );
            
            // Enregistrer le paiement dans la base de données
            boolean paiementReussi = PaiementDAO.enregistrerPaiement(paiement);
            
            if (paiementReussi) {
                // Créer le stationnement associé au paiement
                boolean stationnementCree = PaiementDAO.creerStationnementApresPaiement(
                    usager.getIdUsager(),
                    typeVehicule,
                    plaqueImmatriculation,
                    zone,
                    dureeHeures,
                    dureeMinutes,
                    montant,
                    paiement.getIdPaiement()
                );
                
                if (stationnementCree) {
                    JOptionPane.showMessageDialog(this,
                        "Paiement effectué avec succès !\n" +
                        "Stationnement confirmé pour " + plaqueImmatriculation + "\n" +
                        "Zone: " + zone + "\n" +
                        "Durée: " + dureeHeures + "h" + dureeMinutes + "min\n" +
                        "Montant: " + String.format("%.2f", montant) + " €",
                        "Paiement réussi",
                        JOptionPane.INFORMATION_MESSAGE);
                        
                    // Retour à la page principale
                    Page_Test pageTest = new Page_Test(emailUtilisateur, usager.getNomUsager(), usager.getPrenomUsager());
                    pageTest.setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Erreur lors de la création du stationnement",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'enregistrement du paiement",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du traitement du paiement: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validerFormulaire() {
        if (txtNomCarte.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir le nom sur la carte", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (txtNumeroCarte.getText().trim().isEmpty() || txtNumeroCarte.getText().trim().length() < 16) {
            JOptionPane.showMessageDialog(this, "Numéro de carte invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (txtDateExpiration.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir la date d'expiration", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (txtCVV.getText().trim().isEmpty() || txtCVV.getText().trim().length() != 3) {
            JOptionPane.showMessageDialog(this, "CVV invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
}
package ihm;

import javax.swing.*;
import controleur.PaiementControleur;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import modele.Usager;
import modele.dao.UsagerDAO;

public class Page_Paiement extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    private double montant;
    private String emailUtilisateur;
    private Usager usager;
    private String typeVehicule;
    private String plaqueImmatriculation;
    private String idZone;
    private String nomZone;
    private int dureeHeures;
    private int dureeMinutes;
    private Integer idStationnement;
    private LocalDateTime heureDepart;
    private PaiementControleur controleur;
    
    private JTextField txtNomCarte;
    private JTextField txtNumeroCarte;
    private JTextField txtDateExpiration;
    private JTextField txtCVV;
    
    public Page_Paiement(double montant, String emailUtilisateur, String typeVehicule, 
                        String plaqueImmatriculation, String idZone, String nomZone, 
                        int dureeHeures, int dureeMinutes) {
        this(montant, emailUtilisateur, typeVehicule, plaqueImmatriculation, idZone, nomZone,
             dureeHeures, dureeMinutes, null, null);
    }
    
    public Page_Paiement(double montant, String emailUtilisateur, String typeVehicule, 
                        String plaqueImmatriculation, String idZone, String nomZone,
                        int dureeHeures, int dureeMinutes, Integer idStationnement, LocalDateTime heureDepart) {
        this.montant = montant;
        this.emailUtilisateur = emailUtilisateur;
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        this.typeVehicule = typeVehicule;
        this.plaqueImmatriculation = plaqueImmatriculation;
        this.idZone = idZone;
        this.nomZone = nomZone;
        this.dureeHeures = dureeHeures;
        this.dureeMinutes = dureeMinutes;
        this.idStationnement = idStationnement;
        this.heureDepart = heureDepart;
        this.controleur = new PaiementControleur(emailUtilisateur);
        initialisePage();
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
        
        JLabel lblMontant = new JLabel("Montant à payer: " + String.format("%.2f", montant) + " €");
        lblMontant.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(lblMontant);
        
        formPanel.add(new JLabel(" "));
        
        String typeStationnement = (idStationnement == null) ? "Voirie" : "Parking";
        JLabel lblType = new JLabel("Type: " + typeStationnement);
        formPanel.add(lblType);
        
        JLabel lblVehicule = new JLabel("Véhicule: " + plaqueImmatriculation);
        formPanel.add(lblVehicule);
        
        JLabel lblZone = new JLabel("Zone: " + nomZone);
        formPanel.add(lblZone);
        
        if (idStationnement == null) {
            JLabel lblDuree = new JLabel("Durée: " + dureeHeures + "h" + dureeMinutes + "min");
            formPanel.add(lblDuree);
        }
        
        formPanel.add(new JLabel(" "));
        
        formPanel.add(new JLabel("Informations de la carte:"));
        
        formPanel.add(new JLabel("Nom sur la carte:"));
        txtNomCarte = new JTextField();
        txtNomCarte.setPreferredSize(new Dimension(300, 30));
        formPanel.add(txtNomCarte);
        
        formPanel.add(new JLabel("Numéro de carte:"));
        txtNumeroCarte = new JTextField();
        txtNumeroCarte.setPreferredSize(new Dimension(300, 30));
        formPanel.add(txtNumeroCarte);
        
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
        
        JPanel panelBoutons = new JPanel(new FlowLayout());
        
        JButton btnAnnuler = new JButton("Annuler");
        JButton btnPayer = new JButton("Payer maintenant");
        
        btnPayer.setBackground(new Color(70, 130, 180));
        btnPayer.setForeground(Color.WHITE);
        btnPayer.setFocusPainted(false);
        
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
    
    private boolean validerFormulaire() {
        PaiementControleur controleur = new PaiementControleur(emailUtilisateur);
        
        return controleur.validerFormulairePaiementComplet(
            txtNomCarte.getText().trim(),
            txtNumeroCarte.getText().trim(),
            txtDateExpiration.getText().trim(),
            txtCVV.getText().trim(),
            this
        );
    }

    private void traiterPaiement() {
        if (!validerFormulaire()) {
            return;
        }
        
        boolean succes = false;
        
        if (idStationnement == null) {
            succes = controleur.traiterPaiementVoirie(
                txtNomCarte.getText().trim(),
                txtNumeroCarte.getText().trim(),
                txtDateExpiration.getText().trim(),
                txtCVV.getText().trim(),
                montant,
                typeVehicule,
                plaqueImmatriculation,
                idZone,
                dureeHeures,
                dureeMinutes,
                this
            );
        } else {
            succes = controleur.traiterPaiementParking(
                txtNomCarte.getText().trim(),
                txtNumeroCarte.getText().trim(),
                txtDateExpiration.getText().trim(),
                txtCVV.getText().trim(),
                montant,
                idStationnement,
                heureDepart,
                this
            );
        }
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
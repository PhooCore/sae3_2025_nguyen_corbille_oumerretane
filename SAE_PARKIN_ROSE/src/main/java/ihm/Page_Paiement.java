package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;

import dao.PaiementDAO;
import dao.StationnementDAO;
import modèle.Paiement;
import modèle.Usager;
import dao.UsagerDAO;

public class Page_Paiement extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    private double montant;                   // Montant à payer
    private String emailUtilisateur;          // Email de l'utilisateur connecté
    private Usager usager;                    // Objet utilisateur avec ses informations
    private String typeVehicule;              // Type de véhicule (Voiture, Moto, Camion)
    private String plaqueImmatriculation;     // Plaque du véhicule
    private String idZone;            		  // ID de la zone (nouveau)
    private String nomZone;                   // Nom de la zone pour l'affichage
    private int dureeHeures;                  // Durée en heures (pour voirie)
    private int dureeMinutes;                 // Durée en minutes (pour voirie)
    private Integer idStationnement;          // ID du stationnement existant (null si nouveau)
    private LocalDateTime heureDepart;        // Heure de départ (pour les parkings)
    
    // === CHAMPS DU FORMULAIRE DE PAIEMENT ===
    private JTextField txtNomCarte;           // Nom porté sur la carte
    private JTextField txtNumeroCarte;        // Numéro de la carte bancaire
    private JTextField txtDateExpiration;     // Date d'expiration (MM/AA)
    private JTextField txtCVV;                // Code de sécurité (CVV)
    
    /**
     * Constructeur pour les nouveaux stationnements (voirie) - ADAPTÉ
     */
    public Page_Paiement(double montant, String emailUtilisateur, String typeVehicule, 
                        String plaqueImmatriculation, String idTarification, String nomZone, 
                        int dureeHeures, int dureeMinutes) {
        // Appel du constructeur principal avec les paramètres adaptés
        this(montant, emailUtilisateur, typeVehicule, plaqueImmatriculation, idTarification, nomZone,
             dureeHeures, dureeMinutes, null, null);
    }
    
    /**
     * Constructeur principal pour tous les types de stationnements - ADAPTÉ
     */
    public Page_Paiement(double montant, String emailUtilisateur, String typeVehicule, 
                        String plaqueImmatriculation, String idTarification, String nomZone,
                        int dureeHeures, int dureeMinutes, Integer idStationnement, LocalDateTime heureDepart) {
        this.montant = montant;
        this.emailUtilisateur = emailUtilisateur;
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        this.typeVehicule = typeVehicule;
        this.plaqueImmatriculation = plaqueImmatriculation;
        this.idZone = idTarification; 
        this.nomZone = nomZone;               
        this.dureeHeures = dureeHeures;
        this.dureeMinutes = dureeMinutes;
        this.idStationnement = idStationnement;
        this.heureDepart = heureDepart;
        
        initialisePage();
    }
    
    /**
     * Initialise l'interface utilisateur de la page de paiement
     */
    private void initialisePage() {
        // Configuration de la fenêtre
        this.setTitle("Paiement du stationnement");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(550, 650);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        // Panel principal avec bordures
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // === TITRE DE LA PAGE ===
        JLabel lblTitre = new JLabel("Paiement", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // === PANEL CENTRAL AVEC FORMULAIRE ===
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new GridLayout(0, 1, 10, 10));
        
        // Ligne 1: Affichage du montant à payer
        JLabel lblMontant = new JLabel("Montant à payer: " + String.format("%.2f", montant) + " €");
        lblMontant.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(lblMontant);
        
        formPanel.add(new JLabel(" ")); // Espacement visuel
        
        // Ligne 2-3: Informations sur le stationnement
        String typeStationnement = (idStationnement == null) ? "Voirie" : "Parking";
        JLabel lblType = new JLabel("Type: " + typeStationnement);
        formPanel.add(lblType);
        
        JLabel lblVehicule = new JLabel("Véhicule: " + plaqueImmatriculation);
        formPanel.add(lblVehicule);
        
        // Ligne supplémentaire pour la zone
        JLabel lblZone = new JLabel("Zone: " + nomZone); // ADAPTATION : Utilisation de nomZone
        formPanel.add(lblZone);
        
        // Ligne pour la durée (uniquement pour voirie)
        if (idStationnement == null) {
            JLabel lblDuree = new JLabel("Durée: " + dureeHeures + "h" + dureeMinutes + "min");
            formPanel.add(lblDuree);
        }
        
        formPanel.add(new JLabel(" ")); // Espacement
        
        // === FORMULAIRE DE CARTE BANCAIRE ===
        formPanel.add(new JLabel("Informations de la carte:"));
        
        // Champ nom sur la carte
        formPanel.add(new JLabel("Nom sur la carte:"));
        txtNomCarte = new JTextField();
        txtNomCarte.setPreferredSize(new Dimension(300, 30));
        formPanel.add(txtNomCarte);
        
        // Champ numéro de carte
        formPanel.add(new JLabel("Numéro de carte:"));
        txtNumeroCarte = new JTextField();
        txtNumeroCarte.setPreferredSize(new Dimension(300, 30));
        formPanel.add(txtNumeroCarte);
        
        // Panel pour date d'expiration et CVV côte à côte
        JPanel panelDateCVV = new JPanel(new GridLayout(1, 2, 15, 10));
        panelDateCVV.setBackground(Color.WHITE);
        
        // Sous-panel date d'expiration
        JPanel panelDate = new JPanel(new BorderLayout());
        panelDate.setBackground(Color.WHITE);
        panelDate.add(new JLabel("Date expiration (MM/AA):"), BorderLayout.NORTH);
        txtDateExpiration = new JTextField();
        txtDateExpiration.setPreferredSize(new Dimension(120, 30));
        panelDate.add(txtDateExpiration, BorderLayout.CENTER);
        
        // Sous-panel CVV
        JPanel panelCVV = new JPanel(new BorderLayout());
        panelCVV.setBackground(Color.WHITE);
        panelCVV.add(new JLabel("CVV:"), BorderLayout.NORTH);
        txtCVV = new JTextField();
        txtCVV.setPreferredSize(new Dimension(80, 30));
        panelCVV.add(txtCVV, BorderLayout.CENTER);
        
        // Assemblage des sous-panels
        panelDateCVV.add(panelDate);
        panelDateCVV.add(panelCVV);
        
        formPanel.add(panelDateCVV);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // === BOUTONS D'ACTION ===
        JPanel panelBoutons = new JPanel(new FlowLayout());
        
        JButton btnAnnuler = new JButton("Annuler");
        JButton btnPayer = new JButton("Payer maintenant");
        
        // Stylisation du bouton payer
        btnPayer.setBackground(new Color(70, 130, 180));
        btnPayer.setForeground(Color.WHITE);
        btnPayer.setFocusPainted(false);
        
        // Actions des boutons
        btnAnnuler.addActionListener(e -> annuler());
        btnPayer.addActionListener(e -> traiterPaiement());
        
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnPayer);
        
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
    }
    
    /**
     * Gère l'annulation du paiement
     */
    private void annuler() {
        int confirmation = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir annuler le paiement ?",
            "Confirmation d'annulation",
            JOptionPane.YES_NO_OPTION);
            
        if (confirmation == JOptionPane.YES_OPTION) {
            this.dispose();
        }
    }
    
    /**
     * Traite le paiement après validation du formulaire - ADAPTÉ
     */
    private void traiterPaiement() {
        // Validation des champs du formulaire
        if (!validerFormulaire()) {
            return;
        }
        
        try {
            // Création de l'objet Paiement avec les données du formulaire
            Paiement paiement = new Paiement(
                txtNomCarte.getText().trim(),
                txtNumeroCarte.getText().trim(),
                txtCVV.getText().trim(),
                montant,
                usager.getIdUsager()
            );
            
            // Enregistrement du paiement en base de données
            boolean paiementReussi = PaiementDAO.enregistrerPaiement(paiement);
            
            if (paiementReussi) {
                boolean operationReussie = false;
                
                if (idStationnement == null) {
                    operationReussie = StationnementDAO.creerStationnementVoirie(
                        usager.getIdUsager(),
                        typeVehicule,
                        plaqueImmatriculation,
                        idZone,      
                        dureeHeures,
                        dureeMinutes,
                        montant,
                        paiement.getIdPaiement()
                    );
                } else {
                    operationReussie = StationnementDAO.terminerStationnementParking(
                        idStationnement,
                        heureDepart,
                        montant,
                        paiement.getIdPaiement()
                    );
                }
                
                if (operationReussie) {
                    afficherConfirmation();
                    retourAccueil();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Erreur lors de la mise à jour du stationnement",
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
            e.printStackTrace();
        }
    }
    
    /**
     * Affiche un message de confirmation adapté au type de stationnement - ADAPTÉ
     */
    private void afficherConfirmation() {
        String message;
        if (idStationnement == null) {
            // Message pour nouveau stationnement voirie
            message = "Paiement effectué avec succès !\n" +
                     "Stationnement confirmé pour " + plaqueImmatriculation + "\n" +
                     "Zone: " + nomZone + "\n" +  
                     "Durée: " + dureeHeures + "h" + dureeMinutes + "min\n" +
                     "Montant: " + String.format("%.2f", montant) + " €";
        } else {
            // Message pour stationnement parking terminé
            message = "Paiement effectué avec succès !\n" +
                     "Stationnement terminé pour " + plaqueImmatriculation + "\n" +
                     "Parking: " + nomZone + "\n" +  
                     "Montant: " + String.format("%.2f", montant) + " €\n" +
                     "Vous pouvez quitter le parking.";
        }
        
        JOptionPane.showMessageDialog(this,
            message,
            "Paiement réussi",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Retourne à la page principale après paiement réussi
     */
    private void retourAccueil() {
        Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
        pagePrincipale.setVisible(true);
        this.dispose();
    }
    
    /**
     * Valide tous les champs du formulaire de paiement
     */
    private boolean validerFormulaire() {
        // Validation du nom sur la carte
        if (txtNomCarte.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir le nom sur la carte", "Erreur", JOptionPane.ERROR_MESSAGE);
            txtNomCarte.requestFocus();
            return false;
        }
        
        // Validation du numéro de carte (16 chiffres)
        String numeroCarte = txtNumeroCarte.getText().trim().replaceAll("\\s+", "");
        if (numeroCarte.isEmpty() || numeroCarte.length() < 16) {
            JOptionPane.showMessageDialog(this, "Numéro de carte invalide (16 chiffres requis)", "Erreur", JOptionPane.ERROR_MESSAGE);
            txtNumeroCarte.requestFocus();
            return false;
        }
        
        // Validation de la date d'expiration
        if (txtDateExpiration.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir la date d'expiration", "Erreur", JOptionPane.ERROR_MESSAGE);
            txtDateExpiration.requestFocus();
            return false;
        }
        
        // Validation du CVV (3 chiffres exactement)
        String cvv = txtCVV.getText().trim();
        if (cvv.isEmpty() || cvv.length() != 3 || !cvv.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "CVV invalide (3 chiffres requis)", "Erreur", JOptionPane.ERROR_MESSAGE);
            txtCVV.requestFocus();
            return false;
        }
        
        return true;
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
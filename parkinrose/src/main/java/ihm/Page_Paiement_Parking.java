package ihm;

import javax.swing.*;
import modele.dao.TarifParkingDAO;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Page_Paiement_Parking extends JFrame {
    private String emailUtilisateur;
    private String idParking;
    private double montant;
    private LocalDateTime heureArrivee;
    private LocalDateTime heureDepart;
    private boolean tarifSoiree;
    
    public Page_Paiement_Parking(String email, String idParking, double montant,
                                LocalDateTime heureArrivee, LocalDateTime heureDepart,
                                boolean tarifSoiree) {
        this.emailUtilisateur = email;
        this.idParking = idParking;
        this.montant = montant;
        this.heureArrivee = heureArrivee;
        this.heureDepart = heureDepart;
        this.tarifSoiree = tarifSoiree;
        initialiserPage();
    }
    
    private void initialiserPage() {
        setTitle("Paiement");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Titre
        JLabel lblTitre = new JLabel("Paiement du stationnement", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // Panel récapitulatif
        JPanel panelRecap = new JPanel(new GridLayout(0, 1, 10, 10));
        panelRecap.setBackground(Color.WHITE);
        panelRecap.setBorder(BorderFactory.createTitledBorder("Récapitulatif"));
        
        // Bandeau tarif soirée si applicable
        if (tarifSoiree) {
            JPanel panelSoiree = new JPanel(new BorderLayout());
            panelSoiree.setBackground(new Color(255, 240, 245));
            panelSoiree.setBorder(BorderFactory.createLineBorder(new Color(128, 0, 128), 2));
            
            JLabel lblSoiree = new JLabel("🌙 TARIF SOIRÉE APPLIQUÉ", SwingConstants.CENTER);
            lblSoiree.setFont(new Font("Arial", Font.BOLD, 14));
            lblSoiree.setForeground(new Color(128, 0, 128));
            
            panelSoiree.add(lblSoiree, BorderLayout.CENTER);
            panelRecap.add(panelSoiree);
        }
        
        // Infos de stationnement
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        JLabel lblParking = new JLabel("Parking : " + getNomParking());
        JLabel lblArrivee = new JLabel("Arrivée : " + heureArrivee.format(formatter));
        JLabel lblDepart = new JLabel("Départ prévu : " + heureDepart.format(formatter));
        
        long dureeHeures = java.time.Duration.between(heureArrivee, heureDepart).toHours();
        long dureeMinutes = java.time.Duration.between(heureArrivee, heureDepart).toMinutes() % 60;
        JLabel lblDuree = new JLabel("Durée : " + dureeHeures + "h " + dureeMinutes + "min");
        
        // Montant avec style différent selon tarif soirée
        JLabel lblMontant = new JLabel("Montant à payer :", SwingConstants.CENTER);
        lblMontant.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel lblMontantValeur = new JLabel(String.format("%.2f €", montant), SwingConstants.CENTER);
        lblMontantValeur.setFont(new Font("Arial", Font.BOLD, 32));
        lblMontantValeur.setForeground(tarifSoiree ? new Color(128, 0, 128) : new Color(0, 150, 0));
        
        panelRecap.add(lblParking);
        panelRecap.add(lblArrivee);
        panelRecap.add(lblDepart);
        panelRecap.add(lblDuree);
        panelRecap.add(new JSeparator());
        panelRecap.add(lblMontant);
        panelRecap.add(lblMontantValeur);
        
        mainPanel.add(panelRecap, BorderLayout.CENTER);
        
        // Panel paiement (simplifié)
        JPanel panelPaiement = new JPanel(new GridLayout(0, 1, 10, 10));
        panelPaiement.setBackground(Color.WHITE);
        panelPaiement.setBorder(BorderFactory.createTitledBorder("Informations de paiement"));
        
        JLabel lblCarte = new JLabel("Numéro de carte :");
        JTextField txtCarte = new JTextField();
        
        JLabel lblExpiration = new JLabel("Date d'expiration (MM/AA) :");
        JTextField txtExpiration = new JTextField();
        
        JLabel lblCVV = new JLabel("CVV :");
        JPasswordField txtCVV = new JPasswordField();
        
        panelPaiement.add(lblCarte);
        panelPaiement.add(txtCarte);
        panelPaiement.add(lblExpiration);
        panelPaiement.add(txtExpiration);
        panelPaiement.add(lblCVV);
        panelPaiement.add(txtCVV);
        
        mainPanel.add(panelPaiement, BorderLayout.SOUTH);
        
        // Boutons
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelBoutons.setBackground(Color.WHITE);
        
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(e -> dispose());
        
        JButton btnPayer = new JButton("Payer maintenant");
        btnPayer.setBackground(new Color(0, 120, 215));
        btnPayer.setForeground(Color.WHITE);
        btnPayer.setFont(new Font("Arial", Font.BOLD, 14));
        btnPayer.addActionListener(e -> traiterPaiement());
        
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnPayer);
        
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private String getNomParking() {
        // Récupérer le nom du parking depuis la base de données
        try {
            String sql = "SELECT libelle_parking FROM Parking WHERE id_parking = ?";
            try (java.sql.Connection conn = modele.dao.MySQLConnection.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, idParking);
                java.sql.ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("libelle_parking");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idParking;
    }
    
    private void traiterPaiement() {
        // Traitement du paiement...
        // Après paiement réussi, enregistrer le stationnement
        
        String message;
        if (tarifSoiree) {
            message = String.format(
                "<html><div style='text-align: center;'>" +
                "<b>✅ PAIEMENT RÉUSSI !</b><br><br>" +
                "Votre stationnement avec tarif soirée a été enregistré.<br>" +
                "<b>Montant :</b> %.2f€<br>" +
                "<b>Arrivée :</b> %s<br>" +
                "<b>Départ prévu :</b> %s<br><br>" +
                "Merci de votre confiance !</div></html>",
                montant,
                heureArrivee.format(DateTimeFormatter.ofPattern("HH:mm")),
                heureDepart.format(DateTimeFormatter.ofPattern("HH:mm"))
            );
        } else {
            message = String.format(
                "<html><div style='text-align: center;'>" +
                "<b>✅ PAIEMENT RÉUSSI !</b><br><br>" +
                "Votre stationnement a été enregistré.<br>" +
                "<b>Montant :</b> %.2f€<br><br>" +
                "Merci de votre confiance !</div></html>",
                montant
            );
        }
        
        JOptionPane.showMessageDialog(this, message, "Paiement confirmé", 
            JOptionPane.INFORMATION_MESSAGE);
        
        dispose();
    }
}
package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import dao.StationnementDAO;
import dao.TarifParkingDAO;
import modèle.Stationnement;
import modèle.Usager;
import dao.UsagerDAO;

public class Page_Quitter_Parking extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Usager usager;
    private Stationnement stationnement;
    private JLabel lblParking, lblVehicule, lblArrivee, lblDuree, lblCout;

    public Page_Quitter_Parking(String email, Stationnement stationnement) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        this.stationnement = stationnement;
        initialisePage();
        calculerCout();
    }
    
    private void initialisePage() {
        this.setTitle("Quitter le parking");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(500, 400);
        this.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Titre
        JLabel lblTitre = new JLabel("Quitter le parking", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // Informations
        JPanel panelInfos = new JPanel();
        panelInfos.setLayout(new GridLayout(5, 2, 10, 10));
        panelInfos.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panelInfos.setBackground(Color.WHITE);
        
        panelInfos.add(new JLabel("Parking:"));
        lblParking = new JLabel(stationnement.getZone());
        lblParking.setFont(new Font("Arial", Font.BOLD, 14));
        panelInfos.add(lblParking);
        
        panelInfos.add(new JLabel("Véhicule:"));
        lblVehicule = new JLabel(stationnement.getTypeVehicule() + " - " + stationnement.getPlaqueImmatriculation());
        lblVehicule.setFont(new Font("Arial", Font.PLAIN, 14));
        panelInfos.add(lblVehicule);
        
        panelInfos.add(new JLabel("Arrivée:"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblArrivee = new JLabel(stationnement.getHeureArrivee().format(formatter));
        panelInfos.add(lblArrivee);
        
        panelInfos.add(new JLabel("Durée:"));
        lblDuree = new JLabel("En cours de calcul...");
        panelInfos.add(lblDuree);
        
        panelInfos.add(new JLabel("Coût:"));
        lblCout = new JLabel("0.00 €");
        lblCout.setFont(new Font("Arial", Font.BOLD, 16));
        lblCout.setForeground(new Color(0, 100, 0));
        panelInfos.add(lblCout);
        
        mainPanel.add(panelInfos, BorderLayout.CENTER);
        
        // Boutons
        JPanel panelBoutons = new JPanel(new FlowLayout());
        
        JButton btnAnnuler = new JButton("Annuler");
        JButton btnPayer = new JButton("Payer et quitter");
        btnPayer.setBackground(new Color(70, 130, 180));
        btnPayer.setForeground(Color.WHITE);
        btnPayer.setFocusPainted(false);
        
        btnAnnuler.addActionListener(e -> dispose());
        btnPayer.addActionListener(e -> procederPaiement());
        
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnPayer);
        
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
    }
    
    private void calculerCout() {
        LocalDateTime heureDepart = LocalDateTime.now();
        double cout = TarifParkingDAO.calculerCoutParking(
            stationnement.getHeureArrivee(), 
            heureDepart, 
            getParkingIdFromName(stationnement.getZone())
        );
        
        // Calcul de la durée
        long dureeMinutes = java.time.Duration.between(stationnement.getHeureArrivee(), heureDepart).toMinutes();
        long heures = dureeMinutes / 60;
        long minutes = dureeMinutes % 60;
        
        lblDuree.setText(String.format("%dh%02d", heures, minutes));
        lblCout.setText(String.format("%.2f €", cout));
    }
    
    private String getParkingIdFromName(String nomParking) {
        // Convertir le nom du parking en ID
        switch(nomParking) {
            case "Parking Capitole": return "PARK_CAPITOLE";
            case "Parking Carnot": return "PARK_CARNOT";
            case "Parking Esquirol": return "PARK_ESQUIROL";
            case "Parking Saint-Étienne": return "PARK_SAINT_ETIENNE";
            case "Parking Jean Jaurès": return "PARK_JEAN_JAURES";
            case "Parking Jeanne d'Arc": return "PARK_JEANNE_DARC";
            case "Parking Europe": return "PARK_EUROPE";
            case "Parking Victor Hugo": return "PARK_VICTOR_HUGO";
            case "Parking Saint-Aubin": return "PARK_SAINT_AUBIN";
            case "Parking Saint-Cyprien": return "PARK_SAINT_CYPRIEN";
            case "Parking Saint-Michel": return "PARK_SAINT_MICHEL";
            case "Parking Matabiau-Ramblas": return "PARK_MATABIAU";
            case "Parking Arnaud Bernard": return "PARK_ARNAUD_BERNARD";
            case "Parking Carmes": return "PARK_CARMES";
            default: return "PARK_CAPITOLE";
        }
    }
    
    private void procederPaiement() {
        LocalDateTime heureDepart = LocalDateTime.now();
        double cout = Double.parseDouble(lblCout.getText().replace(" €", "").replace(",", "."));
        
        // Ouvrir la page de paiement
        Page_Paiement pagePaiement = new Page_Paiement(
            cout,
            emailUtilisateur,
            stationnement.getTypeVehicule(),
            stationnement.getPlaqueImmatriculation(),
            stationnement.getZone(),
            0, // Pas de durée pour parking
            0,
            stationnement.getIdStationnement(), // Passer l'ID du stationnement
            heureDepart // Passer l'heure de départ
        );
        pagePaiement.setVisible(true);
        dispose();
    }
}
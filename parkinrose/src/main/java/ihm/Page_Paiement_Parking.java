package ihm;

import javax.swing.*;

import controleur.PaiementControleur;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import modele.Paiement;
import modele.Stationnement;
import modele.Parking;
import modele.Usager;
import modele.dao.PaiementDAO;
import modele.dao.ParkingDAO;
import modele.dao.StationnementDAO;
import modele.dao.UsagerDAO;

public class Page_Paiement_Parking extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    private static final double TARIF_SOIREE = 5.90;
    
    private double montant;
    private String emailUtilisateur;
    private Usager usager;
    private Integer idStationnement;
    private Stationnement stationnement;
    private Parking parking;
    
    private JTextField txtNomCarte;
    private JTextField txtNumeroCarte;
    private JTextField txtDateExpiration;
    private JTextField txtCVV;
    
    private JLabel lblMontant;
    private JLabel lblDuree;
    private JLabel lblHeureArrivee;
    private JLabel lblHeureDepart;
    private JLabel lblTarifHoraire;
    private JLabel lblTypeTarif;
    
    public Page_Paiement_Parking(Integer idStationnement, String emailUtilisateur) {
        this.idStationnement = idStationnement;
        this.emailUtilisateur = emailUtilisateur;
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        this.stationnement = StationnementDAO.getStationnementById(idStationnement);
        
        if (stationnement != null) {
            String parkingId = stationnement.getIdTarification();
            this.parking = ParkingDAO.getParkingById(parkingId);
        }
        
        calculerMontant();
        initialisePage();
    }
    
    private void calculerMontant() {
        if (stationnement == null || parking == null) {
            this.montant = 0.0;
            return;
        }
        
        LocalDateTime heureArrivee = stationnement.getHeureArrivee();
        LocalDateTime heureDepart = LocalDateTime.now();
        
        if (estEligibleTarifSoiree(heureArrivee, heureDepart)) {
            this.montant = TARIF_SOIREE;
            return;
        }
        
        long dureeMinutes = Duration.between(heureArrivee, heureDepart).toMinutes();
        long quartHeures = (dureeMinutes + 14) / 15;
        double tarifHoraire = getTarifHoraireNormal();
        double tarifQuartHeure = tarifHoraire / 4.0;
        
        this.montant = quartHeures * tarifQuartHeure;
        
        if (this.montant < tarifQuartHeure) {
            this.montant = tarifQuartHeure;
        }
    }
    
    private boolean estEligibleTarifSoiree(LocalDateTime arrivee, LocalDateTime depart) {
        if (parking == null || !parking.hasTarifSoiree()) {
            return false;
        }
        
        boolean arriveeValide = (arrivee.getHour() == 19 && arrivee.getMinute() >= 30) || 
                               (arrivee.getHour() >= 20 && arrivee.getHour() < 22);
        
        boolean departValide = depart.getHour() < 3 || 
                              (depart.getHour() == 3 && depart.getMinute() == 0);
        
        return arriveeValide && departValide;
    }
    
    private double getTarifHoraireNormal() {
        switch (parking.getIdParking()) {
            case "P001": case "P002": case "P003":
                return 3.50;
            case "P004": case "P005": case "P006":
                return 4.00;
            case "P007": case "P008": case "P009":
                return 3.00;
            case "P010": case "P011": case "P012":
                return 3.20;
            case "P013": case "P014":
                return 4.50;
            default:
                return 3.00;
        }
    }
    
    private void initialisePage() {
        this.setTitle("Paiement du stationnement - Parking");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(600, 750);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitre = new JLabel("Paiement Parking", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setLayout(new GridLayout(0, 1, 10, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Récapitulatif du stationnement"));
        
        lblMontant = new JLabel("Montant à payer: " + String.format("%.2f", montant) + " €");
        lblMontant.setFont(new Font("Arial", Font.BOLD, 16));
        lblMontant.setForeground(new Color(0, 100, 0));
        infoPanel.add(lblMontant);
        
        infoPanel.add(new JLabel(" "));
        
        if (stationnement != null) {
            infoPanel.add(new JLabel("Véhicule: " + stationnement.getTypeVehicule() + " - " + stationnement.getPlaqueImmatriculation()));
            infoPanel.add(new JLabel("Parking: " + parking.getLibelleParking()));
        }
        
        if (stationnement != null) {
            LocalDateTime heureArrivee = stationnement.getHeureArrivee();
            LocalDateTime heureDepart = LocalDateTime.now();
            
            lblHeureArrivee = new JLabel("Heure d'arrivée: " + heureArrivee.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            lblHeureDepart = new JLabel("Heure de départ: " + heureDepart.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
            long dureeMinutes = Duration.between(heureArrivee, heureDepart).toMinutes();
            long heures = dureeMinutes / 60;
            long minutes = dureeMinutes % 60;
            lblDuree = new JLabel("Durée totale: " + heures + "h" + minutes + "min");
            
            infoPanel.add(lblHeureArrivee);
            infoPanel.add(lblHeureDepart);
            infoPanel.add(lblDuree);
        }
        
        if (parking != null) {
            boolean tarifSoiree = estEligibleTarifSoiree(stationnement.getHeureArrivee(), LocalDateTime.now());
            
            if (tarifSoiree) {
                lblTypeTarif = new JLabel("Tarif appliqué: Forfait Soirée");
                lblTarifHoraire = new JLabel("Montant forfait: " + String.format("%.2f", TARIF_SOIREE) + " €");
            } else {
                lblTypeTarif = new JLabel("Tarif appliqué: Quart d'heure");
                double tarifQuartHeure = getTarifHoraireNormal() / 4.0;
                lblTarifHoraire = new JLabel("Tarif quart d'heure: " + String.format("%.2f", tarifQuartHeure) + " €");
            }
            
            infoPanel.add(lblTypeTarif);
            infoPanel.add(lblTarifHoraire);
            
            if (parking.hasTarifSoiree()) {
                JLabel lblInfoSoiree = new JLabel("Ce parking propose le forfait soirée à " + TARIF_SOIREE + " €");
                lblInfoSoiree.setForeground(new Color(0, 100, 0));
                lblInfoSoiree.setFont(new Font("Arial", Font.ITALIC, 12));
                infoPanel.add(lblInfoSoiree);
            }
        }
        
        centerPanel.add(infoPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new GridLayout(0, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Informations de paiement"));
        
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
        
        centerPanel.add(formPanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        JPanel panelBoutons = new JPanel(new FlowLayout());
        
        JButton btnAnnuler = new JButton("Annuler");
        JButton btnPayer = new JButton("Payer " + String.format("%.2f", montant) + " €");
        
        btnPayer.setBackground(new Color(70, 130, 180));
        btnPayer.setForeground(Color.WHITE);
        btnPayer.setFocusPainted(false);
        btnPayer.setFont(new Font("Arial", Font.BOLD, 14));
        
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
        if (!validerFormulaire()) {
            return;
        }
        
        try {
            // Créer le paiement
            Paiement paiement = new Paiement(
                txtNomCarte.getText().trim(),
                txtNumeroCarte.getText().trim(),
                txtCVV.getText().trim(),
                montant,
                usager.getIdUsager()
            );
            
            // Simuler le paiement via le contrôleur
            PaiementControleur controleur = new PaiementControleur(emailUtilisateur);
            boolean paiementSimuleReussi = controleur.simulerPaiement(
                montant,
                txtNumeroCarte.getText().trim(),
                txtDateExpiration.getText().trim(),
                txtCVV.getText().trim()
            );
            
            if (!paiementSimuleReussi) {
                JOptionPane.showMessageDialog(this,
                    "Le paiement a été refusé par la banque",
                    "Paiement refusé",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Enregistrer le paiement dans la base
            boolean paiementEnregistre = PaiementDAO.enregistrerPaiement(paiement);
            
            if (paiementEnregistre) {
                // Mettre à jour le stationnement
                boolean operationReussie = StationnementDAO.terminerStationnementParking(
                    idStationnement,
                    LocalDateTime.now(),
                    montant,
                    paiement.getIdPaiement()
                );
                
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
    
    private void afficherConfirmation() {
        String message = "Paiement effectué avec succès !\n\n" +
                       "Stationnement terminé pour " + stationnement.getPlaqueImmatriculation() + "\n" +
                       "Parking: " + parking.getLibelleParking() + "\n" +
                       "Durée: " + lblDuree.getText().replace("Durée totale: ", "") + "\n" +
                       "Montant: " + String.format("%.2f", montant) + " €\n\n" +
                       "Vous pouvez quitter le parking.";
        
        JOptionPane.showMessageDialog(this,
            message,
            "Paiement réussi",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void retourAccueil() {
        Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
        pagePrincipale.setVisible(true);
        this.dispose();
    }
   

}
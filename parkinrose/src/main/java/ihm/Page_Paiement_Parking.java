package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import dao.PaiementDAO;
import dao.StationnementDAO;
import dao.ParkingDAO;
import modele.Paiement;
import modele.Stationnement;
import modele.Parking;
import modele.Usager;
import dao.UsagerDAO;

public class Page_Paiement_Parking extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // Liste des IDs des parkings avec tarif soirée
    private static final List<String> PARKINGS_TARIF_SOIREE_IDS = Arrays.asList(
        "P001", "P002", "P003", "P004", "P005", "P006", "P007", 
        "P008", "P009", "P010", "P011", "P012", "P013", "P014"
    );
    
    private static final double TARIF_SOIREE = 5.90; // Forfait soirée
    
    private double montant;                   // Montant à payer calculé
    private String emailUtilisateur;          // Email de l'utilisateur connecté
    private Usager usager;                    // Objet utilisateur avec ses informations
    private Integer idStationnement;          // ID du stationnement existant
    private Stationnement stationnement;      // Objet stationnement complet
    private Parking parking;                  // Objet parking
    
    // === CHAMPS DU FORMULAIRE DE PAIEMENT ===
    private JTextField txtNomCarte;           // Nom porté sur la carte
    private JTextField txtNumeroCarte;        // Numéro de la carte bancaire
    private JTextField txtDateExpiration;     // Date d'expiration (MM/AA)
    private JTextField txtCVV;                // Code de sécurité (CVV)
    
    // === LABELS POUR L'AFFICHAGE DES INFORMATIONS ===
    private JLabel lblMontant;
    private JLabel lblDuree;
    private JLabel lblHeureArrivee;
    private JLabel lblHeureDepart;
    private JLabel lblTarifHoraire;
    private JLabel lblTypeTarif;
    
    /**
     * Constructeur pour le paiement des parkings
     */
    public Page_Paiement_Parking(Integer idStationnement, String emailUtilisateur) {
        this.idStationnement = idStationnement;
        this.emailUtilisateur = emailUtilisateur;
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        this.stationnement = StationnementDAO.getStationnementById(idStationnement);
        
        if (stationnement != null) {
            // Récupérer le parking par son ID depuis le stationnement
            this.parking = ParkingDAO.getParkingById(parking.getIdParking());
        }
        
        calculerMontant();
        initialisePage();
    }
    
    /**
     * Calcule le montant en fonction de la durée réelle avec tarification au quart d'heure
     */
    private void calculerMontant() {
        if (stationnement == null || parking == null) {
            this.montant = 0.0;
            return;
        }
        
        LocalDateTime heureArrivee = stationnement.getHeureArrivee();
        LocalDateTime heureDepart = LocalDateTime.now();
        
        // Vérifier si le forfait soirée s'applique
        if (estEligibleTarifSoiree(heureArrivee, heureDepart)) {
            this.montant = TARIF_SOIREE;
            return;
        }
        
        // Calcul de la durée en minutes
        long dureeMinutes = Duration.between(heureArrivee, heureDepart).toMinutes();
        
        // Tarification au quart d'heure
        long quartHeures = (dureeMinutes + 14) / 15; // Arrondi au quart d'heure supérieur
        
        // Application du tarif horaire divisé par 4 (puisqu'il y a 4 quarts d'heure dans une heure)
        double tarifHoraire = getTarifHoraireNormal();
        double tarifQuartHeure = tarifHoraire / 4.0;
        
        this.montant = quartHeures * tarifQuartHeure;
        
        // Minimum de 15 minutes
        if (this.montant < tarifQuartHeure) {
            this.montant = tarifQuartHeure;
        }
    }
    
    /**
     * Vérifie si le stationnement est éligible au tarif soirée
     */
    private boolean estEligibleTarifSoiree(LocalDateTime arrivee, LocalDateTime depart) {
        // Vérifier si le parking propose le tarif soirée via son ID
        if (!PARKINGS_TARIF_SOIREE_IDS.contains(parking.getIdParking())) {
            return false;
        }
        
        // Vérifier si l'arrivée est entre 19h30 et 22h
        boolean arriveeValide = (arrivee.getHour() == 19 && arrivee.getMinute() >= 30) || 
                               (arrivee.getHour() >= 20 && arrivee.getHour() < 22);
        
        // Vérifier si le départ est avant 3h le lendemain
        boolean departValide;
        if (depart.getHour() < 3) {
            // Départ le lendemain avant 3h
            departValide = true;
        } else if (depart.getHour() == 3 && depart.getMinute() == 0) {
            // Départ exactement à 3h00
            departValide = true;
        } else {
            // Départ après 3h
            departValide = false;
        }
        
        return arriveeValide && departValide;
    }
    
    /**
     * Retourne le tarif horaire normal du parking
     */
    private double getTarifHoraireNormal() {
        // Implémentation de la logique tarifaire basée sur l'ID du parking
        // Pour l'exemple, on utilise des tarifs fixes par ID
        switch (parking.getIdParking()) {
            case "P001": // Arnaud Bernard
            case "P002": // Carmes
            case "P003": // Esquirol
                return 3.50;
            case "P004": // Jean Jaurès
            case "P005": // Jeanne d'Arc
            case "P006": // Europe
                return 4.00;
            case "P007": // Saint Aubin
            case "P008": // Victor Hugo
            case "P009": // Saint Cyprien
                return 3.00;
            case "P010": // Saint Etienne
            case "P011": // Saint Michel
            case "P012": // Carnot
                return 3.20;
            case "P013": // Capitole
            case "P014": // Matabiau-Ramblas
                return 4.50;
            default:
                return 3.00;
        }
    }
    
    /**
     * Initialise l'interface utilisateur de la page de paiement
     */
    private void initialisePage() {
        // Configuration de la fenêtre
        this.setTitle("Paiement du stationnement - Parking");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(600, 750);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        // Panel principal avec bordures
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // === TITRE DE LA PAGE ===
        JLabel lblTitre = new JLabel("Paiement Parking", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // === PANEL CENTRAL AVEC INFORMATIONS ET FORMULAIRE ===
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        // === SECTION INFORMATIONS STATIONNEMENT ===
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setLayout(new GridLayout(0, 1, 10, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Récapitulatif du stationnement"));
        
        // Montant à payer
        lblMontant = new JLabel("Montant à payer: " + String.format("%.2f", montant) + " €");
        lblMontant.setFont(new Font("Arial", Font.BOLD, 16));
        lblMontant.setForeground(new Color(0, 100, 0));
        infoPanel.add(lblMontant);
        
        infoPanel.add(new JLabel(" ")); // Espacement
        
        // Informations véhicule
        if (stationnement != null) {
            infoPanel.add(new JLabel("Véhicule: " + stationnement.getTypeVehicule() + " - " + stationnement.getPlaqueImmatriculation()));
            infoPanel.add(new JLabel("Parking: " + parking.getLibelleParking()));
        }
        
        // Informations durée
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
        
        // Informations tarifaires
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
            
            // Afficher une info si le parking propose le tarif soirée
            if (PARKINGS_TARIF_SOIREE_IDS.contains(parking.getIdParking())) {
                JLabel lblInfoSoiree = new JLabel("✓ Ce parking propose le forfait soirée à " + TARIF_SOIREE + " €");
                lblInfoSoiree.setForeground(new Color(0, 100, 0));
                lblInfoSoiree.setFont(new Font("Arial", Font.ITALIC, 12));
                infoPanel.add(lblInfoSoiree);
            }
        }
        
        centerPanel.add(infoPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // === FORMULAIRE DE CARTE BANCAIRE ===
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new GridLayout(0, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Informations de paiement"));
        
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
        
        centerPanel.add(formPanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // === BOUTONS D'ACTION ===
        JPanel panelBoutons = new JPanel(new FlowLayout());
        
        JButton btnAnnuler = new JButton("Annuler");
        JButton btnPayer = new JButton("Payer " + String.format("%.2f", montant) + " €");
        
        // Stylisation du bouton payer
        btnPayer.setBackground(new Color(70, 130, 180));
        btnPayer.setForeground(Color.WHITE);
        btnPayer.setFocusPainted(false);
        btnPayer.setFont(new Font("Arial", Font.BOLD, 14));
        
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
     * Traite le paiement après validation du formulaire
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
                // Mettre à jour le stationnement avec le paiement
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
    
    /**
     * Affiche un message de confirmation
     */
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
                    // Pour test
                    new Page_Paiement_Parking(1, "test@example.com").setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
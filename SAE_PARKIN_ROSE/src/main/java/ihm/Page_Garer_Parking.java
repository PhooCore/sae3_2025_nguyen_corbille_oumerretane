package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import dao.StationnementDAO;
import dao.ParkingDAO;
import dao.UsagerDAO;
import modèle.Stationnement;
import modèle.Parking;
import modèle.Usager;
import java.util.List;

public class Page_Garer_Parking extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Déclaration des composants de l'interface
    private JPanel contentPanel;
    private JLabel lblNom, lblPrenom, lblEmail, lblPlaque;
    private JComboBox<String> comboParking;
    private JLabel lblPlacesDispo, lblTarifHoraire, lblHeureArrivee;
    private JRadioButton radioVoiture, radioMoto, radioCamion;
    private ButtonGroup groupeTypeVehicule;
    private List<Parking> listeParkings;
    private String emailUtilisateur;
    private Usager usager;

    /**
     * Constructeur de la page de stationnement en parking
     * @param email l'email de l'utilisateur connecté
     */
    public Page_Garer_Parking(String email) {
    	this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        initialisePage();
        initialiseDonnees();
        initializeEventListeners();
    }
    
    /**
     * Initialise l'interface utilisateur
     */
    private void initialisePage() {
        this.setTitle("Stationnement en Parking");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        this.setContentPane(contentPanel);
        
        // Titre de la page
        JLabel lblTitre = new JLabel("Stationnement en Parking Intérieur", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        contentPanel.add(lblTitre, BorderLayout.NORTH);
        
        // Panel principal avec layout vertical
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        
        // Section : Informations personnelles (lecture seule)
        JPanel panelInfos = new JPanel();
        panelInfos.setLayout(new GridLayout(3, 2, 10, 10));
        panelInfos.setBorder(BorderFactory.createTitledBorder("Vos informations"));
        
        panelInfos.add(new JLabel("Nom:"));
        lblNom = new JLabel(usager != null ? usager.getNomUsager() : "Non connecté");
        lblNom.setFont(new Font("Arial", Font.BOLD, 14));
        panelInfos.add(lblNom);
        
        panelInfos.add(new JLabel("Prénom:"));
        lblPrenom = new JLabel(usager != null ? usager.getPrenomUsager() : "Non connecté");
        lblPrenom.setFont(new Font("Arial", Font.BOLD, 14));
        panelInfos.add(lblPrenom);
        
        panelInfos.add(new JLabel("Email:"));
        lblEmail = new JLabel(usager != null ? usager.getMailUsager() : "Non connecté");
        lblEmail.setFont(new Font("Arial", Font.BOLD, 14));
        panelInfos.add(lblEmail);
        
        panelPrincipal.add(panelInfos);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Section : Véhicule
        JPanel panelVehicule = new JPanel();
        panelVehicule.setLayout(new BorderLayout());
        panelVehicule.setBorder(BorderFactory.createTitledBorder("Véhicule"));
        
        // Boutons radio pour le type de véhicule
        JPanel panelType = new JPanel(new FlowLayout(FlowLayout.LEFT));
        groupeTypeVehicule = new ButtonGroup();
        
        radioVoiture = new JRadioButton("Voiture", true);
        radioMoto = new JRadioButton("Moto");
        radioCamion = new JRadioButton("Camion");
        
        groupeTypeVehicule.add(radioVoiture);
        groupeTypeVehicule.add(radioMoto);
        groupeTypeVehicule.add(radioCamion);
        
        panelType.add(radioVoiture);
        panelType.add(radioMoto);
        panelType.add(radioCamion);
        
        panelVehicule.add(panelType, BorderLayout.NORTH);
        
        // Section plaque d'immatriculation
        JPanel panelPlaque = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelPlaque.add(new JLabel("Plaque d'immatriculation:"));
        lblPlaque = new JLabel();
        lblPlaque.setFont(new Font("Arial", Font.PLAIN, 14));
        panelPlaque.add(lblPlaque);
        
        // Bouton pour modifier la plaque
        JButton btnModifierPlaque = new JButton("Modifier");
        btnModifierPlaque.addActionListener(e -> modifierPlaque());
        panelPlaque.add(btnModifierPlaque);
        
        panelVehicule.add(panelPlaque, BorderLayout.SOUTH);
        
        panelPrincipal.add(panelVehicule);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Section : Parking (sélection, informations)
        JPanel panelParking = new JPanel();
        panelParking.setLayout(new GridLayout(4, 2, 10, 10));
        panelParking.setBorder(BorderFactory.createTitledBorder("Parking"));
        
        panelParking.add(new JLabel("Parking:"));
        comboParking = new JComboBox<>();
        panelParking.add(comboParking);
        
        panelParking.add(new JLabel("Places disponibles:"));
        lblPlacesDispo = new JLabel("-");
        lblPlacesDispo.setFont(new Font("Arial", Font.BOLD, 14));
        panelParking.add(lblPlacesDispo);
        
        panelParking.add(new JLabel("Tarif horaire:"));
        lblTarifHoraire = new JLabel("-");
        lblTarifHoraire.setFont(new Font("Arial", Font.BOLD, 14));
        panelParking.add(lblTarifHoraire);
        
        panelParking.add(new JLabel("Heure d'arrivée:"));
        lblHeureArrivee = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lblHeureArrivee.setFont(new Font("Arial", Font.BOLD, 14));
        panelParking.add(lblHeureArrivee);
        
        panelPrincipal.add(panelParking);
        
        contentPanel.add(panelPrincipal, BorderLayout.CENTER);
        
        // Boutons de navigation
        JPanel panelBoutons = new JPanel(new FlowLayout());
        
        JButton btnAnnuler = new JButton("Annuler");
        JButton btnReserver = new JButton("Réserver");
        
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnReserver);
        
        contentPanel.add(panelBoutons, BorderLayout.SOUTH);
    }
    
    /**
     * Ouvre une boîte de dialogue pour modifier la plaque d'immatriculation
     */
    private void modifierPlaque() {
        String nouvellePlaque = JOptionPane.showInputDialog(this, 
            "Entrez la plaque d'immatriculation:", 
            lblPlaque.getText());
        
        if (nouvellePlaque != null && !nouvellePlaque.trim().isEmpty()) {
            lblPlaque.setText(nouvellePlaque.trim());
        }
    }

    /**
     * Charge les données depuis la base de données
     */
    private void initialiseDonnees() {
        // Charger les parkings depuis la base de données
        listeParkings = ParkingDAO.getAllParkings();
        
        // Peupler la liste déroulante des parkings
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (Parking parking : listeParkings) {
            model.addElement(parking.getLibelleParking() + " - " + parking.getAdresseParking());
        }
        comboParking.setModel(model);
        
        // Initialiser la plaque si vide
        if (lblPlaque.getText().isEmpty()) {
            lblPlaque.setText("Non définie");
        }
        
        // Mettre à jour les informations du premier parking
        if (!listeParkings.isEmpty()) {
            mettreAJourInfosParking(0);
        }
    }
    
    /**
     * Initialise les écouteurs d'événements
     */
    private void initializeEventListeners() {
        // Mettre à jour les infos quand le parking change
        comboParking.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    mettreAJourInfosParking(comboParking.getSelectedIndex());
                }
            }
        });
        
        // Bouton Annuler - retour à la page principale
        JButton btnAnnuler = (JButton) ((JPanel) contentPanel.getComponent(2)).getComponent(0);
        btnAnnuler.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
                pagePrincipale.setVisible(true);
                dispose();
            }
        });
        
        // Bouton Réserver - validation du formulaire
        JButton btnReserver = (JButton) ((JPanel) contentPanel.getComponent(2)).getComponent(1);
        btnReserver.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (validerFormulaire()) {
                    reserverPlace();
                }
            }
        });
    }
    
    /**
     * Met à jour les informations affichées pour le parking sélectionné
     * @param index l'index du parking dans la liste
     */
    private void mettreAJourInfosParking(int index) {
        if (index >= 0 && index < listeParkings.size()) {
            Parking parking = listeParkings.get(index);
            lblPlacesDispo.setText(String.valueOf(parking.getPlacesDisponibles()));
        }
    }
    
    /**
     * Valide le formulaire avant réservation
     * @return true si le formulaire est valide, false sinon
     */
    private boolean validerFormulaire() {
        // Vérifier que la plaque est définie
        if (lblPlaque.getText().equals("Non définie") || lblPlaque.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Veuillez définir une plaque d'immatriculation",
                "Plaque manquante",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Vérifier si l'utilisateur a déjà un stationnement actif
        Stationnement stationnementActif = StationnementDAO.getStationnementActifByUsager(usager.getIdUsager());
        if (stationnementActif != null) {
            String message = "Vous avez déjà un stationnement " + stationnementActif.getTypeStationnement() + " actif !\n\n" +
                            "Véhicule: " + stationnementActif.getTypeVehicule() + " - " + stationnementActif.getPlaqueImmatriculation() + "\n";
            
            if (stationnementActif.estVoirie()) {
                message += "Zone: " + stationnementActif.getZone() + "\n" +
                          "Début: " + stationnementActif.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            } else if (stationnementActif.estParking()) {
                message += "Parking: " + stationnementActif.getZone() + "\n" +
                          "Arrivée: " + stationnementActif.getHeureArrivee().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
            
            message += "\n\nVeuillez terminer ce stationnement avant d'en créer un nouveau.";
            
            JOptionPane.showMessageDialog(this, message, "Stationnement actif", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Vérifier qu'il reste des places disponibles
        int index = comboParking.getSelectedIndex();
        if (index >= 0 && index < listeParkings.size()) {
            Parking parking = listeParkings.get(index);
            if (parking.getPlacesDisponibles() <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Aucune place disponible dans ce parking",
                    "Parking complet",
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Effectue la réservation de la place de parking
     */
    private void reserverPlace() {
        int index = comboParking.getSelectedIndex();
        if (index >= 0 && index < listeParkings.size()) {
            Parking parking = listeParkings.get(index);
            
            // Récapitulatif de la réservation
            String message = "Réservation confirmée :\n\n" +
                "Nom: " + usager.getNomUsager() + "\n" +
                "Prénom: " + usager.getPrenomUsager() + "\n" +
                "Email: " + usager.getMailUsager() + "\n" +
                "Véhicule: " + getTypeVehicule() + " - " + lblPlaque.getText() + "\n" +
                "Parking: " + parking.getLibelleParking() + "\n" +
                "Adresse: " + parking.getAdresseParking() + "\n" +
                "Heure d'arrivée: " + lblHeureArrivee.getText() + "\n\n" +
                "Le paiement sera effectué à la sortie en fonction de la durée réelle.";
            
            int choix = JOptionPane.showConfirmDialog(this,
                message + "\n\nConfirmer la réservation ?",
                "Confirmation de réservation",
                JOptionPane.YES_NO_OPTION);
            
            if (choix == JOptionPane.YES_OPTION) {
                // Créer le stationnement en base de données
                Stationnement stationnement = new Stationnement(
                    usager.getIdUsager(),
                    getTypeVehicule(),
                    lblPlaque.getText(),
                    "PARKING", 
                    0, 
                    0, 
                    0.0, 
                    null 
                );
                
                boolean succes = StationnementDAO.creerStationnementParking(stationnement);
                
                if (succes) {                    
                    JOptionPane.showMessageDialog(this,
                        "Réservation confirmée !\n\n" +
                        "Votre place est réservée dans le parking " + parking.getLibelleParking() + ".\n" +
                        "N'oubliez pas de valider votre sortie pour le paiement.",
                        "Réservation réussie",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Retour à la page principale
                    Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
                    pagePrincipale.setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Erreur lors de la réservation. Veuillez réessayer.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    /**
     * Retourne le type de véhicule sélectionné
     * @return le type de véhicule
     */
    private String getTypeVehicule() {
        if (radioVoiture.isSelected()) return "Voiture";
        if (radioMoto.isSelected()) return "Moto";
        return "Camion";
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Pour tester la page indépendamment
                    new Page_Garer_Parking("pho@email.com").setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
package ihm;

import javax.swing.*;
import java.awt.*;
import controleur.ControleurGarerParking;

public class Page_Garer_Parking extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Variables accessibles au contrôleur
    public String emailUtilisateur;
    public java.util.List<modele.Parking> listeParkings;
    public JComboBox<String> comboParking;
    public JLabel lblPlaque;
    public JRadioButton radioVoiture, radioMoto, radioCamion;
    
    // Composants UI
    private JLabel lblPlacesDispo;
    private JButton btnAnnuler;
    private JButton btnReserver;
    private JButton btnModifierPlaque;

    public Page_Garer_Parking(String email, modele.Parking parkingPreSelectionne) {
        this.emailUtilisateur = email;
        initialisePage();
        
        // Créer et lier le contrôleur
        new ControleurGarerParking(this);
        
        // Initialiser les données
        initialiserDonnees(parkingPreSelectionne);
    }
    
    private void initialisePage() {
        this.setTitle("Stationnement en Parking");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        this.setContentPane(contentPanel);
        
        JLabel lblTitre = new JLabel("Stationnement en Parking Intérieur", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        contentPanel.add(lblTitre, BorderLayout.NORTH);
        
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        
        // Informations utilisateur
        JPanel panelInfos = new JPanel();
        panelInfos.setLayout(new GridLayout(3, 2, 10, 10));
        panelInfos.setBorder(BorderFactory.createTitledBorder("Vos informations"));
        
        modele.Usager usager = modele.dao.UsagerDAO.getUsagerByEmail(emailUtilisateur);
        
        panelInfos.add(new JLabel("Nom:"));
        JLabel lblNom = new JLabel(usager != null ? usager.getNomUsager() : "Non connecté");
        lblNom.setFont(new Font("Arial", Font.BOLD, 14));
        panelInfos.add(lblNom);
        
        panelInfos.add(new JLabel("Prénom:"));
        JLabel lblPrenom = new JLabel(usager != null ? usager.getPrenomUsager() : "Non connecté");
        lblPrenom.setFont(new Font("Arial", Font.BOLD, 14));
        panelInfos.add(lblPrenom);
        
        panelInfos.add(new JLabel("Email:"));
        JLabel lblEmail = new JLabel(usager != null ? usager.getMailUsager() : "Non connecté");
        lblEmail.setFont(new Font("Arial", Font.BOLD, 14));
        panelInfos.add(lblEmail);
        
        panelPrincipal.add(panelInfos);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Véhicule
        JPanel panelVehicule = new JPanel();
        panelVehicule.setLayout(new BorderLayout());
        panelVehicule.setBorder(BorderFactory.createTitledBorder("Véhicule"));
        
        JPanel panelType = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup groupeTypeVehicule = new ButtonGroup();
        
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
        
        JPanel panelPlaque = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelPlaque.add(new JLabel("Plaque d'immatriculation:"));
        lblPlaque = new JLabel("Non définie");
        lblPlaque.setFont(new Font("Arial", Font.PLAIN, 14));
        panelPlaque.add(lblPlaque);
        
        btnModifierPlaque = new JButton("Modifier");
        panelPlaque.add(btnModifierPlaque);
        
        panelVehicule.add(panelPlaque, BorderLayout.SOUTH);
        
        panelPrincipal.add(panelVehicule);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Parking
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
        JLabel lblTarifHoraire = new JLabel("-");
        lblTarifHoraire.setFont(new Font("Arial", Font.BOLD, 14));
        panelParking.add(lblTarifHoraire);
        
        panelParking.add(new JLabel("Heure d'arrivée:"));
        JLabel lblHeureArrivee = new JLabel(
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        lblHeureArrivee.setFont(new Font("Arial", Font.BOLD, 14));
        panelParking.add(lblHeureArrivee);
        
        panelPrincipal.add(panelParking);
        contentPanel.add(panelPrincipal, BorderLayout.CENTER);
        
        // Boutons
        JPanel panelBoutons = new JPanel(new FlowLayout());
        panelBoutons.setBackground(Color.WHITE);
        
        btnAnnuler = new JButton("Annuler");
        btnReserver = new JButton("Réserver");
        
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnReserver);
        
        contentPanel.add(panelBoutons, BorderLayout.SOUTH);
    }
    
    private void initialiserDonnees(modele.Parking parkingPreSelectionne) {
        listeParkings = modele.dao.ParkingDAO.getAllParkings();
        
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        int indexSelectionne = -1;
        
        for (int i = 0; i < listeParkings.size(); i++) {
            modele.Parking parking = listeParkings.get(i);
            String texte = parking.getLibelleParking() + " - " + parking.getAdresseParking();
            model.addElement(texte);
            
            if (parkingPreSelectionne != null && 
                parkingPreSelectionne.getIdParking().equals(parking.getIdParking())) {
                indexSelectionne = i;
            }
        }
        
        comboParking.setModel(model);
        
        if (indexSelectionne != -1) {
            comboParking.setSelectedIndex(indexSelectionne);
            mettreAJourInfosParking(indexSelectionne);
        } else if (!listeParkings.isEmpty()) {
            mettreAJourInfosParking(0);
        }
    }
    
    public void mettreAJourInfosParking(int index) {
        if (index >= 0 && index < listeParkings.size()) {
            modele.Parking parking = listeParkings.get(index);
            lblPlacesDispo.setText(parking.getPlacesDisponibles() + " / " + parking.getNombrePlaces());
            
            if (parking.getPlacesDisponibles() <= 5) {
                lblPlacesDispo.setForeground(Color.RED);
            } else if (parking.getPlacesDisponibles() <= 10) {
                lblPlacesDispo.setForeground(Color.ORANGE);
            } else {
                lblPlacesDispo.setForeground(Color.BLACK);
            }
        }
    }
    
    public String getTypeVehicule() {
        if (radioVoiture.isSelected()) return "Voiture";
        if (radioMoto.isSelected()) return "Moto";
        return "Camion";
    }
    
    // Getters pour le contrôleur
    public JButton getBtnAnnuler() { return btnAnnuler; }
    public JButton getBtnReserver() { return btnReserver; }
    public JButton getBtnModifierPlaque() { return btnModifierPlaque; }
    public JLabel getLblPlacesDispo() { return lblPlacesDispo; }
    
    // Méthode pour identifier les actions
    public static String getActionBouton(JButton b) {
        String texte = b.getText();
        if (texte != null) {
            if (texte.contains("Annuler")) {
                return "ANNULER";
            } else if (texte.contains("Réserver") || texte.contains("Stationner")) {
                return "RESERVER";
            } else if (texte.contains("Modifier")) {
                return "MODIFIER_PLAQUE";
            }
        }
        return "INCONNU";
    }
}
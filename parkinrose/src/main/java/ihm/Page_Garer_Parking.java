package ihm;

import javax.swing.*;
import controleur.StationnementControleur;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import modele.Parking;
import modele.Usager;
import modele.dao.ParkingDAO;
import modele.dao.UsagerDAO;
import java.util.List;

public class Page_Garer_Parking extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private JPanel contentPanel;
    private JLabel lblNom, lblPrenom, lblEmail, lblPlaque;
    private JComboBox<String> comboParking;
    private JLabel lblPlacesDispo, lblTarifHoraire, lblHeureArrivee;
    private JRadioButton radioVoiture, radioMoto, radioCamion;
    private ButtonGroup groupeTypeVehicule;
    private List<Parking> listeParkings;
    private String emailUtilisateur;
    private Usager usager;
    private StationnementControleur controleur;
    private Parking parkingPreSelectionne;

    public Page_Garer_Parking(String email,Parking parkingPreSelectionne) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        this.controleur = new StationnementControleur(email);
        this.parkingPreSelectionne = parkingPreSelectionne;
        initialisePage();
        initialiseDonnees();
        initializeEventListeners();
    }
    
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
        
        JLabel lblTitre = new JLabel("Stationnement en Parking Intérieur", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        contentPanel.add(lblTitre, BorderLayout.NORTH);
        
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        
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
        
        JPanel panelVehicule = new JPanel();
        panelVehicule.setLayout(new BorderLayout());
        panelVehicule.setBorder(BorderFactory.createTitledBorder("Véhicule"));
        
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
        
        JPanel panelPlaque = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelPlaque.add(new JLabel("Plaque d'immatriculation:"));
        lblPlaque = new JLabel();
        lblPlaque.setFont(new Font("Arial", Font.PLAIN, 14));
        panelPlaque.add(lblPlaque);
        
        JButton btnModifierPlaque = new JButton("Modifier");
        btnModifierPlaque.addActionListener(e -> modifierPlaque());
        panelPlaque.add(btnModifierPlaque);
        
        panelVehicule.add(panelPlaque, BorderLayout.SOUTH);
        
        panelPrincipal.add(panelVehicule);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));
        
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
        
        JPanel panelBoutons = new JPanel(new FlowLayout());
        
        JButton btnAnnuler = new JButton("Annuler");
        JButton btnReserver = new JButton("Réserver");
        
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnReserver);
        
        contentPanel.add(panelBoutons, BorderLayout.SOUTH);
    }
    
    private void modifierPlaque() {
        String nouvellePlaque = JOptionPane.showInputDialog(this, 
            "Entrez la plaque d'immatriculation:", 
            lblPlaque.getText());
        
        if (nouvellePlaque != null && !nouvellePlaque.trim().isEmpty()) {
            String plaqueNettoyee = nouvellePlaque.trim().toUpperCase();
            
            if (controleur.validerPlaque(plaqueNettoyee)) {
                lblPlaque.setText(plaqueNettoyee);
            }
        }
    }

    private void initialiseDonnees() {
        listeParkings = ParkingDAO.getAllParkings();
        
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        int indexSelectionne = -1;
        
        for (int i = 0; i < listeParkings.size(); i++) {
            Parking parking = listeParkings.get(i);
            String texte = parking.getLibelleParking() + " - " + parking.getAdresseParking();
            model.addElement(texte);
            
            // Si ce parking est celui pré-sélectionné, mémoriser l'index
            if (parkingPreSelectionne != null && 
                parkingPreSelectionne.getIdParking().equals(parking.getIdParking())) {
                indexSelectionne = i;
            }
        }
        
        comboParking.setModel(model);
        
        // Sélectionner le parking pré-sélectionné si spécifié
        if (indexSelectionne != -1) {
            comboParking.setSelectedIndex(indexSelectionne);
            mettreAJourInfosParking(indexSelectionne);
        } else if (!listeParkings.isEmpty()) {
            mettreAJourInfosParking(0);
        }
        
        if (lblPlaque.getText() == null || lblPlaque.getText().isEmpty()) {
            lblPlaque.setText("Non définie");
        }
    }
    
    
    private void initializeEventListeners() {
        comboParking.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    mettreAJourInfosParking(comboParking.getSelectedIndex());
                }
            }
        });
        
        JButton btnAnnuler = (JButton) ((JPanel) contentPanel.getComponent(2)).getComponent(0);
        btnAnnuler.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
                pagePrincipale.setVisible(true);
                dispose();
            }
        });
        
        JButton btnReserver = (JButton) ((JPanel) contentPanel.getComponent(2)).getComponent(1);
        btnReserver.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reserverPlace();
            }
        });
    }
    
    private void mettreAJourInfosParking(int index) {
        if (index >= 0 && index < listeParkings.size()) {
            Parking parking = listeParkings.get(index);
            lblPlacesDispo.setText(parking.getPlacesDisponibles() + " / " + parking.getNombrePlaces());
            lblTarifHoraire.setText("2.50 €/h");
            
            if (parking.getPlacesDisponibles() <= 5) {
                lblPlacesDispo.setForeground(Color.RED);
            } else if (parking.getPlacesDisponibles() <= 10) {
                lblPlacesDispo.setForeground(Color.ORANGE);
            } else {
                lblPlacesDispo.setForeground(Color.BLACK);
            }
        }
    }
    
    private void reserverPlace() {
        int index = comboParking.getSelectedIndex();
        if (index >= 0 && index < listeParkings.size()) {
            Parking parking = listeParkings.get(index);
            String typeVehicule = getTypeVehicule();
            
            //Vérif moto
            if ("Moto".equals(typeVehicule)) {
                if (!parking.hasMoto()) {
                    JOptionPane.showMessageDialog(this,
                        "Ce parking ne dispose pas de places pour les motos",
                        "Parking non adapté",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (parking.getPlacesMotoDisponibles() <= 0) {
                    JOptionPane.showMessageDialog(this,
                        "Plus de places moto disponibles dans ce parking",
                        "Parking complet",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            
            boolean succes = controleur.preparerStationnementParking(
                typeVehicule,
                lblPlaque.getText(),
                parking.getIdParking(),
                this
            );
        }
    }
    private String getTypeVehicule() {
        if (radioVoiture.isSelected()) return "Voiture";
        if (radioMoto.isSelected()) return "Moto";
        return "Camion";
    }


    
}
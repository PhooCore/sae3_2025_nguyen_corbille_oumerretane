package ihm;

import javax.swing.*;
import controleur.ControleurGarerVoirie; // Ajouter cette importation
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import modele.Zone;
import modele.dao.UsagerDAO;
import modele.dao.ZoneDAO;
import modele.Usager;
import java.util.List;

public class Page_Garer_Voirie extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Variables rendues publiques pour le contrôleur
    public JPanel contentPanel;
    public JLabel lblNom, lblPrenom, lblEmail, lblPlaque;
    public JComboBox<String> comboZone, comboHeures, comboMinutes;
    public JLabel lblCout;
    public JRadioButton radioVoiture, radioMoto, radioCamion;
    public ButtonGroup groupeTypeVehicule;
    public List<Zone> zones; // Rendre public
    public String emailUtilisateur;
    public Usager usager;
    
    // Boutons rendus publics pour le contrôleur
    public JButton btnAnnuler;
    public JButton btnValider;
    public JButton btnModifierPlaque;

    public Page_Garer_Voirie(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        initialisePage();
        initialiserDonnees();
        
        // Créer et lier le contrôleur
        new ControleurGarerVoirie(this);
    }
    
    private void initialisePage() {
        this.setTitle("Stationnement en Voirie");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        this.setContentPane(contentPanel);
        
        JLabel lblTitre = new JLabel("Préparer un Stationnement en voirie", SwingConstants.CENTER);
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
        
        btnModifierPlaque = new JButton("Modifier");
        panelPlaque.add(btnModifierPlaque);
        
        panelVehicule.add(panelPlaque, BorderLayout.SOUTH);
        
        panelPrincipal.add(panelVehicule);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JPanel panelStationnement = new JPanel();
        panelStationnement.setLayout(new GridLayout(3, 2, 10, 10));
        panelStationnement.setBorder(BorderFactory.createTitledBorder("Stationnement"));
        
        panelStationnement.add(new JLabel("Zone:"));
        comboZone = new JComboBox<>();
        panelStationnement.add(comboZone);
        
        panelStationnement.add(new JLabel("Durée:"));
        JPanel panelDuree = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] heures = {"0", "1", "2", "3", "4", "5", "6", "7", "8"};
        String[] minutes = {"0", "15", "30", "45"};
        
        comboHeures = new JComboBox<>(heures);
        comboMinutes = new JComboBox<>(minutes);
        
        panelDuree.add(comboHeures);
        panelDuree.add(new JLabel("h"));
        panelDuree.add(comboMinutes);
        panelDuree.add(new JLabel("min"));
        panelStationnement.add(panelDuree);
        
        panelStationnement.add(new JLabel("Coût:"));
        lblCout = new JLabel("0.00 €");
        lblCout.setFont(new Font("Arial", Font.BOLD, 14));
        panelStationnement.add(lblCout);
        
        panelPrincipal.add(panelStationnement);
        
        contentPanel.add(panelPrincipal, BorderLayout.CENTER);
        
        JPanel panelBoutons = new JPanel(new FlowLayout());
        
        btnAnnuler = new JButton("Annuler");
        btnValider = new JButton("Valider");
        
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnValider);
        
        contentPanel.add(panelBoutons, BorderLayout.SOUTH);
    }
    
    private void initialiserDonnees() {
        this.zones = ZoneDAO.getAllZones();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        
        for (Zone zone : zones) {
            model.addElement(zone.getAffichage());
        }
        comboZone.setModel(model);
        
        if (lblPlaque.getText() == null || lblPlaque.getText().isEmpty()) {
            lblPlaque.setText("Non définie");
        }
    }
    
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Pour tester, utilisez un email de test
                    String emailTest = "test@example.com";
                    Page_Garer_Voirie frame = new Page_Garer_Voirie(emailTest);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
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
import dao.UsagerDAO;
import dao.ZoneDAO;
import modèle.Zone;
import modèle.Usager;
import modèle.Stationnement;
import java.util.List;

public class Page_Garer_Voirie extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private JPanel contentPanel;
    private JLabel lblNom, lblPrenom, lblEmail, lblPlaque;
    private JComboBox<String> comboZone, comboHeures, comboMinutes;
    private JLabel lblCout;
    private JRadioButton radioVoiture, radioMoto, radioCamion;
    private ButtonGroup groupeTypeVehicule;
    private List<Zone> zones;
    private String emailUtilisateur;
    private Usager usager;

    public Page_Garer_Voirie(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        initialisePage();
        initialiseDonnees();
        initializeEventListeners();
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
        
        JButton btnModifierPlaque = new JButton("Modifier");
        btnModifierPlaque.addActionListener(e -> modifierPlaque());
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
        
        JButton btnAnnuler = new JButton("Annuler");
        JButton btnValider = new JButton("Valider");
        
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnValider);
        
        contentPanel.add(panelBoutons, BorderLayout.SOUTH);
    }
    
    private void modifierPlaque() {
        String nouvellePlaque = JOptionPane.showInputDialog(this, 
            "Entrez la plaque d'immatriculation:", 
            lblPlaque.getText());
        
        if (nouvellePlaque != null && !nouvellePlaque.trim().isEmpty()) {
            lblPlaque.setText(nouvellePlaque.trim());
        }
    }

    private void initialiseDonnees() {
        // CORRECTION : Utiliser StationnementTarifDAO au lieu de TarifDAO
        this.zones = ZoneDAO.getAllZones();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (Zone zone : zones) {
            model.addElement(zone.getAffichage());
        }
        comboZone.setModel(model);
        
        if (lblPlaque.getText() == null || lblPlaque.getText().isEmpty()) {
            lblPlaque.setText("Non définie");
        }
        
        calculerCout();
    }
    private void initializeEventListeners() {
        ItemListener calculateurCout = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                calculerCout();
            }
        };
        
        comboZone.addItemListener(calculateurCout);
        comboHeures.addItemListener(calculateurCout);
        comboMinutes.addItemListener(calculateurCout);
        
        JButton btnAnnuler = (JButton) ((JPanel) contentPanel.getComponent(2)).getComponent(0);
        btnAnnuler.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
                pagePrincipale.setVisible(true);
                dispose();
            }
        });
        
        JButton btnValider = (JButton) ((JPanel) contentPanel.getComponent(2)).getComponent(1);
        btnValider.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (validerFormulaire()) {
                    afficherConfirmation();
                }
            }
        });
    }
    
    private void calculerCout() {
        try {
            int heures = Integer.parseInt(comboHeures.getSelectedItem().toString());
            int minutes = Integer.parseInt(comboMinutes.getSelectedItem().toString());
            int dureeTotaleMinutes = (heures * 60) + minutes;
            
            int index = comboZone.getSelectedIndex();
            if (index >= 0 && index < zones.size()) {
                Zone zone = zones.get(index);
                
                // SIMPLIFICATION : Appel direct sans paramètres de date
                double cout = zone.calculerCout(dureeTotaleMinutes);
                lblCout.setText(String.format("%.2f €", cout));
            }
            
        } catch (Exception e) {
            lblCout.setText("0.00 €");
        }
    }
    
    private boolean validerFormulaire() {
        if (lblPlaque.getText().equals("Non définie") || lblPlaque.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Veuillez définir une plaque d'immatriculation",
                "Plaque manquante",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
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
        
        if (!validerDureeMaximale()) {
            return false;
        }
        
        return true;
    }
    
    private boolean validerDureeMaximale() {
        int heures = Integer.parseInt(comboHeures.getSelectedItem().toString());
        int minutes = Integer.parseInt(comboMinutes.getSelectedItem().toString());
        int dureeTotaleMinutes = (heures * 60) + minutes;
        
        int index = comboZone.getSelectedIndex();
        if (index >= 0 && index < zones.size()) {
            Zone zone = zones.get(index);
            if (dureeTotaleMinutes > zone.getDureeMaxMinutes()) {
                JOptionPane.showMessageDialog(this,
                    "Durée maximale dépassée pour " + zone.getLibelleZone() +
                    " (max: " + formatDuree(zone.getDureeMaxMinutes()) + ")",
                    "Erreur",
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        
        return true;
    }
    
    private String formatDuree(int minutes) {
        int heures = minutes / 60;
        int mins = minutes % 60;
        if (mins == 0) {
            return heures + "h";
        } else {
            return heures + "h" + mins + "min";
        }
    }
    
    private void afficherConfirmation() {
        int index = comboZone.getSelectedIndex();
        String idZone = "";
        String nomZone = "";
        Zone zoneSelectionnee = null;  // Déclarer la variable ici
        
        if (index >= 0 && index < zones.size()) {
            zoneSelectionnee = zones.get(index);  // Utiliser un nom différent
            idZone = zoneSelectionnee.getIdZone();
            nomZone = zoneSelectionnee.getLibelleZone();
        }
        
        String message = "Stationnement confirmé :\n\n" +
            "Nom: " + usager.getNomUsager() + "\n" +
            "Prénom: " + usager.getPrenomUsager() + "\n" +
            "Email: " + usager.getMailUsager() + "\n" +
            "Véhicule: " + getTypeVehicule() + " - " + lblPlaque.getText() + "\n" +
            "Zone: " + nomZone + "\n" +
            "Durée: " + comboHeures.getSelectedItem() + "h" + comboMinutes.getSelectedItem() + "min\n" +
            "Coût: " + lblCout.getText();
        
        int choix = JOptionPane.showConfirmDialog(this,
            message + "\n\nVoulez-vous procéder au paiement ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (choix == JOptionPane.YES_OPTION) {
            String coutText = lblCout.getText().replace(" €", "").replace(",", ".");
            double montant = Double.parseDouble(coutText);
            
            Page_Paiement pagePaiement = new Page_Paiement(
                montant,
                emailUtilisateur,
                getTypeVehicule(),
                lblPlaque.getText(),
                idZone,  // Utiliser idZone qui est déjà définie
                nomZone,  // Utiliser nomZone qui est déjà définie
                Integer.parseInt(comboHeures.getSelectedItem().toString()),
                Integer.parseInt(comboMinutes.getSelectedItem().toString())
            );
            pagePaiement.setVisible(true);
            dispose();
        } else {
            Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
            pagePrincipale.setVisible(true);
            dispose();
        }
    }
    
    private String getTypeVehicule() {
        if (radioVoiture.isSelected()) return "Voiture";
        if (radioMoto.isSelected()) return "Moto";
        return "Camion";
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
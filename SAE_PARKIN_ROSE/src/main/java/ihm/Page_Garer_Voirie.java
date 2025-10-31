package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import dao.StationnementDAO;
import dao.TarifDAO;
import dao.UsagerDAO;
import modèle.Stationnement;
import modèle.Tarif;
import modèle.Usager;
import java.util.List;

public class Page_Garer_Voirie extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPanel;
    private JLabel lblNom, lblPrenom, lblEmail, lblPlaque;
    private JComboBox<String> comboZone, comboHeures, comboMinutes;
    private JLabel lblCout;
    private JRadioButton radioVoiture, radioMoto, radioCamion;
    private ButtonGroup groupeTypeVehicule;
    private List<Tarif> listeTarifs;
    private String emailUtilisateur;
    private Usager usager;

    public Page_Garer_Voirie(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        initializeUI();
        initializeData();
        initializeEventListeners();
    }
    
    private void initializeUI() {
        this.setTitle("Stationnement en Voirie");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        this.setContentPane(contentPanel);
        
        // Titre
        JLabel lblTitre = new JLabel("Préparer un Stationnement en voirie", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        contentPanel.add(lblTitre, BorderLayout.NORTH);
        
        // Panel principal
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        
        // Informations personnelles (affichage seulement)
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
        
        // Véhicule
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
        
        // Bouton pour modifier la plaque
        JButton btnModifierPlaque = new JButton("Modifier");
        btnModifierPlaque.addActionListener(e -> modifierPlaque());
        panelPlaque.add(btnModifierPlaque);
        
        panelVehicule.add(panelPlaque, BorderLayout.SOUTH);
        
        panelPrincipal.add(panelVehicule);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Zone et durée
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
        
        // Boutons
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

    private void initializeData() {
        // Charger les tarifs depuis la base de données
        listeTarifs = TarifDAO.TouslesTarifs();
        
        // ComboBox avec les tarifs
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (Tarif tarif : listeTarifs) {
            model.addElement(tarif.getAffichage());
        }
        comboZone.setModel(model);
        
        // Initialiser la plaque si vide
        if (lblPlaque.getText().isEmpty()) {
            lblPlaque.setText("Non définie");
        }
    }
    
    private void initializeEventListeners() {
        // Calcul du coût
        ItemListener calculateurCout = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                calculerCout();
            }
        };
        
        comboZone.addItemListener(calculateurCout);
        comboHeures.addItemListener(calculateurCout);
        comboMinutes.addItemListener(calculateurCout);
        
        // Bouton Annuler
        JButton btnAnnuler = (JButton) ((JPanel) contentPanel.getComponent(2)).getComponent(0);
        btnAnnuler.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Page_Test pageTest = new Page_Test(emailUtilisateur, 
                    usager.getNomUsager(), 
                    usager.getPrenomUsager());
                pageTest.setVisible(true);
                dispose();
            }
        });
        
        // Bouton Valider
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
            if (index >= 0 && index < listeTarifs.size()) {
                Tarif tarif = listeTarifs.get(index);
                double cout = tarif.calculerCout(dureeTotaleMinutes);
                lblCout.setText(String.format("%.2f €", cout));
            }
            
        } catch (Exception e) {
            lblCout.setText("0.00 €");
        }
    }
    
    private boolean validerFormulaire() {
        // Vérifier que la plaque est définie
        if (lblPlaque.getText().equals("Non définie") || lblPlaque.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Veuillez définir une plaque d'immatriculation",
                "Plaque manquante",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // VÉRIFICATION IMPORTANTE : Stationnement actif existant
        if (StationnementDAO.vehiculeAStationnementActif(lblPlaque.getText().trim())) {
            Stationnement stationnementActif = StationnementDAO.getStationnementActif(lblPlaque.getText().trim());
            
            String message = "Vous avez déjà un stationnement actif !\n\n" +
                            "Plaque: " + stationnementActif.getPlaqueImmatriculation() + "\n" +
                            "Zone: " + stationnementActif.getZone() + "\n" +
                            "Début: " + stationnementActif.getDateCreation().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
                            "Statut: " + stationnementActif.getStatut();
            
            JOptionPane.showMessageDialog(this,
                message,
                "Stationnement actif existant",
                JOptionPane.WARNING_MESSAGE);
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
        if (index >= 0 && index < listeTarifs.size()) {
            Tarif tarif = listeTarifs.get(index);
            if (dureeTotaleMinutes > tarif.getDureeMaxMinutes()) {
                JOptionPane.showMessageDialog(this,
                    "Durée maximale dépassée pour " + tarif.getNomZone() + 
                    " (max: " + formatDuree(tarif.getDureeMaxMinutes()) + ")",
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
        String nomZone = "";
        if (index >= 0 && index < listeTarifs.size()) {
            nomZone = listeTarifs.get(index).getNomZone();
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
            

         // Au lieu de Page_Paiement, utilisez Page_Paiement_Complet
            Page_Paiement pagePaiement = new Page_Paiement(
                montant,
                emailUtilisateur,
                getTypeVehicule(),
                lblPlaque.getText(),
                nomZone,
                Integer.parseInt(comboHeures.getSelectedItem().toString()),
                Integer.parseInt(comboMinutes.getSelectedItem().toString())
            );
            pagePaiement.setVisible(true);
            dispose();

            dispose();
        } else {
            Page_Test pageTest = new Page_Test(emailUtilisateur, 
                usager.getNomUsager(), 
                usager.getPrenomUsager());
            pageTest.setVisible(true);
            dispose();
        }
    }
    
    private String getTypeVehicule() {
        if (radioVoiture.isSelected()) return "Voiture";
        if (radioMoto.isSelected()) return "Moto";
        return "Camion";
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Page_Garer_Voirie("pho@gmail.com").setVisible(true);
        });
    }
}
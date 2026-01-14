package ihm;

import javax.swing.*;
import java.awt.*;
import controleur.ControleurGarerVoirie;
import modele.Zone;
import modele.dao.ZoneDAO;
import modele.dao.AbonnementDAO;
import modele.dao.UsagerDAO;
import modele.Abonnement;
import modele.Usager;
import java.util.List;

public class Page_Garer_Voirie extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Composants UI (privés)
    private String emailUtilisateur;
    private JComboBox<String> comboZone;
    private JComboBox<String> comboHeures;
    private JComboBox<String> comboMinutes;
    private JLabel lblPlaque;
    private JLabel lblCout;
    private JRadioButton radioVoiture;
    private JRadioButton radioMoto;
    private JRadioButton radioCamion;
    private JButton btnAnnuler;
    private JButton btnValider;
    private JButton btnModifierPlaque;
    
    // Labels pour les informations utilisateur
    private JLabel lblNom;
    private JLabel lblPrenom;
    private JLabel lblEmail;
    
    // Données
    private List<Zone> zones;
    
    public Page_Garer_Voirie(String email) {
        this.emailUtilisateur = email;
        initialiseUI();
        
        // Créer le contrôleur
        new ControleurGarerVoirie(this);
        
        setVisible(true);
    }
    
    private void initialiseUI() {
        setTitle("Stationnement en Voirie");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(contentPanel);
        
        // Titre
        JLabel lblTitre = new JLabel("Stationnement en Voirie", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitre.setForeground(new Color(0, 51, 102));
        contentPanel.add(lblTitre, BorderLayout.NORTH);
        
        // Panneau principal
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // 1. Informations utilisateur
        panelPrincipal.add(creerPanelInfosUtilisateur());
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // 2. Véhicule
        panelPrincipal.add(creerPanelVehicule());
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // 3. Stationnement
        panelPrincipal.add(creerPanelStationnement());
        
        contentPanel.add(new JScrollPane(panelPrincipal), BorderLayout.CENTER);
        
        // 4. Boutons
        contentPanel.add(creerPanelBoutons(), BorderLayout.SOUTH);
    }
    
    private JPanel creerPanelInfosUtilisateur() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 204), 2),
            "Vos informations"));
        
        panel.add(new JLabel("Nom:"));
        lblNom = new JLabel("Chargement...");
        lblNom.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblNom);
        
        panel.add(new JLabel("Prénom:"));
        lblPrenom = new JLabel("Chargement...");
        lblPrenom.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblPrenom);
        
        panel.add(new JLabel("Email:"));
        lblEmail = new JLabel(emailUtilisateur);
        lblEmail.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblEmail);
        
        return panel;
    }
    
    private JPanel creerPanelVehicule() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 153, 76), 2),
            "Véhicule"));
        
        // Type de véhicule
        JPanel panelType = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup groupeType = new ButtonGroup();
        
        radioVoiture = new JRadioButton("Voiture", true);
        radioMoto = new JRadioButton("Moto");
        radioCamion = new JRadioButton("Camion");
        
        // Style des boutons radio
        Font radioFont = new Font("Arial", Font.PLAIN, 13);
        radioVoiture.setFont(radioFont);
        radioMoto.setFont(radioFont);
        radioCamion.setFont(radioFont);
        
        groupeType.add(radioVoiture);
        groupeType.add(radioMoto);
        groupeType.add(radioCamion);
        
        panelType.add(radioVoiture);
        panelType.add(radioMoto);
        panelType.add(radioCamion);
        
        panel.add(panelType, BorderLayout.NORTH);
        
        // Plaque
        JPanel panelPlaque = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelPlaque.add(new JLabel("Plaque d'immatriculation:"));
        
        lblPlaque = new JLabel("Chargement...");
        lblPlaque.setFont(new Font("Arial", Font.BOLD, 14));
        lblPlaque.setForeground(Color.BLUE);
        lblPlaque.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        lblPlaque.setOpaque(true);
        lblPlaque.setBackground(new Color(240, 240, 240));
        lblPlaque.setPreferredSize(new Dimension(150, 25));
        lblPlaque.setHorizontalAlignment(SwingConstants.CENTER);
        panelPlaque.add(lblPlaque);
        
        btnModifierPlaque = new JButton("Modifier");
        btnModifierPlaque.setFont(new Font("Arial", Font.PLAIN, 12));
        btnModifierPlaque.setPreferredSize(new Dimension(80, 25));
        panelPlaque.add(btnModifierPlaque);
        
        panel.add(panelPlaque, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel creerPanelStationnement() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10)); // Changé de 4 à 5 lignes
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(153, 76, 0), 2),
            "Stationnement"));
        
        // Zone
        panel.add(new JLabel("Zone:"));
        comboZone = new JComboBox<>();
        comboZone.setFont(new Font("Arial", Font.PLAIN, 12));
        comboZone.setPreferredSize(new Dimension(300, 25));
        panel.add(comboZone);
        
        // Durée
        panel.add(new JLabel("Durée:"));
        JPanel panelDuree = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        String[] heures = {"0", "1", "2", "3", "4", "5", "6", "7", "8"};
        String[] minutes = {"0", "15", "30", "45"};
        
        comboHeures = new JComboBox<>(heures);
        comboHeures.setFont(new Font("Arial", Font.PLAIN, 12));
        comboHeures.setPreferredSize(new Dimension(60, 25));
        
        comboMinutes = new JComboBox<>(minutes);
        comboMinutes.setFont(new Font("Arial", Font.PLAIN, 12));
        comboMinutes.setPreferredSize(new Dimension(60, 25));
        
        panelDuree.add(comboHeures);
        panelDuree.add(new JLabel("h"));
        panelDuree.add(comboMinutes);
        panelDuree.add(new JLabel("min"));
        
        panel.add(panelDuree);
        
        // Abonnement (nouvelle ligne)
        panel.add(new JLabel("Abonnement:"));
        JLabel lblAbonnement = new JLabel("Chargement...");
        lblAbonnement.setFont(new Font("Arial", Font.ITALIC, 12));
        lblAbonnement.setForeground(Color.GRAY);
        
        // Vérifier l'abonnement et mettre à jour le label
        SwingUtilities.invokeLater(() -> {
            try {
                Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
                if (usager != null) {
                    Abonnement abonnement = AbonnementDAO.getAbonnementActifStatic(usager.getIdUsager());
                    if (abonnement != null && abonnement.estActif()) {
                        lblAbonnement.setText("✓ " + abonnement.getLibelleAbonnement());
                        lblAbonnement.setForeground(new Color(0, 150, 0)); // Vert
                    } else {
                        lblAbonnement.setText("Aucun abonnement actif");
                        lblAbonnement.setForeground(Color.GRAY);
                    }
                }
            } catch (Exception e) {
                lblAbonnement.setText("Erreur chargement");
                lblAbonnement.setForeground(Color.RED);
            }
        });
        
        panel.add(lblAbonnement);
        
        // Coût
        panel.add(new JLabel("Coût estimé:"));
        lblCout = new JLabel("0.00 €");
        lblCout.setFont(new Font("Arial", Font.BOLD, 16));
        lblCout.setForeground(new Color(0, 100, 0));
        panel.add(lblCout);
        
        // Informations sur les zones
        JLabel lblInfoZones = new JLabel("Note:");
        lblInfoZones.setFont(new Font("Arial", Font.ITALIC, 11));
        panel.add(lblInfoZones);
        
        JLabel lblDetailsZones = new JLabel("Les zones bleues sont gratuites");
        lblDetailsZones.setFont(new Font("Arial", Font.ITALIC, 11));
        lblDetailsZones.setForeground(Color.GRAY);
        panel.add(lblDetailsZones);
        
        return panel;
    }
    
    private JPanel creerPanelBoutons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        
        btnAnnuler = new JButton("Annuler");
        btnAnnuler.setFont(new Font("Arial", Font.BOLD, 14));
        btnAnnuler.setPreferredSize(new Dimension(120, 35));
        btnAnnuler.setBackground(new Color(220, 220, 220));
        
        btnValider = new JButton("Valider");
        btnValider.setFont(new Font("Arial", Font.BOLD, 14));
        btnValider.setPreferredSize(new Dimension(120, 35));
        btnValider.setBackground(new Color(0, 153, 0));
        btnValider.setForeground(Color.WHITE);
        
        panel.add(btnAnnuler);
        panel.add(btnValider);
        
        return panel;
    }
    
    // ============================================
    // Getters pour le contrôleur
    // ============================================
    
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }
    
    public JComboBox<String> getComboZone() {
        return comboZone;
    }
    
    public JComboBox<String> getComboHeures() {
        return comboHeures;
    }
    
    public JComboBox<String> getComboMinutes() {
        return comboMinutes;
    }
    
    public JButton getBtnAnnuler() {
        return btnAnnuler;
    }
    
    public JButton getBtnValider() {
        return btnValider;
    }
    
    public JButton getBtnModifierPlaque() {
        return btnModifierPlaque;
    }
    
    public String getTypeVehicule() {
        if (radioVoiture.isSelected()) return "Voiture";
        if (radioMoto.isSelected()) return "Moto";
        if (radioCamion.isSelected()) return "Camion";
        return null;
    }
    
    public String getPlaque() {
        return lblPlaque.getText();
    }
    
    // ============================================
    // Setters pour le contrôleur
    // ============================================
    
    public void setNomUsager(String nom) {
        lblNom.setText(nom);
    }
    
    public void setPrenomUsager(String prenom) {
        lblPrenom.setText(prenom);
    }
    
    public void setEmailUsager(String email) {
        lblEmail.setText(email);
    }
    
    public void setPlaque(String plaque) {
        lblPlaque.setText(plaque);
    }
    
    public void setTypeVehicule(String type) {
        if ("Voiture".equals(type)) {
            radioVoiture.setSelected(true);
        } else if ("Moto".equals(type)) {
            radioMoto.setSelected(true);
        } else if ("Camion".equals(type)) {
            radioCamion.setSelected(true);
        }
    }
    
    public void setCout(String cout) {
        if ("GRATUIT".equalsIgnoreCase(cout) || "0.00 €".equals(cout)) {
            lblCout.setText("GRATUIT");
            lblCout.setForeground(new Color(0, 150, 0)); // Vert
        } else {
            lblCout.setText(cout);
            lblCout.setForeground(Color.BLACK);
        }
    }
    
    public void setCoutAvecCouleur(String cout, Color couleur) {
        lblCout.setText(cout);
        lblCout.setForeground(couleur);
    }
    
    // ============================================
    // Méthodes utilitaires
    // ============================================
    
    public void chargerZones() {
        try {
            this.zones = ZoneDAO.getAllZones();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            
            for (Zone zone : zones) {
                String texte = zone.getAffichage();
                
                // Ajouter des indicateurs visuels
                if (zone.getLibelleZone().toLowerCase().contains("bleu")) {
                    texte += " G";
                } else if (zone.getLibelleZone().toLowerCase().contains("orange")) {
                    texte += " O";
                } else if (zone.getLibelleZone().toLowerCase().contains("vert")) {
                    texte += " V";
                }
                
                model.addElement(texte);
            }
            
            comboZone.setModel(model);
            
        } catch (Exception e) {
            afficherMessageErreur("Erreur chargement zones", 
                "Impossible de charger les zones: " + e.getMessage());
        }
    }
    
    public Zone getZoneSelectionnee() {
        int index = comboZone.getSelectedIndex();
        if (index >= 0 && zones != null && index < zones.size()) {
            return zones.get(index);
        }
        return null;
    }
    
    public List<Zone> getZones() {
        return zones;
    }
    
    public void afficherMessageErreur(String titre, String message) {
        JOptionPane.showMessageDialog(this, message, titre, JOptionPane.ERROR_MESSAGE);
    }
    
    public void afficherMessageInformation(String titre, String message) {
        JOptionPane.showMessageDialog(this, message, titre, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public int demanderConfirmation(String titre, String message) {
        return JOptionPane.showConfirmDialog(this, message, titre, 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Pour tester, utilisez un email de test
                String emailTest = "test@example.com";
                Page_Garer_Voirie frame = new Page_Garer_Voirie(emailTest);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
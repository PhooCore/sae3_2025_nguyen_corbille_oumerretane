package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import controleur.ControleurGarerParking;
import modele.Parking;

import java.util.ArrayList;
import java.util.List;
public class Page_Garer_Parking extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Composants UI (privés)
    private String emailUtilisateur;
    private JComboBox<String> comboParking;
    private JLabel lblPlaque;
    private JLabel lblPlacesDispo;
    private JLabel lblTarifHoraire;
    private JLabel lblTarifSoiree;
    private JLabel lblHeureArrivee;
    private JRadioButton radioVoiture;
    private JRadioButton radioMoto;
    private JRadioButton radioCamion;
    private JButton btnAnnuler;
    private JButton btnReserver;
    private JButton btnModifierPlaque;
    
    // Labels pour les informations utilisateur
    private JLabel lblNom;
    private JLabel lblPrenom;
    private JLabel lblEmail;
    private JLabel lblPlacesMoto;
    private JLabel lblTypeParking; // Nouveau label
    
    public Page_Garer_Parking(String email, Parking parkingPreSelectionne) {
        this.emailUtilisateur = email;
        initialiseUI();
        
        // Créer le contrôleur
        new ControleurGarerParking(this, parkingPreSelectionne);
        
        setVisible(true);
    }
    
    private void initialiseUI() {
        setTitle("Stationnement en Parking");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(contentPanel);
        
        // Titre
        JLabel lblTitre = new JLabel("Stationnement en Parking Intérieur", SwingConstants.CENTER);
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
        
        // 3. Parking
        panelPrincipal.add(creerPanelParking());
        
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
    
    private JPanel creerPanelParking() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(153, 76, 0), 2),
            "Parking"));
        
        // Parking
        panel.add(new JLabel("Parking:"));
        comboParking = new JComboBox<>();
        comboParking.setFont(new Font("Arial", Font.PLAIN, 12));
        comboParking.setPreferredSize(new Dimension(300, 25));
        panel.add(comboParking);
        
        // Places disponibles (voiture/camion)
        panel.add(new JLabel("Places disponibles (voiture/camion):"));
        lblPlacesDispo = new JLabel("-");
        lblPlacesDispo.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblPlacesDispo);
        
        // Places moto
        panel.add(new JLabel("Places moto disponibles:"));
        lblPlacesMoto = new JLabel("-");
        lblPlacesMoto.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblPlacesMoto);
        
        // Tarif horaire
        panel.add(new JLabel("Tarif horaire:"));
        lblTarifHoraire = new JLabel("- €/h");
        lblTarifHoraire.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblTarifHoraire);
        
        // Tarif soirée
        panel.add(new JLabel("Tarif soirée (19h30-3h):"));
        lblTarifSoiree = new JLabel("-");
        lblTarifSoiree.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblTarifSoiree);
        
        // Heure d'arrivée
        panel.add(new JLabel("Heure d'arrivée:"));
        lblHeureArrivee = new JLabel(
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        lblHeureArrivee.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblHeureArrivee);
        
        // Type de parking
        panel.add(new JLabel("Type de parking:"));
        lblTypeParking = new JLabel("-");
        lblTypeParking.setFont(new Font("Arial", Font.ITALIC, 12));
        panel.add(lblTypeParking);
        
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
       
        btnReserver = new JButton("Réserver");
        btnReserver.setFont(new Font("Arial", Font.BOLD, 14));
        btnReserver.setPreferredSize(new Dimension(120, 35));
        btnReserver.setBackground(new Color(0, 153, 0));
        btnReserver.setForeground(Color.WHITE);
        
        panel.add(btnAnnuler);
        panel.add(btnReserver);
        
        return panel;
    }
    
    // ============================================
    // Getters pour le contrôleur
    // ============================================
    
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }

    public JComboBox<String> getComboParking() {
        return comboParking;
    }

    public JButton getBtnAnnuler() {
        return btnAnnuler;
    }

    public JButton getBtnReserver() {
        return btnReserver;
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
    
    public void setPlacesDisponibles(int disponibles, int total) {
        if (disponibles <= 0) {
            lblPlacesDispo.setText("COMPLET");
            lblPlacesDispo.setForeground(Color.RED);
            lblPlacesDispo.setFont(new Font("Arial", Font.BOLD, 14));
        } else {
            lblPlacesDispo.setText(disponibles + " / " + total);
            lblPlacesDispo.setFont(new Font("Arial", Font.BOLD, 14));
            
            if (disponibles <= 5) {
                lblPlacesDispo.setForeground(Color.RED);
            } else if (disponibles <= 10) {
                lblPlacesDispo.setForeground(Color.ORANGE);
            } else {
                lblPlacesDispo.setForeground(new Color(0, 153, 0)); // Vert
            }
        }
    }
    
    public void setPlacesMotoDisponibles(int disponibles, int total) {
        if (total > 0) {
            if (disponibles <= 0) {
                lblPlacesMoto.setText("COMPLET");
                lblPlacesMoto.setForeground(Color.RED);
                lblPlacesMoto.setFont(new Font("Arial", Font.BOLD, 14));
            } else {
                lblPlacesMoto.setText(disponibles + " / " + total);
                lblPlacesMoto.setFont(new Font("Arial", Font.BOLD, 14));
                
                if (disponibles <= 2) {
                    lblPlacesMoto.setForeground(Color.RED);
                } else if (disponibles <= 5) {
                    lblPlacesMoto.setForeground(Color.ORANGE);
                } else {
                    lblPlacesMoto.setForeground(new Color(0, 153, 0)); // Vert
                }
            }
        } else {
            lblPlacesMoto.setText("Non disponible");
            lblPlacesMoto.setForeground(Color.GRAY);
        }
    }
    
    public void setTarifHoraire(String texte) {
        lblTarifHoraire.setText(texte);
    }
    
    public void setTarifSoiree(String texte) {
        lblTarifSoiree.setText(texte);
    }
    
    public void setTarifSoireeCouleur(Color couleur) {
        lblTarifSoiree.setForeground(couleur);
    }
    
    public void setTypeParking(String type) {
        lblTypeParking.setText(type);
        if ("Gratuit".equals(type)) {
            lblTypeParking.setForeground(new Color(0, 128, 0)); // Vert
        } else if ("Relais Tisséo".equals(type)) {
            lblTypeParking.setForeground(new Color(0, 102, 204)); // Bleu
        } else {
            lblTypeParking.setForeground(Color.BLACK);
        }
    }
    
    public void setTexteBoutonReserver(String texte, Color couleurFond) {
        btnReserver.setText(texte);
        btnReserver.setBackground(couleurFond);
    }

    public void setTexteBoutonReserver(String texte) {
        btnReserver.setText(texte);
    }

    public String getTexteBoutonReserver() {
        return btnReserver.getText();
    }
    // ============================================
    // Méthodes utilitaires
    // ============================================
    
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

    /**
     * Affiche une fenêtre simple pour choisir un parking alternatif
     */
    public int afficherParkingsAlternatifs(List<Parking> parkingsAlternatifs, Parking parkingComplet, String typeVehicule) {
        // Créer une fenêtre modale
        JDialog dialog = new JDialog(this, "Alternatives disponibles", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        // Variable pour stocker le choix
        final int[] choix = {-1};
        
        // === EN-TÊTE ===
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panelHeader.setBackground(new Color(30, 70, 130));
        
        JLabel lblTitre = new JLabel(
            "<html><div style='color: white;'>"
            + "<h3 style='margin: 0 0 5px 0;'>Parking complet</h3>"
            + "<p style='margin: 0; font-size: 13px;'>"
            + parkingComplet.getLibelleParking()
            + "</p></div></html>"
        );
        lblTitre.setFont(new Font("Arial", Font.PLAIN, 14));
        panelHeader.add(lblTitre, BorderLayout.WEST);
        
        // Icône
        JLabel lblIcone = new JLabel("🔄");
        lblIcone.setFont(new Font("Arial", Font.PLAIN, 24));
        panelHeader.add(lblIcone, BorderLayout.EAST);
        
        dialog.add(panelHeader, BorderLayout.NORTH);
        
        // === MESSAGE ===
        JPanel panelMessage = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelMessage.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        panelMessage.setBackground(Color.WHITE);
        
        JLabel lblMessage = new JLabel(
            "<html><div style='width: 500px;'>"
            + "Ce parking n'a plus de places disponibles. "
            + "Voici d'autres parkings à proximité :"
            + "</div></html>"
        );
        lblMessage.setFont(new Font("Arial", Font.PLAIN, 13));
        panelMessage.add(lblMessage);
        
        dialog.add(panelMessage, BorderLayout.CENTER);
        
        // === LISTE DES PARKINGS ===
        JPanel panelParkings = new JPanel();
        panelParkings.setLayout(new BoxLayout(panelParkings, BoxLayout.Y_AXIS));
        panelParkings.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        panelParkings.setBackground(Color.WHITE);
        
        final List<JRadioButton> boutons = new ArrayList<>();
        
        for (int i = 0; i < parkingsAlternatifs.size(); i++) {
            Parking p = parkingsAlternatifs.get(i);
            
            // Créer un bouton radio pour chaque parking
            JRadioButton radio = new JRadioButton();
            radio.setFont(new Font("Arial", Font.PLAIN, 13));
            radio.setBackground(Color.WHITE);
            
            boutons.add(radio);
            panelParkings.add(radio);
            panelParkings.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        
        // Grouper les boutons
        ButtonGroup groupe = new ButtonGroup();
        for (JRadioButton radio : boutons) {
            groupe.add(radio);
        }
        
        // Sélectionner le premier par défaut
        if (!boutons.isEmpty()) {
            boutons.get(0).setSelected(true);
        }
        
        JScrollPane scrollPane = new JScrollPane(panelParkings);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        // === BOUTONS ===
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        panelBoutons.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        panelBoutons.setBackground(new Color(245, 245, 245));
        
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.setFont(new Font("Arial", Font.PLAIN, 13));
        btnAnnuler.setPreferredSize(new Dimension(100, 35));
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        JButton btnChoisir = new JButton("Choisir");
        btnChoisir.setFont(new Font("Arial", Font.BOLD, 13));
        btnChoisir.setPreferredSize(new Dimension(100, 35));
        btnChoisir.setBackground(new Color(30, 70, 130));
        btnChoisir.setForeground(Color.WHITE);
        
        btnChoisir.addActionListener(e -> {
            for (int i = 0; i < boutons.size(); i++) {
                if (boutons.get(i).isSelected()) {
                    choix[0] = i;
                    break;
                }
            }
            dialog.dispose();
        });
        
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnChoisir);
        dialog.add(panelBoutons, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
        return choix[0];
    }
    
    public JRadioButton getRadioVoiture() {
        return radioVoiture;
    }

    public JRadioButton getRadioMoto() {
        return radioMoto;
    }

    public JRadioButton getRadioCamion() {
        return radioCamion;
    }



}
package ihm;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import modele.Parking;
import modele.dao.ParkingDAO;
import controleur.ControleurCarteAdmin;

import java.sql.SQLException;

public class CarteAdminOSMPanel extends JPanel {
    
    private JFXPanel fxPanel;
    private WebEngine webEngine;
    private String emailAdmin;
    private Map<String, Parking> parkingsMap = new HashMap<>();
    private boolean initialisationEnCours = false;
    private ParkingDAO parkingDAO;
    private ControleurCarteAdmin controleur;
    
    // Pour stocker les coordonnées du clic
    private Double latitudeSelectionnee = null;
    private Double longitudeSelectionnee = null;
    
    // Composants de la barre d'outils
    private JLabel lblCoords;
    private JPanel toolbar;
    
    // Constantes pour le tarif soirée
    private static final double TARIF_SOIREE = 5.90;
    
    public CarteAdminOSMPanel(String emailAdmin) {
        this.emailAdmin = emailAdmin;
        this.parkingDAO = ParkingDAO.getInstance();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Créer la barre d'outils D'ABORD
        toolbar = creerBarreOutils();
        add(toolbar, BorderLayout.NORTH);
        
        // Créer un panel pour contenir la carte avec un layout qui garde la bonne taille
        JPanel carteContainer = new JPanel(new BorderLayout());
        
        // Afficher un message de chargement DANS le container, pas dans le panel principal
        JLabel lblChargement = new JLabel("Chargement de la carte d'administration...", SwingConstants.CENTER);
        lblChargement.setFont(new Font("Arial", Font.PLAIN, 14));
        lblChargement.setForeground(Color.GRAY);
        carteContainer.add(lblChargement, BorderLayout.CENTER);
        
        // Ajouter le container au centre (pas directement la carte)
        add(carteContainer, BorderLayout.CENTER);
        
        // Initialiser le contrôleur
        this.controleur = new ControleurCarteAdmin(this, emailAdmin);
        
        // Initialiser JavaFX en arrière-plan
        SwingUtilities.invokeLater(() -> {
            initialiserCarte(carteContainer, lblChargement);
        });
    }
    
    private void initialiserCarte(JPanel container, JLabel lblChargement) {
        if (initialisationEnCours) return;
        initialisationEnCours = true;
        
        try {
            JavaFXInitializer.initializeJavaFX();
            
            Platform.runLater(() -> {
                try {
                    creerFXPanel(container, lblChargement);
                } catch (Exception e) {
                    e.printStackTrace();
                    afficherErreur(container, "Erreur JavaFX: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur(container, "Erreur d'initialisation: " + e.getMessage());
            initialisationEnCours = false;
        }
    }
    
    private void creerFXPanel(JPanel container, JLabel lblChargement) {
        try {
            fxPanel = new JFXPanel();
            
            fxPanel.setMinimumSize(new Dimension(100, 100));
            fxPanel.setPreferredSize(new Dimension(800, 600));
            
            Platform.runLater(() -> {
                try {
                    WebView webView = new WebView();
                    webEngine = webView.getEngine();
                    
                    webEngine.setOnAlert(event -> {
                        String message = event.getData();
                        
                        if (message.startsWith("coords:")) {
                            String[] parts = message.split(":");
                            if (parts.length >= 3) {
                                try {
                                    latitudeSelectionnee = Double.parseDouble(parts[1]);
                                    longitudeSelectionnee = Double.parseDouble(parts[2]);
                                    
                                    SwingUtilities.invokeLater(() -> {
                                        updateCoordsLabel(latitudeSelectionnee, longitudeSelectionnee);
                                        ouvrirFormulaireParking();
                                    });
                                } catch (NumberFormatException e) {
                                    System.err.println("Erreur de parsing des coordonnées: " + message);
                                }
                            }
                        } else if (message.startsWith("modifierParking:")) {
                            String[] parts = message.split(":");
                            if (parts.length >= 2) {
                                String idParking = parts[1];
                                SwingUtilities.invokeLater(() -> {
                                    ouvrirFormulaireModificationParking(idParking);
                                });
                            }
                        } else if (message.startsWith("supprimerParking:")) {
                            String[] parts = message.split(":");
                            if (parts.length >= 2) {
                                String idParking = parts[1];
                                SwingUtilities.invokeLater(() -> {
                                    supprimerParking(idParking);
                                });
                            }
                        }
                    });
                    
                    String html = genererHTML();
                    webEngine.loadContent(html);
                    
                    fxPanel.setScene(new Scene(webView));
                    
                    SwingUtilities.invokeLater(() -> {
                        try {
                            container.removeAll();
                            container.setLayout(new BorderLayout());
                            container.add(fxPanel, BorderLayout.CENTER);
                            container.revalidate();
                            container.repaint();
                            revalidate();
                            repaint();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        initialisationEnCours = false;
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        afficherErreur(container, "Erreur WebView: " + e.getMessage());
                        initialisationEnCours = false;
                    });
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                afficherErreur(container, "Erreur JFXPanel: " + e.getMessage());
                initialisationEnCours = false;
            });
        }
    }
    
    private void afficherErreur(JPanel container, String message) {
        container.removeAll();
        JLabel lblErreur = new JLabel("<html><center>" + message + "</center></html>", 
                                      SwingConstants.CENTER);
        lblErreur.setFont(new Font("Arial", Font.PLAIN, 12));
        lblErreur.setForeground(Color.RED);
        container.add(lblErreur, BorderLayout.CENTER);
        container.revalidate();
        container.repaint();
    }
    
    private JPanel creerBarreOutils() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(new Color(240, 240, 240));
        toolbar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JButton btnAjouterParking = new JButton("➕ Ajouter un parking");
        btnAjouterParking.setFont(new Font("Arial", Font.BOLD, 12));
        btnAjouterParking.setBackground(new Color(76, 175, 80));
        btnAjouterParking.setForeground(Color.WHITE);
        btnAjouterParking.setFocusPainted(false);
        btnAjouterParking.addActionListener(e -> activerModeAjout());
        
        lblCoords = new JLabel("Aucun emplacement sélectionné");
        lblCoords.setFont(new Font("Arial", Font.ITALIC, 12));
        lblCoords.setForeground(Color.DARK_GRAY);
        
        toolbar.add(btnAjouterParking);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(lblCoords);
        
        return toolbar;
    }
    
    private void activerModeAjout() {
        if (webEngine != null) {
            Platform.runLater(() -> {
                webEngine.executeScript("if (window.activateAddMode) window.activateAddMode();");
            });
            
            JOptionPane.showMessageDialog(this,
                "Mode ajout activé !\n\n" +
                "Instructions :\n" +
                "1. Cliquez sur la carte à l'emplacement souhaité\n" +
                "2. Un marqueur vert apparaîtra\n" +
                "3. Confirmez l'emplacement dans le popup",
                "Mode Ajout",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            afficherMessageErreur("Carte non chargée", "La carte n'est pas encore chargée. Veuillez patienter.");
        }
    }
    
    public void ouvrirFormulaireParking() {
        if (latitudeSelectionnee == null || longitudeSelectionnee == null) {
            JOptionPane.showMessageDialog(this,
                "Veuillez d'abord sélectionner un emplacement sur la carte",
                "Emplacement requis",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Créer un dialogue pour le formulaire
        JDialog dialog = new JDialog((Window) null, "Ajouter un parking", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(500, 550);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Titre
        JLabel lblTitre = new JLabel("Nouveau parking");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitre.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitre);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Coordonnées
        JPanel panelCoords = new JPanel(new BorderLayout(10, 5));
        JLabel lblCoordsInfo = new JLabel("Coordonnées sélectionnées:");
        JLabel lblCoordsValue = new JLabel(String.format("Lat: %.6f, Lng: %.6f", 
            latitudeSelectionnee, longitudeSelectionnee));
        lblCoordsValue.setFont(new Font("Arial", Font.ITALIC, 12));
        panelCoords.add(lblCoordsInfo, BorderLayout.WEST);
        panelCoords.add(lblCoordsValue, BorderLayout.CENTER);
        mainPanel.add(panelCoords);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Champs du formulaire
        JTextField txtNom = new JTextField();
        JTextField txtId = new JTextField();
        JTextField txtAdresse = new JTextField();
        JSpinner spinnerPlacesTotal = new JSpinner(new SpinnerNumberModel(50, 1, 1000, 1));
        JSpinner spinnerPlacesMoto = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        JSpinner spinnerHauteur = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10.0, 0.5));
        JCheckBox cbRelaisTisseo = new JCheckBox("Parking Relais Tisséo (gratuit)");
        JSpinner spinnerTarifHoraire = new JSpinner(new SpinnerNumberModel(2.0, 0.0, 10.0, 0.5));
        JCheckBox cbTarifSoiree = new JCheckBox("Proposer un tarif soirée (" + TARIF_SOIREE + "€ de 19h30 à 3h)");
        
        // Configurer le champ ID (non éditable, généré automatiquement)
        txtId.setEditable(false);
        txtId.setBackground(new Color(240, 240, 240));
        
        // Générer l'ID automatiquement à partir du nom
        txtNom.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateIdFromNom(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateIdFromNom(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateIdFromNom(); }
            
            private void updateIdFromNom() {
                String nom = txtNom.getText().trim();
                if (!nom.isEmpty()) {
                    String suggestedId = nom.toUpperCase()
                        .replaceAll(" ", "_")
                        .replaceAll("[^A-Z0-9_]", "")
                        .replaceAll("__+", "_");
                    
                    if (suggestedId.length() > 50) {
                        suggestedId = suggestedId.substring(0, 50);
                    }
                    
                    if (!suggestedId.startsWith("PARK_")) {
                        suggestedId = "PARK_" + suggestedId;
                    }
                    
                    txtId.setText(suggestedId);
                }
            }
        });
        
        // Écouteurs pour les interactions
        cbRelaisTisseo.addActionListener(e -> {
            boolean isRelais = cbRelaisTisseo.isSelected();
            spinnerTarifHoraire.setEnabled(!isRelais);
            cbTarifSoiree.setEnabled(!isRelais);
            
            if (isRelais) {
                spinnerTarifHoraire.setValue(0.0);
                cbTarifSoiree.setSelected(false);
                JOptionPane.showMessageDialog(dialog,
                    "Parking Relais Tisséo:\n\n" +
                    "• Gratuit pour les abonnés Tisséo\n" +
                    "• Situé près des stations de métro/tram\n" +
                    "• Encourage l'intermodalité voiture + transport public",
                    "Information Parking Relais",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        // Ajouter les champs au formulaire
        mainPanel.add(creerChamp("Nom du parking*:", txtNom));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(creerChamp("ID Parking (auto-généré):", txtId));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(creerChamp("Adresse*:", txtAdresse));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(creerChamp("Nombre total de places:", spinnerPlacesTotal));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Places moto (toujours activé)
        mainPanel.add(creerChamp("Nombre de places moto:", spinnerPlacesMoto));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        mainPanel.add(creerChamp("Hauteur maximale (m, 0 si non limité):", spinnerHauteur));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Options tarifaires
        JLabel lblTarifs = new JLabel("Options tarifaires:");
        lblTarifs.setFont(new Font("Arial", Font.BOLD, 12));
        lblTarifs.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblTarifs);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JPanel panelOptions = new JPanel(new GridLayout(2, 1, 5, 5));
        panelOptions.add(cbRelaisTisseo);
        panelOptions.add(cbTarifSoiree);
        panelOptions.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(panelOptions);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        mainPanel.add(creerChamp("Tarif horaire standard (€/h):", spinnerTarifHoraire));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Info générale
        JLabel lblInfo = new JLabel("* Champs obligatoires");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(lblInfo);
        
        // Boutons
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnValider = new JButton("Ajouter");
        JButton btnAnnuler = new JButton("Annuler");
        
        btnValider.setBackground(new Color(46, 204, 113));
        btnValider.setForeground(Color.WHITE);
        btnAnnuler.setBackground(new Color(231, 76, 60));
        btnAnnuler.setForeground(Color.WHITE);
        
        btnValider.addActionListener(e -> {
            // Validation
            String nom = txtNom.getText().trim();
            String id = txtId.getText().trim();
            String adresse = txtAdresse.getText().trim();
            
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Le nom du parking est obligatoire", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "L'ID du parking est obligatoire", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (adresse.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "L'adresse est obligatoire", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Vérifier l'unicité de l'ID
            try {
                if (parkingDAO.findById(id) != null) {
                    int counter = 1;
                    String baseId = id;
                    while (parkingDAO.findById(id) != null && counter < 100) {
                        id = baseId + "_" + counter;
                        counter++;
                    }
                    
                    if (counter >= 100) {
                        JOptionPane.showMessageDialog(dialog,
                            "Impossible de générer un ID unique. Veuillez modifier le nom.",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    txtId.setText(id);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Erreur de vérification de l'ID: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Créer le parking
            int placesMoto = (Integer) spinnerPlacesMoto.getValue();
            boolean hasMoto = placesMoto > 0;
            
            Parking parking = new Parking(
                id,                                    // id_parking
                nom,                                   // libelle_parking
                adresse,                               // adresse_parking
                (Integer) spinnerPlacesTotal.getValue(), // nombre_places
                (Integer) spinnerPlacesTotal.getValue(), // places_disponibles (initialement = nombre total)
                (Double) spinnerHauteur.getValue(),    // hauteur_parking
                cbTarifSoiree.isSelected(),           // tarif_soiree
                hasMoto,                              // has_moto
                placesMoto,                           // places_moto
                placesMoto,                           // places_moto_disponibles (initialement = total)
                cbRelaisTisseo.isSelected(),          // est_relais
                longitudeSelectionnee.floatValue(),    // position_x (longitude)
                latitudeSelectionnee.floatValue()      // position_y (latitude)
            );
            
            // Définir le tarif horaire
            if (cbRelaisTisseo.isSelected()) {
                parking.setTarifHoraire(0.0);
            } else {
                parking.setTarifHoraire((Double) spinnerTarifHoraire.getValue());
            }
            
            // Ajouter le parking à la base via le contrôleur
            controleur.ajouterParking(parking);
            
            dialog.dispose();
            
            // Réinitialiser les coordonnées
            latitudeSelectionnee = null;
            longitudeSelectionnee = null;
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        panelBoutons.add(btnValider);
        panelBoutons.add(btnAnnuler);
        
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(panelBoutons, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private JPanel creerChamp(String label, JComponent composant) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(200, 25));
        
        if (label.endsWith("*:")) {
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            lbl.setForeground(new Color(220, 53, 69));
        }
        
        panel.add(lbl, BorderLayout.WEST);
        panel.add(composant, BorderLayout.CENTER);
        return panel;
    }
    
    public void ouvrirFormulaireModificationParking(String idParking) {
        Parking parking = parkingsMap.get(idParking);
        if (parking == null) {
            JOptionPane.showMessageDialog(this, "Parking non trouvé: " + idParking,
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Créer un dialogue pour le formulaire de modification
        JDialog dialog = new JDialog((Window) null, "Modifier le parking", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(500, 600);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Titre
        JLabel lblTitre = new JLabel("Modifier le parking");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitre.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitre);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Champs du formulaire
        JTextField txtNom = new JTextField(parking.getLibelleParking());
        JTextField txtId = new JTextField(parking.getIdParking());
        JTextField txtAdresse = new JTextField(parking.getAdresseParking());
        JSpinner spinnerPlacesTotal = new JSpinner(new SpinnerNumberModel(parking.getNombrePlaces(), 1, 1000, 1));
        JSpinner spinnerPlacesDispo = new JSpinner(new SpinnerNumberModel(parking.getPlacesDisponibles(), 0, 1000, 1));
        JSpinner spinnerPlacesMoto = new JSpinner(new SpinnerNumberModel(parking.getPlacesMoto(), 0, 100, 1));
        JSpinner spinnerPlacesMotoDispo = new JSpinner(new SpinnerNumberModel(parking.getPlacesMotoDisponibles(), 0, 100, 1));
        JSpinner spinnerHauteur = new JSpinner(new SpinnerNumberModel(parking.getHauteurParking(), 0.0, 10.0, 0.5));
        JCheckBox cbRelaisTisseo = new JCheckBox("Parking Relais Tisséo (gratuit)", parking.isEstRelais());
        JSpinner spinnerTarifHoraire = new JSpinner(new SpinnerNumberModel(parking.getTarifHoraire(), 0.0, 10.0, 0.5));
        JCheckBox cbTarifSoiree = new JCheckBox("Proposer un tarif soirée (" + TARIF_SOIREE + "€ de 19h30 à 3h)", 
                                               parking.hasTarifSoiree());
        
        // Configurer les champs
        txtId.setEditable(false);
        txtId.setBackground(new Color(240, 240, 240));
        spinnerTarifHoraire.setEnabled(!parking.isEstRelais());
        cbTarifSoiree.setEnabled(!parking.isEstRelais());
        
        // Écouteurs
        cbRelaisTisseo.addActionListener(e -> {
            boolean isRelais = cbRelaisTisseo.isSelected();
            spinnerTarifHoraire.setEnabled(!isRelais);
            cbTarifSoiree.setEnabled(!isRelais);
            
            if (isRelais) {
                spinnerTarifHoraire.setValue(0.0);
                cbTarifSoiree.setSelected(false);
            }
        });
        
        // Ajouter les champs au formulaire
        mainPanel.add(creerChamp("Nom du parking*:", txtNom));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(creerChamp("ID Parking:", txtId));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(creerChamp("Adresse*:", txtAdresse));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(creerChamp("Nombre total de places:", spinnerPlacesTotal));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(creerChamp("Places disponibles:", spinnerPlacesDispo));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Places moto
        mainPanel.add(creerChamp("Places moto total:", spinnerPlacesMoto));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(creerChamp("Places moto disponibles:", spinnerPlacesMotoDispo));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        mainPanel.add(creerChamp("Hauteur maximale (m):", spinnerHauteur));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Options tarifaires
        JLabel lblTarifs = new JLabel("Options tarifaires:");
        lblTarifs.setFont(new Font("Arial", Font.BOLD, 12));
        lblTarifs.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblTarifs);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JPanel panelOptions = new JPanel(new GridLayout(2, 1, 5, 5));
        panelOptions.add(cbRelaisTisseo);
        panelOptions.add(cbTarifSoiree);
        panelOptions.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(panelOptions);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        mainPanel.add(creerChamp("Tarif horaire standard (€/h):", spinnerTarifHoraire));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Boutons
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnValider = new JButton("Modifier");
        JButton btnAnnuler = new JButton("Annuler");
        
        btnValider.setBackground(new Color(255, 152, 0));
        btnValider.setForeground(Color.WHITE);
        btnAnnuler.setBackground(new Color(231, 76, 60));
        btnAnnuler.setForeground(Color.WHITE);
        
        btnValider.addActionListener(e -> {
            // Validation
            String nom = txtNom.getText().trim();
            String adresse = txtAdresse.getText().trim();
            
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Le nom du parking est obligatoire", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (adresse.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "L'adresse est obligatoire", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Mettre à jour le parking
            parking.setLibelleParking(nom);
            parking.setAdresseParking(adresse);
            parking.setNombrePlaces((Integer) spinnerPlacesTotal.getValue());
            parking.setPlacesDisponibles((Integer) spinnerPlacesDispo.getValue());
            parking.setHauteurParking((Double) spinnerHauteur.getValue());
            parking.setEstRelais(cbRelaisTisseo.isSelected());
            parking.setTarifSoiree(cbTarifSoiree.isSelected());
            
            int placesMoto = (Integer) spinnerPlacesMoto.getValue();
            int placesMotoDispo = (Integer) spinnerPlacesMotoDispo.getValue();
            boolean hasMoto = placesMoto > 0;
            
            parking.setHasMoto(hasMoto);
            parking.setPlacesMoto(placesMoto);
            parking.setPlacesMotoDisponibles(placesMotoDispo);
            
            if (cbRelaisTisseo.isSelected()) {
                parking.setTarifHoraire(0.0);
            } else {
                parking.setTarifHoraire((Double) spinnerTarifHoraire.getValue());
            }
            
            // Modifier le parking via le contrôleur
            controleur.modifierParking(parking);
            
            dialog.dispose();
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        panelBoutons.add(btnValider);
        panelBoutons.add(btnAnnuler);
        
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(panelBoutons, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private String genererHTML() {
        // Charger les parkings
        List<Parking> parkings = getParkingsFromDatabase();
        parkingsMap.clear();
        
        StringBuilder markersJS = new StringBuilder();
        int parkingsAvecCoords = 0;
        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLng = Double.MIN_VALUE;
        
        for (Parking parking : parkings) {
            Float posX = parking.getPositionX();
            Float posY = parking.getPositionY();
            
            if (posX != null && posY != null && posX != 0.0f && posY != 0.0f) {
                String name = parking.getLibelleParking();
                String id = parking.getIdParking();
                double lat = posY;
                double lng = posX;
                
                parkingsMap.put(id, parking);
                
                minLat = Math.min(minLat, lat);
                maxLat = Math.max(maxLat, lat);
                minLng = Math.min(minLng, lng);
                maxLng = Math.max(maxLng, lng);
                
                StringBuilder popupContent = new StringBuilder();
                popupContent.append("<b>").append(escapeHtml(name)).append("</b><br>");
                popupContent.append("<small>ID: ").append(id).append("</small><br>");
                
                String adresse = parking.getAdresseParking();
                if (adresse != null && !adresse.isEmpty()) {
                    popupContent.append("Adresse: ").append(escapeHtml(adresse)).append("<br>");
                }
                
                popupContent.append("Places: ").append(parking.getPlacesDisponibles())
                           .append("/").append(parking.getNombrePlaces());
                
                if (parking.hasMoto()) {
                    int placesMoto = parking.getPlacesMoto();
                    int placesMotoDisponibles = parking.getPlacesMotoDisponibles();
                    popupContent.append("<br>Places moto: ").append(placesMotoDisponibles)
                               .append("/").append(placesMoto);
                }
                
                if (parking.isEstRelais()) {
                    popupContent.append("<br><i>Parking Relais</i>");
                }
                
                popupContent.append("<br><br>");
                popupContent.append("<button onclick=\"window.alert('modifierParking:").append(id).append("');\" ")
                           .append("style=\"background:#FF9800;color:white;padding:8px 16px;")
                           .append("border:none;border-radius:4px;cursor:pointer;margin-right:5px;font-weight:bold;\">")
                           .append("Modifier</button>");
                
                popupContent.append("<button onclick=\"window.alert('supprimerParking:").append(id).append("');\" ")
                           .append("style=\"background:#F44336;color:white;padding:8px 16px;")
                           .append("border:none;border-radius:4px;cursor:pointer;font-weight:bold;\">")
                           .append("Supprimer</button>");
                
                String markerColor = parking.isEstRelais() ? "#ff3333" : "#3388ff";
                
                markersJS.append("L.circleMarker([").append(lat).append(", ").append(lng).append("], {")
                        .append("radius: 10,")
                        .append("fillColor: '").append(markerColor).append("',")
                        .append("color: '#000',")
                        .append("weight: 2,")
                        .append("opacity: 1,")
                        .append("fillOpacity: 0.8")
                        .append("})")
                        .append(".addTo(map)")
                        .append(".bindPopup('").append(popupContent.toString().replace("'", "\\'")).append("');\n");
                
                parkingsAvecCoords++;
            }
        }
        
        
        boolean hasValidBounds = (minLat < maxLat && minLng < maxLng);
        String boundsJS = "";
        if (hasValidBounds && parkingsAvecCoords > 0) {
            boundsJS = "        var bounds = L.latLngBounds([\n" +
                      "            [" + (minLat - 0.01) + ", " + (minLng - 0.01) + "],\n" +
                      "            [" + (maxLat + 0.01) + ", " + (maxLng + 0.01) + "]\n" +
                      "        ]);\n" +
                      "        map.fitBounds(bounds, {padding: [50, 50]});\n";
        } else {
            boundsJS = "        map.setView([43.604652, 1.444209], 13);\n";
        }
        
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.css\" />\n" +
            "    <script src=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.js\"></script>\n" +
            "    <style>\n" +
            "        body { margin: 0; padding: 0; }\n" +
            "        #map { height: 100vh; width: 100vw; }\n" +
            "        .info-box {\n" +
            "            position: absolute;\n" +
            "            top: 10px;\n" +
            "            right: 10px;\n" +
            "            background: white;\n" +
            "            padding: 15px;\n" +
            "            border-radius: 8px;\n" +
            "            box-shadow: 0 0 15px rgba(0,0,0,0.3);\n" +
            "            z-index: 1000;\n" +
            "            font-family: Arial, sans-serif;\n" +
            "            max-width: 320px;\n" +
            "            font-size: 14px;\n" +
            "        }\n" +
            "        .legend {\n" +
            "            margin-top: 10px;\n" +
            "            padding-top: 10px;\n" +
            "            border-top: 1px solid #ddd;\n" +
            "        }\n" +
            "        .legend-item {\n" +
            "            display: flex;\n" +
            "            align-items: center;\n" +
            "            margin-bottom: 5px;\n" +
            "        }\n" +
            "        .legend-color {\n" +
            "            width: 20px;\n" +
            "            height: 20px;\n" +
            "            margin-right: 8px;\n" +
            "            border-radius: 50%;\n" +
            "        }\n" +
            "        .mode-indicator {\n" +
            "            position: absolute;\n" +
            "            top: 10px;\n" +
            "            left: 10px;\n" +
            "            background: white;\n" +
            "            padding: 10px;\n" +
            "            border-radius: 5px;\n" +
            "            box-shadow: 0 0 10px rgba(0,0,0,0.2);\n" +
            "            z-index: 1000;\n" +
            "            font-family: Arial, sans-serif;\n" +
            "            font-weight: bold;\n" +
            "            color: #2196F3;\n" +
            "        }\n" +
            "        .temp-marker {\n" +
            "            fill-color: #4CAF50 !important;\n" +
            "            border-color: #2E7D32 !important;\n" +
            "        }\n" +
            "        h3 { margin: 0 0 10px 0; color: #333; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div id=\"map\"></div>\n" +
            "    <div class=\"info-box\">\n" +
            "        <h3>Administration des Parkings</h3>\n" +
            "        <p><b>" + parkingsAvecCoords + " parkings</b> affichés</p>\n" +
            "        <p>Cliquez sur un parking pour le gérer</p>\n" +
            "        <p id=\"add-mode-info\" style=\"color: #4CAF50; font-weight: bold; display: none;\">" +
            "📍 Mode ajout: cliquez sur la carte</p>\n" +
            "        <div class=\"legend\">\n" +
            "            <div class=\"legend-item\">\n" +
            "                <div class=\"legend-color\" style=\"background-color: #3388ff;\"></div>\n" +
            "                <span>Parking normal</span>\n" +
            "            </div>\n" +
            "            <div class=\"legend-item\">\n" +
            "                <div class=\"legend-color\" style=\"background-color: #ff3333;\"></div>\n" +
            "                <span>Parking relais</span>\n" +
            "            </div>\n" +
            "            <div class=\"legend-item\">\n" +
            "                <div class=\"legend-color\" style=\"background-color: #4CAF50;\"></div>\n" +
            "                <span>Nouveau parking (mode ajout)</span>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    <div class=\"mode-indicator\" id=\"modeIndicator\">Mode: Visualisation</div>\n" +
            "    <script>\n" +
            "        var map;\n" +
            "        var tempMarker = null;\n" +
            "        var isAddMode = false;\n" +
            "        \n" +
            "        function initMap() {\n" +
            "            map = L.map('map').setView([43.604652, 1.444209], 13);\n" +
            "            \n" +
            "            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
            "                maxZoom: 19,\n" +
            "                attribution: '© OpenStreetMap contributors'\n" +
            "            }).addTo(map);\n" +
            "            \n" +
            markersJS.toString() +
            boundsJS +
            "            \n" +
            "            map.on('click', function(e) {\n" +
            "                if (isAddMode) {\n" +
            "                    if (tempMarker) {\n" +
            "                        map.removeLayer(tempMarker);\n" +
            "                    }\n" +
            "                    \n" +
            "                    tempMarker = L.circleMarker(e.latlng, {\n" +
            "                        radius: 12,\n" +
            "                        fillColor: '#4CAF50',\n" +
            "                        color: '#2E7D32',\n" +
            "                        weight: 3,\n" +
            "                        opacity: 1,\n" +
            "                        fillOpacity: 0.8\n" +
            "                    }).addTo(map);\n" +
            "                    \n" +
            "                    var popupContent = '<div style=\"text-align: center; padding: 10px; min-width: 250px;\">' +\n" +
            "                        '<h4 style=\"margin-top: 0; color: #4CAF50;\">Confirmer l\\'emplacement</h4>' +\n" +
            "                        '<p><b>Latitude:</b> ' + e.latlng.lat.toFixed(6) + '</p>' +\n" +
            "                        '<p><b>Longitude:</b> ' + e.latlng.lng.toFixed(6) + '</p>' +\n" +
            "                        '<button onclick=\"confirmLocation(' + e.latlng.lat + ', ' + e.latlng.lng + ')\" ' +\n" +
            "                        'style=\"background:#4CAF50;color:white;padding:8px 16px;border:none;border-radius:4px;cursor:pointer;margin:5px;font-weight:bold;\">' +\n" +
            "                        'Oui, ajouter ici</button>' +\n" +
            "                        '<button onclick=\"cancelLocation()\" ' +\n" +
            "                        'style=\"background:#f44336;color:white;padding:8px 16px;border:none;border-radius:4px;cursor:pointer;margin:5px;\">' +\n" +
            "                        'Annuler</button>' +\n" +
            "                        '</div>';\n" +
            "                    \n" +
            "                    tempMarker.bindPopup(popupContent).openPopup();\n" +
            "                }\n" +
            "            });\n" +
            "        }\n" +
            "        \n" +
            "        window.activateAddMode = function() {\n" +
            "            isAddMode = true;\n" +
            "            document.getElementById('modeIndicator').innerHTML = 'Mode: Ajout (cliquez sur la carte)';\n" +
            "            document.getElementById('modeIndicator').style.color = '#4CAF50';\n" +
            "            document.getElementById('add-mode-info').style.display = 'block';\n" +
            "            \n" +
            "            if (tempMarker) {\n" +
            "                map.removeLayer(tempMarker);\n" +
            "                tempMarker = null;\n" +
            "            }\n" +
            "        };\n" +
            "        \n" +
            "        function confirmLocation(lat, lng) {\n" +
            "            window.alert('coords:' + lat + ':' + lng);\n" +
            "            \n" +
            "            if (tempMarker) {\n" +
            "                tempMarker.setStyle({fillColor: '#FF9800'});\n" +
            "                tempMarker.closePopup();\n" +
            "            }\n" +
            "        }\n" +
            "        \n" +
            "        function cancelLocation() {\n" +
            "            if (tempMarker) {\n" +
            "                map.removeLayer(tempMarker);\n" +
            "                tempMarker = null;\n" +
            "            }\n" +
            "        }\n" +
            "        \n" +
            "        window.deactivateAddMode = function() {\n" +
            "            isAddMode = false;\n" +
            "            document.getElementById('modeIndicator').innerHTML = 'Mode: Visualisation';\n" +
            "            document.getElementById('modeIndicator').style.color = '#2196F3';\n" +
            "            document.getElementById('add-mode-info').style.display = 'none';\n" +
            "            \n" +
            "            if (tempMarker) {\n" +
            "                map.removeLayer(tempMarker);\n" +
            "                tempMarker = null;\n" +
            "            }\n" +
            "        };\n" +
            "        \n" +
            "        initMap();\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";
    }
    
    public void recharger() {
        SwingUtilities.invokeLater(() -> {
            if (webEngine != null) {
                Platform.runLater(() -> {
                    try {
                        String html = genererHTML();
                        webEngine.loadContent(html);
                        
                        webEngine.executeScript("if (window.deactivateAddMode) window.deactivateAddMode();");
                        
                        latitudeSelectionnee = null;
                        longitudeSelectionnee = null;
                        
                        if (lblCoords != null) {
                            lblCoords.setText("Aucun emplacement sélectionné");
                        }
                        
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        initialisationEnCours = false;
                        
                        Component[] components = getComponents();
                        for (Component comp : components) {
                            if (comp instanceof JPanel && comp != toolbar) {
                                remove(comp);
                                break;
                            }
                        }
                        
                        JPanel newContainer = new JPanel(new BorderLayout());
                        JLabel newLblChargement = new JLabel("Rechargement de la carte...", SwingConstants.CENTER);
                        newLblChargement.setFont(new Font("Arial", Font.PLAIN, 14));
                        newLblChargement.setForeground(Color.GRAY);
                        newContainer.add(newLblChargement, BorderLayout.CENTER);
                        
                        add(newContainer, BorderLayout.CENTER);
                        revalidate();
                        repaint();
                        
                        initialiserCarte(newContainer, newLblChargement);
                    }
                });
            } else {
                initialisationEnCours = false;
                
                Component[] components = getComponents();
                for (Component comp : components) {
                    if (comp instanceof JPanel && comp != toolbar) {
                        remove(comp);
                        break;
                    }
                }
                
                JPanel newContainer = new JPanel(new BorderLayout());
                JLabel newLblChargement = new JLabel("Chargement de la carte...", SwingConstants.CENTER);
                newLblChargement.setFont(new Font("Arial", Font.PLAIN, 14));
                newLblChargement.setForeground(Color.GRAY);
                newContainer.add(newLblChargement, BorderLayout.CENTER);
                
                add(newContainer, BorderLayout.CENTER);
                revalidate();
                repaint();
                
                initialiserCarte(newContainer, newLblChargement);
            }
        });
    }
    
    public void nettoyer() {
        if (webEngine != null) {
            Platform.runLater(() -> {
                webEngine.load(null);
            });
        }
    }
    
    private List<Parking> getParkingsFromDatabase() {
        try {
            return parkingDAO.findAll();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des parkings: " + e.getMessage());
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }
    
    private void supprimerParking(String idParking) {
        Parking parking = parkingsMap.get(idParking);
        if (parking != null) {
            int confirmation = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer le parking :\n" +
                parking.getLibelleParking() + " ?\n\n" +
                "Cette action est irréversible !",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirmation == JOptionPane.YES_OPTION) {
                controleur.supprimerParking(idParking);
            }
        }
    }
    
    public WebEngine getWebEngine() {
        return webEngine;
    }
    
    public void afficherMessageErreur(String titre, String message) {
        JOptionPane.showMessageDialog(this, message, titre, JOptionPane.ERROR_MESSAGE);
    }
    
    public void afficherMessageSucces(String titre, String message) {
        JOptionPane.showMessageDialog(this, message, titre, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void updateCoordsLabel(Double lat, Double lng) {
        if (lblCoords != null) {
            lblCoords.setText(String.format("Emplacement: %.6f, %.6f", lat, lng));
        }
    }
    
    public void executerJavaScript(String script) {
        if (webEngine != null) {
            Platform.runLater(() -> {
                webEngine.executeScript(script);
            });
        }
    }
    
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
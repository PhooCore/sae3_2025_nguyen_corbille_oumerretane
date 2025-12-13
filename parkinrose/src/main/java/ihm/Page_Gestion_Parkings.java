package ihm;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import modele.Parking;
import modele.dao.ParkingDAO;
import modele.dao.TarifParkingDAO;

public class Page_Gestion_Parkings extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private List<Parking> parkings;
    private List<Parking> parkingsFiltres;
    private JPanel panelParkings;
    private JComboBox<String> comboFiltres;
    private JCheckBox checkGratuit, checkSoiree, checkRelais, checkMoto;

    public Page_Gestion_Parkings(String email) {
        this.emailUtilisateur = email;
        this.parkings = ParkingDAO.getAllParkings();
        this.parkingsFiltres = new ArrayList<>(parkings);
        initialisePage();
    }
    
    private void initialisePage() {
        this.setTitle("Gestion des Parkings - Mode Administrateur");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(1000, 750);
        this.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 245, 255));
        
        JPanel headerPanel = creerHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        panelParkings = new JPanel();
        panelParkings.setLayout(new BoxLayout(panelParkings, BoxLayout.Y_AXIS));
        panelParkings.setBackground(new Color(240, 245, 255));
        
        JScrollPane scrollPane = new JScrollPane(panelParkings);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        afficherParkings();
        
        this.setContentPane(mainPanel);
    }
    
    private JPanel creerHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 245, 255));
        
        JButton btnRetour = new JButton("← Retour Dashboard");
        btnRetour.addActionListener(e -> retourDashboard());
        btnRetour.setBackground(Color.WHITE);
        btnRetour.setFocusPainted(false);
        headerPanel.add(btnRetour, BorderLayout.WEST);
        
        JLabel lblTitre = new JLabel("Gestion des Parkings (" + parkingsFiltres.size() + " parkings)", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitre.setForeground(new Color(0, 70, 150));
        headerPanel.add(lblTitre, BorderLayout.NORTH);
        
        JPanel filtresPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        filtresPanel.setBackground(new Color(240, 245, 255));
        
        JButton btnAjouter = new JButton("＋ Ajouter un Parking");
        btnAjouter.setBackground(new Color(0, 150, 0));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.setFocusPainted(false);
        btnAjouter.addActionListener(e -> ajouterParking());
        
        JLabel lblFiltresType = new JLabel("Filtrer:");
        checkGratuit = new JCheckBox("Gratuits");
        checkSoiree = new JCheckBox("Tarif soirée");
        checkRelais = new JCheckBox("Parkings relais");
        checkMoto = new JCheckBox("Places moto");
        
        checkGratuit.addActionListener(e -> appliquerFiltres());
        checkSoiree.addActionListener(e -> appliquerFiltres());
        checkRelais.addActionListener(e -> appliquerFiltres());
        checkMoto.addActionListener(e -> appliquerFiltres());
        
        JLabel lblTri = new JLabel("Trier par:");
        comboFiltres = new JComboBox<>(new String[]{
            "Ordre alphabétique (A-Z)",
            "Places disponibles (décroissant)",
            "Capacité totale (décroissant)",
            "ID Parking"
        });
        comboFiltres.addActionListener(e -> appliquerFiltres());
        
        filtresPanel.add(btnAjouter);
        filtresPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        filtresPanel.add(lblFiltresType);
        filtresPanel.add(checkGratuit);
        filtresPanel.add(checkSoiree);
        filtresPanel.add(checkRelais);
        filtresPanel.add(checkMoto);
        filtresPanel.add(lblTri);
        filtresPanel.add(comboFiltres);
        
        headerPanel.add(filtresPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private void appliquerFiltres() {
        String triSelectionne = (String) comboFiltres.getSelectedItem();
        parkingsFiltres = new ArrayList<>(parkings);
        
        if (checkGratuit.isSelected()) {
            parkingsFiltres.removeIf(p -> !TarifParkingDAO.estParkingGratuit(p.getIdParking()));
        }
        if (checkSoiree.isSelected()) {
            parkingsFiltres.removeIf(p -> !TarifParkingDAO.proposeTarifSoiree(p.getIdParking()));
        }
        if (checkRelais.isSelected()) {
            parkingsFiltres.removeIf(p -> !TarifParkingDAO.estParkingRelais(p.getIdParking()));
        }
        if (checkMoto.isSelected()) {
            parkingsFiltres.removeIf(p -> !p.hasMoto());
        }
        
        switch (triSelectionne) {
            case "Ordre alphabétique (A-Z)":
                parkingsFiltres.sort(Comparator.comparing(Parking::getLibelleParking));
                break;
            case "Places disponibles (décroissant)":
                parkingsFiltres.sort(Comparator.comparingInt(Parking::getPlacesDisponibles).reversed());
                break;
            case "Capacité totale (décroissant)":
                parkingsFiltres.sort(Comparator.comparingInt(Parking::getNombrePlaces).reversed());
                break;
            case "ID Parking":
                parkingsFiltres.sort(Comparator.comparing(Parking::getIdParking));
                break;
        }
        
        String titre = "Gestion des Parkings (" + parkingsFiltres.size() + " parkings)";
        ((JLabel)((JPanel)getContentPane().getComponent(0)).getComponent(1)).setText(titre);
        
        afficherParkings();
    }
    
    private void afficherParkings() {
        panelParkings.removeAll();
        
        if (parkingsFiltres.isEmpty()) {
            JLabel lblAucun = new JLabel("Aucun parking ne correspond aux critères sélectionnés", SwingConstants.CENTER);
            lblAucun.setFont(new Font("Arial", Font.PLAIN, 16));
            lblAucun.setForeground(Color.GRAY);
            lblAucun.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelParkings.add(lblAucun);
        } else {
            for (Parking parking : parkingsFiltres) {
                panelParkings.add(creerCarteParking(parking));
                panelParkings.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
        panelParkings.revalidate();
        panelParkings.repaint();
    }
    
    private JPanel creerCarteParking(Parking parking) {
        JPanel carte = new JPanel();
        carte.setLayout(new BorderLayout());
        carte.setBackground(new Color(255, 253, 240));
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 180, 100)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        carte.setMaximumSize(new Dimension(950, 200));
        
        // Panneau d'informations
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(255, 253, 240));
        
        JLabel lblNom = new JLabel(parking.getLibelleParking());
        lblNom.setFont(new Font("Arial", Font.BOLD, 18));
        lblNom.setForeground(new Color(0, 70, 150));
        
        JLabel lblId = new JLabel("ID: " + parking.getIdParking());
        lblId.setFont(new Font("Arial", Font.ITALIC, 12));
        lblId.setForeground(Color.DARK_GRAY);
        
        JLabel lblAdresse = new JLabel(parking.getAdresseParking());
        lblAdresse.setFont(new Font("Arial", Font.PLAIN, 14));
        lblAdresse.setForeground(Color.DARK_GRAY);
        
        // Détails du parking
        JPanel detailsPanel = new JPanel(new GridLayout(2, 3, 15, 5));
        detailsPanel.setBackground(new Color(255, 253, 240));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Ligne 1
        detailsPanel.add(creerDetailItem("🚗 Places totales", parking.getNombrePlaces() + ""));
        detailsPanel.add(creerDetailItem("🅿️ Places disponibles", parking.getPlacesDisponibles() + ""));
        detailsPanel.add(creerDetailItem("📏 Hauteur", parking.getHauteurParking() + " m"));
        
        // Ligne 2
        detailsPanel.add(creerDetailItem("🏍️ Places moto", parking.hasMoto() ? 
            parking.getPlacesMoto() + " (dispo: " + parking.getPlacesMotoDisponibles() + ")" : "Non"));
        detailsPanel.add(creerDetailItem("🌙 Tarif soirée", parking.hasTarifSoiree() ? "Oui" : "Non"));
        detailsPanel.add(creerDetailItem("💰 Gratuit", TarifParkingDAO.estParkingGratuit(parking.getIdParking()) ? "Oui" : "Non"));
        
        infoPanel.add(lblNom);
        infoPanel.add(lblId);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(lblAdresse);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(detailsPanel);
        
        carte.add(infoPanel, BorderLayout.CENTER);
        
        // Panneau de boutons d'action
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBackground(new Color(255, 253, 240));
        
        JButton btnModifier = new JButton("✏️ Modifier");
        btnModifier.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnModifier.setMaximumSize(new Dimension(150, 35));
        btnModifier.setBackground(new Color(0, 120, 200));
        btnModifier.setForeground(Color.WHITE);
        btnModifier.addActionListener(e -> modifierParking(parking));
        
        JButton btnSupprimer = new JButton("🗑️ Supprimer");
        btnSupprimer.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSupprimer.setMaximumSize(new Dimension(150, 35));
        btnSupprimer.setBackground(new Color(200, 60, 60));
        btnSupprimer.setForeground(Color.WHITE);
        btnSupprimer.addActionListener(e -> supprimerParking(parking));
        
        JButton btnGestionPlaces = new JButton("🔄 Gérer places");
        btnGestionPlaces.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGestionPlaces.setMaximumSize(new Dimension(150, 35));
        btnGestionPlaces.setBackground(new Color(100, 100, 200));
        btnGestionPlaces.setForeground(Color.WHITE);
        btnGestionPlaces.addActionListener(e -> gererPlacesParking(parking));
        
        actionPanel.add(btnModifier);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        actionPanel.add(btnGestionPlaces);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        actionPanel.add(btnSupprimer);
        
        carte.add(actionPanel, BorderLayout.EAST);
        
        return carte;
    }
    
    private JPanel creerDetailItem(String label, String valeur) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(new Color(255, 253, 240));
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        lblLabel.setForeground(Color.GRAY);
        
        JLabel lblValeur = new JLabel(valeur);
        lblValeur.setFont(new Font("Arial", Font.BOLD, 12));
        lblValeur.setForeground(Color.BLACK);
        
        item.add(lblLabel, BorderLayout.NORTH);
        item.add(lblValeur, BorderLayout.CENTER);
        
        return item;
    }
    
    private void ajouterParking() {
        JDialog dialog = new JDialog(this, "Ajouter un nouveau parking", true);
        dialog.setSize(500, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Champs du formulaire
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("ID Parking*:"), gbc);
        gbc.gridx = 1;
        JTextField txtId = new JTextField(20);
        formPanel.add(txtId, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nom du parking*:"), gbc);
        gbc.gridx = 1;
        JTextField txtNom = new JTextField(20);
        formPanel.add(txtNom, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Adresse*:"), gbc);
        gbc.gridx = 1;
        JTextField txtAdresse = new JTextField(20);
        formPanel.add(txtAdresse, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Nombre total de places*:"), gbc);
        gbc.gridx = 1;
        JSpinner spinnerPlacesTotal = new JSpinner(new SpinnerNumberModel(50, 1, 2000, 1));
        formPanel.add(spinnerPlacesTotal, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Places disponibles*:"), gbc);
        gbc.gridx = 1;
        JSpinner spinnerPlacesDispo = new JSpinner(new SpinnerNumberModel(50, 0, 2000, 1));
        formPanel.add(spinnerPlacesDispo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Hauteur (mètres)*:"), gbc);
        gbc.gridx = 1;
        JSpinner spinnerHauteur = new JSpinner(new SpinnerNumberModel(2.0, 1.5, 5.0, 0.1));
        formPanel.add(spinnerHauteur, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Tarif soirée:"), gbc);
        gbc.gridx = 1;
        JCheckBox checkTarifSoiree = new JCheckBox("Propose le tarif soirée (19h30-22h00)");
        formPanel.add(checkTarifSoiree, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Places moto:"), gbc);
        gbc.gridx = 1;
        JCheckBox checkHasMoto = new JCheckBox("Propose des places moto");
        
        JPanel motoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        motoPanel.add(checkHasMoto);
        motoPanel.add(new JLabel("Total:"));
        JSpinner spinnerMotoTotal = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
        spinnerMotoTotal.setEnabled(false);
        motoPanel.add(spinnerMotoTotal);
        motoPanel.add(new JLabel("Disponibles:"));
        JSpinner spinnerMotoDispo = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
        spinnerMotoDispo.setEnabled(false);
        motoPanel.add(spinnerMotoDispo);
        formPanel.add(motoPanel, gbc);
        
        checkHasMoto.addActionListener(e -> {
            boolean enabled = checkHasMoto.isSelected();
            spinnerMotoTotal.setEnabled(enabled);
            spinnerMotoDispo.setEnabled(enabled);
            if (!enabled) {
                spinnerMotoTotal.setValue(0);
                spinnerMotoDispo.setValue(0);
            }
        });
        
        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnValider = new JButton("Ajouter le parking");
        btnValider.setBackground(new Color(0, 150, 0));
        btnValider.setForeground(Color.WHITE);
        
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.setBackground(Color.LIGHT_GRAY);
        
        btnValider.addActionListener(e -> {
            // Validation des champs obligatoires
            if (txtId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "L'ID du parking est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (txtNom.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Le nom du parking est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (txtAdresse.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "L'adresse est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // Création du nouvel objet Parking
                Parking nouveauParking = new Parking(
                    txtId.getText().trim().toUpperCase(),
                    txtNom.getText().trim(),
                    (int) spinnerPlacesTotal.getValue(),
                    (int) spinnerPlacesDispo.getValue(),
                    txtAdresse.getText().trim(),
                    (double) spinnerHauteur.getValue(),
                    checkTarifSoiree.isSelected(),
                    checkHasMoto.isSelected(),
                    checkHasMoto.isSelected() ? (int) spinnerMotoTotal.getValue() : 0,
                    checkHasMoto.isSelected() ? (int) spinnerMotoDispo.getValue() : 0
                );
                
                // Vérification que les places disponibles ne dépassent pas le total
                if (nouveauParking.getPlacesDisponibles() > nouveauParking.getNombrePlaces()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Les places disponibles ne peuvent pas dépasser le nombre total de places",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Vérification similaire pour les motos
                if (nouveauParking.hasMoto()) {
                    if (nouveauParking.getPlacesMotoDisponibles() > nouveauParking.getPlacesMoto()) {
                        JOptionPane.showMessageDialog(dialog,
                            "Les places moto disponibles ne peuvent pas dépasser le total des places moto",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                // Insertion dans la base de données
                boolean success = ParkingDAO.ajouterParking(nouveauParking);
                if (success) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Parking ajouté avec succès!\n" +
                        "ID: " + nouveauParking.getIdParking() + "\n" +
                        "Nom: " + nouveauParking.getLibelleParking(),
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Ajouter à la liste et rafraîchir
                    parkings.add(nouveauParking);
                    dialog.dispose();
                    appliquerFiltres();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Erreur lors de l'ajout du parking.\n" +
                        "L'ID existe peut-être déjà dans la base de données.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Erreur: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnAnnuler);
        buttonPanel.add(btnValider);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        infoPanel.setBackground(new Color(240, 248, 255));
        
        JLabel lblInfo = new JLabel("⚠️ Champs marqués d'un * sont obligatoires");
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblInfo.setForeground(Color.DARK_GRAY);
        infoPanel.add(lblInfo);
        
        dialog.add(infoPanel, BorderLayout.NORTH);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void modifierParking(Parking parking) {
        JDialog dialog = new JDialog(this, "Modifier le parking: " + parking.getLibelleParking(), true);
        dialog.setSize(500, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Champs du formulaire pré-remplis
        // ... (code identique au formulaire précédent)
        
        JButton btnValider = new JButton("Enregistrer les modifications");
        JButton btnAnnuler = new JButton("Annuler");
        
        btnValider.addActionListener(e -> {
            try {
                // Récupérer les nouvelles valeurs
                Parking parkingModifie = new Parking(
                    parking.getIdParking(), // ID inchangé
                    // ... autres valeurs récupérées des champs
                );
                
                // Appeler la DAO pour mettre à jour
                boolean success = ParkingDAO.modifierParking(parkingModifie);
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Parking modifié avec succès!");
                    // Rafraîchir la liste
                    int index = parkings.indexOf(parking);
                    if (index != -1) {
                        parkings.set(index, parkingModifie);
                    }
                    dialog.dispose();
                    appliquerFiltres(); // Rafraîchir l'affichage
                }
            } catch (Exception ex) {
                // Gestion des erreurs
            }
        });
        
        // ... reste du code du formulaire
    }
    
    private void supprimerParking(Parking parking) {
        // Code de suppression avec confirmation
    }
    
    private void gererPlacesParking(Parking parking) {
        // Code pour gérer les places disponibles
    }
    
    private void retourDashboard() {
        // Retour au dashboard admin ou à la page principale
        // Page_Principale pagePrinc = new Page_Principale(emailUtilisateur);
        // pagePrinc.setVisible(true);
        dispose();
    }
}
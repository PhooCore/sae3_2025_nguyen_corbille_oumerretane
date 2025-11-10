package ihm;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import modèle.Parking;
import dao.ParkingDAO;
import dao.TarifParkingDAO;

public class Page_Tous_Parkings extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;          // Email de l'utilisateur connecté
    private List<Parking> parkings;           // Liste de tous les parkings
    private List<Parking> parkingsFiltres;    // Liste des parkings après filtrage
    private JPanel panelParkings;             // Panel pour afficher les parkings
    private JComboBox<String> comboFiltres;   // Combo box pour le tri
    private JCheckBox checkGratuit, checkSoiree, checkRelais; // Checkboxes pour les filtres

    /**
     * Constructeur de la page affichant tous les parkings
     * @param email l'email de l'utilisateur connecté
     * @param parkings la liste des parkings à afficher
     */
    public Page_Tous_Parkings(String email, List<Parking> parkings) {
        this.emailUtilisateur = email;
        this.parkings = parkings;
        this.parkingsFiltres = new ArrayList<>(parkings); // Initialise avec tous les parkings
        initialisePage(); // Initialisation de l'interface
    }
    
    /**
     * Initialise l'interface utilisateur de la page
     * Structure : En-tête + Liste des parkings + Boutons
     */
    private void initialisePage() {
        // Configuration de la fenêtre
        this.setTitle("Tous les parkings");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Ne ferme que cette fenêtre
        this.setSize(900, 700); // Plus large pour accommoder les filtres avancés
        this.setLocationRelativeTo(null); // Centre la fenêtre
        
        // Panel principal avec bordures
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Marge de 20px
        mainPanel.setBackground(Color.WHITE);
        
        // === EN-TÊTE DE LA PAGE AVEC FILTRES AVANCÉS ===
        JPanel headerPanel = creerHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // === LISTE DES PARKINGS ===
        panelParkings = new JPanel();
        panelParkings.setLayout(new BoxLayout(panelParkings, BoxLayout.Y_AXIS)); // Layout vertical
        panelParkings.setBackground(Color.WHITE);
        
        // Scroll pane pour permettre le défilement si nombreux parkings
        JScrollPane scrollPane = new JScrollPane(panelParkings);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // Marge supérieure
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        afficherParkings(); // Remplissage de la liste des parkings
        
        this.setContentPane(mainPanel);
    }
    
    /**
     * Crée le panel d'en-tête avec bouton retour, titre et système de filtrage avancé
     */
    private JPanel creerHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        // Bouton retour vers l'accueil
        JButton btnRetour = new JButton("← Retour");
        btnRetour.addActionListener(e -> retourAccueil());
        btnRetour.setBackground(Color.WHITE);
        btnRetour.setFocusPainted(false); // Désactive l'effet de focus
        headerPanel.add(btnRetour, BorderLayout.WEST);
        
        // Titre avec le nombre total de parkings
        JLabel lblTitre = new JLabel("Tous les parkings (" + parkingsFiltres.size() + ")", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 19));
        headerPanel.add(lblTitre, BorderLayout.NORTH);
        
        // === PANEL DES FILTRES AVANCÉS ===
        JPanel filtresPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        filtresPanel.setBackground(Color.WHITE);
        
        // Filtres par type (checkboxes)
        JLabel lblFiltresType = new JLabel("Filtrer:");
        checkGratuit = new JCheckBox("Gratuits");
        checkSoiree = new JCheckBox("Tarif soirée");
        checkRelais = new JCheckBox("Parkings relais");
        
        // Ajout des listeners pour les checkboxes
        checkGratuit.addActionListener(e -> appliquerFiltres());
        checkSoiree.addActionListener(e -> appliquerFiltres());
        checkRelais.addActionListener(e -> appliquerFiltres());
        
        // Séparateur visuel
        filtresPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        
        // Tri (combo box)
        JLabel lblTri = new JLabel("Trier par:");
        comboFiltres = new JComboBox<>(new String[]{
            "Ordre alphabétique (A-Z)",
            "Ordre alphabétique (Z-A)",
            "Places disponibles (décroissant)",
            "Places disponibles (croissant)", 
            "Capacité totale (décroissant)",
            "Capacité totale (croissant)",
            "Hauteur (décroissant)",
            "Hauteur (croissant)"
        });
        comboFiltres.addActionListener(e -> appliquerFiltres());
        
        // Assemblage des composants de filtrage
        filtresPanel.add(lblFiltresType);
        filtresPanel.add(checkGratuit);
        filtresPanel.add(checkSoiree);
        filtresPanel.add(checkRelais);
        filtresPanel.add(lblTri);
        filtresPanel.add(comboFiltres);
        
        headerPanel.add(filtresPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Applique les filtres et le tri sélectionnés sur la liste des parkings
     */
    private void appliquerFiltres() {
        parkingsFiltres = new ArrayList<>(parkings); // Réinitialise avec tous les parkings
        
        // Application des filtres par type (checkboxes)
        if (checkGratuit.isSelected()) {
            parkingsFiltres.removeIf(p -> !TarifParkingDAO.estParkingGratuit(p.getIdParking()));
        }
        if (checkSoiree.isSelected()) {
            parkingsFiltres.removeIf(p -> !TarifParkingDAO.proposeTarifSoiree(p.getIdParking()));
        }
        if (checkRelais.isSelected()) {
            parkingsFiltres.removeIf(p -> !TarifParkingDAO.estParkingRelais(p.getIdParking()));
        }
        
        // Application du tri (combo box)
        String triSelectionne = (String) comboFiltres.getSelectedItem();
        switch (triSelectionne) {
            case "Ordre alphabétique (A-Z)":
                parkingsFiltres.sort(Comparator.comparing(Parking::getLibelleParking));
                break;
            case "Ordre alphabétique (Z-A)":
                parkingsFiltres.sort(Comparator.comparing(Parking::getLibelleParking).reversed());
                break;
            case "Places disponibles (décroissant)":
                parkingsFiltres.sort(Comparator.comparingInt(Parking::getPlacesDisponibles).reversed());
                break;
            case "Places disponibles (croissant)":
                parkingsFiltres.sort(Comparator.comparingInt(Parking::getPlacesDisponibles));
                break;
            case "Capacité totale (décroissant)":
                parkingsFiltres.sort(Comparator.comparingInt(Parking::getNombrePlaces).reversed());
                break;
            case "Capacité totale (croissant)":
                parkingsFiltres.sort(Comparator.comparingInt(Parking::getNombrePlaces));
                break;
            case "Hauteur (décroissant)":
                parkingsFiltres.sort(Comparator.comparingDouble(Parking::getHauteurParking).reversed());
                break;
            case "Hauteur (croissant)":
                parkingsFiltres.sort(Comparator.comparingDouble(Parking::getHauteurParking));
                break;
        }
        
        // Mettre à jour le titre avec le nouveau nombre de résultats
        String titre = "Tous les parkings (" + parkingsFiltres.size() + ")";
        ((JLabel)((JPanel)getContentPane().getComponent(0)).getComponent(1)).setText(titre);
        
        afficherParkings(); // Réaffiche les parkings filtrés
    }
    
    /**
     * Affiche tous les parkings dans le panel
     * Gère le cas où aucun parking n'est disponible
     */
    private void afficherParkings() {
        panelParkings.removeAll(); // Vide le panel avant de le remplir
        
        if (parkingsFiltres.isEmpty()) {
            // === CAS AUCUN PARKING DISPONIBLE ===
            JLabel lblAucun = new JLabel("Aucun parking ne correspond aux critères sélectionnés", SwingConstants.CENTER);
            lblAucun.setFont(new Font("Arial", Font.PLAIN, 16));
            lblAucun.setForeground(Color.GRAY);
            lblAucun.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrage horizontal
            panelParkings.add(lblAucun);
        } else {
            // === CAS AVEC PARKINGS ===
            // Création d'une carte pour chaque parking
            for (Parking parking : parkingsFiltres) {
                panelParkings.add(creerCarteParking(parking));
                panelParkings.add(Box.createRigidArea(new Dimension(0, 10))); // Espacement entre les cartes
            }
        }
        
        // Actualisation de l'affichage
        panelParkings.revalidate();
        panelParkings.repaint();
    }
    
    /**
     * Crée une carte visuelle pour représenter un parking
     * @param parking l'objet Parking à afficher
     * @return JPanel représentant la carte du parking
     */
    private JPanel creerCarteParking(Parking parking) {
        JPanel carte = new JPanel();
        carte.setLayout(new BorderLayout());
        carte.setBackground(Color.WHITE);
        // Bordure grise avec padding interne
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), // Bordure externe
            BorderFactory.createEmptyBorder(15, 15, 15, 15) // Marge interne
        ));
        carte.setMaximumSize(new Dimension(800, 120)); // Taille maximale fixe
        carte.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main au survol
        
        // === PANEL DES INFORMATIONS (partie gauche) ===
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        
        // Nom du parking (en bleu et gras)
        JLabel lblNom = new JLabel(parking.getLibelleParking());
        lblNom.setFont(new Font("Arial", Font.BOLD, 16));
        lblNom.setForeground(new Color(0, 100, 200)); // Bleu
        
        // Adresse du parking
        JLabel lblAdresse = new JLabel(parking.getAdresseParking());
        lblAdresse.setFont(new Font("Arial", Font.PLAIN, 14));
        lblAdresse.setForeground(Color.DARK_GRAY);
        
        // === PANEL DES DÉTAILS (indicateurs + infos techniques) ===
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        detailsPanel.setBackground(Color.WHITE);
        
        // Indicateur nombre de places (disponibles/total)
        JLabel lblPlaces = new JLabel("Places: " + parking.getPlacesDisponibles() + "/" + parking.getNombrePlaces());
        lblPlaces.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Indicateur hauteur maximale autorisée
        JLabel lblHauteur = new JLabel("Hauteur max: " + parking.getHauteurParking() + "m");
        lblHauteur.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // === INDICATEURS SPÉCIAUX (badges colorés) ===
        
        // Indicateur parking gratuit (étoile verte)
        if (TarifParkingDAO.estParkingGratuit(parking.getIdParking())) {
            JLabel lblGratuit = new JLabel("★ GRATUIT");
            lblGratuit.setFont(new Font("Arial", Font.BOLD, 12));
            lblGratuit.setForeground(Color.GREEN.darker()); // Vert foncé
            detailsPanel.add(lblGratuit);
        }
        
        // Indicateur tarif soirée (étoile orange)
        if (TarifParkingDAO.proposeTarifSoiree(parking.getIdParking())) {
            JLabel lblSoiree = new JLabel("★ Tarif soirée");
            lblSoiree.setFont(new Font("Arial", Font.BOLD, 12));
            lblSoiree.setForeground(Color.ORANGE.darker()); // Orange foncé
            detailsPanel.add(lblSoiree);
        }
        
        // Indicateur parking relais (étoile bleue)
        if (TarifParkingDAO.estParkingRelais(parking.getIdParking())) {
            JLabel lblRelais = new JLabel("★ Parking relais");
            lblRelais.setFont(new Font("Arial", Font.BOLD, 12));
            lblRelais.setForeground(Color.BLUE.darker()); // Bleu foncé
            detailsPanel.add(lblRelais);
        }
        
        // Ajout des informations techniques aux indicateurs
        detailsPanel.add(lblPlaces);
        detailsPanel.add(lblHauteur);
        
        // === ASSEMBLAGE DU PANEL D'INFORMATIONS ===
        infoPanel.add(lblNom);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Espacement
        infoPanel.add(lblAdresse);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Espacement
        infoPanel.add(detailsPanel);
        
        carte.add(infoPanel, BorderLayout.CENTER);
        
        // === BOUTON D'ACTION (partie droite) ===
        JButton btnSelect = new JButton("Stationner ici");
        btnSelect.setPreferredSize(new Dimension(120, 35)); // Taille fixe
        btnSelect.addActionListener(e -> selectionnerParking(parking));
        
        carte.add(btnSelect, BorderLayout.EAST);
        
        return carte;
    }
    
    /**
     * Gère la sélection d'un parking par l'utilisateur
     * Affiche une confirmation avec les détails du parking avant redirection
     * @param parking le parking sélectionné
     */
    private void selectionnerParking(Parking parking) {
        // Message de confirmation avec les détails du parking
        int choix = JOptionPane.showConfirmDialog(this,
            "Voulez-vous préparer un stationnement pour :\n" +
            parking.getLibelleParking() + "\n" +
            parking.getAdresseParking() + "\n\n" +
            "Places disponibles: " + parking.getPlacesDisponibles() + "/" + parking.getNombrePlaces() + "\n" +
            "Hauteur maximale: " + parking.getHauteurParking() + "m",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
            
        if (choix == JOptionPane.YES_OPTION) {
            // CORRECTION : Passage des paramètres nécessaires au constructeur
            Page_Garer_Parking pageParking = new Page_Garer_Parking(this.emailUtilisateur);//email et parking
            pageParking.setVisible(true);
            dispose(); // Ferme la page actuelle
        }
    }
    
    /**
     * Retourne à la page principale (accueil)
     */
    private void retourAccueil() {
        Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
        pagePrincipale.setVisible(true);
        dispose(); // Ferme la page actuelle
    }
}
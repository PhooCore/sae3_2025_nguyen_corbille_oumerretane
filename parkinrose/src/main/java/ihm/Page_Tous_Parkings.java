package ihm;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import modele.Parking;
import modele.dao.TarifParkingDAO;
import controleur.ControleurTousParkings;

public class Page_Tous_Parkings extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // Constantes de style
    private static final Color COULEUR_FOND = Color.WHITE;
    private static final Color COULEUR_PRIMAIRE = new Color(0, 100, 200);
    private static final Color COULEUR_SECONDAIRE = Color.DARK_GRAY;
    private static final Color COULEUR_GRATUIT = Color.GREEN.darker();
    private static final Color COULEUR_SOIREE = Color.ORANGE.darker();
    private static final Color COULEUR_BORDURE = new Color(200, 200, 200);
    private static final Color COULEUR_TEXTE_MOTO = new Color(100, 100, 100);
    
    // Dimensions
    private static final Dimension DIMENSION_CARTE = new Dimension(800, 140);
    private static final Dimension DIMENSION_BOUTON = new Dimension(120, 35);
    private static final int ESPACEMENT_VERTICAL = 10;
    private static final int ESPACEMENT_INTERNE = 5;
    private static final int MARGE_EXT = 20;
    private static final int MARGE_SCROLL = 20;
    
    // Polices
    private static final Font POLICE_TITRE = new Font("Arial", Font.BOLD, 19);
    private static final Font POLICE_NOM = new Font("Arial", Font.BOLD, 16);
    private static final Font POLICE_ADRESSE = new Font("Arial", Font.PLAIN, 14);
    private static final Font POLICE_DETAIL = new Font("Arial", Font.PLAIN, 12);
    private static final Font POLICE_ETIQUETTE = new Font("Arial", Font.BOLD, 12);
    private static final Font POLICE_MESSAGE = new Font("Arial", Font.PLAIN, 16);
    
    // Données
    private final String emailUtilisateur;
    private final List<Parking> parkings;
    private List<Parking> parkingsFiltres;
    
    // Composants d'interface
    private JPanel panelParkings;
    private JLabel lblTitre;
    
    // Composants de filtrage (publics pour le contrôleur)
    private JComboBox<String> comboFiltres;
    private JCheckBox checkGratuit, checkSoiree, checkRelais, checkMoto;
    
    // DAO pour accès aux données
    private TarifParkingDAO tarifParkingDAO;
    
    public Page_Tous_Parkings(String email, List<Parking> parkings) {
        this.emailUtilisateur = email;
        this.parkings = parkings != null ? parkings : new ArrayList<>();
        this.parkingsFiltres = new ArrayList<>(this.parkings);
        this.tarifParkingDAO = TarifParkingDAO.getInstance();
        
        initialiserPage();
        initialiserControleur();
    }
    
    /**
     * Initialise la page
     */
    private void initialiserPage() {
        configurerFenetre();
        creerInterface();
        afficherParkings();
    }
    
    /**
     * Configure les propriétés de la fenêtre
     */
    private void configurerFenetre() {
        setTitle("Tous les parkings");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
    }
    
    /**
     * Crée l'interface utilisateur
     */
    private void creerInterface() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(MARGE_EXT, MARGE_EXT, MARGE_EXT, MARGE_EXT));
        mainPanel.setBackground(COULEUR_FOND);
        
        mainPanel.add(creerPanelEnTete(), BorderLayout.NORTH);
        mainPanel.add(creerPanelParkings(), BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }
    
    /**
     * Crée le panneau d'en-tête
     */
    private JPanel creerPanelEnTete() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COULEUR_FOND);
        
        headerPanel.add(creerBoutonRetour(), BorderLayout.WEST);
        headerPanel.add(creerTitre(), BorderLayout.CENTER);
        headerPanel.add(creerPanelFiltres(), BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Crée le bouton de retour
     */
    private JButton creerBoutonRetour() {
        JButton btnRetour = new JButton("← Retour");
        btnRetour.setActionCommand("RETOUR");
        return btnRetour;
    }
    
    /**
     * Crée le titre de la page
     */
    private JLabel creerTitre() {
        lblTitre = new JLabel(creerTexteTitre(), SwingConstants.CENTER);
        lblTitre.setFont(POLICE_TITRE);
        return lblTitre;
    }
    
    /**
     * Crée le texte du titre
     */
    private String creerTexteTitre() {
        return "Tous les parkings (" + parkingsFiltres.size() + ")";
    }
    
    /**
     * Crée le panneau de filtres
     */
    private JPanel creerPanelFiltres() {
        JPanel filtresPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        filtresPanel.setBackground(COULEUR_FOND);
        filtresPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        
        initialiserComposantsFiltres();
        
        filtresPanel.add(new JLabel("Filtrer:"));
        filtresPanel.add(checkGratuit);
        filtresPanel.add(checkSoiree);
        filtresPanel.add(checkRelais);
        filtresPanel.add(checkMoto);
        filtresPanel.add(new JLabel("Trier par:"));
        filtresPanel.add(comboFiltres);
        
        return filtresPanel;
    }
    
    /**
     * Initialise les composants de filtrage
     */
    private void initialiserComposantsFiltres() {
        checkGratuit = new JCheckBox("Gratuits");
        checkSoiree = new JCheckBox("Tarif soirée");
        checkRelais = new JCheckBox("Parkings relais");
        checkMoto = new JCheckBox("Places moto");
        
        String[] optionsTri = {
            "Ordre alphabétique (A-Z)",
            "Ordre alphabétique (Z-A)",
            "Places disponibles (décroissant)",
            "Places disponibles (croissant)", 
            "Capacité totale (décroissant)",
            "Capacité totale (croissant)",
            "Places moto disponibles",
            "Hauteur (décroissant)",
            "Hauteur (croissant)"
        };
        
        comboFiltres = new JComboBox<>(optionsTri);
    }
    
    /**
     * Crée le panneau des parkings
     */
    private JScrollPane creerPanelParkings() {
        panelParkings = new JPanel();
        panelParkings.setLayout(new BoxLayout(panelParkings, BoxLayout.Y_AXIS));
        panelParkings.setBackground(COULEUR_FOND);
        
        JScrollPane scrollPane = new JScrollPane(panelParkings);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(MARGE_SCROLL, 0, 0, 0));
        return scrollPane;
    }
    
    /**
     * Initialise le contrôleur
     */
    private void initialiserControleur() {
        new ControleurTousParkings(this);
    }
    
    /**
     * Applique les filtres et tris sélectionnés
     */
    public void appliquerFiltres() {
        filtrerParkings();
        trierParkings();
        mettreAJourTitre();
        afficherParkings();
    }
    
    /**
     * Filtre les parkings selon les critères sélectionnés
     */
    private void filtrerParkings() {
        parkingsFiltres = new ArrayList<>(parkings);
        
        if (checkGratuit.isSelected()) {
            parkingsFiltres.removeIf(p -> !estParkingGratuit(p));
        }
        if (checkSoiree.isSelected()) {
            parkingsFiltres.removeIf(p -> !aTarifSoiree(p));
        }
        if (checkRelais.isSelected()) {
            parkingsFiltres.removeIf(p -> !estParkingRelais(p));
        }
        if (checkMoto.isSelected()) {
            parkingsFiltres.removeIf(p -> !aPlacesMotoDisponibles(p));
        }
    }
    
    /**
     * Trie les parkings selon le critère sélectionné
     */
    private void trierParkings() {
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
            case "Places moto disponibles":
                parkingsFiltres.sort(this::comparerPlacesMoto);
                break;
            case "Hauteur (décroissant)":
                parkingsFiltres.sort(Comparator.comparingDouble(Parking::getHauteurParking).reversed());
                break;
            case "Hauteur (croissant)":
                parkingsFiltres.sort(Comparator.comparingDouble(Parking::getHauteurParking));
                break;
            default:
                // Par défaut, ordre alphabétique
                parkingsFiltres.sort(Comparator.comparing(Parking::getLibelleParking));
                break;
        }
    }
    
    /**
     * Compare deux parkings pour le tri par places moto
     */
    private int comparerPlacesMoto(Parking p1, Parking p2) {
        boolean hasMoto1 = p1.hasMoto();
        boolean hasMoto2 = p2.hasMoto();
        
        if (hasMoto1 && hasMoto2) {
            return Integer.compare(p2.getPlacesMotoDisponibles(), p1.getPlacesMotoDisponibles());
        } else if (hasMoto1) {
            return -1;
        } else if (hasMoto2) {
            return 1;
        }
        return 0;
    }
    
    /**
     * Vérifie si un parking est gratuit
     */
    private boolean estParkingGratuit(Parking parking) {
        try {
            return tarifParkingDAO.estParkingGratuit(parking.getIdParking());
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification du parking gratuit: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Vérifie si un parking a le tarif soirée
     */
    private boolean aTarifSoiree(Parking parking) {
        try {
            return tarifParkingDAO.proposeTarifSoiree(parking.getIdParking());
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification du tarif soirée: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Vérifie si un parking est un parking relais
     */
    private boolean estParkingRelais(Parking parking) {
        try {
            return tarifParkingDAO.estParkingRelais(parking.getIdParking());
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification du parking relais: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Vérifie si un parking a des places moto disponibles
     */
    private boolean aPlacesMotoDisponibles(Parking parking) {
        return parking.hasMoto() && parking.hasPlacesMotoDisponibles();
    }
    
    /**
     * Met à jour le titre avec le nombre de résultats
     */
    private void mettreAJourTitre() {
        lblTitre.setText(creerTexteTitre());
    }
    
    /**
     * Affiche les parkings
     */
    private void afficherParkings() {
        panelParkings.removeAll();
        
        if (parkingsFiltres.isEmpty()) {
            afficherAucunResultat();
        } else {
            afficherListeParkings();
        }
        
        panelParkings.revalidate();
        panelParkings.repaint();
    }
    
    /**
     * Affiche le message lorsqu'aucun résultat n'est trouvé
     */
    private void afficherAucunResultat() {
        JLabel lblAucun = creerLabelMessage(
            "Aucun parking ne correspond aux critères sélectionnés",
            POLICE_MESSAGE,
            Color.GRAY
        );
        panelParkings.add(lblAucun);
    }
    
    /**
     * Affiche la liste des parkings
     */
    private void afficherListeParkings() {
        for (int i = 0; i < parkingsFiltres.size(); i++) {
            Parking parking = parkingsFiltres.get(i);
            panelParkings.add(creerCarteParking(parking, i));
            panelParkings.add(Box.createRigidArea(new Dimension(0, ESPACEMENT_VERTICAL)));
        }
    }
    
    /**
     * Crée une carte représentant un parking
     */
    private JPanel creerCarteParking(Parking parking, int index) {
        JPanel carte = new JPanel(new BorderLayout());
        carte.setBackground(COULEUR_FOND);
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COULEUR_BORDURE),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        carte.setMaximumSize(DIMENSION_CARTE);
        carte.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        carte.add(creerPanelInformations(parking), BorderLayout.CENTER);
        carte.add(creerPanelBouton(index), BorderLayout.EAST);
        
        return carte;
    }
    
    /**
     * Crée le panneau d'informations du parking
     */
    private JPanel creerPanelInformations(Parking parking) {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(COULEUR_FOND);
        infoPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        
        infoPanel.add(creerLabelNomParking(parking));
        infoPanel.add(Box.createRigidArea(new Dimension(0, ESPACEMENT_INTERNE)));
        infoPanel.add(creerLabelAdresse(parking));
        infoPanel.add(Box.createRigidArea(new Dimension(0, ESPACEMENT_INTERNE)));
        infoPanel.add(creerPanelDetails(parking));
        
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(COULEUR_FOND);
        centerContainer.add(infoPanel, BorderLayout.WEST);
        
        return centerContainer;
    }
    
    /**
     * Crée le label du nom du parking
     */
    private JLabel creerLabelNomParking(Parking parking) {
        JLabel lblNom = new JLabel(parking.getLibelleParking());
        lblNom.setFont(POLICE_NOM);
        lblNom.setForeground(COULEUR_PRIMAIRE);
        lblNom.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lblNom;
    }
    
    /**
     * Crée le label de l'adresse
     */
    private JLabel creerLabelAdresse(Parking parking) {
        JLabel lblAdresse = new JLabel(parking.getAdresseParking());
        lblAdresse.setFont(POLICE_ADRESSE);
        lblAdresse.setForeground(COULEUR_SECONDAIRE);
        lblAdresse.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lblAdresse;
    }
    
    /**
     * Crée le panneau des détails du parking
     */
    private JPanel creerPanelDetails(Parking parking) {
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        detailsPanel.setBackground(COULEUR_FOND);
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        detailsPanel.add(creerLabelPlaces(parking));
        
        if (parking.hasMoto()) {
            detailsPanel.add(creerLabelPlacesMoto(parking));
        }
        
        detailsPanel.add(creerLabelHauteur(parking));
        
        if (estParkingGratuit(parking)) {
            detailsPanel.add(creerLabelEtiquette("GRATUIT", COULEUR_GRATUIT));
        }
        
        if (parking.hasTarifSoiree()) {
            detailsPanel.add(creerLabelEtiquette("Tarif soirée", COULEUR_SOIREE));
        }
        
        try {
            if (tarifParkingDAO.estParkingRelais(parking.getIdParking())) {
                detailsPanel.add(creerLabelEtiquette("Parking relais", Color.BLUE));
            }
        } catch (Exception e) {
            // Ignorer l'erreur pour l'affichage
        }
        
        return detailsPanel;
    }
    
    /**
     * Crée le label des places disponibles
     */
    private JLabel creerLabelPlaces(Parking parking) {
        String texte = "Ⓥ " + parking.getPlacesDisponibles() + "/" + parking.getNombrePlaces() + " places";
        return creerLabelDetail(texte, POLICE_DETAIL, COULEUR_SECONDAIRE);
    }
    
    /**
     * Crée le label des places moto
     */
    private JLabel creerLabelPlacesMoto(Parking parking) {
        String texte = "Ⓜ " + parking.getPlacesMotoDisponibles() + "/" + parking.getPlacesMoto() + " places moto";
        return creerLabelDetail(texte, POLICE_DETAIL, COULEUR_TEXTE_MOTO);
    }
    
    /**
     * Crée le label de la hauteur
     */
    private JLabel creerLabelHauteur(Parking parking) {
        String texte = "↑ " + parking.getHauteurParking() + "m";
        return creerLabelDetail(texte, POLICE_DETAIL, COULEUR_SECONDAIRE);
    }
    
    /**
     * Crée un label d'étiquette (gratuit, tarif soirée)
     */
    private JLabel creerLabelEtiquette(String texte, Color couleur) {
        JLabel label = new JLabel(texte);
        label.setFont(POLICE_ETIQUETTE);
        label.setForeground(couleur);
        return label;
    }
    
    /**
     * Crée un label de détail générique
     */
    private JLabel creerLabelDetail(String texte, Font font, Color couleur) {
        JLabel label = new JLabel(texte);
        label.setFont(font);
        label.setForeground(couleur);
        return label;
    }
    
    /**
     * Crée le panneau du bouton de stationnement
     */
    private JPanel creerPanelBouton(int index) {
        JButton btnSelect = new JButton("Stationner ici");
        btnSelect.setActionCommand("STATIONNER_" + index);
        btnSelect.setPreferredSize(DIMENSION_BOUTON);
        
        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.setBackground(COULEUR_FOND);
        buttonContainer.add(btnSelect, BorderLayout.NORTH);
        buttonContainer.add(Box.createVerticalGlue(), BorderLayout.CENTER);
        
        return buttonContainer;
    }
    
    /**
     * Crée un label de message centré
     */
    private JLabel creerLabelMessage(String texte, Font font, Color couleur) {
        JLabel label = new JLabel(texte, SwingConstants.CENTER);
        label.setFont(font);
        label.setForeground(couleur);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }
    
    // Getters pour le contrôleur
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }
    
    public List<Parking> getParkingsFiltres() {
        return parkingsFiltres;
    }
    
    public JComboBox<String> getComboFiltres() {
        return comboFiltres;
    }
    
    public JCheckBox getCheckGratuit() {
        return checkGratuit;
    }
    
    public JCheckBox getCheckSoiree() {
        return checkSoiree;
    }
    
    public JCheckBox getCheckRelais() {
        return checkRelais;
    }
    
    public JCheckBox getCheckMoto() {
        return checkMoto;
    }
    
    public TarifParkingDAO getTarifParkingDAO() {
        return tarifParkingDAO;
    }
}
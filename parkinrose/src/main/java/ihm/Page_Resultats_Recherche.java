package ihm;

import javax.swing.*;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import modele.Parking;
import modele.dao.FavoriDAO;
import modele.dao.ParkingDAO;
import modele.dao.TarifParkingDAO;
import modele.dao.UsagerDAO;
import controleur.ControleurFavoris;
import controleur.ControleurResultatsRecherche;

public class Page_Resultats_Recherche extends JFrame {
    
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
    private static final Font POLICE_TITRE = new Font("Arial", Font.BOLD, 18);
    private static final Font POLICE_NOM = new Font("Arial", Font.BOLD, 16);
    private static final Font POLICE_ADRESSE = new Font("Arial", Font.PLAIN, 14);
    private static final Font POLICE_DETAIL = new Font("Arial", Font.PLAIN, 12);
    private static final Font POLICE_ETIQUETTE = new Font("Arial", Font.BOLD, 12);
    private static final Font POLICE_MESSAGE = new Font("Arial", Font.PLAIN, 16);
    private static final Font POLICE_SUGGESTION = new Font("Arial", Font.PLAIN, 14);
    
    // Données
    private final String emailUtilisateur;
    private final String termeRecherche;
    private final List<Parking> parkings;
    private List<Parking> parkingsFiltres;
    private int idUsager;
    
    // Composants d'interface
    private JPanel panelResultats;
    private JLabel lblTitre;
    
    // Composants de filtrage (publics pour le contrôleur)
    public JComboBox<String> comboFiltres;
    public JCheckBox checkGratuit, checkSoiree, checkRelais, checkMoto;
    
    // Contrôleur
    private ControleurResultatsRecherche controleur;
    
    // Icones coeur
    private final ImageIcon COEUR_VIDE =
    	    chargerIconeRedimensionnee("/images/coeurVide.png", 24, 24);

    	private final ImageIcon COEUR_REMPLI =
    	    chargerIconeRedimensionnee("/images/coeurRempli.png", 24, 24);

    public Page_Resultats_Recherche(String email, String termeRecherche) {
        this.emailUtilisateur = email;
        this.termeRecherche = termeRecherche;
        
        try {
            this.idUsager = UsagerDAO.getInstance()
                .findById(emailUtilisateur)
                .getIdUsager();
        } catch (Exception e) {
            e.printStackTrace();
            this.idUsager = -1;
        }
        
        this.parkings = chargerParkings();
        this.setParkingsFiltres(new ArrayList<>(parkings));
        
        initialiserPage();
        initialiserControleur();
    }
    
    /**
     * Charge les parkings depuis la base de données
     */
    private List<Parking> chargerParkings() {
        try {
            return ParkingDAO.getInstance().rechercherParkings(termeRecherche);
        } catch (Exception e) {
            afficherMessageErreur("Erreur lors du chargement des parkings");
            return new ArrayList<>();
        }
    }
    
    /**
     * Initialise la page
     */
    private void initialiserPage() {
        configurerFenetre();
        creerInterface();
        afficherResultatsInitials();
    }
    
    /**
     * Configure les propriétés de la fenêtre
     */
    private void configurerFenetre() {
        setTitle("Résultats de recherche - " + termeRecherche);
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
        mainPanel.add(creerPanelResultats(), BorderLayout.CENTER);
        
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
        return getParkingsFiltres().size() + " résultat(s) pour \"" + termeRecherche + "\"";
    }
    
    /**
     * Crée le panneau de filtres
     */
    private JPanel creerPanelFiltres() {
        JPanel filtresPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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
            "Pertinence",
            "Places disponibles (décroissant)",
            "Places disponibles (croissant)", 
            "Parkings gratuits",
            "Tarif soirée",
            "Parkings relais",
            "Places moto disponibles",
            "Hauteur (décroissant)",
            "Hauteur (croissant)"
        };
        
        comboFiltres = new JComboBox<>(optionsTri);
    }
    
    /**
     * Crée le panneau des résultats
     */
    private JScrollPane creerPanelResultats() {
        panelResultats = new JPanel();
        panelResultats.setLayout(new BoxLayout(panelResultats, BoxLayout.Y_AXIS));
        panelResultats.setBackground(COULEUR_FOND);
        
        JScrollPane scrollPane = new JScrollPane(panelResultats);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(MARGE_SCROLL, 0, 0, 0));
        return scrollPane;
    }
    
    /**
     * Initialise le contrôleur
     */
    private void initialiserControleur() {
        controleur = new ControleurResultatsRecherche(this);
    }
    
    /**
     * Affiche les résultats initiaux sans configurer les listeners
     */
    private void afficherResultatsInitials() {
        panelResultats.removeAll();
        
        if (getParkingsFiltres().isEmpty()) {
            afficherAucunResultat();
        } else {
            afficherListeParkings();
        }
        
        panelResultats.revalidate();
        panelResultats.repaint();
    }
    
    /**
     * Applique les filtres et tris sélectionnés
     */
    public void appliquerFiltres() {
        filtrerParkings();
        trierParkings();
        mettreAJourTitre();
        afficherResultats();
    }
    
    /**
     * Filtre les parkings selon les critères sélectionnés
     */
    private void filtrerParkings() {
        setParkingsFiltres(new ArrayList<>(parkings));
        
        if (checkGratuit.isSelected()) {
            getParkingsFiltres().removeIf(p -> !estParkingGratuit(p));
        }
        if (checkSoiree.isSelected()) {
            getParkingsFiltres().removeIf(p -> !aTarifSoiree(p));
        }
        if (checkRelais.isSelected()) {
            getParkingsFiltres().removeIf(p -> !estParkingRelais(p));
        }
        if (checkMoto.isSelected()) {
            getParkingsFiltres().removeIf(p -> !aPlacesMotoDisponibles(p));
        }
    }
    
    /**
     * Trie les parkings selon le critère sélectionné
     */
    private void trierParkings() {
        String filtreSelectionne = (String) comboFiltres.getSelectedItem();
        
        switch (filtreSelectionne) {
            case "Places disponibles (décroissant)":
                getParkingsFiltres().sort(Comparator.comparingInt(Parking::getPlacesDisponibles).reversed());
                break;
            case "Places disponibles (croissant)":
                getParkingsFiltres().sort(Comparator.comparingInt(Parking::getPlacesDisponibles));
                break;
            case "Parkings gratuits":
                getParkingsFiltres().sort(Comparator.comparing(this::estParkingGratuit).reversed());
                break;
            case "Tarif soirée":
                getParkingsFiltres().sort(Comparator.comparing(Parking::hasTarifSoiree).reversed());
                break;
            case "Parkings relais":
                getParkingsFiltres().sort(Comparator.comparing(this::estParkingRelais).reversed());
                break;
            case "Places moto disponibles":
                getParkingsFiltres().sort(this::comparerPlacesMoto);
                break;
            case "Hauteur (décroissant)":
                getParkingsFiltres().sort(Comparator.comparingDouble(Parking::getHauteurParking).reversed());
                break;
            case "Hauteur (croissant)":
                getParkingsFiltres().sort(Comparator.comparingDouble(Parking::getHauteurParking));
                break;
            default: // "Pertinence" - pas de tri
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
     * Vérifie si un parking est gratuit - utilise la méthode statique
     */
    private boolean estParkingGratuit(Parking parking) {
        try {
            return TarifParkingDAO.getInstance().estParkingGratuit(parking.getIdParking());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Vérifie si un parking a le tarif soirée - utilise la méthode statique
     */
    private boolean aTarifSoiree(Parking parking) {
        try {
            return TarifParkingDAO.getInstance().proposeTarifSoiree(parking.getIdParking());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Vérifie si un parking est un parking relais - utilise la méthode statique
     */
    private boolean estParkingRelais(Parking parking) {
        try {
            return TarifParkingDAO.getInstance().estParkingRelais(parking.getIdParking());
        } catch (Exception e) {
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
     * Affiche les résultats avec reconfiguration des listeners
     */
    public void afficherResultats() {
        panelResultats.removeAll();
        
        if (getParkingsFiltres().isEmpty()) {
            afficherAucunResultat();
        } else {
            afficherListeParkings();
        }
        
        panelResultats.revalidate();
        panelResultats.repaint();
        
        reconfigurerListeners();
    }
    
    /**
     * Affiche le message lorsqu'aucun résultat n'est trouvé
     */
    private void afficherAucunResultat() {
        panelResultats.add(creerLabelMessage("Aucun parking trouvé pour votre recherche.", POLICE_MESSAGE));
        panelResultats.add(Box.createRigidArea(new Dimension(0, ESPACEMENT_VERTICAL)));
        panelResultats.add(creerLabelMessage(
            "Essayez avec d'autres termes ou consultez tous les parkings.",
            POLICE_SUGGESTION
        ));
        panelResultats.add(Box.createRigidArea(new Dimension(0, 20)));
        panelResultats.add(creerBoutonTousParkings());
    }
    
    /**
     * Affiche la liste des parkings
     */
    private void afficherListeParkings() {
        for (int i = 0; i < getParkingsFiltres().size(); i++) {
            Parking parking = getParkingsFiltres().get(i);
            panelResultats.add(creerCarteParking(parking, i));
            panelResultats.add(Box.createRigidArea(new Dimension(0, ESPACEMENT_VERTICAL)));
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
        
        carte.add(creerPanelCoeur(parking), BorderLayout.WEST);
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
        
        return detailsPanel;
    }
    
    /**
     * Crée le label des places disponibles
     */
    private JLabel creerLabelPlaces(Parking parking) {
        String texte = parking.getPlacesDisponibles() + "/" + parking.getNombrePlaces() + " places";
        return creerLabelDetail(texte, POLICE_DETAIL, COULEUR_SECONDAIRE);
    }
    
    /**
     * Crée le label des places moto
     */
    private JLabel creerLabelPlacesMoto(Parking parking) {
        String texte = parking.getPlacesMotoDisponibles() + "/" + parking.getPlacesMoto() + " places moto";
        return creerLabelDetail(texte, POLICE_DETAIL, COULEUR_TEXTE_MOTO);
    }
    
    /**
     * Crée le label de la hauteur
     */
    private JLabel creerLabelHauteur(Parking parking) {
        String texte = parking.getHauteurParking() + "m";
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
    private JLabel creerLabelMessage(String texte, Font font) {
        JLabel label = new JLabel(texte, SwingConstants.CENTER);
        label.setFont(font);
        label.setForeground(Color.GRAY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }
    
    /**
     * Crée le bouton "Voir tous les parkings"
     */
    private JButton creerBoutonTousParkings() {
        JButton btnTousParkings = new JButton("Voir tous les parkings");
        btnTousParkings.setActionCommand("TOUS_PARKINGS");
        btnTousParkings.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btnTousParkings;
    }
    
    private ImageIcon chargerIconeRedimensionnee(String chemin, int largeur, int hauteur) {
        ImageIcon icon = new ImageIcon(getClass().getResource(chemin));
        Image image = icon.getImage().getScaledInstance(largeur, hauteur, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    
    private JPanel creerPanelCoeur(Parking parking) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COULEUR_FOND);
        panel.setPreferredSize(new Dimension(50, 100));

        JButton coeur = creerBoutonCoeur(parking);
        coeur.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(coeur);
        panel.add(Box.createVerticalGlue());

        return panel;
    }



    private JButton creerBoutonCoeur(Parking parking) {
        JButton btnCoeur = new JButton();
        
        // Créer un contrôleur local pour gérer ce favori
        ControleurFavoris controleurFav = new ControleurFavoris(null, idUsager);
        
        // Vérifier l'état initial
        boolean estFavori = controleurFav.estFavori(parking.getIdParking());
        btnCoeur.setIcon(estFavori ? COEUR_REMPLI : COEUR_VIDE);
        
        btnCoeur.setBorderPainted(false);
        btnCoeur.setContentAreaFilled(false);
        btnCoeur.setFocusPainted(false);
        btnCoeur.setOpaque(false);
        btnCoeur.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCoeur.setPreferredSize(new Dimension(32, 32));
        
        // UTILISER LE CONTRÔLEUR pour basculer le favori
        btnCoeur.addActionListener(e -> {
            boolean succes = controleurFav.basculerFavori(parking.getIdParking());
            
            if (succes) {
                // Mettre à jour l'icône
                boolean nouveauEtat = controleurFav.estFavori(parking.getIdParking());
                btnCoeur.setIcon(nouveauEtat ? COEUR_REMPLI : COEUR_VIDE);
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors de la gestion des favoris",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        
        return btnCoeur;
    }

    
    /**
     * Reconfigure les listeners après mise à jour de l'interface
     */
    private void reconfigurerListeners() {
        if (controleur != null) {
            controleur.configurerListenersApresFiltrage();
        }
    }
    
    /**
     * Affiche un message d'erreur
     */
    private void afficherMessageErreur(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
    
    // Getters pour le contrôleur
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }

	public List<Parking> getParkingsFiltres() {
		return parkingsFiltres;
	}

	public void setParkingsFiltres(List<Parking> parkingsFiltres) {
		this.parkingsFiltres = parkingsFiltres;
	}
	
}
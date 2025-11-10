package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import modèle.Parking;
import dao.ParkingDAO;
import dao.TarifParkingDAO;

public class Page_Resultats_Recherche extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;          // Email de l'utilisateur connecté
    private String termeRecherche;            // Terme de recherche saisi par l'utilisateur
    private List<Parking> parkings;           // Liste des parkings correspondants
    private List<Parking> parkingsFiltres;    // Liste des parkings après filtrage
    private JPanel panelResultats;            // Panel pour afficher les résultats
    private JComboBox<String> comboFiltres;   // Combo box pour les filtres

    /**
     * Constructeur de la page de résultats de recherche
     * @param email l'email de l'utilisateur connecté
     * @param termeRecherche le terme de recherche saisi
     */
    public Page_Resultats_Recherche(String email, String termeRecherche) {
        this.emailUtilisateur = email;
        this.termeRecherche = termeRecherche;
        // Recherche des parkings correspondants dans la base de données
        this.parkings = ParkingDAO.rechercherParkings(termeRecherche);
        this.parkingsFiltres = new ArrayList<>(parkings); // Initialise avec tous les parkings
        initialisePage(); // Initialisation de l'interface
    }
    
    /**
     * Initialise l'interface utilisateur de la page de résultats
     */
    private void initialisePage() {
        // Configuration de la fenêtre
        this.setTitle("Résultats de recherche - " + termeRecherche);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Ne ferme que cette fenêtre
        this.setSize(900, 700); 
        this.setLocationRelativeTo(null); // Centre la fenêtre
        
        // Panel principal avec bordures
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // === EN-TÊTE DE LA PAGE AVEC FILTRES ===
        JPanel headerPanel = creerHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // === ZONE DES RÉSULTATS ===
        panelResultats = new JPanel();
        panelResultats.setLayout(new BoxLayout(panelResultats, BoxLayout.Y_AXIS)); // Layout vertical
        panelResultats.setBackground(Color.WHITE);
        
        // Scroll pane pour permettre le défilement si nombreux résultats
        JScrollPane scrollPane = new JScrollPane(panelResultats);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        afficherResultats(); // Remplissage des résultats
        
        this.setContentPane(mainPanel);
    }
    
    /**
     * Crée le panel d'en-tête avec bouton retour, titre et filtres
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
        
        // Titre avec le nombre de résultats trouvés
        String titre = parkingsFiltres.size() + " résultat(s) pour \"" + termeRecherche + "\"";
        JLabel lblTitre = new JLabel(titre, SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(lblTitre, BorderLayout.CENTER);
        
        // === PANEL DES FILTRES ===
        JPanel filtresPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filtresPanel.setBackground(Color.WHITE);
        
        // Label pour le filtre
        JLabel lblFiltre = new JLabel("Trier par:");
        
        // Combo box avec les options de filtrage
        comboFiltres = new JComboBox<>(new String[]{
            "Pertinence",
            "Places disponibles (décroissant)",
            "Places disponibles (croissant)", 
            "Parkings gratuits",
            "Tarif soirée",
            "Parkings relais",
            "Hauteur (décroissant)",
            "Hauteur (croissant)"
        });
        
        // Listener pour appliquer le filtre quand la sélection change
        comboFiltres.addActionListener(e -> appliquerFiltre());
        
        filtresPanel.add(lblFiltre);
        filtresPanel.add(comboFiltres);
        headerPanel.add(filtresPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Applique le filtre sélectionné sur la liste des parkings
     */
    private void appliquerFiltre() {
        String filtreSelectionne = (String) comboFiltres.getSelectedItem();
        parkingsFiltres = new ArrayList<>(parkings); // Réinitialise avec tous les parkings
        
        switch (filtreSelectionne) {
            case "Places disponibles (décroissant)":
                parkingsFiltres.sort(Comparator.comparingInt(Parking::getPlacesDisponibles).reversed());
                break;
            case "Places disponibles (croissant)":
                parkingsFiltres.sort(Comparator.comparingInt(Parking::getPlacesDisponibles));
                break;
            case "Parkings gratuits":
                parkingsFiltres.removeIf(p -> !TarifParkingDAO.estParkingGratuit(p.getIdParking()));
                break;
            case "Tarif soirée":
                parkingsFiltres.removeIf(p -> !TarifParkingDAO.proposeTarifSoiree(p.getIdParking()));
                break;
            case "Parkings relais":
                parkingsFiltres.removeIf(p -> !TarifParkingDAO.estParkingRelais(p.getIdParking()));
                break;
            case "Hauteur (décroissant)":
                parkingsFiltres.sort(Comparator.comparingDouble(Parking::getHauteurParking).reversed());
                break;
            case "Hauteur (croissant)":
                parkingsFiltres.sort(Comparator.comparingDouble(Parking::getHauteurParking));
                break;
            case "Pertinence":
            default:
                // Garde l'ordre original de pertinence
                break;
        }
        
        // Mettre à jour le titre avec le nouveau nombre de résultats
        String titre = parkingsFiltres.size() + " résultat(s) pour \"" + termeRecherche + "\"";
        ((JLabel)((JPanel)getContentPane().getComponent(0)).getComponent(1)).setText(titre);
        
        afficherResultats(); // Réaffiche les résultats filtrés
    }
    
    /**
     * Affiche les résultats de recherche dans le panel
     * Gère le cas où aucun résultat n'est trouvé
     */
    private void afficherResultats() {
        panelResultats.removeAll(); // Vide le panel avant de le remplir
        
        if (parkingsFiltres.isEmpty()) {
            // === CAS AUCUN RÉSULTAT ===
            
            // Message principal
            JLabel lblAucunResultat = new JLabel("Aucun parking trouvé pour votre recherche.", SwingConstants.CENTER);
            lblAucunResultat.setFont(new Font("Arial", Font.PLAIN, 16));
            lblAucunResultat.setForeground(Color.GRAY);
            lblAucunResultat.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrage horizontal
            panelResultats.add(lblAucunResultat);
            
            // Suggestion pour l'utilisateur
            JLabel lblSuggestion = new JLabel("Essayez avec d'autres termes ou consultez tous les parkings.", SwingConstants.CENTER);
            lblSuggestion.setFont(new Font("Arial", Font.PLAIN, 14));
            lblSuggestion.setForeground(Color.GRAY);
            lblSuggestion.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelResultats.add(Box.createRigidArea(new Dimension(0, 10))); // Espacement
            panelResultats.add(lblSuggestion);
            
            // Bouton alternatif pour voir tous les parkings
            JButton btnTousParkings = new JButton("Voir tous les parkings");
            btnTousParkings.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnTousParkings.addActionListener(e -> afficherTousParkings());
            panelResultats.add(Box.createRigidArea(new Dimension(0, 20))); // Espacement
            panelResultats.add(btnTousParkings);
            
        } else {
            // === CAS AVEC RÉSULTATS ===
            
            // Création d'une carte pour chaque parking trouvé
            for (Parking parking : parkingsFiltres) {
                panelResultats.add(creerCarteParking(parking));
                panelResultats.add(Box.createRigidArea(new Dimension(0, 10))); // Espacement entre les cartes
            }
        }
        
        // Actualisation de l'affichage
        panelResultats.revalidate();
        panelResultats.repaint();
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
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15) 
        ));
        carte.setMaximumSize(new Dimension(800, 120)); 
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
        
        // Indicateur nombre de places
        JLabel lblPlaces = new JLabel("Places: " + parking.getPlacesDisponibles() + "/" + parking.getNombrePlaces());
        lblPlaces.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Indicateur hauteur maximale
        JLabel lblHauteur = new JLabel("Hauteur max: " + parking.getHauteurParking() + "m");
        lblHauteur.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // === INDICATEURS SPÉCIAUX ===
        
        // Indicateur parking gratuit (étoile verte)
        if (TarifParkingDAO.estParkingGratuit(parking.getIdParking())) {
            JLabel lblGratuit = new JLabel("★ GRATUIT");
            lblGratuit.setFont(new Font("Arial", Font.BOLD, 12));
            lblGratuit.setForeground(Color.GREEN.darker());
            detailsPanel.add(lblGratuit);
        }
        
        // Indicateur tarif soirée (étoile orange)
        if (TarifParkingDAO.proposeTarifSoiree(parking.getIdParking())) {
            JLabel lblSoiree = new JLabel("★ Tarif soirée");
            lblSoiree.setFont(new Font("Arial", Font.BOLD, 12));
            lblSoiree.setForeground(Color.ORANGE.darker());
            detailsPanel.add(lblSoiree);
        }
        
        // Indicateur parking relais (étoile bleue)
        if (TarifParkingDAO.estParkingRelais(parking.getIdParking())) {
            JLabel lblRelais = new JLabel("★ Parking relais");
            lblRelais.setFont(new Font("Arial", Font.BOLD, 12));
            lblRelais.setForeground(Color.BLUE.darker());
            detailsPanel.add(lblRelais);
        }
        
        // Ajout des informations techniques
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
     * Affiche une confirmation avant de rediriger vers la page de stationnement
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
            Page_Garer_Parking pageParking = new Page_Garer_Parking(this.emailUtilisateur); //email et voiture
            pageParking.setVisible(true);
            dispose(); // Ferme la page actuelle
        }
    }
    
    /**
     * Affiche la page avec tous les parkings disponibles
     * Alternative quand la recherche ne donne pas de résultats
     */
    private void afficherTousParkings() {
        List<Parking> tousParkings = ParkingDAO.getAllParkings();
        Page_Tous_Parkings pageTousParkings = new Page_Tous_Parkings(emailUtilisateur, tousParkings);
        pageTousParkings.setVisible(true);
        dispose(); // Ferme la page actuelle
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
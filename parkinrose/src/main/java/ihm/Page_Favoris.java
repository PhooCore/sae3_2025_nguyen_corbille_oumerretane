package ihm;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import modele.Parking;
import controleur.ControleurFavoris;

// Page affichant les parkings favoris de l'utilisateur
public class Page_Favoris extends JFrame {

    // Pour la sérialisation
    private static final long serialVersionUID = 1L;

    // Constantes de style
    // Couleurs
    private static final Color COULEUR_FOND = Color.WHITE;
    private static final Color COULEUR_PRIMAIRE = new Color(0, 100, 200);
    private static final Color COULEUR_SECONDAIRE = Color.DARK_GRAY;
    private static final Color COULEUR_BORDURE = new Color(200, 200, 200);

    // Dimensions des composants
    private static final Dimension DIMENSION_CARTE = new Dimension(800, 140);
    private static final Dimension DIMENSION_BOUTON = new Dimension(140, 35);

    // Polices de caractères
    private static final Font POLICE_TITRE = new Font("Arial", Font.BOLD, 18);
    private static final Font POLICE_NOM = new Font("Arial", Font.BOLD, 16);
    private static final Font POLICE_ADRESSE = new Font("Arial", Font.PLAIN, 14);
    private static final Font POLICE_DETAIL = new Font("Arial", Font.PLAIN, 12);

    // Attributs
    private final String emailUtilisateur;    // Email de l'utilisateur connecté
    private final int idUsager;               // ID de l'utilisateur connecté

    private JPanel panelResultats;            // Panel contenant les résultats des parkings favoris
    private JLabel lblTitre;                  // Label du titre de la page

    // Icône du cœur rempli pour les favoris
    private static final ImageIcon COEUR_REMPLI =
            new ImageIcon(Page_Favoris.class.getResource("/images/coeurRempli.png"));

    // LE CONTRÔLEUR
    private ControleurFavoris controleur;

    // Constructeur
    public Page_Favoris(String emailUtilisateur, int idUsager) {
        this.emailUtilisateur = emailUtilisateur;
        this.idUsager = idUsager;

        initialiserPage();                            // Initialiser les composants de la page
        
        // Créer le contrôleur APRÈS l'initialisation de la vue
        this.controleur = new ControleurFavoris(this, idUsager);
        
        // Charger les données via le contrôleur
        rafraichirAffichage();
    }

    // ------------ Méthodes privées ------------
    // Initialisation de la page
    private void initialiserPage() {
        setTitle("Mes parkings favoris");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Création du panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COULEUR_FOND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Ajout des composants au panel principal
        mainPanel.add(creerHeader(), BorderLayout.NORTH);
        mainPanel.add(creerPanelResultats(), BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    // Création de l'en-tête de la page
    private JPanel creerHeader() {
        // Panel de l'en-tête
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COULEUR_FOND);

        // Bouton de retour
        JButton btnRetour = new JButton("← Retour");
        btnRetour.addActionListener(e -> dispose());

        // Titre de la page
        lblTitre = new JLabel("Mes parkings favoris", SwingConstants.CENTER);
        lblTitre.setFont(POLICE_TITRE);

        // Ajout des composants à l'en-tête
        header.add(btnRetour, BorderLayout.WEST);
        header.add(lblTitre, BorderLayout.CENTER);

        return header;
    }

    // Création du panel des résultats
    private JScrollPane creerPanelResultats() {

        panelResultats = new JPanel();
        panelResultats.setLayout(new BoxLayout(panelResultats, BoxLayout.Y_AXIS));
        panelResultats.setBackground(COULEUR_FOND);

        // Ajout d'un JScrollPane pour permettre le défilement
        JScrollPane scroll = new JScrollPane(panelResultats);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        return scroll;
    }

    /**
     * MÉTHODE PUBLIQUE appelée par le contrôleur pour rafraîchir l'affichage
     */
    public void rafraichirAffichage() {
        // Demander les données AU CONTRÔLEUR
        List<Parking> parkingsFavoris = controleur.chargerParkingsFavoris();
        
        // Vider le panel avant d'ajouter les nouveaux résultats
        panelResultats.removeAll();

        // Vérifier s'il y a des parkings favoris
        if (parkingsFavoris.isEmpty()) {
            afficherAucunFavori();
        } else {
            // Ajouter chaque parking favori au panel des résultats
            for (Parking parking : parkingsFavoris) {
                panelResultats.add(creerCarteParking(parking));
                panelResultats.add(Box.createVerticalStrut(10));
            }
        }

        // Mettre à jour l'affichage
        panelResultats.revalidate();
        panelResultats.repaint();
    }

    // Affichage du message lorsqu'il n'y a aucun parking favori
    private void afficherAucunFavori() {
        
        JLabel lbl = new JLabel("Vous n'avez encore aucun parking en favori.");
        lbl.setFont(new Font("Arial", Font.PLAIN, 16));
        lbl.setForeground(Color.GRAY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Centrer le message verticalement
        panelResultats.add(Box.createVerticalGlue());
        panelResultats.add(lbl);
        panelResultats.add(Box.createVerticalGlue());
    }

    // Création de la carte d'un parking favori
    // Chaque carte contient les informations du parking et les boutons d'action
    private JPanel creerCarteParking(Parking parking) {
        JPanel carte = new JPanel(new BorderLayout());
        carte.setBackground(COULEUR_FOND);
        carte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COULEUR_BORDURE),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        carte.setMaximumSize(DIMENSION_CARTE);

        // Ajouter les informations et les boutons à la carte
        carte.add(creerPanelCoeur(parking), BorderLayout.WEST);
        carte.add(creerPanelInformations(parking), BorderLayout.CENTER);
        carte.add(creerPanelBoutons(parking), BorderLayout.EAST);

        return carte;
    }

    // Création du panel contenant les informations du parking
    private JPanel creerPanelInformations(Parking parking) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COULEUR_FOND);

        JLabel lblNom = new JLabel(parking.getLibelleParking());
        lblNom.setFont(POLICE_NOM);
        lblNom.setForeground(COULEUR_PRIMAIRE);

        JLabel lblAdresse = new JLabel(parking.getAdresseParking());
        lblAdresse.setFont(POLICE_ADRESSE);
        lblAdresse.setForeground(COULEUR_SECONDAIRE);

        JLabel lblPlaces = new JLabel(
                parking.getPlacesDisponibles() + "/" +
                parking.getNombrePlaces() + " places"
        );
        lblPlaces.setFont(POLICE_DETAIL);

        JLabel lblHauteur = new JLabel(
                "Hauteur max : " + parking.getHauteurParking() + " m"
        );
        lblHauteur.setFont(POLICE_DETAIL);

        panel.add(lblNom);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblAdresse);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblPlaces);
        panel.add(lblHauteur);

        return panel;
    }

    // Création du panel contenant les boutons d'action pour le parking
    private JPanel creerPanelBoutons(Parking parking) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COULEUR_FOND);

        JButton btnStationner = new JButton("Stationner");
        btnStationner.setPreferredSize(DIMENSION_BOUTON);
        btnStationner.addActionListener(e -> selectionnerParking(parking));
        
        panel.add(btnStationner);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }
    
    // Création du panel contenant le bouton cœur (favori)
    private JPanel creerPanelCoeur(Parking parking) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COULEUR_FOND);
        panel.setPreferredSize(new Dimension(50, 100));

        JButton btnCoeur = new JButton(redimensionner(COEUR_REMPLI));

        btnCoeur.setBorderPainted(false);
        btnCoeur.setContentAreaFilled(false);
        btnCoeur.setFocusPainted(false);
        btnCoeur.setOpaque(false);
        btnCoeur.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCoeur.setAlignmentX(Component.CENTER_ALIGNMENT);

        // APPEL AU CONTRÔLEUR pour supprimer
        btnCoeur.addActionListener(e -> controleur.supprimerFavori(parking));

        panel.add(btnCoeur);
        panel.add(Box.createVerticalGlue());

        return panel;
    }
    
    // Redimensionner une icône à une taille fixe
    private ImageIcon redimensionner(ImageIcon icon) {
        Image img = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    // Sélectionner un parking pour se garer
    private void selectionnerParking(Parking parking) {
        new Page_Garer_Parking(emailUtilisateur, parking);
        dispose();
    }

    /**
     * Affiche un message d'erreur (appelé par le contrôleur)
     */
    public void afficherErreur(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Erreur",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
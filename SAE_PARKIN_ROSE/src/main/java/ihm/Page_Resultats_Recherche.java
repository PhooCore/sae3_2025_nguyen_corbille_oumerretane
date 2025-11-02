package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import modèle.Parking;
import dao.ParkingDAO;

public class Page_Resultats_Recherche extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private String termeRecherche;
    private List<Parking> parkings;
    private JPanel panelResultats;

    public Page_Resultats_Recherche(String email, String termeRecherche) {
        this.emailUtilisateur = email;
        this.termeRecherche = termeRecherche;
        this.parkings = ParkingDAO.rechercherParkings(termeRecherche);
        initialisePage();
    }
    
    private void initialisePage() {
        this.setTitle("Résultats de recherche - " + termeRecherche);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // === EN-TÊTE ===
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        // Bouton retour
        JButton btnRetour = new JButton("← Retour");
        btnRetour.addActionListener(e -> retourAccueil());
        btnRetour.setBackground(Color.WHITE);
        btnRetour.setFocusPainted(false);
        headerPanel.add(btnRetour, BorderLayout.WEST);
        
        // Titre avec nombre de résultats
        String titre = parkings.size() + " résultat(s) pour \"" + termeRecherche + "\"";
        JLabel lblTitre = new JLabel(titre, SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(lblTitre, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // === RÉSULTATS ===
        panelResultats = new JPanel();
        panelResultats.setLayout(new BoxLayout(panelResultats, BoxLayout.Y_AXIS));
        panelResultats.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(panelResultats);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        afficherResultats();
        
        this.setContentPane(mainPanel);
    }
    
    private void afficherResultats() {
        panelResultats.removeAll();
        
        if (parkings.isEmpty()) {
            // Aucun résultat
            JLabel lblAucunResultat = new JLabel("Aucun parking trouvé pour votre recherche.", SwingConstants.CENTER);
            lblAucunResultat.setFont(new Font("Arial", Font.PLAIN, 16));
            lblAucunResultat.setForeground(Color.GRAY);
            lblAucunResultat.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelResultats.add(lblAucunResultat);
            
            // Suggestion de recherche
            JLabel lblSuggestion = new JLabel("Essayez avec d'autres termes ou consultez tous les parkings.", SwingConstants.CENTER);
            lblSuggestion.setFont(new Font("Arial", Font.PLAIN, 14));
            lblSuggestion.setForeground(Color.GRAY);
            lblSuggestion.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelResultats.add(Box.createRigidArea(new Dimension(0, 10)));
            panelResultats.add(lblSuggestion);
            
            // Bouton pour voir tous les parkings
            JButton btnTousParkings = new JButton("Voir tous les parkings");
            btnTousParkings.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnTousParkings.addActionListener(e -> afficherTousParkings());
            panelResultats.add(Box.createRigidArea(new Dimension(0, 20)));
            panelResultats.add(btnTousParkings);
            
        } else {
            // Affichage des résultats
            for (Parking parking : parkings) {
                panelResultats.add(creerCarteParking(parking));
                panelResultats.add(Box.createRigidArea(new Dimension(0, 10))); // Espacement
            }
        }
        
        panelResultats.revalidate();
        panelResultats.repaint();
    }
    
    private JPanel creerCarteParking(Parking parking) {
        JPanel carte = new JPanel();
        carte.setLayout(new BorderLayout());
        carte.setBackground(Color.WHITE);
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        carte.setMaximumSize(new Dimension(700, 120));
        carte.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Informations du parking
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        
        // Nom du parking
        JLabel lblNom = new JLabel(parking.getLibelleParking());
        lblNom.setFont(new Font("Arial", Font.BOLD, 16));
        lblNom.setForeground(new Color(0, 100, 200));
        
        // Adresse
        JLabel lblAdresse = new JLabel(parking.getAdresseParking());
        lblAdresse.setFont(new Font("Arial", Font.PLAIN, 14));
        lblAdresse.setForeground(Color.DARK_GRAY);
        
        // Détails (places et hauteur)
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        detailsPanel.setBackground(Color.WHITE);
        
        JLabel lblPlaces = new JLabel("Places: " + parking.getPlacesDisponibles() + "/" + parking.getNombrePlaces());
        lblPlaces.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel lblHauteur = new JLabel("Hauteur max: " + parking.getHauteurParking() + "m");
        lblHauteur.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Indicateur parking gratuit
        if (dao.TarifParkingDAO.estParkingGratuit(parking.getIdParking())) {
            JLabel lblGratuit = new JLabel("★ GRATUIT");
            lblGratuit.setFont(new Font("Arial", Font.BOLD, 12));
            lblGratuit.setForeground(Color.GREEN.darker());
            detailsPanel.add(lblGratuit);
        }
        
        // Indicateur tarif soirée
        if (dao.TarifParkingDAO.proposeTarifSoiree(parking.getIdParking())) {
            JLabel lblSoiree = new JLabel("★ Tarif soirée");
            lblSoiree.setFont(new Font("Arial", Font.BOLD, 12));
            lblSoiree.setForeground(Color.ORANGE.darker());
            detailsPanel.add(lblSoiree);
        }
        
        detailsPanel.add(lblPlaces);
        detailsPanel.add(lblHauteur);
        
        infoPanel.add(lblNom);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(lblAdresse);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(detailsPanel);
        
        carte.add(infoPanel, BorderLayout.CENTER);
        
        // Bouton de sélection
        JButton btnSelect = new JButton("Stationner ici");
        btnSelect.setPreferredSize(new Dimension(120, 35));
        btnSelect.addActionListener(e -> selectionnerParking(parking));
        
        carte.add(btnSelect, BorderLayout.EAST);
        
        return carte;
    }
    
    private void selectionnerParking(Parking parking) {
        int choix = JOptionPane.showConfirmDialog(this,
            "Voulez-vous préparer un stationnement pour :\n" +
            parking.getLibelleParking() + "\n" +
            parking.getAdresseParking() + "\n\n" +
            "Places disponibles: " + parking.getPlacesDisponibles() + "/" + parking.getNombrePlaces() + "\n" +
            "Hauteur maximale: " + parking.getHauteurParking() + "m",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
            
        if (choix == JOptionPane.YES_OPTION) {
            // Redirection vers la page de stationnement en parking
            Page_Garer_Parking pageParking = new Page_Garer_Parking();//email et parking
            pageParking.setVisible(true);
            dispose();
        }
    }
    
    private void afficherTousParkings() {
        List<Parking> tousParkings = ParkingDAO.getAllParkings();
        Page_Tous_Parkings pageTousParkings = new Page_Tous_Parkings(emailUtilisateur, tousParkings);
        pageTousParkings.setVisible(true);
        dispose();
    }
    
    private void retourAccueil() {
        Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
        pagePrincipale.setVisible(true);
        dispose();
    }
}
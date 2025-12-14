package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import modele.Parking;
import modele.dao.ParkingDAO;
import modele.dao.TarifParkingDAO;
import controleur.ControleurResultatsRecherche;

public class Page_Resultats_Recherche extends JFrame {
    
    private static final long serialVersionUID = 1L;
    public String emailUtilisateur;
    public String termeRecherche;
    public List<Parking> parkings;
    public List<Parking> parkingsFiltres;
    private JPanel panelResultats;
    
    // Variables rendues publiques pour le contrôleur
    public JComboBox<String> comboFiltres;
    public JCheckBox checkGratuit, checkSoiree, checkRelais, checkMoto;
    public JLabel lblTitre;
    private ControleurResultatsRecherche controleur;

    public Page_Resultats_Recherche(String email, String termeRecherche) {
        this.emailUtilisateur = email;
        this.termeRecherche = termeRecherche;
        this.parkings = ParkingDAO.rechercherParkings(termeRecherche);
        this.parkingsFiltres = new ArrayList<>(parkings);
        initialisePage();
        
        // Créer et lier le contrôleur APRÈS l'initialisation
        controleur = new ControleurResultatsRecherche(this);
    }
    
    private void initialisePage() {
        this.setTitle("Résultats de recherche - " + termeRecherche);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(900, 700);
        this.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        JPanel headerPanel = creerHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        panelResultats = new JPanel();
        panelResultats.setLayout(new BoxLayout(panelResultats, BoxLayout.Y_AXIS));
        panelResultats.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(panelResultats);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Afficher les résultats sans configurer les listeners encore
        afficherResultatsSansConfigurerListeners();
        
        this.setContentPane(mainPanel);
    }
    
    private JPanel creerHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JButton btnRetour = new JButton("← Retour");
        btnRetour.setActionCommand("RETOUR");
        headerPanel.add(btnRetour, BorderLayout.WEST);
        
        String titre = parkingsFiltres.size() + " résultat(s) pour \"" + termeRecherche + "\"";
        lblTitre = new JLabel(titre, SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(lblTitre, BorderLayout.CENTER);
        
        JPanel filtresPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filtresPanel.setBackground(Color.WHITE);
        
        JLabel lblFiltre = new JLabel("Filtrer:");
        checkGratuit = new JCheckBox("Gratuits");
        checkSoiree = new JCheckBox("Tarif soirée");
        checkRelais = new JCheckBox("Parkings relais");
        checkMoto = new JCheckBox("Places moto");
        
        filtresPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        
        JLabel lblTri = new JLabel("Trier par:");
        comboFiltres = new JComboBox<>(new String[]{
            "Pertinence",
            "Places disponibles (décroissant)",
            "Places disponibles (croissant)", 
            "Parkings gratuits",
            "Tarif soirée",
            "Parkings relais",
            "Places moto disponibles",
            "Hauteur (décroissant)",
            "Hauteur (croissant)"
        });
        
        filtresPanel.add(lblFiltre);
        filtresPanel.add(checkGratuit);
        filtresPanel.add(checkSoiree);
        filtresPanel.add(checkRelais);
        filtresPanel.add(checkMoto);
        filtresPanel.add(lblTri);
        filtresPanel.add(comboFiltres);
        
        headerPanel.add(filtresPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    public void appliquerFiltres() {
        String filtreSelectionne = (String) comboFiltres.getSelectedItem();
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
            parkingsFiltres.removeIf(p -> !p.hasMoto() || !p.hasPlacesMotoDisponibles());
        }
        
        switch (filtreSelectionne) {
            case "Places disponibles (décroissant)":
                parkingsFiltres.sort(Comparator.comparingInt(Parking::getPlacesDisponibles).reversed());
                break;
            case "Places disponibles (croissant)":
                parkingsFiltres.sort(Comparator.comparingInt(Parking::getPlacesDisponibles));
                break;
            case "Parkings gratuits":
                parkingsFiltres.sort((p1, p2) -> {
                    boolean g1 = TarifParkingDAO.estParkingGratuit(p1.getIdParking());
                    boolean g2 = TarifParkingDAO.estParkingGratuit(p2.getIdParking());
                    return Boolean.compare(g2, g1);
                });
                break;
            case "Tarif soirée":
                parkingsFiltres.sort((p1, p2) -> Boolean.compare(p2.hasTarifSoiree(), p1.hasTarifSoiree()));
                break;
            case "Parkings relais":
                parkingsFiltres.sort((p1, p2) -> {
                    boolean r1 = TarifParkingDAO.estParkingRelais(p1.getIdParking());
                    boolean r2 = TarifParkingDAO.estParkingRelais(p2.getIdParking());
                    return Boolean.compare(r2, r1);
                });
                break;
            case "Places moto disponibles":
                parkingsFiltres.sort((p1, p2) -> {
                    if (p1.hasMoto() && p2.hasMoto()) {
                        return Integer.compare(p2.getPlacesMotoDisponibles(), p1.getPlacesMotoDisponibles());
                    } else if (p1.hasMoto()) {
                        return -1;
                    } else if (p2.hasMoto()) {
                        return 1;
                    }
                    return 0;
                });
                break;
            case "Hauteur (décroissant)":
                parkingsFiltres.sort(Comparator.comparingDouble(Parking::getHauteurParking).reversed());
                break;
            case "Hauteur (croissant)":
                parkingsFiltres.sort(Comparator.comparingDouble(Parking::getHauteurParking));
                break;
            case "Pertinence":
            default:
                break;
        }
        
        String titre = parkingsFiltres.size() + " résultat(s) pour \"" + termeRecherche + "\"";
        lblTitre.setText(titre);
        
        afficherResultats();
    }
    
    // Version initiale sans reconfiguration des listeners
    private void afficherResultatsSansConfigurerListeners() {
        panelResultats.removeAll();
        
        if (parkingsFiltres.isEmpty()) {
            JLabel lblAucunResultat = new JLabel("Aucun parking trouvé pour votre recherche.", SwingConstants.CENTER);
            lblAucunResultat.setFont(new Font("Arial", Font.PLAIN, 16));
            lblAucunResultat.setForeground(Color.GRAY);
            lblAucunResultat.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelResultats.add(lblAucunResultat);
            
            JLabel lblSuggestion = new JLabel("Essayez avec d'autres termes ou consultez tous les parkings.", SwingConstants.CENTER);
            lblSuggestion.setFont(new Font("Arial", Font.PLAIN, 14));
            lblSuggestion.setForeground(Color.GRAY);
            lblSuggestion.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelResultats.add(Box.createRigidArea(new Dimension(0, 10)));
            panelResultats.add(lblSuggestion);
            
            JButton btnTousParkings = new JButton("Voir tous les parkings");
            btnTousParkings.setActionCommand("TOUS_PARKINGS");
            btnTousParkings.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelResultats.add(Box.createRigidArea(new Dimension(0, 20)));
            panelResultats.add(btnTousParkings);
            
        } else {
            for (int i = 0; i < parkingsFiltres.size(); i++) {
                Parking parking = parkingsFiltres.get(i);
                JPanel carte = creerCarteParking(parking, i);
                panelResultats.add(carte);
                panelResultats.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
        panelResultats.revalidate();
        panelResultats.repaint();
    }
    
    // Version publique avec reconfiguration des listeners
    public void afficherResultats() {
        panelResultats.removeAll();
        
        if (parkingsFiltres.isEmpty()) {
            JLabel lblAucunResultat = new JLabel("Aucun parking trouvé pour votre recherche.", SwingConstants.CENTER);
            lblAucunResultat.setFont(new Font("Arial", Font.PLAIN, 16));
            lblAucunResultat.setForeground(Color.GRAY);
            lblAucunResultat.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelResultats.add(lblAucunResultat);
            
            JLabel lblSuggestion = new JLabel("Essayez avec d'autres termes ou consultez tous les parkings.", SwingConstants.CENTER);
            lblSuggestion.setFont(new Font("Arial", Font.PLAIN, 14));
            lblSuggestion.setForeground(Color.GRAY);
            lblSuggestion.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelResultats.add(Box.createRigidArea(new Dimension(0, 10)));
            panelResultats.add(lblSuggestion);
            
            JButton btnTousParkings = new JButton("Voir tous les parkings");
            btnTousParkings.setActionCommand("TOUS_PARKINGS");
            btnTousParkings.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelResultats.add(Box.createRigidArea(new Dimension(0, 20)));
            panelResultats.add(btnTousParkings);
            
        } else {
            for (int i = 0; i < parkingsFiltres.size(); i++) {
                Parking parking = parkingsFiltres.get(i);
                JPanel carte = creerCarteParking(parking, i);
                panelResultats.add(carte);
                panelResultats.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
        panelResultats.revalidate();
        panelResultats.repaint();
        
        // Reconfigurer les listeners si le contrôleur existe
        if (controleur != null) {
            controleur.configurerListenersApresFiltrage();
        }
    }
    
    private JPanel creerCarteParking(Parking parking, int index) {
        JPanel carte = new JPanel();
        carte.setLayout(new BorderLayout());
        carte.setBackground(Color.WHITE);
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        carte.setMaximumSize(new Dimension(800, 140));
        carte.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        
        JLabel lblNom = new JLabel(parking.getLibelleParking());
        lblNom.setFont(new Font("Arial", Font.BOLD, 16));
        lblNom.setForeground(new Color(0, 100, 200));
        
        JLabel lblAdresse = new JLabel(parking.getAdresseParking());
        lblAdresse.setFont(new Font("Arial", Font.PLAIN, 14));
        lblAdresse.setForeground(Color.DARK_GRAY);
        
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        detailsPanel.setBackground(Color.WHITE);
        
        JLabel lblPlaces = new JLabel(parking.getPlacesDisponibles() + "/" + parking.getNombrePlaces() + " places");
        lblPlaces.setFont(new Font("Arial", Font.PLAIN, 12));
        
        if (parking.hasMoto()) {
            JLabel lblMoto = new JLabel(""+parking.getPlacesMotoDisponibles() + "/" + parking.getPlacesMoto() + " places moto");
            lblMoto.setFont(new Font("Arial", Font.PLAIN, 12));
            lblMoto.setForeground(new Color(100, 100, 100));
            detailsPanel.add(lblMoto);
        }
        
        JLabel lblHauteur = new JLabel(parking.getHauteurParking() + "m");
        lblHauteur.setFont(new Font("Arial", Font.PLAIN, 12));
        
        if (TarifParkingDAO.estParkingGratuit(parking.getIdParking())) {
            JLabel lblGratuit = new JLabel("☑ GRATUIT");
            lblGratuit.setFont(new Font("Arial", Font.BOLD, 12));
            lblGratuit.setForeground(Color.GREEN.darker());
            detailsPanel.add(lblGratuit);
        }
        
        if (parking.hasTarifSoiree()) {
            JLabel lblSoiree = new JLabel("☽ Tarif soirée");
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
        
        JButton btnSelect = new JButton("Stationner ici");
        btnSelect.setActionCommand("STATIONNER_" + index);
        btnSelect.setPreferredSize(new Dimension(120, 35));
        
        carte.add(btnSelect, BorderLayout.EAST);
        
        return carte;
    }
}
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

public class Page_Resultats_Recherche extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private String termeRecherche;
    private List<Parking> parkings;
    private List<Parking> parkingsFiltres;
    private JPanel panelResultats;
    private JComboBox<String> comboFiltres;
    private JCheckBox checkGratuit, checkSoiree, checkRelais, checkMoto;

    public Page_Resultats_Recherche(String email, String termeRecherche) {
        this.emailUtilisateur = email;
        this.termeRecherche = termeRecherche;
        this.parkings = ParkingDAO.rechercherParkings(termeRecherche);
        this.parkingsFiltres = new ArrayList<>(parkings);
        initialisePage();
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
        
        afficherResultats();
        
        this.setContentPane(mainPanel);
    }
    
    private JPanel creerHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JButton btnRetour = new JButton("← Retour");
        btnRetour.addActionListener(e -> retourAccueil());
        btnRetour.setBackground(Color.WHITE);
        btnRetour.setFocusPainted(false);
        headerPanel.add(btnRetour, BorderLayout.WEST);
        
        String titre = parkingsFiltres.size() + " résultat(s) pour \"" + termeRecherche + "\"";
        JLabel lblTitre = new JLabel(titre, SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(lblTitre, BorderLayout.CENTER);
        
        JPanel filtresPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filtresPanel.setBackground(Color.WHITE);
        
        JLabel lblFiltre = new JLabel("Filtrer:");
        checkGratuit = new JCheckBox("Gratuits");
        checkSoiree = new JCheckBox("Tarif soirée");
        checkRelais = new JCheckBox("Parkings relais");
        checkMoto = new JCheckBox("Places moto");
        
        checkGratuit.addActionListener(e -> appliquerFiltres());
        checkSoiree.addActionListener(e -> appliquerFiltres());
        checkRelais.addActionListener(e -> appliquerFiltres());
        checkMoto.addActionListener(e -> appliquerFiltres());
        
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
        comboFiltres.addActionListener(e -> appliquerFiltres());
        
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
    
    private void appliquerFiltres() {
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
        ((JLabel)((JPanel)getContentPane().getComponent(0)).getComponent(1)).setText(titre);
        
        afficherResultats();
    }
    
    private void afficherResultats() {
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
            btnTousParkings.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnTousParkings.addActionListener(e -> afficherTousParkings());
            panelResultats.add(Box.createRigidArea(new Dimension(0, 20)));
            panelResultats.add(btnTousParkings);
            
        } else {
            for (Parking parking : parkingsFiltres) {
                panelResultats.add(creerCarteParking(parking));
                panelResultats.add(Box.createRigidArea(new Dimension(0, 10)));
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
            "Places voiture: " + parking.getPlacesDisponibles() + "/" + parking.getNombrePlaces() + "\n" +
            (parking.hasMoto() ? "Places moto: " + parking.getPlacesMotoDisponibles() + "/" + parking.getPlacesMoto() + "\n" : "") +
            "Hauteur maximale: " + parking.getHauteurParking() + "m",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
            
        if (choix == JOptionPane.YES_OPTION) {
            Page_Garer_Parking pageParking = new Page_Garer_Parking(emailUtilisateur);
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
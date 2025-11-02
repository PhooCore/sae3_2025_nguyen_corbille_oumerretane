package ihm;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import modèle.Parking;
import dao.ParkingDAO;
import dao.TarifParkingDAO;

public class Page_Tous_Parkings extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private List<Parking> parkings;
    private JPanel panelParkings;

    public Page_Tous_Parkings(String email, List<Parking> parkings) {
        this.emailUtilisateur = email;
        this.parkings = parkings;
        initialisePage();
    }
    
    private void initialisePage() {
        this.setTitle("Tous les parkings");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // En-tête
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JButton btnRetour = new JButton("← Retour");
        btnRetour.addActionListener(e -> retourAccueil());
        btnRetour.setBackground(Color.WHITE);
        btnRetour.setFocusPainted(false);
        headerPanel.add(btnRetour, BorderLayout.WEST);
        
        JLabel lblTitre = new JLabel("Tous les parkings (" + parkings.size() + ")", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(lblTitre, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Liste des parkings
        panelParkings = new JPanel();
        panelParkings.setLayout(new BoxLayout(panelParkings, BoxLayout.Y_AXIS));
        panelParkings.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(panelParkings);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        afficherParkings();
        
        this.setContentPane(mainPanel);
    }
    
    private void afficherParkings() {
        panelParkings.removeAll();
        
        if (parkings.isEmpty()) {
            JLabel lblAucun = new JLabel("Aucun parking disponible", SwingConstants.CENTER);
            lblAucun.setFont(new Font("Arial", Font.PLAIN, 16));
            lblAucun.setForeground(Color.GRAY);
            lblAucun.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelParkings.add(lblAucun);
        } else {
            for (Parking parking : parkings) {
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
        if (TarifParkingDAO.estParkingGratuit(parking.getIdParking())) {
            JLabel lblGratuit = new JLabel("★ GRATUIT");
            lblGratuit.setFont(new Font("Arial", Font.BOLD, 12));
            lblGratuit.setForeground(Color.GREEN.darker());
            detailsPanel.add(lblGratuit);
        }
        
        // Indicateur tarif soirée
        if (TarifParkingDAO.proposeTarifSoiree(parking.getIdParking())) {
            JLabel lblSoiree = new JLabel("★ Tarif soirée");
            lblSoiree.setFont(new Font("Arial", Font.BOLD, 12));
            lblSoiree.setForeground(Color.ORANGE.darker());
            detailsPanel.add(lblSoiree);
        }
        
        // Indicateur parking relais
        if (TarifParkingDAO.estParkingRelais(parking.getIdParking())) {
            JLabel lblRelais = new JLabel("★ Parking relais");
            lblRelais.setFont(new Font("Arial", Font.BOLD, 12));
            lblRelais.setForeground(Color.BLUE.darker());
            detailsPanel.add(lblRelais);
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
            Page_Garer_Parking pageParking = new Page_Garer_Parking();//email et utilisateur
            pageParking.setVisible(true);
            dispose();
        }
    }
    
    private void retourAccueil() {
        Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
        pagePrincipale.setVisible(true);
        dispose();
    }
}
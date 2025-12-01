package ihm;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

import modele.Stationnement;
import modele.Usager;
import modele.dao.StationnementDAO;
import modele.dao.UsagerDAO;


public class Page_Historique_Stationnements extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;  // Email de l'utilisateur connecté
    private Usager usager;            // Objet utilisateur contenant les informations personnelles

    /**
     * Constructeur de la page d'historique des stationnements
     * @param email l'email de l'utilisateur connecté
     */
    public Page_Historique_Stationnements(String email) {
        this.emailUtilisateur = email;
        // Récupération des informations utilisateur depuis la base de données
        this.usager = UsagerDAO.getUsagerByEmail(email);
        initialisePage();  
    }
    
    /**
     * Initialise l'interface utilisateur de la page d'historique
     * Structure : En-tête + Tableau des stationnements + Statistiques
     */
    private void initialisePage() {
        // Configuration de la fenêtre
        this.setTitle("Historique des stationnements");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 600);  
        this.setLocationRelativeTo(null);  
        
        // Panel principal avec bordures
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);  
        
        //EN-TÊTE DE LA PAGE
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        // Bouton de retour vers la page profil
        JButton btnRetour = new JButton("← Retour");
        btnRetour.addActionListener(e -> retourProfil());  
        headerPanel.add(btnRetour, BorderLayout.WEST);  
        
        // Titre centré de la page
        JLabel lblTitre = new JLabel("Historique de vos stationnements", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18)); 
        headerPanel.add(lblTitre, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);  
        
        //TABLEAU DES STATIONNEMENTS
        
        // Récupération de l'historique des stationnements depuis la base de données
        List<Stationnement> stationnements = StationnementDAO.getHistoriqueStationnements(usager.getIdUsager());
        
        // Définition des colonnes du tableau
        String[] colonnes = {"Date", "Type", "Véhicule", "Zone/Parking", "Durée", "Coût", "Statut"};
        Object[][] donnees = new Object[stationnements.size()][7];  // 7 colonnes
        
        // Formatage des dates pour l'affichage
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        // Remplissage du tableau avec les données des stationnements
        for (int i = 0; i < stationnements.size(); i++) {
            Stationnement s = stationnements.get(i);
            
            // Colonne 1: Date de création formatée
            donnees[i][0] = s.getDateCreation().format(formatter);
            
            // Colonne 2: Type de stationnement (Voirie ou Parking)
            donnees[i][1] = s.getTypeStationnement();
            
            // Colonne 3: Véhicule (type + plaque)
            donnees[i][2] = s.getTypeVehicule() + " - " + s.getPlaqueImmatriculation();
            
            // Colonne 4: Zone ou nom du parking
            donnees[i][3] = s.getZone();
            
            // Colonne 5: Durée du stationnement
            if (s.estVoirie()) {
                // Pour la voirie : durée planifiée
                donnees[i][4] = s.getDureeHeures() + "h" + s.getDureeMinutes() + "min";
            } else {
                // Pour les parkings : durée réelle calculée
                if (s.getHeureArrivee() != null && s.getHeureDepart() != null) {
                    // Calcul de la durée réelle entre arrivée et départ
                    long minutes = java.time.Duration.between(s.getHeureArrivee(), s.getHeureDepart()).toMinutes();
                    long heures = minutes / 60;
                    long mins = minutes % 60;
                    donnees[i][4] = heures + "h" + mins + "min";
                } else {
                    // Stationnement encore en cours
                    donnees[i][4] = "En cours";
                }
            }
            
            // Colonne 6: Coût formaté avec 2 décimales
            donnees[i][5] = String.format("%.2f €", s.getCout());
            
            // Colonne 7: Statut (ACTIF, TERMINE, EXPIRE)
            donnees[i][6] = s.getStatut();
        }
        
        // Création du tableau avec les données
        JTable table = new JTable(donnees, colonnes);
        table.setFont(new Font("Arial", Font.PLAIN, 12));  
        table.setRowHeight(25);  // Hauteur des lignes
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12)); 
        table.setDefaultEditor(Object.class, null);
        
        // Ajout du tableau dans un scroll pane pour le défilement
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);  
        
        //PANEL DES STATISTIQUES
        JPanel statsPanel = new JPanel(new FlowLayout());
        statsPanel.setBackground(Color.WHITE);
        
        // Calcul des statistiques
        long totalStationnements = stationnements.size();  
        long stationnementsActifs = stationnements.stream()
                .filter(s -> "ACTIF".equals(s.getStatut()))
                .count();
        
        // Affichage des statistiques
        JLabel lblStats = new JLabel("Total: " + totalStationnements + " stationnement(s) | Actifs: " + stationnementsActifs);
        lblStats.setFont(new Font("Arial", Font.BOLD, 14));
        statsPanel.add(lblStats);
        
        mainPanel.add(statsPanel, BorderLayout.SOUTH);
        
        // Définition du panel principal comme contenu de la fenêtre
        this.setContentPane(mainPanel);
    }
    
    /**
     * Retourne à la page du profil utilisateur
     * Ferme la page actuelle et ouvre la page utilisateur
     */
    private void retourProfil() {
        Page_Utilisateur pageUtilisateur = new Page_Utilisateur(emailUtilisateur);
        pageUtilisateur.setVisible(true);  // Affiche la page profil
        dispose();  // Ferme la page actuelle
    }
}
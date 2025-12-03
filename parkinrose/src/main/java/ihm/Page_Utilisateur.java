package ihm;

import javax.swing.*;

import controleur.UtilisateurControleur;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import modele.Usager;
import modele.Zone;
import modele.dao.PaiementDAO;
import modele.dao.ParkingDAO;
import modele.dao.StationnementDAO;
import modele.dao.UsagerDAO;
import modele.dao.ZoneDAO;
import modele.Paiement;
import modele.Parking;
import modele.Stationnement;
import java.util.List;

/**
 * Page de gestion du compte utilisateur
 * Présente trois onglets : Informations personnelles, Historique des paiements, Historique des stationnements
 */
public class Page_Utilisateur extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;  // Email de l'utilisateur connecté
    private Usager usager;            // Objet utilisateur contenant les informations personnelles
    private UtilisateurControleur controleur;
    /**
     * Constructeur de la page utilisateur
     * @param email l'email de l'utilisateur connecté
     * @param rafraichir si true, rafraîchit les données depuis la base
     */
    public Page_Utilisateur(String email, boolean rafraichir) {
        this.emailUtilisateur = email;
        if (rafraichir) {
            // Forcer le rafraîchissement des données depuis la base
            this.usager = UsagerDAO.getUsagerByEmail(email);
        } else {
            this.usager = UsagerDAO.getUsagerByEmail(email);
        }
        this.controleur = new UtilisateurControleur(email);
        initialisePage();
    }
    /**
     * Constructeur par défaut (pour compatibilité)
     * @param email l'email de l'utilisateur connecté
     */
    public Page_Utilisateur(String email) {
        this(email, false);
    }
    
    /**
     * Initialise l'interface utilisateur de la page
     * Structure : Titre + Système d'onglets + Bouton retour
     */
    private void initialisePage() {
        // Configuration de la fenêtre
        this.setTitle("Mon Compte");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Ferme seulement cette fenêtre
        this.setSize(700, 600); // Taille adaptée pour afficher les tableaux
        this.setLocationRelativeTo(null); // Centre la fenêtre
        
        // Panel principal avec bordures
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Titre de la page
        JLabel lblTitre = new JLabel("Mon Compte", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // === SYSTÈME D'ONGLETS ===
        JTabbedPane onglets = new JTabbedPane();
        onglets.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Onglet 1 : Informations personnelles
        JPanel panelInfos = creerOngletInfos();
        onglets.addTab("Informations", panelInfos);
        
        // Onglet 2 : Historique des paiements
        JPanel panelHistorique = creerOngletHistorique();
        onglets.addTab("Historique des paiements", panelHistorique);
        
        // Onglet 3 : Historique des stationnements
        JPanel panelStationnements = creerOngletStationnements();
        onglets.addTab("Historique des stationnements", panelStationnements);
        
        mainPanel.add(onglets, BorderLayout.CENTER);
        
        // === BOUTON RETOUR ===
        JButton btnRetour = new JButton("Retour à l'accueil");
        btnRetour.addActionListener(e -> retourAccueil());
        mainPanel.add(btnRetour, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
    }
    
    /**
     * Crée l'onglet des informations personnelles
     * @return JPanel configuré pour l'onglet Informations
     */
    private JPanel creerOngletInfos() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // === AFFICHAGE DES INFORMATIONS PERSONNELLES (lecture seule) ===
        ajouterLigneInfo(panel, "Nom:", usager.getNomUsager());
        ajouterLigneInfo(panel, "Prénom:", usager.getPrenomUsager());
        ajouterLigneInfo(panel, "Email:", usager.getMailUsager());
        
        panel.add(Box.createVerticalStrut(30)); 
        
        // === BOUTONS D'ACTION ===
        JButton btnModifierMdp = new JButton("Modifier le mot de passe");
        btnModifierMdp.setAlignmentX(Component.CENTER_ALIGNMENT); 
        btnModifierMdp.addActionListener(e -> controleur.redirigerVersModificationMDP(Page_Utilisateur.this));

        JButton btnHistorique = new JButton("Voir l'historique des stationnements");
        btnHistorique.setAlignmentX(Component.CENTER_ALIGNMENT); 
        btnHistorique.addActionListener(e -> controleur.redirigerVersHistoriqueStationnements(Page_Utilisateur.this));

        JButton btnDeconnexion = new JButton("Déconnexion");
        btnDeconnexion.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDeconnexion.setBackground(new Color(220, 80, 80));
        btnDeconnexion.setForeground(Color.WHITE);
        btnDeconnexion.addActionListener(e -> controleur.deconnecterUtilisateur(Page_Utilisateur.this));
        
        // AJOUT DES BOUTONS AU PANEL
        panel.add(btnModifierMdp);
        panel.add(Box.createVerticalStrut(10)); // Espacement entre boutons
        panel.add(btnHistorique);
        panel.add(Box.createVerticalStrut(10)); // Espacement entre boutons
        panel.add(btnDeconnexion);
        
        return panel;
    }
    /**
     * Crée l'onglet de l'historique des paiements
     * @return JPanel configuré pour l'onglet Historique des paiements
     */
    private JPanel creerOngletHistorique() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // === RÉCUPÉRATION DES DONNÉES RÉELLES (toujours fraîches) ===
        // Récupère tous les paiements de l'utilisateur depuis la base de données
        List<Paiement> paiements = PaiementDAO.getPaiementsByUsager(usager.getIdUsager());
        
        // En-têtes des colonnes du tableau
        String[] colonnes = {"Date", "Montant", "Type", "Statut", "ID Paiement"};
        
        // Conversion des objets Paiement en données pour le tableau
        Object[][] donnees = new Object[paiements.size()][5];
        double totalDepense = 0.0; // Variable pour calculer le total dépensé
        
        for (int i = 0; i < paiements.size(); i++) {
            Paiement p = paiements.get(i);
            // Formatage des données pour chaque colonne
            donnees[i][0] = p.getDatePaiement().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            donnees[i][1] = String.format("%.2f €", p.getMontant());
            donnees[i][2] = p.getTypePaiement();
            donnees[i][3] = "Payé"; // Statut fixe pour l'instant
            donnees[i][4] = p.getIdPaiement();
            totalDepense += p.getMontant(); // Calcul du total dépensé
        }
        
        // === CRÉATION DU TABLEAU ===
        JTable table = new JTable(donnees, colonnes);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25); // Hauteur des lignes
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12)); 
        
        // Empêcher l'édition des cellules (données en lecture seule)
        table.setDefaultEditor(Object.class, null);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // === PANEL DE RÉSUMÉ STATISTIQUES ===
        JPanel panelResume = new JPanel(new GridLayout(1, 3, 10, 0)); // 3 colonnes, espacement 10px
        panelResume.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelResume.setBackground(Color.WHITE);
        
        // Calcul de la date du dernier paiement
        String dernierPaiement = "Aucun";
        if (!paiements.isEmpty()) {
            // Le premier paiement de la liste est le plus récent (trié par ordre décroissant)
            dernierPaiement = paiements.get(0).getDatePaiement().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        
        // Ajout des statistiques
        ajouterStatistique(panelResume, "Total dépensé", String.format("%.2f €", totalDepense));
        ajouterStatistique(panelResume, "Nombre de paiements", String.valueOf(paiements.size()));
        ajouterStatistique(panelResume, "Dernier paiement", dernierPaiement);
        
        panel.add(panelResume, BorderLayout.SOUTH);
        
        return panel;
    }
    /**
     * Crée l'onglet de l'historique des stationnements
     * @return JPanel configuré pour l'onglet Historique des stationnements
     */
    private JPanel creerOngletStationnements() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Récupération de l'historique des stationnements (toujours frais)
        List<Stationnement> stationnements = StationnementDAO.getHistoriqueStationnements(usager.getIdUsager());
        
        // En-têtes des colonnes du tableau
        String[] colonnes = {"Date", "Type", "Véhicule", "Zone/Parking", "Durée", "Coût", "Statut"};
        Object[][] donnees = new Object[stationnements.size()][7];
        
        // Remplissage du tableau avec les données des stationnements
        for (int i = 0; i < stationnements.size(); i++) {
            Stationnement s = stationnements.get(i);
            
            // Colonne 1: Date de création formatée
            donnees[i][0] = s.getDateCreation().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
            // Colonne 2: Type de stationnement (Voirie ou Parking)
            donnees[i][1] = s.getTypeStationnement();
            
            // Colonne 3: Véhicule (type + plaque)
            donnees[i][2] = s.getTypeVehicule() + " - " + s.getPlaqueImmatriculation();
            
         // Colonne 4: Zone ou nom du parking - VERSION CORRECTE
            String zoneId = s.getIdTarification();

            if (zoneId == null || zoneId.trim().isEmpty()) {
                donnees[i][3] = "Non spécifié";
            } else {
                if ("PARKING".equals(s.getTypeStationnement())) {
                    // Récupérer le parking et utiliser son libellé directement
                    Parking parking = ParkingDAO.getParkingById(zoneId);
                    donnees[i][3] = (parking != null) ? parking.getLibelleParking() : zoneId;
                } else {
                    // Pour la voirie
                    Zone zone = ZoneDAO.getZoneById(zoneId);
                    donnees[i][3] = (zone != null) ? zone.getLibelleZone() : zoneId;
                }
            }
            // Colonne 5: Durée du stationnement (calcul différencié)
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
        
        // Création du tableau
        JTable table = new JTable(donnees, colonnes);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setDefaultEditor(Object.class, null); // Tableau non éditable
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // === PANEL DES STATISTIQUES ===
        JPanel statsPanel = new JPanel(new FlowLayout());
        statsPanel.setBackground(Color.WHITE);
        
        // Calcul des statistiques
        long totalStationnements = stationnements.size(); // Nombre total
        long stationnementsActifs = stationnements.stream()
                .filter(s -> "ACTIF".equals(s.getStatut()))
                .count();
        
        // Affichage des statistiques
        JLabel lblStats = new JLabel("Total: " + totalStationnements + " stationnement(s) | Actifs: " + stationnementsActifs);
        lblStats.setFont(new Font("Arial", Font.BOLD, 14));
        statsPanel.add(lblStats);
        
        panel.add(statsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Ajoute une ligne d'information dans un panel
     * Format : Libellé (gras) à gauche, Valeur (normal) à droite
     * @param panel le panel parent où ajouter la ligne
     * @param libelle le texte du libellé
     * @param valeur le texte de la valeur
     */
    private void ajouterLigneInfo(JPanel panel, String libelle, String valeur) {
        JPanel ligne = new JPanel(new BorderLayout());
        ligne.setBackground(Color.WHITE);
        ligne.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JLabel lblLibelle = new JLabel(libelle);
        lblLibelle.setFont(new Font("Arial", Font.BOLD, 14)); 
        lblLibelle.setPreferredSize(new Dimension(100, 25)); 
        
        JLabel lblValeur = new JLabel(valeur);
        lblValeur.setFont(new Font("Arial", Font.PLAIN, 14)); 
        
        ligne.add(lblLibelle, BorderLayout.WEST);
        ligne.add(lblValeur, BorderLayout.CENTER);
        
        panel.add(ligne);
    }
    
    /**
     * Ajoute un élément de statistique dans un panel
     * @param panel le panel parent où ajouter la statistique
     * @param libelle le libellé de la statistique
     * @param valeur la valeur de la statistique
     */
    private void ajouterStatistique(JPanel panel, String libelle, String valeur) {
        JPanel statPanel = new JPanel();
        statPanel.setLayout(new BoxLayout(statPanel, BoxLayout.Y_AXIS)); // Layout vertical
        statPanel.setBackground(Color.WHITE);
        statPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // Bordure grise
        
        JLabel lblLibelle = new JLabel(libelle, SwingConstants.CENTER);
        lblLibelle.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel lblValeur = new JLabel(valeur, SwingConstants.CENTER);
        lblValeur.setFont(new Font("Arial", Font.BOLD, 16)); // Valeur en gras et plus gros
        
        // Espacement et centrage
        statPanel.add(Box.createVerticalStrut(10));
        statPanel.add(lblLibelle);
        statPanel.add(lblValeur);
        statPanel.add(Box.createVerticalStrut(10));
        
        panel.add(statPanel);
    }
    
    /**
     * Retourne les avantages selon le type d'abonnement
     */
    private String getAvantagesByType(String idAbonnement) {
        switch(idAbonnement.toUpperCase()) {
            case "ABN_BASIC":
                return "• Stationnement illimité en voirie (2h max)\n" +
                       "• 10% de réduction dans les parkings partenaires\n" +
                       "• Accès aux zones bleues";
            case "ABN_PREMIUM":
                return "• Stationnement illimité en voirie\n" +
                       "• 25% de réduction dans les parkings partenaires\n" +
                       "• Accès à toutes les zones\n" +
                       "• Réservation prioritaire";
            case "ABN_ETUDIANT":
                return "• 50% de réduction sur tous les stationnements\n" +
                       "• Accès aux zones universitaires\n" +
                       "• Valable uniquement avec carte étudiante";
            case "ABN_SENIOR":
                return "• 40% de réduction sur tous les stationnements\n" +
                       "• Accès aux zones résidentielles\n" +
                       "• Pour les 65 ans et plus";
            default:
                return "• Avantages personnalisés\n" +
                       "• Contactez-nous pour plus d'informations";
        }
    }
    
    /**
     * Retourne à la page principale
     */
    private void retourAccueil() {
        Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
        pagePrincipale.setVisible(true);
        dispose(); // Ferme la page actuelle
    }
    
    /**
     * Point d'entrée de l'application (méthode main)
     * Lance l'application avec la page de bienvenue
     */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Page_Bienvenue().setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import dao.UsagerDAO;
import dao.PaiementDAO;
import dao.StationnementDAO;
import modèle.Usager;
import modèle.Paiement;
import modèle.Stationnement;
import java.util.List;

/**
 * Page de gestion du compte utilisateur
 * Présente trois onglets : Informations personnelles, Historique des paiements, Historique des stationnements
 */
public class Page_Utilisateur extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;  // Email de l'utilisateur connecté
    private Usager usager;            // Objet utilisateur contenant les informations personnelles

    /**
     * Constructeur de la page utilisateur
     * @param email l'email de l'utilisateur connecté
     */
    public Page_Utilisateur(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email); // Récupération des données utilisateur
        initialisePage(); // Initialisation de l'interface
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
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Marges
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
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Layout vertical
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // === AFFICHAGE DES INFORMATIONS PERSONNELLES (lecture seule) ===
        ajouterLigneInfo(panel, "Nom:", usager.getNomUsager());
        ajouterLigneInfo(panel, "Prénom:", usager.getPrenomUsager());
        ajouterLigneInfo(panel, "Email:", usager.getMailUsager());
        
        panel.add(Box.createVerticalStrut(30)); // Espacement important
        
        // === BOUTONS D'ACTION ===
        JButton btnModifierMdp = new JButton("Modifier le mot de passe");
        btnModifierMdp.addActionListener(e -> modifierMotDePasse());
        
        JButton btnHistorique = new JButton("Voir l'historique des stationnements");
        btnHistorique.addActionListener(e -> {
            // Ouverture de la page dédiée à l'historique des stationnements
            Page_Historique_Stationnements pageHistorique = new Page_Historique_Stationnements(emailUtilisateur);
            pageHistorique.setVisible(true);
        });
        
        JButton btnDeconnexion = new JButton("Déconnexion");
        btnDeconnexion.setBackground(new Color(220, 80, 80)); // Rouge
        btnDeconnexion.setForeground(Color.WHITE);
        btnDeconnexion.addActionListener(e -> deconnexion());
        
        // Ajout des boutons avec espacement
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
        
        // === RÉCUPÉRATION DES DONNÉES RÉELLES ===
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
            donnees[i][2] = "Stationnement"; // Type fixe pour l'instant
            donnees[i][3] = "Payé"; // Statut fixe pour l'instant
            donnees[i][4] = p.getIdPaiement();
            totalDepense += p.getMontant(); // Calcul du total dépensé
        }
        
        // === CRÉATION DU TABLEAU ===
        JTable table = new JTable(donnees, colonnes);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25); // Hauteur des lignes
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12)); // En-tête en gras
        
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
        
        // Récupération de l'historique des stationnements
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
            
            // Colonne 4: Zone ou nom du parking
            donnees[i][3] = s.getZone();
            
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
        // Comptage des stationnements actifs avec stream Java 8
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
        ligne.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Espacement vertical
        
        JLabel lblLibelle = new JLabel(libelle);
        lblLibelle.setFont(new Font("Arial", Font.BOLD, 14)); // Texte en gras
        lblLibelle.setPreferredSize(new Dimension(100, 25)); // Largeur fixe pour l'alignement
        
        JLabel lblValeur = new JLabel(valeur);
        lblValeur.setFont(new Font("Arial", Font.PLAIN, 14)); // Texte normal
        
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
     * Ouvre la page de modification de mot de passe
     */
    private void modifierMotDePasse() {
        Page_Modif_MDP pageModifMdp = new Page_Modif_MDP();
        pageModifMdp.setVisible(true);
        this.dispose(); // Ferme la page actuelle
    }
    
    /**
     * Gère la déconnexion de l'utilisateur
     * Demande confirmation puis retourne à la page d'authentification
     */
    private void deconnexion() {
        int confirmation = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir vous déconnecter ?",
            "Déconnexion",
            JOptionPane.YES_NO_OPTION);
            
        if (confirmation == JOptionPane.YES_OPTION) {
            // Retour à la page de connexion
            Page_Authentification authPage = new Page_Authentification();
            authPage.setVisible(true);
            dispose(); // Ferme la page actuelle
        }
        // Si NON, ne rien faire (reste sur la page)
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
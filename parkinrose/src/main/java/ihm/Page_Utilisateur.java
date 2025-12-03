package ihm;

import javax.swing.*;

import controleur.UtilisateurControleur;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import modele.Abonnement;
import modele.Usager;
import modele.Zone;
import modele.dao.AbonnementDAO;
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
 * Présente quatre onglets : Informations personnelles, Mon Abonnement, Historique des paiements, Historique des stationnements
 */
public class Page_Utilisateur extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;  // Email de l'utilisateur connecté
    private Usager usager;            // Objet utilisateur contenant les informations personnelles
    private UtilisateurControleur controleur;
    
    /**
     * Constructeur de la page utilisateur
     * @param email l'email de l'utilisateur connecté
     */
    public Page_Utilisateur(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        this.controleur = new UtilisateurControleur(email);
        initialisePage();
    }
    
    /**
     * Initialise l'interface utilisateur de la page
     * Structure : Titre + Système d'onglets + Bouton retour
     */
    private void initialisePage() {
        // Configuration de la fenêtre
        this.setTitle("Mon Compte");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Ferme seulement cette fenêtre
        this.setSize(800, 650); // Taille augmentée pour accommoder le nouvel onglet
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
        
        // Onglet 2 : Mon Abonnement (NOUVEL ONGLET AJOUTÉ)
        JPanel panelAbonnement = creerOngletAbonnement();
        onglets.addTab("Mon Abonnement", panelAbonnement);
        
        // Onglet 3 : Historique des paiements
        JPanel panelHistorique = creerOngletHistorique();
        onglets.addTab("Historique des paiements", panelHistorique);
        
        // Onglet 4 : Historique des stationnements
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
        
        panel.add(Box.createVerticalStrut(20));
        
        // === NOUVEAU : SECTION ABONNEMENT DANS L'ONGLET INFORMATIONS ===
        JPanel panelAbonnementInfo = new JPanel(new BorderLayout());
        panelAbonnementInfo.setBackground(new Color(245, 245, 245));
        panelAbonnementInfo.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 204), 2),
            "Statut de l'abonnement"
        ));
        
        // Récupérer l'abonnement actuel
        List<Abonnement> abonnements = AbonnementDAO.getAbonnementsByUsager(usager.getIdUsager());
        
        JLabel lblStatutAbonnement = new JLabel();
        lblStatutAbonnement.setFont(new Font("Arial", Font.BOLD, 14));
        lblStatutAbonnement.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton btnActionAbonnement = new JButton();
        btnActionAbonnement.setFont(new Font("Arial", Font.PLAIN, 12));
        
        if (abonnements.isEmpty()) {
            // Pas d'abonnement actif
            lblStatutAbonnement.setText("Aucun abonnement actif");
            lblStatutAbonnement.setForeground(Color.RED);
            
            btnActionAbonnement.setText("Souscrire à un abonnement");
            btnActionAbonnement.setBackground(new Color(0, 102, 204));
            btnActionAbonnement.setForeground(Color.WHITE);
            btnActionAbonnement.addActionListener(e -> {
                Page_Abonnements pageAbonnements = new Page_Abonnements(emailUtilisateur);
                pageAbonnements.setVisible(true);
                dispose();
            });
        } else {
            // Afficher l'abonnement actif
            Abonnement abonnementActuel = abonnements.get(0);
            lblStatutAbonnement.setText("Abonnement actif : " + abonnementActuel.getLibelleAbonnement());
            lblStatutAbonnement.setForeground(new Color(0, 150, 0));
            
            btnActionAbonnement.setText("Gérer mon abonnement");
            btnActionAbonnement.setBackground(new Color(255, 153, 0));
            btnActionAbonnement.setForeground(Color.WHITE);
            btnActionAbonnement.addActionListener(e -> {
                // Basculer sur l'onglet "Mon Abonnement"
                JTabbedPane parent = (JTabbedPane) panel.getParent().getParent().getParent();
                parent.setSelectedIndex(1); // Index de l'onglet "Mon Abonnement"
            });
        }
        
        panelAbonnementInfo.add(lblStatutAbonnement, BorderLayout.CENTER);
        panelAbonnementInfo.add(btnActionAbonnement, BorderLayout.SOUTH);
        
        panel.add(panelAbonnementInfo);
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
     * NOUVELLE MÉTHODE : Crée l'onglet pour gérer l'abonnement de l'utilisateur
     * @return JPanel configuré pour l'onglet Abonnement
     */
    private JPanel creerOngletAbonnement() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Récupérer l'abonnement actuel de l'utilisateur
        List<Abonnement> abonnements = AbonnementDAO.getAbonnementsByUsager(usager.getIdUsager());
        
        if (abonnements.isEmpty()) {
            // Afficher un message si aucun abonnement
            JPanel panelSansAbonnement = new JPanel(new BorderLayout());
            panelSansAbonnement.setBackground(Color.WHITE);
            
            JLabel lblMessage = new JLabel(
                "<html><div style='text-align: center;'>" +
                "<h3>Vous n'avez pas d'abonnement actif</h3>" +
                "<p>Profitez de nos offres spéciales pour bénéficier d'avantages exclusifs !</p>" +
                "</div></html>",
                SwingConstants.CENTER
            );
            lblMessage.setFont(new Font("Arial", Font.PLAIN, 14));
            
            JButton btnVoirAbonnements = new JButton("Voir les abonnements disponibles");
            btnVoirAbonnements.setFont(new Font("Arial", Font.BOLD, 14));
            btnVoirAbonnements.setBackground(new Color(0, 102, 204));
            btnVoirAbonnements.setForeground(Color.WHITE);
            btnVoirAbonnements.addActionListener(e -> {
                Page_Abonnements pageAbonnements = new Page_Abonnements(emailUtilisateur);
                pageAbonnements.setVisible(true);
                dispose();
            });
            
            panelSansAbonnement.add(lblMessage, BorderLayout.CENTER);
            panelSansAbonnement.add(btnVoirAbonnements, BorderLayout.SOUTH);
            
            panel.add(panelSansAbonnement, BorderLayout.CENTER);
        } else {
            // Afficher les détails de l'abonnement actuel
            Abonnement abonnementActuel = abonnements.get(0);
            
            // ========== NOUVEAU : BOUTON RAFRAÎCHIR ==========
            JPanel panelHaut = new JPanel(new BorderLayout());
            panelHaut.setBackground(Color.WHITE);
            
            JButton btnRafraichir = new JButton("Rafraîchir");
            btnRafraichir.setFont(new Font("Arial", Font.PLAIN, 12));
            btnRafraichir.setBackground(new Color(200, 200, 200));
            btnRafraichir.addActionListener(e -> {
                // Rafraîchir la page après un paiement
                dispose();
                Page_Utilisateur nouvellePage = new Page_Utilisateur(emailUtilisateur);
                nouvellePage.setVisible(true);
            });
            panelHaut.add(btnRafraichir, BorderLayout.EAST);
            panel.add(panelHaut, BorderLayout.NORTH);
            // ========== FIN BOUTON RAFRAÎCHIR ==========
            
            JPanel panelDetails = new JPanel();
            panelDetails.setLayout(new BoxLayout(panelDetails, BoxLayout.Y_AXIS));
            panelDetails.setBackground(Color.WHITE);
            panelDetails.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
            
            // En-tête
            JLabel lblTitre = new JLabel("Votre abonnement actuel", SwingConstants.CENTER);
            lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
            lblTitre.setForeground(new Color(0, 102, 204));
            lblTitre.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            panelDetails.add(lblTitre);
            
            // Détails de l'abonnement
            JPanel panelInfo = new JPanel(new GridLayout(0, 2, 15, 10));
            panelInfo.setBackground(Color.WHITE);
            panelInfo.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            
            panelInfo.add(new JLabel("Nom de l'abonnement:"));
            JLabel lblNom = new JLabel(abonnementActuel.getLibelleAbonnement());
            lblNom.setFont(new Font("Arial", Font.BOLD, 14));
            panelInfo.add(lblNom);
            
            panelInfo.add(new JLabel("Code:"));
            panelInfo.add(new JLabel(abonnementActuel.getIdAbonnement()));
            
            panelInfo.add(new JLabel("Prix mensuel:"));
            JLabel lblPrix = new JLabel(String.format("%.2f €", abonnementActuel.getTarifAbonnement()));
            lblPrix.setFont(new Font("Arial", Font.BOLD, 14));
            lblPrix.setForeground(new Color(0, 150, 0));
            panelInfo.add(lblPrix);
            
            panelInfo.add(new JLabel("Date de souscription:"));
            // Note: Vous devrez ajouter un champ date_debut dans la table Appartenir
            panelInfo.add(new JLabel("01/01/2024")); // À remplacer par la date réelle
            
            panelInfo.add(new JLabel("Prochain renouvellement:"));
            panelInfo.add(new JLabel("01/02/2024")); // À remplacer par la date réelle
            
            panelDetails.add(panelInfo);
            
            // Avantages
            JPanel panelAvantages = new JPanel(new BorderLayout());
            panelAvantages.setBackground(new Color(245, 245, 245));
            panelAvantages.setBorder(BorderFactory.createTitledBorder("Avantages inclus"));
            
            JTextArea txtAvantages = new JTextArea(getAvantagesByType(abonnementActuel.getIdAbonnement()));
            txtAvantages.setEditable(false);
            txtAvantages.setFont(new Font("Arial", Font.PLAIN, 12));
            txtAvantages.setBackground(new Color(245, 245, 245));
            txtAvantages.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            panelAvantages.add(txtAvantages, BorderLayout.CENTER);
            panelDetails.add(panelAvantages);
            
            panel.add(panelDetails, BorderLayout.CENTER);
            
            // Bouton pour changer d'abonnement
            JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            panelBoutons.setBackground(Color.WHITE);
            
            JButton btnChangerAbonnement = new JButton("Changer d'abonnement");
            btnChangerAbonnement.setFont(new Font("Arial", Font.BOLD, 12));
            btnChangerAbonnement.setBackground(new Color(255, 153, 0));
            btnChangerAbonnement.setForeground(Color.WHITE);
            btnChangerAbonnement.addActionListener(e -> {
                Page_Abonnements pageAbonnements = new Page_Abonnements(emailUtilisateur);
                pageAbonnements.setVisible(true);
                dispose();
            });
            
            JButton btnResilier = new JButton("Résilier l'abonnement");
            btnResilier.setFont(new Font("Arial", Font.PLAIN, 12));
            btnResilier.setBackground(new Color(220, 80, 80));
            btnResilier.setForeground(Color.WHITE);
            btnResilier.addActionListener(e -> {
                int confirmation = JOptionPane.showConfirmDialog(this,
                    "Êtes-vous sûr de vouloir résilier votre abonnement ?\n" +
                    "Cette action sera effective à la fin de la période en cours.",
                    "Résiliation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                    
                if (confirmation == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(this,
                        "Votre demande de résiliation a été enregistrée.\n" +
                        "Votre abonnement sera actif jusqu'à la fin de la période en cours.",
                        "Demande enregistrée",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            panelBoutons.add(btnChangerAbonnement);
            panelBoutons.add(btnResilier);
            panel.add(panelBoutons, BorderLayout.SOUTH);
        }
        
        return panel;
    }
    
    /**
     * NOUVELLE MÉTHODE : Ouvre la page des abonnements disponibles
     */
    private void ouvrirPageAbonnements() {
        Page_Abonnements pageAbonnements = new Page_Abonnements(emailUtilisateur);
        pageAbonnements.setVisible(true);
        dispose();
    }
    
    /**
     * NOUVELLE MÉTHODE : Demande de résiliation d'abonnement
     */
    private void demanderResiliation() {
        int confirmation = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir résilier votre abonnement ?\n" +
            "Cette action sera effective à la fin de la période en cours.",
            "Résiliation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirmation == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                "Votre demande de résiliation a été enregistrée.\n" +
                "Votre abonnement sera actif jusqu'à la fin de la période en cours.",
                "Demande enregistrée",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * NOUVELLE MÉTHODE : Retourne les avantages selon le type d'abonnement
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
     * Rafraîchit l'affichage de l'abonnement dans l'onglet Informations
     */
    private void rafraichirAffichageAbonnement() {
        // Cette méthode serait appelée après un paiement réussi
        // Pour l'instant, on recrée simplement la page
        dispose();
        Page_Utilisateur nouvellePage = new Page_Utilisateur(emailUtilisateur);
        nouvellePage.setVisible(true);
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
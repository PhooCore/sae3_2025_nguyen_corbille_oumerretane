package ihm;

import javax.swing.*;

import controleur.ControleurUtilisateur;
import modele.Usager;
import modele.dao.AbonnementDAO;
import modele.dao.UsagerDAO;
import modele.dao.PaiementDAO;
import modele.dao.StationnementDAO;
import modele.dao.ZoneDAO;
import modele.dao.ParkingDAO;
import modele.dao.VehiculeUsagerDAO;
import modele.Abonnement;
import modele.Paiement;
import modele.Stationnement;
import modele.Zone;
import modele.Parking;
import modele.VehiculeUsager;
import java.awt.*;
import java.util.List;

public class Page_Utilisateur extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Usager usager;
    
    // Déclaration des boutons comme attributs
    private JButton btnModifierMdp;
    private JButton btnDeconnexion;
    private JButton btnRetour;
    private JButton btnGestionVehicules;
    
    public Page_Utilisateur(String email, boolean rafraichir) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        initialisePage();
        new ControleurUtilisateur(this);
    }
    
    public Page_Utilisateur(String email) {
        this(email, false);
    }
    
    private void initialisePage() {
        this.setTitle("Mon Compte");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 700);
        this.setLocationRelativeTo(null);
        
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
        JPanel panelPaiements = creerOngletPaiements();
        onglets.addTab("Historique des paiements", panelPaiements);
        
        // Onglet 3 : Historique des stationnements
        JPanel panelStationnements = creerOngletStationnements();
        onglets.addTab("Historique des stationnements", panelStationnements);
        
        // Onglet 4 : Gestion des véhicules
        JPanel panelVehicules = creerOngletVehicules();
        onglets.addTab("Mes véhicules", panelVehicules);
        
        mainPanel.add(onglets, BorderLayout.CENTER);
        
        // Bouton retour
        JPanel panelBoutonRetour = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoutonRetour.setBackground(Color.WHITE);
        btnRetour = new JButton("Retour à l'accueil");
        panelBoutonRetour.add(btnRetour);
        mainPanel.add(panelBoutonRetour, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
    }
    
    private JPanel creerOngletInfos() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // Informations personnelles
        ajouterLigneInfo(panel, "Nom:", usager.getNomUsager());
        ajouterLigneInfo(panel, "Prénom:", usager.getPrenomUsager());
        ajouterLigneInfo(panel, "Email:", usager.getMailUsager());
        
        panel.add(Box.createVerticalStrut(20));
        
        // Abonnement
        try {
            List<Abonnement> abonnements = AbonnementDAO.getInstance().getAbonnementsByUsager(usager.getIdUsager());
            
            if (!abonnements.isEmpty()) {
                Abonnement abonnementActif = abonnements.get(0);
                ajouterLigneInfo(panel, "Abonnement:", abonnementActif.getLibelleAbonnement());
                
                panel.add(Box.createVerticalStrut(10));
                
                JPanel panelBoutonsAbo = new JPanel(new FlowLayout(FlowLayout.LEFT));
                panelBoutonsAbo.setBackground(Color.WHITE);
                
                JButton btnChanger = new JButton("Changer d'abonnement");
                btnChanger.addActionListener(e -> changerAbonnement(abonnementActif));
                
                JButton btnResilier = new JButton("Résilier");
                btnResilier.setBackground(new Color(220, 80, 80));
                btnResilier.setForeground(Color.WHITE);
                btnResilier.addActionListener(e -> resilierAbonnement(abonnementActif));
                
                panelBoutonsAbo.add(btnChanger);
                panelBoutonsAbo.add(btnResilier);
                panel.add(panelBoutonsAbo);
                
            } else {
                JPanel ligneAbonnement = new JPanel(new BorderLayout());
                ligneAbonnement.setBackground(Color.WHITE);
                ligneAbonnement.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                
                JLabel lblLibelle = new JLabel("Abonnement:");
                lblLibelle.setFont(new Font("Arial", Font.BOLD, 14));
                lblLibelle.setPreferredSize(new Dimension(120, 25));
                
                JLabel lblValeur = new JLabel("Aucun abonnement actif");
                lblValeur.setFont(new Font("Arial", Font.PLAIN, 14));
                lblValeur.setForeground(Color.RED);
                
                JButton btnSouscrire = new JButton("Souscrire");
                btnSouscrire.setFont(new Font("Arial", Font.PLAIN, 12));
                btnSouscrire.setBackground(new Color(0, 120, 215));
                btnSouscrire.setForeground(Color.WHITE);
                btnSouscrire.setFocusPainted(false);
                btnSouscrire.addActionListener(e -> {
                    new Page_Abonnements(emailUtilisateur).setVisible(true);
                    dispose();
                });
                
                ligneAbonnement.add(lblLibelle, BorderLayout.WEST);
                ligneAbonnement.add(lblValeur, BorderLayout.CENTER);
                ligneAbonnement.add(btnSouscrire, BorderLayout.EAST);
                panel.add(ligneAbonnement);
            }
        } catch (Exception e) {
            System.err.println("Erreur récupération abonnements: " + e.getMessage());
            ajouterLigneInfo(panel, "Abonnement:", "Erreur de chargement");
        }
        
        panel.add(Box.createVerticalStrut(20));
        
        // Section Carte Tisséo
        try {
            String carteTisseo = UsagerDAO.getInstance().getCarteTisseoByUsager(usager.getIdUsager());

            if (carteTisseo == null || carteTisseo.isEmpty()) {
                // Aucune carte enregistrée - afficher le bouton pour ajouter
                JPanel ligneCarte = new JPanel(new BorderLayout());
                ligneCarte.setBackground(Color.WHITE);
                ligneCarte.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

                JLabel lblLibelle = new JLabel("Carte Tisséo:");
                lblLibelle.setFont(new Font("Arial", Font.BOLD, 14));
                lblLibelle.setPreferredSize(new Dimension(120, 25));

                JLabel lblValeur = new JLabel("Aucune carte Tisséo renseignée");
                lblValeur.setFont(new Font("Arial", Font.PLAIN, 14));
                lblValeur.setForeground(Color.RED);

                JButton btnAjouter = new JButton("Ajouter une carte");
                btnAjouter.addActionListener(e -> ouvrirPopupAjoutCarteTisseo());

                ligneCarte.add(lblLibelle, BorderLayout.WEST);
                ligneCarte.add(lblValeur, BorderLayout.CENTER);
                ligneCarte.add(btnAjouter, BorderLayout.EAST);

                panel.add(ligneCarte);

            } else {
                // Carte déjà enregistrée - afficher avec options de modification
                JPanel ligneCarte = new JPanel(new BorderLayout());
                ligneCarte.setBackground(Color.WHITE);
                ligneCarte.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

                JLabel lblLibelle = new JLabel("Carte Tisséo:");
                lblLibelle.setFont(new Font("Arial", Font.BOLD, 14));
                lblLibelle.setPreferredSize(new Dimension(120, 25));

                JLabel lblValeur = new JLabel(carteTisseo);
                lblValeur.setFont(new Font("Arial", Font.PLAIN, 14));
                lblValeur.setForeground(new Color(0, 100, 0));

                JPanel panelBoutonsCarte = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                panelBoutonsCarte.setBackground(Color.WHITE);
                
                JButton btnChangerCarte = new JButton("Changer");
                btnChangerCarte.setFont(new Font("Arial", Font.PLAIN, 12));
                btnChangerCarte.addActionListener(e -> ouvrirPopupModifCarteTisseo(carteTisseo));
                
                JButton btnSupprimerCarte = new JButton("Supprimer");
                btnSupprimerCarte.setFont(new Font("Arial", Font.PLAIN, 12));
                btnSupprimerCarte.setBackground(new Color(220, 80, 80));
                btnSupprimerCarte.setForeground(Color.WHITE);
                btnSupprimerCarte.addActionListener(e -> supprimerCarteTisseo());

                panelBoutonsCarte.add(btnChangerCarte);
                panelBoutonsCarte.add(btnSupprimerCarte);

                ligneCarte.add(lblLibelle, BorderLayout.WEST);
                ligneCarte.add(lblValeur, BorderLayout.CENTER);
                ligneCarte.add(panelBoutonsCarte, BorderLayout.EAST);

                panel.add(ligneCarte);
            }
        } catch (Exception e) {
            System.err.println("Erreur récupération carte Tisséo: " + e.getMessage());
            ajouterLigneInfo(panel, "Carte Tisséo:", "Erreur de chargement");
        }
        
        panel.add(Box.createVerticalStrut(30));
        
        // Section Véhicules
        JPanel panelVehiculesInfo = new JPanel(new BorderLayout());
        panelVehiculesInfo.setBackground(Color.WHITE);
        panelVehiculesInfo.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel lblTitreVehicules = new JLabel("Véhicules enregistrés:");
        lblTitreVehicules.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Compter le nombre de véhicules
        List<VehiculeUsager> vehiculesList = VehiculeUsagerDAO.getVehiculesByUsager(usager.getIdUsager());
        int nbVehicules = vehiculesList.size();
        int nbVehiculesPrincipaux = 0;
        for (VehiculeUsager v : vehiculesList) {
            if (v.isEstPrincipal()) {
                nbVehiculesPrincipaux++;
            }
        }
        
        JLabel lblNbVehicules = new JLabel(nbVehicules + " véhicule(s) - " + nbVehiculesPrincipaux + " principal(aux)");
        lblNbVehicules.setFont(new Font("Arial", Font.PLAIN, 12));
        lblNbVehicules.setForeground(Color.BLUE);
        
        panelVehiculesInfo.add(lblTitreVehicules, BorderLayout.WEST);
        panelVehiculesInfo.add(lblNbVehicules, BorderLayout.EAST);
        panel.add(panelVehiculesInfo);
        
        panel.add(Box.createVerticalStrut(30));
        
        // Boutons d'action
        btnGestionVehicules = new JButton("Gestion complète des véhicules");
        btnGestionVehicules.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGestionVehicules.setBackground(new Color(70, 130, 180));
        btnGestionVehicules.setForeground(Color.WHITE);
        
        panel.add(btnGestionVehicules);
        panel.add(Box.createVerticalStrut(20));
        
        btnModifierMdp = new JButton("Modifier le mot de passe");
        btnModifierMdp.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnDeconnexion = new JButton("Déconnexion");
        btnDeconnexion.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDeconnexion.setBackground(new Color(220, 80, 80));
        btnDeconnexion.setForeground(Color.WHITE);
        
        panel.add(btnModifierMdp);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnDeconnexion);
        
        return panel;
    }
    
    private JPanel creerOngletVehicules() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // Titre
        JLabel lblTitre = new JLabel("Gestion des véhicules", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(lblTitre, BorderLayout.NORTH);
        
        // Liste des véhicules
        DefaultListModel<VehiculeUsager> listModel = new DefaultListModel<>();
        JList<VehiculeUsager> listVehicules = new JList<>(listModel);
        listVehicules.setCellRenderer(new VehiculeRenderer());
        listVehicules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(listVehicules);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Vos véhicules"));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Charger les véhicules
        List<VehiculeUsager> vehiculesList = VehiculeUsagerDAO.getVehiculesByUsager(usager.getIdUsager());
        for (VehiculeUsager v : vehiculesList) {
            listModel.addElement(v);
        }
        
        // Panel boutons
        JPanel panelBoutons = new JPanel(new GridLayout(1, 4, 10, 0));
        panelBoutons.setBackground(Color.WHITE);
        
        JButton btnAjouter = new JButton("Ajouter");
        JButton btnModifier = new JButton("Modifier");
        JButton btnSupprimer = new JButton("Supprimer");
        JButton btnDefinirPrincipal = new JButton("Définir principal");
        
        // Écouteurs d'événements
        btnAjouter.addActionListener(e -> {
            Page_Gestion_Vehicules pageGestion = new Page_Gestion_Vehicules(emailUtilisateur);
            pageGestion.setVisible(true);
            pageGestion.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    // Rafraîchir la liste
                    listModel.clear();
                    List<VehiculeUsager> nouvellesVehicules = VehiculeUsagerDAO.getVehiculesByUsager(usager.getIdUsager());
                    for (VehiculeUsager v : nouvellesVehicules) {
                        listModel.addElement(v);
                    }
                }
            });
        });
        
        btnModifier.addActionListener(e -> {
            VehiculeUsager vehicule = listVehicules.getSelectedValue();
            if (vehicule != null) {
                JOptionPane.showMessageDialog(this, 
                    "Pour modifier un véhicule, veuillez utiliser la page de gestion complète.",
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
                Page_Gestion_Vehicules pageGestion = new Page_Gestion_Vehicules(emailUtilisateur);
                pageGestion.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez sélectionner un véhicule à modifier", 
                    "Aucune sélection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        btnSupprimer.addActionListener(e -> {
            VehiculeUsager vehicule = listVehicules.getSelectedValue();
            if (vehicule != null) {
                int choix = JOptionPane.showConfirmDialog(this,
                    "Êtes-vous sûr de vouloir supprimer ce véhicule ?\n" +
                    "Plaque: " + vehicule.getPlaqueImmatriculation() + "\n" +
                    "Type: " + vehicule.getTypeVehicule(),
                    "Confirmation de suppression",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (choix == JOptionPane.YES_OPTION) {
                    if (VehiculeUsagerDAO.supprimerVehiculeStatic(vehicule.getIdVehiculeUsager())) {
                        listModel.removeElement(vehicule);
                        JOptionPane.showMessageDialog(this,
                            "Véhicule supprimé avec succès",
                            "Suppression réussie",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez sélectionner un véhicule à supprimer", 
                    "Aucune sélection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        btnDefinirPrincipal.addActionListener(e -> {
            VehiculeUsager vehicule = listVehicules.getSelectedValue();
            if (vehicule != null) {
                if (vehicule.isEstPrincipal()) {
                    JOptionPane.showMessageDialog(this,
                        "Ce véhicule est déjà défini comme véhicule principal",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                
                int choix = JOptionPane.showConfirmDialog(this,
                    "Définir ce véhicule comme véhicule principal ?\n\n" +
                    "Plaque: " + vehicule.getPlaqueImmatriculation() + "\n" +
                    "Type: " + vehicule.getTypeVehicule(),
                    "Définir comme véhicule principal",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (choix == JOptionPane.YES_OPTION) {
                    if (VehiculeUsagerDAO.definirVehiculePrincipalStatic(
                        vehicule.getIdVehiculeUsager(), usager.getIdUsager())) {
                        
                        // Rafraîchir la liste
                        listModel.clear();
                        List<VehiculeUsager> nouvellesVehicules = VehiculeUsagerDAO.getVehiculesByUsager(usager.getIdUsager());
                        for (VehiculeUsager v : nouvellesVehicules) {
                            listModel.addElement(v);
                        }
                        
                        JOptionPane.showMessageDialog(this,
                            "Véhicule défini comme principal avec succès",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez sélectionner un véhicule", 
                    "Aucune sélection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnModifier);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnDefinirPrincipal);
        
        panel.add(panelBoutons, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel creerOngletPaiements() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Récupération des paiements
        List<Paiement> paiements;
        try {
            paiements = PaiementDAO.getInstance().getPaiementsByUsager(usager.getIdUsager());
        } catch (Exception e) {
            System.err.println("Erreur récupération paiements: " + e.getMessage());
            paiements = new java.util.ArrayList<>();
        }
        
        // En-têtes des colonnes
        String[] colonnes = {"Date", "Montant", "Type", "Détails", "Statut"};
        Object[][] donnees = new Object[paiements.size()][5];
        
        double totalDepense = 0.0;
        double totalAbonnements = 0.0;
        double totalStationnements = 0.0;
        
        for (int i = 0; i < paiements.size(); i++) {
            Paiement p = paiements.get(i);
            
            donnees[i][0] = p.getDatePaiement().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            donnees[i][1] = String.format("%.2f €", p.getMontant());
            donnees[i][2] = p.getTypePaiement();
            
            // Détails spécifiques
            String details = "-";
            if ("ABONNEMENT".equals(p.getTypePaiement()) && p.getIdAbonnement() != null) {
                try {
                    Abonnement abonnement = AbonnementDAO.getInstance().findById(p.getIdAbonnement());
                    details = (abonnement != null) ? abonnement.getLibelleAbonnement() : p.getIdAbonnement();
                } catch (Exception e) {
                    details = p.getIdAbonnement();
                }
                totalAbonnements += p.getMontant();
            } else if ("STATIONNEMENT".equals(p.getTypePaiement())) {
                totalStationnements += p.getMontant();
            }
            donnees[i][3] = details;
            
            donnees[i][4] = "Payé";
            
            totalDepense += p.getMontant();
        }
        
        // Création du tableau
        JTable table = new JTable(donnees, colonnes);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setDefaultEditor(Object.class, null);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel de statistiques
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        statsPanel.setBackground(Color.WHITE);
        
        // Calcul de la date du dernier paiement
        String dernierPaiement = "Aucun";
        if (!paiements.isEmpty()) {
            dernierPaiement = paiements.get(0).getDatePaiement()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        
        // Ajout des statistiques
        ajouterStatistique(statsPanel, "Total dépensé", String.format("%.2f €", totalDepense));
        ajouterStatistique(statsPanel, "Abonnements", String.format("%.2f €", totalAbonnements));
        ajouterStatistique(statsPanel, "Stationnements", String.format("%.2f €", totalStationnements));
        ajouterStatistique(statsPanel, "Nb paiements", String.valueOf(paiements.size()));
        ajouterStatistique(statsPanel, "Dernier", dernierPaiement);
        
        panel.add(statsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel creerOngletStationnements() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Récupération des stationnements
        List<Stationnement> stationnements = StationnementDAO.getHistoriqueStationnementsStatic(usager.getIdUsager());
        
        String[] colonnes = {"Date", "Type", "Véhicule", "Zone/Parking", "Durée", "Coût", "Statut"};
        Object[][] donnees = new Object[stationnements.size()][7];
        
        for (int i = 0; i < stationnements.size(); i++) {
            Stationnement s = stationnements.get(i);
            
            donnees[i][0] = s.getDateCreation()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            donnees[i][1] = s.getTypeStationnement();
            donnees[i][2] = s.getTypeVehicule() + " - " + s.getPlaqueImmatriculation();
            
            // Colonne Zone/Parking
            String zoneId = s.getIdTarification();
            if (zoneId == null || zoneId.trim().isEmpty()) {
                donnees[i][3] = "Non spécifié";
            } else {
                if ("PARKING".equals(s.getTypeStationnement())) {
                    try {
                        Parking parking = ParkingDAO.getInstance().findById(zoneId);
                        donnees[i][3] = (parking != null) ? parking.getLibelleParking() : zoneId;
                    } catch (Exception e) {
                        donnees[i][3] = zoneId;
                    }
                } else {
                    try {
                        Zone zone = ZoneDAO.getInstance().findById(zoneId);
                        donnees[i][3] = (zone != null) ? zone.getLibelleZone() : zoneId;
                    } catch (Exception e) {
                        donnees[i][3] = zoneId;
                    }
                }
            }
            
            // Durée
            if (s.estVoirie()) {
                donnees[i][4] = s.getDureeHeures() + "h" + s.getDureeMinutes() + "min";
            } else {
                if (s.getHeureArrivee() != null && s.getHeureDepart() != null) {
                    long minutes = java.time.Duration.between(s.getHeureArrivee(), s.getHeureDepart()).toMinutes();
                    long heures = minutes / 60;
                    long mins = minutes % 60;
                    donnees[i][4] = heures + "h" + mins + "min";
                } else {
                    donnees[i][4] = "En cours";
                }
            }
            
            donnees[i][5] = String.format("%.2f €", s.getCout());
            donnees[i][6] = s.getStatut();
        }
        
        // Création du tableau
        JTable table = new JTable(donnees, colonnes);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setDefaultEditor(Object.class, null);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Statistiques
        JPanel statsPanel = new JPanel(new FlowLayout());
        statsPanel.setBackground(Color.WHITE);
        
        long totalStationnements = stationnements.size();
        long stationnementsActifs = stationnements.stream()
                .filter(s -> "ACTIF".equals(s.getStatut()))
                .count();
        
        JLabel lblStats = new JLabel("Total: " + totalStationnements + " stationnement(s) | Actifs: " + stationnementsActifs);
        lblStats.setFont(new Font("Arial", Font.BOLD, 14));
        statsPanel.add(lblStats);
        
        panel.add(statsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void ajouterLigneInfo(JPanel panel, String libelle, String valeur) {
        JPanel ligne = new JPanel(new BorderLayout());
        ligne.setBackground(Color.WHITE);
        ligne.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JLabel lblLibelle = new JLabel(libelle);
        lblLibelle.setFont(new Font("Arial", Font.BOLD, 14)); 
        lblLibelle.setPreferredSize(new Dimension(120, 25)); 
        
        JLabel lblValeur = new JLabel(valeur);
        lblValeur.setFont(new Font("Arial", Font.PLAIN, 14)); 
        
        ligne.add(lblLibelle, BorderLayout.WEST);
        ligne.add(lblValeur, BorderLayout.CENTER);
        panel.add(ligne);
    }
    
    private void ajouterStatistique(JPanel panel, String libelle, String valeur) {
        JPanel statPanel = new JPanel();
        statPanel.setLayout(new BoxLayout(statPanel, BoxLayout.Y_AXIS));
        statPanel.setBackground(Color.WHITE);
        statPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JLabel lblLibelle = new JLabel(libelle, SwingConstants.CENTER);
        lblLibelle.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel lblValeur = new JLabel(valeur, SwingConstants.CENTER);
        lblValeur.setFont(new Font("Arial", Font.BOLD, 16));
        
        statPanel.add(Box.createVerticalStrut(10));
        statPanel.add(lblLibelle);
        statPanel.add(lblValeur);
        statPanel.add(Box.createVerticalStrut(10));
        
        panel.add(statPanel);
    }
    
    /**
     * Ouvre le popup pour ajouter une carte Tisséo
     */
    private void ouvrirPopupAjoutCarteTisseo() {
        String numeroCarte = JOptionPane.showInputDialog(
            this,
            "Entrez votre numéro de carte Tisséo (Pastel) :\n" +
            "Format : 10 chiffres (ex: 1234567890)",
            "Carte Tisséo",
            JOptionPane.PLAIN_MESSAGE
        );

        if (numeroCarte != null && !numeroCarte.trim().isEmpty()) {
            if (!numeroCarte.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(this,
                    "Le format est incorrect.\n" +
                    "Veuillez entrer 10 chiffres (ex: 1234567890).",
                    "Numéro invalide", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                boolean succes = UsagerDAO.enregistrerCarteTisseo(usager.getIdUsager(), numeroCarte.trim());
                
                if (succes) {
                    JOptionPane.showMessageDialog(this,
                        "✅ Carte Tisséo enregistrée avec succès\n\n" +
                        "Numéro : " + numeroCarte + "\n" +
                        "Vous pouvez maintenant utiliser les parkings relais gratuitement.",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE
                    );

                    // Rafraîchir la page
                    new Page_Utilisateur(emailUtilisateur, true).setVisible(true);
                    dispose();
                    
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'enregistrement de la carte.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Ouvre le popup pour modifier une carte Tisséo existante
     */
    private void ouvrirPopupModifCarteTisseo(String carteActuelle) {
        String numeroCarte = JOptionPane.showInputDialog(
            this,
            "Votre carte Tisséo actuelle : " + carteActuelle + "\n\n" +
            "Entrez le nouveau numéro de carte Tisséo (Pastel) :\n" +
            "Format : 10 chiffres (ex: 1234567890)\n\n" +
            "Laissez vide pour conserver la carte actuelle.",
            "Modifier carte Tisséo",
            JOptionPane.PLAIN_MESSAGE
        );

        if (numeroCarte != null) {
            if (numeroCarte.trim().isEmpty()) {
                return; // L'utilisateur a annulé
            }
            
            if (!numeroCarte.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(this,
                    "Le format est incorrect.\n" +
                    "Veuillez entrer 10 chiffres (ex: 1234567890).",
                    "Numéro invalide", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                boolean succes = UsagerDAO.enregistrerCarteTisseo(usager.getIdUsager(), numeroCarte.trim());
                
                if (succes) {
                    JOptionPane.showMessageDialog(this,
                        "✅ Carte Tisséo modifiée avec succès\n\n" +
                        "Nouveau numéro : " + numeroCarte,
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE
                    );

                    // Rafraîchir la page
                    new Page_Utilisateur(emailUtilisateur, true).setVisible(true);
                    dispose();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Supprime la carte Tisséo
     */
    private void supprimerCarteTisseo() {
        int confirmation = JOptionPane.showConfirmDialog(
            this,
            "Êtes-vous sûr de vouloir supprimer votre carte Tisséo ?\n\n" +
            "Vous ne pourrez plus bénéficier des parkings relais gratuits.",
            "Confirmation de suppression",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                boolean succes = UsagerDAO.enregistrerCarteTisseo(usager.getIdUsager(), null); // null pour supprimer
                
                if (succes) {
                    JOptionPane.showMessageDialog(this,
                        "✅ Carte Tisséo supprimée avec succès",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE
                    );

                    // Rafraîchir la page
                    new Page_Utilisateur(emailUtilisateur, true).setVisible(true);
                    dispose();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    private void changerAbonnement(Abonnement abonnementActuel) {
        String message = "Vous avez actuellement l'abonnement : " + abonnementActuel.getLibelleAbonnement() + "\n\n" +
                        "⚠️ En changeant d'abonnement, votre abonnement actuel sera résilié.\n" +
                        "Le montant déjà payé ne sera pas remboursé.\n\n" +
                        "Voulez-vous continuer ?";
        
        int choix = JOptionPane.showConfirmDialog(
            this,
            message,
            "Changement d'abonnement",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choix == JOptionPane.YES_OPTION) {
            try {
                // Correction ici : getInstance().supprimerAbonnementsUtilisateur() renvoie void, pas boolean
                AbonnementDAO.getInstance().supprimerAbonnementsUtilisateur(usager.getIdUsager());
                
                JOptionPane.showMessageDialog(
                    this,
                    "Votre ancien abonnement a été résilié.\nVous allez être redirigé vers la page des abonnements.",
                    "Abonnement résilié",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                new Page_Abonnements(emailUtilisateur).setVisible(true);
                dispose();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors de la résiliation de l'abonnement : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void resilierAbonnement(Abonnement abonnementActuel) {
        String message = "⚠️ ATTENTION ⚠️\n\n" +
                        "Vous êtes sur le point de résilier votre abonnement :\n" +
                        abonnementActuel.getLibelleAbonnement() + " - " + 
                        String.format("%.2f €", abonnementActuel.getTarifAbonnement()) + "\n\n" +
                        "Conséquences :\n" +
                        "• Perte de tous les avantages\n" +
                        "• Aucun remboursement\n" +
                        "• Retour aux tarifs standards\n\n" +
                        "Êtes-vous sûr de vouloir résilier et perdre votre argent ?";
        
        int choix = JOptionPane.showConfirmDialog(
            this,
            message,
            "Résiliation d'abonnement",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (choix == JOptionPane.YES_OPTION) {
            int confirmation2 = JOptionPane.showConfirmDialog(
                this,
                "Dernière confirmation :\n\n" +
                "Vous allez perdre " + String.format("%.2f €", abonnementActuel.getTarifAbonnement()) + "\n\n" +
                "Confirmez-vous définitivement la résiliation ?",
                "Confirmation finale",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirmation2 == JOptionPane.YES_OPTION) {
                try {
                    // Correction ici aussi
                    AbonnementDAO.getInstance().supprimerAbonnementsUtilisateur(usager.getIdUsager());
                    
                    JOptionPane.showMessageDialog(
                        this,
                        "Votre abonnement a été résilié avec succès.\n\n" +
                        "Vous pouvez souscrire à un nouvel abonnement à tout moment.",
                        "Résiliation confirmée",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    new Page_Utilisateur(emailUtilisateur, true).setVisible(true);
                    dispose();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Erreur lors de la résiliation : " + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }
    
    // GETTERS POUR LE CONTROLEUR
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }
    
    public Usager getUsager() {
        return usager;
    }
    
    public JButton getBtnModifierMdp() {
        return btnModifierMdp;
    }
    
    public JButton getBtnDeconnexion() {
        return btnDeconnexion;
    }
    
    public JButton getBtnRetour() {
        return btnRetour;
    }
    
    public JButton getBtnGestionVehicules() {
        return btnGestionVehicules;
    }
    
    // Renderer pour la liste des véhicules
    private class VehiculeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                     int index, boolean isSelected, 
                                                     boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            VehiculeUsager vehicule = (VehiculeUsager) value;
            
            StringBuilder texte = new StringBuilder("<html>");
            
            if (vehicule.isEstPrincipal()) {
                texte.append("<b>★ ").append(vehicule.getPlaqueImmatriculation()).append("</b>");
            } else {
                texte.append(vehicule.getPlaqueImmatriculation());
            }
            
            texte.append(" - ").append(vehicule.getTypeVehicule());
            
            if (vehicule.getMarque() != null && !vehicule.getMarque().isEmpty()) {
                texte.append(" ").append(vehicule.getMarque());
            }
            if (vehicule.getModele() != null && !vehicule.getModele().isEmpty()) {
                texte.append(" ").append(vehicule.getModele());
            }
            
            texte.append("</html>");
            
            setText(texte.toString());
            
            if (isSelected) {
                setBackground(new Color(220, 240, 255));
                setForeground(Color.BLACK);
            } else {
                if (vehicule.isEstPrincipal()) {
                    setBackground(new Color(255, 255, 220));
                } else {
                    setBackground(Color.WHITE);
                }
            }
            
            return this;
        }
    }
}
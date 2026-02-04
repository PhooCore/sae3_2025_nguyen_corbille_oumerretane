package ihm;

import javax.swing.*;

import controleur.ControleurUtilisateur;
import modele.Usager;
import modele.dao.AbonnementDAO;
import modele.dao.AdresseDAO;
import modele.dao.UsagerDAO;
import modele.dao.PaiementDAO;
import modele.dao.StationnementDAO;
import modele.dao.ZoneDAO;
import modele.dao.ParkingDAO;
import modele.dao.VehiculeUsagerDAO;
import modele.Abonnement;
import modele.Adresse;
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
    private JButton btnModifierMdp;
    private JButton btnDeconnexion;
    private JButton btnRetour;
    private JButton btnGestionVehicules;
    
    /**
     * constructeur de la page utilisateur avec option de rafraîchissement
     */
    public Page_Utilisateur(String email, boolean rafraichir) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        initialisePage();
        new ControleurUtilisateur(this);
    }
    
    /**
     * constructeur simplifié de la page utilisateur
     */
    public Page_Utilisateur(String email) {
        this(email, false);
    }
    
    /**
     * initialise tous les composants graphiques de la page
     */
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
    
    // === ONGLETS ===
    
    /**
     * crée l'onglet des informations personnelles de l'utilisateur
     */
    private JPanel creerOngletInfos() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // Informations personnelles
        ajouterLigneInfo(panel, "Nom:", usager.getNomUsager());
        ajouterLigneInfo(panel, "Prénom:", usager.getPrenomUsager());
        ajouterLigneInfo(panel, "Email:", usager.getMailUsager());
        
        // Adresse
        try {
            Adresse adressePrincipale = AdresseDAO.getInstance().getAdressePrincipale(usager.getIdUsager());
            
            JPanel ligneAdresse = new JPanel(new BorderLayout());
            ligneAdresse.setBackground(Color.WHITE);
            ligneAdresse.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            
            JLabel lblLibelle = new JLabel("Mon adresse:");
            lblLibelle.setFont(new Font("Arial", Font.BOLD, 14));
            lblLibelle.setPreferredSize(new Dimension(120, 25));
            
            ligneAdresse.add(lblLibelle, BorderLayout.WEST);
            
            if (adressePrincipale != null) {
                // Panel pour afficher l'adresse
                JPanel panelAdresseText = new JPanel();
                panelAdresseText.setLayout(new BoxLayout(panelAdresseText, BoxLayout.Y_AXIS));
                panelAdresseText.setBackground(Color.WHITE);
                
                // Lignes d'adresse
                JLabel lblLigne1 = new JLabel(adressePrincipale.getNumero() + " " + adressePrincipale.getRue());
                lblLigne1.setFont(new Font("Arial", Font.PLAIN, 14));
                lblLigne1.setForeground(Color.BLACK);
                lblLigne1.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelAdresseText.add(lblLigne1);
                
                if (adressePrincipale.getComplement() != null && !adressePrincipale.getComplement().trim().isEmpty()) {
                    JLabel lblComplement = new JLabel(adressePrincipale.getComplement());
                    lblComplement.setFont(new Font("Arial", Font.PLAIN, 14));
                    lblComplement.setForeground(Color.BLACK);
                    lblComplement.setAlignmentX(Component.LEFT_ALIGNMENT);
                    panelAdresseText.add(lblComplement);
                }
                
                JLabel lblLigne3 = new JLabel(adressePrincipale.getCodePostal() + " " + adressePrincipale.getVille());
                lblLigne3.setFont(new Font("Arial", Font.PLAIN, 14));
                lblLigne3.setForeground(Color.BLACK);
                lblLigne3.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelAdresseText.add(lblLigne3);
                
                JPanel panelCombined = new JPanel(new BorderLayout());
                panelCombined.setBackground(Color.WHITE);
                panelCombined.add(panelAdresseText, BorderLayout.WEST);
                
                JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                panelBoutons.setBackground(Color.WHITE);
                
                JButton btnGerer = new JButton("Gérer");
                btnGerer.addActionListener(e -> ouvrirGestionAdresse());
                
                JButton btnChanger = new JButton("Changer");
                btnChanger.addActionListener(e -> {
                    ouvrirFormulaireAdresse(adressePrincipale);
                    rafraichirAffichage();
                });
                
                panelBoutons.add(btnGerer);
                panelBoutons.add(btnChanger);
                
                panelCombined.add(panelBoutons, BorderLayout.EAST);
                ligneAdresse.add(panelCombined, BorderLayout.CENTER);
                
            } else {
                // Aucune adresse
                JLabel lblValeur = new JLabel("Aucune adresse enregistrée");
                lblValeur.setFont(new Font("Arial", Font.PLAIN, 14));
                lblValeur.setForeground(Color.RED);
                
                JButton btnAjouter = new JButton("Ajouter une adresse");
                btnAjouter.addActionListener(e -> {
                    ouvrirFormulaireAdresse(null);
                    rafraichirAffichage();
                });
                
                ligneAdresse.add(lblLibelle, BorderLayout.WEST);

                JPanel panelCentre = new JPanel(new BorderLayout());
                panelCentre.setBackground(Color.WHITE);
                panelCentre.add(lblValeur, BorderLayout.CENTER);
                panelCentre.add(btnAjouter, BorderLayout.EAST);
                
                ligneAdresse.add(panelCentre, BorderLayout.CENTER);
            }
            
            panel.add(ligneAdresse);
            
        } catch (Exception e) {
            System.err.println("Erreur chargement adresse: " + e.getMessage());
            
            JPanel ligneAdresseErreur = new JPanel(new BorderLayout());
            ligneAdresseErreur.setBackground(Color.WHITE);
            ligneAdresseErreur.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            
            JLabel lblLibelle = new JLabel("Mon adresse:");
            lblLibelle.setFont(new Font("Arial", Font.BOLD, 14));
            lblLibelle.setPreferredSize(new Dimension(120, 25));
            
            JLabel lblErreur = new JLabel("Erreur de chargement");
            lblErreur.setFont(new Font("Arial", Font.PLAIN, 14));
            lblErreur.setForeground(Color.RED);
            
            ligneAdresseErreur.add(lblLibelle, BorderLayout.WEST);
            ligneAdresseErreur.add(lblErreur, BorderLayout.CENTER);
            panel.add(ligneAdresseErreur);
        }
        
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
    
    /**
     * crée l'onglet de gestion des véhicules
     */
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
        
        JPanel panelBoutons = new JPanel(new GridLayout(1, 4, 10, 0));
        panelBoutons.setBackground(Color.WHITE);
        
        JButton btnAjouter = new JButton("Ajouter");
        JButton btnModifier = new JButton("Modifier");
        JButton btnSupprimer = new JButton("Supprimer");
        JButton btnDefinirPrincipal = new JButton("Définir principal");
        
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
    
    /**
     * crée l'onglet d'historique des paiements
     */
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
    
    /**
     * crée l'onglet d'historique des stationnements
     */
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
    
    /**
     * ajoute une ligne d'information avec un libellé et une valeur
     */
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
    
    /**
     * ajoute une carte de statistique avec un libellé et une valeur
     */
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
    
    // === ADRESSE(S) ===
    
    /**
     * ouvre la fenêtre de gestion des adresses avec la liste de toutes les adresses
     */
    private void ouvrirGestionAdresse() {
        try {
            JDialog dialog = new JDialog(this, "Gestion de mes adresses", true);
            dialog.setSize(500, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(0, 0));
            
            List<Adresse> adresses = AdresseDAO.getInstance().getAdressesByUsager(usager.getIdUsager());
            
            // En-tête
            JPanel panelTitre = new JPanel(new BorderLayout());
            panelTitre.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            panelTitre.setBackground(new Color(240, 240, 240));
            
            JLabel lblTitre = new JLabel("Mes adresses");
            lblTitre.setFont(new Font("Arial", Font.BOLD, 16));
            panelTitre.add(lblTitre, BorderLayout.WEST);
            
            JButton btnFermer = new JButton("Fermer");
            btnFermer.setFont(new Font("Arial", Font.PLAIN, 12));
            btnFermer.addActionListener(e -> dialog.dispose());
            panelTitre.add(btnFermer, BorderLayout.EAST);
            
            dialog.add(panelTitre, BorderLayout.NORTH);
            
            // Contenu
            JPanel panelContenu = new JPanel();
            panelContenu.setLayout(new BoxLayout(panelContenu, BoxLayout.Y_AXIS));
            panelContenu.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panelContenu.setBackground(Color.WHITE);
            
            if (adresses.isEmpty()) {
                JLabel lblAucune = new JLabel("Aucune adresse enregistrée", SwingConstants.CENTER);
                lblAucune.setFont(new Font("Arial", Font.ITALIC, 14));
                lblAucune.setForeground(Color.GRAY);
                lblAucune.setAlignmentX(Component.CENTER_ALIGNMENT);
                panelContenu.add(lblAucune);
            } else {
                for (Adresse adresse : adresses) {
                    panelContenu.add(creerPanelAdressePopup(adresse, dialog));
                    panelContenu.add(Box.createVerticalStrut(10));
                }
            }
            
            JScrollPane scrollPane = new JScrollPane(panelContenu);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            dialog.add(scrollPane, BorderLayout.CENTER);
            
            // Pied de page
            JPanel panelPied = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panelPied.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            panelPied.setBackground(Color.WHITE);
            
            JButton btnAjouter = new JButton("+ Ajouter une nouvelle adresse");
            btnAjouter.setFont(new Font("Arial", Font.BOLD, 12));
            btnAjouter.setBackground(new Color(0, 120, 215));
            btnAjouter.setForeground(Color.WHITE);
            btnAjouter.setFocusPainted(false);
            btnAjouter.addActionListener(e -> {
                dialog.dispose();
                ouvrirFormulaireAdresse(null);
            });
            
            panelPied.add(btnAjouter);
            dialog.add(panelPied, BorderLayout.SOUTH);
            
            dialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de l'ouverture de la gestion d'adresses: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * crée un panneau pour afficher une adresse dans le popup de gestion
     */
    private JPanel creerPanelAdressePopup(Adresse adresse, JDialog parentDialog) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(Color.WHITE);
        
        // Indicateur d'adresse principale avec étoile
        JPanel panelTitre = new JPanel(new BorderLayout());
        panelTitre.setBackground(Color.WHITE);
        
        if (adresse.isEstPrincipale()) {
            JLabel lblPrincipale = new JLabel("★ Adresse principale");
            lblPrincipale.setFont(new Font("Arial", Font.BOLD, 12));
            lblPrincipale.setForeground(new Color(0, 100, 200));
            panelTitre.add(lblPrincipale, BorderLayout.WEST);
        } else {
            panelTitre.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        }
        
        panel.add(panelTitre, BorderLayout.NORTH);
        
        // Détails de l'adresse
        JPanel panelAdresse = new JPanel();
        panelAdresse.setLayout(new BoxLayout(panelAdresse, BoxLayout.Y_AXIS));
        panelAdresse.setBackground(Color.WHITE);
        
        // Ligne 1 : Numéro + Rue
        JLabel lblLigne1 = new JLabel(adresse.getNumero() + " " + adresse.getRue());
        lblLigne1.setFont(new Font("Arial", Font.PLAIN, 13));
        panelAdresse.add(lblLigne1);
        
        // Ligne 2 : Complément (si existe)
        if (adresse.getComplement() != null && !adresse.getComplement().trim().isEmpty()) {
            JLabel lblComplement = new JLabel(adresse.getComplement());
            lblComplement.setFont(new Font("Arial", Font.PLAIN, 13));
            lblComplement.setForeground(Color.DARK_GRAY);
            panelAdresse.add(lblComplement);
        }
        
        // Ligne 3 : Code postal + Ville
        JLabel lblLigne3 = new JLabel(adresse.getCodePostal() + " " + adresse.getVille());
        lblLigne3.setFont(new Font("Arial", Font.PLAIN, 13));
        panelAdresse.add(lblLigne3);
        
        panel.add(panelAdresse, BorderLayout.CENTER);
        
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelBoutons.setBackground(Color.WHITE);
        
        
        // Gestion des adresses
        if (!adresse.isEstPrincipale()) {
            JButton btnPrincipale = new JButton("Principale");
            btnPrincipale.setFont(new Font("Arial", Font.PLAIN, 11));
            btnPrincipale.setBackground(new Color(255, 215, 0));
            btnPrincipale.addActionListener(e -> {
                definirAdressePrincipale(adresse);
                parentDialog.dispose();
            });
            panelBoutons.add(btnPrincipale);
        }
        
        JButton btnModifier = new JButton("Modifier");
        btnModifier.setFont(new Font("Arial", Font.PLAIN, 11));
        btnModifier.addActionListener(e -> {
            parentDialog.dispose();
            ouvrirFormulaireAdresse(adresse);
        });
        
        JButton btnSupprimer = new JButton("Supprimer");
        btnSupprimer.setFont(new Font("Arial", Font.PLAIN, 11));
        btnSupprimer.setBackground(new Color(220, 80, 80));
        btnSupprimer.setForeground(Color.WHITE);
        btnSupprimer.addActionListener(e -> {
            parentDialog.dispose();
            supprimerAdresse(adresse);
        });
        
        panelBoutons.add(btnModifier);
        panelBoutons.add(btnSupprimer);
        
        panel.add(panelBoutons, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * ouvre le formulaire pour ajouter ou modifier une adresse
     */
    private void ouvrirFormulaireAdresse(Adresse adresseExistante) {
        JDialog dialog = new JDialog(this, 
            adresseExistante == null ? "Ajouter une adresse" : "Modifier une adresse", 
            true);
        dialog.setSize(450, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // En-tête
        JPanel panelTitre = new JPanel(new BorderLayout());
        panelTitre.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panelTitre.setBackground(new Color(240, 240, 240));
        
        JLabel lblTitre = new JLabel(adresseExistante == null ? "Ajouter une adresse" : "Modifier une adresse");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 16));
        panelTitre.add(lblTitre, BorderLayout.WEST);
        
        dialog.add(panelTitre, BorderLayout.NORTH);
        
        // Formulaire
        JPanel panelFormulaire = new JPanel(new GridBagLayout());
        panelFormulaire.setBorder(BorderFactory.createEmptyBorder(25, 30, 20, 30));
        panelFormulaire.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Numéro
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblNumero = new JLabel("Numéro:*");
        lblNumero.setFont(new Font("Arial", Font.BOLD, 12));
        panelFormulaire.add(lblNumero, gbc);
        
        JTextField txtNumero = new JTextField(10);
        txtNumero.setFont(new Font("Arial", Font.PLAIN, 13));
        txtNumero.setPreferredSize(new Dimension(200, 28));
        gbc.gridx = 1; gbc.gridy = 0;
        panelFormulaire.add(txtNumero, gbc);
        
        // Rue
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblRue = new JLabel("Voie:*");
        lblRue.setFont(new Font("Arial", Font.BOLD, 12));
        panelFormulaire.add(lblRue, gbc);
        
        JTextField txtRue = new JTextField(20);
        txtRue.setFont(new Font("Arial", Font.PLAIN, 13));
        txtRue.setPreferredSize(new Dimension(250, 28));
        gbc.gridx = 1; gbc.gridy = 1;
        panelFormulaire.add(txtRue, gbc);
        
        // Complément
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblComplement = new JLabel("Complément:");
        lblComplement.setFont(new Font("Arial", Font.BOLD, 12));
        panelFormulaire.add(lblComplement, gbc);
        
        JTextField txtComplement = new JTextField(20);
        txtComplement.setFont(new Font("Arial", Font.PLAIN, 13));
        txtComplement.setPreferredSize(new Dimension(250, 28));
        gbc.gridx = 1; gbc.gridy = 2;
        panelFormulaire.add(txtComplement, gbc);
        
        // Code postal
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblCodePostal = new JLabel("Code postal:*");
        lblCodePostal.setFont(new Font("Arial", Font.BOLD, 12));
        panelFormulaire.add(lblCodePostal, gbc);
        
        JTextField txtCodePostal = new JTextField(10);
        txtCodePostal.setFont(new Font("Arial", Font.PLAIN, 13));
        txtCodePostal.setPreferredSize(new Dimension(100, 28));
        gbc.gridx = 1; gbc.gridy = 3;
        panelFormulaire.add(txtCodePostal, gbc);
        
        // Ville
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblVille = new JLabel("Ville:*");
        lblVille.setFont(new Font("Arial", Font.BOLD, 12));
        panelFormulaire.add(lblVille, gbc);
        
        JTextField txtVille = new JTextField(20);
        txtVille.setFont(new Font("Arial", Font.PLAIN, 13));
        txtVille.setPreferredSize(new Dimension(200, 28));
        gbc.gridx = 1; gbc.gridy = 4;
        panelFormulaire.add(txtVille, gbc);
        
        // Pays
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel lblPays = new JLabel("Pays:");
        lblPays.setFont(new Font("Arial", Font.BOLD, 12));
        panelFormulaire.add(lblPays, gbc);
        
        JTextField txtPays = new JTextField(20);
        txtPays.setFont(new Font("Arial", Font.PLAIN, 13));
        txtPays.setText("France");
        txtPays.setPreferredSize(new Dimension(200, 28));
        gbc.gridx = 1; gbc.gridy = 5;
        panelFormulaire.add(txtPays, gbc);
        
        // Case à cocher pour adresse principale
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JCheckBox chkPrincipale = new JCheckBox("Définir comme adresse principale");
        chkPrincipale.setFont(new Font("Arial", Font.PLAIN, 12));
        chkPrincipale.setBackground(Color.WHITE);
        
        if (adresseExistante != null) {
            chkPrincipale.setSelected(adresseExistante.isEstPrincipale());
        } else {
            // Par défaut, la première adresse est principale
            try {
                List<Adresse> adresses = AdresseDAO.getInstance().getAdressesByUsager(usager.getIdUsager());
                chkPrincipale.setSelected(adresses.isEmpty());
            } catch (Exception e) {
                chkPrincipale.setSelected(true);
            }
        }
        panelFormulaire.add(chkPrincipale, gbc);
        
        dialog.add(panelFormulaire, BorderLayout.CENTER);
        
        // Pré-remplir si modification
        if (adresseExistante != null) {
            txtNumero.setText(adresseExistante.getNumero());
            txtRue.setText(adresseExistante.getRue());
            txtComplement.setText(adresseExistante.getComplement());
            txtCodePostal.setText(adresseExistante.getCodePostal());
            txtVille.setText(adresseExistante.getVille());
            txtPays.setText(adresseExistante.getPays());
        }
        
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelBoutons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        panelBoutons.setBackground(Color.WHITE);
        
        JButton btnValider = new JButton(adresseExistante == null ? "Ajouter l'adresse" : "Modifier l'adresse");
        btnValider.setFont(new Font("Arial", Font.BOLD, 13));
        btnValider.setBackground(new Color(0, 120, 215));
        btnValider.setForeground(Color.WHITE);
        btnValider.setPreferredSize(new Dimension(180, 35));
        btnValider.setFocusPainted(false);
        
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.setFont(new Font("Arial", Font.PLAIN, 13));
        btnAnnuler.setPreferredSize(new Dimension(100, 35));
        
        panelBoutons.add(btnValider);
        panelBoutons.add(btnAnnuler);
        
        btnValider.addActionListener(e -> {
            if (validerFormulaireAdresse(txtNumero, txtRue, txtCodePostal, txtVille)) {
                try {
                    Adresse adresse;
                    if (adresseExistante == null) {
                        adresse = new Adresse();
                        adresse.setIdUsager(usager.getIdUsager());
                    } else {
                        adresse = adresseExistante;
                    }
                    
                    adresse.setNumero(txtNumero.getText().trim());
                    adresse.setRue(txtRue.getText().trim());
                    adresse.setComplement(txtComplement.getText().trim());
                    adresse.setCodePostal(txtCodePostal.getText().trim());
                    adresse.setVille(txtVille.getText().trim());
                    adresse.setPays(txtPays.getText().trim());
                    adresse.setEstPrincipale(chkPrincipale.isSelected());
                    
                    if (adresseExistante == null) {
                        AdresseDAO.getInstance().create(adresse);
                    } else {
                        AdresseDAO.getInstance().update(adresse);
                    }
                    
                    if (adresse.isEstPrincipale()) {
                        AdresseDAO.getInstance().definirAdressePrincipale(adresse.getIdAdresse(), usager.getIdUsager());
                    }
                    
                    JOptionPane.showMessageDialog(dialog,
                        adresseExistante == null ? 
                            "Adresse ajoutée !\n"
                            + "Votre adresse a été enregistrée avec succès." :
                            "Adresse modifiée !\n"
                            + "Votre adresse a été mise à jour avec succès.",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Rafraîchir la page
                    new Page_Utilisateur(emailUtilisateur, true).setVisible(true);
                    dispose();
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog,
                        "Erreur " + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
     
        dialog.getRootPane().setDefaultButton(btnValider);
        dialog.add(panelBoutons, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * valide les champs du formulaire d'adresse
     */
    private boolean validerFormulaireAdresse(JTextField txtNumero, JTextField txtRue, 
                                            JTextField txtCodePostal, JTextField txtVille) {
        if (txtNumero.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le numéro est obligatoire", "Champ manquant", JOptionPane.WARNING_MESSAGE);
            txtNumero.requestFocus();
            return false;
        }
        
        if (txtRue.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La rue est obligatoire", "Champ manquant", JOptionPane.WARNING_MESSAGE);
            txtRue.requestFocus();
            return false;
        }
        
        if (txtCodePostal.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le code postal est obligatoire", "Champ manquant", JOptionPane.WARNING_MESSAGE);
            txtCodePostal.requestFocus();
            return false;
        }
        
        if (txtVille.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La ville est obligatoire", "Champ manquant", JOptionPane.WARNING_MESSAGE);
            txtVille.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * supprime une adresse de l'utilisateur
     */
    private void supprimerAdresse(Adresse adresse) {
        int confirmation = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir supprimer cette adresse ?\n\n" +
            adresse.getAdresseLigne(),
            "Confirmation de suppression",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                // Si c'est l'adresse principale, demander confirmation supplémentaire
                if (adresse.isEstPrincipale()) {
                    int confirmationPrincipale = JOptionPane.showConfirmDialog(this,
                        "Vous supprimez votre adresse principale.\n" +
                        "Aucune autre adresse ne sera marquée comme principale.\n\n" +
                        "Confirmez-vous la suppression ?",
                        "Suppression adresse principale",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    
                    if (confirmationPrincipale != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                
                AdresseDAO.getInstance().delete(adresse);
                JOptionPane.showMessageDialog(this,
                    "Adresse supprimée avec succès",
                    "Suppression réussie",
                    JOptionPane.INFORMATION_MESSAGE);
                
                rafraichirAffichage();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * définit une adresse comme principale
     */
    private void definirAdressePrincipale(Adresse adresse) {
        try {
            AdresseDAO.getInstance().definirAdressePrincipale(adresse.getIdAdresse(), usager.getIdUsager());
            JOptionPane.showMessageDialog(this,
                "Adresse définie comme principale avec succès",
                "Succès",
                JOptionPane.INFORMATION_MESSAGE);
            
            rafraichirAffichage();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // === ABONNEMENT ===
    
    /**
     * permet de changer l'abonnement actuel de l'utilisateur
     */
    private void changerAbonnement(Abonnement abonnementActuel) {
        String message = "Vous avez actuellement l'abonnement : " + abonnementActuel.getLibelleAbonnement() + "\n\n" +
                        "!!! En changeant d'abonnement, votre abonnement actuel sera résilié.\n" +
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
    
    /**
     * permet de résilier l'abonnement actuel de l'utilisateur
     */
    private void resilierAbonnement(Abonnement abonnementActuel) {
        String message = "!!! ATTENTION !!!\n\n" +
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
    
    // === CARTE TISSEO ===
    
    /**
     * ouvre le popup pour ajouter une carte tisséo
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
                        "Carte Tisséo enregistrée avec succès\n\n" +
                        "Numéro : " + numeroCarte + "\n" +
                        "Vous pouvez maintenant utiliser les parkings relais gratuitement.",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE
                    );
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
     * ouvre le popup pour modifier une carte tisséo existante
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
                return;
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
                        "Carte Tisséo modifiée avec succès\n\n" +
                        "Nouveau numéro : " + numeroCarte,
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE
                    );
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
     * supprime la carte tisséo de l'utilisateur
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
                        "Carte Tisséo supprimée avec succès",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE
                    );
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
     * classe interne pour personnaliser l'affichage des véhicules dans la liste
     */
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

    /**
     * rafraîchit l'affichage en recréant la page
     */
    private void rafraichirAffichage() {
        // Rafraîchir la page en recréant la vue
    	new Page_Utilisateur(emailUtilisateur, true).setVisible(true);
    	dispose();
    }
    
    // === GETTERS POUR LE CONTROLEUR ===
    
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
    
}


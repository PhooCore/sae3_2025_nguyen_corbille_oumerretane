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
import modele.Abonnement;
import modele.Paiement;
import modele.Stationnement;
import modele.Zone;
import modele.Parking;
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
    
    public Page_Utilisateur(String email, boolean rafraichir) {
        this.emailUtilisateur = email;
        if (rafraichir) {
            this.usager = UsagerDAO.getUsagerByEmail(email);
        } else {
            this.usager = UsagerDAO.getUsagerByEmail(email);
        }
        initialisePage();
        new ControleurUtilisateur(this);
    }
    
    public Page_Utilisateur(String email) {
        this(email, false);
    }
    
    private void initialisePage() {
        this.setTitle("Mon Compte");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 650);
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
        
        mainPanel.add(onglets, BorderLayout.CENTER);
        
        // Bouton retour
        btnRetour = new JButton("Retour à l'accueil");
        mainPanel.add(btnRetour, BorderLayout.SOUTH);
        
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
        List<Abonnement> abonnements = AbonnementDAO.getAbonnementsByUsager(usager.getIdUsager());
        
        if (!abonnements.isEmpty()) {
            Abonnement abonnementActif = abonnements.get(0);
            ajouterLigneInfo(panel, "Abonnement:", abonnementActif.getLibelleAbonnement());
            
            java.sql.Date dateDebut = AbonnementDAO.getDateDebutAbonnement(usager.getIdUsager());
            if (dateDebut != null) {
                ajouterLigneInfo(panel, "Depuis le:", dateDebut.toString());
            }
            
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
            lblLibelle.setPreferredSize(new Dimension(100, 25));
            
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
        
        panel.add(Box.createVerticalStrut(30));
        
        // Boutons d'action
        btnModifierMdp = new JButton("Modifier le mot de passe");
        btnModifierMdp.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Le contrôleur gérera cet ActionListener

        btnDeconnexion = new JButton("Déconnexion");
        btnDeconnexion.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDeconnexion.setBackground(new Color(220, 80, 80));
        btnDeconnexion.setForeground(Color.WHITE);
        // Le contrôleur gérera cet ActionListener
        
        panel.add(btnModifierMdp);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnDeconnexion);
        
        return panel;
    }
    
    private JPanel creerOngletPaiements() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Récupération des paiements
        List<Paiement> paiements = PaiementDAO.getPaiementsByUsager(usager.getIdUsager());
        
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
                Abonnement abonnement = AbonnementDAO.getAbonnementById(p.getIdAbonnement());
                details = (abonnement != null) ? abonnement.getLibelleAbonnement() : p.getIdAbonnement();
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
        List<Stationnement> stationnements = StationnementDAO.getHistoriqueStationnements(usager.getIdUsager());
        
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
                    Parking parking = ParkingDAO.getParkingById(zoneId);
                    donnees[i][3] = (parking != null) ? parking.getLibelleParking() : zoneId;
                } else {
                    Zone zone = ZoneDAO.getZoneById(zoneId);
                    donnees[i][3] = (zone != null) ? zone.getLibelleZone() : zoneId;
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
        lblLibelle.setPreferredSize(new Dimension(100, 25)); 
        
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
    
    private void retourAccueil() {
        // Retour à la page principale
        dispose();
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
            boolean supprime = AbonnementDAO.supprimerAbonnementsUtilisateur(usager.getIdUsager());
            
            if (supprime) {
                JOptionPane.showMessageDialog(
                    this,
                    "Votre ancien abonnement a été résilié.\nVous allez être redirigé vers la page des abonnements.",
                    "Abonnement résilié",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                new Page_Abonnements(emailUtilisateur).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors de la résiliation de l'abonnement.\nVeuillez réessayer ou contacter le support.",
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
                boolean supprime = AbonnementDAO.supprimerAbonnementsUtilisateur(usager.getIdUsager());
                
                if (supprime) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Votre abonnement a été résilié avec succès.\n\n" +
                        "Vous pouvez souscrire à un nouvel abonnement à tout moment.",
                        "Résiliation confirmée",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    new Page_Utilisateur(emailUtilisateur, true).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Erreur lors de la résiliation.\nVeuillez contacter le support.",
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
}
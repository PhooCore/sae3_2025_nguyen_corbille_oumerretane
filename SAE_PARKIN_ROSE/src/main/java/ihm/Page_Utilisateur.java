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

public class Page_Utilisateur extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Usager usager;

    public Page_Utilisateur(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        initialisePage();
    }
    
    private void initialisePage() {
        this.setTitle("Mon Compte");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(700, 600);
        this.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        JLabel lblTitre = new JLabel("Mon Compte", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // Système d'onglets
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
        
        JButton btnRetour = new JButton("Retour à l'accueil");
        btnRetour.addActionListener(e -> retourAccueil());
        mainPanel.add(btnRetour, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
    }
    
    private JPanel creerOngletInfos() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        ajouterLigneInfo(panel, "Nom:", usager.getNomUsager());
        ajouterLigneInfo(panel, "Prénom:", usager.getPrenomUsager());
        ajouterLigneInfo(panel, "Email:", usager.getMailUsager());
        
        panel.add(Box.createVerticalStrut(30));
        
        JButton btnModifierMdp = new JButton("Modifier le mot de passe");
        btnModifierMdp.addActionListener(e -> modifierMotDePasse());
        
        JButton btnHistorique = new JButton("Voir l'historique des stationnements");
        btnHistorique.addActionListener(e -> {
            Page_Historique_Stationnements pageHistorique = new Page_Historique_Stationnements(emailUtilisateur);
            pageHistorique.setVisible(true);
        });
        
        JButton btnDeconnexion = new JButton("Déconnexion");
        btnDeconnexion.setBackground(new Color(220, 80, 80));
        btnDeconnexion.setForeground(Color.WHITE);
        btnDeconnexion.addActionListener(e -> deconnexion());
        
        panel.add(btnModifierMdp);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnHistorique);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnDeconnexion);
        
        return panel;
    }
    
    private JPanel creerOngletHistorique() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        List<Paiement> paiements = PaiementDAO.getPaiementsByUsager(usager.getIdUsager());
        
        String[] colonnes = {"Date", "Montant", "Type", "Statut", "ID Paiement"};
        Object[][] donnees = new Object[paiements.size()][5];
        double totalDepense = 0.0;
        
        for (int i = 0; i < paiements.size(); i++) {
            Paiement p = paiements.get(i);
            donnees[i][0] = p.getDatePaiement().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            donnees[i][1] = String.format("%.2f €", p.getMontant());
            donnees[i][2] = "Stationnement";
            donnees[i][3] = "Payé";
            donnees[i][4] = p.getIdPaiement();
            totalDepense += p.getMontant();
        }
        
        JTable table = new JTable(donnees, colonnes);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setDefaultEditor(Object.class, null);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel panelResume = new JPanel(new GridLayout(1, 3, 10, 0));
        panelResume.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelResume.setBackground(Color.WHITE);
        
        String dernierPaiement = "Aucun";
        if (!paiements.isEmpty()) {
            dernierPaiement = paiements.get(0).getDatePaiement().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        
        ajouterStatistique(panelResume, "Total dépensé", String.format("%.2f €", totalDepense));
        ajouterStatistique(panelResume, "Nombre de paiements", String.valueOf(paiements.size()));
        ajouterStatistique(panelResume, "Dernier paiement", dernierPaiement);
        
        panel.add(panelResume, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel creerOngletStationnements() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        List<Stationnement> stationnements = StationnementDAO.getHistoriqueStationnements(usager.getIdUsager());
        
        String[] colonnes = {"Date", "Type", "Véhicule", "Zone/Parking", "Durée", "Coût", "Statut"};
        Object[][] donnees = new Object[stationnements.size()][7];
        
        for (int i = 0; i < stationnements.size(); i++) {
            Stationnement s = stationnements.get(i);
            donnees[i][0] = s.getDateCreation().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            donnees[i][1] = s.getTypeStationnement();
            donnees[i][2] = s.getTypeVehicule() + " - " + s.getPlaqueImmatriculation();
            donnees[i][3] = s.getZone();
            
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
        long stationnementsActifs = stationnements.stream().filter(s -> "ACTIF".equals(s.getStatut())).count();
        
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
    
    private void modifierMotDePasse() {
        Page_Modif_MDP pageModifMdp = new Page_Modif_MDP();
        pageModifMdp.setVisible(true);
        this.dispose();
    }
    
    private void deconnexion() {
        int confirmation = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir vous déconnecter ?",
            "Déconnexion",
            JOptionPane.YES_NO_OPTION);
            
        if (confirmation == JOptionPane.YES_OPTION) {
            Page_Authentification authPage = new Page_Authentification();
            authPage.setVisible(true);
            dispose();
        }
    }
    
    private void retourAccueil() {
        Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
        pagePrincipale.setVisible(true);
        dispose();
    }
    
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
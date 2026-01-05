package ihm;

import javax.swing.*;
import modele.Abonnement;
import modele.Usager;
import modele.dao.AbonnementDAO;
import modele.dao.UsagerDAO;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;

public class Page_Abonnements extends JFrame {
    
    private String emailUtilisateur;
    private int idUsager;
    private Usager usager;
    private List<Abonnement> abonnements;
    private List<Abonnement> abonnementsFiltres;
    private JPanel panelAbonnements;
    private JComboBox<String> comboTri;
    private JCheckBox checkGratuit, checkMoto, checkAnnuel, checkHebdo;
    private JTextField txtRechercher;
    private JButton rechercheBtn;
    private JLabel lblTitre;
    private JButton btnRetour;
    
    public Page_Abonnements(String email) {
        this.emailUtilisateur = email;
        try {
            this.usager = UsagerDAO.getInstance().findById(email);
            if (usager != null) {
                this.idUsager = usager.getIdUsager();
            } else {
                JOptionPane.showMessageDialog(this, "Utilisateur non trouvé", "Erreur", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        this.abonnementsFiltres = new ArrayList<>();
        initialiserPage();
        
        // Instancier le contrôleur
        new controleur.ControleurAbonnements(this);
    }
    
    private void initialiserPage() {
        setTitle("Abonnements disponibles");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Header avec titre et filtres
        JPanel headerPanel = creerHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Panel pour les cartes d'abonnement
        panelAbonnements = new JPanel(new GridLayout(0, 1, 0, 15));
        panelAbonnements.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(panelAbonnements);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Bouton retour
        btnRetour = new JButton("Retour au compte");
        
        JPanel panelBouton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBouton.setBackground(Color.WHITE);
        panelBouton.add(btnRetour);
        mainPanel.add(panelBouton, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel creerHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(Color.WHITE);
        
        // Panel supérieur avec titre et barre de recherche
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        
        // Titre avec compteur
        lblTitre = new JLabel("Choisissez votre abonnement (0)", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(lblTitre, BorderLayout.CENTER);
        
        // Barre de recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchPanel.setBackground(Color.WHITE);
        
        txtRechercher = new JTextField();
        txtRechercher.setPreferredSize(new Dimension(200, 30));
        txtRechercher.setFont(new Font("Arial", Font.PLAIN, 12));
        txtRechercher.setText("Rechercher un abonnement...");
        txtRechercher.setForeground(Color.GRAY);
        
        rechercheBtn = new JButton("Rechercher");
        rechercheBtn.setPreferredSize(new Dimension(120, 30));
        rechercheBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        rechercheBtn.setFocusPainted(false);
        
        searchPanel.add(txtRechercher);
        searchPanel.add(rechercheBtn);
        
        topPanel.add(searchPanel, BorderLayout.EAST);
        headerPanel.add(topPanel, BorderLayout.NORTH);
        
        // Panel des filtres et tri
        JPanel filtresPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        filtresPanel.setBackground(Color.WHITE);
        
        // Checkboxes de filtrage
        JLabel lblFiltrer = new JLabel("Filtrer:");
        lblFiltrer.setFont(new Font("Arial", Font.BOLD, 13));
        
        checkGratuit = new JCheckBox("Gratuits");
        checkMoto = new JCheckBox("Moto");
        checkAnnuel = new JCheckBox("Annuels");
        checkHebdo = new JCheckBox("Hebdomadaires");
        
        filtresPanel.add(lblFiltrer);
        filtresPanel.add(checkGratuit);
        filtresPanel.add(checkMoto);
        filtresPanel.add(checkAnnuel);
        filtresPanel.add(checkHebdo);
        
        // Séparateur visuel
        filtresPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        
        // ComboBox de tri
        JLabel lblTrier = new JLabel("Trier par:");
        lblTrier.setFont(new Font("Arial", Font.BOLD, 13));
        
        comboTri = new JComboBox<>(new String[]{
            "Prix croissant",
            "Prix décroissant",
            "Ordre alphabétique (A-Z)",
            "Ordre alphabétique (Z-A)"
        });
        
        filtresPanel.add(lblTrier);
        filtresPanel.add(comboTri);
        
        headerPanel.add(filtresPanel, BorderLayout.SOUTH);
        
        return headerPanel;
    }
    
    // Méthodes pour mettre à jour l'affichage depuis le contrôleur
    public void mettreAJourAffichageAbonnements(List<Abonnement> abonnements) {
        panelAbonnements.removeAll();
        
        for (Abonnement abonnement : abonnements) {
            panelAbonnements.add(creerCarteAbonnement(abonnement));
        }
        
        if (abonnements.isEmpty()) {
            String message = getRechercheTexte().isEmpty() || 
                           getRechercheTexte().equals("Rechercher un abonnement...") ?
                    "Aucun abonnement ne correspond à vos critères de filtrage" :
                    "Aucun abonnement ne correspond à \"" + getRechercheTexte() + "\"";
            
            JLabel lblAucun = new JLabel("<html><center>" + message + "<br>Tentez d'autres critères de recherche</center></html>", SwingConstants.CENTER);
            lblAucun.setFont(new Font("Arial", Font.ITALIC, 16));
            lblAucun.setForeground(Color.GRAY);
            lblAucun.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
            panelAbonnements.add(lblAucun);
        }
        
        panelAbonnements.revalidate();
        panelAbonnements.repaint();
    }
    
    public void mettreAJourTitre(int nombreAbonnements) {
        lblTitre.setText("Choisissez votre abonnement (" + nombreAbonnements + ")");
    }
    
    private JPanel creerCarteAbonnement(Abonnement abonnement) {
        JPanel carte = new JPanel(new BorderLayout(15, 10));
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        carte.setBackground(Color.WHITE);
        
        JPanel panelInfo = new JPanel(new GridLayout(0, 1, 5, 5));
        panelInfo.setBackground(Color.WHITE);
        
        JLabel lblTitre = new JLabel(abonnement.getLibelleAbonnement());
        lblTitre.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitre.setForeground(new Color(0, 100, 200));
        
        JLabel lblTarif = new JLabel(String.format("%.2f €", abonnement.getTarifAbonnement()));
        lblTarif.setFont(new Font("Arial", Font.BOLD, 18));
        
        if (abonnement.getTarifAbonnement() == 0) {
            lblTarif.setForeground(new Color(0, 180, 0));
            lblTarif.setText("GRATUIT");
        } else {
            lblTarif.setForeground(new Color(0, 150, 0));
        }
        
        JLabel lblId = new JLabel("Code : " + abonnement.getIdAbonnement());
        lblId.setFont(new Font("Arial", Font.ITALIC, 12));
        lblId.setForeground(Color.GRAY);
        
        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        badgesPanel.setBackground(Color.WHITE);
        
        String idUpper = abonnement.getIdAbonnement().toUpperCase();
        if (idUpper.contains("ANNUEL")) {
            JLabel badge = new JLabel("Annuel");
            badge.setFont(new Font("Arial", Font.PLAIN, 11));
            badge.setForeground(new Color(0, 100, 200));
            badgesPanel.add(badge);
        }
        if (idUpper.contains("HEBDO") || idUpper.contains("SEMAINE")) {
            JLabel badge = new JLabel("Hebdomadaire");
            badge.setFont(new Font("Arial", Font.PLAIN, 11));
            badge.setForeground(new Color(0, 100, 200));
            badgesPanel.add(badge);
        }
        if (idUpper.contains("MOTO")) {
            JLabel badge = new JLabel("Ⓜ Moto");
            badge.setFont(new Font("Arial", Font.PLAIN, 11));
            badge.setForeground(new Color(100, 100, 100));
            badgesPanel.add(badge);
        }
        if (idUpper.contains("RESIDENT")) {
            JLabel badge = new JLabel("⛫ Résident");
            badge.setFont(new Font("Arial", Font.PLAIN, 11));
            badge.setForeground(new Color(150, 75, 0));
            badgesPanel.add(badge);
        }
        if (idUpper.contains("ELECTRIQUE")) {
            JLabel badge = new JLabel("⚡ Électrique");
            badge.setFont(new Font("Arial", Font.PLAIN, 11));
            badge.setForeground(new Color(0, 150, 0));
            badgesPanel.add(badge);
        }
        
        panelInfo.add(lblTitre);
        panelInfo.add(lblTarif);
        panelInfo.add(lblId);
        panelInfo.add(badgesPanel);
        
        // Partie droite : Bouton de sélection
        String texteBouton;
        boolean estActif;
        
        try {
            boolean hasAbonnement = AbonnementDAO.getInstance().getAbonnementsByUsager(idUsager)
                .stream().anyMatch(a -> a.getIdAbonnement().equals(abonnement.getIdAbonnement()));
            
            if (hasAbonnement) {
                texteBouton = "Déjà souscrit";
                estActif = false;
            } else {
                texteBouton = "Choisir cet abonnement";
                estActif = true;
            }
        } catch (SQLException e) {
            texteBouton = "Choisir cet abonnement";
            estActif = true;
        }
        
        JButton btnChoisir = new JButton(texteBouton);
        if (estActif) {
            btnChoisir.setBackground(new Color(0, 120, 215));
            btnChoisir.setForeground(Color.WHITE);
        } else {
            btnChoisir.setBackground(Color.GRAY);
            btnChoisir.setForeground(Color.WHITE);
            btnChoisir.setEnabled(false);
        }
        btnChoisir.setFont(new Font("Arial", Font.BOLD, 14));
        btnChoisir.setFocusPainted(false);
        
        JPanel panelBouton = new JPanel(new BorderLayout());
        panelBouton.setBackground(Color.WHITE);
        panelBouton.add(btnChoisir, BorderLayout.CENTER);
        
        carte.add(panelInfo, BorderLayout.CENTER);
        carte.add(panelBouton, BorderLayout.EAST);
        
        return carte;
    }
    
    // Getters pour le contrôleur
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }
    
    public int getIdUsager() {
        return idUsager;
    }
    
    public Usager getUsager() {
        return usager;
    }
    
    public List<Abonnement> getAbonnements() {
        return abonnements;
    }
    
    public List<Abonnement> getAbonnementsFiltres() {
        return abonnementsFiltres;
    }
    
    public JButton getBtnRetour() {
        return btnRetour;
    }
    
    public JPanel getPanelAbonnements() {
        return panelAbonnements;
    }
    
    public JComboBox<String> getComboTri() {
        return comboTri;
    }
    
    public JCheckBox getCheckGratuit() {
        return checkGratuit;
    }
    
    public JCheckBox getCheckMoto() {
        return checkMoto;
    }
    
    public JCheckBox getCheckAnnuel() {
        return checkAnnuel;
    }
    
    public JCheckBox getCheckHebdo() {
        return checkHebdo;
    }
    
    public JTextField getTxtRechercher() {
        return txtRechercher;
    }
    
    public JButton getRechercheBtn() {
        return rechercheBtn;
    }
    
    public String getRechercheTexte() {
        return txtRechercher.getText().trim();
    }
    
    public boolean isCheckGratuitSelected() {
        return checkGratuit.isSelected();
    }
    
    public boolean isCheckMotoSelected() {
        return checkMoto.isSelected();
    }
    
    public boolean isCheckAnnuelSelected() {
        return checkAnnuel.isSelected();
    }
    
    public boolean isCheckHebdoSelected() {
        return checkHebdo.isSelected();
    }
    
    public String getTriSelectionne() {
        return (String) comboTri.getSelectedItem();
    }
}
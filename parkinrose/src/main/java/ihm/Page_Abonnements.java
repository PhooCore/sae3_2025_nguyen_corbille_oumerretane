package ihm;

import javax.swing.*;
import modele.Abonnement;
import modele.Usager;
import modele.dao.AbonnementDAO;
import modele.dao.UsagerDAO;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class Page_Abonnements extends JFrame {
    
    private String emailUtilisateur;
    private int idUsager;
    private Usager usager;
    private JPanel panelAbonnements;
    private JComboBox<String> comboTri;
    private JCheckBox checkGratuit, checkMoto, checkAnnuel, checkHebdo;
    private JTextField txtRechercher;
    private JButton rechercheBtn;
    private JLabel lblTitre;
    private JButton btnRetour;
    
    /**
     * constructeur de la page d'abonnements pour un utilisateur donné
     */
    public Page_Abonnements(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        if (usager != null) {
            this.idUsager = usager.getIdUsager();
        } else {
            JOptionPane.showMessageDialog(this, "Utilisateur non trouvé", "Erreur", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        initialiserPage();
        
        new controleur.ControleurAbonnements(this);
    }
    
    /**
     * initialise tous les composants graphiques de la page d'abonnements
     */
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
        panelAbonnements = new JPanel();
        panelAbonnements.setLayout(new BoxLayout(panelAbonnements, BoxLayout.Y_AXIS));
        panelAbonnements.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(panelAbonnements);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        btnRetour = new JButton("Retour au compte");
        
        JPanel panelBouton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBouton.setBackground(Color.WHITE);
        panelBouton.add(btnRetour);
        mainPanel.add(panelBouton, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    /**
     * crée le panneau d'en-tête avec le titre, la barre de recherche et les filtres
     */
    private JPanel creerHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(Color.WHITE);
        
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        
        lblTitre = new JLabel("Choisissez votre abonnement (0)", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(lblTitre, BorderLayout.CENTER);
        
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
        
        filtresPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        
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
    
    /**
     * met à jour le titre avec le nombre d'abonnements affichés
     */
    public void mettreAJourTitre(int nombreAbonnements) {
        lblTitre.setText("Choisissez votre abonnement (" + nombreAbonnements + ")");
    }
    
    // === GETTERS POUR LE CONTROLEUR ===
    
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }
    public int getIdUsager() {
        return idUsager;
    }
    public Usager getUsager() {
        return usager;
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
    public String getTriSelectionne() {
        return (String) comboTri.getSelectedItem();
    }
    
    // === CHECKBOXES POUR LE CONTROLEUR ===

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
  
}
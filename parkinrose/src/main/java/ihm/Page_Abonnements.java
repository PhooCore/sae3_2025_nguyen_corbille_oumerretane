package ihm;

import javax.swing.*;
import modele.Abonnement;
import modele.dao.AbonnementDAO;
import modele.dao.UsagerDAO;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class Page_Abonnements extends JFrame {
    
    private String emailUtilisateur;
    private int idUsager;
    private List<Abonnement> abonnements;
    private List<Abonnement> abonnementsFiltres;
    private JPanel panelAbonnements;
    private JComboBox<String> comboTri;
    private JCheckBox checkGratuit, checkMoto, checkAnnuel, checkHebdo;
    private JTextField txtRechercher;
    private JButton rechercheBtn;
    
    // Add these as instance variables to store references
    private JPanel headerPanel;
    private JLabel lblTitre;
    private JButton btnRetour; // Ajout de la référence au bouton
    
    public Page_Abonnements(String email) {
        this.emailUtilisateur = email;
        this.idUsager = UsagerDAO.getUsagerByEmail(email).getIdUsager();
        this.abonnements = AbonnementDAO.getAllAbonnements();
        this.abonnementsFiltres = new ArrayList<>(abonnements);
        initialiserPage();
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
        headerPanel = creerHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Panel pour les cartes d'abonnement
        panelAbonnements = new JPanel(new GridLayout(0, 1, 0, 15));
        panelAbonnements.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(panelAbonnements);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        afficherAbonnements();
        
        // Bouton retour
        btnRetour = new JButton("Retour au compte"); // Initialisation du bouton
        btnRetour.addActionListener(e -> {
            new Page_Utilisateur(emailUtilisateur, true).setVisible(true);
            dispose();
        });
        
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
        lblTitre = new JLabel("Choisissez votre abonnement (" + abonnementsFiltres.size() + ")", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(lblTitre, BorderLayout.CENTER);
        
        // Barre de recherche - Inspirée de Page_Accueil
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchPanel.setBackground(Color.WHITE);
        
        txtRechercher = new JTextField();
        txtRechercher.setPreferredSize(new Dimension(200, 30));
        txtRechercher.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Ajout du texte par défaut
        txtRechercher.setText("Rechercher un abonnement...");
        txtRechercher.setForeground(Color.GRAY);
        
        // Gestion du focus pour le texte par défaut
        txtRechercher.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtRechercher.getText().equals("Rechercher un abonnement...")) {
                    txtRechercher.setText("");
                    txtRechercher.setForeground(Color.BLACK);
                }
            }
            
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtRechercher.getText().isEmpty()) {
                    txtRechercher.setText("Rechercher un abonnement...");
                    txtRechercher.setForeground(Color.GRAY);
                }
            }
        });
        
        // Bouton de recherche avec icône
        rechercheBtn = new JButton("🔍 Rechercher");
        rechercheBtn.setPreferredSize(new Dimension(120, 30));
        rechercheBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        rechercheBtn.setFocusPainted(false);
        rechercheBtn.addActionListener(e -> appliquerFiltres());
        
        // Recherche automatique à l'appui sur Entrée
        txtRechercher.addActionListener(e -> appliquerFiltres());
        
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
        
        checkGratuit.addActionListener(e -> appliquerFiltres());
        checkMoto.addActionListener(e -> appliquerFiltres());
        checkAnnuel.addActionListener(e -> appliquerFiltres());
        checkHebdo.addActionListener(e -> appliquerFiltres());
        
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
        comboTri.addActionListener(e -> appliquerFiltres());
        
        filtresPanel.add(lblTrier);
        filtresPanel.add(comboTri);
        
        headerPanel.add(filtresPanel, BorderLayout.SOUTH);
        
        return headerPanel;
    }
    
    private void appliquerFiltres() {
        // Réinitialiser la liste filtrée
        abonnementsFiltres = new ArrayList<>(abonnements);
        
        // Récupérer le texte de recherche
        String rechercheTexte = txtRechercher.getText().trim();
        
        // Appliquer la recherche textuelle si différente du texte par défaut
        if (!rechercheTexte.isEmpty() && !rechercheTexte.equals("Rechercher un abonnement...")) {
            String rechercheLower = rechercheTexte.toLowerCase();
            abonnementsFiltres.removeIf(a -> 
                !a.getLibelleAbonnement().toLowerCase().contains(rechercheLower) &&
                !a.getIdAbonnement().toLowerCase().contains(rechercheLower)
            );
        }
        
        // Appliquer les filtres par catégorie
        if (checkGratuit.isSelected()) {
            abonnementsFiltres.removeIf(a -> a.getTarifAbonnement() > 0);
        }
        
        if (checkMoto.isSelected()) {
            abonnementsFiltres.removeIf(a -> !a.getIdAbonnement().toUpperCase().contains("MOTO"));
        }
        
        if (checkAnnuel.isSelected()) {
            abonnementsFiltres.removeIf(a -> !a.getIdAbonnement().toUpperCase().contains("ANNUEL"));
        }
        
        if (checkHebdo.isSelected()) {
            abonnementsFiltres.removeIf(a -> !a.getIdAbonnement().toUpperCase().contains("HEBDO") 
                                          && !a.getIdAbonnement().toUpperCase().contains("SEMAINE"));
        }
        
        // Appliquer le tri
        String triSelectionne = (String) comboTri.getSelectedItem();
        switch (triSelectionne) {
            case "Prix croissant":
                abonnementsFiltres.sort(Comparator.comparingDouble(Abonnement::getTarifAbonnement));
                break;
            case "Prix décroissant":
                abonnementsFiltres.sort(Comparator.comparingDouble(Abonnement::getTarifAbonnement).reversed());
                break;
            case "Ordre alphabétique (A-Z)":
                abonnementsFiltres.sort(Comparator.comparing(Abonnement::getLibelleAbonnement));
                break;
            case "Ordre alphabétique (Z-A)":
                abonnementsFiltres.sort(Comparator.comparing(Abonnement::getLibelleAbonnement).reversed());
                break;
        }
        
        // Mettre à jour le titre avec le nouveau compteur
        lblTitre.setText("Choisissez votre abonnement (" + abonnementsFiltres.size() + ")");
        
        // Réafficher les abonnements
        afficherAbonnements();
    }
    
    private void afficherAbonnements() {
        // Vider le panel actuel
        panelAbonnements.removeAll();
        
        // Ajouter chaque abonnement filtré
        for (Abonnement abonnement : abonnementsFiltres) {
            panelAbonnements.add(creerCarteAbonnement(abonnement));
        }
        
        // Si aucun abonnement ne correspond aux filtres
        if (abonnementsFiltres.isEmpty()) {
            String rechercheTexte = txtRechercher.getText().trim();
            String message;
            
            if (!rechercheTexte.isEmpty() && !rechercheTexte.equals("Rechercher un abonnement...")) {
                message = "Aucun abonnement ne correspond à \"" + rechercheTexte + "\"";
            } else {
                message = "Aucun abonnement ne correspond à vos critères de filtrage";
            }
            
            JLabel lblAucun = new JLabel("<html><center>" + message + "<br>Tentez d'autres critères de recherche</center></html>", SwingConstants.CENTER);
            lblAucun.setFont(new Font("Arial", Font.ITALIC, 16));
            lblAucun.setForeground(Color.GRAY);
            lblAucun.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
            panelAbonnements.add(lblAucun);
        }
        
        // Rafraîchir l'affichage
        panelAbonnements.revalidate();
        panelAbonnements.repaint();
    }
    
    private JPanel creerCarteAbonnement(Abonnement abonnement) {
        JPanel carte = new JPanel(new BorderLayout(15, 10));
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        carte.setBackground(Color.WHITE);
        
        // Partie gauche : Informations de base
        JPanel panelInfo = new JPanel(new GridLayout(0, 1, 5, 5));
        panelInfo.setBackground(Color.WHITE);
        
        JLabel lblTitre = new JLabel(abonnement.getLibelleAbonnement());
        lblTitre.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitre.setForeground(new Color(0, 100, 200));
        
        JLabel lblTarif = new JLabel(String.format("%.2f €", abonnement.getTarifAbonnement()));
        lblTarif.setFont(new Font("Arial", Font.BOLD, 18));
        
        // Couleur verte pour les abonnements gratuits, sinon vert foncé
        if (abonnement.getTarifAbonnement() == 0) {
            lblTarif.setForeground(new Color(0, 180, 0));
            lblTarif.setText("GRATUIT");
        } else {
            lblTarif.setForeground(new Color(0, 150, 0));
        }
        
        JLabel lblId = new JLabel("Code : " + abonnement.getIdAbonnement());
        lblId.setFont(new Font("Arial", Font.ITALIC, 12));
        lblId.setForeground(Color.GRAY);
        
        // Badges pour catégories
        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        badgesPanel.setBackground(Color.WHITE);
        
        String idUpper = abonnement.getIdAbonnement().toUpperCase();
        if (idUpper.contains("ANNUEL")) {
            JLabel badge = new JLabel("📅 Annuel");
            badge.setFont(new Font("Arial", Font.PLAIN, 11));
            badge.setForeground(new Color(0, 100, 200));
            badgesPanel.add(badge);
        }
        if (idUpper.contains("HEBDO") || idUpper.contains("SEMAINE")) {
            JLabel badge = new JLabel("📆 Hebdomadaire");
            badge.setFont(new Font("Arial", Font.PLAIN, 11));
            badge.setForeground(new Color(0, 100, 200));
            badgesPanel.add(badge);
        }
        if (idUpper.contains("MOTO")) {
            JLabel badge = new JLabel("🏍️ Moto");
            badge.setFont(new Font("Arial", Font.PLAIN, 11));
            badge.setForeground(new Color(100, 100, 100));
            badgesPanel.add(badge);
        }
        if (idUpper.contains("RESIDENT")) {
            JLabel badge = new JLabel("🏠 Résident");
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
        JButton btnChoisir = new JButton("Choisir cet abonnement");
        btnChoisir.setActionCommand("SOUSCRIRE_" + abonnement.getIdAbonnement()); // Ajout de l'action command
        btnChoisir.setBackground(new Color(0, 120, 215));
        btnChoisir.setForeground(Color.WHITE);
        btnChoisir.setFont(new Font("Arial", Font.BOLD, 14));
        btnChoisir.setFocusPainted(false);
        
        // Vérifier si l'utilisateur a déjà cet abonnement
        if (AbonnementDAO.hasAbonnement(idUsager, abonnement.getIdAbonnement())) {
            btnChoisir.setText("Déjà souscrit");
            btnChoisir.setEnabled(false);
            btnChoisir.setBackground(Color.GRAY);
        } else {
            // Stocker le tarif pour l'utiliser dans l'actionListener
            double tarif = abonnement.getTarifAbonnement();
            
            btnChoisir.addActionListener(e -> {
                if (tarif == 0) {
                    // Afficher popup de confirmation pour abonnement gratuit
                    int confirmation = JOptionPane.showConfirmDialog(
                        this,
                        "Voulez-vous vraiment souscrire à l'abonnement gratuit :\n\n" +
                        "\"" + abonnement.getLibelleAbonnement() + "\"\n" +
                        "Code : " + abonnement.getIdAbonnement() + "\n\n" +
                        "Cet abonnement sera activé immédiatement sans frais.",
                        "Confirmation d'abonnement gratuit",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    
                    if (confirmation == JOptionPane.YES_OPTION) {
                        // Ajouter l'abonnement gratuit à l'utilisateur
                        boolean success = AbonnementDAO.ajouterAbonnementUtilisateur(idUsager, abonnement.getIdAbonnement());
                        
                        if (success) {
                            JOptionPane.showMessageDialog(
                                this,
                                "✅ Abonnement souscrit avec succès !\n" +
                                "Votre abonnement \"" + abonnement.getLibelleAbonnement() + "\" est maintenant actif.",
                                "Abonnement activé",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            
                            // Mettre à jour l'affichage (le bouton deviendra "Déjà souscrit")
                            appliquerFiltres();
                            new Page_Utilisateur(emailUtilisateur, true).setVisible(true);
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(
                                this,
                                "❌ Une erreur est survenue lors de la souscription.",
                                "Erreur",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                } else {
                    // Pour les abonnements payants, ouvrir la page de paiement
                    new Page_Paiement_Abonnement(emailUtilisateur, abonnement).setVisible(true);
                    dispose();
                }
            });
        }
        
        JPanel panelBouton = new JPanel(new BorderLayout());
        panelBouton.setBackground(Color.WHITE);
        panelBouton.add(btnChoisir, BorderLayout.CENTER);
        
        carte.add(panelInfo, BorderLayout.CENTER);
        carte.add(panelBouton, BorderLayout.EAST);
        
        return carte;
    }
    
    // Getters pour les tests ou extensions
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }
    
    public int getIdUsager() {
        return idUsager;
    }
    
    public List<Abonnement> getAbonnements() {
        return abonnements;
    }
    
    public List<Abonnement> getAbonnementsFiltres() {
        return abonnementsFiltres;
    }
    
    // GETTERS POUR LE CONTROLEUR
    public JButton getBtnRetour() {
        return btnRetour;
    }
    
    // Méthode pour obtenir l'utilisateur (si nécessaire pour le contrôleur)
    public modele.Usager getUsager() {
        return modele.dao.UsagerDAO.getUsagerByEmail(emailUtilisateur);
    }
}
package ihm;

import modele.Usager;
import modele.dao.UsagerDAO;
import controleur.ControleurGestionUtilisateurs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.List;

public class PageGestionUtilisateurs extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // Composants
    private JTextField txtRecherche;
    private JTable tableUtilisateurs;
    private DefaultTableModel tableModel;
    private List<Usager> utilisateursCourants;
    
    // Boutons
    private JButton btnRechercher;
    private JButton btnActualiser;
    private JButton btnNouveau;
    private JButton btnModifier;
    private JButton btnSupprimer;
    private JButton btnVoirVehicules;
    private JButton btnVoirAbonnements;
    private JButton btnVoirStationnements;
    private JButton btnRetour;
    
    private String emailAdmin;
    private UsagerDAO usagerDAO;
    
    // Constructeur avec email admin
    public PageGestionUtilisateurs(String emailAdmin) {
        this.emailAdmin = emailAdmin;
        this.usagerDAO = UsagerDAO.getInstance();
        
        // Vérifier les droits administrateur
        if (!verifierDroitsAdmin()) {
            return;
        }
        
        initialisePage();
        new ControleurGestionUtilisateurs(this);
        chargerUtilisateurs();
        setVisible(true);
    }
    
    private boolean verifierDroitsAdmin() {
        if (emailAdmin == null) {
            return demanderAuthentificationAdmin();
        }
        
        // Utiliser la méthode statique qui ne déclare pas SQLException
        Usager admin = UsagerDAO.getUsagerByEmail(emailAdmin);
        if (admin == null) {
            JOptionPane.showMessageDialog(null,
                "Utilisateur non trouvé.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!admin.isAdmin()) {
            JOptionPane.showMessageDialog(null,
                "Vous n'avez pas les droits administrateur nécessaires.",
                "Accès non autorisé",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private boolean demanderAuthentificationAdmin() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblEmail = new JLabel("Email admin:");
        JTextField txtEmail = new JTextField(20);
        
        JLabel lblMdp = new JLabel("Mot de passe:");
        JPasswordField txtMdp = new JPasswordField(20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(lblEmail, gbc);
        gbc.gridx = 1;
        panel.add(txtEmail, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(lblMdp, gbc);
        gbc.gridx = 1;
        panel.add(txtMdp, gbc);
        
        int result = JOptionPane.showConfirmDialog(null, panel,
            "Authentification administrateur",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String email = txtEmail.getText().trim();
            String mdp = new String(txtMdp.getPassword());
            
            Usager admin = UsagerDAO.getUsagerByEmail(email);
            if (admin != null && admin.isAdmin() && admin.getMotDePasse().equals(mdp)) {
                this.emailAdmin = email;
                return true;
            } else {
                JOptionPane.showMessageDialog(null,
                    "Authentification échouée. Email ou mot de passe incorrect.",
                    "Échec d'authentification",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        return false;
    }
    
    private void initialisePage() {
        // Configuration de base de la fenêtre
        setTitle("Gestion des Utilisateurs - Administration");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Barre de recherche
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        txtRecherche = new JTextField(30);
        btnRechercher = new JButton("Rechercher");
        searchPanel.add(new JLabel("Recherche par nom, prénom ou email:"), BorderLayout.WEST);
        searchPanel.add(txtRecherche, BorderLayout.CENTER);
        searchPanel.add(btnRechercher, BorderLayout.EAST);
        
        // Table des utilisateurs
        String[] colonnes = {"ID", "Nom", "Prénom", "Email", "Admin", "Carte Tisséo"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Boolean.class;
                return String.class;
            }
        };
        
        tableUtilisateurs = new JTable(tableModel);
        tableUtilisateurs.setAutoCreateRowSorter(true);
        tableUtilisateurs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableUtilisateurs.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(tableUtilisateurs);
        scrollPane.setPreferredSize(new Dimension(0, 400));
        
        // Panel des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnActualiser = new JButton("Actualiser");
        btnNouveau = new JButton("Nouvel utilisateur");
        btnModifier = new JButton("Modifier");
        btnSupprimer = new JButton("Changer statut admin");
        btnVoirVehicules = new JButton("Gérer véhicules");
        btnVoirAbonnements = new JButton("Gérer abonnements");
        btnVoirStationnements = new JButton("Voir stationnements");
        btnRetour = new JButton("Retour");
        
        // Styliser les boutons
        btnNouveau.setBackground(new Color(30, 144, 255));
        btnNouveau.setForeground(Color.WHITE);
        btnModifier.setBackground(new Color(255, 140, 0));
        btnModifier.setForeground(Color.WHITE);
        btnSupprimer.setBackground(new Color(220, 20, 60));
        btnSupprimer.setForeground(Color.WHITE);
        btnRetour.setBackground(Color.GRAY);
        btnRetour.setForeground(Color.WHITE);
        
        buttonPanel.add(btnActualiser);
        buttonPanel.add(btnNouveau);
        buttonPanel.add(btnModifier);
        buttonPanel.add(btnSupprimer);
        buttonPanel.add(btnVoirVehicules);
        buttonPanel.add(btnVoirAbonnements);
        buttonPanel.add(btnVoirStationnements);
        buttonPanel.add(btnRetour);
        
        // Assembler les composants
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    // ========== MÉTHODES PUBLIQUES POUR LE CONTRÔLEUR ==========
    
    public void chargerUtilisateurs() {
        try {
            tableModel.setRowCount(0);
            utilisateursCourants = usagerDAO.findAll(); // Utiliser l'instance
            
            for (Usager usager : utilisateursCourants) {
                // Utiliser la méthode statique pour la carte Tisséo
                String carteTisseo = usager.getNumeroCarteTisseo();
                tableModel.addRow(new Object[]{
                    usager.getIdUsager(),
                    usager.getNomUsager(),
                    usager.getPrenomUsager(),
                    usager.getMailUsager(),
                    usager.isAdmin(),
                    carteTisseo != null && !carteTisseo.isEmpty() ? carteTisseo : "Non renseignée"
                });
            }
            afficherInformation("Liste des utilisateurs chargée avec succès");
        } catch (SQLException e) {
            afficherErreur("Erreur lors du chargement des utilisateurs: " + e.getMessage());
        }
    }
    
    public void rechercherUtilisateurs() {
        String recherche = txtRecherche.getText().trim().toLowerCase();
        
        if (recherche.isEmpty()) {
            chargerUtilisateurs();
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            
            for (Usager usager : utilisateursCourants) {
                if (usager.getNomUsager().toLowerCase().contains(recherche) ||
                    usager.getPrenomUsager().toLowerCase().contains(recherche) ||
                    usager.getMailUsager().toLowerCase().contains(recherche)) {
                    
                    String carteTisseo = usager.getNumeroCarteTisseo();
                    tableModel.addRow(new Object[]{
                        usager.getIdUsager(),
                        usager.getNomUsager(),
                        usager.getPrenomUsager(),
                        usager.getMailUsager(),
                        usager.isAdmin(),
                        carteTisseo != null && !carteTisseo.isEmpty() ? carteTisseo : "Non renseignée"
                    });
                }
            }
            
            if (tableModel.getRowCount() == 0) {
                afficherInformation("Aucun utilisateur trouvé pour: " + recherche);
            }
        } catch (Exception e) {
            afficherErreur("Erreur lors de la recherche: " + e.getMessage());
        }
    }
    
    public void afficherFormulaireNouvelUtilisateur() {
        JDialog dialog = new JDialog(this, "Nouvel utilisateur", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Champs du formulaire
        JLabel lblNom = new JLabel("Nom:*");
        JTextField txtNom = new JTextField(20);
        
        JLabel lblPrenom = new JLabel("Prénom:*");
        JTextField txtPrenom = new JTextField(20);
        
        JLabel lblEmail = new JLabel("Email:*");
        JTextField txtEmail = new JTextField(20);
        
        JLabel lblMdp = new JLabel("Mot de passe:*");
        JPasswordField txtMdp = new JPasswordField(20);
        
        JCheckBox chkAdmin = new JCheckBox("Administrateur");
        
        // Ajout des composants
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(lblNom, gbc);
        gbc.gridx = 1;
        dialog.add(txtNom, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(lblPrenom, gbc);
        gbc.gridx = 1;
        dialog.add(txtPrenom, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(lblEmail, gbc);
        gbc.gridx = 1;
        dialog.add(txtEmail, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(lblMdp, gbc);
        gbc.gridx = 1;
        dialog.add(txtMdp, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        dialog.add(chkAdmin, gbc);
        
        // Boutons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        gbc.gridx = 1;
        JButton btnValider = new JButton("Créer");
        btnValider.setBackground(new Color(30, 144, 255));
        btnValider.setForeground(Color.WHITE);
        btnValider.addActionListener(e -> {
            // Validation
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String email = txtEmail.getText().trim();
            String mdp = new String(txtMdp.getPassword());
            
            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
                afficherErreur("Tous les champs obligatoires (*) doivent être remplis");
                return;
            }
            
            if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                afficherErreur("Format d'email invalide");
                return;
            }
            
            try {
                // Vérifier si l'email existe déjà
                if (usagerDAO.emailExiste(email)) {
                    afficherErreur("Cet email est déjà utilisé");
                    return;
                }
                
                // Créer l'utilisateur
                Usager nouvelUsager = new Usager();
                nouvelUsager.setNomUsager(nom);
                nouvelUsager.setPrenomUsager(prenom);
                nouvelUsager.setMailUsager(email);
                nouvelUsager.setMotDePasse(mdp);
                nouvelUsager.setAdmin(chkAdmin.isSelected());
                
                usagerDAO.create(nouvelUsager);
                afficherInformation("Utilisateur créé avec succès");
                chargerUtilisateurs();
                dialog.dispose();
            } catch (SQLException ex) {
                afficherErreur("Erreur lors de la création de l'utilisateur: " + ex.getMessage());
            }
        });
        
        JPanel panelBoutons = new JPanel(new FlowLayout());
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnValider);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        dialog.add(panelBoutons, gbc);
        
        dialog.pack();
        dialog.setVisible(true);
    }
    
    public void modifierUtilisateur() {
        Usager usager = getUsagerSelectionne();
        if (usager == null) {
            afficherInformation("Veuillez sélectionner un utilisateur");
            return;
        }
        
        JDialog dialog = new JDialog(this, "Modifier l'utilisateur", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Champs du formulaire
        JLabel lblNom = new JLabel("Nom:");
        JTextField txtNom = new JTextField(usager.getNomUsager(), 20);
        
        JLabel lblPrenom = new JLabel("Prénom:");
        JTextField txtPrenom = new JTextField(usager.getPrenomUsager(), 20);
        
        JLabel lblEmail = new JLabel("Email:");
        JTextField txtEmail = new JTextField(usager.getMailUsager(), 20);
        
        JLabel lblMdp = new JLabel("Nouveau mot de passe:");
        JPasswordField txtMdp = new JPasswordField(20);
        txtMdp.setToolTipText("Laissez vide pour ne pas modifier");
        
        JCheckBox chkAdmin = new JCheckBox("Administrateur", usager.isAdmin());
        
        // Ajout des composants
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(lblNom, gbc);
        gbc.gridx = 1;
        dialog.add(txtNom, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(lblPrenom, gbc);
        gbc.gridx = 1;
        dialog.add(txtPrenom, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(lblEmail, gbc);
        gbc.gridx = 1;
        dialog.add(txtEmail, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(lblMdp, gbc);
        gbc.gridx = 1;
        dialog.add(txtMdp, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        dialog.add(chkAdmin, gbc);
        
        // Boutons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        gbc.gridx = 1;
        JButton btnValider = new JButton("Enregistrer");
        btnValider.setBackground(new Color(30, 144, 255));
        btnValider.setForeground(Color.WHITE);
        btnValider.addActionListener(e -> {
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String email = txtEmail.getText().trim();
            String mdp = new String(txtMdp.getPassword());
            
            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
                afficherErreur("Nom, prénom et email sont obligatoires");
                return;
            }
            
            if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                afficherErreur("Format d'email invalide");
                return;
            }
            
            try {
                // Vérifier si l'email existe déjà (pour un autre utilisateur)
                if (!email.equals(usager.getMailUsager()) && usagerDAO.emailExiste(email)) {
                    afficherErreur("Cet email est déjà utilisé par un autre utilisateur");
                    return;
                }
                
                // Mettre à jour l'utilisateur
                usager.setNomUsager(nom);
                usager.setPrenomUsager(prenom);
                usager.setMailUsager(email);
                usager.setAdmin(chkAdmin.isSelected());
                
                if (!mdp.isEmpty()) {
                    usager.setMotDePasse(mdp);
                }
                
                usagerDAO.update(usager);
                afficherInformation("Utilisateur modifié avec succès");
                chargerUtilisateurs();
                dialog.dispose();
            } catch (SQLException ex) {
                afficherErreur("Erreur lors de la modification de l'utilisateur: " + ex.getMessage());
            }
        });
        
        JPanel panelBoutons = new JPanel(new FlowLayout());
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnValider);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        dialog.add(panelBoutons, gbc);
        
        dialog.pack();
        dialog.setVisible(true);
    }
    
    // ========== GETTERS POUR LE CONTRÔLEUR ==========
    
    public Usager getUsagerSelectionne() {
        int selectedRow = tableUtilisateurs.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = tableUtilisateurs.convertRowIndexToModel(selectedRow);
            int idUsager = (int) tableModel.getValueAt(modelRow, 0);
            
            for (Usager usager : utilisateursCourants) {
                if (usager.getIdUsager() == idUsager) {
                    return usager;
                }
            }
        }
        return null;
    }
    
    public List<Usager> getUtilisateursCourants() {
        return utilisateursCourants;
    }
    
    public JTextField getTxtRecherche() {
        return txtRecherche;
    }
    
    public JButton getBtnRechercher() {
        return btnRechercher;
    }
    
    public JButton getBtnActualiser() {
        return btnActualiser;
    }
    
    public JButton getBtnNouveau() {
        return btnNouveau;
    }
    
    public JButton getBtnModifier() {
        return btnModifier;
    }
    
    public JButton getBtnSupprimer() {
        return btnSupprimer;
    }
    
    public JButton getBtnVoirVehicules() {
        return btnVoirVehicules;
    }
    
    public JButton getBtnVoirAbonnements() {
        return btnVoirAbonnements;
    }
    
    public JButton getBtnVoirStationnements() {
        return btnVoirStationnements;
    }
    
    public JButton getBtnRetour() {
        return btnRetour;
    }
    
    public UsagerDAO getUsagerDAO() {
        return usagerDAO;
    }
    
    public String getEmailAdmin() {
        return emailAdmin;
    }
    
    // ========== MÉTHODES UTILITAIRES ==========
    
    public void afficherInformation(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void afficherErreur(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
    
    public boolean demanderConfirmation(String message) {
        int choix = JOptionPane.showConfirmDialog(this, message, "Confirmation", 
                                                 JOptionPane.YES_NO_OPTION, 
                                                 JOptionPane.QUESTION_MESSAGE);
        return choix == JOptionPane.YES_OPTION;
    }
}
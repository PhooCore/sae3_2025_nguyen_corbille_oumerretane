package controleur;

import ihm.PageGestionUtilisateurs;
import ihm.Page_Administration;
import modele.Usager;
import modele.VehiculeUsager;
import modele.Abonnement;
import modele.dao.UsagerDAO;
import modele.dao.VehiculeUsagerDAO;
import modele.dao.AbonnementDAO;
import modele.dao.StationnementDAO;
import modele.dao.MySQLConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.*;
import java.util.List;

public class ControleurGestionUtilisateurs implements ActionListener {
    
    // États du contrôleur
    private enum Etat {
        INITIAL,
        CHARGEMENT,
        AFFICHAGE,
        RECHERCHE,
        CREATION,
        MODIFICATION,
        GESTION_VEHICULES,
        GESTION_ABONNEMENTS,
        CONSULTATION_STATIONNEMENTS,
        CHANGEMENT_ADMIN,
        RETOUR,
        ERREUR
    }
    
    // Références
    private PageGestionUtilisateurs vue;
    private Etat etat;
    
    // DAOs
    private UsagerDAO usagerDAO;
    private VehiculeUsagerDAO vehiculeUsagerDAO;
    private AbonnementDAO abonnementDAO;
    
    // Données
    private Usager usagerSelectionne;
    
    public ControleurGestionUtilisateurs(PageGestionUtilisateurs vue) {
        this.vue = vue;
        this.etat = Etat.INITIAL;
        this.usagerDAO = UsagerDAO.getInstance();
        this.vehiculeUsagerDAO = VehiculeUsagerDAO.getInstance();
        this.abonnementDAO = AbonnementDAO.getInstance();
        
        initialiserControleur();
    }
    
    private void initialiserControleur() {
        try {
            configurerListeners();
            chargerUtilisateurs();
            etat = Etat.AFFICHAGE;
        } catch (Exception e) {
            gererErreurInitialisation(e.getMessage());
        }
    }
    
    private void configurerListeners() {
        // Boutons
        vue.getBtnRechercher().addActionListener(this);
        vue.getBtnActualiser().addActionListener(this);
        vue.getBtnNouveau().addActionListener(this);
        vue.getBtnModifier().addActionListener(this);
        vue.getBtnSupprimer().addActionListener(this);
        vue.getBtnVoirVehicules().addActionListener(this);
        vue.getBtnVoirAbonnements().addActionListener(this);
        vue.getBtnVoirStationnements().addActionListener(this);
        vue.getBtnRetour().addActionListener(this);
        
        // Recherche à la frappe
        vue.getTxtRecherche().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (etat == Etat.AFFICHAGE && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    etat = Etat.RECHERCHE;
                    rechercherUtilisateurs();
                    etat = Etat.AFFICHAGE;
                }
            }
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        switch (etat) {
            case AFFICHAGE:
                if (source == vue.getBtnRechercher()) {
                    etat = Etat.RECHERCHE;
                    rechercherUtilisateurs();
                    etat = Etat.AFFICHAGE;
                } else if (source == vue.getBtnActualiser()) {
                    etat = Etat.CHARGEMENT;
                    chargerUtilisateurs();
                    etat = Etat.AFFICHAGE;
                } else if (source == vue.getBtnNouveau()) {
                    etat = Etat.CREATION;
                    afficherFormulaireNouvelUtilisateur();
                } else if (source == vue.getBtnModifier()) {
                    etat = Etat.MODIFICATION;
                    modifierUtilisateur();
                } else if (source == vue.getBtnSupprimer()) {
                    etat = Etat.CHANGEMENT_ADMIN;
                    changerStatutAdmin();
                } else if (source == vue.getBtnVoirVehicules()) {
                    etat = Etat.GESTION_VEHICULES;
                    gererVehicules();
                } else if (source == vue.getBtnVoirAbonnements()) {
                    etat = Etat.GESTION_ABONNEMENTS;
                    gererAbonnements();
                } else if (source == vue.getBtnVoirStationnements()) {
                    etat = Etat.CONSULTATION_STATIONNEMENTS;
                    voirStationnements();
                } else if (source == vue.getBtnRetour()) {
                    etat = Etat.RETOUR;
                    retourAdministration();
                }
                break;
                
            case CREATION:
            case MODIFICATION:
            case CHANGEMENT_ADMIN:
            case GESTION_VEHICULES:
            case GESTION_ABONNEMENTS:
            case CONSULTATION_STATIONNEMENTS:
                // Les traitements sont gérés dans les méthodes spécifiques
                break;
                
            case ERREUR:
                // Ne rien faire en état d'erreur
                break;
        }
    }
    
    private void chargerUtilisateurs() {
        try {
            vue.chargerUtilisateurs();
            vue.afficherInformation("Liste des utilisateurs chargée avec succès");
        } catch (Exception e) {
            gererErreur("Erreur chargement utilisateurs", e.getMessage());
        }
    }
    
    private void rechercherUtilisateurs() {
        vue.rechercherUtilisateurs();
    }
    
    private void afficherFormulaireNouvelUtilisateur() {
        JDialog dialog = new JDialog(vue, "Nouvel utilisateur", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(vue);
        dialog.setLayout(new java.awt.GridBagLayout());
        
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        
        // Champs
        JLabel lblNom = new JLabel("Nom:*");
        JTextField txtNom = new JTextField(20);
        
        JLabel lblPrenom = new JLabel("Prénom:*");
        JTextField txtPrenom = new JTextField(20);
        
        JLabel lblEmail = new JLabel("Email:*");
        JTextField txtEmail = new JTextField(20);
        
        JLabel lblMdp = new JLabel("Mot de passe:*");
        JPasswordField txtMdp = new JPasswordField(20);
        
        JCheckBox chkAdmin = new JCheckBox("Administrateur");
        
        // Layout
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
        btnValider.setBackground(new java.awt.Color(30, 144, 255));
        btnValider.setForeground(java.awt.Color.WHITE);
        btnValider.addActionListener(e -> {
            if (!validerFormulaireNouvelUtilisateur(txtNom, txtPrenom, txtEmail, txtMdp)) {
                return;
            }
            
            creerNouvelUtilisateur(
                txtNom.getText().trim(),
                txtPrenom.getText().trim(),
                txtEmail.getText().trim(),
                new String(txtMdp.getPassword()),
                chkAdmin.isSelected(),
                dialog
            );
        });
        
        JPanel panelBoutons = new JPanel(new java.awt.FlowLayout());
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnValider);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        dialog.add(panelBoutons, gbc);
        
        dialog.pack();
        dialog.setVisible(true);
        etat = Etat.AFFICHAGE;
    }
    
    private boolean validerFormulaireNouvelUtilisateur(JTextField txtNom, JTextField txtPrenom, 
                                                      JTextField txtEmail, JPasswordField txtMdp) {
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email = txtEmail.getText().trim();
        String mdp = new String(txtMdp.getPassword());
        
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
            vue.afficherErreur("Tous les champs obligatoires (*) doivent être remplis");
            return false;
        }
        
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            vue.afficherErreur("Format d'email invalide");
            return false;
        }
        
        return true;
    }
    
    private void creerNouvelUtilisateur(String nom, String prenom, String email, 
                                       String mdp, boolean estAdmin, JDialog dialog) {
        try {
            if (usagerDAO.emailExiste(email)) {
                vue.afficherErreur("Cet email est déjà utilisé");
                return;
            }
            
            Usager nouvelUsager = new Usager();
            nouvelUsager.setNomUsager(nom);
            nouvelUsager.setPrenomUsager(prenom);
            nouvelUsager.setMailUsager(email);
            nouvelUsager.setMotDePasse(mdp);
            nouvelUsager.setAdmin(estAdmin);
            
            usagerDAO.create(nouvelUsager);
            vue.afficherInformation("Utilisateur créé avec succès");
            chargerUtilisateurs();
            dialog.dispose();
        } catch (SQLException ex) {
            gererErreur("Erreur création utilisateur", ex.getMessage());
        }
    }
    
    private void modifierUtilisateur() {
        usagerSelectionne = vue.getUsagerSelectionne();
        if (usagerSelectionne == null) {
            vue.afficherInformation("Veuillez sélectionner un utilisateur");
            etat = Etat.AFFICHAGE;
            return;
        }
        
        JDialog dialog = new JDialog(vue, "Modifier l'utilisateur", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(vue);
        dialog.setLayout(new java.awt.GridBagLayout());
        
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        
        // Champs pré-remplis
        JLabel lblNom = new JLabel("Nom:*");
        JTextField txtNom = new JTextField(usagerSelectionne.getNomUsager(), 20);
        
        JLabel lblPrenom = new JLabel("Prénom:*");
        JTextField txtPrenom = new JTextField(usagerSelectionne.getPrenomUsager(), 20);
        
        JLabel lblEmail = new JLabel("Email:*");
        JTextField txtEmail = new JTextField(usagerSelectionne.getMailUsager(), 20);
        
        JLabel lblMdp = new JLabel("Nouveau mot de passe:");
        JPasswordField txtMdp = new JPasswordField(20);
        txtMdp.setToolTipText("Laissez vide pour ne pas modifier");
        
        JCheckBox chkAdmin = new JCheckBox("Administrateur", usagerSelectionne.isAdmin());
        
        // Layout
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
        btnValider.setBackground(new java.awt.Color(30, 144, 255));
        btnValider.setForeground(java.awt.Color.WHITE);
        btnValider.addActionListener(e -> {
            if (!validerFormulaireModification(txtNom, txtPrenom, txtEmail)) {
                return;
            }
            
            modifierUtilisateurExistant(
                txtNom.getText().trim(),
                txtPrenom.getText().trim(),
                txtEmail.getText().trim(),
                new String(txtMdp.getPassword()),
                chkAdmin.isSelected(),
                dialog
            );
        });
        
        JPanel panelBoutons = new JPanel(new java.awt.FlowLayout());
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnValider);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        dialog.add(panelBoutons, gbc);
        
        dialog.pack();
        dialog.setVisible(true);
        etat = Etat.AFFICHAGE;
    }
    
    private boolean validerFormulaireModification(JTextField txtNom, JTextField txtPrenom, 
                                                 JTextField txtEmail) {
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email = txtEmail.getText().trim();
        
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            vue.afficherErreur("Nom, prénom et email sont obligatoires");
            return false;
        }
        
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            vue.afficherErreur("Format d'email invalide");
            return false;
        }
        
        return true;
    }
    
    private void modifierUtilisateurExistant(String nom, String prenom, String email, 
                                            String mdp, boolean estAdmin, JDialog dialog) {
        try {
            if (!email.equals(usagerSelectionne.getMailUsager()) && usagerDAO.emailExiste(email)) {
                vue.afficherErreur("Cet email est déjà utilisé par un autre utilisateur");
                return;
            }
            
            usagerSelectionne.setNomUsager(nom);
            usagerSelectionne.setPrenomUsager(prenom);
            usagerSelectionne.setMailUsager(email);
            usagerSelectionne.setAdmin(estAdmin);
            
            if (!mdp.isEmpty()) {
                usagerSelectionne.setMotDePasse(mdp);
            }
            
            usagerDAO.update(usagerSelectionne);
            vue.afficherInformation("Utilisateur modifié avec succès");
            chargerUtilisateurs();
            dialog.dispose();
        } catch (SQLException ex) {
            gererErreur("Erreur modification utilisateur", ex.getMessage());
        }
    }
    
    private void changerStatutAdmin() {
        usagerSelectionne = vue.getUsagerSelectionne();
        if (usagerSelectionne == null) {
            vue.afficherInformation("Veuillez sélectionner un utilisateur");
            etat = Etat.AFFICHAGE;
            return;
        }
        
        String action = usagerSelectionne.isAdmin() ? "retirer les droits d'administrateur" : "donner les droits d'administrateur";
        
        if (vue.demanderConfirmation("Voulez-vous " + action + " à " + 
                                   usagerSelectionne.getPrenomUsager() + " " + 
                                   usagerSelectionne.getNomUsager() + " ?")) {
            
            try {
                boolean success = mettreAJourStatutAdmin(usagerSelectionne.getIdUsager(), !usagerSelectionne.isAdmin());
                if (success) {
                    vue.afficherInformation("Statut administrateur modifié avec succès");
                    chargerUtilisateurs();
                } else {
                    vue.afficherErreur("Erreur lors de la modification du statut");
                }
            } catch (Exception e) {
                gererErreur("Erreur changement statut admin", e.getMessage());
            }
        }
        
        etat = Etat.AFFICHAGE;
    }
    
    private boolean mettreAJourStatutAdmin(int idUsager, boolean estAdmin) throws SQLException {
        String sql = "UPDATE Usager SET is_admin = ? WHERE id_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, estAdmin);
            pstmt.setInt(2, idUsager);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    private void gererVehicules() {
        usagerSelectionne = vue.getUsagerSelectionne();
        if (usagerSelectionne == null) {
            vue.afficherInformation("Veuillez sélectionner un utilisateur");
            etat = Etat.AFFICHAGE;
            return;
        }
        
        JDialog dialog = new JDialog(vue, "Gestion des Véhicules - " + usagerSelectionne.getNomUsager(), true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(vue);
        dialog.setLayout(new java.awt.BorderLayout(10, 10));
        
        String[] colonnes = {"ID", "Plaque", "Type", "Marque", "Modèle", "Principal", "Date d'ajout"};
        DefaultTableModel model = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        
        chargerVehicules(usagerSelectionne.getIdUsager(), model);
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        JPanel panelBoutons = new JPanel();
        JButton btnAjouter = new JButton("Ajouter un véhicule");
        JButton btnSupprimer = new JButton("Supprimer le véhicule");
        JButton btnDefinirPrincipal = new JButton("Définir comme principal");
        JButton btnFermer = new JButton("Fermer");
        
        btnAjouter.addActionListener(e -> ajouterVehiculeDialog(dialog, model));
        btnSupprimer.addActionListener(e -> supprimerVehicule(table, model));
        btnDefinirPrincipal.addActionListener(e -> definirPrincipalVehicule(table, model));
        btnFermer.addActionListener(e -> dialog.dispose());
        
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnDefinirPrincipal);
        panelBoutons.add(btnFermer);
        
        dialog.add(new JLabel("Véhicules de " + usagerSelectionne.getPrenomUsager() + " " + usagerSelectionne.getNomUsager()), 
                  java.awt.BorderLayout.NORTH);
        dialog.add(scrollPane, java.awt.BorderLayout.CENTER);
        dialog.add(panelBoutons, java.awt.BorderLayout.SOUTH);
        
        dialog.setVisible(true);
        etat = Etat.AFFICHAGE;
    }
    
    private void chargerVehicules(int idUsager, DefaultTableModel model) {
        try {
            model.setRowCount(0);
            List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsagerStatic(idUsager);
            
            for (VehiculeUsager v : vehicules) {
                model.addRow(new Object[]{
                    v.getIdVehiculeUsager(),
                    v.getPlaqueImmatriculation(),
                    v.getTypeVehicule(),
                    v.getMarque() != null ? v.getMarque() : "",
                    v.getModele() != null ? v.getModele() : "",
                    v.isEstPrincipal() ? "Oui" : "Non",
                    v.getDateAjout().toString()
                });
            }
        } catch (Exception e) {
            gererErreur("Erreur chargement véhicules", e.getMessage());
        }
    }
    
    private void ajouterVehiculeDialog(JDialog parent, DefaultTableModel model) {
        JDialog dialog = new JDialog(parent, "Ajouter un véhicule", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new java.awt.GridBagLayout());
        
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        
        JLabel lblPlaque = new JLabel("Plaque d'immatriculation:*");
        JTextField txtPlaque = new JTextField(15);
        
        JLabel lblType = new JLabel("Type de véhicule:*");
        JComboBox<String> comboType = new JComboBox<>(new String[]{"Voiture", "Moto", "Camion"});
        
        JLabel lblMarque = new JLabel("Marque (optionnel):");
        JTextField txtMarque = new JTextField(15);
        
        JLabel lblModele = new JLabel("Modèle (optionnel):");
        JTextField txtModele = new JTextField(15);
        
        JCheckBox chkPrincipal = new JCheckBox("Définir comme véhicule principal");
        
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(lblPlaque, gbc);
        gbc.gridx = 1;
        dialog.add(txtPlaque, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(lblType, gbc);
        gbc.gridx = 1;
        dialog.add(comboType, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(lblMarque, gbc);
        gbc.gridx = 1;
        dialog.add(txtMarque, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(lblModele, gbc);
        gbc.gridx = 1;
        dialog.add(txtModele, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        dialog.add(chkPrincipal, gbc);
        
        // Boutons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        gbc.gridx = 1;
        JButton btnValider = new JButton("Ajouter");
        btnValider.addActionListener(e -> {
            String plaque = txtPlaque.getText().trim().toUpperCase();
            
            if (plaque.isEmpty()) {
                vue.afficherErreur("La plaque d'immatriculation est obligatoire");
                return;
            }
            
            if (!validerFormatPlaque(plaque)) {
                vue.afficherErreur("Format de plaque invalide. Utilisez AA-123-AA ou AA123AA");
                return;
            }
            
            String plaqueNormalisee = normaliserPlaque(plaque);
            
            try {
                if (vehiculeUsagerDAO.plaqueExistePourUsager(usagerSelectionne.getIdUsager(), plaqueNormalisee)) {
                    vue.afficherErreur("Cette plaque existe déjà pour cet utilisateur");
                    return;
                }
                
                VehiculeUsager vehicule = new VehiculeUsager(
                    usagerSelectionne.getIdUsager(),
                    plaqueNormalisee,
                    (String) comboType.getSelectedItem()
                );
                vehicule.setMarque(txtMarque.getText().trim());
                vehicule.setModele(txtModele.getText().trim());
                vehicule.setEstPrincipal(chkPrincipal.isSelected());
                
                vehiculeUsagerDAO.create(vehicule);
                
                vue.afficherInformation("Véhicule ajouté avec succès");
                chargerVehicules(usagerSelectionne.getIdUsager(), model);
                dialog.dispose();
            } catch (SQLException ex) {
                gererErreur("Erreur ajout véhicule", ex.getMessage());
            }
        });
        
        JPanel panelBoutons = new JPanel();
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnValider);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        dialog.add(panelBoutons, gbc);
        
        dialog.pack();
        dialog.setVisible(true);
    }
    
    private void supprimerVehicule(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int idVehicule = (int) model.getValueAt(selectedRow, 0);
            String plaque = (String) model.getValueAt(selectedRow, 1);
            boolean estPrincipal = "Oui".equals(model.getValueAt(selectedRow, 5));
            
            if (vue.demanderConfirmation("Supprimer le véhicule " + plaque + " ?")) {
                if (estPrincipal) {
                    int confirmation = JOptionPane.showConfirmDialog(vue,
                        "Ce véhicule est défini comme principal. Sa suppression désactivera le véhicule principal.\nContinuer quand même ?",
                        "Attention - Véhicule principal",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    
                    if (confirmation != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                
                try {
                    if (VehiculeUsagerDAO.supprimerVehiculeStatic(idVehicule)) {
                        model.removeRow(selectedRow);
                        vue.afficherInformation("Véhicule supprimé avec succès");
                    } else {
                        vue.afficherErreur("Erreur lors de la suppression du véhicule");
                    }
                } catch (Exception ex) {
                    gererErreur("Erreur suppression véhicule", ex.getMessage());
                }
            }
        } else {
            vue.afficherInformation("Veuillez sélectionner un véhicule");
        }
    }
    
    private void definirPrincipalVehicule(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int idVehicule = (int) model.getValueAt(selectedRow, 0);
            String plaque = (String) model.getValueAt(selectedRow, 1);
            
            if ("Oui".equals(model.getValueAt(selectedRow, 5))) {
                vue.afficherInformation("Ce véhicule est déjà défini comme principal");
                return;
            }
            
            if (vue.demanderConfirmation("Définir le véhicule " + plaque + " comme véhicule principal ?")) {
                try {
                    if (VehiculeUsagerDAO.definirVehiculePrincipalStatic(idVehicule, usagerSelectionne.getIdUsager())) {
                        vue.afficherInformation("Véhicule défini comme principal avec succès");
                        chargerVehicules(usagerSelectionne.getIdUsager(), model);
                    } else {
                        vue.afficherErreur("Erreur lors de la définition du véhicule principal");
                    }
                } catch (Exception ex) {
                    gererErreur("Erreur définition véhicule principal", ex.getMessage());
                }
            }
        } else {
            vue.afficherInformation("Veuillez sélectionner un véhicule");
        }
    }
    
    private void gererAbonnements() {
        usagerSelectionne = vue.getUsagerSelectionne();
        if (usagerSelectionne == null) {
            vue.afficherInformation("Veuillez sélectionner un utilisateur");
            etat = Etat.AFFICHAGE;
            return;
        }
        
        try {
            List<Abonnement> abonnements = abonnementDAO.findAll();
            
            if (abonnements.isEmpty()) {
                vue.afficherInformation("Aucun abonnement disponible");
                etat = Etat.AFFICHAGE;
                return;
            }
            
            String[] options = abonnements.stream()
                .map(a -> a.getLibelleAbonnement() + " (" + a.getTarifAbonnement() + "€/mois)")
                .toArray(String[]::new);
            
            List<Abonnement> abonnementsActuels = abonnementDAO.getAbonnementsByUsager(usagerSelectionne.getIdUsager());
            String abonnementActuel = "Aucun";
            if (!abonnementsActuels.isEmpty()) {
                abonnementActuel = abonnementsActuels.get(0).getLibelleAbonnement();
            }
            
            JDialog dialog = new JDialog(vue, "Gestion des Abonnements", true);
            dialog.setSize(500, 300);
            dialog.setLocationRelativeTo(vue);
            dialog.setLayout(new java.awt.BorderLayout(10, 10));
            
            JPanel panelInfo = new JPanel(new java.awt.GridLayout(4, 1, 5, 5));
            panelInfo.add(new JLabel("Utilisateur: " + usagerSelectionne.getPrenomUsager() + " " + usagerSelectionne.getNomUsager()));
            panelInfo.add(new JLabel("Email: " + usagerSelectionne.getMailUsager()));
            panelInfo.add(new JLabel("Abonnement actuel: " + abonnementActuel));
            
            JComboBox<String> comboAbonnements = new JComboBox<>(options);
            panelInfo.add(new JLabel("Nouvel abonnement:"));
            panelInfo.add(comboAbonnements);
            
            JPanel panelBoutons = new JPanel();
            JButton btnAttribuer = new JButton("Attribuer/Mettre à jour");
            JButton btnSupprimer = new JButton("Supprimer");
            JButton btnFermer = new JButton("Fermer");
            
            btnAttribuer.addActionListener(e -> {
                int selectedIndex = comboAbonnements.getSelectedIndex();
                if (selectedIndex >= 0) {
                    Abonnement abonnement = abonnements.get(selectedIndex);
                    
                    if (vue.demanderConfirmation("Attribuer l'abonnement '" + 
                        abonnement.getLibelleAbonnement() + "' à " + 
                        usagerSelectionne.getPrenomUsager() + " " + usagerSelectionne.getNomUsager() + " ?")) {
                        
                        try {
                            boolean success = abonnementDAO.ajouterAbonnementUtilisateur(
                                usagerSelectionne.getIdUsager(), abonnement.getIdAbonnement());
                            
                            if (success) {
                                vue.afficherInformation("Abonnement attribué avec succès");
                                dialog.dispose();
                            } else {
                                vue.afficherErreur("Erreur lors de l'attribution de l'abonnement");
                            }
                        } catch (SQLException ex) {
                            gererErreur("Erreur attribution abonnement", ex.getMessage());
                        }
                    }
                }
            });
            
            btnSupprimer.addActionListener(e -> {
                if (vue.demanderConfirmation("Supprimer l'abonnement de " + 
                    usagerSelectionne.getPrenomUsager() + " " + usagerSelectionne.getNomUsager() + " ?")) {
                    
                    try {
                        abonnementDAO.supprimerAbonnementsUtilisateur(usagerSelectionne.getIdUsager());
                        vue.afficherInformation("Abonnement supprimé avec succès");
                        dialog.dispose();
                    } catch (SQLException ex) {
                        gererErreur("Erreur suppression abonnement", ex.getMessage());
                    }
                }
            });
            
            btnFermer.addActionListener(e -> dialog.dispose());
            
            panelBoutons.add(btnAttribuer);
            panelBoutons.add(btnSupprimer);
            panelBoutons.add(btnFermer);
            
            dialog.add(panelInfo, java.awt.BorderLayout.CENTER);
            dialog.add(panelBoutons, java.awt.BorderLayout.SOUTH);
            
            dialog.setVisible(true);
            etat = Etat.AFFICHAGE;
            
        } catch (SQLException e) {
            gererErreur("Erreur chargement abonnements", e.getMessage());
        }
    }
    
    private void voirStationnements() {
        usagerSelectionne = vue.getUsagerSelectionne();
        if (usagerSelectionne == null) {
            vue.afficherInformation("Veuillez sélectionner un utilisateur");
            etat = Etat.AFFICHAGE;
            return;
        }
        
        JDialog dialog = new JDialog(vue, "Historique des Stationnements", true);
        dialog.setSize(900, 500);
        dialog.setLocationRelativeTo(vue);
        dialog.setLayout(new java.awt.BorderLayout(10, 10));
        
        String[] colonnes = {"ID", "Type", "Véhicule", "Lieu", "Date début", "Date fin", "Durée", "Coût", "Statut", "Paiement"};
        DefaultTableModel model = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        
        chargerStationnements(usagerSelectionne.getIdUsager(), model);
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        JPanel panelInfo = new JPanel();
        panelInfo.add(new JLabel("Stationnements de " + usagerSelectionne.getPrenomUsager() + " " + 
                                usagerSelectionne.getNomUsager() + " (" + model.getRowCount() + " stationnement(s))"));
        
        JPanel panelBoutons = new JPanel();
        JButton btnActualiser = new JButton("Actualiser");
        JButton btnFermer = new JButton("Fermer");
        
        btnActualiser.addActionListener(e -> {
            model.setRowCount(0);
            chargerStationnements(usagerSelectionne.getIdUsager(), model);
            vue.afficherInformation("Liste actualisée");
        });
        
        btnFermer.addActionListener(e -> dialog.dispose());
        
        panelBoutons.add(btnActualiser);
        panelBoutons.add(btnFermer);
        
        dialog.add(panelInfo, java.awt.BorderLayout.NORTH);
        dialog.add(scrollPane, java.awt.BorderLayout.CENTER);
        dialog.add(panelBoutons, java.awt.BorderLayout.SOUTH);
        
        dialog.setVisible(true);
        etat = Etat.AFFICHAGE;
    }
    
    private void chargerStationnements(int idUsager, DefaultTableModel model) {
        try {
            StationnementDAO stationnementDAO = StationnementDAO.getInstance();
            List<modele.Stationnement> stationnements = stationnementDAO.getHistoriqueStationnements(idUsager);
            
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            for (modele.Stationnement s : stationnements) {
                model.addRow(new Object[]{
                    s.getIdStationnement(),
                    s.getTypeStationnement(),
                    s.getTypeVehicule() + " - " + s.getPlaqueImmatriculation(),
                    s.getIdTarification(),
                    s.getDateCreation() != null ? dateFormat.format(
                        java.sql.Timestamp.valueOf(s.getDateCreation())) : "-",
                    s.getDateFin() != null ? dateFormat.format(
                        java.sql.Timestamp.valueOf(s.getDateFin())) : "-",
                    s.getDureeHeures() + "h" + s.getDureeMinutes() + "min",
                    String.format("%.2f €", s.getCout()),
                    s.getStatut(),
                    s.getStatutPaiement()
                });
            }
        } catch (SQLException e) {
            gererErreur("Erreur chargement stationnements", e.getMessage());
        }
    }
    
    private void retourAdministration() {
        String emailAdmin = vue.getEmailAdmin();
        if (emailAdmin != null) {
            new Page_Administration(emailAdmin).setVisible(true);
            vue.dispose();
        } else {
            vue.afficherErreur("Impossible de revenir à l'administration");
        }
    }
    
    // Méthodes utilitaires
    
    private boolean validerFormatPlaque(String plaque) {
        return plaque.matches("[A-Z]{2}-\\d{3}-[A-Z]{2}") || 
               plaque.matches("[A-Z]{2}\\d{3}[A-Z]{2}");
    }
    
    private String normaliserPlaque(String plaque) {
        if (plaque.matches("[A-Z]{2}\\d{3}[A-Z]{2}")) {
            return plaque.substring(0, 2) + "-" + 
                   plaque.substring(2, 5) + "-" + 
                   plaque.substring(5);
        }
        return plaque;
    }
    
    private void gererErreur(String titre, String message) {
        System.err.println(titre + ": " + message);
        vue.afficherErreur(titre);
        etat = Etat.ERREUR;
    }
    
    private void gererErreurInitialisation(String message) {
        System.err.println("Erreur initialisation: " + message);
        JOptionPane.showMessageDialog(vue,
            message + "\n\nL'application va se fermer.",
            "Erreur d'initialisation",
            JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
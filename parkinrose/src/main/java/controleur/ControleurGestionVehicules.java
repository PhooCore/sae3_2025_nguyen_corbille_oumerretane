package controleur;

import ihm.Page_Gestion_Vehicules;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import modele.Usager;
import modele.VehiculeUsager;
import modele.dao.UsagerDAO;
import modele.dao.VehiculeUsagerDAO;
import java.util.List;
import java.awt.*;

public class ControleurGestionVehicules implements ActionListener {
    
    private enum EtatGestion {
        INITIAL,
        AFFICHAGE,
        AJOUT,
        MODIFICATION,
        SUPPRESSION
    }
    
    private Page_Gestion_Vehicules vue;
    private EtatGestion etat;
    private Usager usager;
    private DefaultListModel<VehiculeUsager> listModel;
    
    public ControleurGestionVehicules(Page_Gestion_Vehicules vue) {
        this.vue = vue;
        this.usager = UsagerDAO.getUsagerByEmail(vue.emailUtilisateur);
        this.etat = EtatGestion.INITIAL;
        this.listModel = vue.listModel; 
        configurerListeners();
        chargerVehicules();
        etat = EtatGestion.AFFICHAGE;
    }
    
    private void configurerListeners() {
        // Ajouter ActionListener aux boutons
        vue.btnAjouter.addActionListener(this);
        vue.btnSupprimer.addActionListener(this);
        vue.btnDefinirPrincipal.addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = getActionBouton((JButton) e.getSource());
        
        switch (etat) {
            case AFFICHAGE:
                if (action.equals("AJOUTER")) {
                    etat = EtatGestion.AJOUT;
                    ajouterVehicule();
                    etat = EtatGestion.AFFICHAGE;
                } else if (action.equals("SUPPRIMER")) {
                    etat = EtatGestion.SUPPRESSION;
                    supprimerVehicule();
                    etat = EtatGestion.AFFICHAGE;
                } else if (action.equals("DEFINIR_PRINCIPAL")) {
                    etat = EtatGestion.MODIFICATION;
                    definirPrincipal();
                    etat = EtatGestion.AFFICHAGE;
                }
                break;
        }
    }
    
    private void chargerVehicules() {
        listModel.clear();
        if (usager != null) {
            List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsagerStatic(usager.getIdUsager());
            for (VehiculeUsager v : vehicules) {
                listModel.addElement(v);
            }
        }
    }
    
    private void ajouterVehicule() {
        // Ouvrir une boîte de dialogue pour ajouter un véhicule
        JDialog dialog = new JDialog(vue, "Ajouter un véhicule", true);
        dialog.setLayout(new GridLayout(5, 2, 5, 5));
        
        JTextField txtPlaque = new JTextField();
        JComboBox<String> comboType = new JComboBox<>(new String[]{"Voiture", "Moto", "Camion"});
        JTextField txtMarque = new JTextField();
        JTextField txtModele = new JTextField();
        JCheckBox chkPrincipal = new JCheckBox("Véhicule principal");
        
        dialog.add(new JLabel("Plaque d'immatriculation:"));
        dialog.add(txtPlaque);
        dialog.add(new JLabel("Type:"));
        dialog.add(comboType);
        dialog.add(new JLabel("Marque (optionnel):"));
        dialog.add(txtMarque);
        dialog.add(new JLabel("Modèle (optionnel):"));
        dialog.add(txtModele);
        dialog.add(new JLabel(""));
        dialog.add(chkPrincipal);
        
        JButton btnValider = new JButton("Valider");
        btnValider.addActionListener(e -> {
            String plaque = txtPlaque.getText().trim().toUpperCase();
            
            // Validation du format de plaque (compatible avec GarerVoirie)
            if (!validerFormatPlaque(plaque)) {
                JOptionPane.showMessageDialog(dialog, 
                    "Format de plaque invalide. Utilisez AA-123-AA ou AA123AA", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (usager != null) {
                // Normaliser le format de plaque
                String plaqueNormalisee = normaliserPlaque(plaque);
                
                // Vérifier si la plaque existe déjà pour cet utilisateur
                if (plaqueExistePourUsager(plaqueNormalisee)) {
                    JOptionPane.showMessageDialog(dialog,
                        "Cette plaque d'immatriculation existe déjà pour votre compte.",
                        "Plaque existante",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                VehiculeUsager vehicule = new VehiculeUsager(
                    usager.getIdUsager(),
                    plaqueNormalisee,
                    (String) comboType.getSelectedItem()
                );
                vehicule.setMarque(txtMarque.getText().trim());
                vehicule.setModele(txtModele.getText().trim());
                vehicule.setEstPrincipal(chkPrincipal.isSelected());
                
                // Utiliser la méthode statique
                if (VehiculeUsagerDAO.ajouterVehiculeStatic(vehicule)) {
                    chargerVehicules();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(vue,
                        "Véhicule ajouté avec succès !",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Erreur lors de l'ajout du véhicule.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        JPanel panelBoutons = new JPanel(new FlowLayout());
        panelBoutons.add(btnValider);
        panelBoutons.add(btnAnnuler);
        
        dialog.add(new JLabel(""));
        dialog.add(panelBoutons);
        
        dialog.pack();
        dialog.setLocationRelativeTo(vue);
        dialog.setVisible(true);
    }
    
    private void supprimerVehicule() {
        // Utiliser le modèle directement (au lieu de vue.listVehicules.getSelectedValue())
        int selectedIndex = vue.listVehicules.getSelectedIndex();
        if (selectedIndex >= 0) {
            VehiculeUsager vehicule = listModel.getElementAt(selectedIndex);
            
            // Vérifier si c'est le véhicule principal
            if (vehicule.isEstPrincipal()) {
                int choix = JOptionPane.showConfirmDialog(vue, 
                    "Vous allez supprimer votre véhicule principal.\n" +
                    "Aucun véhicule principal ne sera défini après cette opération.\n\n" +
                    "Confirmez-vous la suppression du véhicule " + vehicule.getPlaqueImmatriculation() + " ?",
                    "Suppression du véhicule principal",
                    JOptionPane.YES_NO_OPTION);
                
                if (choix != JOptionPane.YES_OPTION) {
                    return;
                }
            } else {
                int choix = JOptionPane.showConfirmDialog(vue, 
                    "Supprimer le véhicule " + vehicule.getPlaqueImmatriculation() + " ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);
                
                if (choix != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            if (VehiculeUsagerDAO.supprimerVehiculeStatic(vehicule.getIdVehiculeUsager())) {
                chargerVehicules();
                JOptionPane.showMessageDialog(vue,
                    "Véhicule supprimé avec succès !",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(vue,
                    "Erreur lors de la suppression du véhicule.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(vue,
                "Veuillez sélectionner un véhicule à supprimer.",
                "Aucun véhicule sélectionné",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void definirPrincipal() {
        // Utiliser le modèle directement
        int selectedIndex = vue.listVehicules.getSelectedIndex();
        if (selectedIndex >= 0) {
            VehiculeUsager vehicule = listModel.getElementAt(selectedIndex);
            
            if (vehicule.isEstPrincipal()) {
                JOptionPane.showMessageDialog(vue,
                    "Ce véhicule est déjà défini comme principal.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int confirmation = JOptionPane.showConfirmDialog(vue,
                "Définir le véhicule " + vehicule.getPlaqueImmatriculation() + " comme véhicule principal ?\n\n" +
                "Le véhicule principal sera utilisé par défaut pour vos stationnements.",
                "Définir comme véhicule principal",
                JOptionPane.YES_NO_OPTION);
            
            if (confirmation == JOptionPane.YES_OPTION && usager != null) {
                // Utiliser la méthode statique
                if (VehiculeUsagerDAO.definirVehiculePrincipalStatic(
                    vehicule.getIdVehiculeUsager(), usager.getIdUsager())) {
                    chargerVehicules();
                    JOptionPane.showMessageDialog(vue,
                        "Véhicule défini comme principal avec succès !",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(vue,
                        "Erreur lors de la modification du véhicule principal.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(vue,
                "Veuillez sélectionner un véhicule à définir comme principal.",
                "Aucun véhicule sélectionné",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private boolean validerFormatPlaque(String plaque) {
        // Compatible avec le format du contrôleur GarerVoirie
        return plaque.matches("[A-Z]{2}-\\d{3}-[A-Z]{2}") || 
               plaque.matches("[A-Z]{2}\\d{3}[A-Z]{2}");
    }
    
    private String normaliserPlaque(String plaque) {
        // Normaliser le format de plaque (AA123AA → AA-123-AA)
        if (plaque.matches("[A-Z]{2}\\d{3}[A-Z]{2}")) {
            return plaque.substring(0, 2) + "-" + 
                   plaque.substring(2, 5) + "-" + 
                   plaque.substring(5);
        }
        return plaque;
    }
    
    private boolean plaqueExistePourUsager(String plaque) {
        if (usager == null) return false;
        
        // Vérifier dans la liste des véhicules existants
        for (int i = 0; i < listModel.size(); i++) {
            VehiculeUsager v = listModel.getElementAt(i);
            if (v.getPlaqueImmatriculation().equalsIgnoreCase(plaque)) {
                return true;
            }
        }
        return false;
    }
    
    private String getActionBouton(JButton b) {
        String texte = b.getText();
        if (texte != null) {
            if (texte.contains("Ajouter")) {
                return "AJOUTER";
            } else if (texte.contains("Supprimer")) {
                return "SUPPRIMER";
            } else if (texte.contains("Définir")) {
                return "DEFINIR_PRINCIPAL";
            }
        }
        return "INCONNU";
    }
}
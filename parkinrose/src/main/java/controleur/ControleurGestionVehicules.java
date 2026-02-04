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

/**
 * Contrôleur gérant l'interface de gestion des véhicules d'un utilisateur.
 * Permet d'ajouter, supprimer des véhicules et de définir le véhicule principal
 * qui sera utilisé par défaut pour les stationnements.
 * Valide les plaques d'immatriculation au format français (AA-123-AA ou AA123AA)
 * et évite les doublons.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Gestion_Vehicules
 * et les modèles (Usager, VehiculeUsager, VehiculeUsagerDAO).
 * 
 * @author Équipe 7
 */
public class ControleurGestionVehicules implements ActionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur de gestion des véhicules.
     * Permet de suivre le cycle de vie des opérations sur les véhicules.
     */
    private enum EtatGestion {
        /** État initial au démarrage du contrôleur */
        INITIAL,
        /** Affichage de la liste des véhicules */
        AFFICHAGE,
        /** Ajout d'un nouveau véhicule en cours */
        AJOUT,
        /** Modification d'un véhicule existant en cours */
        MODIFICATION,
        /** Suppression d'un véhicule en cours */
        SUPPRESSION
    }
    
    private Page_Gestion_Vehicules vue;
    private EtatGestion etat;
    private Usager usager;
    private DefaultListModel<VehiculeUsager> listModel;
    
    /**
     * Constructeur du contrôleur de gestion des véhicules.
     * Initialise le contrôleur avec la vue associée, charge l'utilisateur et ses véhicules.
     * 
     * @param vue la page d'interface graphique de gestion des véhicules
     */
    public ControleurGestionVehicules(Page_Gestion_Vehicules vue) {
        this.vue = vue;
        this.usager = UsagerDAO.getUsagerByEmail(vue.emailUtilisateur);
        this.etat = EtatGestion.INITIAL;
        this.listModel = vue.listModel; 
        configurerListeners();
        chargerVehicules();
        etat = EtatGestion.AFFICHAGE;
    }
    
    /**
     * Configure les écouteurs d'événements pour les boutons de la vue.
     * Connecte les boutons d'ajout, suppression et définition du véhicule principal.
     */
    private void configurerListeners() {
        vue.btnAjouter.addActionListener(this);
        vue.btnSupprimer.addActionListener(this);
        vue.btnDefinirPrincipal.addActionListener(this);
    }
    
    /**
     * Gère les événements d'action des boutons de la vue.
     * Route les actions vers les méthodes appropriées en fonction de l'état actuel
     * et du bouton cliqué.
     * 
     * @param e l'événement d'action
     */
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
    
    /**
     * Charge la liste complète des véhicules de l'utilisateur depuis la base de données
     * et met à jour l'affichage dans la liste.
     */
    private void chargerVehicules() {
        listModel.clear();
        if (usager != null) {
            List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsagerStatic(usager.getIdUsager());
            for (VehiculeUsager v : vehicules) {
                listModel.addElement(v);
            }
        }
    }
    
    /**
     * Affiche un dialogue modal pour ajouter un nouveau véhicule.
     * Le formulaire comprend : plaque d'immatriculation, type, marque (optionnel),
     * modèle (optionnel) et la possibilité de le définir comme véhicule principal.
     * Valide le format de la plaque et vérifie l'absence de doublon avant l'ajout.
     */
    private void ajouterVehicule() {
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
            
            if (!validerFormatPlaque(plaque)) {
                JOptionPane.showMessageDialog(dialog, 
                    "Format de plaque invalide. Utilisez AA-123-AA ou AA123AA", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (usager != null) {
                String plaqueNormalisee = normaliserPlaque(plaque);
                
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
    
    /**
     * Supprime le véhicule sélectionné dans la liste après confirmation.
     * Avertit l'utilisateur de manière spécifique si le véhicule à supprimer
     * est le véhicule principal (aucun véhicule principal ne sera défini après).
     */
    private void supprimerVehicule() {
        int selectedIndex = vue.listVehicules.getSelectedIndex();
        if (selectedIndex >= 0) {
            VehiculeUsager vehicule = listModel.getElementAt(selectedIndex);
            
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
    
    /**
     * Définit le véhicule sélectionné comme véhicule principal de l'utilisateur
     * après confirmation. Le véhicule principal sera utilisé par défaut pour
     * les stationnements. Tous les autres véhicules perdent ce statut.
     */
    private void definirPrincipal() {
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
    
    /**
     * Valide le format d'une plaque d'immatriculation française.
     * Accepte les formats AA-123-AA et AA123AA.
     * Compatible avec le format utilisé dans le contrôleur GarerVoirie.
     * 
     * @param plaque la plaque à valider
     * @return true si le format est valide, false sinon
     */
    private boolean validerFormatPlaque(String plaque) {
        return plaque.matches("[A-Z]{2}-\\d{3}-[A-Z]{2}") || 
               plaque.matches("[A-Z]{2}\\d{3}[A-Z]{2}");
    }
    
    /**
     * Normalise le format d'une plaque d'immatriculation en ajoutant les tirets
     * si nécessaire (AA123AA → AA-123-AA).
     * 
     * @param plaque la plaque à normaliser
     * @return la plaque normalisée au format AA-123-AA
     */
    private String normaliserPlaque(String plaque) {
        if (plaque.matches("[A-Z]{2}\\d{3}[A-Z]{2}")) {
            return plaque.substring(0, 2) + "-" + 
                   plaque.substring(2, 5) + "-" + 
                   plaque.substring(5);
        }
        return plaque;
    }
    
    /**
     * Vérifie si une plaque d'immatriculation existe déjà pour l'utilisateur.
     * Recherche dans la liste des véhicules déjà enregistrés.
     * 
     * @param plaque la plaque à vérifier
     * @return true si la plaque existe déjà, false sinon
     */
    private boolean plaqueExistePourUsager(String plaque) {
        if (usager == null) return false;
        
        for (int i = 0; i < listModel.size(); i++) {
            VehiculeUsager v = listModel.getElementAt(i);
            if (v.getPlaqueImmatriculation().equalsIgnoreCase(plaque)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Détermine l'action associée à un bouton en analysant son texte.
     * 
     * @param b le bouton dont on veut identifier l'action
     * @return une chaîne représentant l'action ("AJOUTER", "SUPPRIMER", "DEFINIR_PRINCIPAL" ou "INCONNU")
     */
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
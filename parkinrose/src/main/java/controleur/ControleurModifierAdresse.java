package controleur;

import ihm.Page_Modifier_Adresse;
import ihm.Page_Utilisateur;
import modele.Usager;
import modele.dao.UsagerDAO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

/**
 * Contrôleur gérant la modification de l'adresse postale de l'utilisateur.
 * Valide les champs (adresse, code postal, ville) et enregistre les informations
 * dans la base de données. L'adresse complète est stockée dans le champ
 * "numero_carte_tisseo" au format : adresse|codePostal|ville.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Modifier_Adresse
 * et le modèle (Usager, UsagerDAO).
 * 
 * @author Équipe 7
 */
public class ControleurModifierAdresse implements ActionListener {
    
    private Page_Modifier_Adresse vue;
    
    /**
     * Constructeur du contrôleur de modification d'adresse.
     * Configure immédiatement les écouteurs d'événements.
     * 
     * @param vue la page d'interface graphique de modification d'adresse
     */
    public ControleurModifierAdresse(Page_Modifier_Adresse vue) {
        this.vue = vue;
        configurerListeners();
    }
    
    /**
     * Configure les écouteurs pour les boutons de validation et d'annulation.
     */
    private void configurerListeners() {
        vue.getBtnValider().addActionListener(this);
        vue.getBtnAnnuler().addActionListener(this);
    }
    
    /**
     * Gère les événements d'action des boutons.
     * Route vers la validation ou l'annulation selon le bouton cliqué.
     * 
     * @param e l'événement d'action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vue.getBtnValider()) {
            validerAdresse();
        } else if (e.getSource() == vue.getBtnAnnuler()) {
            annuler();
        }
    }
    
    /**
     * Valide et enregistre l'adresse saisie par l'utilisateur.
     * Vérifie que tous les champs sont remplis et que le code postal est valide (5 chiffres).
     * L'adresse est stockée au format : adresse|codePostal|ville dans le champ numero_carte_tisseo.
     * Affiche un message de confirmation en cas de succès et retourne à la page utilisateur.
     */
    private void validerAdresse() {
        String adresse = vue.getTxtAdresse().getText().trim();
        String codePostal = vue.getTxtCodePostal().getText().trim();
        String ville = vue.getTxtVille().getText().trim();
        
        if (adresse.isEmpty()) {
            JOptionPane.showMessageDialog(vue,
                "Veuillez saisir votre adresse",
                "Adresse manquante",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (codePostal.isEmpty()) {
            JOptionPane.showMessageDialog(vue,
                "Veuillez saisir votre code postal",
                "Code postal manquant",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (ville.isEmpty()) {
            JOptionPane.showMessageDialog(vue,
                "Veuillez saisir votre ville",
                "Ville manquante",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!codePostal.matches("\\d{5}")) {
            JOptionPane.showMessageDialog(vue,
                "Code postal invalide. Format: 5 chiffres (ex: 31000)",
                "Code postal invalide",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Usager usager = vue.getUsager();

            String adresseComplete = adresse + "|" + codePostal + "|" + ville;
            usager.setNumeroCarteTisseo(adresseComplete);
            

            boolean success = UsagerDAO.modifierUsager(usager);
            
            if (success) {
                String message = "Adresse enregistrée avec succès !\n\n";
                message += "Adresse: " + adresse + "\n";
                message += "CP: " + codePostal + " " + ville + "\n";
                
                JOptionPane.showMessageDialog(vue,
                    message,
                    "Adresse enregistrée",
                    JOptionPane.INFORMATION_MESSAGE);
                
                retourPageUtilisateur();
                
            } else {
                JOptionPane.showMessageDialog(vue,
                    "Erreur lors de l'enregistrement de l'adresse",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(vue,
                "Erreur technique lors de la mise à jour: " + ex.getMessage(),
                "Erreur système",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Gère l'annulation de la modification d'adresse.
     * Demande confirmation avant de retourner à la page utilisateur.
     */
    private void annuler() {
        int confirm = JOptionPane.showConfirmDialog(vue,
            "Annuler les modifications ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            retourPageUtilisateur();
        }
    }
    
    /**
     * Retourne à la page utilisateur en mode rafraîchi et ferme la page actuelle.
     */
    private void retourPageUtilisateur() {
        Page_Utilisateur pageUtilisateur = new Page_Utilisateur(vue.getEmailUtilisateur(), true);
        pageUtilisateur.setVisible(true);
        vue.dispose();
    }
}
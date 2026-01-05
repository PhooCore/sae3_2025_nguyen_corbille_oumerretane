package controleur;

import ihm.Page_Modifier_Adresse;
import ihm.Page_Utilisateur;
import modele.Usager;
import modele.dao.UsagerDAO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class ControleurModifierAdresse implements ActionListener {
    
    private Page_Modifier_Adresse vue;
    
    public ControleurModifierAdresse(Page_Modifier_Adresse vue) {
        this.vue = vue;
        configurerListeners();
    }
    
    private void configurerListeners() {
        vue.getBtnValider().addActionListener(this);
        vue.getBtnAnnuler().addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vue.getBtnValider()) {
            validerAdresse();
        } else if (e.getSource() == vue.getBtnAnnuler()) {
            annuler();
        }
    }
    
    private void validerAdresse() {
        // Récupérer les valeurs
        String adresse = vue.getTxtAdresse().getText().trim();
        String codePostal = vue.getTxtCodePostal().getText().trim();
        String ville = vue.getTxtVille().getText().trim();
        
        // Validation
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
        
        // Validation du code postal 
        if (!codePostal.matches("\\d{5}")) {
            JOptionPane.showMessageDialog(vue,
                "Code postal invalide. Format: 5 chiffres (ex: 31000)",
                "Code postal invalide",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Mettre à jour l'usager
        try {
            Usager usager = vue.getUsager();

            String adresseComplete = adresse + "|" + codePostal + "|" + ville;
            usager.setNumeroCarteTisseo(adresseComplete);
            

            boolean success = UsagerDAO.modifierUsager(usager);
            
            if (success) {
                // Afficher confirmation
                String message = "Adresse enregistrée avec succès !\n\n";
                message += "Adresse: " + adresse + "\n";
                message += "CP: " + codePostal + " " + ville + "\n";
                
                JOptionPane.showMessageDialog(vue,
                    message,
                    "Adresse enregistrée",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Retour à la page utilisateur
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
    
    private void annuler() {
        int confirm = JOptionPane.showConfirmDialog(vue,
            "Annuler les modifications ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            retourPageUtilisateur();
        }
    }
    
    private void retourPageUtilisateur() {
        Page_Utilisateur pageUtilisateur = new Page_Utilisateur(vue.getEmailUtilisateur(), true);
        pageUtilisateur.setVisible(true);
        vue.dispose();
    }
}
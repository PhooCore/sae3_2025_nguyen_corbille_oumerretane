package controleur;

import modele.Usager;
import modele.dao.ModifMdpDAO;
import modele.dao.UsagerDAO;
import ihm.Page_Utilisateur;
import ihm.Page_Authentification;
import ihm.Page_Modif_MDP;
import ihm.Page_Historique_Stationnements;
import javax.swing.JOptionPane;

public class UtilisateurControleur {
    
    private String emailUtilisateur;
    private Usager usager;
    
    /**
     * Contrôleur pour la gestion des utilisateurs
     * @param email l'email de l'utilisateur connecté
     */
    public UtilisateurControleur(String email) {
        this.emailUtilisateur = email;
        if (email != null) {
            this.usager = UsagerDAO.getUsagerByEmail(email);
        }
    }
    
    /**
     * Récupère les informations de l'utilisateur
     * @return l'objet Usager
     */
    public Usager getInformationsUtilisateur() {
        return usager;
    }
    
    /**
     * Modifie le mot de passe de l'utilisateur
     * @param ancienMotDePasse l'ancien mot de passe
     * @param nouveauMotDePasse le nouveau mot de passe
     * @param confirmation la confirmation du nouveau mot de passe
     * @param pageUtilisateur la page utilisateur pour les callbacks
     * @return true si la modification réussit
     */
    public boolean modifierMotDePasse(String ancienMotDePasse, String nouveauMotDePasse, 
                                     String confirmation, Page_Utilisateur pageUtilisateur) {
        
        // Validation des champs
        if (ancienMotDePasse == null || ancienMotDePasse.isEmpty() ||
            nouveauMotDePasse == null || nouveauMotDePasse.isEmpty() ||
            confirmation == null || confirmation.isEmpty()) {
            
            JOptionPane.showMessageDialog(pageUtilisateur, 
                "Veuillez remplir tous les champs", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Vérification de la correspondance des mots de passe
        if (!nouveauMotDePasse.equals(confirmation)) {
            JOptionPane.showMessageDialog(pageUtilisateur, 
                "Les mots de passe ne correspondent pas", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Vérification de la longueur minimale
        if (nouveauMotDePasse.length() < 6) {
            JOptionPane.showMessageDialog(pageUtilisateur, 
                "Le mot de passe doit contenir au moins 6 caractères", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Vérification de l'ancien mot de passe
        ModifMdpDAO modifMdpDAO = new ModifMdpDAO();
        boolean ancienMdpCorrect = modifMdpDAO.verifierAncienMotDePasse(emailUtilisateur, ancienMotDePasse);
        
        if (!ancienMdpCorrect) {
            JOptionPane.showMessageDialog(pageUtilisateur, 
                "L'ancien mot de passe est incorrect", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Modification du mot de passe
        boolean modificationReussie = modifMdpDAO.modifierMotDePasse(emailUtilisateur, nouveauMotDePasse);
        
        if (modificationReussie) {
            JOptionPane.showMessageDialog(pageUtilisateur, 
                "Mot de passe modifié avec succès !", 
                "Succès", 
                JOptionPane.INFORMATION_MESSAGE);
            return true;
        } else {
            JOptionPane.showMessageDialog(pageUtilisateur, 
                "Erreur lors de la modification du mot de passe", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Déconnecte l'utilisateur
     * @param pageUtilisateur la page utilisateur pour les callbacks
     */
    public void deconnecterUtilisateur(Page_Utilisateur pageUtilisateur) {
        int confirmation = JOptionPane.showConfirmDialog(pageUtilisateur,
            "Êtes-vous sûr de vouloir vous déconnecter ?",
            "Déconnexion",
            JOptionPane.YES_NO_OPTION);
            
        if (confirmation == JOptionPane.YES_OPTION) {
            Page_Authentification authPage = new Page_Authentification();
            authPage.setVisible(true);
            pageUtilisateur.dispose();
        }
    }
    
    /**
     * Redirige vers la page de modification de mot de passe
     * @param pageUtilisateur la page actuelle à fermer
     */
    public void redirigerVersModificationMDP(Page_Utilisateur pageUtilisateur) {
        // Utilise le nouveau constructeur avec email pré-rempli ET la page parente
        Page_Modif_MDP pageModifMdp = new Page_Modif_MDP(emailUtilisateur, pageUtilisateur);
        pageModifMdp.setVisible(true);
        pageUtilisateur.setVisible(false); // Cache la page utilisateur au lieu de la fermer
        // Ne pas appeler dispose() ici pour pouvoir y retourner
    }
    
    /**
     * Redirige vers la page d'historique des stationnements
     * @param pageUtilisateur la page actuelle
     */
    public void redirigerVersHistoriqueStationnements(Page_Utilisateur pageUtilisateur) {
        Page_Historique_Stationnements pageHistorique = new Page_Historique_Stationnements(emailUtilisateur);
        pageHistorique.setVisible(true);
        // Ne pas fermer la page utilisateur pour permettre le retour
    }
    
    /**
     * Met à jour les informations de l'utilisateur
     * @param nom le nouveau nom
     * @param prenom le nouveau prénom
     * @param email le nouvel email
     * @param pageUtilisateur la page utilisateur pour les callbacks
     * @return true si la mise à jour réussit
     */
    public boolean mettreAJourInformations(String nom, String prenom, String email, 
                                          Page_Utilisateur pageUtilisateur) {
        // Cette méthode pourrait être implémentée si vous ajoutez 
        // la fonctionnalité de modification des informations personnelles
        
        JOptionPane.showMessageDialog(pageUtilisateur, 
            "Fonctionnalité de modification des informations personnelles à implémenter", 
            "Information", 
            JOptionPane.INFORMATION_MESSAGE);
        return false;
    }
}
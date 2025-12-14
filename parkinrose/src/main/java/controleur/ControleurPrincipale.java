package controleur;

import ihm.Page_Principale;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ControleurPrincipale implements ActionListener {
    
    private Page_Principale vue;
    private String emailUtilisateur;
    
    public ControleurPrincipale(Page_Principale vue, String email) {
        this.vue = vue;
        this.emailUtilisateur = email;
        configurerListeners();
    }
    
    private void configurerListeners() {
        if (vue.btnUtilisateur != null) {
            vue.btnUtilisateur.addActionListener(this);
        } else {
            System.err.println("ERREUR: btnUtilisateur est null!");
        }
        
        if (vue.btnPreparerStationnement != null) {
            vue.btnPreparerStationnement.addActionListener(this);
        } else {
            System.err.println("ERREUR: btnPreparerStationnement est null!");
        }
        
        if (vue.btnStationnement != null) {
            vue.btnStationnement.addActionListener(this);
        } else {
            System.err.println("ERREUR: btnStationnement est null!");
        }
        
        if (vue.btnSearch != null) {
            vue.btnSearch.addActionListener(this);
        }
        
        if (vue.getSearchField() != null) {
            vue.getSearchField().addActionListener(e -> {
                rechercherParking();
            });
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        String action = "INCONNU";
        
        if (source == vue.btnUtilisateur) {
            action = "PROFIL";
            System.out.println("Bouton Mon Compte cliqué!");
        } else if (source == vue.btnPreparerStationnement) {
            action = "PREPARER";
        } else if (source == vue.btnStationnement) {
            action = "STATIONNEMENT";
        } else if (source == vue.btnSearch) {
            action = "RECHERCHE";
        }

        
        switch (action) {
            case "PROFIL":
                ouvrirProfil();
                break;
            case "STATIONNEMENT":
            case "PREPARER":
                ouvrirStationnement();
                break;
            case "RECHERCHE":
                rechercherParking();
                break;
        }
    }
    
    private void ouvrirProfil() {
        
        try {
            // Utilisez SwingUtilities pour ouvrir la nouvelle fenêtre
            SwingUtilities.invokeLater(() -> {
                ihm.Page_Utilisateur pageUtilisateur = new ihm.Page_Utilisateur(emailUtilisateur);
                pageUtilisateur.setVisible(true);
                
                // NE PAS APPELER vue.dispose() ici !
                // Cela garde la page principale ouverte
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(vue,
                "Erreur lors de l'ouverture du profil: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void ouvrirStationnement() {
        
        // Vérifier s'il y a un stationnement actif
        controleur.StationnementControleur stationnementControleur = 
            new controleur.StationnementControleur(emailUtilisateur);
        
        if (stationnementControleur.getStationnementActif() != null) {
            // Ouvrir la page de stationnement en cours
            ihm.Page_Stationnement_En_Cours page = new ihm.Page_Stationnement_En_Cours(emailUtilisateur);
            page.setVisible(true);
            vue.dispose();
        } else {
            // Demander le type de stationnement
            Object[] options = {"Stationnement en Voirie", "Stationnement en Parking"};
            int choix = JOptionPane.showOptionDialog(vue,
                "Choisissez le type de stationnement :",
                "Nouveau stationnement",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
            
            if (choix == 0) {
                ihm.Page_Garer_Voirie page = new ihm.Page_Garer_Voirie(emailUtilisateur);
                page.setVisible(true);
                vue.dispose();
            } else if (choix == 1) {
                ihm.Page_Garer_Parking page = new ihm.Page_Garer_Parking(emailUtilisateur, null);
                page.setVisible(true);
                vue.dispose();
            }
        }
    }
    
    private void rechercherParking() {
        String recherche = vue.getSearchField().getText().trim();
        
        if (!recherche.isEmpty() && !recherche.equals("Rechercher un parking...")) {
            ihm.Page_Resultats_Recherche page = new ihm.Page_Resultats_Recherche(emailUtilisateur, recherche);
            page.setVisible(true);
            vue.dispose();
        } else {
            JOptionPane.showMessageDialog(vue, 
                "Veuillez saisir un terme de recherche", 
                "Recherche vide", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
}
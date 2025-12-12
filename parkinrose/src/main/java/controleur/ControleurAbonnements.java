package controleur;

import ihm.Page_Abonnements;
import ihm.Page_Utilisateur;
import ihm.Page_Paiement_Abonnement;
import modele.Abonnement;
import modele.dao.AbonnementDAO;
import javax.swing.*;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ControleurAbonnements implements ActionListener {
    
    private enum EtatAbonnements {
        INITIAL,
        CHARGEMENT,
        AFFICHAGE,
        SELECTION_ABONNEMENT,
        CONFIRMATION_REMPLACEMENT,
        REDIRECTION_PAIEMENT,
        RETOUR_EN_COURS,
        ERREUR
    }
    
    private Page_Abonnements vue;
    private EtatAbonnements etat;
    private String emailUtilisateur;
    private List<Abonnement> abonnementsDisponibles;
    private Abonnement abonnementSelectionne;
    private Map<String, JButton> boutonsAbonnements;
    
    public ControleurAbonnements(Page_Abonnements vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.etat = EtatAbonnements.INITIAL;
        this.boutonsAbonnements = new HashMap<>();
      
        configurerListeners();
        chargerAbonnements();
    }
    
    private void configurerListeners() {
        etat = EtatAbonnements.CHARGEMENT;
        
        // Configurer le bouton retour
        if (vue.getBtnRetour() != null) {
            vue.getBtnRetour().addActionListener(this);
        }
        
        // Configurer les listeners des boutons d'abonnements
        configurerListenersRecursifs(vue.getContentPane());
    }
    
    private void chargerAbonnements() {
        
        SwingWorker<List<Abonnement>, Void> worker = new SwingWorker<List<Abonnement>, Void>() {
            @Override
            protected List<Abonnement> doInBackground() throws Exception {
                return AbonnementDAO.getAllAbonnements();
            }
            
            @Override
            protected void done() {
                try {
                    abonnementsDisponibles = get();
                    etat = EtatAbonnements.AFFICHAGE;
                    
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement des abonnements:");
                    e.printStackTrace();
                    etat = EtatAbonnements.ERREUR;
                    afficherErreur("Erreur de chargement des abonnements");
                }
            }
        };
        
        worker.execute();
    }
    
    private void configurerListenersRecursifs(java.awt.Container container) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                String actionCommand = btn.getActionCommand();
                
                if (actionCommand != null) {
                    if (actionCommand.startsWith("SOUSCRIRE_")) {
                        String idAbonnement = actionCommand.replace("SOUSCRIRE_", "");
                        boutonsAbonnements.put(idAbonnement, btn);
                        btn.addActionListener(this);
                    } else if (actionCommand.startsWith("GERER_") || actionCommand.startsWith("RESILIER_")) {
                        btn.addActionListener(this);
                    }
                }
            } else if (comp instanceof JPanel) {
                configurerListenersRecursifs((JPanel) comp);
            } else if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                configurerListenersRecursifs(scrollPane.getViewport());
            } else if (comp instanceof Container) {
                configurerListenersRecursifs((Container) comp);
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        String action = "INCONNU";
        
        if (source instanceof JButton) {
            JButton btn = (JButton) source;
            String actionCommand = btn.getActionCommand();
            String texte = btn.getText();
            
            if (actionCommand != null) {
                if (actionCommand.startsWith("SOUSCRIRE_")) {
                    action = actionCommand;
                } else if (actionCommand.startsWith("GERER_")) {
                    action = "GERER";
                } else if (actionCommand.startsWith("RESILIER_")) {
                    action = "RESILIER";
                }
            } else if (texte != null && texte.contains("Retour")) {
                action = "RETOUR";
            }
        }
        
        
        if (!estActionValide(etat, action)) {
            System.out.println("Action non valide dans l'état " + etat);
            return;
        }
        
        switch (etat) {
            case AFFICHAGE:
                traiterActionsAffichage(action);
                break;
                
            case SELECTION_ABONNEMENT:
                traiterSelectionAbonnement(action);
                break;
                
            case CONFIRMATION_REMPLACEMENT:
                traiterConfirmationRemplacement(action);
                break;
                
            case RETOUR_EN_COURS:
                traiterRetour(action);
                break;
                
            case ERREUR:
                if (action.equals("RETOUR")) {
                    etat = EtatAbonnements.RETOUR_EN_COURS;
                    retourProfil();
                }
                break;
        }
    }
    
    private boolean estActionValide(EtatAbonnements etatActuel, String action) {
        switch (etatActuel) {
            case AFFICHAGE:
                return action.equals("RETOUR") || 
                       action.startsWith("SOUSCRIRE_") ||
                       action.equals("GERER") || 
                       action.equals("RESILIER");
                
            case SELECTION_ABONNEMENT:
            case CONFIRMATION_REMPLACEMENT:
                return action.equals("CONFIRMER") || 
                       action.equals("ANNULER") || 
                       action.equals("RETOUR");
                
            case RETOUR_EN_COURS:
            case REDIRECTION_PAIEMENT:
                return false;
                
            default:
                return false;
        }
    }
    
    private void traiterActionsAffichage(String action) {
        if (action.equals("RETOUR")) {
            etat = EtatAbonnements.RETOUR_EN_COURS;
            retourProfil();
            
        } else if (action.startsWith("SOUSCRIRE_")) {
            etat = EtatAbonnements.SELECTION_ABONNEMENT;
            String idAbonnement = action.replace("SOUSCRIRE_", "");
            selectionnerAbonnement(idAbonnement);
            
        } else if (action.equals("RESILIER")) {
            // Gérer la résiliation d'abonnement
            JOptionPane.showMessageDialog(vue,
                "Pour résilier votre abonnement, veuillez retourner sur votre page de compte.",
                "Résiliation d'abonnement",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void selectionnerAbonnement(String idAbonnement) {
        
        abonnementSelectionne = null;
        for (Abonnement abonnement : abonnementsDisponibles) {
            if (abonnement.getIdAbonnement().equals(idAbonnement)) {
                abonnementSelectionne = abonnement;
                break;
            }
        }
        
        if (abonnementSelectionne == null) {
            etat = EtatAbonnements.ERREUR;
            afficherErreur("Abonnement non trouvé");
            return;
        }
        
        // Vérifier si l'utilisateur a déjà un abonnement
        List<Abonnement> abonnementsExistants = 
            AbonnementDAO.getAbonnementsByUsager(vue.getUsager().getIdUsager());
        
        if (!abonnementsExistants.isEmpty()) {
            etat = EtatAbonnements.CONFIRMATION_REMPLACEMENT;
            demanderConfirmationRemplacement(abonnementsExistants.get(0));
        } else {
            etat = EtatAbonnements.REDIRECTION_PAIEMENT;
            redirigerVersPaiement();
        }
    }
    
    private void demanderConfirmationRemplacement(Abonnement abonnementExistant) {
        
        Object[] options = {"Remplacer", "Conserver mon abonnement actuel"};
        int choix = JOptionPane.showOptionDialog(vue,
            "<html><div style='text-align: center;'>"
            + "<h3>Abonnement existant détecté</h3>"
            + "<p>Vous avez déjà un abonnement actif :</p>"
            + "<p><b>" + abonnementExistant.getLibelleAbonnement() + "</b></p>"
            + "<br>"
            + "<p>Souhaitez-vous le remplacer par :</p>"
            + "<p><b>" + abonnementSelectionne.getLibelleAbonnement() + "</b> ?</p>"
            + "</div></html>",
            "Confirmation de remplacement",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]);
        
        if (choix == JOptionPane.YES_OPTION) {
            etat = EtatAbonnements.REDIRECTION_PAIEMENT;
            redirigerVersPaiement();
        } else {
            etat = EtatAbonnements.AFFICHAGE;
        }
    }
    
    private void redirigerVersPaiement() {
        
        Page_Paiement_Abonnement pagePaiement = new Page_Paiement_Abonnement(
            emailUtilisateur, 
            abonnementSelectionne
        );
        pagePaiement.setVisible(true);
    }
    
    private void traiterSelectionAbonnement(String action) {
        // Gestion des actions pendant la sélection
    }
    
    private void traiterConfirmationRemplacement(String action) {
        // Gestion des actions pendant la confirmation de remplacement
    }
    
    private void traiterRetour(String action) {
        // Gestion du retour en cours
    }
    
    private void retourProfil() {
        
        Page_Utilisateur pageUtilisateur = new Page_Utilisateur(emailUtilisateur, true);
        pageUtilisateur.setVisible(true);
        vue.dispose();
    }
    
    private void afficherErreur(String message) {
        JOptionPane.showMessageDialog(vue,
            message,
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
    }
    
    // Getters pour le débogage
    public EtatAbonnements getEtat() {
        return etat;
    }
    
    public String getEtatString() {
        return etat.toString();
    }
    
    public int getNombreAbonnements() {
        return abonnementsDisponibles != null ? abonnementsDisponibles.size() : 0;
    }
    
    public String getAbonnementSelectionne() {
        return abonnementSelectionne != null ? abonnementSelectionne.getLibelleAbonnement() : "Aucun";
    }
}
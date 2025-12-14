package controleur;

import ihm.Page_Administration;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

public class ControleurAdministration implements ActionListener {
    
    private enum EtatAdministration {
        INITIAL,
        NAVIGATION,
        GESTION,
        STATISTIQUES,
        CONFIGURATION,
        REDIRECTION
    }
    
    private Page_Administration vue;
    private EtatAdministration etat;
    private String emailAdmin;
    
    public ControleurAdministration(Page_Administration vue, String emailAdmin) {
        this.vue = vue;
        this.emailAdmin = emailAdmin;
        this.etat = EtatAdministration.INITIAL;
        configurerListeners();
        
        etat = EtatAdministration.NAVIGATION;
    }
    
    private void configurerListeners() {
        // Ajouter ActionListener à tous les boutons
        for (java.awt.Component comp : vue.getContentPane().getComponents()) {
            if (comp instanceof javax.swing.JPanel) {
                java.awt.Component[] sousComposants = ((javax.swing.JPanel) comp).getComponents();
                for (java.awt.Component sousComp : sousComposants) {
                    if (sousComp instanceof JButton) {
                        ((JButton) sousComp).addActionListener(this);
                    }
                }
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = getActionBouton((JButton) e.getSource());
        
        System.out.println("Action: " + action + " - État: " + etat);
        
        switch (etat) {
            case NAVIGATION:
                if (action.equals("RETOUR")) {
                    etat = EtatAdministration.REDIRECTION;
                    retourAccueil();
                } else if (action.equals("GESTION_PARKINGS")) {
                    etat = EtatAdministration.GESTION;
                    ouvrirGestionParkings();
                } else if (action.equals("GESTION_UTILISATEURS")) {
                    etat = EtatAdministration.GESTION;
                    ouvrirGestionUtilisateurs();
                } else if (action.equals("STATISTIQUES")) {
                    etat = EtatAdministration.STATISTIQUES;
                    ouvrirStatistiques();
                } else if (action.equals("HISTORIQUE_GLOBAL")) {
                    etat = EtatAdministration.STATISTIQUES;
                    ouvrirHistoriqueGlobal();
                } else if (action.equals("CONFIGURATION")) {
                    etat = EtatAdministration.CONFIGURATION;
                    ouvrirConfiguration();
                } else if (action.equals("RAPPORTS")) {
                    etat = EtatAdministration.STATISTIQUES;
                    genererRapports();
                } else if (action.equals("GESTION_CARTE")) {
                    // La carte s'ouvre directement depuis la vue
                }
                break;
                
            case REDIRECTION:
                // Ne rien faire
                break;
        }
    }
    
    private void ouvrirGestionParkings() {
        // TODO: Implémenter l'ouverture de la gestion des parkings
        etat = EtatAdministration.NAVIGATION;
    }
    
    private void ouvrirGestionUtilisateurs() {
        // TODO: Implémenter l'ouverture de la gestion des utilisateurs
        JOptionPane.showMessageDialog(vue,
            "Fonctionnalité en cours de développement",
            "Gestion des Utilisateurs",
            JOptionPane.INFORMATION_MESSAGE);
        etat = EtatAdministration.NAVIGATION;
    }
    
    private void ouvrirStatistiques() {
        // TODO: Implémenter l'ouverture des statistiques
        JOptionPane.showMessageDialog(vue,
            "Fonctionnalité en cours de développement",
            "Statistiques",
            JOptionPane.INFORMATION_MESSAGE);
        etat = EtatAdministration.NAVIGATION;
    }
    
    private void ouvrirHistoriqueGlobal() {
        // TODO: Implémenter l'ouverture de l'historique global
        JOptionPane.showMessageDialog(vue,
            "Fonctionnalité en cours de développement",
            "Historique Global",
            JOptionPane.INFORMATION_MESSAGE);
        etat = EtatAdministration.NAVIGATION;
    }
    
    private void ouvrirConfiguration() {
        // TODO: Implémenter l'ouverture de la configuration
        JOptionPane.showMessageDialog(vue,
            "Fonctionnalité en cours de développement",
            "Configuration",
            JOptionPane.INFORMATION_MESSAGE);
        etat = EtatAdministration.NAVIGATION;
    }
    
    private void genererRapports() {
        // TODO: Implémenter la génération de rapports
        JOptionPane.showMessageDialog(vue,
            "Fonctionnalité en cours de développement",
            "Rapports",
            JOptionPane.INFORMATION_MESSAGE);
        etat = EtatAdministration.NAVIGATION;
    }
    
    private void retourAccueil() {
        ihm.Page_Principale pagePrincipale = new ihm.Page_Principale(emailAdmin);
        pagePrincipale.setVisible(true);
        vue.dispose();
    }
    
    private String getActionBouton(JButton b) {
        String texte = b.getText();
        if (texte != null) {
            if (texte.contains("Retour")) {
                return "RETOUR";
            } else if (texte.contains("Parkings") && !texte.contains("Carte")) {
                return "GESTION_PARKINGS";
            } else if (texte.contains("Carte")) {
                return "GESTION_CARTE"; 
            } else if (texte.contains("Utilisateurs")) {
                return "GESTION_UTILISATEURS";
            } else if (texte.contains("Statistiques")) {
                return "STATISTIQUES";
            } else if (texte.contains("Historique Global")) {
                return "HISTORIQUE_GLOBAL";
            } else if (texte.contains("Configuration")) {
                return "CONFIGURATION";
            } else if (texte.contains("Rapports")) {
                return "RAPPORTS";
            }
        }
        return "INCONNU";
    }
}
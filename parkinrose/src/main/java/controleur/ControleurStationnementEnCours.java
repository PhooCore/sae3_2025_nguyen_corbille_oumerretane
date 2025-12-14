package controleur;

import ihm.Page_Stationnement_En_Cours;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.JOptionPane;

public class ControleurStationnementEnCours implements ActionListener {
    
    private enum EtatStationnement {
        INITIAL,
        AFFICHAGE,
        REDIRECTION
    }
    
    private Page_Stationnement_En_Cours vue;
    private EtatStationnement etat;
    private Timer timer;
    
    public ControleurStationnementEnCours(Page_Stationnement_En_Cours vue) {
        this.vue = vue;
        this.etat = EtatStationnement.INITIAL;
        configurerListeners();
        
        etat = EtatStationnement.AFFICHAGE;
        demarrerTimer();
    }
    
    private void configurerListeners() {
        // Recherche des boutons dans la vue
        rechercherBoutons(vue.getContentPane());
    }
    
    private void rechercherBoutons(java.awt.Container container) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                String texte = btn.getText();
                if (texte != null && (texte.contains("Retour") || texte.contains("←"))) {
                    btn.addActionListener(this);
                }
            } else if (comp instanceof javax.swing.JPanel) {
                rechercherBoutons((javax.swing.JPanel) comp);
            } else if (comp instanceof javax.swing.JScrollPane) {
                rechercherBoutons(((javax.swing.JScrollPane) comp).getViewport());
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source instanceof JButton) {
            JButton btn = (JButton) source;
            String texte = btn.getText();
            
            if (texte != null && texte.contains("Retour")) {
                retourAccueil();
            }
        }
    }
    
    private void retourAccueil() {
        etat = EtatStationnement.REDIRECTION;
        arreterTimer();
        
        ihm.Page_Principale pagePrincipale = new ihm.Page_Principale(vue.getEmailUtilisateur());
        pagePrincipale.setVisible(true);
        vue.dispose();
    }
    
    private void demarrerTimer() {
        // Timer pour l'actualisation automatique toutes les 30 secondes
        timer = new Timer(30000, e -> {
            if (vue != null && vue.isVisible()) {
                // Recharger les données
                java.lang.reflect.Method chargerMethod;
                try {
                    chargerMethod = vue.getClass().getDeclaredMethod("chargerStationnementActif");
                    chargerMethod.invoke(vue);
                } catch (Exception ex) {
                    System.err.println("Erreur lors du rechargement des données: " + ex.getMessage());
                }
                
                // Rafraîchir l'affichage
                java.lang.reflect.Method afficherMethod;
                try {
                    afficherMethod = vue.getClass().getDeclaredMethod("afficherInformationsStationnement");
                    afficherMethod.invoke(vue);
                } catch (Exception ex) {
                    System.err.println("Erreur lors du rafraîchissement de l'affichage: " + ex.getMessage());
                }
                
                // Vérifier si le stationnement existe encore
                try {
                    java.lang.reflect.Method getStationnementMethod = vue.getClass().getMethod("getStationnementActif");
                    Object stationnement = getStationnementMethod.invoke(vue);
                    
                    if (stationnement == null) {
                        JOptionPane.showMessageDialog(vue, 
                            "Le stationnement a été terminé.", 
                            "Information", 
                            JOptionPane.INFORMATION_MESSAGE);
                        retourAccueil();
                    }
                } catch (Exception ex) {
                    System.err.println("Erreur lors de la vérification du stationnement: " + ex.getMessage());
                }
            }
        });
        timer.start();
    }
    
    private void arreterTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
    
    // Méthodes pour la gestion des états
    
    public EtatStationnement getEtat() {
        return etat;
    }
    
    public String getEtatString() {
        return etat.toString();
    }
    
    public void setVue(Page_Stationnement_En_Cours vue) {
        this.vue = vue;
    }
    
    public void nettoyer() {
        arreterTimer();
    }
}
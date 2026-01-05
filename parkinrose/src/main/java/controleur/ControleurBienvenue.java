package controleur;

import ihm.Page_Bienvenue;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControleurBienvenue implements ActionListener {
    
    private enum EtatBienvenue {
        INITIAL,
        ATTENTE,
        REDIRECTION
    }
    
    private Page_Bienvenue vue;
    private EtatBienvenue etat;
    
    public ControleurBienvenue(Page_Bienvenue vue) {
        this.vue = vue;
        this.etat = EtatBienvenue.INITIAL;
        configurerListeners();
        
        etat = EtatBienvenue.ATTENTE;
    }
    
    private void configurerListeners() {
        vue.getBtnEntrer().addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = getActionBouton((JButton) e.getSource());
        
        switch (etat) {
            case ATTENTE:
                if (action.equals("ENTRER")) {
                    etat = EtatBienvenue.REDIRECTION;
                    ouvrirAuthentification();
                }
                break;
                
            case REDIRECTION:
                break;
        }
    }
    
    private void ouvrirAuthentification() {
        ihm.Page_Authentification pageAuth = new ihm.Page_Authentification();
        pageAuth.setVisible(true);
        vue.dispose();
    }
    
    private String getActionBouton(JButton b) {
        String texte = b.getText();
        if (texte != null) {
            if (texte.contains("ENTRER")) {
                return "ENTRER";
            }
        }
        return "INCONNU";
    }
}
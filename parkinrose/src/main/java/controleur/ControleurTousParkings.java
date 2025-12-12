package controleur;

import ihm.Page_Tous_Parkings;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

public class ControleurTousParkings implements ActionListener {
    
    private Page_Tous_Parkings vue;
    
    public ControleurTousParkings(Page_Tous_Parkings vue) {
        this.vue = vue;
        configurerListeners();
    }
    
    private void configurerListeners() {
        // Configurer les listeners pour les filtres
        if (vue.comboFiltres != null) {
            vue.comboFiltres.addActionListener(e -> vue.appliquerFiltres());
        }
        
        if (vue.checkGratuit != null) {
            vue.checkGratuit.addActionListener(e -> vue.appliquerFiltres());
        }
        if (vue.checkSoiree != null) {
            vue.checkSoiree.addActionListener(e -> vue.appliquerFiltres());
        }
        if (vue.checkRelais != null) {
            vue.checkRelais.addActionListener(e -> vue.appliquerFiltres());
        }
        if (vue.checkMoto != null) {
            vue.checkMoto.addActionListener(e -> vue.appliquerFiltres());
        }
        
        // Configurer les boutons de manière récursive
        configurerListenersRecursifs(vue.getContentPane());
    }
    
    private void configurerListenersRecursifs(java.awt.Container container) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                ((JButton) comp).addActionListener(this);
            } else if (comp instanceof javax.swing.JPanel) {
                configurerListenersRecursifs((javax.swing.JPanel) comp);
            } else if (comp instanceof javax.swing.JScrollPane) {
                javax.swing.JScrollPane scrollPane = (javax.swing.JScrollPane) comp;
                configurerListenersRecursifs(scrollPane.getViewport());
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        
        if (action == null) {
            action = getActionFromButton((JButton) e.getSource());
        }
        
        if (action.equals("RETOUR")) {
            retourAccueil();
        } else if (action.startsWith("STATIONNER_")) {
            int index = Integer.parseInt(action.replace("STATIONNER_", ""));
            selectionnerParking(index);
        }
    }
    
    private void selectionnerParking(int index) {
        if (index >= 0 && index < vue.parkingsFiltres.size()) {
            modele.Parking parking = vue.parkingsFiltres.get(index);
            
            int choix = JOptionPane.showConfirmDialog(vue,
                "Voulez-vous préparer un stationnement pour :\n" +
                parking.getLibelleParking() + "\n" +
                parking.getAdresseParking() + "\n\n" +
                "Places voiture: " + parking.getPlacesDisponibles() + "/" + parking.getNombrePlaces() + "\n" +
                (parking.hasMoto() ? "Places moto: " + parking.getPlacesMotoDisponibles() + "/" + parking.getPlacesMoto() + "\n" : "") +
                "Hauteur maximale: " + parking.getHauteurParking() + "m",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);
                
            if (choix == JOptionPane.YES_OPTION) {
                ihm.Page_Garer_Parking pageParking = new ihm.Page_Garer_Parking(vue.emailUtilisateur, parking);
                pageParking.setVisible(true);
                vue.dispose();
            }
        }
    }
    
    private void retourAccueil() {
        ihm.Page_Principale pagePrincipale = new ihm.Page_Principale(vue.emailUtilisateur);
        pagePrincipale.setVisible(true);
        vue.dispose();
    }
    
    private String getActionFromButton(JButton b) {
        String texte = b.getText();
        if (texte != null) {
            if (texte.contains("Retour")) {
                return "RETOUR";
            }
        }
        return "INCONNU";
    }
}
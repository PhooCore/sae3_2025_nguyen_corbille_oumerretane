package controleur;

import ihm.Page_Resultats_Recherche;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControleurResultatsRecherche implements ActionListener {
    
    private Page_Resultats_Recherche vue;
    
    public ControleurResultatsRecherche(Page_Resultats_Recherche vue) {
        this.vue = vue;
        configurerListeners();
    }
    
    public void configurerListeners() {
        // Configurer les listeners pour les filtres
        if (vue.comboFiltres != null) {
            vue.comboFiltres.addActionListener(this);
        }
        
        if (vue.checkGratuit != null) {
            vue.checkGratuit.addActionListener(this);
        }
        if (vue.checkSoiree != null) {
            vue.checkSoiree.addActionListener(this);
        }
        if (vue.checkRelais != null) {
            vue.checkRelais.addActionListener(this);
        }
        if (vue.checkMoto != null) {
            vue.checkMoto.addActionListener(this);
        }
        
        // Configurer les boutons de manière récursive
        configurerListenersRecursifs(vue.getContentPane());
    }
    
    // Nouvelle méthode pour reconfigurer les listeners après filtrage
    public void configurerListenersApresFiltrage() {
        configurerListenersRecursifs(vue.getContentPane());
    }
    
    private void configurerListenersRecursifs(java.awt.Container container) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                // Ajouter le listener seulement si le bouton n'en a pas encore
                boolean alreadyHasListener = false;
                for (ActionListener listener : button.getActionListeners()) {
                    if (listener == this) {
                        alreadyHasListener = true;
                        break;
                    }
                }
                if (!alreadyHasListener) {
                    button.addActionListener(this);
                }
            } else if (comp instanceof JPanel) {
                configurerListenersRecursifs((JPanel) comp);
            } else if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                configurerListenersRecursifs(scrollPane.getViewport());
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        // Gérer les filtres
        if (source == vue.comboFiltres || 
            source == vue.checkGratuit || 
            source == vue.checkSoiree || 
            source == vue.checkRelais || 
            source == vue.checkMoto) {
            vue.appliquerFiltres();
            return;
        }
        
        // Gérer les boutons avec action command
        if (source instanceof JButton) {
            JButton button = (JButton) source;
            String action = button.getActionCommand();
            
            if (action != null) {
                if (action.equals("RETOUR")) {
                    retourAccueil();
                } else if (action.equals("TOUS_PARKINGS")) {
                    afficherTousParkings();
                } else if (action.startsWith("STATIONNER_")) {
                    int index = Integer.parseInt(action.replace("STATIONNER_", ""));
                    selectionnerParking(index);
                }
            }
        }
    }
    
    private void selectionnerParking(int index) {
        if (index >= 0 && index < vue.parkingsFiltres.size()) {
            modele.Parking parking = vue.parkingsFiltres.get(index);
            
            int choix = JOptionPane.showConfirmDialog(vue,
                "Voulez-vous préparer un stationnement pour :\n" +
                parking.getLibelleParking() + "\n" +
                parking.getAdresseParking() + "\n\n" +
                "Places disponibles: " + parking.getPlacesDisponibles() + "/" + parking.getNombrePlaces() + "\n" +
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
    
    private void afficherTousParkings() {
        java.util.List<modele.Parking> tousParkings = modele.dao.ParkingDAO.getAllParkings();
        ihm.Page_Tous_Parkings pageTousParkings = new ihm.Page_Tous_Parkings(vue.emailUtilisateur, tousParkings);
        pageTousParkings.setVisible(true);
        vue.dispose();
    }
}
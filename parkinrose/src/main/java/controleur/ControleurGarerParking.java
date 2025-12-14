package controleur;

import ihm.Page_Garer_Parking;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

public class ControleurGarerParking implements ActionListener {
    
    private enum EtatParking {
        INITIAL,
        SELECTION,
        VERIFICATION,
        RESERVATION,
        REDIRECTION
    }
    
    private Page_Garer_Parking vue;
    private EtatParking etat;
    private StationnementControleur controleur;
    
    public ControleurGarerParking(Page_Garer_Parking vue) {
        this.vue = vue;
        this.controleur = new StationnementControleur(vue.emailUtilisateur);
        this.etat = EtatParking.INITIAL;
        configurerListeners();
        
        etat = EtatParking.SELECTION;
    }
    
    private void configurerListeners() {
        // Ajouter ActionListener aux boutons
        vue.getBtnAnnuler().addActionListener(this);
        vue.getBtnReserver().addActionListener(this);
        vue.getBtnModifierPlaque().addActionListener(this);
        
        // Ajouter ItemListener au combobox
        vue.comboParking.addItemListener(e -> {
            if (etat == EtatParking.SELECTION && e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                mettreAJourInfosParking(vue.comboParking.getSelectedIndex());
            }
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = getActionBouton((JButton) e.getSource());
        
        System.out.println("Action: " + action + " - État: " + etat);
        
        switch (etat) {
            case SELECTION:
                if (action.equals("ANNULER")) {
                    etat = EtatParking.REDIRECTION;
                    annuler();
                } else if (action.equals("RESERVER")) {
                    etat = EtatParking.VERIFICATION;
                    verifierReservation();
                } else if (action.equals("MODIFIER_PLAQUE")) {
                    modifierPlaque();
                }
                break;
                
            case VERIFICATION:
                if (action.equals("CONFIRMER")) {
                    etat = EtatParking.RESERVATION;
                    reserverPlace();
                } else if (action.equals("MODIFIER")) {
                    etat = EtatParking.SELECTION;
                }
                break;
                
            case REDIRECTION:
                // Ne rien faire
                break;
        }
    }
    
    private void verifierReservation() {
        int index = vue.comboParking.getSelectedIndex();
        if (index >= 0 && index < vue.listeParkings.size()) {
            modele.Parking parking = vue.listeParkings.get(index);
            String typeVehicule = vue.getTypeVehicule();
            
            // Vérification pour les motos
            if ("Moto".equals(typeVehicule)) {
                if (!parking.hasMoto()) {
                    JOptionPane.showMessageDialog(vue,
                        "Ce parking ne dispose pas de places pour les motos",
                        "Parking non adapté",
                        JOptionPane.WARNING_MESSAGE);
                    etat = EtatParking.SELECTION;
                    return;
                }
                if (parking.getPlacesMotoDisponibles() <= 0) {
                    JOptionPane.showMessageDialog(vue,
                        "Plus de places moto disponibles dans ce parking",
                        "Parking complet",
                        JOptionPane.WARNING_MESSAGE);
                    etat = EtatParking.SELECTION;
                    return;
                }
            }
            
            // Demander confirmation
            int choix = JOptionPane.showConfirmDialog(vue,
                "Confirmez-vous la réservation pour le parking :\n" +
                parking.getLibelleParking() + "\n" +
                "Véhicule: " + typeVehicule,
                "Confirmation de réservation",
                JOptionPane.YES_NO_OPTION);
                
            if (choix == JOptionPane.YES_OPTION) {
                etat = EtatParking.RESERVATION;
                reserverPlace();
            } else {
                etat = EtatParking.SELECTION;
            }
        }
    }
    
    private void reserverPlace() {
        int index = vue.comboParking.getSelectedIndex();
        if (index >= 0 && index < vue.listeParkings.size()) {
            modele.Parking parking = vue.listeParkings.get(index);
            String typeVehicule = vue.getTypeVehicule();
            
            boolean succes = controleur.preparerStationnementParking(
                typeVehicule,
                vue.lblPlaque.getText(),
                parking.getIdParking(),
                vue
            );
            
            if (succes) {
                etat = EtatParking.REDIRECTION;
            } else {
                etat = EtatParking.SELECTION;
            }
        }
    }
    
    private void modifierPlaque() {
        String nouvellePlaque = JOptionPane.showInputDialog(vue, 
            "Entrez la plaque d'immatriculation:", 
            vue.lblPlaque.getText());
        
        if (nouvellePlaque != null && !nouvellePlaque.trim().isEmpty()) {
            String plaqueNettoyee = nouvellePlaque.trim().toUpperCase();
            
            if (controleur.validerPlaque(plaqueNettoyee)) {
                vue.lblPlaque.setText(plaqueNettoyee);
            } else {
                JOptionPane.showMessageDialog(vue,
                    "Format de plaque invalide. Utilisez AA-123-AA",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void mettreAJourInfosParking(int index) {
        // Utiliser le getter public pour accéder à lblPlacesDispo
        if (index >= 0 && index < vue.listeParkings.size()) {
            modele.Parking parking = vue.listeParkings.get(index);
            vue.getLblPlacesDispo().setText(parking.getPlacesDisponibles() + " / " + parking.getNombrePlaces());
            
            if (parking.getPlacesDisponibles() <= 5) {
                vue.getLblPlacesDispo().setForeground(java.awt.Color.RED);
            } else if (parking.getPlacesDisponibles() <= 10) {
                vue.getLblPlacesDispo().setForeground(java.awt.Color.ORANGE);
            } else {
                vue.getLblPlacesDispo().setForeground(java.awt.Color.BLACK);
            }
        }
    }
    
    private void annuler() {
        etat = EtatParking.REDIRECTION;
        ihm.Page_Principale pagePrincipale = new ihm.Page_Principale(vue.emailUtilisateur);
        pagePrincipale.setVisible(true);
        vue.dispose();
    }
    
    private String getActionBouton(JButton b) {
        String texte = b.getText();
        if (texte != null) {
            if (texte.contains("Annuler")) {
                return "ANNULER";
            } else if (texte.contains("Réserver") || texte.contains("Stationner")) {
                return "RESERVER";
            } else if (texte.contains("Modifier")) {
                return "MODIFIER_PLAQUE";
            }
        }
        return "INCONNU";
    }
}
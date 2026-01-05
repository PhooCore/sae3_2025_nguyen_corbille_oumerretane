package controleur;

import ihm.Page_Tous_Parkings;
import ihm.Page_Garer_Parking;
import ihm.Page_Principale;
import ihm.Page_Utilisateur;
import modele.Parking;
import modele.Usager;
import modele.dao.TarifParkingDAO;
import modele.dao.UsagerDAO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ControleurTousParkings implements ActionListener {
    
    // État du contrôleur
    private enum EtatControleur {
        INITIAL,
        FILTRAGE_EN_COURS,
        SELECTION_PARKING,
        VERIFICATION_CARTE,
        CONFIRMATION_STATIONNEMENT,
        OUVERTURE_STATIONNEMENT,
        OUVERTURE_PROFIL,
        RETOUR_ACCUEIL
    }
    
    private Page_Tous_Parkings vue;
    private EtatControleur etat;
    private Parking parkingSelectionne;
    private String carteTisseoUtilisateur;
    private int indexParkingSelectionne;
    
    public ControleurTousParkings(Page_Tous_Parkings vue) {
        this.vue = vue;
        this.etat = EtatControleur.INITIAL;
        this.parkingSelectionne = null;
        this.carteTisseoUtilisateur = null;
        this.indexParkingSelectionne = -1;
        
        initialiserControleur();
        chargerCarteTisseoUtilisateur();
    }
    
    private void initialiserControleur() {
        configurerListeners();
    }
    
    private void chargerCarteTisseoUtilisateur() {
        try {
            String email = vue.getEmailUtilisateur();
            if (email != null) {
                Usager usager = UsagerDAO.getUsagerByEmail(email);
                if (usager != null) {
                    carteTisseoUtilisateur = UsagerDAO.getInstance().getCarteTisseoByUsager(usager.getIdUsager());
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la carte Tisséo: " + e.getMessage());
        }
    }
    
    private void configurerListeners() {
        configurerListenersFiltres();
        configurerListenersRecursifs(vue.getContentPane());
    }
    
    private void configurerListenersFiltres() {
        if (vue.getComboFiltres() != null) {
            vue.getComboFiltres().addActionListener(this);
        }
        
        if (vue.getCheckGratuit() != null) {
            vue.getCheckGratuit().addActionListener(this);
        }
        if (vue.getCheckSoiree() != null) {
            vue.getCheckSoiree().addActionListener(this);
        }
        if (vue.getCheckRelais() != null) {
            vue.getCheckRelais().addActionListener(this);
        }
        if (vue.getCheckMoto() != null) {
            vue.getCheckMoto().addActionListener(this);
        }
    }
    
    private void configurerListenersRecursifs(java.awt.Container container) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                ((JButton) comp).addActionListener(this);
            } else if (comp instanceof JPanel) {
                configurerListenersRecursifs((JPanel) comp);
            } else if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                if (scrollPane.getViewport() != null && scrollPane.getViewport().getView() != null) {
                    configurerListenersRecursifs((java.awt.Container) scrollPane.getViewport().getView());
                }
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = obtenirAction(e);
        
        switch (etat) {
            case INITIAL:
                traiterEtatInitial(action);
                break;
                
            case FILTRAGE_EN_COURS:
                traiterEtatFiltrageEnCours(action);
                break;
                
            case SELECTION_PARKING:
                traiterEtatSelectionParking(action);
                break;
                
            case VERIFICATION_CARTE:
                traiterEtatVerificationCarte(action);
                break;
                
            case CONFIRMATION_STATIONNEMENT:
                traiterEtatConfirmationStationnement(action);
                break;
                
            case OUVERTURE_STATIONNEMENT:
                traiterEtatOuvertureStationnement(action);
                break;
                
            case OUVERTURE_PROFIL:
                traiterEtatOuvertureProfil(action);
                break;
                
            case RETOUR_ACCUEIL:
                traiterEtatRetourAccueil(action);
                break;
        }
    }
    
    private String obtenirAction(ActionEvent e) {
        Object source = e.getSource();
        
        if (source instanceof JComboBox) {
            return "FILTRE_COMBO";
        } else if (source instanceof JCheckBox) {
            return "FILTRE_CHECKBOX";
        } else if (source instanceof JButton) {
            String texte = ((JButton) source).getText();
            if (texte != null) {
                if (texte.contains("Retour")) {
                    return "RETOUR";
                } else if (texte.startsWith("Stationner")) {
                    return "STATIONNER_" + extraireIndexParking(texte);
                }
            }
        }
        
        return e.getActionCommand();
    }
    
    private int extraireIndexParking(String texteBouton) {
        try {
            // Exemple: "Stationner parking 1" -> extraire "1"
            String[] parties = texteBouton.split(" ");
            String dernierElement = parties[parties.length - 1];
            return Integer.parseInt(dernierElement) - 1; // -1 car les indices commencent à 0
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private void traiterEtatInitial(String action) {
        switch (action) {
            case "FILTRE_COMBO":
            case "FILTRE_CHECKBOX":
                etat = EtatControleur.FILTRAGE_EN_COURS;
                appliquerFiltres();
                etat = EtatControleur.INITIAL;
                break;
                
            case "RETOUR":
                etat = EtatControleur.RETOUR_ACCUEIL;
                retourAccueil();
                break;
                
            default:
                if (action.startsWith("STATIONNER_")) {
                    etat = EtatControleur.SELECTION_PARKING;
                    int index = Integer.parseInt(action.replace("STATIONNER_", ""));
                    selectionnerParking(index);
                }
                break;
        }
    }
    
    private void traiterEtatFiltrageEnCours(String action) {
        // En cours de filtrage, on ignore les autres actions
        if (action.equals("ANNULER_FILTRAGE")) {
            etat = EtatControleur.INITIAL;
        }
    }
    
    private void traiterEtatSelectionParking(String action) {
        if (action.equals("ANNULER_SELECTION")) {
            etat = EtatControleur.INITIAL;
            parkingSelectionne = null;
        } else if (action.equals("VERIFIER_TYPE_PARKING")) {
            verifierTypeParking();
        }
    }
    
    private void traiterEtatVerificationCarte(String action) {
        switch (action) {
            case "CARTE_VALIDE":
                etat = EtatControleur.CONFIRMATION_STATIONNEMENT;
                demanderConfirmationParkingRelais();
                break;
                
            case "CARTE_INVALIDE":
                etat = EtatControleur.INITIAL;
                afficherMessageAccesRefuse();
                break;
                
            case "AJOUTER_CARTE_TISSEO":
                etat = EtatControleur.OUVERTURE_PROFIL;
                ouvrirPageUtilisateur();
                break;
                
            case "ANNULER_ACCES":
                etat = EtatControleur.INITIAL;
                parkingSelectionne = null;
                break;
        }
    }
    
    private void traiterEtatConfirmationStationnement(String action) {
        switch (action) {
            case "CONFIRMER_STATIONNEMENT":
                etat = EtatControleur.OUVERTURE_STATIONNEMENT;
                ouvrirPageStationnement();
                break;
                
            case "ANNULER_STATIONNEMENT":
                etat = EtatControleur.INITIAL;
                parkingSelectionne = null;
                break;
        }
    }
    
    private void traiterEtatOuvertureStationnement(String action) {
        // En cours d'ouverture du stationnement
        if (action.equals("ERREUR_OUVERTURE")) {
            etat = EtatControleur.INITIAL;
            parkingSelectionne = null;
        }
    }
    
    private void traiterEtatOuvertureProfil(String action) {
        // En cours d'ouverture du profil
        if (action.equals("FERMER_PROFIL")) {
            etat = EtatControleur.INITIAL;
        }
    }
    
    private void traiterEtatRetourAccueil(String action) {
        // En cours de retour à l'accueil
        if (action.equals("ANNULER_RETOUR")) {
            etat = EtatControleur.INITIAL;
        }
    }
    
    private void appliquerFiltres() {
        vue.appliquerFiltres();
    }
    
    private void selectionnerParking(int index) {
        if (index >= 0 && index < vue.getParkingsFiltres().size()) {
            parkingSelectionne = vue.getParkingsFiltres().get(index);
            indexParkingSelectionne = index;
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "VERIFIER_TYPE_PARKING"));
        } else {
            JOptionPane.showMessageDialog(vue, "Parking non trouvé", "Erreur", JOptionPane.ERROR_MESSAGE);
            etat = EtatControleur.INITIAL;
        }
    }
    
    private void verifierTypeParking() {
        try {
            if (TarifParkingDAO.getInstance().estParkingRelais(parkingSelectionne.getIdParking())) {
                etat = EtatControleur.VERIFICATION_CARTE;
                verifierCarteTisseo();
            } else {
                etat = EtatControleur.CONFIRMATION_STATIONNEMENT;
                demanderConfirmationStationnement();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vue, 
                "Erreur lors de la vérification du parking: " + ex.getMessage(), 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            etat = EtatControleur.INITIAL;
        }
    }
    
    private void verifierCarteTisseo() {
        if (carteTisseoUtilisateur == null || carteTisseoUtilisateur.trim().isEmpty()) {
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CARTE_INVALIDE"));
        } else {
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CARTE_VALIDE"));
        }
    }
    
    private void afficherMessageAccesRefuse() {
        Object[] options = {"Ajouter une carte Tisséo", "Annuler"};
        
        String message = "🚫  ACCÈS IMPOSSIBLE\n\n" +
                        parkingSelectionne.getLibelleParking() + "\n" +
                        "(" + parkingSelectionne.getAdresseParking() + ")\n\n" +
                        "❌  Ce parking relais est exclusivement réservé\n" +
                        "aux détenteurs d'une carte Tisséo (Pastel).\n\n" +
                        "Vous ne pouvez pas stationner dans ce parking\n" +
                        "sans présenter votre carte Tisséo.\n\n" +
                        "Veuillez ajouter votre carte Tisséo à votre compte\n" +
                        "pour accéder à ce parking.";
        
        int choix = JOptionPane.showOptionDialog(
            vue,
            message,
            "Accès refusé - Parking réservé",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choix == JOptionPane.YES_OPTION) {
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "AJOUTER_CARTE_TISSEO"));
        } else {
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ANNULER_ACCES"));
        }
    }
    
    private void demanderConfirmationParkingRelais() {
        String numeroMasque = masquerNumeroCarte(carteTisseoUtilisateur);
        String message = construireMessageConfirmationParkingRelais(numeroMasque);
        
        int choix = JOptionPane.showConfirmDialog(vue,
            message,
            "Stationnement parking relais",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE);
        
        if (choix == JOptionPane.YES_OPTION) {
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CONFIRMER_STATIONNEMENT"));
        } else {
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ANNULER_STATIONNEMENT"));
        }
    }
    
    private String construireMessageConfirmationParkingRelais(String numeroCarteMasque) {
        StringBuilder message = new StringBuilder();
        message.append("Vous avez une carte Tisséo valide : ").append(numeroCarteMasque).append("\n\n")
               .append("Voulez-vous préparer un stationnement pour :\n")
               .append(parkingSelectionne.getLibelleParking()).append("\n")
               .append(parkingSelectionne.getAdresseParking()).append("\n\n")
               .append("Parking relais - Stationnement gratuit avec carte Tisséo\n")
               .append("Places voiture: ")
               .append(parkingSelectionne.getPlacesDisponibles()).append("/")
               .append(parkingSelectionne.getNombrePlaces()).append("\n");
        
        if (parkingSelectionne.hasMoto()) {
            message.append("Places moto: ")
                   .append(parkingSelectionne.getPlacesMotoDisponibles()).append("/")
                   .append(parkingSelectionne.getPlacesMoto()).append("\n");
        }
        
        message.append("Hauteur maximale: ").append(parkingSelectionne.getHauteurParking()).append("m");
        
        return message.toString();
    }
    
    private void demanderConfirmationStationnement() {
        String message = construireMessageConfirmation();
        
        int choix = JOptionPane.showConfirmDialog(vue,
            message,
            "Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (choix == JOptionPane.YES_OPTION) {
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CONFIRMER_STATIONNEMENT"));
        } else {
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ANNULER_STATIONNEMENT"));
        }
    }
    
    private String construireMessageConfirmation() {
        StringBuilder message = new StringBuilder();
        message.append("Voulez-vous préparer un stationnement pour :\n")
               .append(parkingSelectionne.getLibelleParking()).append("\n")
               .append(parkingSelectionne.getAdresseParking()).append("\n\n")
               .append("Places voiture: ")
               .append(parkingSelectionne.getPlacesDisponibles()).append("/")
               .append(parkingSelectionne.getNombrePlaces()).append("\n");

        if (parkingSelectionne.hasMoto()) {
            message.append("Places moto: ")
                   .append(parkingSelectionne.getPlacesMotoDisponibles()).append("/")
                   .append(parkingSelectionne.getPlacesMoto()).append("\n");
        }

        message.append("Hauteur maximale: ")
               .append(parkingSelectionne.getHauteurParking()).append("m\n");

        try {
            if (TarifParkingDAO.getInstance().estParkingGratuit(parkingSelectionne.getIdParking())) {
                message.append("\nParking gratuit\n");
            } else if (parkingSelectionne.hasTarifSoiree()) {
                message.append("\nTarif soirée disponible: 5.90€\n");
                message.append("(Arrivée 19h30-minuit, départ avant 3h)\n");
            } else {
                double tarifHoraire = TarifParkingDAO.getInstance().getTarifHoraire(parkingSelectionne.getIdParking());
                message.append(String.format("\nTarif: %.2f€/h (%.2f€/15min)\n", 
                    tarifHoraire, tarifHoraire/4));
            }
        } catch (Exception e) {
            // Ignorer l'erreur pour le message
        }

        return message.toString();
    }
    
    private void ouvrirPageStationnement() {
        try {
            Page_Garer_Parking pageParking = new Page_Garer_Parking(
                vue.getEmailUtilisateur(), 
                parkingSelectionne
            );
            pageParking.setVisible(true);
            vue.dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(vue, 
                "Erreur lors de l'ouverture de la page de stationnement: " + e.getMessage(), 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ERREUR_OUVERTURE"));
        }
    }
    
    private void ouvrirPageUtilisateur() {
        try {
            Page_Utilisateur pageUtilisateur = new Page_Utilisateur(vue.getEmailUtilisateur());
            pageUtilisateur.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(vue, 
                "Erreur lors de l'ouverture de la page utilisateur: " + e.getMessage(), 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void retourAccueil() {
        try {
            Page_Principale pagePrincipale = new Page_Principale(vue.getEmailUtilisateur());
            pagePrincipale.setVisible(true);
            vue.dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(vue, 
                "Erreur lors du retour à l'accueil: " + e.getMessage(), 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String masquerNumeroCarte(String numeroCarte) {
        if (numeroCarte != null && numeroCarte.length() >= 4) {
            return numeroCarte.substring(0, 4) + "******";
        }
        return numeroCarte != null ? numeroCarte : "Non disponible";
    }
    
    // Méthodes pour obtenir l'état actuel
    public EtatControleur getEtat() {
        return etat;
    }
    
    public Parking getParkingSelectionne() {
        return parkingSelectionne;
    }
    
    public String getCarteTisseoUtilisateur() {
        return carteTisseoUtilisateur;
    }
}
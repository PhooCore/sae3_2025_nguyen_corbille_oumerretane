package controleur;

import ihm.Page_Garer_Parking;
import ihm.Page_Resultats_Recherche;
import ihm.Page_Tous_Parkings;
import ihm.Page_Principale;
import ihm.Page_Utilisateur;
import modele.Parking;
import modele.Usager;
import modele.dao.TarifParkingDAO;
import modele.dao.UsagerDAO;
import modele.dao.ParkingDAO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

public class ControleurResultatsRecherche implements ActionListener {
    
    // États possibles du contrôleur
    public enum EtatResultats {
        AFFICHAGE_RESULTATS,
        FILTRAGE_EN_COURS,
        SELECTION_PARKING,
        VERIFICATION_ACCES,
        DEMANDE_CONFIRMATION,
        STATIONNEMENT_EN_COURS,
        RETOUR_ACCUEIL,
        AFFICHAGE_TOUS_PARKINGS
    }
    
    // Constantes pour les actions
    private static final String ACTION_RETOUR = "RETOUR";
    private static final String ACTION_TOUS_PARKINGS = "TOUS_PARKINGS";
    private static final String ACTION_STATIONNER_PREFIX = "STATIONNER_";
    private static final String ACTION_FILTRE_COMBO = "FILTRE_COMBO";
    private static final String ACTION_FILTRE_CHECKBOX = "FILTRE_CHECKBOX";
    
    // Messages
    private static final String TITRE_ERREUR = "Erreur";
    private static final String TITRE_SYSTEME = "Erreur système";
    private static final String TITRE_CARTE_TISSEO = "Carte Tisséo requise";
    private static final String TITRE_ACCES_REFUSE = "Accès refusé - Parking réservé";
    private static final String TITRE_CONFIRMATION = "Confirmation";
    
    // Composants
    private final Page_Resultats_Recherche vue;
    private EtatResultats etat;
    
    // Variables temporaires pour le processus en cours
    private Parking parkingSelectionne;
    private int indexParkingSelectionne;
    
    public ControleurResultatsRecherche(Page_Resultats_Recherche vue) {
        this.vue = vue;
        this.etat = EtatResultats.AFFICHAGE_RESULTATS;
        this.parkingSelectionne = null;
        initialiserControleur();
    }
    
    /**
     * Initialise le contrôleur
     */
    private void initialiserControleur() {
        configurerListeners();
    }
    
    /**
     * Configure les écouteurs d'événements
     */
    private void configurerListeners() {
        configurerListenersFiltres();
        configurerListenersBoutons();
    }
    
    /**
     * Configure les écouteurs pour les filtres
     */
    private void configurerListenersFiltres() {
        try {
            // Accès par réflexion aux composants
            java.lang.reflect.Field comboField = vue.getClass().getDeclaredField("comboFiltres");
            comboField.setAccessible(true);
            JComboBox<String> combo = (JComboBox<String>) comboField.get(vue);
            if (combo != null) {
                combo.addActionListener(e -> 
                    actionPerformed(new ActionEvent(combo, ActionEvent.ACTION_PERFORMED, ACTION_FILTRE_COMBO)));
            }
            
            String[] checkboxes = {"checkGratuit", "checkSoiree", "checkRelais", "checkMoto"};
            for (String checkboxName : checkboxes) {
                java.lang.reflect.Field field = vue.getClass().getDeclaredField(checkboxName);
                field.setAccessible(true);
                JCheckBox checkbox = (JCheckBox) field.get(vue);
                if (checkbox != null) {
                    checkbox.addActionListener(e -> 
                        actionPerformed(new ActionEvent(checkbox, ActionEvent.ACTION_PERFORMED, ACTION_FILTRE_CHECKBOX)));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des filtres: " + e.getMessage());
        }
    }
    
    /**
     * Configure les écouteurs pour les boutons
     */
    private void configurerListenersBoutons() {
        configurerListenersRecursifs(vue.getContentPane());
    }
    
    /**
     * Reconfigure les listeners après filtrage
     */
    public void configurerListenersApresFiltrage() {
        configurerListenersBoutons();
    }
    
    /**
     * Configure les écouteurs de manière récursive
     */
    private void configurerListenersRecursifs(java.awt.Container container) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                configurerListenerBouton((JButton) comp);
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
    
    /**
     * Configure l'écouteur pour un bouton spécifique
     */
    private void configurerListenerBouton(JButton button) {
        if (!estBoutonDejaConfigure(button)) {
            button.addActionListener(this);
        }
    }
    
    /**
     * Vérifie si un bouton a déjà ce contrôleur comme écouteur
     */
    private boolean estBoutonDejaConfigure(JButton button) {
        for (ActionListener listener : button.getActionListeners()) {
            if (listener == this) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gère les événements d'action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        
        // Traiter selon l'état courant
        switch (etat) {
            case AFFICHAGE_RESULTATS:
                traiterActionAffichage(action, e);
                break;
                
            case FILTRAGE_EN_COURS:
                // En cours de filtrage, ignorer les autres actions
                break;
                
            case SELECTION_PARKING:
                if (action.equals("ANNULER_SELECTION")) {
                    etat = EtatResultats.AFFICHAGE_RESULTATS;
                }
                break;
                
            case VERIFICATION_ACCES:
                if (action.equals("AJOUTER_CARTE_TISSEO")) {
                    ajouterCarteTisseo();
                } else if (action.equals("ANNULER_ACCES")) {
                    etat = EtatResultats.AFFICHAGE_RESULTATS;
                }
                break;
                
            case DEMANDE_CONFIRMATION:
                if (action.equals("CONFIRMER_STATIONNEMENT")) {
                    preparerStationnement();
                } else if (action.equals("ANNULER_STATIONNEMENT")) {
                    etat = EtatResultats.AFFICHAGE_RESULTATS;
                }
                break;
                
            case STATIONNEMENT_EN_COURS:
                // En cours d'ouverture de stationnement
                break;
                
            case RETOUR_ACCUEIL:
                // En cours de retour à l'accueil
                break;
                
            case AFFICHAGE_TOUS_PARKINGS:
                // En cours d'affichage de tous les parkings
                break;
        }
    }
    
    private void traiterActionAffichage(String action, ActionEvent e) {
        switch (action) {
            case ACTION_FILTRE_COMBO:
            case ACTION_FILTRE_CHECKBOX:
                appliquerFiltres();
                break;
                
            case ACTION_RETOUR:
                retourAccueil();
                break;
                
            case ACTION_TOUS_PARKINGS:
                afficherTousParkings();
                break;
                
            default:
                if (action.startsWith(ACTION_STATIONNER_PREFIX)) {
                    selectionnerParking(action);
                }
                break;
        }
    }
    
    /**
     * Applique les filtres
     */
    private void appliquerFiltres() {
        etat = EtatResultats.FILTRAGE_EN_COURS;
        
        try {
            Method method = vue.getClass().getMethod("appliquerFiltres");
            method.invoke(vue);
            
            // Reconfigurer les listeners après filtrage
            configurerListenersApresFiltrage();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel de appliquerFiltres: " + e.getMessage());
        } finally {
            etat = EtatResultats.AFFICHAGE_RESULTATS;
        }
    }
    
    /**
     * Sélectionne un parking
     */
    private void selectionnerParking(String action) {
        etat = EtatResultats.SELECTION_PARKING;
        
        try {
            int index = extraireIndexParking(action);
            List<Parking> parkingsFiltres = obtenirParkingsFiltres();
            
            if (!estIndexValide(index, parkingsFiltres)) {
                etat = EtatResultats.AFFICHAGE_RESULTATS;
                return;
            }
            
            parkingSelectionne = parkingsFiltres.get(index);
            indexParkingSelectionne = index;
            
            verifierAccessibiliteParking();
        } catch (NumberFormatException e) {
            afficherErreur("Format d'index de parking invalide", TITRE_ERREUR);
            etat = EtatResultats.AFFICHAGE_RESULTATS;
        }
    }
    
    /**
     * Extrait l'index du parking de l'action
     */
    private int extraireIndexParking(String action) {
        return Integer.parseInt(action.replace(ACTION_STATIONNER_PREFIX, ""));
    }
    
    /**
     * Obtient la liste des parkings filtrés
     */
    private List<Parking> obtenirParkingsFiltres() {
        try {
            java.lang.reflect.Field field = vue.getClass().getDeclaredField("parkingsFiltres");
            field.setAccessible(true);
            return (List<Parking>) field.get(vue);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'accès à parkingsFiltres: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Vérifie si l'index est valide
     */
    private boolean estIndexValide(int index, List<Parking> parkingsFiltres) {
        return index >= 0 && index < parkingsFiltres.size();
    }
    
    /**
     * Vérifie l'accessibilité du parking
     */
    private void verifierAccessibiliteParking() {
        etat = EtatResultats.VERIFICATION_ACCES;
        
        try {
            boolean estRelais = TarifParkingDAO.getInstance().estParkingRelais(parkingSelectionne.getIdParking());
            
            if (estRelais) {
                verifierCarteTisseo();
            } else {
                etat = EtatResultats.DEMANDE_CONFIRMATION;
                demanderConfirmationStationnement();
            }
        } catch (Exception e) {
            afficherErreur("Erreur lors de la vérification du parking: " + e.getMessage(), TITRE_ERREUR);
            etat = EtatResultats.AFFICHAGE_RESULTATS;
        }
    }
    
    /**
     * Vérifie la carte Tisséo pour les parkings relais
     */
    private void verifierCarteTisseo() {
        String carteTisseo = obtenirCarteTisseoUtilisateur();
        
        if (carteTisseo == null) {
            afficherMessageAccesRefuse();
        } else {
            etat = EtatResultats.DEMANDE_CONFIRMATION;
            demanderConfirmationStationnement();
        }
    }
    
    /**
     * Obtient la carte Tisséo de l'utilisateur
     */
    private String obtenirCarteTisseoUtilisateur() {
        try {
            String email = obtenirEmailUtilisateur();
            if (email != null) {
                Usager usager = UsagerDAO.getUsagerByEmail(email);
                if (usager != null) {
                    return UsagerDAO.getInstance().getCarteTisseoByUsager(usager.getIdUsager());
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de la carte Tisséo: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Obtient l'email de l'utilisateur
     */
    private String obtenirEmailUtilisateur() {
        try {
            Method method = vue.getClass().getMethod("getEmailUtilisateur");
            return (String) method.invoke(vue);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'obtention de l'email: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Affiche le message d'accès refusé pour les parkings relais
     */
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
            TITRE_ACCES_REFUSE,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choix == JOptionPane.YES_OPTION) {
            ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "AJOUTER_CARTE_TISSEO");
            actionPerformed(e);
        } else {
            ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ANNULER_ACCES");
            actionPerformed(e);
        }
    }
    
    /**
     * Ajoute une carte Tisséo (ouvre la page utilisateur)
     */
    private void ajouterCarteTisseo() {
        try {
            String email = obtenirEmailUtilisateur();
            if (email != null) {
                Page_Utilisateur pageUtilisateur = new Page_Utilisateur(email);
                pageUtilisateur.setVisible(true);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la page utilisateur: " + e.getMessage());
        } finally {
            etat = EtatResultats.AFFICHAGE_RESULTATS;
        }
    }
    
    /**
     * Demande confirmation pour le stationnement
     */
    private void demanderConfirmationStationnement() {
        String message = construireMessageConfirmation(parkingSelectionne);
        
        int choix = JOptionPane.showConfirmDialog(
            vue,
            message,
            TITRE_CONFIRMATION,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choix == JOptionPane.YES_OPTION) {
            ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CONFIRMER_STATIONNEMENT");
            actionPerformed(e);
        } else {
            ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ANNULER_STATIONNEMENT");
            actionPerformed(e);
        }
    }
    
    /**
     * Construit le message de confirmation
     */
    private String construireMessageConfirmation(Parking parking) {
        StringBuilder message = new StringBuilder();
        
        message.append("Voulez-vous préparer un stationnement pour :\n")
               .append(parking.getLibelleParking()).append("\n")
               .append(parking.getAdresseParking()).append("\n\n")
               .append("Places voiture: ")
               .append(parking.getPlacesDisponibles()).append("/")
               .append(parking.getNombrePlaces()).append("\n");

        if (parking.hasMoto()) {
            message.append("Places moto: ")
                   .append(parking.getPlacesMotoDisponibles()).append("/")
                   .append(parking.getPlacesMoto()).append("\n");
        }

        message.append("Hauteur maximale: ")
               .append(parking.getHauteurParking()).append("m\n");

        try {
            if (TarifParkingDAO.getInstance().estParkingRelais(parking.getIdParking())) {
                String carteTisseo = obtenirCarteTisseoUtilisateur();
                if (carteTisseo != null) {
                    String numeroMasque = masquerNumeroCarte(carteTisseo);
                    message.append("\nCarte Tisséo détectée : ")
                           .append(numeroMasque)
                           .append("\nStationnement gratuit");
                }
            }
        } catch (Exception e) {
            // Ignorer l'exception pour le message
        }

        return message.toString();
    }
    
    /**
     * Masque une partie du numéro de carte
     */
    private String masquerNumeroCarte(String numeroCarte) {
        if (numeroCarte != null && numeroCarte.length() >= 4) {
            return numeroCarte.substring(0, 4) + "******";
        }
        return numeroCarte != null ? numeroCarte : "Non disponible";
    }
    
    /**
     * Prépare le stationnement
     */
    private void preparerStationnement() {
        etat = EtatResultats.STATIONNEMENT_EN_COURS;
        
        try {
            if (TarifParkingDAO.getInstance().estParkingRelais(parkingSelectionne.getIdParking())) {
                verifierCarteTisseoPourStationnement();
            }
            
            ouvrirPageStationnement();
        } catch (IllegalStateException e) {
            // Carte Tisséo manquante, retour à l'affichage
            etat = EtatResultats.AFFICHAGE_RESULTATS;
        } catch (Exception e) {
            afficherErreur("Erreur lors du traitement: " + e.getMessage(), TITRE_SYSTEME);
            etat = EtatResultats.AFFICHAGE_RESULTATS;
        }
    }
    
    /**
     * Vérifie la carte Tisséo pour le stationnement (version finale)
     */
    private void verifierCarteTisseoPourStationnement() {
        String carteTisseo = obtenirCarteTisseoUtilisateur();
        
        if (carteTisseo == null) {
            JOptionPane.showMessageDialog(
                vue,
                "Vous n'avez aucune carte Tisseo renseignée.",
                TITRE_CARTE_TISSEO,
                JOptionPane.WARNING_MESSAGE
            );
            throw new IllegalStateException("Carte Tisséo requise");
        }
    }
    
    /**
     * Ouvre la page de stationnement
     */
    private void ouvrirPageStationnement() {
        try {
            String email = obtenirEmailUtilisateur();
            if (email != null) {
                Page_Garer_Parking pageParking = new Page_Garer_Parking(email, parkingSelectionne);
                pageParking.setVisible(true);
                vue.dispose();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la page de stationnement: " + e.getMessage());
        }
    }
    
    /**
     * Retourne à l'accueil
     */
    private void retourAccueil() {
        etat = EtatResultats.RETOUR_ACCUEIL;
        
        try {
            String email = obtenirEmailUtilisateur();
            if (email != null) {
                Page_Principale pagePrincipale = new Page_Principale(email);
                pagePrincipale.setVisible(true);
                vue.dispose();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du retour à l'accueil: " + e.getMessage());
        }
    }
    
    /**
     * Affiche tous les parkings
     */
    private void afficherTousParkings() {
        etat = EtatResultats.AFFICHAGE_TOUS_PARKINGS;
        
        try {
            List<Parking> tousParkings = ParkingDAO.getInstance().findAll();
            String email = obtenirEmailUtilisateur();
            if (email != null) {
                Page_Tous_Parkings pageTousParkings = new Page_Tous_Parkings(email, tousParkings);
                pageTousParkings.setVisible(true);
                vue.dispose();
            }
        } catch (Exception e) {
            afficherErreur("Erreur lors du chargement des parkings: " + e.getMessage(), TITRE_SYSTEME);
            etat = EtatResultats.AFFICHAGE_RESULTATS;
        }
    }
    
    /**
     * Affiche un message d'erreur
     */
    private void afficherErreur(String message, String titre) {
        JOptionPane.showMessageDialog(vue, message, titre, JOptionPane.ERROR_MESSAGE);
    }
    
    // Getter pour l'état courant (utile pour les tests)
    public EtatResultats getEtatCourant() {
        return etat;
    }
}
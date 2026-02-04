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

/**
 * Contrôleur gérant l'interface des résultats de recherche de parkings.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Resultats_Recherche
 * et le modèle (Parking, Usager, TarifParking).
 * Gère le filtrage, la sélection de parkings et les vérifications d'accès (carte Tisséo).
 * 
 * @author Équipe 7
 */
public class ControleurResultatsRecherche implements ActionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur.
     * Permet de suivre le processus de sélection d'un parking et de gestion des accès.
     */
    public enum EtatResultats {
        /** Affichage des résultats de recherche */
        AFFICHAGE_RESULTATS,
        /** Application des filtres en cours */
        FILTRAGE_EN_COURS,
        /** Un parking a été sélectionné */
        SELECTION_PARKING,
        /** Vérification de l'accès au parking (carte Tisséo) */
        VERIFICATION_ACCES,
        /** Demande de confirmation pour le stationnement */
        DEMANDE_CONFIRMATION,
        /** Ouverture de la page de stationnement */
        STATIONNEMENT_EN_COURS,
        /** Retour à la page d'accueil */
        RETOUR_ACCUEIL,
        /** Affichage de tous les parkings */
        AFFICHAGE_TOUS_PARKINGS
    }
    
    private static final String ACTION_RETOUR = "RETOUR";
    private static final String ACTION_TOUS_PARKINGS = "TOUS_PARKINGS";
    private static final String ACTION_STATIONNER_PREFIX = "STATIONNER_";
    private static final String ACTION_FILTRE_COMBO = "FILTRE_COMBO";
    private static final String ACTION_FILTRE_CHECKBOX = "FILTRE_CHECKBOX";
    
    private static final String TITRE_ERREUR = "Erreur";
    private static final String TITRE_SYSTEME = "Erreur système";
    private static final String TITRE_CARTE_TISSEO = "Carte Tisséo requise";
    private static final String TITRE_ACCES_REFUSE = "Accès refusé - Parking réservé";
    private static final String TITRE_CONFIRMATION = "Confirmation";
    
    private final Page_Resultats_Recherche vue;
    private EtatResultats etat;
    private Parking parkingSelectionne;
    private int indexParkingSelectionne;
    
    /**
     * Constructeur du contrôleur des résultats de recherche.
     * Initialise le contrôleur avec la vue associée.
     * 
     * @param vue la page d'interface graphique des résultats de recherche
     */
    public ControleurResultatsRecherche(Page_Resultats_Recherche vue) {
        this.vue = vue;
        this.etat = EtatResultats.AFFICHAGE_RESULTATS;
        this.parkingSelectionne = null;
        initialiserControleur();
    }
    
    /**
     * Initialise le contrôleur en configurant les écouteurs d'événements.
     */
    private void initialiserControleur() {
        configurerListeners();
    }
    
    /**
     * Configure tous les écouteurs d'événements pour les composants de la vue.
     */
    private void configurerListeners() {
        configurerListenersFiltres();
        configurerListenersBoutons();
    }
    
    /**
     * Configure les écouteurs pour les composants de filtrage (combo box et cases à cocher).
     * Utilise la réflexion pour accéder aux composants privés de la vue.
     */
    private void configurerListenersFiltres() {
        try {
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
     * Configure les écouteurs pour tous les boutons de la vue.
     */
    private void configurerListenersBoutons() {
        configurerListenersRecursifs(vue.getContentPane());
    }
    
    /**
     * Reconfigure les listeners après un filtrage.
     * Nécessaire car de nouveaux boutons sont créés dynamiquement.
     */
    public void configurerListenersApresFiltrage() {
        configurerListenersBoutons();
    }
    
    /**
     * Configure les écouteurs de manière récursive dans un conteneur.
     * Parcourt tous les composants pour trouver les boutons à configurer.
     * 
     * @param container le conteneur à parcourir
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
     * Configure l'écouteur pour un bouton spécifique.
     * Vérifie d'abord si le bouton n'a pas déjà ce contrôleur comme écouteur.
     * 
     * @param button le bouton à configurer
     */
    private void configurerListenerBouton(JButton button) {
        if (!estBoutonDejaConfigure(button)) {
            button.addActionListener(this);
        }
    }
    
    /**
     * Vérifie si un bouton a déjà ce contrôleur comme écouteur.
     * Évite l'ajout de listeners en double.
     * 
     * @param button le bouton à vérifier
     * @return true si le bouton est déjà configuré, false sinon
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
     * Gère les événements d'action en fonction de l'état courant du contrôleur.
     * 
     * @param e l'événement d'action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        
        switch (etat) {
            case AFFICHAGE_RESULTATS:
                traiterActionAffichage(action, e);
                break;
                
            case FILTRAGE_EN_COURS:
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
            case RETOUR_ACCUEIL:
            case AFFICHAGE_TOUS_PARKINGS:
                break;
        }
    }
    
    /**
     * Traite les actions en état d'affichage des résultats.
     * 
     * @param action l'action à traiter
     * @param e l'événement d'action
     */
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
     * Applique les filtres sélectionnés par l'utilisateur.
     * Appelle la méthode de filtrage de la vue puis reconfigure les listeners.
     */
    private void appliquerFiltres() {
        etat = EtatResultats.FILTRAGE_EN_COURS;
        
        try {
            Method method = vue.getClass().getMethod("appliquerFiltres");
            method.invoke(vue);
            
            configurerListenersApresFiltrage();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel de appliquerFiltres: " + e.getMessage());
        } finally {
            etat = EtatResultats.AFFICHAGE_RESULTATS;
        }
    }
    
    /**
     * Gère la sélection d'un parking par l'utilisateur.
     * Extrait l'index du parking et déclenche la vérification d'accessibilité.
     * 
     * @param action l'action contenant l'index du parking
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
     * Extrait l'index du parking de la commande d'action.
     * 
     * @param action la commande d'action
     * @return l'index du parking
     */
    private int extraireIndexParking(String action) {
        return Integer.parseInt(action.replace(ACTION_STATIONNER_PREFIX, ""));
    }
    
    /**
     * Obtient la liste des parkings filtrés depuis la vue.
     * Utilise la réflexion pour accéder au champ privé.
     * 
     * @return la liste des parkings filtrés
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
     * Vérifie si un index est valide pour la liste de parkings.
     * 
     * @param index l'index à vérifier
     * @param parkingsFiltres la liste des parkings
     * @return true si l'index est valide, false sinon
     */
    private boolean estIndexValide(int index, List<Parking> parkingsFiltres) {
        return index >= 0 && index < parkingsFiltres.size();
    }
    
    /**
     * Vérifie l'accessibilité du parking sélectionné.
     * Pour les parkings relais, vérifie que l'utilisateur possède une carte Tisséo.
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
     * Vérifie la présence d'une carte Tisséo pour l'utilisateur.
     * Si absente, affiche un message d'accès refusé.
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
     * Obtient le numéro de carte Tisséo de l'utilisateur depuis la base de données.
     * 
     * @return le numéro de carte Tisséo ou null si absent
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
     * Obtient l'email de l'utilisateur depuis la vue.
     * Utilise la réflexion pour appeler la méthode.
     * 
     * @return l'email de l'utilisateur
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
     * Affiche un message d'accès refusé pour un parking relais sans carte Tisséo.
     * Propose à l'utilisateur d'ajouter une carte Tisséo.
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
     * Ouvre la page utilisateur pour permettre l'ajout d'une carte Tisséo.
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
     * Demande confirmation à l'utilisateur pour le stationnement.
     * Affiche les informations du parking et de la carte Tisséo si applicable.
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
     * Construit le message de confirmation pour le stationnement.
     * Inclut les informations sur le parking et la carte Tisséo si c'est un parking relais.
     * 
     * @param parking le parking sélectionné
     * @return le message de confirmation
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
        }

        return message.toString();
    }
    
    /**
     * Masque une partie du numéro de carte pour des raisons de sécurité.
     * Affiche seulement les 4 premiers caractères.
     * 
     * @param numeroCarte le numéro de carte complet
     * @return le numéro de carte masqué
     */
    private String masquerNumeroCarte(String numeroCarte) {
        if (numeroCarte != null && numeroCarte.length() >= 4) {
            return numeroCarte.substring(0, 4) + "******";
        }
        return numeroCarte != null ? numeroCarte : "Non disponible";
    }
    
    /**
     * Prépare le stationnement après confirmation.
     * Vérifie à nouveau la carte Tisséo pour les parkings relais avant d'ouvrir la page.
     */
    private void preparerStationnement() {
        etat = EtatResultats.STATIONNEMENT_EN_COURS;
        
        try {
            if (TarifParkingDAO.getInstance().estParkingRelais(parkingSelectionne.getIdParking())) {
                verifierCarteTisseoPourStationnement();
            }
            
            ouvrirPageStationnement();
        } catch (IllegalStateException e) {
            etat = EtatResultats.AFFICHAGE_RESULTATS;
        } catch (Exception e) {
            afficherErreur("Erreur lors du traitement: " + e.getMessage(), TITRE_SYSTEME);
            etat = EtatResultats.AFFICHAGE_RESULTATS;
        }
    }
    
    /**
     * Vérifie la carte Tisséo une dernière fois avant le stationnement.
     * Lance une exception si la carte est absente.
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
     * Ouvre la page de stationnement en parking avec le parking sélectionné.
     * Ferme la page des résultats de recherche.
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
     * Retourne à la page d'accueil de l'application.
     * Ferme la page des résultats de recherche.
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
     * Affiche la page de tous les parkings disponibles.
     * Ferme la page des résultats de recherche.
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
     * Affiche un message d'erreur dans une boîte de dialogue.
     * 
     * @param message le message d'erreur
     * @param titre le titre de la boîte de dialogue
     */
    private void afficherErreur(String message, String titre) {
        JOptionPane.showMessageDialog(vue, message, titre, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Retourne l'état courant du contrôleur.
     * 
     * @return l'état courant
     */
    public EtatResultats getEtatCourant() {
        return etat;
    }
}
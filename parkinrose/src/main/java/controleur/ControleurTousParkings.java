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

/**
 * Contrôleur gérant l'interface d'affichage de tous les parkings.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Tous_Parkings
 * et le modèle (Parking, Usager, TarifParking).
 * Gère le filtrage, la sélection de parkings et les vérifications d'accès (carte Tisséo).
 * 
 * @author Équipe 7
 */
public class ControleurTousParkings implements ActionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur.
     * Permet de suivre le processus de sélection d'un parking et de gestion des accès.
     */
    private enum EtatControleur {
        /** État initial, affichage de la liste des parkings */
        INITIAL,
        /** Application des filtres en cours */
        FILTRAGE_EN_COURS,
        /** Un parking a été sélectionné */
        SELECTION_PARKING,
        /** Vérification de la carte Tisséo en cours */
        VERIFICATION_CARTE,
        /** Demande de confirmation pour le stationnement */
        CONFIRMATION_STATIONNEMENT,
        /** Ouverture de la page de stationnement */
        OUVERTURE_STATIONNEMENT,
        /** Ouverture de la page profil utilisateur */
        OUVERTURE_PROFIL,
        /** Retour à la page d'accueil */
        RETOUR_ACCUEIL
    }
    
    private static final String TITRE_ERREUR = "Erreur";
    
    private Page_Tous_Parkings vue;
    private EtatControleur etat;
    private Parking parkingSelectionne;
    private String carteTisseoUtilisateur;
    private int indexParkingSelectionne;
    
    /**
     * Constructeur du contrôleur de tous les parkings.
     * Initialise le contrôleur avec la vue associée et charge la carte Tisséo de l'utilisateur.
     * 
     * @param vue la page d'interface graphique de tous les parkings
     */
    public ControleurTousParkings(Page_Tous_Parkings vue) {
        this.vue = vue;
        this.etat = EtatControleur.INITIAL;
        this.parkingSelectionne = null;
        this.carteTisseoUtilisateur = null;
        this.indexParkingSelectionne = -1;
        
        initialiserControleur();
        chargerCarteTisseoUtilisateur();
    }
    
    /**
     * Initialise le contrôleur en configurant les écouteurs d'événements.
     */
    private void initialiserControleur() {
        configurerListeners();
    }
    
    /**
     * Charge le numéro de carte Tisséo de l'utilisateur depuis la base de données.
     * Stocke la carte en mémoire pour les vérifications ultérieures.
     */
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
    
    /**
     * Configure tous les écouteurs d'événements pour les composants de la vue.
     */
    private void configurerListeners() {
        configurerListenersFiltres();
        configurerListenersRecursifs(vue.getContentPane());
    }
    
    /**
     * Configure les écouteurs pour les composants de filtrage.
     */
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
    
    /**
     * Configure les écouteurs de manière récursive dans un conteneur.
     * Parcourt tous les composants pour trouver les boutons à configurer.
     * 
     * @param container le conteneur à parcourir
     */
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
    
    /**
     * Gère les événements d'action en fonction de l'état courant du contrôleur.
     * 
     * @param e l'événement d'action
     */
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
    
    /**
     * Détermine l'action à partir de la source de l'événement.
     * Identifie le type de composant et son texte pour déterminer l'action.
     * 
     * @param e l'événement d'action
     * @return une chaîne identifiant l'action
     */
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
    
    /**
     * Extrait l'index du parking du texte du bouton.
     * 
     * @param texteBouton le texte du bouton
     * @return l'index du parking ou -1 en cas d'erreur
     */
    private int extraireIndexParking(String texteBouton) {
        try {
            String[] parties = texteBouton.split(" ");
            String dernierElement = parties[parties.length - 1];
            return Integer.parseInt(dernierElement) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Traite les actions en état INITIAL.
     * 
     * @param action l'action à traiter
     */
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
    
    /**
     * Traite les actions en état FILTRAGE_EN_COURS.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatFiltrageEnCours(String action) {
        if (action.equals("ANNULER_FILTRAGE")) {
            etat = EtatControleur.INITIAL;
        }
    }
    
    /**
     * Traite les actions en état SELECTION_PARKING.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatSelectionParking(String action) {
        if (action.equals("ANNULER_SELECTION")) {
            etat = EtatControleur.INITIAL;
            parkingSelectionne = null;
        } else if (action.equals("VERIFIER_TYPE_PARKING")) {
            verifierTypeParking();
        }
    }
    
    /**
     * Traite les actions en état VERIFICATION_CARTE.
     * 
     * @param action l'action à traiter
     */
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
    
    /**
     * Traite les actions en état CONFIRMATION_STATIONNEMENT.
     * 
     * @param action l'action à traiter
     */
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
    
    /**
     * Traite les actions en état OUVERTURE_STATIONNEMENT.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatOuvertureStationnement(String action) {
        if (action.equals("ERREUR_OUVERTURE")) {
            etat = EtatControleur.INITIAL;
            parkingSelectionne = null;
        }
    }
    
    /**
     * Traite les actions en état OUVERTURE_PROFIL.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatOuvertureProfil(String action) {
        if (action.equals("FERMER_PROFIL")) {
            etat = EtatControleur.INITIAL;
        }
    }
    
    /**
     * Traite les actions en état RETOUR_ACCUEIL.
     * 
     * @param action l'action à traiter
     */
    private void traiterEtatRetourAccueil(String action) {
        if (action.equals("ANNULER_RETOUR")) {
            etat = EtatControleur.INITIAL;
        }
    }
    
    /**
     * Applique les filtres sélectionnés par l'utilisateur.
     * Appelle la méthode de filtrage de la vue.
     */
    private void appliquerFiltres() {
        vue.appliquerFiltres();
    }
    
    /**
     * Sélectionne un parking à partir de son index dans la liste filtrée.
     * Vérifie la validité de l'index avant de continuer.
     * 
     * @param index l'index du parking dans la liste filtrée
     */
    private void selectionnerParking(int index) {
        List<Parking> parkingsFiltres = vue.getParkingsFiltres();
        
        if (estIndexValide(index, parkingsFiltres)) {
            parkingSelectionne = parkingsFiltres.get(index);
            indexParkingSelectionne = index;
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "VERIFIER_TYPE_PARKING"));
        } else {
            afficherErreur("Parking non trouvé");
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ANNULER_SELECTION"));
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
     * Vérifie le type du parking sélectionné.
     * Pour les parkings relais, vérifie la carte Tisséo.
     * Pour les autres, demande directement confirmation.
     */
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
            afficherErreur("Erreur lors de la vérification du parking: " + ex.getMessage());
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ANNULER_SELECTION"));
        }
    }
    
    /**
     * Vérifie la présence et la validité de la carte Tisséo.
     * Déclenche l'événement approprié selon le résultat.
     */
    private void verifierCarteTisseo() {
        if (carteTisseoUtilisateur == null || carteTisseoUtilisateur.trim().isEmpty()) {
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CARTE_INVALIDE"));
        } else {
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CARTE_VALIDE"));
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
    
    /**
     * Demande confirmation pour un stationnement dans un parking relais.
     * Affiche les informations du parking et la carte Tisséo détectée.
     * 
     * @param numeroCarteMasque le numéro de carte Tisséo masqué
     */
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
    
    /**
     * Construit le message de confirmation pour un parking relais.
     * 
     * @param numeroCarteMasque le numéro de carte Tisséo masqué
     * @return le message de confirmation
     */
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
    
    /**
     * Demande confirmation pour un stationnement dans un parking normal.
     * Affiche les informations du parking et les tarifs.
     */
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
    
    /**
     * Construit le message de confirmation pour un parking normal.
     * Inclut les informations sur les places disponibles et les tarifs.
     * 
     * @return le message de confirmation
     */
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
        }

        return message.toString();
    }
    
    /**
     * Ouvre la page de stationnement en parking avec le parking sélectionné.
     * Ferme la page actuelle.
     */
    private void ouvrirPageStationnement() {
        try {
            Page_Garer_Parking pageParking = new Page_Garer_Parking(
                vue.getEmailUtilisateur(), 
                parkingSelectionne
            );
            pageParking.setVisible(true);
            vue.dispose();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture de la page de stationnement: " + e.getMessage());
            actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ERREUR_OUVERTURE"));
        }
    }
    
    /**
     * Ouvre la page utilisateur pour permettre l'ajout d'une carte Tisséo.
     */
    private void ouvrirPageUtilisateur() {
        try {
            Page_Utilisateur pageUtilisateur = new Page_Utilisateur(vue.getEmailUtilisateur());
            pageUtilisateur.setVisible(true);
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture de la page utilisateur: " + e.getMessage());
        }
    }
    
    /**
     * Retourne à la page d'accueil de l'application.
     * Ferme la page actuelle.
     */
    private void retourAccueil() {
        try {
            Page_Principale pagePrincipale = new Page_Principale(vue.getEmailUtilisateur());
            pagePrincipale.setVisible(true);
            vue.dispose();
        } catch (Exception e) {
            afficherErreur("Erreur lors du retour à l'accueil: " + e.getMessage());
        }
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
     * Affiche un message d'erreur dans une boîte de dialogue.
     * 
     * @param message le message d'erreur
     */
    private void afficherErreur(String message) {
        JOptionPane.showMessageDialog(vue, message, TITRE_ERREUR, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Retourne l'état actuel du contrôleur.
     * 
     * @return l'état actuel
     */
    public EtatControleur getEtat() {
        return etat;
    }
    
    /**
     * Retourne le parking actuellement sélectionné.
     * 
     * @return le parking sélectionné
     */
    public Parking getParkingSelectionne() {
        return parkingSelectionne;
    }
    
    /**
     * Retourne le numéro de carte Tisséo de l'utilisateur.
     * 
     * @return le numéro de carte Tisséo
     */
    public String getCarteTisseoUtilisateur() {
        return carteTisseoUtilisateur;
    }
}
package controleur;

import ihm.CarteAdminOSMPanel;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import modele.Parking;
import modele.dao.ParkingDAO;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur gérant la carte interactive d'administration des parkings.
 * Permet aux administrateurs d'ajouter, modifier, supprimer et visualiser les parkings
 * directement sur une carte OpenStreetMap.
 * Gère les interactions entre la vue CarteAdminOSMPanel et le modèle (Parking, ParkingDAO).
 * 
 * @author Équipe 7
 */
public class ControleurCarteAdmin implements ActionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur d'administration de la carte.
     * Permet de suivre le mode d'interaction avec la carte et les opérations en cours.
     */
    private enum EtatAdmin {
        /** État initial au démarrage du contrôleur */
        INITIAL,
        /** Mode visualisation simple de la carte et des parkings */
        VISUALISATION,
        /** Mode d'ajout activé, en attente de sélection d'un emplacement */
        MODE_AJOUT,
        /** Formulaire de création ou modification ouvert */
        FORMULAIRE_OUVERT,
        /** Modification d'un parking existant en cours */
        MODIFICATION,
        /** Une erreur s'est produite */
        ERREUR
    }
    
    private CarteAdminOSMPanel vue;
    private EtatAdmin etat;
    private String emailAdmin;
    private ParkingDAO parkingDAO;
    private Map<String, Parking> parkingsMap;
    private Double latitudeSelectionnee;
    private Double longitudeSelectionnee;
    
    private static final double TARIF_SOIREE = 5.90;
    
    /**
     * Constructeur du contrôleur de la carte d'administration.
     * Initialise le contrôleur avec la vue associée et charge les parkings existants.
     * 
     * @param vue le panel de la carte d'administration
     * @param emailAdmin l'email de l'administrateur connecté
     */
    public ControleurCarteAdmin(CarteAdminOSMPanel vue, String emailAdmin) {
        this.vue = vue;
        this.emailAdmin = emailAdmin;
        this.parkingDAO = ParkingDAO.getInstance();
        this.parkingsMap = new HashMap<>();
        
        initialiserControleur();
    }
    
    /**
     * Initialise le contrôleur en configurant la vue et en passant à l'état de visualisation.
     * En cas d'erreur, affiche un message et gère l'erreur d'initialisation.
     */
    private void initialiserControleur() {
        try {
            this.etat = EtatAdmin.INITIAL;
            initialiserVue();
            
            etat = EtatAdmin.VISUALISATION;
            
        } catch (Exception e) {
            gererErreurInitialisation("Erreur d'initialisation", e.getMessage());
        }
    }
    
    /**
     * Initialise la vue en configurant les écouteurs d'événements.
     */
    private void initialiserVue() {
        configurerListeners();
    }
    
    /**
     * Configure les écouteurs d'événements pour les composants de la vue.
     * Note : les listeners sont principalement configurés dans la vue elle-même.
     */
    private void configurerListeners() {
        // Les listeners sont déjà configurés dans la vue
    }
    
    /**
     * Gère les événements d'action des composants de la vue.
     * Traite principalement l'activation du mode ajout de parking.
     * 
     * @param e l'événement d'action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (etat == EtatAdmin.ERREUR) {
            return;
        }
        
        Object source = e.getSource();
        String actionCommand = e.getActionCommand();
        
        try {
            if ("Ajouter un parking".equals(actionCommand)) {
                activerModeAjout();
            }
            
        } catch (Exception ex) {
            gererErreur("Erreur action", ex.getMessage());
        }
    }
    
    /**
     * Active le mode d'ajout de parking sur la carte.
     * Permet à l'administrateur de cliquer sur la carte pour sélectionner l'emplacement
     * d'un nouveau parking. Affiche des instructions à l'utilisateur.
     */
    public void activerModeAjout() {
        if (vue.getWebEngine() == null) {
            vue.afficherMessageErreur("Carte non chargée", 
                "La carte n'est pas encore chargée. Veuillez patienter.");
            return;
        }
        
        try {
            vue.getWebEngine().executeScript("if (window.activateAddMode) window.activateAddMode();");
            
            etat = EtatAdmin.MODE_AJOUT;
            
            JOptionPane.showMessageDialog(vue,
                "Mode ajout activé !\n\n" +
                "Instructions :\n" +
                "1. Cliquez sur la carte à l'emplacement souhaité\n" +
                "2. Un marqueur vert apparaîtra\n" +
                "3. Confirmez l'emplacement dans le popup",
                "Mode Ajout",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            gererErreur("Erreur activation mode ajout", e.getMessage());
        }
    }
    
    /**
     * Désactive le mode d'ajout de parking et retourne au mode de visualisation.
     * Supprime le marqueur temporaire de la carte si présent.
     */
    public void desactiverModeAjout() {
        try {
            if (vue.getWebEngine() != null) {
                vue.getWebEngine().executeScript("if (window.deactivateAddMode) window.deactivateAddMode();");
            }
            etat = EtatAdmin.VISUALISATION;
        } catch (Exception e) {
            // Ignorer les erreurs silencieusement
        }
    }
    
    /**
     * Enregistre les coordonnées sélectionnées sur la carte et met à jour l'affichage.
     * Si le mode ajout est actif, ouvre automatiquement le formulaire de création de parking.
     * 
     * @param latitude la latitude du point sélectionné
     * @param longitude la longitude du point sélectionné
     */
    public void setCoordonneesSelectionnees(Double latitude, Double longitude) {
        this.latitudeSelectionnee = latitude;
        this.longitudeSelectionnee = longitude;
        vue.updateCoordsLabel(latitude, longitude);
        
        if (etat == EtatAdmin.MODE_AJOUT) {
            ouvrirFormulaireParking();
        }
    }
    
    /**
     * Ouvre le formulaire de création d'un nouveau parking.
     * Nécessite que des coordonnées aient été préalablement sélectionnées sur la carte.
     * Désactive le mode ajout avant l'ouverture du formulaire.
     */
    private void ouvrirFormulaireParking() {
        if (latitudeSelectionnee == null || longitudeSelectionnee == null) {
            vue.afficherMessageErreur("Emplacement requis",
                "Veuillez d'abord sélectionner un emplacement sur la carte.");
            return;
        }
        
        etat = EtatAdmin.FORMULAIRE_OUVERT;
        
        try {
            desactiverModeAjout();
            vue.ouvrirFormulaireParking();
            
        } catch (Exception e) {
            gererErreur("Erreur ouverture formulaire", e.getMessage());
        }
    }
    
    /**
     * Ouvre le formulaire de modification d'un parking existant.
     * Affiche un message d'erreur si le parking n'est pas trouvé dans la map locale.
     * 
     * @param idParking l'identifiant du parking à modifier
     */
    public void ouvrirFormulaireModificationParking(String idParking) {
        Parking parking = parkingsMap.get(idParking);
        if (parking == null) {
            vue.afficherMessageErreur("Parking non trouvé", 
                "Le parking avec l'ID " + idParking + " n'a pas été trouvé.");
            return;
        }
        
        etat = EtatAdmin.MODIFICATION;
        
        try {
            vue.ouvrirFormulaireModificationParking(idParking);
            
        } catch (Exception e) {
            gererErreur("Erreur ouverture modification", e.getMessage());
        }
    }
    
    /**
     * Ajoute un nouveau parking à la base de données et à la carte.
     * Valide les données du parking avant l'insertion, met à jour la map locale
     * et recharge la carte pour afficher le nouveau parking.
     * 
     * @param parking le parking à ajouter
     */
    public void ajouterParking(Parking parking) {
        try {
            if (!validerParking(parking)) {
                return;
            }
            
            boolean succes = parkingDAO.creerParking(parking);
            
            if (succes) {
                parkingsMap.put(parking.getIdParking(), parking);
                
                vue.afficherMessageSucces("Parking ajouté",
                    "Le parking " + parking.getLibelleParking() + " a été ajouté avec succès !");
                
                rechargerCarte();
                
                latitudeSelectionnee = null;
                longitudeSelectionnee = null;
                
                etat = EtatAdmin.VISUALISATION;
                
            } else {
                vue.afficherMessageErreur("Erreur ajout",
                    "Une erreur est survenue lors de l'ajout du parking.");
            }
            
        } catch (Exception e) {
            gererErreur("Erreur ajout parking", e.getMessage());
        }
    }
    
    /**
     * Modifie un parking existant dans la base de données.
     * Valide les nouvelles données, met à jour la base de données et la map locale,
     * puis recharge la carte pour afficher les modifications.
     * 
     * @param parking le parking avec les données modifiées
     */
    public void modifierParking(Parking parking) {
        try {
            if (!validerParking(parking)) {
                return;
            }
            
            boolean succes = parkingDAO.mettreAJourParking(parking);
            
            if (succes) {
                parkingsMap.put(parking.getIdParking(), parking);
                
                vue.afficherMessageSucces("Parking modifié",
                    "Le parking " + parking.getLibelleParking() + " a été modifié avec succès !");
                
                rechargerCarte();
                
                etat = EtatAdmin.VISUALISATION;
                
            } else {
                vue.afficherMessageErreur("Erreur modification",
                    "Une erreur est survenue lors de la modification du parking.");
            }
            
        } catch (Exception e) {
            gererErreur("Erreur modification parking", e.getMessage());
        }
    }
    
    /**
     * Supprime un parking de la base de données et de la carte.
     * Demande une confirmation à l'administrateur avant la suppression définitive.
     * Met à jour la map locale et recharge la carte après suppression.
     * 
     * @param idParking l'identifiant du parking à supprimer
     */
    public void supprimerParking(String idParking) {
        Parking parking = parkingsMap.get(idParking);
        if (parking == null) {
            vue.afficherMessageErreur("Parking non trouvé",
                "Le parking avec l'ID " + idParking + " n'a pas été trouvé.");
            return;
        }
        
        int confirmation = JOptionPane.showConfirmDialog(vue,
            "Êtes-vous sûr de vouloir supprimer le parking :\n" +
            parking.getLibelleParking() + " ?\n\n" +
            "Cette action est irréversible !",
            "Confirmation de suppression",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            boolean succes = parkingDAO.supprimerParking(parking);
            
            if (succes) {
                parkingsMap.remove(idParking);
                
                vue.afficherMessageSucces("Parking supprimé",
                    "Le parking " + parking.getLibelleParking() + " a été supprimé avec succès !");
                
                rechargerCarte();
                
            } else {
                vue.afficherMessageErreur("Erreur suppression",
                    "Une erreur est survenue lors de la suppression du parking.");
            }
            
        } catch (Exception e) {
            gererErreur("Erreur suppression parking", e.getMessage());
        }
    }
    
    /**
     * Valide les données d'un parking avant insertion ou modification.
     * Vérifie que tous les champs obligatoires sont remplis et que les valeurs
     * sont cohérentes (places disponibles, tarifs, coordonnées GPS, etc.).
     * 
     * @param parking le parking à valider
     * @return true si le parking est valide, false sinon (avec affichage d'un message d'erreur)
     */
    private boolean validerParking(Parking parking) {
        if (parking.getLibelleParking() == null || parking.getLibelleParking().trim().isEmpty()) {
            vue.afficherMessageErreur("Nom manquant", "Le nom du parking est obligatoire.");
            return false;
        }
        
        if (parking.getAdresseParking() == null || parking.getAdresseParking().trim().isEmpty()) {
            vue.afficherMessageErreur("Adresse manquante", "L'adresse du parking est obligatoire.");
            return false;
        }
        
        if (parking.getIdParking() == null || parking.getIdParking().trim().isEmpty()) {
            vue.afficherMessageErreur("ID manquant", "L'ID du parking est obligatoire.");
            return false;
        }
        
        if (parking.getNombrePlaces() <= 0) {
            vue.afficherMessageErreur("Places invalides", "Le nombre de places doit être positif.");
            return false;
        }
        
        if (parking.getPlacesDisponibles() < 0 || 
            parking.getPlacesDisponibles() > parking.getNombrePlaces()) {
            vue.afficherMessageErreur("Places disponibles invalides",
                "Le nombre de places disponibles doit être entre 0 et " + parking.getNombrePlaces() + ".");
            return false;
        }
        
        if (parking.hasMoto()) {
            if (parking.getPlacesMoto() < 0) {
                vue.afficherMessageErreur("Places moto invalides",
                    "Le nombre de places moto doit être positif ou nul.");
                return false;
            }
            
            if (parking.getPlacesMotoDisponibles() < 0 || 
                parking.getPlacesMotoDisponibles() > parking.getPlacesMoto()) {
                vue.afficherMessageErreur("Places moto disponibles invalides",
                    "Le nombre de places moto disponibles doit être entre 0 et " + 
                    parking.getPlacesMoto() + ".");
                return false;
            }
        }
        
        if (parking.getHauteurParking() < 0) {
            vue.afficherMessageErreur("Hauteur invalide",
                "La hauteur doit être positive ou nulle (0 pour non limitée).");
            return false;
        }
        
        if (!parking.isEstRelais() && parking.getTarifHoraire() < 0) {
            vue.afficherMessageErreur("Tarif invalide",
                "Le tarif horaire doit être positif ou nul.");
            return false;
        }
        
        if (parking.isEstRelais() && parking.getTarifHoraire() != 0) {
            vue.afficherMessageErreur("Tarif relais invalide",
                "Un parking relais Tisséo doit être gratuit (tarif = 0).");
            return false;
        }
        
        if (parking.getPositionX() == null || parking.getPositionY() == null ||
            parking.getPositionX() == 0.0f || parking.getPositionY() == 0.0f) {
            vue.afficherMessageErreur("Coordonnées manquantes",
                "Les coordonnées GPS du parking sont obligatoires.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Recharge complètement la carte et réinitialise l'état du contrôleur.
     * Désactive le mode ajout si actif et efface les coordonnées sélectionnées.
     */
    public void rechargerCarte() {
        try {
            vue.recharger();
            
            desactiverModeAjout();
            
            latitudeSelectionnee = null;
            longitudeSelectionnee = null;
            
            etat = EtatAdmin.VISUALISATION;
            
        } catch (Exception e) {
            gererErreur("Erreur rechargement carte", e.getMessage());
        }
    }
    
    /**
     * Charge tous les parkings depuis la base de données et les stocke dans la map locale.
     * Permet de maintenir une copie locale des parkings pour un accès rapide.
     * 
     * @throws SQLException si une erreur survient lors de l'accès à la base de données
     */
    public void chargerParkings() {
        try {
            java.util.List<Parking> parkings = parkingDAO.findAll();
            parkingsMap.clear();
            
            for (Parking parking : parkings) {
                parkingsMap.put(parking.getIdParking(), parking);
            }
            
        } catch (SQLException e) {
            gererErreur("Erreur chargement parkings", e.getMessage());
        }
    }
    
    /**
     * Gère une erreur survenue pendant l'utilisation du contrôleur.
     * Affiche un message d'erreur à l'utilisateur et passe à l'état ERREUR.
     * 
     * @param titre le titre du message d'erreur
     * @param message la description détaillée de l'erreur
     */
    private void gererErreur(String titre, String message) {
        System.err.println(titre + ": " + message);
        vue.afficherMessageErreur(titre, message);
        etat = EtatAdmin.ERREUR;
    }
    
    /**
     * Gère une erreur critique survenue lors de l'initialisation.
     * Affiche un message d'erreur et ferme l'application.
     * 
     * @param titre le titre du message d'erreur
     * @param message la description détaillée de l'erreur
     */
    private void gererErreurInitialisation(String titre, String message) {
        System.err.println(titre + ": " + message);
        JOptionPane.showMessageDialog(vue,
            message + "\n\nL'application va se fermer.",
            titre,
            JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
    
    /**
     * Retourne l'état actuel du contrôleur.
     * 
     * @return l'état actuel
     */
    public EtatAdmin getEtat() {
        return etat;
    }
    
    /**
     * Retourne l'email de l'administrateur connecté.
     * 
     * @return l'email de l'administrateur
     */
    public String getEmailAdmin() {
        return emailAdmin;
    }
    
    /**
     * Retourne la map des parkings chargés localement.
     * 
     * @return la map associant les IDs de parking aux objets Parking
     */
    public Map<String, Parking> getParkingsMap() {
        return parkingsMap;
    }
    
    /**
     * Retourne la latitude du point actuellement sélectionné sur la carte.
     * 
     * @return la latitude sélectionnée, ou null si aucun point n'est sélectionné
     */
    public Double getLatitudeSelectionnee() {
        return latitudeSelectionnee;
    }
    
    /**
     * Retourne la longitude du point actuellement sélectionné sur la carte.
     * 
     * @return la longitude sélectionnée, ou null si aucun point n'est sélectionné
     */
    public Double getLongitudeSelectionnee() {
        return longitudeSelectionnee;
    }
    
    /**
     * Retourne le tarif soirée par défaut pour les parkings.
     * 
     * @return le tarif soirée (5.90€)
     */
    public static double getTarifSoiree() {
        return TARIF_SOIREE;
    }
    
    /**
     * Génère un identifiant unique pour un nouveau parking basé sur son nom.
     * Transforme le nom en majuscules, remplace les espaces par des underscores,
     * ajoute le préfixe "PARK_" et vérifie l'unicité dans la base de données.
     * En cas de conflit, ajoute un suffixe numérique incrémental.
     * 
     * @param nom le nom du parking servant de base pour l'ID
     * @return un identifiant unique de parking
     */
    public String genererIdParking(String nom) {
        try {
            String baseId = nom.toUpperCase()
                .replaceAll(" ", "_")
                .replaceAll("[^A-Z0-9_]", "")
                .replaceAll("__+", "_");
            
            if (!baseId.startsWith("PARK_")) {
                baseId = "PARK_" + baseId;
            }
            
            String finalId = baseId;
            int counter = 1;
            
            while (parkingDAO.findById(finalId) != null) {
                finalId = baseId + "_" + counter;
                counter++;
                
                if (counter > 100) {
                    return "PARK_" + System.currentTimeMillis();
                }
            }
            
            return finalId;
        } catch (SQLException e) {
            System.err.println("Erreur génération ID unique: " + e.getMessage());
            return "PARK_" + System.currentTimeMillis();
        }
    }
}
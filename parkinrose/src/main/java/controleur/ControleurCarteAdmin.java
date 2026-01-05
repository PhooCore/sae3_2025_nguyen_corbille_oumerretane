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

public class ControleurCarteAdmin implements ActionListener {
    
    private enum EtatAdmin {
        INITIAL,
        VISUALISATION,
        MODE_AJOUT,
        FORMULAIRE_OUVERT,
        MODIFICATION,
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
    
    public ControleurCarteAdmin(CarteAdminOSMPanel vue, String emailAdmin) {
        this.vue = vue;
        this.emailAdmin = emailAdmin;
        this.parkingDAO = ParkingDAO.getInstance();
        this.parkingsMap = new HashMap<>();
        
        initialiserControleur();
    }
    
    private void initialiserControleur() {
        try {
            this.etat = EtatAdmin.INITIAL;
            initialiserVue();
            
            etat = EtatAdmin.VISUALISATION;
            
        } catch (Exception e) {
            gererErreurInitialisation("Erreur d'initialisation", e.getMessage());
        }
    }
    
    private void initialiserVue() {
        configurerListeners();
    }
    
    private void configurerListeners() {
        // Les listeners sont déjà configurés dans la vue
    }
    
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
    
    // ===================== MÉTHODES GÉNÉRALES =====================
    
    public void activerModeAjout() {
        if (vue.getWebEngine() == null) {
            vue.afficherMessageErreur("Carte non chargée", 
                "La carte n'est pas encore chargée. Veuillez patienter.");
            return;
        }
        
        try {
            // Activer le mode ajout dans la carte JavaScript
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
    
    // ===================== GESTION DES COORDONNÉES =====================
    
    public void setCoordonneesSelectionnees(Double latitude, Double longitude) {
        this.latitudeSelectionnee = latitude;
        this.longitudeSelectionnee = longitude;
        vue.updateCoordsLabel(latitude, longitude);
        
        if (etat == EtatAdmin.MODE_AJOUT) {
            ouvrirFormulaireParking();
        }
    }
    
    // ===================== GESTION DES FORMULAIRES =====================
    
    private void ouvrirFormulaireParking() {
        if (latitudeSelectionnee == null || longitudeSelectionnee == null) {
            vue.afficherMessageErreur("Emplacement requis",
                "Veuillez d'abord sélectionner un emplacement sur la carte.");
            return;
        }
        
        etat = EtatAdmin.FORMULAIRE_OUVERT;
        
        try {
            // Désactiver le mode ajout
            desactiverModeAjout();
            
            // Ouvrir le formulaire d'ajout de parking
            vue.ouvrirFormulaireParking();
            
        } catch (Exception e) {
            gererErreur("Erreur ouverture formulaire", e.getMessage());
        }
    }
    
    public void ouvrirFormulaireModificationParking(String idParking) {
        Parking parking = parkingsMap.get(idParking);
        if (parking == null) {
            vue.afficherMessageErreur("Parking non trouvé", 
                "Le parking avec l'ID " + idParking + " n'a pas été trouvé.");
            return;
        }
        
        etat = EtatAdmin.MODIFICATION;
        
        try {
            // Ouvrir le formulaire de modification
            vue.ouvrirFormulaireModificationParking(idParking);
            
        } catch (Exception e) {
            gererErreur("Erreur ouverture modification", e.getMessage());
        }
    }
    
    // ===================== GESTION DES PARKINGS =====================
    
    public void ajouterParking(Parking parking) {
        try {
            // Validation du parking
            if (!validerParking(parking)) {
                return;
            }
            
            // Ajouter le parking à la base de données
            boolean succes = parkingDAO.creerParking(parking);
            
            if (succes) {
                // Ajouter le parking à la map locale
                parkingsMap.put(parking.getIdParking(), parking);
                
                // Afficher un message de confirmation
                vue.afficherMessageSucces("Parking ajouté",
                    "Le parking " + parking.getLibelleParking() + " a été ajouté avec succès !");
                
                // Recharger la carte
                rechargerCarte();
                
                // Réinitialiser les coordonnées
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
    
    public void modifierParking(Parking parking) {
        try {
            // Validation du parking
            if (!validerParking(parking)) {
                return;
            }
            
            // Mettre à jour le parking dans la base de données
            boolean succes = parkingDAO.mettreAJourParking(parking);
            
            if (succes) {
                // Mettre à jour la map locale
                parkingsMap.put(parking.getIdParking(), parking);
                
                // Afficher un message de confirmation
                vue.afficherMessageSucces("Parking modifié",
                    "Le parking " + parking.getLibelleParking() + " a été modifié avec succès !");
                
                // Recharger la carte
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
    
    public void supprimerParking(String idParking) {
        Parking parking = parkingsMap.get(idParking);
        if (parking == null) {
            vue.afficherMessageErreur("Parking non trouvé",
                "Le parking avec l'ID " + idParking + " n'a pas été trouvé.");
            return;
        }
        
        // Demander confirmation
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
            // Supprimer le parking de la base de données
            boolean succes = parkingDAO.supprimerParking(parking);
            
            if (succes) {
                // Retirer le parking de la map locale
                parkingsMap.remove(idParking);
                
                // Afficher un message de confirmation
                vue.afficherMessageSucces("Parking supprimé",
                    "Le parking " + parking.getLibelleParking() + " a été supprimé avec succès !");
                
                // Recharger la carte
                rechargerCarte();
                
            } else {
                vue.afficherMessageErreur("Erreur suppression",
                    "Une erreur est survenue lors de la suppression du parking.");
            }
            
        } catch (Exception e) {
            gererErreur("Erreur suppression parking", e.getMessage());
        }
    }
    
    private boolean validerParking(Parking parking) {
        // Validation des champs obligatoires
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
        
        // Validation des nombres
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
        
        // Validation de la hauteur
        if (parking.getHauteurParking() < 0) {
            vue.afficherMessageErreur("Hauteur invalide",
                "La hauteur doit être positive ou nulle (0 pour non limitée).");
            return false;
        }
        
        // Validation du tarif
        if (!parking.isEstRelais() && parking.getTarifHoraire() < 0) {
            vue.afficherMessageErreur("Tarif invalide",
                "Le tarif horaire doit être positif ou nul.");
            return false;
        }
        
        // Si c'est un relais Tisséo, le tarif doit être 0
        if (parking.isEstRelais() && parking.getTarifHoraire() != 0) {
            vue.afficherMessageErreur("Tarif relais invalide",
                "Un parking relais Tisséo doit être gratuit (tarif = 0).");
            return false;
        }
        
        // Validation des coordonnées
        if (parking.getPositionX() == null || parking.getPositionY() == null ||
            parking.getPositionX() == 0.0f || parking.getPositionY() == 0.0f) {
            vue.afficherMessageErreur("Coordonnées manquantes",
                "Les coordonnées GPS du parking sont obligatoires.");
            return false;
        }
        
        return true;
    }
    
    // ===================== GESTION DE LA CARTE =====================
    
    public void rechargerCarte() {
        try {
            vue.recharger();
            
            // Désactiver le mode ajout si actif
            desactiverModeAjout();
            
            // Réinitialiser les coordonnées
            latitudeSelectionnee = null;
            longitudeSelectionnee = null;
            
            etat = EtatAdmin.VISUALISATION;
            
        } catch (Exception e) {
            gererErreur("Erreur rechargement carte", e.getMessage());
        }
    }
    
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
    
    // ===================== GESTION DES ERREURS =====================
    
    private void gererErreur(String titre, String message) {
        System.err.println(titre + ": " + message);
        vue.afficherMessageErreur(titre, message);
        etat = EtatAdmin.ERREUR;
    }
    
    private void gererErreurInitialisation(String titre, String message) {
        System.err.println(titre + ": " + message);
        JOptionPane.showMessageDialog(vue,
            message + "\n\nL'application va se fermer.",
            titre,
            JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
    
    // ===================== GETTERS ET SETTERS =====================
    
    public EtatAdmin getEtat() {
        return etat;
    }
    
    public String getEmailAdmin() {
        return emailAdmin;
    }
    
    public Map<String, Parking> getParkingsMap() {
        return parkingsMap;
    }
    
    public Double getLatitudeSelectionnee() {
        return latitudeSelectionnee;
    }
    
    public Double getLongitudeSelectionnee() {
        return longitudeSelectionnee;
    }
    
    public static double getTarifSoiree() {
        return TARIF_SOIREE;
    }
    
    // ===================== MÉTHODES UTILITAIRES =====================
    
    public String genererIdParking(String nom) {
        try {
            String baseId = nom.toUpperCase()
                .replaceAll(" ", "_")
                .replaceAll("[^A-Z0-9_]", "")
                .replaceAll("__+", "_");
            
            if (!baseId.startsWith("PARK_")) {
                baseId = "PARK_" + baseId;
            }
            
            // Vérifier l'unicité
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
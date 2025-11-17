package controleur;

import modele.Stationnement;
import modele.Usager;
import modele.Zone;
import modele.dao.PaiementDAO;
import modele.dao.ParkingDAO;
import modele.dao.StationnementDAO;
import modele.dao.UsagerDAO;
import modele.dao.ZoneDAO;
import modele.Parking;
import ihm.Page_Garer_Voirie;
import ihm.Page_Garer_Parking;
import ihm.Page_Paiement;
import ihm.Page_Principale;
import javax.swing.JOptionPane;
import java.time.LocalDateTime;
import java.util.List;

public class StationnementControleur {
    
    private String emailUtilisateur;
    private Usager usager;
    
    /**
     * Contrôleur pour la gestion des stationnements
     * @param email l'email de l'utilisateur connecté
     */
    public StationnementControleur(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
    }
    
    /**
     * Vérifie si l'utilisateur a un stationnement actif
     * @return le stationnement actif ou null
     */
    public Stationnement getStationnementActif() {
        if (usager != null) {
            return StationnementDAO.getStationnementActifValideByUsager(usager.getIdUsager());
        }
        return null;
    }
    
    /**
     * Prépare un nouveau stationnement en voirie
     * @param typeVehicule le type de véhicule
     * @param plaqueImmatriculation la plaque d'immatriculation
     * @param idZone l'ID de la zone
     * @param dureeHeures la durée en heures
     * @param dureeMinutes la durée en minutes
     * @param pageVoirie la page voirie pour les callbacks
     * @return true si la préparation réussit
     */
    public boolean preparerStationnementVoirie(String typeVehicule, String plaqueImmatriculation,
                                              String idZone, int dureeHeures, int dureeMinutes,
                                              Page_Garer_Voirie pageVoirie) {
        
        // Validation des champs
        if (!validerStationnementVoirie(typeVehicule, plaqueImmatriculation, idZone, 
                                       dureeHeures, dureeMinutes, pageVoirie)) {
            return false;
        }
        
        // Récupération de la zone
        Zone zone = ZoneDAO.getZoneById(idZone);
        if (zone == null) {
            JOptionPane.showMessageDialog(pageVoirie,
                "Zone non trouvée",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Calcul du coût
        int dureeTotaleMinutes = (dureeHeures * 60) + dureeMinutes;
        double cout = zone.calculerCout(dureeTotaleMinutes);
        
        // Vérification de la durée maximale
        if (dureeTotaleMinutes > zone.getDureeMaxMinutes()) {
            JOptionPane.showMessageDialog(pageVoirie,
                "Durée maximale dépassée pour " + zone.getLibelleZone() +
                " (max: " + formatDuree(zone.getDureeMaxMinutes()) + ")",
                "Erreur",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Redirection vers le paiement
        Page_Paiement pagePaiement = new Page_Paiement(
            cout,
            emailUtilisateur,
            typeVehicule,
            plaqueImmatriculation,
            idZone,
            zone.getLibelleZone(),
            dureeHeures,
            dureeMinutes
        );
        pagePaiement.setVisible(true);
        pageVoirie.dispose();
        
        return true;
    }
    
    /**
     * Prépare un nouveau stationnement en parking
     * @param typeVehicule le type de véhicule
     * @param plaqueImmatriculation la plaque d'immatriculation
     * @param idParking l'ID du parking
     * @param pageParking la page parking pour les callbacks
     * @return true si la préparation réussit
     */
    public boolean preparerStationnementParking(String typeVehicule, String plaqueImmatriculation,
                                               String idParking, Page_Garer_Parking pageParking) {
        
        // Validation des champs
        if (!validerStationnementParking(typeVehicule, plaqueImmatriculation, idParking, pageParking)) {
            return false;
        }
        
        // Récupération du parking
        Parking parking = ParkingDAO.getParkingById(idParking);
        if (parking == null) {
            JOptionPane.showMessageDialog(pageParking,
                "Parking non trouvé",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Vérification des places disponibles
        if (parking.getPlacesDisponibles() <= 0) {
            JOptionPane.showMessageDialog(pageParking,
                "Aucune place disponible dans ce parking",
                "Parking complet",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Création du stationnement
        boolean succes = StationnementDAO.creerStationnementParking(
            usager.getIdUsager(),
            typeVehicule,
            plaqueImmatriculation,
            idParking,
            LocalDateTime.now()
        );
        
        if (succes) {
            JOptionPane.showMessageDialog(pageParking,
                "Réservation confirmée !\n\n" +
                "Votre place est réservée dans le parking " + parking.getLibelleParking() + ".\n" +
                "N'oubliez pas de valider votre sortie pour le paiement.",
                "Réservation réussie",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Retour à la page principale
            Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
            pagePrincipale.setVisible(true);
            pageParking.dispose();
            return true;
        } else {
            JOptionPane.showMessageDialog(pageParking,
                "Erreur lors de la réservation. Veuillez réessayer.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Termine un stationnement voirie
     * @param idStationnement l'ID du stationnement
     * @return true si la terminaison réussit
     */
    public boolean terminerStationnementVoirie(int idStationnement) {
        return StationnementDAO.terminerStationnement(idStationnement);
    }
    
    /**
     * Termine un stationnement parking avec paiement
     * @param idStationnement l'ID du stationnement
     * @param heureDepart l'heure de départ
     * @param cout le coût calculé
     * @param idPaiement l'ID du paiement
     * @return true si la terminaison réussit
     */
    public boolean terminerStationnementParking(int idStationnement, LocalDateTime heureDepart,
                                               double cout, String idPaiement) {
        return StationnementDAO.terminerStationnementParking(
            idStationnement, heureDepart, cout, idPaiement);
    }
    
    /**
     * Valide les données pour un stationnement voirie
     */
    private boolean validerStationnementVoirie(String typeVehicule, String plaqueImmatriculation,
                                              String idZone, int dureeHeures, int dureeMinutes,
                                              Page_Garer_Voirie pageVoirie) {
        
        if (plaqueImmatriculation == null || plaqueImmatriculation.trim().isEmpty() ||
            plaqueImmatriculation.equals("Non définie")) {
            JOptionPane.showMessageDialog(pageVoirie,
                "Veuillez définir une plaque d'immatriculation",
                "Plaque manquante",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Vérification stationnement actif
        Stationnement stationnementActif = getStationnementActif();
        if (stationnementActif != null) {
            afficherMessageStationnementActif(stationnementActif, pageVoirie);
            return false;
        }
        
        return true;
    }
    
    /**
     * Valide les données pour un stationnement parking
     */
    private boolean validerStationnementParking(String typeVehicule, String plaqueImmatriculation,
                                               String idParking, Page_Garer_Parking pageParking) {
        
        if (plaqueImmatriculation == null || plaqueImmatriculation.trim().isEmpty() ||
            plaqueImmatriculation.equals("Non définie")) {
            JOptionPane.showMessageDialog(pageParking,
                "Veuillez définir une plaque d'immatriculation",
                "Plaque manquante",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Vérification stationnement actif
        Stationnement stationnementActif = getStationnementActif();
        if (stationnementActif != null) {
            afficherMessageStationnementActif(stationnementActif, pageParking);
            return false;
        }
        
        return true;
    }
    
    /**
     * Affiche un message d'erreur pour un stationnement actif
     */
    private void afficherMessageStationnementActif(Stationnement stationnement, javax.swing.JFrame parent) {
        String message = "Vous avez déjà un stationnement " + stationnement.getTypeStationnement() + " actif !\n\n" +
                        "Véhicule: " + stationnement.getTypeVehicule() + " - " + stationnement.getPlaqueImmatriculation() + "\n";
        
        if (stationnement.estVoirie()) {
            message += "Zone: " + stationnement.getZone() + "\n" +
                      "Début: " + stationnement.getDateCreation().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } else if (stationnement.estParking()) {
            message += "Parking: " + stationnement.getZone() + "\n" +
                      "Arrivée: " + stationnement.getHeureArrivee().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        
        message += "\n\nVeuillez terminer ce stationnement avant d'en créer un nouveau.";
        
        JOptionPane.showMessageDialog(parent, message, "Stationnement actif", JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Formate une durée en minutes en texte lisible
     */
    private String formatDuree(int minutes) {
        int heures = minutes / 60;
        int mins = minutes % 60;
        if (mins == 0) {
            return heures + "h";
        } else {
            return heures + "h" + mins + "min";
        }
    }
    
    /**
     * Récupère l'historique des stationnements de l'utilisateur
     * @return la liste des stationnements
     */
    public List<Stationnement> getHistoriqueStationnements() {
        if (usager != null) {
            return StationnementDAO.getHistoriqueStationnements(usager.getIdUsager());
        }
        return java.util.Collections.emptyList();
    }
}
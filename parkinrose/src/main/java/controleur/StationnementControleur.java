package controleur;

import modele.Stationnement;
import modele.Usager;
import modele.Zone;
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
    
    public StationnementControleur(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
    }
    
    public Stationnement getStationnementActif() {
        if (usager != null) {
            return StationnementDAO.getStationnementActifValideByUsager(usager.getIdUsager());
        }
        return null;
    }
    
    public boolean preparerStationnementVoirie(String typeVehicule, String plaqueImmatriculation,
                                              String idZone, int dureeHeures, int dureeMinutes,
                                              Page_Garer_Voirie pageVoirie) {
        
        if (!validerStationnementVoirie(plaqueImmatriculation, typeVehicule, idZone, 
                                       dureeHeures, dureeMinutes, pageVoirie)) {
            return false;
        }
        
        Zone zone = ZoneDAO.getZoneById(idZone);
        if (zone == null) {
            JOptionPane.showMessageDialog(pageVoirie,
                "Zone non trouvée",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        int dureeTotaleMinutes = (dureeHeures * 60) + dureeMinutes;
        double cout = zone.calculerCout(dureeTotaleMinutes);
        
        if (dureeTotaleMinutes > zone.getDureeMaxMinutes()) {
            JOptionPane.showMessageDialog(pageVoirie,
                "Durée maximale dépassée pour " + zone.getLibelleZone() +
                " (max: " + formatDuree(zone.getDureeMaxMinutes()) + ")",
                "Erreur",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
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
    
    public boolean preparerStationnementParking(String typeVehicule, String plaqueImmatriculation,
                                               String idParking, Page_Garer_Parking pageParking) {
        
        if (!validerStationnementParking(plaqueImmatriculation, typeVehicule, idParking, pageParking)) {
            return false;
        }
        
        Parking parking = ParkingDAO.getParkingById(idParking);
        if (parking == null) {
            JOptionPane.showMessageDialog(pageParking,
                "Parking non trouvé",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (parking.getPlacesDisponibles() <= 0) {
            JOptionPane.showMessageDialog(pageParking,
                "Aucune place disponible dans ce parking",
                "Parking complet",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
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
    
    public boolean terminerStationnementVoirie(int idStationnement) {
        return StationnementDAO.terminerStationnement(idStationnement);
    }
    
    public boolean terminerStationnementParking(int idStationnement, LocalDateTime heureDepart,
                                               double cout, String idPaiement) {
        return StationnementDAO.terminerStationnementParking(
            idStationnement, heureDepart, cout, idPaiement);
    }
    
    public boolean validerPlaque(String plaque) {
        if (plaque == null || plaque.trim().isEmpty() || plaque.equals("Non définie")) {
            return false;
        }
        return plaque.matches("[A-Z]{2}-\\d{3}-[A-Z]{2}");
    }
    
    private boolean validerStationnementVoirie(String plaqueImmatriculation, String typeVehicule,
                                              String idZone, int dureeHeures, int dureeMinutes,
                                              Page_Garer_Voirie pageVoirie) {
        
        if (!validerPlaque(plaqueImmatriculation)) {
            JOptionPane.showMessageDialog(pageVoirie,
                "Veuillez définir une plaque d'immatriculation valide (format: AA-123-AA)",
                "Plaque manquante",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        Stationnement stationnementActif = getStationnementActif();
        if (stationnementActif != null) {
            afficherMessageStationnementActif(stationnementActif, pageVoirie);
            return false;
        }
        
        return true;
    }
    
    private boolean validerStationnementParking(String plaqueImmatriculation, String typeVehicule,
                                               String idParking, Page_Garer_Parking pageParking) {
        
        if (!validerPlaque(plaqueImmatriculation)) {
            JOptionPane.showMessageDialog(pageParking,
                "Veuillez définir une plaque d'immatriculation valide (format: AA-123-AA)",
                "Plaque manquante",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        Stationnement stationnementActif = getStationnementActif();
        if (stationnementActif != null) {
            afficherMessageStationnementActif(stationnementActif, pageParking);
            return false;
        }
        
        return true;
    }
    
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
    
    private String formatDuree(int minutes) {
        int heures = minutes / 60;
        int mins = minutes % 60;
        if (mins == 0) {
            return heures + "h";
        } else {
            return heures + "h" + mins + "min";
        }
    }
    
    public List<Stationnement> getHistoriqueStationnements() {
        if (usager != null) {
            return StationnementDAO.getHistoriqueStationnements(usager.getIdUsager());
        }
        return java.util.Collections.emptyList();
    }
}
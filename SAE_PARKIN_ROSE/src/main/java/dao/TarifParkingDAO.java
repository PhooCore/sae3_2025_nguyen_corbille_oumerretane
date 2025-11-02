package dao;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class TarifParkingDAO {
    
    // Liste des parkings avec tarif soirée
    private static final String[] PARKINGS_TARIF_SOIREE = {
        "PARK_ARNAUD_BERNARD", "PARK_CARMES", "PARK_ESQUIROL", "PARK_JEAN_JAURES", 
        "PARK_JEANNE_DARC", "PARK_EUROPE", "PARK_SAINT_AUBIN", "PARK_VICTOR_HUGO", 
        "PARK_SAINT_CYPRIEN", "PARK_SAINT_ETIENNE", "PARK_SAINT_MICHEL", 
        "PARK_CARNOT", "PARK_CAPITOLE", "PARK_MATABIAU"
    };
    
    // Liste des parkings gratuits
    private static final String[] PARKINGS_GRATUITS = {
        "PARK_VIGUERIE", "PARK_BOULE", "PARK_VELODROME",
        "PARK_PONTS_JUMEAUX", "PARK_BONNEFOY", "PARK_MIRAIL", "PARK_CROIX_PIERRE"
    };
    
    // Liste des parkings relais (gratuits avec titre transport)
    private static final String[] PARKINGS_RELAIS = {
        "PARK_SEPT_DENIERS", "PARK_BAGATELLE", "PARK_JOLIMONT", "PARK_ARENES"
    };
    
    /**
     * Calcule le coût du stationnement en parking selon la durée réelle
     */
    public static double calculerCoutParking(LocalDateTime heureArrivee, LocalDateTime heureDepart, String idParking) {
        // Vérifier si le parking est gratuit
        if (estParkingGratuit(idParking)) {
            return 0.00;
        }
        
        // Vérifier si le tarif soirée s'applique
        if (tarifSoireeApplicable(heureArrivee, heureDepart, idParking)) {
            return 5.90; // Tarif forfaitaire soirée
        }
        
        // Calcul de la durée en minutes
        long dureeMinutes = ChronoUnit.MINUTES.between(heureArrivee, heureDepart);
        
        // Tarification au quart d'heure
        double tarifQuartHeure = getTarifQuartHeure(idParking);
        int nombreQuarts = (int) Math.ceil(dureeMinutes / 15.0);
        
        return nombreQuarts * tarifQuartHeure;
    }
    
    /**
     * Récupère le tarif au quart d'heure pour un parking donné
     */
    private static double getTarifQuartHeure(String idParking) {
        Map<String, Double> tarifs = new HashMap<>();
        
        // Parkings standards
        tarifs.put("PARK_CAPITOLE", 0.75);   // 3€/h
        tarifs.put("PARK_CARNOT", 0.75);     // 3€/h
        tarifs.put("PARK_ESQUIROL", 0.63);   // 2.50€/h
        tarifs.put("PARK_SAINT_ETIENNE", 0.63); // 2.50€/h
        tarifs.put("PARK_JEAN_JAURES", 0.50); // 2€/h
        tarifs.put("PARK_JEANNE_DARC", 0.50); // 2€/h
        tarifs.put("PARK_EUROPE", 0.50);     // 2€/h
        tarifs.put("PARK_VICTOR_HUGO", 0.50); // 2€/h
        tarifs.put("PARK_SAINT_AUBIN", 0.50); // 2€/h
        tarifs.put("PARK_SAINT_CYPRIEN", 0.50); // 2€/h
        tarifs.put("PARK_SAINT_MICHEL", 0.38); // 1.50€/h
        tarifs.put("PARK_MATABIAU", 1.00);   // 4€/h
        tarifs.put("PARK_ARNAUD_BERNARD", 0.38); // 1.50€/h
        tarifs.put("PARK_CARMES", 0.63);     // 2.50€/h
        
        // Parkings relais (tarif normal si pas de titre transport)
        tarifs.put("PARK_SEPT_DENIERS", 0.25); // 1€/h
        tarifs.put("PARK_BAGATELLE", 0.25);  // 1€/h
        tarifs.put("PARK_JOLIMONT", 0.25);   // 1€/h
        tarifs.put("PARK_ARENES", 0.25);     // 1€/h
        
        return tarifs.getOrDefault(idParking, 0.50); // Tarif par défaut 2€/h
    }
    
    /**
     * Vérifie si le tarif soirée s'applique
     */
    public static boolean tarifSoireeApplicable(LocalDateTime heureArrivee, LocalDateTime heureDepart, String idParking) {
        // Vérifier si le parking propose le tarif soirée
        boolean parkingEligible = false;
        for (String parking : PARKINGS_TARIF_SOIREE) {
            if (parking.equals(idParking)) {
                parkingEligible = true;
                break;
            }
        }
        if (!parkingEligible) return false;
        
        // Vérifier les horaires soirée (19h30-22h arrivée, départ avant 3h)
        boolean arriveeSoiree = (heureArrivee.getHour() == 19 && heureArrivee.getMinute() >= 30) ||
                               (heureArrivee.getHour() >= 20 && heureArrivee.getHour() < 22) ||
                               (heureArrivee.getHour() == 22 && heureArrivee.getMinute() == 0);
        
        boolean departAvant3h = heureDepart.getHour() < 3 ||
                               (heureDepart.getHour() == 3 && heureDepart.getMinute() == 0);
        
        return arriveeSoiree && departAvant3h;
    }
    
    /**
     * Vérifie si le parking est gratuit
     */
    public static boolean estParkingGratuit(String idParking) {
        for (String parking : PARKINGS_GRATUITS) {
            if (parking.equals(idParking)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Vérifie si c'est un parking relais
     */
    public static boolean estParkingRelais(String idParking) {
        for (String parking : PARKINGS_RELAIS) {
            if (parking.equals(idParking)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Vérifie si le parking propose le tarif soirée
     */
    public static boolean proposeTarifSoiree(String idParking) {
        for (String parking : PARKINGS_TARIF_SOIREE) {
            if (parking.equals(idParking)) {
                return true;
            }
        }
        return false;
    }
}
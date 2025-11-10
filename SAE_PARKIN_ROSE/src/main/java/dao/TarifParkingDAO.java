package dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class TarifParkingDAO {
    
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
     * Basé sur le champ tarif_soiree de la table Parking
     */
    public static boolean tarifSoireeApplicable(LocalDateTime heureArrivee, LocalDateTime heureDepart, String idParking) {
        // Vérifier si le parking propose le tarif soirée
        boolean parkingEligible = false;
        try {
            // Récupérer l'information depuis la base de données
            String sql = "SELECT tarif_soiree FROM Parking WHERE id_parking = ?";
            try (Connection conn = MySQLConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, idParking);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    parkingEligible = rs.getBoolean("tarif_soiree");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification tarif soirée: " + e.getMessage());
            return false;
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
     * Vérifie si le parking propose le tarif soirée
     */
    public static boolean proposeTarifSoiree(String idParking) {
        try {
            String sql = "SELECT tarif_soiree FROM Parking WHERE id_parking = ?";
            try (Connection conn = MySQLConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, idParking);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getBoolean("tarif_soiree");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification tarif soirée: " + e.getMessage());
        }
        return false;
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
     * Récupère le tarif horaire pour un parking
     */
    public static double getTarifHoraire(String idParking) {
        return getTarifQuartHeure(idParking) * 4; // 4 quarts d'heure = 1 heure
    }
    
    /**
     * Calcule la durée totale en minutes entre deux dates
     */
    public static long calculerDureeMinutes(LocalDateTime debut, LocalDateTime fin) {
        return ChronoUnit.MINUTES.between(debut, fin);
    }
    
    /**
     * Formate la durée en heures et minutes
     */
    public static String formaterDuree(long minutes) {
        long heures = minutes / 60;
        long mins = minutes % 60;
        if (heures == 0) {
            return mins + " min";
        } else if (mins == 0) {
            return heures + " h";
        } else {
            return heures + " h " + mins + " min";
        }
    }
    
    /**
     * Vérifie si un parking existe
     */
    public static boolean parkingExiste(String idParking) {
        String sql = "SELECT id_parking FROM Parking WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
            
        } catch (SQLException e) {
            System.err.println("Erreur vérification existence parking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Récupère les informations complètes d'un parking
     */
    public static Map<String, Object> getInfosParking(String idParking) {
        Map<String, Object> infos = new HashMap<>();
        String sql = "SELECT libelle_parking, adresse_parking, nombre_places, hauteur_parking, tarif_soiree FROM Parking WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                infos.put("libelle", rs.getString("libelle_parking"));
                infos.put("adresse", rs.getString("adresse_parking"));
                infos.put("places", rs.getInt("nombre_places"));
                infos.put("hauteur", rs.getDouble("hauteur_parking"));
                infos.put("tarif_soiree", rs.getBoolean("tarif_soiree"));
                infos.put("gratuit", estParkingGratuit(idParking));
                infos.put("relais", estParkingRelais(idParking));
                infos.put("tarif_horaire", getTarifHoraire(idParking));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération infos parking: " + e.getMessage());
        }
        
        return infos;
    }
}
package modele.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TarifParkingDAO {
    
    // Liste des parkings gratuits
    private static final String[] PARKINGS_GRATUITS = {
        "PARK_VIGUERIE", "PARK_BOULE", "PARK_VELODROME",
        "PARK_PONTS_JUMEAUX", "PARK_BONNEFOY", "PARK_MIRAIL", "PARK_CROIX_PIERRE"
    };
    
    /**
     * Récupère la liste des parkings relais (gratuits mais accessibles seulement si on a une carte pastel)
     */
    public static List<String> getParkingsRelais() {
        List<String> parkingsRelais = new ArrayList<>();

        String sql = "select id_parking from parking where est_relais = 1";

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                parkingsRelais.add(rs.getString("id_parking"));
            }

        } catch (SQLException e) {
            System.err.println("erreur récupération parkings relais : " + e.getMessage());
        }

        return parkingsRelais;
    }

    
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
            System.out.println("Tarif soirée appliqué pour " + idParking + ": 5.90€");
            return 5.90;
        }
        
        // Calcul normal de la durée
        long dureeMinutes = ChronoUnit.MINUTES.between(heureArrivee, heureDepart);
        
        // Minimum de 15 minutes
        if (dureeMinutes < 15) {
            dureeMinutes = 15;
        }
        
        // Tarification au quart d'heure
        double tarifQuartHeure = getTarifQuartHeure(idParking);
        int nombreQuarts = (int) Math.ceil(dureeMinutes / 15.0);
        
        double cout = nombreQuarts * tarifQuartHeure;
        
        // Forfait 24h maximum (tarif horaire * 24)
        double tarifHoraire = getTarifHoraire(idParking);
        double max24h = tarifHoraire * 24;
        
        if (cout > max24h && dureeMinutes <= (24 * 60)) {
            cout = max24h;
        }
        
        System.out.println("Calcul cout parking " + idParking + 
                         ": durée=" + dureeMinutes + "min, quarts=" + nombreQuarts + 
                         ", tarif/quart=" + tarifQuartHeure + ", cout=" + cout);
        
        return cout;
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
     * Vérifie si le tarif soirée s'applique avec des règles précises
     * NOUVEAU: Arrivée entre 19h30 et MINUIT, départ avant 3h le lendemain
     */
    public static boolean tarifSoireeApplicable(LocalDateTime heureArrivee, LocalDateTime heureDepart, String idParking) {
        // Vérifier si le parking propose le tarif soirée
        if (!proposeTarifSoiree(idParking)) {
            return false;
        }
        
        // Extraire les composants de temps
        int heureArriveeH = heureArrivee.getHour();
        int minuteArrivee = heureArrivee.getMinute();
        int heureDepartH = heureDepart.getHour();
        int minuteDepart = heureDepart.getMinute();
        
        // NOUVEAU: Vérifier si l'arrivée est entre 19h30 et MINUIT (24h00)
        boolean arriveeValide = false;
        
        // Cas 1: Arrivée entre 19h30 et 19h59
        if (heureArriveeH == 19 && minuteArrivee >= 30) {
            arriveeValide = true;
        }
        // Cas 2: Arrivée entre 20h et 23h
        else if (heureArriveeH >= 20 && heureArriveeH <= 23) {
            arriveeValide = true;
        }
        // Cas 3: Arrivée à minuit pile (peu probable mais possible)
        else if (heureArriveeH == 0 && minuteArrivee == 0) {
            // Vérifier que c'est bien le soir (date correspondante)
            // Si arrivée à minuit, on considère que c'est encore le tarif soirée
            arriveeValide = true;
        }
        
        // Vérifier si le départ est avant 3h00 du matin suivant
        boolean departValide = false;
        
        // Calculer la différence en heures
        long dureeHeures = ChronoUnit.HOURS.between(heureArrivee, heureDepart);
        long dureeMinutesTotal = ChronoUnit.MINUTES.between(heureArrivee, heureDepart);
        
        // Pour le tarif soirée, la durée totale doit être inférieure à 8h
        // (de minuit à 3h max = 3h, mais on laisse une marge)
        boolean dureeValide = dureeMinutesTotal <= (8 * 60); // 8 heures maximum
        
        // Conditions de départ:
        // 1. Si arrivée et départ même jour (cas rare pour tarif soirée)
        if (heureArrivee.toLocalDate().equals(heureDepart.toLocalDate())) {
            departValide = heureDepartH < 3;
        }
        // 2. Départ le lendemain (cas normal)
        else {
            LocalDateTime lendemain = heureArrivee.plusDays(1);
            if (heureDepart.toLocalDate().equals(lendemain.toLocalDate())) {
                departValide = heureDepartH < 3 || (heureDepartH == 3 && minuteDepart == 0);
            }
        }
        
        System.out.println("Vérification tarif soirée - " + idParking + 
                         ": Arrivée " + heureArriveeH + "h" + minuteArrivee + 
                         " (" + arriveeValide + "), Départ " + heureDepartH + "h" + minuteDepart + 
                         " (" + departValide + "), Durée totale: " + dureeMinutesTotal + "min (" + dureeValide + ")");
        
        return arriveeValide && departValide && dureeValide;
    }
    /**
     * Vérifie si une heure donnée est dans la plage du tarif soirée (pour affichage)
     */
    public static boolean estDansPlageTarifSoiree(java.time.LocalDateTime heure) {
        if (heure == null) return false;
        
        int heureH = heure.getHour();
        int minute = heure.getMinute();
        
        // Entre 19h30 et minuit
        if (heureH == 19 && minute >= 30) {
            return true;
        } else if (heureH >= 20 && heureH <= 23) {
            return true;
        } else if (heureH == 0 && minute == 0) {
            return true; // Minuit pile
        }
        
        return false;
    }
    /**
     * Formate l'affichage des tarifs pour l'interface utilisateur
     */
    public static String formaterAffichageTarifs(String idParking) {
        StringBuilder sb = new StringBuilder();
        
        if (estParkingGratuit(idParking)) {
            sb.append("Parking gratuit");
            return sb.toString();
        }
        
        double tarifHoraire = getTarifHoraire(idParking);
        sb.append(String.format("Tarif: %.2f€/h (%.2f€/15min)", tarifHoraire, tarifHoraire/4));
        
        if (proposeTarifSoiree(idParking)) {
            sb.append("\n");
            sb.append("Tarif soirée disponible: 5.90€");
            sb.append("\n(Arrivée 19h30-minuit, départ avant 3h)");
        }
        
        if (estParkingRelais(idParking)) {
            sb.append("\n");
            sb.append("Parking relais: Gratuit avec titre de transport");
            sb.append("\nSans titre: ").append(String.format("%.2f€/h", tarifHoraire));
        }
        
        return sb.toString();
    }
    /**
     * Donne la description textuelle du tarif soirée
     */
    public static String getDescriptionTarifSoiree() {
        return "Tarif Soirée: 5.90€\n" +
               "Conditions:\n" +
               "- Arrivée entre 19h30 et minuit\n" +
               "- Départ avant 3h le lendemain\n" +
               "- Durée maximale: 8 heures";
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
        return getParkingsRelais().contains(idParking);
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
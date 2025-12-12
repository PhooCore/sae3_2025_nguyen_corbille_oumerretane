package modele.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

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
    
    // Tarif soirée - configuration
    private static final double TARIF_SOIREE = 5.90;
    private static final LocalTime DEBUT_SOIREE = LocalTime.of(19, 30); // 19h30
    private static final LocalTime FIN_SOIREE = LocalTime.of(22, 0);    // 22h00
    private static final LocalTime DEPART_MAX_SOIREE = LocalTime.of(3, 0); // 3h00 du matin
    
    /**
     * Calcule le coût du stationnement en parking selon la durée réelle
     * APPLIQUE AUTOMATIQUEMENT le tarif soirée si l'heure d'arrivée est en soirée
     */
    public static double calculerCoutParking(LocalDateTime heureArrivee, LocalDateTime heureDepart, String idParking) {
        // Vérifier si le parking est gratuit
        if (estParkingGratuit(idParking)) {
            return 0.00;
        }
        
        // Vérifier si le tarif soirée s'applique AUTOMATIQUEMENT
        if (tarifSoireeApplicable(heureArrivee, heureDepart, idParking)) {
            return TARIF_SOIREE;
        }
        
        // Calcul normal basé sur la durée
        long dureeMinutes = ChronoUnit.MINUTES.between(heureArrivee, heureDepart);
        double tarifQuartHeure = getTarifQuartHeure(idParking);
        int nombreQuarts = (int) Math.ceil(dureeMinutes / 15.0);
        
        return nombreQuarts * tarifQuartHeure;
    }
    
    /**
     * Vérifie si le tarif soirée s'applique POUR CETTE RÉSERVATION
     * BASÉ SUR L'HEURE D'ARRIVÉE RÉELLE (LocalDateTime)
     */
    public static boolean tarifSoireeApplicable(LocalDateTime heureArrivee, LocalDateTime heureDepart, String idParking) {
        // Vérifier si le parking propose le tarif soirée
        if (!proposeTarifSoiree(idParking)) {
            return false;
        }
        
        LocalTime heureArriveeTime = heureArrivee.toLocalTime();
        LocalTime heureDepartTime = heureDepart.toLocalTime();
        
        // Vérifier que l'arrivée est entre 19h30 et 22h00
        boolean arriveeEnSoiree = !heureArriveeTime.isBefore(DEBUT_SOIREE) && 
                                  !heureArriveeTime.isAfter(FIN_SOIREE);
        
        // Vérifier que le départ est avant 3h du matin (même jour ou jour suivant)
        boolean departValide = false;
        
        if (heureDepart.toLocalDate().equals(heureArrivee.toLocalDate())) {
            // Même jour : départ doit être avant 3h du matin (techniquement jour suivant)
            departValide = heureDepartTime.isBefore(DEPART_MAX_SOIREE);
        } else {
            // Jour suivant : départ avant 3h
            departValide = heureDepartTime.isBefore(DEPART_MAX_SOIREE);
        }
        
        return arriveeEnSoiree && departValide;
    }
    
    /**
     * Vérifie si le tarif soirée s'appliquerait pour un stationnement COMMENCÉ MAINTENANT
     */
    public static boolean tarifSoireeApplicableMaintenant(String idParking) {
        if (!proposeTarifSoiree(idParking)) {
            return false;
        }
        
        LocalDateTime maintenant = LocalDateTime.now();
        LocalTime heureActuelle = maintenant.toLocalTime();
        
        // Vérifier si on est dans la plage horaire soirée (19h30-22h00)
        return !heureActuelle.isBefore(DEBUT_SOIREE) && 
               !heureActuelle.isAfter(FIN_SOIREE);
    }
    
    /**
     * Vérifie si un parking propose le tarif soirée
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
     * Récupère l'information du tarif soirée pour l'affichage
     */
    public static String getInfoTarifSoireePourParking(String idParking) {
        if (!proposeTarifSoiree(idParking)) {
            return "Pas de tarif soirée";
        }
        
        LocalDateTime maintenant = LocalDateTime.now();
        LocalTime heureActuelle = maintenant.toLocalTime();
        boolean applicableMaintenant = tarifSoireeApplicableMaintenant(idParking);
        
        String statut = applicableMaintenant ? 
            String.format("✅ TARIF SOIRÉE APPLICABLE MAINTENANT (%s)", heureActuelle) :
            String.format("⚠️ Disponible de %s à %s", DEBUT_SOIREE, FIN_SOIREE);
        
        return String.format(
            "🌙 Tarif soirée: %.2f€\n%s\nDépart avant %s",
            TARIF_SOIREE, statut, DEPART_MAX_SOIREE
        );
    }
    
    /**
     * Calcule le tarif pour un stationnement qui commence MAINTENANT
     */
    public static double calculerTarifMaintenant(int heures, int minutes, String idParking) {
        LocalDateTime heureArrivee = LocalDateTime.now();
        LocalDateTime heureDepart = heureArrivee.plusHours(heures).plusMinutes(minutes);
        
        return calculerCoutParking(heureArrivee, heureDepart, idParking);
    }
    
    /**
     * Récupère les informations sur les tarifs soirées pour l'affichage
     */
    public static String getInfoTarifSoiree(String idParking) {
        if (!proposeTarifSoiree(idParking)) {
            return null;
        }
        
        // Vérifier si le tarif soirée s'appliquerait si on stationnait maintenant
        boolean applicableMaintenant = tarifSoireeApplicableMaintenant(idParking);
        
        String statut = applicableMaintenant ? "✅ APPLICABLE MAINTENANT" : "⚠️ Pas dans la plage horaire";
        
        return String.format(
            "🌙 TARIF SOIRÉE\n" +
            "%s\n" +
            "Arrivée entre %s et %s\n" +
            "Départ avant %s\n" +
            "Tarif forfaitaire : %.2f€",
            statut, DEBUT_SOIREE, FIN_SOIREE, DEPART_MAX_SOIREE, TARIF_SOIREE
        );
    }
    
    // ... (le reste de votre classe reste inchangé) ...

    
    /**
     * Vérifie si le parking propose le tarif soirée
     */
    /*public static boolean proposeTarifSoiree(String idParking) {
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
    }*/
    
    /**
     * Affiche un popup d'information sur le tarif soirée
     */
    public static void afficherPopupTarifSoiree(java.awt.Component parent, String idParking) {
        if (proposeTarifSoiree(idParking)) {
            String message = String.format(
                "<html><div style='text-align: center;'><b>🎉 TARIF SOIRÉE DISPONIBLE !</b><br><br>" +
                "Ce parking propose un tarif spécial soirée :<br>" +
                "<font size='+2' color='#8B0000'><b>%.2f€</b></font><br><br>" +
                "<b>Conditions :</b><br>" +
                "• Arrivée entre %s et %s<br>" +
                "• Départ avant %s<br><br>" +
                "Ce tarif est forfaitaire et s'applique automatiquement<br>" +
                "si vous réservez dans cette plage horaire.</div></html>",
                TARIF_SOIREE, DEBUT_SOIREE, FIN_SOIREE, DEPART_MAX_SOIREE
            );
            
            JOptionPane.showMessageDialog(parent, message, 
                "Tarif soirée", JOptionPane.INFORMATION_MESSAGE);
        }
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
     * Récupère les informations complètes d'un parking
     */
    public static Map<String, Object> getInfosParking(String idParking) {
        Map<String, Object> infos = new HashMap<>();
        String sql = "SELECT libelle_parking, adresse_parking, nombre_places, " +
                    "places_disponibles, hauteur_parking, tarif_soiree " +
                    "FROM Parking WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                infos.put("libelle", rs.getString("libelle_parking"));
                infos.put("adresse", rs.getString("adresse_parking"));
                infos.put("places", rs.getInt("nombre_places"));
                infos.put("places_dispo", rs.getInt("places_disponibles"));
                infos.put("hauteur", rs.getDouble("hauteur_parking"));
                infos.put("tarif_soiree", rs.getBoolean("tarif_soiree"));
                infos.put("gratuit", estParkingGratuit(idParking));
                infos.put("relais", estParkingRelais(idParking));
                infos.put("tarif_horaire", getTarifHoraire(idParking));
                infos.put("tarif_soiree_montant", TARIF_SOIREE);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération infos parking: " + e.getMessage());
        }
        
        return infos;
    }
    
    /**
     * Récupère le tarif soirée
     */
    public static double getTarifSoiree() {
        return TARIF_SOIREE;
    }
    
    /**
     * Récupère les horaires soirée pour l'affichage
     */
    public static String getHorairesSoiree() {
        return DEBUT_SOIREE + " - " + FIN_SOIREE + " (départ avant " + DEPART_MAX_SOIREE + ")";
    }
}
package dao;

import modele.Zone;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ZoneDAO {
    
    /**
     * Récupère toutes les zones
     */
	public static List<Zone> getAllZones() {
	    List<Zone> zones = new ArrayList<>();
	    
	    // Ajouter une condition WHERE pour exclure les parkings
	    String sql = "SELECT id_zone, libelle_zone, couleur_zone, tarif_par_heure, duree_max FROM zone " +
	                "WHERE id_zone IN ('ZONE_BLEUE', 'ZONE_VERTE', 'ZONE_JAUNE', 'ZONE_ORANGE', 'ZONE_ROUGE') " +
	                "ORDER BY libelle_zone";
	    
	    try (Connection conn = MySQLConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {
	        
	        while (rs.next()) {
	            String idZone = rs.getString("id_zone");
	            String libelleZone = rs.getString("libelle_zone");
	            String couleurZone = rs.getString("couleur_zone");
	            double tarifParHeure = rs.getDouble("tarif_par_heure");
	            
	            String dureeMaxStr = rs.getString("duree_max");
	            LocalTime dureeMax = parseDureeMax(dureeMaxStr);
	            
	            Zone zone = new Zone(idZone, libelleZone, couleurZone, tarifParHeure, dureeMax);
	            zones.add(zone);
	        }
	        
	    } catch (SQLException e) {
	        System.err.println("Erreur lors de la récupération des zones: " + e.getMessage());
	        e.printStackTrace();
	    }
	    
	    return zones;
	}
    
    /**
     * Récupère une zone par son ID
     */
    public static Zone getZoneById(String idZone) {
        String sql = "SELECT id_zone, libelle_zone, couleur_zone, tarif_par_heure, duree_max FROM zone WHERE id_zone = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idZone);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String libelleZone = rs.getString("libelle_zone");
                    String couleurZone = rs.getString("couleur_zone");
                    double tarifParHeure = rs.getDouble("tarif_par_heure");
                    
                    // Récupérer la durée max comme String
                    String dureeMaxStr = rs.getString("duree_max");
                    LocalTime dureeMax = parseDureeMax(dureeMaxStr);
                    
                    return new Zone(idZone, libelleZone, couleurZone, tarifParHeure, dureeMax);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la zone: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Parse la durée maximale depuis un string MySQL
     * Gère le cas spécial "24:00:00" qui n'est pas un Time valide pour JDBC
     */
    private static LocalTime parseDureeMax(String dureeMaxStr) {
        if (dureeMaxStr == null || dureeMaxStr.trim().isEmpty()) {
            return LocalTime.of(0, 0); // Valeur par défaut
        }
        
        // Gérer le cas "24:00:00" qui n'est pas un Time valide pour JDBC
        if ("24:00:00".equals(dureeMaxStr) || "24:00".equals(dureeMaxStr)) {
            // Convertir en 23:59:59 ou selon votre logique métier
            return LocalTime.of(23, 59, 59);
        }
        
        try {
            // Supprimer les millisecondes si présentes
            if (dureeMaxStr.length() > 8) {
                dureeMaxStr = dureeMaxStr.substring(0, 8);
            }
            return LocalTime.parse(dureeMaxStr);
        } catch (Exception e) {
            System.err.println("Erreur parsing durée max: " + dureeMaxStr + " - " + e.getMessage());
            return LocalTime.of(0, 0); // Valeur par défaut en cas d'erreur
        }
    }
    
    /**
     * Recherche des zones par terme
     */
    public static List<Zone> rechercherZones(String terme) {
        List<Zone> zones = new ArrayList<>();
        String sql = "SELECT id_zone, libelle_zone, couleur_zone, tarif_par_heure, duree_max FROM zone " +
                    "WHERE libelle_zone LIKE ? OR id_zone LIKE ? " +
                    "ORDER BY libelle_zone";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String termeRecherche = "%" + terme + "%";
            stmt.setString(1, termeRecherche);
            stmt.setString(2, termeRecherche);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String idZone = rs.getString("id_zone");
                    String libelleZone = rs.getString("libelle_zone");
                    String couleurZone = rs.getString("couleur_zone");
                    double tarifParHeure = rs.getDouble("tarif_par_heure");
                    
                    String dureeMaxStr = rs.getString("duree_max");
                    LocalTime dureeMax = parseDureeMax(dureeMaxStr);
                    
                    Zone zone = new Zone(idZone, libelleZone, couleurZone, tarifParHeure, dureeMax);
                    zones.add(zone);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des zones: " + e.getMessage());
            e.printStackTrace();
        }
        
        return zones;
    }
}
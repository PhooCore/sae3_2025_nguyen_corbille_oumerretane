package dao;

import modèle.Zone;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ZoneDAO {
    
    public static List<Zone> getAllZones() {
        List<Zone> zones = new ArrayList<>();
        
        String sql = "SELECT id_zone, libelle_zone, couleur_zone, tarif_par_heure, duree_max FROM zone";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String idZone = rs.getString("id_zone");
                String libelleZone = rs.getString("libelle_zone");
                String couleurZone = rs.getString("couleur_zone");
                double tarifParHeure = rs.getDouble("tarif_par_heure");
                Time dureeMaxTime = rs.getTime("duree_max");
                
                LocalTime dureeMax = (dureeMaxTime != null) ? dureeMaxTime.toLocalTime() : LocalTime.of(2, 0);
                
                Zone zone = new Zone(idZone, libelleZone, couleurZone, tarifParHeure, dureeMax);
                zones.add(zone);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des zones: " + e.getMessage());
            e.printStackTrace();
        }
        
        return zones;
    }
    
    public static Zone getZoneById(String idZone) {
        String sql = "SELECT id_zone, libelle_zone, couleur_zone, tarif_par_heure, duree_max " +
                    "FROM zone WHERE id_zone = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idZone);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String libelleZone = rs.getString("libelle_zone");
                String couleurZone = rs.getString("couleur_zone");
                double tarifParHeure = rs.getDouble("tarif_par_heure");
                Time dureeMaxTime = rs.getTime("duree_max");
                
                LocalTime dureeMax = dureeMaxTime.toLocalTime();
                
                return new Zone(idZone, libelleZone, couleurZone, tarifParHeure, dureeMax);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la zone: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}
package dao;

import modèle.Parking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingDAO {
    
    /**
     * Récupère tous les parkings
     */
    public static List<Parking> getAllParkings() {
        List<Parking> parkings = new ArrayList<>();
        
        String sql = "SELECT id_parking, libelle_parking, adresse_parking, " +
                    "nombre_places, hauteur_parking FROM Parking " +
                    "ORDER BY libelle_parking";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Parking parking = new Parking(
                    rs.getString("id_parking"),
                    rs.getString("libelle_parking"),
                    rs.getString("adresse_parking"),
                    rs.getInt("nombre_places"),
                    rs.getDouble("hauteur_parking")
                );
                parkings.add(parking);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des parkings: " + e.getMessage());
            e.printStackTrace();
        }
        
        return parkings;
    }
    
    /**
     * Recherche des parkings par terme
     */
    public static List<Parking> rechercherParkings(String terme) {
        List<Parking> parkings = new ArrayList<>();
        
        String sql = "SELECT id_parking, libelle_parking, adresse_parking, " +
                    "nombre_places, hauteur_parking FROM Parking " +
                    "WHERE libelle_parking LIKE ? OR adresse_parking LIKE ? " +
                    "ORDER BY libelle_parking";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String termeRecherche = "%" + terme + "%";
            stmt.setString(1, termeRecherche);
            stmt.setString(2, termeRecherche);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Parking parking = new Parking(
                        rs.getString("id_parking"),
                        rs.getString("libelle_parking"),
                        rs.getString("adresse_parking"),
                        rs.getInt("nombre_places"),
                        rs.getDouble("hauteur_parking")
                    );
                    parkings.add(parking);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des parkings: " + e.getMessage());
            e.printStackTrace();
        }
        
        return parkings;
    }
    
    /**
     * Récupère un parking par son ID
     */
    public static Parking getParkingById(String idParking) {
        String sql = "SELECT id_parking, libelle_parking, adresse_parking, " +
                    "nombre_places, hauteur_parking FROM Parking " +
                    "WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Parking(
                        rs.getString("id_parking"),
                        rs.getString("libelle_parking"),
                        rs.getString("adresse_parking"),
                        rs.getInt("nombre_places"),
                        rs.getDouble("hauteur_parking")
                    );
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du parking: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}
package modele.dao;

import modele.Parking;

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
	                "nombre_places, places_disponibles, hauteur_parking, tarif_soiree FROM Parking " +
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
	                rs.getInt("places_disponibles"), 
	                rs.getDouble("hauteur_parking"),
	                rs.getBoolean("tarif_soiree")
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
	 * Récupère un parking par son ID
	 */
	public static Parking getParkingById(String idParking) {
	    // Gérer le cas PARKING_DEFAULT avec des valeurs réalistes
	    if ("PARKING_DEFAULT".equals(idParking)) {
	        return new Parking("PARKING_DEFAULT", "Parking par défaut", 
	                          "Adresse non spécifiée", 100, 80, 2.50, false); // 80/100 places
	    }
	    
	    String sql = "SELECT id_parking, libelle_parking, adresse_parking, " +
	                "nombre_places, places_disponibles, hauteur_parking, tarif_soiree FROM Parking " +
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
	                    rs.getInt("places_disponibles"),
	                    rs.getDouble("hauteur_parking"),
	                    rs.getBoolean("tarif_soiree")
	                );
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("Erreur lors de la récupération du parking: " + e.getMessage());
	        e.printStackTrace();
	    }
	    return null;
	}

	/**
	 * Recherche des parkings par terme
	 */
	public static List<Parking> rechercherParkings(String terme) {
	    List<Parking> parkings = new ArrayList<>();
	    String sql = "SELECT id_parking, libelle_parking, adresse_parking, " +
	                "nombre_places, places_disponibles, hauteur_parking, tarif_soiree FROM Parking " + // Ajout de places_disponibles
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
	                    rs.getInt("places_disponibles"), 
	                    rs.getDouble("hauteur_parking"),
	                    rs.getBoolean("tarif_soiree")
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
     * Décrémente le nombre de places disponibles d'un parking
     */
    public static boolean decrementerPlacesDisponibles(String idParking) {
        String sql = "UPDATE Parking SET places_disponibles = places_disponibles - 1 WHERE id_parking = ? AND places_disponibles > 0";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la décrémentation des places: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Incrémente le nombre de places disponibles d'un parking
     */
    public static boolean incrementerPlacesDisponibles(String idParking) {
        String sql = "UPDATE Parking SET places_disponibles = places_disponibles + 1 WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'incrémentation des places: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère le nombre de places disponibles actuel
     */
    public static int getPlacesDisponibles(String idParking) {
        String sql = "SELECT places_disponibles FROM Parking WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("places_disponibles");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération places disponibles: " + e.getMessage());
        }
        
        return 0;
    }
}
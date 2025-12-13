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
        String sql = "SELECT * FROM Parking ORDER BY libelle_parking";
        
        try (
            Connection conn = MySQLConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Parking parking = new Parking(
                    rs.getString("id_parking"),
                    rs.getString("libelle_parking"),
                    rs.getString("adresse_parking"),
                    rs.getInt("places_disponibles"),
                    rs.getInt("nombre_places"),
                    rs.getDouble("hauteur_parking"),
                    rs.getBoolean("tarif_soiree"),
                    rs.getBoolean("has_moto"),
                    rs.getInt("places_moto"),
                    rs.getInt("places_moto_disponibles")
                );
                parkings.add(parking);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération parkings: " + e.getMessage());
        }
        return parkings;
    }
    
    /**
     * Modifie un parking existant dans la base de données
     */
    public static boolean modifierParking(Parking parking) {
        String sql = "UPDATE Parking SET " +
                     "libelle_parking = ?, " +
                     "nombre_places = ?, " +
                     "places_disponibles = ?, " +
                     "adresse_parking = ?, " +
                     "hauteur_parking = ?, " +
                     "tarif_soiree = ?, " +
                     "has_moto = ?, " +
                     "places_moto = ?, " +
                     "places_moto_disponibles = ? " +
                     "WHERE id_parking = ?";
        
        try (
            Connection conn = MySQLConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, parking.getLibelleParking());
            stmt.setInt(2, parking.getNombrePlaces());
            stmt.setInt(3, parking.getPlacesDisponibles());
            stmt.setString(4, parking.getAdresseParking());
            stmt.setDouble(5, parking.getHauteurParking());
            stmt.setBoolean(6, parking.hasTarifSoiree());
            stmt.setBoolean(7, parking.hasMoto());
            stmt.setInt(8, parking.getPlacesMoto());
            stmt.setInt(9, parking.getPlacesMotoDisponibles());
            stmt.setString(10, parking.getIdParking());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur modification parking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Ajoute un nouveau parking dans la base de données
     */
    public static boolean ajouterParking(Parking parking) {
        String sql = "INSERT INTO Parking " +
                     "(id_parking, libelle_parking, nombre_places, places_disponibles, " +
                     "adresse_parking, hauteur_parking, tarif_soiree, has_moto, " +
                     "places_moto, places_moto_disponibles) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (
            Connection conn = MySQLConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, parking.getIdParking());
            stmt.setString(2, parking.getLibelleParking());
            stmt.setInt(3, parking.getNombrePlaces());
            stmt.setInt(4, parking.getPlacesDisponibles());
            stmt.setString(5, parking.getAdresseParking());
            stmt.setDouble(6, parking.getHauteurParking());
            stmt.setBoolean(7, parking.hasTarifSoiree());
            stmt.setBoolean(8, parking.hasMoto());
            stmt.setInt(9, parking.getPlacesMoto());
            stmt.setInt(10, parking.getPlacesMotoDisponibles());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur ajout parking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Supprime un parking de la base de données
     */
    public static boolean supprimerParking(String idParking) {
        String sql = "DELETE FROM Parking WHERE id_parking = ?";
        
        try (
            Connection conn = MySQLConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, idParking);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur suppression parking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Récupère un parking par son ID
     */
    public static Parking getParkingById(String idParking) {
        String sql = "SELECT id_parking, libelle_parking, adresse_parking, " +
                    "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                    "has_moto, places_moto, places_moto_disponibles FROM Parking " +
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
                        rs.getBoolean("tarif_soiree"),
                        rs.getBoolean("has_moto"),
                        rs.getInt("places_moto"),
                        rs.getInt("places_moto_disponibles")
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
                    "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                    "has_moto, places_moto, places_moto_disponibles FROM Parking " +
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
                        rs.getBoolean("tarif_soiree"),
                        rs.getBoolean("has_moto"),
                        rs.getInt("places_moto"),
                        rs.getInt("places_moto_disponibles")
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
     * Décrémente le nombre de places disponibles d'un parking (voiture/camion)
     */
    public static boolean decrementerPlacesDisponibles(String idParking) {
        String sql = "UPDATE Parking SET places_disponibles = places_disponibles - 1 " +
                    "WHERE id_parking = ? AND places_disponibles > 0";
        
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
     * Incrémente le nombre de places disponibles d'un parking (voiture/camion)
     */
    public static boolean incrementerPlacesDisponibles(String idParking) {
        String sql = "UPDATE Parking SET places_disponibles = places_disponibles + 1 " +
                    "WHERE id_parking = ?";
        
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
     * Décrémente le nombre de places moto disponibles
     */
    public static boolean decrementerPlacesMotoDisponibles(String idParking) {
        String sql = "UPDATE Parking SET places_moto_disponibles = places_moto_disponibles - 1 " +
                    "WHERE id_parking = ? AND has_moto = TRUE AND places_moto_disponibles > 0";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la décrémentation des places moto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Incrémente le nombre de places moto disponibles
     */
    public static boolean incrementerPlacesMotoDisponibles(String idParking) {
        String sql = "UPDATE Parking SET places_moto_disponibles = places_moto_disponibles + 1 " +
                    "WHERE id_parking = ? AND has_moto = TRUE";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'incrémentation des places moto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère le nombre de places disponibles actuel (voiture/camion)
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
    
    /**
     * Récupère le nombre de places moto disponibles
     */
    public static int getPlacesMotoDisponibles(String idParking) {
        String sql = "SELECT places_moto_disponibles FROM Parking WHERE id_parking = ? AND has_moto = TRUE";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("places_moto_disponibles");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération places moto disponibles: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Vérifie si un parking a des places moto disponibles
     */
    public static boolean hasPlacesMotoDisponibles(String idParking) {
        String sql = "SELECT places_moto_disponibles FROM Parking WHERE id_parking = ? AND has_moto = TRUE";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("places_moto_disponibles") > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur vérification places moto: " + e.getMessage());
        }
        
        return false;
    }
}
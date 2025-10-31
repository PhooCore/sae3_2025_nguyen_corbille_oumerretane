package dao;

import modèle.Stationnement;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StationnementDAO {
    
    // Vérifie si un véhicule a déjà un stationnement actif
    public static boolean vehiculeAStationnementActif(String plaqueImmatriculation) {
        String sql = "SELECT COUNT(*) FROM Stationnement WHERE plaque_immatriculation = ? AND statut = 'ACTIF'";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, plaqueImmatriculation);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du stationnement actif: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    // Récupère le stationnement actif d'un véhicule
    public static Stationnement getStationnementActif(String plaqueImmatriculation) {
        String sql = "SELECT * FROM Stationnement WHERE plaque_immatriculation = ? AND statut = 'ACTIF'";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, plaqueImmatriculation);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStationnement(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du stationnement actif: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    // Méthode pour terminer un stationnement
    public static boolean terminerStationnement(int idStationnement) {
        String sql = "UPDATE Stationnement SET statut = 'TERMINE', date_fin = NOW() WHERE id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idStationnement);
            
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fin du stationnement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Crée un nouveau stationnement
    public static boolean creerStationnement(Stationnement stationnement) {
        String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, " +
                    "zone, duree_heures, duree_minutes, cout, date_creation, date_fin, statut, id_paiement) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? MINUTE), 'ACTIF', ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            int dureeTotaleMinutes = stationnement.getDureeHeures() * 60 + stationnement.getDureeMinutes();
            
            stmt.setInt(1, stationnement.getIdUsager());
            stmt.setString(2, stationnement.getTypeVehicule());
            stmt.setString(3, stationnement.getPlaqueImmatriculation());
            stmt.setString(4, stationnement.getZone());
            stmt.setInt(5, stationnement.getDureeHeures());
            stmt.setInt(6, stationnement.getDureeMinutes());
            stmt.setDouble(7, stationnement.getCout());
            stmt.setInt(8, dureeTotaleMinutes);
            stmt.setString(9, stationnement.getIdPaiement());
            
            int rowsInserted = stmt.executeUpdate();
            
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        stationnement.setIdStationnement(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création du stationnement: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    // Méthode utilitaire pour mapper ResultSet à Stationnement
    private static Stationnement mapResultSetToStationnement(ResultSet rs) throws SQLException {
        Stationnement stationnement = new Stationnement();
        stationnement.setIdStationnement(rs.getInt("id_stationnement"));
        stationnement.setIdUsager(rs.getInt("id_usager"));
        stationnement.setTypeVehicule(rs.getString("type_vehicule"));
        stationnement.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
        stationnement.setZone(rs.getString("zone"));
        stationnement.setDureeHeures(rs.getInt("duree_heures"));
        stationnement.setDureeMinutes(rs.getInt("duree_minutes"));
        stationnement.setCout(rs.getDouble("cout"));
        stationnement.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        
        Timestamp dateFin = rs.getTimestamp("date_fin");
        if (dateFin != null) {
            stationnement.setDateFin(dateFin.toLocalDateTime());
        }
        
        stationnement.setStatut(rs.getString("statut"));
        stationnement.setIdPaiement(rs.getString("id_paiement"));
        return stationnement;
    }
}
package modele.dao;

import modele.Vehicule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeDAO {
    
    public static boolean ajouterVehiculeUsager(int idUsager, String plaque, String alias) {
        String sql = "INSERT INTO vehicule_usager (id_usager, plaque_immatriculation, alias_vehicule) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE alias_vehicule = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String plaqueClean = plaque.toUpperCase().replaceAll("\\s", "");
            
            pstmt.setInt(1, idUsager);
            pstmt.setString(2, plaqueClean);
            pstmt.setString(3, alias);
            pstmt.setString(4, alias);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur dans ajouterVehiculeUsager: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static List<Vehicule> getVehiculesByUsager(int idUsager) {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT id_vehicule_usager, plaque_immatriculation, alias_vehicule, est_vehicule_principal " +
                    "FROM vehicule_usager " +
                    "WHERE id_usager = ? " +
                    "ORDER BY est_vehicule_principal DESC, date_ajout DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Vehicule vehicule = new Vehicule();
                vehicule.setIdVehiculeUsager(rs.getInt("id_vehicule_usager"));
                vehicule.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
                vehicule.setAlias(rs.getString("alias_vehicule"));
                vehicule.setEstPrincipal(rs.getBoolean("est_vehicule_principal"));
                vehicules.add(vehicule);
            }
        } catch (SQLException e) {
            System.err.println("Erreur dans getVehiculesByUsager: " + e.getMessage());
            e.printStackTrace();
        }
        return vehicules;
    }
    
    public static boolean supprimerVehicule(int idVehiculeUsager) {
        String sql = "DELETE FROM vehicule_usager WHERE id_vehicule_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idVehiculeUsager);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur dans supprimerVehicule: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean definirVehiculePrincipal(int idUsager, int idVehiculeUsager) {
        Connection conn = null;
        
        try {
            conn = MySQLConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Réinitialiser tous les véhicules
            String sqlReset = "UPDATE vehicule_usager SET est_vehicule_principal = FALSE WHERE id_usager = ?";
            try (PreparedStatement pstmtReset = conn.prepareStatement(sqlReset)) {
                pstmtReset.setInt(1, idUsager);
                pstmtReset.executeUpdate();
            }
            
            // 2. Définir le nouveau véhicule principal
            String sqlSet = "UPDATE vehicule_usager SET est_vehicule_principal = TRUE WHERE id_vehicule_usager = ?";
            try (PreparedStatement pstmtSet = conn.prepareStatement(sqlSet)) {
                pstmtSet.setInt(1, idVehiculeUsager);
                boolean success = pstmtSet.executeUpdate() > 0;
                conn.commit();
                return success;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur dans definirVehiculePrincipal: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static boolean existePlaquePourUsager(int idUsager, String plaque) {
        String sql = "SELECT COUNT(*) as count FROM vehicule_usager " +
                    "WHERE id_usager = ? AND plaque_immatriculation = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String plaqueClean = plaque.toUpperCase().replaceAll("\\s", "");
            pstmt.setInt(1, idUsager);
            pstmt.setString(2, plaqueClean);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur dans existePlaquePourUsager: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
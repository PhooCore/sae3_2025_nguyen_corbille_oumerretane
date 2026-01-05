package modele.dao;

import modele.VehiculeUsager;
import modele.dao.requetes.RequeteSelectVehiculeUsagerByUsager;
import modele.dao.requetes.RequeteSelectVehiculeUsagerPrincipal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeUsagerDAO extends DaoModele<VehiculeUsager> implements Dao<VehiculeUsager> {

    private static VehiculeUsagerDAO instance;
    
    // Singleton
    public static VehiculeUsagerDAO getInstance() {
        if (instance == null) {
            instance = new VehiculeUsagerDAO();
        }
        return instance;
    }
    
    // Constructeur privé pour singleton
    private VehiculeUsagerDAO() {}

    @Override
    public List<VehiculeUsager> findAll() throws SQLException {
        throw new UnsupportedOperationException("Utiliser getVehiculesByUsager pour récupérer les véhicules d'un usager spécifique");
    }

    @Override
    public VehiculeUsager findById(String... id) throws SQLException {
        throw new UnsupportedOperationException("Utiliser les méthodes spécifiques pour récupérer les véhicules usager");
    }

    @Override
    public void create(VehiculeUsager vehicule) throws SQLException {
        ajouterVehicule(vehicule);
    }

    @Override
    public void update(VehiculeUsager vehicule) throws SQLException {
        throw new UnsupportedOperationException("Mise à jour non implémentée pour VehiculeUsager");
    }

    @Override
    public void delete(VehiculeUsager vehicule) throws SQLException {
        throw new UnsupportedOperationException("Delete non implémenté pour VehiculeUsager");
    }

    /**
     * Vérifie si une plaque existe déjà pour un utilisateur
     */
    public boolean plaqueExistePourUsager(int idUsager, String plaque) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Vehicule_Usager WHERE id_usager = ? AND plaque_immatriculation = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            stmt.setString(2, plaque.toUpperCase());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        }
        return false;
    }

    @Override
    protected VehiculeUsager creerInstance(ResultSet curseur) throws SQLException {
        VehiculeUsager vehicule = new VehiculeUsager();
        vehicule.setIdVehiculeUsager(curseur.getInt("id_vehicule_usager"));
        vehicule.setIdUsager(curseur.getInt("id_usager"));
        vehicule.setPlaqueImmatriculation(curseur.getString("plaque_immatriculation"));
        vehicule.setTypeVehicule(curseur.getString("type_vehicule"));
        vehicule.setMarque(curseur.getString("marque"));
        vehicule.setModele(curseur.getString("modele"));
        
        Date dateAjout = curseur.getDate("date_ajout");
        if (dateAjout != null) {
            vehicule.setDateAjout(dateAjout.toLocalDate());
        }
        
        vehicule.setEstPrincipal(curseur.getBoolean("est_principal"));
        return vehicule;
    }

    // Méthodes statiques pour compatibilité
    
    /**
     * Récupère le véhicule principal d'un utilisateur (version statique)
     */
    public static VehiculeUsager getVehiculePrincipalStatic(int idUsager) {
        try {
            return getInstance().getVehiculePrincipal(idUsager);
        } catch (Exception e) {
            System.err.println("Erreur récupération véhicule principal: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Récupère tous les véhicules d'un utilisateur (version statique)
     */
    public static List<VehiculeUsager> getVehiculesByUsagerStatic(int idUsager) {
        try {
            return getInstance().getVehiculesByUsager(idUsager);
        } catch (Exception e) {
            System.err.println("Erreur récupération véhicules: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Ajoute un véhicule (version statique)
     */
    public static boolean ajouterVehiculeStatic(VehiculeUsager vehicule) {
        try {
            getInstance().ajouterVehicule(vehicule);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur ajout véhicule: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Supprime un véhicule (version statique) - NOUVELLE MÉTHODE
     */
    public static boolean supprimerVehiculeStatic(int idVehicule) {
        String sql = "DELETE FROM Vehicule_Usager WHERE id_vehicule_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idVehicule);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression véhicule: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Définit un véhicule comme principal (version statique) - NOUVELLE MÉTHODE
     */
    public static boolean definirVehiculePrincipalStatic(int idVehicule, int idUsager) {
        try (Connection conn = MySQLConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            // D'abord, désactiver tous les véhicules principaux de cet utilisateur
            String sqlDesactiver = "UPDATE Vehicule_Usager SET est_principal = FALSE WHERE id_usager = ?";
            try (PreparedStatement stmtDesactiver = conn.prepareStatement(sqlDesactiver)) {
                stmtDesactiver.setInt(1, idUsager);
                stmtDesactiver.executeUpdate();
            }
            
            // Ensuite, activer le véhicule sélectionné
            String sqlActiver = "UPDATE Vehicule_Usager SET est_principal = TRUE WHERE id_vehicule_usager = ? AND id_usager = ?";
            try (PreparedStatement stmtActiver = conn.prepareStatement(sqlActiver)) {
                stmtActiver.setInt(1, idVehicule);
                stmtActiver.setInt(2, idUsager);
                int rows = stmtActiver.executeUpdate();
                
                if (rows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur définition véhicule principal: " + e.getMessage());
            return false;
        }
    }

    // Méthodes privées utilitaires
    
    private void desactiverAncienPrincipal(Connection conn, int idUsager) throws SQLException {
        String sql = "UPDATE Vehicule_Usager SET est_principal = FALSE WHERE id_usager = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsager);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Récupère le véhicule principal d'un utilisateur (version statique)
     */
    public static VehiculeUsager getVehiculePrincipal(int idUsager) {
        String sql = "SELECT * FROM Vehicule_Usager WHERE id_usager = ? AND est_principal = TRUE LIMIT 1";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVehiculeUsager(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération véhicule principal: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Récupère tous les véhicules d'un utilisateur (version statique)
     */
    public static List<VehiculeUsager> getVehiculesByUsager(int idUsager) {
        List<VehiculeUsager> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM Vehicule_Usager WHERE id_usager = ? ORDER BY est_principal DESC, date_ajout DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapResultSetToVehiculeUsager(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération véhicules: " + e.getMessage());
        }
        return vehicules;
    }
    
    /**
     * Ajoute un véhicule (version statique)
     */
    public static boolean ajouterVehicule(VehiculeUsager vehicule) {
        String sql = "INSERT INTO Vehicule_Usager (id_usager, plaque_immatriculation, type_vehicule, est_principal, date_ajout) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = MySQLConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            // Si c'est le véhicule principal, désactiver l'ancien principal
            if (vehicule.isEstPrincipal()) {
                String updateSql = "UPDATE Vehicule_Usager SET est_principal = FALSE WHERE id_usager = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, vehicule.getIdUsager());
                    updateStmt.executeUpdate();
                }
            }
            
            // Ajouter le nouveau véhicule
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, vehicule.getIdUsager());
                stmt.setString(2, vehicule.getPlaqueImmatriculation());
                stmt.setString(3, vehicule.getTypeVehicule());
                stmt.setBoolean(4, vehicule.isEstPrincipal());
                stmt.setDate(5, Date.valueOf(vehicule.getDateAjout()));
                
                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false;
                }
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Erreur ajout véhicule: " + e.getMessage());
            return false;
        }
    }
    
    private static VehiculeUsager mapResultSetToVehiculeUsager(ResultSet rs) throws SQLException {
        VehiculeUsager vehicule = new VehiculeUsager();
        vehicule.setIdVehiculeUsager(rs.getInt("id_vehicule_usager"));
        vehicule.setIdUsager(rs.getInt("id_usager"));
        vehicule.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
        vehicule.setTypeVehicule(rs.getString("type_vehicule"));
        vehicule.setMarque(rs.getString("marque"));
        vehicule.setModele(rs.getString("modele"));
        
        Date dateAjout = rs.getDate("date_ajout");
        if (dateAjout != null) {
            vehicule.setDateAjout(dateAjout.toLocalDate());
        }
        
        vehicule.setEstPrincipal(rs.getBoolean("est_principal"));
        return vehicule;
    }
}

package modele.dao;

import modele.Adresse;
import modele.dao.requetes.Requete;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdresseDAO extends DaoModele<Adresse> implements Dao<Adresse> {
    
    private static AdresseDAO instance;
    
    private AdresseDAO() {}
    
    public static AdresseDAO getInstance() {
        if (instance == null) {
            instance = new AdresseDAO();
        }
        return instance;
    }
    
    @Override
    public List<Adresse> findAll() throws SQLException {
        String sql = "SELECT * FROM Adresse ORDER BY est_principale DESC, id_adresse";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            List<Adresse> adresses = new ArrayList<>();
            while (rs.next()) {
                adresses.add(creerInstance(rs));
            }
            return adresses;
        }
    }
    
    @Override
    public Adresse findById(String... id) throws SQLException {
        String sql = "SELECT * FROM Adresse WHERE id_adresse = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, Integer.parseInt(id[0]));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return creerInstance(rs);
                }
            }
        }
        return null;
    }
    
    @Override
    public void create(Adresse adresse) throws SQLException {
        String sql = "INSERT INTO Adresse (id_usager, numero, rue, complement, code_postal, ville, pays, est_principale) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, adresse.getIdUsager());
            stmt.setString(2, adresse.getNumero());
            stmt.setString(3, adresse.getRue());
            stmt.setString(4, adresse.getComplement());
            stmt.setString(5, adresse.getCodePostal());
            stmt.setString(6, adresse.getVille());
            stmt.setString(7, adresse.getPays());
            stmt.setBoolean(8, adresse.isEstPrincipale());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        adresse.setIdAdresse(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }
    
    @Override
    public void update(Adresse adresse) throws SQLException {
        String sql = "UPDATE Adresse SET numero = ?, rue = ?, complement = ?, " +
                    "code_postal = ?, ville = ?, pays = ?, est_principale = ? " +
                    "WHERE id_adresse = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, adresse.getNumero());
            stmt.setString(2, adresse.getRue());
            stmt.setString(3, adresse.getComplement());
            stmt.setString(4, adresse.getCodePostal());
            stmt.setString(5, adresse.getVille());
            stmt.setString(6, adresse.getPays());
            stmt.setBoolean(7, adresse.isEstPrincipale());
            stmt.setInt(8, adresse.getIdAdresse());
            
            stmt.executeUpdate();
        }
    }
    
    @Override
    public void delete(Adresse adresse) throws SQLException {
        String sql = "DELETE FROM Adresse WHERE id_adresse = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adresse.getIdAdresse());
            stmt.executeUpdate();
        }
    }
    
    @Override
    protected Adresse creerInstance(ResultSet rs) throws SQLException {
        Adresse adresse = new Adresse();
        adresse.setIdAdresse(rs.getInt("id_adresse"));
        adresse.setIdUsager(rs.getInt("id_usager"));
        adresse.setNumero(rs.getString("numero"));
        adresse.setRue(rs.getString("rue"));
        adresse.setComplement(rs.getString("complement"));
        adresse.setCodePostal(rs.getString("code_postal"));
        adresse.setVille(rs.getString("ville"));
        adresse.setPays(rs.getString("pays"));
        adresse.setEstPrincipale(rs.getBoolean("est_principale"));
        return adresse;
    }
    
    // Méthodes spécifiques
    
    public List<Adresse> getAdressesByUsager(int idUsager) throws SQLException {
        String sql = "SELECT * FROM Adresse WHERE id_usager = ? ORDER BY est_principale DESC, id_adresse";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Adresse> adresses = new ArrayList<>();
                while (rs.next()) {
                    adresses.add(creerInstance(rs));
                }
                return adresses;
            }
        }
    }
    
    public Adresse getAdressePrincipale(int idUsager) throws SQLException {
        String sql = "SELECT * FROM Adresse WHERE id_usager = ? AND est_principale = TRUE";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return creerInstance(rs);
                }
            }
        }
        return null;
    }
    
    public void definirAdressePrincipale(int idAdresse, int idUsager) throws SQLException {
        Connection conn = null;
        try {
            conn = MySQLConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Désactiver toutes les adresses principales
            String sqlReset = "UPDATE Adresse SET est_principale = FALSE WHERE id_usager = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlReset)) {
                stmt.setInt(1, idUsager);
                stmt.executeUpdate();
            }
            
            // Définir la nouvelle adresse principale
            String sqlSet = "UPDATE Adresse SET est_principale = TRUE WHERE id_adresse = ? AND id_usager = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlSet)) {
                stmt.setInt(1, idAdresse);
                stmt.setInt(2, idUsager);
                stmt.executeUpdate();
            }
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    public static boolean adresseExistePourUsager(int idUsager, String numero, String rue, String codePostal, String ville) {
        String sql = "SELECT COUNT(*) FROM Adresse WHERE id_usager = ? AND numero = ? AND rue = ? AND code_postal = ? AND ville = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            stmt.setString(2, numero);
            stmt.setString(3, rue);
            stmt.setString(4, codePostal);
            stmt.setString(5, ville);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification adresse: " + e.getMessage());
        }
        return false;
    }
}
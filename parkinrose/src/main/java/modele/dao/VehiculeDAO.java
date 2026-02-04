package modele.dao;

import modele.Vehicule;
import modele.dao.requetes.RequeteSelectVehiculeByUsager;
import modele.dao.requetes.RequeteSelectVehiculeById;
import modele.dao.requetes.RequeteSelectVehiculeByPlaque;
import modele.dao.requetes.RequeteInsertVehicule;
import modele.dao.requetes.RequeteUpdateVehicule;
import modele.dao.requetes.RequeteSelectAllVehicules;
import modele.dao.requetes.RequeteSelectVehiculesOrphelins;
import modele.dao.requetes.RequeteSelectVehiculesRecherche;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class VehiculeDAO extends DaoModele<Vehicule> implements Dao<Vehicule> {

    private static Iterateur<Vehicule> iterateur = null;

    @Override
    public List<Vehicule> findAll() throws SQLException {
        RequeteSelectAllVehicules req = new RequeteSelectAllVehicules();
        return find(req);
    }

    @Override
    public Vehicule findById(String... id) throws SQLException {
        RequeteSelectVehiculeById req = new RequeteSelectVehiculeById();
        return findById(req, id);
    }

    @Override
    public void create(Vehicule vehicule) throws SQLException {
        throw new UnsupportedOperationException("Utiliser ajouterVehiculeUtilisateur pour créer un véhicule avec propriétaire");
    }

    @Override
    public void update(Vehicule vehicule) throws SQLException {
        RequeteUpdateVehicule req = new RequeteUpdateVehicule();
        miseAJour(req, vehicule);
    }

    @Override
    public void delete(Vehicule vehicule) throws SQLException {
        throw new UnsupportedOperationException("Utiliser supprimerVehiculeUtilisateur pour supprimer un véhicule");
    }

    /**
     * Récupère tous les véhicules d'un utilisateur
     */
    public List<Vehicule> getVehiculesByUsager(int idUsager) throws SQLException {
        RequeteSelectVehiculeByUsager req = new RequeteSelectVehiculeByUsager();
        return find(req, String.valueOf(idUsager));
    }

    /**
     * Récupère un véhicule par sa plaque d'immatriculation
     */
    public Vehicule getVehiculeByPlaque(String plaqueImmatriculation) throws SQLException {
        RequeteSelectVehiculeByPlaque req = new RequeteSelectVehiculeByPlaque();
        return findById(req, plaqueImmatriculation);
    }

    /**
     * Vérifie si une plaque existe déjà dans la base
     */
    public boolean plaqueExiste(String plaqueImmatriculation) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Vehicule WHERE plaque_immatriculation = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, plaqueImmatriculation.toUpperCase());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        }
        return false;
    }

    /**
     * Ajoute un nouveau véhicule et l'associe à un utilisateur
     */
    public boolean ajouterVehiculeUtilisateur(int idUsager, Vehicule vehicule) throws SQLException {
        Connection conn = null;
        try {
            conn = MySQLConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Générer un ID unique pour le véhicule
            String idVehicule = genererIdVehicule();
            vehicule.setIdVehicule(idVehicule);
            
            // 1. Insérer le véhicule dans la table Vehicule
            RequeteInsertVehicule reqInsert = new RequeteInsertVehicule();
            PreparedStatement pstmtVehicule = conn.prepareStatement(reqInsert.requete());
            reqInsert.parametres(pstmtVehicule, vehicule);
            pstmtVehicule.executeUpdate();
            pstmtVehicule.close();
            
            // 2. Créer la relation dans la table Posseder
            String sqlPosseder = "INSERT INTO Posseder (id_usager, id_vehicule) VALUES (?, ?)";
            try (PreparedStatement pstmtPosseder = conn.prepareStatement(sqlPosseder)) {
                pstmtPosseder.setInt(1, idUsager);
                pstmtPosseder.setString(2, idVehicule);
                pstmtPosseder.executeUpdate();
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new SQLException("Erreur lors du rollback: " + ex.getMessage(), ex);
                }
            }
            throw new SQLException("Erreur lors de l'ajout du véhicule: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    throw new SQLException("Erreur lors de la fermeture de la connexion: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Supprime un véhicule et sa relation avec l'utilisateur
     */
    public boolean supprimerVehiculeUtilisateur(int idUsager, String plaqueImmatriculation) throws SQLException {
        Connection conn = null;
        try {
            conn = MySQLConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Récupérer l'ID du véhicule à partir de la plaque
            String idVehicule = getIdVehiculeByPlaque(conn, plaqueImmatriculation);
            if (idVehicule == null) {
                throw new SQLException("Véhicule non trouvé: " + plaqueImmatriculation);
            }
            
            // 2. Vérifier que le véhicule appartient bien à l'utilisateur
            if (!verifierAppartenance(conn, idUsager, idVehicule)) {
                throw new SQLException("Le véhicule n'appartient pas à cet utilisateur");
            }
            
            // 3. Vérifier si le véhicule est utilisé dans des stationnements actifs
            if (estUtiliseDansStationnements(conn, idVehicule)) {
                throw new SQLException("Le véhicule est utilisé dans des stationnements actifs");
            }
            
            // 4. Supprimer la relation dans Posseder
            String sqlSupprimerPosseder = "DELETE FROM Posseder WHERE id_usager = ? AND id_vehicule = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlSupprimerPosseder)) {
                pstmt.setInt(1, idUsager);
                pstmt.setString(2, idVehicule);
                pstmt.executeUpdate();
            }
            
            // 5. Supprimer le véhicule
            String sqlSupprimerVehicule = "DELETE FROM Vehicule WHERE id_vehicule = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlSupprimerVehicule)) {
                pstmt.setString(1, idVehicule);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected == 0) {
                    throw new SQLException("Échec de la suppression du véhicule");
                }
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new SQLException("Erreur lors du rollback: " + ex.getMessage(), ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    throw new SQLException("Erreur lors de la fermeture de la connexion: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Récupère les véhicules sans propriétaire (orphelins)
     */
    public List<Vehicule> getVehiculesOrphelins() throws SQLException {
        RequeteSelectVehiculesOrphelins req = new RequeteSelectVehiculesOrphelins();
        return find(req);
    }

    /**
     * Compte le nombre de véhicules d'un utilisateur
     */
    public int countVehiculesByUsager(int idUsager) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Posseder WHERE id_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        }
        return 0;
    }

    /**
     * Nettoie les véhicules orphelins (sans propriétaire)
     */
    public int nettoyerVehiculesOrphelins() throws SQLException {
        List<Vehicule> vehiculesOrphelins = getVehiculesOrphelins();
        int count = 0;
        
        for (Vehicule vehicule : vehiculesOrphelins) {
            if (!vehiculeUtilise(vehicule.getPlaqueImmatriculation())) {
                String sql = "DELETE FROM Vehicule WHERE id_vehicule = ?";
                
                try (Connection conn = MySQLConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    
                    pstmt.setString(1, vehicule.getIdVehicule());
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        count++;
                    }
                    
                }
            }
        }
        
        return count;
    }

    /**
     * Recherche des véhicules par plaque (pour l'administration)
     */
    public List<Vehicule> rechercherVehicules(String recherche) throws SQLException {
        RequeteSelectVehiculesRecherche req = new RequeteSelectVehiculesRecherche();
        return find(req, "%" + recherche + "%");
    }

    @Override
    protected Vehicule creerInstance(ResultSet curseur) throws SQLException {
        Vehicule vehicule = new Vehicule();
        vehicule.setIdVehicule(curseur.getString("id_vehicule"));
        vehicule.setPlaqueImmatriculation(curseur.getString("plaque_immatriculation"));
        vehicule.setTypeVehicule(vehicule.determinerTypeVehicule(vehicule.getPlaqueImmatriculation()));
        return vehicule;
    }

    /**
     * Méthode pour utiliser l'itérateur
     */
    public Iterateur<Vehicule> findAllIte() throws SQLException {
        RequeteSelectAllVehicules req = new RequeteSelectAllVehicules();
        Connection conn = MySQLConnection.getConnection();
        PreparedStatement prSt = conn.prepareStatement(req.requete());
        ResultSet rs = prSt.executeQuery();
        iterateur = new Iterateur<>(rs, this);
        return iterateur;
    }

    // Méthodes privées utilitaires
    
    private String genererIdVehicule() {
        return "VEH_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    private String getIdVehiculeByPlaque(Connection conn, String plaque) throws SQLException {
        String sql = "SELECT id_vehicule FROM Vehicule WHERE plaque_immatriculation = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plaque);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id_vehicule");
            }
        }
        return null;
    }
    
    private boolean verifierAppartenance(Connection conn, int idUsager, String idVehicule) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Posseder WHERE id_usager = ? AND id_vehicule = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsager);
            pstmt.setString(2, idVehicule);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    private boolean estUtiliseDansStationnements(Connection conn, String idVehicule) throws SQLException {
        String plaque = getPlaqueById(conn, idVehicule);
        if (plaque == null) return false;
        
        String sql = "SELECT COUNT(*) FROM Stationnement WHERE plaque_immatriculation = ? AND statut = 'ACTIF'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plaque);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    private String getPlaqueById(Connection conn, String idVehicule) throws SQLException {
        String sql = "SELECT plaque_immatriculation FROM Vehicule WHERE id_vehicule = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idVehicule);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("plaque_immatriculation");
            }
        }
        return null;
    }
    
    private boolean vehiculeUtilise(String plaque) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Stationnement WHERE plaque_immatriculation = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, plaque);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        }
        return false;
    }
}
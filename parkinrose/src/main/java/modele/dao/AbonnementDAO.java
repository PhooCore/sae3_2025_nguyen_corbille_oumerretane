package modele.dao;

import modele.Abonnement;
import modele.dao.requetes.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbonnementDAO extends DaoModele<Abonnement> {
    
    private static AbonnementDAO instance;
    private static Iterateur<Abonnement> iterateur;
    
    public AbonnementDAO() {}
    
    public static AbonnementDAO getInstance() {
        if (instance == null) {
            instance = new AbonnementDAO();
        }
        return instance;
    }
    
    public static Iterateur<Abonnement> getIterateur() {
        return iterateur;
    }
    
    public static void setIterateur(Iterateur<Abonnement> iterateur) {
        AbonnementDAO.iterateur = iterateur;
    }
    
    @Override
    protected Abonnement creerInstance(ResultSet curseur) throws SQLException {
        Abonnement abonnement = new Abonnement();
        abonnement.setIdAbonnement(curseur.getString("id_abonnement"));
        abonnement.setLibelleAbonnement(curseur.getString("libelle_abonnement"));
        abonnement.setTarifAbonnement(curseur.getDouble("tarif_applique"));
        return abonnement;
    }
    
    @Override
    public List<Abonnement> findAll() throws SQLException {
        RequeteSelectAbonnement req = new RequeteSelectAbonnement();
        return find(req);
    }
    
    @Override
    public Abonnement findById(String... id) throws SQLException {
        if (id.length == 0) {
            return null;
        }
        RequeteSelectAbonnementById req = new RequeteSelectAbonnementById();
        return findById(req, id[0]);
    }
    
    @Override
    public void create(Abonnement abonnement) throws SQLException {
        // D'abord supprimer les relations dans Appartenir
        String sqlDeleteAppartenir = "DELETE FROM Appartenir WHERE id_abonnement = ?";
        Connection conn = MySQLConnection.getConnection();
        PreparedStatement pstmt1 = conn.prepareStatement(sqlDeleteAppartenir);
        pstmt1.setString(1, abonnement.getIdAbonnement());
        pstmt1.executeUpdate();
        pstmt1.close();
        
        // Ensuite insérer l'abonnement
        RequeteInsertAbonnement req = new RequeteInsertAbonnement();
        miseAJour(req, abonnement);
    }
    
    @Override
    public void update(Abonnement abonnement) throws SQLException {
        RequeteUpdateAbonnement req = new RequeteUpdateAbonnement();
        miseAJour(req, abonnement);
    }
    
    @Override
    public void delete(Abonnement abonnement) throws SQLException {
        // D'abord supprimer les relations dans Appartenir
        String sqlDeleteAppartenir = "DELETE FROM Appartenir WHERE id_abonnement = ?";
        Connection conn = MySQLConnection.getConnection();
        PreparedStatement pstmt1 = conn.prepareStatement(sqlDeleteAppartenir);
        pstmt1.setString(1, abonnement.getIdAbonnement());
        pstmt1.executeUpdate();
        pstmt1.close();
        
        // Ensuite supprimer l'abonnement
        RequeteDeleteAbonnement req = new RequeteDeleteAbonnement();
        miseAJour(req, abonnement);
    }
    
    public Iterateur<Abonnement> findAllIte() throws SQLException {
        Connection conn = MySQLConnection.getConnection();
        PreparedStatement prSt = conn.prepareStatement("SELECT * FROM Abonnement ORDER BY tarif_applique");
        ResultSet rs = prSt.executeQuery();
        iterateur = new Iterateur<>(rs, this);
        return iterateur;
    }
    
    // Méthodes spécifiques pour les abonnements
    public List<Abonnement> getAbonnementsByUsager(int idUsager) throws SQLException {
        String sql = "SELECT a.* FROM Abonnement a " +
                    "INNER JOIN Appartenir ap ON a.id_abonnement = ap.id_abonnement " +
                    "WHERE ap.id_usager = ? ORDER BY a.tarif_applique";
        Connection conn = MySQLConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, idUsager);
        return select(pstmt);
    }
    
    public boolean ajouterAbonnementUtilisateur(int idUsager, String idAbonnement) throws SQLException {
        if (findById(idAbonnement) == null) {
            return false;
        }
        
        // Vérifier si l'utilisateur existe
        if (!usagerExiste(idUsager)) {
            return false;
        }
        
        // D'abord supprimer les anciens abonnements
        supprimerAbonnementsUtilisateur(idUsager);
        
        String sql = "INSERT INTO Appartenir (id_usager, id_abonnement, date_debut) VALUES (?, ?, CURDATE())";
        Connection conn = MySQLConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, idUsager);
        pstmt.setString(2, idAbonnement);
        
        return pstmt.executeUpdate() > 0;
    }
    
    public void supprimerAbonnementsUtilisateur(int idUsager) throws SQLException {
        String sql = "DELETE FROM Appartenir WHERE id_usager = ?";
        Connection conn = MySQLConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, idUsager);
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    private boolean usagerExiste(int idUsager) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Usager WHERE id_usager = ?";
        Connection conn = MySQLConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, idUsager);
        ResultSet rs = pstmt.executeQuery();
        boolean existe = false;
        if (rs.next()) {
            existe = rs.getInt(1) > 0;
        }
        rs.close();
        pstmt.close();
        return existe;
    }
    
    /**
     * Vérifie si un usager a un abonnement actif
     */
    public boolean usagerAAbonnementActif(int idUsager) throws SQLException {
        // Version simplifiée : vérifier juste si une ligne existe dans Appartenir
        String sql = "SELECT COUNT(*) FROM Appartenir WHERE id_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        }
        return false;
    }

    /**
     * Récupère l'abonnement actif d'un usager
     */
    public Abonnement getAbonnementActif(int idUsager) throws SQLException {
        // Jointure avec Appartenir pour récupérer aussi les dates
        String sql = "SELECT a.*, ap.date_debut, ap.date_fin, ap.est_actif " +
                    "FROM Abonnement a " +
                    "INNER JOIN Appartenir ap ON a.id_abonnement = ap.id_abonnement " +
                    "WHERE ap.id_usager = ? " +
                    "AND ap.est_actif = TRUE " +
                    "LIMIT 1";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Abonnement abonnement = creerInstance(rs);
                System.out.println("DEBUG: Abonnement trouvé - " + abonnement.getLibelleAbonnement() + 
                                 " - Tarif: " + abonnement.getTarifAbonnement() +
                                 " - Gratuit: " + abonnement.estGratuit());
                return abonnement;
            } else {
                System.out.println("DEBUG: Aucun abonnement actif trouvé pour l'usager " + idUsager);
            }
        }
        return null;
    }

    /**
     * Récupère le tarif de l'abonnement actif d'un usager
     * Retourne 0.0 si l'abonnement rend le stationnement gratuit
     */
    public double getTarifAbonnementActif(int idUsager) throws SQLException {
        Abonnement abonnement = getAbonnementActif(idUsager);
        if (abonnement != null) {
            return abonnement.getTarifAbonnement();
        }
        return -1.0; // Pas d'abonnement
    }

    /**
     * Version statique pour compatibilité
     */
    public static boolean usagerAAbonnementActifStatic(int idUsager) {
        try {
            return getInstance().usagerAAbonnementActif(idUsager);
        } catch (SQLException e) {
            System.err.println("Erreur vérification abonnement: " + e.getMessage());
            return false;
        }
    }

    /**
     * Version statique pour récupérer l'abonnement actif
     */
    public static Abonnement getAbonnementActifStatic(int idUsager) {
        try {
            return getInstance().getAbonnementActif(idUsager);
        } catch (SQLException e) {
            System.err.println("Erreur récupération abonnement actif: " + e.getMessage());
            return null;
        }
    }
}
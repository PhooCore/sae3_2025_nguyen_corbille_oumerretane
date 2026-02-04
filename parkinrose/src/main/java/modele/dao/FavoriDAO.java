package modele.dao;

import modele.Favori;
import modele.Parking;
import modele.dao.requetes.RequeteCreerFavori;
import modele.dao.requetes.RequeteDeleteFavori;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO pour gérer les favoris des utilisateurs
public class FavoriDAO extends DaoModele<Favori> {

    // Singleton instance
    private static FavoriDAO instance;

    private final RequeteCreerFavori reqCreate = new RequeteCreerFavori();
    private final RequeteDeleteFavori reqDelete = new RequeteDeleteFavori();
    
    // Constructeur privé pour le singleton
    private FavoriDAO() {}

    // Méthode pour obtenir l'instance unique de FavoriDAO
    public static FavoriDAO getInstance() {
        if (instance == null) {
            instance = new FavoriDAO();
        }
        return instance;
    }

    @Override
    // Crée une instance de Favori à partir d'un ResultSet
    protected Favori creerInstance(ResultSet rs) throws SQLException {
        return new Favori(
            rs.getInt("id_usager"),
            rs.getString("id_parking")
        );
    }


    @Override
    // Crée un nouveau favori dans la base de données
    public void create(Favori favori) throws SQLException {
        miseAJour(reqCreate, favori);
    }

    @Override
    // Supprime un favori de la base de données
    public void delete(Favori favori) throws SQLException {
        miseAJour(reqDelete, favori);
    }

    @Override
    // Mise à jour non supportée pour Favori
    public void update(Favori obj) throws SQLException {
    	//inutile dans ce contexte
        throw new UnsupportedOperationException("update non supporté pour Favori");
    }

    @Override
    // Trouve un favori par son identifiant composé
    public Favori findById(String... id) throws SQLException {
        // Vérifier que l'identifiant est correct
        if (id.length < 2) return null;

        // Requête SQL pour trouver un favori
        String sql = "SELECT * FROM Favori WHERE id_usager = ? AND id_parking = ?";

        // Gérer la connexion et la déclaration
        try (Connection conn = MySQLConnection.getConnection();
            // Préparer la déclaration SQL
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Définir les paramètres de la requête
            stmt.setInt(1, Integer.parseInt(id[0]));
            stmt.setString(2, id[1]);

            // Exécuter la requête et traiter le résultat
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return creerInstance(rs);
                }
            }
        }
        return null;
    }

    @Override
    // Récupère tous les favoris de la base de données
    public List<Favori> findAll() throws SQLException {
        // Requête SQL pour récupérer tous les favoris
        String sql = "SELECT * FROM Favoris";
        // Liste pour stocker les favoris
        List<Favori> liste = new ArrayList<>();

        // Gérer la connexion, la déclaration et le résultat
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            // Parcourir les résultats et créer des instances de Favori
            while (rs.next()) {
                liste.add(creerInstance(rs));
            }
        }
        return liste;
    }


    /* ================= Méthodes métier ================= */

    /**
     * Ajouter un parking en favori
     */
    
    public boolean ajouterFavori(int idUsager, String idParking) {
        try {
            create(new Favori(idUsager, idParking));
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur ajout favori: " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprimer un favori
     */
    
    public boolean supprimerFavori(int idUsager, String idParking) {
        try {
            delete(new Favori(idUsager, idParking));
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur suppression favori: " + e.getMessage());
            return false;
        }
    }

    /*
     * Vérifier si un parking est déjà en favori
     */
    
    
    public boolean estFavori(int idUsager, String idParking) throws SQLException {
        return findById(String.valueOf(idUsager), idParking) != null;
    }

    /**
    * Récupérer les parkings favoris d'un utilisateur
    */
    
    
    public List<String> getFavorisUtilisateur(int idUsager) throws SQLException {
        String sql = "SELECT id_parking FROM Favori WHERE id_usager = ?";
        List<String> favoris = new ArrayList<>();

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsager);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    favoris.add(rs.getString("id_parking"));
                }
            }
        }
        return favoris;
    }
    
    /**
     * Récupérer les objets Parking des favoris d'un utilisateur
     */
    
    public List<Parking> getParkingsFavoris(int idUsager) throws SQLException {
        List<Parking> parkings = new ArrayList<>();
        List<String> ids = getFavorisUtilisateur(idUsager);

        ParkingDAO parkingDAO = ParkingDAO.getInstance();

        for (String idParking : ids) {
            Parking p = parkingDAO.findById(idParking);
            if (p != null) {
                parkings.add(p);
            }
        }
        return parkings;
    }

}

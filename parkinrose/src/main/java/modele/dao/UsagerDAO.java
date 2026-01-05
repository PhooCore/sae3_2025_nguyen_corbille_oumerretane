package modele.dao;

import modele.Usager;
import modele.dao.requetes.RequeteSelectUsager;
import modele.dao.requetes.RequeteSelectUsagerByEmail;
import modele.dao.requetes.RequeteInsertUsager;
import modele.dao.requetes.RequeteUpdateUsager;
import modele.dao.requetes.RequeteUpdateMotDePasse;
import modele.dao.requetes.RequeteUpdateCarteTisseo;
import modele.dao.requetes.RequeteSelectCarteTisseo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UsagerDAO extends DaoModele<Usager> implements Dao<Usager> {

    private static UsagerDAO instance;

    // Singleton
    public static UsagerDAO getInstance() {
        if (instance == null) {
            instance = new UsagerDAO();
        }
        return instance;
    }

    // Constructeur privé pour singleton
    private UsagerDAO() {}

    @Override
    public List<Usager> findAll() throws SQLException {
        RequeteSelectUsager req = new RequeteSelectUsager();
        return find(req);
    }

    @Override
    public Usager findById(String... id) throws SQLException {
        RequeteSelectUsagerByEmail req = new RequeteSelectUsagerByEmail();
        return findById(req, id);
    }

    @Override
    public void create(Usager usager) throws SQLException {
        RequeteInsertUsager req = new RequeteInsertUsager();
        miseAJour(req, usager);
    }

    @Override
    public void update(Usager usager) throws SQLException {
        RequeteUpdateUsager req = new RequeteUpdateUsager();
        miseAJour(req, usager);
    }

    @Override
    public void delete(Usager usager) throws SQLException {
        throw new UnsupportedOperationException("Delete non implémenté pour Usager");
    }

    @Override
    protected Usager creerInstance(ResultSet curseur) throws SQLException {
        Usager usager = new Usager();
        usager.setIdUsager(curseur.getInt("id_usager"));
        usager.setNomUsager(curseur.getString("nom_usager"));
        usager.setPrenomUsager(curseur.getString("prenom_usager"));
        usager.setMailUsager(curseur.getString("mail_usager"));
        usager.setMotDePasse(curseur.getString("mot_de_passe"));
        usager.setNumeroCarteTisseo(curseur.getString("numero_carte_tisseo"));
        usager.setAdmin(curseur.getBoolean("is_admin"));
        return usager;
    }

    /**
     * Vérifie si un email existe déjà
     */
    public boolean emailExiste(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Usager WHERE mail_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        }
        return false;
    }

    /**
     * Modifie le mot de passe d'un utilisateur
     */
    public boolean modifierMotDePasse(String email, String nouveauMotDePasse) throws SQLException {
        RequeteUpdateMotDePasse req = new RequeteUpdateMotDePasse();
        Usager usagerTemp = new Usager();
        usagerTemp.setMailUsager(email);
        usagerTemp.setMotDePasse(nouveauMotDePasse);
        int result = miseAJour(req, usagerTemp);
        return result > 0;
    }

    /**
     * Récupère la carte Tisséo d'un utilisateur
     */
    public String getCarteTisseoByUsager(int idUsager) throws SQLException {
        String sql = "SELECT numero_carte_tisseo FROM Usager WHERE id_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("numero_carte_tisseo");
            }
            
        }
        return null;
    }

    /**
     * Enregistre la carte Tisséo d'un utilisateur (version statique)
     */
    public static boolean enregistrerCarteTisseo(int idUsager, String numeroCarte) {
        String sql = "UPDATE Usager SET numero_carte_tisseo = ? WHERE id_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, numeroCarte);
            stmt.setInt(2, idUsager);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur enregistrement carte Tisséo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère tous les utilisateurs (méthode static pour compatibilité)
     */
    public static List<Usager> getAllUsagers() {
        try {
            return getInstance().findAll();
        } catch (SQLException e) {
            System.err.println("Erreur récupération utilisateurs: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Ajoute un nouvel utilisateur (méthode static pour compatibilité)
     */
    public static boolean ajouterUsager(Usager usager) {
        try {
            getInstance().create(usager);
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur ajout utilisateur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modifie un utilisateur existant (méthode static pour compatibilité)
     */
    public static boolean modifierUsager(Usager usager) {
        try {
            getInstance().update(usager);
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur modification utilisateur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Vérifie si un email existe déjà dans la base de données
     * Utilisé lors de l'inscription pour éviter les doublons
     * 
     * @param email l'adresse email à vérifier
     * @return true si l'email existe déjà, false sinon
     */
    public static boolean emailExisteDeja(String email) {
        // Requête SQL pour compter les utilisateurs avec cet email
        String sql = "SELECT mail_usager FROM Usager WHERE mail_usager = ?";
        
        try (
            Connection conn = MySQLConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, email); // 1er ? : email à vérifier
            
            try (ResultSet rs = stmt.executeQuery()) {
                // Si rs.next() retourne true, c'est qu'un enregistrement a été trouvé
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Erreur vérification email: " + e.getMessage());
            return false; // En cas d'erreur, on considère que l'email n'existe pas
        }
    }

    /**
     * Mappe un ResultSet vers un objet Usager
     */
    private static Usager mapResultSetToUsager(ResultSet rs) throws SQLException {
        Usager usager = new Usager();
        usager.setIdUsager(rs.getInt("id_usager"));
        usager.setNomUsager(rs.getString("nom_usager"));
        usager.setPrenomUsager(rs.getString("prenom_usager"));
        usager.setMailUsager(rs.getString("mail_usager"));
        usager.setMotDePasse(rs.getString("mot_de_passe"));
        usager.setNumeroCarteTisseo(rs.getString("numero_carte_tisseo"));
        usager.setAdmin(rs.getBoolean("is_admin"));
        return usager;
    }
    /**
     * Récupère un utilisateur par son adresse email (version statique)
     */
    public static Usager getUsagerByEmail(String email) {
        try {
            return getInstance().findById(email);
        } catch (SQLException e) {
            System.err.println("Erreur récupération utilisateur par email: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }	
}
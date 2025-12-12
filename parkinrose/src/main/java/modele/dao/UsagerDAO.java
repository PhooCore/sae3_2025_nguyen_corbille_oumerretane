package modele.dao;
import modele.Usager;

import java.sql.*;

public class UsagerDAO {
    
    /**
     * Ajoute un nouvel utilisateur dans la base de données
     * Utilisé lors de l'inscription d'un nouvel utilisateur
     * 
     * @param usager l'objet Usager contenant les informations du nouvel utilisateur
     * @return true si l'ajout a réussi, false sinon
     */
    public static boolean ajouterUsager(Usager usager) {
        String sql = "INSERT INTO Usager (nom_usager, prenom_usager, mail_usager, mot_de_passe, is_admin) VALUES (?, ?, ?, ?, ?)";
        try (
            // Connexion à la base de données
            Connection conn = MySQLConnection.getConnection();
            
            // Préparation de la requête avec retour des clés générées (pour récupérer l'ID auto-incrémenté)
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            // Remplissage des paramètres de la requête
            stmt.setString(1, usager.getNomUsager());        // 1er ? : nom de l'usager
            stmt.setString(2, usager.getPrenomUsager());     // 2ème ? : prénom de l'usager
            stmt.setString(3, usager.getMailUsager());       // 3ème ? : email de l'usager
            stmt.setString(4, usager.getMotDePasse());       // 4ème ? : mot de passe (en clair - à hasher en production)
            stmt.setBoolean(5, usager.isAdmin());
            // Exécution de la requête d'insertion
            int ligneinseree = stmt.executeUpdate();
            
            // Vérification que l'insertion a réussi
            if (ligneinseree > 0) {
                // Récupération de l'ID auto-généré par la base de données
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Attribution de l'ID à l'objet Usager
                        usager.setIdUsager(generatedKeys.getInt(1));
                    }
                }
                return true; // Succès de l'insertion
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'usager: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false; // Échec de l'insertion
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
     * Récupère un utilisateur par son adresse email
     * Utilisé pour la connexion et pour récupérer les informations de l'utilisateur connecté
     * 
     * @param email l'adresse email de l'utilisateur recherché
     * @return l'objet Usager correspondant, ou null si non trouvé
     */
    public static Usager getUsagerByEmail(String email) {
        // Requête SQL pour sélectionner un utilisateur par son email
        String sql = "SELECT * FROM Usager WHERE mail_usager = ?";
        
        try (
            Connection conn = MySQLConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, email); // 1er ? : email de l'utilisateur recherché
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Création d'un nouvel objet Usager
                    Usager usager = new Usager();
                    
                    // Remplissage de l'objet avec les données de la base
                    usager.setIdUsager(rs.getInt("id_usager"));              // ID auto-généré
                    usager.setNomUsager(rs.getString("nom_usager"));         // Nom de famille
                    usager.setPrenomUsager(rs.getString("prenom_usager"));   // Prénom
                    usager.setMailUsager(rs.getString("mail_usager"));       // Email
                    usager.setMotDePasse(rs.getString("mot_de_passe"));      // Mot de passe
                    usager.setAdmin(rs.getBoolean("is_admin"));
                    
                    return usager;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'usager: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Aucun utilisateur trouvé
    }
    /**
     * Modifie le mot de passe d'un utilisateur
     * @param email l'email de l'utilisateur
     * @param nouveauMotDePasse le nouveau mot de passe
     * @return true si la modification a réussi, false sinon
     */
    public static boolean modifierMotDePasse(String email, String nouveauMotDePasse) {
        String sql = "UPDATE Usager SET mot_de_passe = ? WHERE mail_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nouveauMotDePasse);
            stmt.setString(2, email);
            
            int lignesAffectees = stmt.executeUpdate();
            
            if (lignesAffectees > 0) {
                System.out.println("Mot de passe modifié pour l'utilisateur: " + email);
                return true;
            } else {
                System.out.println("Aucun utilisateur trouvé avec l'email: " + email);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification du mot de passe: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
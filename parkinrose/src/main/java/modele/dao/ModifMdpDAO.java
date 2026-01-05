package modele.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ModifMdpDAO {
    
    // Requêtes SQL
    private static final String UPDATE_MDP = "UPDATE Usager SET mot_de_passe = ? WHERE mail_usager = ?";
    private static final String VERIFIER_EMAIL = "SELECT mail_usager FROM Usager WHERE mail_usager = ?";
    private static final String VERIFIER_ANCIEN_MDP = "SELECT mail_usager FROM Usager WHERE mail_usager = ? AND mot_de_passe = ?";
    private static final String GET_ID_BY_EMAIL = "SELECT id_usager FROM Usager WHERE mail_usager = ?";
    private static final String GET_INFOS_USAGER = "SELECT nom_usager, prenom_usager FROM Usager WHERE mail_usager = ?";
    
    // Critères de force du mot de passe
    private static final int LONGUEUR_MIN_MDP = 8;
    
    /**
     * Modifie le mot de passe d'un utilisateur
     * @param email Email de l'utilisateur
     * @param nouveauMotDePasse Nouveau mot de passe (en clair)
     * @return true si la modification a réussi, false sinon
     */
    public boolean modifierMotDePasse(String email, String nouveauMotDePasse) {
        if (!estDonneesValides(email, nouveauMotDePasse)) {
            return false;
        }
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_MDP)) {
            
            stmt.setString(1, nouveauMotDePasse);
            stmt.setString(2, email);
            
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
            
        } catch (SQLException e) {
            gererExceptionSQL(e, "modification du mot de passe");
            return false;
        }
    }
    
    /**
     * Modifie le mot de passe avec hachage (version sécurisée)
     * @param email Email de l'utilisateur
     * @param nouveauMotDePasse Nouveau mot de passe (en clair)
     * @return true si la modification a réussi
     */
    public boolean modifierMotDePasseSecurise(String email, String nouveauMotDePasse) {
        String motDePasseHache = hacherMotDePasse(nouveauMotDePasse);
        return modifierMotDePasse(email, motDePasseHache);
    }
    
    /**
     * Vérifie si un email existe dans la base
     * @param email Email à vérifier
     * @return true si l'email existe
     */
    public boolean verifierEmailExiste(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(VERIFIER_EMAIL)) {
            
            stmt.setString(1, email.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            gererExceptionSQL(e, "vérification de l'email");
            return false;
        }
    }
    
    /**
     * Vérifie l'ancien mot de passe d'un utilisateur
     * @param email Email de l'utilisateur
     * @param ancienMotDePasse Ancien mot de passe à vérifier
     * @return true si l'ancien mot de passe est correct
     */
    public boolean verifierAncienMotDePasse(String email, String ancienMotDePasse) {
        if (!estDonneesValides(email, ancienMotDePasse)) {
            return false;
        }
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(VERIFIER_ANCIEN_MDP)) {
            
            stmt.setString(1, email);
            stmt.setString(2, ancienMotDePasse);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            gererExceptionSQL(e, "vérification de l'ancien mot de passe");
            return false;
        }
    }
    
    /**
     * Récupère l'ID de l'utilisateur par son email
     * @param email Email de l'utilisateur
     * @return ID de l'utilisateur ou -1 si non trouvé
     */
    public int getIdUsagerByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return -1;
        }
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_ID_BY_EMAIL)) {
            
            stmt.setString(1, email.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_usager");
                }
            }
            
        } catch (SQLException e) {
            gererExceptionSQL(e, "récupération de l'ID utilisateur");
        }
        
        return -1;
    }
    
    /**
     * Récupère les informations de l'utilisateur
     * @param email Email de l'utilisateur
     * @return Tableau [nom, prénom] ou null si non trouvé
     */
    public String[] getInfosUsager(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_INFOS_USAGER)) {
            
            stmt.setString(1, email.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String nom = rs.getString("nom_usager");
                    String prenom = rs.getString("prenom_usager");
                    return new String[]{nom, prenom};
                }
            }
            
        } catch (SQLException e) {
            gererExceptionSQL(e, "récupération des informations utilisateur");
        }
        
        return null;
    }
    
    /**
     * Vérifie la validité des données avant traitement
     */
    private boolean estDonneesValides(String email, String motDePasse) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        if (motDePasse == null || motDePasse.trim().isEmpty()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Vérifie la force du mot de passe
     * @param motDePasse Mot de passe à vérifier
     * @return true si le mot de passe est assez fort
     */
    public boolean verifierForceMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < LONGUEUR_MIN_MDP) {
            return false;
        }
        
        // Calcul des critères
        int criteresValides = 0;
        
        if (contientMajuscule(motDePasse)) criteresValides++;
        if (contientMinuscule(motDePasse)) criteresValides++;
        if (contientChiffre(motDePasse)) criteresValides++;
        if (contientCaractereSpecial(motDePasse)) criteresValides++;
        
        // Au moins 3 critères sur 4 doivent être respectés
        return criteresValides >= 3;
    }
    
    /**
     * Méthode de hachage de mot de passe (à implémenter selon les besoins)
     */
    private String hacherMotDePasse(String motDePasse) {
        // À implémenter avec BCrypt, Argon2, ou autre algorithme sécurisé
        // Pour l'instant, retourne le mot de passe en clair (À CHANGER)
        return motDePasse;
    }
    
    // Méthodes de validation des critères de mot de passe
    private boolean contientMajuscule(String texte) {
        return !texte.equals(texte.toLowerCase());
    }
    
    private boolean contientMinuscule(String texte) {
        return !texte.equals(texte.toUpperCase());
    }
    
    private boolean contientChiffre(String texte) {
        return texte.matches(".*\\d.*");
    }
    
    private boolean contientCaractereSpecial(String texte) {
        return texte.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    }
    
    /**
     * Gère les exceptions SQL de manière centralisée
     */
    private void gererExceptionSQL(SQLException e, String operation) {
        // Log l'erreur (à adapter selon votre système de logging)
        System.err.println("Erreur lors de " + operation + ": " + e.getMessage());
        
        // Vous pourriez aussi :
        // 1. Journaliser dans un fichier log
        // 2. Envoyer une notification
        // 3. Relancer une exception personnalisée
        // 4. etc.
        
        // Pour l'instant, on se contente d'afficher la stack trace
        e.printStackTrace();
    }
    
    /**
     * Récupère le statut de l'utilisateur
     * @param email Email de l'utilisateur
     * @return Statut de l'utilisateur ou null si non trouvé
     */
    public String getStatutUsager(String email) {
        String sql = "SELECT statut FROM Usager WHERE mail_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("statut");
                }
            }
            
        } catch (SQLException e) {
            gererExceptionSQL(e, "récupération du statut utilisateur");
        }
        
        return null;
    }
    
    /**
     * Met à jour la date de dernière modification du mot de passe
     * @param email Email de l'utilisateur
     * @return true si la mise à jour a réussi
     */
    public boolean mettreAJourDateModificationMDP(String email) {
        String sql = "UPDATE Usager SET date_modification_mdp = CURRENT_TIMESTAMP WHERE mail_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
            
        } catch (SQLException e) {
            gererExceptionSQL(e, "mise à jour de la date de modification MDP");
            return false;
        }
    }
}
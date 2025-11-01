package dao;

import modèle.Paiement;
import modèle.Usager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaiementDAO {
    
    /**
     * Enregistre un nouveau paiement dans la base de données
     * @param paiement l'objet Paiement contenant toutes les informations du paiement
     * @return true si l'enregistrement a réussi, false sinon
     */
    public static boolean enregistrerPaiement(Paiement paiement) {
        // Requête SQL pour insérer un nouveau paiement
        String sql = "INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, id_abonnement, montant, id_usager, date_paiement) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Remplissage des paramètres de la requête avec les données du paiement
            stmt.setString(1, paiement.getIdPaiement());          // ID unique du paiement
            stmt.setString(2, paiement.getNomCarte());            // Nom sur la carte
            stmt.setString(3, paiement.getNumeroCarte());         // Numéro de la carte
            stmt.setString(4, paiement.getCodeSecretCarte());     // Code secret (CVV)
            stmt.setString(5, paiement.getIdAbonnement());        // Type d'abonnement
            stmt.setDouble(6, paiement.getMontant());             // Montant du paiement
            stmt.setInt(7, paiement.getIdUsager());               // ID de l'usager
            
            // Exécution de la requête et vérification du résultat
            int ligneinseree = stmt.executeUpdate();
            return ligneinseree > 0; // Retourne true si au moins une ligne a été insérée
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement du paiement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Crée un stationnement après un paiement réussi
     * Cette méthode est appelée après la validation d'un paiement
     * @param idUsager ID de l'utilisateur qui effectue le stationnement
     * @param typeVehicule Type de véhicule (voiture, moto, camion)
     * @param plaqueImmatriculation Plaque d'immatriculation du véhicule
     * @param zone Zone de stationnement choisie
     * @param dureeHeures Durée en heures du stationnement
     * @param dureeMinutes Durée en minutes du stationnement
     * @param cout Coût total du stationnement
     * @param idPaiement ID du paiement associé à ce stationnement
     * @return true si la création a réussi, false sinon
     */
    public static boolean creerStationnementApresPaiement(int idUsager, String typeVehicule, 
            String plaqueImmatriculation, String zone, int dureeHeures, int dureeMinutes, 
            double cout, String idPaiement) {
        
        // Requête SQL pour créer un nouveau stationnement
        // DATE_ADD(NOW(), INTERVAL ? MINUTE) calcule la date de fin automatiquement
        String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, zone, duree_heures, duree_minutes, cout, date_creation, date_fin, statut, id_paiement) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? MINUTE), 'ACTIF', ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Calcul de la durée totale en minutes pour la date de fin
            int dureeTotaleMinutes = (dureeHeures * 60) + dureeMinutes;
            
            // Remplissage des paramètres de la requête
            stmt.setInt(1, idUsager);                     // ID de l'utilisateur
            stmt.setString(2, typeVehicule);              // Type de véhicule
            stmt.setString(3, plaqueImmatriculation);     // Plaque d'immatriculation
            stmt.setString(4, zone);                      // Zone de stationnement
            stmt.setInt(5, dureeHeures);                  // Durée en heures
            stmt.setInt(6, dureeMinutes);                 // Durée en minutes
            stmt.setDouble(7, cout);                      // Coût total
            stmt.setInt(8, dureeTotaleMinutes);           // Durée totale en minutes pour DATE_ADD
            stmt.setString(9, idPaiement);                // ID du paiement associé
            
            // Exécution de la requête
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0; // Retourne true si l'insertion a réussi
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création du stationnement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère tous les paiements d'un utilisateur spécifique
     * Cette méthode est utilisée pour afficher l'historique des paiements
     * @param idUsager ID de l'utilisateur dont on veut l'historique
     * @return Liste des paiements triés du plus récent au plus ancien
     */
    public static List<Paiement> getPaiementsByUsager(int idUsager) {
        List<Paiement> paiements = new ArrayList<>();
        
        // Requête SQL pour récupérer les paiements d'un utilisateur
        // ORDER BY id_paiement DESC pour avoir les plus récents en premier
        String sql = "SELECT * FROM Paiement WHERE id_usager = ? ORDER BY id_paiement DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager); // Paramètre : ID de l'utilisateur
            
            try (ResultSet rs = stmt.executeQuery()) {
                // Parcours de tous les résultats de la requête
                while (rs.next()) {
                    // Création d'un objet Paiement à partir des données de la base
                    Paiement paiement = new Paiement(
                        rs.getString("nom_carte"),         // Nom sur la carte
                        rs.getString("numero_carte"),      // Numéro de carte
                        rs.getString("code_secret_carte"), // Code secret
                        rs.getDouble("montant"),           // Montant payé
                        rs.getInt("id_usager")             // ID utilisateur
                    );
                    
                    // Attribution des propriétés supplémentaires
                    paiement.setIdPaiement(rs.getString("id_paiement"));      // ID unique
                    paiement.setIdAbonnement(rs.getString("id_abonnement"));  // Type d'abonnement
                    
                    // Ajout du paiement à la liste
                    paiements.add(paiement);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des paiements: " + e.getMessage());
            e.printStackTrace();
        }
        return paiements; // Retourne la liste des paiements (vide si erreur ou aucun paiement)
    }
}
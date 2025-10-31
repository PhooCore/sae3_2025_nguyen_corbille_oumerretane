package dao;

import modèle.Paiement;
import modèle.Usager;
import java.sql.*;

public class PaiementDAO {
    
    public static boolean enregistrerPaiement(Paiement paiement) {
        String sql = "INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, id_abonnement, montant, id_usager) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, paiement.getIdPaiement());
            stmt.setString(2, paiement.getNomCarte());
            stmt.setString(3, paiement.getNumeroCarte());
            stmt.setString(4, paiement.getCodeSecretCarte());
            stmt.setString(5, paiement.getIdAbonnement());
            stmt.setDouble(6, paiement.getMontant());
            stmt.setInt(7, paiement.getIdUsager()); // Ajout de l'ID utilisateur
            
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement du paiement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Méthode pour créer un stationnement après paiement
    public static boolean creerStationnementApresPaiement(int idUsager, String typeVehicule, 
            String plaqueImmatriculation, String zone, int dureeHeures, int dureeMinutes, 
            double cout, String idPaiement) {
        
        String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, zone, duree_heures, duree_minutes, cout, date_creation, statut, id_paiement) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), 'ACTIF', ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            stmt.setString(2, typeVehicule);
            stmt.setString(3, plaqueImmatriculation);
            stmt.setString(4, zone);
            stmt.setInt(5, dureeHeures);
            stmt.setInt(6, dureeMinutes);
            stmt.setDouble(7, cout);
            stmt.setString(8, idPaiement);
            
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création du stationnement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
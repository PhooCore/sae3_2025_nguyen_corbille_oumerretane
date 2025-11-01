package dao;

import modèle.Tarif;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TarifDAO {
    
    /**
     * Récupère tous les tarifs de stationnement depuis la base de données
     * Cette méthode est utilisée pour peupler la liste déroulante des zones dans Page_Garer_Voirie
     * 
     * @return Une liste contenant tous les objets Tarif disponibles
     */
    public static List<Tarif> TouslesTarifs() {
        // Initialisation de la liste qui contiendra les tarifs
        List<Tarif> tarifs = new ArrayList<>();
        
        // Requête SQL pour sélectionner toutes les tarifications
        // On récupère l'identifiant, le tarif horaire et la durée maximale
        String sql = "SELECT id_tarification, tarif_par_heure, duree_max FROM Tarification";
        
        try (
            // Établissement de la connexion à la base de données
            Connection conn = MySQLConnection.getConnection();
            
            // Création de la requête préparée (sans paramètres ici)
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            // Exécution de la requête et récupération du résultat
            ResultSet rs = stmt.executeQuery()
        ) {
            // Parcours de tous les enregistrements retournés par la requête
            while (rs.next()) {
                // Extraction des données de chaque ligne du ResultSet
                
                // Récupère l'identifiant de la tarification (ex: "TARIF_JAUNE")
                String idTarification = rs.getString("id_tarification");
                
                // Récupère le tarif horaire en euros (ex: 1.50)
                double tarifParHeure = rs.getDouble("tarif_par_heure");
                
                // Récupère la durée maximale sous forme d'objet Time SQL
                Time dureeMaxTime = rs.getTime("duree_max");
                
                // Conversion du Time SQL en LocalTime Java pour une manipulation plus facile
                LocalTime dureeMax = dureeMaxTime.toLocalTime();
                
                // Création d'un nouvel objet Tarif avec les données récupérées
                Tarif tarif = new Tarif(idTarification, tarifParHeure, dureeMax);
                
                // Ajout du tarif à la liste
                tarifs.add(tarif);
            }
            
        } catch (SQLException e) {
            // Gestion des erreurs de base de données
            System.err.println("Erreur lors de la récupération des tarifs: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Retourne la liste des tarifs (peut être vide en cas d'erreur ou si aucun tarif n'existe)
        return tarifs;
    }
}
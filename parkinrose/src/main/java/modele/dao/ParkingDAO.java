package modele.dao;

import modele.Parking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingDAO {
    
    /**
     * Récupère tous les parkings avec leurs positions
     */
    public static List<Parking> getAllParkings() {
        List<Parking> parkings = new ArrayList<>();
        
        String sql = "SELECT id_parking, libelle_parking, adresse_parking, " +
                    "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                    "has_moto, places_moto, places_moto_disponibles, est_relais, " +
                    "position_x, position_y FROM Parking " +
                    "ORDER BY libelle_parking";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Parking parking = new Parking(
                    rs.getString("id_parking"),
                    rs.getString("libelle_parking"),
                    rs.getString("adresse_parking"),
                    rs.getInt("nombre_places"),
                    rs.getInt("places_disponibles"), 
                    rs.getDouble("hauteur_parking"),
                    rs.getBoolean("tarif_soiree"),
                    rs.getBoolean("has_moto"),
                    rs.getInt("places_moto"),
                    rs.getInt("places_moto_disponibles"),
                    rs.getBoolean("est_relais")
                );
                
                // Ajouter les positions si elles existent
                try {
                    Float posX = rs.getFloat("position_x");
                    Float posY = rs.getFloat("position_y");
                    if (!rs.wasNull()) {
                        parking.setPositionX(posX);
                        parking.setPositionY(posY);
                    }
                } catch (SQLException e) {
                    // Les colonnes position_x et position_y n'existent pas encore
                    // On continue sans les positions
                    System.out.println("Les colonnes position_x/position_y n'existent pas encore");
                }
                
                parkings.add(parking);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des parkings: " + e.getMessage());
            e.printStackTrace();
            
        }
        
        return parkings;
    }
    
    /**
     * Récupère un parking par son ID avec sa position
     */
    public static Parking getParkingById(String idParking) {
        String sql = "SELECT id_parking, libelle_parking, adresse_parking, " +
                    "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                    "has_moto, places_moto, places_moto_disponibles, est_relais, " +
                    "position_x, position_y FROM Parking " +
                    "WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Parking parking = new Parking(
                        rs.getString("id_parking"),
                        rs.getString("libelle_parking"),
                        rs.getString("adresse_parking"),
                        rs.getInt("nombre_places"),
                        rs.getInt("places_disponibles"),
                        rs.getDouble("hauteur_parking"),
                        rs.getBoolean("tarif_soiree"),
                        rs.getBoolean("has_moto"),
                        rs.getInt("places_moto"),
                        rs.getInt("places_moto_disponibles"),
                        rs.getBoolean("est_relais")
                    );
                    
                    // Ajouter les positions si elles existent
                    Float posX = rs.getFloat("position_x");
                    Float posY = rs.getFloat("position_y");
                    if (!rs.wasNull()) {
                        parking.setPositionX(posX);
                        parking.setPositionY(posY);
                    }
                    
                    return parking;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du parking: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Recherche des parkings par terme
     */
    public static List<Parking> rechercherParkings(String terme) {
        List<Parking> parkings = new ArrayList<>();
        String sql = "SELECT id_parking, libelle_parking, adresse_parking, " +
                    "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                    "has_moto, places_moto, places_moto_disponibles FROM Parking " +
                    "WHERE libelle_parking LIKE ? OR adresse_parking LIKE ? " +
                    "ORDER BY libelle_parking";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String termeRecherche = "%" + terme + "%";
            stmt.setString(1, termeRecherche);
            stmt.setString(2, termeRecherche);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Parking parking = new Parking(
                        rs.getString("id_parking"),
                        rs.getString("libelle_parking"),
                        rs.getString("adresse_parking"),
                        rs.getInt("nombre_places"),
                        rs.getInt("places_disponibles"), 
                        rs.getDouble("hauteur_parking"),
                        rs.getBoolean("tarif_soiree"),
                        rs.getBoolean("has_moto"),
                        rs.getInt("places_moto"),
                        rs.getInt("places_moto_disponibles")
                    );
                    parkings.add(parking);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des parkings: " + e.getMessage());
            e.printStackTrace();
        }
        
        return parkings;
    }
    
    /**
     * Décrémente le nombre de places disponibles d'un parking (voiture/camion)
     */
    public static boolean decrementerPlacesDisponibles(String idParking) {
        String sql = "UPDATE Parking SET places_disponibles = places_disponibles - 1 " +
                    "WHERE id_parking = ? AND places_disponibles > 0";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la décrémentation des places: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Incrémente le nombre de places disponibles d'un parking (voiture/camion)
     */
    public static boolean incrementerPlacesDisponibles(String idParking) {
        String sql = "UPDATE Parking SET places_disponibles = places_disponibles + 1 " +
                    "WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'incrémentation des places: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Décrémente le nombre de places moto disponibles
     */
    public static boolean decrementerPlacesMotoDisponibles(String idParking) {
        String sql = "UPDATE Parking SET places_moto_disponibles = places_moto_disponibles - 1 " +
                    "WHERE id_parking = ? AND has_moto = TRUE AND places_moto_disponibles > 0";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la décrémentation des places moto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Incrémente le nombre de places moto disponibles
     */
    public static boolean incrementerPlacesMotoDisponibles(String idParking) {
        String sql = "UPDATE Parking SET places_moto_disponibles = places_moto_disponibles + 1 " +
                    "WHERE id_parking = ? AND has_moto = TRUE";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'incrémentation des places moto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère le nombre de places disponibles actuel (voiture/camion)
     */
    public static int getPlacesDisponibles(String idParking) {
        String sql = "SELECT places_disponibles FROM Parking WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("places_disponibles");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération places disponibles: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Récupère le nombre de places moto disponibles
     */
    public static int getPlacesMotoDisponibles(String idParking) {
        String sql = "SELECT places_moto_disponibles FROM Parking WHERE id_parking = ? AND has_moto = TRUE";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("places_moto_disponibles");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération places moto disponibles: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Vérifie si un parking a des places moto disponibles
     */
    public static boolean hasPlacesMotoDisponibles(String idParking) {
        String sql = "SELECT places_moto_disponibles FROM Parking WHERE id_parking = ? AND has_moto = TRUE";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("places_moto_disponibles") > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur vérification places moto: " + e.getMessage());
        }
        
        return false;
    }
    /**
     * Ajoute un nouveau parking avec position
     */
    public static boolean ajouterParking(Parking parking) {
        // Vérifier si les colonnes position_x/position_y existent
        boolean hasPositions = columnExists("Parking", "position_x") && columnExists("Parking", "position_y");
        boolean hasEstRelais = columnExists("Parking", "est_relais");
        
        StringBuilder sql = new StringBuilder();
        
        if (hasEstRelais && hasPositions) {
            sql.append("INSERT INTO Parking (id_parking, libelle_parking, adresse_parking, ")
               .append("nombre_places, places_disponibles, hauteur_parking, tarif_soiree, ")
               .append("has_moto, places_moto, places_moto_disponibles, est_relais, position_x, position_y) ")
               .append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        } else if (hasEstRelais) {
            sql.append("INSERT INTO Parking (id_parking, libelle_parking, adresse_parking, ")
               .append("nombre_places, places_disponibles, hauteur_parking, tarif_soiree, ")
               .append("has_moto, places_moto, places_moto_disponibles, est_relais) ")
               .append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        } else {
            sql.append("INSERT INTO Parking (id_parking, libelle_parking, adresse_parking, ")
               .append("nombre_places, places_disponibles, hauteur_parking, tarif_soiree, ")
               .append("has_moto, places_moto, places_moto_disponibles) ")
               .append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        }
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            stmt.setString(1, parking.getIdParking());
            stmt.setString(2, parking.getLibelleParking());
            stmt.setString(3, parking.getAdresseParking());
            stmt.setInt(4, parking.getNombrePlaces());
            stmt.setInt(5, parking.getPlacesDisponibles());
            stmt.setDouble(6, parking.getHauteurParking());
            stmt.setBoolean(7, parking.hasTarifSoiree());
            stmt.setBoolean(8, parking.hasMoto());
            stmt.setInt(9, parking.getPlacesMoto());
            stmt.setInt(10, parking.getPlacesMotoDisponibles());
            
            int paramIndex = 11;
            
            if (hasEstRelais) {
                stmt.setBoolean(paramIndex++, parking.isEstRelais());
            }
            
            if (hasPositions) {
                if (parking.getPositionX() != null && parking.getPositionY() != null) {
                    stmt.setFloat(paramIndex++, parking.getPositionX());
                    stmt.setFloat(paramIndex++, parking.getPositionY());
                } else {
                    stmt.setNull(paramIndex++, java.sql.Types.FLOAT);
                    stmt.setNull(paramIndex++, java.sql.Types.FLOAT);
                }
            }
            
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du parking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Vérifie si une colonne existe dans une table
     */
    private static boolean columnExists(String tableName, String columnName) {
        String sql = "SELECT COUNT(*) as count FROM information_schema.columns " +
                    "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification colonne " + columnName + ": " + e.getMessage());
        }
        return false;
    }
    public static boolean mettreAJourParking(Parking parking) {
        // Vérifier si les colonnes existent
        boolean hasPositions = columnExists("Parking", "position_x") && columnExists("Parking", "position_y");
        boolean hasEstRelais = columnExists("Parking", "est_relais");
        
        StringBuilder sql = new StringBuilder("UPDATE Parking SET ")
            .append("libelle_parking = ?, ")
            .append("adresse_parking = ?, ")
            .append("nombre_places = ?, ")
            .append("places_disponibles = ?, ")
            .append("hauteur_parking = ?, ")
            .append("tarif_soiree = ?, ")
            .append("has_moto = ?, ")
            .append("places_moto = ?, ")
            .append("places_moto_disponibles = ?");
        
        if (hasEstRelais) {
            sql.append(", est_relais = ?");
        }
        
        if (hasPositions) {
            sql.append(", position_x = ?, position_y = ?");
        }
        
        sql.append(" WHERE id_parking = ?");
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            stmt.setString(paramIndex++, parking.getLibelleParking());
            stmt.setString(paramIndex++, parking.getAdresseParking());
            stmt.setInt(paramIndex++, parking.getNombrePlaces());
            stmt.setInt(paramIndex++, parking.getPlacesDisponibles());
            stmt.setDouble(paramIndex++, parking.getHauteurParking());
            stmt.setBoolean(paramIndex++, parking.hasTarifSoiree());
            stmt.setBoolean(paramIndex++, parking.hasMoto());
            stmt.setInt(paramIndex++, parking.getPlacesMoto());
            stmt.setInt(paramIndex++, parking.getPlacesMotoDisponibles());
            
            if (hasEstRelais) {
                stmt.setBoolean(paramIndex++, parking.isEstRelais());
            }
            
            if (hasPositions) {
                if (parking.getPositionX() != null && parking.getPositionY() != null) {
                    stmt.setFloat(paramIndex++, parking.getPositionX());
                    stmt.setFloat(paramIndex++, parking.getPositionY());
                } else {
                    stmt.setNull(paramIndex++, java.sql.Types.FLOAT);
                    stmt.setNull(paramIndex++, java.sql.Types.FLOAT);
                }
            }
            
            stmt.setString(paramIndex, parking.getIdParking());
            
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du parking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Met à jour uniquement la position d'un parking
     */
    public static boolean mettreAJourPositionParking(String idParking, float positionX, float positionY) {
        if (!columnExists("Parking", "position_x") || !columnExists("Parking", "position_y")) {
            System.err.println("Les colonnes position_x/position_y n'existent pas");
            return false;
        }
        
        String sql = "UPDATE Parking SET position_x = ?, position_y = ? WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setFloat(1, positionX);
            stmt.setFloat(2, positionY);
            stmt.setString(3, idParking);
            
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la position: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    

    
    /**
     * Supprime un parking par son ID
     */
    public static boolean supprimerParking(String idParking) {
        // D'abord vérifier s'il y a des stationnements en cours dans ce parking
        String verifSql = "SELECT COUNT(*) as count FROM Stationnement " +
                         "WHERE id_parking = ? AND date_fin IS NULL";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement verifStmt = conn.prepareStatement(verifSql)) {
            
            verifStmt.setString(1, idParking);
            ResultSet rs = verifStmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") > 0) {
                System.err.println("Impossible de supprimer: stationnements en cours");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur vérification stationnements: " + e.getMessage());
            return false;
        }
        
        // Supprimer le parking
        String sql = "DELETE FROM Parking WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du parking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Vérifie si un ID parking existe déjà
     */
    public static boolean idParkingExiste(String idParking) {
        String sql = "SELECT COUNT(*) as count FROM Parking WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur vérification ID parking: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Génère un nouvel ID parking unique
     */
 // Dans la classe ParkingDAO
    public static String genererNouvelIdParking() {
        // Ancienne méthode - gardée pour compatibilité
        return "PARK_" + System.currentTimeMillis();
    }

    // Nouvelle méthode pour générer un ID basé sur le nom
    public static String genererIdDepuisNom(String nom) {
        if (nom == null || nom.isEmpty()) {
            return genererNouvelIdParking();
        }
        
        String id = nom.toUpperCase()
            .replaceAll(" ", "_")
            .replaceAll("[^A-Z0-9_]", "")
            .replaceAll("__+", "_");
        
        if (!id.startsWith("PARK_")) {
            id = "PARK_" + id;
        }
        
        // Vérifier et rendre unique si nécessaire
        String baseId = id;
        int counter = 1;
        
        while (idParkingExiste(id)) {
            id = baseId + "_" + counter;
            counter++;
            if (counter > 1000) {
                return genererNouvelIdParking();
            }
        }
        
        return id;
    }
    
    /**
     * Recherche les parkings avec filtres avancés
     */
    public static List<Parking> rechercherParkingsAvances(String terme, 
                                                         boolean avecTarifSoiree, 
                                                         boolean avecMoto, 
                                                         int placesMin) {
        List<Parking> parkings = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder(
            "SELECT id_parking, libelle_parking, adresse_parking, " +
            "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
            "has_moto, places_moto, places_moto_disponibles FROM Parking " +
            "WHERE 1=1"
        );
        
        List<Object> parametres = new ArrayList<>();
        
        if (terme != null && !terme.trim().isEmpty()) {
            sql.append(" AND (libelle_parking LIKE ? OR adresse_parking LIKE ?)");
            String termeRecherche = "%" + terme + "%";
            parametres.add(termeRecherche);
            parametres.add(termeRecherche);
        }
        
        if (avecTarifSoiree) {
            sql.append(" AND tarif_soiree = TRUE");
        }
        
        if (avecMoto) {
            sql.append(" AND has_moto = TRUE AND places_moto_disponibles > 0");
        }
        
        if (placesMin > 0) {
            sql.append(" AND places_disponibles >= ?");
            parametres.add(placesMin);
        }
        
        sql.append(" ORDER BY libelle_parking");
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < parametres.size(); i++) {
                stmt.setObject(i + 1, parametres.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Parking parking = new Parking(
                        rs.getString("id_parking"),
                        rs.getString("libelle_parking"),
                        rs.getString("adresse_parking"),
                        rs.getInt("nombre_places"),
                        rs.getInt("places_disponibles"),
                        rs.getDouble("hauteur_parking"),
                        rs.getBoolean("tarif_soiree"),
                        rs.getBoolean("has_moto"),
                        rs.getInt("places_moto"),
                        rs.getInt("places_moto_disponibles")
                    );
                    parkings.add(parking);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche avancée: " + e.getMessage());
            e.printStackTrace();
        }
        
        return parkings;
    }
    
    /**
     * Récupère les statistiques des parkings
     */
    public static java.util.Map<String, Object> getStatistiquesParkings() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        String sqlTotal = "SELECT COUNT(*) as total, " +
                         "SUM(nombre_places) as capacite_totale, " +
                         "SUM(places_disponibles) as disponibles_totales, " +
                         "SUM(CASE WHEN has_moto = TRUE THEN 1 ELSE 0 END) as avec_moto " +
                         "FROM Parking";
        
        String sqlMoyenne = "SELECT AVG(places_disponibles) as moyenne_dispo, " +
                           "AVG(nombre_places) as moyenne_capacite " +
                           "FROM Parking";
        
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Statistiques totales
            ResultSet rsTotal = stmt.executeQuery(sqlTotal);
            if (rsTotal.next()) {
                stats.put("total", rsTotal.getInt("total"));
                stats.put("capacite_totale", rsTotal.getInt("capacite_totale"));
                stats.put("disponibles_totales", rsTotal.getInt("disponibles_totales"));
                stats.put("avec_moto", rsTotal.getInt("avec_moto"));
                
                // Calculer le taux d'occupation
                int capacite = rsTotal.getInt("capacite_totale");
                int dispo = rsTotal.getInt("disponibles_totales");
                if (capacite > 0) {
                    double tauxOccupation = ((capacite - dispo) * 100.0) / capacite;
                    stats.put("taux_occupation", Math.round(tauxOccupation * 100.0) / 100.0);
                }
            }
            
            // Statistiques moyennes
            ResultSet rsMoyenne = stmt.executeQuery(sqlMoyenne);
            if (rsMoyenne.next()) {
                stats.put("moyenne_dispo", Math.round(rsMoyenne.getDouble("moyenne_dispo") * 100.0) / 100.0);
                stats.put("moyenne_capacite", Math.round(rsMoyenne.getDouble("moyenne_capacite") * 100.0) / 100.0);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * Met à jour toutes les places disponibles (pour simulation ou reset)
     */
    public static boolean mettreAJourToutesPlacesDisponibles() {
        String sql = "UPDATE Parking SET " +
                    "places_disponibles = FLOOR(RAND() * nombre_places), " +
                    "places_moto_disponibles = CASE " +
                    "WHEN has_moto = TRUE THEN FLOOR(RAND() * places_moto) " +
                    "ELSE 0 END";
        
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            int lignesAffectees = stmt.executeUpdate(sql);
            System.out.println("Mise à jour aléatoire effectuée sur " + lignesAffectees + " parkings");
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour places: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
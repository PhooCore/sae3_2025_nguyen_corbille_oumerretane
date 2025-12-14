package modele.dao;

import modele.Parking;
import modele.Stationnement;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class StationnementDAO {

    /**
     * Récupère le stationnement actif d'un usager
     */
    public static Stationnement getStationnementActifByUsager(int idUsager) {
        String sql = "SELECT s.* FROM Stationnement s " +
                    "WHERE s.id_usager = ? AND s.statut = 'ACTIF' " +
                    "ORDER BY s.date_creation DESC LIMIT 1";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToStationnement(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération stationnement actif: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Récupère le stationnement actif valide d'un usager (vérifie aussi les dates)
     */
    public static Stationnement getStationnementActifValideByUsager(int idUsager) {
        String sql = "SELECT s.* FROM Stationnement s " +
                    "WHERE s.id_usager = ? AND s.statut = 'ACTIF' " +
                    "AND (s.date_fin IS NULL OR s.date_fin > NOW()) " +
                    "ORDER BY s.date_creation DESC LIMIT 1";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToStationnement(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération stationnement actif valide: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Termine un stationnement (pour voirie)
     */
    public static boolean terminerStationnement(int idStationnement) {
        String sql = "UPDATE Stationnement SET statut = 'TERMINE', date_fin = NOW() WHERE id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idStationnement);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur terminaison stationnement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Termine un stationnement parking et libère les places selon le type de véhicule
     */
    public static boolean terminerStationnementParking(int idStationnement, LocalDateTime heureDepart, 
                                                      double cout, String idPaiement) {
        
        Connection conn = null;
        try {
            conn = MySQLConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Récupérer les infos du stationnement
            String sqlSelect = "SELECT s.*, p.has_moto FROM Stationnement s " +
                              "LEFT JOIN Parking p ON s.id_parking = p.id_parking " +
                              "WHERE s.id_stationnement = ?";
            
            String idParking = null;
            String typeVehicule = null;
            boolean hasMoto = false;
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
                stmt.setInt(1, idStationnement);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    idParking = rs.getString("id_parking");
                    typeVehicule = rs.getString("type_vehicule");
                    hasMoto = rs.getBoolean("has_moto");
                } else {
                    conn.rollback();
                    return false;
                }
            }
            
            if (idParking == null) {
                conn.rollback();
                return false;
            }
            
            // 2. Mettre à jour le stationnement
            String sqlUpdate = "UPDATE Stationnement SET statut = 'TERMINE', heure_depart = ?, cout = ?, " +
                              "id_paiement = ?, statut_paiement = 'PAYE' WHERE id_stationnement = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setTimestamp(1, Timestamp.valueOf(heureDepart));
                stmt.setDouble(2, cout);
                stmt.setString(3, idPaiement);
                stmt.setInt(4, idStationnement);
                
                int lignesAffectees = stmt.executeUpdate();
                
                if (lignesAffectees > 0) {
                    // 3. Libérer les places selon le type de véhicule
                    boolean isMoto = "Moto".equalsIgnoreCase(typeVehicule);
                    boolean placesLiberees;
                    
                    if (isMoto && hasMoto) {
                        placesLiberees = ParkingDAO.incrementerPlacesMotoDisponibles(idParking);
                    } else {
                        placesLiberees = ParkingDAO.incrementerPlacesDisponibles(idParking);
                    }
                    
                    if (placesLiberees) {
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                } else {
                    conn.rollback();
                    return false;
                }
            }
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Erreur lors du rollback: " + ex.getMessage());
            }
            System.err.println("Erreur terminaison stationnement parking: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
    
    /**
     * Récupère l'historique des stationnements d'un usager
     */
    public static List<Stationnement> getHistoriqueStationnements(int idUsager) {
        List<Stationnement> stationnements = new ArrayList<>();
        String sql = "SELECT s.* FROM Stationnement s " +
                    "WHERE s.id_usager = ? ORDER BY s.date_creation DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Stationnement stationnement = mapResultSetToStationnement(rs);
                stationnements.add(stationnement);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération historique: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stationnements;
    }
    
    /**
     * Nettoie les stationnements expirés
     */
    public static void nettoyerStationnementsExpires() {
        String sql = "UPDATE Stationnement SET statut = 'EXPIRE' WHERE statut = 'ACTIF' AND " +
                    "date_fin IS NOT NULL AND date_fin < NOW()";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println(rowsUpdated + " stationnement(s) expiré(s) nettoyé(s)");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur nettoyage stationnements expirés: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Récupère un stationnement par son ID
     */
    public static Stationnement getStationnementById(int idStationnement) {
        String sql = "SELECT s.*, p.libelle_parking, z.libelle_zone " +
                    "FROM Stationnement s " +
                    "LEFT JOIN Parking p ON s.id_parking = p.id_parking " +
                    "LEFT JOIN Zone z ON s.id_zone = z.id_zone " +
                    "WHERE s.id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idStationnement);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToStationnement(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération stationnement par ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Vérifie si un usager a un stationnement actif (pour le contrôleur)
     */
    public static boolean hasStationnementActif(int idUsager) {
        String sql = "SELECT COUNT(*) FROM Stationnement WHERE id_usager = ? AND statut = 'ACTIF'";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur vérification stationnement actif: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Méthode utilitaire pour mapper un ResultSet vers un objet Stationnement
     */
    private static Stationnement mapResultSetToStationnement(ResultSet rs) throws SQLException {
        Stationnement stationnement = new Stationnement();
        
        stationnement.setIdStationnement(rs.getInt("id_stationnement"));
        stationnement.setIdUsager(rs.getInt("id_usager"));
        stationnement.setTypeVehicule(rs.getString("type_vehicule"));
        stationnement.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
        stationnement.setDureeHeures(rs.getInt("duree_heures"));
        stationnement.setDureeMinutes(rs.getInt("duree_minutes"));
        stationnement.setCout(rs.getDouble("cout"));
        
        // Gérer id_zone et id_parking
        String idZone = rs.getString("id_zone");
        String idParking = rs.getString("id_parking");
        
        if (idZone != null) {
            stationnement.setIdTarification(idZone);
        } else if (idParking != null) {
            stationnement.setIdTarification(idParking);
        }
        
        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            stationnement.setDateCreation(dateCreation.toLocalDateTime());
        }
        
        Timestamp dateFin = rs.getTimestamp("date_fin");
        if (dateFin != null) {
            stationnement.setDateFin(dateFin.toLocalDateTime());
        }
        
        Timestamp heureArrivee = rs.getTimestamp("heure_arrivee");
        if (heureArrivee != null) {
            stationnement.setHeureArrivee(heureArrivee.toLocalDateTime());
        }
        
        Timestamp heureDepart = rs.getTimestamp("heure_depart");
        if (heureDepart != null) {
            stationnement.setHeureDepart(heureDepart.toLocalDateTime());
        }
        
        stationnement.setStatut(rs.getString("statut"));
        stationnement.setTypeStationnement(rs.getString("type_stationnement"));
        stationnement.setStatutPaiement(rs.getString("statut_paiement"));
        stationnement.setIdPaiement(rs.getString("id_paiement"));
        
        return stationnement;
    }
    
    /**
     * Met à jour le statut de paiement d'un stationnement
     */
    public static boolean mettreAJourStatutPaiement(int idStationnement, String idPaiement, String statutPaiement) {
        String sql = "UPDATE Stationnement SET id_paiement = ?, statut_paiement = ? WHERE id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idPaiement);
            stmt.setString(2, statutPaiement);
            stmt.setInt(3, idStationnement);
            
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour statut paiement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère les stationnements actifs expirés
     */
    public static List<Stationnement> getStationnementsExpires() {
        List<Stationnement> stationnements = new ArrayList<>();
        String sql = "SELECT s.* FROM Stationnement s " +
                    "WHERE s.statut = 'ACTIF' AND s.date_fin IS NOT NULL AND s.date_fin < NOW()";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Stationnement stationnement = mapResultSetToStationnement(rs);
                stationnements.add(stationnement);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération stationnements expirés: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stationnements;
    }

    
    /**
     * Récupère les stationnements d'un usager filtrés par statut
     * @param idUsager ID de l'usager
     * @param statut Statut à filtrer (ACTIF, TERMINE, EXPIRE)
     * @return Liste des stationnements filtrés
     */
    public static List<Stationnement> getStationnementsParStatut(int idUsager, String statut) {
        List<Stationnement> stationnements = new ArrayList<>();
        String sql = "SELECT s.* FROM Stationnement s " +
                    "WHERE s.id_usager = ? AND s.statut = ? " +
                    "ORDER BY s.date_creation DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            stmt.setString(2, statut);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Stationnement stationnement = mapResultSetToStationnement(rs);
                    stationnements.add(stationnement);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération des stationnements par statut: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stationnements;
    }
    
    /**
     * Récupère l'historique des stationnements d'un usager avec possibilité de filtre par type
     * @param idUsager ID de l'usager
     * @param typeStationnement Type de stationnement (VOIRIE, PARKING, ou null pour tous)
     * @return Liste des stationnements
     */
    public static List<Stationnement> getHistoriqueStationnementsFiltre(int idUsager, String typeStationnement) {
        List<Stationnement> stationnements = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT s.* FROM Stationnement s WHERE s.id_usager = ?");
        
        if (typeStationnement != null && !typeStationnement.isEmpty()) {
            sql.append(" AND s.type_stationnement = ?");
        }
        
        sql.append(" ORDER BY s.date_creation DESC");
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            stmt.setInt(1, idUsager);
            
            if (typeStationnement != null && !typeStationnement.isEmpty()) {
                stmt.setString(2, typeStationnement);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Stationnement stationnement = mapResultSetToStationnement(rs);
                    stationnements.add(stationnement);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération historique filtré: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stationnements;
    }
    
    /**
     * Récupère les stationnements avec filtres multiples
     * @param idUsager ID de l'usager
     * @param statut Statut (ACTIF, TERMINE, EXPIRE) ou null pour tous
     * @param typeStationnement Type (VOIRIE, PARKING) ou null pour tous
     * @param dateDebut Date de début ou null
     * @param dateFin Date de fin ou null
     * @return Liste des stationnements filtrés
     */
    public static List<Stationnement> getStationnementsAvecFiltres(int idUsager, String statut, 
                                                                  String typeStationnement,
                                                                  LocalDateTime dateDebut, 
                                                                  LocalDateTime dateFin) {
        List<Stationnement> stationnements = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT s.* FROM Stationnement s WHERE s.id_usager = ?");
        List<Object> parametres = new ArrayList<>();
        parametres.add(idUsager);
        
        if (statut != null && !statut.isEmpty()) {
            sql.append(" AND s.statut = ?");
            parametres.add(statut);
        }
        
        if (typeStationnement != null && !typeStationnement.isEmpty()) {
            sql.append(" AND s.type_stationnement = ?");
            parametres.add(typeStationnement);
        }
        
        if (dateDebut != null) {
            sql.append(" AND s.date_creation >= ?");
            parametres.add(Timestamp.valueOf(dateDebut));
        }
        
        if (dateFin != null) {
            sql.append(" AND s.date_creation <= ?");
            parametres.add(Timestamp.valueOf(dateFin));
        }
        
        sql.append(" ORDER BY s.date_creation DESC");
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < parametres.size(); i++) {
                stmt.setObject(i + 1, parametres.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Stationnement stationnement = mapResultSetToStationnement(rs);
                    stationnements.add(stationnement);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération avec filtres: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stationnements;
    }
    
    /**
     * Récupère les statistiques des stationnements d'un usager
     * @param idUsager ID de l'usager
     * @return Tableau avec [total, actifs, voirie, parking, totalCout]
     */
    public static Object[] getStatistiquesStationnements(int idUsager) {
        String sql = "SELECT " +
                    "COUNT(*) as total, " +
                    "SUM(CASE WHEN statut = 'ACTIF' THEN 1 ELSE 0 END) as actifs, " +
                    "SUM(CASE WHEN type_stationnement = 'VOIRIE' THEN 1 ELSE 0 END) as voirie, " +
                    "SUM(CASE WHEN type_stationnement = 'PARKING' THEN 1 ELSE 0 END) as parking, " +
                    "COALESCE(SUM(cout), 0) as total_cout " +
                    "FROM Stationnement WHERE id_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[] {
                        rs.getInt("total"),
                        rs.getInt("actifs"),
                        rs.getInt("voirie"),
                        rs.getInt("parking"),
                        rs.getDouble("total_cout")
                    };
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération statistiques: " + e.getMessage());
            e.printStackTrace();
        }
        
        return new Object[] {0, 0, 0, 0, 0.0};
    }
    
    
    /**
     * Crée un nouveau stationnement en voirie
     */
    public static boolean creerStationnementVoirie(Stationnement stationnement) {
        String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, " +
                     "id_zone, duree_heures, duree_minutes, cout, " +
                     "statut, date_creation, date_fin, type_stationnement, statut_paiement, id_paiement) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), " +
                     "DATE_ADD(NOW(), INTERVAL ? MINUTE), 'VOIRIE', 'PAYE', ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, stationnement.getIdUsager());
            pstmt.setString(2, stationnement.getTypeVehicule());
            pstmt.setString(3, stationnement.getPlaqueImmatriculation());
            pstmt.setString(4, stationnement.getIdTarification()); // C'est l'ID de la zone
            pstmt.setInt(5, stationnement.getDureeHeures());
            pstmt.setInt(6, stationnement.getDureeMinutes());
            pstmt.setDouble(7, stationnement.getCout());
            pstmt.setString(8, "ACTIF");
            
            int dureeTotaleMinutes = (stationnement.getDureeHeures() * 60) + stationnement.getDureeMinutes();
            pstmt.setInt(9, dureeTotaleMinutes);
            
            pstmt.setString(10, stationnement.getIdPaiement());
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Stationnement voirie créé, lignes affectées: " + rowsAffected);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la création du stationnement voirie:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Termine un stationnement parking avec libération des places
     */
    public static boolean terminerStationnementParking(Integer idStationnement, 
                                                      LocalDateTime heureDepart, 
                                                      double cout, 
                                                      String idPaiement) {
        Connection conn = null;
        try {
            conn = MySQLConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Récupérer les infos du stationnement
            String sqlSelect = "SELECT s.*, p.has_moto FROM Stationnement s " +
                              "LEFT JOIN Parking p ON s.id_parking = p.id_parking " +
                              "WHERE s.id_stationnement = ?";
            
            String idParking = null;
            String typeVehicule = null;
            boolean hasMoto = false;
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
                stmt.setInt(1, idStationnement);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    idParking = rs.getString("id_parking");
                    typeVehicule = rs.getString("type_vehicule");
                    hasMoto = rs.getBoolean("has_moto");
                } else {
                    conn.rollback();
                    return false;
                }
            }
            
            if (idParking == null) {
                conn.rollback();
                return false;
            }
            
            // 2. Mettre à jour le stationnement 
            String sqlUpdate = "UPDATE Stationnement SET statut = 'TERMINE', heure_depart = ?, cout = ?, " +
                              "id_paiement = ?, statut_paiement = 'PAYE' WHERE id_stationnement = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setTimestamp(1, Timestamp.valueOf(heureDepart));
                stmt.setDouble(2, cout);
                stmt.setString(3, idPaiement);
                stmt.setInt(4, idStationnement);
                
                int lignesAffectees = stmt.executeUpdate();
                
                if (lignesAffectees > 0) {
                    // 3. Libérer les places selon le type de véhicule
                    boolean isMoto = "Moto".equalsIgnoreCase(typeVehicule);
                    boolean placesLiberees;
                    
                    if (isMoto && hasMoto) {
                        placesLiberees = ParkingDAO.incrementerPlacesMotoDisponibles(idParking);
                    } else {
                        placesLiberees = ParkingDAO.incrementerPlacesDisponibles(idParking);
                    }
                    
                    if (placesLiberees) {
                        conn.commit();
                        System.out.println("Stationnement parking terminé et places libérées");
                        return true;
                    } else {
                        conn.rollback();
                        System.err.println("Erreur lors de la libération des places");
                        return false;
                    }
                } else {
                    conn.rollback();
                    System.err.println("Aucune ligne affectée lors de la mise à jour");
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Erreur lors du rollback: " + ex.getMessage());
            }
            System.err.println("Erreur SQL lors de la terminaison du stationnement parking:");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
    
    /**
     * Crée un stationnement en parking avec gestion des places
     */
    public static boolean creerStationnementParking(int idUsager, String typeVehicule, String plaqueImmatriculation,
            String idParking, LocalDateTime heureArrivee) {

        Connection conn = null;
        try {
            conn = MySQLConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Récupérer les infos du parking
            Parking parking = ParkingDAO.getParkingById(idParking);
            if (parking == null) {
                JOptionPane.showMessageDialog(null, "Parking non trouvé", "Erreur", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Vérifier si c'est un parking gratuit 
            boolean estGratuit = TarifParkingDAO.estParkingGratuit(idParking);
            
            // Vérifier les places selon le type de véhicule
            boolean isMoto = "Moto".equalsIgnoreCase(typeVehicule);
            
            if (isMoto) {
                if (!parking.hasMoto()) {
                    JOptionPane.showMessageDialog(null, "Ce parking ne dispose pas de places pour les motos", 
                        "Parking non adapté", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                if (parking.getPlacesMotoDisponibles() <= 0) {
                    JOptionPane.showMessageDialog(null, 
                            "Plus de places moto disponibles dans ce parking",
                            "Parking complet", 
                            JOptionPane.WARNING_MESSAGE);
                        return false;	
                }
                // Décrémenter places moto
                boolean placesDecrementees = ParkingDAO.decrementerPlacesMotoDisponibles(idParking);
                if (!placesDecrementees) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(null, "Erreur lors de la réservation de la place moto", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                if (parking.getPlacesDisponibles() <= 0) {
                    JOptionPane.showMessageDialog(null, "Plus de places disponibles dans ce parking", 
                        "Parking complet", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                
                // Décrémenter places normales
                boolean placesDecrementees = ParkingDAO.decrementerPlacesDisponibles(idParking);
                if (!placesDecrementees) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(null, "Erreur lors de la réservation de la place", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            
            // Créer le stationnement
            String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, " +
                    "id_parking, heure_arrivee, type_stationnement, statut_paiement, statut, cout) " +
                    "VALUES (?, ?, ?, ?, ?, 'PARKING', ?, 'ACTIF', ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, idUsager);
                stmt.setString(2, typeVehicule);
                stmt.setString(3, plaqueImmatriculation);
                stmt.setString(4, idParking);
                stmt.setTimestamp(5, Timestamp.valueOf(heureArrivee));
            
                if (estGratuit) {
                    stmt.setString(6, "GRATUIT");
                    stmt.setDouble(7, 0.0); // Coût 0 pour les parkings gratuits
                } else {
                    stmt.setString(6, "NON_PAYE");
                    stmt.setDouble(7, 0.0); // Coût initial à 0
                }
                
                int lignesAffectees = stmt.executeUpdate();
            
                if (lignesAffectees > 0) {
                    conn.commit();
                    String message;
                    if (estGratuit) {
                        message = "Stationnement confirmé !\n\n" +
                                "Votre place est réservée dans le parking " + parking.getLibelleParking() + ".\n" +
                                "Ce parking est gratuit.\n" +
                                "N'oubliez pas de valider votre sortie.";
                    } else {
                        message = "Stationnement confirmé !\n\n" +
                                "Votre place est réservée dans le parking " + parking.getLibelleParking() + ".\n" +
                                "N'oubliez pas de valider votre sortie pour le paiement.";
                    }
                    
                    JOptionPane.showMessageDialog(null,
                        message,
                        "Réservation réussie",
                        JOptionPane.INFORMATION_MESSAGE);
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Erreur lors du rollback: " + ex.getMessage());
            }
            System.err.println("Erreur création stationnement parking: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
    
    /**
     * Vérifie s'il y a des stationnements en cours dans un parking
     */
    public static boolean hasStationnementEnCours(String idParking) {
        String sql = "SELECT COUNT(*) as count FROM Stationnement " +
                    "WHERE id_parking = ? AND date_fin IS NULL";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur vérification stationnements en cours: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
}
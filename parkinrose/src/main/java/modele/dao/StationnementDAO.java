package modele.dao;

import modele.Stationnement;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class StationnementDAO {
    
    /**
     * Crée un nouveau stationnement en voirie
     */
	public static boolean creerStationnementVoirie(Stationnement stationnement) {
	    String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, " +
	                 "id_tarification, type_stationnement, duree_heures, duree_minutes, cout, " +
	                 "statut, date_creation, id_paiement) " +
	                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	    
	    try (Connection conn = MySQLConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, stationnement.getIdUsager());
	        pstmt.setString(2, stationnement.getTypeVehicule());
	        pstmt.setString(3, stationnement.getPlaqueImmatriculation());
	        pstmt.setString(4, stationnement.getIdTarification());
	        pstmt.setString(5, stationnement.getTypeStationnement());
	        pstmt.setInt(6, stationnement.getDureeHeures());
	        pstmt.setInt(7, stationnement.getDureeMinutes());
	        pstmt.setDouble(8, stationnement.getCout());
	        pstmt.setString(9, stationnement.getStatut());
	        pstmt.setTimestamp(10, java.sql.Timestamp.valueOf(stationnement.getDateCreation()));
	        pstmt.setString(11, stationnement.getIdPaiement());
	        
	        int rowsAffected = pstmt.executeUpdate();
	        return rowsAffected > 0;
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public static boolean terminerStationnementParking(Integer idStationnement, 
	                                                  LocalDateTime heureDepart, 
	                                                  double cout, 
	                                                  String idPaiement) {
	    String sql = "UPDATE Stationnement SET heure_depart = ?, cout = ?, statut = 'TERMINE', " +
	                 "id_paiement = ? WHERE id_stationnement = ?";
	    
	    try (Connection conn = MySQLConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setTimestamp(1, java.sql.Timestamp.valueOf(heureDepart));
	        pstmt.setDouble(2, cout);
	        pstmt.setString(3, idPaiement);
	        pstmt.setInt(4, idStationnement);
	        
	        int rowsAffected = pstmt.executeUpdate();
	        return rowsAffected > 0;
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
    
    /**
     * Crée un nouveau stationnement en parking
     */
    public static boolean creerStationnementParking(int idUsager, String typeVehicule, String plaqueImmatriculation,
                                                   String idParking, LocalDateTime heureArrivee) {
        
        Connection conn = null;
        try {
            conn = MySQLConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Vérifier qu'il y a encore des places disponibles
            int placesDisponibles = ParkingDAO.getPlacesDisponibles(idParking);
            if (placesDisponibles <= 0) {
                JOptionPane.showMessageDialog(null, "Plus de places disponibles dans ce parking", "Parking complet", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            // 2. Décrémenter les places disponibles
            boolean placesDecrementees = ParkingDAO.decrementerPlacesDisponibles(idParking);
            if (!placesDecrementees) {
                conn.rollback();
                JOptionPane.showMessageDialog(null, "Erreur lors de la réservation de la place", "Erreur", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // 3. Créer le stationnement avec id_parking au lieu de id_zone
            String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, " +
                        "id_parking, heure_arrivee, type_stationnement, statut_paiement, statut) " +
                        "VALUES (?, ?, ?, ?, ?, 'PARKING', 'NON_PAYE', 'ACTIF')";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idUsager);
                stmt.setString(2, typeVehicule);
                stmt.setString(3, plaqueImmatriculation);
                stmt.setString(4, idParking); // Utiliser id_parking
                stmt.setTimestamp(5, Timestamp.valueOf(heureArrivee));
                
                int lignesAffectees = stmt.executeUpdate();
                
                if (lignesAffectees > 0) {
                    conn.commit();
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
     * Termine un stationnement parking et incrémente les places disponibles
     */
    public static boolean terminerStationnementParking(int idStationnement, LocalDateTime heureDepart, 
                                                      double cout, String idPaiement) {
        
        Connection conn = null;
        try {
            conn = MySQLConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Récupérer l'ID du parking depuis le stationnement
            String idParking = getIdParkingFromStationnement(idStationnement);
            if (idParking == null) {
                conn.rollback();
                return false;
            }
            
            // 2. Mettre à jour le stationnement
            String sql = "UPDATE Stationnement SET statut = 'TERMINE', heure_depart = ?, cout = ?, " +
                        "id_paiement = ?, statut_paiement = 'PAYE' WHERE id_stationnement = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setTimestamp(1, Timestamp.valueOf(heureDepart));
                stmt.setDouble(2, cout);
                stmt.setString(3, idPaiement);
                stmt.setInt(4, idStationnement);
                
                int lignesAffectees = stmt.executeUpdate();
                
                if (lignesAffectees > 0) {
                    // 3. Incrémenter les places disponibles
                    boolean placesIncrementees = ParkingDAO.incrementerPlacesDisponibles(idParking);
                    if (placesIncrementees) {
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
        stationnement.setIdTarification(rs.getString("id_zone"));
        stationnement.setTypeVehicule(rs.getString("type_vehicule"));
        stationnement.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
        stationnement.setDureeHeures(rs.getInt("duree_heures"));
        stationnement.setDureeMinutes(rs.getInt("duree_minutes"));
        stationnement.setCout(rs.getDouble("cout"));
        
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
     * Récupère l'ID du parking depuis un stationnement
     */
    private static String getIdParkingFromStationnement(int idStationnement) {
        String sql = "SELECT id_parking FROM Stationnement WHERE id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idStationnement);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("id_parking");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération ID parking: " + e.getMessage());
        }
        return null;
    }
    
}
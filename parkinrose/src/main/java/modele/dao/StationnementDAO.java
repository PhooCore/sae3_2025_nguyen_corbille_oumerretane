package modele.dao;

import modele.Stationnement;
import modele.dao.requetes.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StationnementDAO extends DaoModele<Stationnement> {
    
    private static StationnementDAO instance;
    
    // Constructeur privé pour le singleton
    private StationnementDAO() {}
    
    // Méthode pour obtenir l'instance unique (Singleton)
    public static StationnementDAO getInstance() {
        if (instance == null) {
            instance = new StationnementDAO();
        }
        return instance;
    }
    
    @Override
    protected Stationnement creerInstance(ResultSet curseur) throws SQLException {
        Stationnement stationnement = new Stationnement();
        
        stationnement.setIdStationnement(curseur.getInt("id_stationnement"));
        stationnement.setIdUsager(curseur.getInt("id_usager"));
        stationnement.setTypeVehicule(curseur.getString("type_vehicule"));
        stationnement.setPlaqueImmatriculation(curseur.getString("plaque_immatriculation"));
        stationnement.setDureeHeures(curseur.getInt("duree_heures"));
        stationnement.setDureeMinutes(curseur.getInt("duree_minutes"));
        stationnement.setCout(curseur.getDouble("cout"));
        
        // Gérer id_zone et id_parking
        String idZone = curseur.getString("id_zone");
        String idParking = curseur.getString("id_parking");
        
        if (idZone != null) {
            stationnement.setIdTarification(idZone);
        } else if (idParking != null) {
            stationnement.setIdTarification(idParking);
        }
        
        Timestamp dateCreation = curseur.getTimestamp("date_creation");
        if (dateCreation != null) {
            stationnement.setDateCreation(dateCreation.toLocalDateTime());
        }
        
        Timestamp dateFin = curseur.getTimestamp("date_fin");
        if (dateFin != null) {
            stationnement.setDateFin(dateFin.toLocalDateTime());
        }
        
        Timestamp heureArrivee = curseur.getTimestamp("heure_arrivee");
        if (heureArrivee != null) {
            stationnement.setHeureArrivee(heureArrivee.toLocalDateTime());
        }
        
        Timestamp heureDepart = curseur.getTimestamp("heure_depart");
        if (heureDepart != null) {
            stationnement.setHeureDepart(heureDepart.toLocalDateTime());
        }
        
        stationnement.setStatut(curseur.getString("statut"));
        stationnement.setTypeStationnement(curseur.getString("type_stationnement"));
        stationnement.setStatutPaiement(curseur.getString("statut_paiement"));
        stationnement.setIdPaiement(curseur.getString("id_paiement"));
        
        return stationnement;
    }
    
    @Override
    public List<Stationnement> findAll() throws SQLException {
        RequeteSelectStationnement req = new RequeteSelectStationnement();
        return find(req);
    }
    
    @Override
    public Stationnement findById(String... id) throws SQLException {
        if (id.length == 0) {
            return null;
        }
        try {
            int idStationnement = Integer.parseInt(id[0]);
            RequeteSelectStationnementById req = new RequeteSelectStationnementById();
            return findById(req, id[0]);
        } catch (NumberFormatException e) {
            throw new SQLException("ID stationnement invalide: " + id[0]);
        }
    }
    
    @Override
    public void create(Stationnement stationnement) throws SQLException {
        if ("VOIRIE".equals(stationnement.getTypeStationnement())) {
            creerStationnementVoirie(stationnement);
        } else if ("PARKING".equals(stationnement.getTypeStationnement())) {
            creerStationnementParking(stationnement);
        } else {
            throw new SQLException("Type de stationnement non supporté: " + stationnement.getTypeStationnement());
        }
    }
    
    @Override
    public void update(Stationnement stationnement) throws SQLException {
        RequeteUpdateStationnement req = new RequeteUpdateStationnement();
        miseAJour(req, stationnement);
    }
    
    @Override
    public void delete(Stationnement stationnement) throws SQLException {
        RequeteDeleteStationnement req = new RequeteDeleteStationnement();
        miseAJour(req, stationnement);
    }
    
    // ===================== MÉTHODES STATIQUES POUR COMPATIBILITÉ =====================
    
    /**
     * Récupère les stationnements par statut (méthode statique)
     */
    public static List<Stationnement> getStationnementsParStatut(int idUsager, String statut) {
        try {
            return getInstance().getStationnementsAvecFiltres(idUsager, statut, null, null, null);
        } catch (Exception e) {
            System.err.println("Erreur récupération stationnements par statut: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Termine un stationnement (méthode statique)
     */
    public static boolean terminerStationnement(int idStationnement) {
        try {
            String sql = "UPDATE Stationnement SET statut = 'TERMINE', date_fin = NOW() WHERE id_stationnement = ?";
            
            try (Connection conn = MySQLConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, idStationnement);
                int lignesAffectees = stmt.executeUpdate();
                return lignesAffectees > 0;
            }
        } catch (Exception e) {
            System.err.println("Erreur terminaison stationnement: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Termine un stationnement parking (méthode statique)
     */
    public static boolean terminerStationnementParking(int idStationnement, LocalDateTime heureDepart, 
                                                      double cout, String idPaiement) {
        try {
            // Vérifier si c'est un parking gratuit
            if (Math.abs(cout) < 0.01) {
                // Pour les parkings gratuits, on utilise une méthode spéciale
                return terminerStationnementParkingGratuit(idStationnement, heureDepart);
            } else {
                // Pour les parkings payants
                return getInstance().terminerStationnementParkingPrive(idStationnement, heureDepart, cout, idPaiement);
            }
        } catch (Exception e) {
            System.err.println("Erreur terminaison stationnement parking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Termine un stationnement parking gratuit
     */
    private static boolean terminerStationnementParkingGratuit(int idStationnement, LocalDateTime heureDepart) throws SQLException {
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
            
            // 2. Mettre à jour le stationnement (id_paiement = NULL pour éviter la contrainte)
            String sqlUpdate = "UPDATE Stationnement SET statut = 'TERMINE', heure_depart = ?, cout = 0.00, " +
                              "id_paiement = NULL, statut_paiement = 'GRATUIT' WHERE id_stationnement = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setTimestamp(1, Timestamp.valueOf(heureDepart));
                stmt.setInt(2, idStationnement);
                
                int lignesAffectees = stmt.executeUpdate();
                
                if (lignesAffectees > 0) {
                    // 3. Libérer les places selon le type de véhicule
                    boolean isMoto = "Moto".equalsIgnoreCase(typeVehicule);
                    boolean placesLiberees;
                    
                    if (isMoto && hasMoto) {
                        placesLiberees = ParkingDAO.getInstance().incrementerPlacesMotoDisponibles(idParking);
                    } else {
                        placesLiberees = ParkingDAO.getInstance().incrementerPlacesDisponibles(idParking);
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
            throw e;
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
     * Récupère un stationnement par son ID (méthode statique)
     */
    public static Stationnement getStationnementById(int idStationnement) {
        try {
            return getInstance().findById(String.valueOf(idStationnement));
        } catch (Exception e) {
            System.err.println("Erreur récupération stationnement: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Récupère l'historique des stationnements d'un usager (version statique)
     */
    public static List<Stationnement> getHistoriqueStationnementsStatic(int idUsager) {
        try {
            return getInstance().getHistoriqueStationnements(idUsager);
        } catch (Exception e) {
            System.err.println("Erreur récupération historique stationnements: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Nettoie les stationnements expirés (méthode statique)
     */
    public static void nettoyerStationnementsExpires() {
        String sql = "UPDATE Stationnement SET statut = 'EXPIRE' WHERE statut = 'ACTIF' AND " +
                    "date_fin IS NOT NULL AND date_fin < NOW()";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erreur nettoyage stationnements expirés: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ===================== MÉTHODES D'INSTANCE =====================
    
    /**
     * Récupère le stationnement actif d'un usager
     */
    public Stationnement getStationnementActifByUsager(int idUsager) throws SQLException {
        String sql = "SELECT s.* FROM Stationnement s " +
                    "WHERE s.id_usager = ? AND s.statut = 'ACTIF' " +
                    "ORDER BY s.date_creation DESC LIMIT 1";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return creerInstance(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Récupère le stationnement actif valide d'un usager (vérifie aussi les dates)
     */
    public Stationnement getStationnementActifValideByUsager(int idUsager) throws SQLException {
        String sql = "SELECT s.* FROM Stationnement s " +
                    "WHERE s.id_usager = ? AND s.statut = 'ACTIF' " +
                    "AND (s.date_fin IS NULL OR s.date_fin > NOW()) " +
                    "ORDER BY s.date_creation DESC LIMIT 1";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return creerInstance(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Termine un stationnement parking et libère les places (version privée)
     */
    public boolean terminerStationnementParkingPrive(int idStationnement, LocalDateTime heureDepart, 
                                                     double cout, String idPaiement) throws SQLException {
        
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
                        placesLiberees = ParkingDAO.getInstance().incrementerPlacesMotoDisponibles(idParking);
                    } else {
                        placesLiberees = ParkingDAO.getInstance().incrementerPlacesDisponibles(idParking);
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
            throw e;
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
    public List<Stationnement> getHistoriqueStationnements(int idUsager) throws SQLException {
        String sql = "SELECT s.* FROM Stationnement s " +
                    "WHERE s.id_usager = ? ORDER BY s.date_creation DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            return select(stmt);
        }
    }
    
    /**
     * Vérifie si un usager a un stationnement actif
     */
    public boolean hasStationnementActif(int idUsager) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Stationnement WHERE id_usager = ? AND statut = 'ACTIF'";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Met à jour le statut de paiement d'un stationnement
     */
    public boolean mettreAJourStatutPaiement(int idStationnement, String idPaiement, String statutPaiement) throws SQLException {
        String sql = "UPDATE Stationnement SET id_paiement = ?, statut_paiement = ? WHERE id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idPaiement);
            stmt.setString(2, statutPaiement);
            stmt.setInt(3, idStationnement);
            
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
        }
    }
    
    /**
     * Crée un stationnement en voirie (méthode publique)
     */
    public boolean creerStationnementVoirie(Stationnement stationnement) throws SQLException {

        String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, " +
                     "id_zone, duree_heures, duree_minutes, cout, " +
                     "statut, date_creation, date_fin, type_stationnement, statut_paiement, id_paiement) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIF', NOW(), " +
                     "DATE_ADD(NOW(), INTERVAL ? MINUTE), 'VOIRIE', ?, ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, stationnement.getIdUsager());
            pstmt.setString(2, stationnement.getTypeVehicule());
            pstmt.setString(3, stationnement.getPlaqueImmatriculation());
            pstmt.setString(4, stationnement.getIdTarification());
            pstmt.setInt(5, stationnement.getDureeHeures());
            pstmt.setInt(6, stationnement.getDureeMinutes());
            pstmt.setDouble(7, stationnement.getCout());
            
            // Calcul de la durée totale en minutes
            int dureeTotaleMinutes = (stationnement.getDureeHeures() * 60) + stationnement.getDureeMinutes();
            pstmt.setInt(8, dureeTotaleMinutes);
            
            pstmt.setString(9, stationnement.getStatutPaiement());
            pstmt.setString(10, stationnement.getIdPaiement());
            
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * Crée un stationnement en parking (méthode publique)
     */
    public boolean creerStationnementParking(Stationnement stationnement) throws SQLException {
        Connection conn = null;
        try {
            conn = MySQLConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Vérifier les places selon le type de véhicule
            boolean isMoto = "Moto".equalsIgnoreCase(stationnement.getTypeVehicule());
            String idParking = stationnement.getIdTarification();
            
            ParkingDAO parkingDAO = ParkingDAO.getInstance();
            
            if (isMoto) {
                if (parkingDAO.getPlacesMotoDisponibles(idParking) <= 0) {
                    conn.rollback();
                    return false;
                }
                // Décrémenter places moto
                if (!parkingDAO.decrementerPlacesMotoDisponibles(idParking)) {
                    conn.rollback();
                    return false;
                }
            } else {
                if (parkingDAO.getPlacesDisponibles(idParking) <= 0) {
                    conn.rollback();
                    return false;
                }
                // Décrémenter places normales
                if (!parkingDAO.decrementerPlacesDisponibles(idParking)) {
                    conn.rollback();
                    return false;
                }
            }
            
            // Créer le stationnement
            String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, " +
                    "id_parking, heure_arrivee, type_stationnement, statut_paiement, statut, cout) " +
                    "VALUES (?, ?, ?, ?, ?, 'PARKING', ?, 'ACTIF', ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, stationnement.getIdUsager());
                stmt.setString(2, stationnement.getTypeVehicule());
                stmt.setString(3, stationnement.getPlaqueImmatriculation());
                stmt.setString(4, idParking);
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(6, "NON_PAYE");
                stmt.setDouble(7, 0.0);
                
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
            throw e;
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
     * Récupère les stationnements avec filtres multiples
     */
    public List<Stationnement> getStationnementsAvecFiltres(int idUsager, String statut, 
                                                           String typeStationnement,
                                                           LocalDateTime dateDebut, 
                                                           LocalDateTime dateFin) throws SQLException {
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
            
            return select(stmt);
        }
    }
    
    /**
     * Récupère les statistiques des stationnements d'un usager
     */
    public Object[] getStatistiquesStationnements(int idUsager) throws SQLException {
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
        }
        return new Object[] {0, 0, 0, 0, 0.0};
    }
    /**
     * Crée un stationnement en voirie gratuit sans passer par le paiement
     * Pour les stationnements gratuits (ex: zone bleue avec abonnement actif)
     */
    public static boolean creerStationnementVoirieGratuit(int idUsager, String typeVehicule, String plaque,
            String idZone, int heures, int minutes) {

String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, " +
"id_zone, duree_heures, duree_minutes, cout, " +
"statut, date_creation, date_fin, type_stationnement, statut_paiement, id_paiement) " +
"VALUES (?, ?, ?, ?, ?, ?, 0.00, 'ACTIF', NOW(), " +
"DATE_ADD(NOW(), INTERVAL ? MINUTE), 'VOIRIE', 'GRATUIT', NULL)";

try (Connection conn = MySQLConnection.getConnection();
PreparedStatement pstmt = conn.prepareStatement(sql)) {

pstmt.setInt(1, idUsager);
pstmt.setString(2, typeVehicule);
pstmt.setString(3, plaque);
pstmt.setString(4, idZone);
pstmt.setInt(5, heures);
pstmt.setInt(6, minutes);

// Calcul de la durée totale en minutes
int dureeTotaleMinutes = (heures * 60) + minutes;
pstmt.setInt(7, dureeTotaleMinutes);

int rowsAffected = pstmt.executeUpdate();

return rowsAffected > 0;

} catch (SQLException e) {
System.err.println("❌ Erreur SQL: " + e.getMessage());
e.printStackTrace();
return false;
}
}
}
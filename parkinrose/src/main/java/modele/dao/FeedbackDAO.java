package modele.dao;

import modele.Feedback;
import modele.Usager;
import modele.dao.requetes.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

public class FeedbackDAO extends DaoModele<Feedback> implements Dao<Feedback> {

    private static FeedbackDAO instance;

    public static FeedbackDAO getInstance() {
        if (instance == null) {
            instance = new FeedbackDAO();
        }
        return instance;
    }

    private FeedbackDAO() {}

    @Override
    public List<Feedback> findAll() throws SQLException {
        RequeteSelectFeedback req = new RequeteSelectFeedback();
        return find(req);
    }

    @Override
    public Feedback findById(String... id) throws SQLException {
        if (id.length < 1) {
            throw new IllegalArgumentException("L'ID du feedback est requis");
        }
        RequeteSelectFeedbackById req = new RequeteSelectFeedbackById();
        return findById(req, id);
    }

    @Override
    public void create(Feedback feedback) throws SQLException {
        if (feedback.getDateCreation() == null) {
            feedback.setDateCreation(LocalDateTime.now());
        }
        if (feedback.getStatut() == null) {
            feedback.setStatut("NOUVEAU");
        }
        
        RequeteInsertFeedback req = new RequeteInsertFeedback();
        miseAJourReturnId(req, feedback);
    }

    @Override
    public void update(Feedback feedback) throws SQLException {
        RequeteUpdateFeedback req = new RequeteUpdateFeedback();
        miseAJour(req, feedback);
    }

    @Override
    public void delete(Feedback feedback) throws SQLException {
        RequeteDeleteFeedback req = new RequeteDeleteFeedback();
        miseAJour(req, feedback);
    }

    @Override
	public Feedback creerInstance(ResultSet curseur) throws SQLException {
        Feedback feedback = new Feedback();
        
        feedback.setIdFeedback(curseur.getInt("id_feedback"));
        feedback.setIdUsager(curseur.getInt("id_usager"));
        feedback.setSujet(curseur.getString("sujet"));
        feedback.setMessage(curseur.getString("message"));
        
        java.sql.Timestamp tsCreation = curseur.getTimestamp("date_creation");
        if (tsCreation != null) {
            feedback.setDateCreation(tsCreation.toLocalDateTime());
        }
        
        feedback.setStatut(curseur.getString("statut"));
        feedback.setGotanswer(curseur.getBoolean("gotanswer"));
        
        int idParent = curseur.getInt("id_feedback_parent");
        if (!curseur.wasNull()) {
            feedback.setIdFeedbackParent(idParent);
        }
        
        int idAdmin = curseur.getInt("id_admin_reponse");
        if (!curseur.wasNull()) {
            feedback.setIdAdminReponse(idAdmin);
        }
        
        java.sql.Timestamp tsReponse = curseur.getTimestamp("date_reponse");
        if (tsReponse != null) {
            feedback.setDateReponse(tsReponse.toLocalDateTime());
        }
        
        feedback.setReponse(curseur.getString("reponse"));
        
        try {
            String prenom = curseur.getString("prenom_usager");
            String nom = curseur.getString("nom_usager");
            String mail = curseur.getString("mail_usager");
            
            if (prenom != null && nom != null) {
                feedback.setPrenomUsager(prenom);
                feedback.setNomUsager(nom);
                feedback.setMailUsager(mail != null ? mail : "");
            }
        } catch (SQLException e) {
        }
        
        try {
            String prenomAdmin = curseur.getString("prenom_admin");
            String nomAdmin = curseur.getString("nom_admin");
            
            if (prenomAdmin != null && nomAdmin != null) {
                feedback.setPrenomAdminReponse(prenomAdmin);
                feedback.setNomAdminReponse(nomAdmin);
            }
        } catch (SQLException e) {
        }
        
        return feedback;
    }
    
    public List<Feedback> findByUser(int idUsager) throws SQLException {
        RequeteSelectFeedbackByUser req = new RequeteSelectFeedbackByUser();
        return find(req, String.valueOf(idUsager));
    }

    public List<Feedback> findAllParent() throws SQLException {
        String sql = "SELECT * FROM Feedback WHERE id_feedback_parent IS NULL ORDER BY date_creation DESC";
        
        Connection conn = MySQLConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        return select(stmt);
    }

    public List<Feedback> findByStatut(String statut) throws SQLException {
        RequeteSelectFeedbackByStatut req = new RequeteSelectFeedbackByStatut();
        return find(req, statut);
    }

    public List<Feedback> findReponses(int idFeedbackParent) throws SQLException {
        RequeteSelectReponsesFeedback req = new RequeteSelectReponsesFeedback();
        return find(req, String.valueOf(idFeedbackParent));
    }

    public List<Feedback> findAllWithUserInfo() throws SQLException {
        RequeteSelectFeedbackWithUserInfo req = new RequeteSelectFeedbackWithUserInfo();
        return find(req);
    }

    public boolean updateStatut(int idFeedback, String statut, boolean gotanswer) throws SQLException {
        RequeteUpdateStatutFeedback req = new RequeteUpdateStatutFeedback();
        
        Feedback feedbackTemp = new Feedback();
        feedbackTemp.setIdFeedback(idFeedback);
        feedbackTemp.setStatut(statut);
        feedbackTemp.setGotanswer(gotanswer);
        
        int result = miseAJour(req, feedbackTemp);
        return result > 0;
    }

    public boolean repondre(int idFeedback, int idAdmin, String reponse) throws SQLException {
        Feedback reponseFeedback = new Feedback();
        reponseFeedback.setIdUsager(idAdmin);
        reponseFeedback.setIdFeedbackParent(idFeedback);
        reponseFeedback.setMessage(reponse);
        reponseFeedback.setDateCreation(LocalDateTime.now());
        reponseFeedback.setStatut("EN_COURS");
        reponseFeedback.setGotanswer(true);
        reponseFeedback.setIdAdminReponse(idAdmin);
        
        RequeteCreerReponseFeedback reqInsert = new RequeteCreerReponseFeedback();
        int resultInsert = miseAJour(reqInsert, reponseFeedback);
        
        if (resultInsert > 0) {
            return updateStatut(idFeedback, "EN_COURS", true);
        }
        
        return false;
    }

    public int countNouveaux() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Feedback WHERE statut = 'NOUVEAU' AND id_feedback_parent IS NULL";
        
        Connection conn = MySQLConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        int count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
        }
        
        rs.close();
        stmt.close();
        return count;
    }
    
    
    public static boolean markMessagesAsRead(int idUsager) {
        String sqlSelect = "SELECT f.id_feedback FROM Feedback f " +
                          "WHERE f.id_usager = ? " +
                          "AND f.id_feedback_parent IS NULL " +
                          "AND f.gotanswer = true " +
                          "AND NOT EXISTS ( " +
                          "    SELECT 1 FROM Feedback vu " +
                          "    WHERE vu.id_usager = ? " +
                          "    AND vu.id_feedback_parent = f.id_feedback " +
                          "    AND vu.message = 'MESSAGE_LU' " +
                          ")";
        
        // Ajouter un sujet valide
        String sqlInsert = "INSERT INTO Feedback (id_usager, sujet, message, date_creation, statut, gotanswer, id_feedback_parent) " +
                          "VALUES (?, 'Message lu', 'MESSAGE_LU', NOW(), 'RESOLU', false, ?)";
        
        try (Connection conn = MySQLConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelect);
                 PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                
                pstmtSelect.setInt(1, idUsager);
                pstmtSelect.setInt(2, idUsager);
                
                ResultSet rs = pstmtSelect.executeQuery();
                boolean hasResults = false;
                
                while (rs.next()) {
                    hasResults = true;
                    int idFeedback = rs.getInt("id_feedback");
                    
                    pstmtInsert.setInt(1, idUsager);
                    pstmtInsert.setInt(2, idFeedback);
                    pstmtInsert.executeUpdate();
                }
                
                rs.close();
                
                if (hasResults) {
                    conn.commit();
                    return true;
                }
                
                conn.commit();
                return false;
                
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean markMessageAsRead(int idFeedback) {
        String sql = "INSERT INTO Feedback (id_usager, sujet, message, date_creation, statut, gotanswer, id_feedback_parent) " +
                    "SELECT id_usager, 'Lecture message', 'MESSAGE_LU', NOW(), 'RESOLU', false, ? " +
                    "FROM Feedback WHERE id_feedback = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idFeedback);
            pstmt.setInt(2, idFeedback);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean hasUnreadMessages(int idUsager) {
        String sql = "SELECT COUNT(*) FROM Feedback f " +
                    "WHERE f.id_usager = ? " +
                    "AND f.id_feedback_parent IS NULL " +
                    "AND f.gotanswer = true " +
                    "AND f.statut != 'RESOLU' " + // Ajouter cette condition
                    "AND NOT EXISTS ( " +
                    "    SELECT 1 FROM Feedback vu " +
                    "    WHERE vu.id_usager = ? " +
                    "    AND vu.id_feedback_parent = f.id_feedback " +
                    "    AND vu.message = 'MESSAGE_LU' " +
                    ")";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            pstmt.setInt(2, idUsager);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean envoyerFeedback(int idUsager, String sujet, String message) {
        try {
            Feedback feedback = new Feedback(idUsager, sujet, message);
            getInstance().create(feedback);
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur envoi feedback: " + e.getMessage());
            return false;
        }
    }

    public static List<Feedback> getFeedbacksByUser(int idUsager) {
        try {
            return getInstance().findByUser(idUsager);
        } catch (SQLException e) {
            System.err.println("Erreur récupération feedbacks utilisateur: " + e.getMessage());
            return null;
        }
    }

    public static List<Feedback> getAllFeedbacksWithInfo() {
        try {
            return getInstance().findAllWithUserInfo();
        } catch (SQLException e) {
            System.err.println("Erreur récupération feedbacks: " + e.getMessage());
            return null;
        }
    }

    public static Feedback getFeedbackById(int idFeedback) {
        try {
            return getInstance().findById(String.valueOf(idFeedback));
        } catch (SQLException e) {
            System.err.println("Erreur récupération feedback: " + e.getMessage());
            return null;
        }
    }

    public static boolean mettreAJourStatut(int idFeedback, String nouveauStatut, boolean gotanswer) {
        String sql = "UPDATE Feedback SET statut = ?, gotanswer = ? " +
                    "WHERE id_feedback = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nouveauStatut);
            pstmt.setBoolean(2, gotanswer);
            pstmt.setInt(3, idFeedback);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean repondreFeedback(int idFeedback, int idAdmin, String message) {
        String sqlInsert = "INSERT INTO Feedback (id_usager, sujet, message, date_creation, statut, gotanswer, id_admin_reponse, id_feedback_parent) " +
                          "SELECT id_usager, CONCAT('Re: ', sujet), ?, NOW(), 'EN_COURS', true, ?, ? " +
                          "FROM Feedback WHERE id_feedback = ?";
        
        String sqlUpdateStatut = "UPDATE Feedback SET statut = 'EN_COURS', gotanswer = true " +
                               "WHERE id_feedback = ?";
        
        try (Connection conn = MySQLConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert);
                 PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdateStatut)) {
                
                pstmtInsert.setString(1, message);
                pstmtInsert.setInt(2, idAdmin);
                pstmtInsert.setInt(3, idFeedback);
                pstmtInsert.setInt(4, idFeedback);
                
                int rowsInserted = pstmtInsert.executeUpdate();
                
                if (rowsInserted > 0) {
                    pstmtUpdate.setInt(1, idFeedback);
                    int rowsUpdated = pstmtUpdate.executeUpdate();
                    
                    if (rowsUpdated > 0) {
                        conn.commit();
                        return true;
                    }
                }
                
                conn.rollback();
                return false;
                
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Feedback> getReponsesFeedback(int idFeedbackParent) {
        try {
            return getInstance().findReponses(idFeedbackParent);
        } catch (SQLException e) {
            System.err.println("Erreur récupération réponses: " + e.getMessage());
            return null;
        }
    }

    public static int getNombreNouveauxFeedbacks() {
        try {
            return getInstance().countNouveaux();
        } catch (SQLException e) {
            System.err.println("Erreur comptage nouveaux feedbacks: " + e.getMessage());
            return 0;
        }
    }

    public static boolean supprimerFeedback(int idFeedback) {
        try {
            Feedback feedback = getFeedbackById(idFeedback);
            if (feedback != null) {
                getInstance().delete(feedback);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur suppression feedback: " + e.getMessage());
            return false;
        }
    }

    public static List<Feedback> getFeedbacksByStatut(String statut) {
        try {
            return getInstance().findByStatut(statut);
        } catch (SQLException e) {
            System.err.println("Erreur récupération feedbacks par statut: " + e.getMessage());
            return null;
        }
    }

    public static List<Feedback> getAllParentFeedbacks() {
        try {
            return getInstance().findAllParent();
        } catch (SQLException e) {
            System.err.println("Erreur récupération feedbacks parents: " + e.getMessage());
            return null;
        }
    }
}
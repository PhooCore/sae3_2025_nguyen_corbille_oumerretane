package modele.dao.requetes;

import modele.Feedback;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

public class RequeteUpdateFeedback extends Requete<Feedback> {
    @Override
    public String requete() {
        return "UPDATE Feedback SET " +
               "id_usager = ?, sujet = ?, message = ?, date_creation = ?, " +
               "statut = ?, gotanswer = ?, id_feedback_parent = ?, " +
               "id_admin_reponse = ?, date_reponse = ?, reponse = ? " +
               "WHERE id_feedback = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Feedback donnee) throws SQLException {
        prSt.setInt(1, donnee.getIdUsager());
        prSt.setString(2, donnee.getSujet());
        prSt.setString(3, donnee.getMessage());
        prSt.setTimestamp(4, Timestamp.valueOf(donnee.getDateCreation()));
        prSt.setString(5, donnee.getStatut());
        prSt.setBoolean(6, donnee.isGotanswer());
        
        if (donnee.getIdFeedbackParent() != null) {
            prSt.setInt(7, donnee.getIdFeedbackParent());
        } else {
            prSt.setNull(7, Types.INTEGER);
        }
        
        if (donnee.getIdAdminReponse() != null) {
            prSt.setInt(8, donnee.getIdAdminReponse());
        } else {
            prSt.setNull(8, Types.INTEGER);
        }
        
        if (donnee.getDateReponse() != null) {
            prSt.setTimestamp(9, Timestamp.valueOf(donnee.getDateReponse()));
        } else {
            prSt.setNull(9, Types.TIMESTAMP);
        }
        
        prSt.setString(10, donnee.getReponse());
        prSt.setInt(11, donnee.getIdFeedback());
    }
}
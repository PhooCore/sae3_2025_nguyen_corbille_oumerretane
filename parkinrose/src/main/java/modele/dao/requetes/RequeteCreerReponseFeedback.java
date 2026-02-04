package modele.dao.requetes;

import modele.Feedback;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteCreerReponseFeedback extends Requete<Feedback> {
    @Override
    public String requete() {
        return "INSERT INTO Feedback (id_usager, sujet, message, date_creation, " +
               "statut, gotanswer, id_feedback_parent, id_admin_reponse) " +
               "VALUES (?, (SELECT sujet FROM Feedback WHERE id_feedback = ?), ?, NOW(), " +
               "'EN_COURS', true, ?, ?)";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Feedback donnee) throws SQLException {
        prSt.setInt(1, donnee.getIdUsager()); // L'admin qui répond
        prSt.setInt(2, donnee.getIdFeedbackParent()); // ID du feedback auquel on répond
        prSt.setString(3, donnee.getMessage()); // Le message de réponse
        prSt.setInt(4, donnee.getIdFeedbackParent()); // id_feedback_parent
        prSt.setInt(5, donnee.getIdAdminReponse()); // id_admin_reponse
    }
}
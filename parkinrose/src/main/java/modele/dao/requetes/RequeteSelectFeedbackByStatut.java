package modele.dao.requetes;

import modele.Feedback;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectFeedbackByStatut extends Requete<Feedback> {
    @Override
    public String requete() {
        return "SELECT * FROM Feedback WHERE statut = ? AND id_feedback_parent IS NULL ORDER BY date_creation DESC";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        if (id.length >= 1) {
            prSt.setString(1, id[0]);
        }
    }
}
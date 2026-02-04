package modele.dao.requetes;

import modele.Feedback;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectFeedbackByUser extends Requete<Feedback> {
    @Override
    public String requete() {
        return "SELECT * FROM Feedback WHERE id_usager = ? AND id_feedback_parent IS NULL ORDER BY date_creation DESC";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        if (id.length >= 1) {
            prSt.setInt(1, Integer.parseInt(id[0]));
        }
    }
}
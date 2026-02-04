package modele.dao.requetes;

import modele.Feedback;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectFeedbackById extends Requete<Feedback> {
    @Override
    public String requete() {
        return "SELECT * FROM Feedback WHERE id_feedback = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        if (id.length >= 1) {
            prSt.setInt(1, Integer.parseInt(id[0]));
        }
    }
}
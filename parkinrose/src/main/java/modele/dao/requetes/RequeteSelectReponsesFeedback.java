package modele.dao.requetes;

import modele.Feedback;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectReponsesFeedback extends Requete<Feedback> {
    @Override
    public String requete() {
        return "SELECT * FROM Feedback WHERE id_feedback_parent = ? ORDER BY date_creation ASC";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        if (id.length >= 1) {
            prSt.setInt(1, Integer.parseInt(id[0]));
        }
    }
}
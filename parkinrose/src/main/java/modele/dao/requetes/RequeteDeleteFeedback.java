package modele.dao.requetes;

import modele.Feedback;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteDeleteFeedback extends Requete<Feedback> {
    @Override
    public String requete() {
        return "DELETE FROM Feedback WHERE id_feedback = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Feedback donnee) throws SQLException {
        prSt.setInt(1, donnee.getIdFeedback());
    }
}
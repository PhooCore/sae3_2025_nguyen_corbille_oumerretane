package modele.dao.requetes;

import modele.Feedback;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectFeedback extends Requete<Feedback> {
    @Override
    public String requete() {
        return "SELECT * FROM Feedback ORDER BY date_creation DESC";
    }
}
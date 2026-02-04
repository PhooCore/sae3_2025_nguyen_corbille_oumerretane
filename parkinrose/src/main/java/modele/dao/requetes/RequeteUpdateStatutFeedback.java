package modele.dao.requetes;

import modele.Feedback;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteUpdateStatutFeedback extends Requete<Feedback> {
    @Override
    public String requete() {
        return "UPDATE Feedback SET statut = ?, gotanswer = ? WHERE id_feedback = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Feedback donnee) throws SQLException {
        prSt.setString(1, donnee.getStatut());
        prSt.setBoolean(2, donnee.isGotanswer());
        prSt.setInt(3, donnee.getIdFeedback());
    }
}
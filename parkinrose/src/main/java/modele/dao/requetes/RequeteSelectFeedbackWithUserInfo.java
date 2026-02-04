package modele.dao.requetes;

import modele.Feedback;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectFeedbackWithUserInfo extends Requete<Feedback> {
    @Override
    public String requete() {
        return "SELECT f.*, u.prenom_usager, u.nom_usager, u.mail_usager, " +
               "a.prenom_usager as prenom_admin, a.nom_usager as nom_admin " +
               "FROM Feedback f " +
               "JOIN Usager u ON f.id_usager = u.id_usager " +
               "LEFT JOIN Usager a ON f.id_admin_reponse = a.id_usager " +
               "WHERE f.id_feedback_parent IS NULL " +
               "ORDER BY f.date_creation DESC";
    }
}
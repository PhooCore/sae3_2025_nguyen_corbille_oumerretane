package modele.dao.requetes;

import modele.Usager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectUsagerByEmail extends Requete<Usager> {
    @Override
    public String requete() {
        return "SELECT * FROM Usager WHERE mail_usager = ?";
    }

    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        prSt.setString(1, id[0]);
    }

    @Override
    public void parametres(PreparedStatement prSt, Usager donnee) throws SQLException {
        prSt.setString(1, donnee.getMailUsager());
    }
}
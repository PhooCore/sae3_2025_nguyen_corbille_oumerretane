package modele.dao.requetes;

import modele.Usager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteUpdateMotDePasse extends Requete<Usager> {
    @Override
    public String requete() {
        return "UPDATE Usager SET mot_de_passe = ? WHERE mail_usager = ?";
    }

    @Override
    public void parametres(PreparedStatement prSt, Usager donnee) throws SQLException {
        prSt.setString(1, donnee.getMotDePasse());
        prSt.setString(2, donnee.getMailUsager());
    }
}
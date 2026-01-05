package modele.dao.requetes;

import modele.Usager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteUpdateCarteTisseo extends Requete<Usager> {
    @Override
    public String requete() {
        return "UPDATE Usager SET numero_carte_tisseo = ? WHERE id_usager = ?";
    }

    @Override
    public void parametres(PreparedStatement prSt, Usager donnee) throws SQLException {
        prSt.setString(1, donnee.getNumeroCarteTisseo());
        prSt.setInt(2, donnee.getIdUsager());
    }
}
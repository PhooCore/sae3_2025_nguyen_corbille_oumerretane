package modele.dao.requetes;

import modele.Favori;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteDeleteFavori extends Requete<Favori> {
    
    @Override
    public String requete() {
        return "DELETE FROM Favori WHERE id_usager = ? AND id_parking = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Favori donnee) throws SQLException {
        prSt.setInt(1, donnee.getIdUsager());
        prSt.setString(2, donnee.getIdParking());
    }
}
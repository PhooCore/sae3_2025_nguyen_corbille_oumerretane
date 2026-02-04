package modele.dao.requetes;

import modele.Favori;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteCreerFavori extends Requete<Favori> {
    
    @Override
    public String requete() {
        return "INSERT INTO Favori (id_usager, id_parking) VALUES (?, ?)";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Favori donnee) throws SQLException {
        prSt.setInt(1, donnee.getIdUsager());
        prSt.setString(2, donnee.getIdParking());
    }
}
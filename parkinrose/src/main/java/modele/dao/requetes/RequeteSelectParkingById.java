package modele.dao.requetes;

import modele.Parking;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectParkingById extends Requete<Parking> {
    @Override
    public String requete() {
        return "SELECT * FROM Parking WHERE id_parking = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        if (id.length >= 1 && id[0] != null) {
            prSt.setString(1, id[0]);
        } else {
            throw new SQLException("ID parking manquant");
        }
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Parking donnee) throws SQLException {
        prSt.setString(1, donnee.getIdParking());
    }
}

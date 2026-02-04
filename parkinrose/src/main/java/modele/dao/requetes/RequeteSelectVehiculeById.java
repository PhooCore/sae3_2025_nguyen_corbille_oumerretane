package modele.dao.requetes;

import modele.Vehicule;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectVehiculeById extends Requete<Vehicule> {
    @Override
    public String requete() {
        return "SELECT * FROM Vehicule WHERE id_vehicule = ?";
    }

    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        prSt.setString(1, id[0]);
    }
}

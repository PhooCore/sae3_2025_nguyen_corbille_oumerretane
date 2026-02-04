package modele.dao.requetes;

import modele.VehiculeUsager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectVehiculeUsagerPrincipal extends Requete<VehiculeUsager> {
    @Override
    public String requete() {
        return "SELECT * FROM Vehicule_Usager WHERE id_usager = ? AND est_principal = TRUE LIMIT 1";
    }

    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        prSt.setInt(1, Integer.parseInt(id[0]));
    }
}
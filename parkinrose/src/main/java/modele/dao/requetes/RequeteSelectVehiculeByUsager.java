package modele.dao.requetes;

import modele.Vehicule;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectVehiculeByUsager extends Requete<Vehicule> {
    @Override
    public String requete() {
        return "SELECT v.* FROM Vehicule v " +
               "INNER JOIN Posseder p ON v.id_vehicule = p.id_vehicule " +
               "WHERE p.id_usager = ? ORDER BY v.plaque_immatriculation";
    }

    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        prSt.setInt(1, Integer.parseInt(id[0]));
    }
}
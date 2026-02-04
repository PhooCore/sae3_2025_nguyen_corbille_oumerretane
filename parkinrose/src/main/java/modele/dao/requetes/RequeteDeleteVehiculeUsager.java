package modele.dao.requetes;

import modele.VehiculeUsager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteDeleteVehiculeUsager extends Requete<VehiculeUsager> {
    @Override
    public String requete() {
        return "DELETE FROM Vehicule_Usager WHERE id_vehicule_usager = ?";
    }

    @Override
    public void parametres(PreparedStatement prSt, VehiculeUsager donnee) throws SQLException {
        prSt.setInt(1, donnee.getIdVehiculeUsager());
    }
}
package modele.dao.requetes;

import modele.Vehicule;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteUpdateVehicule extends Requete<Vehicule> {
    @Override
    public String requete() {
        return "UPDATE Vehicule SET plaque_immatriculation = ? WHERE id_vehicule = ?";
    }

    @Override
    public void parametres(PreparedStatement prSt, Vehicule donnee) throws SQLException {
        prSt.setString(1, donnee.getPlaqueImmatriculation());
        prSt.setString(2, donnee.getIdVehicule());
    }
}
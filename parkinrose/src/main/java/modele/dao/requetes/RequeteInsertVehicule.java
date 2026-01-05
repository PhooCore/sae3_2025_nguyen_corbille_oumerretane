package modele.dao.requetes;

import modele.Vehicule;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteInsertVehicule extends Requete<Vehicule> {
    @Override
    public String requete() {
        return "INSERT INTO Vehicule (id_vehicule, plaque_immatriculation) VALUES (?, ?)";
    }

    @Override
    public void parametres(PreparedStatement prSt, Vehicule donnee) throws SQLException {
        prSt.setString(1, donnee.getIdVehicule());
        prSt.setString(2, donnee.getPlaqueImmatriculation());
    }
}
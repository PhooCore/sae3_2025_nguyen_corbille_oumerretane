package modele.dao.requetes;

import modele.VehiculeUsager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteInsertVehiculeUsager extends Requete<VehiculeUsager> {
    @Override
    public String requete() {
        return "INSERT INTO Vehicule_Usager (id_usager, plaque_immatriculation, type_vehicule, marque, modele, est_principal) " +
               "VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void parametres(PreparedStatement prSt, VehiculeUsager donnee) throws SQLException {
        prSt.setInt(1, donnee.getIdUsager());
        prSt.setString(2, donnee.getPlaqueImmatriculation());
        prSt.setString(3, donnee.getTypeVehicule());
        prSt.setString(4, donnee.getMarque());
        prSt.setString(5, donnee.getModele());
        prSt.setBoolean(6, donnee.isEstPrincipal());
    }
}

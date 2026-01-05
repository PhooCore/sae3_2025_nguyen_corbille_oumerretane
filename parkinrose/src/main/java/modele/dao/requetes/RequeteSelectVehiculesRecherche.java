package modele.dao.requetes;

import modele.Vehicule;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectVehiculesRecherche extends Requete<Vehicule> {
    @Override
    public String requete() {
        return "SELECT v.*, u.nom_usager, u.prenom_usager FROM Vehicule v " +
               "LEFT JOIN Posseder p ON v.id_vehicule = p.id_vehicule " +
               "LEFT JOIN Usager u ON p.id_usager = u.id_usager " +
               "WHERE v.plaque_immatriculation LIKE ? " +
               "ORDER BY v.plaque_immatriculation";
    }

    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        prSt.setString(1, id[0]);
    }
}
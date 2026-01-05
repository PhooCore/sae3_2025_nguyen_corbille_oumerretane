package modele.dao.requetes;

import modele.Vehicule;

public class RequeteSelectVehiculesOrphelins extends Requete<Vehicule> {
    @Override
    public String requete() {
        return "SELECT v.* FROM Vehicule v " +
               "LEFT JOIN Posseder p ON v.id_vehicule = p.id_vehicule " +
               "WHERE p.id_usager IS NULL";
    }
}

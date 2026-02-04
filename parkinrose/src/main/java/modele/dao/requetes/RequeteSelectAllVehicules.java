package modele.dao.requetes;

import modele.Vehicule;

public class RequeteSelectAllVehicules extends Requete<Vehicule> {
    @Override
    public String requete() {
        return "SELECT v.*, u.nom_usager, u.prenom_usager FROM Vehicule v " +
               "LEFT JOIN Posseder p ON v.id_vehicule = p.id_vehicule " +
               "LEFT JOIN Usager u ON p.id_usager = u.id_usager " +
               "ORDER BY v.plaque_immatriculation";
    }
}
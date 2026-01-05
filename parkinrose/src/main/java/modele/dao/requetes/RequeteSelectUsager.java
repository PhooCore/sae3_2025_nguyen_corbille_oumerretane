package modele.dao.requetes;

import modele.Usager;

public class RequeteSelectUsager extends Requete<Usager> {
    @Override
    public String requete() {
        return "SELECT * FROM Usager ORDER BY nom_usager, prenom_usager";
    }
}
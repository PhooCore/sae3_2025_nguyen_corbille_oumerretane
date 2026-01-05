package modele.dao.requetes;

import modele.Zone;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectZonesRecherche extends Requete<Zone> {
    @Override
    public String requete() {
        return "SELECT id_zone, libelle_zone, couleur_zone, tarif_par_heure, duree_max FROM zone " +
               "WHERE libelle_zone LIKE ? OR id_zone LIKE ? " +
               "ORDER BY libelle_zone";
    }

    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        prSt.setString(1, id[0]); // terme pour libelle_zone
        prSt.setString(2, id[1]); // terme pour id_zone
    }
}
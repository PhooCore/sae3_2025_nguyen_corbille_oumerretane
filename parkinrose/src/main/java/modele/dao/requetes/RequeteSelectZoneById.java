package modele.dao.requetes;

import modele.Zone;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectZoneById extends Requete<Zone> {
    @Override
    public String requete() {
        return "SELECT id_zone, libelle_zone, couleur_zone, tarif_par_heure, duree_max FROM zone WHERE id_zone = ?";
    }

    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        prSt.setString(1, id[0]);
    }
}
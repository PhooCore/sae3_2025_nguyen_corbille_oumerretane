package modele.dao.requetes;

import modele.Stationnement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteDeleteStationnement extends Requete<Stationnement> {
    @Override
    public String requete() {
        return "DELETE FROM Stationnement WHERE id_stationnement = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        if (id.length >= 1 && id[0] != null) {
            try {
                prSt.setInt(1, Integer.parseInt(id[0]));
            } catch (NumberFormatException e) {
                throw new SQLException("ID stationnement invalide: " + id[0]);
            }
        } else {
            throw new SQLException("ID stationnement manquant");
        }
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Stationnement donnee) throws SQLException {
        prSt.setInt(1, donnee.getIdStationnement());
    }
}
package modele.dao.requetes;

import modele.Abonnement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteDeleteAbonnement extends Requete<Abonnement> {
    @Override
    public String requete() {
        return "DELETE FROM Abonnement WHERE id_abonnement = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        if (id.length >= 1 && id[0] != null) {
            prSt.setString(1, id[0]);
        } else {
            throw new SQLException("ID d'abonnement manquant");
        }
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Abonnement donnee) throws SQLException {
        prSt.setString(1, donnee.getIdAbonnement());
    }
}
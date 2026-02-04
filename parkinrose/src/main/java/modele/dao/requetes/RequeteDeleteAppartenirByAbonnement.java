package modele.dao.requetes;

import modele.Abonnement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteDeleteAppartenirByAbonnement extends Requete<Abonnement> {
    @Override
    public String requete() {
        return "DELETE FROM Appartenir WHERE id_abonnement = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        if (id.length >= 1 && id[0] != null) {
            prSt.setString(1, id[0]);
        } else {
            throw new SQLException("ID abonnement manquant");
        }
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Abonnement donnee) throws SQLException {
        prSt.setString(1, donnee.getIdAbonnement());
    }
}
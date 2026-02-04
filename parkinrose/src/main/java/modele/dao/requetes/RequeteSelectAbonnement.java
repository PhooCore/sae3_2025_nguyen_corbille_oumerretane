package modele.dao.requetes;

import modele.Abonnement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectAbonnement extends Requete<Abonnement> {
    @Override
    public String requete() {
        return "SELECT * FROM Abonnement ORDER BY tarif_applique";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Pas de paramètres pour un select all
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Abonnement donnee) throws SQLException {
        // Pas de paramètres pour un select all
    }
}
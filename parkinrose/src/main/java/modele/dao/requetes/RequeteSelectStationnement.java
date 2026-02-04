package modele.dao.requetes;

import modele.Stationnement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectStationnement extends Requete<Stationnement> {
    @Override
    public String requete() {
        return "SELECT * FROM Stationnement ORDER BY date_creation DESC";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Pas de paramètres pour un select all
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Stationnement donnee) throws SQLException {
        // Pas de paramètres pour un select all
    }
}
package modele.dao.requetes;

import modele.Parking;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectParking extends Requete<Parking> {
    @Override
    public String requete() {
        return "SELECT * FROM Parking ORDER BY libelle_parking";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Pas de paramètres pour un select all
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Parking donnee) throws SQLException {
        // Pas de paramètres pour un select all
    }
}
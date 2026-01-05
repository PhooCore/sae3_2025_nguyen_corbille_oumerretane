package modele.dao.requetes;

import modele.Paiement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectPaiement extends Requete<Paiement> {
    @Override
    public String requete() {
        return "SELECT * FROM Paiement ORDER BY date_paiement DESC";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Pas de paramètres pour un select all
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Paiement donnee) throws SQLException {
        // Pas de paramètres pour un select all
    }
}
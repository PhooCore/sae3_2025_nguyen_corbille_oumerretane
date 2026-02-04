package modele.dao.requetes;

import modele.Paiement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectPaiementById extends Requete<Paiement> {
    @Override
    public String requete() {
        return "SELECT * FROM Paiement WHERE id_paiement = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        if (id.length >= 1 && id[0] != null) {
            prSt.setString(1, id[0]);
        } else {
            throw new SQLException("ID paiement manquant");
        }
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Paiement donnee) throws SQLException {
        prSt.setString(1, donnee.getIdPaiement());
    }
}
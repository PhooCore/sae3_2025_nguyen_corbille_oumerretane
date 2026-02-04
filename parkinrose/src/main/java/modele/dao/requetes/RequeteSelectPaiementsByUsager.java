package modele.dao.requetes;

import modele.Paiement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectPaiementsByUsager extends Requete<Paiement> {
    @Override
    public String requete() {
        return "SELECT * FROM Paiement WHERE id_usager = ? ORDER BY date_paiement DESC";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        if (id.length >= 1 && id[0] != null) {
            try {
                prSt.setInt(1, Integer.parseInt(id[0]));
            } catch (NumberFormatException e) {
                throw new SQLException("ID usager invalide: " + id[0]);
            }
        } else {
            throw new SQLException("ID usager manquant");
        }
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Paiement donnee) throws SQLException {
        prSt.setInt(1, donnee.getIdUsager());
    }
}
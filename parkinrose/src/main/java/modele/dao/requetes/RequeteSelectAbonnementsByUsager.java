package modele.dao.requetes;

import modele.Abonnement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectAbonnementsByUsager extends Requete<Abonnement> {
    @Override
    public String requete() {
        return "SELECT a.* FROM Abonnement a " +
               "INNER JOIN Appartenir ap ON a.id_abonnement = ap.id_abonnement " +
               "WHERE ap.id_usager = ? ORDER BY a.tarif_applique";
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
    public void parametres(PreparedStatement prSt, Abonnement donnee) throws SQLException {
        // Non utilisé pour cette requête
    }
}
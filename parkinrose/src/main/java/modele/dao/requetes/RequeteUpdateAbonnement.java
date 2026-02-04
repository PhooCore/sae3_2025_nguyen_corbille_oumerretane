package modele.dao.requetes;

import modele.Abonnement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteUpdateAbonnement extends Requete<Abonnement> {
    @Override
    public String requete() {
        return "UPDATE Abonnement SET libelle_abonnement = ?, tarif_applique = ? WHERE id_abonnement = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Non utilisé pour l'update
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Abonnement donnee) throws SQLException {
        prSt.setString(1, donnee.getLibelleAbonnement());
        prSt.setDouble(2, donnee.getTarifAbonnement());
        prSt.setString(3, donnee.getIdAbonnement());
    }
}
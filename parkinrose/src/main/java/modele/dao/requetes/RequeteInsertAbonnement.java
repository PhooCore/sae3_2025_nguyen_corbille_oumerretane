package modele.dao.requetes;

import modele.Abonnement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteInsertAbonnement extends Requete<Abonnement> {
    @Override
    public String requete() {
        return "INSERT INTO Abonnement (id_abonnement, libelle_abonnement, tarif_applique) VALUES (?, ?, ?)";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Non utilisé pour l'insertion
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Abonnement donnee) throws SQLException {
        prSt.setString(1, donnee.getIdAbonnement());
        prSt.setString(2, donnee.getLibelleAbonnement());
        prSt.setDouble(3, donnee.getTarifAbonnement());
    }
}
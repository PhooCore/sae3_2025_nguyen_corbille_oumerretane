package modele.dao.requetes;

import modele.Paiement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteUpdatePaiement extends Requete<Paiement> {
    @Override
    public String requete() {
        return "UPDATE Paiement SET nom_carte = ?, numero_carte = ?, " +
               "code_secret_carte = ?, montant = ?, statut = ? " +
               "WHERE id_paiement = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Non utilisé pour l'update
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Paiement donnee) throws SQLException {
        prSt.setString(1, donnee.getNomCarte());
        prSt.setString(2, donnee.getNumeroCarte());
        prSt.setString(3, donnee.getCodeSecretCarte());
        prSt.setDouble(4, donnee.getMontant());
        prSt.setString(5, donnee.getStatut());
        prSt.setString(6, donnee.getIdPaiement());
    }
}
package modele.dao.requetes;

import modele.Paiement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteInsertPaiement extends Requete<Paiement> {
    @Override
    public String requete() {
        return "INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, " +
               "code_secret_carte, id_abonnement, montant, id_usager, date_paiement, " +
               "methode_paiement, statut) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), 'CARTE', 'REUSSI')";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Non utilisé pour l'insertion
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Paiement donnee) throws SQLException {
        prSt.setString(1, donnee.getIdPaiement());
        prSt.setString(2, donnee.getNomCarte());
        prSt.setString(3, donnee.getNumeroCarte());
        prSt.setString(4, donnee.getCodeSecretCarte());
        
        // Gérer l'id_abonnement qui peut être null
        if (donnee.getIdAbonnement() != null && !donnee.getIdAbonnement().isEmpty()) {
            prSt.setString(5, donnee.getIdAbonnement());
        } else {
            prSt.setNull(5, java.sql.Types.VARCHAR);
        }
        
        prSt.setDouble(6, donnee.getMontant());
        prSt.setInt(7, donnee.getIdUsager());
    }
}

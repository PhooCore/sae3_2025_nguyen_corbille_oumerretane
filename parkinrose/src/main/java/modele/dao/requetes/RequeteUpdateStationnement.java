package modele.dao.requetes;

import modele.Stationnement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteUpdateStationnement extends Requete<Stationnement> {
    @Override
    public String requete() {
        return "UPDATE Stationnement SET " +
               "type_vehicule = ?, plaque_immatriculation = ?, " +
               "duree_heures = ?, duree_minutes = ?, cout = ?, " +
               "statut = ?, date_fin = ?, heure_arrivee = ?, heure_depart = ?, " +
               "statut_paiement = ?, id_paiement = ? " +
               "WHERE id_stationnement = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Non utilisé pour l'update
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Stationnement donnee) throws SQLException {
        prSt.setString(1, donnee.getTypeVehicule());
        prSt.setString(2, donnee.getPlaqueImmatriculation());
        prSt.setInt(3, donnee.getDureeHeures());
        prSt.setInt(4, donnee.getDureeMinutes());
        prSt.setDouble(5, donnee.getCout());
        prSt.setString(6, donnee.getStatut());
        
        if (donnee.getDateFin() != null) {
            prSt.setTimestamp(7, java.sql.Timestamp.valueOf(donnee.getDateFin()));
        } else {
            prSt.setNull(7, java.sql.Types.TIMESTAMP);
        }
        
        if (donnee.getHeureArrivee() != null) {
            prSt.setTimestamp(8, java.sql.Timestamp.valueOf(donnee.getHeureArrivee()));
        } else {
            prSt.setNull(8, java.sql.Types.TIMESTAMP);
        }
        
        if (donnee.getHeureDepart() != null) {
            prSt.setTimestamp(9, java.sql.Timestamp.valueOf(donnee.getHeureDepart()));
        } else {
            prSt.setNull(9, java.sql.Types.TIMESTAMP);
        }
        
        prSt.setString(10, donnee.getStatutPaiement());
        prSt.setString(11, donnee.getIdPaiement());
        prSt.setInt(12, donnee.getIdStationnement());
    }
}

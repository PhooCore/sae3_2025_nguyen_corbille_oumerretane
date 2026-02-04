package modele.dao.requetes;

import modele.Stationnement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteInsertStationnement extends Requete<Stationnement> {
    @Override
    public String requete() {
        return "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, " +
               "id_zone, id_parking, duree_heures, duree_minutes, cout, " +
               "statut, date_creation, date_fin, heure_arrivee, heure_depart, " +
               "type_stationnement, statut_paiement, id_paiement) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Non utilisé pour l'insertion
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Stationnement donnee) throws SQLException {
        prSt.setInt(1, donnee.getIdUsager());
        prSt.setString(2, donnee.getTypeVehicule());
        prSt.setString(3, donnee.getPlaqueImmatriculation());
        
        // Gérer id_zone et id_parking selon le type
        if ("VOIRIE".equals(donnee.getTypeStationnement())) {
            prSt.setString(4, donnee.getIdTarification());
            prSt.setNull(5, java.sql.Types.VARCHAR);
        } else if ("PARKING".equals(donnee.getTypeStationnement())) {
            prSt.setNull(4, java.sql.Types.VARCHAR);
            prSt.setString(5, donnee.getIdTarification());
        } else {
            prSt.setNull(4, java.sql.Types.VARCHAR);
            prSt.setNull(5, java.sql.Types.VARCHAR);
        }
        
        prSt.setInt(6, donnee.getDureeHeures());
        prSt.setInt(7, donnee.getDureeMinutes());
        prSt.setDouble(8, donnee.getCout());
        prSt.setString(9, donnee.getStatut());
        prSt.setTimestamp(10, java.sql.Timestamp.valueOf(donnee.getDateCreation()));
        
        if (donnee.getDateFin() != null) {
            prSt.setTimestamp(11, java.sql.Timestamp.valueOf(donnee.getDateFin()));
        } else {
            prSt.setNull(11, java.sql.Types.TIMESTAMP);
        }
        
        if (donnee.getHeureArrivee() != null) {
            prSt.setTimestamp(12, java.sql.Timestamp.valueOf(donnee.getHeureArrivee()));
        } else {
            prSt.setNull(12, java.sql.Types.TIMESTAMP);
        }
        
        if (donnee.getHeureDepart() != null) {
            prSt.setTimestamp(13, java.sql.Timestamp.valueOf(donnee.getHeureDepart()));
        } else {
            prSt.setNull(13, java.sql.Types.TIMESTAMP);
        }
        
        prSt.setString(14, donnee.getTypeStationnement());
        prSt.setString(15, donnee.getStatutPaiement());
        prSt.setString(16, donnee.getIdPaiement());
    }
}
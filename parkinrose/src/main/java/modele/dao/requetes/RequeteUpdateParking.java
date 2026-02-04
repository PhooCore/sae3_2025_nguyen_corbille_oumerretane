package modele.dao.requetes;

import modele.Parking;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteUpdateParking extends Requete<Parking> {
    @Override
    public String requete() {
        return "UPDATE Parking SET " +
               "libelle_parking = ?, adresse_parking = ?, " +
               "nombre_places = ?, places_disponibles = ?, " +
               "hauteur_parking = ?, tarif_soiree = ?, " +
               "has_moto = ?, places_moto = ?, places_moto_disponibles = ?, " +
               "est_relais = ?, position_x = ?, position_y = ? " +
               "WHERE id_parking = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Non utilisé pour l'update
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Parking donnee) throws SQLException {
        prSt.setString(1, donnee.getLibelleParking());
        prSt.setString(2, donnee.getAdresseParking());
        prSt.setInt(3, donnee.getNombrePlaces());
        prSt.setInt(4, donnee.getPlacesDisponibles());
        prSt.setDouble(5, donnee.getHauteurParking());
        prSt.setBoolean(6, donnee.hasTarifSoiree());
        prSt.setBoolean(7, donnee.hasMoto());
        prSt.setInt(8, donnee.getPlacesMoto());
        prSt.setInt(9, donnee.getPlacesMotoDisponibles());
        prSt.setBoolean(10, donnee.isEstRelais());
        
        if (donnee.getPositionX() != null && donnee.getPositionY() != null) {
            prSt.setFloat(11, donnee.getPositionX());
            prSt.setFloat(12, donnee.getPositionY());
        } else {
            prSt.setNull(11, java.sql.Types.FLOAT);
            prSt.setNull(12, java.sql.Types.FLOAT);
        }
        
        prSt.setString(13, donnee.getIdParking());
    }
}
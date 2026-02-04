package modele.dao.requetes;

import modele.Parking;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteInsertParking extends Requete<Parking> {
    @Override
    public String requete() {
        return "INSERT INTO Parking (id_parking, libelle_parking, adresse_parking, " +
               "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
               "has_moto, places_moto, places_moto_disponibles, est_relais, position_x, position_y) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Non utilisé pour l'insertion
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Parking donnee) throws SQLException {
        
        prSt.setString(1, donnee.getIdParking());
        prSt.setString(2, donnee.getLibelleParking());
        prSt.setString(3, donnee.getAdresseParking());
        prSt.setInt(4, donnee.getNombrePlaces());
        prSt.setInt(5, donnee.getPlacesDisponibles());
        prSt.setDouble(6, donnee.getHauteurParking());
        prSt.setBoolean(7, donnee.hasTarifSoiree());
        prSt.setBoolean(8, donnee.hasMoto());
        prSt.setInt(9, donnee.getPlacesMoto());
        prSt.setInt(10, donnee.getPlacesMotoDisponibles());
        prSt.setBoolean(11, donnee.isEstRelais());
        
        // IMPORTANT: Gestion des coordonnées
        Float posX = donnee.getPositionX();
        Float posY = donnee.getPositionY();
        
        if (posX != null && posY != null) {
            prSt.setFloat(12, posX);
            prSt.setFloat(13, posY);
        } else {
            prSt.setNull(12, java.sql.Types.FLOAT);
            prSt.setNull(13, java.sql.Types.FLOAT);
        }
        
    }
}
// RequeteSelectParkingTarif.java (optionnel)
package modele.dao.requetes;

import modele.Parking;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectParkingTarif extends Requete<Parking> {
    
    private String typeTarif; // "GRATUIT", "SOIREE", "RELAIS", etc.
    
    public RequeteSelectParkingTarif(String typeTarif) {
        this.typeTarif = typeTarif;
    }
    
    @Override
    public String requete() {
        StringBuilder sql = new StringBuilder("SELECT * FROM Parking WHERE ");
        
        switch (typeTarif.toUpperCase()) {
            case "GRATUIT":
                sql.append("id_parking IN ('PARK_VIGUERIE', 'PARK_BOULE', 'PARK_VELODROME', 'PARK_PONTS_JUMEAUX', 'PARK_BONNEFOY', 'PARK_MIRAIL', 'PARK_CROIX_PIERRE')");
                break;
            case "SOIREE":
                sql.append("tarif_soiree = TRUE");
                break;
            case "RELAIS":
                sql.append("est_relais = TRUE");
                break;
            case "MOTO":
                sql.append("has_moto = TRUE AND places_moto_disponibles > 0");
                break;
            default:
                sql.append("1=1"); // Tous les parkings
        }
        
        sql.append(" ORDER BY libelle_parking");
        return sql.toString();
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Pas de paramètres pour cette requête simple
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Parking donnee) throws SQLException {
        // Pas de paramètres pour cette requête simple
    }
}
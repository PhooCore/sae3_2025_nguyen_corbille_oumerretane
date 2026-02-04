package modele.dao.requetes;

import modele.Usager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteSelectUsagerById extends Requete<Usager> {
    
    @Override
    public String requete() {
        return "SELECT * FROM Usager WHERE id_usager = ?";
    }

    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Vérifier que l'ID est bien un nombre
        try {
            int idUsager = Integer.parseInt(id[0]);
            prSt.setInt(1, idUsager);
        } catch (NumberFormatException e) {
            throw new SQLException("L'ID utilisateur doit être un nombre valide: " + id[0]);
        }
    }

    @Override
    public void parametres(PreparedStatement prSt, Usager donnee) throws SQLException {
        prSt.setInt(1, donnee.getIdUsager());
    }
}
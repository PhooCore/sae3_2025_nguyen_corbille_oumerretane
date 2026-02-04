package modele.dao.requetes;

import modele.Usager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RequeteSelectCarteTisseo extends Requete<Usager> {
    @Override
    public String requete() {
        return "SELECT numero_carte_tisseo FROM Usager WHERE id_usager = ?";
    }

    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        prSt.setInt(1, Integer.parseInt(id[0]));
    }

    @Override
    public void parametres(PreparedStatement prSt, Usager donnee) throws SQLException {
        prSt.setInt(1, donnee.getIdUsager());
    }
    
    // Note: Cette méthode est spécifique à cette requête car on ne sélectionne qu'un champ
    public Usager creerInstance(ResultSet curseur) throws SQLException {
        Usager usager = new Usager();
        usager.setNumeroCarteTisseo(curseur.getString("numero_carte_tisseo"));
        return usager;
    }
}
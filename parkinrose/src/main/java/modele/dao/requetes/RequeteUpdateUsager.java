package modele.dao.requetes;

import modele.Usager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteUpdateUsager extends Requete<Usager> {
    @Override
    public String requete() {
        return "UPDATE Usager SET nom_usager = ?, prenom_usager = ?, " +
               "mail_usager = ?, mot_de_passe = ?, numero_carte_tisseo = ?, is_admin = ? " +
               "WHERE id_usager = ?";
    }

    @Override
    public void parametres(PreparedStatement prSt, Usager donnee) throws SQLException {
        prSt.setString(1, donnee.getNomUsager());
        prSt.setString(2, donnee.getPrenomUsager());
        prSt.setString(3, donnee.getMailUsager());
        prSt.setString(4, donnee.getMotDePasse());
        prSt.setString(5, donnee.getNumeroCarteTisseo());
        prSt.setBoolean(6, donnee.isAdmin());
        prSt.setInt(7, donnee.getIdUsager());
    }
}

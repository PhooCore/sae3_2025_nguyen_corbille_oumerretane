// RequeteModifierMotDePasse.java
package modele.dao.requetes;

import modele.Usager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequeteModifierMotDePasse extends Requete<Usager> {
    
    private String email;
    private String nouveauMotDePasse;
    
    public RequeteModifierMotDePasse(String email, String nouveauMotDePasse) {
        this.email = email;
        this.nouveauMotDePasse = nouveauMotDePasse;
    }
    
    @Override
    public String requete() {
        return "UPDATE Usager SET mot_de_passe = ? WHERE mail_usager = ?";
    }
    
    @Override
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {
        // Non utilisé pour cette requête
    }
    
    @Override
    public void parametres(PreparedStatement prSt, Usager donnee) throws SQLException {
        // Non utilisé pour cette requête
    }
    
    public void parametres(PreparedStatement prSt) throws SQLException {
        prSt.setString(1, nouveauMotDePasse);
        prSt.setString(2, email);
    }
}
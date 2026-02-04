package modele.dao;

import modele.Usager;
import modele.dao.requetes.Requete;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuthentificationDAO extends Requete<Usager> {
    
    private String email;
    private String motDePasse;
    
    public AuthentificationDAO(String email, String motDePasse) {
        this.email = email;
        this.motDePasse = motDePasse;
    }
    
    @Override
    public String requete() {
        return "SELECT id_usager FROM Usager WHERE mail_usager = ? AND mot_de_passe = ?";
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
        prSt.setString(1, email);
        prSt.setString(2, motDePasse);
    }
}
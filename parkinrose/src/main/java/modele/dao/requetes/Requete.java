package modele.dao.requetes;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Requete<T> {
    public abstract String requete();
    
    public void parametres(PreparedStatement prSt, String... id) throws SQLException {}
    
    public void parametres(PreparedStatement prSt, T donnee) throws SQLException {}
}
package modele.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Iterateur<T> implements Iterator<T> {
    private ResultSet rs;
    private DaoModele<T> dao;
    private boolean hasNext;
    
    public Iterateur(ResultSet rs, DaoModele<T> dao) throws SQLException {
        this.rs = rs;
        this.dao = dao;
        this.hasNext = rs.next();
    }
    
    @Override
    public boolean hasNext() {
        return hasNext;
    }
    
    @Override
    public T next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }
        try {
            T instance = dao.creerInstance(rs);
            hasNext = rs.next();
            if (!hasNext) {
                rs.close();
            }
            return instance;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    public void close() throws SQLException {
        if (rs != null && !rs.isClosed()) {
            rs.close();
        }
    }
}
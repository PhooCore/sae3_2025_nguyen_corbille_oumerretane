package modele.dao;

import java.sql.SQLException;
import java.util.List;

public interface Dao<T> {
    List<T> findAll() throws SQLException;
    T findById(String... id) throws SQLException;
    void create(T obj) throws SQLException;
    void update(T obj) throws SQLException;
    void delete(T obj) throws SQLException;}

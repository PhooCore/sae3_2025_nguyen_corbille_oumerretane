package modele.test;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import modele.Usager;
import modele.dao.MySQLConnection;
import modele.dao.UsagerDAO;

import java.sql.*;
import java.util.List;

public class UsagerDAOTest {
    
    private UsagerDAO dao;
    private Connection conn;
    
    @Before
    public void setUp() throws SQLException {
        dao = UsagerDAO.getInstance();
        conn = MySQLConnection.getConnection();
        
        // Nettoyer et préparer la base de test
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Usager");
            
            // Insérer des utilisateurs de test
            stmt.execute("INSERT INTO Usager (id_usager, nom_usager, prenom_usager, mail_usager, mot_de_passe, numero_carte_tisseo, is_admin) " +
                        "VALUES (1, 'Dupont', 'Jean', 'jean.dupont@test.com', 'mdp123', '1234567890', 0)");
            
            stmt.execute("INSERT INTO Usager (id_usager, nom_usager, prenom_usager, mail_usager, mot_de_passe, is_admin) " +
                        "VALUES (2, 'Admin', 'System', 'admin@test.com', 'admin123', 1)");
        }
    }
    
    @After
    public void tearDown() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Usager");
        }
        
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    @Test
    public void testFindAll() throws SQLException {
        List<Usager> usagers = dao.findAll();
        
        assertNotNull(usagers);
        assertEquals(2, usagers.size());
        
        boolean foundUser = false;
        boolean foundAdmin = false;
        for (Usager u : usagers) {
            if (u.getMailUsager().equals("jean.dupont@test.com")) {
                foundUser = true;
                assertEquals("Dupont", u.getNomUsager());
                assertEquals("Jean", u.getPrenomUsager());
                assertEquals("1234567890", u.getNumeroCarteTisseo());
                assertFalse(u.isAdmin());
            }
            if (u.getMailUsager().equals("admin@test.com")) {
                foundAdmin = true;
                assertTrue(u.isAdmin());
            }
        }
        
        assertTrue(foundUser);
        assertTrue(foundAdmin);
    }
    
    @Test
    public void testFindById() throws SQLException {
        Usager usager = dao.findById("jean.dupont@test.com");
        
        assertNotNull(usager);
        assertEquals("Dupont", usager.getNomUsager());
        assertEquals("Jean", usager.getPrenomUsager());
        assertEquals("jean.dupont@test.com", usager.getMailUsager());
        assertEquals("1234567890", usager.getNumeroCarteTisseo());
        assertFalse(usager.isAdmin());
    }
    
    @Test
    public void testCreate() throws SQLException {
        Usager nouveau = new Usager("Martin", "Paul", "paul.martin@test.com", "newpass123");
        
        dao.create(nouveau);
        
        Usager verif = dao.findById("paul.martin@test.com");
        assertNotNull(verif);
        assertEquals("Martin", verif.getNomUsager());
        assertEquals("Paul", verif.getPrenomUsager());
        assertEquals("paul.martin@test.com", verif.getMailUsager());
    }
    
    @Test
    public void testUpdate() throws SQLException {
        Usager usager = dao.findById("jean.dupont@test.com");
        assertNotNull(usager);
        
        usager.setNomUsager("Dupont-Modifié");
        usager.setPrenomUsager("Jean-Paul");
        usager.setNumeroCarteTisseo("0987654321");
        
        dao.update(usager);
        
        Usager verif = dao.findById("jean.dupont@test.com");
        assertNotNull(verif);
        assertEquals("Dupont-Modifié", verif.getNomUsager());
        assertEquals("Jean-Paul", verif.getPrenomUsager());
        assertEquals("0987654321", verif.getNumeroCarteTisseo());
    }
    
    @Test
    public void testEmailExiste() throws SQLException {
        boolean existe = dao.emailExiste("jean.dupont@test.com");
        assertTrue(existe);
        
        boolean nExistePas = dao.emailExiste("inexistant@test.com");
        assertFalse(nExistePas);
    }
    
    @Test
    public void testModifierMotDePasse() throws SQLException {
        boolean result = dao.modifierMotDePasse("jean.dupont@test.com", "nouveaumdp456");
        assertTrue(result);
        
        // Vérifier que le mot de passe a été changé
        String sql = "SELECT mot_de_passe FROM Usager WHERE mail_usager = 'jean.dupont@test.com'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                assertEquals("nouveaumdp456", rs.getString("mot_de_passe"));
            }
        }
    }
    
    @Test
    public void testGetCarteTisseoByUsager() throws SQLException {
        String carte = dao.getCarteTisseoByUsager(1);
        assertEquals("1234567890", carte);
        
        String carteAdmin = dao.getCarteTisseoByUsager(2);
        assertNull(carteAdmin); // L'admin n'a pas de carte
    }
}
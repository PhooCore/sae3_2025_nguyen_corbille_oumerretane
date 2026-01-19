package modele.test;

import org.junit.Test;

import modele.dao.ModifMdpDAO;
import modele.dao.MySQLConnection;

import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.sql.*;

public class ModifMdpDAOTest {
    
    private ModifMdpDAO dao;
    private Connection conn;
    
    @Before
    public void setUp() throws SQLException {
        dao = new ModifMdpDAO();
        conn = MySQLConnection.getConnection();
        
        // Nettoyer et préparer la base de test
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Usager");
            
            // Insérer un utilisateur de test
            stmt.execute("INSERT INTO Usager (id_usager, nom_usager, prenom_usager, mail_usager, mot_de_passe) " +
                        "VALUES (999, 'Test', 'User', 'test.mdp@test.com', 'ancienmdp')");
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
    public void testModifierMotDePasse() {
        boolean result = dao.modifierMotDePasse("test.mdp@test.com", "nouveaumdp");
        assertTrue(result);
        
        // Vérifier que le mot de passe a été changé
        String sql = "SELECT mot_de_passe FROM Usager WHERE mail_usager = 'test.mdp@test.com'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                assertEquals("nouveaumdp", rs.getString("mot_de_passe"));
            }
        } catch (SQLException e) {
            fail("Erreur SQL: " + e.getMessage());
        }
    }
    
    @Test
    public void testVerifierEmailExiste() {
        boolean existe = dao.verifierEmailExiste("test.mdp@test.com");
        assertTrue(existe);
        
        boolean nExistePas = dao.verifierEmailExiste("inexistant@test.com");
        assertFalse(nExistePas);
    }
    
    @Test
    public void testVerifierAncienMotDePasse() {
        boolean correct = dao.verifierAncienMotDePasse("test.mdp@test.com", "ancienmdp");
        assertTrue(correct);
        
        boolean incorrect = dao.verifierAncienMotDePasse("test.mdp@test.com", "mauvaismdp");
        assertFalse(incorrect);
    }
    
    @Test
    public void testGetIdUsagerByEmail() {
        int id = dao.getIdUsagerByEmail("test.mdp@test.com");
        assertEquals(999, id);
        
        int idInexistant = dao.getIdUsagerByEmail("inexistant@test.com");
        assertEquals(-1, idInexistant);
    }
    
    @Test
    public void testGetInfosUsager() {
        String[] infos = dao.getInfosUsager("test.mdp@test.com");
        assertNotNull(infos);
        assertEquals(2, infos.length);
        assertEquals("Test", infos[0]);
        assertEquals("User", infos[1]);
        
        String[] infosInexistant = dao.getInfosUsager("inexistant@test.com");
        assertNull(infosInexistant);
    }
    
    @Test
    public void testVerifierForceMotDePasse() {
        // Test de différents niveaux de force
        assertFalse("Trop court", dao.verifierForceMotDePasse("abc"));
        assertFalse("Pas assez de critères", dao.verifierForceMotDePasse("abcdefgh"));
        
        // Bon mot de passe avec 3 critères
        assertTrue("MDP fort avec 3 critères", 
            dao.verifierForceMotDePasse("MotDePasse123")); // majuscule, minuscule, chiffre
        
        assertTrue("MDP fort avec caractère spécial",
            dao.verifierForceMotDePasse("motdepasse123!")); // minuscule, chiffre, caractère spécial
        
        // MDP parfait avec 4 critères
        assertTrue("MDP parfait avec 4 critères",
            dao.verifierForceMotDePasse("MotDePasse123!"));
    }
}
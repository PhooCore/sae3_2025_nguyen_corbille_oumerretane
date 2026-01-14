package modele.test;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import modele.Abonnement;
import modele.dao.AbonnementDAO;
import modele.dao.MySQLConnection;

import java.sql.*;
import java.util.List;
import java.time.LocalDateTime;

public class AbonnementDAOTest {
    
    private AbonnementDAO dao;
    private Connection conn;
    
    @Before
    public void setUp() throws SQLException {
        dao = AbonnementDAO.getInstance();
        conn = MySQLConnection.getConnection();
        
        // Nettoyer et préparer la base de test
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Appartenir");
            stmt.execute("DELETE FROM Abonnement");
            
            // Insérer des données de test
            stmt.execute("INSERT INTO Abonnement (id_abonnement, libelle_abonnement, tarif_applique) " +
                        "VALUES ('TEST_ABO_1', 'Test Abonnement 1', 10.00)");
            stmt.execute("INSERT INTO Abonnement (id_abonnement, libelle_abonnement, tarif_applique) " +
                        "VALUES ('TEST_ABO_2', 'Test Abonnement 2', 20.00)");
        }
    }
    
    @After
    public void tearDown() throws SQLException {
        // Nettoyer après les tests
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Appartenir");
            stmt.execute("DELETE FROM Abonnement");
        }
        
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    @Test
    public void testFindAll() throws SQLException {
        List<Abonnement> abonnements = dao.findAll();
        
        assertNotNull("La liste ne doit pas être null", abonnements);
        assertTrue("Doit contenir au moins 2 abonnements", abonnements.size() >= 2);
        
        boolean found1 = false;
        boolean found2 = false;
        for (Abonnement abo : abonnements) {
            if ("TEST_ABO_1".equals(abo.getIdAbonnement())) {
                found1 = true;
                assertEquals("Test Abonnement 1", abo.getLibelleAbonnement());
                assertEquals(10.00, abo.getTarifAbonnement(), 0.001);
            }
            if ("TEST_ABO_2".equals(abo.getIdAbonnement())) {
                found2 = true;
            }
        }
        
        assertTrue("TEST_ABO_1 doit être trouvé", found1);
        assertTrue("TEST_ABO_2 doit être trouvé", found2);
    }
    
    @Test
    public void testFindById() throws SQLException {
        Abonnement abo = dao.findById("TEST_ABO_1");
        
        assertNotNull("L'abonnement ne doit pas être null", abo);
        assertEquals("TEST_ABO_1", abo.getIdAbonnement());
        assertEquals("Test Abonnement 1", abo.getLibelleAbonnement());
        assertEquals(10.00, abo.getTarifAbonnement(), 0.001);
    }
    
    @Test
    public void testFindById_NotFound() throws SQLException {
        Abonnement abo = dao.findById("INEXISTANT");
        assertNull("Doit retourner null pour un ID inexistant", abo);
    }
    
    @Test
    public void testCreate() throws SQLException {
        Abonnement nouveau = new Abonnement();
        nouveau.setIdAbonnement("TEST_ABO_3");
        nouveau.setLibelleAbonnement("Test Abonnement 3");
        nouveau.setTarifAbonnement(30.00);
        
        dao.create(nouveau);
        
        // Vérifier que l'abonnement a été créé
        Abonnement verif = dao.findById("TEST_ABO_3");
        assertNotNull("L'abonnement doit être créé", verif);
        assertEquals("Test Abonnement 3", verif.getLibelleAbonnement());
        assertEquals(30.00, verif.getTarifAbonnement(), 0.001);
    }
    
    @Test
    public void testUpdate() throws SQLException {
        Abonnement abo = dao.findById("TEST_ABO_1");
        assertNotNull(abo);
        
        abo.setLibelleAbonnement("Test Modifié");
        abo.setTarifAbonnement(15.00);
        
        dao.update(abo);
        
        // Vérifier la mise à jour
        Abonnement verif = dao.findById("TEST_ABO_1");
        assertNotNull(verif);
        assertEquals("Test Modifié", verif.getLibelleAbonnement());
        assertEquals(15.00, verif.getTarifAbonnement(), 0.001);
    }
    
    @Test
    public void testDelete() throws SQLException {
        Abonnement abo = dao.findById("TEST_ABO_1");
        assertNotNull(abo);
        
        dao.delete(abo);
        
        // Vérifier la suppression
        Abonnement verif = dao.findById("TEST_ABO_1");
        assertNull("L'abonnement doit être supprimé", verif);
    }
    
    @Test
    public void testAjouterAbonnementUtilisateur() throws SQLException {
        // Créer un utilisateur de test
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Usager (id_usager, nom_usager, prenom_usager, mail_usager, mot_de_passe) " +
                        "VALUES (9999, 'Test', 'User', 'test@test.com', 'mdp')");
        }
        
        boolean result = dao.ajouterAbonnementUtilisateur(9999, "TEST_ABO_1");
        assertTrue("Doit retourner true", result);
        
        // Vérifier l'association dans Appartenir
        String sql = "SELECT COUNT(*) FROM Appartenir WHERE id_usager = 9999 AND id_abonnement = 'TEST_ABO_1'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                assertEquals(1, rs.getInt(1));
            }
        }
    }
    
    @Test
    public void testAjouterAbonnementUtilisateur_AbonnementInexistant() throws SQLException {
        boolean result = dao.ajouterAbonnementUtilisateur(9999, "INEXISTANT");
        assertFalse("Doit retourner false pour abonnement inexistant", result);
    }
    
    @Test
    public void testSupprimerAbonnementsUtilisateur() throws SQLException {
        // Créer un utilisateur et lui ajouter un abonnement
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Usager (id_usager, nom_usager, prenom_usager, mail_usager, mot_de_passe) " +
                        "VALUES (9998, 'Test', 'User', 'test2@test.com', 'mdp')");
            stmt.execute("INSERT INTO Appartenir (id_usager, id_abonnement) VALUES (9998, 'TEST_ABO_1')");
        }
        
        dao.supprimerAbonnementsUtilisateur(9998);
        
        // Vérifier la suppression
        String sql = "SELECT COUNT(*) FROM Appartenir WHERE id_usager = 9998";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                assertEquals(0, rs.getInt(1));
            }
        }
    }
}
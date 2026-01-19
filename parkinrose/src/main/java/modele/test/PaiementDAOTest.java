package modele.test;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import modele.Paiement;
import modele.dao.MySQLConnection;
import modele.dao.PaiementDAO;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaiementDAOTest {
    
    private PaiementDAO dao;
    private Connection conn;
    private int testUserId;
    
    @Before
    public void setUp() throws SQLException {
        dao = PaiementDAO.getInstance();
        conn = MySQLConnection.getConnection();
        
        // Nettoyer et préparer la base de test
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Paiement");
            stmt.execute("DELETE FROM Abonnement");
            stmt.execute("DELETE FROM Usager");
            
            // Créer un utilisateur de test
            stmt.execute("INSERT INTO Usager (nom_usager, prenom_usager, mail_usager, mot_de_passe) " +
                        "VALUES ('Test', 'User', 'test@paiement.com', 'mdp')");
            
            // Récupérer l'ID
            ResultSet rs = stmt.executeQuery("SELECT id_usager FROM Usager WHERE mail_usager = 'test@paiement.com'");
            if (rs.next()) {
                testUserId = rs.getInt("id_usager");
            }
            
            // Créer un abonnement de test
            stmt.execute("INSERT INTO Abonnement (id_abonnement, libelle_abonnement, tarif_applique) " +
                        "VALUES ('TEST_ABO', 'Test Abonnement', 10.00)");
            
            // Format de date pour MySQL
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String now = LocalDateTime.now().format(formatter);
            
            // Insérer des paiements de test avec type_paiement défini
            stmt.execute("INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, " +
                        "id_abonnement, montant, id_usager, date_paiement, methode_paiement, statut) " +
                        "VALUES ('PAY_1', 'John Doe', '1234567890123456', '123', 'TEST_ABO', " +
                        "10.00, " + testUserId + ", '" + now + "', 'CARTE', 'REUSSI')");
            
            stmt.execute("INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, " +
                        "montant, id_usager, date_paiement, methode_paiement, statut) " +
                        "VALUES ('PAY_2', 'Jane Doe', '9876543210987654', '456', " +
                        "5.50, " + testUserId + ", '" + now + "', 'CARTE', 'REUSSI')");
        }
    }
    
    @After
    public void tearDown() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Paiement");
            stmt.execute("DELETE FROM Abonnement");
            stmt.execute("DELETE FROM Usager");
        }
        
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    @Test
    public void testFindAll() throws SQLException {
        List<Paiement> paiements = dao.findAll();
        
        assertNotNull(paiements);
        assertTrue("Doit contenir au moins 2 paiements", paiements.size() >= 2);
        
        boolean found1 = false;
        boolean found2 = false;
        for (Paiement p : paiements) {
            if ("PAY_1".equals(p.getIdPaiement())) {
                found1 = true;
                assertEquals("John Doe", p.getNomCarte());
                assertEquals("TEST_ABO", p.getIdAbonnement());
                assertEquals(10.00, p.getMontant(), 0.001);
                assertEquals(testUserId, p.getIdUsager());
                assertEquals("Abonnement", p.getTypePaiement()); // Vérifier la casse
            }
            if ("PAY_2".equals(p.getIdPaiement())) {
                found2 = true;
                assertNull(p.getIdAbonnement());
                assertEquals("Stationnement", p.getTypePaiement()); // Vérifier la casse
            }
        }
        
        assertTrue("PAY_1 doit être trouvé", found1);
        assertTrue("PAY_2 doit être trouvé", found2);
    }
    
    @Test
    public void testFindById() throws SQLException {
        Paiement paiement = dao.findById("PAY_1");
        
        assertNotNull(paiement);
        assertEquals("PAY_1", paiement.getIdPaiement());
        assertEquals("John Doe", paiement.getNomCarte());
        assertEquals("TEST_ABO", paiement.getIdAbonnement());
        assertEquals(10.00, paiement.getMontant(), 0.001);
        assertEquals(testUserId, paiement.getIdUsager());
        assertEquals("Abonnement", paiement.getTypePaiement()); // Vérifier la casse
    }
    
    @Test
    public void testCreate() throws SQLException {
        Paiement nouveau = new Paiement(
            "Test User",
            "1111222233334444",
            "789",
            25.50,
            testUserId
        );
        nouveau.setIdPaiement("PAY_3");
        // Note: Le type sera déterminé automatiquement par getTypePaiement()
        
        dao.create(nouveau);
        
        Paiement verif = dao.findById("PAY_3");
        assertNotNull(verif);
        assertEquals("Test User", verif.getNomCarte());
        assertEquals(25.50, verif.getMontant(), 0.001);
        assertEquals(testUserId, verif.getIdUsager());
        assertEquals("Stationnement", verif.getTypePaiement()); // Vérifier la casse
    }
    
    @Test
    public void testCreateWithAbonnement() throws SQLException {
        Paiement nouveau = new Paiement(
            "Test User",
            "1111222233334444",
            "789",
            30.00,
            testUserId,
            "TEST_ABO"
        );
        nouveau.setIdPaiement("PAY_4");
        
        dao.create(nouveau);
        
        Paiement verif = dao.findById("PAY_4");
        assertNotNull(verif);
        assertEquals("TEST_ABO", verif.getIdAbonnement());
        assertEquals("Abonnement", verif.getTypePaiement()); // Vérifier la casse
    }
    
    @Test
    public void testGetPaiementsByUsager() throws SQLException {
        List<Paiement> paiements = dao.getPaiementsByUsager(testUserId);
        
        assertNotNull(paiements);
        assertTrue(paiements.size() >= 2);
        
        for (Paiement p : paiements) {
            assertEquals(testUserId, p.getIdUsager());
        }
    }
    
    @Test
    public void testGetPaiementsAbonnementByUsager() throws SQLException {
        List<Paiement> paiements = dao.getPaiementsAbonnementByUsager(testUserId);
        
        assertNotNull(paiements);
        assertEquals(1, paiements.size()); // Seulement PAY_1 a un abonnement
        
        Paiement p = paiements.get(0);
        assertEquals("PAY_1", p.getIdPaiement());
        assertNotNull(p.getIdAbonnement());
        assertEquals("Abonnement", p.getTypePaiement()); // Vérifier la casse
    }
    
    @Test
    public void testGetPaiementsStationnementByUsager() throws SQLException {
        List<Paiement> paiements = dao.getPaiementsStationnementByUsager(testUserId);
        
        assertNotNull(paiements);
        assertEquals(1, paiements.size()); // Seulement PAY_2 n'a pas d'abonnement
        
        Paiement p = paiements.get(0);
        assertEquals("PAY_2", p.getIdPaiement());
        assertNull(p.getIdAbonnement());
        assertEquals("Stationnement", p.getTypePaiement()); // Vérifier la casse
    }
    
    @Test
    public void testPaiementExiste() throws SQLException {
        boolean existe = dao.paiementExiste("PAY_1");
        assertTrue(existe);
        
        boolean nExistePas = dao.paiementExiste("INEXISTANT");
        assertFalse(nExistePas);
    }
    
    @Test
    public void testGetTotalDepenses() throws SQLException {
        double total = dao.getTotalDepenses(testUserId);
        assertEquals(15.50, total, 0.001); // 10.00 + 5.50
    }
    
    @Test
    public void testGetDernierPaiement() throws SQLException {
        Paiement dernier = dao.getDernierPaiement(testUserId);
        assertNotNull(dernier);
        // PAY_2 devrait être le plus récent (inséré après PAY_1 dans setUp)
        assertEquals("PAY_2", dernier.getIdPaiement());
    }
    
    @Test
    public void testGetTypePaiementLogic() {
        // Test de la logique de détermination du type
        Paiement p1 = new Paiement();
        p1.setIdAbonnement("ABO_1");
        assertEquals("Abonnement", p1.getTypePaiement());
        
        Paiement p2 = new Paiement();
        p2.setIdAbonnement("");
        assertEquals("Stationnement", p2.getTypePaiement());
        
        Paiement p3 = new Paiement();
        p3.setIdAbonnement(null);
        assertEquals("Stationnement", p3.getTypePaiement());
        
        Paiement p4 = new Paiement();
        p4.setTypePaiement("Autre");
        assertEquals("Autre", p4.getTypePaiement());
    }
}
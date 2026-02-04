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

/**
 * Classe de tests unitaires pour la classe UsagerDAO.
 * Teste les opérations CRUD (Create, Read, Update) sur les utilisateurs.
 */
public class UsagerDAOTest {
    
    private UsagerDAO dao;      // Instance du DAO à tester
    private Connection conn;    // Connexion à la base de données de test
    
    /**
     * Méthode exécutée avant chaque test.
     * Initialise l'environnement de test avec des données propres.
     * @throws SQLException en cas d'erreur de connexion ou d'exécution SQL
     */
    @Before
    public void setUp() throws SQLException {
        // Récupération de l'instance singleton du DAO
        dao = UsagerDAO.getInstance();
        
        // Établissement de la connexion à la base de test
        conn = MySQLConnection.getConnection();
        
        // Nettoyage et préparation de la base de test
        try (Statement stmt = conn.createStatement()) {
            // Suppression de toutes les données existantes pour garantir un état propre
            stmt.execute("DELETE FROM Usager");
            
            /**
             * Insertion d'utilisateurs de test avec différents profils :
             * 1. Un utilisateur standard avec carte Tisséo
             * 2. Un administrateur sans carte Tisséo
             */
            
            // Utilisateur standard (non-admin) avec carte Tisséo
            stmt.execute("INSERT INTO Usager (id_usager, nom_usager, prenom_usager, mail_usager, " +
                        "mot_de_passe, numero_carte_tisseo, is_admin) " +
                        "VALUES (1, 'Dupont', 'Jean', 'jean.dupont@test.com', 'mdp123', '1234567890', 0)");
            
            // Administrateur (is_admin = 1) sans carte Tisséo
            stmt.execute("INSERT INTO Usager (id_usager, nom_usager, prenom_usager, mail_usager, " +
                        "mot_de_passe, is_admin) " +
                        "VALUES (2, 'Admin', 'System', 'admin@test.com', 'admin123', 1)");
        }
    }
    
    /**
     * Méthode exécutée après chaque test.
     * Nettoie les données et ferme les ressources.
     * @throws SQLException en cas d'erreur lors du nettoyage
     */
    @After
    public void tearDown() throws SQLException {
        // Suppression de toutes les données de test
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Usager");
        }
        
        // Fermeture de la connexion si elle est ouverte
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    /**
     * Test de la méthode findAll().
     * Vérifie que tous les utilisateurs sont correctement récupérés.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Récupération de tous les utilisateurs
        List<Usager> usagers = dao.findAll();
        
        // Vérifications de base
        assertNotNull("La liste des utilisateurs ne doit pas être null", usagers);
        assertEquals("Devrait avoir 2 utilisateurs", 2, usagers.size());
        
        // Vérification détaillée des utilisateurs récupérés
        boolean foundUser = false;      // Utilisateur standard trouvé ?
        boolean foundAdmin = false;     // Administrateur trouvé ?
        
        for (Usager u : usagers) {
            if (u.getMailUsager().equals("jean.dupont@test.com")) {
                foundUser = true;
                assertEquals("Nom incorrect pour Jean Dupont", "Dupont", u.getNomUsager());
                assertEquals("Prénom incorrect pour Jean Dupont", "Jean", u.getPrenomUsager());
                assertEquals("Carte Tisséo incorrecte", "1234567890", u.getNumeroCarteTisseo());
                assertFalse("Jean Dupont ne devrait pas être admin", u.isAdmin());
            }
            if (u.getMailUsager().equals("admin@test.com")) {
                foundAdmin = true;
                assertTrue("admin@test.com devrait être admin", u.isAdmin());
            }
        }
        
        // Vérification que les deux utilisateurs ont été trouvés
        assertTrue("L'utilisateur standard devrait être dans la liste", foundUser);
        assertTrue("L'administrateur devrait être dans la liste", foundAdmin);
    }
    
    /**
     * Test de la méthode findById().
     * Vérifie la récupération d'un utilisateur par son email.
     */
    @Test
    public void testFindById() throws SQLException {
        // Recherche de l'utilisateur par son email
        Usager usager = dao.findById("jean.dupont@test.com");
        
        // Vérifications
        assertNotNull("L'utilisateur devrait être trouvé", usager);
        assertEquals("Nom incorrect", "Dupont", usager.getNomUsager());
        assertEquals("Prénom incorrect", "Jean", usager.getPrenomUsager());
        assertEquals("Email incorrect", "jean.dupont@test.com", usager.getMailUsager());
        assertEquals("Carte Tisséo incorrecte", "1234567890", usager.getNumeroCarteTisseo());
        assertFalse("Ne devrait pas être admin", usager.isAdmin());
    }
    
    /**
     * Test de la méthode create().
     * Vérifie la création d'un nouvel utilisateur.
     */
    @Test
    public void testCreate() throws SQLException {
        // Création d'un nouvel utilisateur
        Usager nouveau = new Usager("Martin", "Paul", "paul.martin@test.com", "newpass123");
        
        // Appel de la méthode à tester
        dao.create(nouveau);
        
        // Vérification que l'utilisateur a bien été créé
        Usager verif = dao.findById("paul.martin@test.com");
        assertNotNull("Le nouvel utilisateur devrait être créé", verif);
        assertEquals("Nom incorrect", "Martin", verif.getNomUsager());
        assertEquals("Prénom incorrect", "Paul", verif.getPrenomUsager());
        assertEquals("Email incorrect", "paul.martin@test.com", verif.getMailUsager());
    }
    
    /**
     * Test de la méthode update().
     * Vérifie la mise à jour des informations d'un utilisateur existant.
     */
    @Test
    public void testUpdate() throws SQLException {
        // Récupération de l'utilisateur à modifier
        Usager usager = dao.findById("jean.dupont@test.com");
        assertNotNull("L'utilisateur à modifier devrait exister", usager);
        
        // Modification des informations
        usager.setNomUsager("Dupont-Modifié");
        usager.setPrenomUsager("Jean-Paul");
        usager.setNumeroCarteTisseo("0987654321");
        
        // Mise à jour dans la base
        dao.update(usager);
        
        // Vérification des modifications
        Usager verif = dao.findById("jean.dupont@test.com");
        assertNotNull("L'utilisateur modifié devrait toujours exister", verif);
        assertEquals("Nom modifié incorrect", "Dupont-Modifié", verif.getNomUsager());
        assertEquals("Prénom modifié incorrect", "Jean-Paul", verif.getPrenomUsager());
        assertEquals("Carte Tisséo modifiée incorrecte", "0987654321", verif.getNumeroCarteTisseo());
    }
    
    /**
     * Test de la méthode emailExiste().
     * Vérifie la détection d'un email existant/non-existant.
     */
    @Test
    public void testEmailExiste() throws SQLException {
        // Test avec un email existant
        boolean existe = dao.emailExiste("jean.dupont@test.com");
        assertTrue("L'email jean.dupont@test.com devrait exister", existe);
        
        // Test avec un email non existant
        boolean nExistePas = dao.emailExiste("inexistant@test.com");
        assertFalse("L'email inexistant@test.com ne devrait pas exister", nExistePas);
    }
    
    /**
     * Test de la méthode modifierMotDePasse().
     * Vérifie le changement de mot de passe d'un utilisateur.
     */
    @Test
    public void testModifierMotDePasse() throws SQLException {
        // Changement du mot de passe
        boolean result = dao.modifierMotDePasse("jean.dupont@test.com", "nouveaumdp456");
        assertTrue("Le changement de mot de passe devrait réussir", result);
        
        // Vérification directe dans la base de données
        String sql = "SELECT mot_de_passe FROM Usager WHERE mail_usager = 'jean.dupont@test.com'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                assertEquals("Le mot de passe devrait être modifié", 
                             "nouveaumdp456", 
                             rs.getString("mot_de_passe"));
            } else {
                fail("L'utilisateur devrait exister");
            }
        }
    }
    
    /**
     * Test de la méthode getCarteTisseoByUsager().
     * Vérifie la récupération du numéro de carte Tisséo par ID utilisateur.
     */
    @Test
    public void testGetCarteTisseoByUsager() throws SQLException {
        // Test avec un utilisateur ayant une carte Tisséo
        String carte = dao.getCarteTisseoByUsager(1);
        assertEquals("Carte Tisséo incorrecte pour l'utilisateur 1", 
                     "1234567890", 
                     carte);
        
        // Test avec un utilisateur sans carte Tisséo (admin)
        String carteAdmin = dao.getCarteTisseoByUsager(2);
        assertNull("L'admin ne devrait pas avoir de carte Tisséo", carteAdmin);
    }
}
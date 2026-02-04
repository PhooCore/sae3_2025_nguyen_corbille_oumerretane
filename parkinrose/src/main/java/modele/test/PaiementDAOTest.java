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

/**
 * Classe de tests unitaires pour la classe PaiementDAO.
 * 
 * Cette classe teste les opérations CRUD sur les paiements ainsi que
 * les méthodes spécifiques de requêtage par utilisateur.
 * 
 * Note: Les tests utilisent une base de données de test isolée.
 */
public class PaiementDAOTest {
    
    // ==================== VARIABLES DE TEST ====================
    
    private PaiementDAO dao;        // DAO à tester (singleton)
    private Connection conn;        // Connexion à la base de données
    private int testUserId;         // ID de l'utilisateur de test
    
    // Constantes pour les tests
    private static final String ID_PAIEMENT_1 = "PAY_1";
    private static final String ID_PAIEMENT_2 = "PAY_2";
    private static final String ID_PAIEMENT_3 = "PAY_3";
    private static final String ID_PAIEMENT_4 = "PAY_4";
    private static final String ID_PAIEMENT_INEXISTANT = "INEXISTANT";
    private static final String NOM_CARTE_1 = "John Doe";
    private static final String NOM_CARTE_2 = "Jane Doe";
    private static final String NUMERO_CARTE_1 = "1234567890123456";
    private static final String NUMERO_CARTE_2 = "9876543210987654";
    private static final String CODE_SECRET_1 = "123";
    private static final String CODE_SECRET_2 = "456";
    private static final String ID_ABONNEMENT_TEST = "TEST_ABO";
    private static final String LIBELLE_ABONNEMENT = "Test Abonnement";
    private static final double MONTANT_1 = 10.00;
    private static final double MONTANT_2 = 5.50;
    private static final double MONTANT_3 = 25.50;
    private static final double MONTANT_4 = 30.00;
    private static final String MAIL_USAGER_TEST = "test@paiement.com";
    private static final String NOM_USAGER_TEST = "Test";
    private static final String PRENOM_USAGER_TEST = "User";
    private static final String MOT_DE_PASSE_TEST = "mdp";
    private static final String METHODE_PAIEMENT = "CARTE";
    private static final String STATUT_REUSSI = "REUSSI";
    
    // ==================== MÉTHODES DE CONFIGURATION ====================
    
    /**
     * Méthode exécutée avant chaque test.
     * Initialise le DAO, établit la connexion et prépare l'environnement de test.
     * 
     * @throws SQLException en cas d'erreur de connexion ou d'exécution SQL
     */
    @Before
    public void setUp() throws SQLException {
        // 1. Initialiser le DAO (singleton)
        dao = PaiementDAO.getInstance();
        
        // 2. Obtenir une connexion à la base de données
        conn = MySQLConnection.getConnection();
        
        // 3. Préparer la base de données pour les tests
        preparerBaseDeDonnees();
    }
    
    /**
     * Prépare la base de données en nettoyant les tables et en insérant des données de test.
     * 
     * @throws SQLException en cas d'erreur SQL
     */
    private void preparerBaseDeDonnees() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 1. Nettoyer les tables (dans l'ordre des dépendances)
            stmt.execute("DELETE FROM Paiement");
            stmt.execute("DELETE FROM Abonnement");
            stmt.execute("DELETE FROM Usager");
            
            // 2. Créer un utilisateur de test
            creerUsagerTest(stmt);
            
            // 3. Créer un abonnement de test
            creerAbonnementTest(stmt);
            
            // 4. Insérer des paiements de test
            insererPaiementsTest(stmt);
        }
    }
    
    /**
     * Crée un utilisateur de test dans la base.
     * 
     * @param stmt Statement pour exécuter les requêtes
     * @throws SQLException en cas d'erreur SQL
     */
    private void creerUsagerTest(Statement stmt) throws SQLException {
        String sql = "INSERT INTO Usager (nom_usager, prenom_usager, mail_usager, mot_de_passe) " +
                    "VALUES ('" + NOM_USAGER_TEST + "', '" + PRENOM_USAGER_TEST + "', '" + 
                    MAIL_USAGER_TEST + "', '" + MOT_DE_PASSE_TEST + "')";
        stmt.execute(sql);
        
        // Récupérer l'ID généré
        ResultSet rs = stmt.executeQuery(
            "SELECT id_usager FROM Usager WHERE mail_usager = '" + MAIL_USAGER_TEST + "'"
        );
        if (rs.next()) {
            testUserId = rs.getInt("id_usager");
        }
    }
    
    /**
     * Crée un abonnement de test dans la base.
     * 
     * @param stmt Statement pour exécuter les requêtes
     * @throws SQLException en cas d'erreur SQL
     */
    private void creerAbonnementTest(Statement stmt) throws SQLException {
        String sql = "INSERT INTO Abonnement (id_abonnement, libelle_abonnement, tarif_applique) " +
                    "VALUES ('" + ID_ABONNEMENT_TEST + "', '" + LIBELLE_ABONNEMENT + "', " + MONTANT_1 + ")";
        stmt.execute(sql);
    }
    
    /**
     * Insère des paiements de test dans la base.
     * 
     * @param stmt Statement pour exécuter les requêtes
     * @throws SQLException en cas d'erreur SQL
     */
    private void insererPaiementsTest(Statement stmt) throws SQLException {
        // Format de date pour MySQL
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        
        // Paiement 1: Avec abonnement
        String sql1 = "INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, " +
                     "id_abonnement, montant, id_usager, date_paiement, methode_paiement, statut) " +
                     "VALUES ('" + ID_PAIEMENT_1 + "', '" + NOM_CARTE_1 + "', '" + NUMERO_CARTE_1 + 
                     "', '" + CODE_SECRET_1 + "', '" + ID_ABONNEMENT_TEST + "', " + MONTANT_1 + 
                     ", " + testUserId + ", '" + now + "', '" + METHODE_PAIEMENT + "', '" + STATUT_REUSSI + "')";
        stmt.execute(sql1);
        
        // Paiement 2: Sans abonnement (stationnement)
        String sql2 = "INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, " +
                     "montant, id_usager, date_paiement, methode_paiement, statut) " +
                     "VALUES ('" + ID_PAIEMENT_2 + "', '" + NOM_CARTE_2 + "', '" + NUMERO_CARTE_2 + 
                     "', '" + CODE_SECRET_2 + "', " + MONTANT_2 + ", " + testUserId + 
                     ", '" + now + "', '" + METHODE_PAIEMENT + "', '" + STATUT_REUSSI + "')";
        stmt.execute(sql2);
    }
    
    /**
     * Méthode exécutée après chaque test.
     * Nettoie les données de test et ferme la connexion.
     * 
     * @throws SQLException en cas d'erreur de fermeture de connexion
     */
    @After
    public void tearDown() throws SQLException {
        // 1. Nettoyer les données de test
        nettoyerBaseDeDonnees();
        
        // 2. Fermer la connexion si elle est ouverte
        fermerConnexion();
    }
    
    /**
     * Nettoie les données de test de la base de données.
     * 
     * @throws SQLException en cas d'erreur SQL
     */
    private void nettoyerBaseDeDonnees() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Supprimer dans l'ordre inverse des dépendances
            stmt.execute("DELETE FROM Paiement");
            stmt.execute("DELETE FROM Abonnement");
            stmt.execute("DELETE FROM Usager");
        }
    }
    
    /**
     * Ferme proprement la connexion à la base de données.
     * 
     * @throws SQLException en cas d'erreur de fermeture
     */
    private void fermerConnexion() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            // Ne pas fermer la connexion si elle est partagée
            // (dépend de l'implémentation de MySQLConnection)
            // conn.close();
        }
    }
    
    // ==================== TESTS DES MÉTHODES CRUD ====================
    
    /**
     * Test de la méthode findAll().
     * Vérifie que tous les paiements sont récupérés.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testFindAll() throws SQLException {
        // 1. Exécuter la méthode à tester
        List<Paiement> paiements = dao.findAll();
        
        // 2. Vérifier les assertions de base
        assertNotNull("La liste ne doit pas être null", paiements);
        assertTrue("Doit contenir au moins 2 paiements (ceux insérés en setup)", 
                  paiements.size() >= 2);
        
        // 3. Vérifier que les paiements de test sont présents
        boolean found1 = false;
        boolean found2 = false;
        for (Paiement p : paiements) {
            if (ID_PAIEMENT_1.equals(p.getIdPaiement())) {
                found1 = true;
                verifierPaiement1(p);
            }
            if (ID_PAIEMENT_2.equals(p.getIdPaiement())) {
                found2 = true;
                verifierPaiement2(p);
            }
        }
        
        // 4. Vérifier que tous les paiements attendus ont été trouvés
        assertTrue(ID_PAIEMENT_1 + " doit être trouvé", found1);
        assertTrue(ID_PAIEMENT_2 + " doit être trouvé", found2);
    }
    
    /**
     * Vérifie les attributs du paiement 1 (avec abonnement).
     * 
     * @param p Le paiement à vérifier
     */
    private void verifierPaiement1(Paiement p) {
        assertEquals("Nom carte incorrect pour " + ID_PAIEMENT_1, NOM_CARTE_1, p.getNomCarte());
        assertEquals("ID abonnement incorrect pour " + ID_PAIEMENT_1, ID_ABONNEMENT_TEST, p.getIdAbonnement());
        assertEquals("Montant incorrect pour " + ID_PAIEMENT_1, MONTANT_1, p.getMontant(), 0.001);
        assertEquals("ID usager incorrect pour " + ID_PAIEMENT_1, testUserId, p.getIdUsager());
        assertEquals("Type paiement incorrect pour " + ID_PAIEMENT_1, 
                    "Abonnement", p.getTypePaiement());
    }
    
    /**
     * Vérifie les attributs du paiement 2 (sans abonnement).
     * 
     * @param p Le paiement à vérifier
     */
    private void verifierPaiement2(Paiement p) {
        assertEquals("Nom carte incorrect pour " + ID_PAIEMENT_2, NOM_CARTE_2, p.getNomCarte());
        assertNull("ID abonnement doit être null pour " + ID_PAIEMENT_2, p.getIdAbonnement());
        assertEquals("Type paiement incorrect pour " + ID_PAIEMENT_2, 
                    "Stationnement", p.getTypePaiement());
    }
    
    /**
     * Test de la méthode findById() avec un ID existant.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testFindById() throws SQLException {
        // 1. Exécuter la méthode à tester
        Paiement paiement = dao.findById(ID_PAIEMENT_1);
        
        // 2. Vérifier les assertions
        assertNotNull("Le paiement ne doit pas être null", paiement);
        assertEquals("ID incorrect", ID_PAIEMENT_1, paiement.getIdPaiement());
        assertEquals("Nom carte incorrect", NOM_CARTE_1, paiement.getNomCarte());
        assertEquals("ID abonnement incorrect", ID_ABONNEMENT_TEST, paiement.getIdAbonnement());
        assertEquals("Montant incorrect", MONTANT_1, paiement.getMontant(), 0.001);
        assertEquals("ID usager incorrect", testUserId, paiement.getIdUsager());
        assertEquals("Type paiement incorrect", "Abonnement", paiement.getTypePaiement());
    }
    
    /**
     * Test de la méthode findById() avec un ID inexistant.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testFindById_NotFound() throws SQLException {
        // 1. Exécuter la méthode à tester
        Paiement paiement = dao.findById(ID_PAIEMENT_INEXISTANT);
        
        // 2. Vérifier que null est retourné
        assertNull("Doit retourner null pour un ID inexistant", paiement);
    }
    
    /**
     * Test de la méthode create() pour un paiement de stationnement.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testCreate() throws SQLException {
        // 1. Créer un nouveau paiement
        Paiement nouveau = new Paiement(
            "Test User",
            "1111222233334444",
            "789",
            MONTANT_3,
            testUserId
        );
        nouveau.setIdPaiement(ID_PAIEMENT_3);
        // Note: Le type sera déterminé automatiquement par getTypePaiement()
        
        // 2. Exécuter la méthode à tester
        dao.create(nouveau);
        
        // 3. Vérifier que le paiement a été créé
        Paiement verif = dao.findById(ID_PAIEMENT_3);
        assertNotNull("Le paiement doit être créé", verif);
        assertEquals("Nom carte incorrect après création", "Test User", verif.getNomCarte());
        assertEquals("Montant incorrect après création", MONTANT_3, verif.getMontant(), 0.001);
        assertEquals("ID usager incorrect après création", testUserId, verif.getIdUsager());
        assertEquals("Type paiement incorrect après création", 
                    "Stationnement", verif.getTypePaiement());
    }
    
    /**
     * Test de la méthode create() pour un paiement d'abonnement.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testCreateWithAbonnement() throws SQLException {
        // 1. Créer un nouveau paiement avec abonnement
        Paiement nouveau = new Paiement(
            "Test User",
            "1111222233334444",
            "789",
            MONTANT_4,
            testUserId,
            ID_ABONNEMENT_TEST
        );
        nouveau.setIdPaiement(ID_PAIEMENT_4);
        
        // 2. Exécuter la méthode à tester
        dao.create(nouveau);
        
        // 3. Vérifier que le paiement a été créé
        Paiement verif = dao.findById(ID_PAIEMENT_4);
        assertNotNull("Le paiement doit être créé", verif);
        assertEquals("ID abonnement incorrect après création", 
                    ID_ABONNEMENT_TEST, verif.getIdAbonnement());
        assertEquals("Type paiement incorrect après création", 
                    "Abonnement", verif.getTypePaiement());
    }
    
    // ==================== TESTS DES MÉTHODES SPÉCIFIQUES ====================
    
    /**
     * Test de la méthode getPaiementsByUsager().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetPaiementsByUsager() throws SQLException {
        // 1. Exécuter la méthode à tester
        List<Paiement> paiements = dao.getPaiementsByUsager(testUserId);
        
        // 2. Vérifier les assertions
        assertNotNull("La liste ne doit pas être null", paiements);
        assertTrue("Doit contenir au moins 2 paiements", paiements.size() >= 2);
        
        // 3. Vérifier que tous les paiements appartiennent à l'utilisateur test
        for (Paiement p : paiements) {
            assertEquals("Tous les paiements doivent appartenir à l'utilisateur test", 
                        testUserId, p.getIdUsager());
        }
    }
    
    /**
     * Test de la méthode getPaiementsByUsager() avec un ID inexistant.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetPaiementsByUsager_UserInexistant() throws SQLException {
        // 1. Exécuter la méthode avec un ID inexistant
        List<Paiement> paiements = dao.getPaiementsByUsager(-1);
        
        // 2. Vérifier qu'une liste vide est retournée
        assertNotNull("La liste ne doit pas être null même si vide", paiements);
        assertTrue("La liste doit être vide pour un utilisateur inexistant", 
                  paiements.isEmpty());
    }
    
    /**
     * Test de la méthode getPaiementsAbonnementByUsager().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetPaiementsAbonnementByUsager() throws SQLException {
        // 1. Exécuter la méthode à tester
        List<Paiement> paiements = dao.getPaiementsAbonnementByUsager(testUserId);
        
        // 2. Vérifier les assertions
        assertNotNull("La liste ne doit pas être null", paiements);
        assertEquals("Doit contenir exactement 1 paiement d'abonnement", 1, paiements.size());
        
        // 3. Vérifier le paiement récupéré
        Paiement p = paiements.get(0);
        assertEquals("ID paiement incorrect", ID_PAIEMENT_1, p.getIdPaiement());
        assertNotNull("L'abonnement ne doit pas être null", p.getIdAbonnement());
        assertEquals("Type paiement incorrect", "Abonnement", p.getTypePaiement());
    }
    
    /**
     * Test de la méthode getPaiementsStationnementByUsager().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetPaiementsStationnementByUsager() throws SQLException {
        // 1. Exécuter la méthode à tester
        List<Paiement> paiements = dao.getPaiementsStationnementByUsager(testUserId);
        
        // 2. Vérifier les assertions
        assertNotNull("La liste ne doit pas être null", paiements);
        assertEquals("Doit contenir exactement 1 paiement de stationnement", 1, paiements.size());
        
        // 3. Vérifier le paiement récupéré
        Paiement p = paiements.get(0);
        assertEquals("ID paiement incorrect", ID_PAIEMENT_2, p.getIdPaiement());
        assertNull("L'abonnement doit être null", p.getIdAbonnement());
        assertEquals("Type paiement incorrect", "Stationnement", p.getTypePaiement());
    }
    
    /**
     * Test de la méthode paiementExiste().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testPaiementExiste() throws SQLException {
        // 1. Vérifier un paiement qui existe
        boolean existe = dao.paiementExiste(ID_PAIEMENT_1);
        assertTrue("Le paiement existant doit être trouvé", existe);
        
        // 2. Vérifier un paiement qui n'existe pas
        boolean nExistePas = dao.paiementExiste(ID_PAIEMENT_INEXISTANT);
        assertFalse("Le paiement inexistant ne doit pas être trouvé", nExistePas);
    }
    
    /**
     * Test de la méthode getTotalDepenses().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetTotalDepenses() throws SQLException {
        // 1. Exécuter la méthode à tester
        double total = dao.getTotalDepenses(testUserId);
        
        // 2. Vérifier le calcul (10.00 + 5.50 = 15.50)
        assertEquals("Total des dépenses incorrect", 15.50, total, 0.001);
    }
    
    /**
     * Test de la méthode getTotalDepenses() avec un utilisateur sans paiements.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetTotalDepenses_UserSansPaiements() throws SQLException {
        // 1. Créer un utilisateur sans paiements
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Usager (nom_usager, prenom_usager, mail_usager, mot_de_passe) " +
                        "VALUES ('Sans', 'Paiements', 'sans@paiements.com', 'mdp')");
            
            ResultSet rs = stmt.executeQuery(
                "SELECT id_usager FROM Usager WHERE mail_usager = 'sans@paiements.com'"
            );
            if (rs.next()) {
                int userIdSansPaiements = rs.getInt("id_usager");
                
                // 2. Exécuter la méthode
                double total = dao.getTotalDepenses(userIdSansPaiements);
                
                // 3. Vérifier que le total est 0
                assertEquals("Total doit être 0 pour utilisateur sans paiements", 
                            0.0, total, 0.001);
            }
        }
    }
    
    /**
     * Test de la méthode getDernierPaiement().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetDernierPaiement() throws SQLException {
        // 1. Exécuter la méthode à tester
        Paiement dernier = dao.getDernierPaiement(testUserId);
        
        // 2. Vérifier les assertions
        assertNotNull("Le dernier paiement ne doit pas être null", dernier);
        // PAY_2 devrait être le plus récent (inséré après PAY_1 dans setUp)
        assertEquals("ID du dernier paiement incorrect", ID_PAIEMENT_2, dernier.getIdPaiement());
    }
    
    /**
     * Test de la méthode getDernierPaiement() avec un utilisateur sans paiements.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetDernierPaiement_UserSansPaiements() throws SQLException {
        // 1. Créer un utilisateur sans paiements
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Usager (nom_usager, prenom_usager, mail_usager, mot_de_passe) " +
                        "VALUES ('Sans', 'Paiements', 'sans2@paiements.com', 'mdp')");
            
            ResultSet rs = stmt.executeQuery(
                "SELECT id_usager FROM Usager WHERE mail_usager = 'sans2@paiements.com'"
            );
            if (rs.next()) {
                int userIdSansPaiements = rs.getInt("id_usager");
                
                // 2. Exécuter la méthode
                Paiement dernier = dao.getDernierPaiement(userIdSansPaiements);
                
                // 3. Vérifier que null est retourné
                assertNull("Doit retourner null pour utilisateur sans paiements", dernier);
            }
        }
    }
    
    // ==================== TESTS DE LA LOGIQUE MÉTIER ====================
    
    /**
     * Test de la logique de détermination du type de paiement.
     */
    @Test
    public void testGetTypePaiementLogic() {
        // Cas 1: Paiement avec abonnement
        Paiement p1 = new Paiement();
        p1.setIdAbonnement("ABO_1");
        assertEquals("Type doit être 'Abonnement' quand idAbonnement est défini", 
                    "Abonnement", p1.getTypePaiement());
        
        // Cas 2: Paiement avec idAbonnement vide
        Paiement p2 = new Paiement();
        p2.setIdAbonnement("");
        assertEquals("Type doit être 'Stationnement' quand idAbonnement est vide", 
                    "Stationnement", p2.getTypePaiement());
        
        // Cas 3: Paiement avec idAbonnement null
        Paiement p3 = new Paiement();
        p3.setIdAbonnement(null);
        assertEquals("Type doit être 'Stationnement' quand idAbonnement est null", 
                    "Stationnement", p3.getTypePaiement());
        
        // Cas 4: Paiement avec type défini explicitement
        Paiement p4 = new Paiement();
        p4.setTypePaiement("Autre");
        assertEquals("Type doit être celui défini explicitement", 
                    "Autre", p4.getTypePaiement());
        
        // Cas 5: Paiement avec type null (déterminé par idAbonnement)
        Paiement p5 = new Paiement();
        p5.setTypePaiement(null);
        p5.setIdAbonnement("ABO_2");
        assertEquals("Type doit être déterminé par idAbonnement quand type est null", 
                    "Abonnement", p5.getTypePaiement());
    }
    
    // ==================== TESTS SUPPLÉMENTAIRES ====================
    
    /**
     * Test de la création d'un paiement avec des valeurs nulles.
     */
    @Test
    public void testCreateWithNullValues() {
        Paiement nouveau = new Paiement();
        nouveau.setIdPaiement("PAY_NULL");
        
        // Essayer de créer un paiement avec des valeurs nulles
        // (Le comportement dépend de l'implémentation du DAO)
        try {
            dao.create(nouveau);
            // Si ça passe, vérifier que le paiement a été créé
            Paiement verif = dao.findById("PAY_NULL");
            assertNotNull(verif);
        } catch (Exception e) {
            // Ou bien une exception est attendue
            assertTrue("Exception attendue pour création avec valeurs nulles", 
                      e instanceof SQLException || e instanceof NullPointerException);
        }
    }
    
    /**
     * Test de la méthode avec des montants négatifs ou nuls.
     */
    @Test
    public void testMontantsNegatifs() throws SQLException {
        Paiement nouveau = new Paiement("Test", "1111", "123", -10.0, testUserId);
        nouveau.setIdPaiement("PAY_NEGATIF");
        
        // La création devrait échouer ou réussir selon les contraintes de la base
        try {
            dao.create(nouveau);
            Paiement verif = dao.findById("PAY_NEGATIF");
            assertNotNull(verif);
            assertEquals(-10.0, verif.getMontant(), 0.001);
        } catch (Exception e) {
            // Une exception est possible
            System.out.println("Montant négatif non accepté: " + e.getMessage());
        }
    }
    
    /**
     * Test de la méthode avec des numéros de carte invalides.
     */
    @Test
    public void testNumerosCarteInvalides() {
        // Tester différents formats de numéros de carte
        String[] numerosInvalides = {
            "123",                   // Trop court
            "12345678901234567890",  // Trop long
            "abcdefghijklmnop",      // Pas uniquement des chiffres
            "1234-5678-9012-3456",   // Avec tirets
            "1234 5678 9012 3456",   // Avec espaces
        };
        
        for (String numero : numerosInvalides) {
            Paiement p = new Paiement("Test", numero, "123", 10.0, testUserId);
            // La validation se fait au niveau de la base ou de l'application
            System.out.println("Test avec numéro de carte: " + numero);
        }
    }
    
    /**
     * Test de performance pour la récupération des paiements.
     */
    @Test(timeout = 5000) // Timeout de 5 secondes
    public void testPerformanceFindAll() throws SQLException {
        // Insérer un grand nombre de paiements
        insererDonneesPerformance(100);
        
        // Exécuter findAll() et mesurer le temps
        long startTime = System.currentTimeMillis();
        List<Paiement> paiements = dao.findAll();
        long endTime = System.currentTimeMillis();
        
        // Vérifications
        assertNotNull(paiements);
        System.out.println("Temps d'exécution findAll() avec 100+ entrées: " + 
                          (endTime - startTime) + "ms");
    }
    
    /**
     * Insère un grand nombre de données pour les tests de performance.
     * 
     * @param count Nombre d'entrées à insérer
     * @throws SQLException en cas d'erreur SQL
     */
    private void insererDonneesPerformance(int count) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String now = LocalDateTime.now().format(formatter);
            
            for (int i = 0; i < count; i++) {
                String id = "PERF_PAY_" + i;
                String sql = "INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, " +
                            "montant, id_usager, date_paiement, methode_paiement, statut) " +
                            "VALUES ('" + id + "', 'Perf Test', '1111222233334444', '123', " +
                            (i * 10) + ", " + testUserId + ", '" + now + "', 'CARTE', 'REUSSI')";
                stmt.addBatch(sql);
            }
            stmt.executeBatch();
        }
    }
}
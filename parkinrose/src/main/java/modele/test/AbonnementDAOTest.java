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

/**
 * Classe de tests unitaires pour la classe AbonnementDAO.
 * Teste les opérations CRUD et les méthodes spécifiques de gestion des abonnements.
 * 
 * Note: Ces tests supposent l'existence d'une base de données de test.
 * Ils peuvent nécessiter une configuration spécifique pour fonctionner.
 */
public class AbonnementDAOTest {
    
    // ==================== VARIABLES DE TEST ====================
    
    private AbonnementDAO dao;      // DAO à tester
    private Connection conn;        // Connexion à la base de données
    
    // Constantes pour les tests
    private static final String ID_ABONNEMENT_1 = "TEST_ABO_1";
    private static final String ID_ABONNEMENT_2 = "TEST_ABO_2";
    private static final String ID_ABONNEMENT_3 = "TEST_ABO_3";
    private static final String ID_ABONNEMENT_INEXISTANT = "INEXISTANT";
    private static final String LIBELLE_ABONNEMENT_1 = "Test Abonnement 1";
    private static final String LIBELLE_ABONNEMENT_2 = "Test Abonnement 2";
    private static final String LIBELLE_ABONNEMENT_3 = "Test Abonnement 3";
    private static final double TARIF_1 = 10.00;
    private static final double TARIF_2 = 20.00;
    private static final double TARIF_3 = 30.00;
    
    private static final int ID_USAGER_TEST_1 = 9999;
    private static final int ID_USAGER_TEST_2 = 9998;
    private static final String NOM_USAGER_TEST = "Test";
    private static final String PRENOM_USAGER_TEST = "User";
    private static final String MAIL_USAGER_TEST_1 = "test@test.com";
    private static final String MAIL_USAGER_TEST_2 = "test2@test.com";
    private static final String MOT_DE_PASSE_TEST = "mdp";
    
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
        dao = AbonnementDAO.getInstance();
        
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
            stmt.execute("DELETE FROM Appartenir");  // Table de liaison d'abonnements
            stmt.execute("DELETE FROM Abonnement");  // Table principale
            
            // 2. Insérer des abonnements de test
            stmt.execute("INSERT INTO Abonnement (id_abonnement, libelle_abonnement, tarif_applique) " +
                        "VALUES ('" + ID_ABONNEMENT_1 + "', '" + LIBELLE_ABONNEMENT_1 + "', " + TARIF_1 + ")");
            stmt.execute("INSERT INTO Abonnement (id_abonnement, libelle_abonnement, tarif_applique) " +
                        "VALUES ('" + ID_ABONNEMENT_2 + "', '" + LIBELLE_ABONNEMENT_2 + "', " + TARIF_2 + ")");
        }
    }
    
    /**
     * Méthode exécutée après chaque test.
     * Nettoie les données de test et ferme la connexion.
     * 
     * @throws SQLException en cas d'erreur de fermeture de connexion
     */
    @After
    public void tearDown() throws SQLException {
        // 1. Nettoyer toutes les données de test
        nettoyerBaseDeDonnees();
        
        // 2. Fermer la connexion si elle est ouverte
        fermerConnexion();
    }
    
    /**
     * Nettoie toutes les données de test de la base de données.
     * 
     * @throws SQLException en cas d'erreur SQL
     */
    private void nettoyerBaseDeDonnees() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Supprimer dans l'ordre inverse des dépendances
            stmt.execute("DELETE FROM Appartenir");
            stmt.execute("DELETE FROM Abonnement");
            stmt.execute("DELETE FROM Usager WHERE id_usager IN (" + 
                        ID_USAGER_TEST_1 + ", " + ID_USAGER_TEST_2 + ")");
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
     * Vérifie que la méthode retourne tous les abonnements présents en base.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testFindAll() throws SQLException {
        // 1. Exécuter la méthode à tester
        List<Abonnement> abonnements = dao.findAll();
        
        // 2. Vérifier les assertions
        assertNotNull("La liste ne doit pas être null", abonnements);
        assertTrue("Doit contenir au moins 2 abonnements (ceux insérés en setup)", 
                  abonnements.size() >= 2);
        
        // 3. Vérifier que les abonnements de test sont présents
        boolean found1 = false;
        boolean found2 = false;
        for (Abonnement abo : abonnements) {
            if (ID_ABONNEMENT_1.equals(abo.getIdAbonnement())) {
                found1 = true;
                assertEquals("Libellé incorrect pour " + ID_ABONNEMENT_1, 
                            LIBELLE_ABONNEMENT_1, abo.getLibelleAbonnement());
                assertEquals("Tarif incorrect pour " + ID_ABONNEMENT_1, 
                            TARIF_1, abo.getTarifAbonnement(), 0.001);
            }
            if (ID_ABONNEMENT_2.equals(abo.getIdAbonnement())) {
                found2 = true;
            }
        }
        
        // 4. Vérifier que tous les abonnements attendus ont été trouvés
        assertTrue(ID_ABONNEMENT_1 + " doit être trouvé", found1);
        assertTrue(ID_ABONNEMENT_2 + " doit être trouvé", found2);
    }
    
    /**
     * Test de la méthode findById() avec un ID existant.
     * Vérifie que l'abonnement est correctement récupéré.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testFindById() throws SQLException {
        // 1. Exécuter la méthode à tester
        Abonnement abo = dao.findById(ID_ABONNEMENT_1);
        
        // 2. Vérifier les assertions
        assertNotNull("L'abonnement ne doit pas être null", abo);
        assertEquals("ID incorrect", ID_ABONNEMENT_1, abo.getIdAbonnement());
        assertEquals("Libellé incorrect", LIBELLE_ABONNEMENT_1, abo.getLibelleAbonnement());
        assertEquals("Tarif incorrect", TARIF_1, abo.getTarifAbonnement(), 0.001);
    }
    
    /**
     * Test de la méthode findById() avec un ID inexistant.
     * Vérifie que null est retourné.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testFindById_NotFound() throws SQLException {
        // 1. Exécuter la méthode à tester
        Abonnement abo = dao.findById(ID_ABONNEMENT_INEXISTANT);
        
        // 2. Vérifier les assertions
        assertNull("Doit retourner null pour un ID inexistant", abo);
    }
    
    /**
     * Test de la méthode create().
     * Vérifie qu'un nouvel abonnement peut être créé en base.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testCreate() throws SQLException {
        // 1. Créer un nouvel abonnement
        Abonnement nouveau = new Abonnement();
        nouveau.setIdAbonnement(ID_ABONNEMENT_3);
        nouveau.setLibelleAbonnement(LIBELLE_ABONNEMENT_3);
        nouveau.setTarifAbonnement(TARIF_3);
        
        // 2. Exécuter la méthode à tester
        dao.create(nouveau);
        
        // 3. Vérifier que l'abonnement a été créé
        Abonnement verif = dao.findById(ID_ABONNEMENT_3);
        assertNotNull("L'abonnement doit être créé", verif);
        assertEquals("Libellé incorrect après création", 
                    LIBELLE_ABONNEMENT_3, verif.getLibelleAbonnement());
        assertEquals("Tarif incorrect après création", 
                    TARIF_3, verif.getTarifAbonnement(), 0.001);
    }
    
    /**
     * Test de la méthode update().
     * Vérifie qu'un abonnement existant peut être mis à jour.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testUpdate() throws SQLException {
        // 1. Récupérer un abonnement existant
        Abonnement abo = dao.findById(ID_ABONNEMENT_1);
        assertNotNull("L'abonnement doit exister", abo);
        
        // 2. Modifier ses attributs
        String nouveauLibelle = "Test Modifié";
        double nouveauTarif = 15.00;
        abo.setLibelleAbonnement(nouveauLibelle);
        abo.setTarifAbonnement(nouveauTarif);
        
        // 3. Exécuter la méthode à tester
        dao.update(abo);
        
        // 4. Vérifier la mise à jour
        Abonnement verif = dao.findById(ID_ABONNEMENT_1);
        assertNotNull("L'abonnement doit toujours exister", verif);
        assertEquals("Libellé non mis à jour", nouveauLibelle, verif.getLibelleAbonnement());
        assertEquals("Tarif non mis à jour", nouveauTarif, verif.getTarifAbonnement(), 0.001);
    }
    
    /**
     * Test de la méthode delete().
     * Vérifie qu'un abonnement peut être supprimé.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testDelete() throws SQLException {
        // 1. Récupérer un abonnement existant
        Abonnement abo = dao.findById(ID_ABONNEMENT_1);
        assertNotNull("L'abonnement doit exister avant suppression", abo);
        
        // 2. Exécuter la méthode à tester
        dao.delete(abo);
        
        // 3. Vérifier la suppression
        Abonnement verif = dao.findById(ID_ABONNEMENT_1);
        assertNull("L'abonnement doit être supprimé", verif);
    }
    
    // ==================== TESTS DES MÉTHODES SPÉCIFIQUES ====================
    
    /**
     * Test de la méthode ajouterAbonnementUtilisateur().
     * Vérifie qu'un abonnement peut être associé à un utilisateur.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testAjouterAbonnementUtilisateur() throws SQLException {
        // 1. Créer un utilisateur de test
        creerUsagerTest(ID_USAGER_TEST_1, MAIL_USAGER_TEST_1);
        
        // 2. Exécuter la méthode à tester
        boolean result = dao.ajouterAbonnementUtilisateur(ID_USAGER_TEST_1, ID_ABONNEMENT_1);
        assertTrue("Doit retourner true pour une association réussie", result);
        
        // 3. Vérifier l'association dans la table Appartenir
        verifierAssociationUtilisateurAbonnement(ID_USAGER_TEST_1, ID_ABONNEMENT_1, 1);
    }
    
    /**
     * Test de la méthode ajouterAbonnementUtilisateur() avec un abonnement inexistant.
     * Vérifie que l'association échoue pour un abonnement qui n'existe pas.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testAjouterAbonnementUtilisateur_AbonnementInexistant() throws SQLException {
        // 1. Exécuter la méthode à tester avec un ID inexistant
        boolean result = dao.ajouterAbonnementUtilisateur(ID_USAGER_TEST_1, ID_ABONNEMENT_INEXISTANT);
        
        // 2. Vérifier que l'opération a échoué
        assertFalse("Doit retourner false pour un abonnement inexistant", result);
    }
    
    /**
     * Test de la méthode supprimerAbonnementsUtilisateur().
     * Vérifie que tous les abonnements d'un utilisateur peuvent être supprimés.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testSupprimerAbonnementsUtilisateur() throws SQLException {
        // 1. Créer un utilisateur et lui ajouter un abonnement
        creerUsagerTest(ID_USAGER_TEST_2, MAIL_USAGER_TEST_2);
        ajouterAbonnementUtilisateur(ID_USAGER_TEST_2, ID_ABONNEMENT_1);
        
        // 2. Exécuter la méthode à tester
        dao.supprimerAbonnementsUtilisateur(ID_USAGER_TEST_2);
        
        // 3. Vérifier la suppression
        verifierAssociationUtilisateurAbonnement(ID_USAGER_TEST_2, ID_ABONNEMENT_1, 0);
    }
    
    // ==================== MÉTHODES UTILITAIRES POUR LES TESTS ====================
    
    /**
     * Crée un utilisateur de test dans la base de données.
     * 
     * @param idUsager ID de l'utilisateur
     * @param mail Mail de l'utilisateur
     * @throws SQLException en cas d'erreur SQL
     */
    private void creerUsagerTest(int idUsager, String mail) throws SQLException {
        String sql = "INSERT INTO Usager (id_usager, nom_usager, prenom_usager, mail_usager, mot_de_passe) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsager);
            pstmt.setString(2, NOM_USAGER_TEST);
            pstmt.setString(3, PRENOM_USAGER_TEST);
            pstmt.setString(4, mail);
            pstmt.setString(5, MOT_DE_PASSE_TEST);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Ajoute manuellement un abonnement à un utilisateur (sans passer par le DAO).
     * 
     * @param idUsager ID de l'utilisateur
     * @param idAbonnement ID de l'abonnement
     * @throws SQLException en cas d'erreur SQL
     */
    private void ajouterAbonnementUtilisateur(int idUsager, String idAbonnement) throws SQLException {
        String sql = "INSERT INTO Appartenir (id_usager, id_abonnement) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsager);
            pstmt.setString(2, idAbonnement);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Vérifie l'association entre un utilisateur et un abonnement.
     * 
     * @param idUsager ID de l'utilisateur
     * @param idAbonnement ID de l'abonnement
     * @param countAttendu Nombre d'associations attendues
     * @throws SQLException en cas d'erreur SQL
     */
    private void verifierAssociationUtilisateurAbonnement(int idUsager, String idAbonnement, int countAttendu) 
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM Appartenir WHERE id_usager = ? AND id_abonnement = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsager);
            pstmt.setString(2, idAbonnement);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertEquals("Nombre incorrect d'associations", 
                                countAttendu, rs.getInt(1));
                }
            }
        }
    }
    
    // ==================== TESTS SUPPLÉMENTAIRES ====================
    
    /**
     * Test de la récupération des abonnements d'un utilisateur spécifique.
     * (À implémenter si la méthode existe dans AbonnementDAO)
     */
    @Test
    public void testFindByUsager() throws SQLException {
        // Exemple de test pour une méthode findByUsager si elle existe
        // Cette méthode devrait retourner la liste des abonnements d'un utilisateur
        
        // 1. Créer un utilisateur et lui ajouter des abonnements
        creerUsagerTest(ID_USAGER_TEST_1, MAIL_USAGER_TEST_1);
        ajouterAbonnementUtilisateur(ID_USAGER_TEST_1, ID_ABONNEMENT_1);
        ajouterAbonnementUtilisateur(ID_USAGER_TEST_1, ID_ABONNEMENT_2);
        
        // 2. Exécuter la méthode à tester (si elle existe)
        // List<Abonnement> abonnements = dao.findByUsager(ID_USAGER_TEST_1);
        
        // 3. Vérifications...
        // assertNotNull(abonnements);
        // assertEquals(2, abonnements.size());
    }
    
    /**
     * Test de la méthode findById avec des caractères spéciaux.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testFindById_AvecCaracteresSpeciaux() throws SQLException {
        // 1. Insérer un abonnement avec des caractères spéciaux
        String idSpecial = "TEST_ABO_ÉÉ";
        String libelleSpecial = "Abonnement avec accents éèà";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Abonnement (id_abonnement, libelle_abonnement, tarif_applique) " +
                        "VALUES ('" + idSpecial + "', '" + libelleSpecial + "', 25.00)");
        }
        
        // 2. Récupérer l'abonnement
        Abonnement abo = dao.findById(idSpecial);
        
        // 3. Vérifications
        assertNotNull("Doit trouver l'abonnement avec caractères spéciaux", abo);
        assertEquals(idSpecial, abo.getIdAbonnement());
        assertEquals(libelleSpecial, abo.getLibelleAbonnement());
    }
    
    /**
     * Test de performance pour findAll() avec beaucoup de données.
     * (À exécuter séparément des tests unitaires normaux)
     */
    @Test(timeout = 5000) // Timeout de 5 secondes
    public void testFindAll_Performance() throws SQLException {
        // 1. Insérer un grand nombre d'abonnements (1000 par exemple)
        insererDonneesPerformance(1000);
        
        // 2. Exécuter findAll() et mesurer le temps
        long startTime = System.currentTimeMillis();
        List<Abonnement> abonnements = dao.findAll();
        long endTime = System.currentTimeMillis();
        
        // 3. Vérifications
        assertNotNull(abonnements);
        System.out.println("Temps d'exécution findAll() avec 1000 entrées: " + 
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
            for (int i = 0; i < count; i++) {
                String id = "PERF_ABO_" + i;
                stmt.addBatch("INSERT INTO Abonnement (id_abonnement, libelle_abonnement, tarif_applique) " +
                             "VALUES ('" + id + "', 'Abonnement Performance " + i + "', " + (i * 10) + ")");
            }
            stmt.executeBatch();
        }
    }
}
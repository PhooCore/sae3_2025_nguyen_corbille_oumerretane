package modele.test;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import modele.Stationnement;
import modele.dao.MySQLConnection;
import modele.dao.ParkingDAO;
import modele.dao.StationnementDAO;
import modele.Parking;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Classe de tests unitaires pour la classe StationnementDAO.
 * 
 * Cette classe teste les opérations CRUD sur les stationnements ainsi que
 * les méthodes spécifiques de gestion des stationnements actifs et historiques.
 */
public class StationnementDAOTest {
    
    // ==================== VARIABLES DE TEST ====================
    
    private StationnementDAO dao;        // DAO à tester (singleton)
    private ParkingDAO parkingDAO;       // DAO pour la gestion des parkings
    private Connection conn;             // Connexion à la base de données
    private int testUserId;              // ID de l'utilisateur de test
    
    // Constantes pour les tests
    private static final int ID_STATIONNEMENT_1 = 1;
    private static final int ID_STATIONNEMENT_2 = 2;
    private static final int ID_STATIONNEMENT_3 = 3;
    private static final int ID_STATIONNEMENT_INEXISTANT = 999;
    private static final String TYPE_VEHICULE_VOITURE = "Voiture";
    private static final String TYPE_VEHICULE_MOTO = "Moto";
    private static final String PLAQUE_1 = "AB-123-CD";
    private static final String PLAQUE_2 = "XY-789-ZW";
    private static final String PLAQUE_3 = "EF-456-GH";
    private static final String ID_ZONE_TEST = "TEST_ZONE";
    private static final String LIBELLE_ZONE_TEST = "Zone Test";
    private static final String ID_PARKING_TEST = "TEST_PARK";
    private static final String LIBELLE_PARKING_TEST = "Parking Test";
    private static final int DUREE_HEURES_1 = 2;
    private static final int DUREE_HEURES_3 = 1;
    private static final int DUREE_MINUTES_1 = 0;
    private static final int DUREE_MINUTES_3 = 30;
    private static final double COUT_1 = 2.00;
    private static final double COUT_3 = 1.50;
    private static final String STATUT_ACTIF = "ACTIF";
    private static final String STATUT_TERMINE = "TERMINE";
    private static final String TYPE_STATIONNEMENT_VOIRIE = "VOIRIE";
    private static final String TYPE_STATIONNEMENT_PARKING = "PARKING";
    private static final String STATUT_PAIEMENT_PAYE = "PAYE";
    private static final String STATUT_PAIEMENT_NON_PAYE = "NON_PAYE";
    private static final String ID_PAIEMENT_1 = "PAY_1";
    private static final String ID_PAIEMENT_3 = "PAY_3";
    private static final String ID_PAIEMENT_NEW = "PAY_NEW";
    private static final String ID_PAIEMENT_UPDATE = "PAY_UPDATE";
    private static final String MAIL_USAGER_TEST = "test@stationnement.com";
    private static final String NOM_USAGER_TEST = "Test";
    private static final String PRENOM_USAGER_TEST = "User";
    private static final String MOT_DE_PASSE_TEST = "mdp";
    private static final String ADRESSE_PARKING_TEST = "Adresse Test";
    
    // ==================== MÉTHODES DE CONFIGURATION ====================
    
    /**
     * Méthode exécutée avant chaque test.
     * Initialise les DAOs, établit la connexion et prépare l'environnement de test.
     * 
     * @throws SQLException en cas d'erreur de connexion ou d'exécution SQL
     */
    @Before
    public void setUp() throws SQLException {
        // 1. Initialiser les DAOs (singletons)
        dao = StationnementDAO.getInstance();
        parkingDAO = ParkingDAO.getInstance();
        
        // 2. Obtenir une connexion à la base de données
        conn = MySQLConnection.getConnection();
        
        // 3. Préparer la base de données pour les tests
        cleanDatabase();
        prepareTestData();
    }
    
    /**
     * Nettoie complètement la base de données.
     * 
     * @throws SQLException en cas d'erreur SQL
     */
    private void cleanDatabase() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Désactiver temporairement les contraintes de clés étrangères
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // Nettoyer toutes les tables dans l'ordre inverse des dépendances
            stmt.execute("DELETE FROM Stationnement");
            stmt.execute("DELETE FROM Paiement");
            stmt.execute("DELETE FROM Vehicule_Usager");
            stmt.execute("DELETE FROM Appartenir");
            stmt.execute("DELETE FROM Parking");
            stmt.execute("DELETE FROM Zone");
            stmt.execute("DELETE FROM Abonnement");
            stmt.execute("DELETE FROM Usager");
            
            // Réactiver les contraintes
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
    
    /**
     * Prépare les données de test dans la base de données.
     * 
     * @throws SQLException en cas d'erreur SQL
     */
    private void prepareTestData() throws SQLException {
        // 1. Créer un utilisateur de test
        creerUsagerTest();
        
        // 2. Créer une zone de test
        creerZoneTest();
        
        // 3. Créer un parking de test
        creerParkingTest();
        
        // 4. Créer des paiements de test
        creerPaiementsTest();
        
        // 5. Insérer des stationnements de test
        insererStationnementsTest();
    }
    
    /**
     * Crée un utilisateur de test.
     * 
     * @throws SQLException en cas d'erreur SQL
     */
    private void creerUsagerTest() throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO Usager (nom_usager, prenom_usager, mail_usager, mot_de_passe) " +
                "VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, NOM_USAGER_TEST);
            pstmt.setString(2, PRENOM_USAGER_TEST);
            pstmt.setString(3, MAIL_USAGER_TEST);
            pstmt.setString(4, MOT_DE_PASSE_TEST);
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                testUserId = rs.getInt(1);
            }
        }
    }
    
    /**
     * Crée une zone de test.
     * 
     * @throws SQLException en cas d'erreur SQL
     */
    private void creerZoneTest() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Zone (id_zone, libelle_zone, couleur_zone, tarif_par_heure, duree_max) " +
                        "VALUES ('" + ID_ZONE_TEST + "', '" + LIBELLE_ZONE_TEST + "', 'blue', 1.00, '02:00:00')");
        }
    }
    
    /**
     * Crée un parking de test.
     * 
     * @throws SQLException en cas d'erreur SQL
     */
    private void creerParkingTest() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Parking (id_parking, libelle_parking, adresse_parking, " +
                        "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                        "has_moto, places_moto, places_moto_disponibles, est_relais) " +
                        "VALUES ('" + ID_PARKING_TEST + "', '" + LIBELLE_PARKING_TEST + "', '" + 
                        ADRESSE_PARKING_TEST + "', 100, 100, 2.0, 1, 1, 20, 20, 0)");
        }
    }
    
    /**
     * Crée des paiements de test.
     * 
     * @throws SQLException en cas d'erreur SQL
     */
    private void creerPaiementsTest() throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        
        try (Statement stmt = conn.createStatement()) {
            // Paiement 1 pour stationnement 1
            stmt.execute("INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, " +
                        "montant, id_usager, date_paiement, methode_paiement, statut) " +
                        "VALUES ('" + ID_PAIEMENT_1 + "', 'John Doe', '1234567890123456', '123', " +
                        COUT_1 + ", " + testUserId + ", '" + now + "', 'CARTE', 'REUSSI')");
            
            // Paiement 3 pour stationnement 3
            stmt.execute("INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, " +
                        "montant, id_usager, date_paiement, methode_paiement, statut) " +
                        "VALUES ('" + ID_PAIEMENT_3 + "', 'Jane Doe', '9876543210987654', '456', " +
                        COUT_3 + ", " + testUserId + ", '" + now + "', 'CARTE', 'REUSSI')");
        }
    }
    
    /**
     * Insère des stationnements de test.
     * 
     * @throws SQLException en cas d'erreur SQL
     */
    private void insererStationnementsTest() throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        
        try (Statement stmt = conn.createStatement()) {
            // Stationnement 1: Voirie actif
            stmt.execute("INSERT INTO Stationnement (id_stationnement, id_usager, type_vehicule, " +
                        "plaque_immatriculation, id_zone, duree_heures, duree_minutes, cout, " +
                        "statut, date_creation, date_fin, type_stationnement, statut_paiement, id_paiement) " +
                        "VALUES (" + ID_STATIONNEMENT_1 + ", " + testUserId + ", '" + TYPE_VEHICULE_VOITURE + 
                        "', '" + PLAQUE_1 + "', '" + ID_ZONE_TEST + "', " + DUREE_HEURES_1 + ", " + 
                        DUREE_MINUTES_1 + ", " + COUT_1 + ", '" + STATUT_ACTIF + "', '" + now + 
                        "', DATE_ADD('" + now + "', INTERVAL 120 MINUTE), '" + TYPE_STATIONNEMENT_VOIRIE + 
                        "', '" + STATUT_PAIEMENT_PAYE + "', '" + ID_PAIEMENT_1 + "')");
            
            // Stationnement 2: Parking actif (sans paiement initial)
            stmt.execute("INSERT INTO Stationnement (id_stationnement, id_usager, type_vehicule, " +
                        "plaque_immatriculation, id_parking, cout, statut, heure_arrivee, " +
                        "type_stationnement, statut_paiement, date_creation) " +
                        "VALUES (" + ID_STATIONNEMENT_2 + ", " + testUserId + ", '" + TYPE_VEHICULE_MOTO + 
                        "', '" + PLAQUE_2 + "', '" + ID_PARKING_TEST + "', 0.00, '" + STATUT_ACTIF + "', " +
                        "'" + now + "', '" + TYPE_STATIONNEMENT_PARKING + "', '" + STATUT_PAIEMENT_NON_PAYE + 
                        "', '" + now + "')");
            
            // Stationnement 3: Voirie terminé
            stmt.execute("INSERT INTO Stationnement (id_stationnement, id_usager, type_vehicule, " +
                        "plaque_immatriculation, id_zone, duree_heures, duree_minutes, cout, " +
                        "statut, date_creation, date_fin, type_stationnement, statut_paiement, id_paiement) " +
                        "VALUES (" + ID_STATIONNEMENT_3 + ", " + testUserId + ", '" + TYPE_VEHICULE_VOITURE + 
                        "', '" + PLAQUE_3 + "', '" + ID_ZONE_TEST + "', " + DUREE_HEURES_3 + ", " + 
                        DUREE_MINUTES_3 + ", " + COUT_3 + ", '" + STATUT_TERMINE + "', " +
                        "DATE_SUB('" + now + "', INTERVAL 3 HOUR), DATE_SUB('" + now + "', INTERVAL 90 MINUTE), " +
                        "'" + TYPE_STATIONNEMENT_VOIRIE + "', '" + STATUT_PAIEMENT_PAYE + "', '" + ID_PAIEMENT_3 + "')");
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
        cleanDatabase();
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    // ==================== TESTS DES MÉTHODES CRUD ====================
    
    /**
     * Test de la méthode findAll().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testFindAll() throws SQLException {
        // 1. Exécuter la méthode à tester
        List<Stationnement> stationnements = dao.findAll();
        
        // 2. Vérifier les assertions
        assertNotNull("La liste ne doit pas être null", stationnements);
        assertEquals("Doit contenir exactement 3 stationnements", 3, stationnements.size());
        
        // 3. Vérifier que chaque stationnement a les attributs de base
        for (Stationnement s : stationnements) {
            assertTrue("ID stationnement doit être > 0", s.getIdStationnement() > 0);
            assertEquals("ID usager incorrect", testUserId, s.getIdUsager());
            assertNotNull("Type véhicule ne doit pas être null", s.getTypeVehicule());
            assertNotNull("Plaque immatriculation ne doit pas être null", s.getPlaqueImmatriculation());
            assertNotNull("Statut ne doit pas être null", s.getStatut());
            assertNotNull("Type stationnement ne doit pas être null", s.getTypeStationnement());
        }
    }
    
    /**
     * Test de la méthode findById() avec un ID existant.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testFindById() throws SQLException {
        // 1. Exécuter la méthode à tester
        Stationnement stationnement = dao.findById(String.valueOf(ID_STATIONNEMENT_1));
        
        // 2. Vérifier les assertions
        assertNotNull("Le stationnement ne doit pas être null", stationnement);
        assertEquals("ID stationnement incorrect", ID_STATIONNEMENT_1, stationnement.getIdStationnement());
        assertEquals("ID usager incorrect", testUserId, stationnement.getIdUsager());
        assertEquals("Type véhicule incorrect", TYPE_VEHICULE_VOITURE, stationnement.getTypeVehicule());
        assertEquals("Plaque immatriculation incorrecte", PLAQUE_1, stationnement.getPlaqueImmatriculation());
        assertEquals("Type stationnement incorrect", TYPE_STATIONNEMENT_VOIRIE, stationnement.getTypeStationnement());
        assertEquals("Statut incorrect", STATUT_ACTIF, stationnement.getStatut());
        assertEquals("Statut paiement incorrect", STATUT_PAIEMENT_PAYE, stationnement.getStatutPaiement());
        assertEquals("ID paiement incorrect", ID_PAIEMENT_1, stationnement.getIdPaiement());
        assertEquals("Durée heures incorrecte", DUREE_HEURES_1, stationnement.getDureeHeures());
        assertEquals("Durée minutes incorrecte", DUREE_MINUTES_1, stationnement.getDureeMinutes());
        assertEquals("Coût incorrect", COUT_1, stationnement.getCout(), 0.001);
        assertNotNull("Date création ne doit pas être null", stationnement.getDateCreation());
        assertNotNull("Date fin ne doit pas être null", stationnement.getDateFin());
    }
    
    /**
     * Test de la méthode findById() avec un ID inexistant.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testFindById_NotFound() throws SQLException {
        // 1. Exécuter la méthode à tester
        Stationnement stationnement = dao.findById(String.valueOf(ID_STATIONNEMENT_INEXISTANT));
        
        // 2. Vérifier que null est retourné
        assertNull("Doit retourner null pour un ID inexistant", stationnement);
    }
    
    // ==================== TESTS DES MÉTHODES DE CRÉATION ====================
    
    /**
     * Test de la création d'un stationnement en voirie.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testCreerStationnementVoirie() throws SQLException {
        // 1. Utiliser la méthode simplifiée pour créer un stationnement voirie gratuit
        boolean result = StationnementDAO.creerStationnementVoirieGratuit(
            testUserId,
            TYPE_VEHICULE_VOITURE,
            "TEST-001",
            ID_ZONE_TEST,
            1,
            30
        );
        
        // 2. Vérifier que la création a réussi
        assertTrue("La création du stationnement voirie doit réussir", result);
        
        // 3. Vérifier que le stationnement a été créé
        List<Stationnement> stationnements = dao.findAll();
        assertEquals("Doit y avoir 4 stationnements après création", 4, stationnements.size());
        
        // 4. Chercher le nouveau stationnement
        boolean found = false;
        for (Stationnement s : stationnements) {
            if ("TEST-001".equals(s.getPlaqueImmatriculation())) {
                found = true;
                assertEquals("Type stationnement incorrect", TYPE_STATIONNEMENT_VOIRIE, s.getTypeStationnement());
                assertEquals("Statut incorrect", STATUT_ACTIF, s.getStatut());
                assertEquals("ID zone incorrect", ID_ZONE_TEST, s.getZone());
                assertEquals("Durée heures incorrecte", 1, s.getDureeHeures());
                assertEquals("Durée minutes incorrecte", 30, s.getDureeMinutes());
                break;
            }
        }
        assertTrue("Le nouveau stationnement doit être trouvé", found);
    }
    
    /**
     * Test de la création d'un stationnement en parking.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testCreerStationnementParking() throws SQLException {
        // 1. Vérifier les places disponibles avant
        Parking parking = parkingDAO.findById(ID_PARKING_TEST);
        int placesAvant = parking.getPlacesDisponibles();
        int placesMotoAvant = parking.getPlacesMotoDisponibles();
        
        // 2. Créer un nouveau stationnement parking
        Stationnement nouveau = new Stationnement(
            testUserId,
            TYPE_VEHICULE_MOTO,
            "TEST-002",
            ID_PARKING_TEST,
            LIBELLE_PARKING_TEST,
            LocalDateTime.now()
        );
        
        boolean result = dao.creerStationnementParking(nouveau);
        
        // 3. Vérifier que la création a réussi
        assertTrue("La création du stationnement parking doit réussir", result);
        
        // 4. Vérifier que les places ont été correctement décrémentées
        parking = parkingDAO.findById(ID_PARKING_TEST);
        int placesApres = parking.getPlacesDisponibles();
        int placesMotoApres = parking.getPlacesMotoDisponibles();
        
        // Pour une moto, seulement les places moto devraient décrémenter
        assertEquals("Places normales ne doivent pas changer pour une moto", 
                    placesAvant, placesApres);
        assertEquals("Places moto doivent décrémenter de 1", 
                    placesMotoAvant - 1, placesMotoApres);
        
        // 5. Vérifier le stationnement créé
        List<Stationnement> stationnements = dao.findAll();
        boolean found = false;
        for (Stationnement s : stationnements) {
            if ("TEST-002".equals(s.getPlaqueImmatriculation())) {
                found = true;
                assertEquals("Type stationnement incorrect", TYPE_STATIONNEMENT_PARKING, s.getTypeStationnement());
                assertEquals("Statut incorrect", STATUT_ACTIF, s.getStatut());
                assertEquals("ID parking incorrect", ID_PARKING_TEST, s.getZone());
                assertEquals("Statut paiement incorrect", STATUT_PAIEMENT_NON_PAYE, s.getStatutPaiement());
                break;
            }
        }
        assertTrue("Le nouveau stationnement parking doit être trouvé", found);
    }
    
    /**
     * Test de la création d'un stationnement en parking avec une voiture.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testCreerStationnementParkingVoiture() throws SQLException {
        // 1. Vérifier les places disponibles avant
        Parking parking = parkingDAO.findById(ID_PARKING_TEST);
        int placesAvant = parking.getPlacesDisponibles();
        
        // 2. Créer un nouveau stationnement parking pour une voiture
        Stationnement nouveau = new Stationnement(
            testUserId,
            TYPE_VEHICULE_VOITURE,
            "TEST-003",
            ID_PARKING_TEST,
            LIBELLE_PARKING_TEST,
            LocalDateTime.now()
        );
        
        boolean result = dao.creerStationnementParking(nouveau);
        
        // 3. Vérifier que la création a réussi
        assertTrue("La création du stationnement parking voiture doit réussir", result);
        
        // 4. Vérifier que les places ont été décrémentées
        parking = parkingDAO.findById(ID_PARKING_TEST);
        int placesApres = parking.getPlacesDisponibles();
        
        assertEquals("Places doivent décrémenter de 1 pour une voiture", 
                    placesAvant - 1, placesApres);
    }
    
    // ==================== TESTS DES MÉTHODES DE TERMINAISON ====================
    
    /**
     * Test de la terminaison d'un stationnement en parking.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testTerminerStationnementParking() throws SQLException {
        // 1. Créer un nouveau paiement pour la terminaison
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, " +
                        "montant, id_usager, date_paiement, methode_paiement, statut) " +
                        "VALUES ('" + ID_PAIEMENT_NEW + "', 'Termination Card', '1111222233334444', '789', " +
                        "5.00, " + testUserId + ", '" + now + "', 'CARTE', 'REUSSI')");
        }
        
        // 2. Récupérer les places moto disponibles avant
        Parking parking = parkingDAO.findById(ID_PARKING_TEST);
        int placesMotoAvant = parking.getPlacesMotoDisponibles();
        
        // 3. Utiliser la méthode statique de terminaison
        LocalDateTime heureDepart = LocalDateTime.now();
        boolean result = StationnementDAO.terminerStationnementParking(ID_STATIONNEMENT_2, heureDepart, 5.00, ID_PAIEMENT_NEW);
        
        // 4. Vérifier que la terminaison a réussi
        assertTrue("La terminaison du stationnement parking doit réussir", result);
        
        // 5. Vérifier que le stationnement a été mis à jour
        Stationnement termine = dao.findById(String.valueOf(ID_STATIONNEMENT_2));
        assertEquals("Statut incorrect après terminaison", STATUT_TERMINE, termine.getStatut());
        assertEquals("Statut paiement incorrect après terminaison", STATUT_PAIEMENT_PAYE, termine.getStatutPaiement());
        assertEquals("Coût incorrect après terminaison", 5.00, termine.getCout(), 0.001);
        assertEquals("ID paiement incorrect après terminaison", ID_PAIEMENT_NEW, termine.getIdPaiement());
        assertNotNull("Heure départ ne doit pas être null après terminaison", termine.getHeureDepart());
        
        // 6. Vérifier que les places moto ont été réincrémentées
        parking = parkingDAO.findById(ID_PARKING_TEST);
        int placesMotoApres = parking.getPlacesMotoDisponibles();
        assertEquals("Places moto doivent être réincrémentées après terminaison", 
                    placesMotoAvant + 1, placesMotoApres);
    }
    
    // ==================== TESTS DES MÉTHODES DE RECHERCHE ====================
    
    /**
     * Test de la récupération de l'historique des stationnements.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetHistoriqueStationnements() throws SQLException {
        // 1. Exécuter la méthode à tester
        List<Stationnement> historique = dao.getHistoriqueStationnements(testUserId);
        
        // 2. Vérifier les assertions
        assertNotNull("L'historique ne doit pas être null", historique);
        assertEquals("Doit contenir tous les stationnements de l'utilisateur", 3, historique.size());
        
        // 3. Vérifier que tous les stationnements appartiennent au bon utilisateur
        for (Stationnement s : historique) {
            assertEquals("ID usager incorrect dans l'historique", testUserId, s.getIdUsager());
        }
    }
    
    /**
     * Test de la récupération du stationnement actif par utilisateur.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetStationnementActifByUsager() throws SQLException {
        // 1. Exécuter la méthode à tester
        Stationnement actif = dao.getStationnementActifByUsager(testUserId);
        
        // 2. Vérifier les assertions
        assertNotNull("Doit retourner un stationnement actif", actif);
        assertEquals("Statut incorrect", STATUT_ACTIF, actif.getStatut());
        assertEquals("ID usager incorrect", testUserId, actif.getIdUsager());
        
        // 3. Vérifier que c'est bien le premier stationnement actif trouvé
        assertEquals("ID stationnement incorrect", ID_STATIONNEMENT_1, actif.getIdStationnement());
    }
    
    /**
     * Test de la vérification de présence de stationnement actif.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testHasStationnementActif() throws SQLException {
        // 1. Tester avec un utilisateur ayant des stationnements actifs
        boolean hasActive = dao.hasStationnementActif(testUserId);
        assertTrue("L'utilisateur doit avoir des stationnements actifs", hasActive);
        
        // 2. Créer un autre utilisateur sans stationnement actif
        int newUserId = creerUsagerSansStationnements();
        
        // 3. Tester avec ce nouvel utilisateur
        hasActive = dao.hasStationnementActif(newUserId);
        assertFalse("Le nouvel utilisateur ne doit pas avoir de stationnements actifs", hasActive);
    }
    
    /**
     * Crée un utilisateur sans stationnements pour les tests.
     * 
     * @return L'ID du nouvel utilisateur
     * @throws SQLException en cas d'erreur SQL
     */
    private int creerUsagerSansStationnements() throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO Usager (nom_usager, prenom_usager, mail_usager, mot_de_passe) " +
                "VALUES ('Test2', 'User2', 'test2@test.com', 'mdp')",
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        }
    }
    
    // ==================== TESTS DES MÉTHODES DE MISE À JOUR ====================
    
    /**
     * Test de la mise à jour du statut de paiement.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testMettreAJourStatutPaiement() throws SQLException {
        // 1. Créer un nouveau paiement
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, " +
                        "montant, id_usager, date_paiement, methode_paiement, statut) " +
                        "VALUES ('" + ID_PAIEMENT_UPDATE + "', 'Update Card', '9999888877776666', '000', " +
                        "10.00, " + testUserId + ", '" + now + "', 'CARTE', 'REUSSI')");
        }
        
        // 2. Exécuter la méthode à tester
        boolean result = dao.mettreAJourStatutPaiement(ID_STATIONNEMENT_2, ID_PAIEMENT_UPDATE, STATUT_PAIEMENT_PAYE);
        
        // 3. Vérifier que la mise à jour a réussi
        assertTrue("La mise à jour du statut de paiement doit réussir", result);
        
        // 4. Vérifier que le stationnement a été mis à jour
        Stationnement misAJour = dao.findById(String.valueOf(ID_STATIONNEMENT_2));
        assertEquals("ID paiement incorrect après mise à jour", ID_PAIEMENT_UPDATE, misAJour.getIdPaiement());
        assertEquals("Statut paiement incorrect après mise à jour", STATUT_PAIEMENT_PAYE, misAJour.getStatutPaiement());
        
        // 5. Vérifier que le statut n'a pas changé (doit rester ACTIF)
        assertEquals("Statut doit rester ACTIF", STATUT_ACTIF, misAJour.getStatut());
    }
    
    // ==================== TESTS DES MÉTHODES DE FILTRAGE ====================
    
    /**
     * Test des filtres de stationnements.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetStationnementsAvecFiltres() throws SQLException {
        // 1. Filtrer par statut ACTIF
        List<Stationnement> actifs = dao.getStationnementsAvecFiltres(testUserId, STATUT_ACTIF, null, null, null);
        assertEquals("Doit trouver 2 stationnements actifs", 2, actifs.size());
        for (Stationnement s : actifs) {
            assertEquals("Statut incorrect pour filtre ACTIF", STATUT_ACTIF, s.getStatut());
        }
        
        // 2. Filtrer par type VOIRIE
        List<Stationnement> voiries = dao.getStationnementsAvecFiltres(testUserId, null, TYPE_STATIONNEMENT_VOIRIE, null, null);
        assertEquals("Doit trouver 2 stationnements voirie", 2, voiries.size());
        for (Stationnement s : voiries) {
            assertEquals("Type stationnement incorrect pour filtre VOIRIE", TYPE_STATIONNEMENT_VOIRIE, s.getTypeStationnement());
        }
        
        // 3. Filtrer par type PARKING
        List<Stationnement> parkings = dao.getStationnementsAvecFiltres(testUserId, null, TYPE_STATIONNEMENT_PARKING, null, null);
        assertEquals("Doit trouver 1 stationnement parking", 1, parkings.size());
        for (Stationnement s : parkings) {
            assertEquals("Type stationnement incorrect pour filtre PARKING", TYPE_STATIONNEMENT_PARKING, s.getTypeStationnement());
        }
        
        // 4. Filtrer par statut TERMINE
        List<Stationnement> termines = dao.getStationnementsAvecFiltres(testUserId, STATUT_TERMINE, null, null, null);
        assertEquals("Doit trouver 1 stationnement terminé", 1, termines.size());
        for (Stationnement s : termines) {
            assertEquals("Statut incorrect pour filtre TERMINE", STATUT_TERMINE, s.getStatut());
        }
        
        // 5. Filtrer sans filtres (tous les stationnements)
        List<Stationnement> tous = dao.getStationnementsAvecFiltres(testUserId, null, null, null, null);
        assertEquals("Doit trouver tous les stationnements", 3, tous.size());
    }
    
    // ==================== TESTS DES STATISTIQUES ====================
    
    /**
     * Test des statistiques de stationnements.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetStatistiquesStationnements() throws SQLException {
        // 1. Exécuter la méthode à tester
        Object[] stats = dao.getStatistiquesStationnements(testUserId);
        
        // 2. Vérifier les assertions
        assertNotNull("Les statistiques ne doivent pas être null", stats);
        assertEquals("Le tableau doit contenir 5 éléments", 5, stats.length);
        
        // 3. Vérifier chaque statistique
        int total = (int) stats[0];
        int actifs = (int) stats[1];
        int voirie = (int) stats[2];
        int parking = (int) stats[3];
        double totalCout = (double) stats[4];
        
        assertEquals("Total incorrect", 3, total);
        assertEquals("Actifs incorrect", 2, actifs);
        assertEquals("Voirie incorrect", 2, voirie);
        assertEquals("Parking incorrect", 1, parking);
        assertEquals("Total coût incorrect", 3.50, totalCout, 0.001);
    }

}
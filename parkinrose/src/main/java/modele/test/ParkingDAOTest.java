package modele.test;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import modele.Parking;
import modele.dao.MySQLConnection;
import modele.dao.ParkingDAO;

import java.sql.*;
import java.util.List;

/**
 * Classe de tests unitaires pour la classe ParkingDAO.
 * 
 * Cette classe teste les opérations CRUD sur les parkings ainsi que
 * les méthodes spécifiques de gestion des places disponibles.
 */
public class ParkingDAOTest {
    
    // ==================== VARIABLES DE TEST ====================
    
    private ParkingDAO dao;        // DAO à tester (singleton)
    private Connection conn;       // Connexion à la base de données
    
    // Constantes pour les tests
    private static final String ID_PARKING_1 = "TEST_PARK_1";
    private static final String ID_PARKING_2 = "TEST_PARK_2";
    private static final String ID_PARKING_3 = "TEST_PARK_3";
    private static final String ID_PARKING_INEXISTANT = "INEXISTANT";
    private static final String LIBELLE_PARKING_1 = "Parking Test 1";
    private static final String LIBELLE_PARKING_2 = "Parking Relais Test";
    private static final String ADRESSE_PARKING_1 = "Adresse Test 1";
    private static final String ADRESSE_PARKING_2 = "Adresse Test 2";
    private static final int NOMBRE_PLACES_1 = 100;
    private static final int NOMBRE_PLACES_2 = 200;
    private static final int PLACES_DISPONIBLES_1 = 50;
    private static final int PLACES_DISPONIBLES_2 = 100;
    private static final double HAUTEUR_PARKING_1 = 2.0;
    private static final double HAUTEUR_PARKING_2 = 2.5;
    private static final boolean TARIF_SOIREE_1 = true;
    private static final boolean TARIF_SOIREE_2 = false;
    private static final boolean HAS_MOTO_1 = true;
    private static final boolean HAS_MOTO_2 = false;
    private static final int PLACES_MOTO_1 = 10;
    private static final int PLACES_MOTO_2 = 0;
    private static final int PLACES_MOTO_DISPONIBLES_1 = 5;
    private static final int PLACES_MOTO_DISPONIBLES_2 = 0;
    private static final boolean EST_RELAIS_1 = false;
    private static final boolean EST_RELAIS_2 = true;
    private static final Float POSITION_X_1 = 1.4442f;
    private static final Float POSITION_Y_1 = 43.6047f;
    
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
        dao = ParkingDAO.getInstance();
        
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
            stmt.execute("DELETE FROM Stationnement");
            stmt.execute("DELETE FROM Parking");
            
            // 2. Insérer des parkings de test
            insererParkingsTest(stmt);
        }
    }
    
    /**
     * Insère des parkings de test dans la base.
     * 
     * @param stmt Statement pour exécuter les requêtes
     * @throws SQLException en cas d'erreur SQL
     */
    private void insererParkingsTest(Statement stmt) throws SQLException {
        // Parking 1: Normal avec toutes les caractéristiques
        String sql1 = "INSERT INTO Parking (id_parking, libelle_parking, adresse_parking, " +
                     "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                     "has_moto, places_moto, places_moto_disponibles, est_relais, position_x, position_y) " +
                     "VALUES ('" + ID_PARKING_1 + "', '" + LIBELLE_PARKING_1 + "', '" + ADRESSE_PARKING_1 + "', " +
                     NOMBRE_PLACES_1 + ", " + PLACES_DISPONIBLES_1 + ", " + HAUTEUR_PARKING_1 + ", " +
                     (TARIF_SOIREE_1 ? 1 : 0) + ", " + (HAS_MOTO_1 ? 1 : 0) + ", " + PLACES_MOTO_1 + ", " +
                     PLACES_MOTO_DISPONIBLES_1 + ", " + (EST_RELAIS_1 ? 1 : 0) + ", " + 
                     POSITION_X_1 + ", " + POSITION_Y_1 + ")";
        stmt.execute(sql1);
        
        // Parking 2: Parking relais sans motos
        String sql2 = "INSERT INTO Parking (id_parking, libelle_parking, adresse_parking, " +
                     "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                     "has_moto, places_moto, places_moto_disponibles, est_relais) " +
                     "VALUES ('" + ID_PARKING_2 + "', '" + LIBELLE_PARKING_2 + "', '" + ADRESSE_PARKING_2 + "', " +
                     NOMBRE_PLACES_2 + ", " + PLACES_DISPONIBLES_2 + ", " + HAUTEUR_PARKING_2 + ", " +
                     (TARIF_SOIREE_2 ? 1 : 0) + ", " + (HAS_MOTO_2 ? 1 : 0) + ", " + PLACES_MOTO_2 + ", " +
                     PLACES_MOTO_DISPONIBLES_2 + ", " + (EST_RELAIS_2 ? 1 : 0) + ")";
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
            stmt.execute("DELETE FROM Stationnement");
            stmt.execute("DELETE FROM Parking");
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
     * Vérifie que tous les parkings sont récupérés.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testFindAll() throws SQLException {
        // 1. Exécuter la méthode à tester
        List<Parking> parkings = dao.findAll();
        
        // 2. Vérifier les assertions de base
        assertNotNull("La liste ne doit pas être null", parkings);
        assertTrue("Doit contenir au moins 2 parkings (ceux insérés en setup)", 
                  parkings.size() >= 2);
        
        // 3. Vérifier que les parkings de test sont présents
        boolean found1 = false;
        boolean found2 = false;
        for (Parking p : parkings) {
            if (ID_PARKING_1.equals(p.getIdParking())) {
                found1 = true;
                verifierParking1(p);
            }
            if (ID_PARKING_2.equals(p.getIdParking())) {
                found2 = true;
                verifierParking2(p);
            }
        }
        
        // 4. Vérifier que tous les parkings attendus ont été trouvés
        assertTrue(ID_PARKING_1 + " doit être trouvé", found1);
        assertTrue(ID_PARKING_2 + " doit être trouvé", found2);
    }
    
    /**
     * Vérifie les attributs du parking 1 (normal avec toutes les caractéristiques).
     * 
     * @param p Le parking à vérifier
     */
    private void verifierParking1(Parking p) {
        assertEquals("Libellé incorrect pour " + ID_PARKING_1, LIBELLE_PARKING_1, p.getLibelleParking());
        assertEquals("Nombre places incorrect pour " + ID_PARKING_1, NOMBRE_PLACES_1, p.getNombrePlaces());
        assertEquals("Places disponibles incorrectes pour " + ID_PARKING_1, 
                    PLACES_DISPONIBLES_1, p.getPlacesDisponibles());
        assertEquals("Hauteur incorrecte pour " + ID_PARKING_1, HAUTEUR_PARKING_1, p.getHauteurParking(), 0.001);
        assertEquals("Tarif soirée incorrect pour " + ID_PARKING_1, TARIF_SOIREE_1, p.hasTarifSoiree());
        assertEquals("Has moto incorrect pour " + ID_PARKING_1, HAS_MOTO_1, p.hasMoto());
        assertEquals("Places moto incorrectes pour " + ID_PARKING_1, PLACES_MOTO_1, p.getPlacesMoto());
        assertEquals("Places moto disponibles incorrectes pour " + ID_PARKING_1, 
                    PLACES_MOTO_DISPONIBLES_1, p.getPlacesMotoDisponibles());
        assertEquals("Est relais incorrect pour " + ID_PARKING_1, EST_RELAIS_1, p.isEstRelais());
        assertNotNull("Position X ne doit pas être null pour " + ID_PARKING_1, p.getPositionX());
        assertNotNull("Position Y ne doit pas être null pour " + ID_PARKING_1, p.getPositionY());
        assertEquals("Position X incorrecte pour " + ID_PARKING_1, 
                    POSITION_X_1, p.getPositionX(), 0.001);
        assertEquals("Position Y incorrecte pour " + ID_PARKING_1, 
                    POSITION_Y_1, p.getPositionY(), 0.001);
    }
    
    /**
     * Vérifie les attributs du parking 2 (parking relais).
     * 
     * @param p Le parking à vérifier
     */
    private void verifierParking2(Parking p) {
        assertEquals("Libellé incorrect pour " + ID_PARKING_2, LIBELLE_PARKING_2, p.getLibelleParking());
        assertTrue("Est relais incorrect pour " + ID_PARKING_2, p.isEstRelais());
        assertFalse("Tarif soirée incorrect pour " + ID_PARKING_2, p.hasTarifSoiree());
        assertFalse("Has moto incorrect pour " + ID_PARKING_2, p.hasMoto());
        assertNull("Position X doit être null pour " + ID_PARKING_2, p.getPositionX());
        assertNull("Position Y doit être null pour " + ID_PARKING_2, p.getPositionY());
    }
    
    /**
     * Test de la méthode findById() avec un ID existant.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testFindById() throws SQLException {
        // 1. Exécuter la méthode à tester
        Parking parking = dao.findById(ID_PARKING_1);
        
        // 2. Vérifier les assertions
        assertNotNull("Le parking ne doit pas être null", parking);
        assertEquals("ID incorrect", ID_PARKING_1, parking.getIdParking());
        assertEquals("Libellé incorrect", LIBELLE_PARKING_1, parking.getLibelleParking());
        assertEquals("Nombre places incorrect", NOMBRE_PLACES_1, parking.getNombrePlaces());
        assertEquals("Places disponibles incorrectes", PLACES_DISPONIBLES_1, parking.getPlacesDisponibles());
    }
    
    /**
     * Test de la méthode findById() avec un ID inexistant.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testFindById_NotFound() throws SQLException {
        // 1. Exécuter la méthode à tester
        Parking parking = dao.findById(ID_PARKING_INEXISTANT);
        
        // 2. Vérifier que null est retourné
        assertNull("Doit retourner null pour un ID inexistant", parking);
    }
    
    /**
     * Test de la méthode create().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testCreate() throws SQLException {
        // 1. Créer un nouveau parking
        Parking nouveau = new Parking(
            ID_PARKING_3,
            "Nouveau Parking",
            "Nouvelle Adresse",
            150,
            75,
            2.2,
            true,
            true,
            15,
            8,
            false,
            1.5f,
            43.5f
        );
        
        // 2. Exécuter la méthode à tester
        dao.create(nouveau);
        
        // 3. Vérifier que le parking a été créé
        Parking verif = dao.findById(ID_PARKING_3);
        assertNotNull("Le parking doit être créé", verif);
        assertEquals("Libellé incorrect après création", "Nouveau Parking", verif.getLibelleParking());
        assertEquals("Nombre places incorrect après création", 150, verif.getNombrePlaces());
        assertEquals("Places disponibles incorrectes après création", 75, verif.getPlacesDisponibles());
        assertTrue("Tarif soirée incorrect après création", verif.hasTarifSoiree());
        assertTrue("Has moto incorrect après création", verif.hasMoto());
        assertEquals("Places moto incorrectes après création", 15, verif.getPlacesMoto());
        assertEquals("Places moto disponibles incorrectes après création", 8, verif.getPlacesMotoDisponibles());
        assertEquals("Position X incorrecte après création", 1.5f, verif.getPositionX(), 0.001);
        assertEquals("Position Y incorrecte après création", 43.5f, verif.getPositionY(), 0.001);
    }
    
    /**
     * Test de la méthode update().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testUpdate() throws SQLException {
        // 1. Récupérer un parking existant
        Parking parking = dao.findById(ID_PARKING_1);
        assertNotNull("Le parking doit exister", parking);
        
        // 2. Modifier ses attributs
        String nouveauLibelle = "Parking Modifié";
        int nouvellesPlacesDisponibles = 30;
        boolean nouveauTarifSoiree = false;
        
        parking.setLibelleParking(nouveauLibelle);
        parking.setPlacesDisponibles(nouvellesPlacesDisponibles);
        parking.setTarifSoiree(nouveauTarifSoiree);
        
        // 3. Exécuter la méthode à tester
        dao.update(parking);
        
        // 4. Vérifier la mise à jour
        Parking verif = dao.findById(ID_PARKING_1);
        assertNotNull("Le parking doit toujours exister", verif);
        assertEquals("Libellé non mis à jour", nouveauLibelle, verif.getLibelleParking());
        assertEquals("Places disponibles non mises à jour", 
                    nouvellesPlacesDisponibles, verif.getPlacesDisponibles());
        assertFalse("Tarif soirée non mis à jour", verif.hasTarifSoiree());
    }
    
    // ==================== TESTS DES MÉTHODES DE GESTION DES PLACES ====================
    
    /**
     * Test de la méthode decrementerPlacesDisponibles().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testDecrementerPlacesDisponibles() throws SQLException {
        // 1. Exécuter la méthode à tester
        boolean result = dao.decrementerPlacesDisponibles(ID_PARKING_1);
        
        // 2. Vérifier le résultat
        assertTrue("La décrémentation doit réussir", result);
        
        // 3. Vérifier que le nombre de places disponibles a diminué
        Parking verif = dao.findById(ID_PARKING_1);
        assertEquals("Places disponibles incorrectes après décrémentation", 
                    PLACES_DISPONIBLES_1 - 1, verif.getPlacesDisponibles());
    }
    
    /**
     * Test de la méthode decrementerPlacesDisponibles() quand il n'y a plus de places.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testDecrementerPlacesDisponibles_NoPlaces() throws SQLException {
        // 1. Mettre places disponibles à 0
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE Parking SET places_disponibles = 0 WHERE id_parking = '" + ID_PARKING_1 + "'");
        }
        
        // 2. Exécuter la méthode à tester
        boolean result = dao.decrementerPlacesDisponibles(ID_PARKING_1);
        
        // 3. Vérifier que l'opération a échoué
        assertFalse("Doit retourner false si pas de places disponibles", result);
        
        // 4. Vérifier que le nombre de places est toujours 0
        Parking verif = dao.findById(ID_PARKING_1);
        assertEquals("Places disponibles doivent rester à 0", 0, verif.getPlacesDisponibles());
    }
    
    /**
     * Test de la méthode incrementerPlacesDisponibles().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testIncrementerPlacesDisponibles() throws SQLException {
        // 1. Exécuter la méthode à tester
        boolean result = dao.incrementerPlacesDisponibles(ID_PARKING_1);
        
        // 2. Vérifier le résultat
        assertTrue("L'incrémentation doit réussir", result);
        
        // 3. Vérifier que le nombre de places disponibles a augmenté
        Parking verif = dao.findById(ID_PARKING_1);
        assertEquals("Places disponibles incorrectes après incrémentation", 
                    PLACES_DISPONIBLES_1 + 1, verif.getPlacesDisponibles());
    }
    
    /**
     * Test de la méthode decrementerPlacesMotoDisponibles().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testDecrementerPlacesMotoDisponibles() throws SQLException {
        // 1. Exécuter la méthode à tester
        boolean result = dao.decrementerPlacesMotoDisponibles(ID_PARKING_1);
        
        // 2. Vérifier le résultat
        assertTrue("La décrémentation moto doit réussir", result);
        
        // 3. Vérifier que le nombre de places moto disponibles a diminué
        Parking verif = dao.findById(ID_PARKING_1);
        assertEquals("Places moto disponibles incorrectes après décrémentation", 
                    PLACES_MOTO_DISPONIBLES_1 - 1, verif.getPlacesMotoDisponibles());
    }
    
    /**
     * Test de la méthode decrementerPlacesMotoDisponibles() pour un parking sans motos.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testDecrementerPlacesMotoDisponibles_PasDeMotos() throws SQLException {
        // 1. Exécuter la méthode sur un parking sans motos
        boolean result = dao.decrementerPlacesMotoDisponibles(ID_PARKING_2);
        
        // 2. Vérifier que l'opération a échoué
        assertFalse("Doit retourner false pour parking sans motos", result);
    }
    
    /**
     * Test de la méthode incrementerPlacesMotoDisponibles().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testIncrementerPlacesMotoDisponibles() throws SQLException {
        // 1. Exécuter la méthode à tester
        boolean result = dao.incrementerPlacesMotoDisponibles(ID_PARKING_1);
        
        // 2. Vérifier le résultat
        assertTrue("L'incrémentation moto doit réussir", result);
        
        // 3. Vérifier que le nombre de places moto disponibles a augmenté
        Parking verif = dao.findById(ID_PARKING_1);
        assertEquals("Places moto disponibles incorrectes après incrémentation", 
                    PLACES_MOTO_DISPONIBLES_1 + 1, verif.getPlacesMotoDisponibles());
    }
    
    // ==================== TESTS DES MÉTHODES D'INFORMATION ====================
    
    /**
     * Test de la méthode getPlacesDisponibles().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetPlacesDisponibles() throws SQLException {
        // 1. Exécuter la méthode à tester
        int places = dao.getPlacesDisponibles(ID_PARKING_1);
        
        // 2. Vérifier le résultat
        assertEquals("Nombre de places disponibles incorrect", 
                    PLACES_DISPONIBLES_1, places);
    }
    
    /**
     * Test de la méthode getPlacesDisponibles() avec un ID inexistant.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetPlacesDisponibles_NotFound() throws SQLException {
        // 1. Exécuter la méthode avec un ID inexistant
        int places = dao.getPlacesDisponibles(ID_PARKING_INEXISTANT);
        
        // 2. Vérifier que -1 est retourné
        assertEquals("Doit retourner -1 pour ID inexistant", -1, places);
    }
    
    /**
     * Test de la méthode getPlacesMotoDisponibles().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetPlacesMotoDisponibles() throws SQLException {
        // 1. Exécuter la méthode à tester
        int places = dao.getPlacesMotoDisponibles(ID_PARKING_1);
        
        // 2. Vérifier le résultat
        assertEquals("Nombre de places moto disponibles incorrect", 
                    PLACES_MOTO_DISPONIBLES_1, places);
    }
    
    /**
     * Test de la méthode getPlacesMotoDisponibles() pour un parking sans motos.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testGetPlacesMotoDisponibles_PasDeMotos() throws SQLException {
        // 1. Exécuter la méthode sur un parking sans motos
        int places = dao.getPlacesMotoDisponibles(ID_PARKING_2);
        
        // 2. Vérifier que 0 est retourné
        assertEquals("Doit retourner 0 pour parking sans motos", 0, places);
    }
    
    /**
     * Test de la méthode hasPlacesMotoDisponibles().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testHasPlacesMotoDisponibles() throws SQLException {
        // 1. Exécuter la méthode à tester
        boolean result = dao.hasPlacesMotoDisponibles(ID_PARKING_1);
        
        // 2. Vérifier le résultat
        assertTrue("Doit retourner true pour parking avec places moto disponibles", result);
        
        // 3. Mettre places moto à 0
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE Parking SET places_moto_disponibles = 0 WHERE id_parking = '" + ID_PARKING_1 + "'");
        }
        
        // 4. Re-exécuter la méthode
        result = dao.hasPlacesMotoDisponibles(ID_PARKING_1);
        
        // 5. Vérifier que false est retourné
        assertFalse("Doit retourner false si pas de places moto disponibles", result);
    }
    
    /**
     * Test de la méthode hasPlacesMotoDisponibles() pour un parking sans motos.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testHasPlacesMotoDisponibles_PasDeMotos() throws SQLException {
        // 1. Exécuter la méthode sur un parking sans motos
        boolean result = dao.hasPlacesMotoDisponibles(ID_PARKING_2);
        
        // 2. Vérifier que false est retourné
        assertFalse("Doit retourner false pour parking sans motos", result);
    }
    
    // ==================== TESTS DES MÉTHODES STATIQUES ====================
    
    /**
     * Test de la méthode statique idParkingExiste().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testIdParkingExiste() throws SQLException {
        // 1. Vérifier un ID qui existe
        boolean existe = ParkingDAO.idParkingExiste(ID_PARKING_1);
        assertTrue("L'ID existant doit être trouvé", existe);
        
        // 2. Vérifier un ID qui n'existe pas
        boolean nExistePas = ParkingDAO.idParkingExiste(ID_PARKING_INEXISTANT);
        assertFalse("L'ID inexistant ne doit pas être trouvé", nExistePas);
    }
    
    /**
     * Test de la méthode statique idParkingExiste() avec ID null.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testIdParkingExiste_Null() throws SQLException {
        // 1. Vérifier avec ID null
        boolean existe = ParkingDAO.idParkingExiste(null);
        
        // 2. Vérifier que false est retourné
        assertFalse("Doit retourner false pour ID null", existe);
    }
    
    /**
     * Test de la méthode statique idParkingExiste() avec ID vide.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testIdParkingExiste_Vide() throws SQLException {
        // 1. Vérifier avec ID vide
        boolean existe = ParkingDAO.idParkingExiste("");
        
        // 2. Vérifier que false est retourné
        assertFalse("Doit retourner false pour ID vide", existe);
    }
    
    // ==================== TESTS DE RECHERCHE ====================
    
    /**
     * Test de la méthode rechercherParkings().
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testRechercherParkings() throws SQLException {
        // 1. Rechercher avec un terme qui correspond aux deux parkings
        List<Parking> resultats = dao.rechercherParkings("Test");
        
        // 2. Vérifier les résultats
        assertNotNull("Les résultats ne doivent pas être null", resultats);
        assertTrue("Doit trouver au moins 2 parkings avec 'Test'", resultats.size() >= 2);
        
        // 3. Rechercher avec un terme spécifique
        resultats = dao.rechercherParkings("Relais");
        assertFalse("Doit trouver au moins un parking avec 'Relais'", resultats.isEmpty());
        assertTrue("Le parking trouvé doit contenir 'Relais' dans son libellé",
                  resultats.get(0).getLibelleParking().contains("Relais"));
    }
    
    /**
     * Test de la méthode rechercherParkings() avec un terme qui ne correspond à rien.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testRechercherParkings_AucunResultat() throws SQLException {
        // 1. Rechercher avec un terme inexistant
        List<Parking> resultats = dao.rechercherParkings("XYZ123");
        
        // 2. Vérifier qu'une liste vide est retournée
        assertNotNull("Les résultats ne doivent pas être null même si vide", resultats);
        assertTrue("La liste doit être vide pour terme inexistant", resultats.isEmpty());
    }
    
    /**
     * Test de la méthode rechercherParkings() avec un terme null.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testRechercherParkings_Null() throws SQLException {
        // 1. Rechercher avec terme null
        List<Parking> resultats = dao.rechercherParkings(null);
        
        // 2. Vérifier qu'une liste vide est retournée
        assertNotNull("Les résultats ne doivent pas être null", resultats);
        assertTrue("La liste doit être vide pour terme null", resultats.isEmpty());
    }
    
    /**
     * Test de la méthode rechercherParkings() avec un terme vide.
     * 
     * @throws SQLException en cas d'erreur d'accès à la base
     */
    @Test
    public void testRechercherParkings_Vide() throws SQLException {
        // 1. Rechercher avec terme vide
        List<Parking> resultats = dao.rechercherParkings("");
        
        // 2. Vérifier qu'une liste vide est retournée
        assertNotNull("Les résultats ne doivent pas être null", resultats);
        assertTrue("La liste doit être vide pour terme vide", resultats.isEmpty());
    }
}
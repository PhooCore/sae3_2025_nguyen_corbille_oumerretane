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

public class ParkingDAOTest {
    
    private ParkingDAO dao;
    private Connection conn;
    
    @Before
    public void setUp() throws SQLException {
        dao = ParkingDAO.getInstance();
        conn = MySQLConnection.getConnection();
        
        // Nettoyer et préparer la base de test
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Stationnement");
            stmt.execute("DELETE FROM Parking");
            
            // Insérer des données de test
            stmt.execute("INSERT INTO Parking (id_parking, libelle_parking, adresse_parking, " +
                        "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                        "has_moto, places_moto, places_moto_disponibles, est_relais, position_x, position_y) " +
                        "VALUES ('TEST_PARK_1', 'Parking Test 1', 'Adresse Test 1', " +
                        "100, 50, 2.0, 1, 1, 10, 5, 0, 1.4442, 43.6047)");
            
            stmt.execute("INSERT INTO Parking (id_parking, libelle_parking, adresse_parking, " +
                        "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                        "has_moto, places_moto, places_moto_disponibles, est_relais) " +
                        "VALUES ('TEST_PARK_2', 'Parking Relais Test', 'Adresse Test 2', " +
                        "200, 100, 2.5, 0, 0, 0, 0, 1)");
        }
    }
    
    @After
    public void tearDown() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Stationnement");
            stmt.execute("DELETE FROM Parking");
        }
        
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    @Test
    public void testFindAll() throws SQLException {
        List<Parking> parkings = dao.findAll();
        
        assertNotNull(parkings);
        assertTrue(parkings.size() >= 2);
        
        boolean found1 = false;
        boolean found2 = false;
        for (Parking p : parkings) {
            if ("TEST_PARK_1".equals(p.getIdParking())) {
                found1 = true;
                assertEquals("Parking Test 1", p.getLibelleParking());
                assertEquals(100, p.getNombrePlaces());
                assertEquals(50, p.getPlacesDisponibles());
                assertTrue(p.hasTarifSoiree());
                assertTrue(p.hasMoto());
                assertEquals(10, p.getPlacesMoto());
                assertEquals(5, p.getPlacesMotoDisponibles());
                assertFalse(p.isEstRelais());
                assertNotNull(p.getPositionX());
                assertNotNull(p.getPositionY());
            }
            if ("TEST_PARK_2".equals(p.getIdParking())) {
                found2 = true;
                assertTrue(p.isEstRelais());
                assertFalse(p.hasTarifSoiree());
            }
        }
        
        assertTrue(found1);
        assertTrue(found2);
    }
    
    @Test
    public void testFindById() throws SQLException {
        Parking parking = dao.findById("TEST_PARK_1");
        
        assertNotNull(parking);
        assertEquals("TEST_PARK_1", parking.getIdParking());
        assertEquals("Parking Test 1", parking.getLibelleParking());
        assertEquals(100, parking.getNombrePlaces());
        assertEquals(50, parking.getPlacesDisponibles());
    }
    
    @Test
    public void testCreate() throws SQLException {
        Parking nouveau = new Parking(
            "TEST_PARK_3",
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
        
        dao.create(nouveau);
        
        Parking verif = dao.findById("TEST_PARK_3");
        assertNotNull(verif);
        assertEquals("Nouveau Parking", verif.getLibelleParking());
        assertEquals(150, verif.getNombrePlaces());
        assertEquals(75, verif.getPlacesDisponibles());
        assertTrue(verif.hasTarifSoiree());
        assertTrue(verif.hasMoto());
        assertEquals(15, verif.getPlacesMoto());
        assertEquals(8, verif.getPlacesMotoDisponibles());
    }
    
    @Test
    public void testUpdate() throws SQLException {
        Parking parking = dao.findById("TEST_PARK_1");
        assertNotNull(parking);
        
        parking.setLibelleParking("Parking Modifié");
        parking.setPlacesDisponibles(30);
        parking.setTarifSoiree(false);
        
        dao.update(parking);
        
        Parking verif = dao.findById("TEST_PARK_1");
        assertNotNull(verif);
        assertEquals("Parking Modifié", verif.getLibelleParking());
        assertEquals(30, verif.getPlacesDisponibles());
        assertFalse(verif.hasTarifSoiree());
    }
    
    @Test
    public void testDecrementerPlacesDisponibles() throws SQLException {
        boolean result = dao.decrementerPlacesDisponibles("TEST_PARK_1");
        assertTrue(result);
        
        Parking verif = dao.findById("TEST_PARK_1");
        assertEquals(49, verif.getPlacesDisponibles());
    }
    
    @Test
    public void testDecrementerPlacesDisponibles_NoPlaces() throws SQLException {
        // Mettre places disponibles à 0
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE Parking SET places_disponibles = 0 WHERE id_parking = 'TEST_PARK_1'");
        }
        
        boolean result = dao.decrementerPlacesDisponibles("TEST_PARK_1");
        assertFalse("Doit retourner false si pas de places disponibles", result);
    }
    
    @Test
    public void testIncrementerPlacesDisponibles() throws SQLException {
        boolean result = dao.incrementerPlacesDisponibles("TEST_PARK_1");
        assertTrue(result);
        
        Parking verif = dao.findById("TEST_PARK_1");
        assertEquals(51, verif.getPlacesDisponibles());
    }
    
    @Test
    public void testDecrementerPlacesMotoDisponibles() throws SQLException {
        boolean result = dao.decrementerPlacesMotoDisponibles("TEST_PARK_1");
        assertTrue(result);
        
        Parking verif = dao.findById("TEST_PARK_1");
        assertEquals(4, verif.getPlacesMotoDisponibles());
    }
    
    @Test
    public void testIncrementerPlacesMotoDisponibles() throws SQLException {
        boolean result = dao.incrementerPlacesMotoDisponibles("TEST_PARK_1");
        assertTrue(result);
        
        Parking verif = dao.findById("TEST_PARK_1");
        assertEquals(6, verif.getPlacesMotoDisponibles());
    }
    
    @Test
    public void testGetPlacesDisponibles() throws SQLException {
        int places = dao.getPlacesDisponibles("TEST_PARK_1");
        assertEquals(50, places);
    }
    
    @Test
    public void testGetPlacesMotoDisponibles() throws SQLException {
        int places = dao.getPlacesMotoDisponibles("TEST_PARK_1");
        assertEquals(5, places);
    }
    
    @Test
    public void testHasPlacesMotoDisponibles() throws SQLException {
        boolean result = dao.hasPlacesMotoDisponibles("TEST_PARK_1");
        assertTrue(result);
        
        // Mettre places moto à 0
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE Parking SET places_moto_disponibles = 0 WHERE id_parking = 'TEST_PARK_1'");
        }
        
        result = dao.hasPlacesMotoDisponibles("TEST_PARK_1");
        assertFalse(result);
    }
    
    @Test
    public void testIdParkingExiste() throws SQLException {
        boolean existe = ParkingDAO.idParkingExiste("TEST_PARK_1");
        assertTrue(existe);
        
        boolean nExistePas = ParkingDAO.idParkingExiste("INEXISTANT");
        assertFalse(nExistePas);
    }
    
    @Test
    public void testRechercherParkings() throws SQLException {
        List<Parking> resultats = dao.rechercherParkings("Test");
        assertNotNull(resultats);
        assertTrue(resultats.size() >= 2);
        
        resultats = dao.rechercherParkings("Relais");
        assertFalse(resultats.isEmpty());
    }
}
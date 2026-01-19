package modele.test;

import org.junit.Test;

import modele.Parking;

import org.junit.Before;
import static org.junit.Assert.*;

public class ParkingTest {
    
    private Parking parking;
    
    @Before
    public void setUp() {
        parking = new Parking(
            "PARK_001",
            "Parking Capitole",
            "Place du Capitole, Toulouse",
            200,
            150,
            2.1,
            true,
            true,
            20,
            10,
            false,
            1.4442f,
            43.6047f
        );
    }
    
    @Test
    public void testConstructeurComplet() {
        assertEquals("PARK_001", parking.getIdParking());
        assertEquals("Parking Capitole", parking.getLibelleParking());
        assertEquals("Place du Capitole, Toulouse", parking.getAdresseParking());
        assertEquals(200, parking.getNombrePlaces());
        assertEquals(150, parking.getPlacesDisponibles());
        assertEquals(2.1, parking.getHauteurParking(), 0.001);
        assertTrue(parking.hasTarifSoiree());
        assertTrue(parking.hasMoto());
        assertEquals(20, parking.getPlacesMoto());
        assertEquals(10, parking.getPlacesMotoDisponibles());
        assertFalse(parking.isEstRelais());
        assertEquals(1.4442f, parking.getPositionX(), 0.0001f);
        assertEquals(43.6047f, parking.getPositionY(), 0.0001f);
    }
    
    @Test
    public void testConstructeurSansPositions() {
        Parking parkingSimple = new Parking(
            "PARK_002",
            "Parking Simple",
            "Adresse Simple",
            100,
            50,
            2.0,
            false,
            false,
            0,
            0,
            true
        );
        
        assertEquals("PARK_002", parkingSimple.getIdParking());
        assertEquals("Parking Simple", parkingSimple.getLibelleParking());
        assertTrue(parkingSimple.isEstRelais());
        assertNull(parkingSimple.getPositionX());
        assertNull(parkingSimple.getPositionY());
    }
    
    @Test
    public void testConstructeurBasique() {
        Parking parkingBasique = new Parking(
            "PARK_003",
            "Parking Basique",
            "Adresse Basique",
            50,
            25,
            2.5,
            true
        );
        
        assertEquals("PARK_003", parkingBasique.getIdParking());
        assertEquals(50, parkingBasique.getNombrePlaces());
        assertEquals(25, parkingBasique.getPlacesDisponibles());
        assertTrue(parkingBasique.hasTarifSoiree());
        assertFalse(parkingBasique.hasMoto());
        assertEquals(0, parkingBasique.getPlacesMoto());
        assertFalse(parkingBasique.isEstRelais());
    }
    
    @Test
    public void testHasPlacesDisponibles() {
        parking.setPlacesDisponibles(10);
        assertTrue(parking.hasPlacesDisponibles());
        
        parking.setPlacesDisponibles(0);
        assertFalse(parking.hasPlacesDisponibles());
        
        parking.setPlacesDisponibles(-5);
        assertFalse(parking.hasPlacesDisponibles());
    }
    
    @Test
    public void testHasPlacesMotoDisponibles() {
        parking.setHasMoto(true);
        parking.setPlacesMotoDisponibles(5);
        assertTrue(parking.hasPlacesMotoDisponibles());
        
        parking.setPlacesMotoDisponibles(0);
        assertFalse(parking.hasPlacesMotoDisponibles());
        
        parking.setHasMoto(false);
        parking.setPlacesMotoDisponibles(10);
        assertFalse(parking.hasPlacesMotoDisponibles());
    }
    
    @Test
    public void testTarifHoraire() {
        parking.setTarifHoraire(3.50);
        assertEquals(3.50, parking.getTarifHoraire(), 0.001);
    }
    
    @Test
    public void testToString() {
        String resultat = parking.toString();
        assertTrue(resultat.contains("Parking Capitole"));
        assertTrue(resultat.contains("Place du Capitole, Toulouse"));
        assertTrue(resultat.contains("150/200"));
        assertTrue(resultat.contains("places moto"));
    }
    
    @Test
    public void testToString_SansMoto() {
        parking.setHasMoto(false);
        String resultat = parking.toString();
        assertFalse(resultat.contains("places moto"));
    }
    
    @Test
    public void testSetters() {
        // Test des setters (pas besoin de constructeur par défaut)
        parking.setIdParking("PARK_NEW");
        assertEquals("PARK_NEW", parking.getIdParking());
        
        parking.setLibelleParking("Nouveau Parking");
        assertEquals("Nouveau Parking", parking.getLibelleParking());
        
        parking.setAdresseParking("Nouvelle Adresse");
        assertEquals("Nouvelle Adresse", parking.getAdresseParking());
        
        parking.setNombrePlaces(300);
        assertEquals(300, parking.getNombrePlaces());
        
        parking.setPlacesDisponibles(100);
        assertEquals(100, parking.getPlacesDisponibles());
        
        parking.setHauteurParking(2.5);
        assertEquals(2.5, parking.getHauteurParking(), 0.001);
        
        parking.setTarifSoiree(false);
        assertFalse(parking.hasTarifSoiree());
        
        parking.setHasMoto(false);
        assertFalse(parking.hasMoto());
        
        parking.setPlacesMoto(15);
        assertEquals(15, parking.getPlacesMoto());
        
        parking.setPlacesMotoDisponibles(8);
        assertEquals(8, parking.getPlacesMotoDisponibles());
        
        parking.setEstRelais(true);
        assertTrue(parking.isEstRelais());
        
        parking.setPositionX(1.0f);
        assertEquals(1.0f, parking.getPositionX(), 0.001f);
        
        parking.setPositionY(2.0f);
        assertEquals(2.0f, parking.getPositionY(), 0.001f);
    }
    
    @Test
    public void testConstructeursChainés() {
        // Test que les constructeurs appellent bien le constructeur principal
        Parking p1 = new Parking("P1", "Lib1", "Addr1", 100, 50, 2.0, true);
        assertEquals("P1", p1.getIdParking());
        assertEquals("Lib1", p1.getLibelleParking());
        assertEquals(100, p1.getNombrePlaces());
        assertEquals(50, p1.getPlacesDisponibles());
        assertEquals(2.0, p1.getHauteurParking(), 0.001);
        assertTrue(p1.hasTarifSoiree());
        assertFalse(p1.hasMoto());
        
        Parking p2 = new Parking("P2", "Lib2", "Addr2", 200, 100, 2.5, false, false, 0, 0);
        assertEquals("P2", p2.getIdParking());
        assertEquals(200, p2.getNombrePlaces());
        assertEquals(100, p2.getPlacesDisponibles());
        assertFalse(p2.hasTarifSoiree());
        assertFalse(p2.hasMoto());
    }
}
package modele.test;

import org.junit.Test;

import modele.Parking;

import static org.junit.Assert.*;

public class ParkingTest {
    
    @Test
    public void testConstructeurAvecTarifSoiree() {
        Parking parking = new Parking(
            "PARK_CAPITOLE", 
            "Parking Capitole", 
            "Place du Capitole, 31000 Toulouse", 
            250, 
            2.10, 
            true
        );
        
        assertEquals("PARK_CAPITOLE", parking.getIdParking());
        assertEquals("Parking Capitole", parking.getLibelleParking());
        assertEquals("Place du Capitole, 31000 Toulouse", parking.getAdresseParking());
        assertEquals(250, parking.getNombrePlaces());
        assertEquals(2.10, parking.getHauteurParking(), 0.001);
        assertTrue(parking.hasTarifSoiree());
        assertEquals(50, parking.getPlacesDisponibles());
    }
    
    @Test
    public void testConstructeurSansTarifSoiree() {
        Parking parking = new Parking(
            "PARK_SEPT_DENIERS", 
            "Parking Relais Sept Deniers", 
            "Avenue de Grande-Bretagne, 31300 Toulouse", 
            500, 
            2.80, 
            false
        );
        
        assertEquals("PARK_SEPT_DENIERS", parking.getIdParking());
        assertFalse(parking.hasTarifSoiree());
        assertEquals(100, parking.getPlacesDisponibles());
    }
    
    @Test
    public void testPlacesDisponiblesCalcul() {
        Parking petitParking = new Parking("TEST1", "Test1", "Adresse1", 50, 2.0, true);
        assertEquals(10, petitParking.getPlacesDisponibles());
        
        Parking grandParking = new Parking("TEST2", "Test2", "Adresse2", 1000, 2.5, false);
        assertEquals(200, grandParking.getPlacesDisponibles());
    }
    
    @Test
    public void testToString() {
        Parking parking = new Parking("TEST", "Parking Test", "Adresse Test", 100, 2.0, true);
        String str = parking.toString();
        
        assertTrue(str.contains("Parking Test"));
        assertTrue(str.contains("Adresse Test"));
    }
    
    @Test
    public void testGetters() {
        Parking parking = new Parking("ID_TEST", "Libelle Test", "Adresse Test", 150, 2.3, true);
        
        assertEquals("ID_TEST", parking.getIdParking());
        assertEquals("Libelle Test", parking.getLibelleParking());
        assertEquals("Adresse Test", parking.getAdresseParking());
        assertEquals(150, parking.getNombrePlaces());
        assertEquals(2.3, parking.getHauteurParking(), 0.001);
        assertTrue(parking.hasTarifSoiree());
        assertEquals(30, parking.getPlacesDisponibles());
    }
}

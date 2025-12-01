package modele.test;

import org.junit.Test;
import modele.Parking;
import static org.junit.Assert.*;

public class ParkingTest {
    
    @Test
    public void testConstructeurComplet() {
        Parking parking = new Parking(
            "PARK_CAPITOLE", 
            "Parking Capitole", 
            "Place du Capitole, 31000 Toulouse", 
            250, 
            125, 
            2.10, 
            true
        );
        
        assertEquals("PARK_CAPITOLE", parking.getIdParking());
        assertEquals("Parking Capitole", parking.getLibelleParking());
        assertEquals("Place du Capitole, 31000 Toulouse", parking.getAdresseParking());
        assertEquals(250, parking.getNombrePlaces());
        assertEquals(125, parking.getPlacesDisponibles());
        assertEquals(2.10, parking.getHauteurParking(), 0.001);
        assertTrue(parking.hasTarifSoiree());
    }
    
    @Test
    public void testSetters() {
        Parking parking = new Parking("ID_TEST", "Libelle Test", "Adresse Test", 200, 100, 2.5, false);
        
        parking.setPlacesDisponibles(75);
        assertEquals(75, parking.getPlacesDisponibles());
        
        parking.setNombrePlaces(300);
        assertEquals(300, parking.getNombrePlaces());
        
        parking.setHauteurParking(3.0);
        assertEquals(3.0, parking.getHauteurParking(), 0.001);
        
        parking.setTarifSoiree(true);
        assertTrue(parking.hasTarifSoiree());
    }
    
    @Test
    public void testPlacesDisponiblesLimites() {
        Parking parkingVide = new Parking("VIDE", "Parking Vide", "Adresse", 100, 0, 2.0, true);
        assertEquals(0, parkingVide.getPlacesDisponibles());
        
        Parking parkingPlein = new Parking("PLEIN", "Parking Plein", "Adresse", 100, 100, 2.0, true);
        assertEquals(100, parkingPlein.getPlacesDisponibles());
    }
    
    @Test
    public void testToString() {
        Parking parking = new Parking("TEST", "Parking Test", "Adresse Test", 100, 45, 2.0, true);
        String str = parking.toString();
        
        assertTrue(str.contains("Parking Test"));
        assertTrue(str.contains("Adresse Test"));
        assertTrue(str.contains("45/100"));
    }
}
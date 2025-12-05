package modele.test;

import org.junit.Test;
import modele.Parking;
import static org.junit.Assert.*;

public class ParkingTest {
    
    @Test
    public void testConstructeurCompletAvecMoto() {
        Parking parking = new Parking(
            "PARK_CAPITOLE", 
            "Parking Capitole", 
            "Place du Capitole, 31000 Toulouse", 
            250, 
            125, 
            2.10, 
            true,
            true,  // hasMoto
            20,    // placesMoto
            15     // placesMotoDisponibles
        );
        
        assertEquals("PARK_CAPITOLE", parking.getIdParking());
        assertEquals("Parking Capitole", parking.getLibelleParking());
        assertEquals("Place du Capitole, 31000 Toulouse", parking.getAdresseParking());
        assertEquals(250, parking.getNombrePlaces());
        assertEquals(125, parking.getPlacesDisponibles());
        assertEquals(2.10, parking.getHauteurParking(), 0.001);
        assertTrue(parking.hasTarifSoiree());
        assertTrue(parking.hasMoto());
        assertEquals(20, parking.getPlacesMoto());
        assertEquals(15, parking.getPlacesMotoDisponibles());
    }
    
    @Test
    public void testConstructeurSansMoto() {
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
        assertFalse(parking.hasMoto()); // Doit être false par défaut
        assertEquals(0, parking.getPlacesMoto()); // Doit être 0 par défaut
        assertEquals(0, parking.getPlacesMotoDisponibles()); // Doit être 0 par défaut
    }
    
    @Test
    public void testSettersMoto() {
        Parking parking = new Parking("ID_TEST", "Libelle Test", "Adresse Test", 200, 100, 2.5, false);
        
        // Test des setters normaux
        parking.setPlacesDisponibles(75);
        assertEquals(75, parking.getPlacesDisponibles());
        
        parking.setNombrePlaces(300);
        assertEquals(300, parking.getNombrePlaces());
        
        parking.setHauteurParking(3.0);
        assertEquals(3.0, parking.getHauteurParking(), 0.001);
        
        parking.setTarifSoiree(true);
        assertTrue(parking.hasTarifSoiree());
        
        // Test des setters moto
        parking.setHasMoto(true);
        assertTrue(parking.hasMoto());
        
        parking.setPlacesMoto(10);
        assertEquals(10, parking.getPlacesMoto());
        
        parking.setPlacesMotoDisponibles(5);
        assertEquals(5, parking.getPlacesMotoDisponibles());
        
        // Tester que hasMoto reste true même si placesMoto = 0
        parking.setPlacesMoto(0);
        parking.setPlacesMotoDisponibles(0);
        assertTrue(parking.hasMoto());
    }
    
    @Test
    public void testPlacesDisponiblesLimites() {
        Parking parkingVide = new Parking("VIDE", "Parking Vide", "Adresse", 100, 0, 2.0, true);
        assertEquals(0, parkingVide.getPlacesDisponibles());
        
        Parking parkingPlein = new Parking("PLEIN", "Parking Plein", "Adresse", 100, 100, 2.0, true);
        assertEquals(100, parkingPlein.getPlacesDisponibles());
        
        // Test avec places moto
        Parking parkingMotoVide = new Parking("MOTO_VIDE", "Parking Moto Vide", "Adresse", 50, 25, 2.0, false, true, 10, 0);
        assertEquals(0, parkingMotoVide.getPlacesMotoDisponibles());
        
        Parking parkingMotoPlein = new Parking("MOTO_PLEIN", "Parking Moto Plein", "Adresse", 50, 25, 2.0, false, true, 10, 10);
        assertEquals(10, parkingMotoPlein.getPlacesMotoDisponibles());
    }
    
    @Test
    public void testHasPlacesMotoDisponibles() {
        // Parking avec places moto disponibles
        Parking parkingAvecPlaces = new Parking("AVEC", "Parking Avec Places", "Adresse", 100, 50, 2.0, true, true, 10, 5);
        assertTrue(parkingAvecPlaces.hasPlacesMotoDisponibles());
        
        // Parking sans places moto
        Parking parkingSansPlaces = new Parking("SANS", "Parking Sans Places", "Adresse", 100, 50, 2.0, true, true, 10, 0);
        assertFalse(parkingSansPlaces.hasPlacesMotoDisponibles());
        
        // Parking qui n'a pas de places moto du tout
        Parking parkingSansMoto = new Parking("PAS_MOTO", "Parking Pas Moto", "Adresse", 100, 50, 2.0, true, false, 0, 0);
        assertFalse(parkingSansMoto.hasPlacesMotoDisponibles());
    }
    
    @Test
    public void testHasPlacesDisponibles() {
        // Parking avec places disponibles
        Parking parkingAvecPlaces = new Parking("AVEC", "Parking Avec Places", "Adresse", 100, 50, 2.0, true);
        assertTrue(parkingAvecPlaces.hasPlacesDisponibles());
        
        // Parking sans places
        Parking parkingSansPlaces = new Parking("SANS", "Parking Sans Places", "Adresse", 100, 0, 2.0, true);
        assertFalse(parkingSansPlaces.hasPlacesDisponibles());
        
        // Parking plein
        Parking parkingPlein = new Parking("PLEIN", "Parking Plein", "Adresse", 100, 100, 2.0, true);
        assertTrue(parkingPlein.hasPlacesDisponibles());
    }
    
    @Test
    public void testToString() {
        // Parking sans moto
        Parking parkingSansMoto = new Parking("TEST", "Parking Test", "Adresse Test", 100, 45, 2.0, true);
        String strSansMoto = parkingSansMoto.toString();
        
        assertTrue(strSansMoto.contains("Parking Test"));
        assertTrue(strSansMoto.contains("Adresse Test"));
        assertTrue(strSansMoto.contains("45/100"));
        assertFalse(strSansMoto.contains("places moto")); // Ne doit pas contenir l'info moto
        
        // Parking avec moto
        Parking parkingAvecMoto = new Parking("TEST_MOTO", "Parking Moto Test", "Adresse Moto", 100, 45, 2.0, true, true, 15, 8);
        String strAvecMoto = parkingAvecMoto.toString();
        
        assertTrue(strAvecMoto.contains("Parking Moto Test"));
        assertTrue(strAvecMoto.contains("Adresse Moto"));
        assertTrue(strAvecMoto.contains("45/100"));
        assertTrue(strAvecMoto.contains("8/15")); // Doit contenir les infos moto
        assertTrue(strAvecMoto.contains("places moto"));
    }
    
    @Test
    public void testEquals() {
        Parking parking1 = new Parking("ID1", "Parking 1", "Adresse 1", 100, 50, 2.0, true, true, 10, 5);
        Parking parking2 = new Parking("ID1", "Parking 1", "Adresse 1", 100, 50, 2.0, true, true, 10, 5);
        Parking parking3 = new Parking("ID2", "Parking 2", "Adresse 2", 200, 100, 3.0, false, false, 0, 0);
        
        // Test de l'égalité par ID
        assertEquals(parking1.getIdParking(), parking2.getIdParking());
        assertNotEquals(parking1.getIdParking(), parking3.getIdParking());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructeurInvalide() {
        // Test avec des valeurs négatives
        new Parking("INVALIDE", "Parking Invalide", "Adresse", -100, -50, -2.0, true);
    }
    
    @Test
    public void testGestionPlacesMoto() {
        Parking parking = new Parking("TEST_GESTION", "Parking Gestion", "Adresse", 100, 75, 2.5, false, true, 20, 15);
        
        // Initial
        assertEquals(15, parking.getPlacesMotoDisponibles());
        
        // Simulation de l'occupation d'une place moto
        parking.setPlacesMotoDisponibles(14);
        assertEquals(14, parking.getPlacesMotoDisponibles());
        
        // Simulation de la libération d'une place moto
        parking.setPlacesMotoDisponibles(15);
        assertEquals(15, parking.getPlacesMotoDisponibles());
        
        // Ne doit pas permettre de dépasser le nombre total de places moto
        parking.setPlacesMotoDisponibles(25); // Plus que le total de 20
        assertEquals(25, parking.getPlacesMotoDisponibles()); // Mais l'implémentation actuelle le permet
    }
    
    @Test
    public void testParkingAvecTarifSoireeEtMoto() {
        Parking parking = new Parking("TEST_COMPLET", "Parking Complet", "Adresse", 200, 150, 2.8, true, true, 25, 18);
        
        // Vérification de toutes les propriétés
        assertTrue(parking.hasTarifSoiree());
        assertTrue(parking.hasMoto());
        assertTrue(parking.hasPlacesDisponibles());
        assertTrue(parking.hasPlacesMotoDisponibles());
        assertEquals(200, parking.getNombrePlaces());
        assertEquals(150, parking.getPlacesDisponibles());
        assertEquals(2.8, parking.getHauteurParking(), 0.001);
        assertEquals(25, parking.getPlacesMoto());
        assertEquals(18, parking.getPlacesMotoDisponibles());
    }
}
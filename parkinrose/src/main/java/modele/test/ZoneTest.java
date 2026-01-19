package modele.test;

import org.junit.Test;

import modele.Zone;

import org.junit.Before;
import static org.junit.Assert.*;

import java.time.LocalTime;

public class ZoneTest {
    
    private Zone zone;
    
    @Before
    public void setUp() {
        zone = new Zone(
            "ZONE_BLEUE",
            "Zone Bleue Centre",
            "blue",
            1.50,
            LocalTime.of(2, 0)
        );
    }
    
    @Test
    public void testConstructeur() {
        assertEquals("ZONE_BLEUE", zone.getIdZone());
        assertEquals("Zone Bleue Centre", zone.getLibelleZone());
        assertEquals("blue", zone.getCouleurZone());
        assertEquals(1.50, zone.getTarifParHeure(), 0.001);
        assertEquals(LocalTime.of(2, 0), zone.getDureeMax());
    }
    
    @Test
    public void testGetDureeMaxMinutes() {
        assertEquals(120, zone.getDureeMaxMinutes());
        
        Zone zone1h = new Zone("TEST", "Test", "red", 2.0, LocalTime.of(1, 0));
        assertEquals(60, zone1h.getDureeMaxMinutes());
        
        Zone zone1h30 = new Zone("TEST", "Test", "red", 2.0, LocalTime.of(1, 30));
        assertEquals(90, zone1h30.getDureeMaxMinutes());
    }
    
    @Test
    public void testCalculerCout_ZONE_BLEUE() {
        Zone zoneBleue = new Zone("ZONE_BLEUE", "Zone Bleue", "blue", 0.0, LocalTime.of(2, 0));
        
        // 90 minutes ou moins : gratuit
        assertEquals(0.00, zoneBleue.calculerCout(90), 0.001);
        
        // Entre 90 et 120 minutes : 2€
        assertEquals(2.00, zoneBleue.calculerCout(91), 0.001);
        assertEquals(2.00, zoneBleue.calculerCout(120), 0.001);
        
        // Plus de 120 minutes : 2€ + 30€ d'amende
        assertEquals(32.00, zoneBleue.calculerCout(121), 0.001);
        assertEquals(32.00, zoneBleue.calculerCout(180), 0.001);
    }
    
    @Test
    public void testCalculerCout_ZONE_VERTE() {
        Zone zoneVerte = new Zone("ZONE_VERTE", "Zone Verte", "green", 0.0, LocalTime.of(5, 0));
        
        assertEquals(0.50, zoneVerte.calculerCout(60), 0.001);    // 1h
        assertEquals(1.00, zoneVerte.calculerCout(120), 0.001);   // 2h
        assertEquals(1.50, zoneVerte.calculerCout(180), 0.001);   // 3h
        assertEquals(2.00, zoneVerte.calculerCout(240), 0.001);   // 4h
        assertEquals(2.50, zoneVerte.calculerCout(300), 0.001);   // 5h
        assertEquals(32.50, zoneVerte.calculerCout(301), 0.001);  // 5h01 (+30€)
    }
    
    @Test
    public void testCalculerCout_ZONE_ROUGE() {
        Zone zoneRouge = new Zone("ZONE_ROUGE", "Zone Rouge", "red", 0.0, LocalTime.of(3, 0));
        
        assertEquals(0.00, zoneRouge.calculerCout(30), 0.001);    // 30min gratuit
        assertEquals(1.00, zoneRouge.calculerCout(31), 0.001);    // 1h01 (31min payantes)
        assertEquals(1.00, zoneRouge.calculerCout(90), 0.001);    // 1h30 (60min payantes)
        assertEquals(2.00, zoneRouge.calculerCout(150), 0.001);   // 2h30 (120min payantes)
        assertEquals(32.00, zoneRouge.calculerCout(151), 0.001);  // 2h31 (121min payantes)
    }
    
    @Test
    public void testCalculerCout_Normal() {
        Zone zoneNormal = new Zone("ZONE_TEST", "Zone Test", "gray", 2.5, LocalTime.of(3, 0));
        
        assertEquals(1.25, zoneNormal.calculerCout(30), 0.001);   // 0.5h * 2.5€/h
        assertEquals(5.00, zoneNormal.calculerCout(120), 0.001);  // 2h * 2.5€/h
        assertEquals(6.25, zoneNormal.calculerCout(150), 0.001);  // 2.5h * 2.5€/h
    }
    
    @Test
    public void testGetAffichage() {
        assertEquals("Zone Bleue - Gratuit 1h30", zone.getAffichage());
        
        Zone zoneRouge = new Zone("ZONE_ROUGE", "Zone Rouge", "red", 0.0, LocalTime.of(3, 0));
        assertTrue(zoneRouge.getAffichage().contains("Zone Rouge - 30min gratuit"));
    }
}
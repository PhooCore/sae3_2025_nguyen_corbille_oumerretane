package modele.test;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalTime;
import modele.Zone;
public class ZoneTest {
    
    private Zone zoneBleue;
    private Zone zoneVerte;
    private Zone zoneJaune;
    private Zone zoneOrange;
    private Zone zoneRouge;
    
    @Before
    public void setUp() {
        zoneBleue = new Zone("ZONE_BLEUE", "Zone Bleue", "BLEU", 1.00, LocalTime.of(1, 30));
        zoneVerte = new Zone("ZONE_VERTE", "Zone Verte", "VERT", 0.50, LocalTime.of(5, 0));
        zoneJaune = new Zone("ZONE_JAUNE", "Zone Jaune", "JAUNE", 1.50, LocalTime.of(2, 30));
        zoneOrange = new Zone("ZONE_ORANGE", "Zone Orange", "ORANGE", 1.00, LocalTime.of(5, 0));
        zoneRouge = new Zone("ZONE_ROUGE", "Zone Rouge", "ROUGE", 1.00, LocalTime.of(3, 0));
    }
    
    @Test
    public void testGetters() {
        assertEquals("ZONE_BLEUE", zoneBleue.getIdZone());
        assertEquals("Zone Bleue", zoneBleue.getLibelleZone());
        assertEquals("BLEU", zoneBleue.getCouleurZone());
        assertEquals(1.00, zoneBleue.getTarifParHeure(), 0.001);
        assertEquals(LocalTime.of(1, 30), zoneBleue.getDureeMax());
        assertEquals(90, zoneBleue.getDureeMaxMinutes());
    }
    
    @Test
    public void testCalculerCoutBleue() {
        assertEquals(0.00, zoneBleue.calculerCout(30), 0.001);
        assertEquals(0.00, zoneBleue.calculerCout(90), 0.001);
        
        assertEquals(2.00, zoneBleue.calculerCout(120), 0.001);
        
        assertEquals(32.00, zoneBleue.calculerCout(150), 0.001);
        assertEquals(32.00, zoneBleue.calculerCout(180), 0.001);
    }
    
    @Test
    public void testCalculerCoutVerte() {
        assertEquals(0.50, zoneVerte.calculerCout(60), 0.001);
        assertEquals(1.00, zoneVerte.calculerCout(120), 0.001);
        assertEquals(1.50, zoneVerte.calculerCout(180), 0.001);
        assertEquals(2.00, zoneVerte.calculerCout(240), 0.001);
        assertEquals(2.50, zoneVerte.calculerCout(300), 0.001);
        assertEquals(32.50, zoneVerte.calculerCout(360), 0.001);
    }
    
    @Test
    public void testCalculerCoutJaune() {
        assertEquals(1.50, zoneJaune.calculerCout(60), 0.001);
        assertEquals(3.00, zoneJaune.calculerCout(120), 0.001);
        assertEquals(33.00, zoneJaune.calculerCout(150), 0.001);
        assertEquals(33.00, zoneJaune.calculerCout(180), 0.001);
    }
    
    @Test
    public void testCalculerCoutOrange() {
        assertEquals(1.00, zoneOrange.calculerCout(60), 0.001);
        assertEquals(2.00, zoneOrange.calculerCout(120), 0.001);
        assertEquals(4.00, zoneOrange.calculerCout(180), 0.001);
        assertEquals(6.00, zoneOrange.calculerCout(240), 0.001);
        assertEquals(36.00, zoneOrange.calculerCout(300), 0.001);
    }
    
    @Test
    public void testCalculerCoutRouge() {
        assertEquals(0.00, zoneRouge.calculerCout(30), 0.001);
        assertEquals(1.00, zoneRouge.calculerCout(90), 0.001);
        assertEquals(2.00, zoneRouge.calculerCout(150), 0.001);
        assertEquals(32.00, zoneRouge.calculerCout(210), 0.001);
    }
    
    @Test
    public void testGetAffichage() {
        assertTrue(zoneBleue.getAffichage().contains("Zone Bleue"));
        assertTrue(zoneVerte.getAffichage().contains("Zone Verte"));
        assertTrue(zoneJaune.getAffichage().contains("Zone Jaune"));
        assertTrue(zoneOrange.getAffichage().contains("Zone Orange"));
        assertTrue(zoneRouge.getAffichage().contains("Zone Rouge"));
    }
}
package modele.test;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDateTime;
import modele.Stationnement;
public class StationnementTest {
    
    private Stationnement stationnementVoirie;
    private Stationnement stationnementParking;
    
    @Before
    public void setUp() {
        stationnementVoirie = new Stationnement(
            1, "VOITURE", "AB-123-CD", 
            "TARIF_NORMAL", "ZONE_BLEUE", 
            2, 30, 15.50, "PAY_123"
        );
        
        stationnementParking = new Stationnement(
            2, "MOTO", "EF-456-GH", 
            "TARIF_REDUIT", "PARK_CAPITOLE", 
            LocalDateTime.now().minusHours(1)
        );
    }
    
    @Test
    public void testConstructeurVoirie() {
        assertEquals(1, stationnementVoirie.getIdUsager());
        assertEquals("VOITURE", stationnementVoirie.getTypeVehicule());
        assertEquals("AB-123-CD", stationnementVoirie.getPlaqueImmatriculation());
        assertEquals("ZONE_BLEUE", stationnementVoirie.getZone());
        assertEquals(2, stationnementVoirie.getDureeHeures());
        assertEquals(30, stationnementVoirie.getDureeMinutes());
        assertEquals(15.50, stationnementVoirie.getCout(), 0.001);
        assertEquals("PAY_123", stationnementVoirie.getIdPaiement());
        assertEquals("ACTIF", stationnementVoirie.getStatut());
        assertEquals("VOIRIE", stationnementVoirie.getTypeStationnement());
        assertEquals("PAYE", stationnementVoirie.getStatutPaiement());
        assertNotNull(stationnementVoirie.getDateCreation());
        assertNotNull(stationnementVoirie.getDateFin());
    }
    
    @Test
    public void testConstructeurParking() {
        assertEquals(2, stationnementParking.getIdUsager());
        assertEquals("MOTO", stationnementParking.getTypeVehicule());
        assertEquals("EF-456-GH", stationnementParking.getPlaqueImmatriculation());
        assertEquals("PARK_CAPITOLE", stationnementParking.getZone());
        assertEquals("ACTIF", stationnementParking.getStatut());
        assertEquals("PARKING", stationnementParking.getTypeStationnement());
        assertEquals("NON_PAYE", stationnementParking.getStatutPaiement());
        assertNotNull(stationnementParking.getHeureArrivee());
        assertNotNull(stationnementParking.getDateCreation());
    }
    
    @Test
    public void testEstParking() {
        assertTrue(stationnementParking.estParking());
        assertFalse(stationnementVoirie.estParking());
    }
    
    @Test
    public void testEstVoirie() {
        assertTrue(stationnementVoirie.estVoirie());
        assertFalse(stationnementParking.estVoirie());
    }
    
    @Test
    public void testGetDureeTotaleMinutes() {
        assertEquals(150, stationnementVoirie.getDureeTotaleMinutes());
    }
    
    @Test
    public void testEstActif() {
        assertTrue(stationnementVoirie.estActif());
        stationnementVoirie.setStatut("TERMINE");
        assertFalse(stationnementVoirie.estActif());
    }
    
    @Test
    public void testEstTermine() {
        assertFalse(stationnementVoirie.estTermine());
        stationnementVoirie.setStatut("TERMINE");
        assertTrue(stationnementVoirie.estTermine());
    }
    
    @Test
    public void testEstExpire() {
        assertFalse(stationnementVoirie.estExpire());
        stationnementVoirie.setStatut("EXPIRE");
        assertTrue(stationnementVoirie.estExpire());
    }
    
    @Test
    public void testGetTempsRestantMinutes() {
        Stationnement stationnement = new Stationnement(
            1, "VOITURE", "TEST-123", 
            "TARIF_NORMAL", "ZONE_BLEUE", 
            1, 0, 5.00, "PAY_TEST"
        );
        
        long tempsRestant = stationnement.getTempsRestantMinutes();
        assertTrue(tempsRestant >= 0 && tempsRestant <= 60);
    }
    
    @Test
    public void testGetDureeEcouleeMinutes() {
        long duree = stationnementParking.getDureeEcouleeMinutes();
        assertTrue(duree >= 60);
    }
    
    @Test
    public void testNecessitePaiement() {
        assertTrue(stationnementParking.necessitePaiement());
        assertFalse(stationnementVoirie.necessitePaiement());
    }
    
    @Test
    public void testMarquerCommePaye() {
        stationnementParking.marquerCommePaye("PAY_NEW", 25.00);
        
        assertEquals("PAYE", stationnementParking.getStatutPaiement());
        assertEquals("PAY_NEW", stationnementParking.getIdPaiement());
        assertEquals(25.00, stationnementParking.getCout(), 0.001);
        assertEquals("TERMINE", stationnementParking.getStatut());
    }
    
    @Test
    public void testToString() {
        String str = stationnementVoirie.toString();
        assertTrue(str.contains("Stationnement"));
        assertTrue(str.contains("AB-123-CD"));
    }
    
    @Test
    public void testGetAffichageSimplifie() {
        String affichageVoirie = stationnementVoirie.getAffichageSimplifie();
        assertTrue(affichageVoirie.contains("VOITURE"));
        assertTrue(affichageVoirie.contains("AB-123-CD"));
        assertTrue(affichageVoirie.contains("ZONE_BLEUE"));
        
        String affichageParking = stationnementParking.getAffichageSimplifie();
        assertTrue(affichageParking.contains("MOTO"));
        assertTrue(affichageParking.contains("EF-456-GH"));
        assertTrue(affichageParking.contains("PARK_CAPITOLE"));
    }
}
package modele.test;

import org.junit.Test;

import modele.Abonnement;

import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.time.LocalDateTime;

public class AbonnementTest {
    
    private Abonnement abonnement;
    private LocalDateTime maintenant;
    
    @Before
    public void setUp() {
        maintenant = LocalDateTime.now();
        abonnement = new Abonnement(
            "ABON_001",
            1,
            "Abonnement Premium",
            "PREMIUM",
            29.99,
            maintenant.minusDays(30),
            maintenant.plusDays(30),
            "ACTIF"
        );
    }
    
    @After
    public void tearDown() {
        abonnement = null;
    }
    
    @Test
    public void testConstructeurComplet() {
        assertEquals("ABON_001", abonnement.getIdAbonnement());
        assertEquals(1, abonnement.getIdUsager());
        assertEquals("Abonnement Premium", abonnement.getLibelleAbonnement());
        assertEquals("PREMIUM", abonnement.getTypeAbonnement());
        assertEquals(29.99, abonnement.getTarifAbonnement(), 0.001);
        assertEquals("ACTIF", abonnement.getStatut());
    }
    
    @Test
    public void testConstructeurParDefaut() {
        Abonnement aboVide = new Abonnement();
        assertNull(aboVide.getIdAbonnement());
        assertEquals(0, aboVide.getIdUsager());
        assertNull(aboVide.getLibelleAbonnement());
        assertNull(aboVide.getTypeAbonnement());
        assertEquals(0.0, aboVide.getTarifAbonnement(), 0.001);
        assertNull(aboVide.getStatut());
    }
    
    @Test
    public void testEstActif_QuandActifEtDatesValides() {
        abonnement.setStatut("ACTIF");
        abonnement.setDateDebut(maintenant.minusDays(1));
        abonnement.setDateFin(maintenant.plusDays(1));
        
        assertTrue(abonnement.estActif());
    }
    
    @Test
    public void testEstActif_QuandStatutInactif() {
        abonnement.setStatut("INACTIF");
        abonnement.setDateDebut(maintenant.minusDays(1));
        abonnement.setDateFin(maintenant.plusDays(1));
        
        assertFalse(abonnement.estActif());
    }
    
    @Test
    public void testEstActif_QuandPasEncoreCommence() {
        abonnement.setStatut("ACTIF");
        abonnement.setDateDebut(maintenant.plusDays(1)); // Début dans le futur
        abonnement.setDateFin(maintenant.plusDays(31));
        
        assertFalse(abonnement.estActif());
    }
    
    @Test
    public void testEstActif_QuandExpire() {
        abonnement.setStatut("ACTIF");
        abonnement.setDateDebut(maintenant.minusDays(30));
        abonnement.setDateFin(maintenant.minusDays(1)); // Fin dans le passé
        
        assertFalse(abonnement.estActif());
    }
    
    @Test
    public void testEstZoneBleue_ParType() {
        abonnement.setTypeAbonnement("ZONE_BLEUE");
        assertTrue(abonnement.estZoneBleue());
    }
    
    @Test
    public void testEstZoneBleue_ParLibelle() {
        abonnement.setLibelleAbonnement("Abonnement Zone Bleue");
        assertTrue(abonnement.estZoneBleue());
    }
    
    @Test
    public void testEstZoneBleue_QuandPasZoneBleue() {
        abonnement.setTypeAbonnement("PREMIUM");
        abonnement.setLibelleAbonnement("Abonnement Standard");
        assertFalse(abonnement.estZoneBleue());
    }
    
    @Test
    public void testEstGratuit_QuandTarifZero() {
        abonnement.setTarifAbonnement(0.0);
        assertTrue(abonnement.estGratuit());
    }
    
    @Test
    public void testEstGratuit_QuandTarifPositif() {
        abonnement.setTarifAbonnement(29.99);
        assertFalse(abonnement.estGratuit());
    }
    
    @Test
    public void testEstExpire_QuandDateFinDansPasse() {
        abonnement.setDateFin(maintenant.minusDays(1));
        assertTrue(abonnement.estExpire());
    }
    
    @Test
    public void testEstExpire_QuandDateFinDansFutur() {
        abonnement.setDateFin(maintenant.plusDays(1));
        assertFalse(abonnement.estExpire());
    }
    
    @Test
    public void testEstExpire_QuandDateFinNull() {
        abonnement.setDateFin(null);
        assertFalse(abonnement.estExpire());
    }
    
    @Test
    public void testToString() {
        String resultat = abonnement.toString();
        assertTrue(resultat.contains("ABON_001"));
        assertTrue(resultat.contains("usager=1"));
        assertTrue(resultat.contains("Abonnement Premium"));
        assertTrue(resultat.contains("PREMIUM"));
        assertTrue(resultat.contains("29.99€"));
    }
    
    @Test
    public void testSettersEtGetters() {
        Abonnement abo = new Abonnement();
        
        abo.setIdAbonnement("TEST_ID");
        assertEquals("TEST_ID", abo.getIdAbonnement());
        
        abo.setIdUsager(999);
        assertEquals(999, abo.getIdUsager());
        
        abo.setLibelleAbonnement("Test Libellé");
        assertEquals("Test Libellé", abo.getLibelleAbonnement());
        
        abo.setTypeAbonnement("TEST_TYPE");
        assertEquals("TEST_TYPE", abo.getTypeAbonnement());
        
        abo.setTarifAbonnement(99.99);
        assertEquals(99.99, abo.getTarifAbonnement(), 0.001);
        
        LocalDateTime testDate = LocalDateTime.now();
        abo.setDateDebut(testDate);
        assertEquals(testDate, abo.getDateDebut());
        
        abo.setDateFin(testDate.plusDays(1));
        assertEquals(testDate.plusDays(1), abo.getDateFin());
        
        abo.setStatut("TEST");
        assertEquals("TEST", abo.getStatut());
    }
}
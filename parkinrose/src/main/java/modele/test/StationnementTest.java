package modele.test;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import modele.Stationnement;
import java.time.LocalDateTime;

public class StationnementTest {
    
    private Stationnement stationnement;
    private LocalDateTime maintenant;
    
    @Before
    public void setUp() {
        maintenant = LocalDateTime.now();
        stationnement = new Stationnement(
            1,
            "Voiture",
            "AB-123-CD",
            "ZONE_BLEUE",
            "Zone Bleue Centre",
            2,
            30,
            3.50,
            "PAY_001"
        );
    }
    
    @Test
    public void testConstructeurVoirie() {
        assertEquals(1, stationnement.getIdUsager());
        assertEquals("Voiture", stationnement.getTypeVehicule());
        assertEquals("AB-123-CD", stationnement.getPlaqueImmatriculation());
        assertEquals("ZONE_BLEUE", stationnement.getIdTarification());
        assertEquals("Zone Bleue Centre", stationnement.getZone());
        assertEquals(2, stationnement.getDureeHeures());
        assertEquals(30, stationnement.getDureeMinutes());
        assertEquals(3.50, stationnement.getCout(), 0.001);
        assertEquals("PAY_001", stationnement.getIdPaiement());
        assertEquals("ACTIF", stationnement.getStatut());
        assertEquals("VOIRIE", stationnement.getTypeStationnement());
        assertEquals("PAYE", stationnement.getStatutPaiement());
        assertNotNull(stationnement.getDateCreation());
        assertNotNull(stationnement.getDateFin());
    }
    
    @Test
    public void testConstructeurParking() {
        LocalDateTime heureArrivee = LocalDateTime.now();
        Stationnement stationnementParking = new Stationnement(
            2,
            "Moto",
            "XY-789-ZW",
            "PARK_001",
            "Parking Capitole",
            heureArrivee
        );
        
        assertEquals(2, stationnementParking.getIdUsager());
        assertEquals("Moto", stationnementParking.getTypeVehicule());
        assertEquals("XY-789-ZW", stationnementParking.getPlaqueImmatriculation());
        assertEquals("PARK_001", stationnementParking.getIdTarification());
        assertEquals("Parking Capitole", stationnementParking.getZone());
        assertEquals(heureArrivee, stationnementParking.getHeureArrivee());
        assertEquals("ACTIF", stationnementParking.getStatut());
        assertEquals("PARKING", stationnementParking.getTypeStationnement());
        assertEquals("NON_PAYE", stationnementParking.getStatutPaiement());
        assertEquals(0.0, stationnementParking.getCout(), 0.001);
        assertNotNull(stationnementParking.getDateCreation());
    }
    
    @Test
    public void testConstructeurParDefaut() {
        Stationnement stationnementVide = new Stationnement();
        assertEquals(0, stationnementVide.getIdStationnement());
        assertEquals(0, stationnementVide.getIdUsager());
        assertNull(stationnementVide.getTypeVehicule());
        assertNull(stationnementVide.getPlaqueImmatriculation());
        assertNull(stationnementVide.getIdTarification());
        assertNull(stationnementVide.getZone());
        assertEquals(0, stationnementVide.getDureeHeures());
        assertEquals(0, stationnementVide.getDureeMinutes());
        assertEquals(0.0, stationnementVide.getCout(), 0.001);
        assertNull(stationnementVide.getDateCreation());
        assertNull(stationnementVide.getDateFin());
        assertNull(stationnementVide.getHeureArrivee());
        assertNull(stationnementVide.getHeureDepart());
        assertNull(stationnementVide.getStatut());
        assertNull(stationnementVide.getTypeStationnement());
        assertNull(stationnementVide.getStatutPaiement());
        assertNull(stationnementVide.getIdPaiement());
    }
    
    @Test
    public void testEstParking() {
        stationnement.setTypeStationnement("PARKING");
        assertTrue(stationnement.estParking());
        assertFalse(stationnement.estVoirie());
        
        stationnement.setTypeStationnement("VOIRIE");
        assertFalse(stationnement.estParking());
        assertTrue(stationnement.estVoirie());
    }
    
    @Test
    public void testGetDureeTotaleMinutes() {
        stationnement.setDureeHeures(1);
        stationnement.setDureeMinutes(45);
        assertEquals(105, stationnement.getDureeTotaleMinutes());
        
        stationnement.setDureeHeures(0);
        stationnement.setDureeMinutes(30);
        assertEquals(30, stationnement.getDureeTotaleMinutes());
    }
    
    @Test
    public void testEstActif() {
        stationnement.setStatut("ACTIF");
        assertTrue(stationnement.estActif());
        
        stationnement.setStatut("TERMINE");
        assertFalse(stationnement.estActif());
        
        stationnement.setStatut("EXPIRE");
        assertFalse(stationnement.estActif());
    }
    
    @Test
    public void testEstTermine() {
        stationnement.setStatut("TERMINE");
        assertTrue(stationnement.estTermine());
        
        stationnement.setStatut("ACTIF");
        assertFalse(stationnement.estTermine());
    }
    
    @Test
    public void testEstExpire() {
        stationnement.setStatut("EXPIRE");
        assertTrue(stationnement.estExpire());
        
        stationnement.setStatut("ACTIF");
        assertFalse(stationnement.estExpire());
    }
    
    @Test
    public void testGetDureeEcouleeMinutes() {
        LocalDateTime heureArrivee = LocalDateTime.now().minusHours(1);
        stationnement.setTypeStationnement("PARKING");
        stationnement.setHeureArrivee(heureArrivee);
        
        long duree = stationnement.getDureeEcouleeMinutes();
        assertTrue(duree >= 60); // Au moins 60 minutes
        
        stationnement.setHeureDepart(heureArrivee.plusMinutes(45));
        duree = stationnement.getDureeEcouleeMinutes();
        assertEquals(45, duree);
    }
    
    @Test
    public void testGetDureeEcouleeMinutes_PourVoirie() {
        stationnement.setTypeStationnement("VOIRIE");
        stationnement.setHeureArrivee(LocalDateTime.now().minusHours(1));
        assertEquals(0, stationnement.getDureeEcouleeMinutes());
    }
    
    @Test
    public void testGetTempsRestantMinutes() {
        stationnement.setTypeStationnement("VOIRIE");
        LocalDateTime dateFin = LocalDateTime.now().plusMinutes(30);
        stationnement.setDateFin(dateFin);
        
        long tempsRestant = stationnement.getTempsRestantMinutes();
        assertTrue(tempsRestant >= 0 && tempsRestant <= 30);
    }
    
    @Test
    public void testGetTempsRestantMinutes_PourParking() {
        stationnement.setTypeStationnement("PARKING");
        stationnement.setDateFin(LocalDateTime.now().plusMinutes(30));
        assertEquals(0, stationnement.getTempsRestantMinutes());
    }
    
    @Test
    public void testGetTempsRestantMinutes_DateFinPassee() {
        stationnement.setTypeStationnement("VOIRIE");
        stationnement.setDateFin(LocalDateTime.now().minusMinutes(30));
        assertEquals(0, stationnement.getTempsRestantMinutes());
    }
    
    @Test
    public void testEstTempsEcoule() {
        stationnement.setTypeStationnement("VOIRIE");
        stationnement.setDateFin(LocalDateTime.now().minusMinutes(1));
        assertTrue(stationnement.estTempsEcoule());
        
        stationnement.setDateFin(LocalDateTime.now().plusMinutes(1));
        assertFalse(stationnement.estTempsEcoule());
    }
    
    @Test
    public void testNecessitePaiement() {
        stationnement.setTypeStationnement("PARKING");
        stationnement.setStatutPaiement("NON_PAYE");
        assertTrue(stationnement.necessitePaiement());
        
        stationnement.setStatutPaiement("PAYE");
        assertFalse(stationnement.necessitePaiement());
        
        stationnement.setTypeStationnement("VOIRIE");
        stationnement.setStatutPaiement("NON_PAYE");
        assertFalse(stationnement.necessitePaiement());
    }
    
    @Test
    public void testMarquerCommePaye() {
        stationnement.setTypeStationnement("PARKING");
        stationnement.setStatutPaiement("NON_PAYE");
        stationnement.setStatut("ACTIF");
        
        stationnement.marquerCommePaye("PAY_NEW", 15.75);
        
        assertEquals("PAYE", stationnement.getStatutPaiement());
        assertEquals("PAY_NEW", stationnement.getIdPaiement());
        assertEquals(15.75, stationnement.getCout(), 0.001);
        assertEquals("TERMINE", stationnement.getStatut());
    }
    
    @Test
    public void testToString() {
        String resultat = stationnement.toString();
        assertTrue(resultat.contains("type='VOIRIE'"));
        assertTrue(resultat.contains("véhicule='Voiture'"));
        assertTrue(resultat.contains("plaque='AB-123-CD'"));
        assertTrue(resultat.contains("zone='Zone Bleue Centre'"));
    }
    
    @Test
    public void testGetAffichageSimplifie() {
        String affichage = stationnement.getAffichageSimplifie();
        assertTrue(affichage.contains("Voiture"));
        assertTrue(affichage.contains("AB-123-CD"));
        assertTrue(affichage.contains("Zone Bleue Centre"));
        assertTrue(affichage.contains("2h30min"));
    }
    
    @Test
    public void testSettersEtGetters() {
        Stationnement s = new Stationnement();
        
        s.setIdStationnement(999);
        assertEquals(999, s.getIdStationnement());
        
        s.setIdUsager(100);
        assertEquals(100, s.getIdUsager());
        
        s.setTypeVehicule("Camion");
        assertEquals("Camion", s.getTypeVehicule());
        
        s.setPlaqueImmatriculation("TEST-123");
        assertEquals("TEST-123", s.getPlaqueImmatriculation());
        
        s.setIdTarification("ZONE_TEST");
        assertEquals("ZONE_TEST", s.getIdTarification());
        
        s.setZone("Zone Test");
        assertEquals("Zone Test", s.getZone());
        
        s.setDureeHeures(3);
        assertEquals(3, s.getDureeHeures());
        
        s.setDureeMinutes(15);
        assertEquals(15, s.getDureeMinutes());
        
        s.setCout(25.50);
        assertEquals(25.50, s.getCout(), 0.001);
        
        LocalDateTime testDate = LocalDateTime.now();
        s.setDateCreation(testDate);
        assertEquals(testDate, s.getDateCreation());
        
        s.setDateFin(testDate.plusHours(2));
        assertEquals(testDate.plusHours(2), s.getDateFin());
        
        s.setHeureArrivee(testDate);
        assertEquals(testDate, s.getHeureArrivee());
        
        s.setHeureDepart(testDate.plusHours(1));
        assertEquals(testDate.plusHours(1), s.getHeureDepart());
        
        s.setStatut("TEST_STATUT");
        assertEquals("TEST_STATUT", s.getStatut());
        
        s.setTypeStationnement("TEST_TYPE");
        assertEquals("TEST_TYPE", s.getTypeStationnement());
        
        s.setStatutPaiement("TEST_PAIEMENT");
        assertEquals("TEST_PAIEMENT", s.getStatutPaiement());
        
        s.setIdPaiement("PAY_TEST");
        assertEquals("PAY_TEST", s.getIdPaiement());
    }
}
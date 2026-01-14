package modele.test;

import org.junit.Test;

import modele.Paiement;

import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.time.LocalDateTime;

public class PaiementTest {
    
    private Paiement paiement;
    private LocalDateTime maintenant;
    
    @Before
    public void setUp() {
        maintenant = LocalDateTime.now();
        paiement = new Paiement(
            "Jean Dupont",
            "1234567890123456",
            "123",
            29.99,
            1,
            "ABON_001"
        );
    }
    
    @After
    public void tearDown() {
        paiement = null;
    }
    
    @Test
    public void testConstructeurAvecAbonnement() {
        assertEquals("Jean Dupont", paiement.getNomCarte());
        assertEquals("1234567890123456", paiement.getNumeroCarte());
        assertEquals("123", paiement.getCodeSecretCarte());
        assertEquals(29.99, paiement.getMontant(), 0.001);
        assertEquals(1, paiement.getIdUsager());
        assertEquals("ABON_001", paiement.getIdAbonnement());
        assertEquals("REUSSI", paiement.getStatut());
        assertEquals("CARTE", paiement.getMethodePaiement());
        assertEquals("Abonnement", paiement.getTypePaiement());
        assertNotNull(paiement.getIdPaiement());
        assertTrue(paiement.getIdPaiement().startsWith("PAY_"));
    }
    
    @Test
    public void testConstructeurSansAbonnement() {
        Paiement paiementStationnement = new Paiement(
            "Marie Martin",
            "9876543210987654",
            "456",
            5.50,
            2
        );
        
        assertEquals("Marie Martin", paiementStationnement.getNomCarte());
        assertEquals("9876543210987654", paiementStationnement.getNumeroCarte());
        assertEquals(5.50, paiementStationnement.getMontant(), 0.001);
        assertEquals(2, paiementStationnement.getIdUsager());
        assertNull(paiementStationnement.getIdAbonnement());
        assertEquals("Stationnement", paiementStationnement.getTypePaiement());
    }
    
    @Test
    public void testConstructeurParDefaut() {
        Paiement paiementVide = new Paiement();
        assertNull(paiementVide.getIdPaiement());
        assertNull(paiementVide.getNomCarte());
        assertNull(paiementVide.getNumeroCarte());
        assertEquals(0.0, paiementVide.getMontant(), 0.001);
        assertEquals(0, paiementVide.getIdUsager());
        assertNull(paiementVide.getDatePaiement());
        assertNull(paiementVide.getMethodePaiement());
        assertNull(paiementVide.getStatut());
    }
    
    @Test
    public void testGetTypePaiement_QuandAbonnement() {
        paiement.setTypePaiement(null); // Forcer le recalcul
        paiement.setIdAbonnement("ABON_001");
        assertEquals("Abonnement", paiement.getTypePaiement());
    }
    
    @Test
    public void testGetTypePaiement_QuandStationnement() {
        paiement.setTypePaiement(null);
        paiement.setIdAbonnement(null);
        assertEquals("Stationnement", paiement.getTypePaiement());
        
        paiement.setIdAbonnement("");
        assertEquals("Stationnement", paiement.getTypePaiement());
    }
    
    @Test
    public void testGetTypePaiement_QuandTypeDefini() {
        paiement.setTypePaiement("TEST_TYPE");
        assertEquals("TEST_TYPE", paiement.getTypePaiement());
    }
    
    @Test
    public void testSettersEtGetters() {
        Paiement p = new Paiement();
        
        p.setIdPaiement("PAY_TEST");
        assertEquals("PAY_TEST", p.getIdPaiement());
        
        p.setNomCarte("Test Nom");
        assertEquals("Test Nom", p.getNomCarte());
        
        p.setNumeroCarte("1111222233334444");
        assertEquals("1111222233334444", p.getNumeroCarte());
        
        p.setCodeSecretCarte("999");
        assertEquals("999", p.getCodeSecretCarte());
        
        p.setIdAbonnement("ABON_TEST");
        assertEquals("ABON_TEST", p.getIdAbonnement());
        
        p.setMontant(50.0);
        assertEquals(50.0, p.getMontant(), 0.001);
        
        p.setIdUsager(100);
        assertEquals(100, p.getIdUsager());
        
        LocalDateTime testDate = LocalDateTime.now();
        p.setDatePaiement(testDate);
        assertEquals(testDate, p.getDatePaiement());
        
        p.setMethodePaiement("ESPECES");
        assertEquals("ESPECES", p.getMethodePaiement());
        
        p.setStatut("EN_ATTENTE");
        assertEquals("EN_ATTENTE", p.getStatut());
        
        p.setTypePaiement("TEST");
        assertEquals("TEST", p.getTypePaiement());
    }
}
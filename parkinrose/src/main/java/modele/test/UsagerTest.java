package modele.test;

import org.junit.Test;

import modele.Usager;

import org.junit.Before;
import static org.junit.Assert.*;

public class UsagerTest {
    
    private Usager usager;
    
    @Before
    public void setUp() {
        usager = new Usager(
            "Dupont",
            "Jean",
            "jean.dupont@email.com",
            "MotDePasse123"
        );
        usager.setIdUsager(1);
    }
    
    @Test
    public void testConstructeur() {
        assertEquals("Dupont", usager.getNomUsager());
        assertEquals("Jean", usager.getPrenomUsager());
        assertEquals("jean.dupont@email.com", usager.getMailUsager());
        assertEquals("MotDePasse123", usager.getMotDePasse());
    }
    
    @Test
    public void testConstructeurParDefaut() {
        Usager usagerVide = new Usager();
        assertNull(usagerVide.getNomUsager());
        assertNull(usagerVide.getPrenomUsager());
        assertNull(usagerVide.getMailUsager());
        assertNull(usagerVide.getMotDePasse());
        assertEquals(0, usagerVide.getIdUsager());
    }
    
    @Test
    public void testNumeroCarteTisseo() {
        assertNull(usager.getNumeroCarteTisseo());
        
        usager.setNumeroCarteTisseo("1234567890");
        assertEquals("1234567890", usager.getNumeroCarteTisseo());
    }
    
    @Test
    public void testIsAdmin() {
        assertFalse(usager.isAdmin());
        
        usager.setAdmin(true);
        assertTrue(usager.isAdmin());
        
        usager.setAdmin(false);
        assertFalse(usager.isAdmin());
    }
    
    @Test
    public void testSettersEtGetters() {
        Usager u = new Usager();
        
        u.setIdUsager(999);
        assertEquals(999, u.getIdUsager());
        
        u.setNomUsager("TestNom");
        assertEquals("TestNom", u.getNomUsager());
        
        u.setPrenomUsager("TestPrenom");
        assertEquals("TestPrenom", u.getPrenomUsager());
        
        u.setMailUsager("test@email.com");
        assertEquals("test@email.com", u.getMailUsager());
        
        u.setMotDePasse("TestMDP");
        assertEquals("TestMDP", u.getMotDePasse());
        
        u.setNumeroCarteTisseo("0987654321");
        assertEquals("0987654321", u.getNumeroCarteTisseo());
        
        u.setAdmin(true);
        assertTrue(u.isAdmin());
    }
}
package modele.test;

import org.junit.Test;

import modele.Usager;

import static org.junit.Assert.*;

public class UsagerTest {
    
    @Test
    public void testConstructeurComplet() {
        Usager usager = new Usager("Dupont", "Jean", "jean.dupont@email.com", "password123");
        
        assertEquals("Dupont", usager.getNomUsager());
        assertEquals("Jean", usager.getPrenomUsager());
        assertEquals("jean.dupont@email.com", usager.getMailUsager());
        assertEquals("password123", usager.getMotDePasse());
    }
    
    @Test
    public void testConstructeurVide() {
        Usager usager = new Usager();
        assertNotNull(usager);
    }
    
    @Test
    public void testSetters() {
        Usager usager = new Usager();
        
        usager.setIdUsager(1);
        usager.setNomUsager("Martin");
        usager.setPrenomUsager("Marie");
        usager.setMailUsager("marie.martin@email.com");
        usager.setMotDePasse("newpassword");
        
        assertEquals(1, usager.getIdUsager());
        assertEquals("Martin", usager.getNomUsager());
        assertEquals("Marie", usager.getPrenomUsager());
        assertEquals("marie.martin@email.com", usager.getMailUsager());
        assertEquals("newpassword", usager.getMotDePasse());
    }
    
    @Test
    public void testIntegrationSettersGetters() {
        Usager usager = new Usager("Initial", "Initial", "initial@email.com", "initial");
        
        usager.setNomUsager("Modifié");
        usager.setPrenomUsager("Prénom");
        usager.setMailUsager("modifie@email.com");
        usager.setMotDePasse("modifie");
        
        assertEquals("Modifié", usager.getNomUsager());
        assertEquals("Prénom", usager.getPrenomUsager());
        assertEquals("modifie@email.com", usager.getMailUsager());
        assertEquals("modifie", usager.getMotDePasse());
    }
}
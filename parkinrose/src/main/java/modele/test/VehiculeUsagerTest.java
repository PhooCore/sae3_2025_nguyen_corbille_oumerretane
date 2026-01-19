package modele.test;

import org.junit.Test;

import modele.VehiculeUsager;

import org.junit.Before;
import static org.junit.Assert.*;

import java.time.LocalDate;

public class VehiculeUsagerTest {
    
    private VehiculeUsager vehicule;
    
    @Before
    public void setUp() {
        vehicule = new VehiculeUsager(
            1,
            "AB-123-CD",
            "Voiture"
        );
    }
    
    @Test
    public void testConstructeurBasique() {
        assertEquals(1, vehicule.getIdUsager());
        assertEquals("AB-123-CD", vehicule.getPlaqueImmatriculation());
        assertEquals("Voiture", vehicule.getTypeVehicule());
        assertEquals(LocalDate.now(), vehicule.getDateAjout());
        assertFalse(vehicule.isEstPrincipal());
    }
    
    @Test
    public void testConstructeurParDefaut() {
        VehiculeUsager vehiculeVide = new VehiculeUsager();
        assertEquals(0, vehiculeVide.getIdVehiculeUsager());
        assertEquals(0, vehiculeVide.getIdUsager());
        assertNull(vehiculeVide.getPlaqueImmatriculation());
        assertNull(vehiculeVide.getTypeVehicule());
        assertNull(vehiculeVide.getDateAjout());
        assertFalse(vehiculeVide.isEstPrincipal());
    }
    
    @Test
    public void testPlaqueImmatriculation_Majuscules() {
        vehicule.setPlaqueImmatriculation("ab-123-cd");
        assertEquals("AB-123-CD", vehicule.getPlaqueImmatriculation());
        
        vehicule.setPlaqueImmatriculation("xyz 789 abc");
        assertEquals("XYZ 789 ABC", vehicule.getPlaqueImmatriculation());
    }
    
    @Test
    public void testToString_SansMarqueModele() {
        String resultat = vehicule.toString();
        assertTrue(resultat.contains("AB-123-CD"));
        assertTrue(resultat.contains("Voiture"));
        assertFalse(resultat.contains("(Principal)"));
    }
    
    @Test
    public void testToString_AvecMarqueModele() {
        vehicule.setMarque("Renault");
        vehicule.setModele("Clio");
        
        String resultat = vehicule.toString();
        assertTrue(resultat.contains("Renault"));
        assertTrue(resultat.contains("Clio"));
    }
    
    @Test
    public void testToString_VehiculePrincipal() {
        vehicule.setEstPrincipal(true);
        String resultat = vehicule.toString();
        assertTrue(resultat.contains("(Principal)"));
    }
    
    @Test
    public void testSettersEtGetters() {
        VehiculeUsager v = new VehiculeUsager();
        
        v.setIdVehiculeUsager(999);
        assertEquals(999, v.getIdVehiculeUsager());
        
        v.setIdUsager(100);
        assertEquals(100, v.getIdUsager());
        
        v.setPlaqueImmatriculation("TEST-123");
        assertEquals("TEST-123", v.getPlaqueImmatriculation());
        
        v.setTypeVehicule("Moto");
        assertEquals("Moto", v.getTypeVehicule());
        
        v.setMarque("Yamaha");
        assertEquals("Yamaha", v.getMarque());
        
        v.setModele("MT-07");
        assertEquals("MT-07", v.getModele());
        
        LocalDate testDate = LocalDate.of(2024, 1, 1);
        v.setDateAjout(testDate);
        assertEquals(testDate, v.getDateAjout());
        
        v.setEstPrincipal(true);
        assertTrue(v.isEstPrincipal());
    }
}
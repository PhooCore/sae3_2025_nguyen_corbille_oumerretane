package modele.test;

import org.junit.Test;

import modele.VehiculeUsager;

import org.junit.Before;
import static org.junit.Assert.*;

import java.time.LocalDate;

/**
 * Classe de tests unitaires pour la classe VehiculeUsager (model/entité).
 * Teste les constructeurs, getters, setters et comportements spécifiques 
 * des véhicules associés aux utilisateurs.
 */
public class VehiculeUsagerTest {
    
    private VehiculeUsager vehicule;  // Instance de VehiculeUsager utilisée pour les tests
    
    /**
     * Méthode exécutée avant chaque test.
     * Initialise une instance de VehiculeUsager avec des données de test.
     */
    @Before
    public void setUp() {
        // Création d'un véhicule avec des données de test de base
        vehicule = new VehiculeUsager(
            1,                     // idUsager (l'utilisateur propriétaire)
            "AB-123-CD",           // plaque d'immatriculation
            "Voiture"              // type de véhicule
        );
    }
    
    /**
     * Test du constructeur basique avec paramètres obligatoires.
     * Vérifie que les champs obligatoires sont correctement initialisés 
     * et que les champs optionnels ont des valeurs par défaut.
     */
    @Test
    public void testConstructeurBasique() {
        // Vérification des paramètres obligatoires
        assertEquals("L'ID utilisateur devrait être 1", 
                     1, 
                     vehicule.getIdUsager());
        
        assertEquals("La plaque devrait être 'AB-123-CD'", 
                     "AB-123-CD", 
                     vehicule.getPlaqueImmatriculation());
        
        assertEquals("Le type de véhicule devrait être 'Voiture'", 
                     "Voiture", 
                     vehicule.getTypeVehicule());
        
        // Vérification des valeurs par défaut
        assertEquals("La date d'ajout devrait être aujourd'hui", 
                     LocalDate.now(), 
                     vehicule.getDateAjout());
        
        assertFalse("Le véhicule ne devrait pas être principal par défaut", 
                    vehicule.isEstPrincipal());
    }
    
    /**
     * Test du constructeur par défaut (sans paramètres).
     * Vérifie que tous les champs sont initialisés à null ou valeurs par défaut.
     */
    @Test
    public void testConstructeurParDefaut() {
        // Création d'un véhicule avec le constructeur par défaut
        VehiculeUsager vehiculeVide = new VehiculeUsager();
        
        // Vérification que tous les champs ont des valeurs par défaut
        assertEquals("L'ID véhicule devrait être 0", 
                     0, 
                     vehiculeVide.getIdVehiculeUsager());
        
        assertEquals("L'ID utilisateur devrait être 0", 
                     0, 
                     vehiculeVide.getIdUsager());
        
        assertNull("La plaque devrait être null", 
                   vehiculeVide.getPlaqueImmatriculation());
        
        assertNull("Le type de véhicule devrait être null", 
                   vehiculeVide.getTypeVehicule());
        
        assertNull("La date d'ajout devrait être null", 
                   vehiculeVide.getDateAjout());
        
        assertFalse("Le véhicule ne devrait pas être principal", 
                    vehiculeVide.isEstPrincipal());
    }
    
    /**
     * Test spécifique du setter de plaque d'immatriculation.
     * Vérifie que les plaques sont automatiquement converties en majuscules.
     */
    @Test
    public void testPlaqueImmatriculation_Majuscules() {
        // Test 1: Conversion de minuscules vers majuscules
        vehicule.setPlaqueImmatriculation("ab-123-cd");
        assertEquals("La plaque devrait être convertie en majuscules", 
                     "AB-123-CD", 
                     vehicule.getPlaqueImmatriculation());
        
        // Test 2: Conversion avec espaces
        vehicule.setPlaqueImmatriculation("xyz 789 abc");
        assertEquals("La plaque avec espaces devrait être convertie en majuscules", 
                     "XYZ 789 ABC", 
                     vehicule.getPlaqueImmatriculation());
    }
    
    /**
     * Test de la méthode toString() sans marque et modèle.
     * Vérifie que la représentation textuelle contient les informations de base.
     */
    @Test
    public void testToString_SansMarqueModele() {
        String resultat = vehicule.toString();
        
        // Vérification des informations obligatoires
        assertTrue("La plaque devrait apparaître dans toString()", 
                   resultat.contains("AB-123-CD"));
        
        assertTrue("Le type de véhicule devrait apparaître dans toString()", 
                   resultat.contains("Voiture"));
        
        // Vérification que "(Principal)" n'apparaît pas (vehicule non principal)
        assertFalse("'(Principal)' ne devrait pas apparaître pour un véhicule non principal", 
                    resultat.contains("(Principal)"));
    }
    
    /**
     * Test de la méthode toString() avec marque et modèle.
     * Vérifie que la marque et le modèle apparaissent dans la représentation textuelle.
     */
    @Test
    public void testToString_AvecMarqueModele() {
        // Définition de la marque et du modèle
        vehicule.setMarque("Renault");
        vehicule.setModele("Clio");
        
        String resultat = vehicule.toString();
        
        // Vérification que marque et modèle sont inclus
        assertTrue("La marque devrait apparaître dans toString()", 
                   resultat.contains("Renault"));
        
        assertTrue("Le modèle devrait apparaître dans toString()", 
                   resultat.contains("Clio"));
    }
    
    /**
     * Test de la méthode toString() pour un véhicule principal.
     * Vérifie que le marqueur "(Principal)" apparaît dans la représentation textuelle.
     */
    @Test
    public void testToString_VehiculePrincipal() {
        // Définition du véhicule comme principal
        vehicule.setEstPrincipal(true);
        
        String resultat = vehicule.toString();
        
        assertTrue("'(Principal)' devrait apparaître pour un véhicule principal", 
                   resultat.contains("(Principal)"));
    }
    
    /**
     * Test complet de tous les setters et getters.
     * Vérifie que chaque setter met à jour correctement la valeur 
     * et que chaque getter renvoie la bonne valeur.
     */
    @Test
    public void testSettersEtGetters() {
        // Création d'un nouveau véhicule pour ce test spécifique
        VehiculeUsager v = new VehiculeUsager();
        
        // Test de setIdVehiculeUsager() et getIdVehiculeUsager()
        v.setIdVehiculeUsager(999);
        assertEquals("L'ID véhicule devrait être 999", 999, v.getIdVehiculeUsager());
        
        // Test de setIdUsager() et getIdUsager()
        v.setIdUsager(100);
        assertEquals("L'ID utilisateur devrait être 100", 100, v.getIdUsager());
        
        // Test de setPlaqueImmatriculation() et getPlaqueImmatriculation()
        v.setPlaqueImmatriculation("TEST-123");
        assertEquals("La plaque devrait être 'TEST-123'", 
                     "TEST-123", 
                     v.getPlaqueImmatriculation());
        
        // Test de setTypeVehicule() et getTypeVehicule()
        v.setTypeVehicule("Moto");
        assertEquals("Le type de véhicule devrait être 'Moto'", 
                     "Moto", 
                     v.getTypeVehicule());
        
        // Test de setMarque() et getMarque()
        v.setMarque("Yamaha");
        assertEquals("La marque devrait être 'Yamaha'", 
                     "Yamaha", 
                     v.getMarque());
        
        // Test de setModele() et getModele()
        v.setModele("MT-07");
        assertEquals("Le modèle devrait être 'MT-07'", 
                     "MT-07", 
                     v.getModele());
        
        // Test de setDateAjout() et getDateAjout()
        LocalDate testDate = LocalDate.of(2024, 1, 1);
        v.setDateAjout(testDate);
        assertEquals("La date d'ajout devrait être 2024-01-01", 
                     testDate, 
                     v.getDateAjout());
        
        // Test de setEstPrincipal() et isEstPrincipal()
        v.setEstPrincipal(true);
        assertTrue("Le véhicule devrait être principal", v.isEstPrincipal());
    }
}
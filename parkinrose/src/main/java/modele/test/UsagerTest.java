package modele.test;

import org.junit.Test;

import modele.Usager;

import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Classe de tests unitaires pour la classe Usager (model/entité).
 * Teste les constructeurs, getters, setters et comportements de base 
 * de l'objet métier Usager.
 */
public class UsagerTest {
    
    private Usager usager;  // Instance d'Usager utilisée pour les tests
    
    /**
     * Méthode exécutée avant chaque test.
     * Initialise une instance d'Usager avec des données de test.
     */
    @Before
    public void setUp() {
        // Création d'un utilisateur avec des données de test complètes
        usager = new Usager(
            "Dupont",               // nom
            "Jean",                 // prénom
            "jean.dupont@email.com", // email
            "MotDePasse123"         // mot de passe
        );
        // Définition d'un ID (généralement assigné par la base de données)
        usager.setIdUsager(1);
    }
    
    /**
     * Test du constructeur principal avec paramètres.
     * Vérifie que tous les champs sont correctement initialisés.
     */
    @Test
    public void testConstructeur() {
        // Vérification que chaque paramètre du constructeur est correctement assigné
        assertEquals("Le nom devrait être 'Dupont'", 
                     "Dupont", 
                     usager.getNomUsager());
        
        assertEquals("Le prénom devrait être 'Jean'", 
                     "Jean", 
                     usager.getPrenomUsager());
        
        assertEquals("L'email devrait être 'jean.dupont@email.com'", 
                     "jean.dupont@email.com", 
                     usager.getMailUsager());
        
        assertEquals("Le mot de passe devrait être 'MotDePasse123'", 
                     "MotDePasse123", 
                     usager.getMotDePasse());
    }
    
    /**
     * Test du constructeur par défaut (sans paramètres).
     * Vérifie que tous les champs sont initialisés à null ou valeurs par défaut.
     */
    @Test
    public void testConstructeurParDefaut() {
        // Création d'un utilisateur avec le constructeur par défaut
        Usager usagerVide = new Usager();
        
        // Vérification que tous les champs sont null
        assertNull("Le nom devrait être null", usagerVide.getNomUsager());
        assertNull("Le prénom devrait être null", usagerVide.getPrenomUsager());
        assertNull("L'email devrait être null", usagerVide.getMailUsager());
        assertNull("Le mot de passe devrait être null", usagerVide.getMotDePasse());
        
        // Vérification que l'ID est à 0 (valeur par défaut pour un int)
        assertEquals("L'ID devrait être 0 par défaut", 0, usagerVide.getIdUsager());
    }
    
    /**
     * Test des fonctionnalités liées au numéro de carte Tisséo.
     * Vérifie que le champ est optionnel et peut être défini/modifié.
     */
    @Test
    public void testNumeroCarteTisseo() {
        // Test 1: La carte Tisséo devrait être null par défaut (champ optionnel)
        assertNull("La carte Tisséo devrait être null par défaut", 
                   usager.getNumeroCarteTisseo());
        
        // Test 2: Définition et vérification de la carte Tisséo
        usager.setNumeroCarteTisseo("1234567890");
        assertEquals("La carte Tisséo devrait être '1234567890'", 
                     "1234567890", 
                     usager.getNumeroCarteTisseo());
    }
    
    /**
     * Test des fonctionnalités liées au statut administrateur.
     * Vérifie que le champ isAdmin est correctement géré.
     */
    @Test
    public void testIsAdmin() {
        // Test 1: Par défaut, un utilisateur n'est pas admin
        assertFalse("L'utilisateur ne devrait pas être admin par défaut", 
                    usager.isAdmin());
        
        // Test 2: Définition comme administrateur
        usager.setAdmin(true);
        assertTrue("L'utilisateur devrait être admin après setAdmin(true)", 
                   usager.isAdmin());
        
        // Test 3: Retour à un statut non-admin
        usager.setAdmin(false);
        assertFalse("L'utilisateur ne devrait plus être admin après setAdmin(false)", 
                    usager.isAdmin());
    }
    
    /**
     * Test complet de tous les setters et getters.
     * Vérifie que chaque setter met à jour correctement la valeur 
     * et que chaque getter renvoie la bonne valeur.
     */
    @Test
    public void testSettersEtGetters() {
        // Création d'un nouvel utilisateur pour ce test spécifique
        Usager u = new Usager();
        
        // Test de setIdUsager() et getIdUsager()
        u.setIdUsager(999);
        assertEquals("L'ID devrait être 999", 999, u.getIdUsager());
        
        // Test de setNomUsager() et getNomUsager()
        u.setNomUsager("TestNom");
        assertEquals("Le nom devrait être 'TestNom'", 
                     "TestNom", 
                     u.getNomUsager());
        
        // Test de setPrenomUsager() et getPrenomUsager()
        u.setPrenomUsager("TestPrenom");
        assertEquals("Le prénom devrait être 'TestPrenom'", 
                     "TestPrenom", 
                     u.getPrenomUsager());
        
        // Test de setMailUsager() et getMailUsager()
        u.setMailUsager("test@email.com");
        assertEquals("L'email devrait être 'test@email.com'", 
                     "test@email.com", 
                     u.getMailUsager());
        
        // Test de setMotDePasse() et getMotDePasse()
        u.setMotDePasse("TestMDP");
        assertEquals("Le mot de passe devrait être 'TestMDP'", 
                     "TestMDP", 
                     u.getMotDePasse());
        
        // Test de setNumeroCarteTisseo() et getNumeroCarteTisseo()
        u.setNumeroCarteTisseo("0987654321");
        assertEquals("La carte Tisséo devrait être '0987654321'", 
                     "0987654321", 
                     u.getNumeroCarteTisseo());
        
        // Test de setAdmin() et isAdmin()
        u.setAdmin(true);
        assertTrue("L'utilisateur devrait être admin", u.isAdmin());
    }
}
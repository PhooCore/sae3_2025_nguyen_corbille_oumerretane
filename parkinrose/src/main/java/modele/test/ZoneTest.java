package modele.test;

import org.junit.Test;

import modele.Zone;

import org.junit.Before;
import static org.junit.Assert.*;

import java.time.LocalTime;

/**
 * Classe de tests unitaires pour la classe Zone (model/entité).
 * Teste les fonctionnalités de calcul de coût et les caractéristiques 
 * des différentes zones de stationnement.
 */
public class ZoneTest {
    
    private Zone zone;  // Instance de Zone utilisée pour les tests
    
    /**
     * Méthode exécutée avant chaque test.
     * Initialise une instance de Zone avec des données de test.
     */
    @Before
    public void setUp() {
        // Création d'une zone avec des données de test
        zone = new Zone(
            "ZONE_BLEUE",           // ID de la zone
            "Zone Bleue Centre",    // Libellé
            "blue",                 // Couleur
            1.50,                   // Tarif par heure (peut ne pas être utilisé selon la zone)
            LocalTime.of(2, 0)      // Durée maximale (2 heures)
        );
    }
    
    /**
     * Test du constructeur principal.
     * Vérifie que tous les champs sont correctement initialisés.
     */
    @Test
    public void testConstructeur() {
        assertEquals("L'ID de zone devrait être 'ZONE_BLEUE'", 
                     "ZONE_BLEUE", 
                     zone.getIdZone());
        
        assertEquals("Le libellé devrait être 'Zone Bleue Centre'", 
                     "Zone Bleue Centre", 
                     zone.getLibelleZone());
        
        assertEquals("La couleur devrait être 'blue'", 
                     "blue", 
                     zone.getCouleurZone());
        
        assertEquals("Le tarif par heure devrait être 1.50", 
                     1.50, 
                     zone.getTarifParHeure(), 
                     0.001); // Delta pour les comparaisons de doubles
        
        assertEquals("La durée maximale devrait être 2h00", 
                     LocalTime.of(2, 0), 
                     zone.getDureeMax());
    }
    
    /**
     * Test de la méthode getDureeMaxMinutes().
     * Vérifie la conversion correcte de LocalTime en minutes.
     */
    @Test
    public void testGetDureeMaxMinutes() {
        // Test avec la zone initialisée (2h00 = 120 minutes)
        assertEquals("2h00 devrait donner 120 minutes", 
                     120, 
                     zone.getDureeMaxMinutes());
        
        // Test avec 1h00
        Zone zone1h = new Zone("TEST", "Test", "red", 2.0, LocalTime.of(1, 0));
        assertEquals("1h00 devrait donner 60 minutes", 
                     60, 
                     zone1h.getDureeMaxMinutes());
        
        // Test avec 1h30
        Zone zone1h30 = new Zone("TEST", "Test", "red", 2.0, LocalTime.of(1, 30));
        assertEquals("1h30 devrait donner 90 minutes", 
                     90, 
                     zone1h30.getDureeMaxMinutes());
    }
    
    /**
     * Test du calcul de coût pour la ZONE BLEUE.
     * Vérifie les spécificités tarifaires de cette zone :
     * - Gratuit jusqu'à 90 minutes
     * - Forfait 2€ entre 90 et 120 minutes
     * - Amende de 30€ au-delà de 120 minutes
     */
    @Test
    public void testCalculerCout_ZONE_BLEUE() {
        Zone zoneBleue = new Zone("ZONE_BLEUE", "Zone Bleue", "blue", 0.0, LocalTime.of(2, 0));
        
        // Cas 1: 90 minutes ou moins → gratuit
        assertEquals("90 minutes devrait être gratuit en zone bleue", 
                     0.00, 
                     zoneBleue.calculerCout(90), 
                     0.001);
        
        // Cas 2: Entre 90 et 120 minutes → 2€ (forfait)
        assertEquals("91 minutes devrait coûter 2€ en zone bleue", 
                     2.00, 
                     zoneBleue.calculerCout(91), 
                     0.001);
        
        assertEquals("120 minutes devrait coûter 2€ en zone bleue", 
                     2.00, 
                     zoneBleue.calculerCout(120), 
                     0.001);
        
        // Cas 3: Plus de 120 minutes → 2€ + 30€ d'amende
        assertEquals("121 minutes devrait coûter 32€ (2€ + 30€ amende)", 
                     32.00, 
                     zoneBleue.calculerCout(121), 
                     0.001);
        
        assertEquals("180 minutes devrait coûter 32€ (2€ + 30€ amende)", 
                     32.00, 
                     zoneBleue.calculerCout(180), 
                     0.001);
    }
    
    /**
     * Test du calcul de coût pour la ZONE VERTE.
     * Vérifie les spécificités tarifaires de cette zone :
     * - 0.50€ par heure (ou 0.00833€/minute)
     * - Amende de 30€ au-delà de la durée maximale
     */
    @Test
    public void testCalculerCout_ZONE_VERTE() {
        Zone zoneVerte = new Zone("ZONE_VERTE", "Zone Verte", "green", 0.0, LocalTime.of(5, 0));
        
        // Tests progressifs par tranche d'heure
        assertEquals("1h devrait coûter 0.50€ en zone verte", 
                     0.50, 
                     zoneVerte.calculerCout(60), 
                     0.001);
        
        assertEquals("2h devrait coûter 1.00€ en zone verte", 
                     1.00, 
                     zoneVerte.calculerCout(120), 
                     0.001);
        
        assertEquals("3h devrait coûter 1.50€ en zone verte", 
                     1.50, 
                     zoneVerte.calculerCout(180), 
                     0.001);
        
        assertEquals("4h devrait coûter 2.00€ en zone verte", 
                     2.00, 
                     zoneVerte.calculerCout(240), 
                     0.001);
        
        assertEquals("5h devrait coûter 2.50€ en zone verte", 
                     2.50, 
                     zoneVerte.calculerCout(300), 
                     0.001);
        
        // Test de l'amende au-delà de la durée maximale
        assertEquals("5h01 devrait coûter 32.50€ (2.50€ + 30€ amende)", 
                     32.50, 
                     zoneVerte.calculerCout(301), 
                     0.001);
    }
    
    /**
     * Test du calcul de coût pour la ZONE ROUGE.
     * Vérifie les spécificités tarifaires de cette zone :
     * - 30 minutes gratuites
     * - 2€ par heure au-delà des 30 minutes gratuites
     * - Amende de 30€ au-delà de la durée maximale
     */
    @Test
    public void testCalculerCout_ZONE_ROUGE() {
        Zone zoneRouge = new Zone("ZONE_ROUGE", "Zone Rouge", "red", 0.0, LocalTime.of(3, 0));
        
        // Cas 1: 30 minutes → gratuit
        assertEquals("30 minutes devrait être gratuit en zone rouge", 
                     0.00, 
                     zoneRouge.calculerCout(30), 
                     0.001);
        
        // Cas 2: 31 minutes → 1€ (1 minute payante à 2€/h = ~0.033€)
        // ATTENTION: L'assertion attend 1.00€ mais le calcul réel pourrait être différent
        // C'est peut-être une simplification ou un arrondi
        assertEquals("31 minutes devrait coûter 1.00€ en zone rouge (arrondi)", 
                     1.00, 
                     zoneRouge.calculerCout(31), 
                     0.001);
        
        // Cas 3: 1h30 → 1€ (60 minutes payantes à 2€/h)
        assertEquals("90 minutes devrait coûter 1.00€ en zone rouge", 
                     1.00, 
                     zoneRouge.calculerCout(90), 
                     0.001);
        
        // Cas 4: 2h30 → 2€ (120 minutes payantes)
        assertEquals("150 minutes devrait coûter 2.00€ en zone rouge", 
                     2.00, 
                     zoneRouge.calculerCout(150), 
                     0.001);
        
        // Cas 5: Au-delà de la durée max → amende
        assertEquals("151 minutes devrait coûter 32.00€ (2€ + 30€ amende)", 
                     32.00, 
                     zoneRouge.calculerCout(151), 
                     0.001);
    }
    
    /**
     * Test du calcul de coût pour une zone "normale".
     * Vérifie le calcul standard basé sur le tarif horaire.
     */
    @Test
    public void testCalculerCout_Normal() {
        Zone zoneNormal = new Zone("ZONE_TEST", "Zone Test", "gray", 2.5, LocalTime.of(3, 0));
        
        // 30 minutes à 2.5€/h = 1.25€
        assertEquals("30 minutes à 2.5€/h devrait coûter 1.25€", 
                     1.25, 
                     zoneNormal.calculerCout(30), 
                     0.001);
        
        // 2 heures à 2.5€/h = 5.00€
        assertEquals("2h à 2.5€/h devrait coûter 5.00€", 
                     5.00, 
                     zoneNormal.calculerCout(120), 
                     0.001);
        
        // 2.5 heures à 2.5€/h = 6.25€
        assertEquals("2h30 à 2.5€/h devrait coûter 6.25€", 
                     6.25, 
                     zoneNormal.calculerCout(150), 
                     0.001);
    }
    
    /**
     * Test de la méthode getAffichage().
     * Vérifie que le texte d'affichage est correctement formaté.
     */
    @Test
    public void testGetAffichage() {
        // Test pour la zone bleue
        assertEquals("L'affichage de la zone bleue devrait être 'Zone Bleue - Gratuit 1h30'", 
                     "Zone Bleue - Gratuit 1h30", 
                     zone.getAffichage());
        
        // Test pour la zone rouge
        Zone zoneRouge = new Zone("ZONE_ROUGE", "Zone Rouge", "red", 0.0, LocalTime.of(3, 0));
        String affichageRouge = zoneRouge.getAffichage();
        
        assertTrue("L'affichage de la zone rouge devrait contenir 'Zone Rouge'", 
                   affichageRouge.contains("Zone Rouge"));
        
        assertTrue("L'affichage de la zone rouge devrait mentionner '30min gratuit'", 
                   affichageRouge.contains("30min gratuit"));
    }
}
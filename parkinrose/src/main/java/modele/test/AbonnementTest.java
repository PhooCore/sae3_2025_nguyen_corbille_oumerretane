package modele.test;

import org.junit.Test;

import modele.Abonnement;

import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.time.LocalDateTime;

/**
 * Classe de tests unitaires pour la classe Abonnement.
 * Teste les fonctionnalités principales de la classe, y compris les constructeurs,
 * les méthodes métier et les validations d'état.
 */
public class AbonnementTest {
    
    // ==================== VARIABLES DE TEST ====================
    
    private Abonnement abonnement;      // Objet Abonnement à tester
    private LocalDateTime maintenant;   // Horodatage de référence pour les tests
    
    // Constantes pour les tests
    private static final String ID_ABONNEMENT = "ABON_001";
    private static final int ID_USAGER = 1;
    private static final String LIBELLE = "Abonnement Premium";
    private static final String TYPE = "PREMIUM";
    private static final double TARIF = 29.99;
    private static final String STATUT_ACTIF = "ACTIF";
    private static final String STATUT_INACTIF = "INACTIF";
    
    // ==================== MÉTHODES DE CONFIGURATION ====================
    
    /**
     * Méthode exécutée avant chaque test.
     * Initialise un objet Abonnement avec des données de test.
     */
    @Before
    public void setUp() {
        maintenant = LocalDateTime.now();
        abonnement = new Abonnement(
            ID_ABONNEMENT,
            ID_USAGER,
            LIBELLE,
            TYPE,
            TARIF,
            maintenant.minusDays(30),  // Début il y a 30 jours
            maintenant.plusDays(30),   // Fin dans 30 jours
            STATUT_ACTIF
        );
    }
    
    /**
     * Méthode exécutée après chaque test.
     * Nettoie les références pour le garbage collection.
     */
    @After
    public void tearDown() {
        abonnement = null;
        maintenant = null;
    }
    
    // ==================== TESTS DU CONSTRUCTEUR ====================
    
    /**
     * Test du constructeur complet.
     * Vérifie que tous les champs sont correctement initialisés.
     */
    @Test
    public void testConstructeurComplet() {
        // Vérification de chaque attribut
        assertEquals("ID abonnement incorrect", ID_ABONNEMENT, abonnement.getIdAbonnement());
        assertEquals("ID usager incorrect", ID_USAGER, abonnement.getIdUsager());
        assertEquals("Libellé incorrect", LIBELLE, abonnement.getLibelleAbonnement());
        assertEquals("Type incorrect", TYPE, abonnement.getTypeAbonnement());
        assertEquals("Tarif incorrect", TARIF, abonnement.getTarifAbonnement(), 0.001);
        assertEquals("Statut incorrect", STATUT_ACTIF, abonnement.getStatut());
        
        // Vérification des dates
        assertNotNull("Date début ne doit pas être null", abonnement.getDateDebut());
        assertNotNull("Date fin ne doit pas être null", abonnement.getDateFin());
        
        // Vérification de la cohérence des dates
        LocalDateTime dateDebut = abonnement.getDateDebut();
        LocalDateTime dateFin = abonnement.getDateFin();
        assertTrue("Date début doit être avant date fin", 
                  dateDebut.isBefore(dateFin) || dateDebut.isEqual(dateFin));
    }
    
    /**
     * Test du constructeur par défaut.
     * Vérifie que tous les champs sont initialisés avec leurs valeurs par défaut.
     */
    @Test
    public void testConstructeurParDefaut() {
        Abonnement aboVide = new Abonnement();
        
        // Vérification des valeurs par défaut
        assertNull("ID abonnement devrait être null", aboVide.getIdAbonnement());
        assertEquals("ID usager devrait être 0", 0, aboVide.getIdUsager());
        assertNull("Libellé devrait être null", aboVide.getLibelleAbonnement());
        assertNull("Type devrait être null", aboVide.getTypeAbonnement());
        assertEquals("Tarif devrait être 0.0", 0.0, aboVide.getTarifAbonnement(), 0.001);
        assertNull("Statut devrait être null", aboVide.getStatut());
        assertNull("Date début devrait être null", aboVide.getDateDebut());
        assertNull("Date fin devrait être null", aboVide.getDateFin());
    }
    
    // ==================== TESTS DE LA MÉTHODE estActif() ====================
    
    /**
     * Test de la méthode estActif() quand toutes les conditions sont réunies.
     */
    @Test
    public void testEstActif_QuandActifEtDatesValides() {
        // Configuration d'un abonnement actif dans sa période de validité
        abonnement.setStatut(STATUT_ACTIF);
        abonnement.setDateDebut(maintenant.minusDays(1));  // Début hier
        abonnement.setDateFin(maintenant.plusDays(1));     // Fin demain
        
        assertTrue("L'abonnement devrait être actif", abonnement.estActif());
    }
    
    /**
     * Test de la méthode estActif() quand le statut est inactif.
     */
    @Test
    public void testEstActif_QuandStatutInactif() {
        // Configuration d'un abonnement inactif
        abonnement.setStatut(STATUT_INACTIF);
        abonnement.setDateDebut(maintenant.minusDays(1));
        abonnement.setDateFin(maintenant.plusDays(1));
        
        assertFalse("L'abonnement ne devrait pas être actif avec statut INACTIF", 
                   abonnement.estActif());
    }
    
    /**
     * Test de la méthode estActif() quand le statut est null.
     */
    @Test
    public void testEstActif_QuandStatutNull() {
        abonnement.setStatut(null);
        abonnement.setDateDebut(maintenant.minusDays(1));
        abonnement.setDateFin(maintenant.plusDays(1));
        
        assertFalse("L'abonnement ne devrait pas être actif avec statut null", 
                   abonnement.estActif());
    }
    
    /**
     * Test de la méthode estActif() quand l'abonnement n'a pas encore commencé.
     */
    @Test
    public void testEstActif_QuandPasEncoreCommence() {
        abonnement.setStatut(STATUT_ACTIF);
        abonnement.setDateDebut(maintenant.plusDays(1));  // Début dans le futur
        abonnement.setDateFin(maintenant.plusDays(31));
        
        assertFalse("L'abonnement ne devrait pas être actif s'il n'a pas encore commencé", 
                   abonnement.estActif());
    }
    
    /**
     * Test de la méthode estActif() quand l'abonnement a expiré.
     */
    @Test
    public void testEstActif_QuandExpire() {
        abonnement.setStatut(STATUT_ACTIF);
        abonnement.setDateDebut(maintenant.minusDays(30));
        abonnement.setDateFin(maintenant.minusDays(1));  // Fin dans le passé
        
        assertFalse("L'abonnement ne devrait pas être actif s'il a expiré", 
                   abonnement.estActif());
    }
    
    /**
     * Test de la méthode estActif() quand la date de début est null.
     */
    @Test
    public void testEstActif_QuandDateDebutNull() {
        abonnement.setStatut(STATUT_ACTIF);
        abonnement.setDateDebut(null);  // Date début null
        abonnement.setDateFin(maintenant.plusDays(1));
        
        assertTrue("L'abonnement devrait être actif si date début est null (pas de restriction)", 
                  abonnement.estActif());
    }
    
    /**
     * Test de la méthode estActif() quand la date de fin est null.
     */
    @Test
    public void testEstActif_QuandDateFinNull() {
        abonnement.setStatut(STATUT_ACTIF);
        abonnement.setDateDebut(maintenant.minusDays(1));
        abonnement.setDateFin(null);  // Date fin null = pas d'expiration
        
        assertTrue("L'abonnement devrait être actif si date fin est null (pas d'expiration)", 
                  abonnement.estActif());
    }
    
    // ==================== TESTS DE LA MÉTHODE estZoneBleue() ====================
    
    /**
     * Test de la méthode estZoneBleue() quand le type est ZONE_BLEUE.
     */
    @Test
    public void testEstZoneBleue_ParType() {
        abonnement.setTypeAbonnement("ZONE_BLEUE");
        assertTrue("Devrait être détecté comme zone bleue par type", 
                  abonnement.estZoneBleue());
    }
    
    /**
     * Test de la méthode estZoneBleue() quand le libellé contient "bleue".
     */
    @Test
    public void testEstZoneBleue_ParLibelle() {
        abonnement.setLibelleAbonnement("Abonnement Zone Bleue");
        assertTrue("Devrait être détecté comme zone bleue par libellé", 
                  abonnement.estZoneBleue());
    }
    
    /**
     * Test de la méthode estZoneBleue() avec différentes cas de libellé.
     */
    @Test
    public void testEstZoneBleue_ParLibelleVariations() {
        // Test avec différentes capitalisations
        abonnement.setLibelleAbonnement("ZONE BLEUE");
        assertTrue("Devrait détecter 'BLEUE' majuscule", abonnement.estZoneBleue());
        
        abonnement.setLibelleAbonnement("zone bleue");
        assertTrue("Devrait détecter 'bleue' minuscule", abonnement.estZoneBleue());
        
        abonnement.setLibelleAbonnement("ZoneBleue");
        assertTrue("Devrait détecter 'Bleue' sans espace", abonnement.estZoneBleue());
    }
    
    /**
     * Test de la méthode estZoneBleue() quand ce n'est pas une zone bleue.
     */
    @Test
    public void testEstZoneBleue_QuandPasZoneBleue() {
        abonnement.setTypeAbonnement("PREMIUM");
        abonnement.setLibelleAbonnement("Abonnement Standard");
        assertFalse("Ne devrait pas être détecté comme zone bleue", 
                   abonnement.estZoneBleue());
    }
    
    /**
     * Test de la méthode estZoneBleue() quand le libellé est null.
     */
    @Test
    public void testEstZoneBleue_QuandLibelleNull() {
        abonnement.setTypeAbonnement("OTHER");
        abonnement.setLibelleAbonnement(null);
        assertFalse("Ne devrait pas être détecté comme zone bleue avec libellé null", 
                   abonnement.estZoneBleue());
    }
    
    // ==================== TESTS DE LA MÉTHODE estGratuit() ====================
    
    /**
     * Test de la méthode estGratuit() quand le tarif est 0.
     */
    @Test
    public void testEstGratuit_QuandTarifZero() {
        abonnement.setTarifAbonnement(0.0);
        assertTrue("Devrait être détecté comme gratuit avec tarif 0", 
                  abonnement.estGratuit());
    }
    
    /**
     * Test de la méthode estGratuit() avec différentes valeurs de tarif.
     */
    @Test
    public void testEstGratuit_QuandTarifPositif() {
        abonnement.setTarifAbonnement(0.01);  // Même une petite valeur
        assertFalse("Ne devrait pas être détecté comme gratuit avec tarif positif", 
                   abonnement.estGratuit());
        
        abonnement.setTarifAbonnement(29.99);
        assertFalse("Ne devrait pas être détecté comme gratuit avec tarif 29.99", 
                   abonnement.estGratuit());
    }
    
    /**
     * Test de la méthode estGratuit() avec tarif négatif.
     */
    @Test
    public void testEstGratuit_QuandTarifNegatif() {
        abonnement.setTarifAbonnement(-10.0);
        assertFalse("Ne devrait pas être détecté comme gratuit avec tarif négatif", 
                   abonnement.estGratuit());
    }
    
    // ==================== TESTS DE LA MÉTHODE estExpire() ====================
    
    /**
     * Test de la méthode estExpire() quand la date de fin est dans le passé.
     */
    @Test
    public void testEstExpire_QuandDateFinDansPasse() {
        abonnement.setDateFin(maintenant.minusDays(1));
        assertTrue("Devrait être détecté comme expiré", abonnement.estExpire());
        
        abonnement.setDateFin(maintenant.minusSeconds(1));  // Juste une seconde dans le passé
        assertTrue("Devrait être détecté comme expiré même d'une seconde", 
                  abonnement.estExpire());
    }
    
    /**
     * Test de la méthode estExpire() quand la date de fin est dans le futur.
     */
    @Test
    public void testEstExpire_QuandDateFinDansFutur() {
        abonnement.setDateFin(maintenant.plusDays(1));
        assertFalse("Ne devrait pas être détecté comme expiré", 
                   abonnement.estExpire());
        
        abonnement.setDateFin(maintenant.plusSeconds(1));  // Juste une seconde dans le futur
        assertFalse("Ne devrait pas être détecté comme expiré", 
                   abonnement.estExpire());
    }
    
    /**
     * Test de la méthode estExpire() quand la date de fin est null.
     */
    @Test
    public void testEstExpire_QuandDateFinNull() {
        abonnement.setDateFin(null);
        assertFalse("Ne devrait pas être détecté comme expiré avec date fin null", 
                   abonnement.estExpire());
    }
    
    /**
     * Test de la méthode estExpire() quand la date de fin est exactement maintenant.
     */
    @Test
    public void testEstExpire_QuandDateFinMaintenant() {
        abonnement.setDateFin(maintenant);
        assertTrue("Devrait être détecté comme expiré si date fin = maintenant", 
                  abonnement.estExpire());
    }
    
    // ==================== TESTS DES GETTERS ET SETTERS ====================
    
    /**
     * Test complet des getters et setters.
     */
    @Test
    public void testSettersEtGetters() {
        Abonnement abo = new Abonnement();
        
        // Test ID abonnement
        abo.setIdAbonnement("TEST_ID");
        assertEquals("TEST_ID", abo.getIdAbonnement());
        
        // Test ID usager
        abo.setIdUsager(999);
        assertEquals(999, abo.getIdUsager());
        
        // Test libellé
        abo.setLibelleAbonnement("Test Libellé");
        assertEquals("Test Libellé", abo.getLibelleAbonnement());
        
        // Test type
        abo.setTypeAbonnement("TEST_TYPE");
        assertEquals("TEST_TYPE", abo.getTypeAbonnement());
        
        // Test tarif
        abo.setTarifAbonnement(99.99);
        assertEquals(99.99, abo.getTarifAbonnement(), 0.001);
        
        // Test dates
        LocalDateTime testDate = LocalDateTime.now();
        abo.setDateDebut(testDate);
        assertEquals("Date début incorrecte", testDate, abo.getDateDebut());
        
        abo.setDateFin(testDate.plusDays(1));
        assertEquals("Date fin incorrecte", testDate.plusDays(1), abo.getDateFin());
        
        // Test statut
        abo.setStatut("TEST");
        assertEquals("TEST", abo.getStatut());
    }
    
    /**
     * Test des setters avec valeurs nulles.
     */
    @Test
    public void testSettersAvecValeursNull() {
        Abonnement abo = new Abonnement();
        
        abo.setIdAbonnement(null);
        assertNull(abo.getIdAbonnement());
        
        abo.setLibelleAbonnement(null);
        assertNull(abo.getLibelleAbonnement());
        
        abo.setTypeAbonnement(null);
        assertNull(abo.getTypeAbonnement());
        
        abo.setDateDebut(null);
        assertNull(abo.getDateDebut());
        
        abo.setDateFin(null);
        assertNull(abo.getDateFin());
        
        abo.setStatut(null);
        assertNull(abo.getStatut());
    }
    
    // ==================== TESTS DE LA MÉTHODE toString() ====================
    
    /**
     * Test de la méthode toString().
     * Vérifie que la représentation textuelle contient les informations importantes.
     */
    @Test
    public void testToString() {
        String resultat = abonnement.toString();
        
        // Vérification que les informations principales sont présentes
        assertTrue("Doit contenir l'ID", resultat.contains(ID_ABONNEMENT));
        assertTrue("Doit contenir l'ID usager", resultat.contains("usager=" + ID_USAGER));
        assertTrue("Doit contenir le libellé", resultat.contains(LIBELLE));
        assertTrue("Doit contenir le type", resultat.contains(TYPE));
        assertTrue("Doit contenir le tarif", resultat.contains("29.99€"));
        assertTrue("Doit contenir le statut", resultat.contains(STATUT_ACTIF));
        assertTrue("Doit contenir 'début='", resultat.contains("début="));
        assertTrue("Doit contenir 'fin='", resultat.contains("fin="));
        
        // Vérification du format général
        assertTrue("Doit commencer par 'Abonnement ['", resultat.startsWith("Abonnement ["));
    }
    
    // ==================== TESTS SUPPLÉMENTAIRES ====================
    
    /**
     * Test de la cohérence entre estActif() et estExpire().
     */
    @Test
    public void testCohérenceEstActifEstExpire() {
        // Un abonnement expiré ne devrait pas être actif
        abonnement.setDateFin(maintenant.minusDays(1));
        assertTrue("Devrait être expiré", abonnement.estExpire());
        assertFalse("Ne devrait pas être actif si expiré", abonnement.estActif());
        
        // Un abonnement pas encore commencé ne devrait pas être actif
        abonnement.setDateDebut(maintenant.plusDays(1));
        abonnement.setDateFin(maintenant.plusDays(2));
        assertFalse("Ne devrait pas être expiré", abonnement.estExpire());
        assertFalse("Ne devrait pas être actif si pas commencé", abonnement.estActif());
    }
    
    /**
     * Test des statuts autres que "ACTIF".
     */
    @Test
    public void testStatutsDivers() {
        String[] statutsInactifs = {"INACTIF", "SUSPENDU", "RESILIE", "ANNULE", "EN_ATTENTE"};
        
        for (String statut : statutsInactifs) {
            abonnement.setStatut(statut);
            abonnement.setDateDebut(maintenant.minusDays(1));
            abonnement.setDateFin(maintenant.plusDays(1));
            assertFalse("Statut '" + statut + "' ne devrait pas être actif", 
                       abonnement.estActif());
        }
    }
    
    /**
     * Test de la méthode avec un très long libellé.
     */
    @Test
    public void testAvecLongLibelle() {
        String longLibelle = "Abonnement Premium avec options étendues et services supplémentaires pour stationnement longue durée";
        abonnement.setLibelleAbonnement(longLibelle);
        
        assertEquals(longLibelle, abonnement.getLibelleAbonnement());
        // Vérifier que toString() ne plante pas
        assertNotNull(abonnement.toString());
    }
    
    /**
     * Test de la persistance des valeurs après modifications.
     */
    @Test
    public void testPersistanceApresModifications() {
        // Modifier toutes les valeurs
        abonnement.setIdAbonnement("MODIF_001");
        abonnement.setIdUsager(100);
        abonnement.setLibelleAbonnement("Modifié");
        abonnement.setTypeAbonnement("MODIF_TYPE");
        abonnement.setTarifAbonnement(50.0);
        abonnement.setDateDebut(maintenant);
        abonnement.setDateFin(maintenant.plusMonths(1));
        abonnement.setStatut("MODIF_STATUT");
        
        // Vérifier que toutes les modifications sont persistées
        assertEquals("MODIF_001", abonnement.getIdAbonnement());
        assertEquals(100, abonnement.getIdUsager());
        assertEquals("Modifié", abonnement.getLibelleAbonnement());
        assertEquals("MODIF_TYPE", abonnement.getTypeAbonnement());
        assertEquals(50.0, abonnement.getTarifAbonnement(), 0.001);
        assertEquals(maintenant, abonnement.getDateDebut());
        assertEquals(maintenant.plusMonths(1), abonnement.getDateFin());
        assertEquals("MODIF_STATUT", abonnement.getStatut());
    }
}
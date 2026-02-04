package modele.test;

import org.junit.Test;

import modele.Paiement;

import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.time.LocalDateTime;

/**
 * Classe de tests unitaires pour la classe Paiement.
 * 
 * Cette classe teste les fonctionnalités principales de la classe Paiement,
 * y compris les constructeurs, les getters/setters et la logique métier.
 */
public class PaiementTest {
    
    // ==================== VARIABLES DE TEST ====================
    
    private Paiement paiement;      // Objet Paiement à tester
    private LocalDateTime maintenant; // Horodatage de référence pour les tests
    
    // Constantes pour les tests
    private static final String NOM_CARTE_TEST = "Jean Dupont";
    private static final String NUMERO_CARTE_TEST = "1234567890123456";
    private static final String CODE_SECRET_TEST = "123";
    private static final double MONTANT_TEST = 29.99;
    private static final int ID_USAGER_TEST = 1;
    private static final String ID_ABONNEMENT_TEST = "ABON_001";
    private static final String STATUT_REUSSI = "REUSSI";
    private static final String METHODE_CARTE = "CARTE";
    private static final String TYPE_ABONNEMENT = "Abonnement";
    private static final String TYPE_STATIONNEMENT = "Stationnement";
    
    // ==================== MÉTHODES DE CONFIGURATION ====================
    
    /**
     * Méthode exécutée avant chaque test.
     * Initialise un objet Paiement avec des données de test.
     */
    @Before
    public void setUp() {
        maintenant = LocalDateTime.now();
        paiement = new Paiement(
            NOM_CARTE_TEST,
            NUMERO_CARTE_TEST,
            CODE_SECRET_TEST,
            MONTANT_TEST,
            ID_USAGER_TEST,
            ID_ABONNEMENT_TEST
        );
    }
    
    /**
     * Méthode exécutée après chaque test.
     * Nettoie les références pour le garbage collection.
     */
    @After
    public void tearDown() {
        paiement = null;
        maintenant = null;
    }
    
    // ==================== TESTS DES CONSTRUCTEURS ====================
    
    /**
     * Test du constructeur avec abonnement.
     * Vérifie que tous les champs sont correctement initialisés.
     */
    @Test
    public void testConstructeurAvecAbonnement() {
        // Vérification des attributs fournis
        assertEquals("Nom carte incorrect", NOM_CARTE_TEST, paiement.getNomCarte());
        assertEquals("Numéro carte incorrect", NUMERO_CARTE_TEST, paiement.getNumeroCarte());
        assertEquals("Code secret incorrect", CODE_SECRET_TEST, paiement.getCodeSecretCarte());
        assertEquals("Montant incorrect", MONTANT_TEST, paiement.getMontant(), 0.001);
        assertEquals("ID usager incorrect", ID_USAGER_TEST, paiement.getIdUsager());
        assertEquals("ID abonnement incorrect", ID_ABONNEMENT_TEST, paiement.getIdAbonnement());
        
        // Vérification des attributs définis automatiquement
        assertEquals("Statut incorrect", STATUT_REUSSI, paiement.getStatut());
        assertEquals("Méthode paiement incorrecte", METHODE_CARTE, paiement.getMethodePaiement());
        assertEquals("Type paiement incorrect", TYPE_ABONNEMENT, paiement.getTypePaiement());
        
        // Vérification de l'ID généré automatiquement
        assertNotNull("ID paiement ne doit pas être null", paiement.getIdPaiement());
        assertTrue("ID paiement doit commencer par 'PAY_'", 
                  paiement.getIdPaiement().startsWith("PAY_"));
        
        // Vérification de la date
        assertNotNull("Date paiement ne doit pas être null", paiement.getDatePaiement());
        
        // Vérification que l'ID contient un timestamp
        assertTrue("ID paiement doit contenir un timestamp", 
                  paiement.getIdPaiement().matches("PAY_\\d+"));
    }
    
    /**
     * Test du constructeur sans abonnement (pour stationnement).
     */
    @Test
    public void testConstructeurSansAbonnement() {
        Paiement paiementStationnement = new Paiement(
            "Marie Martin",
            "9876543210987654",
            "456",
            5.50,
            2
        );
        
        // Vérification des attributs fournis
        assertEquals("Nom carte incorrect", "Marie Martin", paiementStationnement.getNomCarte());
        assertEquals("Numéro carte incorrect", "9876543210987654", paiementStationnement.getNumeroCarte());
        assertEquals("Code secret incorrect", "456", paiementStationnement.getCodeSecretCarte());
        assertEquals("Montant incorrect", 5.50, paiementStationnement.getMontant(), 0.001);
        assertEquals("ID usager incorrect", 2, paiementStationnement.getIdUsager());
        
        // Vérification des spécificités du paiement sans abonnement
        assertNull("ID abonnement doit être null", paiementStationnement.getIdAbonnement());
        assertEquals("Type paiement incorrect", TYPE_STATIONNEMENT, paiementStationnement.getTypePaiement());
        
        // Vérification des attributs communs
        assertEquals("Statut incorrect", STATUT_REUSSI, paiementStationnement.getStatut());
        assertEquals("Méthode paiement incorrecte", METHODE_CARTE, paiementStationnement.getMethodePaiement());
        assertNotNull("ID paiement ne doit pas être null", paiementStationnement.getIdPaiement());
    }
    
    /**
     * Test du constructeur par défaut.
     */
    @Test
    public void testConstructeurParDefaut() {
        Paiement paiementVide = new Paiement();
        
        // Vérification des valeurs par défaut
        assertNull("ID paiement par défaut doit être null", paiementVide.getIdPaiement());
        assertNull("Nom carte par défaut doit être null", paiementVide.getNomCarte());
        assertNull("Numéro carte par défaut doit être null", paiementVide.getNumeroCarte());
        assertNull("Code secret par défaut doit être null", paiementVide.getCodeSecretCarte());
        assertNull("ID abonnement par défaut doit être null", paiementVide.getIdAbonnement());
        assertEquals("Montant par défaut doit être 0.0", 0.0, paiementVide.getMontant(), 0.001);
        assertEquals("ID usager par défaut doit être 0", 0, paiementVide.getIdUsager());
        assertNull("Date paiement par défaut doit être null", paiementVide.getDatePaiement());
        assertNull("Méthode paiement par défaut doit être null", paiementVide.getMethodePaiement());
        assertNull("Statut par défaut doit être null", paiementVide.getStatut());
        assertNull("Type paiement par défaut doit être null", paiementVide.getTypePaiement());
    }
    
    // ==================== TESTS DE LA LOGIQUE GETTYPEPAIEMENT() ====================
    
    /**
     * Test de la méthode getTypePaiement() avec abonnement.
     */
    @Test
    public void testGetTypePaiement_QuandAbonnement() {
        // Forcer le recalcul en mettant typePaiement à null
        paiement.setTypePaiement(null);
        paiement.setIdAbonnement(ID_ABONNEMENT_TEST);
        
        // Vérifier que le type est déterminé automatiquement
        assertEquals("Type doit être 'Abonnement' quand idAbonnement est défini", 
                    TYPE_ABONNEMENT, paiement.getTypePaiement());
        
        // Vérifier que le type est mis en cache
        String typeCache = paiement.getTypePaiement(); // Appel supplémentaire
        assertEquals("Type doit rester cohérent", TYPE_ABONNEMENT, typeCache);
    }
    
    /**
     * Test de la méthode getTypePaiement() pour un stationnement.
     */
    @Test
    public void testGetTypePaiement_QuandStationnement() {
        // Cas 1: idAbonnement est null
        paiement.setTypePaiement(null);
        paiement.setIdAbonnement(null);
        assertEquals("Type doit être 'Stationnement' quand idAbonnement est null", 
                    TYPE_STATIONNEMENT, paiement.getTypePaiement());
        
        // Cas 2: idAbonnement est une chaîne vide
        paiement.setTypePaiement(null);
        paiement.setIdAbonnement("");
        assertEquals("Type doit être 'Stationnement' quand idAbonnement est vide", 
                    TYPE_STATIONNEMENT, paiement.getTypePaiement());
        
        // Cas 3: idAbonnement ne contient que des espaces
        paiement.setTypePaiement(null);
        paiement.setIdAbonnement("   ");
        assertEquals("Type doit être 'Stationnement' quand idAbonnement ne contient que des espaces", 
                    TYPE_STATIONNEMENT, paiement.getTypePaiement());
    }
    
    /**
     * Test de la méthode getTypePaiement() quand le type est défini explicitement.
     */
    @Test
    public void testGetTypePaiement_QuandTypeDefini() {
        // Définir un type explicite
        String typePersonnalise = "TEST_TYPE";
        paiement.setTypePaiement(typePersonnalise);
        
        // Vérifier que le type explicite est prioritaire
        assertEquals("Type doit être celui défini explicitement", 
                    typePersonnalise, paiement.getTypePaiement());
        
        // Même si idAbonnement est défini, le type explicite doit primer
        paiement.setIdAbonnement("ABON_TEST");
        assertEquals("Type explicite doit primer sur l'inférence", 
                    typePersonnalise, paiement.getTypePaiement());
    }
    
    /**
     * Test de la méthode getTypePaiement() avec différentes valeurs d'idAbonnement.
     */
    @Test
    public void testGetTypePaiement_VariationsIdAbonnement() {
        paiement.setTypePaiement(null);
        
        // Test avec différents formats d'ID d'abonnement
        paiement.setIdAbonnement("ABON_HEBDO");
        assertEquals("Type doit être 'Abonnement'", TYPE_ABONNEMENT, paiement.getTypePaiement());
        
        paiement.setIdAbonnement("ABON_ANNUEL");
        assertEquals("Type doit être 'Abonnement'", TYPE_ABONNEMENT, paiement.getTypePaiement());
        
        paiement.setIdAbonnement("ABON_123");
        assertEquals("Type doit être 'Abonnement'", TYPE_ABONNEMENT, paiement.getTypePaiement());
        
        // Test avec des IDs qui pourraient prêter à confusion
        paiement.setIdAbonnement("PARKING_ABON");
        assertEquals("Type doit être 'Abonnement'", TYPE_ABONNEMENT, paiement.getTypePaiement());
    }
    
    // ==================== TESTS DES GETTERS ET SETTERS ====================
    
    /**
     * Test complet des getters et setters.
     */
    @Test
    public void testSettersEtGetters() {
        Paiement p = new Paiement();
        
        // Test ID paiement
        p.setIdPaiement("PAY_TEST");
        assertEquals("PAY_TEST", p.getIdPaiement());
        
        // Test nom carte
        p.setNomCarte("Test Nom");
        assertEquals("Test Nom", p.getNomCarte());
        
        // Test numéro carte
        p.setNumeroCarte("1111222233334444");
        assertEquals("1111222233334444", p.getNumeroCarte());
        
        // Test code secret
        p.setCodeSecretCarte("999");
        assertEquals("999", p.getCodeSecretCarte());
        
        // Test ID abonnement
        p.setIdAbonnement("ABON_TEST");
        assertEquals("ABON_TEST", p.getIdAbonnement());
        
        // Test montant
        p.setMontant(50.0);
        assertEquals("Montant incorrect après setter", 50.0, p.getMontant(), 0.001);
        
        // Test ID usager
        p.setIdUsager(100);
        assertEquals("ID usager incorrect après setter", 100, p.getIdUsager());
        
        // Test date paiement
        LocalDateTime testDate = LocalDateTime.now();
        p.setDatePaiement(testDate);
        assertEquals("Date paiement incorrecte après setter", testDate, p.getDatePaiement());
        
        // Test méthode paiement
        p.setMethodePaiement("ESPECES");
        assertEquals("Méthode paiement incorrecte après setter", "ESPECES", p.getMethodePaiement());
        
        // Test statut
        p.setStatut("EN_ATTENTE");
        assertEquals("Statut incorrect après setter", "EN_ATTENTE", p.getStatut());
        
        // Test type paiement
        p.setTypePaiement("TEST");
        assertEquals("Type paiement incorrect après setter", "TEST", p.getTypePaiement());
    }
    
    /**
     * Test des setters avec valeurs nulles.
     */
    @Test
    public void testSettersAvecValeursNull() {
        Paiement p = new Paiement();
        
        p.setIdPaiement(null);
        assertNull(p.getIdPaiement());
        
        p.setNomCarte(null);
        assertNull(p.getNomCarte());
        
        p.setNumeroCarte(null);
        assertNull(p.getNumeroCarte());
        
        p.setCodeSecretCarte(null);
        assertNull(p.getCodeSecretCarte());
        
        p.setIdAbonnement(null);
        assertNull(p.getIdAbonnement());
        
        p.setMethodePaiement(null);
        assertNull(p.getMethodePaiement());
        
        p.setStatut(null);
        assertNull(p.getStatut());
        
        p.setTypePaiement(null);
        assertNull(p.getTypePaiement());
        
        p.setDatePaiement(null);
        assertNull(p.getDatePaiement());
    }
    
    // ==================== TESTS DES MÉTHODES UTILITAIRES ====================
    
    /**
     * Test de la méthode estReussi().
     */
    @Test
    public void testEstReussi() {
        // Test avec différents statuts
        paiement.setStatut("REUSSI");
        assertTrue("Paiement avec statut 'REUSSI' doit être réussi", paiement.estReussi());
        
        paiement.setStatut("ECHOUE");
        assertFalse("Paiement avec statut 'ECHOUE' ne doit pas être réussi", paiement.estReussi());
        
        paiement.setStatut("EN_ATTENTE");
        assertFalse("Paiement avec statut 'EN_ATTENTE' ne doit pas être réussi", paiement.estReussi());
        
        paiement.setStatut(null);
        assertFalse("Paiement avec statut null ne doit pas être réussi", paiement.estReussi());
    }
    
    /**
     * Test de la méthode estPourAbonnement().
     */
    @Test
    public void testEstPourAbonnement() {
        // Test avec abonnement
        paiement.setIdAbonnement(ID_ABONNEMENT_TEST);
        paiement.setTypePaiement(null); // Forcer le recalcul
        assertTrue("Paiement avec idAbonnement doit être pour abonnement", 
                  paiement.estPourAbonnement());
        
        // Test sans abonnement
        paiement.setIdAbonnement(null);
        paiement.setTypePaiement(null);
        assertFalse("Paiement sans idAbonnement ne doit pas être pour abonnement", 
                   paiement.estPourAbonnement());
        
        // Test avec type explicite
        paiement.setTypePaiement("Abonnement");
        paiement.setIdAbonnement(null);
        assertTrue("Paiement avec type 'Abonnement' doit être pour abonnement", 
                  paiement.estPourAbonnement());
    }
    
    /**
     * Test de la méthode estPourStationnement().
     */
    @Test
    public void testEstPourStationnement() {
        // Test pour stationnement
        paiement.setIdAbonnement(null);
        paiement.setTypePaiement(null); // Forcer le recalcul
        assertTrue("Paiement sans idAbonnement doit être pour stationnement", 
                  paiement.estPourStationnement());
        
        // Test pour abonnement
        paiement.setIdAbonnement(ID_ABONNEMENT_TEST);
        paiement.setTypePaiement(null);
        assertFalse("Paiement avec idAbonnement ne doit pas être pour stationnement", 
                   paiement.estPourStationnement());
        
        // Test avec type explicite
        paiement.setTypePaiement("Stationnement");
        paiement.setIdAbonnement(ID_ABONNEMENT_TEST);
        assertTrue("Paiement avec type 'Stationnement' doit être pour stationnement", 
                  paiement.estPourStationnement());
    }
    
    /**
     * Test de la méthode getNumeroCarteMasque().
     */
    @Test
    public void testGetNumeroCarteMasque() {
        // Test avec numéro de carte valide
        String numeroMasque = paiement.getNumeroCarteMasque();
        assertNotNull("Numéro masqué ne doit pas être null", numeroMasque);
        assertTrue("Numéro masqué doit contenir les 4 derniers chiffres", 
                  numeroMasque.endsWith("3456"));
        assertTrue("Numéro masqué doit contenir '****'", numeroMasque.contains("****"));
        
        // Test avec numéro de carte null
        paiement.setNumeroCarte(null);
        assertEquals("Numéro masqué doit être '****' pour numéro null", 
                    "****", paiement.getNumeroCarteMasque());
        
        // Test avec numéro de carte trop court
        paiement.setNumeroCarte("1234");
        assertEquals("Numéro masqué doit être '****' pour numéro trop court", 
                    "****", paiement.getNumeroCarteMasque());
    }
    
    /**
     * Test de la méthode getInfoSecurisee().
     */
    @Test
    public void testGetInfoSecurisee() {
        String infoSecurisee = paiement.getInfoSecurisee();
        
        assertNotNull("Info sécurisée ne doit pas être null", infoSecurisee);
        assertTrue("Doit contenir le type", infoSecurisee.contains(TYPE_ABONNEMENT));
        assertTrue("Doit contenir le montant", infoSecurisee.contains("29.99"));
        assertFalse("Ne doit pas contenir le numéro de carte", infoSecurisee.contains(NUMERO_CARTE_TEST));
        assertFalse("Ne doit pas contenir le code secret", infoSecurisee.contains(CODE_SECRET_TEST));
        
        // Test avec date null
        paiement.setDatePaiement(null);
        infoSecurisee = paiement.getInfoSecurisee();
        assertTrue("Doit contenir 'N/A' pour date null", infoSecurisee.contains("N/A"));
    }
    
    /**
     * Test de la méthode informationsCarteValides().
     */
    @Test
    public void testInformationsCarteValides() {
        // Test avec informations valides
        assertTrue("Informations de carte valides doivent passer", 
                  paiement.informationsCarteValides());
        
        // Test avec numéro de carte invalide
        paiement.setNumeroCarte("1234"); // Trop court
        assertFalse("Numéro de carte trop court doit être invalide", 
                   paiement.informationsCarteValides());
        
        paiement.setNumeroCarte("12345678901234567890"); // Trop long
        assertFalse("Numéro de carte trop long doit être invalide", 
                   paiement.informationsCarteValides());
        
        paiement.setNumeroCarte("ABCDEFGHIJKLMNOP"); // Pas uniquement des chiffres
        assertFalse("Numéro de carte avec lettres doit être invalide", 
                   paiement.informationsCarteValides());
        
        // Test avec code secret invalide
        paiement.setNumeroCarte(NUMERO_CARTE_TEST); // Remettre valide
        paiement.setCodeSecretCarte("12"); // Trop court
        assertFalse("Code secret trop court doit être invalide", 
                   paiement.informationsCarteValides());
        
        paiement.setCodeSecretCarte("12345"); // Trop long
        assertFalse("Code secret trop long doit être invalide", 
                   paiement.informationsCarteValides());
        
        paiement.setCodeSecretCarte("ABC"); // Pas uniquement des chiffres
        assertFalse("Code secret avec lettres doit être invalide", 
                   paiement.informationsCarteValides());
        
        // Test avec nom invalide
        paiement.setCodeSecretCarte(CODE_SECRET_TEST); // Remettre valide
        paiement.setNomCarte(""); // Vide
        assertFalse("Nom vide doit être invalide", paiement.informationsCarteValides());
        
        paiement.setNomCarte("   "); // Espaces seulement
        assertFalse("Nom avec espaces seulement doit être invalide", 
                   paiement.informationsCarteValides());
        
        // Test avec montant invalide
        paiement.setNomCarte(NOM_CARTE_TEST); // Remettre valide
        paiement.setMontant(0.0); // Zéro
        assertFalse("Montant 0 doit être invalide", paiement.informationsCarteValides());
        
        paiement.setMontant(-10.0); // Négatif
        assertFalse("Montant négatif doit être invalide", paiement.informationsCarteValides());
    }
    
    // ==================== TESTS DE LA MÉTHODE TOSTRING() ====================
    
    /**
     * Test de la méthode toString().
     */
    @Test
    public void testToString() {
        String resultat = paiement.toString();
        
        // Vérifier que les informations importantes sont présentes
        assertTrue("Doit contenir l'ID paiement", resultat.contains(paiement.getIdPaiement()));
        assertTrue("Doit contenir le type", resultat.contains(TYPE_ABONNEMENT));
        assertTrue("Doit contenir le montant", resultat.contains("29.99€"));
        assertTrue("Doit contenir le statut", resultat.contains(STATUT_REUSSI));
        assertTrue("Doit contenir la méthode", resultat.contains(METHODE_CARTE));
        
        // Vérifier que les informations sensibles ne sont pas exposées
        assertFalse("Ne doit pas contenir le numéro de carte complet", 
                   resultat.contains(NUMERO_CARTE_TEST));
        assertFalse("Ne doit pas contenir le code secret", 
                   resultat.contains(CODE_SECRET_TEST));
        
        // Vérifier que le numéro est masqué
        assertTrue("Doit contenir le numéro masqué", resultat.contains("**** **** ****"));
    }
    
    // ==================== TESTS SUPPLÉMENTAIRES ====================
    
    /**
     * Test d'égalité entre deux paiements.
     */
    @Test
    public void testEquals() {
        // Deux paiements avec le même ID doivent être égaux
        Paiement p1 = new Paiement();
        p1.setIdPaiement("PAY_123");
        
        Paiement p2 = new Paiement();
        p2.setIdPaiement("PAY_123");
        
        // L'implémentation de equals dépend de la classe
        // Si equals n'est pas redéfini, ils ne seront pas égaux
        // assertTrue(p1.equals(p2)); // À décommenter si equals est implémenté
    }
    
    /**
     * Test avec des valeurs limites.
     */
    @Test
    public void testValeursLimites() {
        Paiement p = new Paiement();
        
        // Montant très grand
        p.setMontant(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, p.getMontant(), 0.001);
        
        // Montant très petit mais positif
        p.setMontant(Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE, p.getMontant(), 0.001);
        
        // ID usager très grand
        p.setIdUsager(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, p.getIdUsager());
    }
    
    /**
     * Test avec des caractères spéciaux dans le nom.
     */
    @Test
    public void testCaracteresSpeciauxDansNom() {
        String nomSpecial = "Jean-Philippe D'Ávila";
        paiement.setNomCarte(nomSpecial);
        assertEquals(nomSpecial, paiement.getNomCarte());
        
        // Vérifier que la validation fonctionne avec caractères spéciaux
        paiement.setNumeroCarte("1234567890123456");
        paiement.setCodeSecretCarte("123");
        paiement.setMontant(10.0);
        assertTrue(paiement.informationsCarteValides());
    }
    
    /**
     * Test de la cohérence entre les méthodes.
     */
    @Test
    public void testCohérenceMethodes() {
        // estPourAbonnement() et estPourStationnement() doivent être complémentaires
        paiement.setIdAbonnement(null);
        paiement.setTypePaiement(null);
        assertTrue(paiement.estPourStationnement());
        assertFalse(paiement.estPourAbonnement());
        
        paiement.setIdAbonnement("ABON_TEST");
        paiement.setTypePaiement(null);
        assertFalse(paiement.estPourStationnement());
        assertTrue(paiement.estPourAbonnement());
    }
}
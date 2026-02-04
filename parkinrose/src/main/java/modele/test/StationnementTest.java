package modele.test;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import modele.Stationnement;
import java.time.LocalDateTime;

/**
 * Classe de tests unitaires pour la classe Stationnement.
 * 
 * Cette classe teste les fonctionnalités principales de la classe Stationnement,
 * y compris les différents constructeurs, les méthodes utilitaires et la logique métier.
 */
public class StationnementTest {
    
    // ==================== VARIABLES DE TEST ====================
    
    private Stationnement stationnement;  // Objet Stationnement à tester
    private LocalDateTime maintenant;     // Horodatage de référence pour les tests
    
    // Constantes pour les tests
    private static final int ID_USAGER_TEST = 1;
    private static final String TYPE_VEHICULE_VOITURE = "Voiture";
    private static final String TYPE_VEHICULE_MOTO = "Moto";
    private static final String PLAQUE_IMMATRICULATION = "AB-123-CD";
    private static final String ID_TARIFICATION = "ZONE_BLEUE";
    private static final String ZONE = "Zone Bleue Centre";
    private static final int DUREE_HEURES = 2;
    private static final int DUREE_MINUTES = 30;
    private static final double COUT = 3.50;
    private static final String ID_PAIEMENT = "PAY_001";
    private static final String STATUT_ACTIF = "ACTIF";
    private static final String STATUT_TERMINE = "TERMINE";
    private static final String STATUT_EXPIRE = "EXPIRE";
    private static final String TYPE_STATIONNEMENT_VOIRIE = "VOIRIE";
    private static final String TYPE_STATIONNEMENT_PARKING = "PARKING";
    private static final String STATUT_PAIEMENT_PAYE = "PAYE";
    private static final String STATUT_PAIEMENT_NON_PAYE = "NON_PAYE";
    
    // ==================== MÉTHODES DE CONFIGURATION ====================
    
    /**
     * Méthode exécutée avant chaque test.
     * Initialise un objet Stationnement avec des données de test.
     */
    @Before
    public void setUp() {
        maintenant = LocalDateTime.now();
        stationnement = new Stationnement(
            ID_USAGER_TEST,
            TYPE_VEHICULE_VOITURE,
            PLAQUE_IMMATRICULATION,
            ID_TARIFICATION,
            ZONE,
            DUREE_HEURES,
            DUREE_MINUTES,
            COUT,
            ID_PAIEMENT
        );
    }
    
    // ==================== TESTS DES CONSTRUCTEURS ====================
    
    /**
     * Test du constructeur pour un stationnement en voirie.
     */
    @Test
    public void testConstructeurVoirie() {
        // Vérification des attributs fournis
        assertEquals("ID usager incorrect", ID_USAGER_TEST, stationnement.getIdUsager());
        assertEquals("Type véhicule incorrect", TYPE_VEHICULE_VOITURE, stationnement.getTypeVehicule());
        assertEquals("Plaque immatriculation incorrecte", PLAQUE_IMMATRICULATION, stationnement.getPlaqueImmatriculation());
        assertEquals("ID tarification incorrect", ID_TARIFICATION, stationnement.getIdTarification());
        assertEquals("Zone incorrecte", ZONE, stationnement.getZone());
        assertEquals("Durée heures incorrecte", DUREE_HEURES, stationnement.getDureeHeures());
        assertEquals("Durée minutes incorrecte", DUREE_MINUTES, stationnement.getDureeMinutes());
        assertEquals("Coût incorrect", COUT, stationnement.getCout(), 0.001);
        assertEquals("ID paiement incorrect", ID_PAIEMENT, stationnement.getIdPaiement());
        
        // Vérification des attributs définis automatiquement
        assertEquals("Statut incorrect", STATUT_ACTIF, stationnement.getStatut());
        assertEquals("Type stationnement incorrect", TYPE_STATIONNEMENT_VOIRIE, stationnement.getTypeStationnement());
        assertEquals("Statut paiement incorrect", STATUT_PAIEMENT_PAYE, stationnement.getStatutPaiement());
        
        // Vérification des dates
        assertNotNull("Date création ne doit pas être null", stationnement.getDateCreation());
        assertNotNull("Date fin ne doit pas être null", stationnement.getDateFin());
        
        // Vérification du calcul de la date de fin
        LocalDateTime dateFinAttendue = stationnement.getDateCreation()
            .plusHours(DUREE_HEURES)
            .plusMinutes(DUREE_MINUTES);
        assertEquals("Date fin incorrecte", dateFinAttendue, stationnement.getDateFin());
        
        // Vérification des attributs spécifiques aux parkings (doivent être null)
        assertNull("Heure arrivée doit être null pour voirie", stationnement.getHeureArrivee());
        assertNull("Heure départ doit être null pour voirie", stationnement.getHeureDepart());
    }
    
    /**
     * Test du constructeur pour un stationnement en parking.
     */
    @Test
    public void testConstructeurParking() {
        LocalDateTime heureArrivee = LocalDateTime.now();
        Stationnement stationnementParking = new Stationnement(
            2,
            TYPE_VEHICULE_MOTO,
            "XY-789-ZW",
            "PARK_001",
            "Parking Capitole",
            heureArrivee
        );
        
        // Vérification des attributs fournis
        assertEquals("ID usager incorrect", 2, stationnementParking.getIdUsager());
        assertEquals("Type véhicule incorrect", TYPE_VEHICULE_MOTO, stationnementParking.getTypeVehicule());
        assertEquals("Plaque immatriculation incorrecte", "XY-789-ZW", stationnementParking.getPlaqueImmatriculation());
        assertEquals("ID tarification incorrect", "PARK_001", stationnementParking.getIdTarification());
        assertEquals("Zone incorrecte", "Parking Capitole", stationnementParking.getZone());
        assertEquals("Heure arrivée incorrecte", heureArrivee, stationnementParking.getHeureArrivee());
        
        // Vérification des attributs définis automatiquement
        assertEquals("Statut incorrect", STATUT_ACTIF, stationnementParking.getStatut());
        assertEquals("Type stationnement incorrect", TYPE_STATIONNEMENT_PARKING, stationnementParking.getTypeStationnement());
        assertEquals("Statut paiement incorrect", STATUT_PAIEMENT_NON_PAYE, stationnementParking.getStatutPaiement());
        assertEquals("Coût initial incorrect", 0.0, stationnementParking.getCout(), 0.001);
        
        // Vérification des dates
        assertNotNull("Date création ne doit pas être null", stationnementParking.getDateCreation());
        
        // Vérification des attributs spécifiques aux voiries (doivent être null)
        assertNull("Date fin doit être null pour parking", stationnementParking.getDateFin());
        assertEquals("Durée heures doit être 0 pour parking", 0, stationnementParking.getDureeHeures());
        assertEquals("Durée minutes doit être 0 pour parking", 0, stationnementParking.getDureeMinutes());
    }
    
    /**
     * Test du constructeur par défaut.
     */
    @Test
    public void testConstructeurParDefaut() {
        Stationnement stationnementVide = new Stationnement();
        
        // Vérification des valeurs par défaut
        assertEquals("ID stationnement par défaut doit être 0", 0, stationnementVide.getIdStationnement());
        assertEquals("ID usager par défaut doit être 0", 0, stationnementVide.getIdUsager());
        assertNull("Type véhicule par défaut doit être null", stationnementVide.getTypeVehicule());
        assertNull("Plaque immatriculation par défaut doit être null", stationnementVide.getPlaqueImmatriculation());
        assertNull("ID tarification par défaut doit être null", stationnementVide.getIdTarification());
        assertNull("Zone par défaut doit être null", stationnementVide.getZone());
        assertEquals("Durée heures par défaut doit être 0", 0, stationnementVide.getDureeHeures());
        assertEquals("Durée minutes par défaut doit être 0", 0, stationnementVide.getDureeMinutes());
        assertEquals("Coût par défaut doit être 0.0", 0.0, stationnementVide.getCout(), 0.001);
        assertNull("Date création par défaut doit être null", stationnementVide.getDateCreation());
        assertNull("Date fin par défaut doit être null", stationnementVide.getDateFin());
        assertNull("Heure arrivée par défaut doit être null", stationnementVide.getHeureArrivee());
        assertNull("Heure départ par défaut doit être null", stationnementVide.getHeureDepart());
        assertNull("Statut par défaut doit être null", stationnementVide.getStatut());
        assertNull("Type stationnement par défaut doit être null", stationnementVide.getTypeStationnement());
        assertNull("Statut paiement par défaut doit être null", stationnementVide.getStatutPaiement());
        assertNull("ID paiement par défaut doit être null", stationnementVide.getIdPaiement());
    }
    
    // ==================== TESTS DES MÉTHODES DE TYPE ====================
    
    /**
     * Test des méthodes estParking() et estVoirie().
     */
    @Test
    public void testEstParking() {
        // Test pour un parking
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_PARKING);
        assertTrue("Doit être un parking", stationnement.estParking());
        assertFalse("Ne doit pas être une voirie", stationnement.estVoirie());
        
        // Test pour une voirie
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_VOIRIE);
        assertFalse("Ne doit pas être un parking", stationnement.estParking());
        assertTrue("Doit être une voirie", stationnement.estVoirie());
        
        // Test avec type null
        stationnement.setTypeStationnement(null);
        assertFalse("Ne doit pas être un parking avec type null", stationnement.estParking());
        assertFalse("Ne doit pas être une voirie avec type null", stationnement.estVoirie());
        
        // Test avec type inconnu
        stationnement.setTypeStationnement("AUTRE");
        assertFalse("Ne doit pas être un parking avec type inconnu", stationnement.estParking());
        assertFalse("Ne doit pas être une voirie avec type inconnu", stationnement.estVoirie());
    }
    
    // ==================== TESTS DES MÉTHODES DE DURÉE ====================
    
    /**
     * Test de la méthode getDureeTotaleMinutes().
     */
    @Test
    public void testGetDureeTotaleMinutes() {
        // Test avec heures et minutes
        stationnement.setDureeHeures(1);
        stationnement.setDureeMinutes(45);
        assertEquals("Durée totale incorrecte", 105, stationnement.getDureeTotaleMinutes());
        
        // Test avec seulement des minutes
        stationnement.setDureeHeures(0);
        stationnement.setDureeMinutes(30);
        assertEquals("Durée totale incorrecte", 30, stationnement.getDureeTotaleMinutes());
        
        // Test avec seulement des heures
        stationnement.setDureeHeures(2);
        stationnement.setDureeMinutes(0);
        assertEquals("Durée totale incorrecte", 120, stationnement.getDureeTotaleMinutes());
        
        // Test avec valeurs négatives (cas limite)
        stationnement.setDureeHeures(-1);
        stationnement.setDureeMinutes(-30);
        assertEquals("Durée totale incorrecte avec valeurs négatives", -90, stationnement.getDureeTotaleMinutes());
    }
    
    /**
     * Test de la méthode getDureeEcouleeMinutes() pour un parking.
     */
    @Test
    public void testGetDureeEcouleeMinutes() {
        // Test avec un parking
        LocalDateTime heureArrivee = LocalDateTime.now().minusHours(1);
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_PARKING);
        stationnement.setHeureArrivee(heureArrivee);
        
        long duree = stationnement.getDureeEcouleeMinutes();
        assertTrue("Durée écoulée doit être d'au moins 60 minutes", duree >= 60);
        
        // Test avec heure de départ définie
        stationnement.setHeureDepart(heureArrivee.plusMinutes(45));
        duree = stationnement.getDureeEcouleeMinutes();
        assertEquals("Durée écoulée incorrecte avec heure départ", 45, duree);
        
        // Test avec heure arrivée null
        stationnement.setHeureArrivee(null);
        assertEquals("Durée écoulée doit être 0 avec heure arrivée null", 0, stationnement.getDureeEcouleeMinutes());
    }
    
    /**
     * Test de la méthode getDureeEcouleeMinutes() pour une voirie.
     */
    @Test
    public void testGetDureeEcouleeMinutes_PourVoirie() {
        // Pour une voirie, la méthode doit toujours retourner 0
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_VOIRIE);
        stationnement.setHeureArrivee(LocalDateTime.now().minusHours(1));
        assertEquals("Durée écoulée doit être 0 pour une voirie", 0, stationnement.getDureeEcouleeMinutes());
    }
    
    /**
     * Test de la méthode getTempsRestantMinutes().
     */
    @Test
    public void testGetTempsRestantMinutes() {
        // Test pour une voirie avec temps restant
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_VOIRIE);
        LocalDateTime dateFin = LocalDateTime.now().plusMinutes(30);
        stationnement.setDateFin(dateFin);
        
        long tempsRestant = stationnement.getTempsRestantMinutes();
        assertTrue("Temps restant doit être entre 0 et 30", tempsRestant >= 0 && tempsRestant <= 30);
        
        // Test avec date fin dans le passé
        stationnement.setDateFin(LocalDateTime.now().minusMinutes(30));
        assertEquals("Temps restant doit être 0 quand date fin dans le passé", 0, stationnement.getTempsRestantMinutes());
        
        // Test avec date fin null
        stationnement.setDateFin(null);
        assertEquals("Temps restant doit être 0 quand date fin est null", 0, stationnement.getTempsRestantMinutes());
    }
    
    /**
     * Test de la méthode getTempsRestantMinutes() pour un parking.
     */
    @Test
    public void testGetTempsRestantMinutes_PourParking() {
        // Pour un parking, la méthode doit toujours retourner 0
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_PARKING);
        stationnement.setDateFin(LocalDateTime.now().plusMinutes(30));
        assertEquals("Temps restant doit être 0 pour un parking", 0, stationnement.getTempsRestantMinutes());
    }
    
    /**
     * Test de la méthode getTempsRestantFormate().
     */
    @Test
    public void testGetTempsRestantFormate() {
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_VOIRIE);
        
        // Test avec temps restant
        LocalDateTime dateFin = LocalDateTime.now().plusHours(1).plusMinutes(15);
        stationnement.setDateFin(dateFin);
        String resultat = stationnement.getTempsRestantFormate();
        assertTrue("Doit contenir '1h 15min'", resultat.contains("h") && resultat.contains("min"));
        
        // Test avec temps écoulé
        stationnement.setDateFin(LocalDateTime.now().minusMinutes(1));
        assertEquals("Doit retourner 'Temps écoulé'", "Temps écoulé", stationnement.getTempsRestantFormate());
        
        // Test avec date fin null
        stationnement.setDateFin(null);
        assertEquals("Doit retourner 'Temps écoulé' quand date fin est null", 
                    "Temps écoulé", stationnement.getTempsRestantFormate());
    }
    
    // ==================== TESTS DES MÉTHODES DE STATUT ====================
    
    /**
     * Test de la méthode estActif().
     */
    @Test
    public void testEstActif() {
        stationnement.setStatut(STATUT_ACTIF);
        assertTrue("Doit être actif", stationnement.estActif());
        
        stationnement.setStatut(STATUT_TERMINE);
        assertFalse("Ne doit pas être actif", stationnement.estActif());
        
        stationnement.setStatut(STATUT_EXPIRE);
        assertFalse("Ne doit pas être actif", stationnement.estActif());
        
        stationnement.setStatut(null);
        assertFalse("Ne doit pas être actif avec statut null", stationnement.estActif());
        
        stationnement.setStatut("AUTRE_STATUT");
        assertFalse("Ne doit pas être actif avec statut inconnu", stationnement.estActif());
    }
    
    /**
     * Test de la méthode estTermine().
     */
    @Test
    public void testEstTermine() {
        stationnement.setStatut(STATUT_TERMINE);
        assertTrue("Doit être terminé", stationnement.estTermine());
        
        stationnement.setStatut(STATUT_ACTIF);
        assertFalse("Ne doit pas être terminé", stationnement.estTermine());
        
        stationnement.setStatut(STATUT_EXPIRE);
        assertFalse("Ne doit pas être terminé", stationnement.estTermine());
    }
    
    /**
     * Test de la méthode estExpire().
     */
    @Test
    public void testEstExpire() {
        stationnement.setStatut(STATUT_EXPIRE);
        assertTrue("Doit être expiré", stationnement.estExpire());
        
        stationnement.setStatut(STATUT_ACTIF);
        assertFalse("Ne doit pas être expiré", stationnement.estExpire());
        
        stationnement.setStatut(STATUT_TERMINE);
        assertFalse("Ne doit pas être expiré", stationnement.estExpire());
    }
    
    /**
     * Test de la méthode estPaye().
     */
    @Test
    public void testEstPaye() {
        stationnement.setStatutPaiement(STATUT_PAIEMENT_PAYE);
        assertTrue("Doit être payé", stationnement.estPaye());
        
        stationnement.setStatutPaiement(STATUT_PAIEMENT_NON_PAYE);
        assertFalse("Ne doit pas être payé", stationnement.estPaye());
        
        stationnement.setStatutPaiement(null);
        assertFalse("Ne doit pas être payé avec statut paiement null", stationnement.estPaye());
        
        stationnement.setStatutPaiement("EN_ATTENTE");
        assertFalse("Ne doit pas être payé avec autre statut", stationnement.estPaye());
    }
    
    /**
     * Test de la méthode estTempsEcoule().
     */
    @Test
    public void testEstTempsEcoule() {
        // Test pour une voirie
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_VOIRIE);
        
        stationnement.setDateFin(LocalDateTime.now().minusMinutes(1));
        assertTrue("Doit être temps écoulé", stationnement.estTempsEcoule());
        
        stationnement.setDateFin(LocalDateTime.now().plusMinutes(1));
        assertFalse("Ne doit pas être temps écoulé", stationnement.estTempsEcoule());
        
        // Test pour un parking (toujours false)
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_PARKING);
        stationnement.setDateFin(LocalDateTime.now().minusMinutes(1));
        assertFalse("Ne doit pas être temps écoulé pour un parking", stationnement.estTempsEcoule());
        
        // Test avec date fin null
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_VOIRIE);
        stationnement.setDateFin(null);
        assertFalse("Ne doit pas être temps écoulé avec date fin null", stationnement.estTempsEcoule());
    }
    
    // ==================== TESTS DES MÉTHODES DE PAIEMENT ====================
    
    /**
     * Test de la méthode necessitePaiement().
     */
    @Test
    public void testNecessitePaiement() {
        // Test pour un parking non payé
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_PARKING);
        stationnement.setStatutPaiement(STATUT_PAIEMENT_NON_PAYE);
        assertTrue("Doit nécessiter un paiement", stationnement.necessitePaiement());
        
        // Test pour un parking payé
        stationnement.setStatutPaiement(STATUT_PAIEMENT_PAYE);
        assertFalse("Ne doit pas nécessiter un paiement", stationnement.necessitePaiement());
        
        // Test pour une voirie non payée
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_VOIRIE);
        stationnement.setStatutPaiement(STATUT_PAIEMENT_NON_PAYE);
        assertFalse("Ne doit pas nécessiter un paiement pour une voirie", stationnement.necessitePaiement());
        
        // Test avec statut paiement null
        stationnement.setStatutPaiement(null);
        assertFalse("Ne doit pas nécessiter un paiement avec statut null", stationnement.necessitePaiement());
    }
    
    /**
     * Test de la méthode marquerCommePaye().
     */
    @Test
    public void testMarquerCommePaye() {
        // Test pour un parking
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_PARKING);
        stationnement.setStatutPaiement(STATUT_PAIEMENT_NON_PAYE);
        stationnement.setStatut(STATUT_ACTIF);
        stationnement.setCout(0.0);
        
        String nouveauPaiementId = "PAY_NEW";
        double nouveauCout = 15.75;
        
        stationnement.marquerCommePaye(nouveauPaiementId, nouveauCout);
        
        assertEquals("Statut paiement incorrect après marquer comme payé", 
                    STATUT_PAIEMENT_PAYE, stationnement.getStatutPaiement());
        assertEquals("ID paiement incorrect après marquer comme payé", 
                    nouveauPaiementId, stationnement.getIdPaiement());
        assertEquals("Coût incorrect après marquer comme payé", 
                    nouveauCout, stationnement.getCout(), 0.001);
        assertEquals("Statut incorrect après marquer comme payé", 
                    STATUT_TERMINE, stationnement.getStatut());
        assertNotNull("Heure départ ne doit pas être null après marquer comme payé", 
                     stationnement.getHeureDepart());
        
        // Test pour une voirie (ne devrait pas changer le statut)
        stationnement = new Stationnement(1, "Voiture", "TEST", "ZONE", "Zone", 1, 0, 5.0, "PAY_OLD");
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_VOIRIE);
        stationnement.setStatut(STATUT_ACTIF);
        
        stationnement.marquerCommePaye("PAY_NEW_2", 10.0);
        
        assertEquals("Statut doit rester ACTIF pour une voirie", 
                    STATUT_ACTIF, stationnement.getStatut());
        assertEquals("Statut paiement doit devenir PAYE", 
                    STATUT_PAIEMENT_PAYE, stationnement.getStatutPaiement());
    }
    
    /**
     * Test de la méthode calculerCoutEstime().
     */
    @Test
    public void testCalculerCoutEstime() {
        // Test pour un parking
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_PARKING);
        LocalDateTime heureArrivee = LocalDateTime.now().minusHours(2);
        stationnement.setHeureArrivee(heureArrivee);
        
        double tarifHoraire = 2.0;
        double coutEstime = stationnement.calculerCoutEstime(tarifHoraire);
        
        // 2 heures à 2€/h = 4€ (arrondi par tranche de 30 min)
        assertEquals("Coût estimé incorrect", 4.0, coutEstime, 0.001);
        
        // Test pour une voirie (doit retourner le coût existant)
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_VOIRIE);
        stationnement.setCout(10.0);
        coutEstime = stationnement.calculerCoutEstime(tarifHoraire);
        assertEquals("Doit retourner le coût existant pour une voirie", 10.0, coutEstime, 0.001);
        
        // Test avec durée fractionnée (1h45 = 2h arrondi à l'heure supérieure)
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_PARKING);
        heureArrivee = LocalDateTime.now().minusMinutes(105); // 1h45
        stationnement.setHeureArrivee(heureArrivee);
        coutEstime = stationnement.calculerCoutEstime(tarifHoraire);
        assertEquals("Coût estimé avec durée fractionnée incorrect", 4.0, coutEstime, 0.001);
    }
    
    // ==================== TESTS DES MÉTHODES D'AFFICHAGE ====================
    
    /**
     * Test de la méthode toString().
     */
    @Test
    public void testToString() {
        String resultat = stationnement.toString();
        
        // Vérification de la présence des informations principales
        assertTrue("Doit contenir le type de stationnement", 
                  resultat.contains("type='VOIRIE'"));
        assertTrue("Doit contenir le type de véhicule", 
                  resultat.contains("véhicule='Voiture'"));
        assertTrue("Doit contenir la plaque d'immatriculation", 
                  resultat.contains("plaque='AB-123-CD'"));
        assertTrue("Doit contenir la zone", 
                  resultat.contains("zone='Zone Bleue Centre'"));
        assertTrue("Doit contenir l'ID tarification", 
                  resultat.contains("tarification='ZONE_BLEUE'"));
        assertTrue("Doit contenir le statut", 
                  resultat.contains("statut='ACTIF'"));
        assertTrue("Doit contenir le statut de paiement", 
                  resultat.contains("paiement='PAYE'"));
        
        // Vérification du format général
        assertTrue("Doit commencer par 'Stationnement{'", resultat.startsWith("Stationnement{"));
        assertTrue("Doit se terminer par '}'", resultat.endsWith("}"));
    }
    
    /**
     * Test de la méthode getAffichageSimplifie().
     */
    @Test
    public void testGetAffichageSimplifie() {
        String affichage = stationnement.getAffichageSimplifie();
        
        // Vérification de la présence des informations principales
        assertTrue("Doit contenir le type de véhicule", affichage.contains(TYPE_VEHICULE_VOITURE));
        assertTrue("Doit contenir la plaque d'immatriculation", affichage.contains(PLAQUE_IMMATRICULATION));
        assertTrue("Doit contenir la zone", affichage.contains(ZONE));
        assertTrue("Doit contenir la durée", affichage.contains("2h30min"));
        
        // Test pour un parking
        Stationnement stationnementParking = new Stationnement(
            1, "Moto", "XY-789", "PARK", "Parking Test", LocalDateTime.now());
        affichage = stationnementParking.getAffichageSimplifie();
        assertTrue("Doit contenir 'Parking'", affichage.contains("Parking"));
    }
    
    // ==================== TESTS DES GETTERS ET SETTERS ====================
    
    /**
     * Test complet des getters et setters.
     */
    @Test
    public void testSettersEtGetters() {
        Stationnement s = new Stationnement();
        
        // Test ID stationnement
        s.setIdStationnement(999);
        assertEquals("ID stationnement incorrect après setter", 999, s.getIdStationnement());
        
        // Test ID usager
        s.setIdUsager(100);
        assertEquals("ID usager incorrect après setter", 100, s.getIdUsager());
        
        // Test type véhicule
        s.setTypeVehicule("Camion");
        assertEquals("Type véhicule incorrect après setter", "Camion", s.getTypeVehicule());
        
        // Test plaque immatriculation
        s.setPlaqueImmatriculation("TEST-123");
        assertEquals("Plaque immatriculation incorrecte après setter", "TEST-123", s.getPlaqueImmatriculation());
        
        // Test ID tarification
        s.setIdTarification("ZONE_TEST");
        assertEquals("ID tarification incorrect après setter", "ZONE_TEST", s.getIdTarification());
        
        // Test zone
        s.setZone("Zone Test");
        assertEquals("Zone incorrecte après setter", "Zone Test", s.getZone());
        
        // Test durée heures
        s.setDureeHeures(3);
        assertEquals("Durée heures incorrecte après setter", 3, s.getDureeHeures());
        
        // Test durée minutes
        s.setDureeMinutes(15);
        assertEquals("Durée minutes incorrecte après setter", 15, s.getDureeMinutes());
        
        // Test coût
        s.setCout(25.50);
        assertEquals("Coût incorrect après setter", 25.50, s.getCout(), 0.001);
        
        // Test date création
        LocalDateTime testDate = LocalDateTime.now();
        s.setDateCreation(testDate);
        assertEquals("Date création incorrecte après setter", testDate, s.getDateCreation());
        
        // Test date fin
        s.setDateFin(testDate.plusHours(2));
        assertEquals("Date fin incorrecte après setter", testDate.plusHours(2), s.getDateFin());
        
        // Test heure arrivée
        s.setHeureArrivee(testDate);
        assertEquals("Heure arrivée incorrecte après setter", testDate, s.getHeureArrivee());
        
        // Test heure départ
        s.setHeureDepart(testDate.plusHours(1));
        assertEquals("Heure départ incorrecte après setter", testDate.plusHours(1), s.getHeureDepart());
        
        // Test statut
        s.setStatut("TEST_STATUT");
        assertEquals("Statut incorrect après setter", "TEST_STATUT", s.getStatut());
        
        // Test type stationnement
        s.setTypeStationnement("TEST_TYPE");
        assertEquals("Type stationnement incorrect après setter", "TEST_TYPE", s.getTypeStationnement());
        
        // Test statut paiement
        s.setStatutPaiement("TEST_PAIEMENT");
        assertEquals("Statut paiement incorrect après setter", "TEST_PAIEMENT", s.getStatutPaiement());
        
        // Test ID paiement
        s.setIdPaiement("PAY_TEST");
        assertEquals("ID paiement incorrect après setter", "PAY_TEST", s.getIdPaiement());
    }
    
    // ==================== TESTS SUPPLÉMENTAIRES ====================
    
    /**
     * Test de la méthode peutAccueillirVehicule().
     */
    @Test
    public void testPeutAccueillirVehicule() {
        // Cette méthode n'existe pas dans la classe fournie,
        // mais pourrait être utile pour valider si un véhicule peut stationner
        // selon sa hauteur et le type de stationnement
        
        // stationnement.peutAccueillirVehicule("Moto", 1.2);
        // stationnement.peutAccueillirVehicule("Voiture", 1.8);
        // stationnement.peutAccueillirVehicule("Camion", 3.5);
    }
    
    /**
     * Test des valeurs limites.
     */
    @Test
    public void testValeursLimites() {
        Stationnement s = new Stationnement();
        
        // Test avec grandes valeurs
        s.setIdStationnement(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, s.getIdStationnement());
        
        s.setIdUsager(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, s.getIdUsager());
        
        s.setDureeHeures(Integer.MAX_VALUE);
        s.setDureeMinutes(Integer.MAX_VALUE);
        // Attention à l'overflow dans getDureeTotaleMinutes()
        long dureeTotale = s.getDureeTotaleMinutes();
        assertTrue("Durée totale doit être calculée", dureeTotale > 0);
        
        // Test avec coût très élevé
        s.setCout(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, s.getCout(), 0.001);
    }
    
    /**
     * Test de la cohérence entre les méthodes.
     */
    @Test
    public void testCohérenceMethodes() {
        // Un stationnement terminé ne devrait pas être actif
        stationnement.setStatut(STATUT_TERMINE);
        assertTrue(stationnement.estTermine());
        assertFalse(stationnement.estActif());
        
        // Un stationnement expiré ne devrait pas être actif
        stationnement.setStatut(STATUT_EXPIRE);
        assertTrue(stationnement.estExpire());
        assertFalse(stationnement.estActif());
        
        // Un stationnement payé ne devrait pas nécessiter de paiement
        stationnement.setStatutPaiement(STATUT_PAIEMENT_PAYE);
        assertTrue(stationnement.estPaye());
        assertFalse(stationnement.necessitePaiement());
        
        // Un stationnement voirie ne devrait pas avoir de durée écoulée
        stationnement.setTypeStationnement(TYPE_STATIONNEMENT_VOIRIE);
        stationnement.setHeureArrivee(LocalDateTime.now().minusHours(1));
        assertEquals(0, stationnement.getDureeEcouleeMinutes());
    }
    
    /**
     * Test avec des caractères spéciaux dans la plaque.
     */
    @Test
    public void testCaracteresSpeciauxPlaque() {
        String plaqueSpeciale = "AB-123-ÉÉ";
        stationnement.setPlaqueImmatriculation(plaqueSpeciale);
        assertEquals(plaqueSpeciale, stationnement.getPlaqueImmatriculation());
        
        // Vérifier que toString() gère correctement les caractères spéciaux
        String resultat = stationnement.toString();
        assertTrue(resultat.contains(plaqueSpeciale));
    }
    
    /**
     * Test des statuts multiples.
     */
    @Test
    public void testStatutsMultiples() {
        // Test de tous les statuts possibles
        String[] statutsPossibles = {"ACTIF", "TERMINE", "EXPIRE", "EN_ATTENTE", "ANNULE", "RESERVE"};
        
        for (String statut : statutsPossibles) {
            stationnement.setStatut(statut);
            assertEquals(statut, stationnement.getStatut());
        }
    }
}
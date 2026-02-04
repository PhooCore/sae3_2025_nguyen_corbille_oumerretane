package modele.test;

import org.junit.Test;

import modele.Parking;

import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Classe de tests unitaires pour la classe Parking.
 * 
 * Cette classe teste les fonctionnalités principales de la classe Parking,
 * y compris les différents constructeurs, les méthodes utilitaires et la logique métier.
 */
public class ParkingTest {
    
    // ==================== VARIABLES DE TEST ====================
    
    private Parking parking;  // Objet Parking à tester
    
    // Constantes pour les tests
    private static final String ID_PARKING = "PARK_001";
    private static final String LIBELLE_PARKING = "Parking Capitole";
    private static final String ADRESSE_PARKING = "Place du Capitole, Toulouse";
    private static final int NOMBRE_PLACES = 200;
    private static final int PLACES_DISPONIBLES = 150;
    private static final double HAUTEUR_PARKING = 2.1;
    private static final boolean TARIF_SOIREE = true;
    private static final boolean HAS_MOTO = true;
    private static final int PLACES_MOTO = 20;
    private static final int PLACES_MOTO_DISPONIBLES = 10;
    private static final boolean EST_RELAIS = false;
    private static final Float POSITION_X = 1.4442f;
    private static final Float POSITION_Y = 43.6047f;
    
    // ==================== MÉTHODES DE CONFIGURATION ====================
    
    /**
     * Méthode exécutée avant chaque test.
     * Initialise un objet Parking avec des données de test complètes.
     */
    @Before
    public void setUp() {
        parking = new Parking(
            ID_PARKING,
            LIBELLE_PARKING,
            ADRESSE_PARKING,
            NOMBRE_PLACES,
            PLACES_DISPONIBLES,
            HAUTEUR_PARKING,
            TARIF_SOIREE,
            HAS_MOTO,
            PLACES_MOTO,
            PLACES_MOTO_DISPONIBLES,
            EST_RELAIS,
            POSITION_X,
            POSITION_Y
        );
    }
    
    // ==================== TESTS DES CONSTRUCTEURS ====================
    
    /**
     * Test du constructeur complet avec toutes les caractéristiques.
     */
    @Test
    public void testConstructeurComplet() {
        // Vérification des attributs de base
        assertEquals("ID parking incorrect", ID_PARKING, parking.getIdParking());
        assertEquals("Libellé parking incorrect", LIBELLE_PARKING, parking.getLibelleParking());
        assertEquals("Adresse parking incorrecte", ADRESSE_PARKING, parking.getAdresseParking());
        assertEquals("Nombre places incorrect", NOMBRE_PLACES, parking.getNombrePlaces());
        assertEquals("Places disponibles incorrectes", PLACES_DISPONIBLES, parking.getPlacesDisponibles());
        assertEquals("Hauteur parking incorrecte", HAUTEUR_PARKING, parking.getHauteurParking(), 0.001);
        
        // Vérification des attributs booléens
        assertTrue("Tarif soirée incorrect", parking.hasTarifSoiree());
        assertTrue("Has moto incorrect", parking.hasMoto());
        assertFalse("Est relais incorrect", parking.isEstRelais());
        
        // Vérification des attributs spécifiques aux motos
        assertEquals("Places moto incorrectes", PLACES_MOTO, parking.getPlacesMoto());
        assertEquals("Places moto disponibles incorrectes", 
                    PLACES_MOTO_DISPONIBLES, parking.getPlacesMotoDisponibles());
        
        // Vérification des coordonnées géographiques
        assertNotNull("Position X ne doit pas être null", parking.getPositionX());
        assertNotNull("Position Y ne doit pas être null", parking.getPositionY());
        assertEquals("Position X incorrecte", POSITION_X, parking.getPositionX(), 0.0001f);
        assertEquals("Position Y incorrecte", POSITION_Y, parking.getPositionY(), 0.0001f);
        
        // Vérification de la valeur par défaut du tarif horaire
        assertEquals("Tarif horaire par défaut incorrect", 0.0, parking.getTarifHoraire(), 0.001);
    }
    
    /**
     * Test du constructeur sans positions géographiques.
     */
    @Test
    public void testConstructeurSansPositions() {
        Parking parkingSimple = new Parking(
            "PARK_002",
            "Parking Simple",
            "Adresse Simple",
            100,
            50,
            2.0,
            false,
            false,
            0,
            0,
            true  // est_relais
        );
        
        // Vérification des attributs de base
        assertEquals("ID parking incorrect", "PARK_002", parkingSimple.getIdParking());
        assertEquals("Libellé parking incorrect", "Parking Simple", parkingSimple.getLibelleParking());
        assertEquals("Nombre places incorrect", 100, parkingSimple.getNombrePlaces());
        assertEquals("Places disponibles incorrectes", 50, parkingSimple.getPlacesDisponibles());
        
        // Vérification des attributs spécifiques
        assertTrue("Est relais incorrect", parkingSimple.isEstRelais());
        assertFalse("Tarif soirée incorrect", parkingSimple.hasTarifSoiree());
        assertFalse("Has moto incorrect", parkingSimple.hasMoto());
        
        // Vérification des coordonnées géographiques (doivent être null)
        assertNull("Position X doit être null", parkingSimple.getPositionX());
        assertNull("Position Y doit être null", parkingSimple.getPositionY());
        
        // Vérification du taux d'occupation
        double tauxAttendu = ((100.0 - 50.0) / 100.0) * 100.0; // 50% d'occupation
        assertEquals("Taux d'occupation incorrect", tauxAttendu, parkingSimple.getTauxOccupation(), 0.001);
    }
    
    /**
     * Test du constructeur basique (minimal).
     */
    @Test
    public void testConstructeurBasique() {
        Parking parkingBasique = new Parking(
            "PARK_003",
            "Parking Basique",
            "Adresse Basique",
            50,
            25,
            2.5,
            true
        );
        
        // Vérification des attributs de base
        assertEquals("ID parking incorrect", "PARK_003", parkingBasique.getIdParking());
        assertEquals("Libellé parking incorrect", "Parking Basique", parkingBasique.getLibelleParking());
        assertEquals("Adresse parking incorrecte", "Adresse Basique", parkingBasique.getAdresseParking());
        assertEquals("Nombre places incorrect", 50, parkingBasique.getNombrePlaces());
        assertEquals("Places disponibles incorrectes", 25, parkingBasique.getPlacesDisponibles());
        assertEquals("Hauteur parking incorrecte", 2.5, parkingBasique.getHauteurParking(), 0.001);
        
        // Vérification des valeurs par défaut
        assertTrue("Tarif soirée incorrect", parkingBasique.hasTarifSoiree());
        assertFalse("Has moto incorrect par défaut", parkingBasique.hasMoto());
        assertEquals("Places moto incorrectes par défaut", 0, parkingBasique.getPlacesMoto());
        assertEquals("Places moto disponibles incorrectes par défaut", 0, parkingBasique.getPlacesMotoDisponibles());
        assertFalse("Est relais incorrect par défaut", parkingBasique.isEstRelais());
        assertNull("Position X doit être null par défaut", parkingBasique.getPositionX());
        assertNull("Position Y doit être null par défaut", parkingBasique.getPositionY());
    }
    
    /**
     * Test de l'enchâinement des constructeurs.
     * Vérifie que tous les constructeurs appellent correctement le constructeur principal.
     */
    @Test
    public void testConstructeursChainés() {
        // Test du constructeur basique
        Parking p1 = new Parking("P1", "Lib1", "Addr1", 100, 50, 2.0, true);
        
        assertEquals("ID parking incorrect pour constructeur basique", "P1", p1.getIdParking());
        assertEquals("Libellé parking incorrect pour constructeur basique", "Lib1", p1.getLibelleParking());
        assertEquals("Nombre places incorrect pour constructeur basique", 100, p1.getNombrePlaces());
        assertEquals("Places disponibles incorrectes pour constructeur basique", 50, p1.getPlacesDisponibles());
        assertEquals("Hauteur parking incorrecte pour constructeur basique", 2.0, p1.getHauteurParking(), 0.001);
        assertTrue("Tarif soirée incorrect pour constructeur basique", p1.hasTarifSoiree());
        assertFalse("Has moto incorrect par défaut", p1.hasMoto());
        assertFalse("Est relais incorrect par défaut", p1.isEstRelais());
        assertNull("Position X doit être null par défaut", p1.getPositionX());
        assertNull("Position Y doit être null par défaut", p1.getPositionY());
        
        // Test du constructeur avec gestion des motos
        Parking p2 = new Parking("P2", "Lib2", "Addr2", 200, 100, 2.5, false, false, 0, 0);
        
        assertEquals("ID parking incorrect pour constructeur motos", "P2", p2.getIdParking());
        assertEquals("Libellé parking incorrect pour constructeur motos", "Lib2", p2.getLibelleParking());
        assertEquals("Nombre places incorrect pour constructeur motos", 200, p2.getNombrePlaces());
        assertEquals("Places disponibles incorrectes pour constructeur motos", 100, p2.getPlacesDisponibles());
        assertEquals("Hauteur parking incorrecte pour constructeur motos", 2.5, p2.getHauteurParking(), 0.001);
        assertFalse("Tarif soirée incorrect pour constructeur motos", p2.hasTarifSoiree());
        assertFalse("Has moto incorrect pour constructeur motos", p2.hasMoto());
        assertFalse("Est relais incorrect par défaut", p2.isEstRelais());
    }
    
    // ==================== TESTS DES MÉTHODES UTILITAIRES ====================
    
    /**
     * Test de la méthode hasPlacesDisponibles().
     */
    @Test
    public void testHasPlacesDisponibles() {
        // Cas 1: Places disponibles > 0
        parking.setPlacesDisponibles(10);
        assertTrue("Doit avoir des places disponibles", parking.hasPlacesDisponibles());
        
        // Cas 2: Places disponibles = 0
        parking.setPlacesDisponibles(0);
        assertFalse("Ne doit pas avoir de places disponibles", parking.hasPlacesDisponibles());
        
        // Cas 3: Places disponibles < 0 (cas limite)
        parking.setPlacesDisponibles(-5);
        assertFalse("Ne doit pas avoir de places disponibles avec valeur négative", 
                   parking.hasPlacesDisponibles());
        
        // Cas 4: Vérification avec méthode estOuvert()
        parking.setPlacesDisponibles(5);
        assertTrue("Parking doit être ouvert avec places disponibles", parking.estOuvert());
        
        parking.setPlacesDisponibles(0);
        assertFalse("Parking ne doit pas être ouvert sans places", parking.estOuvert());
    }
    
    /**
     * Test de la méthode hasPlacesMotoDisponibles().
     */
    @Test
    public void testHasPlacesMotoDisponibles() {
        // Cas 1: Parking avec motos et places disponibles
        parking.setHasMoto(true);
        parking.setPlacesMotoDisponibles(5);
        assertTrue("Doit avoir des places moto disponibles", parking.hasPlacesMotoDisponibles());
        
        // Cas 2: Parking avec motos mais sans places disponibles
        parking.setPlacesMotoDisponibles(0);
        assertFalse("Ne doit pas avoir de places moto disponibles", parking.hasPlacesMotoDisponibles());
        
        // Cas 3: Parking sans motos (même avec des places en théorie)
        parking.setHasMoto(false);
        parking.setPlacesMotoDisponibles(10);
        assertFalse("Ne doit pas avoir de places moto si hasMoto = false", 
                   parking.hasPlacesMotoDisponibles());
        
        // Cas 4: Places moto disponibles négatives
        parking.setHasMoto(true);
        parking.setPlacesMotoDisponibles(-3);
        assertFalse("Ne doit pas avoir de places moto disponibles avec valeur négative", 
                   parking.hasPlacesMotoDisponibles());
    }
    
    /**
     * Test du tarif horaire.
     */
    @Test
    public void testTarifHoraire() {
        // Test du setter et getter
        parking.setTarifHoraire(3.50);
        assertEquals("Tarif horaire incorrect après setter", 3.50, parking.getTarifHoraire(), 0.001);
        
        // Test avec valeur nulle
        parking.setTarifHoraire(0.0);
        assertEquals("Tarif horaire nul incorrect", 0.0, parking.getTarifHoraire(), 0.001);
        
        // Test avec valeur négative (si autorisé)
        parking.setTarifHoraire(-1.0);
        assertEquals("Tarif horaire négatif incorrect", -1.0, parking.getTarifHoraire(), 0.001);
        
        // Test avec valeur très élevée
        parking.setTarifHoraire(1000.0);
        assertEquals("Tarif horaire élevé incorrect", 1000.0, parking.getTarifHoraire(), 0.001);
    }
    
    // ==================== TESTS DE LA MÉTHODE TOSTRING() ====================
    
    /**
     * Test de la méthode toString().
     */
    @Test
    public void testToString() {
        String resultat = parking.toString();
        
        // Vérification de la présence des informations principales
        assertTrue("Doit contenir le libellé du parking", 
                  resultat.contains(LIBELLE_PARKING));
        assertTrue("Doit contenir l'adresse du parking", 
                  resultat.contains(ADRESSE_PARKING));
        assertTrue("Doit contenir les places disponibles et totales", 
                  resultat.contains("150/200"));
        
        // Vérification de la présence des informations moto
        assertTrue("Doit contenir 'places moto' quand hasMoto = true", 
                  resultat.contains("places moto"));
        assertTrue("Doit contenir le nombre de places moto disponibles/totales", 
                  resultat.contains("10/20"));
    }
    
    /**
     * Test de la méthode toString() sans motos.
     */
    @Test
    public void testToString_SansMoto() {
        parking.setHasMoto(false);
        String resultat = parking.toString();
        
        // Vérifier que les informations moto ne sont pas présentes
        assertFalse("Ne doit pas contenir 'places moto' quand hasMoto = false", 
                   resultat.contains("places moto"));
        
        // Vérifier que les autres informations sont toujours présentes
        assertTrue("Doit toujours contenir le libellé", 
                  resultat.contains(LIBELLE_PARKING));
        assertTrue("Doit toujours contenir les places voitures", 
                  resultat.contains("150/200"));
    }
    
    /**
     * Test de la méthode toString() pour un parking relais.
     */
    @Test
    public void testToString_Relais() {
        parking.setEstRelais(true);
        String resultat = parking.toString();
        
        // Vérifier que le parking relais est bien indiqué
        assertTrue("Doit contenir '[Relais]' pour un parking relais", 
                  resultat.contains("[Relais]"));
    }
    
    /**
     * Test de la méthode toString() pour un parking avec tarif soirée.
     */
    @Test
    public void testToString_TarifSoiree() {
        parking.setTarifSoiree(true);
        String resultat = parking.toString();
        
        // Vérifier que le tarif soirée est bien indiqué
        assertTrue("Doit contenir '[Tarif soirée]' quand tarifSoiree = true", 
                  resultat.contains("[Tarif soirée]"));
    }
    
    // ==================== TESTS DES GETTERS ET SETTERS ====================
    
    /**
     * Test complet des getters et setters.
     */
    @Test
    public void testSetters() {
        // Test ID parking
        parking.setIdParking("PARK_NEW");
        assertEquals("ID parking incorrect après setter", "PARK_NEW", parking.getIdParking());
        
        // Test libellé parking
        parking.setLibelleParking("Nouveau Parking");
        assertEquals("Libellé parking incorrect après setter", "Nouveau Parking", parking.getLibelleParking());
        
        // Test adresse parking
        parking.setAdresseParking("Nouvelle Adresse");
        assertEquals("Adresse parking incorrecte après setter", "Nouvelle Adresse", parking.getAdresseParking());
        
        // Test nombre places
        parking.setNombrePlaces(300);
        assertEquals("Nombre places incorrect après setter", 300, parking.getNombrePlaces());
        
        // Test places disponibles
        parking.setPlacesDisponibles(100);
        assertEquals("Places disponibles incorrectes après setter", 100, parking.getPlacesDisponibles());
        
        // Test hauteur parking
        parking.setHauteurParking(2.5);
        assertEquals("Hauteur parking incorrecte après setter", 2.5, parking.getHauteurParking(), 0.001);
        
        // Test tarif soirée
        parking.setTarifSoiree(false);
        assertFalse("Tarif soirée incorrect après setter", parking.hasTarifSoiree());
        
        // Test has moto
        parking.setHasMoto(false);
        assertFalse("Has moto incorrect après setter", parking.hasMoto());
        
        // Test places moto
        parking.setPlacesMoto(15);
        assertEquals("Places moto incorrectes après setter", 15, parking.getPlacesMoto());
        
        // Test places moto disponibles
        parking.setPlacesMotoDisponibles(8);
        assertEquals("Places moto disponibles incorrectes après setter", 8, parking.getPlacesMotoDisponibles());
        
        // Test est relais
        parking.setEstRelais(true);
        assertTrue("Est relais incorrect après setter", parking.isEstRelais());
        
        // Test position X
        parking.setPositionX(1.0f);
        assertNotNull("Position X ne doit pas être null après setter", parking.getPositionX());
        assertEquals("Position X incorrecte après setter", 1.0f, parking.getPositionX(), 0.001f);
        
        // Test position Y
        parking.setPositionY(2.0f);
        assertNotNull("Position Y ne doit pas être null après setter", parking.getPositionY());
        assertEquals("Position Y incorrecte après setter", 2.0f, parking.getPositionY(), 0.001f);
        
        // Test avec position null
        parking.setPositionX(null);
        assertNull("Position X doit pouvoir être null", parking.getPositionX());
        
        parking.setPositionY(null);
        assertNull("Position Y doit pouvoir être null", parking.getPositionY());
    }
    
    // ==================== TESTS SUPPLÉMENTAIRES ====================
    
    /**
     * Test du calcul du taux d'occupation.
     */
    @Test
    public void testTauxOccupation() {
        // Cas 1: Parking à moitié occupé
        parking.setNombrePlaces(100);
        parking.setPlacesDisponibles(50);
        double tauxAttendu = ((100 - 50) * 100.0) / 100;
        assertEquals("Taux d'occupation incorrect pour 50%", 
                    tauxAttendu, parking.getTauxOccupation(), 0.001);
        
        // Cas 2: Parking vide
        parking.setPlacesDisponibles(100);
        assertEquals("Taux d'occupation incorrect pour 0%", 
                    0.0, parking.getTauxOccupation(), 0.001);
        
        // Cas 3: Parking plein
        parking.setPlacesDisponibles(0);
        assertEquals("Taux d'occupation incorrect pour 100%", 
                    100.0, parking.getTauxOccupation(), 0.001);
        
        // Cas 4: Diviser par zéro (nombre places = 0)
        parking.setNombrePlaces(0);
        parking.setPlacesDisponibles(0);
        assertEquals("Taux d'occupation doit être 0 quand nombre places = 0", 
                    0.0, parking.getTauxOccupation(), 0.001);
    }
    
    /**
     * Test du taux d'occupation des motos.
     */
    @Test
    public void testTauxOccupationMoto() {
        // Cas 1: Parking avec motos partiellement occupé
        parking.setHasMoto(true);
        parking.setPlacesMoto(20);
        parking.setPlacesMotoDisponibles(10);
        double tauxAttendu = ((20 - 10) * 100.0) / 20;
        assertEquals("Taux d'occupation moto incorrect", 
                    tauxAttendu, parking.getTauxOccupationMoto(), 0.001);
        
        // Cas 2: Parking sans motos
        parking.setHasMoto(false);
        assertEquals("Taux d'occupation moto doit être 0 sans motos", 
                    0.0, parking.getTauxOccupationMoto(), 0.001);
        
        // Cas 3: Diviser par zéro (places moto = 0)
        parking.setHasMoto(true);
        parking.setPlacesMoto(0);
        assertEquals("Taux d'occupation moto doit être 0 quand places moto = 0", 
                    0.0, parking.getTauxOccupationMoto(), 0.001);
    }
    
    /**
     * Test de la méthode hasCoordonnees().
     */
    @Test
    public void testHasCoordonnees() {
        // Cas 1: Avec coordonnées
        parking.setPositionX(1.0f);
        parking.setPositionY(2.0f);
        assertTrue("Doit avoir des coordonnées", parking.hasCoordonnees());
        
        // Cas 2: Sans coordonnées (une seule coordonnée null)
        parking.setPositionX(null);
        assertFalse("Ne doit pas avoir de coordonnées si positionX est null", 
                   parking.hasCoordonnees());
        
        // Cas 3: Sans coordonnées (les deux coordonnées null)
        parking.setPositionY(null);
        assertFalse("Ne doit pas avoir de coordonnées si les deux sont null", 
                   parking.hasCoordonnees());
        
        // Cas 4: Avec seulement positionX
        parking.setPositionX(1.0f);
        parking.setPositionY(null);
        assertFalse("Ne doit pas avoir de coordonnées si seulement positionX est définie", 
                   parking.hasCoordonnees());
        
        // Cas 5: Avec seulement positionY
        parking.setPositionX(null);
        parking.setPositionY(2.0f);
        assertFalse("Ne doit pas avoir de coordonnées si seulement positionY est définie", 
                   parking.hasCoordonnees());
    }
    
    /**
     * Test de la méthode peutAccueillirHauteur().
     */
    @Test
    public void testPeutAccueillirHauteur() {
        // Cas 1: Véhicule plus petit que la hauteur limite
        assertTrue("Doit pouvoir accueillir un véhicule de 2.0m", 
                  parking.peutAccueillirHauteur(2.0));
        
        // Cas 2: Véhicule exactement à la hauteur limite
        assertTrue("Doit pouvoir accueillir un véhicule de 2.1m (limite)", 
                  parking.peutAccueillirHauteur(2.1));
        
        // Cas 3: Véhicule plus grand que la hauteur limite
        assertFalse("Ne doit pas pouvoir accueillir un véhicule de 2.2m", 
                   parking.peutAccueillirHauteur(2.2));
        
        // Cas 4: Hauteur négative (si le véhicule est souterrain ?)
        assertFalse("Ne doit pas pouvoir accueillir un véhicule avec hauteur négative", 
                   parking.peutAccueillirHauteur(-1.0));
    }
    
    /**
     * Test de la méthode getDisponibiliteTexte().
     */
    @Test
    public void testGetDisponibiliteTexte() {
        // Cas 1: Parking complet
        parking.setPlacesDisponibles(0);
        assertEquals("Disponibilité texte incorrecte pour parking complet", 
                    "Complet", parking.getDisponibiliteTexte());
        
        // Cas 2: Parking presque complet (1-3 places)
        parking.setPlacesDisponibles(3);
        assertTrue("Disponibilité texte incorrecte pour parking presque complet", 
                  parking.getDisponibiliteTexte().contains("Presque complet"));
        assertTrue("Doit indiquer le nombre de places", 
                  parking.getDisponibiliteTexte().contains("3"));
        
        parking.setPlacesDisponibles(1);
        assertTrue("Doit indiquer le nombre de places pour 1 place", 
                  parking.getDisponibiliteTexte().contains("1"));
        
        // Cas 3: Parking avec plusieurs places disponibles
        parking.setPlacesDisponibles(10);
        assertTrue("Doit indiquer le nombre de places disponibles", 
                  parking.getDisponibiliteTexte().contains("10"));
        assertFalse("Ne doit pas dire 'Presque complet' pour 10 places", 
                   parking.getDisponibiliteTexte().contains("Presque complet"));
    }
    
    /**
     * Test de la méthode getDisponibiliteMotoTexte().
     */
    @Test
    public void testGetDisponibiliteMotoTexte() {
        // Cas 1: Parking sans motos
        parking.setHasMoto(false);
        assertEquals("Disponibilité moto texte incorrecte sans motos", 
                    "Pas de places motos", parking.getDisponibiliteMotoTexte());
        
        // Cas 2: Parking avec motos mais complet
        parking.setHasMoto(true);
        parking.setPlacesMotoDisponibles(0);
        assertEquals("Disponibilité moto texte incorrecte pour complet", 
                    "Complet (motos)", parking.getDisponibiliteMotoTexte());
        
        // Cas 3: Parking avec motos et places disponibles
        parking.setPlacesMotoDisponibles(5);
        assertTrue("Doit indiquer le nombre de places moto disponibles", 
                  parking.getDisponibiliteMotoTexte().contains("5"));
    }
    
    /**
     * Test avec des valeurs limites.
     */
    @Test
    public void testValeursLimites() {
        // Test avec très grand nombre de places
        parking.setNombrePlaces(Integer.MAX_VALUE);
        parking.setPlacesDisponibles(Integer.MAX_VALUE / 2);
        assertTrue(parking.hasPlacesDisponibles());
        
        // Test avec coordonnées extrêmes
        parking.setPositionX(Float.MAX_VALUE);
        parking.setPositionY(Float.MIN_VALUE);
        assertTrue(parking.hasCoordonnees());
        
        // Test avec hauteur extrême
        parking.setHauteurParking(Double.MAX_VALUE);
        assertTrue("Doit pouvoir accueillir n'importe quel véhicule avec hauteur max", 
                  parking.peutAccueillirHauteur(Double.MAX_VALUE - 1));
    }
    
    /**
     * Test de la cohérence des données.
     */
    @Test
    public void testCohérenceDonnées() {
        // Les places disponibles ne doivent pas dépasser le nombre total de places
        parking.setNombrePlaces(100);
        parking.setPlacesDisponibles(150); // Incohérent mais possible avec le setter
        assertTrue("Places disponibles peuvent dépasser le nombre total (validation à faire ailleurs)", 
                  parking.hasPlacesDisponibles());
        
        // Même chose pour les motos
        parking.setPlacesMoto(10);
        parking.setPlacesMotoDisponibles(15); // Incohérent
        assertTrue("Places moto disponibles peuvent dépasser le total (validation à faire ailleurs)", 
                  parking.hasPlacesMotoDisponibles());
    }
}
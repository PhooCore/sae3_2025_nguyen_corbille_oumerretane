package modele.test;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import modele.Stationnement;
import modele.dao.MySQLConnection;
import modele.dao.ParkingDAO;
import modele.dao.StationnementDAO;
import modele.Parking;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StationnementDAOTest {
    
    private StationnementDAO dao;
    private ParkingDAO parkingDAO;
    private Connection conn;
    private int testUserId;
    
    @Before
    public void setUp() throws SQLException {
        dao = StationnementDAO.getInstance();
        parkingDAO = ParkingDAO.getInstance();
        conn = MySQLConnection.getConnection();
        
        // Nettoyer et préparer la base de test
        cleanDatabase();
        prepareTestData();
    }
    
    private void cleanDatabase() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Désactiver temporairement les contraintes
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // Nettoyer dans l'ordre inverse des dépendances
            stmt.execute("DELETE FROM Stationnement");
            stmt.execute("DELETE FROM Paiement");
            stmt.execute("DELETE FROM Vehicule_Usager");
            stmt.execute("DELETE FROM Appartenir");
            stmt.execute("DELETE FROM Parking");
            stmt.execute("DELETE FROM Zone");
            stmt.execute("DELETE FROM Abonnement");
            stmt.execute("DELETE FROM Usager");
            
            // Réactiver les contraintes
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
    
    private void prepareTestData() throws SQLException {
        // Créer un utilisateur de test
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO Usager (nom_usager, prenom_usager, mail_usager, mot_de_passe) " +
                "VALUES ('Test', 'User', 'test@stationnement.com', 'mdp')",
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                testUserId = rs.getInt(1);
            }
        }
        
        // Créer une zone de test
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Zone (id_zone, libelle_zone, couleur_zone, tarif_par_heure, duree_max) " +
                        "VALUES ('TEST_ZONE', 'Zone Test', 'blue', 1.00, '02:00:00')");
            
            // Créer un parking de test avec suffisamment de places
            stmt.execute("INSERT INTO Parking (id_parking, libelle_parking, adresse_parking, " +
                        "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                        "has_moto, places_moto, places_moto_disponibles, est_relais) " +
                        "VALUES ('TEST_PARK', 'Parking Test', 'Adresse Test', " +
                        "100, 100, 2.0, 1, 1, 20, 20, 0)");
            
            // Créer des paiements de test
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String now = LocalDateTime.now().format(formatter);
            
            stmt.execute("INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, " +
                        "montant, id_usager, date_paiement, methode_paiement, statut) " +
                        "VALUES ('PAY_1', 'John Doe', '1234567890123456', '123', " +
                        "2.00, " + testUserId + ", '" + now + "', 'CARTE', 'REUSSI')");
            
            stmt.execute("INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, " +
                        "montant, id_usager, date_paiement, methode_paiement, statut) " +
                        "VALUES ('PAY_3', 'Jane Doe', '9876543210987654', '456', " +
                        "1.50, " + testUserId + ", '" + now + "', 'CARTE', 'REUSSI')");
            
            // Insérer des stationnements de test
            // Stationnement voirie actif
            stmt.execute("INSERT INTO Stationnement (id_stationnement, id_usager, type_vehicule, " +
                        "plaque_immatriculation, id_zone, duree_heures, duree_minutes, cout, " +
                        "statut, date_creation, date_fin, type_stationnement, statut_paiement, id_paiement) " +
                        "VALUES (1, " + testUserId + ", 'Voiture', 'AB-123-CD', 'TEST_ZONE', 2, 0, 2.00, " +
                        "'ACTIF', '" + now + "', DATE_ADD('" + now + "', INTERVAL 120 MINUTE), 'VOIRIE', 'PAYE', 'PAY_1')");
            
            // Stationnement parking actif (sans paiement initial)
            stmt.execute("INSERT INTO Stationnement (id_stationnement, id_usager, type_vehicule, " +
                        "plaque_immatriculation, id_parking, cout, statut, heure_arrivee, " +
                        "type_stationnement, statut_paiement, date_creation) " +
                        "VALUES (2, " + testUserId + ", 'Moto', 'XY-789-ZW', 'TEST_PARK', 0.00, 'ACTIF', " +
                        "'" + now + "', 'PARKING', 'NON_PAYE', '" + now + "')");
            
            // Stationnement terminé
            stmt.execute("INSERT INTO Stationnement (id_stationnement, id_usager, type_vehicule, " +
                        "plaque_immatriculation, id_zone, duree_heures, duree_minutes, cout, " +
                        "statut, date_creation, date_fin, type_stationnement, statut_paiement, id_paiement) " +
                        "VALUES (3, " + testUserId + ", 'Voiture', 'EF-456-GH', 'TEST_ZONE', 1, 30, 1.50, " +
                        "'TERMINE', DATE_SUB('" + now + "', INTERVAL 3 HOUR), DATE_SUB('" + now + "', INTERVAL 90 MINUTE), 'VOIRIE', 'PAYE', 'PAY_3')");
        }
    }
    
    @After
    public void tearDown() throws SQLException {
        cleanDatabase();
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    @Test
    public void testFindAll() throws SQLException {
        List<Stationnement> stationnements = dao.findAll();
        assertNotNull(stationnements);
        assertEquals(3, stationnements.size());
    }
    
    @Test
    public void testFindById() throws SQLException {
        Stationnement stationnement = dao.findById("1");
        assertNotNull(stationnement);
        assertEquals(1, stationnement.getIdStationnement());
        assertEquals(testUserId, stationnement.getIdUsager());
        assertEquals("Voiture", stationnement.getTypeVehicule());
        assertEquals("AB-123-CD", stationnement.getPlaqueImmatriculation());
        assertEquals("VOIRIE", stationnement.getTypeStationnement());
        assertEquals("ACTIF", stationnement.getStatut());
    }
    
    @Test
    public void testCreerStationnementVoirie() throws SQLException {
        // Test simplifié - créer un stationnement sans paiement d'abord
        Stationnement nouveau = new Stationnement(
            testUserId,
            "Voiture",
            "TEST-001",
            "TEST_ZONE",
            "Zone Test",
            1,
            30,
            1.50,
            null  // Pas de paiement pour ce test
        );
        
        // Utiliser la méthode statique qui gère les paiements null
        boolean result = StationnementDAO.creerStationnementVoirieGratuit(
            testUserId,
            "Voiture",
            "TEST-001",
            "TEST_ZONE",
            1,
            30
        );
        
        assertTrue(result);
    }
    
    @Test
    public void testCreerStationnementParking() throws SQLException {
        // Vérifier les places disponibles avant
        Parking parking = parkingDAO.findById("TEST_PARK");
        int placesAvant = parking.getPlacesDisponibles();
        int placesMotoAvant = parking.getPlacesMotoDisponibles();
        
        // Créer un stationnement parking
        Stationnement nouveau = new Stationnement(
            testUserId,
            "Moto",
            "TEST-002",
            "TEST_PARK",
            "Parking Test",
            LocalDateTime.now()
        );
        
        boolean result = dao.creerStationnementParking(nouveau);
        assertTrue(result);
        
        // Vérifier que les places ont été décrémentées
        parking = parkingDAO.findById("TEST_PARK");
        int placesApres = parking.getPlacesDisponibles();
        int placesMotoApres = parking.getPlacesMotoDisponibles();
        
        // Pour une moto, seulement les places moto devraient décrémenter
        assertEquals(placesAvant, placesApres); // Places normales inchangées
        assertEquals(placesMotoAvant - 1, placesMotoApres); // Places moto décrémentées
    }
    
    @Test
    public void testTerminerStationnementParking() throws SQLException {
        // Créer un nouveau paiement pour la terminaison
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, " +
                        "montant, id_usager, date_paiement, methode_paiement, statut) " +
                        "VALUES ('PAY_NEW', 'Termination Card', '1111222233334444', '789', " +
                        "5.00, " + testUserId + ", '" + now + "', 'CARTE', 'REUSSI')");
        }
        
        // Récupérer les places disponibles avant
        Parking parking = parkingDAO.findById("TEST_PARK");
        int placesMotoAvant = parking.getPlacesMotoDisponibles();
        
        // Utiliser la méthode statique de terminaison
        LocalDateTime heureDepart = LocalDateTime.now();
        boolean result = StationnementDAO.terminerStationnementParking(2, heureDepart, 5.00, "PAY_NEW");
        
        assertTrue(result);
        
        // Vérifier que le stationnement est terminé
        Stationnement termine = dao.findById("2");
        assertEquals("TERMINE", termine.getStatut());
        assertEquals("PAYE", termine.getStatutPaiement());
        assertEquals(5.00, termine.getCout(), 0.001);
        assertEquals("PAY_NEW", termine.getIdPaiement());
        
        // Vérifier que les places ont été réincrémentées
        parking = parkingDAO.findById("TEST_PARK");
        int placesMotoApres = parking.getPlacesMotoDisponibles();
        assertEquals(placesMotoAvant + 1, placesMotoApres);
    }
    
    @Test
    public void testGetHistoriqueStationnements() throws SQLException {
        List<Stationnement> historique = dao.getHistoriqueStationnements(testUserId);
        
        assertNotNull(historique);
        assertEquals(3, historique.size());
        
        // Vérifier que tous les stationnements appartiennent au bon utilisateur
        for (Stationnement s : historique) {
            assertEquals(testUserId, s.getIdUsager());
        }
    }
    
    @Test
    public void testGetStationnementActifByUsager() throws SQLException {
        Stationnement actif = dao.getStationnementActifByUsager(testUserId);
        
        assertNotNull(actif);
        assertEquals("ACTIF", actif.getStatut());
        assertEquals(testUserId, actif.getIdUsager());
    }
    
    @Test
    public void testHasStationnementActif() throws SQLException {
        boolean hasActive = dao.hasStationnementActif(testUserId);
        assertTrue(hasActive);
        
        // Créer un autre utilisateur sans stationnement actif
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO Usager (nom_usager, prenom_usager, mail_usager, mot_de_passe) " +
                "VALUES ('Test2', 'User2', 'test2@test.com', 'mdp')",
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            int newUserId = -1;
            if (rs.next()) {
                newUserId = rs.getInt(1);
            }
            
            hasActive = dao.hasStationnementActif(newUserId);
            assertFalse(hasActive);
        }
    }
    
    @Test
    public void testMettreAJourStatutPaiement() throws SQLException {
        // Créer un nouveau paiement
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, " +
                        "montant, id_usager, date_paiement, methode_paiement, statut) " +
                        "VALUES ('PAY_UPDATE', 'Update Card', '9999888877776666', '000', " +
                        "10.00, " + testUserId + ", '" + now + "', 'CARTE', 'REUSSI')");
        }
        
        boolean result = dao.mettreAJourStatutPaiement(2, "PAY_UPDATE", "PAYE");
        assertTrue(result);
        
        Stationnement misAJour = dao.findById("2");
        assertEquals("PAY_UPDATE", misAJour.getIdPaiement());
        assertEquals("PAYE", misAJour.getStatutPaiement());
    }
    
    @Test
    public void testGetStationnementsAvecFiltres() throws SQLException {
        // Filtrer par statut ACTIF
        List<Stationnement> actifs = dao.getStationnementsAvecFiltres(testUserId, "ACTIF", null, null, null);
        assertEquals(2, actifs.size());
        
        // Filtrer par type VOIRIE
        List<Stationnement> voiries = dao.getStationnementsAvecFiltres(testUserId, null, "VOIRIE", null, null);
        assertEquals(2, voiries.size());
        
        // Filtrer par type PARKING
        List<Stationnement> parkings = dao.getStationnementsAvecFiltres(testUserId, null, "PARKING", null, null);
        assertEquals(1, parkings.size());
        
        // Filtrer par statut TERMINE
        List<Stationnement> termines = dao.getStationnementsAvecFiltres(testUserId, "TERMINE", null, null, null);
        assertEquals(1, termines.size());
    }
    
    @Test
    public void testGetStatistiquesStationnements() throws SQLException {
        Object[] stats = dao.getStatistiquesStationnements(testUserId);
        
        assertNotNull(stats);
        assertEquals(5, stats.length);
        
        int total = (int) stats[0];
        int actifs = (int) stats[1];
        int voirie = (int) stats[2];
        int parking = (int) stats[3];
        double totalCout = (double) stats[4];
        
        assertEquals(3, total);
        assertEquals(2, actifs);
        assertEquals(2, voirie);
        assertEquals(1, parking);
        assertEquals(3.50, totalCout, 0.001);
    }
}
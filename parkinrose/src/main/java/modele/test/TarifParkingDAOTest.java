package modele.test;

import org.junit.Test;

import modele.dao.MySQLConnection;
import modele.dao.TarifParkingDAO;

import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class TarifParkingDAOTest {
    
    private TarifParkingDAO dao;
    private Connection conn;
    
    @Before
    public void setUp() throws SQLException {
        dao = TarifParkingDAO.getInstance();
        conn = MySQLConnection.getConnection();
        
        // Nettoyer et préparer la base de test
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Parking");
            
            // Insérer des parkings de test avec différentes caractéristiques
            stmt.execute("INSERT INTO Parking (id_parking, libelle_parking, adresse_parking, " +
                        "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                        "has_moto, places_moto, places_moto_disponibles, est_relais) " +
                        "VALUES ('PARK_SOIREE', 'Parking Soirée Test', 'Adresse 1', " +
                        "100, 50, 2.0, 1, 0, 0, 0, 0)");
            
            stmt.execute("INSERT INTO Parking (id_parking, libelle_parking, adresse_parking, " +
                        "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                        "has_moto, places_moto, places_moto_disponibles, est_relais) " +
                        "VALUES ('PARK_RELAIS', 'Parking Relais Test', 'Adresse 2', " +
                        "200, 100, 2.5, 0, 0, 0, 0, 1)");
            
            stmt.execute("INSERT INTO Parking (id_parking, libelle_parking, adresse_parking, " +
                        "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                        "has_moto, places_moto, places_moto_disponibles, est_relais) " +
                        "VALUES ('PARK_GRATUIT', 'Parking Gratuit Test', 'Adresse 3', " +
                        "50, 25, 2.0, 0, 0, 0, 0, 0)");
            
            stmt.execute("INSERT INTO Parking (id_parking, libelle_parking, adresse_parking, " +
                        "nombre_places, places_disponibles, hauteur_parking, tarif_soiree, " +
                        "has_moto, places_moto, places_moto_disponibles, est_relais) " +
                        "VALUES ('PARK_STANDARD', 'Parking Standard Test', 'Adresse 4', " +
                        "150, 75, 2.0, 0, 0, 0, 0, 0)");
        }
    }
    
    @After
    public void tearDown() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Parking");
        }
        
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    @Test
    public void testProposeTarifSoiree() throws SQLException {
        boolean proposeSoiree = dao.proposeTarifSoiree("PARK_SOIREE");
        assertTrue(proposeSoiree);
        
        boolean neProposePas = dao.proposeTarifSoiree("PARK_RELAIS");
        assertFalse(neProposePas);
    }
    
    @Test
    public void testEstParkingGratuit() {
        // Note: Cette méthode utilise une liste statique de parkings gratuits
        boolean estGratuit = dao.estParkingGratuit("PARK_VIGUERIE");
        assertTrue(estGratuit);
        
        boolean estPayant = dao.estParkingGratuit("PARK_SOIREE");
        assertFalse(estPayant);
    }
    
    @Test
    public void testEstParkingRelais() throws SQLException {
        boolean estRelais = dao.estParkingRelais("PARK_RELAIS");
        assertTrue(estRelais);
        
        boolean nestPasRelais = dao.estParkingRelais("PARK_SOIREE");
        assertFalse(nestPasRelais);
    }
    
    @Test
    public void testGetParkingsRelais() throws SQLException {
        List<String> parkingsRelais = dao.getParkingsRelais();
        
        assertNotNull(parkingsRelais);
        assertTrue(parkingsRelais.contains("PARK_RELAIS"));
    }
    
    @Test
    public void testTarifSoireeApplicable_CasFavorable() throws SQLException {
        // Arrivée à 19h45, départ à 22h00 (même jour)
        LocalDateTime arrivee = LocalDateTime.of(2024, 1, 1, 19, 45);
        LocalDateTime depart = LocalDateTime.of(2024, 1, 1, 22, 0);
        
        boolean applicable = dao.tarifSoireeApplicable(arrivee, depart, "PARK_SOIREE");
        assertTrue("Tarif soirée devrait s'appliquer pour 19h45-22h00", applicable);
    }
    
    @Test
    public void testTarifSoireeApplicable_CasFavorableLendemain() throws SQLException {
        // Arrivée à 23h00, départ à 02h00 (lendemain)
        LocalDateTime arrivee = LocalDateTime.of(2024, 1, 1, 23, 0);
        LocalDateTime depart = LocalDateTime.of(2024, 1, 2, 2, 0);
        
        boolean applicable = dao.tarifSoireeApplicable(arrivee, depart, "PARK_SOIREE");
        assertTrue(applicable);
    }
    
    @Test
    public void testTarifSoireeApplicable_ArriveeTropTot() throws SQLException {
        // Arrivée à 18h00 (trop tôt pour tarif soirée)
        LocalDateTime arrivee = LocalDateTime.of(2024, 1, 1, 18, 0);
        LocalDateTime depart = LocalDateTime.of(2024, 1, 1, 20, 0);
        
        boolean applicable = dao.tarifSoireeApplicable(arrivee, depart, "PARK_SOIREE");
        assertFalse(applicable);
    }
    
    @Test
    public void testTarifSoireeApplicable_DepartTropTard() throws SQLException {
        // Arrivée à 23h00, départ à 04h00 (trop tard pour tarif soirée)
        LocalDateTime arrivee = LocalDateTime.of(2024, 1, 1, 23, 0);
        LocalDateTime depart = LocalDateTime.of(2024, 1, 2, 4, 0);
        
        boolean applicable = dao.tarifSoireeApplicable(arrivee, depart, "PARK_SOIREE");
        assertFalse(applicable);
    }
    
    @Test
    public void testTarifSoireeApplicable_DureeTropLongue() throws SQLException {
        // Arrivée à 19h30, départ à 05h00 (plus de 8 heures)
        LocalDateTime arrivee = LocalDateTime.of(2024, 1, 1, 19, 30);
        LocalDateTime depart = LocalDateTime.of(2024, 1, 2, 5, 0);
        
        boolean applicable = dao.tarifSoireeApplicable(arrivee, depart, "PARK_SOIREE");
        assertFalse(applicable);
    }
    
    @Test
    public void testEstDansPlageTarifSoiree() {
        // Test à 19h45
        LocalDateTime heure1 = LocalDateTime.of(2024, 1, 1, 19, 45);
        assertTrue(dao.estDansPlageTarifSoiree(heure1));
        
        // Test à 22h30
        LocalDateTime heure2 = LocalDateTime.of(2024, 1, 1, 22, 30);
        assertTrue(dao.estDansPlageTarifSoiree(heure2));
        
        // Test à minuit pile
        LocalDateTime heure3 = LocalDateTime.of(2024, 1, 1, 0, 0);
        assertTrue(dao.estDansPlageTarifSoiree(heure3));
        
        // Test à 19h15 (trop tôt)
        LocalDateTime heure4 = LocalDateTime.of(2024, 1, 1, 19, 15);
        assertFalse(dao.estDansPlageTarifSoiree(heure4));
        
        // Test à 01h30 (en dehors de la plage)
        LocalDateTime heure5 = LocalDateTime.of(2024, 1, 1, 1, 30);
        assertFalse(dao.estDansPlageTarifSoiree(heure5));
    }
    
    @Test
    public void testGetTarifQuartHeure() {
        // Test de différents parkings
        double tarifCapitole = dao.getTarifQuartHeure("PARK_CAPITOLE");
        assertEquals(0.75, tarifCapitole, 0.001); // 3€/h = 0.75€/15min
        
        double tarifEsquirol = dao.getTarifQuartHeure("PARK_ESQUIROL");
        assertEquals(0.63, tarifEsquirol, 0.001); // 2.50€/h = 0.625€/15min
        
        double tarifDefault = dao.getTarifQuartHeure("PARK_INCONNU");
        assertEquals(0.50, tarifDefault, 0.001); // Tarif par défaut 2€/h
    }
    
    @Test
    public void testGetTarifHoraire() {
        double tarifHoraire = dao.getTarifHoraire("PARK_CAPITOLE");
        assertEquals(3.00, tarifHoraire, 0.001); // 0.75 * 4 = 3.00
    }
    
    @Test
    public void testParkingExiste() throws SQLException {
        boolean existe = dao.parkingExiste("PARK_SOIREE");
        assertTrue(existe);
        
        boolean nExistePas = dao.parkingExiste("PARK_INEXISTANT");
        assertFalse(nExistePas);
    }
    
    @Test
    public void testGetInfosParking() throws SQLException {
        Map<String, Object> infos = dao.getInfosParking("PARK_SOIREE");
        
        assertNotNull(infos);
        assertEquals("Parking Soirée Test", infos.get("libelle"));
        assertEquals(100, infos.get("places"));
        assertEquals(2.0, (double) infos.get("hauteur"), 0.001);
        assertTrue((boolean) infos.get("tarif_soiree"));
        assertFalse((boolean) infos.get("gratuit"));
        assertFalse((boolean) infos.get("relais"));
    }
    
    @Test
    public void testFormaterAffichageTarifs() throws SQLException {
        String affichage = dao.formaterAffichageTarifs("PARK_SOIREE");
        assertNotNull(affichage);
        assertTrue(affichage.contains("Tarif soirée disponible"));
        assertTrue(affichage.contains("5.90€"));
    }
}
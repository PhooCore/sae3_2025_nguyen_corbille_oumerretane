package modele.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    AbonnementTest.class,
    PaiementTest.class,
    ParkingTest.class,
    StationnementTest.class,
    UsagerTest.class,
    VehiculeUsagerTest.class,
    ZoneTest.class,
    AbonnementDAOTest.class,
    ParkingDAOTest.class,
    PaiementDAOTest.class,
    StationnementDAOTest.class,
    UsagerDAOTest.class,
    TarifParkingDAOTest.class,
    ModifMdpDAOTest.class
})
public class AllTests {
    // Classe conteneur pour exécuter tous les tests
}
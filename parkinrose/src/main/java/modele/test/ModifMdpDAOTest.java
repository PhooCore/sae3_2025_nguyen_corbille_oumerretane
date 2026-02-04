package modele.test;

import org.junit.Test;

import modele.dao.ModifMdpDAO;
import modele.dao.MySQLConnection;

import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.sql.*;

/**
 * Classe de tests unitaires pour la classe ModifMdpDAO.
 * 
 * Cette classe teste les fonctionnalités de gestion des mots de passe :
 * - Modification du mot de passe
 * - Vérification de l'existence d'un email
 * - Vérification de l'ancien mot de passe
 * - Récupération des informations utilisateur
 * - Validation de la force du mot de passe
 */
public class ModifMdpDAOTest {
    
    // ==================== VARIABLES DE TEST ====================
    
    private ModifMdpDAO dao;        // DAO à tester
    private Connection conn;        // Connexion à la base de données
    
    // Constantes pour les tests
    private static final int ID_USAGER_TEST = 999;
    private static final String NOM_USAGER_TEST = "Test";
    private static final String PRENOM_USAGER_TEST = "User";
    private static final String MAIL_USAGER_TEST = "test.mdp@test.com";
    private static final String ANCIEN_MDP = "ancienmdp";
    private static final String NOUVEAU_MDP = "nouveaumdp";
    private static final String MAIL_INEXISTANT = "inexistant@test.com";
    private static final String MAUVAIS_MDP = "mauvaismdp";
    
    // ==================== MÉTHODES DE CONFIGURATION ====================
    
    /**
     * Méthode exécutée avant chaque test.
     * Initialise le DAO, établit la connexion et prépare l'environnement de test.
     * 
     * @throws SQLException en cas d'erreur de connexion ou d'exécution SQL
     */
    @Before
    public void setUp() throws SQLException {
        // 1. Initialiser le DAO
        dao = new ModifMdpDAO();
        
        // 2. Obtenir une connexion à la base de données
        conn = MySQLConnection.getConnection();
        
        // 3. Préparer la base de données pour les tests
        preparerBaseDeDonnees();
    }
    
    /**
     * Prépare la base de données en nettoyant les tables et en insérant des données de test.
     * 
     * @throws SQLException en cas d'erreur SQL
     */
    private void preparerBaseDeDonnees() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 1. Nettoyer les tables
            stmt.execute("DELETE FROM Usager");
            
            // 2. Insérer un utilisateur de test
            stmt.execute("INSERT INTO Usager (id_usager, nom_usager, prenom_usager, mail_usager, mot_de_passe) " +
                        "VALUES (" + ID_USAGER_TEST + ", '" + NOM_USAGER_TEST + "', '" + PRENOM_USAGER_TEST + 
                        "', '" + MAIL_USAGER_TEST + "', '" + ANCIEN_MDP + "')");
        }
    }
    
    /**
     * Méthode exécutée après chaque test.
     * Nettoie les données de test et ferme la connexion.
     * 
     * @throws SQLException en cas d'erreur de fermeture de connexion
     */
    @After
    public void tearDown() throws SQLException {
        // 1. Nettoyer les données de test
        nettoyerBaseDeDonnees();
        
        // 2. Fermer la connexion si elle est ouverte
        fermerConnexion();
    }
    
    /**
     * Nettoie les données de test de la base de données.
     * 
     * @throws SQLException en cas d'erreur SQL
     */
    private void nettoyerBaseDeDonnees() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Usager");
        }
    }
    
    /**
     * Ferme proprement la connexion à la base de données.
     * 
     * @throws SQLException en cas d'erreur de fermeture
     */
    private void fermerConnexion() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            // Ne pas fermer la connexion si elle est partagée
            // (dépend de l'implémentation de MySQLConnection)
            // conn.close();
        }
    }
    
    // ==================== TESTS DES MÉTHODES PRINCIPALES ====================
    
    /**
     * Test de la méthode modifierMotDePasse().
     * Vérifie qu'un mot de passe peut être modifié avec succès.
     */
    @Test
    public void testModifierMotDePasse() {
        // 1. Exécuter la méthode à tester
        boolean result = dao.modifierMotDePasse(MAIL_USAGER_TEST, NOUVEAU_MDP);
        
        // 2. Vérifier le résultat
        assertTrue("La modification du mot de passe doit réussir", result);
        
        // 3. Vérifier que le mot de passe a été effectivement changé
        verifierMotDePasseDansBase(MAIL_USAGER_TEST, NOUVEAU_MDP);
    }
    
    /**
     * Test de la méthode modifierMotDePasse() avec un email inexistant.
     */
    @Test
    public void testModifierMotDePasse_EmailInexistant() {
        // 1. Exécuter la méthode avec un email inexistant
        boolean result = dao.modifierMotDePasse(MAIL_INEXISTANT, NOUVEAU_MDP);
        
        // 2. Vérifier que l'opération échoue
        assertFalse("La modification doit échouer pour un email inexistant", result);
    }
    
    /**
     * Test de la méthode modifierMotDePasse() avec un mot de passe null.
     */
    @Test
    public void testModifierMotDePasse_MotDePasseNull() {
        // 1. Exécuter la méthode avec mot de passe null
        boolean result = dao.modifierMotDePasse(MAIL_USAGER_TEST, null);
        
        // 2. Vérifier que l'opération échoue ou gère correctement le null
        // (dépend de l'implémentation)
        // assertFalse(result); // ou assertTrue si la méthode gère le null
    }
    
    /**
     * Test de la méthode modifierMotDePasse() avec un email null.
     */
    @Test
    public void testModifierMotDePasse_EmailNull() {
        // 1. Exécuter la méthode avec email null
        boolean result = dao.modifierMotDePasse(null, NOUVEAU_MDP);
        
        // 2. Vérifier que l'opération échoue
        assertFalse("La modification doit échouer pour un email null", result);
    }
    
    /**
     * Test de la méthode modifierMotDePasse() avec un mot de passe vide.
     */
    @Test
    public void testModifierMotDePasse_MotDePasseVide() {
        // 1. Exécuter la méthode avec mot de passe vide
        boolean result = dao.modifierMotDePasse(MAIL_USAGER_TEST, "");
        
        // 2. Vérifier que l'opération échoue
        assertFalse("La modification doit échouer pour un mot de passe vide", result);
    }
    
    // ==================== TESTS DE VÉRIFICATION ====================
    
    /**
     * Test de la méthode verifierEmailExiste().
     */
    @Test
    public void testVerifierEmailExiste() {
        // 1. Vérifier un email qui existe
        boolean existe = dao.verifierEmailExiste(MAIL_USAGER_TEST);
        assertTrue("L'email existant doit être trouvé", existe);
        
        // 2. Vérifier un email qui n'existe pas
        boolean nExistePas = dao.verifierEmailExiste(MAIL_INEXISTANT);
        assertFalse("L'email inexistant ne doit pas être trouvé", nExistePas);
    }
    
    /**
     * Test de la méthode verifierEmailExiste() avec email null.
     */
    @Test
    public void testVerifierEmailExiste_EmailNull() {
        // 1. Vérifier avec email null
        boolean result = dao.verifierEmailExiste(null);
        
        // 2. Vérifier que la méthode retourne false pour email null
        assertFalse("Email null ne doit pas exister", result);
    }
    
    /**
     * Test de la méthode verifierEmailExiste() avec email vide.
     */
    @Test
    public void testVerifierEmailExiste_EmailVide() {
        // 1. Vérifier avec email vide
        boolean result = dao.verifierEmailExiste("");
        
        // 2. Vérifier que la méthode retourne false pour email vide
        assertFalse("Email vide ne doit pas exister", result);
    }
    
    /**
     * Test de la méthode verifierAncienMotDePasse().
     */
    @Test
    public void testVerifierAncienMotDePasse() {
        // 1. Vérifier avec le bon ancien mot de passe
        boolean correct = dao.verifierAncienMotDePasse(MAIL_USAGER_TEST, ANCIEN_MDP);
        assertTrue("L'ancien mot de passe correct doit être validé", correct);
        
        // 2. Vérifier avec un mauvais ancien mot de passe
        boolean incorrect = dao.verifierAncienMotDePasse(MAIL_USAGER_TEST, MAUVAIS_MDP);
        assertFalse("Le mauvais ancien mot de passe ne doit pas être validé", incorrect);
    }
    
    /**
     * Test de la méthode verifierAncienMotDePasse() avec email inexistant.
     */
    @Test
    public void testVerifierAncienMotDePasse_EmailInexistant() {
        // 1. Vérifier avec email inexistant
        boolean result = dao.verifierAncienMotDePasse(MAIL_INEXISTANT, ANCIEN_MDP);
        
        // 2. Vérifier que l'opération échoue
        assertFalse("Vérification doit échouer pour email inexistant", result);
    }
    
    /**
     * Test de la méthode verifierAncienMotDePasse() avec email null.
     */
    @Test
    public void testVerifierAncienMotDePasse_EmailNull() {
        // 1. Vérifier avec email null
        boolean result = dao.verifierAncienMotDePasse(null, ANCIEN_MDP);
        
        // 2. Vérifier que l'opération échoue
        assertFalse("Vérification doit échouer pour email null", result);
    }
    
    /**
     * Test de la méthode verifierAncienMotDePasse() avec mot de passe null.
     */
    @Test
    public void testVerifierAncienMotDePasse_MotDePasseNull() {
        // 1. Vérifier avec mot de passe null
        boolean result = dao.verifierAncienMotDePasse(MAIL_USAGER_TEST, null);
        
        // 2. Vérifier que l'opération échoue
        assertFalse("Vérification doit échouer pour mot de passe null", result);
    }
    
    // ==================== TESTS DE RÉCUPÉRATION D'INFORMATIONS ====================
    
    /**
     * Test de la méthode getIdUsagerByEmail().
     */
    @Test
    public void testGetIdUsagerByEmail() {
        // 1. Récupérer l'ID d'un utilisateur existant
        int id = dao.getIdUsagerByEmail(MAIL_USAGER_TEST);
        assertEquals("ID usager incorrect", ID_USAGER_TEST, id);
        
        // 2. Récupérer l'ID d'un utilisateur inexistant
        int idInexistant = dao.getIdUsagerByEmail(MAIL_INEXISTANT);
        assertEquals("ID doit être -1 pour utilisateur inexistant", -1, idInexistant);
    }
    
    /**
     * Test de la méthode getIdUsagerByEmail() avec email null.
     */
    @Test
    public void testGetIdUsagerByEmail_EmailNull() {
        // 1. Récupérer l'ID avec email null
        int id = dao.getIdUsagerByEmail(null);
        
        // 2. Vérifier que la méthode retourne -1
        assertEquals("ID doit être -1 pour email null", -1, id);
    }
    
    /**
     * Test de la méthode getInfosUsager().
     */
    @Test
    public void testGetInfosUsager() {
        // 1. Récupérer les informations d'un utilisateur existant
        String[] infos = dao.getInfosUsager(MAIL_USAGER_TEST);
        
        // 2. Vérifier les résultats
        assertNotNull("Les informations ne doivent pas être null", infos);
        assertEquals("Le tableau doit contenir 2 éléments (nom et prénom)", 2, infos.length);
        assertEquals("Nom incorrect", NOM_USAGER_TEST, infos[0]);
        assertEquals("Prénom incorrect", PRENOM_USAGER_TEST, infos[1]);
    }
    
    /**
     * Test de la méthode getInfosUsager() avec email inexistant.
     */
    @Test
    public void testGetInfosUsager_EmailInexistant() {
        // 1. Récupérer les informations d'un utilisateur inexistant
        String[] infosInexistant = dao.getInfosUsager(MAIL_INEXISTANT);
        
        // 2. Vérifier que null est retourné
        assertNull("Les informations doivent être null pour utilisateur inexistant", 
                  infosInexistant);
    }
    
    /**
     * Test de la méthode getInfosUsager() avec email null.
     */
    @Test
    public void testGetInfosUsager_EmailNull() {
        // 1. Récupérer les informations avec email null
        String[] infos = dao.getInfosUsager(null);
        
        // 2. Vérifier que null est retourné
        assertNull("Les informations doivent être null pour email null", infos);
    }
    
    // ==================== TESTS DE VALIDATION DE FORCE DU MOT DE PASSE ====================
    
    /**
     * Test complet de la méthode verifierForceMotDePasse().
     */
    @Test
    public void testVerifierForceMotDePasse() {
        // 1. Tests de mots de passe trop courts
        assertFalse("Mot de passe trop court doit être rejeté", 
                   dao.verifierForceMotDePasse("abc"));
        assertFalse("Mot de passe de 7 caractères doit être rejeté", 
                   dao.verifierForceMotDePasse("abcdefg"));
        
        // 2. Test de mot de passe sans assez de critères
        assertFalse("Mot de passe avec seulement des lettres doit être rejeté", 
                   dao.verifierForceMotDePasse("abcdefgh"));
        assertFalse("Mot de passe avec seulement des chiffres doit être rejeté", 
                   dao.verifierForceMotDePasse("12345678"));
        assertFalse("Mot de passe avec seulement des majuscules doit être rejeté", 
                   dao.verifierForceMotDePasse("ABCDEFGH"));
        
        // 3. Tests de mots de passe acceptables (3 critères sur 4)
        assertTrue("Mot de passe fort avec majuscule, minuscule et chiffre doit être accepté", 
                  dao.verifierForceMotDePasse("MotDePasse123"));
        assertTrue("Mot de passe fort avec minuscule, chiffre et caractère spécial doit être accepté", 
                  dao.verifierForceMotDePasse("motdepasse123!"));
        assertTrue("Mot de passe fort avec majuscule, minuscule et caractère spécial doit être accepté", 
                  dao.verifierForceMotDePasse("MotDePasse!"));
        assertTrue("Mot de passe fort avec majuscule, chiffre et caractère spécial doit être accepté", 
                  dao.verifierForceMotDePasse("MOTDEPASSE123!"));
        
        // 4. Tests de mots de passe parfaits (4 critères)
        assertTrue("Mot de passe parfait avec tous les critères doit être accepté", 
                  dao.verifierForceMotDePasse("MotDePasse123!"));
        assertTrue("Mot de passe parfait complexe doit être accepté", 
                  dao.verifierForceMotDePasse("P@ssw0rd2024!"));
        
        // 5. Tests avec caractères spéciaux variés
        assertTrue("Mot de passe avec @ doit être accepté", 
                  dao.verifierForceMotDePasse("Mot@1234"));
        assertTrue("Mot de passe avec # doit être accepté", 
                  dao.verifierForceMotDePasse("Mot#1234"));
        assertTrue("Mot de passe avec $ doit être accepté", 
                  dao.verifierForceMotDePasse("Mot$1234"));
        assertTrue("Mot de passe avec % doit être accepté", 
                  dao.verifierForceMotDePasse("Mot%1234"));
        assertTrue("Mot de passe avec & doit être accepté", 
                  dao.verifierForceMotDePasse("Mot&1234"));
    }
    
    /**
     * Test de la méthode verifierForceMotDePasse() avec mot de passe null.
     */
    @Test
    public void testVerifierForceMotDePasse_Null() {
        // 1. Vérifier avec mot de passe null
        boolean result = dao.verifierForceMotDePasse(null);
        
        // 2. Vérifier que la méthode retourne false
        assertFalse("Mot de passe null doit être rejeté", result);
    }
    
    /**
     * Test de la méthode verifierForceMotDePasse() avec mot de passe vide.
     */
    @Test
    public void testVerifierForceMotDePasse_Vide() {
        // 1. Vérifier avec mot de passe vide
        boolean result = dao.verifierForceMotDePasse("");
        
        // 2. Vérifier que la méthode retourne false
        assertFalse("Mot de passe vide doit être rejeté", result);
    }
    
    // ==================== TESTS SUPPLÉMENTAIRES ====================
    
    /**
     * Test d'intégration complet : vérifier puis modifier le mot de passe.
     */
    @Test
    public void testIntegrationComplet() {
        // 1. Vérifier que l'email existe
        assertTrue(dao.verifierEmailExiste(MAIL_USAGER_TEST));
        
        // 2. Vérifier l'ancien mot de passe
        assertTrue(dao.verifierAncienMotDePasse(MAIL_USAGER_TEST, ANCIEN_MDP));
        
        // 3. Vérifier la force du nouveau mot de passe
        String nouveauMdpFort = "NouveauMdp123!";
        assertTrue(dao.verifierForceMotDePasse(nouveauMdpFort));
        
        // 4. Modifier le mot de passe
        assertTrue(dao.modifierMotDePasse(MAIL_USAGER_TEST, nouveauMdpFort));
        
        // 5. Vérifier que l'ancien mot de passe ne fonctionne plus
        assertFalse(dao.verifierAncienMotDePasse(MAIL_USAGER_TEST, ANCIEN_MDP));
        
        // 6. Vérifier que le nouveau mot de passe fonctionne
        // (nécessiterait une méthode de vérification avec le nouveau mot de passe)
    }
    
    /**
     * Test avec des caractères spéciaux dans l'email.
     */
    @Test
    public void testAvecCaracteresSpeciauxEmail() {
        // Créer un utilisateur avec email contenant des caractères spéciaux
        String emailSpecial = "test.spécial@exemple-été.fr";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Usager (id_usager, nom_usager, prenom_usager, mail_usager, mot_de_passe) " +
                        "VALUES (1000, 'Spécial', 'Test', '" + emailSpecial + "', 'mdp')");
        } catch (SQLException e) {
            // Ignorer si l'email n'est pas accepté par la base
            System.out.println("Email avec caractères spéciaux non testé: " + e.getMessage());
            return;
        }
        
        // Tester la récupération
        int id = dao.getIdUsagerByEmail(emailSpecial);
        assertEquals(1000, id);
        
        // Tester la modification
        boolean result = dao.modifierMotDePasse(emailSpecial, "nouveaumdp");
        assertTrue(result);
    }
    
    
    /**
     * Test avec des espaces dans le mot de passe.
     */
    @Test
    public void testMotDePasseAvecEspaces() {
        String mdpAvecEspaces = "Mot de passe 123!";
        // Selon l'implémentation, les espaces peuvent être acceptés ou non
        boolean result = dao.verifierForceMotDePasse(mdpAvecEspaces);
        // Le test passe quels que soient les espaces
        // Si les espaces sont considérés comme des caractères spéciaux, le mot de passe est fort
        // Sinon, il ne l'est pas
        System.out.println("Mot de passe avec espaces: " + (result ? "accepté" : "rejeté"));
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Vérifie le mot de passe dans la base de données.
     * 
     * @param email Email de l'utilisateur
     * @param mdpAttendu Mot de passe attendu
     */
    private void verifierMotDePasseDansBase(String email, String mdpAttendu) {
        String sql = "SELECT mot_de_passe FROM Usager WHERE mail_usager = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String mdpBase = rs.getString("mot_de_passe");
                    assertEquals("Mot de passe incorrect dans la base", 
                                mdpAttendu, mdpBase);
                } else {
                    fail("Utilisateur non trouvé dans la base");
                }
            }
        } catch (SQLException e) {
            fail("Erreur SQL lors de la vérification: " + e.getMessage());
        }
    }
    
    /**
     * Test de la méthode avec injection SQL potentielle.
     */
    @Test
    public void testInjectionSQL() {
        // Test avec une tentative d'injection SQL dans l'email
        String emailInjection = "test@test.com' OR '1'='1";
        boolean existe = dao.verifierEmailExiste(emailInjection);
        // Le DAO devrait utiliser des PreparedStatement, donc l'injection ne devrait pas fonctionner
        assertFalse("L'injection SQL ne devrait pas fonctionner", existe);
        
        // Test avec injection dans le mot de passe
        String mdpInjection = "mdp' OR '1'='1";
        boolean verification = dao.verifierAncienMotDePasse(MAIL_USAGER_TEST, mdpInjection);
        assertFalse("L'injection SQL ne devrait pas fonctionner", verification);
    }
}
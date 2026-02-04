package modele.test;

import modele.Feedback;
import modele.dao.FeedbackDAO;
import org.junit.*;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe de tests unitaires pour la classe FeedbackDAO et le modèle Feedback.
 * 
 * Cette classe teste :
 * 1. Le pattern Singleton de FeedbackDAO
 * 2. L'existence et l'accessibilité des méthodes
 * 3. Les fonctionnalités du modèle Feedback
 * 4. Les cas limites et les validations
 */
public class FeedbackDAOTest {
    
    // ==================== TESTS DU PATTERN SINGLETON ====================
    
    /**
     * Test du pattern Singleton pour FeedbackDAO.
     * Vérifie qu'une seule instance est créée et qu'elle est partagée.
     */
    @Test
    public void testSingleton() {
        // Récupérer deux instances
        FeedbackDAO instance1 = FeedbackDAO.getInstance();
        FeedbackDAO instance2 = FeedbackDAO.getInstance();
        
        // Vérifications
        assertNotNull("Instance 1 ne doit pas être null", instance1);
        assertNotNull("Instance 2 ne doit pas être null", instance2);
        assertSame("Les deux références doivent pointer vers la même instance", 
                  instance1, instance2);
    }
    
    /**
     * Test de l'accessibilité du constructeur.
     * Le constructeur devrait être privé pour respecter le pattern Singleton.
     * 
     * @throws Exception en cas d'erreur de réflexion
     */
    @Test
    public void testConstructeurAccessibilite() throws Exception {
        // Obtenir le constructeur par réflexion
        Constructor<FeedbackDAO> constructor = FeedbackDAO.class.getDeclaredConstructor();
        
        // Vérifier que le constructeur n'est pas public (idéalement privé)
        int modifiers = constructor.getModifiers();
        assertFalse("Le constructeur ne devrait pas être public", 
                   Modifier.isPublic(modifiers));
        
        // Vérifier qu'il n'est pas protégé non plus (idéalement privé)
        assertFalse("Le constructeur ne devrait pas être protected", 
                   Modifier.isProtected(modifiers));
        
        // Le rendre accessible pour pouvoir l'instancier (pour le test)
        constructor.setAccessible(true);
        
        // Instancier une nouvelle instance
        FeedbackDAO instance = constructor.newInstance();
        assertNotNull("L'instance créée par réflexion ne doit pas être null", instance);
        
        // Vérifier que c'est bien une instance de FeedbackDAO
        assertTrue("L'instance doit être de type FeedbackDAO", 
                  instance instanceof FeedbackDAO);
    }
    
    // ==================== TESTS DE L'EXISTENCE DES MÉTHODES ====================
    
    /**
     * Test de l'existence des méthodes principales de FeedbackDAO.
     * Vérifie que toutes les méthodes déclarées existent bien.
     */
    @Test
    public void testMethodesExistent() {
        FeedbackDAO dao = FeedbackDAO.getInstance();
        assertNotNull("Le DAO ne doit pas être null", dao);
        
        try {
            // Vérifier l'existence de chaque méthode par réflexion
            Method[] methods = FeedbackDAO.class.getDeclaredMethods();
            assertTrue("FeedbackDAO doit avoir des méthodes", methods.length > 0);
            
            // Vérifier les méthodes spécifiques (à adapter selon l'implémentation réelle)
            FeedbackDAO.class.getMethod("getInstance");
            FeedbackDAO.class.getMethod("envoyerFeedback", int.class, String.class, String.class);
            FeedbackDAO.class.getMethod("getFeedbackById", int.class);
            FeedbackDAO.class.getMethod("mettreAJourStatut", int.class, String.class, boolean.class);
            FeedbackDAO.class.getMethod("repondreFeedback", int.class, int.class, String.class);
            FeedbackDAO.class.getMethod("getFeedbacksByUser", int.class);
            FeedbackDAO.class.getMethod("getAllFeedbacksWithInfo");
            FeedbackDAO.class.getMethod("getNombreNouveauxFeedbacks");
            FeedbackDAO.class.getMethod("supprimerFeedback", int.class);
            FeedbackDAO.class.getMethod("getFeedbacksByStatut", String.class);
            FeedbackDAO.class.getMethod("getAllParentFeedbacks");
            FeedbackDAO.class.getMethod("getReponsesFeedback", int.class);
            
        } catch (NoSuchMethodException e) {
            fail("Méthode manquante dans FeedbackDAO: " + e.getMessage());
        }
    }
    
    // ==================== TESTS DU MODÈLE FEEDBACK ====================
    
    /**
     * Test complet du modèle Feedback avec tous ses attributs.
     */
    @Test
    public void testFeedbackModelComplet() {
        // Création d'un feedback avec le constructeur principal
        Feedback feedback = new Feedback(1, "Problème de connexion", "Je ne peux pas me connecter");
        
        // Vérifier les valeurs initiales définies par le constructeur
        assertEquals("ID usager incorrect", 1, feedback.getIdUsager());
        assertEquals("Sujet incorrect", "Problème de connexion", feedback.getSujet());
        assertEquals("Message incorrect", "Je ne peux pas me connecter", feedback.getMessage());
        assertEquals("Statut initial incorrect", "NOUVEAU", feedback.getStatut());
        assertFalse("Gotanswer initial doit être false", feedback.isGotanswer());
        assertNotNull("Date de création ne doit pas être null", feedback.getDateCreation());
        
        // Tester les setters
        feedback.setIdFeedback(100);
        assertEquals("ID feedback incorrect", 100, feedback.getIdFeedback());
        
        feedback.setStatut("EN_COURS");
        assertEquals("Statut incorrect", "EN_COURS", feedback.getStatut());
        
        feedback.setGotanswer(true);
        assertTrue("Gotanswer doit être true", feedback.isGotanswer());
        
        feedback.setIdAdminReponse(10);
        assertEquals("ID admin réponse incorrect", Integer.valueOf(10), feedback.getIdAdminReponse());
        
        feedback.setReponse("Nous traitons votre demande");
        assertEquals("Réponse incorrecte", "Nous traitons votre demande", feedback.getReponse());
        
        // Tester les méthodes utilitaires
        assertTrue("Feedback doit être marqué comme répondu", feedback.isRepondu());
        
        feedback.setIdFeedbackParent(50);
        assertFalse("Feedback ne doit pas être un message parent", feedback.estUnMessageParent());
        
        feedback.setIdFeedbackParent(null);
        assertTrue("Feedback doit être un message parent", feedback.estUnMessageParent());
    }
    
    /**
     * Test du constructeur par défaut de Feedback.
     */
    @Test
    public void testConstructeurParDefaut() {
        Feedback feedback = new Feedback();
        
        // Vérifier les valeurs par défaut
        assertEquals("ID usager par défaut doit être 0", 0, feedback.getIdUsager());
        assertNull("Sujet par défaut doit être null", feedback.getSujet());
        assertNull("Message par défaut doit être null", feedback.getMessage());
        assertNull("Statut par défaut doit être null", feedback.getStatut());
        assertNull("Date création par défaut doit être null", feedback.getDateCreation());
    }
    
    // ==================== TESTS DES MÉTHODES UTILITAIRES ====================
    
    /**
     * Test de la méthode getNomCompletUsager().
     * Vérifie les différents cas de figure pour l'affichage du nom.
     */
    @Test
    public void testNomCompletUsager() {
        Feedback feedback = new Feedback();
        feedback.setIdUsager(123);
        
        // Cas 1: Sans nom ni prénom
        assertEquals("Format sans nom/prénom incorrect", 
                    "Utilisateur #123", feedback.getNomCompletUsager());
        
        // Cas 2: Avec nom seulement
        feedback.setNomUsager("Martin");
        assertEquals("Format avec nom seulement incorrect", 
                    "Utilisateur #123", feedback.getNomCompletUsager());
        
        // Cas 3: Avec prénom seulement
        feedback.setNomUsager(null);
        feedback.setPrenomUsager("Julie");
        assertEquals("Format avec prénom seulement incorrect", 
                    "Utilisateur #123", feedback.getNomCompletUsager());
        
        // Cas 4: Avec nom et prénom complets
        feedback.setNomUsager("Martin");
        feedback.setPrenomUsager("Julie");
        assertEquals("Format avec nom et prénom incorrect", 
                    "Julie Martin", feedback.getNomCompletUsager());
        
        // Cas 5: Avec chaînes vides
        feedback.setNomUsager("");
        feedback.setPrenomUsager("");
        assertEquals("Format avec chaînes vides incorrect", 
                    "Utilisateur #123", feedback.getNomCompletUsager());
        
        // Cas 6: Avec espaces
        feedback.setNomUsager("  Martin  ");
        feedback.setPrenomUsager("  Julie  ");
        assertEquals("Format avec espaces incorrect", 
                    "Julie Martin", feedback.getNomCompletUsager());
    }
    
    /**
     * Test de la méthode getNomCompletAdminReponse().
     * Vérifie les différents cas pour l'affichage du nom de l'admin.
     */
    @Test
    public void testNomCompletAdminReponse() {
        Feedback feedback = new Feedback();
        
        // Cas 1: Sans admin
        assertNull("Nom admin doit être null sans admin", feedback.getNomCompletAdminReponse());
        
        // Cas 2: Avec ID admin mais sans nom
        feedback.setIdAdminReponse(50);
        assertEquals("Format avec ID admin seulement incorrect", 
                    "Admin #50", feedback.getNomCompletAdminReponse());
        
        // Cas 3: Avec nom seulement
        feedback.setNomAdminReponse("Admin");
        assertEquals("Format avec nom admin seulement incorrect", 
                    "Admin #50", feedback.getNomCompletAdminReponse());
        
        // Cas 4: Avec prénom seulement
        feedback.setNomAdminReponse(null);
        feedback.setPrenomAdminReponse("System");
        assertEquals("Format avec prénom admin seulement incorrect", 
                    "Admin #50", feedback.getNomCompletAdminReponse());
        
        // Cas 5: Avec nom et prénom complets
        feedback.setNomAdminReponse("Admin");
        feedback.setPrenomAdminReponse("System");
        assertEquals("Format avec nom et prénom admin incorrect", 
                    "System Admin", feedback.getNomCompletAdminReponse());
        
        // Cas 6: ID admin null
        feedback.setIdAdminReponse(null);
        assertNull("Nom admin doit être null avec ID admin null", 
                  feedback.getNomCompletAdminReponse());
    }
    
    // ==================== TESTS DE LA MÉTHODE toString() ====================
    
    /**
     * Test de la méthode toString().
     * Vérifie que la représentation textuelle contient les informations importantes.
     */
    @Test
    public void testToString() {
        Feedback feedback = new Feedback(1, "Test sujet", "Test message");
        feedback.setIdFeedback(99);
        
        String toString = feedback.toString();
        
        // Vérifier la présence des informations clés
        assertTrue("Doit contenir l'ID", toString.contains("id=99"));
        assertTrue("Doit contenir le sujet", toString.contains("sujet='Test sujet'"));
        assertTrue("Doit contenir le statut", toString.contains("statut='NOUVEAU'"));
        assertTrue("Doit contenir la date", toString.contains("date="));
        
        // Vérifier le format général
        assertTrue("Doit commencer par 'Feedback{'", toString.startsWith("Feedback{"));
        assertTrue("Doit se terminer par '}'", toString.endsWith("}"));
    }
    
    /**
     * Test de toString() avec différents statuts.
     */
    @Test
    public void testToStringAvecDifferentStatuts() {
        Feedback feedback = new Feedback(1, "Sujet", "Message");
        feedback.setIdFeedback(100);
        
        feedback.setStatut("EN_COURS");
        assertTrue(feedback.toString().contains("statut='EN_COURS'"));
        
        feedback.setStatut("RESOLU");
        assertTrue(feedback.toString().contains("statut='RESOLU'"));
        
        feedback.setStatut("FERME");
        assertTrue(feedback.toString().contains("statut='FERME'"));
    }
    
    // ==================== TESTS DES STATUTS ====================
    
    /**
     * Test de la validité des différents statuts possibles.
     */
    @Test
    public void testStatutsValides() {
        Feedback feedback = new Feedback(1, "Test", "Message");
        
        // Test des différents statuts standard
        String[] statutsValides = {"NOUVEAU", "EN_COURS", "RESOLU", "FERME", "EN_ATTENTE"};
        
        for (String statut : statutsValides) {
            feedback.setStatut(statut);
            assertEquals("Statut '" + statut + "' non conservé", 
                        statut, feedback.getStatut());
        }
        
        // Test avec statut personnalisé
        feedback.setStatut("CUSTOM_STATUS");
        assertEquals("Statut personnalisé non conservé", 
                    "CUSTOM_STATUS", feedback.getStatut());
        
        // Test avec statut null
        feedback.setStatut(null);
        assertNull("Statut null non conservé", feedback.getStatut());
    }
    
    // ==================== TESTS DES MÉTHODES BOOLÉENNES ====================
    
    /**
     * Test de la méthode isRepondu().
     * Vérifie les différentes conditions qui indiquent qu'un feedback a une réponse.
     */
    @Test
    public void testIsReponduMethod() {
        Feedback feedback = new Feedback(1, "Test", "Message");
        
        // Cas 1: Initialement non répondu
        assertFalse("Feedback neuf ne doit pas être répondu", feedback.isRepondu());
        
        // Cas 2: Répondu via gotanswer
        feedback.setGotanswer(true);
        assertTrue("Feedback avec gotanswer=true doit être répondu", feedback.isRepondu());
        
        // Cas 3: Répondu via idAdminReponse
        feedback.setGotanswer(false);
        feedback.setIdAdminReponse(10);
        assertTrue("Feedback avec idAdminReponse doit être répondu", feedback.isRepondu());
        
        // Cas 4: Répondu via les deux
        feedback.setGotanswer(true);
        feedback.setIdAdminReponse(10);
        assertTrue("Feedback avec les deux indicateurs doit être répondu", feedback.isRepondu());
        
        // Cas 5: ID admin réponse à 0 (devrait être null pour non répondu)
        feedback.setGotanswer(false);
        feedback.setIdAdminReponse(0);
        assertFalse("Feedback avec idAdminReponse=0 ne doit pas être répondu", feedback.isRepondu());
    }
    
    /**
     * Test de la méthode estUnMessageParent().
     * Vérifie si un feedback est un message initial ou une réponse.
     */
    @Test
    public void testEstUnMessageParent() {
        Feedback feedback = new Feedback();
        
        // Cas 1: Initialement un message parent (pas de parent)
        assertTrue("Feedback sans parent doit être un message parent", 
                  feedback.estUnMessageParent());
        
        // Cas 2: Définir un parent -> devient un message enfant
        feedback.setIdFeedbackParent(100);
        assertFalse("Feedback avec parent ne doit pas être un message parent", 
                   feedback.estUnMessageParent());
        
        // Cas 3: Retirer le parent -> redevient un message parent
        feedback.setIdFeedbackParent(null);
        assertTrue("Feedback sans parent doit être un message parent", 
                  feedback.estUnMessageParent());
        
        // Cas 4: Parent à 0 (traité comme null)
        feedback.setIdFeedbackParent(0);
        assertTrue("Feedback avec parent=0 doit être un message parent", 
                  feedback.estUnMessageParent());
    }
    
    // ==================== TESTS SUPPLÉMENTAIRES ====================
    
    /**
     * Test de l'indépendance des instances Feedback.
     */
    @Test
    public void testMultipleInstancesFeedback() {
        // Créer plusieurs instances indépendantes
        Feedback feedback1 = new Feedback(1, "Sujet 1", "Message 1");
        Feedback feedback2 = new Feedback(2, "Sujet 2", "Message 2");
        
        // Vérifier qu'elles sont bien distinctes
        assertNotSame("Les instances doivent être différentes", feedback1, feedback2);
        assertEquals("ID usager différent", 1, feedback1.getIdUsager());
        assertEquals("ID usager différent", 2, feedback2.getIdUsager());
        assertEquals("Sujet différent", "Sujet 1", feedback1.getSujet());
        assertEquals("Sujet différent", "Sujet 2", feedback2.getSujet());
        
        // Modifier l'une ne doit pas affecter l'autre
        feedback1.setStatut("MODIFIE");
        assertEquals("Statut modifié", "MODIFIE", feedback1.getStatut());
        assertEquals("Statut inchangé", "NOUVEAU", feedback2.getStatut());
    }
    
    /**
     * Test des attributs temporels.
     */
    @Test
    public void testFeedbackAvecDateReponse() {
        Feedback feedback = new Feedback();
        
        // Sans date de réponse
        assertNull("Date réponse initiale doit être null", feedback.getDateReponse());
        
        // Avec date de réponse
        LocalDateTime now = LocalDateTime.now();
        feedback.setDateReponse(now);
        assertEquals("Date réponse incorrecte", now, feedback.getDateReponse());
        
        // Test formatage pour affichage
        String dateFormatee = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assertNotNull("Date formatée ne doit pas être null", dateFormatee);
    }
    
    /**
     * Test des attributs d'affichage (non persistés).
     */
    @Test
    public void testFeedbackAvecMailUsager() {
        Feedback feedback = new Feedback();
        
        // Sans mail
        assertNull("Mail usager initial doit être null", feedback.getMailUsager());
        
        // Avec mail
        String mailTest = "test@example.com";
        feedback.setMailUsager(mailTest);
        assertEquals("Mail usager incorrect", mailTest, feedback.getMailUsager());
        
        // Test d'autres attributs d'affichage
        feedback.setNomUsager("Dupont");
        feedback.setPrenomUsager("Jean");
        assertEquals("Dupont", feedback.getNomUsager());
        assertEquals("Jean", feedback.getPrenomUsager());
        
        feedback.setNomAdminReponse("AdminNom");
        feedback.setPrenomAdminReponse("AdminPrenom");
        assertEquals("AdminNom", feedback.getNomAdminReponse());
        assertEquals("AdminPrenom", feedback.getPrenomAdminReponse());
    }
    
    /**
     * Test des cas limites pour les IDs.
     */
    @Test
    public void testCasLimitesIDs() {
        Feedback feedback = new Feedback();
        
        // IDs négatifs (si autorisés)
        feedback.setIdUsager(-1);
        assertEquals(-1, feedback.getIdUsager());
        
        feedback.setIdFeedback(-100);
        assertEquals(-100, feedback.getIdFeedback());
        
        // Grands nombres
        feedback.setIdUsager(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, feedback.getIdUsager());
        
        feedback.setIdFeedback(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, feedback.getIdFeedback());
    }
    
    
    /**
     * Test de la méthode getMessageTronque (si elle existe).
     * Note: Cette méthode n'est pas dans le code fourni, mais pourrait être utile.
     */
    @Test
    public void testGetMessageTronque() {
        Feedback feedback = new Feedback(1, "Sujet", "Message très long qui doit être tronqué");
        
        // Si la méthode existe, la tester
        try {
            Method method = Feedback.class.getMethod("getMessageTronque", int.class);
            String tronque = (String) method.invoke(feedback, 10);
            assertTrue("Message tronqué trop long", tronque.length() <= 13); // 10 + "..."
            assertTrue("Message tronqué doit contenir '...'", tronque.contains("..."));
        } catch (NoSuchMethodException e) {
            // Méthode non implémentée, c'est OK
            System.out.println("Méthode getMessageTronque non implémentée");
        } catch (Exception e) {
            fail("Erreur lors de l'appel à getMessageTronque: " + e.getMessage());
        }
    }
    
    /**
     * Test de la cohérence des dates.
     */
    @Test
    public void testCohérenceDates() {
        LocalDateTime maintenant = LocalDateTime.now();
        
        Feedback feedback = new Feedback(1, "Test", "Message");
        feedback.setDateCreation(maintenant);
        feedback.setDateReponse(maintenant.plusDays(1));
        
        // La date de réponse doit être après la date de création
        assertTrue("Date réponse doit être après date création",
                  feedback.getDateReponse().isAfter(feedback.getDateCreation()));
    }
    
    /**
     * Test de la méthode toString() avec toutes les informations.
     */
    @Test
    public void testToStringComplet() {
        LocalDateTime date = LocalDateTime.of(2024, 1, 30, 10, 30);
        
        Feedback feedback = new Feedback();
        feedback.setIdFeedback(123);
        feedback.setIdUsager(456);
        feedback.setSujet("Problème technique");
        feedback.setMessage("L'application plante");
        feedback.setDateCreation(date);
        feedback.setStatut("RESOLU");
        
        String result = feedback.toString();
        
        // Vérifier que toutes les infos importantes sont présentes
        assertTrue(result.contains("id=123"));
        assertTrue(result.contains("sujet='Problème technique'"));
        assertTrue(result.contains("statut='RESOLU'"));
        assertTrue(result.contains("date=2024-01-30T10:30"));
    }
}
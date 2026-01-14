package modele.test;

import modele.Feedback;
import modele.dao.FeedbackDAO;
import org.junit.*;
import static org.junit.Assert.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class FeedbackDAOTest {
    
    @Test
    public void testSingleton() {
        FeedbackDAO instance1 = FeedbackDAO.getInstance();
        FeedbackDAO instance2 = FeedbackDAO.getInstance();
        
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }
    
    @Test
    public void testConstructeurAccessibilite() throws Exception {
        Constructor<FeedbackDAO> constructor = FeedbackDAO.class.getDeclaredConstructor();
        
        // Vérifier que le constructeur est accessible (package-private ou private)
        // En réalité, il devrait être privé pour un singleton
        assertFalse("Le constructeur ne devrait pas être public", 
                   Modifier.isPublic(constructor.getModifiers()));
        
        // Le rendre accessible pour l'instanciation
        constructor.setAccessible(true);
        FeedbackDAO instance = constructor.newInstance();
        assertNotNull(instance);
    }
    
    @Test
    public void testMethodesExistent() {
        FeedbackDAO dao = FeedbackDAO.getInstance();
        assertNotNull(dao);
        
        // Vérifier que les méthodes statiques existent
        try {
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
            fail("Méthode manquante: " + e.getMessage());
        }
    }
    
    @Test
    public void testFeedbackModelComplet() {
        // Test complet du modèle Feedback
        Feedback feedback = new Feedback(1, "Problème de connexion", "Je ne peux pas me connecter");
        
        // Vérifier les valeurs initiales
        assertEquals(1, feedback.getIdUsager());
        assertEquals("Problème de connexion", feedback.getSujet());
        assertEquals("Je ne peux pas me connecter", feedback.getMessage());
        assertEquals("NOUVEAU", feedback.getStatut());
        assertFalse(feedback.isGotanswer());
        assertNotNull(feedback.getDateCreation());
        
        // Tester les setters
        feedback.setIdFeedback(100);
        feedback.setStatut("EN_COURS");
        feedback.setGotanswer(true);
        feedback.setIdAdminReponse(10);
        feedback.setReponse("Nous traitons votre demande");
        
        assertEquals(100, feedback.getIdFeedback());
        assertEquals("EN_COURS", feedback.getStatut());
        assertTrue(feedback.isGotanswer());
        assertEquals(Integer.valueOf(10), feedback.getIdAdminReponse());
        assertEquals("Nous traitons votre demande", feedback.getReponse());
        
        // Tester les méthodes utilitaires
        assertTrue(feedback.isRepondu());
        
        feedback.setIdFeedbackParent(50);
        assertFalse(feedback.estUnMessageParent());
        
        feedback.setIdFeedbackParent(null);
        assertTrue(feedback.estUnMessageParent());
    }
    
    @Test
    public void testNomCompletUsager() {
        Feedback feedback = new Feedback();
        feedback.setIdUsager(123);
        
        // Test sans nom/prénom
        assertEquals("Utilisateur #123", feedback.getNomCompletUsager());
        
        // Test avec nom seulement
        feedback.setNomUsager("Martin");
        assertEquals("Utilisateur #123", feedback.getNomCompletUsager());
        
        // Test avec prénom seulement
        feedback.setNomUsager(null);
        feedback.setPrenomUsager("Julie");
        assertEquals("Utilisateur #123", feedback.getNomCompletUsager());
        
        // Test avec nom et prénom
        feedback.setNomUsager("Martin");
        feedback.setPrenomUsager("Julie");
        assertEquals("Julie Martin", feedback.getNomCompletUsager());
    }
    
    @Test
    public void testNomCompletAdminReponse() {
        Feedback feedback = new Feedback();
        
        // Test sans admin
        assertNull(feedback.getNomCompletAdminReponse());
        
        // Test avec ID admin mais sans nom
        feedback.setIdAdminReponse(50);
        assertEquals("Admin #50", feedback.getNomCompletAdminReponse());
        
        // Test avec nom seulement
        feedback.setNomAdminReponse("Admin");
        assertEquals("Admin #50", feedback.getNomCompletAdminReponse());
        
        // Test avec prénom seulement
        feedback.setNomAdminReponse(null);
        feedback.setPrenomAdminReponse("System");
        assertEquals("Admin #50", feedback.getNomCompletAdminReponse());
        
        // Test avec nom et prénom complets
        feedback.setNomAdminReponse("Admin");
        feedback.setPrenomAdminReponse("System");
        assertEquals("System Admin", feedback.getNomCompletAdminReponse());
    }
    
    @Test
    public void testToString() {
        Feedback feedback = new Feedback(1, "Test sujet", "Test message");
        feedback.setIdFeedback(99);
        
        String toString = feedback.toString();
        
        assertTrue(toString.contains("id=99"));
        assertTrue(toString.contains("sujet='Test sujet'"));
        assertTrue(toString.contains("statut='NOUVEAU'"));
        assertTrue(toString.contains("date="));
    }
    
    @Test
    public void testStatutsValides() {
        Feedback feedback = new Feedback(1, "Test", "Message");
        
        // Test des différents statuts possibles
        feedback.setStatut("NOUVEAU");
        assertEquals("NOUVEAU", feedback.getStatut());
        
        feedback.setStatut("EN_COURS");
        assertEquals("EN_COURS", feedback.getStatut());
        
        feedback.setStatut("RESOLU");
        assertEquals("RESOLU", feedback.getStatut());
        
        feedback.setStatut("FERME");
        assertEquals("FERME", feedback.getStatut());
    }
    
    @Test
    public void testIsReponduMethod() {
        Feedback feedback = new Feedback(1, "Test", "Message");
        
        // Initialement non répondu
        assertFalse(feedback.isRepondu());
        
        // Répondu via gotanswer
        feedback.setGotanswer(true);
        assertTrue(feedback.isRepondu());
        
        // Répondu via idAdminReponse
        feedback.setGotanswer(false);
        feedback.setIdAdminReponse(10);
        assertTrue(feedback.isRepondu());
        
        // Répondu via les deux
        feedback.setGotanswer(true);
        feedback.setIdAdminReponse(10);
        assertTrue(feedback.isRepondu());
    }
    
    @Test
    public void testEstUnMessageParent() {
        Feedback feedback = new Feedback();
        
        // Initialement un message parent
        assertTrue(feedback.estUnMessageParent());
        
        // Définir un parent -> devient un message enfant
        feedback.setIdFeedbackParent(100);
        assertFalse(feedback.estUnMessageParent());
        
        // Retirer le parent -> redevient un message parent
        feedback.setIdFeedbackParent(null);
        assertTrue(feedback.estUnMessageParent());
    }
    
    @Test
    public void testMultipleInstancesFeedback() {
        // Créer plusieurs instances de Feedback
        Feedback feedback1 = new Feedback(1, "Sujet 1", "Message 1");
        Feedback feedback2 = new Feedback(2, "Sujet 2", "Message 2");
        
        // Vérifier qu'elles sont indépendantes
        assertNotSame(feedback1, feedback2);
        assertEquals(1, feedback1.getIdUsager());
        assertEquals(2, feedback2.getIdUsager());
        assertEquals("Sujet 1", feedback1.getSujet());
        assertEquals("Sujet 2", feedback2.getSujet());
    }
    
    @Test
    public void testFeedbackAvecDateReponse() {
        Feedback feedback = new Feedback();
        
        // Sans date de réponse
        assertNull(feedback.getDateReponse());
        
        // Avec date de réponse
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        feedback.setDateReponse(now);
        assertEquals(now, feedback.getDateReponse());
    }
    
    @Test
    public void testFeedbackAvecMailUsager() {
        Feedback feedback = new Feedback();
        
        // Sans mail
        assertNull(feedback.getMailUsager());
        
        // Avec mail
        feedback.setMailUsager("test@example.com");
        assertEquals("test@example.com", feedback.getMailUsager());
    }
}
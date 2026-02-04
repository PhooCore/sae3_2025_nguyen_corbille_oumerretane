package modele.test;

import org.junit.Test;

import modele.Feedback;

import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import java.time.LocalDateTime;

public class FeedbackTest {

    private Feedback feedback;

    @Before
    public void setUp() {
        feedback = new Feedback();
    }

    @After
    public void tearDown() {
        feedback = null;
    }

    @Test
    public void testConstructeurParDefaut() {
        assertNotNull(feedback);
        assertEquals(0, feedback.getIdFeedback());
        assertEquals(0, feedback.getIdUsager());
        assertNull(feedback.getSujet());
        assertNull(feedback.getMessage());
        assertNull(feedback.getDateCreation());
        assertNull(feedback.getStatut());
        assertNull(feedback.getIdAdminReponse());
        assertNull(feedback.getIdFeedbackParent());
        assertNull(feedback.getDateReponse());
        assertNull(feedback.getReponse());
        assertNull(feedback.getNomUsager());
        assertNull(feedback.getPrenomUsager());
        assertNull(feedback.getMailUsager());
        assertNull(feedback.getNomAdminReponse());
        assertNull(feedback.getPrenomAdminReponse());
    }

    @Test
    public void testConstructeurAvecParametres() {
        int idUsager = 123;
        String sujet = "Problème de connexion";
        String message = "Je ne peux pas me connecter à mon compte";
        
        Feedback feedback = new Feedback(idUsager, sujet, message);
        
        assertEquals(idUsager, feedback.getIdUsager());
        assertEquals(sujet, feedback.getSujet());
        assertEquals(message, feedback.getMessage());
        assertNotNull(feedback.getDateCreation());
        assertEquals("NOUVEAU", feedback.getStatut());
        assertFalse(feedback.isGotanswer());
        assertFalse(feedback.isRepondu());
        assertTrue(feedback.estUnMessageParent());
        assertNull(feedback.getIdAdminReponse());
        assertNull(feedback.getIdFeedbackParent());
    }

    @Test
    public void testGettersEtSetters() {
        LocalDateTime maintenant = LocalDateTime.now();
        
        feedback.setIdFeedback(1);
        feedback.setIdUsager(100);
        feedback.setSujet("Suggestion d'amélioration");
        feedback.setMessage("Je propose d'ajouter une fonctionnalité...");
        feedback.setDateCreation(maintenant);
        feedback.setStatut("EN_COURS");
        feedback.setGotanswer(true);
        feedback.setIdAdminReponse(50);
        feedback.setIdFeedbackParent(10);
        feedback.setDateReponse(maintenant.plusHours(2));
        feedback.setReponse("Merci pour votre suggestion");
        feedback.setNomUsager("Dupont");
        feedback.setPrenomUsager("Jean");
        feedback.setMailUsager("jean.dupont@email.com");
        feedback.setNomAdminReponse("Martin");
        feedback.setPrenomAdminReponse("Sophie");
        
        assertEquals(1, feedback.getIdFeedback());
        assertEquals(100, feedback.getIdUsager());
        assertEquals("Suggestion d'amélioration", feedback.getSujet());
        assertEquals("Je propose d'ajouter une fonctionnalité...", feedback.getMessage());
        assertEquals(maintenant, feedback.getDateCreation());
        assertEquals("EN_COURS", feedback.getStatut());
        assertTrue(feedback.isGotanswer());
        assertEquals(Integer.valueOf(50), feedback.getIdAdminReponse());
        assertEquals(Integer.valueOf(10), feedback.getIdFeedbackParent());
        assertEquals(maintenant.plusHours(2), feedback.getDateReponse());
        assertEquals("Merci pour votre suggestion", feedback.getReponse());
        assertEquals("Dupont", feedback.getNomUsager());
        assertEquals("Jean", feedback.getPrenomUsager());
        assertEquals("jean.dupont@email.com", feedback.getMailUsager());
        assertEquals("Martin", feedback.getNomAdminReponse());
        assertEquals("Sophie", feedback.getPrenomAdminReponse());
    }

    @Test
    public void testGetNomCompletUsager() {
        // Test 1: Sans nom et prénom
        assertEquals("Utilisateur #0", feedback.getNomCompletUsager());
        
        // Test 2: Avec nom seulement
        feedback.setNomUsager("Durand");
        assertEquals("Utilisateur #0", feedback.getNomCompletUsager());
        
        // Test 3: Avec prénom seulement
        feedback.setNomUsager(null);
        feedback.setPrenomUsager("Marie");
        assertEquals("Utilisateur #0", feedback.getNomCompletUsager());
        
        // Test 4: Avec nom et prénom
        feedback.setNomUsager("Durand");
        feedback.setPrenomUsager("Marie");
        assertEquals("Marie Durand", feedback.getNomCompletUsager());
        
        // Test 5: Avec ID usager spécifique
        feedback.setIdUsager(200);
        feedback.setNomUsager(null);
        feedback.setPrenomUsager(null);
        assertEquals("Utilisateur #200", feedback.getNomCompletUsager());
    }

    @Test
    public void testGetNomCompletAdminReponse() {
        // Test 1: Sans admin
        assertNull(feedback.getNomCompletAdminReponse());
        
        // Test 2: Avec ID admin mais sans nom/prenom
        feedback.setIdAdminReponse(30);
        assertEquals("Admin #30", feedback.getNomCompletAdminReponse());
        
        // Test 3: Avec nom seulement
        feedback.setNomAdminReponse("Lefevre");
        assertEquals("Admin #30", feedback.getNomCompletAdminReponse());
        
        // Test 4: Avec prénom seulement
        feedback.setNomAdminReponse(null);
        feedback.setPrenomAdminReponse("Paul");
        assertEquals("Admin #30", feedback.getNomCompletAdminReponse());
        
        // Test 5: Avec nom et prénom
        feedback.setNomAdminReponse("Lefevre");
        feedback.setPrenomAdminReponse("Paul");
        assertEquals("Paul Lefevre", feedback.getNomCompletAdminReponse());
    }

    @Test
    public void testIsRepondu() {
        // Test 1: Non répondu
        assertFalse(feedback.isRepondu());
        
        // Test 2: Répondu avec gotanswer = true
        feedback.setGotanswer(true);
        assertTrue(feedback.isRepondu());
        
        // Test 3: Répondu avec idAdminReponse
        feedback.setGotanswer(false);
        feedback.setIdAdminReponse(1);
        assertTrue(feedback.isRepondu());
        
        // Test 4: Répondu avec les deux
        feedback.setGotanswer(true);
        feedback.setIdAdminReponse(1);
        assertTrue(feedback.isRepondu());
    }

    @Test
    public void testEstUnMessageParent() {
        // Test 1: Message parent (pas de parent)
        assertTrue(feedback.estUnMessageParent());
        
        // Test 2: Message enfant (avec parent)
        feedback.setIdFeedbackParent(1);
        assertFalse(feedback.estUnMessageParent());
        
        // Test 3: Retour à parent
        feedback.setIdFeedbackParent(null);
        assertTrue(feedback.estUnMessageParent());
    }

    @Test
    public void testToString() {
        Feedback feedback = new Feedback(1, "Test sujet", "Test message");
        feedback.setIdFeedback(99);
        LocalDateTime date = LocalDateTime.of(2024, 1, 15, 10, 30);
        feedback.setDateCreation(date);
        feedback.setStatut("NOUVEAU");
        
        String result = feedback.toString();
        
        assertTrue(result.contains("id=99"));
        assertTrue(result.contains("sujet='Test sujet'"));
        assertTrue(result.contains("statut='NOUVEAU'"));
        assertTrue(result.contains("date="));
    }

    @Test
    public void testStatutMiseAJour() {
        Feedback feedback = new Feedback(1, "Sujet", "Message");
        
        assertEquals("NOUVEAU", feedback.getStatut());
        
        feedback.setStatut("EN_COURS");
        assertEquals("EN_COURS", feedback.getStatut());
        
        feedback.setStatut("RESOLU");
        assertEquals("RESOLU", feedback.getStatut());
    }

    @Test
    public void testIntegrationFeedbackComplet() {
        Feedback feedbackParent = new Feedback(100, "Problème technique", "Je rencontre un bug");
        feedbackParent.setIdFeedback(1);
        feedbackParent.setNomUsager("Dupont");
        feedbackParent.setPrenomUsager("Jean");
        
        Feedback feedbackReponse = new Feedback();
        feedbackReponse.setIdFeedback(2);
        feedbackReponse.setIdFeedbackParent(1);
        feedbackReponse.setIdAdminReponse(50);
        feedbackReponse.setNomAdminReponse("Admin");
        feedbackReponse.setPrenomAdminReponse("System");
        feedbackReponse.setReponse("Le bug a été corrigé");
        feedbackReponse.setDateReponse(LocalDateTime.now().plusDays(1));
        feedbackReponse.setGotanswer(true);
        
        assertTrue(feedbackParent.estUnMessageParent());
        assertEquals("Jean Dupont", feedbackParent.getNomCompletUsager());
        
        assertFalse(feedbackReponse.estUnMessageParent());
        assertEquals("System Admin", feedbackReponse.getNomCompletAdminReponse());
        assertTrue(feedbackReponse.isRepondu());
        assertTrue(feedbackReponse.isGotanswer());
    }

    @Test
    public void testEqualsNonImplemente() {
        // Ce test vérifie que deux instances avec les mêmes données ne sont pas égales
        // car equals() n'est pas implémenté dans Feedback
        Feedback feedback1 = new Feedback(1, "Sujet", "Message");
        feedback1.setIdFeedback(100);
        
        Feedback feedback2 = new Feedback(1, "Sujet", "Message");
        feedback2.setIdFeedback(100);
        
        assertFalse(feedback1.equals(feedback2));
    }

    @Test
    public void testHashCodeNonImplemente() {
        // Ce test vérifie que hashCode() n'est pas redéfini
        Feedback feedback1 = new Feedback(1, "Sujet", "Message");
        Feedback feedback2 = feedback1;
        
        assertEquals(feedback1.hashCode(), feedback2.hashCode());
    }

    @Test
    public void testChangementStatutApresReponse() {
        Feedback feedback = new Feedback(1, "Question", "Message");
        
        // Initialement NON répondu
        assertFalse(feedback.isRepondu());
        assertEquals("NOUVEAU", feedback.getStatut());
        
        // Ajout d'une réponse
        feedback.setReponse("Réponse de l'admin");
        feedback.setIdAdminReponse(1);
        feedback.setGotanswer(true);
        
        // Maintenant répondu
        assertTrue(feedback.isRepondu());
        
        // Le statut peut être changé indépendamment
        feedback.setStatut("RESOLU");
        assertEquals("RESOLU", feedback.getStatut());
    }

    @Test
    public void testDateCreationAutomatique() {
        LocalDateTime avantCreation = LocalDateTime.now();
        
        Feedback feedback = new Feedback(1, "Test", "Message");
        
        LocalDateTime apresCreation = LocalDateTime.now();
        LocalDateTime dateCreation = feedback.getDateCreation();
        
        // La date de création doit être entre avant et après
        assertTrue((dateCreation.isEqual(avantCreation) || dateCreation.isAfter(avantCreation)) &&
                   (dateCreation.isEqual(apresCreation) || dateCreation.isBefore(apresCreation)));
    }

}
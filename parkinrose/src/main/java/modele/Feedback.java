package modele;

import java.time.LocalDateTime;

/**
 * Classe représentant un feedback (retour utilisateur) dans le système.
 * Gère les messages des usagers vers l'administration, ainsi que les réponses.
 * Permet de créer des conversations structurées avec des messages parents/enfants.
 */
public class Feedback {
    // ==================== ATTRIBUTS PERSISTÉS ====================
    
    // Identifiant unique du feedback en base de données
    private int idFeedback;
    // Identifiant de l'usager émetteur du feedback
    private int idUsager;
    // Sujet du feedback (ex: "Problème technique", "Suggestion")
    private String sujet;
    // Message principal du feedback
    private String message;
    // Date et heure de création du feedback
    private LocalDateTime dateCreation;
    // Statut actuel (ex: "NOUVEAU", "EN_COURS", "RESOLU", "FERME")
    private String statut;
    // Indique si une réponse a été apportée
    private boolean gotanswer;
    // Identifiant de l'administrateur ayant répondu (null si pas de réponse)
    private Integer idAdminReponse;
    // Référence vers le feedback parent (pour les réponses, null pour un message initial)
    private Integer idFeedbackParent;
    // Date et heure de la réponse (null si pas de réponse)
    private LocalDateTime dateReponse;
    // Contenu de la réponse administrative
    private String reponse;
    
    // ==================== ATTRIBUTS D'AFFICHAGE (NON PERSISTÉS) ====================
    
    // Informations sur l'usager (pour affichage uniquement)
    private String nomUsager;
    private String prenomUsager;
    private String mailUsager;
    
    // Informations sur l'administrateur ayant répondu (pour affichage uniquement)
    private String nomAdminReponse;
    private String prenomAdminReponse;
    
    // ==================== CONSTRUCTEURS ====================
    
    /**
     * Constructeur par défaut.
     * Nécessaire pour les frameworks (JPA, Hibernate, etc.)
     */
    public Feedback() {
    }
    
    /**
     * Constructeur pour créer un nouveau feedback (message initial).
     * Définit automatiquement la date de création et le statut "NOUVEAU".
     * 
     * @param idUsager Identifiant de l'usager émetteur
     * @param sujet Sujet du feedback
     * @param message Message du feedback
     */
    public Feedback(int idUsager, String sujet, String message) {
        this.idUsager = idUsager;
        this.sujet = sujet;
        this.message = message;
        this.dateCreation = LocalDateTime.now();  // Date courante par défaut
        this.statut = "NOUVEAU";  // Statut initial
        this.gotanswer = false;   // Pas de réponse au départ
    }
    
    // ==================== GETTERS & SETTERS ====================
    
    public int getIdFeedback() {
        return idFeedback;
    }
    
    public void setIdFeedback(int idFeedback) {
        this.idFeedback = idFeedback;
    }
    
    public int getIdUsager() {
        return idUsager;
    }
    
    public void setIdUsager(int idUsager) {
        this.idUsager = idUsager;
    }
    
    public String getSujet() {
        return sujet;
    }
    
    public void setSujet(String sujet) {
        this.sujet = sujet;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public boolean isGotanswer() {
        return gotanswer;
    }
    
    public void setGotanswer(boolean gotanswer) {
        this.gotanswer = gotanswer;
    }
    
    public Integer getIdAdminReponse() {
        return idAdminReponse;
    }
    
    public void setIdAdminReponse(Integer idAdminReponse) {
        this.idAdminReponse = idAdminReponse;
    }
    
    public Integer getIdFeedbackParent() {
        return idFeedbackParent;
    }
    
    public void setIdFeedbackParent(Integer idFeedbackParent) {
        this.idFeedbackParent = idFeedbackParent;
    }
    
    public LocalDateTime getDateReponse() {
        return dateReponse;
    }
    
    public void setDateReponse(LocalDateTime dateReponse) {
        this.dateReponse = dateReponse;
    }
    
    public String getReponse() {
        return reponse;
    }
    
    public void setReponse(String reponse) {
        this.reponse = reponse;
    }
    
    // ==================== GETTERS & SETTERS POUR L'AFFICHAGE ====================
    
    public String getNomUsager() {
        return nomUsager;
    }
    
    public void setNomUsager(String nomUsager) {
        this.nomUsager = nomUsager;
    }
    
    public String getPrenomUsager() {
        return prenomUsager;
    }
    
    public void setPrenomUsager(String prenomUsager) {
        this.prenomUsager = prenomUsager;
    }
    
    public String getMailUsager() {
        return mailUsager;
    }
    
    public void setMailUsager(String mailUsager) {
        this.mailUsager = mailUsager;
    }
    
    public String getNomAdminReponse() {
        return nomAdminReponse;
    }
    
    public void setNomAdminReponse(String nomAdminReponse) {
        this.nomAdminReponse = nomAdminReponse;
    }
    
    public String getPrenomAdminReponse() {
        return prenomAdminReponse;
    }
    
    public void setPrenomAdminReponse(String prenomAdminReponse) {
        this.prenomAdminReponse = prenomAdminReponse;
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Retourne le nom complet de l'usager (prénom + nom).
     * Si les informations ne sont pas disponibles, retourne un identifiant.
     * 
     * @return Le nom complet de l'usager ou un identifiant par défaut
     */
    public String getNomCompletUsager() {
        if (prenomUsager != null && nomUsager != null) {
            return prenomUsager + " " + nomUsager;
        }
        return "Utilisateur #" + idUsager;
    }
    
    /**
     * Retourne le nom complet de l'administrateur ayant répondu.
     * Si les informations ne sont pas disponibles, retourne un identifiant ou null.
     * 
     * @return Le nom complet de l'admin ou un identifiant par défaut, ou null
     */
    public String getNomCompletAdminReponse() {
        if (prenomAdminReponse != null && nomAdminReponse != null) {
            return prenomAdminReponse + " " + nomAdminReponse;
        }
        return idAdminReponse != null ? "Admin #" + idAdminReponse : null;
    }
    
    /**
     * Vérifie si le feedback a reçu une réponse.
     * Considère qu'il y a une réponse si gotanswer est true OU si un admin est référencé.
     * 
     * @return true si le feedback a une réponse, false sinon
     */
    public boolean isRepondu() {
        return gotanswer || idAdminReponse != null;
    }
    
    /**
     * Vérifie si ce feedback est un message parent (message initial).
     * Un message parent n'a pas de feedback parent.
     * 
     * @return true si c'est un message parent, false si c'est une réponse
     */
    public boolean estUnMessageParent() {
        return idFeedbackParent == null;
    }
    
    /**
     * Marque le feedback comme résolu.
     * Change le statut en "RESOLU" et met à jour gotanswer.
     */
    public void marquerCommeResolu() {
        this.statut = "RESOLU";
        this.gotanswer = true;
    }
    
    /**
     * Marque le feedback comme en cours de traitement.
     * Change le statut en "EN_COURS".
     */
    public void marquerCommeEnCours() {
        this.statut = "EN_COURS";
    }
    
    /**
     * Vérifie si le feedback est en statut "NOUVEAU".
     * 
     * @return true si le statut est "NOUVEAU", false sinon
     */
    public boolean estNouveau() {
        return "NOUVEAU".equals(statut);
    }
    
    /**
     * Vérifie si le feedback est en statut "RESOLU".
     * 
     * @return true si le statut est "RESOLU", false sinon
     */
    public boolean estResolu() {
        return "RESOLU".equals(statut);
    }
    
    /**
     * Retourne une version tronquée du message (pour les aperçus).
     * 
     * @param longueurMax Longueur maximum souhaitée
     * @return Le message tronqué avec "..." si nécessaire
     */
    public String getMessageTronque(int longueurMax) {
        if (message == null) {
            return "";
        }
        if (message.length() <= longueurMax) {
            return message;
        }
        return message.substring(0, longueurMax - 3) + "...";
    }
    
    // ==================== MÉTHODES STANDARD ====================
    
    /**
     * Représentation textuelle de l'objet pour le débogage.
     * 
     * @return Une chaîne formatée contenant les informations principales
     */
    @Override
    public String toString() {
        return "Feedback{" +
               "id=" + idFeedback +
               ", sujet='" + sujet + '\'' +
               ", statut='" + statut + '\'' +
               ", date=" + dateCreation +
               '}';
    }
}
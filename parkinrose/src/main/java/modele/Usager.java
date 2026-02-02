package modele;

/**
 * Classe représentant un usager (utilisateur) du système.
 * Gère les informations personnelles d'un usager, son authentification
 * et ses droits d'accès.
 * Cette classe sert d'entité centrale pour la gestion des utilisateurs.
 */
public class Usager {
    // ==================== ATTRIBUTS PRINCIPAUX ====================
    
    // Identifiant unique de l'usager en base de données
    private int idUsager;
    // Nom de famille de l'usager
    private String nomUsager;
    // Prénom de l'usager
    private String prenomUsager;
    // Adresse email utilisée pour l'authentification
    private String mailUsager;
    // Mot de passe crypté pour l'authentification
    private String motDePasse;
    // Numéro de carte de transport Tisséo (optionnel)
    private String numeroCarteTisseo;
    // Indique si l'usager a des droits d'administrateur
    private boolean isAdmin;

    // ==================== CONSTRUCTEURS ====================

    /**
     * Constructeur pour créer un nouvel usager (non administrateur).
     * Utilisé lors de l'inscription d'un nouvel utilisateur.
     * 
     * @param nomUsager Nom de famille de l'usager
     * @param prenomUsager Prénom de l'usager
     * @param mailUsager Adresse email (utilisée comme identifiant)
     * @param motDePasse Mot de passe (devrait être crypté avant stockage)
     */
    public Usager(String nomUsager, String prenomUsager, String mailUsager, String motDePasse) {
        this.nomUsager = nomUsager;
        this.prenomUsager = prenomUsager;
        this.mailUsager = mailUsager;
        this.motDePasse = motDePasse;
        this.numeroCarteTisseo = null;  // Pas de carte Tisséo par défaut
        this.isAdmin = false;           // Non admin par défaut
    }

    /**
     * Constructeur par défaut.
     * Nécessaire pour les frameworks (JPA, Hibernate, etc.)
     */
    public Usager() {
        // Initialisation par défaut
        this.isAdmin = false;
    }

    // ==================== GETTERS & SETTERS ====================

    public int getIdUsager() { 
        return idUsager; 
    }
    
    public void setIdUsager(int idUsager) { 
        this.idUsager = idUsager; 
    }
    
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
    
    public String getMotDePasse() { 
        return motDePasse; 
    }
    
    public void setMotDePasse(String motDePasse) { 
        this.motDePasse = motDePasse; 
    }
    
    public String getNumeroCarteTisseo() {
        return numeroCarteTisseo;
    }

    public void setNumeroCarteTisseo(String numeroCarteTisseo) {
        this.numeroCarteTisseo = numeroCarteTisseo;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Retourne le nom complet de l'usager (prénom + nom).
     * 
     * @return Le nom complet formaté
     */
    public String getNomComplet() {
        return prenomUsager + " " + nomUsager;
    }
    
    /**
     * Retourne l'initiale du prénom suivi du nom complet.
     * Ex: "J. Dupont"
     * 
     * @return Format abrégé du nom
     */
    public String getNomAbrege() {
        if (prenomUsager != null && prenomUsager.length() > 0 && nomUsager != null) {
            return prenomUsager.charAt(0) + ". " + nomUsager;
        }
        return getNomComplet();
    }
    
    /**
     * Vérifie si l'usager a une carte Tisséo associée.
     * 
     * @return true si numeroCarteTisseo n'est pas null ni vide, false sinon
     */
    public boolean hasCarteTisseo() {
        return numeroCarteTisseo != null && !numeroCarteTisseo.trim().isEmpty();
    }
    
    /**
     * Valide l'adresse email (validation basique).
     * Note: En production, utiliser une bibliothèque de validation dédiée.
     * 
     * @return true si l'email semble valide, false sinon
     */
    public boolean emailValide() {
        if (mailUsager == null) {
            return false;
        }
        // Validation très basique
        return mailUsager.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    /**
     * Vérifie si le mot de passe est suffisamment fort (validation basique).
     * 
     * @return true si le mot de passe a au moins 8 caractères, false sinon
     */
    public boolean motDePasseValide() {
        return motDePasse != null && motDePasse.length() >= 8;
    }
    
    /**
     * Compare deux usagers par leur email (utilisation courante pour l'authentification).
     * 
     * @param autre L'autre usager à comparer
     * @return true si les emails sont identiques (insensible à la casse), false sinon
     */
    public boolean equalsByEmail(Usager autre) {
        if (autre == null) {
            return false;
        }
        return this.mailUsager != null && 
               this.mailUsager.equalsIgnoreCase(autre.getMailUsager());
    }
    
    /**
     * Retourne une représentation sécurisée pour les logs (sans mot de passe).
     * 
     * @return Représentation sécurisée de l'usager
     */
    public String toSecureString() {
        return "Usager{" +
               "id=" + idUsager +
               ", nom='" + nomUsager + '\'' +
               ", prenom='" + prenomUsager + '\'' +
               ", mail='" + mailUsager + '\'' +
               ", admin=" + isAdmin +
               ", hasCarteTisseo=" + hasCarteTisseo() +
               '}';
    }
    
    /**
     * Vérifie si l'usager est un utilisateur standard (non administrateur).
     * 
     * @return true si l'usager n'est pas admin, false sinon
     */
    public boolean estUtilisateurStandard() {
        return !isAdmin;
    }
    
    /**
     * Vérifie si l'usager a un nom et prénom valides.
     * 
     * @return true si nom et prénom ne sont pas vides, false sinon
     */
    public boolean informationsValides() {
        return nomUsager != null && !nomUsager.trim().isEmpty() &&
               prenomUsager != null && !prenomUsager.trim().isEmpty() &&
               mailUsager != null && !mailUsager.trim().isEmpty() &&
               motDePasse != null && !motDePasse.trim().isEmpty();
    }

    // ==================== MÉTHODES STANDARD ====================

    /**
     * Représentation textuelle de l'objet pour le débogage.
     * N'inclut PAS le mot de passe pour des raisons de sécurité.
     * 
     * @return Une chaîne formatée contenant les informations (sécurisées)
     */
    @Override
    public String toString() {
        return toSecureString();
    }
    
    /**
     * Comparaison d'égalité basée sur l'ID et l'email.
     * 
     * @param obj L'objet à comparer
     * @return true si les usagers sont identiques, false sinon
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Usager usager = (Usager) obj;
        
        // Comparaison par ID si disponible
        if (idUsager != 0 && usager.idUsager != 0) {
            return idUsager == usager.idUsager;
        }
        
        // Sinon comparaison par email
        return mailUsager != null && mailUsager.equals(usager.mailUsager);
    }
    
    /**
     * Code de hachage basé sur l'ID et l'email.
     * 
     * @return Code de hachage
     */
    @Override
    public int hashCode() {
        int result = idUsager;
        result = 31 * result + (mailUsager != null ? mailUsager.hashCode() : 0);
        return result;
    }
}
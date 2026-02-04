package modele;

/**
 * Classe représentant une adresse postale associée à un usager.
 * Permet de gérer les informations d'adresse (personnelle, professionnelle, etc.)
 * avec la possibilité de désigner une adresse principale.
 */
public class Adresse {
    // Identifiant unique de l'adresse en base de données
    private int idAdresse;
    // Identifiant de l'usager propriétaire de cette adresse
    private int idUsager;
    // Numéro dans la rue (ex: "12", "12bis")
    private String numero;
    // Nom de la rue, avenue, boulevard, etc.
    private String rue;
    // Complément d'adresse (étage, appartement, bâtiment, etc.)
    private String complement;
    // Code postal (5 chiffres pour la France)
    private String codePostal;
    // Ville
    private String ville;
    // Pays (par défaut "France")
    private String pays;
    // Indique si c'est l'adresse principale de l'usager
    private boolean estPrincipale;

    /**
     * Constructeur par défaut.
     * Nécessaire pour les frameworks (JPA, Hibernate, etc.)
     */
    public Adresse() {}

    /**
     * Constructeur pratique pour créer une adresse en France.
     * Définit automatiquement le pays comme "France" et l'adresse comme non principale.
     * 
     * @param idUsager Identifiant de l'usager propriétaire
     * @param numero Numéro dans la rue
     * @param rue Nom de la rue
     * @param codePostal Code postal (5 chiffres)
     * @param ville Ville
     */
    public Adresse(int idUsager, String numero, String rue, String codePostal, String ville) {
        this.idUsager = idUsager;
        this.numero = numero;
        this.rue = rue;
        this.codePostal = codePostal;
        this.ville = ville;
        this.pays = "France";  // Valeur par défaut
        this.estPrincipale = false;  // Par défaut, pas principale
    }

    // ==================== GETTERS & SETTERS ====================
    // Méthodes d'accès standard pour chaque attribut
    
    public int getIdAdresse() { 
        return idAdresse; 
    }
    
    public void setIdAdresse(int idAdresse) { 
        this.idAdresse = idAdresse; 
    }
    
    public int getIdUsager() { 
        return idUsager; 
    }
    
    public void setIdUsager(int idUsager) { 
        this.idUsager = idUsager; 
    }
    
    public String getNumero() { 
        return numero; 
    }
    
    public void setNumero(String numero) { 
        this.numero = numero; 
    }
    
    public String getRue() { 
        return rue; 
    }
    
    public void setRue(String rue) { 
        this.rue = rue; 
    }
    
    public String getComplement() { 
        return complement; 
    }
    
    public void setComplement(String complement) { 
        this.complement = complement; 
    }
    
    public String getCodePostal() { 
        return codePostal; 
    }
    
    public void setCodePostal(String codePostal) { 
        this.codePostal = codePostal; 
    }
    
    public String getVille() { 
        return ville; 
    }
    
    public void setVille(String ville) { 
        this.ville = ville; 
    }
    
    public String getPays() { 
        return pays; 
    }
    
    public void setPays(String pays) { 
        this.pays = pays; 
    }
    
    public boolean isEstPrincipale() { 
        return estPrincipale; 
    }
    
    public void setEstPrincipale(boolean estPrincipale) { 
        this.estPrincipale = estPrincipale; 
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Retourne l'adresse complète formatée sur plusieurs lignes.
     * Format typique :
     * 12 Rue de la Paix, Appartement 3
     * 75008 Paris
     * France
     * 
     * @return L'adresse formatée avec saut de ligne (\n)
     */
    public String getAdresseComplete() {
        StringBuilder sb = new StringBuilder();
        // Première ligne : numéro + rue (+ complément si présent)
        sb.append(numero).append(" ").append(rue);
        
        if (complement != null && !complement.trim().isEmpty()) {
            sb.append(", ").append(complement);
        }
        
        // Deuxième ligne : code postal + ville
        sb.append("\n").append(codePostal).append(" ").append(ville);
        
        // Troisième ligne : pays si différent de France
        if (pays != null && !"France".equals(pays)) {
            sb.append("\n").append(pays);
        }
        
        return sb.toString();
    }
    
    /**
     * Retourne l'adresse sur une seule ligne.
     * Format typique : "12 Rue de la Paix, Appartement 3, 75008 Paris"
     * Utile pour les interfaces avec peu d'espace.
     * 
     * @return L'adresse formatée sur une seule ligne
     */
    public String getAdresseLigne() {
        StringBuilder sb = new StringBuilder();
        sb.append(numero).append(" ").append(rue);
        
        if (complement != null && !complement.trim().isEmpty()) {
            sb.append(", ").append(complement);
        }
        
        sb.append(", ").append(codePostal).append(" ").append(ville);
        return sb.toString();
    }
    
    /**
     * Vérifie si l'adresse est en France.
     * 
     * @return true si le pays est "France" ou null, false sinon
     */
    public boolean estEnFrance() {
        return pays == null || "France".equals(pays);
    }
    
    /**
     * Vérifie si l'adresse a un complément.
     * 
     * @return true si le complément existe et n'est pas vide, false sinon
     */
    public boolean aComplement() {
        return complement != null && !complement.trim().isEmpty();
    }
    
    /**
     * Vérifie si le code postal semble valide (5 chiffres pour la France).
     * Note : validation basique, pourrait être améliorée.
     * 
     * @return true si le code postal a 5 chiffres, false sinon
     */
    public boolean codePostalValide() {
        return codePostal != null && codePostal.matches("\\d{5}");
    }
}
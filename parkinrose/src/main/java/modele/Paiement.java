package modele;

import java.time.LocalDateTime;

/**
 * Classe représentant un paiement effectué dans le système.
 * Gère les informations relatives aux paiements de stationnement ou d'abonnements.
 * Note importante : Le stockage des informations sensibles (code secret de carte) 
 * devrait être crypté en production.
 */
public class Paiement {
    // Identifiant unique du paiement
    private String idPaiement;
    // Nom du titulaire de la carte bancaire
    private String nomCarte;
    // Numéro de la carte bancaire (16 chiffres généralement)
    private String numeroCarte;
    // Code secret de la carte (CVV/CVC) - À crypter en production !
    private String codeSecretCarte;
    // Identifiant de l'abonnement lié (null pour un paiement de stationnement simple)
    private String idAbonnement;
    // Montant du paiement en euros
    private double montant;
    // Identifiant de l'usager effectuant le paiement
    private int idUsager;
    // Date et heure du paiement
    private LocalDateTime datePaiement;
    // Méthode de paiement utilisée (ex: "CARTE", "PAYPAL", "GOOGLE_PAY")
    private String methodePaiement;
    // Statut du paiement (ex: "REUSSI", "ECHOUE", "EN_ATTENTE", "ANNULE")
    private String statut;
    // Type de paiement (ex: "Stationnement", "Abonnement", "Amende")
    private String typePaiement;

    // ==================== CONSTRUCTEURS ====================

    /**
     * Constructeur pour un paiement de stationnement simple.
     * Définit automatiquement plusieurs valeurs par défaut.
     * 
     * @param nomCarte Nom du titulaire de la carte
     * @param numeroCarte Numéro de la carte (16 chiffres)
     * @param codeSecretCarte Code secret (CVV/CVC)
     * @param montant Montant à payer en euros
     * @param idUsager Identifiant de l'usager effectuant le paiement
     */
    public Paiement(String nomCarte, String numeroCarte, String codeSecretCarte, double montant, int idUsager) {
        this.nomCarte = nomCarte;
        this.numeroCarte = numeroCarte;
        this.codeSecretCarte = codeSecretCarte;
        this.montant = montant;
        this.idUsager = idUsager;
        this.idPaiement = "PAY_" + System.currentTimeMillis();  // Génération d'ID unique
        this.datePaiement = LocalDateTime.now();                // Date courante
        this.methodePaiement = "CARTE";                        // Méthode par défaut
        this.statut = "REUSSI";                                 // Statut par défaut
        this.typePaiement = "Stationnement";                    // Type par défaut pour ce constructeur
    }

    /**
     * Constructeur pour un paiement d'abonnement.
     * Appelle le constructeur principal puis initialise les spécificités d'abonnement.
     * 
     * @param nomCarte Nom du titulaire de la carte
     * @param numeroCarte Numéro de la carte (16 chiffres)
     * @param codeSecretCarte Code secret (CVV/CVC)
     * @param montant Montant de l'abonnement en euros
     * @param idUsager Identifiant de l'usager effectuant le paiement
     * @param idAbonnement Identifiant de l'abonnement concerné
     */
    public Paiement(String nomCarte, String numeroCarte, String codeSecretCarte, double montant, int idUsager, String idAbonnement) {
        this(nomCarte, numeroCarte, codeSecretCarte, montant, idUsager);  // Appel au constructeur principal
        this.idAbonnement = idAbonnement;
        this.typePaiement = "Abonnement";  // Type spécifique pour abonnement
    }

    /**
     * Constructeur par défaut.
     * Nécessaire pour les frameworks (JPA, Hibernate, etc.)
     */
    public Paiement() {
    }

    // ==================== GETTERS & SETTERS ====================

    public String getIdPaiement() { 
        return idPaiement; 
    }
    
    public void setIdPaiement(String idPaiement) { 
        this.idPaiement = idPaiement; 
    }
    
    public String getNomCarte() { 
        return nomCarte; 
    }
    
    public void setNomCarte(String nomCarte) { 
        this.nomCarte = nomCarte; 
    }
    
    public String getNumeroCarte() { 
        return numeroCarte; 
    }
    
    public void setNumeroCarte(String numeroCarte) { 
        this.numeroCarte = numeroCarte; 
    }
    
    public String getCodeSecretCarte() { 
        return codeSecretCarte; 
    }
    
    public void setCodeSecretCarte(String codeSecretCarte) { 
        this.codeSecretCarte = codeSecretCarte; 
    }
    
    public String getIdAbonnement() { 
        return idAbonnement; 
    }
    
    public void setIdAbonnement(String idAbonnement) { 
        this.idAbonnement = idAbonnement; 
    }
    
    public double getMontant() { 
        return montant; 
    }
    
    public void setMontant(double montant) { 
        this.montant = montant; 
    }
    
    public int getIdUsager() { 
        return idUsager; 
    }
    
    public void setIdUsager(int idUsager) { 
        this.idUsager = idUsager; 
    }
    
    public LocalDateTime getDatePaiement() { 
        return datePaiement; 
    }
    
    public void setDatePaiement(LocalDateTime datePaiement) { 
        this.datePaiement = datePaiement; 
    }
    
    public String getMethodePaiement() { 
        return methodePaiement; 
    }
    
    public void setMethodePaiement(String methodePaiement) { 
        this.methodePaiement = methodePaiement; 
    }
    
    public String getStatut() { 
        return statut; 
    }
    
    public void setStatut(String statut) { 
        this.statut = statut; 
    }

    /**
     * Retourne le type de paiement.
     * Si le type n'est pas défini, le déduit de la présence d'un idAbonnement.
     * 
     * @return Le type de paiement ("Abonnement" ou "Stationnement")
     */
    public String getTypePaiement() { 
        if (typePaiement == null) {
            // Déduction logique du type
            return idAbonnement != null && !idAbonnement.isEmpty() ? "Abonnement" : "Stationnement";
        }
        return typePaiement; 
    }
    
    public void setTypePaiement(String typePaiement) { 
        this.typePaiement = typePaiement; 
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Vérifie si le paiement est réussi.
     * 
     * @return true si le statut est "REUSSI", false sinon
     */
    public boolean estReussi() {
        return "REUSSI".equals(statut);
    }
    
    /**
     * Vérifie si le paiement est pour un abonnement.
     * 
     * @return true si le type est "Abonnement" ou si idAbonnement est présent, false sinon
     */
    public boolean estPourAbonnement() {
        return "Abonnement".equals(getTypePaiement()) || 
               (idAbonnement != null && !idAbonnement.isEmpty());
    }
    
    /**
     * Vérifie si le paiement est pour un stationnement simple.
     * 
     * @return true si le type est "Stationnement" ou si idAbonnement est absent, false sinon
     */
    public boolean estPourStationnement() {
        return !estPourAbonnement();
    }
    
    /**
     * Masque partiellement le numéro de carte pour l'affichage.
     * Format: XXXX XXXX XXXX 1234 (affiche seulement les 4 derniers chiffres)
     * 
     * @return Le numéro de carte masqué pour l'affichage sécurisé
     */
    public String getNumeroCarteMasque() {
        if (numeroCarte == null || numeroCarte.length() < 4) {
            return "****";
        }
        String derniersChiffres = numeroCarte.substring(numeroCarte.length() - 4);
        return "**** **** **** " + derniersChiffres;
    }
    
    /**
     * Retourne une version sécurisée pour l'affichage (sans informations sensibles).
     * 
     * @return Une représentation sécurisée du paiement
     */
    public String getInfoSecurisee() {
        return String.format("Paiement %s - %.2f€ - %s", 
               getTypePaiement(), montant, datePaiement != null ? datePaiement.toLocalDate().toString() : "N/A");
    }
    
    /**
     * Valide les informations de la carte (validation basique).
     * Note: En production, utiliser une bibliothèque de validation dédiée.
     * 
     * @return true si les informations semblent valides, false sinon
     */
    public boolean informationsCarteValides() {
        // Validation du numéro de carte (16 chiffres)
        if (numeroCarte == null || !numeroCarte.matches("\\d{16}")) {
            return false;
        }
        
        // Validation du code secret (3 ou 4 chiffres)
        if (codeSecretCarte == null || !codeSecretCarte.matches("\\d{3,4}")) {
            return false;
        }
        
        // Validation du nom (non vide)
        if (nomCarte == null || nomCarte.trim().isEmpty()) {
            return false;
        }
        
        // Validation du montant (positif)
        if (montant <= 0) {
            return false;
        }
        
        return true;
    }
    
    // ==================== MÉTHODES STANDARD ====================

    /**
     * Représentation textuelle de l'objet pour le débogage.
     * N'affiche pas les informations sensibles comme le code secret.
     * 
     * @return Une chaîne formatée contenant les informations non sensibles
     */
    @Override
    public String toString() {
        return "Paiement{" +
               "id='" + idPaiement + '\'' +
               ", type='" + getTypePaiement() + '\'' +
               ", montant=" + montant + "€" +
               ", statut='" + statut + '\'' +
               ", date=" + datePaiement +
               ", methode='" + methodePaiement + '\'' +
               ", carte=**** **** **** " + (numeroCarte != null && numeroCarte.length() >= 4 ? 
                  numeroCarte.substring(numeroCarte.length() - 4) : "****") +
               '}';
    }
}
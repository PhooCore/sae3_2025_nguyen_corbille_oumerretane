package modele;

import java.time.LocalDateTime;

/**
 * Classe représentant un abonnement d'usager dans le système.
 * Gère les informations relatives à un abonnement (parking, transport, etc.)
 * ainsi que sa validité dans le temps.
 */
public class Abonnement {
    // Identifiant unique de l'abonnement (généralement un UUID ou code)
    private String idAbonnement;
    // Référence vers l'usager propriétaire de l'abonnement
    private int idUsager;  
    // Nom/label descriptif de l'abonnement (ex: "Abonnement Premium Mensuel")
    private String libelleAbonnement;
    // Type/catégorie de l'abonnement (ex: "ZONE_BLEUE", "ANNUEL", "ETUDIANT")
    private String typeAbonnement;  
    // Prix de l'abonnement en euros
    private double tarifAbonnement;  
    // Date et heure de début de validité
    private LocalDateTime dateDebut;  
    // Date et heure de fin de validité (peut être null pour abonnement sans expiration)
    private LocalDateTime dateFin; 
    // Statut actuel (ex: "ACTIF", "SUSPENDU", "RESILIE")
    private String statut;           
    
    /**
     * Constructeur par défaut.
     * Nécessaire pour certaines librairies (JPA, sérialisation, etc.)
     */
    public Abonnement() {}
    
    /**
     * Constructeur complet avec tous les attributs.
     * 
     * @param idAbonnement Identifiant unique de l'abonnement
     * @param idUsager Identifiant de l'usager propriétaire
     * @param libelleAbonnement Libellé descriptif
     * @param typeAbonnement Type/catégorie de l'abonnement
     * @param tarifAbonnement Prix en euros
     * @param dateDebut Date de début de validité
     * @param dateFin Date de fin de validité (peut être null)
     * @param statut Statut initial de l'abonnement
     */
    public Abonnement(String idAbonnement, int idUsager, String libelleAbonnement, 
                     String typeAbonnement, double tarifAbonnement, 
                     LocalDateTime dateDebut, LocalDateTime dateFin, String statut) {
        this.idAbonnement = idAbonnement;
        this.idUsager = idUsager;
        this.libelleAbonnement = libelleAbonnement;
        this.typeAbonnement = typeAbonnement;
        this.tarifAbonnement = tarifAbonnement;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
    }
    
    // ==================== GETTERS & SETTERS ====================
    // Méthodes d'accès standard pour chaque attribut
    
    public String getIdAbonnement() {
        return idAbonnement;
    }
    
    public void setIdAbonnement(String idAbonnement) {
        this.idAbonnement = idAbonnement;
    }
    
    public int getIdUsager() {
        return idUsager;
    }
    
    public void setIdUsager(int idUsager) {
        this.idUsager = idUsager;
    }
    
    public String getLibelleAbonnement() {
        return libelleAbonnement;
    }
    
    public void setLibelleAbonnement(String libelleAbonnement) {
        this.libelleAbonnement = libelleAbonnement;
    }
    
    public String getTypeAbonnement() {
        return typeAbonnement;
    }
    
    public void setTypeAbonnement(String typeAbonnement) {
        this.typeAbonnement = typeAbonnement;
    }
    
    public double getTarifAbonnement() {
        return tarifAbonnement;
    }
    
    public void setTarifAbonnement(double tarifAbonnement) {
        this.tarifAbonnement = tarifAbonnement;
    }
    
    public LocalDateTime getDateDebut() {
        return dateDebut;
    }
    
    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }
    
    public LocalDateTime getDateFin() {
        return dateFin;
    }
    
    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    // ==================== MÉTHODES MÉTIER ====================
    
    /**
     * Vérifie si l'abonnement est actuellement actif.
     * Un abonnement est considéré actif si :
     * 1. Son statut est "ACTIF"
     * 2. La date courante est après la date de début (si définie)
     * 3. La date courante est avant la date de fin (si définie)
     * 
     * @return true si l'abonnement est actif, false sinon
     */
    public boolean estActif() {
        // Vérifier le statut (doit être "ACTIF")
        if (statut != null && !"ACTIF".equals(statut)) {
            return false;
        }
        
        LocalDateTime maintenant = LocalDateTime.now();
        
        // Vérifier que la date courante est après la date de début (si elle existe)
        if (dateDebut != null && dateDebut.isAfter(maintenant)) {
            return false; // L'abonnement n'a pas encore commencé
        }
        
        // Vérifier que la date courante est avant la date de fin (si elle existe)
        if (dateFin != null && dateFin.isBefore(maintenant)) {
            return false; // L'abonnement a expiré
        }
        
        // Si pas de date de fin (NULL), l'abonnement n'expire jamais
        // Toutes les conditions sont remplies
        return true;
    }
    
    /**
     * Vérifie si l'abonnement est de type "zone bleue".
     * La vérification se fait sur le type ou par recherche dans le libellé.
     * 
     * @return true si c'est un abonnement zone bleue, false sinon
     */
    public boolean estZoneBleue() {
        return "ZONE_BLEUE".equals(typeAbonnement) || 
               (libelleAbonnement != null && libelleAbonnement.toLowerCase().contains("bleue"));
    }
    
    /**
     * Vérifie si l'abonnement est gratuit (tarif à 0€).
     * 
     * @return true si le tarif est égal à 0.0, false sinon
     */
    public boolean estGratuit() {
        return tarifAbonnement == 0.0;
    }
    
    /**
     * Vérifie si l'abonnement est expiré (date de fin dépassée).
     * Ne tient pas compte du statut, seulement de la date.
     * 
     * @return true si la date de fin est passée, false sinon (ou si dateFin est null)
     */
    public boolean estExpire() {
        LocalDateTime maintenant = LocalDateTime.now();
        return dateFin != null && dateFin.isBefore(maintenant);
    }
    
    // ==================== MÉTHODES STANDARD ====================
    
    /**
     * Représentation textuelle de l'objet pour le débogage et l'affichage.
     * 
     * @return Une chaîne formatée contenant les principales informations
     */
    @Override
    public String toString() {
        return "Abonnement [id=" + idAbonnement + 
               ", usager=" + idUsager + 
               ", libellé=" + libelleAbonnement + 
               ", type=" + typeAbonnement + 
               ", tarif=" + tarifAbonnement + "€" +
               ", statut=" + statut + 
               ", début=" + dateDebut + 
               ", fin=" + dateFin + "]";
    }
}
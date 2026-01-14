package modele;

import java.time.LocalDateTime;

public class Abonnement {
    private String idAbonnement;
    private int idUsager;  
    private String libelleAbonnement;
    private String typeAbonnement;  
    private double tarifAbonnement;  
    private LocalDateTime dateDebut;  
    private LocalDateTime dateFin; 
    private String statut;           
    
    public Abonnement() {}
    
    // Constructeur complet
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
    
    // Méthode pour vérifier si l'abonnement est actif
    public boolean estActif() {
        // Vérifier le statut
        if (statut != null && !"ACTIF".equals(statut)) {
            return false;
        }
        
        LocalDateTime maintenant = LocalDateTime.now();
        
        // Vérifier les dates seulement si elles sont définies
        if (dateDebut != null && dateDebut.isAfter(maintenant)) {
            return false; // Pas encore commencé
        }
        
        if (dateFin != null && dateFin.isBefore(maintenant)) {
            return false; // Expiré
        }
        
        // Si pas de date de fin (NULL), l'abonnement n'expire jamais
        return true;
    }
    
    // Méthode pour vérifier si c'est un abonnement zone bleue
    public boolean estZoneBleue() {
        return "ZONE_BLEUE".equals(typeAbonnement) || 
               (libelleAbonnement != null && libelleAbonnement.toLowerCase().contains("bleue"));
    }
    
    // Méthode pour vérifier si l'abonnement est gratuit
    public boolean estGratuit() {
        return tarifAbonnement == 0.0;
    }
    
    // Méthode pour vérifier si l'abonnement a expiré
    public boolean estExpire() {
        LocalDateTime maintenant = LocalDateTime.now();
        return dateFin != null && dateFin.isBefore(maintenant);
    }
    
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
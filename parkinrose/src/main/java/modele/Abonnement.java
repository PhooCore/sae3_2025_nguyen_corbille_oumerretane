package modele;

public class Abonnement {
    private String idAbonnement;
    private String libelleAbonnement;
    private double tarifApplique;
    
    public Abonnement() {}
    
    public Abonnement(String idAbonnement, String libelleAbonnement, double tarifApplique) {
        this.idAbonnement = idAbonnement;
        this.libelleAbonnement = libelleAbonnement;
        this.tarifApplique = tarifApplique;
    }
    
    public String getIdAbonnement() {
        return idAbonnement;
    }
    
    public void setIdAbonnement(String idAbonnement) {
        this.idAbonnement = idAbonnement;
    }
    
    public String getLibelleAbonnement() {
        return libelleAbonnement;
    }
    
    public void setLibelleAbonnement(String libelleAbonnement) {
        this.libelleAbonnement = libelleAbonnement;
    }
    
    public double getTarifAbonnement() {
        return tarifApplique;
    }
    
    public void setTarifAbonnement(double tarifApplique) {
        this.tarifApplique = tarifApplique;
    }

    @Override
    public String toString() {
        return "Abonnement [idAbonnement=" + idAbonnement + ", libelleAbonnement=" + libelleAbonnement
                + ", tarifApplique=" + tarifApplique + "]";
    }
}
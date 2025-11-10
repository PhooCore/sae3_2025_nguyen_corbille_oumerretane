package modèle;

import java.time.LocalDateTime;

public class Stationnement {
    private int idStationnement;
    private int idUsager;
    private String typeVehicule;
    private String plaqueImmatriculation;
    private String zone;
    private int dureeHeures;
    private int dureeMinutes;
    private double cout;
    private LocalDateTime dateCreation;
    private LocalDateTime dateFin;
    private LocalDateTime heureArrivee;
    private LocalDateTime heureDepart;
    private String statut;
    private String typeStationnement;
    private String statutPaiement;
    private String idPaiement;

    // CONSTRUCTEUR PAR DÉFAUT - AJOUT IMPORTANT
    public Stationnement() {
        // Constructeur vide nécessaire pour le DAO
    }

    // Constructeur pour voirie (paiement immédiat)
    public Stationnement(int idUsager, String typeVehicule, String plaqueImmatriculation, 
                        String zone, int dureeHeures, int dureeMinutes, double cout, String idPaiement) {
        this.idUsager = idUsager;
        this.typeVehicule = typeVehicule;
        this.plaqueImmatriculation = plaqueImmatriculation;
        this.zone = zone;
        this.dureeHeures = dureeHeures;
        this.dureeMinutes = dureeMinutes;
        this.cout = cout;
        this.idPaiement = idPaiement;
        this.dateCreation = LocalDateTime.now();
        this.statut = "ACTIF";
        this.typeStationnement = "VOIRIE";
        this.statutPaiement = "PAYE";
    }

    // Constructeur pour parking (paiement différé)
    public Stationnement(int idUsager, String typeVehicule, String plaqueImmatriculation, 
                        String nomParking, LocalDateTime heureArrivee) {
        this.idUsager = idUsager;
        this.typeVehicule = typeVehicule;
        this.plaqueImmatriculation = plaqueImmatriculation;
        this.zone = nomParking;
        this.heureArrivee = heureArrivee;
        this.dateCreation = LocalDateTime.now();
        this.statut = "ACTIF";
        this.typeStationnement = "PARKING";
        this.statutPaiement = "NON_PAYE";
        this.cout = 0.0;
    }

    // Getters et Setters
    public int getIdStationnement() { return idStationnement; }
    public void setIdStationnement(int idStationnement) { this.idStationnement = idStationnement; }

    public int getIdUsager() { return idUsager; }
    public void setIdUsager(int idUsager) { this.idUsager = idUsager; }

    public String getTypeVehicule() { return typeVehicule; }
    public void setTypeVehicule(String typeVehicule) { this.typeVehicule = typeVehicule; }

    public String getPlaqueImmatriculation() { return plaqueImmatriculation; }
    public void setPlaqueImmatriculation(String plaqueImmatriculation) { this.plaqueImmatriculation = plaqueImmatriculation; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public int getDureeHeures() { return dureeHeures; }
    public void setDureeHeures(int dureeHeures) { this.dureeHeures = dureeHeures; }

    public int getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(int dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public double getCout() { return cout; }
    public void setCout(double cout) { this.cout = cout; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public LocalDateTime getHeureArrivee() { return heureArrivee; }
    public void setHeureArrivee(LocalDateTime heureArrivee) { this.heureArrivee = heureArrivee; }

    public LocalDateTime getHeureDepart() { return heureDepart; }
    public void setHeureDepart(LocalDateTime heureDepart) { this.heureDepart = heureDepart; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getTypeStationnement() { return typeStationnement; }
    public void setTypeStationnement(String typeStationnement) { this.typeStationnement = typeStationnement; }

    public String getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(String statutPaiement) { this.statutPaiement = statutPaiement; }

    public String getIdPaiement() { return idPaiement; }
    public void setIdPaiement(String idPaiement) { this.idPaiement = idPaiement; }

    // Méthodes utilitaires
    public boolean estParking() {
        return "PARKING".equals(typeStationnement);
    }
    
    public boolean estVoirie() {
        return "VOIRIE".equals(typeStationnement);
    }

    public int getDureeTotaleMinutes() {
        return (dureeHeures * 60) + dureeMinutes;
    }

    public boolean estActif() {
        return "ACTIF".equals(statut);
    }

    @Override
    public String toString() {
        return "Stationnement{" +
                "id=" + idStationnement +
                ", type='" + typeStationnement + '\'' +
                ", véhicule='" + typeVehicule + '\'' +
                ", plaque='" + plaqueImmatriculation + '\'' +
                ", zone='" + zone + '\'' +
                ", statut='" + statut + '\'' +
                ", paiement='" + statutPaiement + '\'' +
                '}';
    }
}
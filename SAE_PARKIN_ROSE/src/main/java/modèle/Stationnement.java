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
    private LocalDateTime dateFin; // Nouveau champ
    private String statut;
    private String idPaiement;

    // Constructeurs
    public Stationnement() {}

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

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getIdPaiement() { return idPaiement; }
    public void setIdPaiement(String idPaiement) { this.idPaiement = idPaiement; }

    // Méthode utilitaire pour obtenir la durée totale en minutes
    public int getDureeTotaleMinutes() {
        return (dureeHeures * 60) + dureeMinutes;
    }

    // Vérifie si le stationnement est encore actif
    public boolean estActif() {
        return "ACTIF".equals(statut) && (dateFin == null || LocalDateTime.now().isBefore(dateFin));
    }

    @Override
    public String toString() {
        return "Stationnement{" +
                "id=" + idStationnement +
                ", véhicule='" + typeVehicule + '\'' +
                ", plaque='" + plaqueImmatriculation + '\'' +
                ", zone='" + zone + '\'' +
                ", durée=" + dureeHeures + "h" + dureeMinutes +
                ", coût=" + cout + "€" +
                ", statut='" + statut + '\'' +
                '}';
    }
}
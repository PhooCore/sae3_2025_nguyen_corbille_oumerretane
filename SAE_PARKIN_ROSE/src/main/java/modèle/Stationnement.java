package modèle;

import java.time.LocalDateTime;

public class Stationnement {
    private int idStationnement;
    private int idUsager;
    private String idTarification;
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

    // CONSTRUCTEUR PAR DÉFAUT
    public Stationnement() {
    }

    // Constructeur pour voirie (paiement immédiat)
    public Stationnement(int idUsager, String typeVehicule, String plaqueImmatriculation, 
                        String idTarification, String zone, int dureeHeures, int dureeMinutes, 
                        double cout, String idPaiement) {
        this.idUsager = idUsager;
        this.typeVehicule = typeVehicule;
        this.plaqueImmatriculation = plaqueImmatriculation;
        this.idTarification = idTarification;
        this.zone = zone;
        this.dureeHeures = dureeHeures;
        this.dureeMinutes = dureeMinutes;
        this.cout = cout;
        this.idPaiement = idPaiement;
        this.dateCreation = LocalDateTime.now();
        this.statut = "ACTIF";
        this.typeStationnement = "VOIRIE";
        this.statutPaiement = "PAYE";
        
        // Calcul de la date de fin pour la voirie
        int dureeTotaleMinutes = (dureeHeures * 60) + dureeMinutes;
        this.dateFin = this.dateCreation.plusMinutes(dureeTotaleMinutes);
    }

    // Constructeur pour parking (paiement différé)
    public Stationnement(int idUsager, String typeVehicule, String plaqueImmatriculation, 
                        String idTarification, String nomParking, LocalDateTime heureArrivee) {
        this.idUsager = idUsager;
        this.typeVehicule = typeVehicule;
        this.plaqueImmatriculation = plaqueImmatriculation;
        this.idTarification = idTarification;
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

    public String getIdTarification() { return idTarification; }
    public void setIdTarification(String idTarification) { this.idTarification = idTarification; }

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

    public boolean estTermine() {
        return "TERMINE".equals(statut);
    }

    public boolean estExpire() {
        return "EXPIRE".equals(statut);
    }

    /**
     * Vérifie si le stationnement est expiré (date de fin dépassée)
     */
    public boolean estTempsEcoule() {
        if (estVoirie() && dateFin != null) {
            return LocalDateTime.now().isAfter(dateFin);
        }
        return false;
    }

    /**
     * Calcule la durée écoulée pour un stationnement parking
     */
    public long getDureeEcouleeMinutes() {
        if (estParking() && heureArrivee != null) {
            LocalDateTime endTime = (heureDepart != null) ? heureDepart : LocalDateTime.now();
            return java.time.Duration.between(heureArrivee, endTime).toMinutes();
        }
        return 0;
    }

    /**
     * Calcule le temps restant pour un stationnement voirie
     */
    public long getTempsRestantMinutes() {
        if (estVoirie() && dateFin != null) {
            long minutesRestantes = java.time.Duration.between(LocalDateTime.now(), dateFin).toMinutes();
            return Math.max(0, minutesRestantes); // Évite les valeurs négatives
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Stationnement{" +
                "id=" + idStationnement +
                ", type='" + typeStationnement + '\'' +
                ", véhicule='" + typeVehicule + '\'' +
                ", plaque='" + plaqueImmatriculation + '\'' +
                ", zone='" + zone + '\'' +
                ", tarification='" + idTarification + '\'' +
                ", statut='" + statut + '\'' +
                ", paiement='" + statutPaiement + '\'' +
                '}';
    }

    /**
     * Méthode pour l'affichage utilisateur
     */
    public String getAffichageSimplifie() {
        String base = typeVehicule + " - " + plaqueImmatriculation + " (" + zone + ")";
        if (estVoirie()) {
            return base + " - " + dureeHeures + "h" + dureeMinutes + "min";
        } else {
            return base + " - Parking";
        }
    }

    /**
     * Vérifie si le stationnement nécessite un paiement
     */
    public boolean necessitePaiement() {
        return "NON_PAYE".equals(statutPaiement) && estParking();
    }

    /**
     * Marque le stationnement comme payé
     */
    public void marquerCommePaye(String idPaiement, double montant) {
        this.statutPaiement = "PAYE";
        this.idPaiement = idPaiement;
        this.cout = montant;
        if (estParking()) {
            this.statut = "TERMINE";
        }
    }
}
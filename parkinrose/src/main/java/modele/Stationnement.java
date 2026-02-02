package modele;

import java.time.LocalDateTime;

/**
 * Classe représentant un stationnement (voiture ou moto) dans le système.
 * Gère à la fois les stationnements en voirie (avec durée prédéfinie) 
 * et les stationnements en parking (avec paiement à la sortie).
 */
public class Stationnement {
    // ==================== ATTRIBUTS PRINCIPAUX ====================
    
    // Identifiant unique du stationnement en base de données
    private int idStationnement;
    // Identifiant de l'usager propriétaire du véhicule
    private int idUsager;
    // Identifiant de la tarification appliquée
    private String idTarification;
    // Type de véhicule (ex: "VOITURE", "MOTO", "CAMION")
    private String typeVehicule;
    // Plaque d'immatriculation du véhicule
    private String plaqueImmatriculation;
    // Zone de stationnement (nom de rue ou nom de parking)
    private String zone;
    // Durée prévue en heures (pour voirie uniquement)
    private int dureeHeures;
    // Durée prévue en minutes (pour voirie uniquement)
    private int dureeMinutes;
    // Coût total du stationnement en euros
    private double cout;
    // Date et heure de création de l'enregistrement
    private LocalDateTime dateCreation;
    // Date et heure de fin prévue (pour voirie uniquement)
    private LocalDateTime dateFin;
    // Heure d'arrivée dans le parking (pour parking uniquement)
    private LocalDateTime heureArrivee;
    // Heure de départ du parking (pour parking uniquement)
    private LocalDateTime heureDepart;
    // Statut actuel (ex: "ACTIF", "TERMINE", "EXPIRE", "ANNULE")
    private String statut;
    // Type de stationnement (ex: "VOIRIE", "PARKING")
    private String typeStationnement;
    // Statut du paiement (ex: "PAYE", "NON_PAYE", "EN_ATTENTE")
    private String statutPaiement;
    // Identifiant du paiement associé (si effectué)
    private String idPaiement;

    // ==================== CONSTRUCTEURS ====================

    /**
     * Constructeur par défaut.
     * Nécessaire pour les frameworks (JPA, Hibernate, etc.)
     */
    public Stationnement() {
    }

    /**
     * Constructeur pour un stationnement en voirie.
     * Crée un stationnement avec durée prédéfinie et paiement immédiat.
     * 
     * @param idUsager Identifiant de l'usager
     * @param typeVehicule Type de véhicule
     * @param plaqueImmatriculation Plaque d'immatriculation
     * @param idTarification Identifiant de la tarification
     * @param zone Zone/Rue de stationnement
     * @param dureeHeures Durée en heures
     * @param dureeMinutes Durée en minutes
     * @param cout Coût total en euros
     * @param idPaiement Identifiant du paiement effectué
     */
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
        this.dateCreation = LocalDateTime.now();  // Date de création actuelle
        this.statut = "ACTIF";                    // Statut initial
        this.typeStationnement = "VOIRIE";        // Type voirie
        this.statutPaiement = "PAYE";             // Paiement immédiat pour voirie
        
        // Calcul de la date de fin en ajoutant la durée totale
        int dureeTotaleMinutes = (dureeHeures * 60) + dureeMinutes;
        this.dateFin = this.dateCreation.plusMinutes(dureeTotaleMinutes);
    }

    /**
     * Constructeur pour un stationnement en parking.
     * Crée un stationnement sans durée prédéfinie, avec paiement à la sortie.
     * 
     * @param idUsager Identifiant de l'usager
     * @param typeVehicule Type de véhicule
     * @param plaqueImmatriculation Plaque d'immatriculation
     * @param idTarification Identifiant de la tarification
     * @param nomParking Nom du parking
     * @param heureArrivee Heure d'entrée dans le parking
     */
    public Stationnement(int idUsager, String typeVehicule, String plaqueImmatriculation, 
                        String idTarification, String nomParking, LocalDateTime heureArrivee) {
        this.idUsager = idUsager;
        this.typeVehicule = typeVehicule;
        this.plaqueImmatriculation = plaqueImmatriculation;
        this.idTarification = idTarification;
        this.zone = nomParking;
        this.heureArrivee = heureArrivee;
        this.dateCreation = LocalDateTime.now();  // Date de création actuelle
        this.statut = "ACTIF";                    // Statut initial
        this.typeStationnement = "PARKING";       // Type parking
        this.statutPaiement = "NON_PAYE";         // Paiement à effectuer à la sortie
        this.cout = 0.0;                          // Coût initialisé à 0
    }

    // ==================== GETTERS & SETTERS ====================

    public int getIdStationnement() { 
        return idStationnement; 
    }
    
    public void setIdStationnement(int idStationnement) { 
        this.idStationnement = idStationnement; 
    }

    public int getIdUsager() { 
        return idUsager; 
    }
    
    public void setIdUsager(int idUsager) { 
        this.idUsager = idUsager; 
    }

    public String getIdTarification() { 
        return idTarification; 
    }
    
    public void setIdTarification(String idTarification) { 
        this.idTarification = idTarification; 
    }

    public String getTypeVehicule() { 
        return typeVehicule; 
    }
    
    public void setTypeVehicule(String typeVehicule) { 
        this.typeVehicule = typeVehicule; 
    }

    public String getPlaqueImmatriculation() { 
        return plaqueImmatriculation; 
    }
    
    public void setPlaqueImmatriculation(String plaqueImmatriculation) { 
        this.plaqueImmatriculation = plaqueImmatriculation; 
    }

    public String getZone() { 
        return zone; 
    }
    
    public void setZone(String zone) { 
        this.zone = zone; 
    }

    public int getDureeHeures() { 
        return dureeHeures; 
    }
    
    public void setDureeHeures(int dureeHeures) { 
        this.dureeHeures = dureeHeures; 
    }

    public int getDureeMinutes() { 
        return dureeMinutes; 
    }
    
    public void setDureeMinutes(int dureeMinutes) { 
        this.dureeMinutes = dureeMinutes; 
    }

    public double getCout() { 
        return cout; 
    }
    
    public void setCout(double cout) { 
        this.cout = cout; 
    }

    public LocalDateTime getDateCreation() { 
        return dateCreation; 
    }
    
    public void setDateCreation(LocalDateTime dateCreation) { 
        this.dateCreation = dateCreation; 
    }

    public LocalDateTime getDateFin() { 
        return dateFin; 
    }
    
    public void setDateFin(LocalDateTime dateFin) { 
        this.dateFin = dateFin; 
    }

    public LocalDateTime getHeureArrivee() { 
        return heureArrivee; 
    }
    
    public void setHeureArrivee(LocalDateTime heureArrivee) { 
        this.heureArrivee = heureArrivee; 
    }

    public LocalDateTime getHeureDepart() { 
        return heureDepart; 
    }
    
    public void setHeureDepart(LocalDateTime heureDepart) { 
        this.heureDepart = heureDepart; 
    }

    public String getStatut() { 
        return statut; 
    }
    
    public void setStatut(String statut) { 
        this.statut = statut; 
    }

    public String getTypeStationnement() { 
        return typeStationnement; 
    }
    
    public void setTypeStationnement(String typeStationnement) { 
        this.typeStationnement = typeStationnement; 
    }

    public String getStatutPaiement() { 
        return statutPaiement; 
    }
    
    public void setStatutPaiement(String statutPaiement) { 
        this.statutPaiement = statutPaiement; 
    }

    public String getIdPaiement() { 
        return idPaiement; 
    }
    
    public void setIdPaiement(String idPaiement) { 
        this.idPaiement = idPaiement; 
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Vérifie si le stationnement est en parking.
     * 
     * @return true si typeStationnement = "PARKING", false sinon
     */
    public boolean estParking() {
        return "PARKING".equals(typeStationnement);
    }
    
    /**
     * Vérifie si le stationnement est en voirie.
     * 
     * @return true si typeStationnement = "VOIRIE", false sinon
     */
    public boolean estVoirie() {
        return "VOIRIE".equals(typeStationnement);
    }

    /**
     * Calcule la durée totale en minutes.
     * 
     * @return Durée totale en minutes (heures * 60 + minutes)
     */
    public int getDureeTotaleMinutes() {
        return (dureeHeures * 60) + dureeMinutes;
    }

    /**
     * Vérifie si le stationnement est actif.
     * 
     * @return true si statut = "ACTIF", false sinon
     */
    public boolean estActif() {
        return "ACTIF".equals(statut);
    }

    /**
     * Vérifie si le stationnement est terminé.
     * 
     * @return true si statut = "TERMINE", false sinon
     */
    public boolean estTermine() {
        return "TERMINE".equals(statut);
    }

    /**
     * Vérifie si le stationnement est expiré.
     * 
     * @return true si statut = "EXPIRE", false sinon
     */
    public boolean estExpire() {
        return "EXPIRE".equals(statut);
    }

    /**
     * Vérifie si le temps est écoulé (pour voirie uniquement).
     * 
     * @return true si la date actuelle dépasse la date de fin, false sinon
     */
    public boolean estTempsEcoule() {
        if (estVoirie() && dateFin != null) {
            return LocalDateTime.now().isAfter(dateFin);
        }
        return false;
    }

    /**
     * Calcule la durée écoulée en minutes (pour parking uniquement).
     * 
     * @return Durée écoulée en minutes, ou 0 si pas applicable
     */
    public long getDureeEcouleeMinutes() {
        if (estParking() && heureArrivee != null) {
            LocalDateTime endTime = (heureDepart != null) ? heureDepart : LocalDateTime.now();
            return java.time.Duration.between(heureArrivee, endTime).toMinutes();
        }
        return 0;
    }

    /**
     * Calcule le temps restant en minutes (pour voirie uniquement).
     * 
     * @return Temps restant en minutes (minimum 0), ou 0 si pas applicable
     */
    public long getTempsRestantMinutes() {
        if (estVoirie() && dateFin != null) {
            long minutesRestantes = java.time.Duration.between(LocalDateTime.now(), dateFin).toMinutes();
            return Math.max(0, minutesRestantes);
        }
        return 0;
    }

    /**
     * Formate le temps restant pour l'affichage (HH:MM).
     * 
     * @return Format "Xh Ymin" ou "Temps écoulé"
     */
    public String getTempsRestantFormate() {
        long minutesRestantes = getTempsRestantMinutes();
        if (minutesRestantes <= 0) {
            return "Temps écoulé";
        }
        long heures = minutesRestantes / 60;
        long minutes = minutesRestantes % 60;
        return heures + "h " + minutes + "min";
    }

    /**
     * Vérifie si un paiement est nécessaire.
     * Uniquement pour les parkings avec statut "NON_PAYE".
     * 
     * @return true si paiement requis, false sinon
     */
    public boolean necessitePaiement() {
        return "NON_PAYE".equals(statutPaiement) && estParking();
    }

    /**
     * Marque le stationnement comme payé.
     * Met à jour le statut de paiement et le coût.
     * Pour les parkings, marque également le stationnement comme terminé.
     * 
     * @param idPaiement Identifiant du paiement effectué
     * @param montant Montant payé en euros
     */
    public void marquerCommePaye(String idPaiement, double montant) {
        this.statutPaiement = "PAYE";
        this.idPaiement = idPaiement;
        this.cout = montant;
        if (estParking()) {
            this.statut = "TERMINE";
            this.heureDepart = LocalDateTime.now();  // Heure de départ au moment du paiement
        }
    }

    /**
     * Vérifie si le stationnement est payé.
     * 
     * @return true si statutPaiement = "PAYE", false sinon
     */
    public boolean estPaye() {
        return "PAYE".equals(statutPaiement);
    }

    /**
     * Calcule le coût estimé pour un parking (basé sur la durée).
     * 
     * @param tarifHoraire Tarif horaire en euros
     * @return Coût estimé en euros
     */
    public double calculerCoutEstime(double tarifHoraire) {
        if (estParking()) {
            long dureeMinutes = getDureeEcouleeMinutes();
            double dureeHeures = dureeMinutes / 60.0;
            // Arrondi à l'heure supérieure
            dureeHeures = Math.ceil(dureeHeures * 2) / 2.0;  // Par tranche de 30 minutes
            return dureeHeures * tarifHoraire;
        }
        return cout;  // Pour voirie, retourner le coût déjà fixé
    }

    // ==================== MÉTHODES STANDARD ====================

    /**
     * Représentation textuelle détaillée pour le débogage.
     * 
     * @return Une chaîne formatée contenant toutes les informations
     */
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
     * Représentation simplifiée pour l'affichage utilisateur.
     * 
     * @return Une chaîne formatée simplifiée
     */
    public String getAffichageSimplifie() {
        String base = typeVehicule + " - " + plaqueImmatriculation + " (" + zone + ")";
        if (estVoirie()) {
            return base + " - " + dureeHeures + "h" + dureeMinutes + "min";
        } else {
            return base + " - Parking";
        }
    }
}
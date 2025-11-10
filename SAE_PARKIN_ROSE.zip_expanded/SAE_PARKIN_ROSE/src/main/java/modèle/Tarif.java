package modèle;

import java.time.LocalTime;

public class Tarif {
    // Attributs de la classe
    private String idTarification;    // Identifiant unique (ex: "TARIF_JAUNE")
    private double tarifParHeure;     // Prix en euros par heure
    private LocalTime dureeMax;       // Durée maximale autorisée
    private String nomZone;           // Nom de la zone déduit de l'ID

    /**
     * Constructeur de la classe Tarif
     * @param idTarification identifiant de la tarification (ex: "TARIF_JAUNE")
     * @param tarifParHeure tarif horaire en euros
     * @param dureeMax durée maximale de stationnement autorisée
     */
    public Tarif(String idTarification, double tarifParHeure, LocalTime dureeMax) {
        this.idTarification = idTarification;
        this.tarifParHeure = tarifParHeure;
        this.dureeMax = dureeMax;
        // Extraction du nom de zone depuis l'ID (ex: "TARIF_JAUNE" → "JAUNE")
        this.nomZone = idTarification.replace("TARIF_", "");
    }


    /**
     * @return l'identifiant de la tarification
     */
    public String getIdTarification() { 
        return idTarification; 
    }
    
    /**
     * @return le tarif horaire en euros
     */
    public double getTarifParHeure() { 
        return tarifParHeure; 
    }
    
    /**
     * @return la durée maximale autorisée sous forme de LocalTime
     */
    public LocalTime getDureeMax() { 
        return dureeMax; 
    }
    
    /**
     * @return le nom de la zone (ex: "JAUNE", "BLEUE")
     */
    public String getNomZone() { 
        return nomZone; 
    }

    /**
     * Convertit la durée maximale en minutes pour faciliter les calculs
     * @return la durée maximale en minutes
     */
    public int getDureeMaxMinutes() {
        return dureeMax.getHour() * 60 + dureeMax.getMinute();
    }

    /**
     * Calcule le coût du stationnement selon la durée et le type de tarif
     * Gère les cas particuliers : forfait soirée et zone gratuite
     * @param dureeMinutes durée du stationnement en minutes
     * @return le coût total en euros
     */
    public double calculerCout(int dureeMinutes) {
        // === CAS PARTICULIER : TARIF SOIREE (forfait fixe) ===
        if (idTarification.equals("TARIF_SOIREE")) {
            return 5.90; // Forfait fixe pour la soirée
        }
        
        // === CAS PARTICULIER : ZONE BLEUE (gratuite) ===
        if (idTarification.equals("TARIF_BLEUE")) {
            return 0.00; // Stationnement gratuit
        }
        
        // === CAS GENERAL : calcul proportionnel à la durée ===
        // Conversion minutes → heures (ex: 90 minutes = 1.5 heures)
        double dureeHeures = dureeMinutes / 60.0;
        // Calcul : durée en heures × tarif horaire
        return dureeHeures * tarifParHeure;
    }

    /**
     * Génère un texte formaté pour l'affichage dans les listes déroulantes
     * Adapte le format selon le type de tarif
     * @return une chaîne formatée pour l'affichage utilisateur
     */
    public String getAffichage() {
        // Formatage du nom de zone : première lettre majuscule, reste minuscule
        // Ex: "JAUNE" → "Jaune", "BLEUE" → "Bleue"
        String nom = nomZone.substring(0, 1).toUpperCase() + nomZone.substring(1).toLowerCase();
        
        // === CAS TARIF SOIREE : affichage forfaitaire ===
        if (idTarification.equals("TARIF_SOIREE")) {
            return "Zone " + nom + " - " + 
                   String.format("%.2f", tarifParHeure) + "€ forfait (" + 
                   dureeMax.getHour() + "h" + 
                   // Affichage conditionnel des minutes si > 0
                   (dureeMax.getMinute() > 0 ? dureeMax.getMinute() : "") + ")";
        } 
        // === CAS ZONE BLEUE : affichage gratuit ===
        else if (idTarification.equals("TARIF_BLEUE")) {
            return "Zone " + nom + " - Gratuite (max " + 
                   dureeMax.getHour() + "h" + 
                   (dureeMax.getMinute() > 0 ? dureeMax.getMinute() : "") + ")";
        } 
        // === CAS GENERAL : affichage tarif horaire ===
        else {
            return "Zone " + nom + " - " + 
                   String.format("%.2f", tarifParHeure) + "€/h (max " + 
                   dureeMax.getHour() + "h" + 
                   (dureeMax.getMinute() > 0 ? dureeMax.getMinute() : "") + ")";
        }
    }
}
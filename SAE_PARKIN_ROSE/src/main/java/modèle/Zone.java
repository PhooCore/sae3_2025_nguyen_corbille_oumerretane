package modèle;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.DayOfWeek;

public class Zone {
    private String idZone;
    private String libelleZone;
    private String couleurZone;
    private double tarifParHeure;
    private LocalTime dureeMax;

    public Zone(String idZone, String libelleZone, String couleurZone, 
                double tarifParHeure, LocalTime dureeMax) {
        this.idZone = idZone;
        this.libelleZone = libelleZone;
        this.couleurZone = couleurZone;
        this.tarifParHeure = tarifParHeure;
        this.dureeMax = dureeMax;
    }

    // Getters
    public String getIdZone() { return idZone; }
    public String getLibelleZone() { return libelleZone; }
    public String getCouleurZone() { return couleurZone; }
    public double getTarifParHeure() { return tarifParHeure; }
    public LocalTime getDureeMax() { return dureeMax; }

    public int getDureeMaxMinutes() {
        return dureeMax.getHour() * 60 + dureeMax.getMinute();
    }

    /**
     * Calcule le coût selon les règles spécifiques de chaque zone
     * Version simplifiée sans les horaires complexes
     */
    public double calculerCout(int dureeMinutes) {
        switch (idZone) {
            case "ZONE_BLEUE":
                return calculerCoutBleue(dureeMinutes);
            case "ZONE_VERTE":
                return calculerCoutVerte(dureeMinutes);
            case "ZONE_JAUNE":
                return calculerCoutJaune(dureeMinutes);
            case "ZONE_ORANGE":
                return calculerCoutOrange(dureeMinutes);
            case "ZONE_ROUGE":
                return calculerCoutRouge(dureeMinutes);
            default:
                return calculerCoutNormal(dureeMinutes);
        }
    }

    /**
     * Zone Bleue - Gratuit 1h30, puis majoration
     */
    private double calculerCoutBleue(int dureeMinutes) {
        if (dureeMinutes <= 90) { // 1h30
            return 0.00; // Gratuit
        } else if (dureeMinutes <= 120) { // 2h
            return 2.00;
        } else { // Au-delà de 2h
            return 2.00 + 30.00; // Majoration 30€
        }
    }

    /**
     * Zone Verte - Tarif progressif
     */
    private double calculerCoutVerte(int dureeMinutes) {
        if (dureeMinutes <= 60) { // 1h
            return 0.50;
        } else if (dureeMinutes <= 120) { // 2h
            return 1.00;
        } else if (dureeMinutes <= 180) { // 3h
            return 1.50;
        } else if (dureeMinutes <= 240) { // 4h
            return 2.00;
        } else if (dureeMinutes <= 300) { // 5h
            return 2.50;
        } else {
            return 2.50 + 30.00; // Majoration au-delà de 5h
        }
    }

    /**
     * Zone Jaune - Tarif avec majoration
     */
    private double calculerCoutJaune(int dureeMinutes) {
        if (dureeMinutes <= 60) { // 1h
            return 1.50;
        } else if (dureeMinutes <= 120) { // 2h
            return 3.00;
        } else if (dureeMinutes <= 150) { // 2h30
            return 3.00 + 30.00; // Majoration 30€
        } else {
            return 3.00 + 30.00; // Majoration maintenue
        }
    }

    /**
     * Zone Orange - Tarif progressif avec majoration
     */
    private double calculerCoutOrange(int dureeMinutes) {
        if (dureeMinutes <= 60) { // 1h
            return 1.00;
        } else if (dureeMinutes <= 120) { // 2h
            return 2.00;
        } else if (dureeMinutes <= 180) { // 3h
            return 4.00;
        } else if (dureeMinutes <= 240) { // 4h
            return 6.00;
        } else if (dureeMinutes <= 300) { // 5h
            return 6.00 + 30.00; // Majoration 30€
        } else {
            return 6.00 + 30.00; // Majoration maintenue
        }
    }

    /**
     * Zone Rouge - Tarif avec majoration
     */
    private double calculerCoutRouge(int dureeMinutes) {
        // 30 minutes gratuites par demi-journée
        int minutesPayantes = Math.max(0, dureeMinutes - 30);
        
        if (minutesPayantes <= 60) { // 1h payante
            return 1.00;
        } else if (minutesPayantes <= 120) { // 2h payantes
            return 2.00;
        } else if (minutesPayantes <= 180) { // 3h payantes
            return 2.00 + 30.00; // Majoration 30€
        } else {
            return 2.00 + 30.00; // Majoration maintenue
        }
    }

    /**
     * Calcul normal pour les autres zones
     */
    private double calculerCoutNormal(int dureeMinutes) {
        double dureeHeures = dureeMinutes / 60.0;
        return dureeHeures * tarifParHeure;
    }

    public String getAffichage() {
        switch (idZone) {
            case "ZONE_BLEUE":
                return "Zone Bleue - Gratuit 1h30";
            case "ZONE_VERTE":
                return "Zone Verte - 0.50€ (1h), 1€ (2h), 1.50€ (3h), 2€ (4h), +30€ (5h)";
            case "ZONE_JAUNE":
                return "Zone Jaune - 1.50€ (1h), 3€ (2h), +30€ (2h30)";
            case "ZONE_ORANGE":
                return "Zone Orange - 1€ (1h), 2€ (2h), 4€ (3h), 6€ (4h), +30€ (5h)";
            case "ZONE_ROUGE":
                return "Zone Rouge - 30min gratuit, 1€ (1h), 2€ (2h), +30€ (3h)";
            default:
                return libelleZone + " - " + String.format("%.2f", tarifParHeure) + "€/h";
        }
    }
}
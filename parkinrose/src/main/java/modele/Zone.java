package modele;

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

    public String getIdZone() { return idZone; }
    public String getLibelleZone() { return libelleZone; }
    public String getCouleurZone() { return couleurZone; }
    public double getTarifParHeure() { return tarifParHeure; }
    public LocalTime getDureeMax() { return dureeMax; }

    public int getDureeMaxMinutes() {
        return dureeMax.getHour() * 60 + dureeMax.getMinute();
    }

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

    private double calculerCoutBleue(int dureeMinutes) {
        if (dureeMinutes <= 90) {
            return 0.00;
        } else if (dureeMinutes <= 120) {
            return 2.00;
        } else {
            return 2.00 + 30.00;
        }
    }

    private double calculerCoutVerte(int dureeMinutes) {
        if (dureeMinutes <= 60) {
            return 0.50;
        } else if (dureeMinutes <= 120) {
            return 1.00;
        } else if (dureeMinutes <= 180) {
            return 1.50;
        } else if (dureeMinutes <= 240) {
            return 2.00;
        } else if (dureeMinutes <= 300) {
            return 2.50;
        } else {
            return 2.50 + 30.00;
        }
    }

    private double calculerCoutJaune(int dureeMinutes) {
        if (dureeMinutes <= 60) {
            return 1.50;
        } else if (dureeMinutes <= 120) {
            return 3.00;
        } else if (dureeMinutes <= 150) {
            return 3.00 + 30.00;
        } else {
            return 3.00 + 30.00;
        }
    }

    private double calculerCoutOrange(int dureeMinutes) {
        if (dureeMinutes <= 60) {
            return 1.00;
        } else if (dureeMinutes <= 120) {
            return 2.00;
        } else if (dureeMinutes <= 180) {
            return 4.00;
        } else if (dureeMinutes <= 240) {
            return 6.00;
        } else if (dureeMinutes <= 300) {
            return 6.00 + 30.00;
        } else {
            return 6.00 + 30.00;
        }
    }

    private double calculerCoutRouge(int dureeMinutes) {
        if (dureeMinutes <= 30) {
            return 0.00;
        }
        
        int minutesPayantes = dureeMinutes - 30;
        
        if (minutesPayantes <= 60) {
            return 1.00;
        } else if (minutesPayantes <= 120) {
            return 2.00;
        } else if (minutesPayantes <= 150) {
            return 2.00 + 30.00;
        } else {
            return 2.00 + 30.00;
        }
    }
    
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
    
    public double calculerCoutAvecAbonnement(int dureeMinutes, Abonnement abonnement) {
        // Si l'usager a un abonnement actif
        if (abonnement != null && abonnement.estActif()) {
            // IMPORTANT: L'abonnement a déjà été payé !
            // Le tarif de l'abonnement n'est PAS le coût par stationnement
            // C'est le prix de l'abonnement lui-même (déjà réglé)
            
            // Si l'abonnement est gratuit (tarif = 0) → Stationnement gratuit
            if (abonnement.estGratuit()) {
                return 0.0;
            }
            
            // Si c'est un abonnement zone bleue et qu'on est en zone bleue → Gratuit
            if (abonnement.estZoneBleue() && "ZONE_BLEUE".equals(idZone)) {
                return 0.0;
            }
            
            // Pour tous les autres abonnements (hebdo, annuel, etc.)
            // Le stationnement est GRATUIT car l'abonnement est déjà payé
            String idAbo = abonnement.getIdAbonnement();
            if (idAbo != null && (
                idAbo.contains("HEBDO") || 
                idAbo.contains("ANNUEL") || 
                idAbo.contains("RESIDENT") ||
                idAbo.contains("MOTO") ||
                idAbo.contains("PACK")
            )) {
                return 0.0;
            }
            
            // Si c'est "ABO_SIMPLE" (paiement ponctuel) → calcul normal
            if (idAbo != null && idAbo.contains("SIMPLE")) {
                return calculerCout(dureeMinutes);
            }
            
            // Par défaut, si on a un abonnement, le stationnement est gratuit
            return 0.0;
        }
        
        // Pas d'abonnement : calcul normal
        return calculerCout(dureeMinutes);
    }
}
package modele;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.DayOfWeek;

/**
 * Classe représentant une zone de stationnement dans le système.
 * Gère les caractéristiques spécifiques de chaque zone (bleue, verte, etc.)
 * ainsi que les tarifs et durées maximales de stationnement.
 */
public class Zone {
    // ==================== ATTRIBUTS PRINCIPAUX ====================
    
    // Identifiant unique de la zone (ex: "ZONE_BLEUE", "ZONE_VERTE")
    private String idZone;
    // Libellé descriptif de la zone (ex: "Zone Bleue Centre-Ville")
    private String libelleZone;
    // Code couleur associé à la zone pour l'affichage (ex: "#3498db")
    private String couleurZone;
    // Tarif horaire de base en euros (utilisé pour les zones sans tarif spécifique)
    private double tarifParHeure;
    // Durée maximale de stationnement autorisée
    private LocalTime dureeMax;

    // ==================== CONSTRUCTEUR ====================

    /**
     * Constructeur complet pour créer une zone de stationnement.
     * 
     * @param idZone Identifiant unique de la zone
     * @param libelleZone Libellé descriptif
     * @param couleurZone Code couleur pour l'affichage
     * @param tarifParHeure Tarif horaire de base en euros
     * @param dureeMax Durée maximale de stationnement autorisée
     */
    public Zone(String idZone, String libelleZone, String couleurZone, 
                double tarifParHeure, LocalTime dureeMax) {
        this.idZone = idZone;
        this.libelleZone = libelleZone;
        this.couleurZone = couleurZone;
        this.tarifParHeure = tarifParHeure;
        this.dureeMax = dureeMax;
    }

    // ==================== GETTERS ====================

    public String getIdZone() { 
        return idZone; 
    }
    
    public String getLibelleZone() { 
        return libelleZone; 
    }
    
    public String getCouleurZone() { 
        return couleurZone; 
    }
    
    public double getTarifParHeure() { 
        return tarifParHeure; 
    }
    
    public LocalTime getDureeMax() { 
        return dureeMax; 
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Retourne la durée maximale en minutes.
     * 
     * @return Durée maximale en minutes
     */
    public int getDureeMaxMinutes() {
        return dureeMax.getHour() * 60 + dureeMax.getMinute();
    }

    /**
     * Calcule le coût du stationnement selon la durée et la zone.
     * Applique les tarifs spécifiques à chaque type de zone.
     * 
     * @param dureeMinutes Durée de stationnement en minutes
     * @return Coût total en euros
     */
    public double calculerCout(int dureeMinutes) {
        // Vérification de la durée minimale
        if (dureeMinutes <= 0) {
            return 0.0;
        }
        
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
     * Calcule le coût pour la Zone Bleue.
     * Tarif spécifique : gratuit jusqu'à 1h30, puis forfait.
     * 
     * @param dureeMinutes Durée en minutes
     * @return Coût en euros
     */
    private double calculerCoutBleue(int dureeMinutes) {
        if (dureeMinutes <= 90) {
            return 0.00; // Gratuit jusqu'à 1h30
        } else if (dureeMinutes <= 120) {
            return 2.00; // Forfait 2h
        } else {
            return 2.00 + 30.00; // Forfait 2h + amende
        }
    }

    /**
     * Calcule le coût pour la Zone Verte.
     * Tarif progressif avec paliers.
     * 
     * @param dureeMinutes Durée en minutes
     * @return Coût en euros
     */
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
            return 2.50 + 30.00; // Tarif max + amende
        }
    }

    /**
     * Calcule le coût pour la Zone Jaune.
     * Tarif spécifique avec amende au-delà de 2h30.
     * 
     * @param dureeMinutes Durée en minutes
     * @return Coût en euros
     */
    private double calculerCoutJaune(int dureeMinutes) {
        if (dureeMinutes <= 60) {
            return 1.50;
        } else if (dureeMinutes <= 120) {
            return 3.00;
        } else if (dureeMinutes <= 150) {
            return 3.00 + 30.00; // Amende
        } else {
            return 3.00 + 30.00; // Amende + tarif max
        }
    }

    /**
     * Calcule le coût pour la Zone Orange.
     * Tarif progressif avec paliers.
     * 
     * @param dureeMinutes Durée en minutes
     * @return Coût en euros
     */
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
            return 6.00 + 30.00; // Amende
        } else {
            return 6.00 + 30.00; // Amende + tarif max
        }
    }

    /**
     * Calcule le coût pour la Zone Rouge.
     * Gratuit les 30 premières minutes, puis tarif spécifique.
     * 
     * @param dureeMinutes Durée en minutes
     * @return Coût en euros
     */
    private double calculerCoutRouge(int dureeMinutes) {
        // 30 premières minutes gratuites
        if (dureeMinutes <= 30) {
            return 0.00;
        }
        
        int minutesPayantes = dureeMinutes - 30;
        
        if (minutesPayantes <= 60) {
            return 1.00;
        } else if (minutesPayantes <= 120) {
            return 2.00;
        } else if (minutesPayantes <= 150) {
            return 2.00 + 30.00; // Amende
        } else {
            return 2.00 + 30.00; // Amende + tarif max
        }
    }
    
    /**
     * Calcule le coût pour les zones sans tarif spécifique.
     * Utilise le tarif horaire standard.
     * 
     * @param dureeMinutes Durée en minutes
     * @return Coût en euros
     */
    private double calculerCoutNormal(int dureeMinutes) {
        double dureeHeures = dureeMinutes / 60.0;
        // Arrondi à l'heure supérieure pour la facturation
        dureeHeures = Math.ceil(dureeHeures);
        return dureeHeures * tarifParHeure;
    }

    /**
     * Retourne une description des tarifs pour l'affichage.
     * 
     * @return Description textuelle des tarifs
     */
    public String getAffichage() {
        switch (idZone) {
            case "ZONE_BLEUE":
                return "Zone Bleue - Gratuit 1h30, 2€ (2h), 32€ (>2h)";
            case "ZONE_VERTE":
                return "Zone Verte - 0.50€ (1h), 1€ (2h), 1.50€ (3h), 2€ (4h), 2.50€ (5h), 32.50€ (>5h)";
            case "ZONE_JAUNE":
                return "Zone Jaune - 1.50€ (1h), 3€ (2h), 33€ (>2h30)";
            case "ZONE_ORANGE":
                return "Zone Orange - 1€ (1h), 2€ (2h), 4€ (3h), 6€ (4h), 36€ (>5h)";
            case "ZONE_ROUGE":
                return "Zone Rouge - 30min gratuit, 1€ (1h), 2€ (2h), 32€ (>3h)";
            default:
                return libelleZone + " - " + String.format("%.2f", tarifParHeure) + "€/h (max " + 
                       getDureeMaxMinutes() + "min)";
        }
    }
    
    /**
     * Calcule le coût avec prise en compte d'un abonnement éventuel.
     * IMPORTANT : L'abonnement a déjà été payé séparément.
     * 
     * @param dureeMinutes Durée de stationnement en minutes
     * @param abonnement Abonnement de l'usager (peut être null)
     * @return Coût total en euros (0 si abonnement couvre le stationnement)
     */
    public double calculerCoutAvecAbonnement(int dureeMinutes, Abonnement abonnement) {
        // Vérification de base
        if (dureeMinutes <= 0) {
            return 0.0;
        }
        
        // Vérifier si l'usager a un abonnement actif
        if (abonnement != null && abonnement.estActif()) {
            // IMPORTANT : Le tarif de l'abonnement est le prix d'ACHAT de l'abonnement,
            // pas un tarif horaire. Le stationnement est GRATUIT si l'abonnement le couvre.
            
            // 1. Abonnements gratuits → stationnement gratuit
            if (abonnement.estGratuit()) {
                return 0.0;
            }
            
            // 2. Abonnement zone bleue en zone bleue → gratuit
            if (abonnement.estZoneBleue() && "ZONE_BLEUE".equals(idZone)) {
                return 0.0;
            }
            
            // 3. Types d'abonnements qui donnent accès gratuit au stationnement
            String idAbo = abonnement.getIdAbonnement();
            String typeAbo = abonnement.getTypeAbonnement();
            
            if (idAbo != null) {
                // Vérification par identifiant
                if (idAbo.contains("HEBDO") || idAbo.contains("ANNUEL") || 
                    idAbo.contains("RESIDENT") || idAbo.contains("MOTO") ||
                    idAbo.contains("PACK") || idAbo.contains("PREMIUM")) {
                    return 0.0; // Stationnement inclus dans l'abonnement
                }
                
                // Abonnement simple (paiement ponctuel) → calcul normal
                if (idAbo.contains("SIMPLE")) {
                    return calculerCout(dureeMinutes);
                }
            }
            
            // Vérification par type d'abonnement
            if (typeAbo != null) {
                switch (typeAbo.toUpperCase()) {
                    case "HEBDOMADAIRE":
                    case "MENSUEL":
                    case "ANNUEL":
                    case "RESIDENT":
                    case "MOTO":
                    case "PREMIUM":
                        return 0.0; // Stationnement inclus
                    case "SIMPLE":
                        return calculerCout(dureeMinutes); // Paiement à l'acte
                    default:
                        // Par défaut, si abonnement actif → stationnement gratuit
                        return 0.0;
                }
            }
            
            // Par défaut, si abonnement actif non reconnu → calcul normal
            return calculerCout(dureeMinutes);
        }
        
        // Pas d'abonnement valide : calcul normal
        return calculerCout(dureeMinutes);
    }
    
    /**
     * Vérifie si la durée demandée dépasse la durée maximale autorisée.
     * 
     * @param dureeMinutes Durée demandée en minutes
     * @return true si la durée est valide, false si elle dépasse la limite
     */
    public boolean dureeValide(int dureeMinutes) {
        return dureeMinutes <= getDureeMaxMinutes();
    }
    
    /**
     * Vérifie si un stationnement est gratuit pour cette zone.
     * Certaines zones ont des périodes de gratuité.
     * 
     * @param dureeMinutes Durée en minutes
     * @return true si le stationnement est gratuit, false sinon
     */
    public boolean estGratuit(int dureeMinutes) {
        switch (idZone) {
            case "ZONE_BLEUE":
                return dureeMinutes <= 90; // Gratuit jusqu'à 1h30
            case "ZONE_ROUGE":
                return dureeMinutes <= 30; // Gratuit 30 premières minutes
            default:
                return false;
        }
    }
    
    /**
     * Retourne le type de zone simplifié pour l'affichage.
     * 
     * @return Type de zone (ex: "Bleue", "Verte", etc.)
     */
    public String getTypeSimple() {
        if (idZone.startsWith("ZONE_")) {
            return idZone.substring(5); // Enlève "ZONE_"
        }
        return idZone;
    }
    
    /**
     * Calcule le coût estimé pour une durée donnée (sans amende).
     * 
     * @param dureeMinutes Durée en minutes
     * @return Coût estimé en euros (sans amende)
     */
    public double calculerCoutEstime(int dureeMinutes) {
        // Pour le calcul estimé, on ne prend pas en compte les amendes
        switch (idZone) {
            case "ZONE_BLEUE":
                return dureeMinutes <= 90 ? 0.0 : 2.0;
            case "ZONE_VERTE":
                if (dureeMinutes <= 60) return 0.5;
                if (dureeMinutes <= 120) return 1.0;
                if (dureeMinutes <= 180) return 1.5;
                if (dureeMinutes <= 240) return 2.0;
                return 2.5;
            case "ZONE_JAUNE":
                return dureeMinutes <= 60 ? 1.5 : 3.0;
            case "ZONE_ORANGE":
                if (dureeMinutes <= 60) return 1.0;
                if (dureeMinutes <= 120) return 2.0;
                if (dureeMinutes <= 180) return 4.0;
                return 6.0;
            case "ZONE_ROUGE":
                int minutesPayantes = Math.max(0, dureeMinutes - 30);
                if (minutesPayantes <= 60) return 1.0;
                return 2.0;
            default:
                double dureeHeures = dureeMinutes / 60.0;
                dureeHeures = Math.ceil(dureeHeures);
                return dureeHeures * tarifParHeure;
        }
    }
    
    /**
     * Représentation textuelle de la zone.
     * 
     * @return Description complète de la zone
     */
    @Override
    public String toString() {
        return libelleZone + " (" + getTypeSimple() + ") - " + getAffichage();
    }
}
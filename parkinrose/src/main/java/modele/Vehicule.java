package modele;

/**
 * Classe représentant un véhicule dans le système.
 * Gère les informations d'immatriculation et de type de véhicule.
 * Cette classe peut être associée à un usager spécifique.
 */
public class Vehicule {
    // ==================== ATTRIBUTS PRINCIPAUX ====================
    
    // Identifiant unique du véhicule
    private String idVehicule;
    // Plaque d'immatriculation du véhicule
    private String plaqueImmatriculation;
    // Type de véhicule (ex: "Voiture", "Moto", "Camion", "Utilitaire")
    private String typeVehicule;
    
    // ==================== CONSTRUCTEURS ====================

    /**
     * Constructeur par défaut.
     * Nécessaire pour les frameworks (JPA, Hibernate, etc.)
     */
    public Vehicule() {
        // Initialisation par défaut
    }
    
    /**
     * Constructeur avec détermination automatique du type de véhicule.
     * Le type est déterminé à partir de la plaque d'immatriculation.
     * 
     * @param idVehicule Identifiant unique du véhicule
     * @param plaqueImmatriculation Plaque d'immatriculation
     */
    public Vehicule(String idVehicule, String plaqueImmatriculation) {
        this.idVehicule = idVehicule;
        this.plaqueImmatriculation = plaqueImmatriculation;
        // Détermination automatique du type basée sur la plaque
        this.typeVehicule = determinerTypeVehicule(plaqueImmatriculation);
    }
    
    /**
     * Constructeur complet avec tous les attributs.
     * 
     * @param idVehicule Identifiant unique du véhicule
     * @param plaqueImmatriculation Plaque d'immatriculation
     * @param typeVehicule Type de véhicule (ex: "Voiture", "Moto")
     */
    public Vehicule(String idVehicule, String plaqueImmatriculation, String typeVehicule) {
        this.idVehicule = idVehicule;
        this.plaqueImmatriculation = plaqueImmatriculation;
        this.typeVehicule = typeVehicule;
    }
    
    // ==================== MÉTHODES DE DÉTERMINATION DU TYPE ====================

    /**
     * Détermine le type de véhicule à partir de la plaque d'immatriculation.
     * Utilise des règles simples basées sur les formats de plaques françaises.
     * 
     * @param plaque Plaque d'immatriculation à analyser
     * @return Le type de véhicule ("Moto" ou "Voiture" par défaut)
     */
    public String determinerTypeVehicule(String plaque) {
        if (plaque == null || plaque.trim().isEmpty()) {
            return "Voiture"; // Valeur par défaut
        }
        
        String plaqueNormalisee = plaque.toUpperCase().replaceAll("\\s+|-", "");
        
        // Logique de détermination basée sur les formats français
        
        // 1. Vérifier si c'est une moto (plaques spéciales)
        // Les motos peuvent avoir des formats spécifiques
        if (estPlaqueMoto(plaqueNormalisee)) {
            return "Moto";
        }
        
        // 2. Vérifier si c'est un camion ou utilitaire (poids lourd)
        if (estPlaquePoidsLourd(plaqueNormalisee)) {
            return "Camion";
        }
        
        // 3. Par défaut, considérer comme voiture
        return "Voiture";
    }
    
    /**
     * Vérifie si la plaque correspond à un format de moto.
     * 
     * @param plaque Plaque normalisée (sans espaces ni tirets)
     * @return true si la plaque semble être une plaque de moto, false sinon
     */
    private boolean estPlaqueMoto(String plaque) {
        // Formats de plaques de moto françaises
        // 1. Format ancien: 2 chiffres + 3 lettres (ex: 12ABC34)
        // 2. Format nouveau: 2 lettres + 3 chiffres + 2 lettres (ex: AB-123-CD)
        // Les motos ont souvent des plaques plus courtes
        
        if (plaque.length() < 7 || plaque.length() > 8) {
            return false; // Les plaques de moto françaises font généralement 7 ou 8 caractères
        }
        
        // Vérifier le format général
        return plaque.matches("^[A-Z]{2}[0-9]{3}[A-Z]{2}$") || // Format nouveau
               plaque.matches("^[0-9]{2}[A-Z]{3}[0-9]{2}$");   // Format ancien
    }
    
    /**
     * Vérifie si la plaque correspond à un véhicule poids lourd.
     * 
     * @param plaque Plaque normalisée (sans espaces ni tirets)
     * @return true si la plaque semble être un poids lourd, false sinon
     */
    private boolean estPlaquePoidsLourd(String plaque) {
        // Les poids lourds ont souvent des indications spécifiques
        // Cette méthode est simplifiée et peut être améliorée
        
        if (plaque.length() != 7) {
            return false;
        }
        
        // Vérifier certains motifs qui pourraient indiquer un poids lourd
        // Note: Cette logique est très simplifiée
        return plaque.matches(".*[WXYZ].*"); // Certaines lettres indiquent des véhicules spéciaux
    }
    
    // ==================== GETTERS & SETTERS ====================

    public String getIdVehicule() {
        return idVehicule;
    }
    
    public void setIdVehicule(String idVehicule) {
        this.idVehicule = idVehicule;
    }
    
    public String getPlaqueImmatriculation() {
        return plaqueImmatriculation;
    }
    
    /**
     * Modifie la plaque d'immatriculation et met à jour automatiquement le type.
     * 
     * @param plaqueImmatriculation Nouvelle plaque d'immatriculation
     */
    public void setPlaqueImmatriculation(String plaqueImmatriculation) {
        this.plaqueImmatriculation = plaqueImmatriculation;
        // Mettre à jour le type automatiquement si la plaque change
        if (plaqueImmatriculation != null) {
            this.typeVehicule = determinerTypeVehicule(plaqueImmatriculation);
        }
    }
    
    public String getTypeVehicule() {
        return typeVehicule;
    }
    
    public void setTypeVehicule(String typeVehicule) {
        this.typeVehicule = typeVehicule;
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Vérifie si le véhicule est une moto.
     * 
     * @return true si le type est "Moto" (insensible à la casse), false sinon
     */
    public boolean estMoto() {
        return "Moto".equalsIgnoreCase(typeVehicule);
    }
    
    /**
     * Vérifie si le véhicule est une voiture.
     * 
     * @return true si le type est "Voiture" (insensible à la casse), false sinon
     */
    public boolean estVoiture() {
        return "Voiture".equalsIgnoreCase(typeVehicule);
    }
    
    /**
     * Vérifie si le véhicule est un camion ou utilitaire.
     * 
     * @return true si le type est "Camion" (insensible à la casse), false sinon
     */
    public boolean estCamion() {
        return "Camion".equalsIgnoreCase(typeVehicule);
    }
    
    /**
     * Retourne la plaque formatée pour l'affichage.
     * Format standard français: AA-123-BB
     * 
     * @return La plaque formatée, ou chaîne vide si la plaque est null
     */
    public String getPlaqueFormatee() {
        if (plaqueImmatriculation == null) {
            return "";
        }
        
        String plaque = plaqueImmatriculation.toUpperCase()
            .replaceAll("[^A-Z0-9]", "") // Garder seulement lettres et chiffres
            .replaceAll("\\s+", "");     // Supprimer les espaces
        
        if (plaque.length() == 7) {
            // Format: AB123CD -> AB-123-CD
            return plaque.substring(0, 2) + "-" + 
                   plaque.substring(2, 5) + "-" + 
                   plaque.substring(5);
        } else if (plaque.length() == 8) {
            // Format: 1234AB56 -> 1234-AB-56
            return plaque.substring(0, 4) + "-" + 
                   plaque.substring(4, 6) + "-" + 
                   plaque.substring(6);
        } else {
            // Retourner la plaque telle quelle si format non reconnu
            return plaqueImmatriculation.toUpperCase();
        }
    }
    
    /**
     * Vérifie si la plaque d'immatriculation est valide.
     * Vérifie le format selon les standards français.
     * 
     * @return true si la plaque semble valide, false sinon
     */
    public boolean plaqueValide() {
        if (plaqueImmatriculation == null || plaqueImmatriculation.trim().isEmpty()) {
            return false;
        }
        
        String plaque = plaqueImmatriculation.toUpperCase()
            .replaceAll("[^A-Z0-9]", "") // Nettoyer la plaque
            .replaceAll("\\s+", "");
        
        // Vérifier la longueur
        if (plaque.length() < 7 || plaque.length() > 9) {
            return false;
        }
        
        // Vérifier les formats français courants
        // Format SIV (depuis 2009): AA-123-BB (7 caractères)
        // Format ancien: 123-AB-45 (7-8 caractères)
        
        if (plaque.length() == 7) {
            // Format SIV: 2 lettres + 3 chiffres + 2 lettres
            return plaque.matches("^[A-Z]{2}[0-9]{3}[A-Z]{2}$");
        } else if (plaque.length() == 8) {
            // Format ancien: 4 chiffres + 2 lettres + 2 chiffres
            return plaque.matches("^[0-9]{4}[A-Z]{2}[0-9]{2}$");
        } else if (plaque.length() == 9) {
            // Autres formats possibles (ex: véhicules spéciaux)
            return plaque.matches("^[A-Z0-9]{9}$");
        }
        
        return false;
    }
    
    /**
     * Vérifie si le véhicule peut stationner dans un parking avec une hauteur donnée.
     * 
     * @param hauteurMaximale Hauteur maximale autorisée en mètres
     * @return true si le véhicule peut entrer (basé sur son type), false sinon
     */
    public boolean peutStationnerDansParking(double hauteurMaximale) {
        // Logique simplifiée : les motos passent partout
        if (estMoto()) {
            return true;
        }
        
        // Pour les autres véhicules, vérifier la hauteur
        // Note: Cette méthode pourrait être améliorée avec des hauteurs spécifiques par type
        double hauteurVehicule = getHauteurEstimee();
        return hauteurVehicule <= hauteurMaximale;
    }
    
    /**
     * Retourne une hauteur estimée du véhicule selon son type.
     * 
     * @return Hauteur estimée en mètres
     */
    private double getHauteurEstimee() {
        switch (typeVehicule.toLowerCase()) {
            case "voiture":
                return 1.6; // Hauteur moyenne d'une voiture
            case "camion":
                return 3.5; // Hauteur moyenne d'un camion
            case "utilitaire":
                return 2.5; // Hauteur moyenne d'un utilitaire
            case "moto":
                return 1.2; // Hauteur moyenne d'une moto
            default:
                return 2.0; // Hauteur par défaut
        }
    }
    
    // ==================== MÉTHODES STANDARD ====================

    /**
     * Représentation textuelle détaillée pour le débogage.
     * 
     * @return Une chaîne formatée contenant toutes les informations
     */
    @Override
    public String toString() {
        return "Vehicule [idVehicule=" + idVehicule + 
               ", plaqueImmatriculation=" + getPlaqueFormatee() + 
               ", typeVehicule=" + typeVehicule + "]";
    }
    
    /**
     * Représentation simplifiée pour l'affichage dans les listes.
     * 
     * @return Une chaîne formatée simplifiée
     */
    public String getAffichage() {
        return getPlaqueFormatee() + " (" + typeVehicule + ")";
    }
    
    /**
     * Comparaison d'égalité basée sur l'ID et la plaque.
     * 
     * @param obj L'objet à comparer
     * @return true si les véhicules sont identiques, false sinon
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Vehicule vehicule = (Vehicule) obj;
        
        // Comparaison par ID si disponible
        if (idVehicule != null && vehicule.idVehicule != null) {
            return idVehicule.equals(vehicule.idVehicule);
        }
        
        // Sinon comparaison par plaque
        return plaqueImmatriculation != null && 
               plaqueImmatriculation.equalsIgnoreCase(vehicule.plaqueImmatriculation);
    }
    
    /**
     * Code de hachage basé sur l'ID et la plaque.
     * 
     * @return Code de hachage
     */
    @Override
    public int hashCode() {
        int result = idVehicule != null ? idVehicule.hashCode() : 0;
        result = 31 * result + (plaqueImmatriculation != null ? plaqueImmatriculation.hashCode() : 0);
        return result;
    }
}
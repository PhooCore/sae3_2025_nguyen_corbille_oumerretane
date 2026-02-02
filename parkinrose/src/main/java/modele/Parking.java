package modele;

/**
 * Classe représentant un parking dans le système.
 * Contient toutes les informations nécessaires pour décrire un parking,
 * y compris sa localisation géographique, ses caractéristiques et sa disponibilité.
 */
public class Parking {
    // ==================== ATTRIBUTS PRINCIPAUX ====================
    
    // Identifiant unique du parking
    private String idParking;
    // Nom/description du parking (ex: "Parking Centre-Ville")
    private String libelleParking;
    // Adresse postale complète du parking
    private String adresseParking;
    // Nombre total de places de stationnement (voitures)
    private int nombrePlaces;
    // Nombre de places disponibles actuellement (voitures)
    private int placesDisponibles;
    // Hauteur maximale autorisée en mètres
    private double hauteurParking;
    // Indique si un tarif spécial soirée/nuit est applicable
    private boolean tarifSoiree;
    // Indique si le parking dispose de places réservées aux motos
    private boolean hasMoto;
    // Nombre total de places réservées aux motos
    private int placesMoto;
    // Nombre de places motos disponibles actuellement
    private int placesMotoDisponibles;
    // Indique si c'est un parking relais (P+R)
    private boolean estRelais;
    // Coordonnée X (longitude ou latitude) pour la localisation géographique
    private Float positionX; 
    // Coordonnée Y (longitude ou latitude) pour la localisation géographique
    private Float positionY; 
    // Tarif horaire standard en euros
    private double tarifHoraire;
    
    // ==================== CONSTRUCTEURS ====================
    // Plusieurs constructeurs pour différentes situations d'initialisation
    
    /**
     * Constructeur minimal (hérité).
     * Crée un parking sans places motos ni coordonnées GPS.
     * 
     * @param idParking Identifiant unique du parking
     * @param libelleParking Nom/description du parking
     * @param adresseParking Adresse postale
     * @param nombrePlaces Nombre total de places voitures
     * @param placesDisponibles Places voitures disponibles
     * @param hauteurParking Hauteur maximale autorisée (m)
     * @param tarifSoiree Indique si tarif soirée applicable
     */
    public Parking(String idParking, String libelleParking, String adresseParking, 
                   int nombrePlaces, int placesDisponibles, double hauteurParking, 
                   boolean tarifSoiree) {
        this(idParking, libelleParking, adresseParking, nombrePlaces, 
             placesDisponibles, hauteurParking, tarifSoiree, false, 0, 0);
    }
    
    /**
     * Constructeur standard (hérité).
     * Ajoute la gestion des places motos.
     * 
     * @param idParking Identifiant unique du parking
     * @param libelleParking Nom/description du parking
     * @param adresseParking Adresse postale
     * @param nombrePlaces Nombre total de places voitures
     * @param placesDisponibles Places voitures disponibles
     * @param hauteurParking Hauteur maximale autorisée (m)
     * @param tarifSoiree Indique si tarif soirée applicable
     * @param hasMoto Indique si places motos disponibles
     * @param placesMoto Nombre total places motos
     * @param placesMotoDisponibles Places motos disponibles
     */
    public Parking(String idParking, String libelleParking, String adresseParking, 
                   int nombrePlaces, int placesDisponibles, double hauteurParking, 
                   boolean tarifSoiree, boolean hasMoto, int placesMoto, int placesMotoDisponibles) {
        this(idParking, libelleParking, adresseParking, nombrePlaces, placesDisponibles, 
             hauteurParking, tarifSoiree, hasMoto, placesMoto, placesMotoDisponibles, false, null, null);
    }
    
    /**
     * Constructeur avec indication de parking relais.
     * 
     * @param idParking Identifiant unique du parking
     * @param libelleParking Nom/description du parking
     * @param adresseParking Adresse postale
     * @param nombrePlaces Nombre total de places voitures
     * @param placesDisponibles Places voitures disponibles
     * @param hauteurParking Hauteur maximale autorisée (m)
     * @param tarifSoiree Indique si tarif soirée applicable
     * @param hasMoto Indique si places motos disponibles
     * @param placesMoto Nombre total places motos
     * @param placesMotoDisponibles Places motos disponibles
     * @param estRelais Indique si c'est un parking relais (P+R)
     */
    public Parking(String idParking, String libelleParking, String adresseParking, 
                   int nombrePlaces, int placesDisponibles, double hauteurParking, 
                   boolean tarifSoiree, boolean hasMoto, int placesMoto, int placesMotoDisponibles,
                   boolean estRelais) {
        this(idParking, libelleParking, adresseParking, nombrePlaces, placesDisponibles, 
             hauteurParking, tarifSoiree, hasMoto, placesMoto, placesMotoDisponibles, estRelais, null, null);
    }
    
    /**
     * Constructeur complet avec coordonnées géographiques.
     * 
     * @param idParking Identifiant unique du parking
     * @param libelleParking Nom/description du parking
     * @param adresseParking Adresse postale
     * @param nombrePlaces Nombre total de places voitures
     * @param placesDisponibles Places voitures disponibles
     * @param hauteurParking Hauteur maximale autorisée (m)
     * @param tarifSoiree Indique si tarif soirée applicable
     * @param hasMoto Indique si places motos disponibles
     * @param placesMoto Nombre total places motos
     * @param placesMotoDisponibles Places motos disponibles
     * @param estRelais Indique si c'est un parking relais (P+R)
     * @param positionX Coordonnée X (longitude ou latitude)
     * @param positionY Coordonnée Y (longitude ou latitude)
     */
    public Parking(String idParking, String libelleParking, String adresseParking, 
                   int nombrePlaces, int placesDisponibles, double hauteurParking, 
                   boolean tarifSoiree, boolean hasMoto, int placesMoto, int placesMotoDisponibles,
                   boolean estRelais, Float positionX, Float positionY) {
        this.idParking = idParking;
        this.libelleParking = libelleParking;
        this.adresseParking = adresseParking;
        this.nombrePlaces = nombrePlaces;
        this.placesDisponibles = placesDisponibles;
        this.hauteurParking = hauteurParking;
        this.tarifSoiree = tarifSoiree;
        this.hasMoto = hasMoto;
        this.placesMoto = placesMoto;
        this.placesMotoDisponibles = placesMotoDisponibles;
        this.estRelais = estRelais;
        this.positionX = positionX;
        this.positionY = positionY;
        this.tarifHoraire = 0.0;  // Valeur par défaut
    }
    
    // ==================== GETTERS & SETTERS ====================
    
    public String getIdParking() {
        return idParking;
    }
    
    public void setIdParking(String idParking) {
        this.idParking = idParking;
    }
    
    public String getLibelleParking() {
        return libelleParking;
    }
    
    public void setLibelleParking(String libelleParking) {
        this.libelleParking = libelleParking;
    }
    
    public String getAdresseParking() {
        return adresseParking;
    }
    
    public void setAdresseParking(String adresseParking) {
        this.adresseParking = adresseParking;
    }
    
    public int getNombrePlaces() {
        return nombrePlaces;
    }
    
    public void setNombrePlaces(int nombrePlaces) {
        this.nombrePlaces = nombrePlaces;
    }
    
    public int getPlacesDisponibles() {
        return placesDisponibles;
    }
    
    public void setPlacesDisponibles(int placesDisponibles) {
        this.placesDisponibles = placesDisponibles;
    }
    
    public double getHauteurParking() {
        return hauteurParking;
    }
    
    public void setHauteurParking(double hauteurParking) {
        this.hauteurParking = hauteurParking;
    }
    
    public boolean hasTarifSoiree() {
        return tarifSoiree;
    }
    
    public void setTarifSoiree(boolean tarifSoiree) {
        this.tarifSoiree = tarifSoiree;
    }
    
    public boolean hasMoto() {
        return hasMoto;
    }
    
    public void setHasMoto(boolean hasMoto) {
        this.hasMoto = hasMoto;
    }
    
    public int getPlacesMoto() {
        return placesMoto;
    }
    
    public void setPlacesMoto(int placesMoto) {
        this.placesMoto = placesMoto;
    }
    
    public int getPlacesMotoDisponibles() {
        return placesMotoDisponibles;
    }
    
    public void setPlacesMotoDisponibles(int placesMotoDisponibles) {
        this.placesMotoDisponibles = placesMotoDisponibles;
    }
    
    public boolean isEstRelais() {
        return estRelais;
    }
    
    public void setEstRelais(boolean estRelais) {
        this.estRelais = estRelais;
    }
    
    public Float getPositionX() {
        return positionX;
    }
    
    public void setPositionX(Float positionX) {
        this.positionX = positionX;
    }
    
    public Float getPositionY() {
        return positionY;
    }
    
    public void setPositionY(Float positionY) {
        this.positionY = positionY;
    }
    
    public double getTarifHoraire() {
        return tarifHoraire;
    }
    
    public void setTarifHoraire(double tarifHoraire) {
        this.tarifHoraire = tarifHoraire;
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Vérifie si le parking a des places disponibles pour les voitures.
     * 
     * @return true si au moins une place voiture est disponible, false sinon
     */
    public boolean hasPlacesDisponibles() {
        return placesDisponibles > 0;
    }
    
    /**
     * Vérifie si le parking a des places disponibles pour les motos.
     * 
     * @return true si le parking accepte les motos ET a au moins une place moto disponible, false sinon
     */
    public boolean hasPlacesMotoDisponibles() {
        return hasMoto && placesMotoDisponibles > 0;
    }
    
    /**
     * Calcule le taux d'occupation du parking (voitures).
     * 
     * @return Pourcentage d'occupation (0-100)
     */
    public double getTauxOccupation() {
        if (nombrePlaces == 0) {
            return 0.0;
        }
        int placesOccupees = nombrePlaces - placesDisponibles;
        return (placesOccupees * 100.0) / nombrePlaces;
    }
    
    /**
     * Calcule le taux d'occupation des places motos.
     * 
     * @return Pourcentage d'occupation (0-100), ou 0 si pas de places motos
     */
    public double getTauxOccupationMoto() {
        if (!hasMoto || placesMoto == 0) {
            return 0.0;
        }
        int placesMotoOccupees = placesMoto - placesMotoDisponibles;
        return (placesMotoOccupees * 100.0) / placesMoto;
    }
    
    /**
     * Vérifie si le parking a des coordonnées géographiques définies.
     * 
     * @return true si les deux coordonnées (X et Y) sont non null, false sinon
     */
    public boolean hasCoordonnees() {
        return positionX != null && positionY != null;
    }
    
    /**
     * Vérifie si le parking peut accueillir un véhicule selon sa hauteur.
     * 
     * @param hauteurVehicule Hauteur du véhicule en mètres
     * @return true si le véhicule peut entrer (hauteur suffisante), false sinon
     */
    public boolean peutAccueillirHauteur(double hauteurVehicule) {
        return hauteurParking >= hauteurVehicule;
    }
    
    /**
     * Retourne une description de la disponibilité.
     * 
     * @return "Complet" si pas de places, sinon "X places disponibles"
     */
    public String getDisponibiliteTexte() {
        if (placesDisponibles <= 0) {
            return "Complet";
        } else if (placesDisponibles <= 3) {
            return "Presque complet (" + placesDisponibles + " places)";
        } else {
            return placesDisponibles + " places disponibles";
        }
    }
    
    /**
     * Retourne une description de la disponibilité moto.
     * 
     * @return "Aucune place moto" si pas de places motos, sinon "X places motos disponibles"
     */
    public String getDisponibiliteMotoTexte() {
        if (!hasMoto) {
            return "Pas de places motos";
        } else if (placesMotoDisponibles <= 0) {
            return "Complet (motos)";
        } else {
            return placesMotoDisponibles + " places motos disponibles";
        }
    }
    
    /**
     * Vérifie si le parking est ouvert (basé sur la disponibilité).
     * Note: Cette méthode pourrait être étendue avec des horaires d'ouverture.
     * 
     * @return true si le parking a au moins une place disponible, false sinon
     */
    public boolean estOuvert() {
        return placesDisponibles > 0;
    }
    
    // ==================== MÉTHODES STANDARD ====================
    
    /**
     * Représentation textuelle de l'objet pour l'affichage.
     * Format convivial pour l'utilisateur.
     * 
     * @return Une chaîne formatée contenant les informations principales
     */
    @Override
    public String toString() {
        return libelleParking + " (" + adresseParking + ") - " + 
               placesDisponibles + "/" + nombrePlaces + " places" +
               (hasMoto ? " - " + placesMotoDisponibles + "/" + placesMoto + " places moto" : "") +
               (estRelais ? " [Relais]" : "") +
               (tarifSoiree ? " [Tarif soirée]" : "");
    }
}
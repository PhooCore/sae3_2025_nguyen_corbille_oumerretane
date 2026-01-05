package modele;

public class Parking {
    private String idParking;
    private String libelleParking;
    private String adresseParking;
    private int nombrePlaces;
    private int placesDisponibles;
    private double hauteurParking;
    private boolean tarifSoiree;
    private boolean hasMoto;
    private int placesMoto;
    private int placesMotoDisponibles;
    private boolean estRelais;
    private Float positionX; 
    private Float positionY; 
    private double tarifHoraire;
    // Constructeur existant (sans positions)
    public Parking(String idParking, String libelleParking, String adresseParking, 
                   int nombrePlaces, int placesDisponibles, double hauteurParking, 
                   boolean tarifSoiree, boolean hasMoto, int placesMoto, int placesMotoDisponibles) {
        this(idParking, libelleParking, adresseParking, nombrePlaces, placesDisponibles, 
             hauteurParking, tarifSoiree, hasMoto, placesMoto, placesMotoDisponibles, false, null, null);
    }
    
    // Constructeur avec estRelais
    public Parking(String idParking, String libelleParking, String adresseParking, 
                   int nombrePlaces, int placesDisponibles, double hauteurParking, 
                   boolean tarifSoiree, boolean hasMoto, int placesMoto, int placesMotoDisponibles,
                   boolean estRelais) {
        this(idParking, libelleParking, adresseParking, nombrePlaces, placesDisponibles, 
             hauteurParking, tarifSoiree, hasMoto, placesMoto, placesMotoDisponibles, estRelais, null, null);
    }
    
    // Nouveau constructeur avec positions
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
    }
    
    // Getters et setters pour les nouvelles propriétés
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
    
    public boolean isEstRelais() {
        return estRelais;
    }
    
    public void setEstRelais(boolean estRelais) {
        this.estRelais = estRelais;
    }
    public Parking(String idParking, String libelleParking, String adresseParking, 
                   int nombrePlaces, int placesDisponibles, double hauteurParking, 
                   boolean tarifSoiree) {
        this(idParking, libelleParking, adresseParking, nombrePlaces, 
             placesDisponibles, hauteurParking, tarifSoiree, false, 0, 0);
    }
    
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
    
    public boolean hasPlacesMotoDisponibles() {
        return hasMoto && placesMotoDisponibles > 0;
    }
    
    public boolean hasPlacesDisponibles() {
        return placesDisponibles > 0;
    }
    public double getTarifHoraire() {
        return tarifHoraire;
    }
    
    public void setTarifHoraire(double tarifHoraire) {
        this.tarifHoraire = tarifHoraire;
    }
    
    @Override
    public String toString() {
        return libelleParking + " (" + adresseParking + ") - " + 
               placesDisponibles + "/" + nombrePlaces + " places" +
               (hasMoto ? " - " + placesMotoDisponibles + "/" + placesMoto + " places moto" : "");
    }
}
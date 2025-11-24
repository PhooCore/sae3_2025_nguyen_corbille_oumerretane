package modele;

public class Parking {
    private String idParking;
    private String libelleParking;
    private String adresseParking;
    private int nombrePlaces;
    private int placesDisponibles;
    private double hauteurParking;
    private boolean tarifSoiree;
    
    public Parking(String idParking, String libelleParking, String adresseParking, 
                  int nombrePlaces, int placesDisponibles, double hauteurParking, boolean tarifSoiree) {
        this.idParking = idParking;
        this.libelleParking = libelleParking;
        this.adresseParking = adresseParking;
        this.nombrePlaces = nombrePlaces;
        this.placesDisponibles = placesDisponibles;
        this.hauteurParking = hauteurParking;
        this.tarifSoiree = tarifSoiree;
    }
    
    // Getters et Setters
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
    
    @Override
    public String toString() {
        return libelleParking + " - " + adresseParking + " (" + placesDisponibles + "/" + nombrePlaces + " places)";
    }
}
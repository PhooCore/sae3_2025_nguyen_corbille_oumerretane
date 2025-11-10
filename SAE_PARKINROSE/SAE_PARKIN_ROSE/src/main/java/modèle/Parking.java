package modèle;

public class Parking {
    private String idParking;
    private String libelleParking;
    private String adresseParking;
    private int nombrePlaces;
    private double hauteurParking;
    private int placesDisponibles;

    public Parking(String idParking, String libelleParking, String adresseParking, 
                   int nombrePlaces, double hauteurParking) {
        this.idParking = idParking;
        this.libelleParking = libelleParking;
        this.adresseParking = adresseParking;
        this.nombrePlaces = nombrePlaces;
        this.hauteurParking = hauteurParking;
        // Estimation des places disponibles (80% de remplissage)
        this.placesDisponibles = (int)(nombrePlaces * 0.2);
    }

    // Getters
    public String getIdParking() { return idParking; }
    public String getLibelleParking() { return libelleParking; }
    public String getAdresseParking() { return adresseParking; }
    public int getNombrePlaces() { return nombrePlaces; }
    public double getHauteurParking() { return hauteurParking; }
    public int getPlacesDisponibles() { return placesDisponibles; }
    
    @Override
    public String toString() {
        return libelleParking + " - " + adresseParking;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Parking parking = (Parking) obj;
        return idParking != null && idParking.equals(parking.idParking);
    }
    
    @Override
    public int hashCode() {
        return idParking != null ? idParking.hashCode() : 0;
    }
}
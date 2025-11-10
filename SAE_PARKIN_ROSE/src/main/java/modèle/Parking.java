package modèle;

public class Parking {
    private String idParking;
    private String libelleParking;
    private String adresseParking;
    private int nombrePlaces;
    private double hauteurParking;
    private int placesDisponibles;
    private boolean tarifSoiree;  // NOUVEAU

    public Parking(String idParking, String libelleParking, String adresseParking, 
                   int nombrePlaces, double hauteurParking, boolean tarifSoiree) {
        this.idParking = idParking;
        this.libelleParking = libelleParking;
        this.adresseParking = adresseParking;
        this.nombrePlaces = nombrePlaces;
        this.hauteurParking = hauteurParking;
        this.tarifSoiree = tarifSoiree;
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
    public boolean hasTarifSoiree() { return tarifSoiree; }  // NOUVEAU
    
    @Override
    public String toString() {
        return libelleParking + " - " + adresseParking;
    }
}
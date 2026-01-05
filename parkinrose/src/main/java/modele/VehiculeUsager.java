package modele;

import java.time.LocalDate;

public class VehiculeUsager {
    private int idVehiculeUsager;
    private int idUsager;
    private String plaqueImmatriculation;
    private String typeVehicule;
    private String marque;
    private String modele;
    private LocalDate dateAjout;
    private boolean estPrincipal;

    // Constructeurs
    public VehiculeUsager() {}

    public VehiculeUsager(int idUsager, String plaqueImmatriculation, String typeVehicule) {
        this.idUsager = idUsager;
        this.plaqueImmatriculation = plaqueImmatriculation.toUpperCase();
        this.typeVehicule = typeVehicule;
        this.dateAjout = LocalDate.now();
        this.estPrincipal = false;
    }

    // Getters et Setters
    public int getIdVehiculeUsager() { return idVehiculeUsager; }
    public void setIdVehiculeUsager(int idVehiculeUsager) { this.idVehiculeUsager = idVehiculeUsager; }

    public int getIdUsager() { return idUsager; }
    public void setIdUsager(int idUsager) { this.idUsager = idUsager; }

    public String getPlaqueImmatriculation() { return plaqueImmatriculation; }
    public void setPlaqueImmatriculation(String plaqueImmatriculation) { 
        this.plaqueImmatriculation = plaqueImmatriculation.toUpperCase(); 
    }

    public String getTypeVehicule() { return typeVehicule; }
    public void setTypeVehicule(String typeVehicule) { this.typeVehicule = typeVehicule; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }

    public LocalDate getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDate dateAjout) { this.dateAjout = dateAjout; }

    public boolean isEstPrincipal() { return estPrincipal; }
    public void setEstPrincipal(boolean estPrincipal) { this.estPrincipal = estPrincipal; }

    @Override
    public String toString() {
        return plaqueImmatriculation + " - " + typeVehicule + 
               (marque != null ? " " + marque : "") + 
               (modele != null ? " " + modele : "") + 
               (estPrincipal ? " (Principal)" : "");
    }
}
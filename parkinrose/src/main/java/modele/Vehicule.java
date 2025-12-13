package modele;

public class Vehicule {
    private int idVehiculeUsager;
    private String plaqueImmatriculation;
    private String alias;
    private boolean estPrincipal;
    
    // Constructeur par défaut
    public Vehicule() {}
    
    // Constructeur avec paramètres
    public Vehicule(int idVehiculeUsager, String plaqueImmatriculation, String alias, boolean estPrincipal) {
        this.idVehiculeUsager = idVehiculeUsager;
        this.plaqueImmatriculation = plaqueImmatriculation;
        this.alias = alias;
        this.estPrincipal = estPrincipal;
    }
    
    // Getters et Setters
    public int getIdVehiculeUsager() { return idVehiculeUsager; }
    public void setIdVehiculeUsager(int idVehiculeUsager) { this.idVehiculeUsager = idVehiculeUsager; }
    
    public String getPlaqueImmatriculation() { return plaqueImmatriculation; }
    public void setPlaqueImmatriculation(String plaqueImmatriculation) { 
        this.plaqueImmatriculation = plaqueImmatriculation; 
    }
    
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    
    public boolean isEstPrincipal() { return estPrincipal; }
    public void setEstPrincipal(boolean estPrincipal) { this.estPrincipal = estPrincipal; }
    
    @Override
    public String toString() {
        if (alias != null && !alias.trim().isEmpty()) {
            return alias + " (" + plaqueImmatriculation + ")";
        }
        return plaqueImmatriculation;
    }
}
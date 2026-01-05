package modele;

public class Vehicule {
    private String idVehicule;
    private String plaqueImmatriculation;
    private String typeVehicule;
    
    public Vehicule() {
        // Constructeur par défaut
    }
    
    public Vehicule(String idVehicule, String plaqueImmatriculation) {
        this.idVehicule = idVehicule;
        this.plaqueImmatriculation = plaqueImmatriculation;
        // Déterminer le type basé sur la plaque ou autre logique
        this.typeVehicule = determinerTypeVehicule(plaqueImmatriculation);
    }
    
    public Vehicule(String idVehicule, String plaqueImmatriculation, String typeVehicule) {
        this.idVehicule = idVehicule;
        this.plaqueImmatriculation = plaqueImmatriculation;
        this.typeVehicule = typeVehicule;
    }
    
    public String determinerTypeVehicule(String plaque) {
        // Logique simplifiée pour déterminer le type de véhicule
        // Vous pouvez adapter cette logique selon vos besoins
        if (plaque != null && plaque.length() >= 2) {
            // Exemple: si la plaque commence par MC ou MT, c'est une moto
            String debut = plaque.substring(0, 2).toUpperCase();
            if (debut.equals("MC") || debut.equals("MT") || debut.equals("MX")) {
                return "Moto";
            }
        }
        return "Voiture"; // Par défaut
    }
    
    // Getters et setters
    public String getIdVehicule() {
        return idVehicule;
    }
    
    public void setIdVehicule(String idVehicule) {
        this.idVehicule = idVehicule;
    }
    
    public String getPlaqueImmatriculation() {
        return plaqueImmatriculation;
    }
    
    public void setPlaqueImmatriculation(String plaqueImmatriculation) {
        this.plaqueImmatriculation = plaqueImmatriculation;
        // Mettre à jour le type si la plaque change
        this.typeVehicule = determinerTypeVehicule(plaqueImmatriculation);
    }
    
    public String getTypeVehicule() {
        return typeVehicule;
    }
    
    public void setTypeVehicule(String typeVehicule) {
        this.typeVehicule = typeVehicule;
    }
    
    // Méthodes utilitaires
    public boolean estMoto() {
        return "Moto".equalsIgnoreCase(typeVehicule);
    }
    
    public boolean estVoiture() {
        return "Voiture".equalsIgnoreCase(typeVehicule);
    }
    
    public String getPlaqueFormatee() {
        if (plaqueImmatriculation == null) return "";
        // Formater la plaque pour l'affichage
        return plaqueImmatriculation.toUpperCase().replaceAll("\\s+", " ");
    }
    
    @Override
    public String toString() {
        return "Vehicule [idVehicule=" + idVehicule + ", plaqueImmatriculation=" + plaqueImmatriculation 
                + ", typeVehicule=" + typeVehicule + "]";
    }
    
    // Pour l'affichage dans les listes
    public String getAffichage() {
        return plaqueImmatriculation + " (" + typeVehicule + ")";
    }
    
    // Vérification de la validité de la plaque
    public boolean plaqueValide() {
        if (plaqueImmatriculation == null || plaqueImmatriculation.trim().isEmpty()) {
            return false;
        }
        
        // Format français simplifié : AA-123-BB ou 1234-AB-56
        String plaque = plaqueImmatriculation.toUpperCase().replaceAll("[^A-Z0-9]", "");
        
        if (plaque.length() < 7 || plaque.length() > 9) {
            return false;
        }
        
        // Vérification basique
        return plaque.matches("[A-Z0-9]+");
    }
}
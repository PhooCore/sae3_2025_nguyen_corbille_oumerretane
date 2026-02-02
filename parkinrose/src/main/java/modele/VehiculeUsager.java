package modele;

import java.time.LocalDate;

/**
 * Classe représentant l'association entre un usager et ses véhicules.
 * Cette classe sert de table d'association dans la base de données
 * pour gérer les véhicules multiples par usager.
 */
public class VehiculeUsager {
    // ==================== ATTRIBUTS PRINCIPAUX ====================
    
    // Identifiant unique de l'association en base de données
    private int idVehiculeUsager;
    // Identifiant de l'usager propriétaire
    private int idUsager;
    // Plaque d'immatriculation du véhicule (format normalisé en majuscules)
    private String plaqueImmatriculation;
    // Type de véhicule (ex: "Voiture", "Moto", "Camion")
    private String typeVehicule;
    // Marque du véhicule (optionnel)
    private String marque;
    // Modèle du véhicule (optionnel)
    private String modele;
    // Date d'ajout du véhicule dans le système
    private LocalDate dateAjout;
    // Indique si c'est le véhicule principal de l'usager
    private boolean estPrincipal;

    // ==================== CONSTRUCTEURS ====================

    /**
     * Constructeur par défaut.
     * Nécessaire pour les frameworks (JPA, Hibernate, etc.)
     */
    public VehiculeUsager() {
        // Initialisation par défaut
        this.dateAjout = LocalDate.now(); // Date courante par défaut
        this.estPrincipal = false;        // Non principal par défaut
    }

    /**
     * Constructeur minimal pour créer une nouvelle association véhicule-usager.
     * Utilise la date courante et marque le véhicule comme non principal.
     * 
     * @param idUsager Identifiant de l'usager propriétaire
     * @param plaqueImmatriculation Plaque d'immatriculation (convertie en majuscules)
     * @param typeVehicule Type de véhicule (ex: "Voiture", "Moto")
     */
    public VehiculeUsager(int idUsager, String plaqueImmatriculation, String typeVehicule) {
        this.idUsager = idUsager;
        this.plaqueImmatriculation = plaqueImmatriculation.toUpperCase(); // Normalisation
        this.typeVehicule = typeVehicule;
        this.dateAjout = LocalDate.now(); // Date courante
        this.estPrincipal = false;        // Non principal par défaut
    }

    // ==================== GETTERS & SETTERS ====================

    public int getIdVehiculeUsager() { 
        return idVehiculeUsager; 
    }
    
    public void setIdVehiculeUsager(int idVehiculeUsager) { 
        this.idVehiculeUsager = idVehiculeUsager; 
    }

    public int getIdUsager() { 
        return idUsager; 
    }
    
    public void setIdUsager(int idUsager) { 
        this.idUsager = idUsager; 
    }

    public String getPlaqueImmatriculation() { 
        return plaqueImmatriculation; 
    }
    
    /**
     * Modifie la plaque d'immatriculation et la convertit automatiquement en majuscules.
     * 
     * @param plaqueImmatriculation Nouvelle plaque d'immatriculation
     */
    public void setPlaqueImmatriculation(String plaqueImmatriculation) { 
        this.plaqueImmatriculation = plaqueImmatriculation.toUpperCase(); 
    }

    public String getTypeVehicule() { 
        return typeVehicule; 
    }
    
    public void setTypeVehicule(String typeVehicule) { 
        this.typeVehicule = typeVehicule; 
    }

    public String getMarque() { 
        return marque; 
    }
    
    public void setMarque(String marque) { 
        this.marque = marque; 
    }

    public String getModele() { 
        return modele; 
    }
    
    public void setModele(String modele) { 
        this.modele = modele; 
    }

    public LocalDate getDateAjout() { 
        return dateAjout; 
    }
    
    public void setDateAjout(LocalDate dateAjout) { 
        this.dateAjout = dateAjout; 
    }

    public boolean isEstPrincipal() { 
        return estPrincipal; 
    }
    
    public void setEstPrincipal(boolean estPrincipal) { 
        this.estPrincipal = estPrincipal; 
    }

    // ==================== MÉTHODES UTILITAIRES ====================

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
        
        String plaque = plaqueImmatriculation
            .replaceAll("[^A-Z0-9]", "") // Garder seulement lettres et chiffres
            .replaceAll("\\s+", "");     // Supprimer les espaces
        
        if (plaque.length() == 7) {
            // Format SIV: AB123CD -> AB-123-CD
            return plaque.substring(0, 2) + "-" + 
                   plaque.substring(2, 5) + "-" + 
                   plaque.substring(5);
        } else if (plaque.length() == 8) {
            // Format ancien: 1234AB56 -> 1234-AB-56
            return plaque.substring(0, 4) + "-" + 
                   plaque.substring(4, 6) + "-" + 
                   plaque.substring(6);
        } else {
            // Retourner la plaque telle quelle si format non reconnu
            return plaqueImmatriculation;
        }
    }
    
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
     * Retourne une description complète du véhicule.
     * 
     * @return Description incluant marque, modèle, plaque et type
     */
    public String getDescriptionComplete() {
        StringBuilder sb = new StringBuilder();
        
        if (marque != null && !marque.isEmpty()) {
            sb.append(marque);
            if (modele != null && !modele.isEmpty()) {
                sb.append(" ").append(modele);
            }
            sb.append(" - ");
        }
        
        sb.append(getPlaqueFormatee());
        sb.append(" (").append(typeVehicule).append(")");
        
        if (estPrincipal) {
            sb.append(" ★"); // Étoile pour indiquer le véhicule principal
        }
        
        return sb.toString();
    }
    
    /**
     * Vérifie si le véhicule a une marque et un modèle spécifiés.
     * 
     * @return true si marque ET modèle sont non null et non vides, false sinon
     */
    public boolean aMarqueModeleComplets() {
        return marque != null && !marque.trim().isEmpty() &&
               modele != null && !modele.trim().isEmpty();
    }
    
    /**
     * Vérifie si le véhicule est récent (ajouté dans les 30 derniers jours).
     * 
     * @return true si le véhicule a été ajouté récemment, false sinon
     */
    public boolean estRecemmentAjoute() {
        if (dateAjout == null) {
            return false;
        }
        LocalDate ilYA30Jours = LocalDate.now().minusDays(30);
        return dateAjout.isAfter(ilYA30Jours) || dateAjout.isEqual(ilYA30Jours);
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
        
        String plaque = plaqueImmatriculation
            .replaceAll("[^A-Z0-9]", "") // Nettoyer la plaque
            .replaceAll("\\s+", "");
        
        // Vérifier la longueur
        if (plaque.length() < 7 || plaque.length() > 9) {
            return false;
        }
        
        // Vérifier les formats français courants
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
     * Retourne l'âge du véhicule dans le système en jours.
     * 
     * @return Nombre de jours depuis l'ajout, ou -1 si dateAjout est null
     */
    public long getAgeEnJours() {
        if (dateAjout == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(dateAjout, LocalDate.now());
    }
    
    /**
     * Définit ce véhicule comme principal.
     * Cette méthode pourrait être étendue pour gérer la logique
     * de "un seul véhicule principal par usager".
     */
    public void definirCommePrincipal() {
        this.estPrincipal = true;
    }
    
    /**
     * Définit ce véhicule comme secondaire.
     */
    public void definirCommeSecondaire() {
        this.estPrincipal = false;
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
        return getPlaqueFormatee() + " - " + typeVehicule + 
               (marque != null ? " " + marque : "") + 
               (modele != null ? " " + modele : "") + 
               (estPrincipal ? " (Principal)" : "");
    }
    
    /**
     * Comparaison d'égalité basée sur l'ID et la plaque.
     * 
     * @param obj L'objet à comparer
     * @return true si les associations sont identiques, false sinon
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        VehiculeUsager that = (VehiculeUsager) obj;
        
        // Comparaison par ID si disponible
        if (idVehiculeUsager != 0 && that.idVehiculeUsager != 0) {
            return idVehiculeUsager == that.idVehiculeUsager;
        }
        
        // Sinon comparaison par usager et plaque
        return idUsager == that.idUsager && 
               plaqueImmatriculation != null && 
               plaqueImmatriculation.equalsIgnoreCase(that.plaqueImmatriculation);
    }
    
    /**
     * Code de hachage basé sur l'ID, l'usager et la plaque.
     * 
     * @return Code de hachage
     */
    @Override
    public int hashCode() {
        int result = idVehiculeUsager;
        result = 31 * result + idUsager;
        result = 31 * result + (plaqueImmatriculation != null ? plaqueImmatriculation.hashCode() : 0);
        return result;
    }
}
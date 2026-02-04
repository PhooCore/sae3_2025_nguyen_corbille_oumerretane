package modele;

/**
 * Classe représentant une relation "favori" entre un usager et un parking.
 * Cette classe matérialise le fait qu'un usager a marqué un parking comme favori.
 * Elle sert généralement de table d'association dans une base de données.
 */
public class Favori {
    // Identifiant de l'usager qui a ajouté le parking en favori
    private int idUsager;
    // Identifiant du parking marqué comme favori
    private String idParking;

    /**
     * Constructeur pour créer une relation favori.
     * 
     * @param idUsager Identifiant de l'usager propriétaire du favori
     * @param idParking Identifiant du parking marqué comme favori
     */
    public Favori(int idUsager, String idParking) {
        this.idUsager = idUsager;
        this.idParking = idParking;
    }

    // ==================== GETTERS & SETTERS ====================
    
    /**
     * Retourne l'identifiant de l'usager.
     * 
     * @return L'identifiant de l'usager
     */
    public int getIdUsager() {
        return idUsager;
    }

    /**
     * Modifie l'identifiant de l'usager.
     * 
     * @param idUsager Le nouvel identifiant de l'usager
     */
    public void setIdUsager(int idUsager) {
        this.idUsager = idUsager;
    }

    /**
     * Retourne l'identifiant du parking favori.
     * 
     * @return L'identifiant du parking
     */
    public String getIdParking() {
        return idParking;
    }

    /**
     * Modifie l'identifiant du parking favori.
     * 
     * @param idParking Le nouvel identifiant du parking
     */
    public void setIdParking(String idParking) {
        this.idParking = idParking;
    }
    
    // ==================== MÉTHODES STANDARD ====================
    
    /**
     * Redéfinition de la méthode equals pour comparer deux objets Favori.
     * Deux favoris sont considérés égaux s'ils ont le même usager et le même parking.
     * 
     * @param obj L'objet à comparer
     * @return true si les deux favoris sont identiques, false sinon
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Favori favori = (Favori) obj;
        return idUsager == favori.idUsager && 
               idParking.equals(favori.idParking);
    }
    
    /**
     * Redéfinition de la méthode hashCode pour correspondre à l'implémentation de equals.
     * Utile pour l'utilisation dans des collections comme HashSet ou HashMap.
     * 
     * @return Le code de hachage de l'objet
     */
    @Override
    public int hashCode() {
        int result = Integer.hashCode(idUsager);
        result = 31 * result + idParking.hashCode();
        return result;
    }
    
    /**
     * Représentation textuelle de l'objet pour le débogage.
     * 
     * @return Une chaîne formatée contenant les informations du favori
     */
    @Override
    public String toString() {
        return "Favori [idUsager=" + idUsager + 
               ", idParking=" + idParking + "]";
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Vérifie si ce favori concerne un usager spécifique.
     * 
     * @param idUsagerTest L'identifiant de l'usager à vérifier
     * @return true si l'usager correspond, false sinon
     */
    public boolean concerneUsager(int idUsagerTest) {
        return this.idUsager == idUsagerTest;
    }
    
    /**
     * Vérifie si ce favori concerne un parking spécifique.
     * 
     * @param idParkingTest L'identifiant du parking à vérifier
     * @return true si le parking correspond, false sinon
     */
    public boolean concerneParking(String idParkingTest) {
        return this.idParking.equals(idParkingTest);
    }
}
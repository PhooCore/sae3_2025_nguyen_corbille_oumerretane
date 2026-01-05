package modele.dao;

import modele.Parking;
import modele.dao.requetes.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ParkingDAO extends DaoModele<Parking> {
    
    private static ParkingDAO instance;
    
    // Constructeur privé pour le singleton
    private ParkingDAO() {}
    
    // Méthode pour obtenir l'instance unique (Singleton)
    public static ParkingDAO getInstance() {
        if (instance == null) {
            instance = new ParkingDAO();
        }
        return instance;
    }
    
    @Override
    protected Parking creerInstance(ResultSet curseur) throws SQLException {
        Parking parking = new Parking(
            curseur.getString("id_parking"),
            curseur.getString("libelle_parking"),
            curseur.getString("adresse_parking"),
            curseur.getInt("nombre_places"),
            curseur.getInt("places_disponibles"),
            curseur.getDouble("hauteur_parking"),
            curseur.getBoolean("tarif_soiree"),
            curseur.getBoolean("has_moto"),
            curseur.getInt("places_moto"),
            curseur.getInt("places_moto_disponibles"),
            curseur.getBoolean("est_relais")
        );
        
        // AJOUTER CETTE PARTIE pour récupérer les coordonnées
        try {
            Float posX = curseur.getFloat("position_x");
            Float posY = curseur.getFloat("position_y");
            
            if (!curseur.wasNull()) {
                parking.setPositionX(posX);
                parking.setPositionY(posY);
            }
        } catch (SQLException e) {
            // Les colonnes n'existent peut-être pas, ignorer l'erreur
            System.err.println("Colonnes position_x/position_y non trouvées: " + e.getMessage());
        }
        
        return parking;
    }
    
    @Override
    public List<Parking> findAll() throws SQLException {
        RequeteSelectParking req = new RequeteSelectParking();
        return find(req);
    }
    
    @Override
    public Parking findById(String... id) throws SQLException {
        if (id.length == 0) {
            return null;
        }
        RequeteSelectParkingById req = new RequeteSelectParkingById();
        return findById(req, id[0]);
    }
    
    @Override
    public void create(Parking parking) throws SQLException {
        RequeteInsertParking req = new RequeteInsertParking();
        miseAJour(req, parking);
    }
    
    @Override
    public void update(Parking parking) throws SQLException {
        RequeteUpdateParking req = new RequeteUpdateParking();
        miseAJour(req, parking);
    }
    
    @Override
    public void delete(Parking parking) throws SQLException {
        RequeteDeleteParking req = new RequeteDeleteParking();
        miseAJour(req, parking);
    }
    
    /**
     * Méthode pour sélectionner avec une requête préparée (manquante)
     */
    public List<Parking> select(PreparedStatement prSt) throws SQLException {
        List<Parking> liste = new ArrayList<>();
        
        try (ResultSet rs = prSt.executeQuery()) {
            while (rs.next()) {
                liste.add(creerInstance(rs));
            }
        }
        
        return liste;
    }
    /**
     * Crée un nouveau parking (méthode pratique pour le contrôleur)
     */
    public boolean creerParking(Parking parking) {
        try {
            create(parking);
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur création parking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Met à jour un parking existant (méthode pratique pour le contrôleur)
     */
    public boolean mettreAJourParking(Parking parking) {
        try {
            update(parking);
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour parking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Supprime un parking (méthode pratique pour le contrôleur - version avec objet Parking)
     */
    public boolean supprimerParking(Parking parking) {
        try {
            delete(parking);
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur suppression parking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Supprime un parking par son ID (méthode pratique pour le contrôleur - version avec String)
     */
    public boolean supprimerParking(String idParking) {
        try {
            Parking parking = findById(idParking);
            if (parking != null) {
                delete(parking);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur suppression parking: " + e.getMessage());
            return false;
        }
    }
    // Méthodes spécifiques pour les parkings
    
    /**
     * Récupère tous les parkings avec leurs positions
     */
    public List<Parking> getAllParkings() throws SQLException {
        return findAll();
    }
    
    /**
     * Récupère un parking par son ID avec sa position
     */
    public Parking getParkingById(String idParking) throws SQLException {
        return findById(idParking);
    }
    
    /**
     * Recherche des parkings par terme
     */
    public List<Parking> rechercherParkings(String terme) throws SQLException {
        String sql = "SELECT * FROM Parking " +
                    "WHERE libelle_parking LIKE ? OR adresse_parking LIKE ? " +
                    "ORDER BY libelle_parking";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String termeRecherche = "%" + terme + "%";
            stmt.setString(1, termeRecherche);
            stmt.setString(2, termeRecherche);
            
            return select(stmt);
        }
    }
    
    /**
     * Décrémente le nombre de places disponibles d'un parking (voiture/camion)
     */
    public boolean decrementerPlacesDisponibles(String idParking) throws SQLException {
        String sql = "UPDATE Parking SET places_disponibles = places_disponibles - 1 " +
                    "WHERE id_parking = ? AND places_disponibles > 0";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
        }
    }
    
    /**
     * Incrémente le nombre de places disponibles d'un parking (voiture/camion)
     */
    public boolean incrementerPlacesDisponibles(String idParking) throws SQLException {
        String sql = "UPDATE Parking SET places_disponibles = places_disponibles + 1 " +
                    "WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
        }
    }
    
    /**
     * Décrémente le nombre de places moto disponibles
     */
    public boolean decrementerPlacesMotoDisponibles(String idParking) throws SQLException {
        String sql = "UPDATE Parking SET places_moto_disponibles = places_moto_disponibles - 1 " +
                    "WHERE id_parking = ? AND has_moto = TRUE AND places_moto_disponibles > 0";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
        }
    }
    
    /**
     * Incrémente le nombre de places moto disponibles
     */
    public boolean incrementerPlacesMotoDisponibles(String idParking) throws SQLException {
        String sql = "UPDATE Parking SET places_moto_disponibles = places_moto_disponibles + 1 " +
                    "WHERE id_parking = ? AND has_moto = TRUE";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
        }
    }
    
    /**
     * Récupère le nombre de places disponibles actuel (voiture/camion)
     */
    public int getPlacesDisponibles(String idParking) throws SQLException {
        String sql = "SELECT places_disponibles FROM Parking WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("places_disponibles");
                }
            }
        }
        return 0;
    }
    
    /**
     * Récupère le nombre de places moto disponibles
     */
    public int getPlacesMotoDisponibles(String idParking) throws SQLException {
        String sql = "SELECT places_moto_disponibles FROM Parking WHERE id_parking = ? AND has_moto = TRUE";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("places_moto_disponibles");
                }
            }
        }
        return 0;
    }
    
    /**
     * Vérifie si un parking a des places moto disponibles
     */
    public boolean hasPlacesMotoDisponibles(String idParking) throws SQLException {
        String sql = "SELECT places_moto_disponibles FROM Parking WHERE id_parking = ? AND has_moto = TRUE";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("places_moto_disponibles") > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Vérifie si un ID parking existe déjà (statique)
     */
    public static boolean idParkingExiste(String idParking) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM Parking WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }
    /**
     * Génère un nouvel ID parking unique
     */
    public String genererNouvelIdParking() throws SQLException {
        // Ancienne méthode - gardée pour compatibilité
        String baseId = "PARK_" + System.currentTimeMillis();
        String id = baseId;
        int counter = 1;
        
        while (idParkingExiste(id)) {
            id = baseId + "_" + counter;
            counter++;
            if (counter > 1000) {
                id = "PARK_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
            }
        }
        
        return id;
    }
    
    /**
     * Génère un ID basé sur le nom
     */
    public String genererIdDepuisNom(String nom) throws SQLException {
        if (nom == null || nom.isEmpty()) {
            return genererNouvelIdParking();
        }
        
        String id = nom.toUpperCase()
            .replaceAll(" ", "_")
            .replaceAll("[^A-Z0-9_]", "")
            .replaceAll("__+", "_");
        
        if (!id.startsWith("PARK_")) {
            id = "PARK_" + id;
        }
        
        // Vérifier et rendre unique si nécessaire
        String baseId = id;
        int counter = 1;
        
        while (idParkingExiste(id)) {
            id = baseId + "_" + counter;
            counter++;
            if (counter > 1000) {
                return genererNouvelIdParking();
            }
        }
        
        return id;
    }
    
    /**
     * Recherche les parkings avec filtres avancés
     */
    public List<Parking> rechercherParkingsAvances(String terme, 
                                                  boolean avecTarifSoiree, 
                                                  boolean avecMoto, 
                                                  int placesMin) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM Parking WHERE 1=1"
        );
        
        List<Object> parametres = new ArrayList<>();
        
        if (terme != null && !terme.trim().isEmpty()) {
            sql.append(" AND (libelle_parking LIKE ? OR adresse_parking LIKE ?)");
            String termeRecherche = "%" + terme + "%";
            parametres.add(termeRecherche);
            parametres.add(termeRecherche);
        }
        
        if (avecTarifSoiree) {
            sql.append(" AND tarif_soiree = TRUE");
        }
        
        if (avecMoto) {
            sql.append(" AND has_moto = TRUE AND places_moto_disponibles > 0");
        }
        
        if (placesMin > 0) {
            sql.append(" AND places_disponibles >= ?");
            parametres.add(placesMin);
        }
        
        sql.append(" ORDER BY libelle_parking");
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < parametres.size(); i++) {
                stmt.setObject(i + 1, parametres.get(i));
            }
            
            return select(stmt);
        }
    }
    
    /**
     * Vérifie si une colonne existe dans une table
     */
    private boolean columnExists(String tableName, String columnName) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM information_schema.columns " +
                    "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Met à jour uniquement la position d'un parking
     */
    public boolean mettreAJourPositionParking(String idParking, float positionX, float positionY) throws SQLException {
        if (!columnExists("Parking", "position_x") || !columnExists("Parking", "position_y")) {
            System.err.println("Les colonnes position_x/position_y n'existent pas");
            return false;
        }
        
        String sql = "UPDATE Parking SET position_x = ?, position_y = ? WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setFloat(1, positionX);
            stmt.setFloat(2, positionY);
            stmt.setString(3, idParking);
            
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
        }
    }
    

    
    /**
     * Ajoute un parking (statique)
     */
    public static boolean ajouterParking(Parking parking) throws SQLException {
        try {
            getInstance().create(parking);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Trouve les parkings les plus proches d'un parking donné
     */
    public List<Parking> trouverParkingsProches(String idParkingOrigine, int nombreParkings) throws SQLException {
        List<Parking> result = new ArrayList<Parking>();
        
        
        try {
            // Récupérer le parking d'origine
            Parking parkingOrigine = findById(idParkingOrigine);
            
            if (parkingOrigine == null) {
                return result;
            }
            
            // Si le parking d'origine n'a pas de coordonnées, retourner vide
            if (parkingOrigine.getPositionX() == null || parkingOrigine.getPositionY() == null) {
                
                // Fallback: retourner 5 parkings aléatoires avec places disponibles
                return trouverParkingsAlternatifsFallback(nombreParkings);
            }
            
            // Récupérer tous les parkings
            List<Parking> tousParkings = findAll();
            
            // Liste pour stocker les parkings avec distances
            List<ParkingDistance> parkingsAvecDistances = new ArrayList<ParkingDistance>();
            
            for (Parking parking : tousParkings) {
                // Ne pas inclure le parking d'origine
                if (parking.getIdParking().equals(idParkingOrigine)) {
                    continue;
                }
                
                // Vérifier que le parking a des coordonnées
                if (parking.getPositionX() == null || parking.getPositionY() == null) {
                    continue;
                }
                
                // Vérifier qu'il y a des places disponibles
                if (parking.getPlacesDisponibles() <= 0) {
                    continue;
                }
                
                // Calculer la distance
                double distance = calculerDistance(
                    parkingOrigine.getPositionX(), parkingOrigine.getPositionY(),
                    parking.getPositionX(), parking.getPositionY()
                );
                
                
                parkingsAvecDistances.add(new ParkingDistance(parking, distance));
            }
            
            
            // Trier par distance
            Collections.sort(parkingsAvecDistances, new Comparator<ParkingDistance>() {
                @Override
                public int compare(ParkingDistance p1, ParkingDistance p2) {
                    return Double.compare(p1.getDistance(), p2.getDistance());
                }
            });
            
            // Prendre les N premiers
            int limit = Math.min(nombreParkings, parkingsAvecDistances.size());
            for (int i = 0; i < limit; i++) {
                result.add(parkingsAvecDistances.get(i).getParking());
            }
            
        } catch (Exception e) {
            System.err.println("ERREUR dans trouverParkingsProches: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    /**
     * Fallback: retourne des parkings aléatoires avec places disponibles
     */
    private List<Parking> trouverParkingsAlternatifsFallback(int nombreParkings) throws SQLException {
        List<Parking> result = new ArrayList<Parking>();
        List<Parking> tousParkings = findAll();
        
        // Mélanger la liste pour avoir un ordre aléatoire
        Collections.shuffle(tousParkings);
        
        int count = 0;
        for (Parking parking : tousParkings) {
            if (parking.getPlacesDisponibles() > 0 && count < nombreParkings) {
                result.add(parking);
                count++;
            }
        }
       
        return result;
    }
    /**
     * Calcul de distance simplifié (distance euclidienne)
     */
    private double calculerDistance(float x1, float y1, float x2, float y2) {
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
    
    /**
     * Classe interne pour stocker un parking avec sa distance
     */
    private static class ParkingDistance {
        private Parking parking;
        private double distance;
        
        public ParkingDistance(Parking parking, double distance) {
            this.parking = parking;
            this.distance = distance;
        }
        
        public Parking getParking() {
            return parking;
        }
        
        public double getDistance() {
            return distance;
        }
    }
}
package modele.dao;

import modele.Abonnement;
import modele.Parking;
import modele.dao.requetes.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TarifParkingDAO {
    
    private static TarifParkingDAO instance;
    
    // Liste des parkings gratuits
    private static final String[] PARKINGS_GRATUITS = {
        "PARK_VIGUERIE", "PARK_BOULE", "PARK_VELODROME",
        "PARK_PONTS_JUMEAUX", "PARK_BONNEFOY", "PARK_MIRAIL", "PARK_CROIX_PIERRE"
    };
    
    // Constructeur privé pour le singleton
    private TarifParkingDAO() {}
    
    // Méthode pour obtenir l'instance unique (Singleton)
    public static TarifParkingDAO getInstance() {
        if (instance == null) {
            instance = new TarifParkingDAO();
        }
        return instance;
    }
    
    // ===================== MÉTHODES STATIQUES =====================
    
    /**
     * Calcule le coût du stationnement en parking (méthode statique)
     */
    public static double calculerCoutParking(LocalDateTime heureArrivee, LocalDateTime heureDepart, String idParking) {
        try {
            return getInstance().calculerCoutParkingPrive(heureArrivee, heureDepart, idParking);
        } catch (Exception e) {
            System.err.println("Erreur calcul coût parking: " + e.getMessage());
            return 0.0;
        }
    }
    
    // ===================== MÉTHODES D'INSTANCE =====================
    
    /**
     * Récupère la liste des parkings relais (gratuits mais accessibles seulement si on a une carte Tisséo)
     */
    public List<String> getParkingsRelais() throws SQLException {
        List<String> parkingsRelais = new ArrayList<>();
        String sql = "SELECT id_parking FROM Parking WHERE est_relais = 1";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                parkingsRelais.add(rs.getString("id_parking"));
            }
        }
        return parkingsRelais;
    }
    
    /**
     * Calcule le coût du stationnement en parking selon la durée réelle (méthode privée)
     */
    private double calculerCoutParkingPrive(LocalDateTime heureArrivee, LocalDateTime heureDepart, String idParking) throws SQLException {
        // Vérifier si le parking est gratuit
        if (estParkingGratuit(idParking)) {
            return 0.00;
        }
        
        if (estParkingRelais(idParking)) {
            return 0.00;
        }
        
        // Vérifier si le tarif soirée s'applique
        if (tarifSoireeApplicable(heureArrivee, heureDepart, idParking)) {
            return 5.90;
        }
        
        // Calcul normal de la durée
        long dureeMinutes = ChronoUnit.MINUTES.between(heureArrivee, heureDepart);
        
        // Minimum de 15 minutes
        if (dureeMinutes < 15) {
            dureeMinutes = 15;
        }
        
        // Tarification au quart d'heure
        double tarifQuartHeure = getTarifQuartHeure(idParking);
        int nombreQuarts = (int) Math.ceil(dureeMinutes / 15.0);
        
        double cout = nombreQuarts * tarifQuartHeure;
        
        // Forfait 24h maximum (tarif horaire * 24)
        double tarifHoraire = getTarifHoraire(idParking);
        double max24h = tarifHoraire * 24;
        
        if (cout > max24h && dureeMinutes <= (24 * 60)) {
            cout = max24h;
        }
        return cout;
    }
    
    /**
     * Récupère le tarif au quart d'heure pour un parking donné
     */
    public double getTarifQuartHeure(String idParking) {
        Map<String, Double> tarifs = new HashMap<>();
        
        // Parkings standards
        tarifs.put("PARK_CAPITOLE", 0.75);   // 3€/h
        tarifs.put("PARK_CARNOT", 0.75);     // 3€/h
        tarifs.put("PARK_ESQUIROL", 0.63);   // 2.50€/h
        tarifs.put("PARK_SAINT_ETIENNE", 0.63); // 2.50€/h
        tarifs.put("PARK_JEAN_JAURES", 0.50); // 2€/h
        tarifs.put("PARK_JEANNE_DARC", 0.50); // 2€/h
        tarifs.put("PARK_EUROPE", 0.50);     // 2€/h
        tarifs.put("PARK_VICTOR_HUGO", 0.50); // 2€/h
        tarifs.put("PARK_SAINT_AUBIN", 0.50); // 2€/h
        tarifs.put("PARK_SAINT_CYPRIEN", 0.50); // 2€/h
        tarifs.put("PARK_SAINT_MICHEL", 0.38); // 1.50€/h
        tarifs.put("PARK_MATABIAU", 1.00);   // 4€/h
        tarifs.put("PARK_ARNAUD_BERNARD", 0.38); // 1.50€/h
        tarifs.put("PARK_CARMES", 0.63);     // 2.50€/h
        
        // Parkings relais (tarif normal si pas de carte Tisséo)
        tarifs.put("PARK_SEPT_DENIERS", 0.25); // 1€/h
        tarifs.put("PARK_BAGATELLE", 0.25);  // 1€/h
        tarifs.put("PARK_JOLIMONT", 0.25);   // 1€/h
        tarifs.put("PARK_ARENES", 0.25);     // 1€/h
        
        return tarifs.getOrDefault(idParking, 0.50); // Tarif par défaut 2€/h
    }
    
    /**
     * Vérifie si le tarif soirée s'applique avec des règles précises
     */
    public boolean tarifSoireeApplicable(LocalDateTime heureArrivee, LocalDateTime heureDepart, String idParking) throws SQLException {
        // Vérifier si le parking propose le tarif soirée
        if (!proposeTarifSoiree(idParking)) {
            return false;
        }
        
        // Extraire les composants de temps
        int heureArriveeH = heureArrivee.getHour();
        int minuteArrivee = heureArrivee.getMinute();
        int heureDepartH = heureDepart.getHour();
        int minuteDepart = heureDepart.getMinute();
        
        // Calculer la durée totale en minutes
        long dureeMinutesTotal = java.time.Duration.between(heureArrivee, heureDepart).toMinutes();
        
        // Vérifier la durée maximale (8 heures = 480 minutes)
        if (dureeMinutesTotal > 480) {
            return false;
        }
        
        // Vérifier si l'arrivée est dans la plage tarif soirée (19h30 à minuit)
        boolean arriveeValide = false;
        
        // Cas 1: Arrivée entre 19h30 et 19h59
        if (heureArriveeH == 19 && minuteArrivee >= 30) {
            arriveeValide = true;
        }
        // Cas 2: Arrivée entre 20h et 23h
        else if (heureArriveeH >= 20 && heureArriveeH <= 23) {
            arriveeValide = true;
        }
        // Cas 3: Arrivée à minuit pile (0h00)
        else if (heureArriveeH == 0 && minuteArrivee == 0 && heureArrivee.toLocalDate().equals(heureDepart.toLocalDate())) {
            arriveeValide = true;
        }
        
        if (!arriveeValide) {
            return false;
        }
        
        // Vérifier si le départ est avant 3h00 du matin
        boolean departValide = false;
        
        // Si arrivée et départ même jour (arrivée entre 19h30 et minuit, départ avant minuit)
        if (heureArrivee.toLocalDate().equals(heureDepart.toLocalDate())) {
            departValide = heureDepartH < 24; // Départ avant minuit
        }
        // Départ le lendemain (cas normal pour tarif soirée)
        else {
            // Vérifier que le départ est le lendemain de l'arrivée
            LocalDateTime lendemainArrivee = heureArrivee.plusDays(1);
            if (heureDepart.toLocalDate().equals(lendemainArrivee.toLocalDate())) {
                departValide = heureDepartH < 3 || (heureDepartH == 3 && minuteDepart == 0);
            }
        }
        
        return departValide;
    }
    
    /**
     * Vérifie si une heure donnée est dans la plage du tarif soirée (pour affichage)
     */
    public boolean estDansPlageTarifSoiree(LocalDateTime heure) {
        if (heure == null) return false;
        
        int heureH = heure.getHour();
        int minute = heure.getMinute();
        
        // Entre 19h30 et minuit
        if (heureH == 19 && minute >= 30) {
            return true;
        } else if (heureH >= 20 && heureH <= 23) {
            return true;
        } else if (heureH == 0 && minute == 0) {
            return true; // Minuit pile
        }
        
        return false;
    }
    
    /**
     * Formate l'affichage des tarifs pour l'interface utilisateur
     */
    public String formaterAffichageTarifs(String idParking) throws SQLException {
        StringBuilder sb = new StringBuilder();
        
        if (estParkingGratuit(idParking)) {
            sb.append("Parking gratuit");
            return sb.toString();
        }
        if (estParkingRelais(idParking)) {
            sb.append("Parking relais : gratuit");
            return sb.toString();
        }
        
        double tarifHoraire = getTarifHoraire(idParking);
        sb.append(String.format("Tarif: %.2f€/h (%.2f€/15min)", tarifHoraire, tarifHoraire/4));
        
        if (proposeTarifSoiree(idParking)) {
            sb.append("\n");
            sb.append("Tarif soirée disponible: 5.90€");
            sb.append("\n(Arrivée 19h30-minuit, départ avant 3h)");
        }
        
        return sb.toString();
    }
    
    /**
     * Donne la description textuelle du tarif soirée
     */
    public String getDescriptionTarifSoiree() {
        return "Tarif Soirée: 5.90€\n" +
               "Conditions:\n" +
               "- Arrivée entre 19h30 et minuit\n" +
               "- Départ avant 3h le lendemain\n" +
               "- Durée maximale: 8 heures";
    }
    
    /**
     * Vérifie si le parking propose le tarif soirée
     */
    public boolean proposeTarifSoiree(String idParking) throws SQLException {
        String sql = "SELECT tarif_soiree FROM Parking WHERE id_parking = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("tarif_soiree");
            }
        }
        return false;
    }
    
    /**
     * Vérifie si le parking est gratuit
     */
    public boolean estParkingGratuit(String idParking) {
        for (String parking : PARKINGS_GRATUITS) {
            if (parking.equals(idParking)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Vérifie si c'est un parking relais
     */
    public boolean estParkingRelais(String idParking) throws SQLException {
        return getParkingsRelais().contains(idParking);
    }
    
    /**
     * Récupère le tarif horaire pour un parking
     */
    public double getTarifHoraire(String idParking) {
        return getTarifQuartHeure(idParking) * 4; // 4 quarts d'heure = 1 heure
    }
    
    /**
     * Calcule la durée totale en minutes entre deux dates
     */
    public long calculerDureeMinutes(LocalDateTime debut, LocalDateTime fin) {
        return ChronoUnit.MINUTES.between(debut, fin);
    }
    
    /**
     * Formate la durée en heures et minutes
     */
    public String formaterDuree(long minutes) {
        long heures = minutes / 60;
        long mins = minutes % 60;
        if (heures == 0) {
            return mins + " min";
        } else if (mins == 0) {
            return heures + " h";
        } else {
            return heures + " h " + mins + " min";
        }
    }
    
    /**
     * Vérifie si un parking existe
     */
    public boolean parkingExiste(String idParking) throws SQLException {
        String sql = "SELECT id_parking FROM Parking WHERE id_parking = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
    
    /**
     * Récupère les informations complètes d'un parking
     */
    public Map<String, Object> getInfosParking(String idParking) throws SQLException {
        Map<String, Object> infos = new HashMap<>();
        String sql = "SELECT libelle_parking, adresse_parking, nombre_places, hauteur_parking, tarif_soiree FROM Parking WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                infos.put("libelle", rs.getString("libelle_parking"));
                infos.put("adresse", rs.getString("adresse_parking"));
                infos.put("places", rs.getInt("nombre_places"));
                infos.put("hauteur", rs.getDouble("hauteur_parking"));
                infos.put("tarif_soiree", rs.getBoolean("tarif_soiree"));
                infos.put("gratuit", estParkingGratuit(idParking));
                infos.put("relais", estParkingRelais(idParking));
                infos.put("tarif_horaire", getTarifHoraire(idParking));
            }
        }
        return infos;
    }
    
    /**
     * Récupère tous les parkings avec leurs tarifs
     */
    public List<Parking> findAll() throws SQLException {
        return ParkingDAO.getInstance().findAll();
    }
    
    /**
     * Récupère un parking par son ID
     */
    public Parking findById(String... id) throws SQLException {
        if (id.length == 0) {
            return null;
        }
        return ParkingDAO.getInstance().findById(id[0]);
    }
    
    /**
     * Vérifie si un véhicule peut entrer dans le parking (hauteur suffisante)
     */
    public boolean verifierHauteurVehicule(String idParking, double hauteurVehicule) throws SQLException {
        String sql = "SELECT hauteur_parking FROM Parking WHERE id_parking = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double hauteurParking = rs.getDouble("hauteur_parking");
                // Si hauteurParking = 0, pas de restriction
                return hauteurParking == 0 || hauteurVehicule <= hauteurParking;
            }
        }
        return false;
    }
    
    /**
     * Récupère les parkings adaptés aux motos
     */
    public List<Parking> getParkingsPourMotos() throws SQLException {
        String sql = "SELECT * FROM Parking WHERE has_moto = TRUE AND places_moto_disponibles > 0 ORDER BY libelle_parking";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return ParkingDAO.getInstance().select(stmt);
        }
    }
    
    /**
     * Récupère les parkings adaptés aux véhicules hauts
     */
    public List<Parking> getParkingsPourVehiculesHauts(double hauteurMinimale) throws SQLException {
        String sql = "SELECT * FROM Parking WHERE hauteur_parking >= ? OR hauteur_parking = 0 ORDER BY libelle_parking";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, hauteurMinimale);
            return ParkingDAO.getInstance().select(stmt);
        }
    }
    
    /**
     * Vérifie si l'usager a un abonnement moto actif
     */
    public boolean aAbonnementMotoActif(int idUsager) throws SQLException {
        try {
            Abonnement abonnement = AbonnementDAO.getInstance().getAbonnementActif(idUsager);
            return abonnement != null && "ABO_MOTO_RESIDENT".equals(abonnement.getIdAbonnement());
        } catch (Exception e) {
            System.err.println("Erreur vérification abonnement moto: " + e.getMessage());
            return false;
        }
    }
    
}
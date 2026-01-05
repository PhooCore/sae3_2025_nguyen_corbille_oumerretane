package modele.dao;

import modele.Zone;
import modele.dao.requetes.RequeteSelectAllZones;
import modele.dao.requetes.RequeteSelectZoneById;
import modele.dao.requetes.RequeteSelectZonesRecherche;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

public class ZoneDAO extends DaoModele<Zone> implements Dao<Zone> {

    private static ZoneDAO instance;
    private static Iterateur<Zone> iterateur = null;

    // Constructeur privé pour le singleton
    private ZoneDAO() {}

    // Méthode pour obtenir l'instance unique (Singleton)
    public static ZoneDAO getInstance() {
        if (instance == null) {
            instance = new ZoneDAO();
        }
        return instance;
    }

    // Méthode statique pour compatibilité (celle qui manque)
    public static List<Zone> getAllZones() {
        try {
            return getInstance().findAll();
        } catch (SQLException e) {
            System.err.println("Erreur récupération zones: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public List<Zone> findAll() throws SQLException {
        RequeteSelectAllZones req = new RequeteSelectAllZones();
        return find(req);
    }

    @Override
    public Zone findById(String... id) throws SQLException {
        RequeteSelectZoneById req = new RequeteSelectZoneById();
        return findById(req, id);
    }

    @Override
    public void create(Zone zone) throws SQLException {
        throw new UnsupportedOperationException("Create non implémenté pour Zone");
    }

    @Override
    public void update(Zone zone) throws SQLException {
        throw new UnsupportedOperationException("Update non implémenté pour Zone");
    }

    @Override
    public void delete(Zone zone) throws SQLException {
        throw new UnsupportedOperationException("Delete non implémenté pour Zone");
    }

    /**
     * Récupère une zone par son ID (méthode spécifique)
     */
    public Zone getZoneById(String idZone) throws SQLException {
        return findById(idZone);
    }

    /**
     * Recherche des zones par terme
     */
    public List<Zone> rechercherZones(String terme) throws SQLException {
        RequeteSelectZonesRecherche req = new RequeteSelectZonesRecherche();
        return find(req, "%" + terme + "%", "%" + terme + "%");
    }

    @Override
    protected Zone creerInstance(ResultSet curseur) throws SQLException {
        String idZone = curseur.getString("id_zone");
        String libelleZone = curseur.getString("libelle_zone");
        String couleurZone = curseur.getString("couleur_zone");
        double tarifParHeure = curseur.getDouble("tarif_par_heure");
        
        String dureeMaxStr = curseur.getString("duree_max");
        LocalTime dureeMax = parseDureeMax(dureeMaxStr);
        
        return new Zone(idZone, libelleZone, couleurZone, tarifParHeure, dureeMax);
    }

    /**
     * Parse la durée maximale depuis un string MySQL
     * Gère le cas spécial "24:00:00" qui n'est pas un Time valide pour JDBC
     */
    private LocalTime parseDureeMax(String dureeMaxStr) {
        if (dureeMaxStr == null || dureeMaxStr.trim().isEmpty()) {
            return LocalTime.of(0, 0); // Valeur par défaut
        }
        
        // Gérer le cas "24:00:00" qui n'est pas un Time valide pour JDBC
        if ("24:00:00".equals(dureeMaxStr) || "24:00".equals(dureeMaxStr)) {
            // Convertir en 23:59:59 ou selon votre logique métier
            return LocalTime.of(23, 59, 59);
        }
        
        try {
            // Supprimer les millisecondes si présentes
            if (dureeMaxStr.length() > 8) {
                dureeMaxStr = dureeMaxStr.substring(0, 8);
            }
            return LocalTime.parse(dureeMaxStr);
        } catch (Exception e) {
            System.err.println("Erreur parsing durée max: " + dureeMaxStr + " - " + e.getMessage());
            return LocalTime.of(0, 0); // Valeur par défaut en cas d'erreur
        }
    }
    
    /**
     * Récupère l'itérateur statique
     */
    public static Iterateur<Zone> getIterateur() {
        return iterateur;
    }

    /**
     * Définit l'itérateur statique
     */
    public static void setIterateur(Iterateur<Zone> iterateur) {
        ZoneDAO.iterateur = iterateur;
    }

    /**
     * Méthode utilitaire pour fermer l'itérateur
     */
    public static void fermerIterateur() throws SQLException {
        if (iterateur != null) {
            iterateur.close();
            iterateur = null;
        }
    }
}
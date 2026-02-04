package modele.dao.requetes;

import modele.Zone;

public class RequeteSelectAllZones extends Requete<Zone> {
    @Override
    public String requete() {
        return "SELECT id_zone, libelle_zone, couleur_zone, tarif_par_heure, duree_max FROM zone " +
               "WHERE id_zone IN ('ZONE_BLEUE', 'ZONE_VERTE', 'ZONE_JAUNE', 'ZONE_ORANGE', 'ZONE_ROUGE') " +
               "ORDER BY libelle_zone";
    }
}
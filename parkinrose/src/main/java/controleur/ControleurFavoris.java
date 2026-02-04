package controleur;

import modele.Parking;
import modele.dao.FavoriDAO;
import ihm.Page_Favoris;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ControleurFavoris {
    
    private Page_Favoris vue;
    private int idUsager;
    private FavoriDAO favoriDAO;
    
    public ControleurFavoris(Page_Favoris vue, int idUsager) {
        this.vue = vue;
        this.idUsager = idUsager;
        this.favoriDAO = FavoriDAO.getInstance();
        
    }
    
    /**
     * Charge les parkings favoris depuis la base de données
     */
    public List<Parking> chargerParkingsFavoris() {
        try {
            return favoriDAO.getParkingsFavoris(idUsager);
        } catch (Exception e) {
            vue.afficherErreur("Erreur lors du chargement des parkings favoris");
            return new ArrayList<>();
        }
    }
    
    /**
     * Supprime un parking des favoris
     */
    public void supprimerFavori(Parking parking) {
        int choix = JOptionPane.showConfirmDialog(
            vue,
            "Retirer ce parking de vos favoris ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION
        );
        
        if (choix == JOptionPane.YES_OPTION) {
            boolean succes = favoriDAO.supprimerFavori(idUsager, parking.getIdParking());
            
            if (succes) {
                vue.rafraichirAffichage();
            } else {
                vue.afficherErreur("Erreur lors de la suppression du favori");
            }
        }
    }
    
    /**
     * Ajoute un parking aux favoris
     */
    public boolean ajouterFavori(String idParking) {
        return favoriDAO.ajouterFavori(idUsager, idParking);
    }
    
    /**
     * Vérifie si un parking est en favori
     */
    public boolean estFavori(String idParking) {
        try {
            return favoriDAO.estFavori(idUsager, idParking);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Bascule l'état favori d'un parking (ajoute ou supprime)
     */
    public boolean basculerFavori(String idParking) {
        try {
            if (favoriDAO.estFavori(idUsager, idParking)) {
                return favoriDAO.supprimerFavori(idUsager, idParking);
            } else {
                return favoriDAO.ajouterFavori(idUsager, idParking);
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
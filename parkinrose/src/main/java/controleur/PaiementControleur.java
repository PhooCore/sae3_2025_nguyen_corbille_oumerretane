package controleur;

import modele.Paiement;
import modele.Usager;
import modele.dao.PaiementDAO;
import modele.dao.StationnementDAO;
import modele.dao.TarifParkingDAO;
import modele.dao.UsagerDAO;
import ihm.Page_Paiement;
import ihm.Page_Principale;
import javax.swing.JOptionPane;
import java.time.LocalDateTime;

public class PaiementControleur {
    
    private String emailUtilisateur;
    private Usager usager;
    
    /**
     * Contrôleur pour la gestion des paiements
     * @param email l'email de l'utilisateur connecté
     */
    public PaiementControleur(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
    }
    
    /**
     * Traite un paiement pour un nouveau stationnement voirie
     * @param nomCarte le nom sur la carte
     * @param numeroCarte le numéro de carte
     * @param cvv le code CVV
     * @param montant le montant à payer
     * @param typeVehicule le type de véhicule
     * @param plaqueImmatriculation la plaque d'immatriculation
     * @param idZone l'ID de la zone
     * @param dureeHeures la durée en heures
     * @param dureeMinutes la durée en minutes
     * @param pagePaiement la page de paiement pour les callbacks
     * @return true si le paiement réussit
     */
    public boolean traiterPaiementVoirie(String nomCarte, String numeroCarte, String cvv,
                                        double montant, String typeVehicule, String plaqueImmatriculation,
                                        String idZone, int dureeHeures, int dureeMinutes,
                                        Page_Paiement pagePaiement) {
        
        // Validation des données de paiement
        if (!validerDonneesPaiement(nomCarte, numeroCarte, cvv, pagePaiement)) {
            return false;
        }
        
        try {
            // Création de l'objet Paiement
            Paiement paiement = new Paiement(nomCarte, numeroCarte, cvv, montant, usager.getIdUsager());
            
            // Enregistrement du paiement
            boolean paiementReussi = PaiementDAO.enregistrerPaiement(paiement);
            
            if (paiementReussi) {
                // Création du stationnement
                boolean stationnementReussi = StationnementDAO.creerStationnementVoirie(
                    usager.getIdUsager(),
                    typeVehicule,
                    plaqueImmatriculation,
                    idZone,
                    dureeHeures,
                    dureeMinutes,
                    montant,
                    paiement.getIdPaiement()
                );
                
                if (stationnementReussi) {
                    afficherConfirmationVoirie(montant, plaqueImmatriculation, idZone, 
                                              dureeHeures, dureeMinutes, pagePaiement);
                    redirigerVersAccueil(pagePaiement);
                    return true;
                } else {
                    JOptionPane.showMessageDialog(pagePaiement,
                        "Erreur lors de la création du stationnement",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                JOptionPane.showMessageDialog(pagePaiement,
                    "Erreur lors de l'enregistrement du paiement",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(pagePaiement,
                "Erreur lors du traitement du paiement: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Traite un paiement pour un stationnement parking existant
     * @param nomCarte le nom sur la carte
     * @param numeroCarte le numéro de carte
     * @param cvv le code CVV
     * @param montant le montant à payer
     * @param idStationnement l'ID du stationnement
     * @param heureDepart l'heure de départ
     * @param pagePaiement la page de paiement pour les callbacks
     * @return true si le paiement réussit
     */
    public boolean traiterPaiementParking(String nomCarte, String numeroCarte, String cvv,
                                         double montant, int idStationnement, LocalDateTime heureDepart,
                                         Page_Paiement pagePaiement) {
        
        // Validation des données de paiement
        if (!validerDonneesPaiement(nomCarte, numeroCarte, cvv, pagePaiement)) {
            return false;
        }
        
        try {
            // Création de l'objet Paiement
            Paiement paiement = new Paiement(nomCarte, numeroCarte, cvv, montant, usager.getIdUsager());
            
            // Enregistrement du paiement
            boolean paiementReussi = PaiementDAO.enregistrerPaiement(paiement);
            
            if (paiementReussi) {
                // Mise à jour du stationnement
                boolean stationnementReussi = StationnementDAO.terminerStationnementParking(
                    idStationnement,
                    heureDepart,
                    montant,
                    paiement.getIdPaiement()
                );
                
                if (stationnementReussi) {
                    afficherConfirmationParking(montant, idStationnement, pagePaiement);
                    redirigerVersAccueil(pagePaiement);
                    return true;
                } else {
                    JOptionPane.showMessageDialog(pagePaiement,
                        "Erreur lors de la mise à jour du stationnement",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                JOptionPane.showMessageDialog(pagePaiement,
                    "Erreur lors de l'enregistrement du paiement",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(pagePaiement,
                "Erreur lors du traitement du paiement: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Valide les données de paiement
     */
    private boolean validerDonneesPaiement(String nomCarte, String numeroCarte, String cvv,
                                          Page_Paiement pagePaiement) {
        
        if (nomCarte == null || nomCarte.trim().isEmpty()) {
            JOptionPane.showMessageDialog(pagePaiement, 
                "Veuillez saisir le nom sur la carte", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validation du numéro de carte (16 chiffres)
        String numeroCarteNettoye = numeroCarte.trim().replaceAll("\\s+", "");
        if (numeroCarteNettoye.isEmpty() || numeroCarteNettoye.length() < 16 || !numeroCarteNettoye.matches("\\d+")) {
            JOptionPane.showMessageDialog(pagePaiement, 
                "Numéro de carte invalide (16 chiffres requis)", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validation du CVV (3 chiffres)
        if (cvv == null || cvv.length() != 3 || !cvv.matches("\\d+")) {
            JOptionPane.showMessageDialog(pagePaiement, 
                "CVV invalide (3 chiffres requis)", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Affiche la confirmation pour un paiement voirie
     */
    private void afficherConfirmationVoirie(double montant, String plaqueImmatriculation,
                                           String idZone, int dureeHeures, int dureeMinutes,
                                           Page_Paiement pagePaiement) {
        String message = "Paiement effectué avec succès !\n\n" +
                       "Stationnement confirmé pour " + plaqueImmatriculation + "\n" +
                       "Zone: " + idZone + "\n" +
                       "Durée: " + dureeHeures + "h" + dureeMinutes + "min\n" +
                       "Montant: " + String.format("%.2f", montant) + " €";
        
        JOptionPane.showMessageDialog(pagePaiement,
            message,
            "Paiement réussi",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Affiche la confirmation pour un paiement parking
     */
    private void afficherConfirmationParking(double montant, int idStationnement, Page_Paiement pagePaiement) {
        String message = "Paiement effectué avec succès !\n\n" +
                       "Stationnement terminé\n" +
                       "Montant: " + String.format("%.2f", montant) + " €\n\n" +
                       "Vous pouvez quitter le parking.";
        
        JOptionPane.showMessageDialog(pagePaiement,
            message,
            "Paiement réussi",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Redirige vers la page principale
     */
    private void redirigerVersAccueil(Page_Paiement pagePaiement) {
        Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
        pagePrincipale.setVisible(true);
        pagePaiement.dispose();
    }
    
    /**
     * Récupère l'historique des paiements de l'utilisateur
     * @return la liste des paiements
     */
    public java.util.List<Paiement> getHistoriquePaiements() {
        if (usager != null) {
            return PaiementDAO.getPaiementsByUsager(usager.getIdUsager());
        }
        return java.util.Collections.emptyList();
    }
}
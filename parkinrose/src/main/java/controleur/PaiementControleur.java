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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PaiementControleur {
    
    private String emailUtilisateur;
    private Usager usager;
    
    public PaiementControleur(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
    }
    
    public boolean traiterPaiementVoirie(String nomCarte, String numeroCarte, String dateExpiration, String cvv,
                                        double montant, String typeVehicule, String plaqueImmatriculation,
                                        String idZone, int dureeHeures, int dureeMinutes,
                                        Page_Paiement pagePaiement) {
        
        if (!validerDonneesPaiement(nomCarte, numeroCarte, dateExpiration, cvv, pagePaiement)) {
            return false;
        }
        
        try {
            Paiement paiement = new Paiement(nomCarte, numeroCarte, cvv, montant, usager.getIdUsager());
            boolean paiementReussi = PaiementDAO.enregistrerPaiement(paiement);
            
            if (paiementReussi) {
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
    
    public boolean traiterPaiementParking(String nomCarte, String numeroCarte, String dateExpiration, String cvv,
                                         double montant, int idStationnement, LocalDateTime heureDepart,
                                         Page_Paiement pagePaiement) {
        
        if (!validerDonneesPaiement(nomCarte, numeroCarte, dateExpiration, cvv, pagePaiement)) {
            return false;
        }
        
        try {
            Paiement paiement = new Paiement(nomCarte, numeroCarte, cvv, montant, usager.getIdUsager());
            boolean paiementReussi = PaiementDAO.enregistrerPaiement(paiement);
            
            if (paiementReussi) {
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
    
    public boolean validerDonneesPaiement(String nomCarte, String numeroCarte, String dateExpiration, String cvv,
                                         javax.swing.JFrame parent) {
        
        // Validation du nom sur la carte
        if (nomCarte == null || nomCarte.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, 
                "Veuillez saisir le nom sur la carte", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validation du numéro de carte (16 chiffres)
        String numeroCarteNettoye = numeroCarte.trim().replaceAll("\\s+", "");
        if (numeroCarteNettoye.isEmpty() || !numeroCarteNettoye.matches("\\d{16}")) {
            JOptionPane.showMessageDialog(parent, 
                "Numéro de carte invalide (16 chiffres requis)", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validation de la date d'expiration
        if (dateExpiration == null || dateExpiration.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, 
                "Veuillez saisir la date d'expiration", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validation du format de la date (MM/AA ou MM/YYYY)
        if (!validerFormatDateExpiration(dateExpiration.trim())) {
            JOptionPane.showMessageDialog(parent, 
                "Format de date invalide. Utilisez MM/AA (ex: 12/25)", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Vérification que la carte n'est pas expirée
        if (!estCarteNonExpiree(dateExpiration.trim())) {
            JOptionPane.showMessageDialog(parent, 
                "La carte est expirée", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validation du CVV (3 ou 4 chiffres)
        String cvvNettoye = cvv.trim();
        if (cvvNettoye.isEmpty() || !cvvNettoye.matches("\\d{3,4}")) {
            JOptionPane.showMessageDialog(parent, 
                "CVV invalide (3 ou 4 chiffres requis)", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Valide le format de la date d'expiration (MM/AA ou MM/YYYY)
     */
    private boolean validerFormatDateExpiration(String dateExpiration) {
        // Accepter les formats MM/AA et MM/YYYY
        if (!dateExpiration.matches("\\d{2}/\\d{2,4}")) {
            return false;
        }
        
        try {
            String[] parties = dateExpiration.split("/");
            int mois = Integer.parseInt(parties[0]);
            int annee = Integer.parseInt(parties[1]);
            
            // Validation du mois (1-12)
            if (mois < 1 || mois > 12) {
                return false;
            }
            
            // Validation de l'année
            if (annee < 0) {
                return false;
            }
            
            return true;
            
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Vérifie si la carte n'est pas expirée
     */
    private boolean estCarteNonExpiree(String dateExpiration) {
        try {
            String[] parties = dateExpiration.split("/");
            int mois = Integer.parseInt(parties[0]);
            int annee = Integer.parseInt(parties[1]);
            
            // Si l'année est sur 2 chiffres, convertir en 4 chiffres
            if (annee < 100) {
                annee += 2000; // Supposer le 21ème siècle
            }
            
            // Créer l'objet YearMonth pour la date d'expiration
            YearMonth expiration = YearMonth.of(annee, mois);
            
            // Obtenir le mois et l'année actuels
            YearMonth maintenant = YearMonth.now();
            
            // La carte est valide si elle expire ce mois-ci ou plus tard
            return !expiration.isBefore(maintenant);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Formate la date d'expiration pour l'affichage
     */
    public String formaterDateExpiration(String dateExpiration) {
        try {
            String[] parties = dateExpiration.split("/");
            int mois = Integer.parseInt(parties[0]);
            int annee = Integer.parseInt(parties[1]);
            
            if (annee < 100) {
                annee += 2000;
            }
            
            return String.format("%02d/%d", mois, annee);
        } catch (Exception e) {
            return dateExpiration;
        }
    }
    
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
    
    private void redirigerVersAccueil(Page_Paiement pagePaiement) {
        Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
        pagePrincipale.setVisible(true);
        pagePaiement.dispose();
    }
    
    public java.util.List<Paiement> getHistoriquePaiements() {
        if (usager != null) {
            return PaiementDAO.getPaiementsByUsager(usager.getIdUsager());
        }
        return java.util.Collections.emptyList();
    }
    /**
     * Valide toutes les données du formulaire de paiement
     */
    public boolean validerFormulairePaiementComplet(String nomCarte, String numeroCarte, 
                                                   String dateExpiration, String cvv,
                                                   javax.swing.JFrame parent) {
        
        // Validation du nom sur la carte
        if (nomCarte == null || nomCarte.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, 
                "Veuillez saisir le nom sur la carte", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validation du numéro de carte
        String numeroCarteNettoye = numeroCarte.trim().replaceAll("\\s+", "");
        if (numeroCarteNettoye.isEmpty() || !numeroCarteNettoye.matches("\\d{16}")) {
            JOptionPane.showMessageDialog(parent, 
                "Numéro de carte invalide (16 chiffres requis)", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validation de la date d'expiration
        if (dateExpiration == null || dateExpiration.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, 
                "Veuillez saisir la date d'expiration", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validation du CVV
        String cvvNettoye = cvv.trim();
        if (cvvNettoye.isEmpty() || !cvvNettoye.matches("\\d{3,4}")) {
            JOptionPane.showMessageDialog(parent, 
                "CVV invalide (3 ou 4 chiffres requis)", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validation métier (format date + expiration)
        return validerDonneesPaiement(nomCarte, numeroCarte, dateExpiration, cvv, parent);
    }

    /**
     * Nettoie et formate le numéro de carte (ajoute des espaces pour l'affichage)
     */
    public String formaterNumeroCarte(String numeroCarte) {
        if (numeroCarte == null) return "";
        
        String numeroNettoye = numeroCarte.trim().replaceAll("\\s+", "");
        if (numeroNettoye.length() != 16) return numeroCarte;
        
        // Formater comme XXXX XXXX XXXX XXXX
        return numeroNettoye.replaceAll("(.{4})", "$1 ").trim();
    }

    /**
     * Nettoie le numéro de carte (supprime les espaces)
     */
    public String nettoyerNumeroCarte(String numeroCarte) {
        if (numeroCarte == null) return "";
        return numeroCarte.trim().replaceAll("\\s+", "");
    }
}
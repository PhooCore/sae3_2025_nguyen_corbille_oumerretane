package controleur;

import modele.Paiement;
import modele.Stationnement;
import modele.Usager;
import modele.dao.PaiementDAO;
import modele.dao.StationnementDAO;
import modele.dao.UsagerDAO;
import ihm.Page_Paiement;
import ihm.Page_Principale;
import javax.swing.JOptionPane;
import java.time.LocalDateTime;
import java.time.YearMonth;

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
            // Créer le paiement
            Paiement paiement = new Paiement(nomCarte, numeroCarte, cvv, montant, usager.getIdUsager());
            boolean paiementReussi = PaiementDAO.enregistrerPaiement(paiement);
            
            if (paiementReussi) {
                // Créer l'objet Stationnement en utilisant le bon constructeur
                Stationnement stationnement = new Stationnement(
                    usager.getIdUsager(),
                    typeVehicule,
                    plaqueImmatriculation,
                    idZone,
                    "", // zone (laissé vide car tu as déjà idZone)
                    dureeHeures,
                    dureeMinutes,
                    montant,
                    paiement.getIdPaiement()
                );
                
                // Appeler la méthode avec l'objet Stationnement
                boolean stationnementReussi = StationnementDAO.creerStationnementVoirie(stationnement);
                
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
        Stationnement stationnement = StationnementDAO.getStationnementById(idStationnement);
        if (stationnement != null && "GRATUIT".equals(stationnement.getStatutPaiement())) {
            // Si gratuit, pas besoin de paiement, terminer directement
            boolean stationnementReussi = StationnementDAO.terminerStationnementParking(
                idStationnement,
                heureDepart,
                0.0, // montant 0
                null // pas d'id_paiement
            );
            
            if (stationnementReussi) {
                afficherConfirmationParking(0.0, idStationnement, pagePaiement);
                redirigerVersAccueil(pagePaiement);
                return true;
            }
            return false;
        }
        if (!validerDonneesPaiement(nomCarte, numeroCarte, dateExpiration, cvv, pagePaiement)) {
            return false;
        }
        
        try {
            // Créer le paiement
            Paiement paiement = new Paiement(nomCarte, numeroCarte, cvv, montant, usager.getIdUsager());
            boolean paiementReussi = PaiementDAO.enregistrerPaiement(paiement);
            
            if (paiementReussi) {
                // Utiliser la méthode qui prend idPaiement en String (correspond à ton DAO)
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

        if (nomCarte == null || nomCarte.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, 
                "Veuillez saisir le nom sur la carte", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String numeroCarteNettoye = numeroCarte.trim().replaceAll("\\s+", "");
        if (numeroCarteNettoye.isEmpty() || !numeroCarteNettoye.matches("\\d{16}")) {
            JOptionPane.showMessageDialog(parent, 
                "Numéro de carte invalide (16 chiffres requis)", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (dateExpiration == null || dateExpiration.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, 
                "Veuillez saisir la date d'expiration", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!validerFormatDateExpiration(dateExpiration.trim())) {
            JOptionPane.showMessageDialog(parent, 
                "Format de date invalide. Utilisez MM/AA (ex: 12/25)", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!estCarteNonExpiree(dateExpiration.trim())) {
            JOptionPane.showMessageDialog(parent, 
                "La carte est expirée", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
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
        if (!dateExpiration.matches("\\d{2}/\\d{2,4}")) {
            return false;
        }
        
        try {
            String[] parties = dateExpiration.split("/");
            int mois = Integer.parseInt(parties[0]);
            int annee = Integer.parseInt(parties[1]);
            
            if (mois < 1 || mois > 12) {
                return false;
            }

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
            if (annee < 100) {
                annee += 2000; 
            }

            YearMonth expiration = YearMonth.of(annee, mois);

            YearMonth maintenant = YearMonth.now();

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
        
        if (nomCarte == null || nomCarte.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, 
                "Veuillez saisir le nom sur la carte", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        String numeroCarteNettoye = numeroCarte.trim().replaceAll("\\s+", "");
        if (numeroCarteNettoye.isEmpty() || !numeroCarteNettoye.matches("\\d{16}")) {
            JOptionPane.showMessageDialog(parent, 
                "Numéro de carte invalide (16 chiffres requis)", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (dateExpiration == null || dateExpiration.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, 
                "Veuillez saisir la date d'expiration", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String cvvNettoye = cvv.trim();
        if (cvvNettoye.isEmpty() || !cvvNettoye.matches("\\d{3,4}")) {
            JOptionPane.showMessageDialog(parent, 
                "CVV invalide (3 ou 4 chiffres requis)", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return validerDonneesPaiement(nomCarte, numeroCarte, dateExpiration, cvv, parent);
    }

    /**
     * Nettoie et formate le numéro de carte (ajoute des espaces pour l'affichage)
     */
    public String formaterNumeroCarte(String numeroCarte) {
        if (numeroCarte == null) return "";
        
        String numeroNettoye = numeroCarte.trim().replaceAll("\\s+", "");
        if (numeroNettoye.length() != 16) return numeroCarte;
        
        return numeroNettoye.replaceAll("(.{4})", "$1 ").trim();
    }

    /**
     * Nettoie le numéro de carte (supprime les espaces)
     */
    public String nettoyerNumeroCarte(String numeroCarte) {
        if (numeroCarte == null) return "";
        return numeroCarte.trim().replaceAll("\\s+", "");
    }
    
    /**
     * Simule un paiement
     */
    public boolean simulerPaiement(double montant, String numeroCarte, String dateExpiration, String cvv) {
        try {
            // Simuler un délai de traitement
            Thread.sleep(1000);
            
            // Vérifications de base
            if (numeroCarte == null || numeroCarte.trim().isEmpty()) return false;
            if (!validerDateExpiration(dateExpiration)) return false;
            if (!validerCVV(cvv)) return false;
            
            // Pour la simulation, accepter toujours (ou presque)
            return Math.random() > 0.1; // 90% de chance de succès
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Valide une date d'expiration (version pour simulerPaiement)
     */
    private boolean validerDateExpiration(String dateExpiration) {
        if (dateExpiration == null || !dateExpiration.matches("\\d{2}/\\d{2}")) {
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
            
            // Vérifier que la carte n'est pas expirée
            int anneeCourante = java.time.Year.now().getValue() % 100;
            int moisCourant = java.time.LocalDate.now().getMonthValue();
            
            if (annee < anneeCourante) {
                return false;
            } else if (annee == anneeCourante && mois < moisCourant) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Valide un code CVV (version pour simulerPaiement)
     */
    private boolean validerCVV(String cvv) {
        return cvv != null && cvv.matches("\\d{3,4}");
    }
    
    /**
     * Traite un paiement pour un abonnement (méthode manquante)
     */
    public boolean traiterPaiementAbonnement(String nomCarte, String numeroCarte, String dateExpiration, 
                                            String cvv, double montant, String idAbonnement, 
                                            String libelleAbonnement, javax.swing.JFrame parent) {
        
        // Valider le formulaire
        if (!validerFormulairePaiementComplet(nomCarte, numeroCarte, dateExpiration, cvv, parent)) {
            return false;
        }
        
        // Simuler le paiement
        if (!simulerPaiement(montant, numeroCarte, dateExpiration, cvv)) {
            JOptionPane.showMessageDialog(parent,
                "Le paiement a été refusé par la banque.",
                "Paiement refusé",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            // Créer le paiement avec l'ID d'abonnement
            Paiement paiement = new Paiement(nomCarte, numeroCarte, cvv, montant, usager.getIdUsager(), idAbonnement);
            boolean paiementEnregistre = PaiementDAO.enregistrerPaiement(paiement);
            
            if (paiementEnregistre) {
                JOptionPane.showMessageDialog(parent,
                    "<html><h3>Paiement confirmé !</h3>" +
                    "<p>Votre abonnement " + libelleAbonnement + " est maintenant actif.</p>" +
                    "<p><b>ID Transaction:</b> " + paiement.getIdPaiement() + "</p>" +
                    "<p><b>Prix mensuel:</b> " + String.format("%.2f €", montant) + "</p></html>",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(parent,
                    "Erreur lors de l'enregistrement du paiement.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                "Erreur lors du traitement: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
}
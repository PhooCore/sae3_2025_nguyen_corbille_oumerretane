package controleur;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.time.LocalDateTime;

import modele.Paiement;
import modele.Stationnement;
import modele.Usager;
import modele.dao.PaiementDAO;
import modele.dao.StationnementDAO;
import modele.dao.UsagerDAO;
import modele.dao.AbonnementDAO;
import ihm.Page_Paiement;
import ihm.Page_Principale;
import javax.swing.JOptionPane;
import java.time.LocalDateTime;
import java.time.YearMonth;

public class PaiementControleur {
    private String emailUtilisateur;
    
    public PaiementControleur(String email) {
        this.emailUtilisateur = email;
    }
    
    /**
     * Valide le formulaire de paiement complet
     */
    public boolean validerFormulairePaiementComplet(String nomCarte, String numeroCarte, 
                                                   String dateExpiration, String cvv, 
                                                   JFrame parent) {
        // Validation du nom
        if (nomCarte == null || nomCarte.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                "Veuillez saisir le nom figurant sur la carte",
                "Nom manquant",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Validation du numéro de carte
        if (!validerNumeroCarte(numeroCarte)) {
            JOptionPane.showMessageDialog(parent,
                "Le numéro de carte est invalide. Il doit contenir 16 chiffres.",
                "Numéro de carte invalide",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Validation de la date d'expiration
        if (!validerDateExpiration(dateExpiration)) {
            JOptionPane.showMessageDialog(parent,
                "La date d'expiration est invalide. Format attendu: MM/AA",
                "Date d'expiration invalide",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Validation du CVV
        if (!validerCVV(cvv)) {
            JOptionPane.showMessageDialog(parent,
                "Le code CVV est invalide. Il doit contenir 3 ou 4 chiffres.",
                "CVV invalide",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Traite un paiement pour la voirie
     */
    public boolean traiterPaiementVoirie(String nomCarte, String numeroCarte, String dateExpiration, 
                                        String cvv, double montant, String typeVehicule, 
                                        String plaqueImmatriculation, String idZone, 
                                        int dureeHeures, int dureeMinutes, JFrame parent) {
        
        // 1. Valider le formulaire
        if (!validerFormulairePaiementComplet(nomCarte, numeroCarte, dateExpiration, cvv, parent)) {
            return false;
        }
        
        // 2. Confirmation avant paiement
        int confirmation = JOptionPane.showConfirmDialog(parent,
            "Confirmez-vous le paiement de " + String.format("%.2f €", montant) + 
            " pour votre stationnement en voirie ?",
            "Confirmation de paiement",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (confirmation != JOptionPane.YES_OPTION) {
            return false;
        }
        
        // 3. Simuler le paiement
        if (!simulerPaiement(montant, numeroCarte, dateExpiration, cvv)) {
            JOptionPane.showMessageDialog(parent,
                "Le paiement a été refusé par la banque.",
                "Paiement refusé",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // 4. Récupérer l'utilisateur
        Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager == null) {
            JOptionPane.showMessageDialog(parent,
                "Utilisateur non trouvé.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            // 5. Créer et enregistrer le paiement
            Paiement paiement = new Paiement(nomCarte, numeroCarte, cvv, montant, usager.getIdUsager());
            boolean paiementEnregistre = PaiementDAO.enregistrerPaiement(paiement);
            
            if (!paiementEnregistre) {
                JOptionPane.showMessageDialog(parent,
                    "Erreur lors de l'enregistrement du paiement.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // 6. Créer le stationnement
            Stationnement stationnement = new Stationnement();
            stationnement.setIdUsager(usager.getIdUsager());
            stationnement.setTypeVehicule(typeVehicule);
            stationnement.setPlaqueImmatriculation(plaqueImmatriculation);
            stationnement.setIdTarification(idZone);
            stationnement.setTypeStationnement("VOIRIE");
            stationnement.setDureeHeures(dureeHeures);
            stationnement.setDureeMinutes(dureeMinutes);
            stationnement.setCout(montant);
            stationnement.setStatut("ACTIF");
            stationnement.setDateCreation(LocalDateTime.now());
            stationnement.setIdPaiement(paiement.getIdPaiement());
            
            // 7. Enregistrer le stationnement
            boolean stationnementCree = StationnementDAO.creerStationnementVoirie(stationnement);
            
            if (stationnementCree) {
                JOptionPane.showMessageDialog(parent,
                    "<html><h3>Paiement confirmé !</h3>" +
                    "<p>Votre stationnement est maintenant actif.</p>" +
                    "<p><b>ID Transaction:</b> " + paiement.getIdPaiement() + "</p>" +
                    "<p><b>Véhicule:</b> " + plaqueImmatriculation + "</p>" +
                    "<p><b>Durée:</b> " + dureeHeures + "h" + dureeMinutes + "min</p>" +
                    "<p><b>Montant:</b> " + String.format("%.2f €", montant) + "</p></html>",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(parent,
                    "Le paiement a été enregistré mais une erreur est survenue\n" +
                    "lors de la création du stationnement.",
                    "Erreur",
                    JOptionPane.WARNING_MESSAGE);
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
    
    /**
     * Traite un paiement pour un parking
     */
    public boolean traiterPaiementParking(String nomCarte, String numeroCarte, String dateExpiration, 
                                         String cvv, double montant, Integer idStationnement, 
                                         LocalDateTime heureDepart, JFrame parent) {
        
        // 1. Valider le formulaire
        if (!validerFormulairePaiementComplet(nomCarte, numeroCarte, dateExpiration, cvv, parent)) {
            return false;
        }
        
        // 2. Confirmation avant paiement
        int confirmation = JOptionPane.showConfirmDialog(parent,
            "Confirmez-vous le paiement de " + String.format("%.2f €", montant) + 
            " pour votre stationnement en parking ?",
            "Confirmation de paiement",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (confirmation != JOptionPane.YES_OPTION) {
            return false;
        }
        
        // 3. Simuler le paiement
        if (!simulerPaiement(montant, numeroCarte, dateExpiration, cvv)) {
            JOptionPane.showMessageDialog(parent,
                "Le paiement a été refusé par la banque.",
                "Paiement refusé",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // 4. Récupérer l'utilisateur
        Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager == null) {
            JOptionPane.showMessageDialog(parent,
                "Utilisateur non trouvé.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            // 5. Créer et enregistrer le paiement
            Paiement paiement = new Paiement(nomCarte, numeroCarte, cvv, montant, usager.getIdUsager());
            boolean paiementEnregistre = PaiementDAO.enregistrerPaiement(paiement);
            
            if (!paiementEnregistre) {
                JOptionPane.showMessageDialog(parent,
                    "Erreur lors de l'enregistrement du paiement.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // 6. Mettre à jour le stationnement
            boolean stationnementTermine = StationnementDAO.terminerStationnementParking(
                idStationnement,
                heureDepart,
                montant,
                paiement.getIdPaiement()
            );
            
            if (stationnementTermine) {
                JOptionPane.showMessageDialog(parent,
                    "<html><h3>Paiement confirmé !</h3>" +
                    "<p>Votre stationnement a été terminé avec succès.</p>" +
                    "<p><b>ID Transaction:</b> " + paiement.getIdPaiement() + "</p>" +
                    "<p><b>Heure de départ:</b> " + 
                        heureDepart.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "</p>" +
                    "<p><b>Montant:</b> " + String.format("%.2f €", montant) + "</p></html>",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(parent,
                    "Le paiement a été enregistré mais une erreur est survenue\n" +
                    "lors de la mise à jour du stationnement.",
                    "Erreur",
                    JOptionPane.WARNING_MESSAGE);
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
    
    /**
     * Traite un paiement pour un abonnement
     */
    public boolean traiterPaiementAbonnement(String nomCarte, String numeroCarte, String dateExpiration, 
                                            String cvv, double montant, String idAbonnement, 
                                            String libelleAbonnement, JFrame parent) {
        
        // 1. Valider le formulaire
        if (!validerFormulairePaiementComplet(nomCarte, numeroCarte, dateExpiration, cvv, parent)) {
            return false;
        }
        
        // 2. Confirmation avant paiement
        int confirmation = JOptionPane.showConfirmDialog(parent,
            "Confirmez-vous le paiement de " + String.format("%.2f €", montant) + 
            " pour l'abonnement " + libelleAbonnement + " ?\n\n" +
            "Ce montant sera prélevé mensuellement jusqu'à résiliation.",
            "Confirmation de paiement",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (confirmation != JOptionPane.YES_OPTION) {
            return false;
        }
        
        // 3. Simuler le paiement
        if (!simulerPaiement(montant, numeroCarte, dateExpiration, cvv)) {
            JOptionPane.showMessageDialog(parent,
                "Le paiement a été refusé par la banque.",
                "Paiement refusé",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // 4. Récupérer l'utilisateur
        Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager == null) {
            JOptionPane.showMessageDialog(parent,
                "Utilisateur non trouvé.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            // 5. Supprimer d'abord les anciens abonnements (s'il y en a)
            boolean anciensSupprimes = AbonnementDAO.supprimerAbonnementsUtilisateur(usager.getIdUsager());
            
            if (!anciensSupprimes) {
                System.out.println("Aucun ancien abonnement à supprimer ou erreur lors de la suppression");
            }
            
            // 6. Ajouter le nouvel abonnement
            boolean abonnementAjoute = AbonnementDAO.ajouterAbonnementUtilisateur(usager.getIdUsager(), idAbonnement);
            
            if (!abonnementAjoute) {
                JOptionPane.showMessageDialog(parent,
                    "Une erreur est survenue lors de l'activation de l'abonnement.\n" +
                    "Veuillez réessayer ou contacter le support.",
                    "Erreur d'activation",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // 7. Créer et enregistrer le paiement
            Paiement paiement = new Paiement(nomCarte, numeroCarte, cvv, montant, usager.getIdUsager(), idAbonnement);
            boolean paiementEnregistre = PaiementDAO.enregistrerPaiement(paiement);
            
            if (!paiementEnregistre) {
                JOptionPane.showMessageDialog(parent,
                    "Le paiement a été traité mais l'enregistrement a échoué.\n" +
                    "Veuillez contacter le support technique.",
                    "Erreur d'enregistrement",
                    JOptionPane.WARNING_MESSAGE);
            }
            
            // 8. Afficher le message de succès
            JOptionPane.showMessageDialog(parent,
                "<html><h3>Paiement confirmé !</h3>" +
                "<p>Votre abonnement " + libelleAbonnement + " est maintenant actif.</p>" +
                "<p><b>ID Transaction:</b> " + paiement.getIdPaiement() + "</p>" +
                "<p><b>Prix mensuel:</b> " + String.format("%.2f €", montant) + "</p>" +
                "<p><b>Date de début:</b> " + 
                    java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</p>" +
                "<p>Un reçu a été envoyé à votre adresse email.</p></html>",
                "Succès",
                JOptionPane.INFORMATION_MESSAGE);
            return true;
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                "Erreur lors du traitement: " + e.getMessage() + 
                "\n\nVeuillez contacter le support technique.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Valide un numéro de carte
     */
    private boolean validerNumeroCarte(String numeroCarte) {
        if (numeroCarte == null) return false;
        
        // Supprimer les espaces et tirets
        String numeroNettoye = numeroCarte.replaceAll("[\\s-]+", "");
        
        // Vérifier que c'est un nombre de 16 chiffres
        if (!numeroNettoye.matches("\\d{16}")) {
            return false;
        }
        
        // Algorithme de Luhn pour vérifier la validité
        return validerAlgorithmeLuhn(numeroNettoye);
    }
    
    /**
     * Valide une date d'expiration
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
     * Valide un code CVV
     */
    private boolean validerCVV(String cvv) {
        return cvv != null && cvv.matches("\\d{3,4}");
    }
    
    /**
     * Implémente l'algorithme de Luhn
     */
    private boolean validerAlgorithmeLuhn(String numero) {
        int somme = 0;
        boolean alterner = false;
        
        for (int i = numero.length() - 1; i >= 0; i--) {
            int chiffre = Character.getNumericValue(numero.charAt(i));
            
            if (alterner) {
                chiffre *= 2;
                if (chiffre > 9) {
                    chiffre = (chiffre % 10) + 1;
                }
            }
            
            somme += chiffre;
            alterner = !alterner;
        }
        
        return (somme % 10 == 0);
    }
    
    /**
     * Simule un paiement
     */
    public boolean simulerPaiement(double montant, String numeroCarte, String dateExpiration, String cvv) {
        try {
            // Simuler un délai de traitement
            Thread.sleep(1000);
            
            // Vérifications supplémentaires
            if (!validerNumeroCarte(numeroCarte)) return false;
            if (!validerDateExpiration(dateExpiration)) return false;
            if (!validerCVV(cvv)) return false;
            
            // Pour la simulation, accepter certains numéros de test
            String numeroNettoye = numeroCarte.replaceAll("[\\s-]+", "");
            if (numeroNettoye.startsWith("4242") || numeroNettoye.startsWith("5555")) {
                return true; // Numéros de test acceptés
            }
            
            // Sinon, accepter aléatoirement (80% de chance)
            return Math.random() > 0.2;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
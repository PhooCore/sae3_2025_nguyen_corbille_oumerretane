package controleur;

import modele.Abonnement;
import modele.Stationnement;
import modele.Usager;
import modele.Zone;
import modele.dao.AbonnementDAO;
import modele.dao.PaiementDAO;
import modele.dao.ParkingDAO;
import modele.dao.StationnementDAO;
import modele.dao.UsagerDAO;
import modele.dao.ZoneDAO;
import modele.Parking;
import ihm.Page_Garer_Voirie;
import ihm.Page_Garer_Parking;
import ihm.Page_Paiement;
import ihm.Page_Principale;
import javax.swing.JOptionPane;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

public class StationnementControleur {

    private String emailUtilisateur;
    private Usager usager;

    public StationnementControleur(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
    }

    // =============================================
    // MÉTHODES POUR LA GESTION DES ABONNEMENTS
    // =============================================

    /**
     * Vérifie si l'usager a un abonnement actif.
     * @param idUsager ID de l'usager
     * @return true si l'usager a un abonnement actif
     */
    public boolean usagerAUnAbonnementActif(int idUsager) {
        try {
            // Utiliser le singleton d'AbonnementDAO
            List<Abonnement> abonnements = AbonnementDAO.getInstance().getAbonnementsByUsager(idUsager);
            for (Abonnement abonnement : abonnements) {
                if (abonnement.estActif()) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur vérification abonnement: " + e.getMessage());
        }
        return false;
    }

    /**
     * Récupère le tarif de l'abonnement actif de l'usager.
     * @param idUsager ID de l'usager
     * @return Tarif de l'abonnement actif, ou 0.0 si aucun
     */
    public double getTarifAbonnement(int idUsager) {
        try {
            List<Abonnement> abonnements = AbonnementDAO.getInstance().getAbonnementsByUsager(idUsager);
            for (Abonnement abonnement : abonnements) {
                if (abonnement.estActif()) {
                    return abonnement.getTarifAbonnement();
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur récupération tarif abonnement: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Récupère l'ID de l'abonnement actif de l'usager.
     * @param idUsager ID de l'usager
     * @return ID de l'abonnement actif, ou null si aucun
     */
    public String getIdAbonnementActif(int idUsager) {
        try {
            List<Abonnement> abonnements = AbonnementDAO.getInstance().getAbonnementsByUsager(idUsager);
            for (Abonnement abonnement : abonnements) {
                if (abonnement.estActif()) {
                    return abonnement.getIdAbonnement();
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur récupération ID abonnement: " + e.getMessage());
        }
        return null;
    }

    // =============================================
    // MÉTHODES EXISTANTES
    // =============================================

    public Stationnement getStationnementActif() {
        if (usager != null) {
            try {
                return StationnementDAO.getInstance().getStationnementActifValideByUsager(usager.getIdUsager());
            } catch (Exception e) {
                System.err.println("Erreur récupération stationnement actif: " + e.getMessage());
            }
        }
        return null;
    }

    public boolean preparerStationnementVoirie(String typeVehicule, String plaqueImmatriculation,
                                              String idZone, int dureeHeures, int dureeMinutes,
                                              Page_Garer_Voirie pageVoirie) {

        if (!validerStationnementVoirie(plaqueImmatriculation, typeVehicule, idZone,
                                       dureeHeures, dureeMinutes, pageVoirie)) {
            return false;
        }

        Zone zone = null;
        try {
            zone = ZoneDAO.getInstance().getZoneById(idZone);
        } catch (Exception e) {
            System.err.println("Erreur récupération zone: " + e.getMessage());
        }
        
        if (zone == null) {
            JOptionPane.showMessageDialog(pageVoirie,
                "Zone non trouvée",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        int dureeTotaleMinutes = (dureeHeures * 60) + dureeMinutes;
        double cout = zone.calculerCout(dureeTotaleMinutes);

        // Vérifier si l'usager a un abonnement actif et appliquer le tarif préférentiel
        if (usagerAUnAbonnementActif(usager.getIdUsager())) {
            double tarifAbonnement = getTarifAbonnement(usager.getIdUsager());
            if (tarifAbonnement > 0) {
                cout = tarifAbonnement;
            } else if (tarifAbonnement == 0.0) {
                cout = 0.0;
            }
        }

        if (dureeTotaleMinutes > zone.getDureeMaxMinutes()) {
            JOptionPane.showMessageDialog(pageVoirie,
                "Durée maximale dépassée pour " + zone.getLibelleZone() +
                " (max: " + formatDuree(zone.getDureeMaxMinutes()) + ")",
                "Erreur",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        Page_Paiement pagePaiement = new Page_Paiement(
            cout,
            emailUtilisateur,
            typeVehicule,
            plaqueImmatriculation,
            idZone,
            zone.getLibelleZone(),
            dureeHeures,
            dureeMinutes
        );
        pagePaiement.setVisible(true);
        pageVoirie.dispose();

        return true;
    }

    public boolean preparerStationnementParking(String typeVehicule, String plaqueImmatriculation,
                                               String idParking, Page_Garer_Parking pageParking) {

        if (!validerStationnementParking(plaqueImmatriculation, typeVehicule, idParking, pageParking)) {
            return false;
        }

        Parking parking = null;
        try {
            parking = ParkingDAO.getInstance().getParkingById(idParking);
        } catch (Exception e) {
            System.err.println("Erreur récupération parking: " + e.getMessage());
        }
        
        if (parking == null) {
            JOptionPane.showMessageDialog(pageParking,
                "Parking non trouvé",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (parking.getPlacesDisponibles() <= 0) {
            JOptionPane.showMessageDialog(pageParking,
                "Aucune place disponible dans ce parking",
                "Parking complet",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Créer l'objet Stationnement
        Stationnement stationnement = new Stationnement();
        stationnement.setIdUsager(usager.getIdUsager());
        stationnement.setTypeVehicule(typeVehicule);
        stationnement.setPlaqueImmatriculation(plaqueImmatriculation);
        stationnement.setIdTarification(idParking); // Utiliser idTarification pour stocker l'ID parking
        stationnement.setTypeStationnement("PARKING");
        stationnement.setStatut("ACTIF");
        stationnement.setStatutPaiement("NON_PAYE");
        stationnement.setCout(0.0);
        stationnement.setHeureArrivee(LocalDateTime.now());

        boolean succes = false;
        try {
            succes = StationnementDAO.getInstance().creerStationnementParking(stationnement);
        } catch (Exception e) {
            System.err.println("Erreur création stationnement parking: " + e.getMessage());
            JOptionPane.showMessageDialog(pageParking,
                "Erreur technique lors de la réservation: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (succes) {
            JOptionPane.showMessageDialog(pageParking,
                "Réservation confirmée !\n\n" +
                "Votre place est réservée dans le parking " + parking.getLibelleParking() + ".\n" +
                "N'oubliez pas de valider votre sortie pour le paiement.",
                "Réservation réussie",
                JOptionPane.INFORMATION_MESSAGE);

            Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
            pagePrincipale.setVisible(true);
            pageParking.dispose();
            return true;
        } else {
            JOptionPane.showMessageDialog(pageParking,
                "Erreur lors de la réservation. Veuillez réessayer.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean terminerStationnementVoirie(int idStationnement) {
        try {
            return StationnementDAO.getInstance().terminerStationnement(idStationnement);
        } catch (Exception e) {
            System.err.println("Erreur terminaison stationnement voirie: " + e.getMessage());
            return false;
        }
    }

    public boolean terminerStationnementParking(int idStationnement, LocalDateTime heureDepart,
                                               double cout, String idPaiement) {
        try {
            return StationnementDAO.getInstance().terminerStationnementParking(
                idStationnement, heureDepart, cout, idPaiement);
        } catch (Exception e) {
            System.err.println("Erreur terminaison stationnement parking: " + e.getMessage());
            return false;
        }
    }

    public boolean validerPlaque(String plaque) {
        if (plaque == null || plaque.trim().isEmpty() || plaque.equals("Non définie")) {
            return false;
        }
        return plaque.matches("[A-Z]{2}-\\d{3}-[A-Z]{2}");
    }

    private boolean validerStationnementVoirie(String plaqueImmatriculation, String typeVehicule,
                                              String idZone, int dureeHeures, int dureeMinutes,
                                              Page_Garer_Voirie pageVoirie) {

        if (!validerPlaque(plaqueImmatriculation)) {
            JOptionPane.showMessageDialog(pageVoirie,
                "Veuillez définir une plaque d'immatriculation valide (format: AA-123-AA)",
                "Plaque manquante",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        Stationnement stationnementActif = getStationnementActif();
        if (stationnementActif != null) {
            afficherMessageStationnementActif(stationnementActif, pageVoirie);
            return false;
        }

        return true;
    }

    private boolean validerStationnementParking(String plaqueImmatriculation, String typeVehicule,
                                               String idParking, Page_Garer_Parking pageParking) {

        if (!validerPlaque(plaqueImmatriculation)) {
            JOptionPane.showMessageDialog(pageParking,
                "Veuillez définir une plaque d'immatriculation valide (format: AA-123-AA)",
                "Plaque manquante",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        Stationnement stationnementActif = getStationnementActif();
        if (stationnementActif != null) {
            afficherMessageStationnementActif(stationnementActif, pageParking);
            return false;
        }

        return true;
    }

    private void afficherMessageStationnementActif(Stationnement stationnement, javax.swing.JFrame parent) {
        String message = "Vous avez déjà un stationnement " + stationnement.getTypeStationnement() + " actif !\n\n" +
                        "Véhicule: " + stationnement.getTypeVehicule() + " - " + stationnement.getPlaqueImmatriculation() + "\n";

        if (stationnement.estVoirie()) {
            message += "Zone: " + stationnement.getZone() + "\n" +
                      "Début: " + stationnement.getDateCreation().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } else if (stationnement.estParking()) {
            message += "Parking: " + stationnement.getZone() + "\n" +
                      "Arrivée: " + stationnement.getHeureArrivee().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }

        message += "\n\nVeuillez terminer ce stationnement avant d'en créer un nouveau.";

        JOptionPane.showMessageDialog(parent, message, "Stationnement actif", JOptionPane.WARNING_MESSAGE);
    }

    private String formatDuree(int minutes) {
        int heures = minutes / 60;
        int mins = minutes % 60;
        if (mins == 0) {
            return heures + "h";
        } else {
            return heures + "h" + mins + "min";
        }
    }

    public List<Stationnement> getHistoriqueStationnements() {
        if (usager != null) {
            try {
                return StationnementDAO.getInstance().getHistoriqueStationnements(usager.getIdUsager());
            } catch (Exception e) {
                System.err.println("Erreur récupération historique: " + e.getMessage());
            }
        }
        return Collections.emptyList();
    }

    public boolean creerStationnementVoirieGratuit(String typeVehicule, String plaque,
            String idZone, int heures, int minutes) {
        try {
            if (this.usager == null) {
                return false;
            }

            Zone zone = null;
            try {
                zone = ZoneDAO.getInstance().getZoneById(idZone);
            } catch (Exception e) {
                System.err.println("Erreur récupération zone: " + e.getMessage());
            }
            
            if (zone == null) {
                return false;
            }

            String idPaiement = "PAY_GRATUIT_" + System.currentTimeMillis();

            boolean paiementCree = creerPaiementGratuit(this.usager.getIdUsager(), idPaiement);
            if (!paiementCree) {
                return false;
            }

            Stationnement stationnement = new Stationnement();
            stationnement.setIdUsager(this.usager.getIdUsager());
            stationnement.setTypeVehicule(typeVehicule);
            stationnement.setPlaqueImmatriculation(plaque);
            stationnement.setIdTarification(idZone);
            stationnement.setZone(zone.getLibelleZone());
            stationnement.setDureeHeures(heures);
            stationnement.setDureeMinutes(minutes);
            stationnement.setCout(0.00);
            stationnement.setStatut("ACTIF");
            stationnement.setTypeStationnement("VOIRIE");
            stationnement.setStatutPaiement("PAYE");
            stationnement.setIdPaiement(idPaiement);
            
            boolean succes = false;
            try {
                // Utiliser la méthode create du DAO
                StationnementDAO.getInstance().create(stationnement);
                succes = true;
            } catch (Exception e) {
                System.err.println("Erreur création stationnement voirie gratuit: " + e.getMessage());
                succes = false;
            }

            return succes;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean creerPaiementGratuit(int idUsager, String idPaiement) {
        try {
            modele.Paiement paiement = new modele.Paiement();
            paiement.setIdPaiement(idPaiement);
            paiement.setNomCarte("GRATUIT");
            paiement.setNumeroCarte("0000000000000000");
            paiement.setCodeSecretCarte("000");
            paiement.setMontant(0.00);
            paiement.setIdUsager(idUsager);
            paiement.setDatePaiement(LocalDateTime.now());
            paiement.setMethodePaiement("GRATUIT");
            paiement.setStatut("REUSSI");
            paiement.setIdAbonnement(null);

            boolean paiementCree = false;
            try {
                PaiementDAO.getInstance().enregistrerPaiement(paiement);
                paiementCree = true;
            } catch (Exception e) {
                System.err.println("Erreur enregistrement paiement: " + e.getMessage());
                paiementCree = false;
            }
            
            if (paiementCree) {
                return true;
            } else {
                System.err.println(" Échec de l'enregistrement du paiement gratuit");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du paiement gratuit: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
}
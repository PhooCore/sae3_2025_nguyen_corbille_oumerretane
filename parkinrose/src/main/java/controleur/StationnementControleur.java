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

public class StationnementControleur {

    private String emailUtilisateur;
    private Usager usager;

    public StationnementControleur(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
    }

    // =============================================
    // NOUVELLES MÉTHODES POUR LA GESTION DES ABONNEMENTS
    // =============================================

    /**
     * Vérifie si l'usager a un abonnement actif.
     * @param idUsager ID de l'usager
     * @return true si l'usager a un abonnement actif
     */
    public boolean usagerAUnAbonnementActif(int idUsager) {
        return AbonnementDAO.utilisateurAUnAbonnement(idUsager);
    }

    /**
     * Récupère l'ID de l'abonnement actif de l'usager.
     * @param idUsager ID de l'usager
     * @return ID de l'abonnement actif, ou null si aucun
     */
    public String getIdAbonnementActif(int idUsager) {
        List<Abonnement> abonnements = AbonnementDAO.getAbonnementsByUsager(idUsager);
        if (abonnements.isEmpty()) {
            return null;
        }
        return abonnements.get(0).getIdAbonnement();
    }

    /**
     * Récupère le tarif de l'abonnement actif de l'usager.
     * @param idUsager ID de l'usager
     * @return Tarif de l'abonnement actif, ou 0.0 si aucun
     */
    public double getTarifAbonnement(int idUsager) {
        List<Abonnement> abonnements = AbonnementDAO.getAbonnementsByUsager(idUsager);
        if (abonnements.isEmpty()) {
            return 0.0;
        }
        return abonnements.get(0).getTarifAbonnement();
    }

    // =============================================
    // MÉTHODES EXISTANTES (sans modification)
    // =============================================

    public Stationnement getStationnementActif() {
        if (usager != null) {
            return StationnementDAO.getStationnementActifValideByUsager(usager.getIdUsager());
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

        Zone zone = ZoneDAO.getZoneById(idZone);
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
                cout = tarifAbonnement; // Appliquer le tarif de l'abonnement
            } else if (tarifAbonnement == 0.0) {
                cout = 0.0; // Gratuit
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

        Parking parking = ParkingDAO.getParkingById(idParking);
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

        boolean succes = StationnementDAO.creerStationnementParking(
            usager.getIdUsager(),
            typeVehicule,
            plaqueImmatriculation,
            idParking,
            LocalDateTime.now(),
            Abonnement.class.getName());

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

    public boolean preparerStationnementParking(String typeVehicule, String plaqueImmatriculation,
                                               String idParking, Page_Garer_Parking pageParking, String idAbonnement) {

        if (!validerStationnementParking(plaqueImmatriculation, typeVehicule, idParking, pageParking)) {
            return false;
        }

        Parking parking = ParkingDAO.getParkingById(idParking);
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

        boolean succes = StationnementDAO.creerStationnementParking(
            usager.getIdUsager(),
            typeVehicule,
            plaqueImmatriculation,
            idParking,
            LocalDateTime.now(),
            idAbonnement // Passer l'ID de l'abonnement
        );

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
        return StationnementDAO.terminerStationnement(idStationnement);
    }

    public boolean terminerStationnementParking(int idStationnement, LocalDateTime heureDepart,
                                               double cout, String idPaiement) {
        return StationnementDAO.terminerStationnementParking(
            idStationnement, heureDepart, cout, idPaiement);
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
            return StationnementDAO.getHistoriqueStationnements(usager.getIdUsager());
        }
        return java.util.Collections.emptyList();
    }

    public boolean creerStationnementVoirieGratuit(String typeVehicule, String plaque,
            String idZone, int heures, int minutes) {
        System.out.println("=== CRÉATION STATIONNEMENT VOIRIE GRATUIT ===");
        System.out.println("Type véhicule: " + typeVehicule);
        System.out.println("Plaque: " + plaque);
        System.out.println("Zone: " + idZone);
        System.out.println("Durée: " + heures + "h" + minutes + "min");

        try {
            if (this.usager == null) {
                System.err.println("❌ Utilisateur non trouvé");
                return false;
            }

            Zone zone = ZoneDAO.getZoneById(idZone);
            if (zone == null) {
                System.err.println("❌ Zone non trouvée: " + idZone);
                return false;
            }

            String idPaiement = "PAY_GRATUIT_" + System.currentTimeMillis();
            System.out.println("ID Paiement généré: " + idPaiement);

            boolean paiementCree = creerPaiementGratuit(this.usager.getIdUsager(), idPaiement);
            if (!paiementCree) {
                System.err.println("❌ Échec de la création du paiement gratuit");
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

            System.out.println("Création du stationnement pour l'usager: " + this.usager.getIdUsager());
            System.out.println("Zone: " + zone.getLibelleZone());

            boolean succes = StationnementDAO.creerStationnementVoirie(stationnement);

            if (succes) {
                System.out.println("✅ Stationnement gratuit créé avec succès !");
                return true;
            } else {
                System.err.println("❌ Échec de la création du stationnement");
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du stationnement gratuit: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean creerPaiementGratuit(int idUsager, String idPaiement) {
        try {
            System.out.println("Création du paiement gratuit: " + idPaiement);

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

            boolean paiementCree = PaiementDAO.enregistrerPaiement(paiement);
            if (paiementCree) {
                System.out.println("✅ Paiement gratuit enregistré avec succès");
                return true;
            } else {
                System.err.println("❌ Échec de l'enregistrement du paiement gratuit");
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du paiement gratuit: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

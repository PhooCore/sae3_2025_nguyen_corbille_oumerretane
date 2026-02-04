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

/**
 * Contrôleur métier gérant la logique de gestion des stationnements.
 * Ce contrôleur ne gère pas directement d'interface graphique mais fournit
 * les méthodes métier utilisées par les autres contrôleurs pour gérer :
 * - La vérification et la validation des stationnements
 * - La création de stationnements en voirie et en parking
 * - La gestion des abonnements et tarifs préférentiels
 * - La terminaison des stationnements
 * - L'historique des stationnements
 * 
 * @author Équipe 7
 */
public class StationnementControleur {

    private String emailUtilisateur;
    private Usager usager;

    /**
     * Constructeur du contrôleur de stationnement.
     * Charge l'utilisateur depuis la base de données à partir de son email.
     * 
     * @param email l'email de l'utilisateur
     */
    public StationnementControleur(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
    }

    /**
     * Vérifie si l'usager possède un abonnement actif.
     * Parcourt tous les abonnements de l'utilisateur et vérifie leur statut.
     * 
     * @param idUsager l'ID de l'usager
     * @return true si l'usager a au moins un abonnement actif, false sinon
     */
    public boolean usagerAUnAbonnementActif(int idUsager) {
        try {
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
     * Retourne le tarif du premier abonnement actif trouvé.
     * 
     * @param idUsager l'ID de l'usager
     * @return le tarif de l'abonnement actif, ou 0.0 si aucun abonnement actif
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
     * Récupère l'identifiant de l'abonnement actif de l'usager.
     * 
     * @param idUsager l'ID de l'usager
     * @return l'ID de l'abonnement actif, ou null si aucun abonnement actif
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

    /**
     * Récupère le stationnement actuellement actif de l'utilisateur.
     * 
     * @return le stationnement actif ou null si aucun stationnement actif
     */
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

    /**
     * Prépare un stationnement en voirie en validant les données et en calculant le coût.
     * Si valide, ouvre la page de paiement avec les informations du stationnement.
     * Applique automatiquement les tarifs préférentiels si l'utilisateur a un abonnement actif.
     * 
     * @param typeVehicule le type de véhicule (Voiture, Moto, etc.)
     * @param plaqueImmatriculation la plaque d'immatriculation du véhicule
     * @param idZone l'identifiant de la zone de stationnement
     * @param dureeHeures la durée en heures du stationnement
     * @param dureeMinutes la durée en minutes du stationnement
     * @param pageVoirie la page de voirie appelante
     * @return true si la préparation a réussi, false sinon
     */
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

    /**
     * Prépare un stationnement en parking en validant les données et en vérifiant la disponibilité.
     * Crée directement le stationnement en base de données avec le statut approprié.
     * Gère les abonnements moto qui bénéficient de la gratuité.
     * 
     * @param typeVehicule le type de véhicule
     * @param plaqueImmatriculation la plaque d'immatriculation
     * @param idParking l'identifiant du parking
     * @param pageParking la page de parking appelante
     * @return true si la préparation a réussi, false sinon
     */
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

    	boolean estMoto = "Moto".equalsIgnoreCase(typeVehicule);
    	boolean aAbonnementMoto = false;

    	if (estMoto) {
    		try {
    			Abonnement abonnement = AbonnementDAO.getInstance().getAbonnementActif(usager.getIdUsager());
    			if (abonnement != null && "ABO_MOTO_RESIDENT".equals(abonnement.getIdAbonnement())) {
    				aAbonnementMoto = true;
    			}
    		} catch (Exception e) {
    			System.err.println("Erreur vérification abonnement moto: " + e.getMessage());
    		}
    	}

    	Stationnement stationnement = new Stationnement();
    	stationnement.setIdUsager(usager.getIdUsager());
    	stationnement.setTypeVehicule(typeVehicule);
    	stationnement.setPlaqueImmatriculation(plaqueImmatriculation);
    	stationnement.setIdTarification(idParking);
    	stationnement.setTypeStationnement("PARKING");
    	stationnement.setStatut("ACTIF");
    	stationnement.setCout(0.0);
    	stationnement.setHeureArrivee(LocalDateTime.now());

    	if (aAbonnementMoto) {
    		stationnement.setStatutPaiement("GRATUIT");
    	} else {
    		stationnement.setStatutPaiement("NON_PAYE");
    	}

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
    		return true;
    	} else {
    		JOptionPane.showMessageDialog(pageParking,
    				"Erreur lors de la réservation. Veuillez réessayer.",
    				"Erreur",
    				JOptionPane.ERROR_MESSAGE);
    		return false;
    	}
    }
    
    /**
     * Termine un stationnement en voirie.
     * Met à jour le statut du stationnement en base de données.
     * 
     * @param idStationnement l'identifiant du stationnement à terminer
     * @return true si la terminaison a réussi, false sinon
     */
    public boolean terminerStationnementVoirie(int idStationnement) {
        try {
            return StationnementDAO.getInstance().terminerStationnement(idStationnement);
        } catch (Exception e) {
            System.err.println("Erreur terminaison stationnement voirie: " + e.getMessage());
            return false;
        }
    }

    /**
     * Termine un stationnement en parking.
     * Enregistre l'heure de départ, le coût total et l'identifiant du paiement.
     * 
     * @param idStationnement l'identifiant du stationnement à terminer
     * @param heureDepart l'heure de départ du parking
     * @param cout le coût total du stationnement
     * @param idPaiement l'identifiant du paiement associé
     * @return true si la terminaison a réussi, false sinon
     */
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

    /**
     * Valide le format d'une plaque d'immatriculation.
     * Le format attendu est : AA-123-AA (2 lettres, 3 chiffres, 2 lettres).
     * 
     * @param plaque la plaque d'immatriculation à valider
     * @return true si la plaque est valide, false sinon
     */
    public boolean validerPlaque(String plaque) {
        if (plaque == null || plaque.trim().isEmpty() || plaque.equals("Non définie")) {
            return false;
        }
        return plaque.matches("[A-Z]{2}-\\d{3}-[A-Z]{2}");
    }

    /**
     * Valide les données d'un stationnement en voirie avant sa création.
     * Vérifie la plaque d'immatriculation et l'absence de stationnement actif.
     * 
     * @param plaqueImmatriculation la plaque d'immatriculation
     * @param typeVehicule le type de véhicule
     * @param idZone l'identifiant de la zone
     * @param dureeHeures la durée en heures
     * @param dureeMinutes la durée en minutes
     * @param pageVoirie la page de voirie appelante
     * @return true si les données sont valides, false sinon
     */
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

    /**
     * Valide les données d'un stationnement en parking avant sa création.
     * Vérifie la plaque d'immatriculation et l'absence de stationnement actif.
     * 
     * @param plaqueImmatriculation la plaque d'immatriculation
     * @param typeVehicule le type de véhicule
     * @param idParking l'identifiant du parking
     * @param pageParking la page de parking appelante
     * @return true si les données sont valides, false sinon
     */
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

    /**
     * Affiche un message d'avertissement indiquant qu'un stationnement est déjà actif.
     * Le message contient les détails du stationnement actif.
     * 
     * @param stationnement le stationnement actif
     * @param parent la fenêtre parente pour le message
     */
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

    /**
     * Formate une durée en minutes en format lisible (Xh ou XhYmin).
     * 
     * @param minutes la durée en minutes
     * @return la durée formatée
     */
    private String formatDuree(int minutes) {
        int heures = minutes / 60;
        int mins = minutes % 60;
        if (mins == 0) {
            return heures + "h";
        } else {
            return heures + "h" + mins + "min";
        }
    }

    /**
     * Récupère l'historique des stationnements de l'utilisateur.
     * Retourne tous les stationnements passés et présents.
     * 
     * @return la liste des stationnements de l'utilisateur, ou une liste vide en cas d'erreur
     */
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

    /**
     * Crée un stationnement en voirie gratuit (abonnement gratuit ou tarif zéro).
     * Crée d'abord un paiement gratuit puis le stationnement associé.
     * 
     * @param typeVehicule le type de véhicule
     * @param plaque la plaque d'immatriculation
     * @param idZone l'identifiant de la zone
     * @param heures la durée en heures
     * @param minutes la durée en minutes
     * @return true si la création a réussi, false sinon
     */
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

    /**
     * Crée un paiement gratuit dans la base de données.
     * Utilisé pour les stationnements avec abonnement gratuit ou tarif zéro.
     * 
     * @param idUsager l'identifiant de l'usager
     * @param idPaiement l'identifiant du paiement à créer
     * @return true si la création a réussi, false sinon
     */
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
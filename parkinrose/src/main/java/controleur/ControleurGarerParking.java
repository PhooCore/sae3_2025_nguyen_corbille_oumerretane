package controleur;

import ihm.Page_Garer_Parking;
import ihm.Page_Principale;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

import modele.Abonnement;
import modele.Parking;
import modele.Usager;
import modele.VehiculeUsager;
import modele.dao.AbonnementDAO;
import modele.dao.ParkingDAO;
import modele.dao.TarifParkingDAO;
import modele.dao.UsagerDAO;
import modele.dao.VehiculeUsagerDAO;

/**
 * Contrôleur gérant la réservation de places de parking.
 * Permet aux utilisateurs de sélectionner un parking, choisir ou saisir un véhicule,
 * consulter les tarifs et effectuer une réservation. Propose des parkings alternatifs
 * en cas d'indisponibilité et gère les spécificités selon le type de véhicule (voiture, moto, camion).
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Garer_Parking
 * et les modèles (Parking, Usager, VehiculeUsager, Abonnement).
 * 
 * @author Équipe 7
 */
public class ControleurGarerParking implements ActionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur de réservation.
     * Permet de suivre le cycle de vie de la réservation et les opérations en cours.
     */
    private enum Etat {
        /** État initial au démarrage du contrôleur */
        INITIAL,
        /** L'utilisateur est en train de sélectionner un parking */
        SELECTION_PARKING,
        /** Vérification de la disponibilité du parking pour le type de véhicule */
        VERIFICATION_DISPONIBILITE,
        /** Modification du véhicule ou de la plaque en cours */
        MODIFICATION_VEHICULE,
        /** Confirmation de la réservation en cours */
        CONFIRMATION_RESERVATION,
        /** Redirection vers une autre page en cours */
        REDIRECTION,
        /** Une erreur s'est produite */
        ERREUR
    }
    
    private Page_Garer_Parking vue;
    private Etat etat;
    private StationnementControleur stationnementControleur;
    private String emailUtilisateur;
    private Usager usager;
    private List<Parking> listeParkings;
    private Parking parkingSelectionne;
    private TarifParkingDAO tarifDAO;
    private List<Parking> parkingsProposes;
    
    /**
     * Constructeur du contrôleur de réservation de parking.
     * Initialise le contrôleur avec la vue associée et un parking éventuellement présélectionné.
     * 
     * @param vue la page d'interface graphique de réservation
     * @param parkingPreSelectionne le parking à présélectionner (peut être null)
     */
    public ControleurGarerParking(Page_Garer_Parking vue, Parking parkingPreSelectionne) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.tarifDAO = TarifParkingDAO.getInstance();
        this.etat = Etat.INITIAL;
        
        initialiserControleur(parkingPreSelectionne);
    }
    
    /**
     * Initialise le contrôleur en chargeant l'utilisateur, le contrôleur de stationnement,
     * la liste des parkings et en configurant la vue.
     * En cas d'erreur, gère l'erreur d'initialisation.
     * 
     * @param parkingPreSelectionne le parking à présélectionner dans la liste
     */
    private void initialiserControleur(Parking parkingPreSelectionne) {
        try {
            chargerUtilisateur();
            initialiserStationnementControleur();
            chargerParkings();
            initialiserVue(parkingPreSelectionne);
            etat = Etat.SELECTION_PARKING;
            
        } catch (Exception e) {
            gererErreurInitialisation(e.getMessage());
        }
    }
    
    /**
     * Charge les informations de l'utilisateur depuis la base de données.
     * 
     * @throws Exception si l'utilisateur n'est pas trouvé
     */
    private void chargerUtilisateur() throws Exception {
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager == null) {
            throw new Exception("Utilisateur non trouvé");
        }
    }
    
    /**
     * Initialise le contrôleur de stationnement qui gère les opérations
     * de création et de gestion des stationnements.
     */
    private void initialiserStationnementControleur() {
        this.stationnementControleur = new StationnementControleur(emailUtilisateur);
    }
    
    /**
     * Charge la liste complète des parkings disponibles depuis la base de données.
     * En cas d'erreur, initialise une liste vide.
     */
    private void chargerParkings() {
        try {
            this.listeParkings = ParkingDAO.getInstance().getAllParkings();
            if (listeParkings.isEmpty()) {
                throw new Exception("Aucun parking disponible");
            }
        } catch (Exception e) {
            listeParkings = new ArrayList<>();
            gererErreur("Erreur chargement parkings", e.getMessage());
        }
    }
    
    /**
     * Initialise la vue en configurant les écouteurs, remplissant les informations utilisateur,
     * chargeant la liste des parkings dans le ComboBox et affichant le véhicule principal.
     * 
     * @param parkingPreSelectionne le parking à présélectionner
     */
    private void initialiserVue(Parking parkingPreSelectionne) {
        configurerListeners();
        vue.setNomUsager(usager.getNomUsager());
        vue.setPrenomUsager(usager.getPrenomUsager());
        vue.setEmailUsager(usager.getMailUsager());
        
        chargerComboParkings(parkingPreSelectionne);
        chargerVehiculePrincipal();
    }
    
    /**
     * Configure tous les écouteurs d'événements pour les composants interactifs de la vue.
     * Connecte les boutons, le ComboBox de parkings et les boutons radio de type de véhicule
     * aux actions appropriées.
     */
    private void configurerListeners() {
        vue.getBtnAnnuler().addActionListener(this);
        vue.getBtnReserver().addActionListener(this);
        vue.getBtnModifierPlaque().addActionListener(this);
        
        vue.getComboParking().addItemListener(e -> {
            if ((etat == Etat.SELECTION_PARKING || etat == Etat.VERIFICATION_DISPONIBILITE) 
                && e.getStateChange() == ItemEvent.SELECTED) {
                int index = vue.getComboParking().getSelectedIndex();
                if (index >= 0 && index < listeParkings.size()) {
                    parkingSelectionne = listeParkings.get(index);
                    etat = Etat.VERIFICATION_DISPONIBILITE;
                    mettreAJourInfosParking(index);
                }
            }
        });
        
        ActionListener radioListener = e -> {
            if (etat == Etat.SELECTION_PARKING || etat == Etat.VERIFICATION_DISPONIBILITE) {
                int index = vue.getComboParking().getSelectedIndex();
                if (index >= 0 && index < listeParkings.size()) {
                    mettreAJourInfosParking(index);
                }
            }
        };
        
        vue.getRadioVoiture().addActionListener(radioListener);
        vue.getRadioMoto().addActionListener(radioListener);
        vue.getRadioCamion().addActionListener(radioListener);
    }
    
    /**
     * Charge la liste des parkings dans le ComboBox avec leurs informations principales
     * et des indicateurs (M=moto, TS=tarif soirée, G=gratuit, R=relais).
     * Présélectionne le parking indiqué s'il existe.
     * 
     * @param parkingPreSelectionne le parking à présélectionner dans la liste
     */
    private void chargerComboParkings(Parking parkingPreSelectionne) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        int indexSelectionne = -1;
        
        for (int i = 0; i < listeParkings.size(); i++) {
            Parking parking = listeParkings.get(i);
            String texte = parking.getLibelleParking() + " - " + parking.getAdresseParking();
            
            StringBuilder indicateurs = new StringBuilder();
            try {
                if (parking.hasMoto() && parking.getPlacesMotoDisponibles() > 0) {
                    indicateurs.append(" M");
                }
                
                if (tarifDAO.proposeTarifSoiree(parking.getIdParking())) {
                    indicateurs.append(" TS");
                }
                
                if (tarifDAO.estParkingGratuit(parking.getIdParking())) {
                    indicateurs.append(" G");
                } else if (parking.isEstRelais()) {
                    indicateurs.append(" R");
                }
            } catch (Exception e) {
                // Ignorer les erreurs d'indicateurs
            }
            
            if (indicateurs.length() > 0) {
                texte += indicateurs.toString();
            }
            
            model.addElement(texte);
            
            if (parkingPreSelectionne != null && 
                parkingPreSelectionne.getIdParking().equals(parking.getIdParking())) {
                indexSelectionne = i;
                parkingSelectionne = parking;
            }
        }
        
        vue.getComboParking().setModel(model);
        
        if (indexSelectionne != -1) {
            vue.getComboParking().setSelectedIndex(indexSelectionne);
            mettreAJourInfosParking(indexSelectionne);
        } else if (!listeParkings.isEmpty()) {
            vue.getComboParking().setSelectedIndex(0);
            parkingSelectionne = listeParkings.get(0);
            mettreAJourInfosParking(0);
        }
    }
    
    /**
     * Charge et affiche le véhicule principal de l'utilisateur.
     * Si aucun véhicule principal n'existe, affiche le premier véhicule disponible.
     * Si aucun véhicule n'est enregistré, affiche un message approprié.
     */
    private void chargerVehiculePrincipal() {
        try {
            VehiculeUsager vehiculePrincipal = VehiculeUsagerDAO.getVehiculePrincipalStatic(usager.getIdUsager());
            if (vehiculePrincipal != null) {
                afficherVehicule(vehiculePrincipal);
            } else {
                List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsagerStatic(usager.getIdUsager());
                if (!vehicules.isEmpty()) {
                    afficherVehicule(vehicules.get(0));
                } else {
                    vue.setPlaque("AUCUN VÉHICULE");
                    vue.afficherMessageInformation("Aucun véhicule", 
                        "Vous n'avez pas encore de véhicule enregistré.");
                }
            }
        } catch (Exception e) {
            vue.setPlaque("ERREUR CHARGEMENT");
            gererErreur("Erreur chargement véhicules", e.getMessage());
        }
    }
    
    /**
     * Affiche les informations d'un véhicule dans la vue.
     * 
     * @param vehicule le véhicule à afficher
     */
    private void afficherVehicule(VehiculeUsager vehicule) {
        vue.setPlaque(vehicule.getPlaqueImmatriculation());
        vue.setTypeVehicule(vehicule.getTypeVehicule());
    }
    
    /**
     * Gère les événements d'action des composants de la vue.
     * Route les actions vers les méthodes appropriées en fonction de l'état actuel
     * et de la source de l'événement.
     * 
     * @param e l'événement d'action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        switch (etat) {
            case SELECTION_PARKING:
            case VERIFICATION_DISPONIBILITE:
                if (source == vue.getBtnAnnuler()) {
                    annuler();
                } else if (source == vue.getBtnReserver()) {
                    traiterActionReservation();
                } else if (source == vue.getBtnModifierPlaque()) {
                    etat = Etat.MODIFICATION_VEHICULE;
                    modifierPlaque();
                }
                break;
                
            case MODIFICATION_VEHICULE:
                break;
                
            case CONFIRMATION_RESERVATION:
                break;
                
            case ERREUR:
                break;
        }
    }
    
    /**
     * Met à jour toutes les informations du parking sélectionné dans la vue :
     * places disponibles, places moto, tarifs et état du bouton de réservation.
     * 
     * @param index l'indice du parking dans la liste
     */
    private void mettreAJourInfosParking(int index) {
        try {
            if (index < 0 || index >= listeParkings.size()) return;
            
            Parking parking = listeParkings.get(index);
            
            vue.setPlacesDisponibles(parking.getPlacesDisponibles(), parking.getNombrePlaces());
            
            if (parking.hasMoto()) {
                vue.setPlacesMotoDisponibles(parking.getPlacesMotoDisponibles(), parking.getPlacesMoto());
            } else {
                vue.setPlacesMotoDisponibles(0, 0);
            }
            
            mettreAJourTarifsParking(parking);
            verifierDisponibiliteEtMettreAJourBouton();
            
        } catch (Exception e) {
            gererErreur("Erreur mise à jour parking", e.getMessage());
        }
    }
    
    /**
     * Met à jour l'affichage des tarifs pour le parking sélectionné.
     * Gère les cas spéciaux : parkings gratuits, relais Tisséo, abonnements moto,
     * et tarifs normaux avec tarif soirée éventuel.
     * 
     * @param parking le parking dont on affiche les tarifs
     */
    private void mettreAJourTarifsParking(Parking parking) {
        try {
            double tarifHoraire = tarifDAO.getTarifHoraire(parking.getIdParking());
            boolean proposeTarifSoiree = tarifDAO.proposeTarifSoiree(parking.getIdParking());
            boolean estDansPlageSoiree = tarifDAO.estDansPlageTarifSoiree(java.time.LocalDateTime.now());
            
            boolean estGratuit = tarifDAO.estParkingGratuit(parking.getIdParking());
            boolean estRelais = parking.isEstRelais();
            
            boolean estMoto = "Moto".equals(vue.getTypeVehicule());
            boolean aAbonnementMoto = false;
            
            if (estMoto && parking.hasMoto()) {
                try {
                    Abonnement abonnement = AbonnementDAO.getInstance().getAbonnementActif(usager.getIdUsager());
                    if (abonnement != null && "ABO_MOTO_RESIDENT".equals(abonnement.getIdAbonnement())) {
                        aAbonnementMoto = true;
                    }
                } catch (Exception e) {
                    System.err.println("Erreur vérification abonnement moto: " + e.getMessage());
                }
            }
            
            if (aAbonnementMoto) {
                vue.setTarifHoraire("GRATUIT (Abonnement moto)");
                vue.setTarifSoiree("Stationnement gratuit avec votre abonnement");
                vue.setTarifSoireeCouleur(new Color(0, 128, 0));
                vue.setTypeParking("Gratuit - Abonnement moto résident");
            } else if (estGratuit) {
                vue.setTarifHoraire("GRATUIT");
                vue.setTarifSoiree("Parking gratuit");
                vue.setTarifSoireeCouleur(new Color(0, 128, 0));
                vue.setTypeParking("Gratuit");
            } else if (estRelais) {
                vue.setTarifHoraire("Relais Tisséo");
                vue.setTarifSoiree("Gratuit avec carte Tisséo");
                vue.setTarifSoireeCouleur(Color.BLUE);
                vue.setTypeParking("Relais Tisséo");
            } else {
                vue.setTarifHoraire(String.format("%.2f €/h", tarifHoraire));
                vue.setTypeParking("Parking payant");
                
                if (proposeTarifSoiree) {
                    if (estDansPlageSoiree) {
                        vue.setTarifSoiree("5.90€ ✓ Actif (19h30-3h)");
                        vue.setTarifSoireeCouleur(new Color(0, 128, 0));
                    } else {
                        vue.setTarifSoiree("5.90€ (19h30-minuit, départ avant 3h)");
                        vue.setTarifSoireeCouleur(Color.BLUE);
                    }
                } else {
                    vue.setTarifSoiree("Non disponible");
                    vue.setTarifSoireeCouleur(Color.GRAY);
                }
            }
            
        } catch (Exception e) {
            vue.setTarifHoraire("Erreur");
            vue.setTarifSoiree("Erreur chargement tarifs");
            vue.setTarifSoireeCouleur(Color.RED);
            vue.setTypeParking("Erreur");
        }
    }
    
    /**
     * Traite l'action du bouton de réservation qui peut être en mode "Réserver"
     * ou "Alternatives" selon la disponibilité du parking.
     */
    private void traiterActionReservation() {
        String texteBouton = vue.getBtnReserver().getText();
        
        if ("Réserver".equals(texteBouton)) {
            reserver();
        } else if ("Alternatives".equals(texteBouton)) {
            proposerParkingsProches();
        }
    }
    
    /**
     * Lance le processus de réservation en validant les prérequis,
     * la compatibilité parking-véhicule, puis en demandant confirmation avant réservation.
     */
    private void reserver() {
        if (!validerPreRequisReservation()) {
            return;
        }
        
        if (!validerParkingPourVehicule()) {
            return;
        }
        
        if (!demanderConfirmationReservation()) {
            return;
        }
        
        effectuerReservation();
    }
    
    /**
     * Valide les prérequis avant une réservation :
     * parking sélectionné, plaque valide, type de véhicule, places disponibles.
     * Si le parking est complet, propose des alternatives.
     * 
     * @return true si tous les prérequis sont validés, false sinon
     */
    private boolean validerPreRequisReservation() {
        if (parkingSelectionne == null) {
            vue.afficherMessageErreur("Aucun parking sélectionné", 
                "Veuillez sélectionner un parking dans la liste.");
            return false;
        }
        
        String plaque = vue.getPlaque();
        if (plaque == null || plaque.trim().isEmpty() || 
            "AUCUN VÉHICULE".equals(plaque) || "ERREUR CHARGEMENT".equals(plaque)) {
            vue.afficherMessageErreur("Plaque manquante",
                "Veuillez sélectionner ou saisir une plaque d'immatriculation valide.");
            return false;
        }
        
        String typeVehicule = vue.getTypeVehicule();
        if (typeVehicule == null) {
            vue.afficherMessageErreur("Type de véhicule manquant",
                "Veuillez sélectionner un type de véhicule.");
            return false;
        }
        
        if (parkingSelectionne.getPlacesDisponibles() <= 0) {
            proposerParkingsProches();
            return false;
        }
        
        if ("Moto".equals(typeVehicule) && parkingSelectionne.hasMoto() && 
            parkingSelectionne.getPlacesMotoDisponibles() <= 0) {
            JOptionPane.showMessageDialog(vue,
                "Ce parking n'a plus de places moto disponibles.",
                "Places moto indisponibles",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Valide que le parking sélectionné est compatible avec le type de véhicule.
     * Vérifie les places moto pour les motos et la hauteur maximale pour les camions.
     * 
     * @return true si le parking est compatible, false sinon
     */
    private boolean validerParkingPourVehicule() {
        String typeVehicule = vue.getTypeVehicule();
        
        switch (typeVehicule) {
            case "Moto":
                return validerParkingPourMoto();
            case "Camion":
                return validerHauteurParking();
            default:
                return true;
        }
    }
    
    /**
     * Valide que le parking dispose de places moto disponibles.
     * 
     * @return true si des places moto sont disponibles, false sinon
     */
    private boolean validerParkingPourMoto() {
        try {
            if (!parkingSelectionne.hasMoto()) {
                vue.afficherMessageErreur("Parking non adapté",
                    "Ce parking ne dispose pas de places pour les motos.");
                return false;
            }
            
            if (parkingSelectionne.getPlacesMotoDisponibles() <= 0) {
                vue.afficherMessageErreur("Places moto indisponibles",
                    "Désolé, plus de places moto disponibles dans ce parking.");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            gererErreur("Erreur validation moto", e.getMessage());
            return false;
        }
    }
    
    /**
     * Valide que le parking a une hauteur suffisante pour un camion (3.0m minimum).
     * 
     * @return true si la hauteur est suffisante ou non limitée, false sinon
     */
    private boolean validerHauteurParking() {
        try {
            double hauteurCamion = 3.0;
            if (parkingSelectionne.getHauteurParking() > 0 && 
                parkingSelectionne.getHauteurParking() < hauteurCamion) {
                vue.afficherMessageErreur("Hauteur insuffisante",
                    String.format("Ce parking a une hauteur maximale de %.1fm. " +
                                 "Votre camion (%.1fm) ne peut pas y entrer.",
                                 parkingSelectionne.getHauteurParking(), hauteurCamion));
                return false;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Propose une liste de parkings alternatifs proches en cas de parking complet.
     * Affiche jusqu'à 5 parkings disponibles avec leurs informations et permet
     * à l'utilisateur d'en sélectionner un directement.
     */
    private void proposerParkingsProches() {
        try {
            parkingsProposes = ParkingDAO.getInstance().trouverParkingsProches(
                parkingSelectionne.getIdParking(), 
                5
            );
            
            if (parkingsProposes.isEmpty()) {
                vue.afficherMessageErreur("Parking complet",
                    "Désolé, ce parking n'a plus de places disponibles.");
                return;
            }
            
            StringBuilder message = new StringBuilder();
            message.append("Le parking \"").append(parkingSelectionne.getLibelleParking())
                   .append("\" est complet.\n\n");
            message.append("Voici les parkings disponibles les plus proches :\n\n");
            
            for (int i = 0; i < parkingsProposes.size(); i++) {
                Parking p = parkingsProposes.get(i);
                message.append(i + 1).append(". ")
                       .append(p.getLibelleParking()).append("\n")
                       .append("   Adresse : ").append(p.getAdresseParking()).append("\n")
                       .append("   Places disponibles : ").append(p.getPlacesDisponibles())
                       .append("/").append(p.getNombrePlaces());
                
                if ("Moto".equals(vue.getTypeVehicule()) && p.hasMoto()) {
                    message.append(" (moto : ").append(p.getPlacesMotoDisponibles())
                           .append("/").append(p.getPlacesMoto()).append(")");
                }
                
                message.append("\n\n");
            }
            
            message.append("Souhaitez-vous sélectionner l'un de ces parkings ?");
            
            String[] options = new String[parkingsProposes.size() + 1];
            for (int i = 0; i < parkingsProposes.size(); i++) {
                Parking p = parkingsProposes.get(i);
                options[i] = "Parking " + (i + 1) + " : " + p.getLibelleParking();
            }
            options[parkingsProposes.size()] = "Annuler";
            
            int choix = JOptionPane.showOptionDialog(vue,
                message.toString(),
                "Parking complet - Alternatives proposées",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);
            
            if (choix >= 0 && choix < parkingsProposes.size()) {
                selectionnerParkingAlternatif(parkingsProposes.get(choix));
            }
            
        } catch (Exception e) {
            vue.afficherMessageErreur("Erreur",
                "Le parking est complet. Erreur lors de la recherche d'alternatives.");
        }
    }
    
    /**
     * Sélectionne un parking alternatif dans le ComboBox et met à jour l'affichage.
     * 
     * @param parking le parking alternatif à sélectionner
     */
    private void selectionnerParkingAlternatif(Parking parking) {
        int index = -1;
        for (int i = 0; i < listeParkings.size(); i++) {
            if (listeParkings.get(i).getIdParking().equals(parking.getIdParking())) {
                index = i;
                break;
            }
        }
        
        if (index != -1) {
            vue.getComboParking().setSelectedIndex(index);
            parkingSelectionne = parking;
            mettreAJourInfosParking(index);
            
            JOptionPane.showMessageDialog(vue,
                "Parking sélectionné : " + parking.getLibelleParking(),
                "Parking alternatif sélectionné",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Demande confirmation à l'utilisateur avant d'effectuer la réservation.
     * Affiche un récapitulatif complet : parking, véhicule, plaque et tarifs.
     * 
     * @return true si l'utilisateur confirme, false sinon
     */
    private boolean demanderConfirmationReservation() {
        StringBuilder message = new StringBuilder();
        message.append("Confirmez-vous la réservation ?\n\n");
        message.append("Parking: ").append(parkingSelectionne.getLibelleParking()).append("\n");
        message.append("Adresse: ").append(parkingSelectionne.getAdresseParking()).append("\n");
        message.append("Véhicule: ").append(vue.getTypeVehicule()).append("\n");
        message.append("Plaque: ").append(vue.getPlaque()).append("\n\n");
        
        try {
            if (tarifDAO.estParkingGratuit(parkingSelectionne.getIdParking())) {
                message.append("Tarif: GRATUIT\n");
            } else if (parkingSelectionne.isEstRelais()) {
                message.append("Tarif: Relais Tisséo (gratuit avec carte)\n");
            } else {
                message.append("Tarif horaire: ").append(
                    String.format("%.2f€/h\n", tarifDAO.getTarifHoraire(parkingSelectionne.getIdParking())));
                
                if (tarifDAO.proposeTarifSoiree(parkingSelectionne.getIdParking())) {
                    message.append("Tarif soirée disponible: 5.90€\n");
                }
            }
        } catch (Exception e) {
            // Ignorer les erreurs de tarifs
        }
        
        int choix = JOptionPane.showConfirmDialog(vue,
            message.toString(),
            "Confirmation de réservation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        return choix == JOptionPane.YES_OPTION;
    }
    
    /**
     * Effectue la réservation en appelant le contrôleur de stationnement.
     * Affiche un message de confirmation en cas de succès et redirige vers la page principale.
     */
    private void effectuerReservation() {
        etat = Etat.CONFIRMATION_RESERVATION;
        
        try {
            String typeVehicule = vue.getTypeVehicule();
            String plaque = vue.getPlaque();
            
            boolean succes = stationnementControleur.preparerStationnementParking(
                typeVehicule,
                plaque,
                parkingSelectionne.getIdParking(),
                vue
            );
            
            if (succes) {
                afficherConfirmationReservation();
                redirigerVersPagePrincipale();
            } else {
                etat = Etat.SELECTION_PARKING;
                vue.afficherMessageErreur("Échec réservation",
                    "La réservation a échoué. Veuillez réessayer.");
            }
            
        } catch (Exception e) {
            etat = Etat.SELECTION_PARKING;
            gererErreur("Erreur réservation", e.getMessage());
        }
    }
    
    /**
     * Affiche un message de confirmation de la réservation avec les détails
     * du parking, l'heure d'arrivée et les informations du véhicule.
     */
    private void afficherConfirmationReservation() {
        String message = "Réservation confirmée !\n\n" +
                        "Parking: " + parkingSelectionne.getLibelleParking() + "\n" +
                        "Heure d'arrivée: " + 
                        java.time.LocalDateTime.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
                        "Véhicule: " + vue.getTypeVehicule() + " - " + vue.getPlaque();
        
        JOptionPane.showMessageDialog(vue,
            message,
            "Réservation réussie",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Permet à l'utilisateur de modifier la plaque d'immatriculation.
     * Propose deux options : choisir un véhicule existant ou saisir une nouvelle plaque.
     */
    private void modifierPlaque() {
        String[] options = {"Choisir un véhicule existant", "Saisir une nouvelle plaque"};
        int choix = JOptionPane.showOptionDialog(vue,
            "Que souhaitez-vous faire ?",
            "Modifier la plaque",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (choix == 0) {
            choisirVehiculeExistant();
        } else if (choix == 1) {
            saisirNouvellePlaque();
        }
    }
    
    /**
     * Affiche la liste des véhicules enregistrés de l'utilisateur et permet d'en sélectionner un.
     * Si aucun véhicule n'existe, propose d'en ajouter un nouveau.
     */
    private void choisirVehiculeExistant() {
        try {
            List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsagerStatic(usager.getIdUsager());
            
            if (vehicules.isEmpty()) {
                int option = vue.demanderConfirmation("Aucun véhicule",
                    "Vous n'avez aucun véhicule enregistré.\nSouhaitez-vous en ajouter un maintenant ?");
                
                if (option == JOptionPane.YES_OPTION) {
                    saisirNouvellePlaque();
                }
                return;
            }
            
            String[] options = new String[vehicules.size()];
            for (int i = 0; i < vehicules.size(); i++) {
                VehiculeUsager v = vehicules.get(i);
                String etoile = v.isEstPrincipal() ? " ★" : "";
                options[i] = v.getPlaqueImmatriculation() + " - " + v.getTypeVehicule() + etoile;
            }
            
            String selection = (String) JOptionPane.showInputDialog(vue,
                "Choisissez un véhicule :",
                "Sélection du véhicule",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
            
            if (selection != null) {
                for (VehiculeUsager v : vehicules) {
                    if (selection.startsWith(v.getPlaqueImmatriculation())) {
                        afficherVehicule(v);
                        break;
                    }
                }
            }
            
            etat = Etat.SELECTION_PARKING;
            
        } catch (Exception e) {
            gererErreur("Erreur sélection véhicule", e.getMessage());
        }
    }
    
    /**
     * Permet de saisir une nouvelle plaque d'immatriculation.
     * Valide le format (AA-123-AA ou AA123AA), normalise la plaque et propose
     * de l'enregistrer pour une utilisation future.
     */
    private void saisirNouvellePlaque() {
        String plaqueActuelle = vue.getPlaque();
        if ("AUCUN VÉHICULE".equals(plaqueActuelle) || "ERREUR CHARGEMENT".equals(plaqueActuelle)) {
            plaqueActuelle = "";
        }
        
        String nouvellePlaque = JOptionPane.showInputDialog(vue, 
            "Entrez la plaque d'immatriculation (format: AA-123-AA ou AA123AA):", 
            plaqueActuelle);
        
        if (nouvellePlaque == null) {
            etat = Etat.SELECTION_PARKING;
            return;
        }
        
        String plaqueNettoyee = nouvellePlaque.trim().toUpperCase();
        
        if (plaqueNettoyee.isEmpty()) {
            vue.afficherMessageErreur("Plaque vide", "La plaque ne peut pas être vide.");
            etat = Etat.SELECTION_PARKING;
            return;
        }
        
        if (!validerFormatPlaque(plaqueNettoyee)) {
            vue.afficherMessageErreur("Format invalide",
                "Format de plaque invalide. Utilisez AA-123-AA ou AA123AA");
            etat = Etat.SELECTION_PARKING;
            return;
        }
        
        plaqueNettoyee = normaliserFormatPlaque(plaqueNettoyee);
        vue.setPlaque(plaqueNettoyee);
        
        if (vue.getTypeVehicule() == null) {
            String type = demanderTypeVehicule();
            if (type != null) {
                vue.setTypeVehicule(type);
            }
        }
        
        if (usager != null) {
            int sauvegarder = JOptionPane.showConfirmDialog(vue,
                "Voulez-vous enregistrer ce véhicule pour une utilisation future ?",
                "Enregistrer le véhicule",
                JOptionPane.YES_NO_OPTION);
            
            if (sauvegarder == JOptionPane.YES_OPTION) {
                sauvegarderVehicule(plaqueNettoyee);
            }
        }
        
        etat = Etat.SELECTION_PARKING;
    }
    
    /**
     * Valide le format d'une plaque d'immatriculation française.
     * Accepte les formats AA-123-AA et AA123AA.
     * 
     * @param plaque la plaque à valider
     * @return true si le format est valide, false sinon
     */
    private boolean validerFormatPlaque(String plaque) {
        return plaque.matches("[A-Z]{2}-\\d{3}-[A-Z]{2}") || 
               plaque.matches("[A-Z]{2}\\d{3}[A-Z]{2}");
    }
    
    /**
     * Normalise le format d'une plaque d'immatriculation en ajoutant les tirets
     * si nécessaire (AA123AA → AA-123-AA).
     * 
     * @param plaque la plaque à normaliser
     * @return la plaque normalisée au format AA-123-AA
     */
    private String normaliserFormatPlaque(String plaque) {
        if (plaque.matches("[A-Z]{2}\\d{3}[A-Z]{2}")) {
            return plaque.substring(0, 2) + "-" + 
                   plaque.substring(2, 5) + "-" + 
                   plaque.substring(5);
        }
        return plaque;
    }
    
    /**
     * Affiche une boîte de dialogue pour sélectionner le type de véhicule.
     * 
     * @return le type sélectionné ("Voiture", "Moto" ou "Camion"), ou null si annulé
     */
    private String demanderTypeVehicule() {
        String[] types = {"Voiture", "Moto", "Camion"};
        return (String) JOptionPane.showInputDialog(vue,
            "Sélectionnez le type de véhicule :",
            "Type de véhicule",
            JOptionPane.QUESTION_MESSAGE,
            null,
            types,
            types[0]);
    }
    
    /**
     * Sauvegarde un nouveau véhicule dans la base de données.
     * Vérifie que le véhicule n'existe pas déjà et demande s'il doit être
     * défini comme véhicule principal.
     * 
     * @param plaque la plaque du véhicule à sauvegarder
     */
    private void sauvegarderVehicule(String plaque) {
        try {
            String typeVehicule = vue.getTypeVehicule();
            if (typeVehicule == null) {
                typeVehicule = demanderTypeVehicule();
                if (typeVehicule == null) return;
                vue.setTypeVehicule(typeVehicule);
            }
            
            List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsagerStatic(usager.getIdUsager());
            for (VehiculeUsager v : vehicules) {
                if (plaque.equals(v.getPlaqueImmatriculation())) {
                    vue.afficherMessageInformation("Véhicule existant",
                        "Ce véhicule est déjà enregistré.");
                    return;
                }
            }
            
            VehiculeUsager vehicule = new VehiculeUsager(
                usager.getIdUsager(),
                plaque,
                typeVehicule
            );
            
            if (vehicules.isEmpty()) {
                vehicule.setEstPrincipal(true);
            } else {
                int estPrincipal = JOptionPane.showConfirmDialog(vue,
                    "Définir ce véhicule comme véhicule principal ?",
                    "Véhicule principal",
                    JOptionPane.YES_NO_OPTION);
                
                vehicule.setEstPrincipal(estPrincipal == JOptionPane.YES_OPTION);
            }
            
            boolean succes = VehiculeUsagerDAO.ajouterVehiculeStatic(vehicule);
            if (succes) {
                vue.afficherMessageInformation("Succès",
                    "Véhicule enregistré avec succès !");
            } else {
                vue.afficherMessageErreur("Erreur",
                    "Erreur lors de l'enregistrement du véhicule.");
            }
            
        } catch (Exception e) {
            gererErreur("Erreur sauvegarde véhicule", e.getMessage());
        }
    }
    
    /**
     * Annule la réservation après confirmation de l'utilisateur
     * et redirige vers la page principale.
     */
    private void annuler() {
        int confirmation = JOptionPane.showConfirmDialog(vue,
            "Êtes-vous sûr de vouloir annuler ?\nVos sélections seront perdues.",
            "Confirmation annulation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            redirigerVersPagePrincipale();
        }
    }
    
    /**
     * Redirige l'utilisateur vers la page principale et ferme la page actuelle.
     */
    private void redirigerVersPagePrincipale() {
        try {
            etat = Etat.REDIRECTION;
            SwingUtilities.invokeLater(() -> {
                new Page_Principale(emailUtilisateur).setVisible(true);
                vue.dispose();
            });
        } catch (Exception e) {
            gererErreur("Erreur redirection", e.getMessage());
        }
    }
    
    /**
     * Vérifie la disponibilité du parking pour le type de véhicule sélectionné
     * et met à jour l'apparence du bouton de réservation en conséquence.
     * Configure le bouton en mode "Alternatives" si le parking est complet.
     */
    private void verifierDisponibiliteEtMettreAJourBouton() {
        if (parkingSelectionne == null) return;
        
        boolean estComplet = false;
        String typeVehicule = vue.getTypeVehicule();
        
        if ("Moto".equals(typeVehicule)) {
            if (parkingSelectionne.hasMoto()) {
                estComplet = parkingSelectionne.getPlacesMotoDisponibles() <= 0;
            } else {
                estComplet = true;
            }
        } else {
            estComplet = parkingSelectionne.getPlacesDisponibles() <= 0;
        }
        
        if (estComplet) {
            mettreBoutonEnModeAlternatives();
        } else {
            mettreBoutonEnModeReserver();
        }
    }

    /**
     * Configure le bouton de réservation en mode "Alternatives" pour indiquer
     * que le parking est complet et proposer d'autres options.
     */
    private void mettreBoutonEnModeAlternatives() {
        JButton btnReserver = vue.getBtnReserver();
        btnReserver.setText("Alternatives");
        btnReserver.setBackground(new Color(255, 165, 0));
        btnReserver.setForeground(Color.WHITE);
        btnReserver.setToolTipText("Ce parking est complet. Cliquez pour voir des alternatives.");
    }

    /**
     * Configure le bouton de réservation en mode normal "Réserver"
     * lorsque des places sont disponibles.
     */
    private void mettreBoutonEnModeReserver() {
        JButton btnReserver = vue.getBtnReserver();
        btnReserver.setText("Réserver");
        btnReserver.setBackground(new Color(0, 153, 0));
        btnReserver.setForeground(Color.WHITE);
        btnReserver.setToolTipText("Réserver une place dans ce parking");
    }
    
    /**
     * Gère une erreur survenue pendant l'utilisation du contrôleur.
     * Affiche un message d'erreur et passe à l'état ERREUR.
     * 
     * @param titre le titre du message d'erreur
     * @param message la description détaillée de l'erreur
     */
    private void gererErreur(String titre, String message) {
        System.err.println(titre + ": " + message);
        vue.afficherMessageErreur(titre, message);
        etat = Etat.ERREUR;
    }
    
    /**
     * Gère une erreur critique survenue lors de l'initialisation.
     * Affiche un message d'erreur et ferme l'application.
     * 
     * @param message la description de l'erreur d'initialisation
     */
    private void gererErreurInitialisation(String message) {
        System.err.println("Erreur initialisation: " + message);
        JOptionPane.showMessageDialog(vue,
            message + "\n\nL'application va se fermer.",
            "Erreur d'initialisation",
            JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
    
    /**
     * Retourne l'état actuel du contrôleur.
     * Utile pour le débogage et les tests.
     * 
     * @return l'état actuel du contrôleur
     */
    public Etat getEtat() {
        return etat;
    }
}
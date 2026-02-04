package controleur;

import ihm.Page_Garer_Voirie;
import ihm.Page_Principale;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import modele.Abonnement;
import modele.Usager;
import modele.Zone;
import modele.VehiculeUsager;
import modele.dao.UsagerDAO;
import modele.dao.VehiculeUsagerDAO;

/**
 * Contrôleur gérant le stationnement sur voirie (zones de stationnement payant en rue).
 * Permet aux utilisateurs de sélectionner une zone, choisir une durée, visualiser le coût
 * en temps réel (avec prise en compte des abonnements) et valider le stationnement.
 * Gère également les stationnements gratuits selon la zone et l'abonnement.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Garer_Voirie
 * et les modèles (Zone, Usager, VehiculeUsager, Abonnement).
 * 
 * @author Équipe 7
 */
public class ControleurGarerVoirie implements ActionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur de stationnement sur voirie.
     * Permet de suivre le cycle de vie du processus de stationnement.
     */
    private enum Etat {
        /** État initial au démarrage du contrôleur */
        INITIAL,
        /** L'utilisateur est en train de saisir les informations de stationnement */
        SAISIE,
        /** Validation des données saisies en cours */
        VALIDATION,
        /** Traitement d'un stationnement gratuit en cours */
        STATIONNEMENT_GRATUIT,
        /** Préparation du paiement pour un stationnement payant */
        PREPARATION_PAIEMENT,
        /** Redirection vers une autre page en cours */
        REDIRECTION,
        /** Une erreur s'est produite */
        ERREUR
    }
    
    private Page_Garer_Voirie vue;
    private Etat etat;
    private StationnementControleur stationnementControleur;
    private String emailUtilisateur;
    private Usager usager;
    private List<Zone> zones;
    private VehiculeUsager vehiculeSelectionne;
    
    /**
     * Constructeur du contrôleur de stationnement sur voirie.
     * Initialise le contrôleur avec la vue associée et charge les données nécessaires.
     * 
     * @param vue la page d'interface graphique de stationnement sur voirie
     */
    public ControleurGarerVoirie(Page_Garer_Voirie vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.etat = Etat.INITIAL;
        
        initialiserControleur();
    }
    
    /**
     * Initialise le contrôleur en chargeant l'utilisateur, le contrôleur de stationnement
     * et en configurant la vue avec les zones et le véhicule principal.
     * En cas d'erreur, gère l'erreur d'initialisation.
     */
    private void initialiserControleur() {
        try {
            chargerUtilisateur();
            initialiserStationnementControleur();
            initialiserVue();
            etat = Etat.SAISIE;
            
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
     * Initialise la vue en configurant les écouteurs, remplissant les informations utilisateur,
     * chargeant les zones disponibles et le véhicule principal, puis calcule le coût initial.
     */
    private void initialiserVue() {
        configurerListeners();
        vue.setNomUsager(usager.getNomUsager());
        vue.setPrenomUsager(usager.getPrenomUsager());
        vue.setEmailUsager(usager.getMailUsager());
        
        chargerZones();
        chargerVehiculePrincipal();
        recalculerCout();
    }
    
    /**
     * Configure tous les écouteurs d'événements pour les composants interactifs de la vue.
     * Connecte les boutons et les ComboBox pour le recalcul automatique du coût
     * lorsque la zone ou la durée change.
     */
    private void configurerListeners() {
        vue.getBtnAnnuler().addActionListener(this);
        vue.getBtnValider().addActionListener(this);
        vue.getBtnModifierPlaque().addActionListener(this);
        
        vue.getComboZone().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (etat == Etat.SAISIE && e.getStateChange() == ItemEvent.SELECTED) {
                    recalculerCout();
                }
            }
        });
        
        vue.getComboHeures().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (etat == Etat.SAISIE && e.getStateChange() == ItemEvent.SELECTED) {
                    recalculerCout();
                }
            }
        });
        
        vue.getComboMinutes().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (etat == Etat.SAISIE && e.getStateChange() == ItemEvent.SELECTED) {
                    recalculerCout();
                }
            }
        });
    }
    
    /**
     * Charge la liste des zones de stationnement disponibles depuis la vue.
     * En cas d'erreur, gère l'erreur de chargement.
     */
    private void chargerZones() {
        try {
            vue.chargerZones();
            this.zones = vue.getZones();
        } catch (Exception e) {
            gererErreur("Erreur chargement zones", e.getMessage());
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
                    vue.setPlaque("Non définie");
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
     * Affiche les informations d'un véhicule dans la vue et le mémorise comme sélectionné.
     * 
     * @param vehicule le véhicule à afficher
     */
    private void afficherVehicule(VehiculeUsager vehicule) {
        vue.setPlaque(vehicule.getPlaqueImmatriculation());
        vue.setTypeVehicule(vehicule.getTypeVehicule());
        this.vehiculeSelectionne = vehicule;
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
            case SAISIE:
                if (source == vue.getBtnAnnuler()) {
                    annuler();
                } else if (source == vue.getBtnValider()) {
                    validerStationnement();
                } else if (source == vue.getBtnModifierPlaque()) {
                    modifierPlaque();
                }
                break;
                
            case VALIDATION:
                break;
                
            case STATIONNEMENT_GRATUIT:
                break;
                
            case PREPARATION_PAIEMENT:
                break;
                
            case ERREUR:
                break;
        }
    }
    
    /**
     * Lance le processus de validation du stationnement.
     * Valide les prérequis, calcule le coût final et redirige vers le traitement
     * approprié selon que le stationnement est gratuit ou payant.
     */
    private void validerStationnement() {
        etat = Etat.VALIDATION;
        
        if (!validerPreRequis()) {
            etat = Etat.SAISIE;
            return;
        }
        
        Zone zone = vue.getZoneSelectionnee();
        int heures = Integer.parseInt(vue.getComboHeures().getSelectedItem().toString());
        int minutes = Integer.parseInt(vue.getComboMinutes().getSelectedItem().toString());
        
        double cout = calculerCoutFinal(zone, heures, minutes);
        
        if (cout == 0.00) {
            etat = Etat.STATIONNEMENT_GRATUIT;
            demanderConfirmationStationnementGratuit(zone, heures, minutes);
        } else {
            etat = Etat.PREPARATION_PAIEMENT;
            demanderConfirmationStationnementPayant(zone, heures, minutes, cout);
        }
    }
    
    /**
     * Valide les prérequis avant de créer un stationnement :
     * plaque valide, zone sélectionnée, type de véhicule défini.
     * 
     * @return true si tous les prérequis sont validés, false sinon avec affichage d'erreur
     */
    private boolean validerPreRequis() {
        String plaque = vue.getPlaque();
        if ("Non définie".equals(plaque) || plaque.trim().isEmpty() || 
            "ERREUR CHARGEMENT".equals(plaque)) {
            vue.afficherMessageErreur(
                "Plaque manquante",
                "Veuillez définir une plaque d'immatriculation");
            return false;
        }
        
        if (!stationnementControleur.validerPlaque(plaque)) {
            vue.afficherMessageErreur(
                "Erreur de plaque",
                "Format de plaque invalide. Utilisez AA-123-AA");
            return false;
        }
        
        Zone zone = vue.getZoneSelectionnee();
        if (zone == null) {
            vue.afficherMessageErreur(
                "Zone manquante",
                "Veuillez sélectionner une zone");
            return false;
        }
        
        String typeVehicule = vue.getTypeVehicule();
        if (typeVehicule == null) {
            vue.afficherMessageErreur(
                "Type de véhicule manquant",
                "Veuillez sélectionner un type de véhicule");
            return false;
        }
        
        return true;
    }
    
    /**
     * Calcule le coût final du stationnement en tenant compte de l'abonnement éventuel.
     * Si l'utilisateur possède un abonnement actif, applique les réductions correspondantes.
     * 
     * @param zone la zone de stationnement
     * @param heures le nombre d'heures de stationnement
     * @param minutes le nombre de minutes de stationnement
     * @return le coût final en euros, 0.00 si gratuit
     */
    private double calculerCoutFinal(Zone zone, int heures, int minutes) {
        int dureeTotaleMinutes = (heures * 60) + minutes;
        
        Abonnement abonnement = modele.dao.AbonnementDAO.getAbonnementActifStatic(usager.getIdUsager());
        
        if (abonnement != null) {
            return zone.calculerCoutAvecAbonnement(dureeTotaleMinutes, abonnement);
        } else {
            return zone.calculerCout(dureeTotaleMinutes);
        }
    }
    
    /**
     * Affiche une boîte de dialogue de confirmation pour un stationnement gratuit.
     * Présente un récapitulatif complet avant validation.
     * 
     * @param zone la zone de stationnement
     * @param heures le nombre d'heures
     * @param minutes le nombre de minutes
     */
    private void demanderConfirmationStationnementGratuit(Zone zone, int heures, int minutes) {
        String message = "Confirmez-vous le stationnement ?\n\n" +
            "Type de véhicule: " + vue.getTypeVehicule() + "\n" +
            "Plaque: " + vue.getPlaque() + "\n" +
            "Zone: " + zone.getLibelleZone() + "\n" +
            "Durée: " + heures + "h" + minutes + "min\n" +
            "Coût: GRATUIT";
        
        int confirmation = JOptionPane.showConfirmDialog(vue,
            message,
            "Confirmation de stationnement gratuit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            enregistrerStationnementGratuit(zone, heures, minutes);
        } else {
            etat = Etat.SAISIE;
        }
    }
    
    /**
     * Affiche une boîte de dialogue de confirmation pour un stationnement payant.
     * Présente un récapitulatif complet avec le coût avant validation.
     * 
     * @param zone la zone de stationnement
     * @param heures le nombre d'heures
     * @param minutes le nombre de minutes
     * @param cout le coût total en euros
     */
    private void demanderConfirmationStationnementPayant(Zone zone, int heures, int minutes, double cout) {
        String message = "Confirmez-vous le stationnement ?\n\n" +
            "Type de véhicule: " + vue.getTypeVehicule() + "\n" +
            "Plaque: " + vue.getPlaque() + "\n" +
            "Zone: " + zone.getLibelleZone() + "\n" +
            "Durée: " + heures + "h" + minutes + "min\n" +
            "Coût: " + String.format("%.2f", cout) + " €";
        
        int confirmation = JOptionPane.showConfirmDialog(vue,
            message,
            "Confirmation de stationnement",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            preparerStationnementPayant(zone, heures, minutes);
        } else {
            etat = Etat.SAISIE;
        }
    }
    
    /**
     * Enregistre un stationnement gratuit dans la base de données.
     * Affiche un message de confirmation en cas de succès et retourne à la page principale.
     * 
     * @param zone la zone de stationnement
     * @param heures le nombre d'heures
     * @param minutes le nombre de minutes
     */
    private void enregistrerStationnementGratuit(Zone zone, int heures, int minutes) {
        String typeVehicule = vue.getTypeVehicule();
        String plaque = vue.getPlaque();
        
        boolean succes = stationnementControleur.creerStationnementVoirieGratuit(
            typeVehicule,
            plaque,
            zone.getIdZone(),
            heures,
            minutes
        );
        
        if (succes) {
            vue.afficherMessageInformation(
                "Stationnement activé",
                "✅ Stationnement gratuit activé avec succès !\n\n" +
                "Votre stationnement en " + zone.getLibelleZone() + " est maintenant actif.\n" +
                "Durée: " + heures + "h" + minutes + "min\n" +
                "Statut: GRATUIT");
            
            retourPagePrincipale();
        } else {
            vue.afficherMessageErreur(
                "Erreur",
                "❌ Une erreur est survenue lors de l'activation du stationnement.");
            etat = Etat.SAISIE;
        }
    }
    
    /**
     * Prépare un stationnement payant en appelant le contrôleur de stationnement.
     * Redirige vers le processus de paiement si la préparation réussit.
     * 
     * @param zone la zone de stationnement
     * @param heures le nombre d'heures
     * @param minutes le nombre de minutes
     */
    private void preparerStationnementPayant(Zone zone, int heures, int minutes) {
        String typeVehicule = vue.getTypeVehicule();
        String plaque = vue.getPlaque();
        
        boolean succes = stationnementControleur.preparerStationnementVoirie(
            typeVehicule,
            plaque,
            zone.getIdZone(),
            heures,
            minutes,
            vue
        );
        
        if (succes) {
            etat = Etat.REDIRECTION;
        } else {
            vue.afficherMessageErreur("Erreur", "Impossible de préparer le stationnement.");
            etat = Etat.SAISIE;
        }
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
     * Met à jour l'affichage et recalcule le coût après sélection.
     */
    private void choisirVehiculeExistant() {
        try {
            List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsagerStatic(usager.getIdUsager());
            
            if (vehicules.isEmpty()) {
                vue.afficherMessageInformation(
                    "Aucun véhicule",
                    "Vous n'avez aucun véhicule enregistré.");
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
                        recalculerCout();
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            gererErreur("Erreur sélection véhicule", e.getMessage());
        }
    }
    
    /**
     * Permet de saisir une nouvelle plaque d'immatriculation.
     * Valide et normalise le format, puis propose d'enregistrer le véhicule.
     * Recalcule le coût après modification.
     */
    private void saisirNouvellePlaque() {
        String plaqueActuelle = vue.getPlaque();
        if ("Non définie".equals(plaqueActuelle) || "ERREUR CHARGEMENT".equals(plaqueActuelle)) {
            plaqueActuelle = "";
        }
        
        String nouvellePlaque = JOptionPane.showInputDialog(vue, 
            "Entrez la plaque d'immatriculation (format: AA-123-AA ou AA123AA):", 
            plaqueActuelle);
        
        if (nouvellePlaque == null) {
            return;
        }
        
        String plaqueNettoyee = nouvellePlaque.trim().toUpperCase();
        
        if (plaqueNettoyee.isEmpty()) {
            vue.afficherMessageErreur("Plaque vide", "La plaque ne peut pas être vide.");
            return;
        }
        
        plaqueNettoyee = normaliserFormatPlaque(plaqueNettoyee);
        
        if (!stationnementControleur.validerPlaque(plaqueNettoyee)) {
            vue.afficherMessageErreur(
                "Format invalide",
                "Format de plaque invalide. Utilisez AA-123-AA ou AA123AA");
            return;
        }
        
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
        
        recalculerCout();
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
     * Recalcule et met à jour l'affichage du coût du stationnement en temps réel.
     * Prend en compte la zone sélectionnée, la durée et l'abonnement éventuel.
     * Affiche "GRATUIT" en vert si le coût est nul.
     */
    private void recalculerCout() {
        try {
            int heures = Integer.parseInt(vue.getComboHeures().getSelectedItem().toString());
            int minutes = Integer.parseInt(vue.getComboMinutes().getSelectedItem().toString());
            int dureeTotaleMinutes = (heures * 60) + minutes;
            
            Zone zone = vue.getZoneSelectionnee();
            if (zone != null) {
                Abonnement abonnement = modele.dao.AbonnementDAO.getAbonnementActifStatic(usager.getIdUsager());
                
                double cout;
                if (abonnement != null) {
                    cout = zone.calculerCoutAvecAbonnement(dureeTotaleMinutes, abonnement);
                } else {
                    cout = zone.calculerCout(dureeTotaleMinutes);
                }
                
                if (cout == 0.00) {
                    vue.setCoutAvecCouleur("GRATUIT", new Color(0, 150, 0));
                } else {
                    vue.setCout(String.format("%.2f €", cout));
                }
            }
        } catch (Exception e) {
            vue.setCout("0.00 €");
        }
    }
    
    /**
     * Annule le processus de stationnement après confirmation de l'utilisateur
     * et retourne à la page principale.
     */
    private void annuler() {
        int confirmation = JOptionPane.showConfirmDialog(vue,
            "Êtes-vous sûr de vouloir annuler ?\nVos sélections seront perdues.",
            "Confirmation annulation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            retourPagePrincipale();
        }
    }
    
    /**
     * Retourne à la page principale et ferme la page actuelle.
     */
    private void retourPagePrincipale() {
        try {
            etat = Etat.REDIRECTION;
            Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
            pagePrincipale.setVisible(true);
            vue.dispose();
        } catch (Exception e) {
            gererErreur("Erreur redirection", e.getMessage());
        }
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
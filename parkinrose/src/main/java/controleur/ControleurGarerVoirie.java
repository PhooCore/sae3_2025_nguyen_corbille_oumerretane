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
import modele.Usager;
import modele.Zone;
import modele.VehiculeUsager;
import modele.dao.UsagerDAO;
import modele.dao.VehiculeUsagerDAO;

public class ControleurGarerVoirie implements ActionListener {
    
    // États du contrôleur
    private enum Etat {
        INITIAL,
        SAISIE,
        VALIDATION,
        STATIONNEMENT_GRATUIT,
        PREPARATION_PAIEMENT,
        REDIRECTION,
        ERREUR
    }
    
    // Références
    private Page_Garer_Voirie vue;
    private Etat etat;
    
    // Contrôleurs
    private StationnementControleur stationnementControleur;
    
    // Données
    private String emailUtilisateur;
    private Usager usager;
    private List<Zone> zones;
    private VehiculeUsager vehiculeSelectionne;
    
    public ControleurGarerVoirie(Page_Garer_Voirie vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.etat = Etat.INITIAL;
        
        initialiserControleur();
    }
    
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
    
    private void chargerUtilisateur() throws Exception {
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager == null) {
            throw new Exception("Utilisateur non trouvé");
        }
    }
    
    private void initialiserStationnementControleur() {
        this.stationnementControleur = new StationnementControleur(emailUtilisateur);
    }
    
    private void initialiserVue() {
        configurerListeners();
        vue.setNomUsager(usager.getNomUsager());
        vue.setPrenomUsager(usager.getPrenomUsager());
        vue.setEmailUsager(usager.getMailUsager());
        
        chargerZones();
        chargerVehiculePrincipal();
        recalculerCout();
    }
    
    private void configurerListeners() {
        // Boutons
        vue.getBtnAnnuler().addActionListener(this);
        vue.getBtnValider().addActionListener(this);
        vue.getBtnModifierPlaque().addActionListener(this);
        
        // Combos pour le recalcul automatique du coût
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
    
    private void chargerZones() {
        try {
            vue.chargerZones();
            this.zones = vue.getZones();
        } catch (Exception e) {
            gererErreur("Erreur chargement zones", e.getMessage());
        }
    }
    
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
    
    private void afficherVehicule(VehiculeUsager vehicule) {
        vue.setPlaque(vehicule.getPlaqueImmatriculation());
        vue.setTypeVehicule(vehicule.getTypeVehicule());
        this.vehiculeSelectionne = vehicule;
    }
    
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
                // Traitement en cours via les méthodes spécifiques
                break;
                
            case STATIONNEMENT_GRATUIT:
                // Traitement en cours via les méthodes spécifiques
                break;
                
            case PREPARATION_PAIEMENT:
                // Traitement en cours via les méthodes spécifiques
                break;
                
            case ERREUR:
                // Ne rien faire en état d'erreur
                break;
        }
    }
    
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
            // Stationnement gratuit
            etat = Etat.STATIONNEMENT_GRATUIT;
            demanderConfirmationStationnementGratuit(zone, heures, minutes);
        } else {
            // Stationnement payant
            etat = Etat.PREPARATION_PAIEMENT;
            demanderConfirmationStationnementPayant(zone, heures, minutes, cout);
        }
    }
    
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
    
    private double calculerCoutFinal(Zone zone, int heures, int minutes) {
        int dureeTotaleMinutes = (heures * 60) + minutes;
        double cout = zone.calculerCout(dureeTotaleMinutes);
        
        // Appliquer le tarif de l'abonnement si l'usager en a un
        if (stationnementControleur.usagerAUnAbonnementActif(usager.getIdUsager())) {
            double tarifAbonnement = stationnementControleur.getTarifAbonnement(usager.getIdUsager());
            if (tarifAbonnement > 0) {
                cout = tarifAbonnement;
            } else if (tarifAbonnement == 0.0) {
                cout = 0.0;
            }
        }
        
        return cout;
    }
    
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
    
    private String normaliserFormatPlaque(String plaque) {
        if (plaque.matches("[A-Z]{2}\\d{3}[A-Z]{2}")) {
            return plaque.substring(0, 2) + "-" + 
                   plaque.substring(2, 5) + "-" + 
                   plaque.substring(5);
        }
        return plaque;
    }
    
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
    
    private void recalculerCout() {
        try {
            int heures = Integer.parseInt(vue.getComboHeures().getSelectedItem().toString());
            int minutes = Integer.parseInt(vue.getComboMinutes().getSelectedItem().toString());
            int dureeTotaleMinutes = (heures * 60) + minutes;
            
            Zone zone = vue.getZoneSelectionnee();
            if (zone != null) {
                double cout = calculerCoutFinal(zone, heures, minutes);
                
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
    
    private void gererErreur(String titre, String message) {
        System.err.println(titre + ": " + message);
        vue.afficherMessageErreur(titre, message);
        etat = Etat.ERREUR;
    }
    
    private void gererErreurInitialisation(String message) {
        System.err.println("Erreur initialisation: " + message);
        JOptionPane.showMessageDialog(vue,
            message + "\n\nL'application va se fermer.",
            "Erreur d'initialisation",
            JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
    
    // Getters pour débogage
    public Etat getEtat() {
        return etat;
    }
}
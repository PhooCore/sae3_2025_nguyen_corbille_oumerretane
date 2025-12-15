package controleur;

import ihm.Page_Garer_Voirie;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ControleurGarerVoirie implements ActionListener {
    
    private enum EtatVoirie {
        INITIAL,
        SAISIE,
        VALIDATION,
        PREPARATION,
        REDIRECTION
    }
    
    private Page_Garer_Voirie vue;
    private EtatVoirie etat;
    private StationnementControleur controleurStationnement;
    
    public ControleurGarerVoirie(Page_Garer_Voirie vue) {
        this.vue = vue;
        this.controleurStationnement = new StationnementControleur(vue.emailUtilisateur);
        this.etat = EtatVoirie.INITIAL;
        configurerListeners();
        
        etat = EtatVoirie.SAISIE;
    }
    
    private void configurerListeners() {
        // Ajouter ActionListener aux boutons
        vue.btnAnnuler.addActionListener(this);
        vue.btnValider.addActionListener(this);
        vue.btnModifierPlaque.addActionListener(this);
        
        // Ajouter ItemListener pour le recalcul automatique du coût
        vue.comboZone.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    recalculerCout();
                }
            }
        });
        
        vue.comboHeures.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    recalculerCout();
                }
            }
        });
        
        vue.comboMinutes.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    recalculerCout();
                }
            }
        });
        
        // Ajouter ItemListener aux radio buttons
        vue.radioVoiture.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    recalculerCout();
                }
            }
        });
        
        vue.radioMoto.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    recalculerCout();
                }
            }
        });
        
        vue.radioCamion.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    recalculerCout();
                }
            }
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = getActionBouton((JButton) e.getSource());
        
        System.out.println("Action: " + action + " - État: " + etat);
        
        switch (etat) {
            case SAISIE:
                if (action.equals("ANNULER")) {
                    etat = EtatVoirie.REDIRECTION;
                    annuler();
                } else if (action.equals("VALIDER")) {
                    etat = EtatVoirie.VALIDATION;
                    validerStationnement();
                } else if (action.equals("MODIFIER_PLAQUE")) {
                    modifierPlaque();
                }
                break;
                
            case VALIDATION:
                // État intermédiaire pour validation
                break;
                
            case PREPARATION:
                // État intermédiaire pour préparation
                break;
                
            case REDIRECTION:
                // Ne rien faire
                break;
        }
    }
    
    private void validerStationnement() {
        // Validation des données
        String plaque = vue.lblPlaque.getText();
        if ("Non définie".equals(plaque) || plaque.trim().isEmpty()) {
            JOptionPane.showMessageDialog(vue,
                "Veuillez définir une plaque d'immatriculation",
                "Plaque manquante",
                JOptionPane.ERROR_MESSAGE);
            etat = EtatVoirie.SAISIE;
            return;
        }
        
        if (!controleurStationnement.validerPlaque(plaque)) {
            JOptionPane.showMessageDialog(vue,
                "Format de plaque invalide. Utilisez AA-123-AA",
                "Erreur de plaque",
                JOptionPane.ERROR_MESSAGE);
            etat = EtatVoirie.SAISIE;
            return;
        }
        
        int indexZone = vue.comboZone.getSelectedIndex();
        if (indexZone < 0) {
            JOptionPane.showMessageDialog(vue,
                "Veuillez sélectionner une zone",
                "Zone manquante",
                JOptionPane.ERROR_MESSAGE);
            etat = EtatVoirie.SAISIE;
            return;
        }
        
        // Récupérer les valeurs
        String typeVehicule = getTypeVehicule();
        String idZone = vue.zones.get(indexZone).getIdZone();
        String nomZone = vue.zones.get(indexZone).getLibelleZone();
        int heures = Integer.parseInt(vue.comboHeures.getSelectedItem().toString());
        int minutes = Integer.parseInt(vue.comboMinutes.getSelectedItem().toString());
        
        // Calculer le coût final
        int dureeTotaleMinutes = (heures * 60) + minutes;
        double cout = vue.zones.get(indexZone).calculerCout(dureeTotaleMinutes);
        
        // Vérifier si c'est une zone bleue avec stationnement gratuit
        boolean estZoneBleueGratuite = idZone.equals("ZONE_BLEUE") && cout == 0.00;
        
        // Demander confirmation
        String messageConfirmation = "Confirmez-vous le stationnement ?\n\n" +
            "Type de véhicule: " + typeVehicule + "\n" +
            "Plaque: " + plaque + "\n" +
            "Zone: " + nomZone + "\n" +
            "Durée: " + heures + "h" + minutes + "min\n" +
            "Coût: " + String.format("%.2f", cout) + " €";
        
        if (estZoneBleueGratuite) {
            messageConfirmation += "\n\n✅ Ce stationnement est GRATUIT !";
        }
        
        int confirmation = JOptionPane.showConfirmDialog(vue,
            messageConfirmation,
            "Confirmation de stationnement",
            JOptionPane.YES_NO_OPTION);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            etat = EtatVoirie.PREPARATION;
            
            // Si zone bleue gratuite, enregistrer directement sans paiement
            if (estZoneBleueGratuite) {
                enregistrerStationnementGratuit(typeVehicule, plaque, idZone, nomZone, heures, minutes);
            } else {
                preparerStationnement(typeVehicule, plaque, idZone, nomZone, heures, minutes, cout);
            }
        } else {
            etat = EtatVoirie.SAISIE;
        }
    }
    
    /**
     * Enregistre directement un stationnement gratuit sans passer par la page de paiement
     */
    private void enregistrerStationnementGratuit(String typeVehicule, String plaque, 
                                                 String idZone, String nomZone, 
                                                 int heures, int minutes) {
        System.out.println("=== ENREGISTREMENT STATIONNEMENT GRATUIT ZONE BLEUE ===");
        
        // Créer le stationnement directement via le DAO
        boolean succes = controleurStationnement.creerStationnementVoirieGratuit(
            typeVehicule,
            plaque,
            idZone,
            heures,
            minutes
        );
        
        if (succes) {
            JOptionPane.showMessageDialog(vue,
                "✅ Stationnement gratuit activé avec succès !\n\n" +
                "Votre stationnement en Zone Bleue est maintenant actif.\n" +
                "Durée: " + heures + "h" + minutes + "min",
                "Stationnement activé",
                JOptionPane.INFORMATION_MESSAGE);
            
            etat = EtatVoirie.REDIRECTION;
            retourPagePrincipale();
        } else {
            JOptionPane.showMessageDialog(vue,
                "❌ Une erreur est survenue lors de l'activation du stationnement.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            etat = EtatVoirie.SAISIE;
        }
    }
    
    private void preparerStationnement(String typeVehicule, String plaque, String idZone, 
                                      String nomZone, int heures, int minutes, double cout) {
        
        // Utiliser le contrôleur de stationnement pour préparer le stationnement
        boolean succes = controleurStationnement.preparerStationnementVoirie(
            typeVehicule,
            plaque,
            idZone,
            heures,
            minutes,
            vue
        );
        
        if (succes) {
            etat = EtatVoirie.REDIRECTION;
            // La redirection est gérée par le contrôleur de stationnement
        } else {
            etat = EtatVoirie.SAISIE;
        }
    }
    
    private void modifierPlaque() {
        String nouvellePlaque = JOptionPane.showInputDialog(vue, 
            "Entrez la plaque d'immatriculation (format: AA-123-AA):", 
            vue.lblPlaque.getText());
        
        if (nouvellePlaque != null && !nouvellePlaque.trim().isEmpty()) {
            String plaqueNettoyee = nouvellePlaque.trim().toUpperCase();
            
            if (controleurStationnement.validerPlaque(plaqueNettoyee)) {
                vue.lblPlaque.setText(plaqueNettoyee);
            } else {
                JOptionPane.showMessageDialog(vue,
                    "Format de plaque invalide. Utilisez AA-123-AA",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void recalculerCout() {
        try {
            int heures = Integer.parseInt(vue.comboHeures.getSelectedItem().toString());
            int minutes = Integer.parseInt(vue.comboMinutes.getSelectedItem().toString());
            int dureeTotaleMinutes = (heures * 60) + minutes;
            
            int index = vue.comboZone.getSelectedIndex();
            if (index >= 0 && index < vue.zones.size()) {
                double cout = vue.zones.get(index).calculerCout(dureeTotaleMinutes);
                
                // Afficher "GRATUIT" si le coût est 0
                if (cout == 0.00) {
                    vue.lblCout.setText("GRATUIT");
                    vue.lblCout.setForeground(new java.awt.Color(0, 150, 0));
                } else {
                    vue.lblCout.setText(String.format("%.2f €", cout));
                    vue.lblCout.setForeground(java.awt.Color.BLACK);
                }
            }
        } catch (Exception e) {
            vue.lblCout.setText("0.00 €");
            vue.lblCout.setForeground(java.awt.Color.BLACK);
        }
    }
    
    private String getTypeVehicule() {
        if (vue.radioVoiture.isSelected()) return "Voiture";
        if (vue.radioMoto.isSelected()) return "Moto";
        return "Camion";
    }
    
    private void annuler() {
        etat = EtatVoirie.REDIRECTION;
        retourPagePrincipale();
    }
    
    private void retourPagePrincipale() {
        ihm.Page_Principale pagePrincipale = new ihm.Page_Principale(vue.emailUtilisateur);
        pagePrincipale.setVisible(true);
        vue.dispose();
    }
    
    private String getActionBouton(JButton b) {
        String texte = b.getText();
        if (texte != null) {
            if (texte.contains("Annuler")) {
                return "ANNULER";
            } else if (texte.contains("Valider")) {
                return "VALIDER";
            } else if (texte.contains("Modifier")) {
                return "MODIFIER_PLAQUE";
            }
        }
        return "INCONNU";
    }
}
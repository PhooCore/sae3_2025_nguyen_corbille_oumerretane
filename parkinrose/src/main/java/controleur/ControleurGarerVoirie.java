package controleur;

import ihm.Page_Garer_Voirie;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import modele.Usager;
import modele.dao.UsagerDAO;

public class ControleurGarerVoirie implements ActionListener {

    private enum EtatVoirie {
        INITIAL, SAISIE, VALIDATION, PREPARATION, REDIRECTION
    }

    private Page_Garer_Voirie vue;
    private EtatVoirie etat;
    private StationnementControleur controleurStationnement;
    private Usager usager;

    public ControleurGarerVoirie(Page_Garer_Voirie vue) {
        this.vue = vue;
        this.usager = UsagerDAO.getUsagerByEmail(vue.emailUtilisateur);
        this.controleurStationnement = new StationnementControleur(vue.emailUtilisateur);
        this.etat = EtatVoirie.INITIAL;
        configurerListeners();
        etat = EtatVoirie.SAISIE;
    }

    private void configurerListeners() {
        vue.btnAnnuler.addActionListener(this);
        vue.btnValider.addActionListener(this);
        vue.btnModifierPlaque.addActionListener(this);

        vue.comboZone.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                recalculerCout();
            }
        });

        vue.comboHeures.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                recalculerCout();
            }
        });

        vue.comboMinutes.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                recalculerCout();
            }
        });

        vue.radioVoiture.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                recalculerCout();
            }
        });

        vue.radioMoto.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                recalculerCout();
            }
        });

        vue.radioCamion.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                recalculerCout();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = getActionBouton((JButton) e.getSource());
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
                break;
            case PREPARATION:
                break;
            case REDIRECTION:
                break;
        }
    }

    private void validerStationnement() {
        String plaque = vue.lblPlaque.getText();
        if ("Non définie".equals(plaque) || plaque.trim().isEmpty()) {
            JOptionPane.showMessageDialog(vue, "Veuillez définir une plaque d'immatriculation", "Plaque manquante", JOptionPane.ERROR_MESSAGE);
            etat = EtatVoirie.SAISIE;
            return;
        }

        if (!controleurStationnement.validerPlaque(plaque)) {
            JOptionPane.showMessageDialog(vue, "Format de plaque invalide. Utilisez AA-123-AA", "Erreur de plaque", JOptionPane.ERROR_MESSAGE);
            etat = EtatVoirie.SAISIE;
            return;
        }

        int indexZone = vue.comboZone.getSelectedIndex();
        if (indexZone < 0) {
            JOptionPane.showMessageDialog(vue, "Veuillez sélectionner une zone", "Zone manquante", JOptionPane.ERROR_MESSAGE);
            etat = EtatVoirie.SAISIE;
            return;
        }

        String typeVehicule = getTypeVehicule();
        String idZone = vue.zones.get(indexZone).getIdZone();
        String nomZone = vue.zones.get(indexZone).getLibelleZone();
        int heures = Integer.parseInt(vue.comboHeures.getSelectedItem().toString());
        int minutes = Integer.parseInt(vue.comboMinutes.getSelectedItem().toString());

        double cout = vue.zones.get(indexZone).calculerCout((heures * 60) + minutes);

        // Appliquer le tarif de l'abonnement si l'usager en a un
        if (controleurStationnement.usagerAUnAbonnementActif(usager.getIdUsager())) {
            double tarifAbonnement = controleurStationnement.getTarifAbonnement(usager.getIdUsager());
            if (tarifAbonnement > 0) {
                cout = tarifAbonnement;
            } else if (tarifAbonnement == 0.0) {
                cout = 0.0;
            }
        }

        String messageConfirmation = "Confirmez-vous le stationnement ?\n\n"
            + "Type de véhicule: " + typeVehicule + "\n"
            + "Plaque: " + plaque + "\n"
            + "Zone: " + nomZone + "\n"
            + "Durée: " + heures + "h" + minutes + "min\n"
            + "Coût: " + String.format("%.2f", cout) + " €";

        if (cout == 0.00) {
            messageConfirmation += "\n\n✅ Ce stationnement est GRATUIT !";
        }

        int confirmation = JOptionPane.showConfirmDialog(vue, messageConfirmation, "Confirmation de stationnement", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            etat = EtatVoirie.PREPARATION;
            if (cout == 0.00) {
                enregistrerStationnementGratuit(typeVehicule, plaque, idZone, nomZone, heures, minutes);
            } else {
                preparerStationnement(typeVehicule, plaque, idZone, nomZone, heures, minutes, cout);
            }
        } else {
            etat = EtatVoirie.SAISIE;
        }
    }

    private void enregistrerStationnementGratuit(String typeVehicule, String plaque, String idZone, String nomZone, int heures, int minutes) {
        boolean succes = controleurStationnement.preparerStationnementVoirie(typeVehicule, plaque, idZone, heures, minutes, vue);
        if (succes) {
            JOptionPane.showMessageDialog(vue, "✅ Stationnement gratuit activé avec succès !", "Stationnement activé", JOptionPane.INFORMATION_MESSAGE);
            etat = EtatVoirie.REDIRECTION;
            retourPagePrincipale();
        } else {
            JOptionPane.showMessageDialog(vue, "❌ Une erreur est survenue.", "Erreur", JOptionPane.ERROR_MESSAGE);
            etat = EtatVoirie.SAISIE;
        }
    }

    private void preparerStationnement(String typeVehicule, String plaque, String idZone, String nomZone, int heures, int minutes, double cout) {
        boolean succes = controleurStationnement.preparerStationnementVoirie(typeVehicule, plaque, idZone, heures, minutes, vue);
        if (succes) {
            etat = EtatVoirie.REDIRECTION;
        } else {
            etat = EtatVoirie.SAISIE;
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

                // Appliquer le tarif de l'abonnement si l'usager en a un
                if (controleurStationnement.usagerAUnAbonnementActif(usager.getIdUsager())) {
                    double tarifAbonnement = controleurStationnement.getTarifAbonnement(usager.getIdUsager());
                    if (tarifAbonnement > 0) {
                        cout = tarifAbonnement;
                    } else if (tarifAbonnement == 0.0) {
                        cout = 0.0;
                    }
                }

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

    private void modifierPlaque() {
        String nouvellePlaque = JOptionPane.showInputDialog(vue, "Entrez la plaque d'immatriculation (format: AA-123-AA):", vue.lblPlaque.getText());
        if (nouvellePlaque != null && !nouvellePlaque.trim().isEmpty()) {
            String plaqueNettoyee = nouvellePlaque.trim().toUpperCase();
            if (controleurStationnement.validerPlaque(plaqueNettoyee)) {
                vue.lblPlaque.setText(plaqueNettoyee);
            } else {
                JOptionPane.showMessageDialog(vue, "Format de plaque invalide. Utilisez AA-123-AA", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
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
            if (texte.contains("Annuler")) return "ANNULER";
            if (texte.contains("Valider")) return "VALIDER";
            if (texte.contains("Modifier")) return "MODIFIER_PLAQUE";
        }
        return "INCONNU";
    }
}

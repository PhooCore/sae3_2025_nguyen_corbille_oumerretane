package controleur;

import ihm.Page_Abonnements;
import ihm.Page_Utilisateur;
import ihm.Page_Paiement_Abonnement;
import modele.Abonnement;
import modele.Usager;
import modele.dao.AbonnementDAO;
import modele.dao.UsagerDAO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class ControleurAbonnements implements ActionListener {
    
    // États du contrôleur
    private enum Etat {
        INITIAL,
        CHARGEMENT_ABONNEMENTS,
        AFFICHAGE_ABONNEMENTS,
        SELECTION_ABONNEMENT,
        CONFIRMATION_REMPLACEMENT,
        TRAITEMENT_ABONNEMENT,
        REDIRECTION_PAIEMENT,
        ERREUR
    }
    
    // Références
    private Page_Abonnements vue;
    private Etat etat;
    
    // Données
    private String emailUtilisateur;
    private int idUsager;
    private Usager usager;
    private List<Abonnement> abonnementsDisponibles;
    private List<Abonnement> abonnementsFiltres;
    private Abonnement abonnementSelectionne;
    
    public ControleurAbonnements(Page_Abonnements vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.etat = Etat.INITIAL;
        
        initialiserControleur();
    }
    
    private void initialiserControleur() {
        chargerUtilisateur();
        configurerListeners();
    }
    
    private void chargerUtilisateur() {
        try {
            this.usager = UsagerDAO.getInstance().findById(emailUtilisateur);
            if (usager != null) {
                this.idUsager = usager.getIdUsager();
                chargerAbonnements();
            } else {
                afficherErreur("Utilisateur non trouvé");
                vue.dispose();
            }
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement: " + e.getMessage());
            vue.dispose();
        }
    }
    
    private void configurerListeners() {
        // Bouton retour
        vue.getBtnRetour().addActionListener(this);
        
        // Bouton recherche
        vue.getRechercheBtn().addActionListener(this);
        
        // Checkboxes de filtrage
        vue.getCheckGratuit().addActionListener(this);
        vue.getCheckMoto().addActionListener(this);
        vue.getCheckAnnuel().addActionListener(this);
        vue.getCheckHebdo().addActionListener(this);
        
        // ComboBox de tri
        vue.getComboTri().addActionListener(this);
        
        // Barre de recherche (Entrée)
        vue.getTxtRechercher().addActionListener(this);
    }
    
    private void chargerAbonnements() {
        etat = Etat.CHARGEMENT_ABONNEMENTS;
        
        SwingWorker<List<Abonnement>, Void> worker = new SwingWorker<List<Abonnement>, Void>() {
            @Override
            protected List<Abonnement> doInBackground() throws Exception {
                return AbonnementDAO.getInstance().findAll();
            }
            
            @Override
            protected void done() {
                try {
                    abonnementsDisponibles = get();
                    abonnementsFiltres = abonnementsDisponibles;
                    etat = Etat.AFFICHAGE_ABONNEMENTS;
                    vue.mettreAJourTitre(abonnementsFiltres.size());
                    
                    // Configurer les listeners des boutons d'abonnements
                    configurerListenersAbonnements();
                    
                } catch (Exception e) {
                    etat = Etat.ERREUR;
                    afficherErreur("Erreur de chargement des abonnements");
                }
            }
        };
        
        worker.execute();
    }
    
    private void configurerListenersAbonnements() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Attendre que les abonnements soient affichés
                Thread.sleep(100);
                return null;
            }
            
            @Override
            protected void done() {
                for (Abonnement abonnement : abonnementsFiltres) {
                    JButton btn = trouverBoutonAbonnement(abonnement);
                    if (btn != null) {
                        btn.addActionListener(e -> gererSelectionAbonnement(abonnement));
                    }
                }
            }
        };
        worker.execute();
    }
    
    private JButton trouverBoutonAbonnement(Abonnement abonnement) {
        return trouverBoutonRecursif(vue.getPanelAbonnements(), abonnement.getLibelleAbonnement());
    }
    
    private JButton trouverBoutonRecursif(java.awt.Container container, String libelle) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if (btn.getText().contains("Choisir cet abonnement")) {
                    java.awt.Container parent = btn.getParent();
                    if (parent != null) {
                        for (java.awt.Component compParent : parent.getComponents()) {
                            if (compParent instanceof JLabel) {
                                JLabel label = (JLabel) compParent;
                                if (label.getText().equals(libelle)) {
                                    return btn;
                                }
                            }
                        }
                    }
                }
            } else if (comp instanceof java.awt.Container) {
                JButton result = trouverBoutonRecursif((java.awt.Container) comp, libelle);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        // Identifier l'action
        String action = "INCONNU";
        
        if (source == vue.getBtnRetour()) {
            action = "RETOUR";
        } else if (source == vue.getRechercheBtn()) {
            action = "FILTRER";
        } else if (source == vue.getTxtRechercher()) {
            action = "FILTRER";
        } else if (source == vue.getCheckGratuit() || 
                   source == vue.getCheckMoto() || 
                   source == vue.getCheckAnnuel() || 
                   source == vue.getCheckHebdo()) {
            action = "FILTRER";
        } else if (source == vue.getComboTri()) {
            action = "FILTRER";
        }
        
        // Traiter selon l'état actuel
        switch (etat) {
            case AFFICHAGE_ABONNEMENTS:
                if (action.equals("RETOUR")) {
                    retourProfil();
                } else if (action.equals("FILTRER")) {
                    appliquerFiltres();
                }
                break;
                
            case ERREUR:
                if (action.equals("RETOUR")) {
                    retourProfil();
                }
                break;
        }
    }
    
    private void appliquerFiltres() {
        if (abonnementsDisponibles == null) return;
        
        // Récupérer les valeurs des filtres
        String rechercheTexte = vue.getRechercheTexte();
        boolean filtreGratuit = vue.isCheckGratuitSelected();
        boolean filtreMoto = vue.isCheckMotoSelected();
        boolean filtreAnnuel = vue.isCheckAnnuelSelected();
        boolean filtreHebdo = vue.isCheckHebdoSelected();
        String triSelectionne = vue.getTriSelectionne();
        
        // Appliquer les filtres
        abonnementsFiltres = new java.util.ArrayList<>(abonnementsDisponibles);
        
        // Recherche textuelle
        if (!rechercheTexte.isEmpty() && !rechercheTexte.equals("Rechercher un abonnement...")) {
            String rechercheLower = rechercheTexte.toLowerCase();
            abonnementsFiltres.removeIf(a -> 
                !a.getLibelleAbonnement().toLowerCase().contains(rechercheLower) &&
                !a.getIdAbonnement().toLowerCase().contains(rechercheLower)
            );
        }
        
        // Filtres par catégorie
        if (filtreGratuit) {
            abonnementsFiltres.removeIf(a -> a.getTarifAbonnement() > 0);
        }
        
        if (filtreMoto) {
            abonnementsFiltres.removeIf(a -> !a.getIdAbonnement().toUpperCase().contains("MOTO"));
        }
        
        if (filtreAnnuel) {
            abonnementsFiltres.removeIf(a -> !a.getIdAbonnement().toUpperCase().contains("ANNUEL"));
        }
        
        if (filtreHebdo) {
            abonnementsFiltres.removeIf(a -> !a.getIdAbonnement().toUpperCase().contains("HEBDO") 
                                          && !a.getIdAbonnement().toUpperCase().contains("SEMAINE"));
        }
        
        // Appliquer le tri
        switch (triSelectionne) {
            case "Prix croissant":
                abonnementsFiltres.sort(java.util.Comparator.comparingDouble(Abonnement::getTarifAbonnement));
                break;
            case "Prix décroissant":
                abonnementsFiltres.sort(java.util.Comparator.comparingDouble(Abonnement::getTarifAbonnement).reversed());
                break;
            case "Ordre alphabétique (A-Z)":
                abonnementsFiltres.sort(java.util.Comparator.comparing(Abonnement::getLibelleAbonnement));
                break;
            case "Ordre alphabétique (Z-A)":
                abonnementsFiltres.sort(java.util.Comparator.comparing(Abonnement::getLibelleAbonnement).reversed());
                break;
        }
        
        // Mettre à jour la vue
        vue.mettreAJourAffichageAbonnements(abonnementsFiltres);
        vue.mettreAJourTitre(abonnementsFiltres.size());
        
        // Reconfigurer les listeners des boutons
        configurerListenersAbonnements();
    }
    
    private void gererSelectionAbonnement(Abonnement abonnement) {
        this.abonnementSelectionne = abonnement;
        etat = Etat.SELECTION_ABONNEMENT;
        
        SwingWorker<List<Abonnement>, Void> worker = new SwingWorker<List<Abonnement>, Void>() {
            @Override
            protected List<Abonnement> doInBackground() throws Exception {
                return AbonnementDAO.getInstance().getAbonnementsByUsager(idUsager);
            }
            
            @Override
            protected void done() {
                try {
                    List<Abonnement> abonnementsExistants = get();
                    
                    if (!abonnementsExistants.isEmpty()) {
                        etat = Etat.CONFIRMATION_REMPLACEMENT;
                        demanderConfirmationRemplacement(abonnementsExistants.get(0));
                    } else {
                        if (abonnementSelectionne.getTarifAbonnement() == 0) {
                            demanderConfirmationAbonnementGratuit();
                        } else {
                            etat = Etat.REDIRECTION_PAIEMENT;
                            redirigerVersPaiement();
                        }
                    }
                } catch (Exception e) {
                    afficherErreur("Erreur: " + e.getMessage());
                    etat = Etat.ERREUR;
                }
            }
        };
        worker.execute();
    }
    
    private void demanderConfirmationAbonnementGratuit() {
        int confirmation = JOptionPane.showConfirmDialog(
            vue,
            "Voulez-vous vraiment souscrire à l'abonnement gratuit :\n\n" +
            "\"" + abonnementSelectionne.getLibelleAbonnement() + "\"\n" +
            "Code : " + abonnementSelectionne.getIdAbonnement() + "\n\n" +
            "Cet abonnement sera activé immédiatement sans frais.",
            "Confirmation d'abonnement gratuit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmation == JOptionPane.YES_OPTION) {
            souscrireAbonnementGratuit();
        } else {
            etat = Etat.AFFICHAGE_ABONNEMENTS;
        }
    }
    
    private void demanderConfirmationRemplacement(Abonnement abonnementExistant) {
        Object[] options = {"Remplacer", "Conserver mon abonnement actuel"};
        int choix = JOptionPane.showOptionDialog(
            vue,
            "<html><div style='text-align: center;'>"
            + "<h3>Abonnement existant détecté</h3>"
            + "<p>Vous avez déjà un abonnement actif :</p>"
            + "<p><b>" + abonnementExistant.getLibelleAbonnement() + "</b></p>"
            + "<br>"
            + "<p>Souhaitez-vous le remplacer par :</p>"
            + "<p><b>" + abonnementSelectionne.getLibelleAbonnement() + "</b> ?</p>"
            + "</div></html>",
            "Confirmation de remplacement",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]
        );
        
        if (choix == JOptionPane.YES_OPTION) {
            if (abonnementSelectionne.getTarifAbonnement() == 0) {
                souscrireAbonnementGratuit();
            } else {
                etat = Etat.REDIRECTION_PAIEMENT;
                redirigerVersPaiement();
            }
        } else {
            etat = Etat.AFFICHAGE_ABONNEMENTS;
        }
    }
    
    private void souscrireAbonnementGratuit() {
        etat = Etat.TRAITEMENT_ABONNEMENT;
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return AbonnementDAO.getInstance().ajouterAbonnementUtilisateur(
                    idUsager, 
                    abonnementSelectionne.getIdAbonnement()
                );
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    
                    if (success) {
                        JOptionPane.showMessageDialog(
                            vue,
                            "Abonnement souscrit avec succès !\n" +
                            "Votre abonnement \"" + abonnementSelectionne.getLibelleAbonnement() + 
                            "\" est maintenant actif.",
                            "Abonnement activé",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        retourProfil();
                    } else {
                        JOptionPane.showMessageDialog(
                            vue,
                            "❌ Une erreur est survenue lors de la souscription.",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE
                        );
                        etat = Etat.AFFICHAGE_ABONNEMENTS;
                    }
                } catch (Exception e) {
                    afficherErreur("Erreur: " + e.getMessage());
                    etat = Etat.ERREUR;
                }
            }
        };
        worker.execute();
    }
    
    private void redirigerVersPaiement() {
        Page_Paiement_Abonnement pagePaiement = new Page_Paiement_Abonnement(
            emailUtilisateur, 
            abonnementSelectionne
        );
        pagePaiement.setVisible(true);
        vue.dispose();
    }
    
    private void retourProfil() {
        Page_Utilisateur pageUtilisateur = new Page_Utilisateur(emailUtilisateur, true);
        pageUtilisateur.setVisible(true);
        vue.dispose();
    }
    
    private void afficherErreur(String message) {
        JOptionPane.showMessageDialog(
            vue,
            message,
            "Erreur",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    // Getters pour le débogage
    public Etat getEtat() {
        return etat;
    }
    
    public int getNombreAbonnements() {
        return abonnementsDisponibles != null ? abonnementsDisponibles.size() : 0;
    }
}
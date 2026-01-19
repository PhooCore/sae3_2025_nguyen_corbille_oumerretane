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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class ControleurAbonnements implements ActionListener {
    
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
    
    private Page_Abonnements vue;
    private Etat etat;
    
    // informations sur l'utilisateur connecté
    private String emailUtilisateur;
    private int idUsager;
    private Usager usager;
    
    // listes pour gérer les abonnements affichés
    private List<Abonnement> abonnementsDisponibles;
    private List<Abonnement> abonnementsFiltres;
    private Abonnement abonnementSelectionne;
    
    public ControleurAbonnements(Page_Abonnements vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.etat = Etat.INITIAL;
        
        initialiserControleur();
    }
    
    // récupère l'utilisateur depuis la base de données et configure la page
    private void initialiserControleur() {
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        
        // vérifier si l'utilisateur existe avec son mail
        // connecter les différents boutons de la vue aux actions du controleur
        // récupérer les abonnements de l'usager
        if (usager != null) {
            this.idUsager = usager.getIdUsager();
            configurerListeners();
            chargerAbonnements();
        } else {
            afficherErreur("Utilisateur non trouvé");
            vue.dispose();
        }
    }
    
    // connecte tous les boutons et éléments interactifs de la vue aux actions appropriées
    private void configurerListeners() {
    	// récupérer le bouton de la vue 
    	// ajouter un listener qui réagit aux clics
    	// si cliqué : on retourne à la page de l'utilisateur
    	vue.getBtnRetour().addActionListener(e -> retourProfil());
    	
        // appliquer les filtres de recherche
        // réagir aux différentes options de filtrage pour relancer le filtrages
        vue.getRechercheBtn().addActionListener(e -> appliquerFiltres());
        vue.getCheckGratuit().addActionListener(e -> appliquerFiltres());
        vue.getCheckMoto().addActionListener(e -> appliquerFiltres());
        vue.getCheckAnnuel().addActionListener(e -> appliquerFiltres());
        vue.getCheckHebdo().addActionListener(e -> appliquerFiltres());
        // ---------------------------------------------------------------------------------------
        vue.getComboTri().addActionListener(e -> appliquerFiltres());
        vue.getTxtRechercher().addActionListener(e -> appliquerFiltres());
        
        // gestion du placeholder dans le champ de recherche
        vue.getTxtRechercher().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (vue.getTxtRechercher().getText().equals("Rechercher un abonnement...")) {
                    vue.getTxtRechercher().setText("");
                    vue.getTxtRechercher().setForeground(java.awt.Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (vue.getTxtRechercher().getText().isEmpty()) {
                    vue.getTxtRechercher().setText("Rechercher un abonnement...");
                    vue.getTxtRechercher().setForeground(java.awt.Color.GRAY);
                }
            }
        });
    }
    
    // charge tous les abonnements disponibles depuis la base de données
    private void chargerAbonnements() {
        etat = Etat.CHARGEMENT_ABONNEMENTS;
        
        SwingWorker<List<Abonnement>, Void> worker = new SwingWorker<List<Abonnement>, Void>() {
            @Override
            protected List<Abonnement> doInBackground() throws Exception {
                try {
                    return AbonnementDAO.getInstance().findAll();
                } catch (SQLException e) {
                    throw new Exception("Erreur de chargement des abonnements: " + e.getMessage());
                }
            }
            
            @Override
            protected void done() {
                try {
                    abonnementsDisponibles = get();
                    abonnementsFiltres = new ArrayList<>(abonnementsDisponibles);
                    etat = Etat.AFFICHAGE_ABONNEMENTS;
                    
                    vue.mettreAJourTitre(abonnementsFiltres.size());
                    
                    afficherAbonnements();
                    
                } catch (Exception e) {
                    etat = Etat.ERREUR;
                    afficherErreur("Erreur de chargement des abonnements");
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    private void afficherAbonnements() {
        vue.getPanelAbonnements().removeAll();
        
        for (Abonnement abonnement : abonnementsFiltres) {
            JPanel carte = creerCarteAbonnement(abonnement);
            vue.getPanelAbonnements().add(carte);
        }
        
        if (abonnementsFiltres.isEmpty()) {
            String rechercheTexte = vue.getRechercheTexte();
            String message;
            
            if (!rechercheTexte.isEmpty() && !rechercheTexte.equals("Rechercher un abonnement...")) {
                message = "Aucun abonnement ne correspond à \"" + rechercheTexte + "\"";
            } else {
                message = "Aucun abonnement ne correspond à vos critères de filtrage";
            }
            
            JLabel lblAucun = new JLabel(message + "\nTentez d'autres critères de recherche", SwingConstants.CENTER);
            lblAucun.setFont(new java.awt.Font("Arial", java.awt.Font.ITALIC, 16));
            lblAucun.setForeground(java.awt.Color.GRAY);
            lblAucun.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
            vue.getPanelAbonnements().add(lblAucun);
        }
        
        vue.getPanelAbonnements().revalidate();
        vue.getPanelAbonnements().repaint();
    }
    
    private JPanel creerCarteAbonnement(Abonnement abonnement) {
        JPanel carte = new JPanel(new java.awt.BorderLayout(15, 10));
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        carte.setBackground(java.awt.Color.WHITE);
        
        JPanel panelInfo = new JPanel(new java.awt.GridLayout(0, 1, 5, 5));
        panelInfo.setBackground(java.awt.Color.WHITE);
        
        JLabel lblTitre = new JLabel(abonnement.getLibelleAbonnement());
        lblTitre.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        lblTitre.setForeground(new java.awt.Color(0, 100, 200));
        
        JLabel lblTarif = new JLabel(String.format("%.2f €", abonnement.getTarifAbonnement()));
        lblTarif.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        
        if (abonnement.getTarifAbonnement() == 0) {
            lblTarif.setForeground(new java.awt.Color(0, 180, 0));
            lblTarif.setText("GRATUIT");
        } else {
            lblTarif.setForeground(new java.awt.Color(0, 150, 0));
        }
        
        JLabel lblId = new JLabel("Code : " + abonnement.getIdAbonnement());
        lblId.setFont(new java.awt.Font("Arial", java.awt.Font.ITALIC, 12));
        lblId.setForeground(java.awt.Color.GRAY);
        
        JPanel badgesPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        badgesPanel.setBackground(java.awt.Color.WHITE);
        
        String idUpper = abonnement.getIdAbonnement().toUpperCase();
        if (idUpper.contains("ANNUEL")) {
            JLabel badge = new JLabel("Annuel");
            badge.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 11));
            badge.setForeground(new java.awt.Color(0, 100, 200));
            badgesPanel.add(badge);
        }
        if (idUpper.contains("HEBDO") || idUpper.contains("SEMAINE")) {
            JLabel badge = new JLabel("Hebdomadaire");
            badge.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 11));
            badge.setForeground(new java.awt.Color(0, 100, 200));
            badgesPanel.add(badge);
        }
        if (idUpper.contains("MOTO")) {
            JLabel badge = new JLabel("Ⓜ Moto");
            badge.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 11));
            badge.setForeground(new java.awt.Color(100, 100, 100));
            badgesPanel.add(badge);
        }
        if (idUpper.contains("RESIDENT")) {
            JLabel badge = new JLabel("⛫ Résident");
            badge.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 11));
            badge.setForeground(new java.awt.Color(150, 75, 0));
            badgesPanel.add(badge);
        }
        if (idUpper.contains("ELECTRIQUE")) {
            JLabel badge = new JLabel("⚡ Électrique");
            badge.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 11));
            badge.setForeground(new java.awt.Color(0, 150, 0));
            badgesPanel.add(badge);
        }
        
        panelInfo.add(lblTitre);
        panelInfo.add(lblTarif);
        panelInfo.add(lblId);
        panelInfo.add(badgesPanel);
        
        JButton btnChoisir = new JButton("Choisir cet abonnement");
        btnChoisir.setActionCommand("SOUSCRIRE_" + abonnement.getIdAbonnement());
        
        boolean aDejaAbonnement = false;
        try {
            List<Abonnement> abonnementsUsager = AbonnementDAO.getInstance().getAbonnementsByUsager(idUsager);
            for (Abonnement abUsager : abonnementsUsager) {
                if (abUsager.getIdAbonnement().equals(abonnement.getIdAbonnement())) {
                    aDejaAbonnement = true;
                    break;
                }
            }
        } catch (SQLException e) {
        }
        
        if (aDejaAbonnement) {
            btnChoisir.setText("Déjà souscrit");
            btnChoisir.setEnabled(false);
            btnChoisir.setBackground(java.awt.Color.GRAY);
            btnChoisir.setForeground(java.awt.Color.WHITE);
        } else {
            btnChoisir.setBackground(new java.awt.Color(0, 120, 215));
            btnChoisir.setForeground(java.awt.Color.WHITE);
            
            btnChoisir.addActionListener(e -> gererSelectionAbonnement(abonnement));
        }
        
        btnChoisir.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        btnChoisir.setFocusPainted(false);
        
        JPanel panelBouton = new JPanel(new java.awt.BorderLayout());
        panelBouton.setBackground(java.awt.Color.WHITE);
        panelBouton.add(btnChoisir, java.awt.BorderLayout.CENTER);
        
        carte.add(panelInfo, java.awt.BorderLayout.CENTER);
        carte.add(panelBouton, java.awt.BorderLayout.EAST);
        
        return carte;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
    }
    
    private void appliquerFiltres() {
        if (abonnementsDisponibles == null) return;
        
        String rechercheTexte = vue.getRechercheTexte();
        boolean filtreGratuit = vue.isCheckGratuitSelected();
        boolean filtreMoto = vue.isCheckMotoSelected();
        boolean filtreAnnuel = vue.isCheckAnnuelSelected();
        boolean filtreHebdo = vue.isCheckHebdoSelected();
        String triSelectionne = vue.getTriSelectionne();
        
        abonnementsFiltres = new ArrayList<>(abonnementsDisponibles);
        
        if (!rechercheTexte.isEmpty() && !rechercheTexte.equals("Rechercher un abonnement...")) {
            String rechercheLower = rechercheTexte.toLowerCase();
            abonnementsFiltres.removeIf(a -> 
                !a.getLibelleAbonnement().toLowerCase().contains(rechercheLower) &&
                !a.getIdAbonnement().toLowerCase().contains(rechercheLower)
            );
        }
        
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
        
        switch (triSelectionne) {
            case "Prix croissant":
                abonnementsFiltres.sort(Comparator.comparingDouble(Abonnement::getTarifAbonnement));
                break;
            case "Prix décroissant":
                abonnementsFiltres.sort(Comparator.comparingDouble(Abonnement::getTarifAbonnement).reversed());
                break;
            case "Ordre alphabétique (A-Z)":
                abonnementsFiltres.sort(Comparator.comparing(Abonnement::getLibelleAbonnement));
                break;
            case "Ordre alphabétique (Z-A)":
                abonnementsFiltres.sort(Comparator.comparing(Abonnement::getLibelleAbonnement).reversed());
                break;
        }
        
        vue.mettreAJourTitre(abonnementsFiltres.size());
        afficherAbonnements();
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
            "Abonnement existant détecté\n\n"
            + "Vous avez déjà un abonnement actif :"
            + abonnementExistant.getLibelleAbonnement()
            + "\nSouhaitez-vous le remplacer par :"
            + abonnementSelectionne.getLibelleAbonnement(),
            "\nConfirmation de remplacement",
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
                            "Une erreur est survenue lors de la souscription.",
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
    

    public Etat getEtat() {
        return etat;
    }
    
    public int getNombreAbonnements() {
        return abonnementsDisponibles != null ? abonnementsDisponibles.size() : 0;
    }
}
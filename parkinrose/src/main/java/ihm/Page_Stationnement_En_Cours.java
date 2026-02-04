package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import modele.Abonnement;
import modele.Stationnement;
import modele.Usager;
import modele.Zone;
import modele.dao.AbonnementDAO;
import modele.dao.StationnementDAO;
import modele.dao.TarifParkingDAO;
import modele.dao.UsagerDAO;
import modele.dao.ZoneDAO;
import controleur.ControleurStationnementEnCours;
import utils.NotificationManager;
import utils.NotificationManager.NotificationListener;
import utils.NotificationManager.NotificationType;

public class Page_Stationnement_En_Cours extends JFrame implements NotificationListener {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Stationnement stationnementActif;
    private JPanel panelInfo;
    private JButton btnArreter;
    private JButton btnRetour;
    private JButton btnProlonger;


    /**
     * constructeur de la page de stationnement en cours
     */

    private Timer timerAlerte;
    private boolean alerteDejaAffichee = false;
    private boolean fenetreFermee = false;
    private NotificationManager notificationManager;
    
    public Page_Stationnement_En_Cours(String email) {
        this.emailUtilisateur = email;
        
        // Initialiser le gestionnaire de notifications
        notificationManager = NotificationManager.getInstance();
        notificationManager.addNotificationListener(this);
        
        initialisePage();
        
        new ControleurStationnementEnCours(this);
        
        // Vérifier immédiatement les notifications
        SwingUtilities.invokeLater(() -> {
            if (stationnementActif != null) {
                notificationManager.verifierStationnement(stationnementActif);
            }
        });
        
        // Démarrer le timer d'alerte
        demarrerTimerAlerte();
    }
    
    /**
     * initialise tous les composants graphiques de la page
     */
    private void initialisePage() {
        this.setTitle("Stationnement en cours");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(600, 500);
        this.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Titre avec icône de notification
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        
        JLabel lblTitre = new JLabel("Stationnement en cours", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Bouton de configuration des notifications
        JButton btnConfigNotifications = new JButton("🔔");
        btnConfigNotifications.setFont(new Font("Arial", Font.PLAIN, 14));
        btnConfigNotifications.setBackground(Color.WHITE);
        btnConfigNotifications.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        btnConfigNotifications.setToolTipText("Configuration des notifications");
        btnConfigNotifications.addActionListener(e -> ouvrirConfigurationNotifications());
        
        titlePanel.add(lblTitre, BorderLayout.CENTER);
        titlePanel.add(btnConfigNotifications, BorderLayout.EAST);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Panel d'informations
        panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setBackground(Color.WHITE);
        panelInfo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JScrollPane scrollPane = new JScrollPane(panelInfo);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel panelBoutons = new JPanel(new FlowLayout());
        btnRetour = new JButton("Retour");
        panelBoutons.add(btnRetour);
        

        // Bouton prolonger (visible uniquement pour voirie)

        // Bouton prolonger
        btnProlonger = new JButton("Prolonger le stationnement");
        btnProlonger.setBackground(new Color(0, 153, 255));
        btnProlonger.setForeground(Color.WHITE);
        btnProlonger.setFocusPainted(false);
        btnProlonger.addActionListener(e -> gérerProlongationStationnement());
        btnProlonger.setVisible(false);
        panelBoutons.add(btnProlonger);
        
        // Bouton arrêter
        btnArreter = new JButton("Arrêter le stationnement");
        btnArreter.setBackground(Color.RED);
        btnArreter.setForeground(Color.WHITE);
        btnArreter.setFocusPainted(false);
        btnArreter.addActionListener(e -> gérerArrêtStationnement());
        panelBoutons.add(btnArreter);
        
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
        
        // Charger les données initiales
        chargerStationnementActif();
        afficherInformationsStationnement();
    }
    
    /**
     * charge le stationnement actif de l'utilisateur depuis la base de données
     */
    private void ouvrirConfigurationNotifications() {
        Page_Configuration_Notifications configPage = new Page_Configuration_Notifications();
        configPage.setVisible(true);
    }
    
    // Getters pour le contrôleur
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }
    
    public Stationnement getStationnementActif() {
        return stationnementActif;
    }
    
    public JButton getBtnRetour() {
        return btnRetour;
    }
    
    public JButton getBtnArreter() {
        return btnArreter;
    }
    
    public JButton getBtnProlonger() {
        return btnProlonger;
    }
    
    // Méthodes appelées par le contrôleur
    public void chargerStationnementActif() {
        Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager != null) {
            List<Stationnement> stationnementsActifs = StationnementDAO.getStationnementsParStatut(usager.getIdUsager(), "ACTIF");
            if (!stationnementsActifs.isEmpty()) {
                stationnementActif = stationnementsActifs.get(0);
                alerteDejaAffichee = false;
            } else {
                stationnementActif = null;
                alerteDejaAffichee = false;
            }
        }
    }
    
    /**
     * affiche toutes les informations du stationnement actif dans le panneau
     */
    public void afficherInformationsStationnement() {
        panelInfo.removeAll();
        
        if (stationnementActif == null) {
            JLabel lblAucun = new JLabel("Aucun stationnement actif", SwingConstants.CENTER);
            lblAucun.setFont(new Font("Arial", Font.PLAIN, 16));
            lblAucun.setForeground(Color.GRAY);
            panelInfo.add(lblAucun);
            btnArreter.setEnabled(false);
            btnProlonger.setVisible(false);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            ajouterLigneInfo("Véhicule:", stationnementActif.getTypeVehicule() + " - " + stationnementActif.getPlaqueImmatriculation());
            
            if (stationnementActif.estVoirie()) {
                ajouterLigneInfo("Type:", "Voirie");
                ajouterLigneInfo("Zone:", stationnementActif.getIdTarification());
                ajouterLigneInfo("Début:", stationnementActif.getDateCreation().format(formatter));
                ajouterLigneInfo("Durée prévue:", stationnementActif.getDureeHeures() + "h" + stationnementActif.getDureeMinutes() + "min");
                ajouterLigneInfo("Coût:", String.format("%.2f", stationnementActif.getCout()) + " €");
                ajouterLigneInfo("Statut paiement:", stationnementActif.getStatutPaiement());
                
                if (stationnementActif.getDateFin() != null) {
                    long minutesRestantes = java.time.Duration.between(LocalDateTime.now(), stationnementActif.getDateFin()).toMinutes();
                    if (minutesRestantes > 0) {
                        long heures = minutesRestantes / 60;
                        long minutes = minutesRestantes % 60;
                        String tempsRestant = String.format("%dh%02d", heures, minutes);
                        JPanel ligneTemps = creerLigneInfoAlerte("Temps restant:", tempsRestant, minutesRestantes <= 10);
                        panelInfo.add(ligneTemps);
                        
                        // Afficher un avertissement visuel si moins de 10 minutes
                        if (minutesRestantes <= 10) {
                            ajouterAvertissementTempsRestant(minutesRestantes);
                        }
                    } else {
                        ajouterLigneInfo("Temps restant:", "Temps écoulé");
                        ajouterAvertissementTempsEcoule();
                    }
                }
                
                btnProlonger.setVisible(true);
                
            } else if (stationnementActif.estParking()) {
                ajouterLigneInfo("Type:", "Parking");
                String nomParking = getLibelleParkingFromId(stationnementActif.getIdTarification());
                ajouterLigneInfo("Parking:", nomParking != null ? nomParking : stationnementActif.getIdTarification());
                
                if (stationnementActif.getHeureArrivee() != null) {
                    ajouterLigneInfo("Arrivée:", stationnementActif.getHeureArrivee().format(formatter));
                    ajouterLigneInfo("Statut paiement:", stationnementActif.getStatutPaiement());
                    
                    long minutesEcoulees = java.time.Duration.between(stationnementActif.getHeureArrivee(), LocalDateTime.now()).toMinutes();
                    long heures = minutesEcoulees / 60;
                    long minutes = minutesEcoulees % 60;
                    ajouterLigneInfo("Temps écoulé:", String.format("%dh%02d", heures, minutes));
                    
                    try {
                        double coutEstime = TarifParkingDAO.calculerCoutParking(
                            stationnementActif.getHeureArrivee(), 
                            LocalDateTime.now(), 
                            stationnementActif.getIdTarification()
                        );
                        ajouterLigneInfo("Coût estimé:", String.format("%.2f €", coutEstime));
                    } catch (Exception e) {
                        ajouterLigneInfo("Coût estimé:", "Calcul en cours...");
                    }
                }
                
                btnProlonger.setVisible(false);
            }
            
            ajouterLigneInfo("Statut:", stationnementActif.getStatut());
            btnArreter.setEnabled(true);
            
            // Indicateur de notifications
            if (notificationManager.areNotificationsActives()) {
                ajouterIndicateurNotifications();
            }
        }
        
        panelInfo.revalidate();
        panelInfo.repaint();
    }
    
    /**
     * ajoute une ligne d'information avec un libellé et une valeur dans le panneau
     */
    private void ajouterLigneInfo(String libelle, String valeur) {
        JPanel ligne = new JPanel(new BorderLayout());
        ligne.setBackground(Color.WHITE);
        ligne.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JLabel lblLibelle = new JLabel(libelle);
        lblLibelle.setFont(new Font("Arial", Font.BOLD, 14));
        lblLibelle.setPreferredSize(new Dimension(150, 25));
        
        JLabel lblValeur = new JLabel(valeur);
        lblValeur.setFont(new Font("Arial", Font.PLAIN, 14));
        
        ligne.add(lblLibelle, BorderLayout.WEST);
        ligne.add(lblValeur, BorderLayout.CENTER);
        panelInfo.add(ligne);
    }
    
    /**
     * gère l'arrêt du stationnement en cours après confirmation de l'utilisateur
     */

    private JPanel creerLigneInfoAlerte(String libelle, String valeur, boolean alerte) {
        JPanel ligne = new JPanel(new BorderLayout());
        ligne.setBackground(Color.WHITE);
        ligne.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JLabel lblLibelle = new JLabel(libelle);
        lblLibelle.setFont(new Font("Arial", Font.BOLD, 14));
        lblLibelle.setPreferredSize(new Dimension(150, 25));
        
        JLabel lblValeur = new JLabel(valeur);
        lblValeur.setFont(new Font("Arial", Font.BOLD, 14));
        
        if (alerte) {
            lblValeur.setForeground(Color.RED);
        }
        
        ligne.add(lblLibelle, BorderLayout.WEST);
        ligne.add(lblValeur, BorderLayout.CENTER);
        return ligne;
    }
    
    private void ajouterAvertissementTempsRestant(long minutesRestantes) {
        JPanel panelAvertissement = new JPanel();
        panelAvertissement.setLayout(new BorderLayout());
        
        Color couleurFond;
        Color couleurBordure;
        String message;
        
        if (minutesRestantes <= 3) {
            couleurFond = new Color(255, 235, 238); // Rouge clair
            couleurBordure = new Color(244, 67, 54); // Rouge
            message = "⛔ URGENT : Il reste seulement " + minutesRestantes + " minute(s) !";
        } else if (minutesRestantes <= 5) {
            couleurFond = new Color(255, 243, 224); // Orange clair
            couleurBordure = new Color(255, 152, 0); // Orange
            message = "⚠ ATTENTION : Il reste " + minutesRestantes + " minutes";
        } else {
            couleurFond = new Color(232, 245, 233); // Vert clair
            couleurBordure = new Color(76, 175, 80); // Vert
            message = "⏰ RAPPEL : Il reste " + minutesRestantes + " minutes";
        }
        
        panelAvertissement.setBackground(couleurFond);
        panelAvertissement.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(couleurBordure, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel lblAvertissement = new JLabel(message);
        lblAvertissement.setFont(new Font("Arial", Font.BOLD, 13));
        lblAvertissement.setForeground(couleurBordure);
        
        panelAvertissement.add(lblAvertissement, BorderLayout.CENTER);
        
        // Ajouter un espace avant l'avertissement
        panelInfo.add(Box.createRigidArea(new Dimension(0, 15)));
        panelInfo.add(panelAvertissement);
        panelInfo.add(Box.createRigidArea(new Dimension(0, 15)));
    }
    
    private void ajouterAvertissementTempsEcoule() {
        JPanel panelAvertissement = new JPanel();
        panelAvertissement.setLayout(new BorderLayout());
        panelAvertissement.setBackground(new Color(255, 235, 238));
        panelAvertissement.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(244, 67, 54), 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel lblAvertissement = new JLabel(
            "<html><b>⛔ TEMPS ÉCOULÉ :</b> Votre stationnement est expiré !<br>" +
            "Veuillez arrêter votre stationnement ou renouveler.</html>");
        lblAvertissement.setFont(new Font("Arial", Font.BOLD, 13));
        lblAvertissement.setForeground(new Color(244, 67, 54));
        
        panelAvertissement.add(lblAvertissement, BorderLayout.CENTER);
        
        // Ajouter un espace avant l'avertissement
        panelInfo.add(Box.createRigidArea(new Dimension(0, 15)));
        panelInfo.add(panelAvertissement);
        panelInfo.add(Box.createRigidArea(new Dimension(0, 15)));
    }
    
    private void ajouterIndicateurNotifications() {
        JPanel panelNotifications = new JPanel();
        panelNotifications.setLayout(new BorderLayout());
        panelNotifications.setBackground(new Color(225, 245, 254));
        panelNotifications.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(3, 169, 244), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        JLabel lblNotifications = new JLabel("🔔 Notifications activées");
        lblNotifications.setFont(new Font("Arial", Font.PLAIN, 12));
        lblNotifications.setForeground(new Color(3, 169, 244));
        
        panelNotifications.add(lblNotifications, BorderLayout.CENTER);
        
        // Ajouter un espace avant l'indicateur
        panelInfo.add(Box.createRigidArea(new Dimension(0, 10)));
        panelInfo.add(panelNotifications);
        panelInfo.add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    // Méthode de gestion d'arrêt
    private void gérerArrêtStationnement() {
        if (stationnementActif == null) {
            JOptionPane.showMessageDialog(this,
                "Aucun stationnement actif à arrêter",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirmation = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir arrêter ce stationnement ?\n\n" +
            "Véhicule: " + stationnementActif.getTypeVehicule() + " - " + stationnementActif.getPlaqueImmatriculation() + "\n" +
            "Type: " + (stationnementActif.estVoirie() ? "Voirie" : "Parking"),
            "Confirmation d'arrêt",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            if (stationnementActif.estVoirie()) {
                arrêterStationnementVoirie();
            } else if (stationnementActif.estParking()) {
                demanderHeureDepartEtPayer();
            }
        }
    }
    
    /**
     * arrête un stationnement de voirie et termine la session
     */
    private void arrêterStationnementVoirie() {
        boolean succes = StationnementDAO.terminerStationnement(stationnementActif.getIdStationnement());
        
        if (succes) {
            arreterTousLesTimers();
            JOptionPane.showMessageDialog(this, 
                "Stationnement arrêté avec succès !", 
                "Succès", 
                JOptionPane.INFORMATION_MESSAGE);
            retourAccueil();
        } else {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de l'arrêt du stationnement",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * demande l'heure de départ et calcule le coût pour un stationnement parking
     */
    private void demanderHeureDepartEtPayer() {
    	
        if ("GRATUIT".equals(stationnementActif.getStatutPaiement())) {
            System.out.println("Stationnement GRATUIT");
            // Demander juste l'heure de départ
            JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
            
            JSpinner spinnerHeureDepart = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerHeureDepart, "dd/MM/yyyy HH:mm");
            spinnerHeureDepart.setEditor(editor);
            spinnerHeureDepart.setValue(new java.util.Date());
            
            panel.add(new JLabel("Heure de départ:"));
            panel.add(spinnerHeureDepart);
            
            int result = JOptionPane.showConfirmDialog(this, panel, 
                "Heure de départ", JOptionPane.OK_CANCEL_OPTION);
            
            if (result == JOptionPane.OK_OPTION) {
                java.util.Date dateDepart = (java.util.Date) spinnerHeureDepart.getValue();
                LocalDateTime heureDepart = dateDepart.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
                
                String nomParking = getLibelleParkingFromId(stationnementActif.getIdTarification());
                
                // Terminer directement sans paiement
                terminerStationnementParkingGratuit(heureDepart, 0.0, nomParking);
            }
            return; // IMPORTANT : sortir de la méthode
        }
        
        // RESTE DU CODE ORIGINAL POUR LES STATIONNEMENTS PAYANTS
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        JSpinner spinnerHeureDepart = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerHeureDepart, "dd/MM/yyyy HH:mm");
        spinnerHeureDepart.setEditor(editor);
        spinnerHeureDepart.setValue(new java.util.Date());
        
        panel.add(new JLabel("Heure de départ:"));
        panel.add(spinnerHeureDepart);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Heure de départ", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            java.util.Date dateDepart = (java.util.Date) spinnerHeureDepart.getValue();
            LocalDateTime heureDepart = dateDepart.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
            
            String nomParking = getLibelleParkingFromId(stationnementActif.getIdTarification());
            
            // Calculer le coût
            double cout = 0;
            try {
                cout = TarifParkingDAO.calculerCoutParking(
                    stationnementActif.getHeureArrivee(), 
                    heureDepart, 
                    stationnementActif.getIdTarification()
                );
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors du calcul du coût: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (Math.abs(cout) < 0.01) {
                // Terminer le stationnement directement (parking gratuit)
                terminerStationnementParkingGratuit(heureDepart, cout, nomParking);
            } else {
                Page_Paiement pagePaiement = new Page_Paiement(
                    cout,
                    emailUtilisateur,
                    stationnementActif.getTypeVehicule(),
                    stationnementActif.getPlaqueImmatriculation(),
                    stationnementActif.getIdTarification(),
                    nomParking != null ? nomParking : "Parking",
                    0, 
                    0,
                    stationnementActif.getIdStationnement(),
                    heureDepart
                );
                pagePaiement.setVisible(true);
                dispose();
            }
        }
    }
    
    /**
     * termine un stationnement parking gratuit sans passer par le paiement
     */
    private void terminerStationnementParkingGratuit(LocalDateTime heureDepart, double cout, String nomParking) {
        arreterTousLesTimers();
        
        boolean succes = StationnementDAO.terminerStationnementParking(
            stationnementActif.getIdStationnement(),
            heureDepart,
            cout,
            null
        );
        
        if (succes) {
            String message;
            if ("GRATUIT".equals(stationnementActif.getStatutPaiement())) {
                // Message spécifique pour abonnement moto
                message = String.format(
                    "✓ Stationnement terminé !\n\n" +
                    "Parking: %s\n" +
                    "Stationnement GRATUIT grâce à votre abonnement moto résident.\n\n" +
                    "Vous pouvez maintenant quitter le parking.",
                    nomParking != null ? nomParking : "Parking"
                );
            } else {
                message = "Stationnement terminé !\nParking gratuit";
            }
            
            JOptionPane.showMessageDialog(this,
                message,
                "Stationnement terminé - GRATUIT",
                JOptionPane.INFORMATION_MESSAGE);
            
            retourAccueil();
        } else {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la fin du stationnement",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * retourne le libellé lisible d'un parking à partir de son identifiant
     */
    private String getLibelleParkingFromId(String idParking) {
        if (idParking == null) return null;
        
        switch(idParking) {
            case "PARK_CAPITOLE": return "Parking Capitole";
            case "PARK_CARNOT": return "Parking Carnot";
            case "PARK_ESQUIROL": return "Parking Esquirol";
            case "PARK_SAINT_ETIENNE": return "Parking Saint-Étienne";
            case "PARK_JEAN_JAURES": return "Parking Jean Jaurès";
            case "PARK_JEANNE_DARC": return "Parking Jeanne d'Arc";
            case "PARK_EUROPE": return "Parking Europe";
            case "PARK_VICTOR_HUGO": return "Parking Victor Hugo";
            case "PARK_SAINT_AUBIN": return "Parking Saint-Aubin";
            case "PARK_SAINT_CYPRIEN": return "Parking Saint-Cyprien";
            case "PARK_SAINT_MICHEL": return "Parking Saint-Michel";
            case "PARK_MATABIAU": return "Parking Matabiau-Ramblas";
            case "PARK_ARNAUD_BERNARD": return "Parking Arnaud Bernard";
            case "PARK_CARMES": return "Parking Carmes";
            case "PARKING_DEFAULT": return "Parking";
            default: return idParking;
        }
    }
    
    /**
     * retourne à la page d'accueil principale
     */
    private void retourAccueil() {
        arreterTousLesTimers();
        Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
        pagePrincipale.setVisible(true);
        dispose();
    }
    
    /**
     * rafraîchit l'affichage en rechargeant les données du stationnement
     */
    public void rafraichirAffichage() {
        chargerStationnementActif();
        afficherInformationsStationnement();
        
        // Vérifier les notifications
        if (stationnementActif != null) {
            notificationManager.verifierStationnement(stationnementActif);
        }
    }
    
    /**
     * ferme la fenêtre et libère les ressources
     */
    // Implémentation de NotificationListener
    @Override
    public void onNotification(String titre, String message, NotificationType type) {
        // Afficher la notification via le gestionnaire (version simple)
        notificationManager.afficherPopupNotificationSimple(titre, message, type);
    }
    
    /**
     * gère la prolongation d'un stationnement en voirie avec sélection de durée
     */
    // PROLONGATION DU STATIONNEMENT
    private void gérerProlongationStationnement() {
        if (stationnementActif == null || !stationnementActif.estVoirie()) {
            JOptionPane.showMessageDialog(this,
                "Seuls les stationnements en voirie peuvent être prolongés",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            Zone zone = ZoneDAO.getInstance().getZoneById(stationnementActif.getIdTarification());
            if (zone == null) {
                JOptionPane.showMessageDialog(this,
                    "Impossible de trouver la zone de stationnement",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            JPanel infoZonePanel = new JPanel(new BorderLayout());
            infoZonePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 102, 204), 2),
                "Tarifs de la zone " + zone.getLibelleZone()
            ));
            
            JTextArea tarifInfo = new JTextArea(zone.getAffichage());
            tarifInfo.setEditable(false);
            tarifInfo.setBackground(new Color(240, 248, 255));
            tarifInfo.setFont(new Font("Arial", Font.PLAIN, 12));
            tarifInfo.setWrapStyleWord(true);
            tarifInfo.setLineWrap(true);
            tarifInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            infoZonePanel.add(tarifInfo, BorderLayout.CENTER);
            
            mainPanel.add(infoZonePanel, BorderLayout.NORTH);
            
            JPanel selectionPanel = new JPanel(new GridLayout(4, 2, 10, 10));
            selectionPanel.setBorder(BorderFactory.createTitledBorder("Durée supplémentaire"));
            
            selectionPanel.add(new JLabel("Durée à ajouter:"));
            selectionPanel.add(new JLabel(""));
            
            String[] heures = {"0", "1", "2", "3", "4", "5", "6", "7", "8"};
            String[] minutes = {"0", "15", "30", "45"};
            
            JComboBox<String> comboHeures = new JComboBox<>(heures);
            JComboBox<String> comboMinutes = new JComboBox<>(minutes);
            comboMinutes.setSelectedItem("15");
            
            JPanel dureePan = new JPanel(new FlowLayout(FlowLayout.LEFT));
            dureePan.add(comboHeures);
            dureePan.add(new JLabel("h"));
            dureePan.add(comboMinutes);
            dureePan.add(new JLabel("min"));
            
            selectionPanel.add(new JLabel("Durée:"));
            selectionPanel.add(dureePan);
            
            JLabel lblCoutCalcule = new JLabel("0.00 €");
            lblCoutCalcule.setFont(new Font("Arial", Font.BOLD, 14));
            lblCoutCalcule.setForeground(new Color(0, 100, 0));
            
            selectionPanel.add(new JLabel("Coût supplémentaire:"));
            selectionPanel.add(lblCoutCalcule);
            
            mainPanel.add(selectionPanel, BorderLayout.CENTER);
            
            ItemListener calculCoutListener = e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int h = Integer.parseInt((String) comboHeures.getSelectedItem());
                    int m = Integer.parseInt((String) comboMinutes.getSelectedItem());
                    int dureeMin = (h * 60) + m;
                    
                    if (dureeMin > 0) {
                        // Vérifier l'abonnement pour le calcul
                        Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
                        Abonnement abonnement = null;
                        if (usager != null) {
                            abonnement = AbonnementDAO.getAbonnementActifStatic(usager.getIdUsager());
                        }
                        
                        // Calculer avec abonnement
                        double cout;
                        if (abonnement != null) {
                            cout = zone.calculerCoutAvecAbonnement(dureeMin, abonnement);
                        } else {
                            cout = zone.calculerCout(dureeMin);
                        }
                        

                        if (cout == 0.0) {
                            lblCoutCalcule.setText("GRATUIT");
                            lblCoutCalcule.setForeground(new Color(0, 150, 0));
                        } else {
                            lblCoutCalcule.setText(String.format("%.2f €", cout));
                            lblCoutCalcule.setForeground(new Color(0, 100, 0));
                        }
                    } else {
                        lblCoutCalcule.setText("0.00 €");
                        lblCoutCalcule.setForeground(Color.GRAY);
                    }
                }
            };
            
            comboHeures.addItemListener(calculCoutListener);
            comboMinutes.addItemListener(calculCoutListener);
            
            int dureeInitiale = 15;
            Usager usagerInitial = UsagerDAO.getUsagerByEmail(emailUtilisateur);
            Abonnement abonnementInitial = null;
            if (usagerInitial != null) {
                abonnementInitial = AbonnementDAO.getAbonnementActifStatic(usagerInitial.getIdUsager());
            }
            
            double coutInitial;
            if (abonnementInitial != null) {
                coutInitial = zone.calculerCoutAvecAbonnement(dureeInitiale, abonnementInitial);
            } else {
                coutInitial = zone.calculerCout(dureeInitiale);
            }
            

            if (coutInitial == 0.0) {
                lblCoutCalcule.setText("GRATUIT");
                lblCoutCalcule.setForeground(new Color(0, 150, 0));
            } else {
                lblCoutCalcule.setText(String.format("%.2f €", coutInitial));
                lblCoutCalcule.setForeground(new Color(0, 100, 0));
            }
            
            int result = JOptionPane.showConfirmDialog(this, mainPanel,
                "Prolonger le stationnement", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                int heuresSupp = Integer.parseInt((String) comboHeures.getSelectedItem());
                int minutesSupp = Integer.parseInt((String) comboMinutes.getSelectedItem());
                
                if (heuresSupp == 0 && minutesSupp == 0) {
                    JOptionPane.showMessageDialog(this,
                        "Veuillez sélectionner une durée à ajouter",
                        "Durée invalide",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int dureeSupplementaireMinutes = (heuresSupp * 60) + minutesSupp;
                Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
                Abonnement abonnement = null;
                if (usager != null) {
                    abonnement = AbonnementDAO.getAbonnementActifStatic(usager.getIdUsager());
                }
                
                double coutSupplementaire;
                if (abonnement != null) {
                    coutSupplementaire = zone.calculerCoutAvecAbonnement(dureeSupplementaireMinutes, abonnement);
                } else {
                    coutSupplementaire = zone.calculerCout(dureeSupplementaireMinutes);
                }

                String messageConfirmation = String.format(
                    "Confirmez-vous la prolongation ?\n\n" +
                    "Durée supplémentaire: %dh%02dmin\n" +
                    "Coût supplémentaire: %.2f €\n\n" +
                    "Nouvelle fin de stationnement: %s",
                    heuresSupp, minutesSupp, coutSupplementaire,
                    stationnementActif.getDateFin().plusMinutes(dureeSupplementaireMinutes)
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
                
                int confirmation = JOptionPane.showConfirmDialog(this,
                    messageConfirmation,
                    "Confirmation de prolongation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (confirmation == JOptionPane.YES_OPTION) {
                    if (coutSupplementaire > 0.01) {
                        ouvrirPagePaiementProlongation(zone, heuresSupp, minutesSupp, 
                            dureeSupplementaireMinutes, coutSupplementaire);
                    } else {
                        prolongerStationnementGratuit(dureeSupplementaireMinutes);
                    }
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la prolongation: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * ouvre la page de paiement pour une prolongation de stationnement payante
     */
    private void ouvrirPagePaiementProlongation(Zone zone, int heures, int minutes, 
            int dureeMinutes, double cout) {
    	Page_Paiement pagePaiement = new Page_Paiement(cout, emailUtilisateur, stationnementActif.getTypeVehicule(),
    												stationnementActif.getPlaqueImmatriculation(), zone.getIdZone(),
    												zone.getLibelleZone(), heures, minutes, 
    												stationnementActif.getIdStationnement(), null);
    	pagePaiement.setVisible(true);
    	dispose();

    }

    /**
     * prolonge un stationnement gratuitement sans passer par le paiement
     */
    private void prolongerStationnementGratuit(int dureeSupplementaireMinutes) {
       	boolean succes = StationnementDAO.prolongerStationnement(
    			stationnementActif.getIdStationnement(), 
    			dureeSupplementaireMinutes
    			);

    	if (succes) {
    		JOptionPane.showMessageDialog(this,
    				"Stationnement prolongé avec succès !",
    				"Succès",
    		JOptionPane.INFORMATION_MESSAGE);
    		rafraichirAffichage();
    	} else {
    		JOptionPane.showMessageDialog(this,
    				"Erreur lors de la prolongation du stationnement",
    				"Erreur",
    				JOptionPane.ERROR_MESSAGE);
    	}

    }

    private void demarrerTimerAlerte() {
        timerAlerte = new Timer(60000, e -> {
            if (!fenetreFermee && isDisplayable()) {
                verifierAlerteTempsRestant();
            } else {
                if (timerAlerte != null) {
                    timerAlerte.stop();
                }
            }
        });
        timerAlerte.start();
    }
    
    private void arreterTousLesTimers() {
        if (timerAlerte != null) {
            timerAlerte.stop();
            timerAlerte = null;
        }
    }
    
    private void verifierAlerteTempsRestant() {
        if (stationnementActif != null && stationnementActif.estVoirie() && 
            stationnementActif.estActif() && !alerteDejaAffichee) {
            
            long minutesRestantes = java.time.Duration.between(LocalDateTime.now(), stationnementActif.getDateFin()).toMinutes();
            
            if (minutesRestantes > 0 && minutesRestantes <= 10) {
                afficherAlerteSimple(minutesRestantes);
            }
        }
    }
    
    private void afficherAlerteSimple(long minutesRestantes) {
        alerteDejaAffichee = true;
        
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                "<html><div style='text-align: center;'>" +
                "<b>⚠️ Attention ! ⚠️</b><br><br>" +
                "Il reste <font color='red'><b>" + minutesRestantes + " minute" + 
                (minutesRestantes > 1 ? "s" : "") + "</b></font> avant la fin de votre stationnement en voirie.<br><br>" +
                "<i>Véhicule: " + stationnementActif.getTypeVehicule() + 
                " - " + stationnementActif.getPlaqueImmatriculation() + "</i><br><br>" +
                "Pensez à arrêter votre stationnement à temps pour éviter une infraction." +
                "</div></html>",
                "Alerte - Stationnement en Voirie",
                JOptionPane.WARNING_MESSAGE);
        });
    }
    
    @Override
    public void dispose() {
        fenetreFermee = true;
        arreterTousLesTimers();
        
        // Nettoyer à la fermeture
        if (notificationManager != null) {
            notificationManager.removeNotificationListener(this);
        }
        super.dispose();
    }
}
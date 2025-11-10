package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.StationnementDAO;
import dao.TarifParkingDAO;
import dao.UsagerDAO;
import modèle.Stationnement;
import modèle.Usager;

/**
 * Page de gestion des stationnements en cours
 * Affiche les informations détaillées d'un stationnement actif et permet son arrêt
 * Gère à la fois les stationnements en voirie et en parking
 */
public class Page_Stationnement_En_Cours extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;          // Email de l'utilisateur connecté
    private Stationnement stationnementActif; // Stationnement actif à afficher
    private JPanel panelInfo;                 // Panel pour afficher les informations
    private JButton btnArreter;               // Bouton pour arrêter le stationnement
    private Timer timer;                      // Timer pour l'actualisation automatique

    /**
     * Constructeur de la page de gestion des stationnements en cours
     * @param email l'email de l'utilisateur connecté
     */
    public Page_Stationnement_En_Cours(String email) {
        this.emailUtilisateur = email;
        chargerStationnementActif(); // Charge le stationnement actif depuis la BDD
        initialisePage();            // Initialise l'interface
        startAutoRefresh();          // Démarre l'actualisation automatique
    }
    
    /**
     * Charge le stationnement actif de l'utilisateur depuis la base de données
     */
    private void chargerStationnementActif() {
        Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager != null) {
            // Récupère le stationnement actif valide (statut ACTIF et date non dépassée)
            stationnementActif = StationnementDAO.getStationnementActifValideByUsager(usager.getIdUsager());
        }
    }
    
    /**
     * Initialise l'interface utilisateur de la page
     * Structure : Titre + Informations détaillées + Boutons d'action
     */
    private void initialisePage() {
        // Configuration de la fenêtre
        this.setTitle("Stationnement en cours");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Ne ferme que cette fenêtre
        this.setSize(600, 500);
        this.setLocationRelativeTo(null); // Centre la fenêtre
        
        // Panel principal avec bordures
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // === TITRE DE LA PAGE ===
        JLabel lblTitre = new JLabel("Stationnement en cours", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // === PANEL D'INFORMATIONS DÉTAILLÉES ===
        panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS)); // Layout vertical
        panelInfo.setBackground(Color.WHITE);
        panelInfo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // Marge verticale
        
        // Ajout du panel dans un scroll pane pour permettre le défilement
        JScrollPane scrollPane = new JScrollPane(panelInfo);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // === BOUTONS D'ACTION ===
        JPanel panelBoutons = new JPanel(new FlowLayout());
        
        // Bouton retour à l'accueil
        JButton btnRetour = new JButton("Retour");
        btnRetour.addActionListener(e -> retourAccueil());
        
        // Bouton arrêter le stationnement (rouge)
        btnArreter = new JButton("Arrêter le stationnement");
        btnArreter.setBackground(Color.RED);
        btnArreter.setForeground(Color.WHITE);
        btnArreter.setFocusPainted(false); // Désactive l'effet de focus
        btnArreter.addActionListener(e -> arreterStationnement());
        
        panelBoutons.add(btnRetour);
        // N'affiche le bouton d'arrêt que si un stationnement actif existe
        if (stationnementActif != null) {
            panelBoutons.add(btnArreter);
        }
        
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
        afficherInformationsStationnement(); // Remplit le panel avec les informations
    }
    
    /**
     * Affiche les informations détaillées du stationnement en cours
     * Adapte l'affichage selon le type de stationnement (voirie ou parking)
     */
    private void afficherInformationsStationnement() {
        panelInfo.removeAll(); // Vide le panel avant de le remplir
        
        if (stationnementActif == null) {
            // === CAS : AUCUN STATIONNEMENT ACTIF ===
            JLabel lblAucun = new JLabel("Aucun stationnement actif", SwingConstants.CENTER);
            lblAucun.setFont(new Font("Arial", Font.PLAIN, 16));
            lblAucun.setForeground(Color.GRAY);
            panelInfo.add(lblAucun);
        } else {
            // === CAS : STATIONNEMENT ACTIF TROUVÉ ===
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            // Informations communes à tous les stationnements
            ajouterLigneInfo("Véhicule:", stationnementActif.getTypeVehicule() + " - " + stationnementActif.getPlaqueImmatriculation());
            
            if (stationnementActif.estVoirie()) {
                // === STATIONNEMENT EN VOIRIE ===
                ajouterLigneInfo("Type:", "Voirie");
                ajouterLigneInfo("Zone:", stationnementActif.getZone());
                ajouterLigneInfo("Début:", stationnementActif.getDateCreation().format(formatter));
                ajouterLigneInfo("Durée prévue:", stationnementActif.getDureeHeures() + "h" + stationnementActif.getDureeMinutes() + "min");
                ajouterLigneInfo("Coût:", String.format("%.2f", stationnementActif.getCout()) + " €");
                ajouterLigneInfo("Statut paiement:", stationnementActif.getStatutPaiement());
                
                // Calcul et affichage du temps restant
                if (stationnementActif.getDateFin() != null) {
                    long minutesRestantes = java.time.Duration.between(LocalDateTime.now(), stationnementActif.getDateFin()).toMinutes();
                    if (minutesRestantes > 0) {
                        long heures = minutesRestantes / 60;
                        long minutes = minutesRestantes % 60;
                        ajouterLigneInfo("Temps restant:", String.format("%dh%02d", heures, minutes));
                    } else {
                        ajouterLigneInfo("Temps restant:", "Temps écoulé");
                    }
                }
                
            } else if (stationnementActif.estParking()) {
                // === STATIONNEMENT EN PARKING ===
                ajouterLigneInfo("Type:", "Parking");
                ajouterLigneInfo("Parking:", stationnementActif.getZone());
                ajouterLigneInfo("Arrivée:", stationnementActif.getHeureArrivee().format(formatter));
                ajouterLigneInfo("Statut paiement:", stationnementActif.getStatutPaiement());
                
                // Calcul du temps écoulé depuis l'arrivée
                long minutesEcoulees = java.time.Duration.between(stationnementActif.getHeureArrivee(), LocalDateTime.now()).toMinutes();
                long heures = minutesEcoulees / 60;
                long minutes = minutesEcoulees % 60;
                ajouterLigneInfo("Temps écoulé:", String.format("%dh%02d", heures, minutes));
                
                // Estimation du coût en temps réel
                double coutEstime = TarifParkingDAO.calculerCoutParking(
                    stationnementActif.getHeureArrivee(), 
                    LocalDateTime.now(), 
                    getParkingIdFromName(stationnementActif.getZone())
                );
                ajouterLigneInfo("Coût estimé:", String.format("%.2f €", coutEstime));
            }
            
            // Statut général du stationnement
            ajouterLigneInfo("Statut:", stationnementActif.getStatut());
        }
        
        // Actualisation de l'affichage
        panelInfo.revalidate();
        panelInfo.repaint();
    }
    
    /**
     * Ajoute une ligne d'information au panel
     * Format : Libellé (gras) à gauche, Valeur (normal) à droite
     * @param libelle le texte du libellé
     * @param valeur le texte de la valeur
     */
    private void ajouterLigneInfo(String libelle, String valeur) {
        JPanel ligne = new JPanel(new BorderLayout());
        ligne.setBackground(Color.WHITE);
        ligne.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Espacement vertical
        
        JLabel lblLibelle = new JLabel(libelle);
        lblLibelle.setFont(new Font("Arial", Font.BOLD, 14)); // Texte en gras
        lblLibelle.setPreferredSize(new Dimension(150, 25)); // Largeur fixe pour l'alignement
        
        JLabel lblValeur = new JLabel(valeur);
        lblValeur.setFont(new Font("Arial", Font.PLAIN, 14)); // Texte normal
        
        ligne.add(lblLibelle, BorderLayout.WEST);
        ligne.add(lblValeur, BorderLayout.CENTER);
        
        panelInfo.add(ligne);
    }
    
    /**
     * Gère l'arrêt du stationnement
     * Comportement différent selon le type de stationnement
     */
    private void arreterStationnement() {
        if (stationnementActif.estVoirie()) {
            // === ARRÊT STATIONNEMENT VOIRIE (simple confirmation) ===
            int confirmation = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir arrêter ce stationnement ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);
                
            if (confirmation == JOptionPane.YES_OPTION) {
                boolean succes = StationnementDAO.terminerStationnement(stationnementActif.getIdStationnement());
                if (succes) {
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
            
        } else if (stationnementActif.estParking()) {
            // === ARRÊT STATIONNEMENT PARKING (demande heure départ + paiement) ===
            demanderHeureDepartEtPayer();
        }
    }
    
    /**
     * Demande l'heure de départ et lance le processus de paiement pour les parkings
     * L'utilisateur peut spécifier l'heure de départ réelle
     */
    private void demanderHeureDepartEtPayer() {
        // Création d'un panel pour la saisie de l'heure de départ
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        // Spinner pour sélectionner la date et heure de départ
        JSpinner spinnerHeureDepart = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerHeureDepart, "dd/MM/yyyy HH:mm");
        spinnerHeureDepart.setEditor(editor);
        spinnerHeureDepart.setValue(new java.util.Date()); // Valeur par défaut = maintenant
        
        panel.add(new JLabel("Heure de départ:"));
        panel.add(spinnerHeureDepart);
        
        // Boîte de dialogue de confirmation
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Heure de départ", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            // Conversion de la date Java en LocalDateTime
            java.util.Date dateDepart = (java.util.Date) spinnerHeureDepart.getValue();
            LocalDateTime heureDepart = dateDepart.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
            
            // Calcul du coût basé sur la durée réelle
            double cout = TarifParkingDAO.calculerCoutParking(
                stationnementActif.getHeureArrivee(), 
                heureDepart, 
                getParkingIdFromName(stationnementActif.getZone())
            );
            
            // Ouverture de la page de paiement avec toutes les informations
            Page_Paiement pagePaiement = new Page_Paiement(
                cout,
                emailUtilisateur,
                stationnementActif.getTypeVehicule(),
                stationnementActif.getPlaqueImmatriculation(),
                stationnementActif.getZone(),
                0, // Pas de durée en heures (calculée automatiquement)
                0, // Pas de durée en minutes (calculée automatiquement)
                stationnementActif.getIdStationnement(), // ID du stationnement existant
                heureDepart // Heure de départ spécifiée
            );
            pagePaiement.setVisible(true);
            dispose(); // Ferme la page actuelle
        }
    }
    
    /**
     * Convertit un nom de parking affiché en ID technique
     * @param nomParking le nom affiché du parking
     * @return l'ID technique correspondant
     */
    private String getParkingIdFromName(String nomParking) {
        // Mapping des noms affichés vers les IDs techniques en base de données
        switch(nomParking) {
            case "Parking Capitole": return "PARK_CAPITOLE";
            case "Parking Carnot": return "PARK_CARNOT";
            case "Parking Esquirol": return "PARK_ESQUIROL";
            case "Parking Saint-Étienne": return "PARK_SAINT_ETIENNE";
            case "Parking Jean Jaurès": return "PARK_JEAN_JAURES";
            case "Parking Jeanne d'Arc": return "PARK_JEANNE_DARC";
            case "Parking Europe": return "PARK_EUROPE";
            case "Parking Victor Hugo": return "PARK_VICTOR_HUGO";
            case "Parking Saint-Aubin": return "PARK_SAINT_AUBIN";
            case "Parking Saint-Cyprien": return "PARK_SAINT_CYPRIEN";
            case "Parking Saint-Michel": return "PARK_SAINT_MICHEL";
            case "Parking Matabiau-Ramblas": return "PARK_MATABIAU";
            case "Parking Arnaud Bernard": return "PARK_ARNAUD_BERNARD";
            case "Parking Carmes": return "PARK_CARMES";
            default: return "PARK_CAPITOLE"; // Valeur par défaut
        }
    }
    
    /**
     * Démarre l'actualisation automatique de l'affichage
     * Met à jour les informations toutes les 30 secondes
     */
    private void startAutoRefresh() {
        // Timer qui se déclenche toutes les 30 secondes (30000 millisecondes)
        timer = new Timer(30000, e -> {
            chargerStationnementActif(); // Recharge les données
            afficherInformationsStationnement(); // Met à jour l'affichage
            
            // Si le stationnement n'est plus actif, ferme la page
            if (stationnementActif == null) {
                JOptionPane.showMessageDialog(this, 
                    "Le stationnement a été terminé.", 
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
                retourAccueil();
            }
        });
        timer.start(); // Démarre le timer
    }
    
    /**
     * Retourne à la page principale
     */
    private void retourAccueil() {
        Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
        pagePrincipale.setVisible(true);
        dispose(); // Ferme la page actuelle
    }
    
    /**
     * Surcharge de la méthode dispose() pour un nettoyage propre
     * Arrête le timer avant la fermeture pour éviter les fuites mémoire
     */
    @Override
    public void dispose() {
        if (timer != null) {
            timer.stop(); // Arrêt du timer
        }
        super.dispose(); // Appel de la méthode parente
    }
}
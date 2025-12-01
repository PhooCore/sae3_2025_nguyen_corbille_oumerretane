package ihm;

import javax.swing.*;

import controleur.StationnementControleur;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import modele.Stationnement;
import modele.Usager;
import modele.dao.StationnementDAO;
import modele.dao.TarifParkingDAO;
import modele.dao.UsagerDAO;

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
    private StationnementControleur controleur;
    /**
     * Constructeur de la page de gestion des stationnements en cours
     * @param email l'email de l'utilisateur connecté
     */
    public Page_Stationnement_En_Cours(String email) {
        this.emailUtilisateur = email;
        this.controleur = new StationnementControleur(email); // Initialisation du contrôleur
        chargerStationnementActif();
        initialisePage();
        startAutoRefresh();
    }
    
    /**
     * Charge le stationnement actif de l'utilisateur depuis la base de données
     */
    private void chargerStationnementActif() {
        stationnementActif = controleur.getStationnementActif();
    }
    
    /**
     * Initialise l'interface utilisateur de la page
     * Structure : Titre + Informations détaillées + Boutons d'action
     */
    private void initialisePage() {
        // Configuration de la fenêtre
        this.setTitle("Stationnement en cours");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        this.setSize(600, 500);
        this.setLocationRelativeTo(null); 
        
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
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS)); 
        panelInfo.setBackground(Color.WHITE);
        panelInfo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
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
        btnArreter.setFocusPainted(false);
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
                ajouterLigneInfo("Zone:", stationnementActif.getIdTarification()); // Utiliser id_tarification comme zone
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
                
                // CORRECTION : Utiliser getLibelleParkingFromId pour afficher le nom du parking
                String nomParking = getLibelleParkingFromId(stationnementActif.getIdTarification());
                ajouterLigneInfo("Parking:", nomParking != null ? nomParking : stationnementActif.getIdTarification());
                
                if (stationnementActif.getHeureArrivee() != null) {
                    ajouterLigneInfo("Arrivée:", stationnementActif.getHeureArrivee().format(formatter));
                }
                
                ajouterLigneInfo("Statut paiement:", stationnementActif.getStatutPaiement());
                
                // Calcul du temps écoulé depuis l'arrivée
                if (stationnementActif.getHeureArrivee() != null) {
                    long minutesEcoulees = java.time.Duration.between(stationnementActif.getHeureArrivee(), LocalDateTime.now()).toMinutes();
                    long heures = minutesEcoulees / 60;
                    long minutes = minutesEcoulees % 60;
                    ajouterLigneInfo("Temps écoulé:", String.format("%dh%02d", heures, minutes));
                    
                    // Estimation du coût en temps réel
                    try {
                        double coutEstime = TarifParkingDAO.calculerCoutParking(
                            stationnementActif.getHeureArrivee(), 
                            LocalDateTime.now(), 
                            stationnementActif.getIdTarification() // Utiliser directement l'ID du parking
                        );
                        ajouterLigneInfo("Coût estimé:", String.format("%.2f €", coutEstime));
                    } catch (Exception e) {
                        ajouterLigneInfo("Coût estimé:", "Calcul en cours...");
                    }
                }
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
            int confirmation = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir arrêter ce stationnement ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);
                
            if (confirmation == JOptionPane.YES_OPTION) {
                boolean succes = controleur.terminerStationnementVoirie(stationnementActif.getIdStationnement());
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
            // Garder la logique existante pour le parking
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
            
            // Récupérer le nom du parking pour l'affichage
            String nomParking = getLibelleParkingFromId(stationnementActif.getIdTarification());
            
            // Calcul du coût basé sur la durée réelle
            double cout = TarifParkingDAO.calculerCoutParking(
                stationnementActif.getHeureArrivee(), 
                heureDepart, 
                stationnementActif.getIdTarification() // Utiliser directement l'ID du parking
            );
            
            // Ouverture de la page de paiement avec toutes les informations
            Page_Paiement pagePaiement = new Page_Paiement(
                cout,
                emailUtilisateur,
                stationnementActif.getTypeVehicule(),
                stationnementActif.getPlaqueImmatriculation(),
                stationnementActif.getIdTarification(), // ID du parking
                nomParking != null ? nomParking : "Parking", // Nom du parking pour l'affichage
                0, 
                0,
                stationnementActif.getIdStationnement(), // ID du stationnement existant
                heureDepart // Heure de départ spécifiée
            );
            pagePaiement.setVisible(true);
            dispose(); // Ferme la page actuelle
        }
    }
    
    /**
     * Récupère le libellé du parking depuis son ID
     * @param idParking l'ID technique du parking
     * @return le libellé du parking ou null si non trouvé
     */
    private String getLibelleParkingFromId(String idParking) {
        if (idParking == null) return null;
        
        // Mapping des IDs techniques vers les noms affichés
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
            default: return idParking; // Retourner l'ID si non trouvé
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
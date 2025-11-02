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

public class Page_Stationnement_En_Cours extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Stationnement stationnementActif;
    private JPanel panelInfo;
    private JButton btnArreter;
    private Timer timer;

    public Page_Stationnement_En_Cours(String email) {
        this.emailUtilisateur = email;
        chargerStationnementActif();
        initialisePage();
        startAutoRefresh();
    }
    
    private void chargerStationnementActif() {
        Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager != null) {
            stationnementActif = StationnementDAO.getStationnementActifValideByUsager(usager.getIdUsager());
        }
    }
    
    private void initialisePage() {
        this.setTitle("Stationnement en cours");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(600, 500);
        this.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Titre
        JLabel lblTitre = new JLabel("Stationnement en cours", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // Panel d'information
        panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setBackground(Color.WHITE);
        panelInfo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JScrollPane scrollPane = new JScrollPane(panelInfo);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Boutons
        JPanel panelBoutons = new JPanel(new FlowLayout());
        
        JButton btnRetour = new JButton("Retour");
        btnRetour.addActionListener(e -> retourAccueil());
        
        btnArreter = new JButton("Arrêter le stationnement");
        btnArreter.setBackground(Color.RED);
        btnArreter.setForeground(Color.WHITE);
        btnArreter.setFocusPainted(false);
        btnArreter.addActionListener(e -> arreterStationnement());
        
        panelBoutons.add(btnRetour);
        if (stationnementActif != null) {
            panelBoutons.add(btnArreter);
        }
        
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
        afficherInformationsStationnement();
    }
    
    private void afficherInformationsStationnement() {
        panelInfo.removeAll();
        
        if (stationnementActif == null) {
            JLabel lblAucun = new JLabel("Aucun stationnement actif", SwingConstants.CENTER);
            lblAucun.setFont(new Font("Arial", Font.PLAIN, 16));
            lblAucun.setForeground(Color.GRAY);
            panelInfo.add(lblAucun);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            // Informations communes
            ajouterLigneInfo("Véhicule:", stationnementActif.getTypeVehicule() + " - " + stationnementActif.getPlaqueImmatriculation());
            
            if (stationnementActif.estVoirie()) {
                // Stationnement voirie
                ajouterLigneInfo("Type:", "Voirie");
                ajouterLigneInfo("Zone:", stationnementActif.getZone());
                ajouterLigneInfo("Début:", stationnementActif.getDateCreation().format(formatter));
                ajouterLigneInfo("Durée prévue:", stationnementActif.getDureeHeures() + "h" + stationnementActif.getDureeMinutes() + "min");
                ajouterLigneInfo("Coût:", String.format("%.2f", stationnementActif.getCout()) + " €");
                ajouterLigneInfo("Statut paiement:", stationnementActif.getStatutPaiement());
                
                // Calcul du temps restant
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
                // Stationnement parking
                ajouterLigneInfo("Type:", "Parking");
                ajouterLigneInfo("Parking:", stationnementActif.getZone());
                ajouterLigneInfo("Arrivée:", stationnementActif.getHeureArrivee().format(formatter));
                ajouterLigneInfo("Statut paiement:", stationnementActif.getStatutPaiement());
                
                // Calcul du temps écoulé
                long minutesEcoulees = java.time.Duration.between(stationnementActif.getHeureArrivee(), LocalDateTime.now()).toMinutes();
                long heures = minutesEcoulees / 60;
                long minutes = minutesEcoulees % 60;
                ajouterLigneInfo("Temps écoulé:", String.format("%dh%02d", heures, minutes));
                
                // Estimation du coût
                double coutEstime = TarifParkingDAO.calculerCoutParking(
                    stationnementActif.getHeureArrivee(), 
                    LocalDateTime.now(), 
                    getParkingIdFromName(stationnementActif.getZone())
                );
                ajouterLigneInfo("Coût estimé:", String.format("%.2f €", coutEstime));
            }
            
            ajouterLigneInfo("Statut:", stationnementActif.getStatut());
        }
        
        panelInfo.revalidate();
        panelInfo.repaint();
    }
    
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
    
    private void arreterStationnement() {
        if (stationnementActif.estVoirie()) {
            // Pour voirie : arrêt simple
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
            // Pour parking : demander l'heure de départ et procéder au paiement
            demanderHeureDepartEtPayer();
        }
    }
    
    private void demanderHeureDepartEtPayer() {
        // Créer un panel pour la saisie de l'heure de départ
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
            
            // Calcul du coût
            double cout = TarifParkingDAO.calculerCoutParking(
                stationnementActif.getHeureArrivee(), 
                heureDepart, 
                getParkingIdFromName(stationnementActif.getZone())
            );
            
            // Ouvrir la page de paiement
            Page_Paiement pagePaiement = new Page_Paiement(
                cout,
                emailUtilisateur,
                stationnementActif.getTypeVehicule(),
                stationnementActif.getPlaqueImmatriculation(),
                stationnementActif.getZone(),
                0,
                0,
                stationnementActif.getIdStationnement(),
                heureDepart
            );
            pagePaiement.setVisible(true);
            dispose();
        }
    }
    
    private String getParkingIdFromName(String nomParking) {
        // Mapping des noms de parking vers leurs IDs
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
            default: return "PARK_CAPITOLE";
        }
    }
    
    private void startAutoRefresh() {
        // Timer pour la mise à jour automatique de l'affichage
        timer = new Timer(30000, e -> {
            chargerStationnementActif();
            afficherInformationsStationnement();
            
            // Si plus de stationnement actif, fermer la page
            if (stationnementActif == null) {
                JOptionPane.showMessageDialog(this, 
                    "Le stationnement a été terminé.", 
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
                retourAccueil();
            }
        });
        timer.start();
    }
    
    private void retourAccueil() {
        Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
        pagePrincipale.setVisible(true);
        dispose();
    }
    
    @Override
    public void dispose() {
        if (timer != null) {
            timer.stop();
        }
        super.dispose();
    }
}
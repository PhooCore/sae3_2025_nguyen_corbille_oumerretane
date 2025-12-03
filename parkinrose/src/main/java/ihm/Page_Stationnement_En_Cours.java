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
 */
public class Page_Stationnement_En_Cours extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;          
    private Stationnement stationnementActif; 
    private JPanel panelInfo;                 
    private JButton btnArreter;               
    private javax.swing.Timer timer;          
    private StationnementControleur controleur;
    
    public Page_Stationnement_En_Cours(String email) {
        this.emailUtilisateur = email;
        this.controleur = new StationnementControleur(email);
        chargerStationnementActif();
        initialisePage();
        startAutoRefresh();
    }
    
    private void chargerStationnementActif() {
        // Utilise le contrôleur pour récupérer le stationnement actif
        this.stationnementActif = controleur.getStationnementActif();
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
        
        JLabel lblTitre = new JLabel("Stationnement en cours", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS)); 
        panelInfo.setBackground(Color.WHITE);
        panelInfo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JScrollPane scrollPane = new JScrollPane(panelInfo);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
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
            
            ajouterLigneInfo("Véhicule:", stationnementActif.getTypeVehicule() + " - " + stationnementActif.getPlaqueImmatriculation());
            
            if (stationnementActif.estVoirie()) {
                ajouterLigneInfo("Type:", "Voirie");
                ajouterLigneInfo("Zone:", stationnementActif.getIdTarification());
                ajouterLigneInfo("Début:", stationnementActif.getDateCreation().format(formatter));
                ajouterLigneInfo("Durée prévue:", stationnementActif.getDureeHeures() + "h" + stationnementActif.getDureeMinutes() + "min");
                ajouterLigneInfo("Coût:", String.format("%.2f", stationnementActif.getCout()) + " €");
                
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
                ajouterLigneInfo("Type:", "Parking");
                
                String nomParking = getLibelleParkingFromId(stationnementActif.getIdTarification());
                ajouterLigneInfo("Parking:", nomParking != null ? nomParking : stationnementActif.getIdTarification());
                
                if (stationnementActif.getHeureArrivee() != null) {
                    ajouterLigneInfo("Arrivée:", stationnementActif.getHeureArrivee().format(formatter));
                }
                
                // Calcul du temps écoulé
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
                            stationnementActif.getIdTarification()
                        );
                        ajouterLigneInfo("Coût estimé:", String.format("%.2f €", coutEstime));
                    } catch (Exception e) {
                        ajouterLigneInfo("Coût estimé:", "Calcul en cours...");
                    }
                }
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
        if (stationnementActif == null) return;
        
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
            demanderHeureDepartEtPayer();
        }
    }
    
    private void demanderHeureDepartEtPayer() {
        // Création d'un panel pour la saisie de l'heure de départ
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
            
            double cout = TarifParkingDAO.calculerCoutParking(
                stationnementActif.getHeureArrivee(), 
                heureDepart, 
                stationnementActif.getIdTarification()
            );
            
            Page_Paiement pagePaiement = new Page_Paiement(
                cout,
                emailUtilisateur,
                idStationnement,
                heureDepart
            );
            pagePaiement.setVisible(true);
            dispose();
        }
    }
    
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
    
    private void startAutoRefresh() {
        timer = new javax.swing.Timer(30000, e -> {
            chargerStationnementActif();
            afficherInformationsStationnement();
            
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
    
    // Correction : Ajout des variables manquantes
    private Integer idStationnement = null;
    
    // Méthode pour tester
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Page_Stationnement_En_Cours("test@example.com").setVisible(true);
        });
    }
}
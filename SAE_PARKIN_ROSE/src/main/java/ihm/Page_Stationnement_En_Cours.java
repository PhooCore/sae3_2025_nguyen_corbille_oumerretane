package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import dao.StationnementDAO;
import dao.UsagerDAO;
import modèle.Stationnement;
import modèle.Usager;

public class Page_Stationnement_En_Cours extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Stationnement stationnementActif;
    private JPanel panelInfo;
    private JButton btnStopper;
    private Timer timer;

    public Page_Stationnement_En_Cours(String email) {
        this.emailUtilisateur = email;
        chargerStationnementActif();
        initialisePage();
    }
    
    private void chargerStationnementActif() {
        // Récupérer l'utilisateur
        Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager != null) {
            // Utiliser la nouvelle méthode de vérification
            stationnementActif = StationnementDAO.getStationnementActifValideByUsager(usager.getIdUsager());
        }
    }
    
    private void initialisePage() {
        this.setTitle("Stationnement en cours");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // IMPORTANT: DISPOSE au lieu de EXIT
        this.setSize(500, 400);
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
        
        btnStopper = new JButton("Stopper le stationnement");
        btnStopper.setBackground(Color.RED);
        btnStopper.setForeground(Color.WHITE);
        btnStopper.setFocusPainted(false);
        btnStopper.addActionListener(e -> stopperStationnement());
        
        panelBoutons.add(btnRetour);
        if (stationnementActif != null) {
            panelBoutons.add(btnStopper);
        }
        
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
        afficherInformationsStationnement();
        
	     // Timer pour la mise à jour automatique de l'affichage
	     // - Actualise les informations toutes les 30 secondes (30000 ms)
	     // - Met à jour le temps restant en temps réel
	     // - Détecte automatiquement si le stationnement a été terminé (via un autre appareil ou expiration)
	     // - Évite à l'utilisateur d'avoir à rafraîchir manuellement la page
	     timer = new Timer(30000, e -> mettreAJourAffichage());
	     timer.start();
    }
    
    private void afficherInformationsStationnement() {
        panelInfo.removeAll();
        
        if (stationnementActif == null) {
            JLabel lblAucun = new JLabel("Aucun stationnement actif", SwingConstants.CENTER);
            lblAucun.setFont(new Font("Arial", Font.PLAIN, 16));
            lblAucun.setForeground(Color.GRAY);
            panelInfo.add(lblAucun);
        } else {
            // Affichage des informations du stationnement
            ajouterLigneInfo("Véhicule:", stationnementActif.getTypeVehicule() + " - " + stationnementActif.getPlaqueImmatriculation());
            ajouterLigneInfo("Zone:", stationnementActif.getZone());
            ajouterLigneInfo("Début:", stationnementActif.getDateCreation().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            ajouterLigneInfo("Durée prévue:", stationnementActif.getDureeHeures() + "h" + stationnementActif.getDureeMinutes() + "min");
            ajouterLigneInfo("Coût:", String.format("%.2f", stationnementActif.getCout()) + " €");
            ajouterLigneInfo("Statut:", stationnementActif.getStatut());
            
            // Calcul du temps restant
            if (stationnementActif.getDateFin() != null) {
                long minutesRestantes = java.time.Duration.between(
                    java.time.LocalDateTime.now(), 
                    stationnementActif.getDateFin()
                ).toMinutes();
                
                if (minutesRestantes > 0) {
                    ajouterLigneInfo("Temps restant:", minutesRestantes + " minutes");
                } else {
                    ajouterLigneInfo("Temps restant:", "Temps écoulé");
                }
            }
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
    
    private void mettreAJourAffichage() {
        // Recharger les données et mettre à jour l'affichage
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
    }
    
    private void stopperStationnement() {
        if (stationnementActif != null) {
            int confirmation = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir stopper ce stationnement ?\n" +
                "Cette action est irréversible.",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);
                
            if (confirmation == JOptionPane.YES_OPTION) {
                boolean succes = StationnementDAO.terminerStationnement(stationnementActif.getIdStationnement());
                
                if (succes) {
                    JOptionPane.showMessageDialog(this,
                        "Stationnement stoppé avec succès !",
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
        }
    }
    
    private void retourAccueil() {
        // On ferme simplement cette page, la Page_Principale existe déjà
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
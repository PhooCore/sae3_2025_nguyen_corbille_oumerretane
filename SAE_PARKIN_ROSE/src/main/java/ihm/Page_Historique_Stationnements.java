package ihm;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.StationnementDAO;
import modèle.Stationnement;
import modèle.Usager;
import dao.UsagerDAO;

public class Page_Historique_Stationnements extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Usager usager;

    public Page_Historique_Stationnements(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        initialisePage();
    }
    
    private void initialisePage() {
        this.setTitle("Historique des stationnements");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // En-tête
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JButton btnRetour = new JButton("← Retour");
        btnRetour.addActionListener(e -> retourProfil());
        headerPanel.add(btnRetour, BorderLayout.WEST);
        
        JLabel lblTitre = new JLabel("Historique de vos stationnements", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(lblTitre, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Tableau des stationnements
        List<Stationnement> stationnements = StationnementDAO.getHistoriqueStationnements(usager.getIdUsager());
        
        String[] colonnes = {"Date", "Type", "Véhicule", "Zone/Parking", "Durée", "Coût", "Statut"};
        Object[][] donnees = new Object[stationnements.size()][7];
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (int i = 0; i < stationnements.size(); i++) {
            Stationnement s = stationnements.get(i);
            donnees[i][0] = s.getDateCreation().format(formatter);
            donnees[i][1] = s.getTypeStationnement();
            donnees[i][2] = s.getTypeVehicule() + " - " + s.getPlaqueImmatriculation();
            donnees[i][3] = s.getZone();
            
            if (s.estVoirie()) {
                donnees[i][4] = s.getDureeHeures() + "h" + s.getDureeMinutes() + "min";
            } else {
                if (s.getHeureArrivee() != null && s.getHeureDepart() != null) {
                    long minutes = java.time.Duration.between(s.getHeureArrivee(), s.getHeureDepart()).toMinutes();
                    long heures = minutes / 60;
                    long mins = minutes % 60;
                    donnees[i][4] = heures + "h" + mins + "min";
                } else {
                    donnees[i][4] = "En cours";
                }
            }
            
            donnees[i][5] = String.format("%.2f €", s.getCout());
            donnees[i][6] = s.getStatut();
        }
        
        JTable table = new JTable(donnees, colonnes);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setDefaultEditor(Object.class, null);
        
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Statistiques
        JPanel statsPanel = new JPanel(new FlowLayout());
        statsPanel.setBackground(Color.WHITE);
        
        long totalStationnements = stationnements.size();
        long stationnementsActifs = stationnements.stream().filter(s -> "ACTIF".equals(s.getStatut())).count();
        
        JLabel lblStats = new JLabel("Total: " + totalStationnements + " stationnement(s) | Actifs: " + stationnementsActifs);
        lblStats.setFont(new Font("Arial", Font.BOLD, 14));
        statsPanel.add(lblStats);
        
        mainPanel.add(statsPanel, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
    }
    
    private void retourProfil() {
        Page_Utilisateur pageUtilisateur = new Page_Utilisateur(emailUtilisateur);
        pageUtilisateur.setVisible(true);
        dispose();
    }
}
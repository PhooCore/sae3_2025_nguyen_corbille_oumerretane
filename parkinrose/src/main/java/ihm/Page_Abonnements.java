package ihm;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import modele.Abonnement;
import modele.dao.AbonnementDAO;
import modele.Usager;
import modele.dao.UsagerDAO;

/**
 * Page qui présente les différents abonnements disponibles
 */
public class Page_Abonnements extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Usager usager;
    
    public Page_Abonnements(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        initialiserPage();
    }
    
    private void initialiserPage() {
        setTitle("Abonnements disponibles");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        JLabel lblTitre = new JLabel("Nos Abonnements", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitre.setForeground(new Color(0, 102, 204));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        JPanel panelAbonnements = new JPanel(new GridLayout(0, 1, 10, 15));
        panelAbonnements.setBackground(Color.WHITE);
        
        List<Abonnement> abonnements = AbonnementDAO.getAllAbonnements();
        
        if (abonnements.isEmpty()) {
            JLabel lblAucun = new JLabel("Aucun abonnement disponible pour le moment", SwingConstants.CENTER);
            lblAucun.setFont(new Font("Arial", Font.ITALIC, 16));
            panelAbonnements.add(lblAucun);
        } else {
            for (Abonnement abonnement : abonnements) {
                JPanel carteAbonnement = creerCarteAbonnement(abonnement);
                panelAbonnements.add(carteAbonnement);
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(panelAbonnements);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBoutons.setBackground(Color.WHITE);
        
        JButton btnRetour = new JButton("Retour");
        btnRetour.addActionListener(e -> {
            Page_Utilisateur pageUser = new Page_Utilisateur(emailUtilisateur);
            pageUser.setVisible(true);
            dispose();
        });
        
        boolean aUnAbonnement = !AbonnementDAO.getAbonnementsByUsager(usager.getIdUsager()).isEmpty();
        
        if (!aUnAbonnement) {
            JButton btnMonCompte = new JButton("Mon Compte");
            btnMonCompte.addActionListener(e -> {
                Page_Utilisateur pageUser = new Page_Utilisateur(emailUtilisateur);
                pageUser.setVisible(true);
                dispose();
            });
            panelBoutons.add(btnMonCompte);
        }
        
        panelBoutons.add(btnRetour);
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel creerCarteAbonnement(Abonnement abonnement) {
        JPanel carte = new JPanel(new BorderLayout(15, 10));
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        carte.setBackground(Color.WHITE);
        
        JPanel panelDetails = new JPanel();
        panelDetails.setLayout(new BoxLayout(panelDetails, BoxLayout.Y_AXIS));
        panelDetails.setBackground(Color.WHITE);
        
        JLabel lblLibelle = new JLabel(abonnement.getLibelleAbonnement());
        lblLibelle.setFont(new Font("Arial", Font.BOLD, 18));
        lblLibelle.setForeground(new Color(51, 51, 51));
        
        JLabel lblId = new JLabel("Code: " + abonnement.getIdAbonnement());
        lblId.setFont(new Font("Arial", Font.PLAIN, 12));
        lblId.setForeground(Color.GRAY);
        
        JLabel lblAvantages = new JLabel(getAvantagesByType(abonnement.getIdAbonnement()));
        lblAvantages.setFont(new Font("Arial", Font.PLAIN, 14));
        lblAvantages.setForeground(new Color(60, 60, 60));
        
        panelDetails.add(lblLibelle);
        panelDetails.add(Box.createVerticalStrut(5));
        panelDetails.add(lblId);
        panelDetails.add(Box.createVerticalStrut(10));
        panelDetails.add(lblAvantages);
        
        JPanel panelActions = new JPanel(new BorderLayout());
        panelActions.setBackground(Color.WHITE);
        
        JLabel lblPrix = new JLabel(String.format("%.2f €/mois", abonnement.getTarifAbonnement()));
        lblPrix.setFont(new Font("Arial", Font.BOLD, 20));
        lblPrix.setForeground(new Color(0, 150, 0));
        lblPrix.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JButton btnSouscrire = new JButton("Souscrire");
        btnSouscrire.setBackground(new Color(0, 102, 204));
        btnSouscrire.setForeground(Color.WHITE);
        btnSouscrire.setFont(new Font("Arial", Font.BOLD, 14));
        btnSouscrire.addActionListener(e -> souscrireAbonnement(abonnement));
        
        if (AbonnementDAO.hasAbonnement(usager.getIdUsager(), abonnement.getIdAbonnement())) {
            btnSouscrire.setText("Déjà souscrit");
            btnSouscrire.setEnabled(false);
            btnSouscrire.setBackground(Color.GRAY);
        }
        
        panelActions.add(lblPrix, BorderLayout.NORTH);
        panelActions.add(btnSouscrire, BorderLayout.SOUTH);
        
        carte.add(panelDetails, BorderLayout.CENTER);
        carte.add(panelActions, BorderLayout.EAST);
        
        return carte;
    }
    
    private String getAvantagesByType(String idAbonnement) {
        switch(idAbonnement.toUpperCase()) {
            case "ABN_BASIC":
                return "• Stationnement illimité en voirie (2h max)\n" +
                       "• 10% de réduction dans les parkings partenaires\n" +
                       "• Accès aux zones bleues";
            case "ABN_PREMIUM":
                return "• Stationnement illimité en voirie\n" +
                       "• 25% de réduction dans les parkings partenaires\n" +
                       "• Accès à toutes les zones\n" +
                       "• Réservation prioritaire";
            case "ABN_ETUDIANT":
                return "• 50% de réduction sur tous les stationnements\n" +
                       "• Accès aux zones universitaires\n" +
                       "• Valable uniquement avec carte étudiante";
            case "ABN_SENIOR":
                return "• 40% de réduction sur tous les stationnements\n" +
                       "• Accès aux zones résidentielles\n" +
                       "• Pour les 65 ans et plus";
            default:
                return "• Avantages personnalisés\n" +
                       "• Contactez-nous pour plus d'informations";
        }
    }
    
    private void souscrireAbonnement(Abonnement abonnement) {
        List<Abonnement> abonnementsExistants = AbonnementDAO.getAbonnementsByUsager(usager.getIdUsager());
        
        if (!abonnementsExistants.isEmpty()) {
            int choix = JOptionPane.showConfirmDialog(this,
                "Vous avez déjà un abonnement actif (" + abonnementsExistants.get(0).getLibelleAbonnement() + ").\n" +
                "Voulez-vous le remplacer par cet abonnement ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
                
            if (choix != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        Page_Paiement_Abonnement pagePaiement = new Page_Paiement_Abonnement(emailUtilisateur, abonnement, this);
        pagePaiement.setVisible(true);
    }
}
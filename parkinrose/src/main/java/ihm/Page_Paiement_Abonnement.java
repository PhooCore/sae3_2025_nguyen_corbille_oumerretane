package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import modele.Abonnement;
import modele.dao.AbonnementDAO;
import modele.Usager;
import modele.dao.UsagerDAO;

/**
 * Page de paiement pour souscrire à un abonnement
 */
public class Page_Paiement_Abonnement extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Abonnement abonnement;
    private JFrame parentFrame;
    
    public Page_Paiement_Abonnement(String email, Abonnement abonnement, JFrame parent) {
        this.emailUtilisateur = email;
        this.abonnement = abonnement;
        this.parentFrame = parent;
        initialiserPage();
    }
    
    private void initialiserPage() {
        setTitle("Paiement de l'abonnement");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        JLabel lblTitre = new JLabel("Souscription à l'abonnement", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitre.setForeground(new Color(0, 102, 204));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        JPanel panelDetails = new JPanel();
        panelDetails.setLayout(new BoxLayout(panelDetails, BoxLayout.Y_AXIS));
        panelDetails.setBackground(Color.WHITE);
        panelDetails.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JPanel panelRecap = new JPanel(new GridLayout(0, 2, 10, 10));
        panelRecap.setBackground(Color.WHITE);
        panelRecap.setBorder(BorderFactory.createTitledBorder("Récapitulatif"));
        
        panelRecap.add(new JLabel("Abonnement:"));
        JLabel lblNomAbonnement = new JLabel(abonnement.getLibelleAbonnement());
        lblNomAbonnement.setFont(new Font("Arial", Font.BOLD, 14));
        panelRecap.add(lblNomAbonnement);
        
        panelRecap.add(new JLabel("Prix mensuel:"));
        JLabel lblPrix = new JLabel(String.format("%.2f €", abonnement.getTarifAbonnement()));
        lblPrix.setFont(new Font("Arial", Font.BOLD, 14));
        lblPrix.setForeground(new Color(0, 150, 0));
        panelRecap.add(lblPrix);
        
        panelRecap.add(new JLabel("Durée:"));
        panelRecap.add(new JLabel("1 mois (renouvelable automatiquement)"));
        
        panelRecap.add(new JLabel("Date de début:"));
        panelRecap.add(new JLabel(LocalDate.now().toString()));
        
        panelDetails.add(panelRecap);
        panelDetails.add(Box.createVerticalStrut(20));
        
        JPanel panelPaiement = new JPanel(new GridLayout(0, 2, 10, 10));
        panelPaiement.setBackground(Color.WHITE);
        panelPaiement.setBorder(BorderFactory.createTitledBorder("Informations de paiement"));
        
        panelPaiement.add(new JLabel("Numéro de carte:"));
        JTextField txtNumCarte = new JTextField("4242 4242 4242 4242");
        panelPaiement.add(txtNumCarte);
        
        panelPaiement.add(new JLabel("Date d'expiration:"));
        JTextField txtExpiration = new JTextField("MM/AA");
        panelPaiement.add(txtExpiration);
        
        panelPaiement.add(new JLabel("Cryptogramme:"));
        JTextField txtCrypto = new JTextField("123");
        panelPaiement.add(txtCrypto);
        
        panelPaiement.add(new JLabel("Titulaire:"));
        JTextField txtTitulaire = new JTextField();
        Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager != null) {
            txtTitulaire.setText(usager.getNomUsager() + " " + usager.getPrenomUsager());
        }
        panelPaiement.add(txtTitulaire);
        
        panelDetails.add(panelPaiement);
        
        mainPanel.add(panelDetails, BorderLayout.CENTER);
        
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBoutons.setBackground(Color.WHITE);
        
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(e -> {
            if (parentFrame != null) {
                parentFrame.setVisible(true);
            }
            dispose();
        });
        
        JButton btnPayer = new JButton("Payer maintenant");
        btnPayer.setBackground(new Color(0, 150, 0));
        btnPayer.setForeground(Color.WHITE);
        btnPayer.setFont(new Font("Arial", Font.BOLD, 14));
        btnPayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                traiterPaiement();
            }
        });
        
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnPayer);
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private void traiterPaiement() {
        // Simuler le traitement du paiement
        int confirmation = JOptionPane.showConfirmDialog(this,
            "Confirmez-vous le paiement de " + String.format("%.2f €", abonnement.getTarifAbonnement()) + 
            " pour l'abonnement " + abonnement.getLibelleAbonnement() + " ?",
            "Confirmation de paiement",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (confirmation == JOptionPane.YES_OPTION) {
            // Ajouter l'abonnement à l'utilisateur
            Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
            
            // Supprimer d'abord les anciens abonnements
            supprimerAbonnementsUtilisateur(usager.getIdUsager());
            
            // Ajouter le nouvel abonnement
            boolean succes = ajouterAbonnementUtilisateur(usager.getIdUsager(), abonnement.getIdAbonnement());
            
            if (succes) {
                JOptionPane.showMessageDialog(this,
                    "Paiement confirmé !\n" +
                    "Votre abonnement " + abonnement.getLibelleAbonnement() + " est maintenant actif.",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
                    
                // Fermer cette fenêtre et celle des abonnements
                if (parentFrame != null) {
                    parentFrame.dispose();
                }
                
                // ========== NOUVEAU : OUVERTURE DE LA PAGE UTILISATEUR AVEC RAFRAÎCHISSEMENT ==========
                // Fermer toutes les fenêtres d'abonnement
                dispose();
                
                // Ouvrir la page utilisateur qui affichera automatiquement le nouvel abonnement
                // L'onglet Informations montrera "Actif - [Nom de l'abonnement]"
                Page_Utilisateur pageUser = new Page_Utilisateur(emailUtilisateur);
                pageUser.setVisible(true);
                // ========== FIN MODIFICATION ==========
                
            } else {
                JOptionPane.showMessageDialog(this,
                    "Une erreur est survenue lors de l'activation de l'abonnement.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void supprimerAbonnementsUtilisateur(int idUsager) {
        System.out.println("Suppression des anciens abonnements pour l'utilisateur " + idUsager);
    }
    
    private boolean ajouterAbonnementUtilisateur(int idUsager, String idAbonnement) {
        System.out.println("Ajout de l'abonnement " + idAbonnement + " à l'utilisateur " + idUsager);
        return true;
    }
}
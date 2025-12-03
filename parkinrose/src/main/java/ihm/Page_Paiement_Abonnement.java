package ihm;

import javax.swing.*;

import controleur.PaiementControleur;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import modele.Abonnement;
import modele.Paiement;
import modele.dao.AbonnementDAO;
import modele.dao.PaiementDAO;
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
    
    // Déclaration des champs textuels comme variables de classe
    private JTextField txtNumeroCarte;
    private JTextField txtExpiration;
    private JTextField txtCrypto;
    private JTextField txtTitulaire;
    
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
        
        // ========== SECTION RÉCAPITULATIF ==========
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
        
        // ========== SECTION FORMULAIRE PAIEMENT ==========
        JPanel panelPaiement = new JPanel(new GridLayout(0, 2, 10, 10));
        panelPaiement.setBackground(Color.WHITE);
        panelPaiement.setBorder(BorderFactory.createTitledBorder("Informations de paiement"));
        
        // Numéro de carte
        panelPaiement.add(new JLabel("Numéro de carte:"));
        txtNumeroCarte = new JTextField("4242 4242 4242 4242");
        panelPaiement.add(txtNumeroCarte);
        
        // Date d'expiration
        panelPaiement.add(new JLabel("Date d'expiration (MM/AA):"));
        txtExpiration = new JTextField("12/25");
        panelPaiement.add(txtExpiration);
        
        // Cryptogramme
        panelPaiement.add(new JLabel("Cryptogramme (CVV):"));
        txtCrypto = new JTextField("123");
        panelPaiement.add(txtCrypto);
        
        // Titulaire
        panelPaiement.add(new JLabel("Titulaire de la carte:"));
        txtTitulaire = new JTextField();
        Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager != null) {
            txtTitulaire.setText(usager.getNomUsager() + " " + usager.getPrenomUsager());
        }
        panelPaiement.add(txtTitulaire);
        
        panelDetails.add(panelPaiement);
        panelDetails.add(Box.createVerticalStrut(20));
        
        // ========== SECTION SÉCURITÉ ==========
        JPanel panelSecurite = new JPanel();
        panelSecurite.setBackground(new Color(240, 248, 255));
        panelSecurite.setLayout(new BoxLayout(panelSecurite, BoxLayout.Y_AXIS));
        panelSecurite.setBorder(BorderFactory.createTitledBorder("Sécurité du paiement"));
        
        JLabel lblSecurite1 = new JLabel("• Votre paiement est sécurisé par cryptage SSL");
        JLabel lblSecurite2 = new JLabel("• Aucune donnée bancaire n'est conservée sur nos serveurs");
        JLabel lblSecurite3 = new JLabel("• Le prélèvement s'effectuera mensuellement");
        
        lblSecurite1.setFont(new Font("Arial", Font.PLAIN, 11));
        lblSecurite2.setFont(new Font("Arial", Font.PLAIN, 11));
        lblSecurite3.setFont(new Font("Arial", Font.PLAIN, 11));
        
        panelSecurite.add(lblSecurite1);
        panelSecurite.add(lblSecurite2);
        panelSecurite.add(lblSecurite3);
        
        panelDetails.add(panelSecurite);
        
        mainPanel.add(panelDetails, BorderLayout.CENTER);
        
        // ========== SECTION BOUTONS ==========
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
        // Valider le formulaire
        if (!validerFormulaire()) {
            return;
        }
        
        // Récupérer l'utilisateur
        Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager == null) {
            JOptionPane.showMessageDialog(this,
                "Utilisateur non trouvé.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Utiliser le contrôleur pour traiter le paiement
        PaiementControleur controleur = new PaiementControleur(emailUtilisateur);
        boolean succes = controleur.traiterPaiementAbonnement(
            txtTitulaire.getText().trim(),
            txtNumeroCarte.getText().trim(),
            txtExpiration.getText().trim(),
            txtCrypto.getText().trim(),
            abonnement.getTarifAbonnement(),
            abonnement.getIdAbonnement(),
            abonnement.getLibelleAbonnement(),
            this
        );
        
        if (succes) {
            // Fermer toutes les fenêtres d'abonnement
            if (parentFrame != null) {
                parentFrame.dispose();
            }
            
            // Ouvrir la page utilisateur avec rafraîchissement
            dispose();
            Page_Utilisateur pageUser = new Page_Utilisateur(emailUtilisateur, true);
            pageUser.setVisible(true);
        }
        // Si erreur, le contrôleur a déjà affiché le message
    }

    private boolean validerFormulaire() {
        // Créer le contrôleur de paiement
        PaiementControleur controleur = new PaiementControleur(emailUtilisateur);
        
        // Valider le formulaire complet
        return controleur.validerFormulairePaiementComplet(
            txtTitulaire.getText().trim(),
            txtNumeroCarte.getText().trim(),
            txtExpiration.getText().trim(),
            txtCrypto.getText().trim(),
            this
        );
    }
    
    // Méthode main pour tester la page
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Créer un abonnement fictif pour le test
            Abonnement testAbonnement = new Abonnement("ABN_BASIC", "Abonnement Basique", 9.99);
            
            // Créer et afficher la page de paiement
            Page_Paiement_Abonnement page = new Page_Paiement_Abonnement("test@example.com", testAbonnement, null);
            page.setVisible(true);
        });
    }
}
package ihm;

import java.awt.*;
import javax.swing.*;
import controleur.ControleurAuthentification;

public class Page_Authentification extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Déclaration publique pour le contrôleur
    public JPanel contentPanel;
    public JLabel lblTitre;
    public JTextField txtEmail;
    public JPasswordField txtPassword;
    public JLabel lblForgotPassword;
    public JLabel lblCreateAccount;
    public JButton btnLogin;

    public Page_Authentification() {
        this.setTitle("Connexion - Parkin'Rose");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        // Initialiser les composants
        initialiserComposants();
        
        // Créer et lier le contrôleur
        new ControleurAuthentification(this);
    }
    
    private void initialiserComposants() {
        contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setLayout(null);
        this.setContentPane(contentPanel);
        
        // Titre
        lblTitre = new JLabel("Connexion");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 32));
        lblTitre.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitre.setForeground(Color.BLACK);
        lblTitre.setBounds(200, 40, 400, 50);
        contentPanel.add(lblTitre);
        
        // Champ Email
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Arial", Font.PLAIN, 18));
        lblEmail.setForeground(Color.BLACK);
        lblEmail.setBounds(50, 130, 100, 25);
        contentPanel.add(lblEmail);
        
        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Arial", Font.PLAIN, 16));
        txtEmail.setBackground(Color.WHITE);
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtEmail.setBounds(50, 160, 700, 40);
        contentPanel.add(txtEmail);
        
        // Champ Mot de passe
        JLabel lblPassword = new JLabel("Mot de passe:");
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 18));
        lblPassword.setForeground(Color.BLACK);
        lblPassword.setBounds(50, 220, 150, 25);
        contentPanel.add(lblPassword);
        
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 16));
        txtPassword.setBackground(Color.WHITE);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtPassword.setBounds(50, 250, 700, 40);
        contentPanel.add(txtPassword);
        
        // Lien "Mot de passe oublié"
        lblForgotPassword = new JLabel("Mot de passe oublié ?");
        lblForgotPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        lblForgotPassword.setForeground(Color.DARK_GRAY);
        lblForgotPassword.setHorizontalAlignment(SwingConstants.LEFT);
        lblForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgotPassword.setBounds(50, 300, 200, 20);
        contentPanel.add(lblForgotPassword);
        
        // Bouton Connexion
        btnLogin = new JButton("SE CONNECTER");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 18));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(new Color(80, 80, 80));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setBounds(50, 350, 700, 50);
        contentPanel.add(btnLogin);
        
        // Lien "Créer un compte"
        lblCreateAccount = new JLabel("Créer un compte", SwingConstants.CENTER);
        lblCreateAccount.setFont(new Font("Arial", Font.BOLD, 16));
        lblCreateAccount.setForeground(new Color(0, 100, 200));
        lblCreateAccount.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblCreateAccount.setBounds(50, 420, 700, 30);
        contentPanel.add(lblCreateAccount);
    }
    
    // Méthode pour identifier l'action du bouton
    public static String getActionBouton(JButton b) {
        String texte = b.getText();
        if (texte != null) {
            if (texte.contains("CONNECTER")) {
                return "CONNEXION";
            }
        }
        return "INCONNU";
    }
}
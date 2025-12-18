package ihm;

import java.awt.*;
import javax.swing.*;
import controleur.ControleurInscription;

public class Page_Inscription extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Déclaration des composants
    private JTextField textFieldNom;
    private JTextField textFieldPrenom;
    private JTextField textFieldAdresse;
    private JTextField textFieldCodePostal;
    private JTextField textFieldVille;
    private JTextField textFieldEmail;
    private JPasswordField passwordField;
    private JPasswordField passwordFieldConfirm;
    private JButton btnRetour;
    private JButton btnCreerCompte;

    public Page_Inscription() {
        setTitle("Création de compte");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null);
        
        initialisePage();
        
        // Créer et lier le contrôleur
        new ControleurInscription(this);
    }
    
    private void initialisePage() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BorderLayout(0, 0));
        setContentPane(mainPanel);

        // Panel retour
        JPanel panelRetour = new JPanel();
        panelRetour.setBackground(Color.WHITE);
        FlowLayout flowLayout = (FlowLayout) panelRetour.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        mainPanel.add(panelRetour, BorderLayout.NORTH);
        
        // Bouton retour
        btnRetour = new JButton("← Retour");
        btnRetour.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRetour.setBackground(Color.WHITE);
        btnRetour.setFocusPainted(false);
        panelRetour.add(btnRetour);

        // Panel formulaire
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // Titre
        JLabel lblTitre = new JLabel("Créer un compte");
        lblTitre.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 28));
        formPanel.add(lblTitre);
        
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Champ Nom
        formPanel.add(creerChampPanel("Nom", 0));
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Champ Prénom
        formPanel.add(creerChampPanel("Prénom", 1));
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Champ Email
        formPanel.add(creerChampPanel("Email", 2));
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Champ Mot de passe
        formPanel.add(creerChampMotDePasse("Mot de passe", 3));
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Champ Confirmation
        formPanel.add(creerChampMotDePasse("Confirmation", 4));
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Bouton Créer compte
        btnCreerCompte = new JButton("CRÉER MON COMPTE");
        btnCreerCompte.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCreerCompte.setFont(new Font("Arial", Font.BOLD, 18));
        btnCreerCompte.setBackground(new Color(80, 80, 80));
        btnCreerCompte.setForeground(Color.WHITE);
        btnCreerCompte.setFocusPainted(false);
        btnCreerCompte.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        formPanel.add(btnCreerCompte);
        
        formPanel.add(Box.createVerticalGlue());
    }
    
    private JPanel creerChampPanel(String label, int index) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(500, 50));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        
        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(120, 30));
        lbl.setMinimumSize(new Dimension(120, 30));
        lbl.setMaximumSize(new Dimension(120, 30));
        lbl.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(lbl);
        
        panel.add(Box.createRigidArea(new Dimension(20, 0)));
        
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(300, 40));
        textField.setMinimumSize(new Dimension(300, 40));
        textField.setMaximumSize(new Dimension(300, 40));
        textField.setFont(new Font("Arial", Font.PLAIN, 16));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panel.add(textField);
        
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        
        // Stocker la référence
        switch(index) {
            case 0: textFieldNom = textField; break;
            case 1: textFieldPrenom = textField; break;
            case 2: textFieldEmail = textField; break;
        }
        
        return panel;
    }
    
    private JPanel creerChampMotDePasse(String label, int index) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(500, 50));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        
        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(120, 30));
        lbl.setMinimumSize(new Dimension(120, 30));
        lbl.setMaximumSize(new Dimension(120, 30));
        lbl.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(lbl);
        
        panel.add(Box.createRigidArea(new Dimension(20, 0)));
        
        JPasswordField pwdField = new JPasswordField();
        pwdField.setPreferredSize(new Dimension(300, 40));
        pwdField.setMinimumSize(new Dimension(300, 40));
        pwdField.setMaximumSize(new Dimension(300, 40));
        pwdField.setFont(new Font("Arial", Font.PLAIN, 16));
        pwdField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panel.add(pwdField);
        
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        
        // Stocker la référence
        if (index == 3) {
            passwordField = pwdField;
        } else if (index == 4) {
            passwordFieldConfirm = pwdField;
        }
        
        return panel;
    }
    
    // Getters pour le contrôleur
    public JTextField getTextFieldNom() {
        return textFieldNom;
    }
    
    public JTextField getTextFieldPrenom() {
        return textFieldPrenom;
    }
    
    public JTextField getTextFieldEmail() {
        return textFieldEmail;
    }
    
    public JPasswordField getPasswordField() {
        return passwordField;
    }
    
    public JPasswordField getPasswordFieldConfirm() {
        return passwordFieldConfirm;
    }
    
    public JButton getBtnRetour() {
        return btnRetour;
    }
    
    public JButton getBtnCreerCompte() {
        return btnCreerCompte;
    }
}
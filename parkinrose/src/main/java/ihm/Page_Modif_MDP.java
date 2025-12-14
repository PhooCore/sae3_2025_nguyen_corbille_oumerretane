package ihm;

import java.awt.*;
import javax.swing.*;

public class Page_Modif_MDP extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Déclaration des composants de formulaire
    private JTextField txtEmail;
    private JPasswordField passwordFieldNouveau;
    private JPasswordField passwordFieldConfirmer;
    private JButton btnModifier;
    private JButton btnRetour;
    private Page_Utilisateur pageParente;
    
    /**
     * Constructeur de la page de modification de mot de passe
     */
    public Page_Modif_MDP() {
        setTitle("Modifier le mot de passe");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        initialiserPage();
    }
    
    private void initialiserPage() {
        // Panel principal avec layout vertical
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 40, 100)); 
        setContentPane(mainPanel);

        // Panel de retour en haut à gauche
        JPanel panelRetour = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelRetour.setBackground(Color.WHITE);
        panelRetour.setMaximumSize(new Dimension(600, 40)); 
        mainPanel.add(panelRetour);

        // Bouton retour
        btnRetour = new JButton("← Retour");
        btnRetour.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); 
        btnRetour.setFocusPainted(false); 
        btnRetour.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); 
        btnRetour.setBackground(Color.WHITE);
        btnRetour.setForeground(new Color(80, 80, 80)); 
        btnRetour.setFont(new Font("Arial", Font.PLAIN, 14));
        panelRetour.add(btnRetour);
        
        // Espacement après le bouton retour
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Titre de la page
        JLabel lblTitre = new JLabel("Modifier le mot de passe", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 32));
        lblTitre.setAlignmentX(CENTER_ALIGNMENT);
        mainPanel.add(lblTitre);
        
        // Espacement après le titre
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // === PANEL EMAIL ===
        JPanel panelEmail = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelEmail.setBackground(Color.WHITE);
        panelEmail.setMaximumSize(new Dimension(600, 50)); 
        
        // Label "Email"
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Arial", Font.PLAIN, 16));
        lblEmail.setPreferredSize(new Dimension(200, 30)); 
        panelEmail.add(lblEmail);
        
        // Champ de saisie pour l'email
        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Arial", Font.PLAIN, 16));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12) 
        ));
        txtEmail.setPreferredSize(new Dimension(350, 40)); 
        panelEmail.add(txtEmail);
        
        mainPanel.add(panelEmail);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20))); 

        // === PANEL NOUVEAU MOT DE PASSE ===
        JPanel panelNouveau = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelNouveau.setBackground(Color.WHITE);
        panelNouveau.setMaximumSize(new Dimension(600, 50));
        
        JLabel lblNouveau = new JLabel("Nouveau mot de passe:");
        lblNouveau.setFont(new Font("Arial", Font.PLAIN, 16));
        lblNouveau.setPreferredSize(new Dimension(200, 30));
        panelNouveau.add(lblNouveau);
        
        // Champ masqué pour le nouveau mot de passe
        passwordFieldNouveau = new JPasswordField();
        passwordFieldNouveau.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordFieldNouveau.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        passwordFieldNouveau.setPreferredSize(new Dimension(350, 40));
        panelNouveau.add(passwordFieldNouveau);
        
        mainPanel.add(panelNouveau);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // === PANEL CONFIRMATION ===
        JPanel panelConfirmer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelConfirmer.setBackground(Color.WHITE);
        panelConfirmer.setMaximumSize(new Dimension(600, 50));
        
        JLabel lblConfirmer = new JLabel("Confirmer le nouveau:");
        lblConfirmer.setFont(new Font("Arial", Font.PLAIN, 16));
        lblConfirmer.setPreferredSize(new Dimension(200, 30));
        panelConfirmer.add(lblConfirmer);
        
        // Champ masqué pour la confirmation du mot de passe
        passwordFieldConfirmer = new JPasswordField();
        passwordFieldConfirmer.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordFieldConfirmer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        passwordFieldConfirmer.setPreferredSize(new Dimension(350, 40));
        panelConfirmer.add(passwordFieldConfirmer);
        
        mainPanel.add(panelConfirmer);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40))); 

        // === BOUTON DE MODIFICATION ===
        btnModifier = new JButton("MODIFIER LE MOT DE PASSE");
        btnModifier.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnModifier.setFocusPainted(false);
        btnModifier.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        btnModifier.setBackground(new Color(80, 80, 80)); 
        btnModifier.setForeground(Color.WHITE);
        btnModifier.setFont(new Font("Arial", Font.BOLD, 18));
        btnModifier.setAlignmentX(CENTER_ALIGNMENT); 
        mainPanel.add(btnModifier);
        
        // Espace flexible pour pousser le contenu vers le haut
        mainPanel.add(Box.createVerticalGlue());
    }
    
    /**
     * Constructeur pour un utilisateur déjà connecté
     */
    public Page_Modif_MDP(String email, Page_Utilisateur pageParente) {
        this(); // Appel du constructeur par défaut
        this.pageParente = pageParente;
        if (email != null && !email.trim().isEmpty()) {
            txtEmail.setText(email);
            txtEmail.setEditable(false); // Empêche la modification de l'email
        }
    }
    
    // Getters pour le contrôleur
    public JTextField getTxtEmail() {
        return txtEmail;
    }
    
    public JPasswordField getPasswordFieldNouveau() {
        return passwordFieldNouveau;
    }
    
    public JPasswordField getPasswordFieldConfirmer() {
        return passwordFieldConfirmer;
    }
    
    public JButton getBtnModifier() {
        return btnModifier;
    }
    
    public JButton getBtnRetour() {
        return btnRetour;
    }
    
    public Page_Utilisateur getPageParente() {
        return pageParente;
    }
    
    // Méthode pour vider les champs
    public void viderChamps() {
        if (passwordFieldNouveau != null) {
            passwordFieldNouveau.setText("");
        }
        if (passwordFieldConfirmer != null) {
            passwordFieldConfirmer.setText("");
        }
    }
    
    // Méthode pour afficher un message
    public void afficherMessage(String message, String titre, int type) {
        JOptionPane.showMessageDialog(this, message, titre, type);
    }
    
    // Méthode pour demander une confirmation
    public int demanderConfirmation(String message, String titre) {
        return JOptionPane.showConfirmDialog(this, message, titre,
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }
}
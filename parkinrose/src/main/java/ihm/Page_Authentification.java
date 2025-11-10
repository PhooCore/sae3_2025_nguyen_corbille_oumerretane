package ihm;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import dao.AuthentificationDAO;

public class Page_Authentification extends JFrame {
	
	/**
	 * Identifiant de version pour la sérialisation
	 */
	private static final long serialVersionUID = 1L;
	
	// Déclaration des composants de l'interface
	public JPanel contentPanel;
	public JLabel lblTitre;
	public JLabel lblEmail;
	public JTextField txtEmail;
	public JLabel lblPassword;
	public JPasswordField txtPassword;
	public JLabel lblForgotPassword;
	public JLabel lblCreateAccount;
	public JButton btnLogin;

	/**
	 * Constructeur de la page d'authentification
	 * Initialise l'interface utilisateur et les écouteurs d'événements
	 */
	public Page_Authentification() {
		// Configuration de la fenêtre principale
		this.setTitle("User Login");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ferme l'application quand on clique sur X
		this.setSize(800, 600); // Taille fixe de la fenêtre
		this.setLocationRelativeTo(null); // Centre la fenêtre sur l'écran
		this.setResizable(false); // Empêche le redimensionnement
		
		// Création du panel principal
		contentPanel = new JPanel();
		contentPanel.setBackground(Color.WHITE); // Fond blanc
		contentPanel.setLayout(null); // Layout absolu (positionnement manuel)
		this.setContentPane(contentPanel); // Définit le panel comme contenu de la fenêtre
		
		// Titre de la page
		lblTitre = new JLabel("Connexion");
		lblTitre.setFont(new Font("Arial", Font.BOLD, 32)); // Police gras, taille 32
		lblTitre.setHorizontalAlignment(SwingConstants.CENTER); // Centrage horizontal
		lblTitre.setForeground(Color.BLACK); // Couleur du texte
		lblTitre.setBounds(200, 40, 400, 50); // Position et taille (x, y, largeur, hauteur)
		contentPanel.add(lblTitre);

		// Label pour l'email
		lblEmail = new JLabel("Email:");
		lblEmail.setFont(new Font("Arial", Font.PLAIN, 18));
		lblEmail.setForeground(Color.BLACK);
		lblEmail.setBounds(50, 130, 100, 25);
		contentPanel.add(lblEmail);
		
		// Champ de saisie pour l'email
		txtEmail = new JTextField();
		txtEmail.setFont(new Font("Arial", Font.PLAIN, 16));
		txtEmail.setBackground(Color.WHITE);
		// Bordure composée : ligne grise + padding interne
		txtEmail.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.GRAY),
			BorderFactory.createEmptyBorder(8, 12, 8, 12) // padding: haut, gauche, bas, droite
		));
		txtEmail.setBounds(50, 160, 700, 40); // Large champ qui prend presque toute la largeur
		contentPanel.add(txtEmail);

		// Label pour le mot de passe
		lblPassword = new JLabel("Mot de passe:");
		lblPassword.setFont(new Font("Arial", Font.PLAIN, 18));
		lblPassword.setForeground(Color.BLACK);
		lblPassword.setBounds(50, 220, 150, 25);
		contentPanel.add(lblPassword);
		
		// Champ de saisie pour le mot de passe (masqué)
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
		lblForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main au survol
		lblForgotPassword.setBounds(50, 300, 200, 20);
		contentPanel.add(lblForgotPassword);

		// Bouton de connexion
		btnLogin = new JButton("SE CONNECTER");
        btnLogin.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		Page_Accueil pA = new Page_Accueil();
        		pA.setVisible(true);
        	}
        });
		btnLogin.setFont(new Font("Arial", Font.BOLD, 18));
		btnLogin.setForeground(Color.WHITE);
		btnLogin.setBackground(new Color(80, 80, 80)); // Gris foncé
		btnLogin.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30)); // Padding interne
		btnLogin.setFocusPainted(false); // Désactive l'effet de focus
		btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main au survol
		btnLogin.setBounds(50, 350, 700, 50);
		contentPanel.add(btnLogin);

		// Lien "Créer un compte"
		lblCreateAccount = new JLabel("Créer un compte", SwingConstants.CENTER);
		lblCreateAccount.setFont(new Font("Arial", Font.BOLD, 16));
		lblCreateAccount.setForeground(new Color(0, 100, 200)); // Bleu
		lblCreateAccount.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lblCreateAccount.setBounds(50, 420, 700, 30);
		contentPanel.add(lblCreateAccount);
		
		// Initialisation des écouteurs d'événements
		initializeEventListeners();
	}
	
	/**
	 * Initialise tous les écouteurs d'événements pour les interactions utilisateur
	 */
	private void initializeEventListeners() {
	    // Écouteur pour le bouton de connexion
	    btnLogin.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            // Récupération des valeurs saisies
	            String email = txtEmail.getText().trim(); // trim() enlève les espaces inutiles
	            String password = new String(txtPassword.getPassword()); // Conversion du char[] en String
	            
	            // Vérification des identifiants avec la base de données
	            boolean authentifie = AuthentificationDAO.verifierUtilisateur(email, password);
	            
	            if (authentifie) {
	                // Récupération des informations supplémentaires de l'utilisateur
	                String[] infosUtilisateur = AuthentificationDAO.getInfosUtilisateur(email);
	                
	                if (infosUtilisateur != null) {
	                    String nom = infosUtilisateur[0];
	                    String prenom = infosUtilisateur[1];
	                    
	                    // Redirection vers la page principale après connexion réussie
	                    Page_Principale pagePrincipale = new Page_Principale(email);
	                    pagePrincipale.setVisible(true);
	                    dispose(); // Ferme la page de connexion
	                    
	                } else {
	                    // Erreur si les infos utilisateur n'ont pas pu être récupérées
	                    JOptionPane.showMessageDialog(null, 
	                        "Erreur lors de la récupération des informations", 
	                        "Erreur", 
	                        JOptionPane.ERROR_MESSAGE);
	                }
	                
	            } else {
	                // Message d'erreur en cas d'identifiants incorrects
	                JOptionPane.showMessageDialog(null, 
	                    "Email ou mot de passe incorrect", 
	                    "Erreur d'authentification", 
	                    JOptionPane.ERROR_MESSAGE);
	                    
	                // Réinitialisation du champ mot de passe et focus sur l'email
	                txtPassword.setText("");
	                txtEmail.requestFocus();
	            }
	        }
	    });
	    
	    // Écouteur pour le lien "Mot de passe oublié"
	    lblForgotPassword.addMouseListener(new MouseAdapter() {
	        public void mouseClicked(MouseEvent e) {
	            // Redirection vers la page de modification de mot de passe
	            Page_Modif_MDP modifMdpPage = new Page_Modif_MDP();
	            modifMdpPage.setVisible(true);
	            dispose(); // Ferme la page actuelle
	        }
	    });
	    
	    // Écouteur pour le lien "Créer un compte"
	    lblCreateAccount.addMouseListener(new MouseAdapter() {
	        public void mouseClicked(MouseEvent e) {
	            // Redirection vers la page d'inscription
	            Page_Inscription registrationPage = new Page_Inscription();
	            registrationPage.setVisible(true);
	            dispose(); // Ferme la page actuelle
	        }
	    });
	}
	
	public static void main(String[] args) {
	    java.awt.EventQueue.invokeLater(new Runnable() {
	        public void run() {
	            try {
	                // Démarre avec la page de bienvenue
	                new Page_Bienvenue().setVisible(true);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    });
	}
}
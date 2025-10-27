


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
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JPanel contentPanel;
	public JLabel lblTitre;
	public JLabel lblEmail;
	public JTextField txtEmail;
	public JLabel lblPassword;
	public JPasswordField txtPassword;
	public JLabel lblForgotPassword;
	public JLabel lblCreateAccount;
	public JButton btnLogin;

	public Page_Authentification() {
		this.setTitle("User Login");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(800, 600);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		
		contentPanel = new JPanel();
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setLayout(null);
		this.setContentPane(contentPanel);
		
		lblTitre = new JLabel("Connexion");
		lblTitre.setFont(new Font("Arial", Font.BOLD, 32));
		lblTitre.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitre.setForeground(Color.BLACK);
		lblTitre.setBounds(200, 40, 400, 50);
		contentPanel.add(lblTitre);

		lblEmail = new JLabel("Email:");
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

		lblPassword = new JLabel("Mot de passe:");
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

		lblForgotPassword = new JLabel("Mot de passe oublié ?");
		lblForgotPassword.setFont(new Font("Arial", Font.PLAIN, 14));
		lblForgotPassword.setForeground(Color.DARK_GRAY);
		lblForgotPassword.setHorizontalAlignment(SwingConstants.LEFT);
		lblForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lblForgotPassword.setBounds(50, 300, 200, 20);
		contentPanel.add(lblForgotPassword);

		btnLogin = new JButton("SE CONNECTER");
		btnLogin.setFont(new Font("Arial", Font.BOLD, 18));
		btnLogin.setForeground(Color.WHITE);
		btnLogin.setBackground(new Color(80, 80, 80));
		btnLogin.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
		btnLogin.setFocusPainted(false);
		btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnLogin.setBounds(50, 350, 700, 50);
		contentPanel.add(btnLogin);

		lblCreateAccount = new JLabel("Créer un compte", SwingConstants.CENTER);
		lblCreateAccount.setFont(new Font("Arial", Font.BOLD, 16));
		lblCreateAccount.setForeground(new Color(0, 100, 200));
		lblCreateAccount.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lblCreateAccount.setBounds(50, 420, 700, 30);
		contentPanel.add(lblCreateAccount);
		
		initializeEventListeners();

	}
	
	private void initializeEventListeners() {
	    btnLogin.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            String email = txtEmail.getText().trim();
	            String password = new String(txtPassword.getPassword());
	            
	            // Vérification normale avec la base de données
	            boolean authentifie = AuthentificationDAO.verifierUtilisateur(email, password);
	            
	            if (authentifie) {
	                // Récupérer les infos de l'utilisateur
	                String[] infosUtilisateur = AuthentificationDAO.getInfosUtilisateur(email);
	                
	                if (infosUtilisateur != null) {
	                    String nom = infosUtilisateur[0];
	                    String prenom = infosUtilisateur[1];
	                    
	                    // Ouvrir Page_Garer_Voirie directement
	                    Page_Garer_Voirie pageGarer = new Page_Garer_Voirie(email);
	                    pageGarer.setVisible(true);
	                    dispose();
	                    
	                } else {
	                    JOptionPane.showMessageDialog(null, 
	                        "Erreur lors de la récupération des informations", 
	                        "Erreur", 
	                        JOptionPane.ERROR_MESSAGE);
	                }
	                
	            } else {
	                JOptionPane.showMessageDialog(null, 
	                    "Email ou mot de passe incorrect", 
	                    "Erreur d'authentification", 
	                    JOptionPane.ERROR_MESSAGE);
	                    
	                // Vider les champs
	                txtPassword.setText("");
	                txtEmail.requestFocus();
	            }
	        }
	    });
	    
	    // Garder les autres écouteurs
	    lblForgotPassword.addMouseListener(new MouseAdapter() {
	        public void mouseClicked(MouseEvent e) {
	            Page_Modif_MDP modifMdpPage = new Page_Modif_MDP();
	            modifMdpPage.setVisible(true);
	            dispose();
	        }
	    });
	    
	    lblCreateAccount.addMouseListener(new MouseAdapter() {
	        public void mouseClicked(MouseEvent e) {
	            Page_Inscription registrationPage = new Page_Inscription();
	            registrationPage.setVisible(true);
	            dispose();
	        }
	    });
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Page_Bienvenue frame = new Page_Bienvenue();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}

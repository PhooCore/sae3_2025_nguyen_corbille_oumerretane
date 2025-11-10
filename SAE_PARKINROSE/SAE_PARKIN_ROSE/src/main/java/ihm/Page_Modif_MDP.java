package ihm;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import dao.ModifMdpDAO;

public class Page_Modif_MDP extends JFrame {
	/**
	 * Identifiant de version pour la sérialisation
	 */
	private static final long serialVersionUID = 1L;
	
	// Déclaration des composants de formulaire
	private JTextField txtEmail;
	private JPasswordField passwordFieldNouveau;
	private JPasswordField passwordFieldConfirmer;

	/**
	 * Constructeur de la page de modification de mot de passe
	 * Permet à un utilisateur de réinitialiser son mot de passe
	 */
	public Page_Modif_MDP() {
		setTitle("Modifier le mot de passe");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(null); // Centre la fenêtre
		

		// Panel principal avec layout vertical
		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 40, 100)); // Marges généreuses
		setContentPane(mainPanel);
		

		// Panel de retour en haut à gauche
		JPanel panelRetour = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panelRetour.setBackground(Color.WHITE);
		panelRetour.setMaximumSize(new Dimension(600, 40)); // Largeur limitée
		mainPanel.add(panelRetour);
		

		// Bouton retour vers la page de connexion
		JButton btnRetour = new JButton("← Retour");
		btnRetour.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				retourProfil();
			}
		});
		btnRetour.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Curseur main
		btnRetour.setFocusPainted(false); // Désactive l'effet de focus
		btnRetour.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding interne
		btnRetour.setBackground(Color.WHITE);
		btnRetour.setForeground(new Color(80, 80, 80)); // Gris foncé
		btnRetour.setFont(new Font("Arial", Font.PLAIN, 14));
		panelRetour.add(btnRetour);
		
		// Espacement après le bouton retour
		mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		

		// Titre de la page
		JLabel lblTitre = new JLabel("Modifier le mot de passe", SwingConstants.CENTER);
		lblTitre.setFont(new Font("Arial", Font.BOLD, 32));
		lblTitre.setAlignmentX(CENTER_ALIGNMENT); // Centrage horizontal dans BoxLayout
		mainPanel.add(lblTitre);
		
		// Espacement après le titre
		mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));
		

		// === PANEL EMAIL ===
		JPanel panelEmail = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panelEmail.setBackground(Color.WHITE);
		panelEmail.setMaximumSize(new Dimension(600, 50)); // Largeur limitée pour centrage
		
		// Label "Email"
		JLabel lblEmail = new JLabel("Email:");
		lblEmail.setFont(new Font("Arial", Font.PLAIN, 16));
		lblEmail.setPreferredSize(new Dimension(200, 30)); // Taille fixe pour l'alignement
		panelEmail.add(lblEmail);
		
		// Champ de saisie pour l'email
		txtEmail = new JTextField();
		txtEmail.setFont(new Font("Arial", Font.PLAIN, 16));
		txtEmail.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.GRAY), // Bordure grise
			BorderFactory.createEmptyBorder(8, 12, 8, 12) // Padding interne
		));
		txtEmail.setPreferredSize(new Dimension(350, 40)); // Taille fixe
		panelEmail.add(txtEmail);
		
		mainPanel.add(panelEmail);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Espacement
		

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
		mainPanel.add(Box.createRigidArea(new Dimension(0, 40))); // Espacement avant le bouton
		

		// === BOUTON DE MODIFICATION ===
		JButton btnModifier = new JButton("MODIFIER LE MOT DE PASSE");
		btnModifier.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modifierMotDePasse(); // Action au clic
			}
		});
		btnModifier.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnModifier.setFocusPainted(false);
		btnModifier.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30)); // Padding généreux
		btnModifier.setBackground(new Color(80, 80, 80)); // Gris foncé
		btnModifier.setForeground(Color.WHITE);
		btnModifier.setFont(new Font("Arial", Font.BOLD, 18));
		btnModifier.setAlignmentX(CENTER_ALIGNMENT); // Centrage horizontal
		mainPanel.add(btnModifier);
		
		// Espace flexible pour pousser le contenu vers le haut
		mainPanel.add(Box.createVerticalGlue());
	}

	/**
	 * Retourne à la page d'authentification
	 * Appelé quand l'utilisateur clique sur "Retour"
	 */
	protected void retourProfil() {
		Page_Authentification loginPage = new Page_Authentification();
		loginPage.setVisible(true);
		this.dispose(); // Ferme la page actuelle
	}

	/**
	 * Modifie le mot de passe après validation
	 * Gère tout le processus de modification sécurisé
	 */
	protected void modifierMotDePasse() {
		// Récupération et nettoyage des données
		String email = txtEmail.getText().trim();
		String nouveauMotDePasse = new String(passwordFieldNouveau.getPassword());
		String confirmerMotDePasse = new String(passwordFieldConfirmer.getPassword());

		// === VALIDATION DES CHAMPS OBLIGATOIRES ===
		if (email.isEmpty() || nouveauMotDePasse.isEmpty() || confirmerMotDePasse.isEmpty()) {
			JOptionPane.showMessageDialog(this, 
				"Veuillez remplir tous les champs", 
				"Erreur", 
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		// === VALIDATION DE LA CORRESPONDANCE DES MOTS DE PASSE ===
		if (!nouveauMotDePasse.equals(confirmerMotDePasse)) {
			JOptionPane.showMessageDialog(this, 
				"Les mots de passe ne correspondent pas", 
				"Erreur", 
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		// === VALIDATION DE LA LONGUEUR MINIMALE ===
		if (nouveauMotDePasse.length() < 6) {
			JOptionPane.showMessageDialog(this, 
				"Le mot de passe doit contenir au moins 6 caractères", 
				"Erreur", 
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		// === VÉRIFICATION DE L'EXISTENCE DE L'EMAIL ===
		ModifMdpDAO dao = new ModifMdpDAO();
		boolean emailExiste = dao.verifierEmailExiste(email);
		
		if (!emailExiste) {
			JOptionPane.showMessageDialog(this, 
				"Email non trouvé", 
				"Erreur", 
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		// === MODIFICATION EFFECTIVE DU MOT DE PASSE ===
		boolean modificationReussie = dao.modifierMotDePasse(email, nouveauMotDePasse);
		
		if (modificationReussie) {
			// Message de succès
			JOptionPane.showMessageDialog(this, 
				"Mot de passe modifié avec succès !", 
				"Succès", 
				JOptionPane.INFORMATION_MESSAGE);
			
			// Retour à la page de connexion
			retourProfil();
		} else {
			// Message d'erreur en cas d'échec
			JOptionPane.showMessageDialog(this, 
				"Erreur lors de la modification du mot de passe", 
				"Erreur", 
				JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void main(String[] args) {
	    java.awt.EventQueue.invokeLater(new Runnable() {
	        public void run() {
	            try {
	                new Page_Bienvenue().setVisible(true);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    });
	}
}
package ihm;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import controleur.UtilisateurControleur;
import modele.dao.ModifMdpDAO;

public class Page_Modif_MDP extends JFrame {
	/**
	 * Identifiant de version pour la sérialisation
	 */
	private static final long serialVersionUID = 1L;
	
	// Déclaration des composants de formulaire
	private JTextField txtEmail;
	private JPasswordField passwordFieldNouveau;
	private JPasswordField passwordFieldConfirmer;
	private Page_Utilisateur pageParente;
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
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 40, 100)); 
		setContentPane(mainPanel);
		

		// Panel de retour en haut à gauche
		JPanel panelRetour = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panelRetour.setBackground(Color.WHITE);
		panelRetour.setMaximumSize(new Dimension(600, 40)); 
		mainPanel.add(panelRetour);
		

		// Bouton retour vers la page de connexion
		JButton btnRetour = new JButton("← Retour");
		btnRetour.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				retourProfil();
			}
		});
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
		JButton btnModifier = new JButton("MODIFIER LE MOT DE PASSE");
		btnModifier.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modifierMotDePasse(); 
			}
		});
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
	 * @param email l'email de l'utilisateur connecté
	 * @param pageParente la page utilisateur parente
	 */
	public Page_Modif_MDP(String email, Page_Utilisateur pageParente) {
	    this(); // Appel du constructeur par défaut
	    this.pageParente = pageParente; // Stocke la référence à la page parente
	    if (email != null) {
	        txtEmail.setText(email);
	        txtEmail.setEditable(false); // Empêche la modification de l'email
	    }
	}

	/**
	 * Retourne à la page appropriée
	 */
	protected void retourProfil() {
	    if (pageParente != null) {
	        // Si on a une page parente, on retourne vers elle
	        pageParente.setVisible(true); // Rend la page parente visible
	        this.dispose(); // Ferme cette page
	    } else if (txtEmail.getText() != null && !txtEmail.getText().trim().isEmpty() && !txtEmail.isEditable()) {
	        // L'utilisateur était connecté mais pas de page parente
	        // On crée une nouvelle page utilisateur
	        Page_Utilisateur pageUtilisateur = new Page_Utilisateur(txtEmail.getText());
	        pageUtilisateur.setVisible(true);
	        this.dispose();
	    } else {
	        // L'utilisateur n'était pas connecté
	        Page_Authentification loginPage = new Page_Authentification();
	        loginPage.setVisible(true);
	        this.dispose();
	    }
	}

	/**
	 * Modifie le mot de passe après validation
	 * Gère tout le processus de modification sécurisé
	 */
	protected void modifierMotDePasse() {
	    UtilisateurControleur controleur = new UtilisateurControleur(null); // Pas d'email car pas encore connecté
	    
	    String email = txtEmail.getText().trim();
	    String nouveauMotDePasse = new String(passwordFieldNouveau.getPassword());
	    String confirmerMotDePasse = new String(passwordFieldConfirmer.getPassword());
	    
	    // Pour cette page spécifique, on utilise directement le DAO
	    // car l'utilisateur n'est pas forcément connecté
	    ModifMdpDAO dao = new ModifMdpDAO();
	    
	    // Validation des champs obligatoires
	    if (email.isEmpty() || nouveauMotDePasse.isEmpty() || confirmerMotDePasse.isEmpty()) {
	        JOptionPane.showMessageDialog(this, 
	            "Veuillez remplir tous les champs", 
	            "Erreur", 
	            JOptionPane.ERROR_MESSAGE);
	        return;
	    }

	    // Validation de la correspondance des mots de passe
	    if (!nouveauMotDePasse.equals(confirmerMotDePasse)) {
	        JOptionPane.showMessageDialog(this, 
	            "Les mots de passe ne correspondent pas", 
	            "Erreur", 
	            JOptionPane.ERROR_MESSAGE);
	        return;
	    }

	    // Validation de la longueur minimale
	    if (nouveauMotDePasse.length() < 6) {
	        JOptionPane.showMessageDialog(this, 
	            "Le mot de passe doit contenir au moins 6 caractères", 
	            "Erreur", 
	            JOptionPane.ERROR_MESSAGE);
	        return;
	    }

	    // Vérification de l'existence de l'email
	    boolean emailExiste = dao.verifierEmailExiste(email);
	    
	    if (!emailExiste) {
	        JOptionPane.showMessageDialog(this, 
	            "Email non trouvé", 
	            "Erreur", 
	            JOptionPane.ERROR_MESSAGE);
	        return;
	    }

	    // Modification effective du mot de passe
	    boolean modificationReussie = dao.modifierMotDePasse(email, nouveauMotDePasse);
	    
	    if (modificationReussie) {
	        JOptionPane.showMessageDialog(this, 
	            "Mot de passe modifié avec succès !", 
	            "Succès", 
	            JOptionPane.INFORMATION_MESSAGE);
	        retourProfil();
	    } else {
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
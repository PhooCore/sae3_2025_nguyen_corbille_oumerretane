package ihm;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.*;

public class Page_Bienvenue extends JFrame {
	
	/**
	 * Identifiant de version pour la sérialisation
	 */
	private static final long serialVersionUID = 1L;
	
	// Déclaration des composants de l'interface
	private JPanel contentPanel;
	private JLabel lblLogo;
	private JLabel lblTitre;
	private JButton btnEntrer;

	/**
	 * Constructeur de la page de bienvenue
	 * Page d'accueil de l'application avec logo et bouton d'entrée
	 */
	public Page_Bienvenue() {
		// Configuration de la fenêtre principale
		this.setTitle("Application - Accueil");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Quitte l'application quand on ferme
		this.setSize(800, 600); // Taille fixe
		this.setLocationRelativeTo(null); // Centre la fenêtre
		this.setResizable(false); // Empêche le redimensionnement
		
		// Création du panel principal avec layout vertical (BoxLayout)
		contentPanel = new JPanel();
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS)); // Alignement vertical
		this.setContentPane(contentPanel); // Définit comme contenu principal
		
		// Ajout d'un espace vide en haut pour centrer verticalement
		contentPanel.add(Box.createRigidArea(new Dimension(0, 80)));
		
		// Panel pour le logo
		JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		logoPanel.setBackground(Color.WHITE);
		
		// Chargement du logo depuis les ressources
		ImageIcon logoIcon = null;
		try {
			String imagePath = "/images/IMG_8155.png"; // Chemin relatif dans le classpath
			URL imageUrl = getClass().getResource(imagePath); // Récupération de l'URL de l'image
			if (imageUrl != null) {
				logoIcon = new ImageIcon(imageUrl); // Création de l'icône si l'image est trouvée
			} else {
				System.err.println("Image non trouvée: " + imagePath); // Message d'erreur en console
			}
		} catch (Exception e) {
			System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
		}
		
		// Configuration du label pour le logo
		lblLogo = new JLabel();
		if (logoIcon != null) {
			try {
				// Redimensionnement de l'image à 80% de sa taille originale
				int originalWidth = logoIcon.getIconWidth();
				int originalHeight = logoIcon.getIconHeight();
				int newWidth = (int)(originalWidth * 0.8); // 80% de la largeur
				int newHeight = (int)(originalHeight * 0.8); // 80% de la hauteur
				
				// Création d'une nouvelle icône redimensionnée avec lissage
				ImageIcon scaledIcon = new ImageIcon(logoIcon.getImage().getScaledInstance(
					newWidth, newHeight, java.awt.Image.SCALE_SMOOTH));
				lblLogo.setIcon(scaledIcon);
			} catch (Exception e) {
				// En cas d'erreur de redimensionnement, utilisation de l'icône originale
				lblLogo.setIcon(logoIcon);
			}
		} else {
			// Fallback si l'image n'est pas trouvée : affichage d'un texte "LOGO"
			lblLogo.setText("LOGO");
			lblLogo.setFont(new Font("Arial", Font.BOLD, 24));
			lblLogo.setForeground(new Color(150, 150, 150)); // Gris moyen
		}
		
		// Centrage du logo
		lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
		logoPanel.add(lblLogo);
		contentPanel.add(logoPanel);
		
		// Espace entre le logo et le titre
		contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
		
		// Panel pour le titre - centré horizontalement
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		titlePanel.setBackground(Color.WHITE);
		
		// Configuration du titre
		lblTitre = new JLabel("Bienvenue !");
		lblTitre.setFont(new Font("Arial", Font.BOLD, 32)); // Police gras, taille 32
		lblTitre.setForeground(new Color(60, 60, 60)); // Gris foncé
		lblTitre.setHorizontalAlignment(SwingConstants.CENTER); // Centrage
		
		titlePanel.add(lblTitre);
		contentPanel.add(titlePanel);
		
		// Panel pour le bouton - centré horizontalement
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setBackground(Color.WHITE);
		
		// Configuration du bouton "ENTRER"
		btnEntrer = new JButton("ENTRER");
		btnEntrer.setFont(new Font("Arial", Font.BOLD, 18));
		btnEntrer.setForeground(Color.WHITE); // Texte blanc
		btnEntrer.setBackground(new Color(70, 130, 180)); // Bleu
		btnEntrer.setBorder(BorderFactory.createEmptyBorder(12, 50, 12, 50)); // Padding généreux
		btnEntrer.setFocusPainted(false); // Désactive l'effet de focus
		btnEntrer.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main au survol
		
		buttonPanel.add(btnEntrer);
		contentPanel.add(buttonPanel);
		
		// Ajout d'un espace flexible pour pousser le contenu vers le haut (centrage vertical)
		contentPanel.add(Box.createVerticalGlue());
		
		// Ajout de l'écouteur d'événement pour le bouton
		btnEntrer.addActionListener(new BtnEntrerActionListener());
	}

	/**
	 * Classe interne pour gérer l'action du bouton "ENTRER"
	 */
	private class BtnEntrerActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Création et affichage de la page d'authentification
			Page_Authentification loginPage = new Page_Authentification();
			loginPage.setVisible(true);
			
			// Fermeture de la page de bienvenue actuelle
			dispose();
		}
	}


	public static void main(String[] args) {
	    java.awt.EventQueue.invokeLater(new Runnable() {
	        public void run() {
	            try {
	                // Création et affichage de la page de bienvenue
	                new Page_Bienvenue().setVisible(true);
	            } catch (Exception e) {
	                e.printStackTrace(); // Affichage des erreurs éventuelles
	            }
	        }
	    });
	}
}
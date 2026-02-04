package ihm;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.*;

import controleur.ControleurBienvenue;

public class Page_Bienvenue extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private JPanel contentPanel;
    private JLabel lblLogo;
    private JLabel lblTitre;
    private JButton btnEntrer;

    public Page_Bienvenue() {
        this.setTitle("Application - Accueil");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        this.setSize(800, 600);
        this.setLocationRelativeTo(null); 
        this.setResizable(false);
        
        contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS)); 
        this.setContentPane(contentPanel);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 80)));
        
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setBackground(Color.WHITE);
        
        ImageIcon logoIcon = null;
        try {
            String imagePath = "/images/IMG_8155.png";
            URL imageUrl = getClass().getResource(imagePath); 
            if (imageUrl != null) {
                logoIcon = new ImageIcon(imageUrl); 
            } else {
                System.err.println("Image non trouvée: " + imagePath); 
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
        }
        
        lblLogo = new JLabel();
        if (logoIcon != null) {
            try {
                int originalWidth = logoIcon.getIconWidth();
                int originalHeight = logoIcon.getIconHeight();
                int newWidth = (int)(originalWidth * 0.8); 
                int newHeight = (int)(originalHeight * 0.8); 
                
                ImageIcon scaledIcon = new ImageIcon(logoIcon.getImage().getScaledInstance(
                    newWidth, newHeight, java.awt.Image.SCALE_SMOOTH));
                lblLogo.setIcon(scaledIcon);
            } catch (Exception e) {
                lblLogo.setIcon(logoIcon);
            }
        } else {
            lblLogo.setText("LOGO");
            lblLogo.setFont(new Font("Arial", Font.BOLD, 24));
            lblLogo.setForeground(new Color(150, 150, 150));
        }
        
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        logoPanel.add(lblLogo);
        contentPanel.add(logoPanel);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(Color.WHITE);
        
        lblTitre = new JLabel("Bienvenue !");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 32)); 
        lblTitre.setForeground(new Color(60, 60, 60)); 
        lblTitre.setHorizontalAlignment(SwingConstants.CENTER); 
        
        titlePanel.add(lblTitre);
        contentPanel.add(titlePanel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        
        setBtnEntrer(new JButton("ENTRER"));
        getBtnEntrer().setFont(new Font("Arial", Font.BOLD, 18));
        getBtnEntrer().setForeground(Color.WHITE); 
        getBtnEntrer().setBackground(new Color(70, 130, 180)); 
        getBtnEntrer().setBorder(BorderFactory.createEmptyBorder(12, 50, 12, 50)); 
        getBtnEntrer().setFocusPainted(false); 
        getBtnEntrer().setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        
        buttonPanel.add(getBtnEntrer());
        contentPanel.add(buttonPanel);

        contentPanel.add(Box.createVerticalGlue());
        new ControleurBienvenue(this);
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

	public JButton getBtnEntrer() {
		return btnEntrer;
	}

	public void setBtnEntrer(JButton btnEntrer) {
		this.btnEntrer = btnEntrer;
	}
}
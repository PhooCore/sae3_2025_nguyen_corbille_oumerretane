package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Page d'administration pour gérer les parkings, utilisateurs, etc.
 */
public class Page_Administration extends JFrame {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String emailAdmin;
    private JPanel contentPane;
    private JButton btnGestionParkings;
    private JButton btnGestionUtilisateurs;
    private JButton btnStatistiques;
    private JButton btnHistoriqueGlobal;
    private JButton btnConfiguration;
    private JButton btnRapports;
    private JButton btnRetour;

    public Page_Administration(String emailAdmin) {
        this.emailAdmin = emailAdmin;
        initialize();
    }

    private void initialize() {
        setTitle("Administration - Parkin'Rose");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 800, 600);
        setLocationRelativeTo(null);
        contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        
        JLabel lblTitre = new JLabel("Panneau d'Administration");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitre.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(lblTitre, BorderLayout.NORTH);
        
        JPanel panelCentre = new JPanel();
        panelCentre.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        contentPane.add(panelCentre, BorderLayout.CENTER);
        panelCentre.setLayout(new GridLayout(2, 3, 15, 15));
        
        btnGestionParkings = new JButton("Gestion des Parkings");
        btnGestionParkings.setFont(new Font("Arial", Font.BOLD, 14));
        btnGestionParkings.setBackground(new Color(70, 130, 180));
        btnGestionParkings.setForeground(Color.WHITE);
        btnGestionParkings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(Page_Administration.this, "À venir", "Gestion des Parkings", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        panelCentre.add(btnGestionParkings);
        
        btnGestionUtilisateurs = new JButton("Gestion Utilisateurs");
        btnGestionUtilisateurs.setFont(new Font("Arial", Font.BOLD, 14));
        btnGestionUtilisateurs.setBackground(new Color(70, 130, 180));
        btnGestionUtilisateurs.setForeground(Color.WHITE);
        btnGestionUtilisateurs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(Page_Administration.this, "À venir", "Gestion des Utilisateurs", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        panelCentre.add(btnGestionUtilisateurs);
        
        btnStatistiques = new JButton("Statistiques");
        btnStatistiques.setFont(new Font("Arial", Font.BOLD, 14));
        btnStatistiques.setBackground(new Color(70, 130, 180));
        btnStatistiques.setForeground(Color.WHITE);
        btnStatistiques.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(Page_Administration.this, "À venir", "Statistiques", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        panelCentre.add(btnStatistiques);
        
        btnHistoriqueGlobal = new JButton("Historique Global");
        btnHistoriqueGlobal.setFont(new Font("Arial", Font.BOLD, 14));
        btnHistoriqueGlobal.setBackground(new Color(70, 130, 180));
        btnHistoriqueGlobal.setForeground(Color.WHITE);
        btnHistoriqueGlobal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(Page_Administration.this, "À venir", "Historique Global", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        panelCentre.add(btnHistoriqueGlobal);
        
        btnConfiguration = new JButton("Configuration");
        btnConfiguration.setFont(new Font("Arial", Font.BOLD, 14));
        btnConfiguration.setBackground(new Color(70, 130, 180));
        btnConfiguration.setForeground(Color.WHITE);
        btnConfiguration.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(Page_Administration.this, "À venir", "Configuration", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        panelCentre.add(btnConfiguration);
        
        btnRapports = new JButton("Rapports");
        btnRapports.setFont(new Font("Arial", Font.BOLD, 14));
        btnRapports.setBackground(new Color(70, 130, 180));
        btnRapports.setForeground(Color.WHITE);
        btnRapports.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(Page_Administration.this, "À venir", "Rapports", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        panelCentre.add(btnRapports);
        
        JPanel panelSud = new JPanel();
        contentPane.add(panelSud, BorderLayout.SOUTH);
        
        btnRetour = new JButton("Retour à l'accueil");
        btnRetour.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Page_Principale pagePrincipale = new Page_Principale(emailAdmin);
                pagePrincipale.setVisible(true);
                dispose();
            }
        });
        panelSud.add(btnRetour);

        appliquerStyleBoutons();
    }
    
    private void appliquerStyleBoutons() {
        Component[] components = contentPane.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                Component[] sousComponents = ((JPanel) comp).getComponents();
                for (Component sousComp : sousComponents) {
                    if (sousComp instanceof JButton) {
                        JButton bouton = (JButton) sousComp;
                        bouton.setFocusPainted(false);
                        bouton.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(50, 110, 160), 2),
                            BorderFactory.createEmptyBorder(10, 5, 10, 5)
                        ));
                        
                        bouton.addMouseListener(new java.awt.event.MouseAdapter() {
                            public void mouseEntered(java.awt.event.MouseEvent evt) {
                                bouton.setBackground(new Color(60, 120, 170));
                            }
                            public void mouseExited(java.awt.event.MouseEvent evt) {
                                bouton.setBackground(new Color(70, 130, 180));
                            }
                        });
                    }
                }
            }
        }
    }
}
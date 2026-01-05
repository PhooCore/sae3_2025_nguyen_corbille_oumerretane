package controleur;

import ihm.Page_Administration;
import ihm.PageGestionUtilisateurs;
import ihm.Page_Principale;
import ihm.CarteAdminOSMPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ControleurAdministration implements ActionListener {
    
    // États du contrôleur
    private enum Etat {
        INITIAL,
        ADMINISTRATION,
        GESTION_UTILISATEURS,
        GESTION_PARKINGS,
        RETOUR
    }
    
    // Références
    private Page_Administration vue;
    private Etat etat;
    
    // Données
    private String emailAdmin;
    
    public ControleurAdministration(Page_Administration vue, String emailAdmin) {
        this.vue = vue;
        this.emailAdmin = emailAdmin;
        this.etat = Etat.INITIAL;
        
        initialiserControleur();
    }
    
    private void initialiserControleur() {
        if (verifierDroitsAdmin()) {
            etat = Etat.ADMINISTRATION;
            configurerListeners();
        } else {
            JOptionPane.showMessageDialog(vue,
                "Vous n'avez pas les droits d'administration nécessaires.",
                "Accès refusé",
                JOptionPane.ERROR_MESSAGE);
            retourAccueil();
        }
    }
    
    private void configurerListeners() {
        // Bouton retour
        vue.getBtnRetour().addActionListener(this);
        
        // Configurer les panels d'options
        configurerPanelUtilisateurs();
        configurerPanelParkings();
    }
    
    private void configurerPanelUtilisateurs() {
        JPanel panelUtilisateurs = vue.getPanelUtilisateurs();
        if (panelUtilisateurs != null) {
            panelUtilisateurs.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    etat = Etat.GESTION_UTILISATEURS;
                    ouvrirGestionUtilisateurs();
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    vue.survolPanelUtilisateurs(true);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    vue.survolPanelUtilisateurs(false);
                }
            });
        }
    }
    
    private void configurerPanelParkings() {
        JPanel panelParkings = vue.getPanelParkings();
        if (panelParkings != null) {
            panelParkings.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    etat = Etat.GESTION_PARKINGS;
                    ouvrirCarteParkingsAdmin();
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    vue.survolPanelParkings(true);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    vue.survolPanelParkings(false);
                }
            });
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == vue.getBtnRetour()) {
            etat = Etat.RETOUR;
            retourAccueil();
        }
    }
    
    private void ouvrirGestionUtilisateurs() {
        PageGestionUtilisateurs pageGestion = new PageGestionUtilisateurs(emailAdmin);
        pageGestion.setVisible(true);
        vue.dispose();
    }
    
    private void ouvrirCarteParkingsAdmin() {
        // Créer une fenêtre pour la carte d'administration
        JFrame frameCarte = new JFrame("Administration des Parkings - Carte Interactive");
        frameCarte.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCarte.setSize(1300, 850);
        frameCarte.setLocationRelativeTo(null);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Panel d'en-tête
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel lblTitre = new JLabel("Carte Interactive des Parkings - Administration");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitre.setForeground(new Color(0, 102, 204));
        
        JLabel lblInstructions = new JLabel("Ajoutez, modifiez ou supprimez des parkings directement sur la carte");
        lblInstructions.setFont(new Font("Arial", Font.ITALIC, 12));
        lblInstructions.setForeground(Color.DARK_GRAY);
        
        headerPanel.add(lblTitre, BorderLayout.NORTH);
        headerPanel.add(lblInstructions, BorderLayout.SOUTH);
        
        // Bouton retour
        JButton btnRetour = new JButton("← Retour");
        btnRetour.addActionListener(e -> {
            frameCarte.dispose();
            vue.setVisible(true);
        });
        headerPanel.add(btnRetour, BorderLayout.EAST);
        
        // Carte d'administration
        CarteAdminOSMPanel cartePanel = new CarteAdminOSMPanel(emailAdmin);
        
        // Ajouter les composants
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(cartePanel, BorderLayout.CENTER);
        frameCarte.add(mainPanel);
        
        // Gérer la fermeture
        frameCarte.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cartePanel.nettoyer();
            }
        });
        
        frameCarte.setVisible(true);
        vue.setVisible(false);
    }
    
    private void retourAccueil() {
        Page_Principale pagePrincipale = new Page_Principale(emailAdmin);
        pagePrincipale.setVisible(true);
        vue.dispose();
    }
    
    private boolean verifierDroitsAdmin() {
        return emailAdmin != null && (emailAdmin.contains("admin") || emailAdmin.equals("admin@pr.com"));
    }
    
    // Getters pour le débogage
    public Etat getEtat() {
        return etat;
    }
    
    public String getEmailAdmin() {
        return emailAdmin;
    }
}
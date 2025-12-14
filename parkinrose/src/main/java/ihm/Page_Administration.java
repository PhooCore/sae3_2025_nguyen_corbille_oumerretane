package ihm;

import javax.swing.*;
import controleur.ControleurAdministration;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modele.dao.ParkingDAO;
import modele.Parking;
import java.io.IOException;
import java.text.DecimalFormat;

public class Page_Administration extends JFrame {
    private static final long serialVersionUID = 1L;
    public enum ModeCarte {
        NAVIGATION,
        AJOUT,
        MODIFICATION
    }
    private String emailAdmin;
    private JPanel contentPane;
    private JButton btnGestionParkings;
    private JButton btnGestionUtilisateurs;
    private JButton btnStatistiques;
    private JButton btnHistoriqueGlobal;
    private JButton btnConfiguration;
    private JButton btnRapports;
    private JButton btnRetour;
    private JButton btnGestionCarte; // Nouveau bouton
    
    public Page_Administration(String emailAdmin) {
        this.emailAdmin = emailAdmin;
        initialize();
        new ControleurAdministration(this, emailAdmin);
    }

    private void initialize() {
        setTitle("Administration - Parkin'Rose");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 1000, 700);
        setLocationRelativeTo(null);
        contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        
        // Panel supérieur avec titre et bouton carte
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblTitre = new JLabel("Panneau d'Administration");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitre.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(lblTitre, BorderLayout.CENTER);
        
        
        contentPane.add(topPanel, BorderLayout.NORTH);
        
        // Panel centre avec les boutons d'administration
        JPanel panelCentre = new JPanel();
        panelCentre.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        contentPane.add(panelCentre, BorderLayout.CENTER);
        panelCentre.setLayout(new GridLayout(2, 3, 15, 15));
        
        btnGestionParkings = new JButton("<html><center>Gestion des<br>Parkings</center></html>");
        btnGestionParkings.setFont(new Font("Arial", Font.BOLD, 14));
        btnGestionParkings.setBackground(new Color(70, 130, 180));
        btnGestionParkings.setForeground(Color.WHITE);
        btnGestionParkings.addActionListener(e -> ouvrirCarteParkings());
        panelCentre.add(btnGestionParkings);
        
        btnGestionUtilisateurs = new JButton("<html><center>Gestion<br>Utilisateurs</center></html>");
        btnGestionUtilisateurs.setFont(new Font("Arial", Font.BOLD, 14));
        btnGestionUtilisateurs.setBackground(new Color(70, 130, 180));
        btnGestionUtilisateurs.setForeground(Color.WHITE);
        panelCentre.add(btnGestionUtilisateurs);
        
        btnStatistiques = new JButton("<html><center>Statistiques<br>& Analyses</center></html>");
        btnStatistiques.setFont(new Font("Arial", Font.BOLD, 14));
        btnStatistiques.setBackground(new Color(70, 130, 180));
        btnStatistiques.setForeground(Color.WHITE);
        panelCentre.add(btnStatistiques);
        
        btnHistoriqueGlobal = new JButton("<html><center>Historique<br>Global</center></html>");
        btnHistoriqueGlobal.setFont(new Font("Arial", Font.BOLD, 14));
        btnHistoriqueGlobal.setBackground(new Color(70, 130, 180));
        btnHistoriqueGlobal.setForeground(Color.WHITE);
        panelCentre.add(btnHistoriqueGlobal);
        
        btnConfiguration = new JButton("<html><center>Configuration<br>du Système</center></html>");
        btnConfiguration.setFont(new Font("Arial", Font.BOLD, 14));
        btnConfiguration.setBackground(new Color(70, 130, 180));
        btnConfiguration.setForeground(Color.WHITE);
        panelCentre.add(btnConfiguration);
        
        btnRapports = new JButton("<html><center>Génération<br>de Rapports</center></html>");
        btnRapports.setFont(new Font("Arial", Font.BOLD, 14));
        btnRapports.setBackground(new Color(70, 130, 180));
        btnRapports.setForeground(Color.WHITE);
        panelCentre.add(btnRapports);
        
        // Panel inférieur avec bouton retour
        JPanel panelSud = new JPanel();
        contentPane.add(panelSud, BorderLayout.SOUTH);
        
        btnRetour = new JButton("Retour à l'accueil");
        btnRetour.addActionListener(e -> {
            Page_Principale pagePrincipale = new Page_Principale(emailAdmin);
            pagePrincipale.setVisible(true);
            dispose();
        });
        panelSud.add(btnRetour);
        
        appliquerStyleBoutons();
    }
    private void ouvrirCarteParkings() {
        JFrame frameCarte = new JFrame("Administration des Parkings - Carte de Toulouse");
        frameCarte.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCarte.setSize(1200, 800); // Fenêtre agrandie
        frameCarte.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        try {
            // Charger l'image de la carte de Toulouse
            java.net.URL imageUrl = getClass().getResource("/images/Map_Toulouse.jpg");
            if (imageUrl == null) {
                throw new IOException("Image Map_Toulouse.jpg non trouvée dans /images/");
            }
            
            // Créer la carte
            CarteAdminPanel cartePanel = new CarteAdminPanel(imageUrl, emailAdmin);
            
            // Panel de contrôle en haut
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            controlPanel.setBackground(Color.WHITE);
            controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Boutons d'actions
            JButton btnAjouterMode = new JButton("Mode Ajout");
            JButton btnModifierMode = new JButton("Mode Modification");
            JButton btnSupprimer = new JButton("Supprimer");
            
            // Style des boutons
            Color vert = new Color(60, 179, 113);
            Color orange = new Color(255, 165, 0);
            Color rouge = new Color(220, 53, 69);
            Color bleu = new Color(52, 152, 219);
            
            btnAjouterMode.setBackground(vert);
            btnModifierMode.setBackground(orange);
            btnSupprimer.setBackground(rouge);
            
            Color textColor = Color.WHITE;
            btnAjouterMode.setForeground(textColor);
            btnModifierMode.setForeground(textColor);
            btnSupprimer.setForeground(textColor);
            
            // Taille des boutons
            Dimension btnSize = new Dimension(180, 40);
            btnAjouterMode.setPreferredSize(btnSize);
            btnModifierMode.setPreferredSize(btnSize);
            btnSupprimer.setPreferredSize(btnSize);
            
            btnAjouterMode.setFont(new Font("Arial", Font.BOLD, 14));
            btnModifierMode.setFont(new Font("Arial", Font.BOLD, 14));
            btnSupprimer.setFont(new Font("Arial", Font.BOLD, 14));
            
            
            btnAjouterMode.setFocusPainted(false);
            btnModifierMode.setFocusPainted(false);
            btnSupprimer.setFocusPainted(false);
            
            // Actions
            btnAjouterMode.addActionListener(e -> cartePanel.modeAjout());
            btnModifierMode.addActionListener(e -> cartePanel.modeModification());
            btnSupprimer.addActionListener(e -> cartePanel.supprimerParkingSelectionne());
            
            controlPanel.add(btnAjouterMode);
            controlPanel.add(btnModifierMode);
            controlPanel.add(btnSupprimer);
            // Panel d'information en bas
            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.setBackground(new Color(240, 240, 240));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            JLabel lblInfo = new JLabel("<html><b>Carte fixe de Toulouse</b> - Activez 'Mode Ajout' puis cliquez sur la carte</html>");
            lblInfo.setForeground(Color.DARK_GRAY);
            infoPanel.add(lblInfo, BorderLayout.WEST);
            
            JLabel lblCompteur = new JLabel("Parkings chargés: 0");
            lblCompteur.setForeground(new Color(70, 130, 180));
            lblCompteur.setFont(new Font("Arial", Font.BOLD, 12));
            infoPanel.add(lblCompteur, BorderLayout.EAST);
            
            // Mettre à jour le compteur
            cartePanel.setCompteurLabel(lblCompteur);
            
            // Assemblages
            mainPanel.add(controlPanel, BorderLayout.NORTH);
            mainPanel.add(cartePanel, BorderLayout.CENTER);
            mainPanel.add(infoPanel, BorderLayout.SOUTH);
            
        } catch (IOException e) {
            JLabel lblErreur = new JLabel("<html><center><font color='red'>Erreur: " + e.getMessage() + 
                                        "</font><br>Veuillez placer Map_Toulouse.jpg dans le dossier /images/</center></html>");
            lblErreur.setFont(new Font("Arial", Font.BOLD, 14));
            lblErreur.setHorizontalAlignment(SwingConstants.CENTER);
            mainPanel.add(lblErreur, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel lblErreur = new JLabel("<html><center><font color='red'>Erreur: " + e.getMessage() + 
                                        "</font><br>Veuillez vérifier les classes CartePanel et CarteAdminPanel</center></html>");
            lblErreur.setFont(new Font("Arial", Font.BOLD, 14));
            lblErreur.setHorizontalAlignment(SwingConstants.CENTER);
            mainPanel.add(lblErreur, BorderLayout.CENTER);
            e.printStackTrace();
        }
        
        frameCarte.setContentPane(mainPanel);
        frameCarte.setVisible(true);
    }
    // Méthodes pour ajouter et modifier les parkings
    private void ajouterParking() {
        // Formulaire d'ajout
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        String nouvelId = ParkingDAO.genererNouvelIdParking();
        JTextField txtId = new JTextField(nouvelId);
        JTextField txtLibelle = new JTextField();
        JTextField txtAdresse = new JTextField();
        JTextField txtPlaces = new JTextField("100");
        JTextField txtHauteur = new JTextField("2.00");
        JCheckBox chkTarifSoiree = new JCheckBox();
        JCheckBox chkMoto = new JCheckBox();
        JTextField txtPlacesMoto = new JTextField("0");
        
        formPanel.add(new JLabel("ID Parking:"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Nom/Libellé:"));
        formPanel.add(txtLibelle);
        formPanel.add(new JLabel("Adresse:"));
        formPanel.add(txtAdresse);
        formPanel.add(new JLabel("Nombre de places:"));
        formPanel.add(txtPlaces);
        formPanel.add(new JLabel("Hauteur maximale (m):"));
        formPanel.add(txtHauteur);
        formPanel.add(new JLabel("Tarif soirée:"));
        formPanel.add(chkTarifSoiree);
        formPanel.add(new JLabel("Parking moto:"));
        formPanel.add(chkMoto);
        formPanel.add(new JLabel("Places moto:"));
        formPanel.add(txtPlacesMoto);
        
        int result = JOptionPane.showConfirmDialog(this, formPanel, 
            "Ajouter un nouveau parking", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String id = txtId.getText().trim();
                String libelle = txtLibelle.getText().trim();
                String adresse = txtAdresse.getText().trim();
                int places = Integer.parseInt(txtPlaces.getText());
                double hauteur = Double.parseDouble(txtHauteur.getText());
                boolean tarifSoiree = chkTarifSoiree.isSelected();
                boolean hasMoto = chkMoto.isSelected();
                int placesMoto = hasMoto ? Integer.parseInt(txtPlacesMoto.getText()) : 0;
                
                // Vérifier si l'ID existe
                if (ParkingDAO.idParkingExiste(id)) {
                    JOptionPane.showMessageDialog(this,
                        "L'ID " + id + " existe déjà. Veuillez en choisir un autre.",
                        "ID dupliqué", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Parking nouveauParking = new Parking(id, libelle, adresse, 
                    places, places, hauteur, tarifSoiree, hasMoto, placesMoto, placesMoto);
                
                if (ParkingDAO.ajouterParking(nouveauParking)) {
                    JOptionPane.showMessageDialog(this, 
                        "Parking ajouté avec succès! (ID: " + id + ")", "Succès", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez entrer des valeurs numériques valides", 
                    "Erreur de format", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur: " + e.getMessage(), "Erreur", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void modifierParking() {
        // Cette méthode serait appelée avec un parking sélectionné
        // Vous devriez ouvrir une boîte de dialogue pour sélectionner le parking
        JOptionPane.showMessageDialog(this,
            "Sélectionnez un parking dans la liste pour le modifier",
            "Modification de parking",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void afficherDetailsParking(Parking parking) {
        DecimalFormat df = new DecimalFormat("#.##");
        String info = String.format(
            "<html><div style='width:300px;'><h3>%s</h3>" +
            "<b>ID:</b> %s<br><b>Adresse:</b> %s<br>" +
            "<b>Capacité:</b> %d places (%d disponibles)<br>" +
            "<b>Hauteur max:</b> %s m<br><b>Tarif soirée:</b> %s<br>" +
            "<b>Parking moto:</b> %s",
            parking.getLibelleParking(),
            parking.getIdParking(),
            parking.getAdresseParking(),
            parking.getNombrePlaces(),
            parking.getPlacesDisponibles(),
            df.format(parking.getHauteurParking()),
            parking.hasTarifSoiree() ? "Oui" : "Non",
            parking.hasMoto() ? "Oui" : "Non"
        );
        
        if (parking.hasMoto()) {
            info += String.format("<br><b>Places moto:</b> %d (%d disponibles)",
                parking.getPlacesMoto(),
                parking.getPlacesMotoDisponibles());
        }
        
        info += "</div></html>";
        
        JOptionPane.showMessageDialog(this, info, "Détails du Parking", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void appliquerStyleBoutons() {
        Component[] components = contentPane.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                Component[] sousComponents = ((JPanel) comp).getComponents();
                for (Component sousComp : sousComponents) {
                    if (sousComp instanceof JButton) {
                        JButton bouton = (JButton) sousComp;
                        styliserBouton(bouton);
                    }
                }
            }
        }
        
        // Style spécifique pour le bouton carte
        if (btnGestionCarte != null) {
            styliserBouton(btnGestionCarte, new Color(60, 179, 113));
        }
    }
    
    private void styliserBouton(JButton bouton) {
        styliserBouton(bouton, new Color(70, 130, 180));
    }
    
    private void styliserBouton(JButton bouton, Color couleur) {
        bouton.setFocusPainted(false);
        bouton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(couleur.darker(), 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        bouton.setBackground(couleur);
        bouton.setForeground(Color.WHITE);
        
        bouton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bouton.setBackground(couleur.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bouton.setBackground(couleur);
            }
        });
    }

}
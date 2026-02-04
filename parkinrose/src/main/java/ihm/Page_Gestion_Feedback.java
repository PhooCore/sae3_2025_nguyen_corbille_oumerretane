package ihm;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.time.format.DateTimeFormatter;

import modele.Feedback;
import modele.Usager;
import modele.dao.FeedbackDAO;
import modele.dao.UsagerDAO;
import controleur.ControleurGestionFeedback;

public class Page_Gestion_Feedback extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailAdmin;

    // Panels principaux
    private JPanel contentPane;
    private JPanel mainPanel;
    private JSplitPane splitPanePrincipal;
    
    // Panel Haut
    private JPanel panelHaut;
    private JLabel lblTitre;
    private JPanel panelBoutonsHaut;
    private JButton btnRetour;
    
    // Panel Gauche
    private JPanel panelGauche;
    private JPanel panelFiltres;
    private JLabel lblFiltre;
    private JComboBox<String> comboFiltre;
    private JScrollPane scrollPaneTable;
    private JTable tableFeedbacks;
    private DefaultTableModel tableModel;
    
    // Panel Droit
    private JPanel panelDroit;
    private JPanel panelInfo;
    private JLabel lblUserInfo;
    private JLabel lblDateInfo;
    private JLabel lblStatut;
    private JLabel lblSujetInfo;
    private JTabbedPane tabbedPane;
    private JPanel panelMessage;
    private JScrollPane scrollPaneMessage;
    private JTextArea txtMessageDetail;
    private JPanel panelHistorique;
    private JScrollPane scrollPaneHistorique;
    private JTextArea txtHistorique;
    private JPanel panelReponse;
    private JLabel lblReponse;
    private JScrollPane scrollPaneReponse;
    private JTextArea txtReponse;
    private JPanel panelBoutonsAction;
    private JButton btnMarquerEnCours;
    private JButton btnMarquerResolu;
    private JButton btnRepondre;
    
    // Contrôleur
    private ControleurGestionFeedback controleur;
    
    public Page_Gestion_Feedback(String emailAdmin) {
        this.emailAdmin = emailAdmin;

        initializeDesign();

        controleur = new ControleurGestionFeedback(this);
    }

    private void initializeDesign() {
        setTitle("Gestion des Feedbacks - Administration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 1200, 700);
        setLocationRelativeTo(null);

        contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        contentPane.add(mainPanel, BorderLayout.CENTER);
        

        panelHaut = new JPanel();
        panelHaut.setLayout(new BorderLayout(10, 10));
        mainPanel.add(panelHaut, BorderLayout.NORTH);
        
        // Titre
        lblTitre = new JLabel("Gestion des Messages Utilisateurs");
        lblTitre.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitre.setForeground(new Color(70, 130, 180));
        panelHaut.add(lblTitre, BorderLayout.CENTER);
        
        // Boutons haut
        panelBoutonsHaut = new JPanel();
        panelBoutonsHaut.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelHaut.add(panelBoutonsHaut, BorderLayout.EAST);
        
        btnRetour = new JButton("← Retour");
        styliserBouton(btnRetour, new Color(169, 169, 169));
        panelBoutonsHaut.add(btnRetour);
        
        
        splitPanePrincipal = new JSplitPane();
        splitPanePrincipal.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPanePrincipal.setDividerSize(5);
        mainPanel.add(splitPanePrincipal, BorderLayout.CENTER);
        
      
        panelGauche = new JPanel();
        panelGauche.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "Messages reçus",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            Color.BLACK
        ));
        panelGauche.setLayout(new BorderLayout(5, 5));
        splitPanePrincipal.setLeftComponent(panelGauche);
        
        // Panel Filtres
        panelFiltres = new JPanel();
        panelFiltres.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelGauche.add(panelFiltres, BorderLayout.NORTH);
        
        lblFiltre = new JLabel("Filtrer : ");
        lblFiltre.setFont(new Font("Arial", Font.PLAIN, 12));
        panelFiltres.add(lblFiltre);
        
        comboFiltre = new JComboBox<>();
        comboFiltre.setModel(new DefaultComboBoxModel<>(new String[] {
            "Tous les messages", "Nouveaux", "En cours", "Résolus"
        }));
        comboFiltre.setFont(new Font("Arial", Font.PLAIN, 12));
        panelFiltres.add(comboFiltre);
        
        // Table des feedbacks
        String[] colonnes = {"ID", "Statut", "Sujet", "Utilisateur", "Date", "Répondu"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableFeedbacks = new JTable(tableModel);
        configurerTableDesign();
        
        scrollPaneTable = new JScrollPane(tableFeedbacks);
        panelGauche.add(scrollPaneTable, BorderLayout.CENTER);
        
        
        panelDroit = new JPanel();
        panelDroit.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "Détails du message",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            Color.BLACK
        ));
        panelDroit.setLayout(new BorderLayout(5, 5));
        splitPanePrincipal.setRightComponent(panelDroit);
        
        // Panel Informations
        panelInfo = new JPanel();
        panelInfo.setLayout(new GridLayout(4, 1, 5, 5));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelDroit.add(panelInfo, BorderLayout.NORTH);
        
        lblUserInfo = new JLabel("Utilisateur : ");
        lblUserInfo.setFont(new Font("Arial", Font.BOLD, 12));
        panelInfo.add(lblUserInfo);
        
        lblDateInfo = new JLabel("Date : ");
        lblDateInfo.setFont(new Font("Arial", Font.BOLD, 12));
        panelInfo.add(lblDateInfo);
        
        lblStatut = new JLabel("Statut : ");
        lblStatut.setFont(new Font("Arial", Font.BOLD, 12));
        panelInfo.add(lblStatut);
        
        lblSujetInfo = new JLabel("Sujet : ");
        lblSujetInfo.setFont(new Font("Arial", Font.BOLD, 12));
        panelInfo.add(lblSujetInfo);
        
        // Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 12));
        panelDroit.add(tabbedPane, BorderLayout.CENTER);
        
        // Onglet Message
        panelMessage = new JPanel();
        panelMessage.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Message original", null, panelMessage, null);
        
        txtMessageDetail = new JTextArea();
        txtMessageDetail.setFont(new Font("Arial", Font.PLAIN, 12));
        txtMessageDetail.setLineWrap(true);
        txtMessageDetail.setWrapStyleWord(true);
        txtMessageDetail.setEditable(false);
        txtMessageDetail.setBackground(new Color(240, 240, 240));
        
        scrollPaneMessage = new JScrollPane(txtMessageDetail);
        panelMessage.add(scrollPaneMessage, BorderLayout.CENTER);
        
        // Onglet Historique
        panelHistorique = new JPanel();
        panelHistorique.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Historique des réponses", null, panelHistorique, null);
        
        txtHistorique = new JTextArea();
        txtHistorique.setFont(new Font("Arial", Font.PLAIN, 12));
        txtHistorique.setLineWrap(true);
        txtHistorique.setWrapStyleWord(true);
        txtHistorique.setEditable(false);
        txtHistorique.setBackground(new Color(250, 250, 220));
        
        scrollPaneHistorique = new JScrollPane(txtHistorique);
        panelHistorique.add(scrollPaneHistorique, BorderLayout.CENTER);
        
        // Panel Réponse
        panelReponse = new JPanel();
        panelReponse.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelReponse.setLayout(new BorderLayout(5, 5));
        panelDroit.add(panelReponse, BorderLayout.SOUTH);
        
        lblReponse = new JLabel("Votre réponse :");
        lblReponse.setFont(new Font("Arial", Font.BOLD, 12));
        panelReponse.add(lblReponse, BorderLayout.NORTH);
        
        txtReponse = new JTextArea();
        txtReponse.setFont(new Font("Arial", Font.PLAIN, 12));
        txtReponse.setLineWrap(true);
        txtReponse.setWrapStyleWord(true);
        txtReponse.setRows(4);
        
        scrollPaneReponse = new JScrollPane(txtReponse);
        panelReponse.add(scrollPaneReponse, BorderLayout.CENTER);
        
        // Panel Boutons Action
        panelBoutonsAction = new JPanel();
        panelBoutonsAction.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelReponse.add(panelBoutonsAction, BorderLayout.SOUTH);
        
        btnMarquerEnCours = new JButton("Marquer en cours");
        styliserBouton(btnMarquerEnCours, new Color(255, 140, 0));
        btnMarquerEnCours.setEnabled(false);
        panelBoutonsAction.add(btnMarquerEnCours);
        
        btnMarquerResolu = new JButton("Marquer résolu");
        styliserBouton(btnMarquerResolu, new Color(50, 205, 50));
        btnMarquerResolu.setEnabled(false);
        panelBoutonsAction.add(btnMarquerResolu);
        
        btnRepondre = new JButton("Répondre");
        styliserBouton(btnRepondre, new Color(70, 130, 180));
        btnRepondre.setEnabled(false);
        panelBoutonsAction.add(btnRepondre);
        
        // Configuration du diviseur
        splitPanePrincipal.setDividerLocation(600);
    }
    

    private void styliserBouton(JButton bouton, Color couleur) {
        bouton.setFont(new Font("Arial", Font.BOLD, 12));
        bouton.setForeground(Color.WHITE);
        bouton.setBackground(couleur);
        bouton.setFocusPainted(false);
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bouton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(couleur.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        // Effets hover
        bouton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (bouton.isEnabled()) {
                    bouton.setBackground(couleur.brighter());
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                bouton.setBackground(couleur);
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                if (bouton.isEnabled()) {
                    bouton.setBackground(couleur.darker());
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (bouton.isEnabled()) {
                    bouton.setBackground(couleur.brighter());
                }
            }
        });
    }
    

    private void configurerTableDesign() {
        tableFeedbacks.setRowHeight(30);
        tableFeedbacks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableFeedbacks.setFont(new Font("Arial", Font.PLAIN, 12));
        tableFeedbacks.setGridColor(new Color(220, 220, 220));
        tableFeedbacks.setShowGrid(true);
        
        // Centrer certaines colonnes
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        tableFeedbacks.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableFeedbacks.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        
        // Largeurs de colonnes
        tableFeedbacks.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableFeedbacks.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableFeedbacks.getColumnModel().getColumn(2).setPreferredWidth(200);
        tableFeedbacks.getColumnModel().getColumn(3).setPreferredWidth(150);
        tableFeedbacks.getColumnModel().getColumn(4).setPreferredWidth(100);
        tableFeedbacks.getColumnModel().getColumn(5).setPreferredWidth(60);
    }
    

    public String getEmailAdmin() {
        return emailAdmin;
    }
    
    
    public JButton getBtnRetour() {
        return btnRetour;
    }
    
    public JButton getBtnMarquerEnCours() {
        return btnMarquerEnCours;
    }
    
    public JButton getBtnMarquerResolu() {
        return btnMarquerResolu;
    }
    
    public JButton getBtnRepondre() {
        return btnRepondre;
    }
    
    public JTable getTableFeedbacks() {
        return tableFeedbacks;
    }
    
    public DefaultTableModel getTableModel() {
        return tableModel;
    }
    
    public JComboBox<String> getComboFiltre() {
        return comboFiltre;
    }
    
    public JLabel getLblUserInfo() {
        return lblUserInfo;
    }
    
    public JLabel getLblDateInfo() {
        return lblDateInfo;
    }
    
    public JLabel getLblStatut() {
        return lblStatut;
    }
    
    public JLabel getLblSujetInfo() {
        return lblSujetInfo;
    }
    
    public JTextArea getTxtMessageDetail() {
        return txtMessageDetail;
    }
    
    public JTextArea getTxtHistorique() {
        return txtHistorique;
    }
    
    public JTextArea getTxtReponse() {
        return txtReponse;
    }
    
    public ControleurGestionFeedback getControleur() {
        return controleur;
    }
    
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Page_Gestion_Feedback page = new Page_Gestion_Feedback("admin@pr.com");
            page.setVisible(true);
        });
    }
}
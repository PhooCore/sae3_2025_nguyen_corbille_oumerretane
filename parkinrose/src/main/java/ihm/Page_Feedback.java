package ihm;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import modele.Feedback;
import modele.Usager;
import modele.dao.FeedbackDAO;
import modele.dao.UsagerDAO;
import controleur.ControleurFeedback;

public class Page_Feedback extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Usager usager;
    private Page_Principale parentPage;
    
    private final Color COULEUR_PRIMAIRE = new Color(70, 130, 180);
    private final Color COULEUR_SECONDAIRE = new Color(60, 179, 113);
    private final Color COULEUR_ACCENT = new Color(186, 85, 211);
    private final Color COULEUR_BACKGROUND = new Color(250, 250, 252);
    private final Color COULEUR_CARD = Color.WHITE;
    private final Color COULEUR_BORDER = new Color(230, 230, 235);
    private final Color COULEUR_TEXTE_SOMBRE = new Color(60, 60, 60);
    private final Color COULEUR_TEXTE_CLAIR = new Color(100, 100, 100);
    private final Color COULEUR_NOTIFICATION = new Color(255, 59, 48);
    
    private JPanel contentPane;
    private JPanel mainPanel;
    private JSplitPane splitPanePrincipal;
    
    private JPanel panelHaut;
    private JPanel panelTitre;
    private JLabel lblTitre;
    private JPanel panelInfoUser;
    private JLabel lblUser;
    private JPanel panelBoutonsHaut;
    private JButton btnFermer;
    
    private JPanel panelGauche;
    private JPanel panelEnteteGauche;
    private JLabel lblTitreConversations;
    private JLabel lblInfoConversation;
    private JPanel panelFiltres;
    private JLabel lblFiltre;
    private JComboBox<String> comboFiltre;
    private JScrollPane scrollPaneTable;
    private JTable tableFeedbacks;
    private DefaultTableModel tableModel;
    
    private JPanel panelDroit;
    private JSplitPane splitPaneDroit;
    
    private JPanel panelDetails;
    private JTabbedPane tabbedPane;
    private JPanel panelMessageOriginal;
    private JScrollPane scrollPaneMessage;
    private JTextArea txtMessageDetail;
    private JPanel panelHistorique;
    private JScrollPane scrollPaneHistorique;
    private JTextArea txtHistorique;
    
    private JPanel panelNouveau;
    private JPanel panelFormNouveau;
    private JPanel panelSujet;
    private JLabel lblSujet;
    private JTextField txtSujetNouveau;
    private JScrollPane scrollPaneNouveauMessage;
    private JTextArea txtNouveauMessage;
    private JPanel panelBoutonsNouveau;
    private JButton btnEnvoyerMessage;
    private JButton btnEffacer;
    
    private ControleurFeedback controleur;
    
    public Page_Feedback(String email, Page_Principale parent) {
        this.emailUtilisateur = email;
        this.parentPage = parent;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        
        if (parent != null) {
            parent.markMessagesAsRead();
        } else if (usager != null) {
            FeedbackDAO.markMessagesAsRead(usager.getIdUsager());
        }
        
        initializeDesign();
        chargerFeedbacks();
        controleur = new ControleurFeedback(this);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (parentPage != null) {
                    parentPage.updateMessagerieIcon();
                }
            }
            
            @Override
            public void windowClosed(WindowEvent e) {
                if (parentPage != null) {
                    parentPage.updateMessagerieIcon();
                }
            }
        });
    }
    
    private void initializeDesign() {
        setTitle("Messagerie - ParkinRose");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 1100, 700);
        setLocationRelativeTo(null);
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(245, 247, 250), 
                    0, getHeight(), new Color(235, 238, 245));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(0, 0));
        contentPane.add(mainPanel, BorderLayout.CENTER);
        
        panelHaut = new JPanel();
        panelHaut.setOpaque(false);
        panelHaut.setBorder(new EmptyBorder(0, 0, 15, 0));
        panelHaut.setLayout(new BorderLayout(10, 10));
        mainPanel.add(panelHaut, BorderLayout.NORTH);
        
        panelTitre = new JPanel();
        panelTitre.setOpaque(false);
        panelTitre.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelHaut.add(panelTitre, BorderLayout.WEST);
        
        lblTitre = new JLabel("Ma Messagerie");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitre.setForeground(COULEUR_PRIMAIRE);
        panelTitre.add(lblTitre);
        
        panelInfoUser = new JPanel();
        panelInfoUser.setOpaque(false);
        panelInfoUser.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        
        JPanel panelCentre = new JPanel();
        panelCentre.setOpaque(false);
        panelCentre.setLayout(new BorderLayout(0, 0));
        panelCentre.add(panelInfoUser, BorderLayout.WEST);
        panelHaut.add(panelCentre, BorderLayout.CENTER);
        
        lblUser = new JLabel("Utilisateur : " + emailUtilisateur);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setForeground(COULEUR_TEXTE_CLAIR);
        panelInfoUser.add(lblUser);
        
        panelBoutonsHaut = new JPanel();
        panelBoutonsHaut.setOpaque(false);
        panelBoutonsHaut.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelHaut.add(panelBoutonsHaut, BorderLayout.EAST);
        
        btnFermer = new JButton("Fermer");
        styliserBouton(btnFermer, COULEUR_ACCENT);
        panelBoutonsHaut.add(btnFermer);
        
        splitPanePrincipal = new JSplitPane();
        splitPanePrincipal.setBorder(null);
        splitPanePrincipal.setBackground(COULEUR_BACKGROUND);
        splitPanePrincipal.setOneTouchExpandable(true);
        mainPanel.add(splitPanePrincipal, BorderLayout.CENTER);
        
        panelGauche = new JPanel();
        panelGauche.setBackground(COULEUR_CARD);
        panelGauche.setBorder(new CompoundBorder(
            new LineBorder(COULEUR_BORDER, 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panelGauche.setLayout(new BorderLayout(0, 10));
        splitPanePrincipal.setLeftComponent(panelGauche);
        
        panelEnteteGauche = new JPanel();
        panelEnteteGauche.setOpaque(false);
        panelEnteteGauche.setBorder(new EmptyBorder(0, 0, 15, 0));
        panelEnteteGauche.setLayout(new BorderLayout(10, 0));
        panelGauche.add(panelEnteteGauche, BorderLayout.NORTH);
        
        lblTitreConversations = new JLabel("Mes conversations");
        lblTitreConversations.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitreConversations.setForeground(COULEUR_TEXTE_SOMBRE);
        panelEnteteGauche.add(lblTitreConversations, BorderLayout.WEST);
        
        lblInfoConversation = new JLabel("0 conversations");
        lblInfoConversation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfoConversation.setForeground(COULEUR_TEXTE_CLAIR);
        panelEnteteGauche.add(lblInfoConversation, BorderLayout.EAST);
        
        panelFiltres = new JPanel();
        panelFiltres.setOpaque(false);
        panelFiltres.setBorder(new EmptyBorder(0, 0, 10, 0));
        panelFiltres.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelGauche.add(panelFiltres, BorderLayout.CENTER);
        
        lblFiltre = new JLabel("Filtrer :");
        lblFiltre.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFiltre.setForeground(COULEUR_TEXTE_CLAIR);
        panelFiltres.add(lblFiltre);
        
        comboFiltre = new JComboBox<>();
        comboFiltre.setModel(new DefaultComboBoxModel<>(new String[] {
            "Toutes mes conversations", "En attente", "Répondu"
        }));
        styliserComboBoxDesign(comboFiltre);
        panelFiltres.add(comboFiltre);
        
        String[] colonnes = {"", "Conversation", "Dernière activité"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableFeedbacks = new JTable(tableModel);
        configurerTableDesign();
        
        scrollPaneTable = new JScrollPane(tableFeedbacks);
        scrollPaneTable.setBorder(new LineBorder(COULEUR_BORDER, 1, true));
        scrollPaneTable.setViewportBorder(null);
        panelGauche.add(scrollPaneTable, BorderLayout.SOUTH);
        
        panelDroit = new JPanel();
        panelDroit.setBackground(COULEUR_CARD);
        panelDroit.setBorder(new CompoundBorder(
            new LineBorder(COULEUR_BORDER, 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panelDroit.setLayout(new BorderLayout(0, 0));
        splitPanePrincipal.setRightComponent(panelDroit);
        
        splitPaneDroit = new JSplitPane();
        splitPaneDroit.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneDroit.setBorder(null);
        panelDroit.add(splitPaneDroit, BorderLayout.CENTER);
        
        panelDetails = new JPanel();
        panelDetails.setBackground(COULEUR_CARD);
        panelDetails.setBorder(new CompoundBorder(
            new TitledBorder(new LineBorder(COULEUR_SECONDAIRE, 1, true), 
                "Détails de la conversation", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), COULEUR_SECONDAIRE),
            new EmptyBorder(10, 10, 10, 10)
        ));
        panelDetails.setLayout(new BorderLayout(0, 0));
        splitPaneDroit.setLeftComponent(panelDetails);
        
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panelDetails.add(tabbedPane, BorderLayout.CENTER);
        
        panelMessageOriginal = new JPanel();
        panelMessageOriginal.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Message original", null, panelMessageOriginal, null);
        
        txtMessageDetail = new JTextArea();
        txtMessageDetail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMessageDetail.setLineWrap(true);
        txtMessageDetail.setWrapStyleWord(true);
        txtMessageDetail.setEditable(false);
        txtMessageDetail.setBackground(new Color(248, 250, 252));
        txtMessageDetail.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        scrollPaneMessage = new JScrollPane(txtMessageDetail);
        scrollPaneMessage.setBorder(new LineBorder(COULEUR_BORDER, 1, true));
        panelMessageOriginal.add(scrollPaneMessage, BorderLayout.CENTER);
        
        panelHistorique = new JPanel();
        panelHistorique.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Historique", null, panelHistorique, null);
        
        txtHistorique = new JTextArea();
        txtHistorique.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtHistorique.setLineWrap(true);
        txtHistorique.setWrapStyleWord(true);
        txtHistorique.setEditable(false);
        txtHistorique.setBackground(new Color(252, 252, 245));
        txtHistorique.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        scrollPaneHistorique = new JScrollPane(txtHistorique);
        scrollPaneHistorique.setBorder(new LineBorder(COULEUR_BORDER, 1, true));
        panelHistorique.add(scrollPaneHistorique, BorderLayout.CENTER);
        
        panelNouveau = new JPanel();
        panelNouveau.setBackground(COULEUR_CARD);
        panelNouveau.setBorder(new CompoundBorder(
            new TitledBorder(new LineBorder(COULEUR_ACCENT, 1, true), 
                "Nouveau message", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), COULEUR_ACCENT),
            new EmptyBorder(10, 10, 10, 10)
        ));
        panelNouveau.setLayout(new BorderLayout(0, 0));
        splitPaneDroit.setRightComponent(panelNouveau);
        
        panelFormNouveau = new JPanel();
        panelFormNouveau.setOpaque(false);
        panelFormNouveau.setLayout(new BorderLayout(10, 10));
        panelNouveau.add(panelFormNouveau, BorderLayout.CENTER);
        
        panelSujet = new JPanel();
        panelSujet.setOpaque(false);
        panelSujet.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelFormNouveau.add(panelSujet, BorderLayout.NORTH);
        
        lblSujet = new JLabel("Sujet :");
        lblSujet.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panelSujet.add(lblSujet);
        
        txtSujetNouveau = new JTextField();
        txtSujetNouveau.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSujetNouveau.setColumns(30);
        txtSujetNouveau.setBorder(new CompoundBorder(
            new LineBorder(COULEUR_BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        panelSujet.add(txtSujetNouveau);
        
        txtNouveauMessage = new JTextArea();
        txtNouveauMessage.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNouveauMessage.setLineWrap(true);
        txtNouveauMessage.setWrapStyleWord(true);
        txtNouveauMessage.setRows(5);
        txtNouveauMessage.setBorder(new CompoundBorder(
            new LineBorder(COULEUR_BORDER, 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        scrollPaneNouveauMessage = new JScrollPane(txtNouveauMessage);
        panelFormNouveau.add(scrollPaneNouveauMessage, BorderLayout.CENTER);
        
        panelBoutonsNouveau = new JPanel();
        panelBoutonsNouveau.setOpaque(false);
        panelBoutonsNouveau.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        panelFormNouveau.add(panelBoutonsNouveau, BorderLayout.SOUTH);
        
        btnEffacer = new JButton("Effacer");
        styliserBouton(btnEffacer, COULEUR_TEXTE_CLAIR);
        panelBoutonsNouveau.add(btnEffacer);
        
        Component horizontalStrut = Box.createHorizontalStrut(10);
        panelBoutonsNouveau.add(horizontalStrut);
        
        btnEnvoyerMessage = new JButton("Envoyer le message");
        styliserBouton(btnEnvoyerMessage, COULEUR_PRIMAIRE);
        panelBoutonsNouveau.add(btnEnvoyerMessage);
        
        splitPanePrincipal.setDividerLocation(400);
        splitPaneDroit.setDividerLocation(300);
    }
    
    private void styliserBouton(JButton bouton, Color couleur) {
        bouton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bouton.setForeground(Color.WHITE);
        bouton.setBackground(couleur);
        bouton.setFocusPainted(false);
        bouton.setBorderPainted(false);
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bouton.setBorder(new CompoundBorder(
            new LineBorder(couleur.darker(), 1),
            new EmptyBorder(8, 15, 8, 15)
        ));
        
        bouton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                bouton.setBackground(couleur.brighter());
                bouton.setBorder(new CompoundBorder(
                    new LineBorder(couleur.darker().brighter(), 1),
                    new EmptyBorder(8, 15, 8, 15)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                bouton.setBackground(couleur);
                bouton.setBorder(new CompoundBorder(
                    new LineBorder(couleur.darker(), 1),
                    new EmptyBorder(8, 15, 8, 15)
                ));
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                bouton.setBackground(couleur.darker());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                bouton.setBackground(couleur.brighter());
            }
        });
    }
    
    private void configurerTableDesign() {
        tableFeedbacks.setRowHeight(60);
        tableFeedbacks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableFeedbacks.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableFeedbacks.setIntercellSpacing(new Dimension(0, 0));
        tableFeedbacks.setShowGrid(false);
        tableFeedbacks.setTableHeader(null);
        
        tableFeedbacks.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableFeedbacks.getColumnModel().getColumn(1).setPreferredWidth(250);
        tableFeedbacks.getColumnModel().getColumn(2).setPreferredWidth(100);

    }
    
    private void styliserComboBoxDesign(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(Color.WHITE);
        combo.setBorder(new CompoundBorder(
            new LineBorder(COULEUR_BORDER, 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
    }
    
    public void chargerFeedbacks() {
        if (usager == null) return;
        
        tableModel.setRowCount(0);
        
        List<Feedback> feedbacks = FeedbackDAO.getFeedbacksByUser(usager.getIdUsager());
        if (feedbacks == null) return;
        
        int totalFeedbacks = 0;
        int nouveauxMessages = 0;
        
        for (Feedback feedback : feedbacks) {
            if (feedback.estUnMessageParent()) {
                totalFeedbacks++;
                
                String statutIcon = "○";
                Color statutColor = Color.GRAY;
                
                if (feedback.isGotanswer()) {
                    List<Feedback> reponses = FeedbackDAO.getReponsesFeedback(feedback.getIdFeedback());
                    boolean dejaLu = false;
                    for (Feedback reponse : reponses) {
                        if (reponse.getMessage() != null && reponse.getMessage().equals("MESSAGE_LU")) {
                            dejaLu = true;
                            break;
                        }
                    }
                    
                    if (!dejaLu) {
                        statutIcon = "●";
                        statutColor = COULEUR_NOTIFICATION;
                        nouveauxMessages++;
                    } else {
                        statutIcon = "✓";
                        statutColor = COULEUR_SECONDAIRE;
                    }
                } else {
                    statutIcon = "○";
                    statutColor = Color.GRAY;
                }
                
                String dateFormattee = feedback.getDateCreation().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"));
                
                String conversationHtml = "<html><b>" + feedback.getSujet() + "</b><br>" +
                    "<font color='gray' size='-2'>" + 
                    (feedback.getMessage().length() > 50 ? 
                     feedback.getMessage().substring(0, 50) + "..." : 
                     feedback.getMessage()) + 
                    "</font></html>";
                
                tableModel.addRow(new Object[]{statutIcon, conversationHtml, dateFormattee});
            }
        }
        
        lblInfoConversation.setText(totalFeedbacks + " conversations");
        
        if (nouveauxMessages > 0) {
            lblInfoConversation.setForeground(COULEUR_NOTIFICATION);
            lblInfoConversation.setText(totalFeedbacks + " conversations (" + nouveauxMessages + " nouveaux)");
        } else {
            lblInfoConversation.setForeground(COULEUR_TEXTE_CLAIR);
        }
    }
    
    public void afficherDetailsFeedback(Feedback feedback) {
        if (feedback == null) return;
        
        // Texte sans emojis/icônes
        txtMessageDetail.setText("Sujet : " + feedback.getSujet() + "\n\n" +
            "Date : " + feedback.getDateCreation().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) + "\n\n" +
            "Message :\n" + feedback.getMessage());
        
        StringBuilder historique = new StringBuilder();
        List<Feedback> reponses = FeedbackDAO.getReponsesFeedback(feedback.getIdFeedback());
        
        for (Feedback reponse : reponses) {
            if (reponse.getMessage() == null || !reponse.getMessage().equals("MESSAGE_LU")) {
                historique.append("Réponse du ").append(
                    reponse.getDateCreation().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))).append(" :\n");
                historique.append(reponse.getMessage()).append("\n\n");
            }
        }
        
        txtHistorique.setText(historique.toString());
        
        FeedbackDAO.markMessageAsRead(feedback.getIdFeedback());
        
        if (parentPage != null) {
            parentPage.updateMessagerieIcon();
        }
    }
    
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }
    
    public JLabel getLblUser() {
        return lblUser;
    }
    
    public JButton getBtnFermer() {
        return btnFermer;
    }
    
    public JButton getBtnEnvoyerMessage() {
        return btnEnvoyerMessage;
    }
    
    public JButton getBtnEffacer() {
        return btnEffacer;
    }
    
    public JTable getTableFeedbacks() {
        return tableFeedbacks;
    }
    
    public DefaultTableModel getTableModel() {
        return tableModel;
    }
    
    public JLabel getLblInfoConversation() {
        return lblInfoConversation;
    }
    
    public JComboBox<String> getComboFiltre() {
        return comboFiltre;
    }
    
    public JTextArea getTxtMessageDetail() {
        return txtMessageDetail;
    }
    
    public JTextArea getTxtHistorique() {
        return txtHistorique;
    }
    
    public JTextField getTxtSujetNouveau() {
        return txtSujetNouveau;
    }
    
    public JTextArea getTxtNouveauMessage() {
        return txtNouveauMessage;
    }
    
    public ControleurFeedback getControleur() {
        return controleur;
    }
    
    public Usager getUsager() {
        return usager;
    }
    

}
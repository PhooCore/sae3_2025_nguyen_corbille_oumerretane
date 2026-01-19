package ihm;

import utils.NotificationManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Page_Configuration_Notifications extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private NotificationManager notificationManager;
    
    // Composants UI
    private JCheckBox chkActiverNotifications;
    private JSpinner spinnerIntervalle;
    private JButton btnTester;
    private JButton btnNettoyer;
    private JButton btnSauvegarder;
    private JButton btnFermer;
    
    public Page_Configuration_Notifications() {
        this.notificationManager = NotificationManager.getInstance();
        initialisePage();
    }
    
    private void initialisePage() {
        this.setTitle("Configuration des notifications");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(500, 400);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());
        
        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Titre
        JLabel lblTitre = new JLabel("🔔 Configuration des notifications", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitre.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitre);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Panel d'activation
        JPanel panelActivation = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelActivation.setBackground(Color.WHITE);
        panelActivation.setMaximumSize(new Dimension(450, 50));
        
        chkActiverNotifications = new JCheckBox("Activer les notifications");
        chkActiverNotifications.setSelected(notificationManager.areNotificationsActives());
        chkActiverNotifications.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkActiverNotifications.addActionListener(e -> mettreAJourEtatChamps());
        
        panelActivation.add(chkActiverNotifications);
        mainPanel.add(panelActivation);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Panel intervalle
        JPanel panelIntervalle = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelIntervalle.setBackground(Color.WHITE);
        panelIntervalle.setMaximumSize(new Dimension(450, 50));
        
        JLabel lblIntervalle = new JLabel("Vérifier toutes les :");
        lblIntervalle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblIntervalle.setPreferredSize(new Dimension(150, 25));
        
        spinnerIntervalle = new JSpinner(new SpinnerNumberModel(
            notificationManager.getVerificationInterval(), 1, 60, 1));
        spinnerIntervalle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spinnerIntervalle.setEnabled(notificationManager.areNotificationsActives());
        
        JLabel lblMinutes = new JLabel("minutes");
        lblMinutes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        panelIntervalle.add(lblIntervalle);
        panelIntervalle.add(spinnerIntervalle);
        panelIntervalle.add(lblMinutes);
        mainPanel.add(panelIntervalle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Panel informations
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setBackground(new Color(240, 248, 255));
        panelInfo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panelInfo.setMaximumSize(new Dimension(450, 150));
        
        JLabel lblInfoTitre = new JLabel("📋 Informations sur les notifications");
        lblInfoTitre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblInfoTitre.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelInfo.add(lblInfoTitre);
        
        panelInfo.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JTextArea txtInfo = new JTextArea(
            "• Les notifications s'affichent lorsqu'il reste 10 minutes ou moins\n" +
            "  pour un stationnement en voirie.\n" +
            "• Pour les 3 dernières minutes, les notifications sont en rouge.\n" +
            "• Les notifications apparaissent en bas à droite de l'écran.\n" +
            "• Elles se ferment automatiquement après 15 secondes."
        );
        txtInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtInfo.setBackground(new Color(240, 248, 255));
        txtInfo.setEditable(false);
        txtInfo.setLineWrap(true);
        txtInfo.setWrapStyleWord(true);
        txtInfo.setBorder(null);
        txtInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panelInfo.add(txtInfo);
        mainPanel.add(panelInfo);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Panel boutons
        JPanel panelBoutons = new JPanel(new GridLayout(2, 2, 10, 10));
        panelBoutons.setBackground(Color.WHITE);
        panelBoutons.setMaximumSize(new Dimension(450, 100));
        
        // Bouton Tester
        btnTester = new JButton("Tester les notifications");
        btnTester.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTester.setBackground(new Color(52, 152, 219));
        btnTester.setForeground(Color.WHITE);
        btnTester.setFocusPainted(false);
        btnTester.addActionListener(e -> notificationManager.testerNotifications());
        
        // Bouton Nettoyer
        btnNettoyer = new JButton("Nettoyer l'historique");
        btnNettoyer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnNettoyer.setBackground(new Color(241, 196, 15));
        btnNettoyer.setForeground(Color.WHITE);
        btnNettoyer.setFocusPainted(false);
        btnNettoyer.addActionListener(e -> {
            notificationManager.clearNotificationHistory();
            JOptionPane.showMessageDialog(this,
                "L'historique des notifications a été nettoyé.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Bouton Sauvegarder
        btnSauvegarder = new JButton("Sauvegarder");
        btnSauvegarder.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSauvegarder.setBackground(new Color(46, 204, 113));
        btnSauvegarder.setForeground(Color.WHITE);
        btnSauvegarder.setFocusPainted(false);
        btnSauvegarder.addActionListener(e -> sauvegarderConfiguration());
        
        // Bouton Fermer
        btnFermer = new JButton("Fermer");
        btnFermer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnFermer.setBackground(new Color(231, 76, 60));
        btnFermer.setForeground(Color.WHITE);
        btnFermer.setFocusPainted(false);
        btnFermer.addActionListener(e -> dispose());
        
        panelBoutons.add(btnTester);
        panelBoutons.add(btnNettoyer);
        panelBoutons.add(btnSauvegarder);
        panelBoutons.add(btnFermer);
        
        mainPanel.add(panelBoutons);
        
        this.add(mainPanel, BorderLayout.CENTER);
        this.setVisible(true);
        
        // Mettre à jour l'état initial des champs
        mettreAJourEtatChamps();
    }
    
    private void mettreAJourEtatChamps() {
        boolean actives = chkActiverNotifications.isSelected();
        spinnerIntervalle.setEnabled(actives);
        btnTester.setEnabled(actives);
    }
    
    private void sauvegarderConfiguration() {
        boolean actives = chkActiverNotifications.isSelected();
        int intervalle = (int) spinnerIntervalle.getValue();
        
        notificationManager.setNotificationsActives(actives);
        notificationManager.setVerificationInterval(intervalle);
        
        JOptionPane.showMessageDialog(this,
            "Configuration sauvegardée avec succès !\n\n" +
            "Notifications : " + (actives ? "ACTIVÉES" : "DÉSACTIVÉES") + "\n" +
            "Vérification : toutes les " + intervalle + " minute(s)",
            "Configuration sauvegardée",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    public void dispose() {
        super.dispose();
    }
}
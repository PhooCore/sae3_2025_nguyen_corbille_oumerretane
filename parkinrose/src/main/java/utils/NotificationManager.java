package utils;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

/**
 * Gestionnaire global de notifications pour les stationnements
 * Fonctionne indépendamment de la page actuellement ouverte
 */
public class NotificationManager {
    
    private static NotificationManager instance;
    private ScheduledExecutorService scheduler;
    private List<NotificationListener> listeners;
    private Preferences prefs;
    private boolean notificationsActives;
    private int verificationIntervalMinutes = 1;
    
    // Interface pour les écouteurs de notifications
    public interface NotificationListener {
        void onNotification(String titre, String message, NotificationType type);
    }
    
    // Types de notifications
    public enum NotificationType {
        INFO("Information", new Color(41, 128, 185), UIManager.getIcon("OptionPane.informationIcon")),
        WARNING("Avertissement", new Color(243, 156, 18), UIManager.getIcon("OptionPane.warningIcon")),
        ERROR("Erreur", new Color(231, 76, 60), UIManager.getIcon("OptionPane.errorIcon")),
        SUCCESS("Succès", new Color(46, 204, 113), UIManager.getIcon("OptionPane.informationIcon"))
        ;
        
        private final String titre;
        private final Color couleur;
        private final Icon icone;
        
        NotificationType(String titre, Color couleur, Icon icone) {
            this.titre = titre;
            this.couleur = couleur;
            this.icone = icone;
        }
        
        public String getTitre() { return titre; }
        public Color getCouleur() { return couleur; }
        public Icon getIcone() { return icone; }
    }
    
    // Singleton privé
    private NotificationManager() {
        this.listeners = new ArrayList<>();
        this.prefs = Preferences.userNodeForPackage(NotificationManager.class);
        this.notificationsActives = prefs.getBoolean("notifications_actives", true);
        this.verificationIntervalMinutes = prefs.getInt("verification_interval", 1);
        initialiserScheduler();
    }
    
    // Méthode singleton
    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }
    
    private void initialiserScheduler() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
            
            // Vérifier à intervalle configuré
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    verifierNotifications();
                } catch (Exception e) {
                    System.err.println("Erreur dans le scheduler de notifications: " + e.getMessage());
                }
            }, 0, verificationIntervalMinutes, TimeUnit.MINUTES);
        }
    }
    
    /**
     * Vérifie les notifications périodiquement
     */
    private void verifierNotifications() {
        if (!notificationsActives) {
            return;
        }
        
        // Cette méthode sera appelée par les pages qui veulent vérifier
        // Le contrôle réel se fait via les méthodes publiques
    }
    
    /**
     * Vérifie un stationnement spécifique pour les notifications
     */
    public void verifierStationnement(modele.Stationnement stationnement) {
        if (!notificationsActives || stationnement == null) {
            return;
        }
        
        // Vérifier que c'est un stationnement en voirie
        if (!stationnement.estVoirie()) {
            return;
        }
        
        // Vérifier que le stationnement est actif
        if (!"ACTIF".equals(stationnement.getStatut())) {
            return;
        }
        
        // Vérifier qu'il y a une date de fin
        if (stationnement.getDateFin() == null) {
            return;
        }
        
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime dateFin = stationnement.getDateFin();
        
        // Calculer le temps restant en minutes
        long minutesRestantes = ChronoUnit.MINUTES.between(maintenant, dateFin);
        
        // Vérifier si le temps est écoulé
        if (minutesRestantes < 0) {
            envoyerNotification(
                "⛔ Stationnement expiré",
                "Votre stationnement en voirie pour " + stationnement.getPlaqueImmatriculation() + 
                " est expiré depuis " + Math.abs(minutesRestantes) + " minute(s).\n" +
                "Zone: " + stationnement.getIdTarification(),
                NotificationType.ERROR
            );
            return;
        }
        
        // Afficher la notification seulement si 10 minutes ou moins restent
        if (minutesRestantes > 0 && minutesRestantes <= 10) {
            String message = formatMessageNotification(minutesRestantes, stationnement);
            NotificationType type = minutesRestantes <= 3 ? NotificationType.WARNING : NotificationType.INFO;
            
            envoyerNotification(
                minutesRestantes <= 3 ? "⚠ Dernières minutes !" : "⏰ Rappel stationnement",
                message,
                type
            );
        }
    }
    
    private String formatMessageNotification(long minutesRestantes, modele.Stationnement stationnement) {
        String vehicule = stationnement.getPlaqueImmatriculation();
        String zone = stationnement.getIdTarification();
        
        if (minutesRestantes == 1) {
            return "Il reste <b>1 minute</b> pour votre stationnement en voirie.\n\n" +
                   "Véhicule: " + vehicule + "\n" +
                   "Zone: " + zone;
        } else if (minutesRestantes <= 3) {
            return "Il reste seulement <b>" + minutesRestantes + " minutes</b> pour votre stationnement en voirie.\n\n" +
                   "Véhicule: " + vehicule + "\n" +
                   "Zone: " + zone;
        } else {
            return "Il reste <b>" + minutesRestantes + " minutes</b> pour votre stationnement en voirie.\n\n" +
                   "Véhicule: " + vehicule + "\n" +
                   "Zone: " + zone;
        }
    }
    
    /**
     * Envoie une notification à tous les écouteurs
     */
    public void envoyerNotification(String titre, String message, NotificationType type) {
        // Éviter les doublons de notifications trop rapprochées
        String key = "notification_" + titre.hashCode() + "_" + message.hashCode();
        long dernierEnvoi = prefs.getLong(key, 0);
        long maintenant = System.currentTimeMillis();
        
        // Ne pas renvoyer la même notification avant 5 minutes
        if (maintenant - dernierEnvoi < 5 * 60 * 1000) {
            return;
        }
        
        prefs.putLong(key, maintenant);
        
        // Notifier tous les écouteurs dans le thread EDT
        SwingUtilities.invokeLater(() -> {
            for (NotificationListener listener : new ArrayList<>(listeners)) {
                try {
                    listener.onNotification(titre, message, type);
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
                }
            }
            
            // Afficher une notification système si aucun écouteur n'est présent
            if (listeners.isEmpty()) {
                afficherNotificationSysteme(titre, message, type);
            }
        });
    }
    
    /**
     * Affiche une notification système
     */
    private void afficherNotificationSysteme(String titre, String message, NotificationType type) {
        try {
            // Afficher un popup dans le thread EDT
            afficherPopupNotificationSimple(titre, message, type);
            
        } catch (Exception e) {
            System.err.println("Impossible d'afficher la notification système: " + e.getMessage());
        }
    }
    
    /**
     * Affiche une notification popup SIMPLE sans effets d'opacité
     */
    public void afficherPopupNotificationSimple(String titre, String message, NotificationType type) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Version simple sans transparence
                JDialog dialog = new JDialog((Frame) null, "Notification", false);
                dialog.setAlwaysOnTop(true);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                
                JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
                mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
                mainPanel.setBackground(Color.WHITE);
                
                // En-tête avec icône
                JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
                headerPanel.setBackground(Color.WHITE);
                
                if (type.getIcone() != null) {
                    JLabel iconLabel = new JLabel(type.getIcone());
                    headerPanel.add(iconLabel, BorderLayout.WEST);
                }
                
                JLabel titleLabel = new JLabel(titre);
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                titleLabel.setForeground(type.getCouleur());
                headerPanel.add(titleLabel, BorderLayout.CENTER);
                
                // Bouton fermer
                JButton closeButton = new JButton("×");
                closeButton.setFont(new Font("Arial", Font.BOLD, 14));
                closeButton.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                closeButton.setBackground(Color.WHITE);
                closeButton.setForeground(Color.GRAY);
                closeButton.setFocusPainted(false);
                closeButton.addActionListener(e -> dialog.dispose());
                headerPanel.add(closeButton, BorderLayout.EAST);
                
                mainPanel.add(headerPanel, BorderLayout.NORTH);
                
                // Message
                JTextArea messageArea = new JTextArea(message.replace("<b>", "").replace("</b>", ""));
                messageArea.setEditable(false);
                messageArea.setLineWrap(true);
                messageArea.setWrapStyleWord(true);
                messageArea.setBackground(Color.WHITE);
                messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                messageArea.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                
                JScrollPane scrollPane = new JScrollPane(messageArea);
                scrollPane.setBorder(null);
                scrollPane.setPreferredSize(new Dimension(350, 80));
                mainPanel.add(scrollPane, BorderLayout.CENTER);
                
                // Pied de page
                JPanel footerPanel = new JPanel(new BorderLayout());
                footerPanel.setBackground(Color.WHITE);
                
                JLabel timeLabel = new JLabel("Il y a quelques instants");
                timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                timeLabel.setForeground(Color.GRAY);
                timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                footerPanel.add(timeLabel, BorderLayout.CENTER);
                
                JButton okButton = new JButton("OK");
                okButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                okButton.setBackground(new Color(240, 240, 240));
                okButton.setFocusPainted(false);
                okButton.addActionListener(e -> dialog.dispose());
                footerPanel.add(okButton, BorderLayout.EAST);
                
                mainPanel.add(footerPanel, BorderLayout.SOUTH);
                
                dialog.add(mainPanel);
                dialog.pack();
                
                // Positionner en bas à droite de l'écran
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int x = screenSize.width - dialog.getWidth() - 20;
                int y = screenSize.height - dialog.getHeight() - 40;
                dialog.setLocation(x, y);
                
                // Son d'avertissement
                Toolkit.getDefaultToolkit().beep();
                
                // Fermer automatiquement après 15 secondes
                Timer closeTimer = new Timer(15000, e -> dialog.dispose());
                closeTimer.setRepeats(false);
                closeTimer.start();
                
                dialog.setVisible(true);
                
            } catch (Exception e) {
                System.err.println("Erreur lors de l'affichage de la notification: " + e.getMessage());
                // Version de secours ultra simple
                JOptionPane.showMessageDialog(null, 
                    "<html><b>" + titre + "</b><br>" + message + "</html>", 
                    "Notification", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
    
    /**
     * Affiche une notification popup (version alternative)
     * Utilise JWindow au lieu de JDialog pour éviter les problèmes
     */
    public void afficherPopupNotification(String titre, String message, NotificationType type) {
        afficherPopupNotificationSimple(titre, message, type);
    }
    
    /**
     * Affiche une notification type "toast" sans fenêtre modale
     */
    public void afficherToastNotification(String titre, String message, NotificationType type) {
        SwingUtilities.invokeLater(() -> {
            try {
                JWindow toast = new JWindow();
                toast.setAlwaysOnTop(true);
                
                JPanel panel = new JPanel(new BorderLayout(10, 10));
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(type.getCouleur(), 2),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
                panel.setBackground(Color.WHITE);
                
                // Titre
                JLabel titleLabel = new JLabel(titre);
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                titleLabel.setForeground(type.getCouleur());
                panel.add(titleLabel, BorderLayout.NORTH);
                
                // Message
                JTextArea messageArea = new JTextArea(message);
                messageArea.setEditable(false);
                messageArea.setLineWrap(true);
                messageArea.setWrapStyleWord(true);
                messageArea.setBackground(Color.WHITE);
                messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                messageArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
                
                panel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
                
                toast.add(panel);
                toast.pack();
                
                // Positionner en bas à droite
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int x = screenSize.width - toast.getWidth() - 20;
                int y = screenSize.height - toast.getHeight() - 40;
                toast.setLocation(x, y);
                
                // Son
                Toolkit.getDefaultToolkit().beep();
                
                toast.setVisible(true);
                
                // Fermer automatiquement après 8 secondes
                Timer closeTimer = new Timer(8000, e -> toast.dispose());
                closeTimer.setRepeats(false);
                closeTimer.start();
                
            } catch (Exception e) {
                System.err.println("Erreur lors de l'affichage du toast: " + e.getMessage());
                afficherPopupNotificationSimple(titre, message, type);
            }
        });
    }
    
    /**
     * Enregistre un écouteur de notifications
     */
    public void addNotificationListener(NotificationListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Retire un écouteur de notifications
     */
    public void removeNotificationListener(NotificationListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Active/désactive les notifications
     */
    public void setNotificationsActives(boolean actives) {
        this.notificationsActives = actives;
        prefs.putBoolean("notifications_actives", actives);
        
        if (actives && (scheduler == null || scheduler.isShutdown())) {
            initialiserScheduler();
        } else if (!actives && scheduler != null) {
            scheduler.shutdown();
        }
    }
    
    /**
     * Définit l'intervalle de vérification
     */
    public void setVerificationInterval(int minutes) {
        this.verificationIntervalMinutes = minutes;
        prefs.putInt("verification_interval", minutes);
        
        // Redémarrer le scheduler avec le nouvel intervalle
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            scheduler = null;
        }
        if (notificationsActives) {
            initialiserScheduler();
        }
    }
    
    /**
     * Vérifie si les notifications sont actives
     */
    public boolean areNotificationsActives() {
        return notificationsActives;
    }
    
    /**
     * Retourne l'intervalle de vérification
     */
    public int getVerificationInterval() {
        return verificationIntervalMinutes;
    }
    
    /**
     * Arrête proprement le gestionnaire
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Nettoie l'historique des notifications
     */
    public void clearNotificationHistory() {
        try {
            prefs.clear();
        } catch (Exception e) {
            System.err.println("Erreur lors du nettoyage de l'historique: " + e.getMessage());
        }
    }
    
    /**
     * Teste les notifications
     */
    public void testerNotifications() {
        // Utiliser la version simple pour le test
        afficherPopupNotificationSimple(
            "🔔 Test de notification",
            "Ceci est une notification de test pour vérifier que le système fonctionne correctement.\n\n" +
            "Les notifications s'afficheront automatiquement lorsqu'il restera 10 minutes ou moins pour vos stationnements en voirie.",
            NotificationType.INFO
        );
    }
}
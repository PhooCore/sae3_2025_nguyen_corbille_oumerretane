package controleur;

import ihm.Page_Stationnement_En_Cours;
import modele.Stationnement;
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Contrôleur pour les notifications de stationnement en voirie
 * Affiche un pop-up lorsqu'il reste 10 minutes ou moins
 */
public class ControleurNotificationStationnement {
    
    private Page_Stationnement_En_Cours vue;
    private ScheduledExecutorService scheduler;
    private boolean notificationAffichee = false;
    private LocalDateTime derniereNotification = null;
    
    public ControleurNotificationStationnement(Page_Stationnement_En_Cours vue) {
        this.vue = vue;
        this.scheduler = Executors.newScheduledThreadPool(1);
        demarrerSurveillance();
    }
    
    /**
     * Démarre la surveillance du temps restant pour le stationnement
     */
    private void demarrerSurveillance() {
        // Vérifier toutes les minutes
        scheduler.scheduleAtFixedRate(() -> {
            try {
                verifierTempsRestant();
            } catch (Exception e) {
                System.err.println("Erreur lors de la vérification du temps restant: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.MINUTES); // Vérification toutes les minutes
    }
    
    /**
     * Vérifie le temps restant et affiche une notification si nécessaire
     */
    private void verifierTempsRestant() {
        if (vue == null || vue.getStationnementActif() == null) {
            return;
        }
        
        Stationnement stationnement = vue.getStationnementActif();
        
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
        
        // Afficher la notification seulement si 10 minutes ou moins restent
        if (minutesRestantes > 0 && minutesRestantes <= 10) {
            // Éviter d'afficher la notification trop souvent
            if (derniereNotification != null) {
                long minutesDepuisDerniereNotification = ChronoUnit.MINUTES.between(derniereNotification, maintenant);
                if (minutesDepuisDerniereNotification < 5) { // Attendre au moins 5 minutes entre les notifications
                    return;
                }
            }
            
            afficherNotification(minutesRestantes, stationnement);
            derniereNotification = maintenant;
        }
    }
    
    /**
     * Affiche une notification à l'utilisateur
     */
    private void afficherNotification(long minutesRestantes, Stationnement stationnement) {
        // Exécuter dans le thread EDT de Swing
        SwingUtilities.invokeLater(() -> {
            if (vue.isVisible()) {
                String message = formatMessageNotification(minutesRestantes, stationnement);
                
                // Créer une fenêtre de dialogue personnalisée pour plus de visibilité
                JDialog dialog = new JDialog(vue, "Rappel stationnement", true);
                dialog.setLayout(new BorderLayout());
                
                // Panel principal
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                
                // Icone d'avertissement
                JLabel iconLabel = new JLabel();
                iconLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
                iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(iconLabel);
                
                panel.add(Box.createRigidArea(new Dimension(0, 15)));
                
                // Message
                JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
                messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(messageLabel);
                
                // Détails supplémentaires
                JLabel detailsLabel = new JLabel("<html><div style='text-align: center; font-size: 10px; color: gray;'>" +
                    "Véhicule: " + stationnement.getPlaqueImmatriculation() + "<br>" +
                    "Zone: " + stationnement.getIdTarification() + "</div></html>");
                detailsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(detailsLabel);
                
                panel.add(Box.createRigidArea(new Dimension(0, 20)));
                
                // Bouton OK
                JButton okButton = new JButton("OK");
                okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                okButton.addActionListener(e -> dialog.dispose());
                
                panel.add(okButton);
                
                dialog.add(panel);
                dialog.pack();
                dialog.setLocationRelativeTo(vue);
                dialog.setVisible(true);
                
                // Jouer un son d'avertissement
                try {
                    Toolkit.getDefaultToolkit().beep();
                } catch (Exception e) {
                    // Ignorer si le son ne peut pas être joué
                }
                
                // Mettre à jour le flag pour éviter les notifications multiples
                notificationAffichee = true;
                
                // Réinitialiser le flag après un certain temps
                new Timer(5 * 60 * 1000, e -> notificationAffichee = false).start(); // 5 minutes
            }
        });
    }
    
    /**
     * Formate le message de notification selon le temps restant
     */
    private String formatMessageNotification(long minutesRestantes, Stationnement stationnement) {
        String vehicule = stationnement.getTypeVehicule() + " (" + stationnement.getPlaqueImmatriculation() + ")";
        
        if (minutesRestantes == 1) {
            return "<b>Attention !</b><br><br>" +
                   "Il reste <b>1 minute</b> pour votre stationnement en voirie.<br>" +
                   "Véhicule: " + vehicule;
        } else if (minutesRestantes <= 3) {
            return "<b>Attention !</b><br><br>" +
                   "Il reste seulement <b>" + minutesRestantes + " minutes</b> pour votre stationnement en voirie.<br>" +
                   "Véhicule: " + vehicule;
        } else {
            return "<b>Rappel de stationnement</b><br><br>" +
                   "Il reste <b>" + minutesRestantes + " minutes</b> pour votre stationnement en voirie.<br>" +
                   "Véhicule: " + vehicule;
        }
    }
    
    /**
     * Force la vérification immédiate du temps restant
     * Utile après un rafraîchissement manuel
     */
    public void verifierMaintenant() {
        verifierTempsRestant();
    }
    
    /**
     * Arrête la surveillance
     */
    public void arreterSurveillance() {
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
     * Démarre la surveillance (si elle a été arrêtée)
     */
    public void demarrer() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
            demarrerSurveillance();
        }
    }
}
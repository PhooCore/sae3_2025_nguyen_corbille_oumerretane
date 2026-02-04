package controleur;

import ihm.Page_Gestion_Feedback;
import ihm.Page_Administration;
import modele.Feedback;
import modele.Usager;
import modele.dao.FeedbackDAO;
import modele.dao.UsagerDAO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur gérant l'interface d'administration des feedbacks utilisateurs.
 * Permet aux administrateurs de consulter les messages des utilisateurs,
 * y répondre, changer leur statut (nouveau, en cours, résolu) et consulter
 * l'historique complet des échanges.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Gestion_Feedback
 * et les modèles (Feedback, Usager, FeedbackDAO).
 * 
 * @author Équipe 7
 */
public class ControleurGestionFeedback implements ActionListener, ListSelectionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur de gestion des feedbacks.
     * Permet de suivre le cycle de vie du traitement des messages utilisateurs.
     */
    private enum Etat {
        /** État initial au démarrage du contrôleur */
        INITIAL,
        /** Chargement des informations de l'administrateur connecté */
        CHARGEMENT_ADMIN,
        /** Chargement de la liste des feedbacks depuis la base de données */
        CHARGEMENT_FEEDBACKS,
        /** Un message a été sélectionné et ses détails sont affichés */
        MESSAGE_SELECTIONNE,
        /** L'administrateur est en train de saisir une réponse */
        SAISIE_REPONSE,
        /** Modification du statut d'un message en cours */
        MODIFICATION_STATUT,
        /** Envoi d'une réponse à l'utilisateur en cours */
        ENVOI_REPONSE,
        /** Actualisation de la liste des feedbacks en cours */
        ACTUALISATION,
        /** Retour à la page d'administration principale */
        RETOUR_ADMINISTRATION,
        /** Une erreur s'est produite */
        ERREUR
    }
    
    private Page_Gestion_Feedback vue;
    private Etat etat;
    private String emailAdmin;
    private Usager admin;
    private List<Feedback> feedbacksList;
    private Feedback feedbackSelectionne;
    
    private static final int LONGUEUR_MIN_REPONSE = 10;
    private static final int LONGUEUR_MAX_REPONSE = 2000;
    
    /**
     * Constructeur du contrôleur de gestion des feedbacks.
     * Initialise le contrôleur avec la vue associée et vérifie les droits d'administration.
     * 
     * @param vue la page d'interface graphique de gestion des feedbacks
     */
    public ControleurGestionFeedback(Page_Gestion_Feedback vue) {
        this.vue = vue;
        this.emailAdmin = vue.getEmailAdmin();
        this.etat = Etat.INITIAL;
        
        initialiserControleur();
    }
    
    /**
     * Initialise le contrôleur en chargeant les informations de l'administrateur,
     * configurant les écouteurs et chargeant la liste des feedbacks.
     * En cas d'erreur, gère l'erreur d'initialisation.
     */
    private void initialiserControleur() {
        try {
            chargerInformationsAdmin();
            configurerListeners();
            chargerFeedbacks();
            etat = Etat.CHARGEMENT_FEEDBACKS;
        } catch (Exception e) {
            gererErreurInitialisation("Erreur initialisation: " + e.getMessage());
        }
    }
    
    /**
     * Charge les informations de l'administrateur connecté depuis la base de données
     * et vérifie qu'il possède bien les droits d'administration.
     * 
     * @throws Exception si l'administrateur n'est pas trouvé ou n'a pas les droits requis
     */
    private void chargerInformationsAdmin() throws Exception {
        this.admin = UsagerDAO.getUsagerByEmail(emailAdmin);
        
        if (this.admin == null) {
            throw new Exception("Administrateur non trouvé");
        }
        
        if (!admin.isAdmin()) {
            throw new Exception("Accès administrateur requis");
        }
    }
    
    /**
     * Configure tous les écouteurs d'événements pour les composants interactifs de la vue.
     * Connecte les boutons d'action, la table de feedbacks et le filtre.
     */
    private void configurerListeners() {
        vue.getBtnRetour().addActionListener(this);
        vue.getBtnMarquerEnCours().addActionListener(this);
        vue.getBtnMarquerResolu().addActionListener(this);
        vue.getBtnRepondre().addActionListener(this);
        
        vue.getTableFeedbacks().getSelectionModel().addListSelectionListener(this);
        
        vue.getComboFiltre().addActionListener(this);
    }
    
    /**
     * Gère les événements d'action des composants de la vue.
     * Route les actions vers les méthodes appropriées en fonction de l'état actuel
     * et de la source de l'événement.
     * 
     * @param e l'événement d'action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        switch (etat) {
            case CHARGEMENT_FEEDBACKS:
            case MESSAGE_SELECTIONNE:
            case SAISIE_REPONSE:
                if (source == vue.getBtnRetour()) {
                    retourAdministration();
                } else if (source == vue.getBtnMarquerEnCours()) {
                    changerStatut("EN_COURS");
                } else if (source == vue.getBtnMarquerResolu()) {
                    changerStatut("RESOLU");
                } else if (source == vue.getBtnRepondre()) {
                    envoyerReponse();
                } else if (source == vue.getComboFiltre()) {
                    filtrerFeedbacks();
                }
                break;
                
            case MODIFICATION_STATUT:
            case ENVOI_REPONSE:
            case ACTUALISATION:
                break;
                
            case ERREUR:
                if (source == vue.getBtnRetour()) {
                    retourAdministration();
                }
                break;
        }
    }
    
    /**
     * Gère les événements de sélection dans la table des feedbacks.
     * Affiche les détails du message sélectionné si l'état le permet.
     * 
     * @param e l'événement de sélection
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && etat != Etat.MODIFICATION_STATUT && 
            etat != Etat.ENVOI_REPONSE && etat != Etat.ACTUALISATION) {
            afficherDetailsSelection();
        }
    }
    
    /**
     * Charge tous les feedbacks depuis la base de données et les affiche dans la table.
     * Met à jour le titre avec le nombre de nouveaux messages non traités.
     */
    private void chargerFeedbacks() {
        try {
            feedbacksList = FeedbackDAO.getAllFeedbacksWithInfo();
            vue.getTableModel().setRowCount(0);
            feedbackSelectionne = null;
            
            if (feedbacksList == null || feedbacksList.isEmpty()) {
                JOptionPane.showMessageDialog(vue,
                    "Aucun message à afficher.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
            
            for (Feedback feedback : feedbacksList) {
                String statut = getStatutAvecIcone(feedback.getStatut());
                String utilisateur = feedback.getPrenomUsager() + " " + feedback.getNomUsager();
                String date = feedback.getDateCreation().format(formatter);
                String repondu = feedback.isRepondu() ? "O" : "X";
                
                vue.getTableModel().addRow(new Object[]{
                    feedback.getIdFeedback(),
                    statut,
                    feedback.getSujet(),
                    utilisateur,
                    date,
                    repondu
                });
            }
            
            mettreAJourTitre();
            
        } catch (Exception ex) {
            gererErreur("Erreur chargement feedbacks: " + ex.getMessage());
        }
    }
    
    /**
     * Convertit le statut d'un feedback en texte lisible avec indication visuelle.
     * 
     * @param statut le code du statut (NOUVEAU, EN_COURS, RESOLU)
     * @return le statut formaté pour affichage
     */
    private String getStatutAvecIcone(String statut) {
        if (statut == null) return "INCONNU";
        
        switch (statut) {
            case "NOUVEAU": return "NOUVEAU";
            case "EN_COURS": return "EN COURS";
            case "RESOLU": return "RÉSOLU";
            default: return statut;
        }
    }
    
    /**
     * Met à jour le titre de la fenêtre avec le nombre de nouveaux feedbacks non traités.
     */
    private void mettreAJourTitre() {
        int nouveaux = FeedbackDAO.getNombreNouveauxFeedbacks();
        if (nouveaux > 0) {
            vue.setTitle("Gestion des Feedbacks - " + nouveaux + " nouveau" + (nouveaux > 1 ? "x" : ""));
        }
    }
    
    /**
     * Filtre la liste des feedbacks selon le critère sélectionné dans le ComboBox.
     * Les filtres disponibles sont : "Tous les messages", "Nouveaux", "En cours", "Résolus".
     * Met à jour l'affichage de la table après filtrage.
     */
    private void filtrerFeedbacks() {
        String filtre = (String) vue.getComboFiltre().getSelectedItem();
        
        if (feedbacksList == null) return;
        
        vue.getTableModel().setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        
        for (Feedback feedback : feedbacksList) {
            boolean afficher = true;
            
            if (filtre != null) {
                switch (filtre) {
                    case "Nouveaux":
                        afficher = "NOUVEAU".equals(feedback.getStatut());
                        break;
                    case "En cours":
                        afficher = "EN_COURS".equals(feedback.getStatut());
                        break;
                    case "Résolus":
                        afficher = "RESOLU".equals(feedback.getStatut());
                        break;
                }
            }
            
            if (afficher || "Tous les messages".equals(filtre) || filtre == null) {
                String statut = getStatutAvecIcone(feedback.getStatut());
                String utilisateur = feedback.getPrenomUsager() != null && feedback.getNomUsager() != null ?
                    feedback.getPrenomUsager() + " " + feedback.getNomUsager() :
                    "Utilisateur #" + feedback.getIdUsager();
                String date = feedback.getDateCreation() != null ?
                    feedback.getDateCreation().format(formatter) : "Date inconnue";
                String repondu = feedback.isRepondu() ? "O" : "X";
                
                vue.getTableModel().addRow(new Object[]{
                    feedback.getIdFeedback(),
                    statut,
                    feedback.getSujet(),
                    utilisateur,
                    date,
                    repondu
                });
            }
        }
    }
    
    /**
     * Affiche les détails complets du feedback sélectionné dans la table.
     * Charge les informations de l'utilisateur, le message original et l'historique des réponses.
     * Active les boutons d'action après sélection réussie.
     */
    private void afficherDetailsSelection() {
        int selectedRow = vue.getTableFeedbacks().getSelectedRow();
        if (selectedRow == -1) {
            desactiverBoutons();
            effacerDetails();
            return;
        }
        
        try {
            Object idObj = vue.getTableModel().getValueAt(selectedRow, 0);
            if (!(idObj instanceof Integer)) {
                afficherMessageErreur("Format d'ID invalide", "Erreur");
                return;
            }
            
            int idFeedback = (Integer) idObj;
            feedbackSelectionne = FeedbackDAO.getFeedbackById(idFeedback);
            
            if (feedbackSelectionne == null) {
                afficherMessageErreur("Impossible de charger les détails du message.", "Erreur");
                return;
            }
            
            etat = Etat.MESSAGE_SELECTIONNE;
            
            activerBoutons();
            afficherInformationsFeedback();
            chargerHistorique();
            
        } catch (Exception ex) {
            gererErreur("Erreur affichage détails: " + ex.getMessage());
        }
    }
    
    /**
     * Active les boutons d'action sur le feedback sélectionné.
     */
    private void activerBoutons() {
        vue.getBtnMarquerEnCours().setEnabled(true);
        vue.getBtnMarquerResolu().setEnabled(true);
        vue.getBtnRepondre().setEnabled(true);
    }
    
    /**
     * Désactive les boutons d'action lorsqu'aucun feedback n'est sélectionné.
     */
    private void desactiverBoutons() {
        vue.getBtnMarquerEnCours().setEnabled(false);
        vue.getBtnMarquerResolu().setEnabled(false);
        vue.getBtnRepondre().setEnabled(false);
    }
    
    /**
     * Efface tous les détails affichés dans la zone de détails du feedback.
     */
    private void effacerDetails() {
        vue.getLblUserInfo().setText("Utilisateur : ");
        vue.getLblDateInfo().setText("Date : ");
        vue.getLblStatut().setText("Statut : ");
        vue.getLblSujetInfo().setText("Sujet : ");
        vue.getTxtMessageDetail().setText("");
        vue.getTxtHistorique().setText("");
        vue.getTxtReponse().setText("");
    }
    
    /**
     * Affiche les informations principales du feedback sélectionné :
     * utilisateur, date de création, statut, sujet et message.
     */
    private void afficherInformationsFeedback() {
        if (feedbackSelectionne == null) return;
        
        try {
            Usager usager = UsagerDAO.getUsagerByEmail(feedbackSelectionne.getMailUsager());
            String nomUtilisateur;
            
            if (usager != null && usager.getPrenomUsager() != null && usager.getNomUsager() != null) {
                nomUtilisateur = usager.getPrenomUsager() + " " + usager.getNomUsager() + 
                               " (" + (feedbackSelectionne.getMailUsager() != null ? feedbackSelectionne.getMailUsager() : "sans email") + ")";
            } else {
                nomUtilisateur = "Utilisateur #" + feedbackSelectionne.getIdUsager();
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String dateStr = feedbackSelectionne.getDateCreation() != null ?
                feedbackSelectionne.getDateCreation().format(formatter) : "Date inconnue";
            
            vue.getLblUserInfo().setText("Utilisateur : " + nomUtilisateur);
            vue.getLblDateInfo().setText("Date : " + dateStr);
            vue.getLblStatut().setText("Statut : " + getStatutAvecIcone(feedbackSelectionne.getStatut()));
            vue.getLblSujetInfo().setText("Sujet : " + feedbackSelectionne.getSujet());
            
            vue.getTxtMessageDetail().setText(feedbackSelectionne.getMessage() != null ? 
                feedbackSelectionne.getMessage() : "Message non disponible");
            
        } catch (Exception ex) {
            vue.getLblUserInfo().setText("Utilisateur : Erreur chargement");
            vue.getTxtMessageDetail().setText("Erreur lors du chargement des informations.");
        }
    }
    
    /**
     * Charge et affiche l'historique complet des réponses administrateur
     * pour le feedback sélectionné.
     * Affiche un message si aucune réponse n'existe encore.
     */
    private void chargerHistorique() {
        if (feedbackSelectionne == null) return;
        
        List<Feedback> reponses = FeedbackDAO.getReponsesFeedback(feedbackSelectionne.getIdFeedback());
        
        if (reponses == null || reponses.isEmpty()) {
            vue.getTxtHistorique().setText("Aucune réponse pour le moment.");
            return;
        }
        
        StringBuilder historique = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Feedback reponse : reponses) {
            String date = reponse.getDateCreation() != null ?
                reponse.getDateCreation().format(formatter) : "Date inconnue";
            
            String nomAdmin = "Administrateur";
            
            if (reponse.getIdAdminReponse() != null) {
                try {
                    Usager admin = UsagerDAO.getUsagerByEmail(String.valueOf(reponse.getIdAdminReponse()));
                    if (admin != null && admin.getPrenomUsager() != null && admin.getNomUsager() != null) {
                        nomAdmin = admin.getPrenomUsager() + " " + admin.getNomUsager();
                    }
                } catch (Exception ex) {
                    // Continuer avec le nom par défaut
                }
            }
            
            historique.append("[").append(date).append("] ").append(nomAdmin).append(" :\n")
                     .append(reponse.getMessage() != null ? reponse.getMessage() : "Message non disponible")
                     .append("\n\n");
        }
        
        vue.getTxtHistorique().setText(historique.toString());
        vue.getTxtHistorique().setCaretPosition(0);
    }
    
    /**
     * Change le statut d'un feedback après confirmation de l'administrateur.
     * Les statuts possibles sont : "EN_COURS" ou "RESOLU".
     * Met à jour l'affichage dans la table et les détails après modification.
     * 
     * @param nouveauStatut le nouveau statut à appliquer
     */
    private void changerStatut(String nouveauStatut) {
        if (feedbackSelectionne == null) {
            afficherMessageErreur("Veuillez sélectionner un message.", "Aucune sélection");
            return;
        }
        
        etat = Etat.MODIFICATION_STATUT;
        
        String messageConfirmation = "Êtes-vous sûr de vouloir marquer ce message comme ";
        if ("EN_COURS".equals(nouveauStatut)) {
            messageConfirmation += "en cours ?";
        } else {
            messageConfirmation += "résolu ?";
        }
        
        int confirmation = JOptionPane.showConfirmDialog(
            vue,
            messageConfirmation,
            "Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmation != JOptionPane.YES_OPTION) {
            etat = Etat.MESSAGE_SELECTIONNE;
            return;
        }
        
        boolean gotanswer = "RESOLU".equals(nouveauStatut);
        boolean success = FeedbackDAO.mettreAJourStatut(feedbackSelectionne.getIdFeedback(), 
                                                       nouveauStatut, gotanswer);
        
        if (success) {
            int selectedRow = vue.getTableFeedbacks().getSelectedRow();
            if (selectedRow != -1) {
                vue.getTableModel().setValueAt(getStatutAvecIcone(nouveauStatut), selectedRow, 1);
                vue.getTableModel().setValueAt(gotanswer ? "O" : "X", selectedRow, 5);
            }
            
            vue.getLblStatut().setText("Statut : " + getStatutAvecIcone(nouveauStatut));
            
            afficherMessageSucces("Statut mis à jour avec succès.");
            
            feedbackSelectionne.setStatut(nouveauStatut);
            feedbackSelectionne.setGotanswer(gotanswer);
            
        } else {
            afficherMessageErreur("Erreur lors de la mise à jour du statut.", "Erreur");
        }
        
        etat = Etat.MESSAGE_SELECTIONNE;
    }
    
    /**
     * Envoie une réponse au feedback sélectionné après validation du contenu.
     * Vérifie que la réponse respecte les contraintes de longueur minimale et maximale.
     * Met à jour automatiquement le statut du feedback à "EN_COURS" et recharge l'historique.
     */
    private void envoyerReponse() {
        if (feedbackSelectionne == null) {
            afficherMessageErreur("Veuillez sélectionner un message.", "Aucune sélection");
            return;
        }
        
        String reponse = vue.getTxtReponse().getText().trim();
        if (reponse.isEmpty()) {
            afficherMessageErreur("Veuillez saisir une réponse.", "Réponse vide");
            return;
        }
        
        if (reponse.length() < LONGUEUR_MIN_REPONSE) {
            afficherMessageErreur(
                String.format("La réponse doit contenir au moins %d caractères.", LONGUEUR_MIN_REPONSE),
                "Réponse trop courte"
            );
            return;
        }
        
        if (reponse.length() > LONGUEUR_MAX_REPONSE) {
            afficherMessageErreur(
                String.format("La réponse ne peut pas dépasser %d caractères.", LONGUEUR_MAX_REPONSE),
                "Réponse trop longue"
            );
            return;
        }
        
        etat = Etat.SAISIE_REPONSE;
        
        int confirmation = JOptionPane.showConfirmDialog(
            vue,
            "Envoyer cette réponse à l'utilisateur ?",
            "Confirmation d'envoi",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmation != JOptionPane.YES_OPTION) {
            etat = Etat.MESSAGE_SELECTIONNE;
            return;
        }
        
        etat = Etat.ENVOI_REPONSE;
        
        try {
            boolean success = FeedbackDAO.repondreFeedback(feedbackSelectionne.getIdFeedback(), 
                                                          admin.getIdUsager(), reponse);
            
            if (success) {
                int selectedRow = vue.getTableFeedbacks().getSelectedRow();
                if (selectedRow != -1) {
                    vue.getTableModel().setValueAt(getStatutAvecIcone("EN_COURS"), selectedRow, 1);
                    vue.getTableModel().setValueAt("O", selectedRow, 5);
                }
                
                vue.getTxtReponse().setText("");
                
                chargerHistorique();
                
                feedbackSelectionne.setStatut("EN_COURS");
                feedbackSelectionne.setGotanswer(true);
                
                afficherMessageSucces("Réponse envoyée avec succès.");
            } else {
                afficherMessageErreur("Erreur lors de l'envoi de la réponse.", "Erreur");
            }
            
        } catch (Exception ex) {
            gererErreur("Erreur envoi réponse: " + ex.getMessage());
        }
        
        etat = Etat.MESSAGE_SELECTIONNE;
    }
    
    /**
     * Actualise la liste complète des feedbacks en rechargeant les données
     * depuis la base de données. Efface la sélection et les détails affichés.
     */
    private void actualiserFeedbacks() {
        etat = Etat.ACTUALISATION;
        chargerFeedbacks();
        effacerDetails();
        desactiverBoutons();
        afficherMessageSucces("Liste des messages actualisée.");
        etat = Etat.CHARGEMENT_FEEDBACKS;
    }
    
    /**
     * Retourne à la page d'administration principale après confirmation
     * si une réponse est en cours de saisie.
     * Ferme la page actuelle et rouvre la page d'administration.
     */
    private void retourAdministration() {
        etat = Etat.RETOUR_ADMINISTRATION;
        
        if (etat == Etat.SAISIE_REPONSE || etat == Etat.MODIFICATION_STATUT) {
            int confirmation = JOptionPane.showConfirmDialog(
                vue,
                "Une réponse est en cours de saisie.\n" +
                "Êtes-vous sûr de vouloir retourner à l'administration ?\n" +
                "Les modifications non enregistrées seront perdues.",
                "Confirmation de retour",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirmation != JOptionPane.YES_OPTION) {
                etat = Etat.MESSAGE_SELECTIONNE;
                return;
            }
        }
        
        vue.dispose();
        
        SwingUtilities.invokeLater(() -> {
            Page_Administration pageAdmin = new Page_Administration(emailAdmin);
            pageAdmin.setVisible(true);
        });
    }
    
    /**
     * Ferme la page de gestion des feedbacks.
     */
    private void fermerPage() {
        vue.dispose();
    }
    
    /**
     * Affiche un message d'erreur dans une boîte de dialogue.
     * 
     * @param message le message d'erreur à afficher
     * @param titre le titre de la boîte de dialogue
     */
    private void afficherMessageErreur(String message, String titre) {
        JOptionPane.showMessageDialog(vue, message, titre, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Affiche un message de succès dans une boîte de dialogue.
     * 
     * @param message le message de succès à afficher
     */
    private void afficherMessageSucces(String message) {
        JOptionPane.showMessageDialog(vue, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Gère une erreur survenue pendant l'utilisation du contrôleur.
     * Affiche un message d'erreur et passe à l'état ERREUR.
     * 
     * @param message la description de l'erreur
     */
    private void gererErreur(String message) {
        System.err.println(message);
        afficherMessageErreur(message, "Erreur");
        etat = Etat.ERREUR;
    }
    
    /**
     * Gère une erreur critique survenue lors de l'initialisation.
     * Affiche un message d'erreur et ferme la page.
     * 
     * @param message la description de l'erreur d'initialisation
     */
    private void gererErreurInitialisation(String message) {
        System.err.println("Erreur initialisation: " + message);
        afficherMessageErreur(message, "Erreur d'initialisation");
        vue.dispose();
    }
    
    /**
     * Retourne l'état actuel du contrôleur.
     * Utile pour le débogage et les tests.
     * 
     * @return l'état actuel du contrôleur
     */
    public Etat getEtat() {
        return etat;
    }
    
    /**
     * Retourne l'administrateur connecté.
     * Utile pour le débogage et les tests.
     * 
     * @return l'administrateur connecté
     */
    public Usager getAdmin() {
        return admin;
    }
}
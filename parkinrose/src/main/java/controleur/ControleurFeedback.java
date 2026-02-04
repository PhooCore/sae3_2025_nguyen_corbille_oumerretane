package controleur;

import ihm.Page_Feedback;
import ihm.Page_Principale;
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
 * Contrôleur gérant la messagerie de feedback entre les utilisateurs et l'administration.
 * Permet aux utilisateurs de consulter leurs conversations, envoyer de nouveaux messages,
 * et visualiser les réponses de l'équipe ParkinRose.
 * Implémente le pattern MVC en coordonnant les interactions entre la vue Page_Feedback
 * et les modèles (Feedback, Usager, FeedbackDAO).
 * 
 * @author Équipe 7
 */
public class ControleurFeedback implements ActionListener, ListSelectionListener {
    
    /**
     * Énumération des différents états possibles du contrôleur de feedback.
     * Permet de suivre le cycle de vie de la messagerie et les opérations en cours.
     */
    private enum Etat {
        /** État initial au démarrage du contrôleur */
        INITIAL,
        /** Chargement des informations de l'utilisateur depuis la base de données */
        CHARGEMENT_UTILISATEUR,
        /** Chargement de la liste des feedbacks de l'utilisateur */
        CHARGEMENT_FEEDBACKS,
        /** Une conversation a été sélectionnée et ses détails sont affichés */
        CONVERSATION_SELECTIONNEE,
        /** L'utilisateur est en train de saisir un nouveau message */
        SAISIE_NOUVEAU_MESSAGE,
        /** Envoi d'un nouveau message en cours */
        ENVOI_MESSAGE,
        /** Actualisation de la liste des feedbacks en cours */
        ACTUALISATION,
        /** Redirection vers une autre page en cours */
        REDIRECTION,
        /** Une erreur s'est produite */
        ERREUR
    }
    
    private Page_Feedback vue;
    private Etat etat;
    private String emailUtilisateur;
    private Usager usager;
    private List<Feedback> feedbacksList;
    private Feedback feedbackSelectionne;
    
    private static final int LONGUEUR_MIN_SUJET = 3;
    private static final int LONGUEUR_MIN_MESSAGE = 10;
    private static final int LONGUEUR_MAX_SUJET = 100;
    private static final int LONGUEUR_MAX_MESSAGE = 1000;
    
    /**
     * Constructeur du contrôleur de feedback.
     * Initialise le contrôleur avec la vue associée et charge les données de l'utilisateur.
     * 
     * @param vue la page d'interface graphique de feedback
     */
    public ControleurFeedback(Page_Feedback vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.etat = Etat.INITIAL;
        
        initialiserControleur();
    }
    
    /**
     * Initialise le contrôleur en chargeant les informations utilisateur,
     * configurant les écouteurs et chargeant les feedbacks existants.
     * En cas d'erreur, gère l'erreur d'initialisation.
     */
    private void initialiserControleur() {
        try {
            chargerInformationsUtilisateur();
            configurerListeners();
            chargerFeedbacks();
            etat = Etat.CHARGEMENT_FEEDBACKS;
        } catch (Exception e) {
            gererErreurInitialisation("Erreur initialisation: " + e.getMessage());
        }
    }
    
    /**
     * Charge les informations de l'utilisateur depuis la base de données.
     * Si l'utilisateur n'est pas trouvé, crée un objet Usager temporaire.
     * Met à jour l'affichage du nom de l'utilisateur dans la vue.
     * 
     * @throws Exception si une erreur survient lors du chargement
     */
    private void chargerInformationsUtilisateur() throws Exception {
        etat = Etat.CHARGEMENT_UTILISATEUR;
        
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        
        if (this.usager == null) {
            this.usager = new Usager();
            this.usager.setMailUsager(emailUtilisateur);
            this.usager.setIdUsager(-1);
        }
        
        String nomUser = usager.getPrenomUsager() != null ? 
            usager.getPrenomUsager() + " " + usager.getNomUsager() : 
            emailUtilisateur;
        vue.getLblUser().setText(nomUser);
    }
    
    /**
     * Configure tous les écouteurs d'événements pour les composants interactifs de la vue.
     * Connecte les boutons, la table de conversations et le filtre aux actions appropriées.
     */
    private void configurerListeners() {
        vue.getBtnFermer().addActionListener(this);
        vue.getBtnEnvoyerMessage().addActionListener(this);
        vue.getBtnEffacer().addActionListener(this);
        
        vue.getTableFeedbacks().getSelectionModel().addListSelectionListener(this);
        
        vue.getComboFiltre().addActionListener(this);
    }
    
    /**
     * Gère les événements d'action des composants de la vue.
     * Route les actions vers les méthodes appropriées en fonction de l'état actuel
     * et de la source de l'événement (boutons, filtre).
     * 
     * @param e l'événement d'action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        switch (etat) {
            case CHARGEMENT_FEEDBACKS:
            case CONVERSATION_SELECTIONNEE:
            case SAISIE_NOUVEAU_MESSAGE:
                if (source == vue.getBtnFermer()) {
                    fermerPage();
                } else if (source == vue.getBtnEnvoyerMessage()) {
                    envoyerNouveauMessage();
                } else if (source == vue.getBtnEffacer()) {
                    effacerFormulaire();
                } else if (source == vue.getComboFiltre()) {
                    filtrerFeedbacks();
                }
                break;
                
            case ENVOI_MESSAGE:
            case ACTUALISATION:
                break;
                
            case ERREUR:
                if (source == vue.getBtnFermer()) {
                    fermerPage();
                }
                break;
        }
    }
    
    /**
     * Gère les événements de sélection dans la table des conversations.
     * Affiche les détails de la conversation sélectionnée si l'état le permet.
     * 
     * @param e l'événement de sélection
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && etat != Etat.ENVOI_MESSAGE && etat != Etat.ACTUALISATION) {
            afficherDetailsConversation();
        }
    }
    
    /**
     * Charge tous les feedbacks de l'utilisateur depuis la base de données
     * et met à jour l'affichage dans la table.
     * Affiche un message si aucune conversation n'existe.
     */
    private void chargerFeedbacks() {
        try {
            feedbacksList = FeedbackDAO.getFeedbacksByUser(usager.getIdUsager());
            
            vue.getTableModel().setRowCount(0);
            feedbackSelectionne = null;
            
            if (feedbacksList == null || feedbacksList.isEmpty()) {
                vue.getTableModel().addRow(new Object[]{"✉️", "Vous n'avez pas encore de conversation", ""});
                mettreAJourInfoConversation();
                return;
            }
            
            for (Feedback feedback : feedbacksList) {
                String statutIcon = getIconeStatut(feedback);
                String conversationInfo = getConversationInfo(feedback);
                String derniereActivite = getDerniereActivite(feedback);
                
                vue.getTableModel().addRow(new Object[]{statutIcon, conversationInfo, derniereActivite});
            }
            
            mettreAJourInfoConversation();
            
        } catch (Exception e) {
            gererErreur("Erreur chargement feedbacks: " + e.getMessage());
        }
    }
    
    /**
     * Retourne l'icône de statut d'un feedback.
     * 
     * @param feedback le feedback dont on veut l'icône
     * @return "✓" si répondu, "●" si en attente
     */
    private String getIconeStatut(Feedback feedback) {
        return feedback.isRepondu() ? "✓" : "●";
    }
    
    /**
     * Génère l'information de conversation formatée en HTML pour affichage dans la table.
     * Inclut le sujet, le statut et un aperçu du message.
     * 
     * @param feedback le feedback dont on veut les informations
     * @return une chaîne HTML formatée contenant les détails de la conversation
     */
    private String getConversationInfo(Feedback feedback) {
        String snippet = feedback.getMessage().length() > 70 ? 
            feedback.getMessage().substring(0, 70) + "..." : 
            feedback.getMessage();
        
        String statutTexte = feedback.isRepondu() ? 
            "<font color='green' size='-1'><b>RÉPONDU</b></font>" : 
            "<font color='orange' size='-1'><b>EN ATTENTE</b></font>";
        
        return "<html><b>" + feedback.getSujet() + "</b><br>" + 
               "<font color='#666666' size='-1'>" + statutTexte + " • " + snippet + "</font></html>";
    }
    
    /**
     * Retourne la date et l'heure de la dernière activité d'un feedback formatées en HTML.
     * Si des réponses existent, retourne la date de la dernière réponse, sinon la date de création.
     * 
     * @param feedback le feedback dont on veut la dernière activité
     * @return une chaîne HTML formatée avec la date et l'heure
     */
    private String getDerniereActivite(Feedback feedback) {
        List<Feedback> reponses = FeedbackDAO.getReponsesFeedback(feedback.getIdFeedback());
        if (reponses != null && !reponses.isEmpty()) {
            Feedback derniere = reponses.get(reponses.size() - 1);
            return "<html><font size='-1'>" + 
                   derniere.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM")) + 
                   "<br><font color='gray'>" + 
                   derniere.getDateCreation().format(DateTimeFormatter.ofPattern("HH:mm")) + 
                   "</font></font></html>";
        }
        return "<html><font size='-1'>" + 
               feedback.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM")) + 
               "<br><font color='gray'>" + 
               feedback.getDateCreation().format(DateTimeFormatter.ofPattern("HH:mm")) + 
               "</font></font></html>";
    }
    
    /**
     * Met à jour le label affichant le nombre total de conversations.
     */
    private void mettreAJourInfoConversation() {
        int total = vue.getTableModel().getRowCount();
        String info = total + " conversation" + (total > 1 ? "s" : "");
        vue.getLblInfoConversation().setText(info);
    }
    
    /**
     * Filtre la liste des conversations selon le critère sélectionné dans le ComboBox.
     * Les filtres disponibles sont : "Toutes mes conversations", "En attente", "Répondu".
     * Met à jour l'affichage de la table après filtrage.
     */
    private void filtrerFeedbacks() {
        String filtre = (String) vue.getComboFiltre().getSelectedItem();
        vue.getTableModel().setRowCount(0);
        
        if (feedbacksList == null) return;
        
        for (Feedback feedback : feedbacksList) {
            boolean afficher = true;
            
            switch (filtre) {
                case "En attente":
                    afficher = !feedback.isRepondu();
                    break;
                case "Répondu":
                    afficher = feedback.isRepondu();
                    break;
            }
            
            if (afficher || filtre.equals("Toutes mes conversations")) {
                String statutIcon = getIconeStatut(feedback);
                String conversationInfo = getConversationInfo(feedback);
                String derniereActivite = getDerniereActivite(feedback);
                
                vue.getTableModel().addRow(new Object[]{statutIcon, conversationInfo, derniereActivite});
            }
        }
        
        mettreAJourInfoConversation();
    }
    
    /**
     * Affiche les détails de la conversation sélectionnée dans la table.
     * Identifie le feedback correspondant en tenant compte du filtre actif,
     * puis charge et affiche ses détails complets.
     */
    private void afficherDetailsConversation() {
        int selectedRow = vue.getTableFeedbacks().getSelectedRow();
        if (selectedRow == -1) {
            feedbackSelectionne = null;
            return;
        }
        
        String filtre = (String) vue.getComboFiltre().getSelectedItem();
        int indexFiltre = 0;
        
        for (int i = 0; i < feedbacksList.size(); i++) {
            Feedback feedback = feedbacksList.get(i);
            boolean afficher = true;
            
            if (!filtre.equals("Toutes mes conversations")) {
                switch (filtre) {
                    case "En attente":
                        afficher = !feedback.isRepondu();
                        break;
                    case "Répondu":
                        afficher = feedback.isRepondu();
                        break;
                }
            }
            
            if (afficher) {
                if (indexFiltre == selectedRow) {
                    feedbackSelectionne = feedback;
                    break;
                }
                indexFiltre++;
            }
        }
        
        if (feedbackSelectionne == null) {
            return;
        }
        
        etat = Etat.CONVERSATION_SELECTIONNEE;
        chargerDetailsConversation();
    }
    
    /**
     * Charge et affiche les détails complets de la conversation sélectionnée.
     * Affiche le message original avec son sujet, sa date et son statut.
     */
    private void chargerDetailsConversation() {
        if (feedbackSelectionne == null) {
            return;
        }
        
        String dateCreation = feedbackSelectionne.getDateCreation().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
        
        String statutTexte = feedbackSelectionne.isRepondu() ? 
            "Répondu" : "En attente de réponse";
        
        vue.getTxtMessageDetail().setText("Sujet : " + feedbackSelectionne.getSujet() + "\n" +
                                         "Date : " + dateCreation + "\n" +
                                         "Statut : " + statutTexte + "\n\n" +
                                         "Votre message :\n" + feedbackSelectionne.getMessage());
        
        chargerHistoriqueConversation();
    }
    
    /**
     * Charge et affiche l'historique complet des réponses de l'équipe ParkinRose
     * pour la conversation sélectionnée.
     * Affiche un message si aucune réponse n'existe encore.
     */
    private void chargerHistoriqueConversation() {
        List<Feedback> reponses = FeedbackDAO.getReponsesFeedback(feedbackSelectionne.getIdFeedback());
        
        if (reponses == null || reponses.isEmpty()) {
            vue.getTxtHistorique().setText("Aucune réponse pour le moment.\n\n" +
                                          "L'équipe ParkinRose vous répondra dans les plus brefs délais.");
            return;
        }
        
        StringBuilder historique = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        
        historique.append("Réponses de l'équipe ParkinRose :\n\n");
        
        for (Feedback reponse : reponses) {
            String date = reponse.getDateCreation().format(formatter);
            
            historique.append("------------------------------------\n")
                     .append(date).append("\n\n")
                     .append(reponse.getMessage()).append("\n\n");
        }
        
        vue.getTxtHistorique().setText(historique.toString());
        vue.getTxtHistorique().setCaretPosition(0);
    }
    
    /**
     * Traite l'envoi d'un nouveau message de feedback.
     * Valide le formulaire avant d'envoyer le message.
     */
    private void envoyerNouveauMessage() {
        etat = Etat.SAISIE_NOUVEAU_MESSAGE;
        
        if (!validerFormulaire()) {
            return;
        }
        
        etat = Etat.ENVOI_MESSAGE;
        traiterEnvoiMessage();
    }
    
    /**
     * Valide le formulaire de nouveau message.
     * Vérifie que le sujet et le message respectent les contraintes de longueur minimale et maximale.
     * 
     * @return true si le formulaire est valide, false sinon avec affichage d'un message d'erreur
     */
    private boolean validerFormulaire() {
        String sujet = vue.getTxtSujetNouveau().getText().trim();
        String message = vue.getTxtNouveauMessage().getText().trim();
        
        if (sujet.isEmpty()) {
            afficherMessageErreur("Veuillez saisir un sujet.", "Sujet vide");
            return false;
        }
        
        if (sujet.length() < LONGUEUR_MIN_SUJET) {
            afficherMessageErreur(
                String.format("Le sujet doit contenir au moins %d caractères.", LONGUEUR_MIN_SUJET),
                "Sujet trop court"
            );
            return false;
        }
        
        if (sujet.length() > LONGUEUR_MAX_SUJET) {
            afficherMessageErreur(
                String.format("Le sujet ne peut pas dépasser %d caractères.", LONGUEUR_MAX_SUJET),
                "Sujet trop long"
            );
            return false;
        }
        
        if (message.isEmpty()) {
            afficherMessageErreur("Veuillez saisir un message.", "Message vide");
            return false;
        }
        
        if (message.length() < LONGUEUR_MIN_MESSAGE) {
            afficherMessageErreur(
                String.format("Le message doit contenir au moins %d caractères.", LONGUEUR_MIN_MESSAGE),
                "Message trop court"
            );
            return false;
        }
        
        if (message.length() > LONGUEUR_MAX_MESSAGE) {
            afficherMessageErreur(
                String.format("Le message ne peut pas dépasser %d caractères.", LONGUEUR_MAX_MESSAGE),
                "Message trop long"
            );
            return false;
        }
        
        return true;
    }
    
    /**
     * Traite l'envoi effectif du message validé vers la base de données.
     * Affiche une confirmation en cas de succès et actualise la liste des feedbacks.
     */
    private void traiterEnvoiMessage() {
        try {
            String sujet = vue.getTxtSujetNouveau().getText().trim();
            String message = vue.getTxtNouveauMessage().getText().trim();
            
            boolean success = FeedbackDAO.envoyerFeedback(usager.getIdUsager(), sujet, message);
            
            if (success) {
                afficherConfirmationEnvoi();
                effacerFormulaire();
                actualiserFeedbacks();
                etat = Etat.CHARGEMENT_FEEDBACKS;
            } else {
                gererErreur("Erreur lors de l'envoi du message.");
            }
            
        } catch (Exception e) {
            gererErreur("Erreur envoi message: " + e.getMessage());
        }
    }
    
    /**
     * Affiche une boîte de dialogue de confirmation après l'envoi réussi d'un message.
     * Présente un résumé formaté du sujet et du message envoyé.
     */
    private void afficherConfirmationEnvoi() {
        String message = String.format(
            "<html><div style='text-align: center;'>"
            + "<h2 style='color: green;'>Message envoyé !</h2>"
            + "<p>Votre message a été envoyé avec succès à notre équipe.</p>"
            + "<br>"
            + "<div style='background-color: #f0f8ff; padding: 15px; border-radius: 5px; text-align: left;'>"
            + "<p><b>Sujet :</b> %s</p>"
            + "<p><b>Message :</b> %s</p>"
            + "</div>"
            + "<p style='color: #666;'>Vous recevrez une réponse dans les plus brefs délais.</p>"
            + "</div></html>",
            vue.getTxtSujetNouveau().getText().trim(),
            vue.getTxtNouveauMessage().getText().trim().substring(0, Math.min(100, vue.getTxtNouveauMessage().getText().trim().length())) + 
            (vue.getTxtNouveauMessage().getText().trim().length() > 100 ? "..." : "")
        );
        
        JOptionPane.showMessageDialog(
            vue,
            message,
            "Message envoyé",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Efface le contenu des champs du formulaire de nouveau message.
     */
    private void effacerFormulaire() {
        vue.getTxtSujetNouveau().setText("");
        vue.getTxtNouveauMessage().setText("");
    }
    
    /**
     * Actualise la liste des feedbacks en rechargeant les données depuis la base de données.
     * Réinitialise également l'affichage des détails de conversation.
     */
    private void actualiserFeedbacks() {
        etat = Etat.ACTUALISATION;
        chargerFeedbacks();
        vue.getTxtMessageDetail().setText("");
        vue.getTxtHistorique().setText("");
        etat = Etat.CHARGEMENT_FEEDBACKS;
    }
    
    /**
     * Gère la fermeture de la page de feedback.
     * Demande confirmation à l'utilisateur avant de fermer.
     */
    private void fermerPage() {
        etat = Etat.REDIRECTION;
        
        int confirmation = JOptionPane.showConfirmDialog(
            vue,
            "Êtes-vous sûr de vouloir fermer la messagerie ?",
            "Confirmation de fermeture",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmation == JOptionPane.YES_OPTION) {
            retourPagePrincipale();
        } else {
            etat = Etat.CHARGEMENT_FEEDBACKS;
        }
    }
    
    /**
     * Ferme la page de feedback et retourne à la page précédente.
     */
    private void retourPagePrincipale() {
        try {
            vue.dispose();
        } catch (Exception e) {
            gererErreur("Erreur fermeture: " + e.getMessage());
        }
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
     * Retourne l'utilisateur connecté.
     * Utile pour le débogage et les tests.
     * 
     * @return l'utilisateur connecté
     */
    public Usager getUsager() {
        return usager;
    }
}